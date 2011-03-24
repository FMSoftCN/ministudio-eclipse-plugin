/*********************************************************************
 * Copyright (C) 2002 ~ 2010, Beijing FMSoft Technology Co., Ltd.
 * Room 902, Floor 9, Taixing, No.11, Huayuan East Road, Haidian
 * District, Beijing, P. R. CHINA 100191.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Beijing FMSoft Technology Co., Ltd. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance you entered into with FMSoft.
 *
 *			http://www.minigui.com
 *
 *********************************************************************/

package org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.preferences.MStudioDeployPreferencePage;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.preferences.MStudioPreferenceConstants;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;


public class MStudioDeployExecutableProjectsWizardPage extends WizardPage {

	private Combo sizeCombo = null;
	private Combo gal = null;
	private Combo ial = null;
	private Composite bottomPanel = null;
	private Table chooseTable = null;
	private CheckboxTableViewer ctv = null;

	private IProject[] projects = null;
	private DirectoryFieldEditor locationPath = null;
	
	private static boolean isDeployPathChanged = false;

	public MStudioDeployExecutableProjectsWizardPage(String pageName) {
		super(pageName);
		init();
	}

	public MStudioDeployExecutableProjectsWizardPage(String pageName,
			String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		init();
	}

	private void init() {
		setTitle(MStudioMessages.getString("MStudioDeployWizardPage.selectExeProjects.pageTitle"));
		setDescription(MStudioMessages.getString("MStudioDeployWizardPage.selectExeProjects.desc"));
	}

