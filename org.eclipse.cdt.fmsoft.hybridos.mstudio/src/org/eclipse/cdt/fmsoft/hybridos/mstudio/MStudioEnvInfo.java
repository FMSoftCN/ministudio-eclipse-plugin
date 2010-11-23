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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards.MStudioParserIniFile;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.preferences.MStudioPreferenceConstants;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProject;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProjectNature;

class DirFilter  implements FilenameFilter {
	public boolean accept(File file,String fname){
		return (file.isDirectory());
	}
}

class hpkgFilter  implements FilenameFilter {
	public boolean accept(File file, String fname){
		return fname.toLowerCase().endsWith(".hpkg");   
	}
}

public class MStudioEnvInfo {
	//key: the specified soft package; value: some packages affected by the specified package
	private Map<String, List<String>> affectedPkgs = null;
	private Map<String, List<String>> depPkgs = null;
	private Map<String, MStudioParserIniFile> allSoftPkgs = null;
	//private Map<String, MStudioParserIniFile> allSoftPkgs = new HashMap<String, MStudioParserIniFile>();
	//SoC configuration path = SoCPathPrefix + "SoC name" + SoCConfigFile
	private final static String SoCPathPrefix = "/opt/hybridos/";
	private final static String SoCConfigFile = ".hybridos.cfg";
	
	//private final static String SOC_CFG_SECTION_SOC = "soc";
	private final static String SOC_CFG_SECTION_MINIGUI = "minigui";
	//private final static String SOC_CFG_SECTION_KERNEL = "kernel";
	//private final static String SOC_CFG_SECTION_TOOLCHAIN = "toolchain";
	private final static String SOC_CFG_SECTION_SERVICES = "services";
	private final static String SOC_CFG_SECTION_MGINIT = "mginit";
	private final static String SOC_CFG_SECTION_TOOLCHAIN = "toolchain";

	//the ini file object which pointer to SoC used by current workspace
	public enum MiniGUIRunMode {
	    thread,
	    process,
	    standalone
	};

	private MStudioParserIniFile iniFile = null;
	private static String SoCName = null;
	private MiniGUIRunMode mgRunMode = MiniGUIRunMode.thread;
	
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
	
	public static MStudioEnvInfo getInstance (){
		return instance;
	}
	
	//-----private methods-----
	private static String getCurSoCConfFileName() {
		if (SoCName == null)
			return null;
		return SoCPathPrefix + SoCName + "/" + SoCConfigFile;
	}

	//get current SoC 
	public MStudioParserIniFile getSoCIniFile() {
		return iniFile;
	}
	
	public String getMginitBinPath () {
		if (iniFile == null || SoCName == null || !mgRunMode.equals(MiniGUIRunMode.process))
			return null;
		return SoCPathPrefix + SoCName + "/" + iniFile.getStringProperty(SOC_CFG_SECTION_MGINIT, "bin");
	}
	
	public String getMginitCfgFile() {
		if (iniFile == null || SoCName == null || !mgRunMode.equals(MiniGUIRunMode.process))
			return null;
		return SoCPathPrefix + SoCName + "/" + iniFile.getStringProperty(SOC_CFG_SECTION_MGINIT, "cfg");
	}
	
