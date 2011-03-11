/*********************************************************************
 * Copyright (C) 2002 ~ 2011, Beijing FMSoft Technology Co., Ltd.
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

package org.eclipse.cdt.fmsoft.hybridos.mstudio.project;

import java.io.File;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards.MStudioParserIniFile;

public class MStudioProjectProperties {
	private final static String PROJECT_CFG = ".hproject";
	private MStudioParserIniFile file = null;
	private final static String SYSTEM_SECTION = "project_info";
	private final static String SOCNAME_PROPERTY = "socName";
	private final static String VERSION_PROPERTY = "studioVersion";
	private final static String PACKAGES_PROPERTY = "depPkgs";
	private final static String IS_DEPLOY_PROPERTY = "deployable";
	private final static String TMPL_TYPE_PROPERTY = "tmplType";
	private final static String ENTRY_TYPE_PROPERTY = "entryType";
	private final static String RES_LOCATION_PROPERTY = "resLocation";
	private final static String BIN_LOCATION_PROPERTY = "binLocation";
	private final static String LIB_LOCATION_PROPERTY = "libLocation";
	private final static String CUSTOM_FILE_LOCATION_PROPERTY = "customFileLocation";
	private final static String CUSTOM_FILES_PROPERTY = "customFiles";
	private final static String DEPLOY_SPLIT_CHAR = ":";
	private final static String EMPTY_STR = "";
	private String cfgName = null;

	public MStudioProjectProperties(String path) {
		if(path != null && path.endsWith(PROJECT_CFG))
			cfgName = path;
		else if(path.endsWith(File.separatorChar + ""))
			cfgName = path + PROJECT_CFG;
		else
			cfgName = path + File.separatorChar + PROJECT_CFG;
		file = new MStudioParserIniFile(cfgName);
	}

	public String getProjectCfgFileName() {
		return cfgName;
	}

	public String getProjectSocName() {
		return file.getStringProperty(SYSTEM_SECTION, SOCNAME_PROPERTY);
	}

	public boolean setProjectSocName(String soc) {
		file.setStringProperty(SYSTEM_SECTION, SOCNAME_PROPERTY, 
				null == soc ? EMPTY_STR : soc, null);
		return file.save();
	}

	public String getProjectHybridVersion() {
		return file.getStringProperty(SYSTEM_SECTION, VERSION_PROPERTY);
	}

	public boolean setProjectHybridVersion(String version) {
		file.setStringProperty(SYSTEM_SECTION, VERSION_PROPERTY, 
				null == version ? EMPTY_STR : version, null);
		return file.save();
	}

	public String[] getDepPkgs() {
		String pkgs = file.getStringProperty(SYSTEM_SECTION, PACKAGES_PROPERTY);
		if (pkgs != null)
			return pkgs.split(DEPLOY_SPLIT_CHAR);
		return new String[0];
	}

	public boolean setDepPkgs(String[] depPkgs) {
		String value = semicolonMerger(depPkgs);
		file.setStringProperty(SYSTEM_SECTION, PACKAGES_PROPERTY, 
				null == value ? EMPTY_STR : value, null);
		return file.save();
	}

	public String getProjectTmplType() {
		return file.getStringProperty(SYSTEM_SECTION, TMPL_TYPE_PROPERTY);
	}

	public boolean setProjectTmplType(String type) {
		file.setStringProperty(SYSTEM_SECTION, TMPL_TYPE_PROPERTY, 
				null == type ? EMPTY_STR : type, null);
		return file.save();
	}

	public String getProjectEntryType() {
		return file.getStringProperty(SYSTEM_SECTION, ENTRY_TYPE_PROPERTY);
	}

	public boolean setProjectEntryType(String type) {
		file.setStringProperty(SYSTEM_SECTION, ENTRY_TYPE_PROPERTY, 
				null == type ? EMPTY_STR : type, null);
		return file.save();
	}

	public String getDefaultDeployable() {
		return file.getStringProperty(SYSTEM_SECTION, IS_DEPLOY_PROPERTY);
	}

	public boolean setDefaultDeployable(String deployable) {
		file.setStringProperty(SYSTEM_SECTION, IS_DEPLOY_PROPERTY, 
				null == deployable ? EMPTY_STR : deployable, null);
		return file.save();
	}

	public String[] getDeployPathInfo() {
		String[] deployPath = new String[4];
		deployPath[0] = file.getStringProperty(SYSTEM_SECTION, RES_LOCATION_PROPERTY);
		deployPath[1] = file.getStringProperty(SYSTEM_SECTION, BIN_LOCATION_PROPERTY);
		deployPath[2] = file.getStringProperty(SYSTEM_SECTION, LIB_LOCATION_PROPERTY);
		deployPath[3] = file.getStringProperty(SYSTEM_SECTION, CUSTOM_FILE_LOCATION_PROPERTY);
		return deployPath;
	}

	public boolean setDeployPathInfo(String[] paths) {
		file.setStringProperty(SYSTEM_SECTION, RES_LOCATION_PROPERTY, 
				null == paths[0] ? EMPTY_STR : paths[0], null);
		file.setStringProperty(SYSTEM_SECTION, BIN_LOCATION_PROPERTY, 
				null == paths[1] ? EMPTY_STR : paths[1], null);
		file.setStringProperty(SYSTEM_SECTION, LIB_LOCATION_PROPERTY, 
				null == paths[2] ? EMPTY_STR : paths[2], null);
		file.setStringProperty(SYSTEM_SECTION, CUSTOM_FILE_LOCATION_PROPERTY, 
				null == paths[3] ? EMPTY_STR : paths[3], null);
		return file.save();
	}

	public String[] getDeployCustomFiles(){
		String deployFile = file.getStringProperty(SYSTEM_SECTION, CUSTOM_FILES_PROPERTY);
		if (deployFile != null && deployFile != EMPTY_STR)
			return deployFile.split(DEPLOY_SPLIT_CHAR);
		return new String[0];
	}

	public boolean setDeployCustomFiles(String[] files) {
		String tempStr = semicolonMerger(files);
		file.setStringProperty(SYSTEM_SECTION, CUSTOM_FILES_PROPERTY,
				null == tempStr ? EMPTY_STR : tempStr, null);
		return file.save();
	}
	
	private String semicolonMerger(String[] sm) {
		if (sm.length <= 0)
			return null;
		String deploy = sm[0];
		for (int i = 1; i < sm.length; i++) {
			if (null != sm[i] && ! sm[i].isEmpty())
				deploy += DEPLOY_SPLIT_CHAR + sm[i];
		}

		return deploy;
	}
}