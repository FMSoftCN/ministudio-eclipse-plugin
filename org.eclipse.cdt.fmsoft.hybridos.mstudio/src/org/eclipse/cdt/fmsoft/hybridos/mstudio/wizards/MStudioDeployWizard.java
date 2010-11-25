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

	public static boolean deployTypeIsHost = false;
	
	private MStudioParserIniFile iniFile = null;
	private final static String DEPLOY_INI_PATH = "/opt/hybridos/";
	private final static String DEPLOY_INI_NAME = "deploy.ini";	
	
	private final static String DEPLOY_CFG_SECTION = "deploy_cfgs";
	private final static String DEPLOY_SERVICES_SECTION = "deploy_services";
	private final static String DEPLOY_DLCUSTOM_SECTION = "deploy_dlcustom";
	private final static String DEPLOY_MODULES_SECTION = "deploy_modules";
	private final static String DEPLOY_AUTOBOOT_SECTION = "deploy_autoboot";
	private final static String DEPLOY_APPS_SECTION = "deploy_apps";
	private final static String USER_LOGIN_SECTION = "userlogin";
	private final static String SOCKET_SERVICE_SECTION = "socket_service";
	
	private final static String SERVICES_NUMBER_PROPERTY = "services_number";
	private final static String SERVICE_NAME_PROPERTY = "name";
	
	private final static String AUTOBOOT_NUMBERS_PROPERTY = "autoboot_numbers";
	private final static String AUTOBOOT_NAME_PROPERTY = "name";
	
