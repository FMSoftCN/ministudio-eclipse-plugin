package org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.Wizard;

public class MStudioDeployWizard extends Wizard{
	
	private MStudioDeployTypeWizardPage deployTypePage;
	private MStudioDeployExecutableProjectsWizardPage exeProjectPage;
	private MStudioDeploySharedLibProjectsWizardPage sharedLibPage;
	private MStudioDeployServicesWizardPage deployServicesPage;
	private MStudioDeployAutobootProjectsWizardPage autobootProjectPage;
	public MStudioEnvInfo envInfo;

	public static boolean deployTypeIsHost=false;
	public static boolean deployCanFinish=false;
	
	public MStudioDeployWizard() {
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
	
	public MStudioDeployTypeWizardPage getDeployTypeWizardPage(){
		return this.deployTypePage;
	}
	public MStudioDeployExecutableProjectsWizardPage getDeployExecuteableWizardPage(){
		return this.exeProjectPage;
	}
	public MStudioDeploySharedLibProjectsWizardPage getDeploySharedLibWizardPage(){
		return this.sharedLibPage;
	}
	public MStudioDeployServicesWizardPage getDeployServiceWizardPage(){
		return this.deployServicesPage;
	}
	public MStudioDeployAutobootProjectsWizardPage getDeployAutobootWizardPage(){
		return this.autobootProjectPage;
	}
	
	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean performCancel(){
		return true;
	}
	
	
	private boolean saveDeployInfo(String filename) {
		return false;
	}

	public boolean canFinish() {
		return deployCanFinish;
	}

	public boolean isHost() {
		return deployTypeIsHost;
	}
	
	//get executable projects in workspace
	public static IProject[] getExeProjects() {
		return MStudioEnvInfo.getExecutableProjects();
	}
	
	//get shared library projects in workspace, not include dlcustom ial project.
	public static IProject[] getModuleProjects() {
		return MStudioEnvInfo.getSharedLibProjects();
	}
	
	//get dlcustom ial projects in workspace.
	public static IProject[] getIALProjects() {
		return MStudioEnvInfo.getDlCustomProjects();
	}
	
	public IProject[] getDeployExeProjects() {
		return exeProjectPage.getDeployExeProjects();
	}

}
