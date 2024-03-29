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
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo.MiniGUIRunMode;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class MStudioDeployAutobootProjectsWizardPage extends WizardPage {

	private Table table=null;
	private Button selectAll=null;
	private Button upButton,downButton;
	private IProject[] projects;	
	private ArrayList<String> projectOfChecked = new ArrayList<String>();
	
	public MStudioDeployAutobootProjectsWizardPage(String pageName) {
		super(pageName);
		init();
	}

	private void init() {
		setTitle(MStudioMessages.getString("MStudioDeployWizardPage.selectAutobootProjects.pageTitle"));
		setDescription(MStudioMessages.getString("MStudioDeployWizardPage.selectAutobootProjects.desc"));
	}

	public MStudioDeployAutobootProjectsWizardPage(String pageName,
			String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		init();
	}
	
	public void createControl(Composite parent) {
		Composite topPanel;
		topPanel = new Composite(parent, SWT.NONE);
		topPanel.setLayout(new GridLayout());
		topPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite com1 = new Composite(topPanel,SWT.NONE);
		com1.setLayout(new GridLayout());
		com1.setLayoutData(new GridData());
		Label title = new Label(com1,SWT.FILL);
		title.setText(MStudioMessages.getString("MStudioDeployWizardPage.selectAutobootProjects.title"));
		title.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite com2 = new Composite(topPanel,SWT.NONE);		
		com2.setLayout(new GridLayout(2,false));
		com2.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridData dg = new GridData(GridData.FILL_BOTH);
		dg.verticalSpan = 2;
		
		table=new Table(com2, SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL |
				SWT.FULL_SELECTION | SWT.FILL | SWT.MULTI | SWT.BORDER);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayout(new GridLayout());
		table.setLayoutData(dg);
		table.addListener(SWT.Selection,new TableListener());
		
		TableColumn col1 = new TableColumn(table,SWT.NONE);
		col1.setText(MStudioMessages.getString("MStudioDeployWizardPage.selectAutobootProjects.tableCol0Title"));
		col1.setWidth(50);
		
		TableColumn col2 = new TableColumn(table,SWT.NONE); 	
		col2.setText(MStudioMessages.getString("MStudioDeployWizardPage.selectAutobootProjects.tableCol1Title"));
		col2.setWidth(100);
		
		upButton = new Button(com2,SWT.NONE);
		upButton.setLayoutData(new GridData(GridData.FILL));
		upButton.setText(MStudioMessages.getString("MStudioDeployWizardPage.selectAutobootProjects.upButtonText"));
		upButton.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
			public void widgetSelected(SelectionEvent e) {
				int index = table.getSelectionIndex();
				if(projects != null && index < projects.length){
					keepCheckedItem();
					IProject p = projects[index];
					projects[index] = projects[index-1];
					projects[index-1] = p;
					updateTable();
					table.setSelection(index-1);
					updateButtons();
				}				
			}			
			});
		downButton = new Button(com2,SWT.NONE);
		downButton.setLayoutData(new GridData(GridData.FILL));
		downButton.setText(MStudioMessages.getString("MStudioDeployWizardPage.selectAutobootProjects.downButtonText"));
		downButton.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {	
			}
			public void widgetSelected(SelectionEvent e) {
				int index = table.getSelectionIndex();
				if(projects != null && index < projects.length){
					keepCheckedItem();
					IProject p = projects[index];
					projects[index] = projects[index+1];
					projects[index+1] = p;
					updateTable();
					table.setSelection(index+1);					
					updateButtons();
				}
			}			
		});
				
		selectAll = new Button(topPanel,SWT.CHECK);
		selectAll.setText(MStudioMessages.getString("MStudioDeployWizardPage.selectAutobootProjects.selectAllButtonText"));
		selectAll.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				TableItem[] items = table.getItems();
				if(selectAll.getSelection()){	
					projectOfChecked.clear();
					for(int i=0; i<items.length; i++){
						items[i].setChecked(true);	
						projectOfChecked.add(items[i].getText(1));
					}					
					//setPageComplete(true);
				}
				else{	
					for(int i=0; i<items.length; i++){
						items[i].setChecked(false);						
					}
					projectOfChecked.clear();
					//setPageComplete(false);					
				}
			}});
		
		initAutoStartProgrames();		
		setControl(topPanel);
		setPageComplete(true);
	}
	public void update(){
		table.setItemCount(0);
		initAutoStartProgrames();
	}
	//keep the state of the checked items
	private void keepCheckedItem() {
		projectOfChecked.clear();
		TableItem[] itm = table.getItems();
		for(int i=0; i<itm.length; i++){
			if(itm[i].getChecked()){
				projectOfChecked.add(itm[i].getText(1));
			}
		}
	}
	private void updateButtons() {		
		if(MStudioEnvInfo.getInstance().getMgRunMode() == MiniGUIRunMode.thread.name()){
			upButton.setEnabled(false);
			downButton.setEnabled(false);
			selectAll.setEnabled(false);
		}
		else{
			if(projects.length <=0 || projects == null || table.getSelectionCount()<=0){
				//setPageComplete(false);
				upButton.setEnabled(false);
				downButton.setEnabled(false);
				return;
			}
			int index = table.getSelectionIndex();	
			if(index < 0){
				upButton.setEnabled(false);
				downButton.setEnabled(false);
			}
			else if(index == 0){
				upButton.setEnabled(false);
				downButton.setEnabled(true);
			}
			else if(index >0 && index < table.getItemCount()-1){
				upButton.setEnabled(true);
				downButton.setEnabled(true);
			}
			else if(index == table.getItemCount()-1){
				upButton.setEnabled(true);
				downButton.setEnabled(false);
			}
			if(table.getItemCount() <= 1){
				upButton.setEnabled(false);
				downButton.setEnabled(false);
				return;
			}
		}
	}
	
	private void updateTable(){
		table.setItemCount(0);
		if(projects != null){			
			for(int i=0; i<projects.length; i++){
				TableItem item = new TableItem(table,SWT.CHECK);
				item.setText(1,projects[i].getName());
				if(projectOfChecked != null && projectOfChecked.contains(projects[i].getName()))
					item.setChecked(true);
			}
		}		
	}
	//init the table data
	private void initAutoStartProgrames(){
		MStudioDeployWizard wizard = (MStudioDeployWizard) getWizard();
		if(wizard == null)
			return;
		if(projects != null && projects.length > 0){
			IProject[] exeProjects = wizard.getDeployExeProjects();
			if(exeProjects == null)
				projects = null;
			else{
				//
				List<IProject> exeList =  new ArrayList<IProject>();
				List<IProject> storeList = new ArrayList<IProject>();
				for(int i=0; i<exeProjects.length; i++){
					exeList.add(exeProjects[i]);
				}				
				for(int i=0; i<projects.length; i++){
					storeList.add(projects[i]);
				}
				//autobutooPage have exePage not have,delete autobutooPage Project
				for(int i=0; i<projects.length; i++){
					if(!exeList.contains(projects[i])){
						storeList.remove(projects[i]);
						if(projectOfChecked != null && projectOfChecked.contains(projects[i].getName())){
							projectOfChecked.remove(projects[i].getName());
						}
					}
				}
				//autobutooPage not have exePage have ,add to autobutooPage
				for(int i=0; i<exeProjects.length; i++){
					//if back here the table not checked before
					if(!storeList.contains(exeProjects[i])){
						storeList.add(exeProjects[i]);
					}
				}
				projects = (IProject[])storeList.toArray(new IProject[storeList.size()]);
			}			
		}
		else
			projects = wizard.getDeployExeProjects();		
		updateTable();	
		updateButtons();
	}
	
	public IProject[] getDeployAutobootProjects() {
		MStudioDeployWizard wizard = (MStudioDeployWizard) getWizard();
		if (wizard == null || projects == null || projects.length <= 0)
			return null;
		IProject[] exeProjects = wizard.getDeployExeProjects();
		List<IProject> exeList=new ArrayList<IProject>();
		List<IProject> list = new ArrayList<IProject>();
		for(int i=0; i<exeProjects.length; i++){
			exeList.add(exeProjects[i]);
		}
		for (int i = 0; i < projects.length; i++) {
			if(projectOfChecked.contains(projects[i].getName()) && exeList.contains(projects[i])){
				list.add(projects[i]);
			}
		}
	
		return (IProject[]) (list.toArray(new IProject[list.size()]));
	}

	public class TableListener implements Listener{
		public void handleEvent(Event event) {
			if(event.detail == SWT.CHECK){
				if(MStudioEnvInfo.getInstance().getMgRunMode() != MiniGUIRunMode.process.name()){
					//thread mode
					TableItem[] items = table.getItems();
					for(int i=0;i<items.length;i++){
						if(!items[i].equals(event.item))
							items[i].setChecked(false);
					}
					//after update of the table to jump to the keepcheckedItem function
					keepCheckedItem();
				}
				else{
					//before update the buttons to jump to the keepCheckedItem function
					keepCheckedItem();
					if(projectOfChecked.size() == projects.length && projects != null)
						selectAll.setSelection(true);
					else
						selectAll.setSelection(false);
				}
			}
			updateButtons();						
		}		
	}
}
