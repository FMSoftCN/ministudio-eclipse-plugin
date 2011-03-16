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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.preferences.MStudioDeployPreferencePage;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProject;


public class MStudioDeployExecutableProjectsWizardPage extends WizardPage {

	private Combo sizeCombo = null;
	//private Combo colorCombo = null;
	private Combo gal = null;
	private Combo ial = null;
	private Composite bottomPanel = null;
	private Table chooseTable = null;
	private CheckboxTableViewer ctv = null;

	private IProject[] projects = null;
	private DirectoryFieldEditor locationPath = null;

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
			}});

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
				if(checkIsDefaultLocation(locationPath.getStringValue())){
					MessageDialog.openWarning(getShell(),
							MStudioMessages.getString("MStudioDeployPreferencePage.pathWarningTitile"),
							MStudioMessages.getString("MStudioDeployPreferencePage.pathWarning").
							replace("${DIR}", locationPath.getStringValue()));
				}
				validatePage();
				}});

		//locationPath.getTextControl(parent).setLayoutData(new GridData(150,20));
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
		sizeCombo = new Combo(bottomPanel3, SWT.READ_ONLY/*SWT.NONE*/);// TODO it later
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
		return !MStudioPlugin.getDefault().getMStudioEnvInfo().getDefaultLocationPath().equals(path);
	}
	
	public boolean locationChanged() {
		
		String localPath = locationPath.getStringValue();
		if(localPath == null || localPath=="")
			return false;
		if(!isValidPath(localPath)){
			locationPath.setErrorMessage(MStudioMessages.
					getString("MStudioDeployWizardPage.deployErrors.pathInValid"));
			locationPath.showErrorMessage();
			return false;
		}
		return true;
		/*
		File file = new File(localPath);

		if (!file.exists())
			return false;
		else
			return true;
			*/
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
		List<String> li = new ArrayList<String>();
		li = MStudioPlugin.getDefault().getMStudioEnvInfo().getResolutions();
		if(li == null)
			return;
		String[] resolution = li.toArray(new String[li.size()]);
		String tmpResolution = MStudioEnvInfo.getInstance().getScreenSize();
		if(tmpResolution == null)
			return;
		boolean bResolution = false;
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
		bottomPanel.setVisible(!MStudioDeployWizard.deployTypeIsHost);
		validatePage();
	}

	/*
	private String[] getIALProject() {

		String[] ialString = null;
		ialProject = MStudioDeployWizard.getIALProjects();

		if (ialProject != null) {
			ialString = new String[ialProject.length];
			for (int i = 0; i < ialProject.length; i++) {
				ialString[i] = ialProject[i].getName().toString();
			}
		}

		return ialString;
	}

	private String[] getGALProject() {

		String[] galString = null;
		galProject = MStudioDeployWizard.getExeProjects();

		if (galProject != null) {
			galString = new String[galProject.length];
			for (int i = 0; i < galProject.length; i++) {
				galString[i] = galProject[i].getName().toString();
			}
		}

		return galString;
	}
*/

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
		String regexString = "[1-9]+[0-9]*\\s*[x*]\\s*[1-9]+[0-9]*\\s*-\\s*\\d{1,2}bpp";
		//return Pattern.matches(regexString, resolution);
		return resolution.matches(regexString);
	}
	/*
	private boolean validateColorDepth(String colorDepth){
		String regexString = "[1-9]+[0-9]*";
		return Pattern.matches(regexString, colorDepth);
	}
*/
	protected boolean validatePage() {
		if (sizeCombo == null/* || colorCombo == null*/) {
			setPageComplete(false);
			return false;
		}
		if(!locationChanged() || (sizeCombo.getText().trim() == "" || !validateResolution(sizeCombo.getText().trim()))){
			setPageComplete(false);
			return false;
		}
		//select target
		
		if (!MStudioDeployWizard.deployTypeIsHost) {
			if (0 > gal.getSelectionIndex() || 0 > ial.getSelectionIndex()
					|| gal == null || ial == null) {
				setPageComplete(false);
				return false;
			}
		}
		
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
//		return filePath.getStringValue();
	}
/*
	public String getColorDepth() {
//		return colorCombo.getItem(colorCombo.getSelectionIndex()).trim();
		String color = colorCombo.getText().trim();
		if(validateColorDepth(color))
			return color;
		return null;
	}
*/
	//resolution format: 320x240-16bpp
	
	public String getResolution() {
		String resolution = sizeCombo.getItem(sizeCombo.getSelectionIndex()).trim();
		//String resolution = sizeCombo.getText().trim();
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

		//wizard.getDeploySharedLibWizardPage().update();
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
}

