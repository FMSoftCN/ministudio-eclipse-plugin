package org.minigui.eclipse.cdt.mstudio.wizards;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.dialogs.WizardDataTransferPage;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.minigui.eclipse.cdt.mstudio.MiniGUIMessages;
import org.minigui.eclipse.cdt.mstudio.project.MgProjectNature;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MStudioDeployWizardPage extends WizardDataTransferPage implements
		Listener {

	private IStructuredSelection fInitialSelection;
	
	private CheckboxTableViewer fProjectViewer;
	
	private Combo destNameField;
	private Button destBrowseButton;
	
	private Combo binNameField;
	private Button binBrowseButton;

	private Combo resPackNameField;
	private Button resPackBrowseButton;

	private Combo ialNameField;
	private Combo galNameField;
	private Combo resolutionNameField;
	private Object checkedObject;
	
	@SuppressWarnings("unused")
	private static final String STORE_TARGET_NAMES_ID = "MStudioDeployWizardPage.STORE_TARGET_NAMES_ID";
	@SuppressWarnings("unused")
	private static final String STORE_BINARY_NAMES_ID = "MStudioDeployWizardPage.STORE_BINARY_NAMES_ID";
	@SuppressWarnings("unused")
	private static final String STORE_RESPACKAGE_NAMES_ID = "MStudioDeployWizardPage.STORE_RESPACKAGE_NAMES_ID";
	@SuppressWarnings("unused")
	private static final String STORE_IAL_NAMES_ID = "MStudioDeployWizardPage.STORE_IAL_NAMES_ID";
	@SuppressWarnings("unused")
	private static final String STORE_GAL_NAMES_ID = "MStudioDeployWizardPage.STORE_GAL_NAMES_ID";
	@SuppressWarnings("unused")
	private static final String STORE_RESOLUTION_NAMES_ID = "MStudioDeployWizardPage.STORE_RESOLUTION_NAMES_ID";

	private static final Map<String, Combo> saveIDField = new HashMap<String, Combo>();
	private static final String MINIGUI_CONFIG_FILE = "MiniGUI.cfg";
	private static final String MSTUDIO_IMAGEID_FILE = "res/image/id.xml";
	
	protected MStudioDeployWizardPage(String pageName, IStructuredSelection selection) {
		super(pageName);
		fInitialSelection = selection;
	}

	public MStudioDeployWizardPage(IStructuredSelection selection) {
		this("MStudioDeployWizardPage", selection);
		setTitle(MiniGUIMessages.getString("MStudioDeployWizardPage.title"));
		setDescription(MiniGUIMessages.getString("MStudioDeployWizardPage.desc"));
	}

	private void initMapInfo() {
		saveIDField.put("STORE_TARGET_NAMES_ID", destNameField);
		saveIDField.put("STORE_BINARY_NAMES_ID", binNameField);
		saveIDField.put("STORE_RESPACKAGE_NAMES_ID", resPackNameField);
		saveIDField.put("STORE_IAL_NAMES_ID", ialNameField);
		saveIDField.put("STORE_GAL_NAMES_ID", galNameField);
		saveIDField.put("STORE_RESOLUTION_NAMES_ID", resolutionNameField);
	}
	
	@Override
	protected boolean allowNewContainerName() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void handleEvent(Event event) {
		// TODO Auto-generated method stub
		Widget source = event.widget;
		handleBrowseButtonPressed(source);
		updatePageCompletion();
	}
	
	protected void createDirectoryDialog(Combo field, String text, String message)
	{
        DirectoryDialog dialog = new DirectoryDialog(getContainer().getShell(),
                SWT.SAVE);

        if (message != null)
            dialog.setMessage(message);

        dialog.setText(text);
        dialog.setFilterPath(getComboValue(field));
        String selectedDirectoryName = dialog.open();

        if (selectedDirectoryName != null) {
            setErrorMessage(null);
            setComboValue(field, selectedDirectoryName);
        }
	}
	
	protected Object getCheckedProject() {
		return checkedObject;
	}
	
	protected void setCheckedProject(Object o) {
		checkedObject = o;
	}
	
	protected void createFileDialog(Combo field, 
			String text, String[] filterNames,
			String[] filterExt, boolean multi) {
		
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        String filterPath = root.getLocation().toString();
        int style = SWT.OPEN;
        
        if (multi)
        	style |= SWT.MULTI;
        
        Object checkedProj = getCheckedProject();
        if (checkedProj != null) {
            if (filterPath.charAt(filterPath.length() - 1) != File.separatorChar) {
            	filterPath += File.separatorChar;
            }
        	filterPath += (checkedProj.toString());
        	if (field == resPackNameField) {
        		filterPath += (File.separatorChar + "res");
        	}
        }
        
        FileDialog dialog = new FileDialog(getContainer().getShell(), style);
        
        dialog.setText(text);
        dialog.setFilterPath(filterPath);
        dialog.setFilterNames(filterNames);
        dialog.setFilterExtensions(filterExt);
        
        String selectedFileName = dialog.open();
        if (selectedFileName != null) {
        	if (!multi) {
                setErrorMessage(null);
                setComboValue(field, selectedFileName);
        	} else {
            	String[] selFiles = dialog.getFileNames();
            	String curFilterPath = dialog.getFilterPath();
                StringBuffer binValue = new StringBuffer();

                if (curFilterPath.charAt(curFilterPath.length() - 1) != File.separatorChar) {
                	curFilterPath += File.separatorChar;
                }
                
            	for (int i = 0; i < selFiles.length; i++) {
            		binValue.append(curFilterPath + selFiles[i]+";");
            	}
            	
                setErrorMessage(null);
                setComboValue(field, binValue.toString());
        	}
        }
	}
		
	protected void handleBrowseButtonPressed(Widget source) {
		
		if (source == destBrowseButton) {
			createDirectoryDialog(destNameField, "Select target directory", null);
			
		} else if (source == binBrowseButton) {
	        String[] filterNames = {"Binary Files(*)", "Executable Files(*.exe)", "All Files(*.*)"};
	        String[] filterExt = {"*","*.exe", "*.*"};
	        createFileDialog(binNameField, "Select Binary File", filterNames, filterExt, false);
	        
		} else if (source == resPackBrowseButton) {
	        String[] filterNames = {"Resource Package Files(*.res)", "All Files(*.*)"};
	        String[] filterExt = {"*.res", "*.*"};
	        createFileDialog(resPackNameField, "Select Resource Package", filterNames, filterExt, false);
		}
	}

	@Override
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
		initializeDialogUnits(parent);
		
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
				| GridData.HORIZONTAL_ALIGN_FILL));
		composite.setFont(parent.getFont());
		
		createProjectsGroup(composite);
		createTargetDirGroup(composite);
		initMapInfo();

		restoreWidgetValues();
		if (fInitialSelection != null) {
			setupBasedOnInitialSelections();
		}
		
		setPageComplete(determinePageCompletion());
		updateWidgetEnablements();
		setErrorMessage(null);
		
		setControl(composite);
		giveFocusToCombo(destNameField);
	}
	
    protected void updateWidgetEnablements() {
        updatePageCompletion();
    }
    
	private void createProjectsGroup(Composite parent) {
		Composite resourcesGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		resourcesGroup.setLayout(layout);
		resourcesGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		resourcesGroup.setFont(parent.getFont());
		
		Table table = new Table(resourcesGroup, SWT.CHECK | SWT.BORDER | SWT.SINGLE);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		fProjectViewer = new CheckboxTableViewer(table);

		ITreeContentProvider contentProvider = getResourceProvider(IResource.PROJECT);
		fProjectViewer.setContentProvider(contentProvider);
		fProjectViewer.setLabelProvider(WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
		

		ICheckStateListener checkListener = new ICheckStateListener() {
			private void tableItemChecked(Object listElement, boolean state) {
				Object checked = getCheckedProject();
				if (state) {
					if (checked != null) {
						fProjectViewer.setChecked(checked, false);
					}
					setCheckedProject(listElement);
				} else {
					if (checked != null && checked == listElement) {
						setCheckedProject(null);
					}
				}
			}
			
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				// TODO Auto-generated method stub
				//get old selected item	
				tableItemChecked(event.getElement(), event.getChecked());
				updateWidgetEnablements();
			}
		};
		fProjectViewer.addCheckStateListener(checkListener);
		
        createLabel(resourcesGroup, "Target Information:");
        createTargetInfoGroup(resourcesGroup);
		initProjects();
	}
	
	protected String fileInProject(String project, String file, boolean needPath) {
        IPath projPath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
        IPath testPath = new Path(file);

        projPath = projPath.append(project);

        if(testPath.matchingFirstSegments(projPath) == projPath.segmentCount()){
        	if (needPath) {
                IPath relativePath = testPath.removeFirstSegments(projPath.segmentCount());
                return relativePath.toString();
        	}
        	return "";
        }
		return null;
	}

    protected boolean validateSourceGroup() {
    	if (getCheckedProject() == null) {
    	    setMessage(getMessage("MStudioDeployWizardPage.deployErrors.target.EmptyProject"));	
    	    return false;
    	}

        if (getComboValue(ialNameField).length() == 0) {
    	    setMessage(getMessage("MStudioDeployWizardPage.deployErrors.target.EmptyIAL"));	
    	    return false;
        }
    		
        if (getComboValue(galNameField).length() == 0) {
    	    setMessage(getMessage("MStudioDeployWizardPage.deployErrors.target.EmptyGAL"));	
    	    return false;
        }

        if (getComboValue(resolutionNameField).length() == 0) {
    	    setMessage(getMessage("MStudioDeployWizardPage.deployErrors.target.EmptyResolution"));	
    	    return false;
        }

        if (getComboValue(resPackNameField).length() == 0) {
    	    setMessage(getMessage("MStudioDeployWizardPage.deployErrors.target.EmptyResPack"));	
    	    return false;
        }
        
        if (getComboValue(binNameField).length() == 0) {
    	    setMessage(getMessage("MStudioDeployWizardPage.deployErrors.target.EmptyBinaryFile"));	
    	    return false;
        }

        return true;
    }
    
	protected boolean valueIsFile(Combo field) {
		return new File(getComboValue(field)).isFile();
	}


	private void initProjects() {
		ArrayList<ICProject> input = new ArrayList<ICProject>();
		ICProject[] projects;
		try {
			projects = CoreModel.getDefault().getCModel().getCProjects();
			for (ICProject project: projects) {
				if (project.getProject().hasNature(MgProjectNature.MG_NATURE_ID))
					input.add(project);
			}
		} catch (CModelException e) {
			
		} catch (CoreException e) {
			
		}
		fProjectViewer.setInput(input);
	}
	
	private ITreeContentProvider getResourceProvider(final int resourceType) {
		return new WorkbenchContentProvider() {
			public Object[] getChildren(Object o) {
				if (o instanceof IContainer) { 
					IResource[] members = null;
					try {
						members = ((IContainer) o).members();
					} catch (CoreException e) {
						return new Object[0];
					}
					ArrayList<IResource> results = new ArrayList<IResource>();
					for (int i = 0; i < members.length; i ++) {
						if ((members[i].getType() & resourceType) > 0) {
							results.add(members[i]);
						}
					}
					return results.toArray();
				}
				
				if (o instanceof ArrayList) {
					return ((ArrayList<?>) o).toArray();
				}
				return new Object[0];
			}
		};
	}

    private void createTargetInfoGroup(Composite parent) {
		Composite targetGroup = new Composite(parent, SWT.NONE);
		targetGroup.setFont(parent.getFont());
		
		GridLayout layout = new GridLayout();
        layout.numColumns = 6;
		targetGroup.setLayout(layout);
		targetGroup.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
				| GridData.HORIZONTAL_ALIGN_FILL));

		new Label(targetGroup, SWT.RIGHT).setText(
			getMessage("MStudioDeployWizardPage.ial.label"));
		ialNameField = new Combo(targetGroup, SWT.SINGLE | SWT.BORDER);
        setComboGroup(targetGroup, ialNameField);

		new Label(targetGroup, SWT.RIGHT).setText(
				getMessage("MStudioDeployWizardPage.gal.label"));
		galNameField = new Combo(targetGroup, SWT.SINGLE | SWT.BORDER);
        setComboGroup(targetGroup, galNameField);

		new Label(targetGroup, SWT.RIGHT).setText(
				getMessage("MStudioDeployWizardPage.resolution.label"));
		resolutionNameField = new Combo(targetGroup, SWT.SINGLE | SWT.BORDER);
        setComboGroup(targetGroup, resolutionNameField);
        resolutionNameField.setTextLimit(2);
        resolutionNameField.addListener(SWT.Verify, new Listener() {
        	public void handleEvent(Event e) {
        		String string = e.text;
        		char [] chars = new char[string.length()];
        		string.getChars(0, chars.length, chars, 0);
        		
        		for (int i = 0; i < chars.length; i++) {
        			if (!('0' <= chars[i] && chars[i] <= '9')) {
        				e.doit = false;
        				return;
        			}
        		}
        	}
        });
    }

    private void setComboGroup(Composite parent, Combo field) {
        setComboGroup(parent, field, null, null);
    }

    private void setComboGroup(Composite parent,
            Combo field, Button browseButton, String buttonText) {
        field.addListener(SWT.Modify, this);
        field.addListener(SWT.Selection, this);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.GRAB_HORIZONTAL);
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        field.setLayoutData(data);
        field.setFont(parent.getFont());

        // browse button
        if (browseButton != null) {
            browseButton.setText(buttonText);
            browseButton.addListener(SWT.Selection, this);
            browseButton.setFont(parent.getFont());
            setButtonLayoutData(browseButton);
        }
    }

    private void createLabel(Composite parent, String text) {
        Label label = new Label(parent, SWT.NONE);
        if (text.length() != 0) {
            label.setText(text);
            label.setFont(parent.getFont());
        }
    }
    
	private void createTargetDirGroup(Composite parent) {
        Font font = parent.getFont();
        Composite destSelectionGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        destSelectionGroup.setLayout(layout);
        destSelectionGroup.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
        destSelectionGroup.setFont(font);

        createLabel(destSelectionGroup, 
        		getMessage("MStudioDeployWizardPage.respackage.label"));
        resPackNameField = new Combo(destSelectionGroup, SWT.SINGLE | SWT.BORDER);
        resPackBrowseButton = new Button(destSelectionGroup, SWT.PUSH);
        setComboGroup(destSelectionGroup, 
                resPackNameField, resPackBrowseButton, 
        		getMessage("MStudioDeployWizardPage.browse.label"));

        createLabel(destSelectionGroup, 
        		getMessage("MStudioDeployWizardPage.binary.label"));
        
        binNameField = new Combo(destSelectionGroup, SWT.SINGLE | SWT.BORDER);
        binBrowseButton = new Button(destSelectionGroup, SWT.PUSH);
        setComboGroup(destSelectionGroup, 
                binNameField, binBrowseButton, 
        		getMessage("MStudioDeployWizardPage.browse.label"));

        createLabel(destSelectionGroup, 
        		getMessage("MStudioDeployWizardPage.target.label"));
        destNameField = new Combo(destSelectionGroup, SWT.SINGLE | SWT.BORDER);
        destBrowseButton = new Button(destSelectionGroup, SWT.PUSH);
        setComboGroup(destSelectionGroup, destNameField, destBrowseButton, 
        		getMessage("MStudioDeployWizardPage.browse.label"));
        
        new Label(destSelectionGroup, SWT.NONE); // vertical spacer
	}
	
	private String getMessage(String id) {
		return MiniGUIMessages.getString(id);
	}

    protected boolean validateDestinationGroup() {
		String destinationValue = getComboValue(destNameField);
		if (destinationValue.length() == 0) {
			setMessage(getMessage("MStudioDeployWizardPage.deployErrors.target.EmptyTargetDirectory"));
			return false;
		}

		String threatenedContainer = getOverlappingProjectName(destinationValue);
		if (threatenedContainer == null) {
			setMessage(null);
        }
		else {
			setMessage(NLS.bind("Warning: damage container", threatenedContainer), WARNING);
        }

		return true;
    }

    private String getOverlappingProjectName(String targetDirectory){
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IPath testPath = new Path(targetDirectory);
        IContainer[] containers = root.findContainersForLocation(testPath);
        if(containers.length > 0){
            return containers[0].getProject().getName();
        }
        return null;
    }

	protected void internalRestoreWidgetValues(IDialogSettings settings,
			String nameID, Combo field)
	{
		if (settings != null) {
			String[] directoryNames = settings.getArray(nameID);
			if (directoryNames != null) {
				setComboValue(field, directoryNames[0]);
				for (int i = 0; i < directoryNames.length; i++) {
					addComboItem(field, directoryNames[i]);
				}
			}
		}
	}

	protected void restoreWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		Iterator<Map.Entry<String, Combo>> keyValuePairs = saveIDField.entrySet().iterator();
		int mapSize = saveIDField.size();
		
		for (int i = 0; i < mapSize; i++) {
			Map.Entry<String, Combo> entry = keyValuePairs.next();
			internalRestoreWidgetValues(settings, 
					(String)entry.getKey(), (Combo)entry.getValue());
		}
	}
	
    protected void saveWidgetValues() {
    	super.saveWidgetValues();
		IDialogSettings settings = getDialogSettings();
		Iterator<Map.Entry<String, Combo>> keyValuePairs = saveIDField.entrySet().iterator();
		int mapSize = saveIDField.size();
		
		for (int i = 0; i < mapSize; i++) {
			Map.Entry<String, Combo> entry = keyValuePairs.next();
			internalSaveComboValues(settings, 
					(String)entry.getKey(), (Combo)entry.getValue());
		}
    }
    
	protected void internalSaveComboValues(IDialogSettings settings,
			String nameID, Combo field)
	{
		if (settings != null) {
			String[] directoryNames = settings.getArray(nameID);
			if (directoryNames == null) {
				directoryNames = new String[0];
			}
			directoryNames = addToHistory(directoryNames, getComboValue(field));
			settings.put(nameID, directoryNames);
		}
	}

	protected void addComboItem(Combo field, String value) {
		field.add(value);
	}

	protected String getComboValue(Combo field) {
		return field.getText().trim();
	}
	
	protected void setComboValue(Combo field, String value) {
		field.setText(value);
	}
	
	//sect: target, binary, respackage
	protected String getComboLabel(String sect) {
		return MiniGUIMessages.getString("MStudioDeployWizardPage."+sect+".label");
	}
		
	private void giveFocusToCombo(Combo field) {
		field.setFocus();
	}
	
	private void setupBasedOnInitialSelections() {
		HashSet<String> names = new HashSet<String>();
		Iterator<?> it = fInitialSelection.iterator();
		while (it.hasNext()) {
			IProject project = (IProject) it.next();
			names.add(project.getName());
		}
		
		Collection<?> prjs = (Collection<?>) fProjectViewer.getInput();
		for (Object element: prjs) {
			ICProject prj = (ICProject) element;
			if (names.contains(prj.getElementName())) {
				fProjectViewer.setChecked(prj, true);
				//only one selection support
				setCheckedProject(prj);
				break;
			}
		}
	}

	public boolean finish() {
        if (!ensureFileIsValid()) {
            return false;
        }

		if (!ensureDirectoryIsValid(destNameField)) {
			return false;
		}
		
		ICProject[] projectsToExport = getCheckedElements();
		saveWidgetValues();
		List<IResource> resourcesToExport = getAllDeployFiles(projectsToExport);

		return executeExportOperation(new MStudioFileExportOperation(null,
				resourcesToExport, getComboValue(destNameField), this));
	}
	
	private List<IResource> getAllDeployFiles(ICProject[] projects) {
		List<IResource> result = new ArrayList<IResource>();
		for (ICProject project : projects) {
			getDeployFiles(project, result);
		}	
		return result;
	}

	private void getDeployFiles(ICProject project, Collection<IResource> result) {
		IProject prj = project.getProject();
		
		List<String> filesList = getDeployFilesList(prj);
		Iterator<String> it = filesList.iterator();
		
		while (it.hasNext()) {
			IResource res = prj.getFile((String)it.next());
			result.add(res);
		}
	}

	private void addComboFileToList(IProject project, Combo field, List<String> list) {
		String binFile = fileInProject(project.getName(), getComboValue(field), true);
		if (binFile != null) {
			list.add(binFile);
		}
	}
	
	private List<String> getDeployFilesList(IProject project) {
		List<String> list = new ArrayList<String>();

		File f = new File(project.getLocation().toOSString());
		String[] filename = f.list(new FilenameFilter() {

			@Override
			public boolean accept(File arg0, String arg1) {
				// TODO Auto-generated method stub
				if (arg1.endsWith(".cfg")) {
					return true;
				}
				return false;
			}
			
		});
		
		for (int i = 0; i < filename.length; i++) {
			list.add(filename[i]);
		}

		//list.add(MINIGUI_CONFIG_FILE);
		addComboFileToList(project, binNameField, list);
		addComboFileToList(project, resPackNameField, list);
		addImagesToList(project, list);
		addFontsToList(project, list);
		
		return list;
	}
	
	private void addImagesToList(IProject project, Collection<String> result) {
		IFile imageFile = project.getFile(MSTUDIO_IMAGEID_FILE);
		if (imageFile.isAccessible()) {
			getImagesInfo(imageFile.getLocation().toOSString(), result);
		}
	}
	
	private void getImagesInfo(String xmlFile, Collection<String> result) {
        DocumentBuilderFactory dom_factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dom_builder = dom_factory.newDocumentBuilder();
            InputStream in = new FileInputStream(xmlFile);

            Document doc = dom_builder.parse(in);
            Element root = doc.getDocumentElement();
            NodeList resids = root.getChildNodes();
            
            for (int i = 0; i < resids.getLength(); i++) {
            	Node resid = resids.item(i);
            	if (resid.getNodeType() == Node.ELEMENT_NODE) {
            		for (Node node = resid.getFirstChild(); node != null; node = node.getNextSibling()) 
            		{
            			if (node.getNodeType() == Node.ELEMENT_NODE) {
            				
            				if (node.getNodeName().equals("source")) {
            					String source = node.getFirstChild().getNodeValue();
            					result.add(source);
            				}
            			}
            		}
            	}
            }
        } catch(ParserConfigurationException e) {
        	
        } catch (SAXException e) {
        	
        } catch(IOException e) {
        	System.out.println(e.getMessage());
        }    
    }
	
	private void addFontsToList(IProject project, Collection<String> result) {
		IFile cfgFile = project.getFile(MINIGUI_CONFIG_FILE);
		if (cfgFile.isAccessible()) {
		    String[] fontKey = {"upf", "truetypefonts"};
            getFontsInfo(cfgFile.getLocation().toOSString(), fontKey, result);
		}
	}

	private void getFontsInfo(String cfgFile, String[] fontKey, Collection<String> result) {
		
	    MStudioParserIniFile iniObj = new MStudioParserIniFile(cfgFile);
	    for (int j = 0; j < fontKey.length; j++) {
		    for(int i = 0; i < iniObj.getIntegerProperty(fontKey[j], "font_number"); i++) {
	            String font = iniObj.getStringProperty(fontKey[j], "fontfile"+i);
	            result.add(font);
		    }
	    }
	}
	
	private void targetMiniGUICfg(String cfgFile) {
	    MStudioParserIniFile iniObj = new MStudioParserIniFile(cfgFile);
	    
	    String galEngine = iniObj.getStringProperty("system", "gal_engine");
	    String newGalEngine= new String(getComboValue(galNameField));
	    iniObj.setStringProperty("system", "gal_engine", newGalEngine, null);
	    
	    iniObj.setStringProperty("system", 
	    		"ial_engine", getComboValue(ialNameField), null);

	    //800x600-16bpp  format:%sx%s-%sbpp
        String defaultMode = iniObj.getStringProperty(galEngine, "defaultmode");
	    String[] modes = defaultMode.split("[-]");
	    String newMode = String.format("%s-%sbpp", modes[0], getComboValue(resolutionNameField));

	    iniObj.setStringProperty(newGalEngine, "defaultmode", newMode, null);
	    iniObj.save();
	}
	
	private ICProject[] getCheckedElements() {
		Object[] obj = fProjectViewer.getCheckedElements();
		ICProject[] prjs = new ICProject[obj.length];
		System.arraycopy(obj, 0, prjs, 0, obj.length);
		return prjs;
	}
	
    protected boolean ensureFileIsValid() {
        if (!valueIsFile(resPackNameField)) {
        	setMessage(getMessage("MStudioDeployWizardPage.deployErrors.target.InvalidResPackName"));
            return false;
        }  
    	
        if (!valueIsFile(binNameField)) {
        	setMessage(getMessage("MStudioDeployWizardPage.deployErrors.target.InvalidBinName"));
            return false;
        }  

        if (null == fileInProject(getCheckedProject().toString(), getComboValue(binNameField), false)) {
    	    setMessage(getMessage("MStudioDeployWizardPage.deployErrors.target.SelectProperBinFile"));	
            return false;
        }

        if (null == fileInProject(getCheckedProject().toString(), getComboValue(resPackNameField), false)) {
    	    setMessage(getMessage("MStudioDeployWizardPage.deployErrors.target.SelectProperResFile"));	
            return false;
        }

        return true;
    }

	protected boolean ensureDirectoryIsValid(Combo field) {
		File targetDirectory = new File(getComboValue(field));		

		if (!ensureDirectoryIsValid(targetDirectory)) {
			giveFocusToCombo(field);
			return false;
		}
		return true;	
	}

	protected boolean ensureDirectoryIsValid(File targetDirectory) {
		if (targetDirectory.exists() && !targetDirectory.isDirectory()) {
			displayErrorDialog(
					MiniGUIMessages.getString("MStudioDeployWizardPage.deployErrors.NoSuchDirectory"));
			return false;
		}
		
		if (!targetDirectory.exists()) {
			if (!queryYesNoQuestion(
					MiniGUIMessages.getString("MStudioDeployWizardPage.deployErrors.CreateDirectoryMessage"))) {
				return false;
			}
			
			if (!targetDirectory.mkdirs()) {
				displayErrorDialog(
						MiniGUIMessages.getString("MStudioDeployWizardPage.deployErrors.CreateDirectoryError"));
				return false;
			}
		}
		return true;
	}

	protected boolean executeExportOperation(MStudioFileExportOperation op) {
		op.setCreateLeadupStructure(true);
		op.setOverwriteFiles(true);
		
		List<String> files = new ArrayList<String>();
		files.add(MINIGUI_CONFIG_FILE);
		op.setNeedSaveTargetFilesList(files);
		
		try {
			getContainer().run(true, true, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			displayErrorDialog(e.getTargetException());
			return false;
		}
		
		IStatus status = op.getStatus();
		if (!status.isOK()) {
			ErrorDialog.openError(getContainer().getShell(), 
					MiniGUIMessages.getString("MStudioDeployWizardPage.deployProblems"),
					null, 
					status);
			return false;
		}
		
		//for MINIGUI_CONFIG_FILE
		Iterator<String> it = op.getTargetFilesList().iterator();
		while (it.hasNext()) {
			targetMiniGUICfg(it.next());
		}
		
		return true;
	}
}
