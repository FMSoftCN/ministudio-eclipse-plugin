/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.fmsoft.hybridos.mstudio.importWizards;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProjectProperties;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.dialogs.WorkingSetGroup;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.StatusUtil;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.datatransfer.ILeveledImportStructureProvider;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.datatransfer.ArchiveFileManipulations;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.datatransfer.TarEntry;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.datatransfer.TarException;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.datatransfer.TarFile;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.datatransfer.TarLeveledStructureProvider;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.datatransfer.ZipLeveledStructureProvider;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.datatransfer.FileSystemStructureProvider;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.datatransfer.ImportOperation;

/**
 * The MStudioExtenalImportProjectWizardPage is the page that allows the user to import
 * projects from a particular location.
 */
public class MStudioExtenalImportProjectWizardPage extends WizardPage implements IOverwriteQuery {

	/**
	 * The name of the folder containing metadata information for the workspace.
	 */
	public static final String METADATA_FOLDER = ".metadata";
	private static final String PROJECT_FILE_NAME = ".hproject";
	/**
	 * The import structure provider.
	 */
	private ILeveledImportStructureProvider structureProvider;
	
	// dialog store id constants
	private final static String STORE_COPY_PROJECT_ID = "MStudioExtenalImportProjectPage.STORE_COPY_PROJECT_ID"; //$NON-NLS-1$
	private final static String STORE_ARCHIVE_SELECTED = "MStudioExtenalImportProjectPage.STORE_ARCHIVE_SELECTED"; //$NON-NLS-1$
	private Text directoryPathField;
	private CheckboxTreeViewer projectsList;
	private Button copyCheckbox;
	private boolean copyFiles = false;
	private boolean lastCopyFiles = false;
	private ProjectRecord[] selectedProjects = new ProjectRecord[0];
	// Keep track of the directory that we browsed to last time
	// the wizard was invoked.
	private static String previouslyBrowsedDirectory = "";
	// Keep track of the archive that we browsed to last time
	// the wizard was invoked.
	private static String previouslyBrowsedArchive = "";
	private Button projectFromDirectoryRadio;
	private Button projectFromArchiveRadio;
	private Text archivePathField;
	private Button browseDirectoriesButton;
	private Button browseArchivesButton;
	private IProject[] wsProjects;
	// constant from WizardArchiveFileResourceImportPage1
	private static final String[] FILE_IMPORT_MASK = {"*.jar;*.zip;*.tar;*.tar.gz;*.tgz", "*.*" };
	private String initialPath;
	private String lastPath;
	// The last time that the file or folder at the selected path was modified
	// to mimize searches
	private long lastModified;
	private WorkingSetGroup workingSetGroup;
	private IStructuredSelection currentSelection;
	private final class ProjectLabelProvider extends LabelProvider implements IColorProvider{
		public String getText(Object element) {
			return ((ProjectRecord) element).getProjectLabel();
		}

		public Color getBackground(Object element) {
			return null;
		}

		public Color getForeground(Object element) {
			ProjectRecord projectRecord = (ProjectRecord) element;
			if(projectRecord.hasConflicts)
				return getShell().getDisplay().getSystemColor(SWT.COLOR_GRAY);
			return null;
		}
	}
	/**
	 * Class declared public only for test suite.
	 * 
	 */
	public class ProjectRecord {
		File projectSystemFile;
		Object projectArchiveFile;
		String projectName;
		Object parent;
		int level;
		boolean hasConflicts;
		IProjectDescription description;

		ProjectRecord(File file) {
			projectSystemFile = file;
			setProjectName();
		}

		/**
		 * @param file
		 * 		The Object representing the .project file
		 * @param parent
		 * 		The parent folder of the .project file
		 * @param level
		 * 		The number of levels deep in the provider the file is
		 */
		ProjectRecord(Object file, Object parent, int level) {
			this.projectArchiveFile = file;
			this.parent = parent;
			this.level = level;
			setProjectName();
		}

		/**
		 * Set the name of the project based on the projectFile.
		 */
		private void setProjectName() {
			try {
				if (projectArchiveFile != null) {
					InputStream stream = structureProvider.getContents(projectArchiveFile);

					// If we can get a description pull the name from there
					if (stream == null) {
						if (projectArchiveFile instanceof ZipEntry) {
							IPath path = new Path(((ZipEntry) projectArchiveFile).getName());
							projectName = path.segment(path.segmentCount() - 2);
						} else if (projectArchiveFile instanceof TarEntry) {
							IPath path = new Path(((TarEntry) projectArchiveFile).getName());
							projectName = path.segment(path.segmentCount() - 2);
						}
					} else {
						description = IDEWorkbenchPlugin.getPluginWorkspace().loadProjectDescription(stream);
						stream.close();
						projectName = description.getName();
					}

				}

				// If we don't have the project name try again
				if (projectName == null) {
					IPath path = new Path(projectSystemFile.getPath());
					// if the file is in the default location, use the directory
					// name as the project name
					if (isDefaultLocation(path)) {
						projectName = path.segment(path.segmentCount() - 2);
						description = IDEWorkbenchPlugin.getPluginWorkspace().newProjectDescription(projectName);
					} else {
						description = IDEWorkbenchPlugin.getPluginWorkspace().loadProjectDescription(path);
						projectName = description.getName();
					}

				}
			} catch (CoreException e) {
				// no good couldn't get the name
			} catch (IOException e) {
				// no good couldn't get the name
			}
		}

