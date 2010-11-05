package org.eclipse.cdt.fmsoft.hybridos.mstudio.project;

import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.preferences.MStudioToolsPreferencePage;
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


public class MStudioProject {
	private enum MStudioProjectTemplateType {
	        exe,
	        normallib,
	        dlcustom,
	        mginitmodule,
	};

	private enum MStudioProjectEntryType {
	        common,
	        minigui
	};

	private enum MStudioProjectDefaultDeployable {
	        yes,
	        no	
	};
	
	private static final String MSTUDIO_VERSION = "org.eclipse.cdt.fmsoft.hybridos.mstudio.version";
	private final static String MSTUDIO_DEPPKGS = "org.eclipse.cdt.feynman.hybridos.mstudio.deppkgs";
	private final static String MSTUDIO_TMPLTYPE = "org.eclipse.cdt.feynman.hybridos.mstudio.tmpltype";
	private final static String MSTUDIO_ENTRYTYPE = "org.eclipse.cdt.feynman.hybridos.mstudio.entrytype";
	private final static String MSTUDIO_DEPLOYABLE = "org.eclipse.cdt.feynman.hybridos.mstudio.deployable";
	
	private IProject wrapped;
	
	public MStudioProject(IProject wrappedProject) {
		wrapped = wrappedProject;
	}
	
	public IProject getProject() {
		return wrapped;
	}
	
	public void initProjectTypeInfo(boolean isLibrary, boolean isMginitModule, boolean isMgEntry) {
		if (isMgEntry)
			setPersistentSettings(MSTUDIO_ENTRYTYPE, MStudioProjectEntryType.minigui.name());
		else
			setPersistentSettings(MSTUDIO_ENTRYTYPE, MStudioProjectEntryType.common.name());
		
		if (isMginitModule)
			setPersistentTmplType(MStudioProjectTemplateType.mginitmodule);
		//TODO:
		
	}
	
	private boolean setPersistentSettings(String name, String value) {
		try {
			wrapped.setPersistentProperty(new QualifiedName("", name), value);
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private String getPersistentSettings(String name) {
		try {
			return wrapped.getPersistentProperty(new QualifiedName("", name));
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
		//TODO:
		return false;
	}
	
	public String[] getDepPkgs() {
		try {
			String pkgs = wrapped.getPersistentProperty(new QualifiedName("", MSTUDIO_DEPPKGS));
			return pkgs.split(" ?", 1);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean setDefaultDeployable(boolean deployable) {
		String newValue;
		if (deployable)
			newValue = MStudioProjectDefaultDeployable.yes.name();
		else
			newValue = MStudioProjectDefaultDeployable.no.name();
		
		return setPersistentSettings(MSTUDIO_DEPLOYABLE, newValue);
	}
	
	public boolean getDefaultDeployable() {
		return getPersistentSettings(MSTUDIO_DEPLOYABLE).equals(MStudioProjectDefaultDeployable.yes);
	}

	public boolean isExeTmplType() {
		String tmplType = getPersistentSettings(MSTUDIO_TMPLTYPE);
		if (tmplType.equals(MStudioProjectTemplateType.exe))
			return true;
		
		return false;
	}
	
	public boolean isIALTmplType() {
		String tmplType = getPersistentSettings(MSTUDIO_TMPLTYPE);
		if (tmplType.equals(MStudioProjectTemplateType.dlcustom))
			return true;

		return false;
	}
	
	public boolean isMginitModuleTmplType() {
		String tmplType = getPersistentSettings(MSTUDIO_TMPLTYPE);
		if (tmplType.equals(MStudioProjectTemplateType.mginitmodule))
			return true;
		
		return false;
	}
	
	public boolean isNormalLibTmplType() {
		String tmplType = getPersistentSettings(MSTUDIO_TMPLTYPE);
		if (tmplType.equals(MStudioProjectTemplateType.normallib))
			return true;
		
		return false;
	}
	
	public boolean isMiniGUIEntryType() {
		String tmplType = getPersistentEntryType();
		if (tmplType.equals(MStudioProjectEntryType.minigui))
			return true;

		return false;
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
		updateMStudioDir(oldBinPath);
		return result;
	}

	public void updateMStudioDir(String oldBinPath) {	
		try {
			if (!wrapped.hasNature(CProjectNature.C_NATURE_ID)) return;		
			if (!wrapped.hasNature(MStudioProjectNature.MSTUDIO_NATURE_ID)) return;

			String msBinPath = getMStudioBinPath();
			if (msBinPath == null) 	return;
			
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
								project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD,monitor);
							} catch (CoreException e) {
							}
							return Status.OK_STATUS;
						}
					};
					buildJob.setRule(project.getWorkspace().getRuleFactory().buildRule());
					buildJob.setUser(true);
					buildJob.schedule();
				} catch (CoreException e) {
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

}

