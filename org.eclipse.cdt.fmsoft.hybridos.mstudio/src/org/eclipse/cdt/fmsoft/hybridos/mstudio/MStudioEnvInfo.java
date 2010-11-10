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

	//SoC configuration path = SoCPathPrefix + "SoC name" + SoCConfigFile
	private final static String SoCPathPrefix = "/opt/hybridos/";
	private final static String SoCConfigFile = ".hybridos.cfg";
	
	//private final static String SoCConf_section_soc = "soc";
	private final static String SoCConf_section_minigui = "minigui";
	//private final static String SoCConf_section_kernel = "kernel";
	//private final static String SoCConf_section_toolchain = "toolchain";
	private final static String SoCConf_section_services = "services";
	private final static String SoCConf_section_miginit = "mginit";

	//the ini file object which pointer to SoC used by current workspace
	private enum MiniGUIRunMode {
	    thread,
	    processes,
	    standalone
	};

	private MStudioParserIniFile iniFile = null;
	private static String SoCName = null;
	private MiniGUIRunMode mgRunMode = MiniGUIRunMode.thread;
	
	public MStudioEnvInfo() {
		updateSoCName();
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
		if (iniFile == null || SoCName == null || !mgRunMode.equals(MiniGUIRunMode.processes))
			return null;
		return SoCPathPrefix + SoCName + "/" + iniFile.getStringProperty(SoCConf_section_miginit, "bin");
	}
	
	public String getMginitCfgFile() {
		if (iniFile == null || SoCName == null || !mgRunMode.equals(MiniGUIRunMode.processes))
			return null;
		return SoCPathPrefix + SoCName + "/" + iniFile.getStringProperty(SoCConf_section_miginit, "cfg");
	}
	
	public List<String> getServices() {
		int srvCount = iniFile.getIntegerProperty(SoCConf_section_services, "num");
		if (srvCount <= 0)
			return null;
		
		List<String> srvList = new ArrayList<String>(srvCount);
		for (int i = 0; i < srvCount; i++) {
			srvList.add(iniFile.getStringProperty(SoCConf_section_services, "service" + i));
		}
		return srvList;
	}

	//get all soft packages name and description.
	public Map<String, String> getAllSoftPkgs() {
		Map<String, String> mapRet =  new HashMap<String, String>();    
		for(Map.Entry<String, MStudioParserIniFile> entry : allSoftPkgs.entrySet()){    
			String name = entry.getKey().toString();
			MStudioParserIniFile ini = entry.getValue();
			String description = ini.getStringProperty("package", "description");
			mapRet.put(name, description);
		}  
		return mapRet;
	}

	public Map<String, List<String>> getDepPkgs() {
		return depPkgs;
	}
	
	public Map<String, List<String>> getAffectedPkgs() {
		return affectedPkgs;
	}

	//get all valid SoC paths
	public static String[] getSoCPaths() {
		File hybridosDir = new File(SoCPathPrefix);
		return hybridosDir.list(new DirFilter());
	}

	//retry to get SoC name from preference
	public void updateSoCName() {
		IPreferenceStore store = MStudioPlugin.getDefault().getPreferenceStore();
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
		iniFile = new MStudioParserIniFile(getCurSoCConfFileName());
		mgRunMode = MiniGUIRunMode.valueOf(iniFile.getStringProperty(SoCConf_section_minigui, "runmode"));
		
		File socDir = new File(SoCPathPrefix + SoCName);
		String hpkgFiles[] = socDir.list(new hpkgFilter());
		for (int i = 0; i < hpkgFiles.length; i++)
		{
			// ".../.XXXX.hpkg" -->>-- ".../.XXXX"
			String pkgName = hpkgFiles[i].replaceAll(".hpkg", null);
			// ".../.XXXX" -->>-- "XXXX"
			pkgName = pkgName.substring(pkgName.lastIndexOf('.') + 1);
			
			MStudioParserIniFile pfgFile = new MStudioParserIniFile(hpkgFiles[i]);
			allSoftPkgs.put(pkgName, pfgFile);
			
			// parse the pfgFile
			//String sect = pkgName + "-dev";
			String sect = pfgFile.getStringProperty("package", "name");
			
			// depend packages ...
			String depend[] = pfgFile.getStringProperty(sect, "depend").split(" ");

			for (i = 0; i < depend.length; i++) {
				depend[i] = depend[i].replace("-dev", null);
				List<String> devAff = affectedPkgs.get(depend[i]);
				if (devAff == null){
					devAff = new ArrayList<String>(1);
				}
				if (!devAff.contains(pkgName)) {
					devAff.add(pkgName);
				}
				affectedPkgs.put(depend[i], devAff);
			}
			List<String> depList = Arrays.asList(depend);
			depPkgs.put(pkgName, depList);
		}
	}

	//current SoC using MiniGUI in thread run mode.
	public String getMgRunMode() {
		return mgRunMode.name();
	}
	
	public boolean supportMginitModule() {
		//whether support mginit? If MiniGUI is processes runmode, return true.
		return mgRunMode == MiniGUIRunMode.processes;
	}

	//get the SoC used by current workspace. If still not set, return null.
	public static String getCurSoCName() {
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
		return exeProj.size() == 0 ? null : (IProject[])exeProj.toArray();
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
		return dlProj.size() == 0 ? null : (IProject[])dlProj.toArray();
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
		return sProj.size() == 0 ? null : (IProject[])sProj.toArray();
	}
	
	public static IProject[] getMStudioProjects() {
		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		List<IProject> msProjects = new ArrayList<IProject>();
		for (int i = 0; i < allProjects.length; ++i) {
			try {
				if (!allProjects[i].hasNature (MStudioProjectNature.MSTUDIO_NATURE_ID))
					msProjects.add(allProjects[i]);
			} catch (CoreException ex) {}
		}
		return msProjects.size() == 0 ? null : (IProject[])msProjects.toArray();
	}
}


