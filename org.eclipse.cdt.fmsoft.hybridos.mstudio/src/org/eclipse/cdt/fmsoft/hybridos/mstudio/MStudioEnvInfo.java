package org.eclipse.cdt.fmsoft.hybridos.mstudio;

import java.util.List;
import java.util.Map;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards.MStudioParserIniFile;
import org.eclipse.core.resources.IProject;

public class MStudioEnvInfo {
	//key: the specified soft package; value: some packages affected by the specified package
	private Map<String, List<String>> affectedPkgs;
	private Map<String, List<String>> depPkgs;
	private Map<String, MStudioParserIniFile>allSoftPkgs;

	//SoC configuration path = SoCPathPrefix + "SoC name" + SoCConfigFile
	private final static String SoCPathPrefix = "/opt/hybridos/";
	private final static String SoCConfigFile = ".hybridos.cfg";

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
		// TODO Auto-generated constructor stub
	}
	
	//-----private methods-----
	private static String getCurSoCConfFileName() {
		//TODO:
		return null;
	}

	//get current SoC 
	public MStudioParserIniFile getSoCIniFile() {
		return null;
	}
	
	public List<String> getServices() {
		return null;
	}

	//get all soft packages name and description.
	public Map<String, String> getAllSoftPkgs() {
		return null;
	}

	public Map<String, List<String>> getDepPkgs() {
		return null;
	}
	
	public Map<String, List<String>> getAffectedPkgs() {
		return null;
	}

	//get all valid SoC paths
	public static String[] getSoCPaths() {
		return null;
	}

	//retry to get SoC name from preference
	public void updateSoCName() {

	}

	//current SoC using MiniGUI in thread run mode.
	public String getMgRunMode() {
		return mgRunMode.name();
	}
	
	public boolean supportMginitModule() {
		//whether support mginit? If MiniGUI is processes runmode, return true.
		return false;
	}

	//get the SoC used by current workspace. If still not set, return null.
	public static String getCurSoCName() {
		return SoCName;
	}
	
	public static IProject[] getExecutableProjects() {
		return null;
	}
	
	public static IProject[] getDlCustomProjects() {
		return null;
	}
	
	public static IProject[] getSharedLibProjects() {
		return null;
	}
	
	public static IProject[] getMStudioProjects() {
		return null;
	}

}
