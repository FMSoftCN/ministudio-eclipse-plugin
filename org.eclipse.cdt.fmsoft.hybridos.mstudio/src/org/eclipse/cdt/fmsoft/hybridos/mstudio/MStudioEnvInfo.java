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

package org.eclipse.cdt.fmsoft.hybridos.mstudio;

import java.io.File;
import java.io.FilenameFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards.MStudioParserIniFile;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.preferences.MStudioPreferenceConstants;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.preferences.MStudioSoCPreferencePage;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProject;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProjectNature;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.internal.provisional.action.ToolBarContributionItem2;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.menus.CommandContributionItem;


@SuppressWarnings("restriction")
class DirFilter implements FilenameFilter {

	public boolean accept(File file, String fname) {
		File dirFile = new File(file.getPath() + File.separator + fname);

		if (dirFile.isDirectory()) {
			// to search if the .hybirdos.cfg file is exist or not
			File cfgfile = new File(MStudioEnvInfo.SOC_PATH_PREFIX + fname
					+ File.separator + MStudioEnvInfo.SOC_CONFIG_FILE);

			return cfgfile.isFile();
		}
		return false;
	}
}

class hpkgFilter implements FilenameFilter {

	public boolean accept(File file, String fname) {
		return fname.toLowerCase().endsWith(".hpkg");
	}
}

public class MStudioEnvInfo {

	protected final static String SOC_PATH_PREFIX = "/opt/hybridos/";
	protected final static String SOC_CONFIG_FILE = ".hybridos.cfg";

	private final static String SOC_CFG_SECTION_MINIGUI = "minigui";
	private final static String SOC_CFG_SECTION_RUNMODE = "runmode";
	private final static String SOC_CFG_SECTION_SERVICES = "services";
	private final static String SOC_CFG_SECTION_MGINIT = "mginit";
	private final static String SOC_CFG_SECTION_TOOLCHAIN = "toolchain";
	private final static String SOC_CFG_SECTION_GAL = "gal";
	private final static String SOC_CFG_SECTION_IAL = "ial";

	// private final String MSEI_EMPTY_PROJECT               = "Empty Project";               // Key depend lib: gio
	private final String MSEI_MINIGUI_PROJECT             = "MiniGUI Project";             // Key depend lib: minigui
	// private final String MSEI_MGINIT_MODULE_PROJECT       = "mginit Module Project";       // Key depend lib: minigui
	// private final String MSEI_DICUSTOM_IAL_ENGINE_PROJECT = "DICustom IAL Engine Project"; // Key depend lib: 
	// private final String MSEI_EMPTY_CPLUSPLUS_PROJECT     = "Empty C++ Project";           // Key depend lib: gio

	private final static String EMPTY_STR = "";
	private final static String MSE_SPACE = " ";
	private final static String RESOLUTION_REGEX_STRING = "[1-9]+[0-9]*\\s*[*x×]\\s*[0-9]+[1-9]*\\s*-\\s*[1-9]+bpp";

	//the ini file object which pointer to SoC used by current workspace
	public enum MiniGUIRunMode {
	    thread,
	    process,
	    standalone
	};

	//key: the specified soft package; value: some packages affected by the specified package
	private Map<String, List<String>> affectedPkgs = null;
	private Map<String, List<String>> depPkgs = null;
	private Map<String, MStudioParserIniFile> allSoftPkgs = null;

	//lib: 
	private String projectTypeName = null;
	private Map<String, List<String>> defaultLibs = null;
	private List<String> defaultKeyLib = new ArrayList<String>();
	private List<String> defaultDepPkgs = new ArrayList<String>();

	private MStudioParserIniFile iniFile = null;
	private MiniGUIRunMode mgRunMode = MiniGUIRunMode.thread;

	private static String SoCName = null;
	private static MStudioEnvInfo instance = new MStudioEnvInfo();

	public static final class PackageItem {
		String name = null;
		String description = null;

		public PackageItem(String name, String desc) {
			this.name = name;
			this.description = desc;
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}
	}

