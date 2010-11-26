package org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileInputStream;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;

public class MStudioDeployWizard extends Wizard{
	
	private MStudioDeployTypeWizardPage deployTypePage;
	private MStudioDeployExecutableProjectsWizardPage exeProjectPage;
	private MStudioDeploySharedLibProjectsWizardPage sharedLibPage;
	private MStudioDeployServicesWizardPage deployServicesPage;
	private MStudioDeployAutobootProjectsWizardPage autobootProjectPage;

	public static boolean deployTypeIsHost = false;
	
	private MStudioParserIniFile iniFile = null;
	private final static String DEPLOY_INI_PATH = Platform.getInstanceLocation().getURL().getPath();
//	private String mgncsCFGNewName = null;
//	private String miniguiCFGNewName = null;
	private String miniguiCFGNewPath = null;
	private String mgncsCFGNewPath = null;
	private final static String DEPLOY_INI_NAME = "deploy.ini";
	
	private final static String DEPLOY_CFG_SECTION = "deploy_cfgs";
	private final static String DEPLOY_SERVICES_SECTION = "deploy_services";
	private final static String DEPLOY_DLCUSTOM_SECTION = "deploy_dlcustom";
	private final static String DEPLOY_MODULES_SECTION = "deploy_modules";
	private final static String DEPLOY_AUTOBOOT_SECTION = "deploy_autoboot";
	private final static String DEPLOY_APPS_SECTION = "deploy_apps";
	
	private final static String MINIGUI_CFG_PROPERTY = "minigui_cfg";
	private final static String MGNCS_CFG_PROPERTY = "mgncs_cfg";
	private final static String MINIGUI_RUNMODE_PROPERTY = "minigui_runmode";
	
	private final static String SERVICES_NUMBER_PROPERTY = "services_number";
	private final static String SERVICE_NAME_PROPERTY = "name";
	
	private final static String AUTOBOOT_NUMBERS_PROPERTY = "autoboot_numbers";
	private final static String AUTOBOOT_NAME_PROPERTY = "name";
	
	private final static String DLCUSTOM_PROGRAM_PROPERTY = "program";
	
	private final static String MODULES_NUMBERS_PROPERTY = "modules_numbers";
	private final static String MODULES_NAME_PROPERTY = "name";

	private final static String PROGRAM_PROPERTY = "program";
	private final static String PROGRAM_DEPLOY_PROPERTY = "program_deploy";
	private final static String PROGRAM_CFG_PROPERTY = "program_cfg";
	private final static String RESPACK_PROPERTY = "respack";
	private final static String RESPACK_DEPLOY_PROPERTY = "respack_deploy";
	private final static String DEPLIBS_PROPERTY = "deplibs";
	private final static String DEPLIBS_DEPLOY_PROPERTY = "deplibs_deploy";
	
	private final static String APPS_NUMBER_PROPERTY = "apps_number";
	private final static String APPS_NAME_PROPERTY = "name";
	
	private final static String MINIGUI_CFG_FILE_NAME = "MiniGUI.cfg";
	private final static String MGNCS_CFG_FILE_NAME = "mgncs.cfg";
	private final static String SYSTEM_SECTION = "system";
	private final static String GAL_PROPERTY = "gal_engine";
	private final static String IAL_PROPERTY = "ial_engine";
	private final static String DEFAULT_MODE_PROPERTY = "defaultmode";
	private final static String MODULES_DEPLOY_PATH="/usr/local/lib";
	private final static String APP_DEPLOY_PATH="/usr/bin";
	private final static String APP_CFG_PATH="_res.cfg";
	private final static String DEPLIBS_PROGRAM_DEPLOY="/usr/local/lib";

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
	
	public MStudioDeployTypeWizardPage getDeployTypeWizardPage() {
		return this.deployTypePage;
	}
	
	public MStudioDeployExecutableProjectsWizardPage getDeployExecuteableWizardPage() {
		return this.exeProjectPage;
	}
	
	public MStudioDeploySharedLibProjectsWizardPage getDeploySharedLibWizardPage() {
		return this.sharedLibPage;
	}
	
	public MStudioDeployServicesWizardPage getDeployServiceWizardPage() {
		return this.deployServicesPage;
	}
	
