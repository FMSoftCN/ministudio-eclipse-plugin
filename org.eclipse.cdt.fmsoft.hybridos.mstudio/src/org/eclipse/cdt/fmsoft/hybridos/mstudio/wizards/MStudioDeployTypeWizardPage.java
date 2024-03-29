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

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

public class MStudioDeployTypeWizardPage extends WizardPage {
	enum MStudioDeployTargetType {
        Host,
        Target
	};

	enum MStudioDeployBuildType {
        Debug,
        Release 
	};
	//the default values
	private MStudioDeployTargetType targetType = MStudioDeployTargetType.Target;
	private MStudioDeployBuildType buildType = MStudioDeployBuildType.Release;
	
	public String getTargetType() {
		return targetType.name();
	}
	
	public String getBuildType() {
		return buildType.name();
	}
	
	public MStudioDeployTypeWizardPage(String pageName) {
		super(pageName);
		init();
	}

	private void init() {
		setTitle(MStudioMessages.getString("MStudioDeployWizardPage.selectType.pageTitle"));
		setDescription(MStudioMessages.getString("MStudioDeployWizardPage.selectType.desc"));
	}

	public MStudioDeployTypeWizardPage(String pageName, String title,
			ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		init();
	}

	@Override
	public void createControl(Composite parent) {
		Composite topPanel;
		topPanel = new Composite(parent, SWT.NONE);
		topPanel.setLayout(new GridLayout());
		topPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
		RadioGroupFieldEditor typeRadioGroup = new RadioGroupFieldEditor("deployType", 
				MStudioMessages.getString("MStudioDeployWizardPage.selectType.pageName"), 1,
			     new String[][] {{MStudioDeployTargetType.Host.name(), MStudioDeployTargetType.Host.name()},
							{MStudioDeployTargetType.Target.name(), MStudioDeployTargetType.Target.name()}},topPanel);
		
		Composite rg = typeRadioGroup.getRadioBoxControl(topPanel);
		Control[] radioButton = (Control[])(rg.getChildren());	
		//MStudioDeployWizard.deployTypeIsHost = false;
		if(targetType.name().equals(MStudioDeployTargetType.Host.name())){
			((Button)radioButton[0]).setSelection(true);
			((Button)radioButton[0]).setFocus();
		}
		else{
			((Button)radioButton[1]).setSelection(true);
			((Button)radioButton[1]).setFocus();
		}
		typeRadioGroup.setPropertyChangeListener(new IPropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent event) {
				if(!targetType.name().equals(event.getNewValue())){
					if(targetType == MStudioDeployTargetType.Target){
						targetType = MStudioDeployTargetType.Host;
						//MStudioDeployWizard.deployTypeIsHost = true;
					}
					else{
						targetType = MStudioDeployTargetType.Target;	
						//MStudioDeployWizard.deployTypeIsHost = false;
					}
				}
			}});
		new Label(topPanel,SWT.FULL_SELECTION | SWT.LINE_SOLID);
		RadioGroupFieldEditor rootfsRadioGroup = new RadioGroupFieldEditor("rootfs",MStudioMessages.getString("MStudioDeployWizardPage.selectRootfs.pageName"),1,
							new String[][]{{MStudioDeployBuildType.Release.name(),MStudioDeployBuildType.Release.name()},
							{MStudioDeployBuildType.Debug.name(),MStudioDeployBuildType.Debug.name()}},topPanel);
		//set the default choice value
		Composite tt = rootfsRadioGroup.getRadioBoxControl(topPanel);
		Control[] radioButtons = (Control[])(tt.getChildren());
		if(buildType.name().equals(MStudioDeployBuildType.Debug.name())){
			((Button)radioButtons[1]).setSelection(true);
		}
		else {
			((Button)radioButtons[0]).setSelection(true);
		}
		rootfsRadioGroup.setPropertyChangeListener(new IPropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent event) {
				if(!buildType.name().equals(event.getNewValue())){
					if(buildType == MStudioDeployBuildType.Release)
						buildType = MStudioDeployBuildType.Debug;
					else
						buildType = MStudioDeployBuildType.Release;					
				}	
			}			
		});
		
		new Label(topPanel,SWT.NONE);
		Color c = Display.getCurrent() .getSystemColor(SWT.COLOR_DARK_GRAY);
		Label releaseDes = new Label(topPanel,SWT.NONE);
		releaseDes.setForeground(c);
		releaseDes.setText(MStudioMessages.getString("MStudioDeployWizardPage.release.desc"));
		Label debugDes=new Label(topPanel,SWT.NONE);
		debugDes.setForeground(c);
		debugDes.setText(MStudioMessages.getString("MStudioDeployWizardPage.debug.desc"));
		setControl(topPanel);
		setPageComplete(true);
	}
	//ervery time init it or return to this page would be execute this function
	public IWizardPage getNextPage() {		
		MStudioDeployWizard wizard = (MStudioDeployWizard) this.getWizard();
        if (wizard == null) {
			return null;
		}
        wizard.getDeployExecuteableWizardPage().update();
        return wizard.getNextPage(this);
    }
}