		/**
		 * Returns whether the given project description file path is in the
		 * default location for a project
		 * 
		 * @param path
		 * 		The path to examine
		 * @return Whether the given path is the default location for a project
		 */
		private boolean isDefaultLocation(IPath path) {
			// The project description file must at least be within the project,
			// which is within the workspace location
			if (path.segmentCount() < 2)
				return false;
			return path.removeLastSegments(2).toFile().equals(Platform.getLocation().toFile());
		}

		/**
		 * Get the name of the project
		 * 
		 * @return String
		 */
		public String getProjectName() {
			return projectName;
		}

		/**
		 * Gets the label to be used when rendering this project record in the
		 * UI.
		 * 
		 * @return String the label
		 * @since 3.4
		 */
		public String getProjectLabel() {
			if (description == null)
				return projectName;
			String path = projectSystemFile == null ? structureProvider.getLabel(parent) : projectSystemFile.getParent();
			return NLS.bind(DataTransferMessages.WizardProjectsImportPage_projectLabel,
					projectName, path);
		}
		
		/**
		 * @return Returns the hasConflicts.
		 */
		public boolean hasConflicts() {
			return hasConflicts;
		}
	}

	/**
	 * Creates a new project creation wizard page.
	 * 
	 */
	public MStudioExtenalImportProjectWizardPage() {
		this("wizardExternalProjectsPage", null, null); //$NON-NLS-1$
	}

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param pageName
	 */
	public MStudioExtenalImportProjectWizardPage(String pageName) {
		this(pageName,null, null);
	}
			
	/**
	 * More (many more) parameters.
	 * 
	 * @param pageName
	 * @param initialPath
	 * @param currentSelection
	 * @since 3.5
	 */
	public MStudioExtenalImportProjectWizardPage(String pageName,String initialPath, IStructuredSelection currentSelection) {
 		super(pageName);
		this.initialPath = initialPath;
		this.currentSelection = currentSelection;
		setPageComplete(false);
		setTitle(MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.ImportProjectsTitle"));
		setDescription(MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.ImportProjectsDescription"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		Composite workArea = new Composite(parent, SWT.NONE);
		setControl(workArea);
		workArea.setLayout(new GridLayout());
		workArea.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

		createProjectsRoot(workArea);
		/**
		 * Create the checkbox list for the found projects.
		 * 
		 * @param workArea
		 */
		createProjectsList(workArea);
		/**
		 * Create the area with the extra options.
		 * 
		 * @param workArea
		 */
		createOptionsArea(workArea);
		createWorkingSetGroup(workArea);
		restoreWidgetValues();
		Dialog.applyDialogFont(workArea);
	}

	private void createWorkingSetGroup(Composite workArea) {
		String[] workingSetIds = new String[] {"org.eclipse.ui.resourceWorkingSetPage","org.eclipse.jdt.ui.JavaWorkingSetPage"};
		workingSetGroup = new WorkingSetGroup(workArea, currentSelection, workingSetIds);
	}

	private void createOptionsArea(Composite workArea) {
		Composite optionsGroup = new Composite(workArea, SWT.NONE);
		optionsGroup.setLayout(new GridLayout());
		optionsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		copyCheckbox = new Button(optionsGroup, SWT.CHECK);
		copyCheckbox.setText(MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.CopyProjectsIntoWorkspace"));
		copyCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		copyCheckbox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				copyFiles = copyCheckbox.getSelection();
			}
		});
	}


