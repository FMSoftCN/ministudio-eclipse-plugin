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

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.preferences.MStudioToolsPreferencePage;

public class MStudioProject {

	private final static String APP_CFG_PATH = "_res.cfg";
	private IProject wrapped = null;
	private MStudioProjectProperties properties = null;

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

	public MStudioProject(IProject wrappedProject) {
		wrapped = wrappedProject;
		properties = new MStudioProjectProperties(wrapped.getLocation().toOSString());
	}

	public IProject getProject() {
		return wrapped;
	}

	public void initProjectTypeInfo(boolean isMgEntry,
			MStudioProjectTemplateType type) {

		if (isMgEntry) {
			setProjectEntryType(MStudioProjectEntryType.minigui);
		} else {
			setProjectEntryType(MStudioProjectEntryType.common);
		}

		setProjectTmplType(type);
	}

	public boolean setDepPkgs(String[] depPkgs) {
		return properties.setDepPkgs(depPkgs);
	}

	public String[] getDepPkgs() {
		return properties.getDepPkgs();
	}

	public boolean setDefaultDeployable(boolean deployable) {
		String isDeploy = null;
		if (deployable) {
			isDeploy = MStudioProjectDefaultDeployable.yes.name();
		} else {
			isDeploy = MStudioProjectDefaultDeployable.no.name();
		}
		return properties.setDefaultDeployable(isDeploy);
	}

	public boolean getDefaultDeployable() {
		String isDeploy = properties.getDefaultDeployable();
		return MStudioProjectDefaultDeployable.yes.name().equals(isDeploy);
	}

	public boolean isExeTmplType() {
		String tmplType = properties.getProjectTmplType();
		return MStudioProjectTemplateType.exe.name().equals(tmplType);
	}

	public boolean isIALTmplType() {
		String tmplType = properties.getProjectTmplType();
		return MStudioProjectTemplateType.dlcustom.name().equals(tmplType);
	}

	public boolean isMginitModuleTmplType() {
		String tmplType = properties.getProjectTmplType();
		return MStudioProjectTemplateType.mginitmodule.name().equals(tmplType);
	}

	public boolean isNormalLibTmplType() {
		String tmplType = properties.getProjectTmplType();
		return MStudioProjectTemplateType.normallib.name().equals(tmplType);
	}

	public boolean isMiniGUIEntryType() {
		String type = properties.getProjectEntryType();
		return MStudioProjectEntryType.minigui.name().equals(type);
	}

	private boolean setProjectTmplType(MStudioProjectTemplateType type) {
		return properties.setProjectTmplType(type.name());
	}

	private boolean setProjectEntryType(MStudioProjectEntryType type) {
		return properties.setProjectEntryType(type.name());
	}

	public String getMStudioBinPath() {
		String version = getMStudioVersion();
		return MStudioToolsPreferencePage.getMStudioBinPath(version);
	}

	public String getMStudioVersion() {
		return properties.getProjectHybridVersion();
	}

	public boolean setMStudioVersion(String version) {
		String oldBinPath = getMStudioBinPath();
		properties.setProjectHybridVersion(version);
		updateMStudioDir(oldBinPath);
		return true;
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

			// TODO , set to the ui-builder start ....
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

	public String[] getDeployPathInfo() {
		return properties.getDeployPathInfo();
	}

	public boolean setDeployPathInfo(String[] paths) {
		return properties.setDeployPathInfo(paths);
	}

	public String[] getDeployCustomFiles() {
		return properties.getDeployCustomFiles();
	}

	public boolean setDeployCustomFiles(String[] files) {
		return properties.setDeployCustomFiles(files);
	}

	public String getProgramCfgFile() {
		if (isMiniGUIEntryType()) {
			return wrapped.getLocation().toOSString() + "/."
					+ wrapped.getName().trim() + APP_CFG_PATH;
		}
		return null;
	}

	public boolean isProgramCfgFileExist() {
		if (!isMiniGUIEntryType()) {
			return false;
		}
		return new File(getProgramCfgFile()).exists();
	}

	public String getProjectCfgFileName() {
		return properties.getProjectCfgFileName();
	}

	public String getProjectSocName() {
		return properties.getProjectSocName();
	}

	public void setProjectSocName(String soc) {
		properties.setProjectSocName(soc);
	}
	
}
