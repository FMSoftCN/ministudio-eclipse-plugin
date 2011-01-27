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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProject;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProjectNature;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.utils.spawner.EnvironmentReader;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;

import org.eclipse.swt.widgets.Display;
public class MStudioDeployWizard extends Wizard{

	private MStudioDeployTypeWizardPage deployTypePage;
	private MStudioDeployExecutableProjectsWizardPage exeProjectPage;
	private MStudioDeploySharedLibProjectsWizardPage sharedLibPage;
	private MStudioDeployServicesWizardPage deployServicesPage;
	private MStudioDeployAutobootProjectsWizardPage autobootProjectPage;
	
	public static boolean deployTypeIsHost = false;
	private MStudioParserIniFile iniFile = null;
	private MStudioDeployDialog dialog;
	private static MStudioEnvInfo einfo = MStudioPlugin.getDefault().getMStudioEnvInfo();
	private final String DEPLOY_INI_PATH = einfo.getWorkSpaceMetadataPath();
	private String miniguiCfgNewPath = DEPLOY_INI_PATH	+ MINIGUI_CFG_FILE_NAME;
	private String mgncsCfgNewPath = DEPLOY_INI_PATH + MGNCS_CFG_FILE_NAME;
	private String miniguiTargetCfgNewPath = DEPLOY_INI_PATH + MINIGUI_TARGET_CFG_FILE_NAME;
	private String mgncsTargetCfgNewPath = DEPLOY_INI_PATH + MGNCS_TARGET_CFG_FILE_NAME;
	private final static String DEPLOY_INI_NAME = "deploy.ini";
	private final static String ROOTFS_NAME = "make_rootfs";
	
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
	private final static String MINIGUI_TARGET_CFG_FILE_NAME = "MiniGUI.cfg.target";
	private final static String MGNCS_CFG_FILE_NAME = "mgncs.cfg";
	private final static String MGNCS_TARGET_CFG_FILE_NAME = "mgncs.cfg.target";
	private final static String SYSTEM_SECTION = "system";
	private final static String GAL_PROPERTY = "gal_engine";
	private final static String IAL_PROPERTY = "ial_engine";
	private final static String DEFAULT_MODE_PROPERTY = "defaultmode";
	private final static String MODULES_DEPLOY_PATH = "/usr/local/lib";
	private final static String APP_DEPLOY_PATH = "/usr/bin";

	private final static String DEPLIBS_PROGRAM_DEPLOY = "/usr/local/lib";
	
	private final String SECTION_PATH_INFO="path_info";
	private final String KEY_RESPKG_PATH="respkg_path";
	private final String KEY_USR_PATH="usr_path";
	private final String DEF_RES_LOCATION = "/usr/local/share/";
	private final String DEF_CUSTOM_FILE_LOCATION = "/usr/local/share";
	
	private final String LIB_SUFFIX_NAME = ".so";
	private final String LIB_PREFIX_NAME = "lib";
	
	private static final String MSMS_EMPTY_STR = "";

	public MStudioDeployWizard() {
		setWindowTitle(MStudioMessages.getString("MStudioDeployWizard.title"));
	}

