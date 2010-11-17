package org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class MStudioDeploySharedLibProjectsWizardPage extends WizardPage {

	public MStudioDeploySharedLibProjectsWizardPage(String pageName) {
		super(pageName);
		// TODO Auto-generated constructor stub
		setTitle(MStudioMessages.getString("MStudioDeployWizardPage.selectLibProjects.pageTitle"));
		setDescription(MStudioMessages.getString("MStudioDeployWizardPage.selectLibProjects.desc"));

	}

	public MStudioDeploySharedLibProjectsWizardPage(String pageName,
			String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
		//MStudioDeployWizard wizard = (MStudioDeployWizard) getWizard();		
		//IProject[] libProjects, ialProjects;
		//libProjects = wizard.getModuleProjects();
		//ialProjects = wizard.getIALProjects();
		
		Composite topPanel;
		topPanel = new Composite(parent, SWT.NONE);
		topPanel.setLayout(new GridLayout());
		topPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		setControl(topPanel);
		setPageComplete(true);

	}

	public IProject getDeployIALProject() {
		return null;
	}
	
	public IProject[] getDeploySharedLibProjects() {
		return null;
	}
}