	private MStudioEnvInfo() {
		updateSoCName();
	}

	public static MStudioEnvInfo getInstance() {
		return instance;
	}

	//-----private static methods-----
	private static String getCurSoCConfFileName() {
		if (SoCName == null)
			return null;

		return SOC_PATH_PREFIX + SoCName + File.separator + SOC_CONFIG_FILE;
	}

	//get current SoC
	public MStudioParserIniFile getSoCIniFile() {
		return iniFile;
	}

	public String getMginitBinPath() {
		if (iniFile == null || SoCName == null || !mgRunMode.equals(MiniGUIRunMode.process))
			return null;

		return SOC_PATH_PREFIX + SoCName + File.separator + iniFile.getStringProperty(SOC_CFG_SECTION_MGINIT, "bin");
	}

	public String getMginitCfgFile() {
		if (iniFile == null || SoCName == null || !mgRunMode.equals(MiniGUIRunMode.process))
			return null;

		String path = SOC_PATH_PREFIX + SoCName + File.separator
							+ iniFile.getStringProperty(SOC_CFG_SECTION_MGINIT, "cfg");

		File file = new File (path);
		if (file.isDirectory() && !path.endsWith("mginit.cfg"))
			path += "/mginit.cfg";

		return (new File(path).exists()) ? path : null;
	}


	public List<String> getServices() {
		if (iniFile == null)
			return null;

		Integer numSer = iniFile.getIntegerProperty(SOC_CFG_SECTION_SERVICES, "num");
		int srvCount = (numSer == null ? 0 : numSer);
		if (srvCount <= 0)
			return null;

		List<String> srvList = new ArrayList<String>(srvCount);

		for (int i = 0; i < srvCount; i++) {
			srvList.add(iniFile.getStringProperty(SOC_CFG_SECTION_SERVICES, "service" + i));
		}
		return srvList;
	}

	public String getToolChainPrefix() {
		if (iniFile == null)
			return null;

		return iniFile.getStringProperty(SOC_CFG_SECTION_TOOLCHAIN, "prefix");
	}

	public String[] getGalOptions() {
		if (iniFile == null)
			return new String[0];

		Integer numGal = iniFile.getIntegerProperty(SOC_CFG_SECTION_GAL, "num");
		int num = (numGal == null ? 0 : numGal);
		String[] ret = new String[num];

		for (int i = 0; i < num; i++) {
			String key = "gal" + i;
			ret[i] = iniFile.getStringProperty(SOC_CFG_SECTION_GAL, key);
		}

		return ret;
	}

	public String[] getIalOptions() {
		if (iniFile == null)
			return new String[0];

		Integer numIal = iniFile.getIntegerProperty(SOC_CFG_SECTION_IAL, "num");
		int num = (numIal == null ? 0 : numIal);
		String[] ret = new String[num];

		for (int i = 0; i < num; i++) {
			String key = "ial" + i;
			ret[i] = iniFile.getStringProperty(SOC_CFG_SECTION_IAL, key);
		}

		return ret;
	}

	public void clearAllSoftPkgs() {
		if (null != allSoftPkgs)
			allSoftPkgs.clear();
	}

	//get all soft packages name and description.
	public Map<String, String> getAllSoftPkgs() {

		Map<String, String> mapRet = new HashMap<String, String>();

		if (null != allSoftPkgs) {
			for (Map.Entry<String, MStudioParserIniFile> entry : allSoftPkgs.entrySet()) {
				String name = entry.getKey().toString();
				MStudioParserIniFile ini = entry.getValue();
				String description = ini.getStringProperty("package", "description");
				mapRet.put(name, description);
			}
		}

		return mapRet;
	}

	public Map<String, List<String>> getDepPkgs() {
		return depPkgs;
	}

	public Map<String, List<String>> getDefaultLibs() {
		return defaultLibs;
	}

	public Map<String, List<String>> getAffectedPkgs() {
		return affectedPkgs;
	}