	public void addPages() {
		deployTypePage = new MStudioDeployTypeWizardPage(
				MStudioMessages.getString("MStudioDeployWizardPage.selectType.pageName"));
		addPage(deployTypePage);

		exeProjectPage = new MStudioDeployExecutableProjectsWizardPage(
				MStudioMessages.getString("MStudioDeployWizardPage.selectExeProjects.pageName"));
		addPage(exeProjectPage);

		sharedLibPage = new MStudioDeploySharedLibProjectsWizardPage(
				MStudioMessages.getString("MStudioDeployWizardPage.selectLibProjects.pageName"));
		addPage(sharedLibPage);

		deployServicesPage = new MStudioDeployServicesWizardPage(
				MStudioMessages.getString("MStudioDeployWizardPage.selectServices.pageName"));
		addPage(deployServicesPage);

		autobootProjectPage = new MStudioDeployAutobootProjectsWizardPage(
				MStudioMessages.getString("MStudioDeployWizardPage.selectAutobootProjects.pageName"));
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
	
	
	public void distory(){
		this.getShell().close();
	}
	@Override
	public boolean performFinish() {	
		dialog=new MStudioDeployDialog(this.getShell());		
		this.getShell().getDisplay().asyncExec(new Runnable(){
			public void run(){				
				dialog.getShell().setText("deploy status");
				dialog.open();	
				boolean error = false;
				try{		
					do{						
						//the string would be write into the property file ,there is a way to write here for testing code  
						dialog.getDescription().setText("check the file deploy.ini exist");
						if(saveDeployInfo(DEPLOY_INI_PATH + DEPLOY_INI_NAME)){
							dialog.getProgressBar().setSelection(50);							
						}
						else{
							dialog.getDescription().setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
							dialog.getDescription().setText("save file error");
							error = true;
							break;
						}
						dialog.getDescription().setText("save ok!");
						dialog.getDescription().setText("now create a process to run the script  to rootfs");
						if(runScript()){
							dialog.getProgressBar().setSelection(100);
							dialog.getDescription().setText("script ok");
							//show the script process return string
							//dialog.getDescription().setText(returnString);
						}
						else{
							dialog.getDescription().setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
							dialog.getDescription().setText("run script error");
							error=true;
							break;
						}
					} while(false);
					
					if(!error){
						dialog.close();
						distory();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				finally{
					
				}
			}
		});		
		return false;
	}
	
	public boolean runScript(){
		CommandLauncher launcher = new CommandLauncher();
		launcher.showCommand(true);
		StringBuffer cmd = new StringBuffer(ROOTFS_NAME);

		String binPath = einfo.getSOCBinPath();
		String targetType = deployTypePage.getTargetType();
		if (targetType.equals("Host")) {
			binPath = einfo.getPCBinPath();
		}

		if (binPath == null)
			return false;
		Path editCommand = null;

		if (binPath == null || binPath.equals(MSMS_EMPTY_STR)) {
			editCommand = new Path (cmd.toString());
		} else {
        	editCommand = new Path (binPath + File.separatorChar +cmd.toString());
        }
        
		List<String> args = new ArrayList<String>();
		args.add("-f");
		args.add(DEPLOY_INI_PATH + DEPLOY_INI_NAME);
		args.add("-p");
		args.add(this.getDeployExecuteableWizardPage().getDeployLocation());

		IPath workingDir = new Path(binPath);
		Properties envProps = EnvironmentReader.getEnvVars();

		envProps.setProperty("CWD", workingDir.toOSString());
		envProps.setProperty("PWD", workingDir.toOSString());	        

		/*Process root = */launcher.execute(editCommand, 
				(String[])args.toArray(new String[args.size()]), 
				createEnvStringList(envProps), workingDir);
		//get the process output string,this would be catch the information
		/*
		InputStream is = root.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String s;
		try {
			while((s = br.readLine())!=null){
				returnString+=s;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		return true;
	}
	
	private static String[] createEnvStringList(Properties envProps) {

        String[] env = null;
        List<String> envList = new ArrayList<String>();
        Enumeration<?> names = envProps.propertyNames();

        if (names != null) {
            while (names.hasMoreElements()) {
                String key = (String) names.nextElement();
                envList.add(key + "=" + envProps.getProperty(key));
            }
            env = (String[]) envList.toArray(new String[envList.size()]);
        }

        return env;
    }
	public boolean performCancel() {
		this.dispose();
		return true;
	}

	
	private boolean saveDeployInfo(String filename) {		
		// create a new file
		try {
			File iniCfg = new File(filename);
			if (iniCfg.exists()) {
				iniCfg.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		iniFile = new MStudioParserIniFile(filename);
		if (null == iniFile)
			return false;	
		//update the host and target config files ,
		//some section would be changed when slect the deploy target
		if(!updateCfgFiles())
			return false;
		
		setCfgsSection();	
		setServicesSection();
		setDlcustomSection();
		setModulesSection();		
		setAutobootSection();
		setAppsSection();
		
		if (!iniFile.save()) 
			return false;
			
		return true;
	}

	private boolean updateCfgFiles() {		
		// select target
		if (!deployTypeIsHost) {
			MStudioParserIniFile targetCfgFile = new MStudioParserIniFile(miniguiTargetCfgNewPath);
			if (null == targetCfgFile)
				return false;
			targetCfgFile.setStringProperty(SYSTEM_SECTION, GAL_PROPERTY,
					exeProjectPage.getGALEngine(), null);
			targetCfgFile.setStringProperty(SYSTEM_SECTION, IAL_PROPERTY,
					exeProjectPage.getIALEngine(), null);
			targetCfgFile.setStringProperty(exeProjectPage.getGALEngine(),
					DEFAULT_MODE_PROPERTY, exeProjectPage.getResolution() + "-"
					+ exeProjectPage.getColorDepth() + "bpp", null);
			return targetCfgFile.save();
		} else {
			MStudioParserIniFile hostCfgFile = new MStudioParserIniFile(miniguiCfgNewPath);
			if (null == hostCfgFile)
				return false;
			String value = hostCfgFile.getStringProperty(SYSTEM_SECTION, GAL_PROPERTY);
			if (value != null) {
				hostCfgFile.setStringProperty(value, DEFAULT_MODE_PROPERTY,
						exeProjectPage.getResolution() + "-" + exeProjectPage.getColorDepth() + "bpp", null);
			}
			hostCfgFile.setStringProperty(SYSTEM_SECTION, DEFAULT_MODE_PROPERTY, 
					exeProjectPage.getResolution() + "-" + exeProjectPage.getColorDepth() + "bpp", null);
			return hostCfgFile.save();
		}		
	}

	public boolean isHost() {
		return deployTypeIsHost;
	}

	// get executable projects in workspace
	public static IProject[] getExeProjects() {
		return einfo.getExecutableProjects();
	}

	// get shared library projects in workspace, not include dlcustom ial
	// project.
	public static IProject[] getModuleProjects() {
		return einfo.getSharedLibProjects();
	}

	// get dlcustom ial projects in workspace.
	public static IProject[] getIALProjects() {
		return einfo.getDlCustomProjects();
	}

	// get dlcustom project in wizard page user select
	public IProject getDeployDLCustom() {
		return sharedLibPage.getDeployIALProject();
	}

	// get shard library projects in wizard page user select
	public IProject[] getDeployModules() {
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

	private void setCfgsSection() {

		iniFile.addSection(DEPLOY_CFG_SECTION, null);
		if(isHost()){
			iniFile.setStringProperty(DEPLOY_CFG_SECTION, MINIGUI_CFG_PROPERTY,
					miniguiCfgNewPath, null);
			iniFile.setStringProperty(DEPLOY_CFG_SECTION, MGNCS_CFG_PROPERTY,
					mgncsCfgNewPath, null);
		}
		else{
			iniFile.setStringProperty(DEPLOY_CFG_SECTION, MINIGUI_CFG_PROPERTY,
					miniguiTargetCfgNewPath, null);
			iniFile.setStringProperty(DEPLOY_CFG_SECTION, MGNCS_CFG_PROPERTY,
					mgncsTargetCfgNewPath, null);
		}
		String temp = einfo.getMgRunMode();
		iniFile.setStringProperty(DEPLOY_CFG_SECTION, MINIGUI_RUNMODE_PROPERTY, 
				temp == null ? "" : temp, null);
	}

	private void setServicesSection() {
		iniFile.addSection(DEPLOY_SERVICES_SECTION, null);
		String[] serv = getDeployService();
		if (null == serv){ 
			iniFile.setIntegerProperty(DEPLOY_SERVICES_SECTION, SERVICES_NUMBER_PROPERTY, 
					0, null);
			return;		
		}
		iniFile.setIntegerProperty(DEPLOY_SERVICES_SECTION, SERVICES_NUMBER_PROPERTY, 
				serv.length, null);		
		for (int i=0; i<serv.length; i++) {
			iniFile.setStringProperty(DEPLOY_SERVICES_SECTION, (SERVICE_NAME_PROPERTY + i), 
				serv[i], null);
		}
	}
	
	private String getConfigureName(IProject project) {
		
		if (null == project) {
			return null;
		}
		
		String type = null;
		String targetType = deployTypePage.getTargetType();
		String buildType = deployTypePage.getBuildType();
		if (targetType.equals("Host") && buildType.equals("Debug")) {
			type = "Debug4Host";
			return type;
		}
		else if (targetType.equals("Host") && buildType.equals("Release")) {
			type = "Release4Host";
			return type;
		}
		else if (targetType.equals("Target") && buildType.equals("Debug")) {
			type = "Debug4";
			IManagedProject managedProj = ManagedBuildManager.getBuildInfo(project).getManagedProject();
			IConfiguration[] cur_cfgs = managedProj.getConfigurations();
			String tmp;
			for (int i = 0; i < cur_cfgs.length; i++) {
				tmp = cur_cfgs[i].getName();
				if (null != tmp && type.equals(tmp.substring(0, type.length())) 
						&& !tmp.equals("Debug4Host")) {
					type = tmp;
					return type;
				}
			}
		}
		else if (targetType.equals("Target") && buildType.equals("Release")) {
			type = "Release4";
			IManagedProject managedProj = ManagedBuildManager.getBuildInfo(project).getManagedProject();
			IConfiguration[] cur_cfgs = managedProj.getConfigurations();
			String tmp;
			for (int i = 0; i < cur_cfgs.length; i++) {
				tmp = cur_cfgs[i].getName();
				if (null != tmp && type.equals(tmp.substring(0, type.length()))
						&& !tmp.equals("Release4Host")) {
					type = tmp;
					return type;
				}
			}
		}

		return null;
	}

	private void setDlcustomSection() {
		iniFile.addSection(DEPLOY_DLCUSTOM_SECTION, null);
		IProject project = getDeployDLCustom();
		String dlcustom = null;
		if (null != project) {
			dlcustom = project.getLocation().toOSString() + File.separatorChar
				+ getConfigureName(project) + File.separatorChar + LIB_PREFIX_NAME 
				+ project.getName() + LIB_SUFFIX_NAME;
		}
		iniFile.setStringProperty(DEPLOY_DLCUSTOM_SECTION, DLCUSTOM_PROGRAM_PROPERTY,
				dlcustom, null);
	}

	private void setModulesSection() {
		
		iniFile.addSection(DEPLOY_MODULES_SECTION, null);
		IProject[] projects = getDeployModules();
		if (null == projects) {
			iniFile.setIntegerProperty(DEPLOY_MODULES_SECTION,
					MODULES_NUMBERS_PROPERTY, 0, null);
			return;
		}
		
		iniFile.setIntegerProperty(DEPLOY_MODULES_SECTION,
				MODULES_NUMBERS_PROPERTY, projects.length, null);
		
		String program = null;
		for (int i = 0; i < projects.length; i++) {
			iniFile.setStringProperty(DEPLOY_MODULES_SECTION,
					(MODULES_NAME_PROPERTY + i), projects[i].getName(), null);
			
			iniFile.addSection(projects[i].getName(), null);
			program = projects[i].getLocation().toOSString() + File.separatorChar
					+ getConfigureName(projects[i]) + File.separatorChar + LIB_PREFIX_NAME 
					+ projects[i].getName() + LIB_SUFFIX_NAME;
			iniFile.setStringProperty(projects[i].getName(), PROGRAM_PROPERTY,
					program, null);
			String temp = getModuleDeploy(projects[i]);
			iniFile.setStringProperty(projects[i].getName(), PROGRAM_DEPLOY_PROPERTY,
					temp == null ? "" : temp, null);
			
			temp = saveProjectResCfgs(projects[i]);
			iniFile.setStringProperty(projects[i].getName(), PROGRAM_CFG_PROPERTY,
					temp == null ? "" : temp, null);
			
			temp = getResPack(projects[i]);
			iniFile.setStringProperty(projects[i].getName(), RESPACK_PROPERTY,
					temp == null ? "" : temp, null);
			temp = getResPackDepoloy(projects[i]);
			iniFile.setStringProperty(projects[i].getName(), RESPACK_DEPLOY_PROPERTY,
					temp == null ? "" : temp, null);
			temp = getDepLibs(projects[i]);
			iniFile.setStringProperty(projects[i].getName(), DEPLIBS_PROPERTY,
					temp == null ? "" : temp, null);
			temp = getDeplibsDeploy(projects[i]);
			iniFile.setStringProperty(projects[i].getName(), DEPLIBS_DEPLOY_PROPERTY,
					temp == null ? "" : temp, null);
		}
	}

	private void setAutobootSection() {
		iniFile.addSection(DEPLOY_AUTOBOOT_SECTION, null);
		IProject[] projects = getDeployAutobootProject();
		if (null == projects){
			iniFile.setIntegerProperty(DEPLOY_AUTOBOOT_SECTION, AUTOBOOT_NUMBERS_PROPERTY,
					0, null);	
			return;		
		}
		iniFile.setIntegerProperty(DEPLOY_AUTOBOOT_SECTION, AUTOBOOT_NUMBERS_PROPERTY,
				projects.length, null);		
		for (int i=0; i<projects.length; i++) {
			iniFile.setStringProperty(DEPLOY_AUTOBOOT_SECTION, (AUTOBOOT_NAME_PROPERTY + i),
					projects[i].getName(), null);
		}	
	}

	private void setAppsSection() {
		
		iniFile.addSection(DEPLOY_APPS_SECTION, null);
		IProject[] projects = getDeployExeProjects();
		if (null == projects) {
			iniFile.setIntegerProperty(DEPLOY_APPS_SECTION,
					APPS_NUMBER_PROPERTY, 0, null);
			return;
		}
		
		iniFile.setIntegerProperty(DEPLOY_APPS_SECTION, APPS_NUMBER_PROPERTY, 
				projects.length, null);	
		
		String program = null;
		for (int i = 0; i < projects.length; i++) {
			iniFile.setStringProperty(DEPLOY_APPS_SECTION, (APPS_NAME_PROPERTY + i),
					projects[i].getName(), null);
			
			iniFile.addSection(projects[i].getName(), null);			
			program = projects[i].getLocation().toOSString() + File.separatorChar
					+ getConfigureName(projects[i]) + File.separatorChar + projects[i].getName();
			iniFile.setStringProperty(projects[i].getName(), PROGRAM_PROPERTY,
					program, null);
			
			String temp =  saveProjectResCfgs(projects[i]);
			iniFile.setStringProperty(projects[i].getName(), PROGRAM_CFG_PROPERTY,
					temp == null ? "" : temp, null);
			
			temp = getAppDeploy(projects[i]);
			iniFile.setStringProperty(projects[i].getName().trim(), PROGRAM_DEPLOY_PROPERTY, 
					temp == null ? "" : temp, null);
			temp = getResPack(projects[i]);
			iniFile.setStringProperty(projects[i].getName(), RESPACK_PROPERTY, 
					temp == null ? "" : temp, null);
			temp = getResPackDepoloy(projects[i]);
			iniFile.setStringProperty(projects[i].getName(), RESPACK_DEPLOY_PROPERTY,
					temp == null ? "" : temp, null);
		    temp = getDepLibs(projects[i]);
			iniFile.setStringProperty(projects[i].getName(), DEPLIBS_PROPERTY,
					temp == null ? "" : temp, null);
			temp = getDeplibsDeploy(projects[i]);
			iniFile.setStringProperty(projects[i].getName(), DEPLIBS_DEPLOY_PROPERTY,
					temp == null ? "" : temp, null);
		}
	}
	
	private boolean copyFile(String oldPath, String newPath) {

		try {
			int bytesum = 0;
			int byteread = 0;
			File oldFile = new File(oldPath);

			if (oldFile.exists()) {
				File newFile = new File(newPath);
				if (newFile.exists())
					newFile.delete();

				FileInputStream inStream = new FileInputStream(oldPath);
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[4096];

				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread;
					fs.write(buffer, 0, byteread);
				}
				inStream.close();

				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private String saveProjectResCfgs (IProject prj){
		
		try {
			if (null == prj || !prj.hasNature(MStudioProjectNature.MSTUDIO_NATURE_ID))
				return null;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		String prj_res_cfg = null;
		MStudioProject msp = new MStudioProject(prj);
		prj_res_cfg = msp.getProgramCfgFile();
		if (null == prj_res_cfg && !msp.isProgramCfgFileExist()){
			return null;
		}
			
		String newCfgFile = prj_res_cfg + ".target";
		if (!copyFile (prj_res_cfg, newCfgFile)){
			return null;
		}

		MStudioParserIniFile prjResFile = new MStudioParserIniFile(newCfgFile);
		if (null ==prjResFile) {
			return null;
		}
			
		String[] paths = msp.getDeployPathInfo();
		if (paths.length == 4){
			prjResFile.setStringProperty(SECTION_PATH_INFO, 
					KEY_RESPKG_PATH, paths[0] + "/" + prj.getName(), null);
			prjResFile.setStringProperty(SECTION_PATH_INFO, 
					KEY_USR_PATH, paths[3] + "/" + prj.getName(), null);
		} else {
			prjResFile.setStringProperty(SECTION_PATH_INFO, 
					KEY_RESPKG_PATH, DEF_RES_LOCATION + "/" + prj.getName(), null);
			prjResFile.setStringProperty(SECTION_PATH_INFO, 
					KEY_USR_PATH, DEF_CUSTOM_FILE_LOCATION + "/" + prj.getName(), null);
		}
		if (!prjResFile.save()){
			return null;
		}

		return newCfgFile;
	}
	
	private String getAppDeploy(IProject project) {
		return APP_DEPLOY_PATH;
	}
	
	private String getModuleDeploy(IProject project) {
		return MODULES_DEPLOY_PATH;
	}
	
	private String getResPack(IProject project) {
		return project.getLocation().toOSString().trim() + "/res/" + project.getName().trim() + ".res";
	}

	private String getResPackDepoloy(IProject project) {
		MStudioProject mStudioProject = new MStudioProject(project);
		String[] paths = mStudioProject.getDeployPathInfo();
		if (paths.length == 4) {
			return paths[0];
		}
		else
			return DEF_RES_LOCATION + project.getName().trim() + "/res";
	}

	private String getDepLibs(IProject project) {
		if (project == null)
			return null;
		MStudioProject mprj = new MStudioProject(project);
		// List<String> depLibList = new ArrayList<String> ();
		String[] pkgs = mprj.getDepPkgs();
		String depLibStr = "";
		for (int idx = 0; idx < pkgs.length; idx++) {
			String[] libs = einfo.getPackageLibs(pkgs[idx]);
			for (int c = 0; c < libs.length; c++) {
				// depLibList.add(libs[c]);
				depLibStr += libs[c] + " ";
			}
		}
		return depLibStr;
	}
	
	private String getDeplibsDeploy(IProject project) {
		return DEPLIBS_PROGRAM_DEPLOY;
	}

}