	public MStudioDeployAutobootProjectsWizardPage getDeployAutobootWizardPage() {
		return this.autobootProjectPage;
	}
	
	@Override
	public boolean performFinish() {

		// firstly, two cfg files should copied to the new path,
		// then change the minigui cfg file's content,
		// the deploy.ini file should be saved at last
//		if(copyMiniguiCFG() && copyMgncsCFG() && modifyMiniguiCFG() && 
//				saveDeployInfo(exeProjectPage.getDeployLocation() + "/" + DEPLOY_INI_NAME)){
		if(saveDeployInfo(DEPLOY_INI_PATH + DEPLOY_INI_NAME)){
			MessageDialog.openWarning(getShell(), MStudioMessages.getString("MStudioDeployProject.error.title"),
					MStudioMessages.getString("MStudioDeployProject.save.successContent"));
			return true;
		}
		else{
			MessageDialog.openError(getShell(), MStudioMessages.getString("MStudioDeployProject.error.title"),
					MStudioMessages.getString("MStudioDeployProject.save.errorContent"));
			return false;
		}
	}

	public boolean performCancel() {
		this.dispose();
		return true;
	}
	
	private boolean saveDeployInfo(String filename) {
		
		// create a new file
		try {
			File iniCfg = new File(filename);
			if(iniCfg.exists()){
				iniCfg.delete();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		iniFile = new MStudioParserIniFile(filename);
		if (null == iniFile)
			return false;
		
		// create the sections and set their contents
		if(copyMiniguiCFG() && copyMgncsCFG() && modifyMiniguiCFG() && 
				setCfgsSection() && setServicesSection() &&
				setAutobootSection() && setAppsSection()){

			setDlcustomSection();
			setModulesSection();
			if (iniFile.save()) {
				return true;
			}
			else
				return false;
		}
		return false;
	}
	
	private boolean copyMiniguiCFG() {
		String cfgOldName = MStudioPlugin.getDefault().getMStudioEnvInfo().getCrossMgCfgFileName();
		miniguiCFGNewPath = Platform.getInstanceLocation().getURL().getPath() + MINIGUI_CFG_FILE_NAME;
		
		return copyFile(cfgOldName, miniguiCFGNewPath);
	}
	
	private boolean copyMgncsCFG() {
		String cfgOldName = MStudioPlugin.getDefault().getMStudioEnvInfo().getCrossMgNcsCfgFileName();
		mgncsCFGNewPath = Platform.getInstanceLocation().getURL().getPath() + MGNCS_CFG_FILE_NAME;
		return copyFile(cfgOldName, mgncsCFGNewPath);

//		mgncsCfgNewPath = Platform.getInstanceLocation().getURL().getPath() + MGNCS_CFG_FILE_NAME;
//		return copyFile(cfgOldName, mgncsCfgNewPath);		
	}
	
	private boolean modifyMiniguiCFG() {
		
		MStudioParserIniFile cfgFile = new MStudioParserIniFile(miniguiCFGNewPath);
		if (null == cfgFile)
			return false;		

		if (!deployTypeIsHost) {
			cfgFile.setStringProperty(SYSTEM_SECTION, GAL_PROPERTY, 
					exeProjectPage.getGALEngine(), null);	
			cfgFile.setStringProperty(SYSTEM_SECTION, IAL_PROPERTY, 
					exeProjectPage.getIALEngine(), null);
		}
		cfgFile.setStringProperty(SYSTEM_SECTION, DEFAULT_MODE_PROPERTY, 
				exeProjectPage.getResolution() + "-" + exeProjectPage.getColorDepth() + "bpp", null);		
		cfgFile.setStringProperty(exeProjectPage.getGALEngine(), DEFAULT_MODE_PROPERTY, 
				exeProjectPage.getResolution() + "-" + exeProjectPage.getColorDepth() + "bpp", null);		
		return cfgFile.save();
	}

	public boolean isHost() {
		return deployTypeIsHost;
	}
	
	//get executable projects in workspace
	public static IProject[] getExeProjects() {
		return MStudioPlugin.getDefault().getMStudioEnvInfo().getExecutableProjects();
	}
	
	//get shared library projects in workspace, not include dlcustom ial project.
	public static IProject[] getModuleProjects() {
		return MStudioPlugin.getDefault().getMStudioEnvInfo().getSharedLibProjects();
	}
	
	//get dlcustom ial projects in workspace.
	public static IProject[] getIALProjects() {
		return MStudioPlugin.getDefault().getMStudioEnvInfo().getDlCustomProjects();
	}
	//get dlcustom project in wizard page user select
	public IProject getDeployDLCustom(){
		return sharedLibPage.getDeployIALProject();
	}
	//get shard library projects in wizard page user select
	public IProject[] getDeployModules(){
		return sharedLibPage.getDeploySharedLibProjects();
	}
	
	public IProject[] getDeployExeProjects() {
		return exeProjectPage.getDeployExeProjects();
	}
	
	public MStudioParserIniFile getDeployIniFile() {
		return iniFile;
	}
	
	public String[] getDeployService() {
		return deployServicesPage.getDeployServices();
	}
	
	public IProject[] getDeployAutobootProject() {
		return autobootProjectPage.getDeployAutobootProjects();
	}
	
	private boolean setCfgsSection() {

		iniFile.addSection(DEPLOY_CFG_SECTION, null);
		iniFile.setStringProperty(DEPLOY_CFG_SECTION, MINIGUI_CFG_PROPERTY, miniguiCFGNewPath, null);
		iniFile.setStringProperty(DEPLOY_CFG_SECTION, MGNCS_CFG_PROPERTY, mgncsCFGNewPath, null);
		String temp = MStudioPlugin.getDefault().getMStudioEnvInfo().getMgRunMode();
//=======
//		iniFile.addSection(DEPLOY_CFG_SECTION, null);		
//		iniFile.setStringProperty(DEPLOY_CFG_SECTION, MINIGUI_CFG_PROPERTY, 
//				miniguiCfgNewPath == null ? "" : miniguiCfgNewPath, null);
//		iniFile.setStringProperty(DEPLOY_CFG_SECTION, MGNCS_CFG_PROPERTY, 
//				mgncsCfgNewPath == null ? "" : mgncsCfgNewPath, null);
//		String temp=MStudioPlugin.getDefault().getMStudioEnvInfo().getMgRunMode();
//>>>>>>> .r2811
		iniFile.setStringProperty(DEPLOY_CFG_SECTION, MINIGUI_RUNMODE_PROPERTY, 
				temp == null ? "" : temp, null);	
		return true;
	}
	
	private boolean setServicesSection() {
		iniFile.addSection(DEPLOY_SERVICES_SECTION, null);
		String[] serv = getDeployService();
		if (null == serv) 
			return false;		
		iniFile.setIntegerProperty(DEPLOY_SERVICES_SECTION, SERVICES_NUMBER_PROPERTY, 
				serv.length, null);		
		for (int i=0; i<serv.length; i++) {
			iniFile.setStringProperty(DEPLOY_SERVICES_SECTION, (SERVICE_NAME_PROPERTY + i), 
				serv[i], null);
		}			
		return true;
	}
	
	private boolean setDlcustomSection() {
		iniFile.addSection(DEPLOY_DLCUSTOM_SECTION, null);
		IProject project = getDeployDLCustom();		
		iniFile.setStringProperty(DEPLOY_DLCUSTOM_SECTION, DLCUSTOM_PROGRAM_PROPERTY, 
				project == null ? "" : project.getLocation().toOSString(), null);
		return true;
	}
	
	private boolean setModulesSection() {
		iniFile.addSection(DEPLOY_MODULES_SECTION, null);
		IProject[] projects = getDeployModules();
		if (null == projects)
			return false;		
		iniFile.setIntegerProperty(DEPLOY_MODULES_SECTION, MODULES_NUMBERS_PROPERTY,
				projects.length, null);		
		for (int i=0; i<projects.length; i++) {
			iniFile.setStringProperty(DEPLOY_MODULES_SECTION, (MODULES_NAME_PROPERTY + i),
					projects[i].getName(), null);			
			iniFile.addSection(projects[i].getName(), null);			
			iniFile.setStringProperty(projects[i].getName(), PROGRAM_PROPERTY, 
					projects[i].getLocation().toOSString(), null);
			iniFile.setStringProperty(projects[i].getName(), PROGRAM_DEPLOY_PROPERTY,
					MODULES_DEPLOY_PATH, null);
			iniFile.setStringProperty(projects[i].getName(), PROGRAM_CFG_PROPERTY, "", null);
		}
		return true;
	}
	
	private boolean setAutobootSection() {
		iniFile.addSection(DEPLOY_AUTOBOOT_SECTION, null);
		IProject[] projects = getDeployAutobootProject();
		if (null == projects)
			return false;		
		iniFile.setIntegerProperty(DEPLOY_AUTOBOOT_SECTION, AUTOBOOT_NUMBERS_PROPERTY,
				projects.length, null);		
		for (int i=0; i<projects.length; i++) {
			iniFile.setStringProperty(DEPLOY_AUTOBOOT_SECTION, (AUTOBOOT_NAME_PROPERTY + i),
					projects[i].getName(), null);
		}	
		return true;
	}
	
	private boolean setAppsSection() {
		iniFile.addSection(DEPLOY_APPS_SECTION, null);
		IProject[] projects = getDeployExeProjects();
		if (null == projects)
			return false;		
		iniFile.setIntegerProperty(DEPLOY_APPS_SECTION, APPS_NUMBER_PROPERTY, 
				projects.length, null);				
		for (int i=0; i<projects.length; i++) {
			iniFile.setStringProperty(DEPLOY_APPS_SECTION, (APPS_NAME_PROPERTY + i),
					projects[i].getName(), null);
			
			iniFile.addSection(projects[i].getName(), null);			
			iniFile.setStringProperty(projects[i].getName(), PROGRAM_PROPERTY,
					projects[i].getLocation().toOSString(), null);
			iniFile.setStringProperty(projects[i].getName(), PROGRAM_CFG_PROPERTY,
					projects[i].getLocationURI() + APP_CFG_PATH, null);
			iniFile.setStringProperty(projects[i].getName(), PROGRAM_DEPLOY_PROPERTY, 
					APP_DEPLOY_PATH, null);
			iniFile.setStringProperty(projects[i].getName(), RESPACK_PROPERTY, 
					projects[i].getLocationURI() + ".res", null);
			iniFile.setStringProperty(projects[i].getName(), RESPACK_DEPLOY_PROPERTY, 
					"/usr/share/" + projects[i].getName() + "/res", null);
			String temp = getDepLibs(projects[i]);
			iniFile.setStringProperty(projects[i].getName(), DEPLIBS_PROPERTY, 
					temp == null? "" : temp, null);
			iniFile.setStringProperty(projects[i].getName(), DEPLIBS_DEPLOY_PROPERTY,
					DEPLIBS_PROGRAM_DEPLOY, null);
		}
		return true;
	}

	private String getDepLibs(IProject project){
		if(project == null)
			return null;
		MStudioProject mprj = new MStudioProject(project);
		//List<String> depLibList = new ArrayList<String> ();
		String[] pkgs = mprj.getDepPkgs();
		String depLibStr="";
		MStudioEnvInfo einfo = MStudioPlugin.getDefault().getMStudioEnvInfo();
		for (int idx = 0; idx < pkgs.length; idx++) {
			String[] libs = einfo.getPackageLibs(pkgs[idx]);
			for (int c = 0; c < libs.length; c++){
				//depLibList.add(libs[c]);
				depLibStr += depLibStr+" ";
			}
		}
		return depLibStr;
	}
	
    private boolean copyFile(String oldPath, String newPath) {
    	try {
    		int bytesum = 0;
    		int byteread = 0;
    		File oldFile = new File(oldPath); 
    		if (oldFile.exists()) {
    			File newFile=new File(newPath);
    			if(newFile.exists())
    				newFile.delete();
    			FileInputStream inStream = new FileInputStream(oldPath);
    			FileOutputStream fs = new FileOutputStream(newPath);
    			byte[] buffer = new byte[1024];
    			while ((byteread = inStream.read(buffer)) != -1) {
    				bytesum += byteread; 
    				fs.write(buffer, 0, byteread); 
    			}
    			inStream.close();
    			return true;
    		}
    		else {
	    		return false;
    		}
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		return false;
    	} 
    }     	
}