	private void createProjectsList(Composite workArea) {
		Label title = new Label(workArea, SWT.NONE);
		title.setText(MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.ProjectsListTitle"));

		Composite listComposite = new Composite(workArea, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.makeColumnsEqualWidth = false;
		listComposite.setLayout(layout);
		listComposite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));

		projectsList = new CheckboxTreeViewer(listComposite, SWT.BORDER);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		//gridData.widthHint = new PixelConverter(projectsList.getControl()).convertWidthInCharsToPixels(25);
		//gridData.heightHint = new PixelConverter(projectsList.getControl()).convertHeightInCharsToPixels(10);
		projectsList.getControl().setLayoutData(gridData);
		projectsList.setContentProvider(new ITreeContentProvider() {
			public Object[] getChildren(Object parentElement) {
				return null;
			}
			public Object[] getElements(Object inputElement) {
				return getProjectRecords();
			}
			public boolean hasChildren(Object element) {
				return false;
			}
			public Object getParent(Object element) {
				return null;
			}
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}
		});
		projectsList.setLabelProvider(new ProjectLabelProvider());
		projectsList.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				ProjectRecord element = (ProjectRecord) event.getElement();
				if(element.hasConflicts) {
					projectsList.setChecked(element, false);
				}
				setPageComplete(projectsList.getCheckedElements().length > 0);
			}
		});
		projectsList.setInput(this);
		projectsList.setComparator(new ViewerComparator());
		/**
		 * Create the selection buttons in the listComposite.
		 * 
		 * @param listComposite
		 */
		createSelectionButtons(listComposite);
	}

	
	private void createSelectionButtons(Composite listComposite) {
		Composite buttonsComposite = new Composite(listComposite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		buttonsComposite.setLayout(layout);
		buttonsComposite.setLayoutData(new GridData(
				GridData.VERTICAL_ALIGN_BEGINNING));

		Button selectAll = new Button(buttonsComposite, SWT.PUSH);
		selectAll.setText(MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.selectallButtonText"));
		selectAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				for (int i = 0; i < selectedProjects.length; i++) {
					if(selectedProjects[i].hasConflicts)
						projectsList.setChecked(selectedProjects[i], false);
					else
						projectsList.setChecked(selectedProjects[i], true);
				}
				setPageComplete(projectsList.getCheckedElements().length > 0);
			}
		});
		Dialog.applyDialogFont(selectAll);
		setButtonLayoutData(selectAll);
		
		Button deselectAll = new Button(buttonsComposite, SWT.PUSH);
		deselectAll.setText(MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.deselectAllButtonText"));
		deselectAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				projectsList.setCheckedElements(new Object[0]);
				setPageComplete(false);
			}
		});
		Dialog.applyDialogFont(deselectAll);
		setButtonLayoutData(deselectAll);

		Button refresh = new Button(buttonsComposite, SWT.PUSH);
		refresh.setText(MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.refreshButtonText"));
		refresh.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (projectFromDirectoryRadio.getSelection()) {
					updateProjectsList(directoryPathField.getText().trim());
				} else {
					updateProjectsList(archivePathField.getText().trim());
				}
			}
		});
		Dialog.applyDialogFont(refresh);
		setButtonLayoutData(refresh);
	}

	private void createProjectsRoot(Composite workArea) {
		Composite projectGroup = new Composite(workArea, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.makeColumnsEqualWidth = false;
		layout.marginWidth = 0;
		projectGroup.setLayout(layout);
		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// new project from directory radio button
		projectFromDirectoryRadio = new Button(projectGroup, SWT.RADIO);
		projectFromDirectoryRadio.setText(MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.RootSelectTitle"));
		// project location entry field
		this.directoryPathField = new Text(projectGroup, SWT.BORDER);

		GridData directoryPathData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		//directoryPathData.widthHint = new PixelConverter(directoryPathField).convertWidthInCharsToPixels(25);
		directoryPathField.setLayoutData(directoryPathData);
		
		// browse button
		browseDirectoriesButton = new Button(projectGroup, SWT.PUSH);
		browseDirectoriesButton.setText(MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.browseButtonText"));
		setButtonLayoutData(browseDirectoriesButton);

		// new project from archive radio button
		projectFromArchiveRadio = new Button(projectGroup, SWT.RADIO);
		projectFromArchiveRadio.setText(MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.ArchiveSelectTitle"));
		// project location entry field
		archivePathField = new Text(projectGroup, SWT.BORDER);

		GridData archivePathData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		//archivePathData.widthHint = new PixelConverter(archivePathField).convertWidthInCharsToPixels(25);
		archivePathField.setLayoutData(archivePathData); // browse button
		browseArchivesButton = new Button(projectGroup, SWT.PUSH);
		browseArchivesButton.setText(MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.browseButtonText"));
		setButtonLayoutData(browseArchivesButton);

		projectFromDirectoryRadio.setSelection(true);
		archivePathField.setEnabled(false);
		browseArchivesButton.setEnabled(false);

		browseDirectoriesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleLocationDirectoryButtonPressed();
			}
		});
		browseArchivesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleLocationArchiveButtonPressed();
			}
		});
		directoryPathField.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					e.doit = false;
					updateProjectsList(directoryPathField.getText().trim());
				}
			}
		});
		directoryPathField.addFocusListener(new FocusAdapter() {
			public void focusLost(org.eclipse.swt.events.FocusEvent e) {
				updateProjectsList(directoryPathField.getText().trim());
			}
		});
		archivePathField.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					e.doit = false;
					updateProjectsList(archivePathField.getText().trim());
				}
			}
		});
		archivePathField.addFocusListener(new FocusAdapter() {
			public void focusLost(org.eclipse.swt.events.FocusEvent e) {
				updateProjectsList(archivePathField.getText().trim());
			}
		});
		projectFromDirectoryRadio.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				directoryRadioSelected();
			}
		});

		projectFromArchiveRadio.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				archiveRadioSelected();
			}
		});
	}

	private void archiveRadioSelected() {
		if (projectFromArchiveRadio.getSelection()) {
			directoryPathField.setEnabled(false);
			browseDirectoriesButton.setEnabled(false);
			archivePathField.setEnabled(true);
			browseArchivesButton.setEnabled(true);
			updateProjectsList(archivePathField.getText());
			archivePathField.setFocus();
			copyCheckbox.setSelection(true);
			copyCheckbox.setEnabled(false);
		}
	}

	private void directoryRadioSelected() {
		if (projectFromDirectoryRadio.getSelection()) {
			directoryPathField.setEnabled(true);
			browseDirectoriesButton.setEnabled(true);
			archivePathField.setEnabled(false);
			browseArchivesButton.setEnabled(false);
			updateProjectsList(directoryPathField.getText());
			directoryPathField.setFocus();
			copyCheckbox.setEnabled(true);
			copyCheckbox.setSelection(copyFiles);
		}
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible && this.projectFromDirectoryRadio.getSelection()) {
			this.directoryPathField.setFocus();
		}
		if (visible && this.projectFromArchiveRadio.getSelection()) {
			this.archivePathField.setFocus();
		}
	}

	public void updateProjectsList(final String path) {
		// on an empty path empty selectedProjects
		if (path == null || path.length() == 0) {
			setMessage(MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.ImportProjectsDescription"));
			selectedProjects = new ProjectRecord[0];
			projectsList.refresh(true);
			projectsList.setCheckedElements(selectedProjects);
			setPageComplete(projectsList.getCheckedElements().length > 0);
			lastPath = path;
			return;
		}

		final File directory = new File(path);
		long modified = directory.lastModified();
		if (path.equals(lastPath) && lastModified == modified && lastCopyFiles == copyFiles) {
			// since the file/folder was not modified and the path did not
			// change, no refreshing is required
			return;
		}

		lastPath = path;
		lastModified = modified;
		lastCopyFiles = copyFiles;

		// We can't access the radio button from the inner class so get the
		// status beforehand
		final boolean dirSelected = this.projectFromDirectoryRadio.getSelection();
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {
					monitor.beginTask(MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.SearchingMessage"),100);
					selectedProjects = new ProjectRecord[0];
					Collection<Object> files = new ArrayList<Object>();
					monitor.worked(10);
					if (!dirSelected && ArchiveFileManipulations.isTarFile(path)) {
						TarFile sourceTarFile = getSpecifiedTarSourceFile(path);
						if (sourceTarFile == null) {
							return;
						}

						structureProvider = new TarLeveledStructureProvider(sourceTarFile);
						Object child = structureProvider.getRoot();

						if (!collectProjectFilesFromProvider(files, child, 0,monitor)) 
							return;
						Iterator<Object> filesIterator = files.iterator();
						selectedProjects = new ProjectRecord[files.size()];
						int index = 0;
						monitor.worked(50);
						monitor.subTask(MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.ProcessingMessage"));
						while (filesIterator.hasNext()) {
							selectedProjects[index++] = (ProjectRecord) filesIterator.next();
						}
					} else if (!dirSelected && ArchiveFileManipulations.isZipFile(path)) {
						ZipFile sourceFile = getSpecifiedZipSourceFile(path);
						if (sourceFile == null) {
							return;
						}
						structureProvider = new ZipLeveledStructureProvider(sourceFile);
						Object child = structureProvider.getRoot();

						if (!collectProjectFilesFromProvider(files, child, 0,
								monitor)) {
							return;
						}
						Iterator<Object> filesIterator = files.iterator();
						selectedProjects = new ProjectRecord[files.size()];
						int index = 0;
						monitor.worked(50);
						monitor.subTask(MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.ProcessingMessage"));
						while (filesIterator.hasNext()) {
							selectedProjects[index++] = (ProjectRecord) filesIterator.next();
						}
					}

					else if (dirSelected && directory.isDirectory()) {
						if (!collectProjectFilesFromDirectory(files, directory,	null, monitor)) {
							System.out.println("the file is not exits");
							return;
						}
						Iterator<Object> filesIterator = files.iterator();
						selectedProjects = new ProjectRecord[files.size()];
						int index = 0;
						monitor.worked(50);
						monitor.subTask(MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.ProcessingMessage"));
						while (filesIterator.hasNext()) {
							File file = (File) filesIterator.next();
							selectedProjects[index] = new ProjectRecord(file);
							index++;
						}
					} else {
						monitor.worked(60);
					}
					monitor.done();
				}
			});
		} catch (InvocationTargetException e) {
			IDEWorkbenchPlugin.log(e.getMessage(), e);
		} catch (InterruptedException e) {
		}

		projectsList.refresh(true);
		ProjectRecord[] projects = getProjectRecords();
		boolean displayWarning = false;
		for (int i = 0; i < projects.length; i++) {
			if(projects[i].hasConflicts) {
				displayWarning = true;
				projectsList.setGrayed(projects[i], true);
			}else {
				projectsList.setChecked(projects[i], true);
			}
		}
		
		if (displayWarning) {
			setMessage(MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.projectsInWorkspace"));
		} else {
			setMessage(MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.ImportProjectsDescription"));
		}
		setPageComplete(projectsList.getCheckedElements().length > 0);
		if(selectedProjects.length == 0) {
			setMessage(MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.noProjectsToImport"), WARNING);
		}
	}

	/**
	 * Answer a handle to the zip file currently specified as being the source.
	 * Return null if this file does not exist or is not of valid format.
	 */
	private ZipFile getSpecifiedZipSourceFile(String fileName) {
		if (fileName.length() == 0) {
			return null;
		}
		try {
			return new ZipFile(fileName);
		} catch (ZipException e) {
			displayErrorDialog(MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.ZipImport.badFormat"));
		} catch (IOException e) {
			displayErrorDialog(MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.ZipImport.couldNotRead"));
		}

		archivePathField.setFocus();
		return null;
	}

	/**
	 * Answer a handle to the zip file currently specified as being the source.
	 * Return null if this file does not exist or is not of valid format.
	 */
	private TarFile getSpecifiedTarSourceFile(String fileName) {
		if (fileName.length() == 0) {
			return null;
		}

		try {
			return new TarFile(fileName);
		} catch (TarException e) {
			displayErrorDialog(MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.TarImport.badFormat"));
		} catch (IOException e) {
			displayErrorDialog(MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.ZipImport.couldNotRead"));
		}

		archivePathField.setFocus();
		return null;
	}

	/**
	 * Display an error dialog with the specified message.
	 * 
	 * @param message
	 * 		the error message
	 */
	protected void displayErrorDialog(String message) {
		MessageDialog.openError(getContainer().getShell(),
				getErrorDialogTitle(), message);
	}

	/**
	 * Get the title for an error dialog. Subclasses should override.
	 */
	protected String getErrorDialogTitle() {
		return IDEWorkbenchMessages.WizardExportPage_internalErrorTitle;
	}

	/**
	 * Collect the list of .project files that are under directory into files.
	 * 
	 * @param files
	 * @param directory
	 * @param directoriesVisited
	 * 		Set of canonical paths of directories, used as recursion guard
	 * @param monitor
	 * 		The monitor to report to
	 * @return boolean <code>true</code> if the operation was completed.
	 */
	private boolean collectProjectFilesFromDirectory(Collection<Object> files,
			File directory, Set<String> directoriesVisited, IProgressMonitor monitor) {

		if (monitor.isCanceled()) {
			return false;
		}
		monitor.subTask(NLS.bind(DataTransferMessages.WizardProjectsImportPage_CheckingMessage,
				directory.getPath()));
		File[] contents = directory.listFiles();
		if (contents == null)
			return false;

		// Initialize recursion guard for recursive symbolic links
		if (directoriesVisited == null) {
			directoriesVisited = new HashSet<String>();
			try {
				directoriesVisited.add(directory.getCanonicalPath());
			} catch (IOException exception) {
				StatusManager.getManager().handle(StatusUtil.newStatus(IStatus.ERROR, exception.getLocalizedMessage(), exception));
			}
		}

		// first look for project description files
		final String dotProject = IProjectDescription.DESCRIPTION_FILE_NAME;
		final String hybridProject = MStudioExtenalImportProjectWizardPage.PROJECT_FILE_NAME;
		List<File> projectFiles = new ArrayList<File>();
		for (int i = 0; i < contents.length; i++) {
			File file = contents[i];
			if (file.isFile() && file.getName().equals(dotProject)) {
				files.add(file);
				// don't search sub-directories since we can't have nested
				// projects
				continue;
			}
			if(file.isFile() && file.getName().equals(hybridProject)){
				MStudioProjectProperties mpp = new MStudioProjectProperties(file.getPath());
				if(mpp == null) 
					continue;
				if(MStudioPlugin.getDefault().getMStudioEnvInfo().getCurSoCName().equals(mpp.getProjectSocName())){
					projectFiles.add(file);
					continue;
				}
			}
		}
		if(projectFiles.size() <= 0){
			files.clear();
			return false;
		}
		if(files.size() > 0 && projectFiles.size() > 0)
			return true;
		
		// no project description file and hybridos project description file found, so recurse into sub-directories
		for (int i = 0; i < contents.length; i++) {
			if (contents[i].isDirectory()) {
				if (!contents[i].getName().equals(METADATA_FOLDER)) {
					try {
						String canonicalPath = contents[i].getCanonicalPath();
						if (!directoriesVisited.add(canonicalPath)) {
							// already been here --> do not recurse
							continue;
						}
					} catch (IOException exception) {
						StatusManager.getManager().handle(
								StatusUtil.newStatus(IStatus.ERROR, exception
										.getLocalizedMessage(), exception));

					}
					collectProjectFilesFromDirectory(files, contents[i],
							directoriesVisited, monitor);
				}
			}
		}
		return true;
	}

	/**
	 * Collect the list of .project files that are under directory into files.
	 * 
	 * @param files
	 * @param monitor
	 * 		The monitor to report to
	 * @return boolean <code>true</code> if the operation was completed.
	 */
	@SuppressWarnings("unchecked")
	private boolean collectProjectFilesFromProvider(Collection files,
			Object entry, int level, IProgressMonitor monitor) {

		if (monitor.isCanceled()) {
			return false;
		}
		monitor.subTask(NLS.bind(DataTransferMessages.WizardProjectsImportPage_CheckingMessage,	structureProvider.getLabel(entry)));
		List children = structureProvider.getChildren(entry);
		if (children == null) {
			children = new ArrayList(1);
		}
		Iterator childrenEnum = children.iterator();
		while (childrenEnum.hasNext()) {
			Object child = childrenEnum.next();
			if (structureProvider.isFolder(child)) {
				collectProjectFilesFromProvider(files, child, level + 1, monitor);
			}
			String elementLabel = structureProvider.getLabel(child);
			if (elementLabel.equals(IProjectDescription.DESCRIPTION_FILE_NAME)) {
				if(elementLabel.equals(MStudioExtenalImportProjectWizardPage.PROJECT_FILE_NAME)){
					MStudioProjectProperties mpp = new MStudioProjectProperties(structureProvider.getFullPath(child));
					if(mpp == null) 
						continue;
					if(MStudioPlugin.getDefault().getMStudioEnvInfo().getCurSoCName().equals(mpp.getProjectSocName())){
						files.add(new ProjectRecord(child, entry, level));
					}
				}
			}
		}
		return true;
	}

	/**
	 * The browse button has been selected. Select the location.
	 */
	protected void handleLocationDirectoryButtonPressed() {

		DirectoryDialog dialog = new DirectoryDialog(directoryPathField.getShell());
		dialog.setMessage(MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.SelectDialogTitle"));

		String dirName = directoryPathField.getText().trim();
		if (dirName.length() == 0) {
			dirName = previouslyBrowsedDirectory;
		}

		if (dirName.length() == 0) {
			dialog.setFilterPath(IDEWorkbenchPlugin.getPluginWorkspace()
					.getRoot().getLocation().toOSString());
		} else {
			File path = new File(dirName);
			if (path.exists()) {
				dialog.setFilterPath(new Path(dirName).toOSString());
			}
		}

		String selectedDirectory = dialog.open();
		if (selectedDirectory != null) {
			previouslyBrowsedDirectory = selectedDirectory;
			directoryPathField.setText(previouslyBrowsedDirectory);
			updateProjectsList(selectedDirectory);
		}

	}

	/**
	 * The browse button has been selected. Select the location.
	 */
	protected void handleLocationArchiveButtonPressed() {
		FileDialog dialog = new FileDialog(archivePathField.getShell());
		dialog.setFilterExtensions(FILE_IMPORT_MASK);
		dialog.setText(MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.SelectArchiveDialogTitle"));

		String fileName = archivePathField.getText().trim();
		if (fileName.length() == 0) {
			fileName = previouslyBrowsedArchive;
		}

		if (fileName.length() == 0) {
			dialog.setFilterPath(IDEWorkbenchPlugin.getPluginWorkspace()
					.getRoot().getLocation().toOSString());
		} else {
			File path = new File(fileName).getParentFile();
			if (path != null && path.exists()) {
				dialog.setFilterPath(path.toString());
			}
		}

		String selectedArchive = dialog.open();
		if (selectedArchive != null) {
			previouslyBrowsedArchive = selectedArchive;
			archivePathField.setText(previouslyBrowsedArchive);
			updateProjectsList(selectedArchive);
		}

	}

	/**
	 * Create the selected projects
	 * 
	 * @return boolean <code>true</code> if all project creations were
	 * 	successful.
	 */
	public boolean createProjects() {
		saveWidgetValues();
		
		final Object[] selected = projectsList.getCheckedElements();
		createdProjects = new ArrayList<IProject>();
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
				try {
					monitor.beginTask("", selected.length); //$NON-NLS-1$
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					for (int i = 0; i < selected.length; i++) {
						createExistingProject((ProjectRecord) selected[i],
								new SubProgressMonitor(monitor, 1));
					}
				} finally {
					monitor.done();
				}
			}
		};
		// run the new project creation operation
		try {
			getContainer().run(true, true, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			// one of the steps resulted in a core exception
			Throwable t = e.getTargetException();
			String message = MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.errorMessage");
			IStatus status;
			if (t instanceof CoreException) {
				status = ((CoreException) t).getStatus();
			} else {
				status = new Status(IStatus.ERROR,
						IDEWorkbenchPlugin.IDE_WORKBENCH, 1, message, t);
			}
			ErrorDialog.openError(getShell(), message, null, status);
			return false;
		}
		ArchiveFileManipulations.closeStructureProvider(structureProvider,
				getShell());

		// Adds the projects to the working sets
		addToWorkingSets();
		
		return true;
	}

	List<IProject> createdProjects;
	
	private void addToWorkingSets() {
		
		IWorkingSet[] selectedWorkingSets = workingSetGroup.getSelectedWorkingSets();
		if(selectedWorkingSets == null || selectedWorkingSets.length == 0)
			return; // no Working set is selected
		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
		for (Iterator<IProject> i = createdProjects.iterator(); i.hasNext();) {
			IProject project = (IProject) i.next();
			workingSetManager.addToWorkingSets(project, selectedWorkingSets);
		}
	}

	/**
	 * Performs clean-up if the user cancels the wizard without doing anything
	 */
	public void performCancel() {
		ArchiveFileManipulations.closeStructureProvider(structureProvider, getShell());
	}

	/**
	 * Create the project described in record. If it is successful return true.
	 * 
	 * @param record
	 * @return boolean <code>true</code> if successful
	 * @throws InterruptedException
	 */
	private boolean createExistingProject(final ProjectRecord record, IProgressMonitor monitor) throws InvocationTargetException,InterruptedException {
		String projectName = record.getProjectName();
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProject project = workspace.getRoot().getProject(projectName);
		createdProjects.add(project);
		if (record.description == null) {
			// error case
			record.description = workspace.newProjectDescription(projectName);
			IPath locationPath = new Path(record.projectSystemFile.getAbsolutePath());

			// If it is under the root use the default location
			if (Platform.getLocation().isPrefixOf(locationPath)) {
				record.description.setLocation(null);
			} else {
				record.description.setLocation(locationPath);
			}
		} else {
			record.description.setName(projectName);
		}
		if (record.projectArchiveFile != null) {
			// import from archive
			List<?> fileSystemObjects = structureProvider
					.getChildren(record.parent);
			structureProvider.setStrip(record.level);
			ImportOperation operation = new ImportOperation(project
					.getFullPath(), structureProvider.getRoot(),
					structureProvider, this, fileSystemObjects);
			
			operation.setContext(getShell());
			operation.run(monitor);
			return true;
		}
		// import from file system
		File importSource = null;
		if (copyFiles) {
			// import project from location copying files - use default project
			// location for this workspace
			URI locationURI = record.description.getLocationURI();
			// if location is null, project already exists in this location or
			// some error condition occured.
			if (locationURI != null) {
				// validate the location of the project being copied
				IStatus result = ResourcesPlugin.getWorkspace().validateProjectLocationURI(project, locationURI);
				if(!result.isOK())					
					throw new InvocationTargetException(new CoreException(result));
				importSource = new File(locationURI);
				IProjectDescription desc = workspace.newProjectDescription(projectName);
				desc.setBuildSpec(record.description.getBuildSpec());
				desc.setComment(record.description.getComment());
				desc.setDynamicReferences(record.description.getDynamicReferences());
				desc.setNatureIds(record.description.getNatureIds());
				desc.setReferencedProjects(record.description.getReferencedProjects());
				record.description = desc;
			}
		}

		try {
			monitor.beginTask(MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.CreateProjectsTask"),	100);
			project.create(record.description, new SubProgressMonitor(monitor, 30));
			project.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(monitor, 70));
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		} finally {
			monitor.done();
		}

		// import operation to import project files if copy checkbox is selected
		if (copyFiles && importSource != null) {
			List<?> filesToImport = FileSystemStructureProvider.INSTANCE.getChildren(importSource);
			ImportOperation operation = new ImportOperation(project.getFullPath(), importSource, FileSystemStructureProvider.INSTANCE, this, filesToImport);
			operation.setContext(getShell());
			operation.setOverwriteResources(true); // need to overwrite
			// .project, .classpath
			// files
			operation.setCreateContainerStructure(false);
			operation.run(monitor);
		}

		return true;
	}

	/**
	 * The <code>WizardDataTransfer</code> implementation of this
	 * <code>IOverwriteQuery</code> method asks the user whether the existing
	 * resource at the given path should be overwritten.
	 * 
	 * @param pathString
	 * @return the user's reply: one of <code>"YES"</code>, <code>"NO"</code>,
	 * 	<code>"ALL"</code>, or <code>"CANCEL"</code>
	 */
	public String queryOverwrite(String pathString) {

		Path path = new Path(pathString);

		String messageString;
		// Break the message up if there is a file name and a directory
		// and there are at least 2 segments.
		if (path.getFileExtension() == null || path.segmentCount() < 2) {
			messageString = NLS.bind(IDEWorkbenchMessages.WizardDataTransfer_existsQuestion, pathString);
		} else {
			messageString = NLS.bind(IDEWorkbenchMessages.WizardDataTransfer_overwriteNameAndPathQuestion,
							path.lastSegment(), path.removeLastSegments(1).toOSString());
		}

		final MessageDialog dialog = new MessageDialog(getContainer()
				.getShell(), IDEWorkbenchMessages.Question, null,
				messageString, MessageDialog.QUESTION, new String[] {
						IDialogConstants.YES_LABEL,
						IDialogConstants.YES_TO_ALL_LABEL,
						IDialogConstants.NO_LABEL,
						IDialogConstants.NO_TO_ALL_LABEL,
						IDialogConstants.CANCEL_LABEL }, 0) {
			protected int getShellStyle() {
				return super.getShellStyle();
			}
		};
		String[] response = new String[] { YES, ALL, NO, NO_ALL, CANCEL };
		// run in syncExec because callback is from an operation,
		// which is probably not running in the UI thread.
		getControl().getDisplay().syncExec(new Runnable() {
			public void run() {
				dialog.open();
			}
		});
		return dialog.getReturnCode() < 0 ? CANCEL : response[dialog
				.getReturnCode()];
	}

	/**
	 * Method used for test suite.
	 * 
	 * @return Button the Import from Directory RadioButton
	 */
	public Button getProjectFromDirectoryRadio() {
		return projectFromDirectoryRadio;
	}

	/**
	 * Method used for test suite.
	 * 
	 * @return CheckboxTreeViewer the viewer containing all the projects found
	 */
	public CheckboxTreeViewer getProjectsList() {
		return projectsList;
	}

	/**
	 * Retrieve all the projects in the current workspace.
	 * 
	 * @return IProject[] array of IProject in the current workspace
	 */
	private IProject[] getProjectsInWorkspace() {
		if (wsProjects == null) {
			wsProjects = IDEWorkbenchPlugin.getPluginWorkspace().getRoot()
					.getProjects();
		}
		return wsProjects;
	}

	public ProjectRecord[] getProjectRecords() {
		List<ProjectRecord> projectRecords = new ArrayList<ProjectRecord>();
		for (int i = 0; i < selectedProjects.length; i++) {
			if ( (isProjectInWorkspacePath(selectedProjects[i].getProjectName()) && copyFiles)||
					isProjectInWorkspace(selectedProjects[i].getProjectName())) {
				selectedProjects[i].hasConflicts = true;
			}
			projectRecords.add(selectedProjects[i]);
		}
		return (ProjectRecord[]) projectRecords.toArray(new ProjectRecord[projectRecords.size()]);
	}

	/**
	 * Determine if there is a directory with the project name in the workspace path.
	 * 
	 * @param projectName the name of the project
	 * @return true if there is a directory with the same name of the imported project
	 */
	private boolean isProjectInWorkspacePath(String projectName){
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IPath wsPath = workspace.getRoot().getLocation();
		IPath localProjectPath = wsPath.append(projectName);
		return localProjectPath.toFile().exists();
	}

	/**
	 * Determine if the project with the given name is in the current workspace.
	 * 
	 * @param projectName
	 * 		String the project name to check
	 * @return boolean true if the project with the given name is in this
	 * 	workspace
	 */
	private boolean isProjectInWorkspace(String projectName) {
		if (projectName == null) {
			return false;
		}
		IProject[] workspaceProjects = getProjectsInWorkspace();
		for (int i = 0; i < workspaceProjects.length; i++) {
			if (projectName.equals(workspaceProjects[i].getName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Use the dialog store to restore widget values to the values that they
	 * held last time this wizard was used to completion, or alternatively,
	 * if an initial path is specified, use it to select values.
	 * 
	 * Method declared public only for use of tests.
	 */
	public void restoreWidgetValues() {
				
		// First, check to see if we have resore settings, and
		// take care of the checkbox
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			// checkbox
			copyFiles = settings.getBoolean(STORE_COPY_PROJECT_ID);
			copyCheckbox.setSelection(copyFiles);
			lastCopyFiles = copyFiles;
		}
				
		// Second, check to see if we don't have an initial path, 
		// and if we do have restore settings.  If so, set the
		// radio selection properly to restore settings
		
		if (initialPath==null && settings!=null)
		{
			// radio selection
			boolean archiveSelected = settings
					.getBoolean(STORE_ARCHIVE_SELECTED);
			projectFromDirectoryRadio.setSelection(!archiveSelected);
			projectFromArchiveRadio.setSelection(archiveSelected);
			if (archiveSelected) {
				archiveRadioSelected();
			} else {
				directoryRadioSelected();
			}
		}	
		// Third, if we do have an initial path, set the proper
		// path and radio buttons to the initial value. Move
		// cursor to the end of the path so user can see the
		// most relevant part (directory / archive name)
		else if (initialPath != null) {
			boolean dir = new File(initialPath).isDirectory();

			projectFromDirectoryRadio.setSelection(dir);
			projectFromArchiveRadio.setSelection(!dir);

			if (dir) {
				directoryPathField.setText(initialPath);
				directoryPathField.setSelection(initialPath.length());
				directoryRadioSelected();
			} else {
				archivePathField.setText(initialPath);
				archivePathField.setSelection(initialPath.length());
				archiveRadioSelected();
			}
		}
	}

	/**
	 * Since Finish was pressed, write widget values to the dialog store so that
	 * they will persist into the next invocation of this wizard page.
	 * 
	 * Method declared public only for use of tests.
	 */
	public void saveWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			settings.put(STORE_COPY_PROJECT_ID, copyCheckbox.getSelection());

			settings.put(STORE_ARCHIVE_SELECTED, projectFromArchiveRadio
					.getSelection());
		}
	}

	public Button getCopyCheckbox() {
		return copyCheckbox;
	}
}

