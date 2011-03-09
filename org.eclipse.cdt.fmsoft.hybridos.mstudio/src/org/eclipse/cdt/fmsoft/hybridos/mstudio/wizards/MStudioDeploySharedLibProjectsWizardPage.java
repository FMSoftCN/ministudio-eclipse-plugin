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

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

public class MStudioDeploySharedLibProjectsWizardPage extends WizardPage {

	private Table tableLabraries;
	private CheckboxTableViewer ctvLabraries;
	private Table tableIAL;
	private CheckboxTableViewer ctvIAL;
	
	public MStudioDeploySharedLibProjectsWizardPage(String pageName) {
		super(pageName);
		init();
	}

	private void init() {
		setTitle(MStudioMessages.getString("MStudioDeployWizardPage.selectLibProjects.pageTitle"));
		setDescription(MStudioMessages.getString("MStudioDeployWizardPage.selectLibProjects.desc"));
	}

	public MStudioDeploySharedLibProjectsWizardPage(String pageName,
			String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		init();
	}

	@Override
	public void createControl(Composite parent) {	
		Composite topPanel;
		topPanel = new Composite(parent, SWT.NONE);
		topPanel.setLayout(new GridLayout());
		topPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		
		Label label1 = new Label(topPanel,SWT.NONE);
		label1.setText(MStudioMessages.getString("MStudioDeployWizardPage.selectLibProjects.dynamicTitle"));
		label1.setLayoutData(new GridData());
		
		tableLabraries = new Table(topPanel, SWT.BORDER | SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
		GridData gd = new GridData(GridData.FILL_BOTH);
		tableLabraries.setLayoutData(gd);
		
		ctvLabraries = new CheckboxTableViewer(tableLabraries);
		
		ctvLabraries.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
			}
		});
		ctvLabraries.addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent event) {
			}
		});
		Label label2 = new Label(topPanel,SWT.NONE);
		label2.setText(MStudioMessages.getString("MStudioDeployWizardPage.selectLibProjects.ialTitle"));
		tableIAL = new Table(topPanel,SWT.BORDER | SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL |SWT.SINGLE);
		GridData gd2 = new GridData(GridData.FILL_BOTH);
		tableIAL.setLayoutData(gd2);
		ctvIAL = new CheckboxTableViewer(tableIAL);
		ctvIAL.setAllGrayed(true);
		ctvIAL.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				ctvIAL.setAllChecked(false);
				ctvIAL.setChecked(event.getElement(), event.getChecked());				
			}
		});
		ctvIAL.addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent event) {
				
			}
		});
		
		initDeploySharedLibTable();
		initIALTable();
		
		setControl(topPanel);
		setPageComplete(true);
	}

	//change the state with the last wizard page select state
	//public void update(){
		//ctvLabraries.setItemCount(0);
		//ctvIAL.setItemCount(0);
		//initDeploySharedLibTable();		
		//initIALTable();
	//}
	
	private void initDeploySharedLibTable(){
		IProject[] libProjects = MStudioDeployWizard.getModuleProjects();
		if(libProjects == null)
			return;
		ArrayList<String> list = new ArrayList<String>();
		ArrayList<String> checkedList = new ArrayList<String>();
		for(int i=0; i<libProjects.length; i++){
			list.add(libProjects[i].getName());
			boolean bDeployable = new MStudioProject(libProjects[i]).getDefaultDeployable();
			if (bDeployable) {
				checkedList.add(libProjects[i].getName());
			}
		}		
		ctvLabraries.add(list.toArray(new String[libProjects.length]));
		ctvLabraries.setCheckedElements(checkedList.toArray(new String[checkedList.size()]));
	}
	
	private void initIALTable(){
		IProject[] ial = MStudioDeployWizard.getIALProjects();
		if(ial == null)
			return;
		ArrayList<String> list = new ArrayList<String>();
		for(int i=0; i<ial.length; i++){
			list.add(ial[i].getName());
		}
		ctvIAL.add(list.toArray(new String[ial.length]));
	}
	
	public IProject getDeployIALProject() {
		IProject[] ial = MStudioDeployWizard.getIALProjects();
		Object[] s = ctvIAL.getCheckedElements();
		if(s.length <= 0)
			return null;
		else{
			String sChecked = s[0].toString();
			for(int i=0; i<ial.length; i++){
				if(ial[i].getName().equals(sChecked)){
					return ial[i];
				}
			}
		}		
		return null;
	}
	
	public IProject[] getDeploySharedLibProjects() {
		IProject[] lab = MStudioDeployWizard.getModuleProjects();
		Object[] s = ctvLabraries.getCheckedElements();
		ArrayList<String> sList = new ArrayList<String>();
		for(int i=0; i<s.length; i++){
			sList.add(s[i].toString());
		}
		List<IProject> list = new ArrayList<IProject>();
		for(int i=0; i<lab.length; i++){
			if(sList.contains(lab[i].getName())){
				list.add(lab[i]);
			}
		}
		return (IProject[])(list.toArray(new IProject[list.size()]));
	}
	
	public IProject[] getMginitProjects() {
		IProject[] msProjects = getDeploySharedLibProjects();
		List<IProject> sProj = new ArrayList<IProject>();
		for (int i = 0; i < msProjects.length ; i++) {
			MStudioProject mpr = new MStudioProject(msProjects[i]);
			if (mpr.isMginitModuleTmplType()) {
				sProj.add(msProjects[i]);
			}
		}

		return (IProject[])sProj.toArray(new IProject[sProj.size()]);
	}
	
	public IWizardPage getNextPage() {
		MStudioDeployWizard wizard = (MStudioDeployWizard) getWizard();
		if(wizard == null)
			return null;	
		//wizard.getDeployServiceWizardPage().update();
		return wizard.getNextPage(this);
	}
}
