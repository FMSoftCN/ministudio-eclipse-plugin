package org.minigui.eclipse.cdt.mstudio.wizards;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

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
import org.eclipse.swt.widgets.Text;
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

	private Text ialNameField;
	private Text galNameField;
	private Text resolutionNameField;
	
	private static final String STORE_TARGET_NAMES_ID = "MStudioDeployWizardPage.STORE_TARGET_NAMES_ID";
	private static final String STORE_BINARY_NAMES_ID = "MStudioDeployWizardPage.STORE_BINARY_NAMES_ID";
	private static final String STORE_RESPACKAGE_NAMES_ID = "MStudioDeployWizardPage.STORE_RESPACKAGE_NAMES_ID";

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
	
	protected void createDirectoryDialog(Combo field, String message, String text)
	{
        DirectoryDialog dialog = new DirectoryDialog(getContainer().getShell(),
                SWT.SAVE);

        dialog.setMessage(message);
        dialog.setText(text);
        dialog.setFilterPath(getComboValue(field));
        String selectedDirectoryName = dialog.open();

        if (selectedDirectoryName != null) {
            setErrorMessage(null);
            setComboValue(field, selectedDirectoryName);
        }
	}
	
    protected String makeRelativePath(String file) {
        IPath rootPath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
        IPath testPath = new Path(file);

        if(testPath.equals(rootPath))
            return rootPath.lastSegment();

        if(testPath.matchingFirstSegments(rootPath) == rootPath.segmentCount()){
            IPath relativePath = testPath.removeFirstSegments(rootPath.segmentCount());
            return relativePath.toString();
        }

        return null;
    }

    
	protected void createFileDialog(Combo field, 
			String text, String[] filterNames,
			String[] filterExt, boolean multi) {
		
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        String filterPath = root.getLocation().toString();
        int style = SWT.OPEN;
        
        if (multi)
        	style |= SWT.MULTI;
        
        FileDialog dialog = new FileDialog(getContainer().getShell(), style);
        
        dialog.setText(text);
        dialog.setFilterPath(filterPath);
        dialog.setFilterNames(filterNames);
        dialog.setFilterExtensions(filterExt);
        
        String selectedFileName = dialog.open();
        if (selectedFileName != null) {
        	if (!multi) {
                setErrorMessage(null);
                String filename = makeRelativePath(selectedFileName);
                System.out.println(filename);
                if (filename != null)
                	setComboValue(field, File.separatorChar + filename);
        	} else {
            	String[] selFiles = dialog.getFileNames();
            	String curFilterPath = dialog.getFilterPath();
                StringBuffer binValue = new StringBuffer();

            	for (int i =1; i < selFiles.length; i++) {
            		binValue.append(curFilterPath);
            		if (binValue.charAt(binValue.length() - 1) != File.separatorChar) {
            			binValue.append(File.separatorChar);
            		}
            		binValue.append(selFiles[i]);
            		binValue.append(" ");
            	}
            	
                setErrorMessage(null);
        		setComboValue(field, binValue.toString());
        	}
        }
	}
		
	protected void handleBrowseButtonPressed(Widget source) {
		
		if (source == destBrowseButton) {
			createDirectoryDialog(destNameField, "Select a target directory", "Target Directory");
			
		} else if (source == binBrowseButton) {
	        String[] filterNames = {"Binary Files(*)", "Executable Files(*.exe)", "All Files(*.*)"};
	        String[] filterExt = {"*","*.exe", "*.*"};
	        createFileDialog(binNameField, "Select Binary File", filterNames, filterExt, false);
	        
		} else if (source == resPackBrowseButton) {
	        String[] filterNames = {"Resource Package Files(*.res)", "All Files(*.*)"};
	        String[] filterExt = {"*.res", "*.*"};
	        createFileDialog(resPackNameField, "Select Resource Packages", filterNames, filterExt, false);
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
		
		createSourceGroup(composite);
		createDestinationGroup(composite);
		
		restoreWidgetValues();
		if (fInitialSelection != null) {
			setupBasedOnInitialSelections();
		}
		
		updateWidgetEnablements();
		setPageComplete(determinePageCompletion());
		setErrorMessage(null);
		
		setControl(composite);
		giveFocusToCombo(destNameField);
	}
	
	private void createSourceGroup(Composite parent) {
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
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				// TODO Auto-generated method stub
				updateWidgetEnablements();
			}
		};
		fProjectViewer.addCheckStateListener(checkListener);
		
        createLabel(resourcesGroup, "Target Information:");
        createTargetInfoGroup(resourcesGroup);
		initProjects();
	}
	
    protected boolean validateSourceGroup() {
        return true;
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
					ArrayList results = new ArrayList();
					for (int i = 0; i < members.length; i ++) {
						if ((members[i].getType() & resourceType) > 0) {
							results.add(members[i]);
						}
					}
					return results.toArray();
				}
				
				if (o instanceof ArrayList) {
					return ((ArrayList) o).toArray();
				}
				return new Object[0];
			}
		};
	}

    private Text createTargetInfoEditor(Composite parent, String initLabel) {
		new Label(parent, SWT.RIGHT).setText(initLabel);
		return new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.CENTER);
    }

    private void createTargetInfoGroup(Composite parent) {
		Composite targetGroup = new Composite(parent, SWT.NONE);
		targetGroup.setFont(parent.getFont());
		
		GridLayout layout2 = new GridLayout(4, true);
		targetGroup.setLayout(layout2);
		targetGroup.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
				| GridData.HORIZONTAL_ALIGN_FILL));

		ialNameField = createTargetInfoEditor(targetGroup, "ial:");
		galNameField = createTargetInfoEditor(targetGroup, "gal:");
		//targetNameField = createTargetInfoEditor(targetGroup, "target name:");
		resolutionNameField = createTargetInfoEditor(targetGroup, "resolution(bpp):");
    }

    private void setComboGroup(Composite parent,
            Combo field, Button browseButton, String buttonText) {
        Font font = parent.getFont();

        field.addListener(SWT.Modify, this);
        field.addListener(SWT.Selection, this);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.GRAB_HORIZONTAL);
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        field.setLayoutData(data);
        field.setFont(font);

        // browse button
        browseButton.setText(buttonText);
        browseButton.addListener(SWT.Selection, this);
        browseButton.setFont(font);
        setButtonLayoutData(browseButton);
    }

    private void createLabel(Composite parent, String text) {
        Label label = new Label(parent, SWT.NONE);
        if (text.length() != 0) {
            label.setText(text);
            label.setFont(parent.getFont());
        }
    }
    
	private void createDestinationGroup(Composite parent) {
        Font font = parent.getFont();
        Composite destSelectionGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        destSelectionGroup.setLayout(layout);
        destSelectionGroup.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
        destSelectionGroup.setFont(font);

        createLabel(destSelectionGroup, "Resource Package");
        resPackNameField = new Combo(destSelectionGroup, SWT.SINGLE | SWT.BORDER);
        resPackBrowseButton = new Button(destSelectionGroup, SWT.PUSH);
        setComboGroup(destSelectionGroup, 
                resPackNameField, resPackBrowseButton, "Browse");

        createLabel(destSelectionGroup, "Binary File");
        binNameField = new Combo(destSelectionGroup, SWT.SINGLE | SWT.BORDER);
        binBrowseButton = new Button(destSelectionGroup, SWT.PUSH);
        setComboGroup(destSelectionGroup, 
                binNameField, binBrowseButton, "Browse");

        createLabel(destSelectionGroup, "Target Directory");
        destNameField = new Combo(destSelectionGroup, SWT.SINGLE | SWT.BORDER);
        destBrowseButton = new Button(destSelectionGroup, SWT.PUSH);
        setComboGroup(destSelectionGroup, 
                destNameField, destBrowseButton, "Browse");
        
        new Label(destSelectionGroup, SWT.NONE); // vertical spacer
	}
	
	protected String destinationEmptyMessage() {
		return MiniGUIMessages.getString(
				"MStudioDeployWizardPage.deployErrors.target.EmptyDirectory");
	}

    protected boolean validateDestinationGroup() {
		String destinationValue = getComboValue(destNameField);
		if (destinationValue.length() == 0) {
			setMessage(destinationEmptyMessage());
			return false;
		}

		String threatenedContainer = getOverlappingProjectName(destinationValue);
		if (threatenedContainer == null)
			setMessage(null);
		else 
			setMessage(NLS.bind("Warning: Deploy damage", threatenedContainer), WARNING);
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
		internalRestoreWidgetValues(settings, STORE_TARGET_NAMES_ID, destNameField);
		internalRestoreWidgetValues(settings, STORE_BINARY_NAMES_ID, binNameField);
		internalRestoreWidgetValues(settings, STORE_RESPACKAGE_NAMES_ID, resPackNameField);
	}
	
    protected void saveWidgetValues() {
    	super.saveWidgetValues();
    	
		IDialogSettings settings = getDialogSettings();
		internalSaveWidgetValues(settings, STORE_TARGET_NAMES_ID, destNameField);
		internalSaveWidgetValues(settings, STORE_BINARY_NAMES_ID, binNameField);
		internalSaveWidgetValues(settings, STORE_RESPACKAGE_NAMES_ID, resPackNameField);
    }
    
	protected void internalSaveWidgetValues(IDialogSettings settings,
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
			}
		}
	}

	public boolean finish() {
		if (!ensureDirectoryIsValid(destNameField)) {
			return false;
		}
		/*
		if (!ensureFileIsValid(binNameField)) {
			return false;
		}
		
		if (!ensureFileIsValid(resPackNameField)) {
			return false;
		}
		*/
		
		ICProject[] projectsToExport = getCheckedElements();

		saveWidgetValues();
		System.out.println(getTextValue(ialNameField));
		System.out.println(getTextValue(galNameField));
		System.out.println(getTextValue(resolutionNameField));

		List resourcesToExport = getAllDeployFiles(projectsToExport);

		return executeExportOperation(new MStudioFileExportOperation(null,
				resourcesToExport, getComboValue(destNameField), this));
	}
	
	private List getAllDeployFiles(ICProject[] projects) {
		List result = new ArrayList();
		for (ICProject project : projects) {
			getDeployFiles(project, result);
		}	
		return result;
	}

	private void getDeployFiles(ICProject project, Collection result) {
		IProject prj = project.getProject();
		
		List filesList = getDeployFilesList(prj);
		Iterator it = filesList.iterator();
		
		while (it.hasNext()) {
			IResource res = prj.getFile((String)it.next());
			result.add(res);
		}
	}

	private List getDeployFilesList(IProject project) {
		List list = new ArrayList();
		list.add(MINIGUI_CONFIG_FILE);
		
		String binFile = getRelativeBinFile(project);
		if (binFile != null) {
	        System.out.println("binary file =" + binFile);
			list.add(binFile);
		}
		addImagesToList(project, list);
		addFontsToList(project, list);
		
		return list;
	}
	
    protected String getRelativeBinFile(IProject project) {
    	String testFile = getComboValue(binNameField);  	
        IPath testPath = new Path(testFile);
        
        String projName = project.getName();
        IPath projPath = project.getFullPath();
        System.out.println(projName);
        System.out.println(projPath.toString());
        
        if(testPath.equals(projPath))
            return null;

        if(testPath.matchingFirstSegments(projPath) == projPath.segmentCount()){
            IPath relativePath = testPath.removeFirstSegments(projPath.segmentCount());
            return relativePath.toString();
        }

        return null;
    }

	private void addImagesToList(IProject project, Collection result) {
		IFile imageFile = project.getFile(MSTUDIO_IMAGEID_FILE);
		if (imageFile.isAccessible()) {
			getImagesInfo(imageFile.getLocation().toOSString(), result);
		}
	}
	
	private void getImagesInfo(String xmlFile, Collection result) {
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
	
	private void addFontsToList(IProject project, Collection result) {
		IFile cfgFile = project.getFile(MINIGUI_CONFIG_FILE);
		if (cfgFile.isAccessible()) {
		    String[] fontKey = {"upf", "truetypefonts"};
            getFontsInfo(cfgFile.getLocation().toOSString(), fontKey, result);
		}
	}

	private void getFontsInfo(String cfgFile, String[] fontKey, Collection result) {
		
	    MStudioParserIniFile iniObj = new MStudioParserIniFile(cfgFile);
	    for (int j = 0; j < fontKey.length; j++) {
		    for(int i = 0; i < iniObj.getIntegerProperty(fontKey[j], "font_number"); i++) {
	            String font = iniObj.getStringProperty(fontKey[j], "fontfile"+i);
	            result.add(font);
		    	System.out.println(fontKey[j] + ", fontfile"+i +"=" + font);	    	
		    }
	    }
	}
	
	private void targetMiniGUICfg(String cfgFile) {

	    MStudioParserIniFile iniObj = new MStudioParserIniFile(cfgFile);
	    
	    String galEngine = iniObj.getStringProperty("system", "gal_engine");
	    System.out.println("gal_engine=" + galEngine);
	    
        //800x600-16bpp  format:%sx%s-%sbpp [0-9][x][0-9][-][0-9]x
        String defaultMode = iniObj.getStringProperty("system", "defaultmode");
	    System.out.println("defaultmode=" + defaultMode);
	    String[] modes = defaultMode.split("[-]");
	    
	    for (int i = 0; i < modes.length; i++) {
    		System.out.println(modes[i]);
	    }
	    String newMode = String.format("%s-%sbpp", modes[0], getTextValue(resolutionNameField));
	    System.out.println("new mode value = " + newMode);
        String ialEngine = iniObj.getStringProperty("system", "ial_engine");
	    System.out.println("ial_engine=" + ialEngine);
	}
	
	private String getTextValue(Text field) {
		return field.getText().trim();
	}
	
	private ICProject[] getCheckedElements() {
		Object[] obj = fProjectViewer.getCheckedElements();
		ICProject[] prjs = new ICProject[obj.length];
		System.arraycopy(obj, 0, prjs, 0, obj.length);
		return prjs;
	}
	

	protected boolean ensureFileIsValid(File file) {
		if (file.exists() && !file.isFile()) {
			return false;
		}
		
		if (!file.exists()) {
			displayErrorDialog(
					MiniGUIMessages.getString("MStudioDeployWizardPage.deployErrors.NoSuchFile"));
			return false;
		}
		
		return true;
	}
	
	protected boolean ensureFileIsValid(Combo field) {
		// for single selection
		File targetFile = new File(getComboValue(field));
		if (!ensureFileIsValid(targetFile)) {
			giveFocusToCombo(field);
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
		
        targetMiniGUICfg(MINIGUI_CONFIG_FILE);
		return true;
	}
}