	public String[] getPackageLibs(String pkg) {
		if (null == allSoftPkgs)
			return new String[0];

		MStudioParserIniFile pckCfgFile = allSoftPkgs.get(pkg);
		if (null == pckCfgFile)
			return new String[0];

		String pName = pckCfgFile.getStringProperty("package", "name");
		if (null == pName)
			return new String[0];

		String libs = pckCfgFile.getStringProperty(pName, "lib");
		if (null == libs)
			return new String[0];

		return libs.split(MSE_SPACE);
	}

	public String getPCIncludePath() {
		if (null == SoCName)
			return EMPTY_STR;
		return SOC_PATH_PREFIX + SoCName + "/pc_symmetry/include/";
	}

	public String getPCLibraryPath() {
		if (null == SoCName)
			return EMPTY_STR;
		return SOC_PATH_PREFIX + SoCName + "/pc_symmetry/lib/";
	}

	public String getPCBinPath() {
		if (null == SoCName)
			return EMPTY_STR;
		return SOC_PATH_PREFIX + SoCName + "/pc_symmetry/bin/";
	}

	public String getCrossIncludePath() {
		if (null == SoCName)
			return EMPTY_STR;
		return SOC_PATH_PREFIX + SoCName + "/cross/include/";
	}

	public String getCrossLibraryPath() {
		if (null == SoCName)
			return EMPTY_STR;
		return SOC_PATH_PREFIX + SoCName + "/cross/lib/";
	}

	public String getRootfsPath() {
		return SOC_PATH_PREFIX + SoCName + "/rootfs";
	}

	public String getCrossMgCfgFileName() {
		if (null == SoCName)
			return EMPTY_STR;
		return SOC_PATH_PREFIX + SoCName + "/cross/etc/MiniGUI.cfg";
	}

	public String getCrossMgNcsCfgFileName() {
		if (null == SoCName)
			return EMPTY_STR;
		return SOC_PATH_PREFIX + SoCName + "/cross/etc/mgncs.cfg";
	}
	public String getSocMginitCfgFile() {
		if (iniFile == null || SoCName == null || !mgRunMode.equals(MiniGUIRunMode.process))
			return EMPTY_STR;

		return SOC_PATH_PREFIX + SoCName + File.separator + "cross/etc/mginit.cfg";
	}

	public String getSOCBinPath() {
		if (null == SoCName)
			return EMPTY_STR;
		return SOC_PATH_PREFIX +SoCName +"/cross/bin/";
	}

	public String getPCMgCfgFileName() {
		if (null == SoCName)
			return EMPTY_STR;
		return SOC_PATH_PREFIX + SoCName + "/pc_symmetry/etc/MiniGUI.cfg";
	}

	public String getPCMgNcsCfgFileName() {
		if (null == SoCName)
			return EMPTY_STR;
		return SOC_PATH_PREFIX + SoCName + "/pc_symmetry/etc/mgncs.cfg";
	}

	//get all valid SoC paths
	public String[] getSoCPaths() {
		File hybridosDir = new File(SOC_PATH_PREFIX);
		return hybridosDir.list(new DirFilter());
	}