	public List<String> getServices() {
		if (iniFile == null)
			return null;
		
		int srvCount = iniFile.getIntegerProperty(SOC_CFG_SECTION_SERVICES, "num");
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

	//get all soft packages name and description.
	public Map<String, String> getAllSoftPkgs() {
		Map<String, String> mapRet =  new HashMap<String, String>();  
		if(null != allSoftPkgs){
			for(Map.Entry<String, MStudioParserIniFile> entry : allSoftPkgs.entrySet()){    
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
	
	public Map<String, List<String>> getAffectedPkgs() {
		return affectedPkgs;
	}
	
	public String[] getPackageLibs (String pkg) {
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
		
		return libs.split(" ");
	}
	
	public String getPCIncludePath () {
		if (null == SoCName)
			return "";
		return SoCPathPrefix + SoCName + "/pc_symmetry/include/";
	}
	
	public String getPCLibraryPath() {
		if (null == SoCName)
			return "";
		return SoCPathPrefix + SoCName + "/pc_symmetry/lib/";
	}
	
	public String getCrossIncludePath(){
		if (null == SoCName)
			return "";
		return SoCPathPrefix + SoCName + "/cross/include/";
	}
	
	public String getCrossLibraryPath() {
		if (null == SoCName)
			return "";
		return SoCPathPrefix + SoCName + "/cross/lib/";
	}

	//get all valid SoC paths
	public String[] getSoCPaths() {
		File hybridosDir = new File(SoCPathPrefix);
		return hybridosDir.list(new DirFilter());
	}

	//retry to get SoC name from preference
	public void updateSoCName() {
		IPreferenceStore store = MStudioPlugin.getDefault().getPreferenceStore();
		String nullString = new String("null");
		if(!store.contains(MStudioPreferenceConstants.MSTUDIO_SOC_NAME)) {
			SoCName = null;
			iniFile = null;
			return;
		}
		if (null == affectedPkgs)
			affectedPkgs = new HashMap<String, List<String>>();
		else
			affectedPkgs.clear();
		if (null == depPkgs)
			depPkgs = new HashMap<String, List<String>>();
		else
			depPkgs.clear();
		if (null == allSoftPkgs)	
			allSoftPkgs = new HashMap<String, MStudioParserIniFile>();
		else
			allSoftPkgs.clear();
		
		SoCName = store.getString(MStudioPreferenceConstants.MSTUDIO_SOC_NAME);
		if (SoCName.equals(nullString)) {
			SoCName = null;
			iniFile = null;
			return;
		}
		iniFile = new MStudioParserIniFile(getCurSoCConfFileName());
		if (null == iniFile) 
			return;
		System.out.println(iniFile.getStringProperty(SOC_CFG_SECTION_MINIGUI, "runmode"));
		mgRunMode = MiniGUIRunMode.valueOf((iniFile.getStringProperty(SOC_CFG_SECTION_MINIGUI, "runmode")));
		
		File socDir = new File(SoCPathPrefix + SoCName);
		if (!socDir.exists())
			return;
		
		String hpkgFiles[] = socDir.list(new hpkgFilter());
		for (int i = 0; i < hpkgFiles.length; i++)
		{
			// ".../.XXXX.hpkg" -->>-- ".../.XXXX"
			String pkgName = hpkgFiles[i].replaceAll(".hpkg", "");
			// ".../.XXXX" -->>-- "XXXX"
			pkgName = pkgName.substring(pkgName.lastIndexOf('.') + 1);

			MStudioParserIniFile pfgFile = new MStudioParserIniFile(socDir.getAbsolutePath() + "/"+ hpkgFiles[i]);
			allSoftPkgs.put(pkgName, pfgFile);
			
			// parse the pfgFile
			//String sect = pkgName + "-dev";
			String sect = pfgFile.getStringProperty("package", "name");
			
			// depend packages ...
			String depends = pfgFile.getStringProperty(sect, "depend");
			if (null != depends && !depends.equals("")){
				String dep[] = depends.split(" ");
	
				for (int j = 0; j < dep.length; j++) {
					dep[j] = dep[j].replace("-dev", "");
					List<String> devAff = affectedPkgs.get(dep[j]);
					if (devAff == null){
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

	//get the SoC used by current workspace. If still not set, return null.
	public String getCurSoCName() {
		return SoCName;
	}
	
	public static IProject[] getExecutableProjects() {
		IProject[] msProjects = getMStudioProjects();
		List<IProject> exeProj = new ArrayList<IProject>();		
		for (int i = 0; i < msProjects.length ; i++) {
			MStudioProject mpr = new MStudioProject (msProjects[i]);
			if (mpr.isExeTmplType()) {
				exeProj.add(msProjects[i]);
			}
		}
		return (IProject[])exeProj.toArray(new IProject[exeProj.size()]);
	}
	
	public static IProject[] getDlCustomProjects() {
		IProject[] msProjects = getMStudioProjects();
		List<IProject> dlProj = new ArrayList<IProject>();
		
		for (int i = 0; i < msProjects.length ; i++) {
			MStudioProject mpr = new MStudioProject (msProjects[i]);
			if (mpr.isIALTmplType()) {
				dlProj.add(msProjects[i]);
			}
		}
		return (IProject[])dlProj.toArray(new IProject[dlProj.size()]);
	}
	
	public static IProject[] getSharedLibProjects() {
		IProject[] msProjects = getMStudioProjects();
		List<IProject> sProj = new ArrayList<IProject>();
		
		for (int i = 0; i < msProjects.length ; i++) {
			MStudioProject mpr = new MStudioProject (msProjects[i]);
			if (mpr.isNormalLibTmplType()) {
				sProj.add(msProjects[i]);
			}
		}
		return (IProject[])sProj.toArray(new IProject[sProj.size()]);
	}
	
	public static IProject[] getMStudioProjects() {
		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		List<IProject> msProjects = new ArrayList<IProject>();
		for (int i = 0; i < allProjects.length; ++i) {
			try {
				if (!allProjects[i].hasNature (MStudioProjectNature.MSTUDIO_NATURE_ID))
					msProjects.add(allProjects[i]);
			} catch (CoreException ex) {
				ex.printStackTrace();
				System.out.println(ex.toString());
			}
		}
		return (IProject[])msProjects.toArray(new IProject[msProjects.size()]);
	}
}

