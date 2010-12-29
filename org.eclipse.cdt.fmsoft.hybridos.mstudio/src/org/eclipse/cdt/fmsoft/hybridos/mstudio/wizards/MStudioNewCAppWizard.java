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
import java.io.FileOutputStream;
import java.io.FileInputStream;

import java.net.URI;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.CWizardHandler;
import org.eclipse.cdt.ui.wizards.IWizardWithMemory;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProject;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProjectNature;


public class MStudioNewCAppWizard extends BasicNewResourceWizard implements
		IExecutableExtension, IWizardWithMemory {

	private final static String MINIGUI_CFG = "MiniGUI.cfg";
	private final static String MGNCS_CFG = "mgncs.cfg";
	private final static String MINIGUI_CFG_TARGET = "MiniGUI.cfg.target";
	private final static String MGNCS_CFG_TARGET = "mgncs.cfg.target";
	private final static String PREFIX = "CProjectWizard";
	private final static String DIALOG_TITLE = MStudioMessages.getString("MGProjectWizard.op_error.title");
	private final static String DIALOG_MESSAGE = MStudioMessages.getString("MGProjectWizard.op_error.message");
	private final static String[] EMPTY_ARR = new String[0];

	protected IConfigurationElement fConfigElement = null;
	protected MStudioNewCAppProjectSelectWizardPage fMainPage = null;
	protected IProject newProject = null;

	private CWizardHandler savedHandler = null;
	private MStudioEnvInfo msEnvInfo = null;
	private URI lastProjectLocation = null;
	private String wz_title = null;
	private String wz_desc = null;
	private String lastProjectName = null;
	private String[] prjDepLibs = null;
	private boolean existingPath = false;

	public MStudioNewCAppWizard() {
		this(MStudioMessages.getString("NewModelCProjectWizard.0"),
				MStudioMessages.getString("NewModelCProjectWizard.1"));
	}

	public MStudioNewCAppWizard(String title, String desc) {
		super();
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
		setNeedsProgressMonitor(true);
		setForcePreviousAndNextButtons(true);
		setWindowTitle(title);
		wz_title = title;
		wz_desc = desc;
		msEnvInfo = MStudioPlugin.getDefault().getMStudioEnvInfo();
	}

	public void addPages() {
		fMainPage = new MStudioNewCAppProjectSelectWizardPage(CUIPlugin.getResourceString(PREFIX));
		fMainPage.setTitle(wz_title);
		fMainPage.setDescription(wz_desc);
		addPage(fMainPage);
	}

	private boolean isChanged() {
		if (savedHandler != fMainPage.h_selected)
			return true;

		if (!fMainPage.getProjectName().equals(lastProjectName))
			return true;

		URI projectLocation = fMainPage.getProjectLocation();
		if (projectLocation == null) {
			if (lastProjectLocation != null)
				return true;
		} else if (!projectLocation.equals(lastProjectLocation))
			return true;

		return savedHandler.isChanged();
	}

	public IProject getProject(boolean defaults) {
		return getProject(defaults, true);
	}

	public IProject getProject(boolean defaults, boolean onFinish) {

		if (newProject != null && isChanged())
			clearProject();

		if (newProject == null) {
			existingPath = false;
			try {
				IFileStore fs = null;
				URI p = fMainPage.getProjectLocation();
				if (p == null) {
					fs = EFS.getStore(ResourcesPlugin.getWorkspace().getRoot().getLocationURI());
					fs = fs.getChild(fMainPage.getProjectName());
				} else {
					fs = EFS.getStore(p);
				}
				IFileInfo f = fs.fetchInfo();
				if (f.exists() && f.isDirectory()) {
					if (fs.getChild(".project").fetchInfo().exists()) {
						if (!MessageDialog.openConfirm(getShell(),
								MStudioMessages.getString("MStudioProjectWizard.0"),
								MStudioMessages.getString("MStudioProjectWizard.1")))
							return null;
					}
					existingPath = true;
				}
			} catch (CoreException e) {
				CUIPlugin.log(e.getStatus());
			}
			savedHandler = fMainPage.h_selected;
			savedHandler.saveState();
			lastProjectName = fMainPage.getProjectName();
			lastProjectLocation = fMainPage.getProjectLocation();

			prjDepLibs = ((MStudioWizardHandler)savedHandler).getCreateDevPackage();

			invokeRunnable(getRunnable(defaults, onFinish));
		}

		return newProject;
	}

	private void clearProject() {
		if (lastProjectName == null)
			return;

		try {
			ResourcesPlugin.getWorkspace().getRoot()
					.getProject(lastProjectName).delete(!existingPath, true, null);
		} catch (CoreException ignore) {
			CUIPlugin.log(ignore.getStatus());
		}

		newProject = null;
		lastProjectName = null;
		lastProjectLocation = null;
		prjDepLibs = null;
	}

	private boolean invokeRunnable(IRunnableWithProgress runnable) {

		IRunnableWithProgress opRunnable = new WorkspaceModifyDelegatingOperation(runnable);

		try {
			getContainer().run(true, true, opRunnable);
		} catch (InvocationTargetException e) {
			CUIPlugin.errorDialog(getShell(), DIALOG_TITLE, DIALOG_MESSAGE, e.getTargetException(), false);
			clearProject();
			return false;
		} catch (InterruptedException e) {
			clearProject();
			return false;
		}

		return true;
	}

	public boolean performFinish() {

		boolean needsPost = (newProject != null && !isChanged());
		// create project if it is not created yet
		if (getProject(fMainPage.isCurrent(), true) == null) {
			MessageDialog.openError(getShell(),
					MStudioMessages.getString("MStudioProjectWizard.op_error.title"),
					MStudioMessages.getString("MStudioProjectWizard.op_error.message"));
			return false;
		}

		fMainPage.h_selected.postProcess(newProject, needsPost);
		try {
			setCreated();
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		}

		BasicNewProjectResourceWizard.updatePerspective(fConfigElement);
		selectAndReveal(newProject);

		copyMiniguiCFG();
		copyMgncsCFG();
		copyMiniguiCFGTarget();
		copyMgncsCFGTarget();

		return true;
	}

	protected boolean setCreated() throws CoreException {

		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		ICProjectDescription des = mngr.getProjectDescription(newProject, false);

		if (des.isCdtProjectCreating()) {
			des = mngr.getProjectDescription(newProject, true);
			des.setCdtProjectCreated();
			mngr.setProjectDescription(newProject, des, false, null);
			return true;
		}

		return false;
	}

	public boolean performCancel() {
		clearProject();
		return fMainPage.h_selected.doCancel();
	}

	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		fConfigElement = config;
	}

	private IRunnableWithProgress getRunnable(boolean _defaults, final boolean onFinish) {

		final boolean defaults = _defaults;
		return new IRunnableWithProgress() {
			public void run(IProgressMonitor imonitor)
					throws InvocationTargetException, InterruptedException {
				getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						try {
							newProject = createIProject(lastProjectName, lastProjectLocation);
							if (newProject != null)
								fMainPage.h_selected.createProject(newProject, defaults, onFinish);
						} catch (CoreException e) {
							CUIPlugin.log(e);
						}
					}
				});
			}
		};
	}

	public IProject createIProject(final String name, final URI location) throws CoreException {

		if (newProject != null)
			return newProject;

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		final IProject newProjectHandle = root.getProject(name);

		if (!newProjectHandle.exists()) {
			IProjectDescription description = workspace
					.newProjectDescription(newProjectHandle.getName());
			if (location != null)
				description.setLocationURI(location);
			newProject = CCorePlugin.getDefault().createCDTProject
								(description,	newProjectHandle, new NullProgressMonitor());
		} else {
			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					newProjectHandle.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
			};
			NullProgressMonitor monitor = new NullProgressMonitor();
			workspace.run(runnable, root, IWorkspace.AVOID_UPDATE, monitor);
			newProject = newProjectHandle;
		}
		// Open the project if we have to
		if (!newProject.isOpen()) {
			newProject.open(new NullProgressMonitor());
		}

		return continueCreation(newProject);
	}

	public String[] getNatures() {
		return new String[] {CProjectNature.C_NATURE_ID, MStudioProjectNature.MSTUDIO_NATURE_ID};
	}

	protected IProject continueCreation(IProject prj) {
		try {
			MStudioProject mprj = new MStudioProject(prj);
			mprj.setDepPkgs(prjDepLibs);
			mprj.addMStudioNature(new NullProgressMonitor());
		} catch (CoreException e) {
			CUIPlugin.log(e.getStatus());
		}

		return prj;
	}

	public void dispose() {
		fMainPage.dispose();
	}

	public boolean canFinish() {
		if (fMainPage.h_selected != null) {
			if (!fMainPage.h_selected.canFinish())
				return false;

			String string = fMainPage.h_selected.getErrorMessage();
			if (string  != null)
				return false;
		}

		return super.canFinish();
	}

	public MStudioEnvInfo getEnvInfo() {
		return msEnvInfo;
	}

	public String getLastProjectName() {
		return lastProjectName;
	}

	public URI getLastProjectLocation() {
		return lastProjectLocation;
	}

	public IProject getLastProject() {
		return newProject;
	}

	// Methods below should provide data for language check
	public String[] getLanguageIDs() {
		return EMPTY_ARR;
	}

	public String[] getContentTypeIDs() {
		return EMPTY_ARR;
	}

	public String[] getExtensions() {
		return EMPTY_ARR;
	}

	private boolean copyMiniguiCFG() {
		String cfgOldName = msEnvInfo.getPCMgCfgFileName();
		String miniguiCFGNewPath = msEnvInfo.getWorkSpaceMetadataPath() + MINIGUI_CFG;

		return copyFile(cfgOldName, miniguiCFGNewPath);
	}

	private boolean copyMgncsCFG() {
		String cfgOldName = msEnvInfo.getPCMgNcsCfgFileName();
		String mgncsCFGNewPath = msEnvInfo.getWorkSpaceMetadataPath() + MGNCS_CFG;

		return copyFile(cfgOldName, mgncsCFGNewPath);
	}

	private boolean copyMiniguiCFGTarget() {
		String cfgOldName = msEnvInfo.getCrossMgCfgFileName();
		String miniguiCFGTarget = msEnvInfo.getWorkSpaceMetadataPath() + MINIGUI_CFG_TARGET;

		return copyFile(cfgOldName, miniguiCFGTarget);
	}

	private boolean copyMgncsCFGTarget() {
		String cfgOldName = msEnvInfo.getCrossMgNcsCfgFileName();
		String mgncsCFGTarget = msEnvInfo.getWorkSpaceMetadataPath() + MGNCS_CFG_TARGET;

		return copyFile(cfgOldName, mgncsCFGTarget);
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
}