	//retry to get SoC name from preference
	public void updateSoCName() {
		String nullString = "null";

		if (null == affectedPkgs)
			affectedPkgs = new HashMap<String, List<String>>();
		else
			affectedPkgs.clear();

		if (null == depPkgs)
			depPkgs = new HashMap<String, List<String>>();
		else
			depPkgs.clear();

		if (null == defaultLibs)
			defaultLibs = new HashMap<String, List<String>>();
		else
			defaultLibs.clear();

		if (null == allSoftPkgs)
			allSoftPkgs = new HashMap<String, MStudioParserIniFile>();
		else
			allSoftPkgs.clear();

		SoCName = MStudioSoCPreferencePage.getCurrentSoC();
//		System.out.println("in MStudioEnfInfo updateSoCName : SoCName -- " + SoCName);
		if (SoCName == null || SoCName.equals(nullString)) {
			SoCName = null;
			iniFile = null;
			return;
		}

		iniFile = new MStudioParserIniFile(getCurSoCConfFileName());
		if (null == iniFile)
			return;

		String runMode = iniFile.getStringProperty(SOC_CFG_SECTION_MINIGUI, SOC_CFG_SECTION_RUNMODE);
		if (null != runMode) {
			try {
				mgRunMode = MiniGUIRunMode.valueOf(runMode);
			} catch (RuntimeException ex) {
				ex.printStackTrace();
				mgRunMode = MiniGUIRunMode.thread;
			}
		}
		else {
			mgRunMode = MiniGUIRunMode.thread;
		}
		
		updateMginitMemus ();

		File socDir = new File(SOC_PATH_PREFIX + SoCName);
		if (!socDir.exists())
			return;

		String hpkgFiles[] = socDir.list(new hpkgFilter());

		for (int i = 0; i < hpkgFiles.length; i++) {
			// ".../.XXXX.hpkg" -->>-- ".../.XXXX"
			String pkgName = hpkgFiles[i].replaceAll(".hpkg", EMPTY_STR);
			// ".../.XXXX" -->>-- "XXXX"
			pkgName = pkgName.substring(pkgName.lastIndexOf('.') + 1);

			MStudioParserIniFile pfgFile = new MStudioParserIniFile(socDir.getAbsolutePath() + File.separator + hpkgFiles[i]);
			allSoftPkgs.put(pkgName, pfgFile);

			// parse the pfgFile
			//String sect = pkgName + "-dev";
			String sect = pfgFile.getStringProperty("package", "name");

			// depend packages ...
			String depends = pfgFile.getStringProperty(sect, "depend");
			if (null != depends && !depends.equals(EMPTY_STR)) {
				String dep[] = depends.split(MSE_SPACE);

				for (int j = 0; j < dep.length; j++) {
					dep[j] = dep[j].replace("-dev", EMPTY_STR);
					List<String> devAff = affectedPkgs.get(dep[j]);
					if (devAff == null) {
						devAff = new ArrayList<String>(1);
					}
					if (!devAff.contains(pkgName)) {
						devAff.add(pkgName);
					}
					affectedPkgs.put(dep[j], devAff);
				}
				List<String> depList = Arrays.asList(dep);
				depPkgs.put(pkgName, depList);
			}

			// libs and packages
			String keyLibs = pfgFile.getStringProperty(sect, "lib");
			if (null != keyLibs && !keyLibs.equals(EMPTY_STR)) {
				String libs[] = keyLibs.split(MSE_SPACE);
				List<String> keyList = Arrays.asList(libs);
				defaultLibs.put(pkgName, keyList);
			}
		}
	}

	//current SoC using MiniGUI in thread run mode.
	public String getMgRunMode() {
		return mgRunMode.name();
	}

	public boolean supportMginitModule() {
		//whether support mginit? If MiniGUI is processes runmode, return true.
		return mgRunMode == MiniGUIRunMode.process;
	}

	public void setSoCNameNull() {
		SoCName = null;
	}

	//get the SoC used by current workspace. If still not set, return null.
	public String getCurSoCName() {
		return SoCName;
	}

	public IProject[] getExecutableProjects() {
		IProject[] msProjects = getMStudioProjects();
		List<IProject> exeProj = new ArrayList<IProject>();
		for (int i = 0; i < msProjects.length ; i++) {
			MStudioProject mpr = new MStudioProject(msProjects[i]);
			if (mpr.isExeTmplType()) {
				exeProj.add(msProjects[i]);
			}
		}
		return (IProject[])exeProj.toArray(new IProject[exeProj.size()]);
	}

	public IProject[] getDlCustomProjects() {

		IProject[] msProjects = getMStudioProjects();
		List<IProject> dlProj = new ArrayList<IProject>();

		for (int i = 0; i < msProjects.length ; i++) {
			MStudioProject mpr = new MStudioProject(msProjects[i]);
			if (mpr.isIALTmplType()) {
				dlProj.add(msProjects[i]);
			}
		}

		return (IProject[])dlProj.toArray(new IProject[dlProj.size()]);
	}

