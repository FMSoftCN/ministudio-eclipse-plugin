package org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class MStudioDeployExecutableProjectsWizardPage extends WizardPage {

	
	public MStudioDeployExecutableProjectsWizardPage(String pageName) {
		super(pageName);
		// TODO Auto-generated constructor stub
		setTitle(MStudioMessages.getString("MStudioDeployWizardPage.selectExeProjects.pageTitle"));
		setDescription(MStudioMessages.getString("MStudioDeployWizardPage.selectExeProjects.desc"));

	}

	public MStudioDeployExecutableProjectsWizardPage(String pageName,
			String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
		Composite topPanel;
		topPanel = new Composite(parent, SWT.NONE);
		topPanel.setLayout(new GridLayout());
		topPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
		
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
		IProject[] libProjects, ialProjects;
		
		libProjects = wizard.getModuleProjects();
		ialProjects = wizard.getIALProjects();
		
		//skip next page
		if ((libProjects == null || libProjects.length <= 0)
				&& (ialProjects == null || ialProjects.length <= 0))
			return wizard.getNextPage(this).getNextPage();
		
		return wizard.getNextPage(this);
	}
}
