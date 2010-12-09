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

package org.eclipse.cdt.fmsoft.hybridos.mstudio.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;

import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.preferences.MStudioToolsPreferencePage;


public class MStudioProject {

	private final static String MSTUDIO_VERSION = "org.eclipse.cdt.fmsoft.hybridos.mstudio.version";
	private final static String MSTUDIO_DEPPKGS = "org.eclipse.cdt.feynman.hybridos.mstudio.deppkgs";
	private final static String MSTUDIO_TMPLTYPE = "org.eclipse.cdt.feynman.hybridos.mstudio.tmpltype";
	private final static String MSTUDIO_ENTRYTYPE = "org.eclipse.cdt.feynman.hybridos.mstudio.entrytype";
	private final static String MSTUDIO_DEPLOYABLE = "org.eclipse.cdt.feynman.hybridos.mstudio.deployable";
	//format: respath;binpath;libpath;custompath
	private final static String MSTUDIO_DEPLOY_PATHINFO = "org.eclipse.cdt.feynman.hybridos.mstudio.deploy.pathinfo";
	//format(file using relative project path): file1;file2;file3;...
	private final static String MSTUDIO_DEPLOY_CUSTOMFILES = "org.eclipse.cdt.feynman.hybridos.mstudio.deploy.customfiles";

//	private final static String SPLIT_SEMICOLON = ";";
	private final static String DEPLOY_SPLIT_CHAR=":";
	private final static String EMPTY_STR = "";

	public enum MStudioProjectTemplateType {
		exe,
		normallib,
		dlcustom,
		mginitmodule,
	};

	public enum MStudioProjectEntryType {
		common,
		minigui
	};

	private enum MStudioProjectDefaultDeployable {
		yes,
		no
	};

	private IProject wrapped = null;

	public MStudioProject(IProject wrappedProject) {
		wrapped = wrappedProject;
	}

	public IProject getProject() {
		return wrapped;
	}

	public void initProjectTypeInfo(boolean isMgEntry, MStudioProjectTemplateType type) {

		if (isMgEntry) {
			setPersistentEntryType (MStudioProjectEntryType.minigui);
		} else {
			setPersistentEntryType (MStudioProjectEntryType.common);
		}

		setPersistentTmplType(type);
	}