	public IProject[] getSharedLibProjects() {

		IProject[] msProjects = getMStudioProjects();
		List<IProject> sProj = new ArrayList<IProject>();

		for (int i = 0; i < msProjects.length ; i++) {
			MStudioProject mpr = new MStudioProject(msProjects[i]);
			if (mpr.isNormalLibTmplType() || mpr.isMginitModuleTmplType()) {
				sProj.add(msProjects[i]);
			}
		}

		return (IProject[])sProj.toArray(new IProject[sProj.size()]);
	}
/*
	public IProject[] getMginitProjects() {

		IProject[] msProjects = getMStudioProjects();
		List<IProject> sProj = new ArrayList<IProject>();

		for (int i = 0; i < msProjects.length ; i++) {
			MStudioProject mpr = new MStudioProject(msProjects[i]);
			if (mpr.isMginitModuleTmplType()) {
				sProj.add(msProjects[i]);
			}
		}

		return (IProject[])sProj.toArray(new IProject[sProj.size()]);
	}
	*/
	public IProject[] getMStudioProjects() {

		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		if (null == allProjects)
			return null;

		List<IProject> msProjects = new ArrayList<IProject>();

		for (int i = 0; i < allProjects.length; ++i) {
			try {
				if (allProjects[i].hasNature(MStudioProjectNature.MSTUDIO_NATURE_ID))
					msProjects.add(allProjects[i]);
			} catch (CoreException ex) {
				ex.printStackTrace();
//				System.out.println(ex.toString());
			}
		}

		return (IProject[])msProjects.toArray(new IProject[msProjects.size()]);
	}

	public String getWorkSpaceMetadataPath() {
		return Platform.getInstanceLocation().getURL().getPath() + ".metadata/";
	}
	
	public List<String> getResolutions() {
		String section = "resolutions";
		String param = "num";
		String cfgFile = MStudioEnvInfo.getCurSoCConfFileName();
		if (cfgFile == null)
			return null;
		MStudioParserIniFile f = new MStudioParserIniFile(cfgFile);
		if (f == null)
			return null;
		Integer numI = f.getIntegerProperty(section, param);
		int num = (numI == null)? 0 : numI;
		if (num <= 0)
			return null;
		List<String> resolutionList = new ArrayList<String>();
		for(int i = 0; i < num; i++)
		{
			String s = f.getStringProperty(section, "resolution" + i);
			if (!s.matches(RESOLUTION_REGEX_STRING) || s == null)
				continue;
			else
				resolutionList.add(s);	
		}
		return resolutionList;
	}

	public String getScreenSize() {
		String PC_XVFB_SECTION = "pc_xvfb";
		String DEFAULT_MODE_PROPERTY = "defaultmode";

		String cfgF = getWorkSpaceMetadataPath() + "MiniGUI.cfg";
		MStudioParserIniFile f = new MStudioParserIniFile(cfgF);
		if (f == null)
			return null;
		String dxwxh = f.getStringProperty(PC_XVFB_SECTION, DEFAULT_MODE_PROPERTY);
		if (dxwxh == null)
			return null;
		// Check the string is "WWWxHHH-DDbpp" or not. 
		if (!dxwxh.matches(RESOLUTION_REGEX_STRING)) 
			return null;
		
		return dxwxh;
	}
	
