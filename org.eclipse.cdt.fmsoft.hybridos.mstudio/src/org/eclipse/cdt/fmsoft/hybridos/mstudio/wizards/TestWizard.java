package org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.jface.wizard.Wizard;

public class TestWizard extends Wizard {

	private MStudioDeployTypeWizardPage deployTypePage;
	private MStudioDeployExecutableProjectsWizardPage exeProjectPage;
	private MStudioDeploySharedLibProjectsWizardPage sharedLibPage;
	private MStudioDeployServicesWizardPage deployServicesPage;
	private MStudioDeployAutobootProjectsWizardPage autobootProjectPage;
	
	public TestWizard(){
		setWindowTitle(MStudioMessages.getString("MStudioDeployWizard.title"));
	}
	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return false;
	}

	
}