	private boolean setPersistentSettings(String name, String value) {

		try {
			wrapped.setPersistentProperty(new QualifiedName(EMPTY_STR, name), value);
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private String getPersistentSettings(String name) {

		try {
			return wrapped.getPersistentProperty(new QualifiedName(EMPTY_STR, name));
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return null;
	}

	private boolean setPersistentTmplType(MStudioProjectTemplateType type) {
		return setPersistentSettings(MSTUDIO_TMPLTYPE, type.name());
	}

	private boolean setPersistentEntryType(MStudioProjectEntryType type) {
		return setPersistentSettings(MSTUDIO_ENTRYTYPE, type.name());
	}

	private String getPersistentEntryType() {
		return getPersistentSettings(MSTUDIO_ENTRYTYPE);
	}

	public boolean setDepPkgs(String[] depPkgs) {
		if (depPkgs.length <= 0)
			return false;

		String tmp = semicolonMerger(depPkgs);

		return setPersistentSettings(MSTUDIO_DEPPKGS, tmp);
	}

	public String[] getDepPkgs() {
		String pkgs = getPersistentSettings(MSTUDIO_DEPPKGS);

		if (pkgs != null)
			return pkgs.split(DEPLOY_SPLIT_CHAR);

		return new String[0];
	}

	public boolean setDefaultDeployable(boolean deployable) {
		return setPersistentSettings(MSTUDIO_DEPLOYABLE,
				deployable ? MStudioProjectDefaultDeployable.yes.name()
							: MStudioProjectDefaultDeployable.no.name());
	}

	public boolean getDefaultDeployable() {
		String bool = getPersistentSettings(MSTUDIO_DEPLOYABLE);
		return MStudioProjectDefaultDeployable.yes.name().equals(bool);
	}

	public boolean isExeTmplType() {
		String tmplType = getPersistentSettings(MSTUDIO_TMPLTYPE);
		return MStudioProjectTemplateType.exe.name().equals(tmplType);
	}

	public boolean isIALTmplType() {
		String tmplType = getPersistentSettings(MSTUDIO_TMPLTYPE);
		return MStudioProjectTemplateType.dlcustom.name().equals(tmplType);
	}

	public boolean isMginitModuleTmplType() {
		String tmplType = getPersistentSettings(MSTUDIO_TMPLTYPE);
		return MStudioProjectTemplateType.mginitmodule.name().equals(tmplType);
	}

	public boolean isNormalLibTmplType() {
		String tmplType = getPersistentSettings(MSTUDIO_TMPLTYPE);
		return MStudioProjectTemplateType.normallib.name().equals(tmplType);
	}

	public boolean isMiniGUIEntryType() {
		String tmplType = getPersistentEntryType();
		return MStudioProjectEntryType.minigui.name().equals(tmplType);
	}

	public String getMStudioBinPath() {
		String version = getPersistentSettings(MSTUDIO_VERSION);
		return MStudioToolsPreferencePage.getMStudioBinPath(version);
	}

	public String getMStudioVersion() {
		return getPersistentSettings(MSTUDIO_VERSION);
	}

	public boolean setMStudioVersion(String version) {
		String oldBinPath = getMStudioBinPath();
		boolean result = setPersistentSettings(MSTUDIO_VERSION, version);

		if (result)
			updateMStudioDir(oldBinPath);

		return result;
	}

	public void updateMStudioDir(String oldBinPath) {

		try {
			if (!wrapped.hasNature(CProjectNature.C_NATURE_ID))
				return;
			if (!wrapped.hasNature(MStudioProjectNature.MSTUDIO_NATURE_ID))
				return;

			String msBinPath = getMStudioBinPath();
			if (msBinPath == null)
				return;

			//TODO , set to the ui-builder start ....
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public void scheduleRebuild() {

		final IProject project = wrapped;
		WorkspaceJob cleanJob = new WorkspaceJob("Clean " + project.getName()) {
			public boolean belongsTo(Object family) {
				return ResourcesPlugin.FAMILY_MANUAL_BUILD.equals(family);
			}

			public IStatus runInWorkspace(IProgressMonitor monitor) {

				try {
					project.build(IncrementalProjectBuilder.CLEAN_BUILD, monitor);
					WorkspaceJob buildJob = new WorkspaceJob("Build " + project.getName()) {

						public boolean belongsTo(Object family) {
							return ResourcesPlugin.FAMILY_MANUAL_BUILD.equals(family);
						}

						public IStatus runInWorkspace(IProgressMonitor monitor) {
							try {
								project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
							} catch (CoreException e) {
								System.out.println(" project build Error.");
								return Status.CANCEL_STATUS;
							}
							return Status.OK_STATUS;
						}
					};

					buildJob.setRule(project.getWorkspace().getRuleFactory().buildRule());
					buildJob.setUser(true);
					buildJob.schedule();
				} catch (CoreException e) {
					System.out.println(" project build Error.");
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
		};

		cleanJob.setRule(project.getWorkspace().getRuleFactory().buildRule());
		cleanJob.setUser(true);
		cleanJob.schedule();
	}

	public void addMStudioNature(IProgressMonitor monitor) throws CoreException {

		IProjectDescription description = wrapped.getDescription();
		String[] natures = description.getNatureIds();
		String[] newNatures = new String[natures.length + 1];

		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length + 0] = MStudioProjectNature.MSTUDIO_NATURE_ID;

		description.setNatureIds(newNatures);
		wrapped.setDescription(description, monitor);
	}
	
	public String[] getDeployPathInfo(){
		String deployPath = getPersistentSettings(MSTUDIO_DEPLOY_PATHINFO);
		if (deployPath != null)
			return deployPath.split(DEPLOY_SPLIT_CHAR);
		return new String[0];
	}
	
	public boolean setDeployPathInfo(String[] paths){
		if(paths.length != 4)
			return false;
		else{
			String deployPath = semicolonMerger(paths);
			return setPersistentSettings(MSTUDIO_DEPLOY_PATHINFO,deployPath);
		}
	}
	
	public String[] getDeployCustomFiles(){
		String deployFile = getPersistentSettings(MSTUDIO_DEPLOY_CUSTOMFILES);
		if (deployFile != null && deployFile != "")
			return deployFile.split(DEPLOY_SPLIT_CHAR);
		return new String[0];
	}

	public boolean setDeployCustomFiles(String[] files){
		String tempStr = "";
		if(files != null){		
			if(files.length > 0)
				tempStr = semicolonMerger(files);
		}
		return setPersistentSettings(MSTUDIO_DEPLOY_CUSTOMFILES,tempStr);
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

