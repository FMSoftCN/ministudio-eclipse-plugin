package org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

public class MStudioDeployExecutableProjectsWizardPage extends WizardPage {

	
	public MStudioDeployExecutableProjectsWizardPage(String pageName) {
		super(pageName);
		setTitle(MStudioMessages.getString("MStudioDeployWizardPage.selectExeProjects.pageTitle"));
		setDescription(MStudioMessages.getString("MStudioDeployWizardPage.selectExeProjects.desc"));
	}

	public MStudioDeployExecutableProjectsWizardPage(String pageName,
			String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	public void createControl(Composite parent) {
		Composite topPanel;
		topPanel = new Composite(parent, SWT.NONE);
		topPanel.setLayout(new GridLayout());
		topPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
		//init the Graphic
		//Label separate=new Lable();
		Composite bottomPanel=new Composite(topPanel,SWT.NONE);
		bottomPanel.setLayout(new GridLayout());
		bottomPanel.setLayoutData(new GridData());
		//devide the bottomPanel to four panel
		//bottomPanel1
		Composite bottomPanel1=new Composite(bottomPanel,SWT.NONE);
		bottomPanel1.setLayout(new GridLayout());
		bottomPanel1.setLayoutData(new GridData());
		//add the Control to the bottomPanel1
		//ListEditor chooseList=new ListEditor();
		List c=new List(bottomPanel1, SWT.CHECK);
		c.add("test");
		//bottomPanel2
		Composite bottomPanel2=new Composite(bottomPanel,SWT.NONE);
		bottomPanel2.setLayout(new GridLayout());
		bottomPanel2.setLayoutData(new GridData());
		//bottomPanel3
		Composite bottomPanel3=new Composite(bottomPanel,SWT.NONE);
		bottomPanel3.setLayout(new GridLayout());
		bottomPanel3.setLayoutData(new GridData());
		//bottomPanel4
		Composite bottomPanel4=new Composite(bottomPanel,SWT.NONE);
		bottomPanel4.setLayout(new GridLayout());
		bottomPanel4.setLayoutData(new GridData());
		
		setControl(topPanel);
		setPageComplete(true);

	}
	
	private boolean validateResolution(String resolution) {
		return false;
	}
	
	protected boolean validatePage() {
		return true;
	}
	
	public IProject[] getDeployExeProjects() {
		return null;
	}
	
	public String getDeployLocation() {
		return null;
	}
	
	//resolution format: 320x240-16bpp
	public String getResolution() {
		return null;
	}
	
	public String getIALEngine() {
		return null;
	}
	
	public String getGALEngine() {
		return null;
	}
	
	public IWizardPage getNextPage() {
		MStudioDeployWizard wizard = (MStudioDeployWizard) getWizard();
		//IProject[] libProjects, ialProjects;
		
		//libProjects = wizard.getModuleProjects();
		//ialProjects = wizard.getIALProjects();
		
		//skip next page
		//if ((libProjects == null || libProjects.length <= 0)
		//		&& (ialProjects == null || ialProjects.length <= 0))
		//	return wizard.getNextPage(this).getNextPage();
		
		return wizard.getNextPage(this);
	}
}