/*	private final static String MINIGUI_CFG_PROPERTY = "minigui_cfg";
	private final static String MGNCS_CFG_PROPERTY = "mgncs_cfg";
	private final static String MINIGUI_RUNMODE_PROPERTY = "minigui_runmode";
	
	
	private final static String DLCUSTOM_PROGRAM_PROPERTY = "program";
	
	private final static String MODULES_NUMBERS_PROPERTY = "modules_numbers";
	private final static String MODULES_NAME_PROPERTY = "name";
	private final static String MODULES_PROGRAM_PROPERTY = "program";
	private final static String MODULES_PROGRAM_DEPLOY_PROPERTY = "program_deploy";
	private final static String MODULES_PROGRAM_CFG_PROPERTY = "program_cfg";
	private final static String MODULES_RESPACK_PROPERTY = "respack";
	private final static String MODULES_RESPACK_DEPLOY_PROPERTY = "respack_deploy";
	private final static String MODULES_DEPLIBS_PROPERTY = "deplibs";
	private final static String MODULES_DEPLIBS_DEPLOY_PROPERTY = "deplibs_deploy";
	
	
	private final static String APPS_NUMBER_PROPERTY = "apps_number";
	private final static String APPS_NAME_PROPERTY = "name";
	
	private final static String USER_LOGIN_PROGRAM_PROPERTY = "program";
	private final static String USER_LOGIN_PROGRAM_CFG_PROPERTY = "program_cfg";
	private final static String USER_LOGIN_PROGRAM_DEPLOY_PROPERTY = "program_deploy";
	private final static String USER_LOGIN_RESPACK_PROPERTY = "respack";
	private final static String USER_LOGIN_RESPACK_DEPLOY_PROPERTY = "respack_deploy";
	private final static String USER_LOGIN_DEPLIBS_PROPERTY = "deplibs";
	private final static String USER_LOGIN_DEPLIBS_DEPLOY_PROPERTY = "deplibs_deploy";

	private final static String SOCKET_SERVICES_PROPERTY = "program";
	private final static String SOCKET_SERVICES_PROGRAM_CFG_PROPERTY = "program_cfg";
	private final static String SOCKET_SERVICES_PROGRAM_DEPLOY_PROPERTY = "program_deploy";
	private final static String SOCKET_SERVICES_RESPACK_PROPERTY = "respack";
	private final static String SOCKET_SERVICES_RESPACK_DEPLOY_PROPERTY = "respack_deploy";
	private final static String SOCKET_SERVICES_DEPLIBS_PROPERTY = "deplibs";
	private final static String SOCKET_SERVICES_DEPLIBS_DEPLOY_PROPERTY = "deplibs_deploy";
	*/
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
		saveDeployIniFile();
		return true;
	}

	public boolean performCancel(){
		this.dispose();
		return true;
	}
	
	
	private boolean saveDeployInfo(String filename) {
		return false;
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
	
	public MStudioParserIniFile getDeployIniFile() {
		return iniFile;
	}

	public boolean saveDeployIniFile() {
		iniFile = new MStudioParserIniFile(DEPLOY_INI_PATH + DEPLOY_INI_NAME);
		if (null == iniFile)
			return false;
		
		modifyCfgsSection();
		modifyServicesSection();
		modifyDlcustomSection();
		modifyModulesSection();
		modifyAutobootSection();
		modifyAppsSection();
		modifyUserLoginSection();	
		modifySocketServiceSection();
	
		iniFile.save();
		return true;
	}
	
	private boolean modifyCfgsSection() {
		iniFile.removeSection(DEPLOY_CFG_SECTION);
		iniFile.addSection(DEPLOY_CFG_SECTION, null);
/*		iniFile.setStringProperty(DEPLOY_CFG_SECTION, MINIGUI_CFG_PROPERTY, 
				"/home/eclipse/workspace/.metadata/MiniGUI.cfg.target", null);
		iniFile.setStringProperty(DEPLOY_CFG_SECTION, MGNCS_CFG_PROPERTY, 
				"/home/eclipse/WorkSpace/.metadata/mgncs.cfg", null);
		iniFile.setStringProperty(DEPLOY_CFG_SECTION, MINIGUI_RUNMODE_PROPERTY, 
				"ths", null);
*/			
		return true;
	}
	
	private boolean modifyServicesSection() {
		iniFile.removeSection(DEPLOY_SERVICES_SECTION);
		iniFile.addSection(DEPLOY_SERVICES_SECTION, null);
		
		Object[] serv = deployServicesPage.getDeployServices();
		if (null == serv) {
			return false;
		}
		
		iniFile.setIntegerProperty(DEPLOY_SERVICES_SECTION, SERVICES_NUMBER_PROPERTY, 
				serv.length, null);
		
		if(serv.length >= 1) {
			for(int i=0; i<serv.length; i++) {
				iniFile.setStringProperty(DEPLOY_SERVICES_SECTION, (SERVICE_NAME_PROPERTY + i), 
					serv[i].toString(), null);
			}			
		}	
		return true;
	}
	
	private boolean modifyDlcustomSection() {
		iniFile.removeSection(DEPLOY_DLCUSTOM_SECTION);
		iniFile.addSection(DEPLOY_DLCUSTOM_SECTION, null);
//		iniFile.setStringProperty(DEPLOY_DLCUSTOM_SECTION, DLCUSTOM_PROGRAM_PROPERTY, 
//				"/home/xwyan/WorkSpace/dlcustom/Release4S3C2410/libdlcustom.so", null);
		return true;
	}
	
	private boolean modifyModulesSection() {
		return true;
	}

	private boolean modifyAutobootSection() {
		iniFile.removeSection(DEPLOY_AUTOBOOT_SECTION);
		iniFile.addSection(DEPLOY_AUTOBOOT_SECTION, null);
		
		IProject[] projects = autobootProjectPage.getDeployAutobootProjects();
		if (null == projects) {
			return false;
		}
		
		iniFile.setIntegerProperty(DEPLOY_AUTOBOOT_SECTION, AUTOBOOT_NUMBERS_PROPERTY, 
				projects.length, null);
		
		if(projects.length >= 1) {
			for (int i=0; i<projects.length; i++) {
				iniFile.setStringProperty(DEPLOY_AUTOBOOT_SECTION, (AUTOBOOT_NAME_PROPERTY + i), 
						projects[i].getName(), null);
			}			
		}	
		return true;
	}
	
	private boolean modifyAppsSection() {
		return true;
	}
	
	private boolean modifyUserLoginSection() {
		return true;
	}
	
	private boolean modifySocketServiceSection() {
		return true;
	}
}
