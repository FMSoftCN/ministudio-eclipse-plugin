/**
 * 
 */
package org.minigui.eclipse.cdt.mstudio.wizards;

import java.net.URI;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.CWizardHandler;
import org.eclipse.cdt.ui.wizards.IWizardWithMemory;

import org.minigui.eclipse.cdt.mstudio.MiniGUIMessages;
import org.minigui.eclipse.cdt.mstudio.project.MgProject;
import org.minigui.eclipse.cdt.mstudio.project.MgProjectNature;

public class NewMiniGUIAppWizard extends BasicNewResourceWizard implements
		IExecutableExtension, IWizardWithMemory {
	
	private static final String PREFIX = "CProjectWizard";  
	private static final String title = MiniGUIMessages.getString("MGProjectWizard.op_error.title");
	private static final String message = MiniGUIMessages.getString("MGProjectWizard.op_error.message");
	private static final String[] EMPTY_ARR = new String[0];

	protected IConfigurationElement fConfigElement;
	protected NewMiniGUIAppWizardPage fMainPage;

	protected IProject newProject;
	private String wz_title;
	private String wz_desc;

	private boolean existingPath = false;
	private String lastProjectName = null;
	private URI lastProjectLocation = null;
	private CWizardHandler savedHandler = null;

	public NewMiniGUIAppWizard() {
		this(MiniGUIMessages.getString("NewModelProjectWizard.0"),
				MiniGUIMessages.getString("NewModelProjectWizard.1")); 
	}
	
	public NewMiniGUIAppWizard(String title, String desc) {
		super();
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
		setNeedsProgressMonitor(true);
		setForcePreviousAndNextButtons(true);
		setWindowTitle(title);
		wz_title = title;
		wz_desc = desc;
	}

	public void addPages() {
		fMainPage = new NewMiniGUIAppWizardPage(CUIPlugin.getResourceString(PREFIX));
		fMainPage.setTitle(wz_title);
		fMainPage.setDescription(wz_desc);
		addPage(fMainPage);
	}

	private boolean isChanged() {
		if (savedHandler != fMainPage.h_selected)
			return true;

		if (!fMainPage.getProjectName().equals(lastProjectName))
			return true;

		URI projectLocation = fMainPage.getProjectLocation();
		if (projectLocation == null) {
			if (lastProjectLocation != null)
				return true;
		} else if (!projectLocation.equals(lastProjectLocation))
			return true;

		return savedHandler.isChanged();
	}

	public IProject getProject(boolean defaults) {
		return getProject(defaults, true);
	}

	public IProject getProject(boolean defaults, boolean onFinish) {
		if (newProject != null && isChanged())
			clearProject();
		if (newProject == null) {
			existingPath = false;
			try {
				IFileStore fs;
				URI p = fMainPage.getProjectLocation();
				if (p == null) {
					fs = EFS.getStore(ResourcesPlugin.getWorkspace().getRoot()
							.getLocationURI());
					fs = fs.getChild(fMainPage.getProjectName());
				} else
					fs = EFS.getStore(p);
				IFileInfo f = fs.fetchInfo();
				if (f.exists() && f.isDirectory()) {
					if (fs.getChild(".project").fetchInfo().exists()) { 
						if (!MessageDialog.openConfirm(getShell(), 
								MiniGUIMessages.getString("MGProjectWizard.0"), 
								MiniGUIMessages.getString("MGProjectWizard.1")))
							return null;
					}
					existingPath = true;
				}
			} catch (CoreException e) {
				CUIPlugin.log(e.getStatus());
			}
			savedHandler = fMainPage.h_selected;
			savedHandler.saveState();
			lastProjectName = fMainPage.getProjectName();
			lastProjectLocation = fMainPage.getProjectLocation();
			// start creation process
			invokeRunnable(getRunnable(defaults, onFinish));
		}
		return newProject;
	}

	private void clearProject() {
		if (lastProjectName == null)
			return;
		try {
			ResourcesPlugin.getWorkspace().getRoot()
					.getProject(lastProjectName).delete(!existingPath, true, null);
		} catch (CoreException ignore) { }
		newProject = null;
		lastProjectName = null;
		lastProjectLocation = null;
	}

	private boolean invokeRunnable(IRunnableWithProgress runnable) {
		IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(runnable);
		try {
			getContainer().run(true, true, op);
		} catch (InvocationTargetException e) {
			CUIPlugin.errorDialog(getShell(), title, message, e.getTargetException(), false);
			clearProject();
			return false;
		} catch (InterruptedException e) {
			clearProject();
			return false;
		}
		return true;
	}

	public boolean performFinish() {
		boolean needsPost = (newProject != null && !isChanged());
		// create project if it is not created yet
		if (getProject(fMainPage.isCurrent(), true) == null)
			return false;
		fMainPage.h_selected.postProcess(newProject, needsPost);
		try {
			setCreated();
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		}
		BasicNewProjectResourceWizard.updatePerspective(fConfigElement);
		selectAndReveal(newProject);
		return true;
	}

	protected boolean setCreated() throws CoreException {
		ICProjectDescriptionManager mngr = CoreModel.getDefault()
				.getProjectDescriptionManager();

		ICProjectDescription des = mngr
				.getProjectDescription(newProject, false);
		if (des.isCdtProjectCreating()) {
			des = mngr.getProjectDescription(newProject, true);
			des.setCdtProjectCreated();
			mngr.setProjectDescription(newProject, des, false, null);
			return true;
		}
		return false;
	}

	public boolean performCancel() {
		clearProject();
		return true;
	}

	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		fConfigElement = config;
	}

	private IRunnableWithProgress getRunnable(boolean _defaults,
			final boolean onFinish) {
		final boolean defaults = _defaults;
		return new IRunnableWithProgress() {
			public void run(IProgressMonitor imonitor)
					throws InvocationTargetException, InterruptedException {
				getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						try {
							newProject = createIProject(lastProjectName, lastProjectLocation);
							if (newProject != null)
								fMainPage.h_selected.createProject(newProject, defaults, onFinish);
						} catch (CoreException e) {
							CUIPlugin.log(e);
						}
					}
				});
			}
		};
	}

	public IProject createIProject(final String name, final URI location)
			throws CoreException {
		if (newProject != null)
			return newProject;
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		final IProject newProjectHandle = root.getProject(name);

		if (!newProjectHandle.exists()) {
			IProjectDescription description = workspace
					.newProjectDescription(newProjectHandle.getName());
			if (location != null)
				description.setLocationURI(location);
			newProject = CCorePlugin.getDefault().createCDTProject
								(description,	newProjectHandle, new NullProgressMonitor());
		} else {
			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					newProjectHandle.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
			};
			NullProgressMonitor monitor = new NullProgressMonitor();
			workspace.run(runnable, root, IWorkspace.AVOID_UPDATE, monitor);
			newProject = newProjectHandle;
		}
		// Open the project if we have to
		if (!newProject.isOpen()) {
			newProject.open(new NullProgressMonitor());
		}

		return continueCreation(newProject);
	}
	
	public String[] getNatures() {
		return new String[] { CProjectNature.C_NATURE_ID, MgProjectNature.MG_NATURE_ID};
	}

	protected IProject continueCreation(IProject prj) {
		try {
			new MgProject(prj).addMgNature(new NullProgressMonitor());
		} catch (CoreException e) {
		}
		return prj;
	}

	public void dispose() {
		fMainPage.dispose();
	}

	public boolean canFinish() {
		if (fMainPage.h_selected != null) {
			if (!fMainPage.h_selected.canFinish())
				return false;
			String s = fMainPage.h_selected.getErrorMessage();
			if (s != null)
				return false;
		}
		return super.canFinish();
	}

	public String getLastProjectName() {
		return lastProjectName;
	}

	public URI getLastProjectLocation() {
		return lastProjectLocation;
	}

	public IProject getLastProject() {
		return newProject;
	}

	// Methods below should provide data for language check
	public String[] getLanguageIDs() {
		return EMPTY_ARR;
	}

	public String[] getContentTypeIDs() {
		return EMPTY_ARR;
	}

	public String[] getExtensions() {
		return EMPTY_ARR;
	}
	
}