	public void updateMginitMemus () {
		
		boolean beShow = MiniGUIRunMode.process.equals(mgRunMode);
		IWorkbenchWindow window = Workbench.getInstance().getActiveWorkbenchWindow();
		
		if(window instanceof WorkbenchWindow) {
		    
		    MenuManager menuManager = ((WorkbenchWindow)window).getMenuManager();
		    if (menuManager != null){
			    String menuId = "org.eclipse.cdt.fmsoft.hybridos.mstudio.menu";
			    IContributionItem menu = menuManager.find(menuId);
			    
			    if (menu != null && menu instanceof MenuManager){
			    	MenuManager hybridMenu = (MenuManager)menu;
			    	String mginitId = "org.eclipse.cdt.fmsoft.hybridos.mstudio.menu.mginitservice";
			    	IContributionItem itm = hybridMenu.find (mginitId);
			    	if (itm instanceof CommandContributionItem) {
			    		CommandContributionItem cci = (CommandContributionItem)itm;
			    		cci.setVisible(beShow);
			    		hybridMenu.update(true);
			    	}
			    }
		    }
		    CoolBarManager cbManager = ((WorkbenchWindow)window).getCoolBarManager();
		    if (cbManager != null){
			    String toolId = "org.eclipse.cdt.fmsoft.hybridos.mstudio.toolbar";
			    IContributionItem toolBar = cbManager.find(toolId);
			    if (toolBar != null && toolBar instanceof ToolBarContributionItem2) {
			    	ToolBarContributionItem2 tci = (ToolBarContributionItem2)toolBar;
			    	IToolBarManager tbm = tci.getToolBarManager();
			    	if (tbm != null){
			    		String mginitId = "org.eclipse.cdt.fmsoft.hybridos.mstudio.toolbar.mginit";
			    		IContributionItem itm = tbm.find(mginitId);
			    		if (itm != null) {
			    			itm.setVisible(beShow);
			    			cbManager.update(true);
			    		}
			    	}
			    }
		    }
		}
	}

	private String getDefaultDepPackagesFileName() {
		String pluginRootPath = getPluginRoot(MStudioPlugin.PLUGIN_ID);
		if (pluginRootPath == null)
		 	return null;

		return pluginRootPath + "project_library.ptkl";
	}

	private String getPluginRoot(String pluginID) {
		String path = null;
		try {
			path = FileLocator.toFileURL(
				Platform.getBundle(pluginID).getEntry("")).getPath();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return path;
	}

	public void setDefaultDepPackages(String section) {
		if (section == null)
			return;

		projectTypeName = section;
		String ddpFile = getDefaultDepPackagesFileName();
		if (ddpFile == null)
			return;

		MStudioParserIniFile fIniFile = new MStudioParserIniFile(ddpFile);
		if (fIniFile == null)
			return;

		defaultKeyLib.clear();
		String keyLib = null;
		if (MSEI_MINIGUI_PROJECT.equals(section)) {
			if (mgRunMode == MiniGUIRunMode.process) {
				keyLib = fIniFile.getStringProperty(section, "key_lib_pe");
			} else if (mgRunMode == MiniGUIRunMode.thread) {
				keyLib = fIniFile.getStringProperty(section, "key_lib_se");
			} else {
				keyLib = fIniFile.getStringProperty(section, "key_lib_sa");
			}
		} else {
			keyLib = fIniFile.getStringProperty(section, "key_lib");
		}
		if (null != keyLib)
			defaultKeyLib.add(keyLib);
		else
			defaultKeyLib.add("freetype");
	}

	public String[] getDefaultKeyLibrary() {
		return defaultKeyLib.toArray(new String[defaultKeyLib.size()]);
	}

	public String getProjectTypeName() {
		return projectTypeName;
	}

	public String[] getDefaultDepPackages() {
		return defaultDepPkgs.toArray(new String[defaultDepPkgs.size()]);
	}

	public void setDefaultDepPackages(String[] defaultDepPkg) {
		if (defaultDepPkg == null || 0 == defaultDepPkg.length)
			return;

		defaultDepPkgs.clear();
		for (int i = 0; i < defaultDepPkg.length; i++) {
			defaultDepPkgs.add(defaultDepPkg[i]);
		}
	}

	public void setCurrentSoC(String name) {
		IPreferenceStore store = MStudioPlugin.getDefault().getPreferenceStore();
		store.putValue(MStudioPreferenceConstants.MSTUDIO_SOC_NAME, name);
		updateSoCName();
	}
	
	public String getDefaultLocationPath(){
		MStudioParserIniFile fIniFile = new MStudioParserIniFile(getCurSoCConfFileName());
		if (fIniFile == null)
			return null;
		final String section = "nfsroot";
		return fIniFile.getStringProperty(section, section);
	}
}

