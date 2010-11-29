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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.preferences.MStudioDeployPreferencePage;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
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

public class MStudioDeployExecutableProjectsWizardPage extends WizardPage {

	private Table chooseTable;
	private CheckboxTableViewer ctv;
	private IProject[] projects = null;
	private Combo sizeCombo = null;
	private Combo colorCombo = null;
	private Combo gal = null;
	private Combo ial = null;
	private Composite bottomPanel4;
	private DirectoryFieldEditor locationPath;
	
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
		
		//tipText = new Label (topPanel,SWT.NONE);
		//init the Graphic
		//bottomPanel1		
		Composite bottomPanel1 = new Composite(topPanel,SWT.NONE);
		bottomPanel1.setLayout(new GridLayout(1,false));
		bottomPanel1.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		chooseTable = new Table(bottomPanel1, SWT.BORDER | SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL | SWT.IMAGE_BMP);
		chooseTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		ctv = new CheckboxTableViewer(chooseTable);
		ctv.addSelectionChangedListener(new ISelectionChangedListener(){
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				validatePage();
			}});
		initExeProjects();		
		//bottomPanel2		
		Composite bottomPanel2 = new Composite(topPanel,SWT.NONE);
		bottomPanel2.setLayout(new GridLayout());
		bottomPanel2.setLayoutData(new GridData(GridData.FILL_BOTH));
		locationPath = new DirectoryFieldEditor("filePath",MStudioMessages.getString("MStudioDeployWizardPage.selectExeProjects.locationTitle"),bottomPanel2);
	    locationPath.getTextControl(bottomPanel2).addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validatePage();
				}});
		//bottomPanel3	    
	    locationPath.setStringValue(MStudioDeployPreferencePage.deployLocation());	    	
		Composite bottomPanel3 = new Composite(topPanel,SWT.NONE);
		bottomPanel3.setLayout(new GridLayout(4,false));
		bottomPanel3.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label title = new Label(bottomPanel3,SWT.FILL);
		title.setText(MStudioMessages.getString("MStudioDeployWizardPage.selectExeProjects.configTitle"));
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.horizontalSpan = 4;
		title.setLayoutData(gd);
		
		Label sizeLabel = new Label(bottomPanel3,SWT.NONE);
		sizeLabel.setText(MStudioMessages.getString("MStudioDeployWizardPage.selectExeProjects.resolutionLabel"));
		sizeCombo = new Combo(bottomPanel3,SWT.READ_ONLY);		
		sizeCombo.addSelectionListener(new SelectedChangeListener());
		
		Label colorLabel = new Label(bottomPanel3,SWT.NONE);
		colorLabel.setText(MStudioMessages.getString("MStudioDeployWizardPage.selectExeProjects.colorLabel"));
		colorCombo = new Combo(bottomPanel3,SWT.READ_ONLY);
		colorCombo.addSelectionListener(new SelectedChangeListener());
		initSizeAndColor();
				
		bottomPanel4 = new Composite(topPanel,SWT.NONE);
		bottomPanel4.setLayout(new GridLayout(4,false));
		bottomPanel4.setLayoutData(new GridData(GridData.FILL_BOTH));
			
		Label galLabel = new Label(bottomPanel4,SWT.NONE);
		galLabel.setText(MStudioMessages.getString("MStudioDeployWizardPage.selectExeProjects.galLabel"));
		gal = new Combo(bottomPanel4,SWT.READ_ONLY);
		gal.addSelectionListener(new SelectedChangeListener());
		Label ialLabel=new Label(bottomPanel4,SWT.NONE);
		ialLabel.setText(MStudioMessages.getString("MStudioDeployWizardPage.selectExeProjects.ialLabel"));
		ial = new Combo(bottomPanel4,SWT.READ_ONLY);
		ial.addSelectionListener(new SelectedChangeListener());
		initGALAndIAL();
		
		setControl(topPanel);
		setPageComplete(false);
	}
	public boolean locationChanged() {
		  String l = locationPath.getStringValue();
		  File file = new File(l);
		  if (!file.exists()) 			  
			  return false;
		  else 
			  return true;
	}
	
	private void initGALAndIAL(){
		String[] galP = MStudioEnvInfo.getInstance().getGalEngines();
		if(galP != null){
			for(int i=0; i<galP.length; i++){
				gal.add(galP[i].toString());
			}
		}
		String[] ialP = MStudioEnvInfo.getInstance().getIalEngines();
		if(ialP != null){
			for(int i=0; i<ialP.length; i++){
				ial.add(ialP[i].toString());
			}
		}
	}
	private void initSizeAndColor(){
		String[] resolution = new String[]{"320x240","640x480","1024x768"};
		String[] colorDepth = new String[]{"16","24","32"};
		for(int i=0; i<resolution.length; i++){
			sizeCombo.add(resolution[i].toString());
		}
		//set the default value
		sizeCombo.select(0);
		for(int i=0; i<colorDepth.length; i++){
			colorCombo.add(colorDepth[i].toString());
		}
		colorCombo.select(0);
	}
	public void update(){
		bottomPanel4.setVisible(!MStudioDeployWizard.deployTypeIsHost);
		validatePage();
	}
	/*
	private String[] getIALProject() {
		String[] ialString = null;
		ialProject = MStudioDeployWizard.getIALProjects();		
		if(ialProject != null){
			ialString = new String[ialProject.length];
			for(int i=0; i<ialProject.length; i++){
				ialString[i] = ialProject[i].getName().toString();
			}
		}
		return ialString;
	}

	private String[] getGALProject() {
		String[] galString = null;
		galProject = MStudioDeployWizard.getExeProjects();
		if(galProject != null){
			galString = new String[galProject.length];
			for(int i=0; i<galProject.length; i++){
				galString[i] = galProject[i].getName().toString();
			}
		}
		return galString;
	}
*/
	private void initExeProjects() {
		projects = MStudioDeployWizard.getExeProjects();
		if(projects == null)
			return;			
		ArrayList<String> list = new ArrayList<String>();
		for(int i=0; i < projects.length; i++){			
			list.add(projects[i].getName());
		}			
		ctv.add(list.toArray(new String[projects.length]));
	}
	
	private boolean validateResolution(String resolution) {
		String regexString = "[1-9]+[0-9]*[xX][1-9]+[0-9]*";		
		return Pattern.matches(regexString, resolution);		
	}
	
	protected boolean validatePage() {	
		if(sizeCombo ==null || colorCombo==null){			
			setPageComplete(false);
			return false;			
		}
		if(!locationChanged() || sizeCombo.getSelectionIndex() < 0 
				|| colorCombo.getSelectionIndex() < 0){
			setPageComplete(false);
			return false;
		}	
		//select target		
		if(!MStudioDeployWizard.deployTypeIsHost){
			if(0 > gal.getSelectionIndex() || 0 > ial.getSelectionIndex() || gal==null || ial==null){	
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
		
		for(int i=0; i<exeChecked.length; i++){
			sList.add(exeChecked[i].toString());
		}
		
		List<IProject> list = new ArrayList<IProject>();
		for(int i=0; i<exeProjects.length; i++){
			if(sList.contains(exeProjects[i].getName())){
				list.add(exeProjects[i]);
			}
		}
		return (IProject[])(list.toArray(new IProject[list.size()]));
	}
	
	public String getDeployLocation() {
		return locationPath.getStringValue().trim();
//		return filePath.getStringValue();
	}
	
	public String getColorDepth(){
		return colorCombo.getItem(colorCombo.getSelectionIndex()).trim();
	}
	//resolution format: 320x240
	public String getResolution() {
		String resolution = sizeCombo.getItem(sizeCombo.getSelectionIndex()).trim();
		if(validateResolution(resolution))
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
		if(wizard == null)
			return null;
		wizard.getDeploySharedLibWizardPage().update();
		return wizard.getNextPage(this);
	}
	
	protected class SelectedChangeListener implements SelectionListener{
		public void widgetDefaultSelected(SelectionEvent e) {			
			validatePage();			
		}
		public void widgetSelected(SelectionEvent e) {
			validatePage();
		}		
	}
}