	public void createControl(Composite parent) {
		Composite topPanel = new Composite(parent, SWT.NONE);
		topPanel.setLayout(new GridLayout());
		topPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

		//init the Graphic
		//bottomPanel1
		Composite bottomPanel1 = new Composite(topPanel, SWT.NONE);
		bottomPanel1.setLayout(new GridLayout(1, false));
		bottomPanel1.setLayoutData(new GridData(GridData.FILL_BOTH));

		chooseTable = new Table(bottomPanel1,
				SWT.BORDER | SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL | SWT.IMAGE_BMP);
		chooseTable.setLayoutData(new GridData(500, 200));
		ctv = new CheckboxTableViewer(chooseTable);
		ctv.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				validatePage();
			}
			});

		//bottomPanel2
		Composite bottomPanel2 = new Composite(topPanel, SWT.NONE);
		bottomPanel2.setLayout(new GridLayout());
		bottomPanel2.setLayoutData(new GridData(GridData.FILL_BOTH));
		locationPath = new DirectoryFieldEditor("filePath",
				MStudioMessages.getString("MStudioDeployWizardPage.selectExeProjects.locationTitle"),
				bottomPanel2);
		locationPath.setStringValue(MStudioDeployPreferencePage.deployLocation());
		locationPath.getTextControl(bottomPanel2).addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				isDeployPathChanged = true;
				}
			});
		locationPath.getTextControl(bottomPanel2).addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent e) {
			}
			public void focusLost(FocusEvent e) {
				if(isDeployPathChanged){
					checkLocation();
					validatePage();
					isDeployPathChanged = false;
				}
			}
			});
		
		Label locationDes = new Label(bottomPanel2,SWT.NONE);
		locationDes.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
		GridData gd1 = new GridData(GridData.FILL_HORIZONTAL);
		gd1.horizontalSpan = 3;
		locationDes.setLayoutData(gd1);
		locationDes.setText(MStudioMessages.
				getString("MStudioDeployPreferencePage.locationPath.description"));
		
		//bottomPanel3
		Composite bottomPanel3 = new Composite(topPanel, SWT.NONE);
		bottomPanel3.setLayout(new GridLayout(4, false));
		bottomPanel3.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label title = new Label(bottomPanel3, SWT.FILL);
		title.setText(MStudioMessages
				.getString("MStudioDeployWizardPage.selectExeProjects.configTitle"));
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.horizontalSpan = 4;
		title.setLayoutData(gd);

		Label sizeLabel = new Label(bottomPanel3, SWT.NONE);
		sizeLabel.setText(MStudioMessages
				.getString("MStudioDeployWizardPage.selectExeProjects.resolutionLabel"));
		sizeCombo = new Combo(bottomPanel3, SWT.READ_ONLY/*SWT.NONE*/);
		sizeCombo.addSelectionListener(new SelectedChangeListener());
		sizeCombo.setLayoutData(new GridData(300, 25));
		sizeCombo.addKeyListener(new ComboKeyListener());
		
		bottomPanel = new Composite(topPanel, SWT.NONE);
		bottomPanel.setLayout(new GridLayout(4, false));
		bottomPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label galLabel = new Label(bottomPanel, SWT.NONE);
		galLabel.setText(MStudioMessages
				.getString("MStudioDeployWizardPage.selectExeProjects.galLabel"));
		gal = new Combo(bottomPanel, SWT.READ_ONLY);
		gal.addSelectionListener(new SelectedChangeListener());
		Label ialLabel=new Label(bottomPanel, SWT.NONE);
		ialLabel.setText(MStudioMessages.
				getString("MStudioDeployWizardPage.selectExeProjects.ialLabel"));
		ial = new Combo(bottomPanel, SWT.READ_ONLY);
		ial.addSelectionListener(new SelectedChangeListener());

		initExeProjects();
		initSizeAndColor();
		initGALAndIAL();

		setControl(topPanel);
		setPageComplete(false);
	}

	public boolean checkIsDefaultLocation(String path){
		if(path == null)
			return false;
		String curLocation = MStudioPlugin.getDefault().getMStudioEnvInfo().getDefaultLocationPath();
		if(curLocation == null)
			return false;
		return curLocation.equals(path);
	}
	
	public boolean locationChanged() {
		String localPath = locationPath.getStringValue();
		if(localPath == null || localPath.equals(""))
			return false;
		if(!isValidPath(localPath)){
			locationPath.setErrorMessage(MStudioMessages.
					getString("MStudioDeployWizardPage.deployErrors.pathInValid"));
			locationPath.showErrorMessage();
			return false;
		}
		return true;
	}
	
	private void initGALAndIAL(){
		String selectedGalEngine = null;
		String selectedIalEngine = null;
		String[] galP = MStudioEnvInfo.getInstance().getGalOptions();
		String[] ialP = MStudioEnvInfo.getInstance().getIalOptions();
		if(galP == null || ialP == null)
			return;
		String tagetMgconfigureFile = MStudioEnvInfo.getInstance().getWorkSpaceMetadataPath() + "MiniGUI.cfg.target";
		MStudioParserIniFile file = new MStudioParserIniFile(tagetMgconfigureFile);
		if(file == null)
			return;
		selectedGalEngine = file.getStringProperty("system", "gal_engine");
		selectedIalEngine = file.getStringProperty("system", "ial_engine");	
		if(selectedGalEngine == null || selectedIalEngine == null)
			return;
		for(int i = 0; i < galP.length; i++){
			gal.add(galP[i]);
			if (selectedGalEngine != null && selectedGalEngine.equals(galP[i])){
				gal.select(i);
			}
		}	
		for(int i = 0; i < ialP.length; i++){
			ial.add(ialP[i]);
			if (selectedIalEngine != null && selectedIalEngine.equals(ialP[i])){
				ial.select(i);
			}
		}
	}

	private void initSizeAndColor() {
		boolean bResolution = false;
		
		List<String> li = MStudioEnvInfo.getInstance().getResolutions();
		if(li == null)
			return;
		
		String[] resolution = li.toArray(new String[li.size()]);
		
		String tmpResolution = MStudioEnvInfo.getInstance().getScreenSize();
		if(tmpResolution == null)
			return;
		
		for (int i = 0; i < resolution.length; i++) {
			sizeCombo.add(resolution[i].toString());
			if (tmpResolution != null && tmpResolution.equals(resolution[i])){
				sizeCombo.select(i);
				bResolution = true;
			}
		}
		if (!bResolution) {
			sizeCombo.select(0);
		}
	}

	public void update() {
		bottomPanel.setVisible(!((MStudioDeployWizard)(this.getWizard())).isHost());
		validatePage();
	}

	private void initExeProjects() {
		projects = MStudioDeployWizard.getExeProjects();
		if (projects == null)
			return;

		ArrayList<String> list = new ArrayList<String>();
		ArrayList<String> checkedList = new ArrayList<String>();

		for (int i = 0; i < projects.length; i++) {
			list.add(projects[i].getName());
			boolean bDeployable = new MStudioProject(projects[i]).getDefaultDeployable();
			if (bDeployable) {
				checkedList.add(projects[i].getName());
			}
		}
		ctv.add(list.toArray(new String[projects.length]));
		ctv.setCheckedElements(checkedList.toArray(new String[checkedList.size()]));
	}

	private boolean validateResolution(String resolution) {
		if (resolution == null || resolution == "")
			return false;
		
		return resolution.matches("[1-9]+[0-9]*\\s*[x*×]\\s*[1-9]+[0-9]*\\s*-\\s*\\d{1,2}bpp");
	}
	
	protected boolean validatePage() {
		if(!locationChanged()) {
			setErrorMessage("Deploy Path Error !");
			setPageComplete(false);
			return false;
		}
		
		if (sizeCombo.getText() == null 
				|| !validateResolution(sizeCombo.getText().trim())){
			setErrorMessage("Resolution Select Error !");
			setPageComplete(false);
			return false;
		}

		MStudioDeployWizard depWizard = (MStudioDeployWizard) this.getWizard();
		
		if (!depWizard.isHost()) {
			if (0 > gal.getSelectionIndex() || 0 > ial.getSelectionIndex()
					|| gal == null || ial == null) {
				setErrorMessage("GAL & IAL setting Error !");
				setPageComplete(false);
				return false;
			}
		}
		IProject[] prjs = getDeployExeProjects();
		String socName = MStudioEnvInfo.getInstance().getCurSoCName();
		String building = "";
		if (depWizard.isDebug()){
			if (depWizard.isHost()){
				building ="Debug4Host";
			} else {
				building ="Debug4" + socName;
			}
		} else {
			if (depWizard.isHost()){
				building ="Release4Host";
			} else {
				building ="Release4" + socName;
			}
		}
		if (prjs != null && prjs.length > 0){
			for (int i = 0; i < prjs.length; i++){
				IManagedProject managedProj = ManagedBuildManager.getBuildInfo(prjs[i]).getManagedProject();
				IConfiguration[] cfg = managedProj.getConfigurations();

				for (int j = 0; j < cfg.length; j++){
					if (cfg[j].getName().equals(building) && cfg[j].needsRebuild()){
						setErrorMessage ("You Haven't build the " 
								+ building + " for project [" + prjs[i].getName() + "]");
						setPageComplete(false);
						return false;
					}
				}
			}
		}
		
		setErrorMessage(null);
		setPageComplete(true);
		return true;
	}

	public IProject[] getDeployExeProjects() {
		Object[] exeChecked = ctv.getCheckedElements();
		IProject[] exeProjects = MStudioDeployWizard.getExeProjects();
		ArrayList<String> sList = new ArrayList<String>();

		for (int i = 0; i < exeChecked.length; i++) {
			sList.add(exeChecked[i].toString());
		}

		List<IProject> list = new ArrayList<IProject>();

		for (int i = 0; i < exeProjects.length; i++) {
			if (sList.contains(exeProjects[i].getName())) {
				list.add(exeProjects[i]);
			}
		}

		return (IProject[])(list.toArray(new IProject[list.size()]));
	}

	public String getDeployLocation() {
		return locationPath.getStringValue().trim();
	}
	
	public String getResolution() {
		String resolution = sizeCombo.getItem(sizeCombo.getSelectionIndex()).trim();
		if (validateResolution(resolution))
			return resolution;
		return null;
	}

	public String getIALEngine() {
		return ial.getItem(ial.getSelectionIndex()).trim();
	}

	public String getGALEngine() {
		return gal.getItem(gal.getSelectionIndex()).trim();
	}

	public IWizardPage getNextPage() {
		MStudioDeployWizard wizard = (MStudioDeployWizard)getWizard();
		if (wizard == null)
			return null;
		
		if (MStudioDeployWizard.getModuleProjects().length <= 0
				&& MStudioDeployWizard.getIALProjects().length <= 0) {
			return wizard.getNextPage(this).getNextPage();
		}

		return wizard.getNextPage(this);
	}

	protected class SelectedChangeListener implements SelectionListener {
		public void widgetDefaultSelected(SelectionEvent e) {
			validatePage();
		}

		public void widgetSelected(SelectionEvent e) {
			validatePage();
		}
	}
	protected class ComboKeyListener implements KeyListener{
		
		public void keyPressed(KeyEvent e){
			validatePage();
		}
		
		public void keyReleased(KeyEvent e){
			validatePage();
		}
	}

	private boolean isValidPath(String path){
		Path p = new Path(path);
		if(p == null)
			return false;
		return p.isValidPath(path);
	}

	private boolean checkLocation(){
		if(!checkIsDefaultLocation(locationPath.getStringValue())){
			if(!openConfirm(getShell(),
					MStudioMessages.getString("MStudioDeployPreferencePage.pathWarningTitile"),
					MStudioMessages.getString("MStudioDeployPreferencePage.pathWarning").
					replace("${DIR}", locationPath.getStringValue()))){
					locationPath.setStringValue(getDefaultDeployLocationPath());
			}
		}
		return true;
	}
	
	public String getDefaultDeployLocationPath(){
		IPreferenceStore store = MStudioPlugin.getDefault().getPreferenceStore();
		if(store == null)
			return null;
		if (store.contains(MStudioPreferenceConstants.MSTUDIO_DEPLOY_LOCATION))
		{
			String prefLocation = store.getString(MStudioPreferenceConstants.MSTUDIO_DEPLOY_LOCATION);
			if(prefLocation != null && prefLocation != "" && isValidPath(prefLocation))
				return prefLocation;	    
		}
		
		return MStudioPlugin.getDefault().getMStudioEnvInfo().getDefaultLocationPath();
	}
	
	public final static int QUESTION = 3;
	public final String OK_LABEL = "&Ok";
	public final String RETURN_TO_DEFAULT = "&Set Defaults";
	
	public boolean openConfirm(Shell parent, String title, String message) {
        MessageDialog dialog = new MessageDialog(parent, title, null,
                message, QUESTION, new String[] { OK_LABEL, RETURN_TO_DEFAULT}, 0);
        return dialog.open() == 0;
    }
}

