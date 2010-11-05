package org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards.MStudioDeployTypeWizardPage.MStudioDeployTargetType;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.Wizard;

public class MStudioDeployWizard extends Wizard {
	private MStudioDeployTypeWizardPage deployTypePage;
	private MStudioDeployExecutableProjectsWizardPage exeProjectPage;
	private MStudioDeploySharedLibProjectsWizardPage sharedLibPage;
	private MStudioDeployServicesWizardPage deployServicesPage;
	private MStudioDeployAutobootProjectsWizardPage autobootProjectPage;
	private IProject[] ialProjects;
	private IProject[] moduleProjects;
	public MStudioEnvInfo envInfo;
	
	public MStudioDeployWizard() {
		// TODO Auto-generated constructor stub
		setWindowTitle(MStudioMessages.getString("MStudioDeployWizard.title"));
	}

	public void addPages() {
		deployTypePage = new MStudioDeployTypeWizardPage(MStudioMessages.getString("MStudioDeployWizardPage.selectType.pageName"));
		addPage(deployTypePage);
		
		exeProjectPage = new MStudioDeployExecutableProjectsWizardPage(MStudioMessages.getString("MStudioDeployWizardPage.selectExeProjects.pageName"));
		addPage(exeProjectPage);
		
		sharedLibPage = new MStudioDeploySharedLibProjectsWizardPage(MStudioMessages.getString("MStudioDeployWizardPage.selectLibProjects.pageName"));
		addPage(sharedLibPage);
		
		deployServicesPage = new MStudioDeployServicesWizardPage(MStudioMessages.getString("MStudioDeployWizardPage.selectServices.pageName"));
		addPage(deployServicesPage);

		autobootProjectPage = new MStudioDeployAutobootProjectsWizardPage(MStudioMessages.getString("MStudioDeployWizardPage.selectAutobootProjects.pageName"));
		addPage(autobootProjectPage);
	}
	
	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return true;
	}

	private boolean saveDeployInfo(String filename) {
		return false;
	}

	public boolean canFinish() {
		return true;
	}

	public boolean isHost() {
		if (deployTypePage.getTargetType().equals(MStudioDeployTargetType.Host.name()))
			return true;
		return false;
	}
	
	//get executable projects in workspace
	public IProject[] getExeProjects() {
		return MStudioEnvInfo.getExecutableProjects();
	}
	
	//get shared library projects in workspace, not include dlcustom ial project.
	public IProject[] getModuleProjects() {
		return MStudioEnvInfo.getSharedLibProjects();
	}
	
	//get dlcustom ial projects in workspace.
	public IProject[] getIALProjects() {
		return MStudioEnvInfo.getDlCustomProjects();
	}
	
	public IProject[] getDeployExeProjects() {
		return exeProjectPage.getDeployExeProjects();
	}
}