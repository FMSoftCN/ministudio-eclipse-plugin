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

public class MStudioDeployServicesWizardPage extends WizardPage {

	public MStudioDeployServicesWizardPage(String pageName) {
		super(pageName);
		// TODO Auto-generated constructor stub
		setTitle(MStudioMessages.getString("MStudioDeployWizardPage.selectServices.pageTitle"));
		setDescription(MStudioMessages.getString("MStudioDeployWizardPage.selectServices.desc"));

	}

	public MStudioDeployServicesWizardPage(String pageName, String title,
			ImageDescriptor titleImage) {
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

	public String[] getDeployServices() {
		return null;
	}
	
	public IWizardPage getNextPage() {
		MStudioDeployWizard wizard = (MStudioDeployWizard) getWizard();
		IProject[] exeProjects = wizard.getDeployExeProjects();
		//skip next page
		if (exeProjects == null || exeProjects.length <= 0)
			return null;
			
		return wizard.getNextPage(this);
	}
}
