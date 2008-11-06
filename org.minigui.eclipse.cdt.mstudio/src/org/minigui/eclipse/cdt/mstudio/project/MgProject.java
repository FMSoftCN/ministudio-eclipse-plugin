package org.minigui.eclipse.cdt.mstudio.project;

import org.eclipse.cdt.core.CProjectNature;
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

import org.minigui.eclipse.cdt.mstudio.preferences.MStudioPreferencePage;

public class MgProject {
	
	private static final String MSVERSION = "org.minigui.eclipse.cdt.mstudio.properties.mgversion";
 
	private IProject wrapped;
	
	public MgProject(IProject wrappedProject) {
		wrapped = wrappedProject;
	}
	
	public IProject getProject() {
		return wrapped;
	}
	
	public String getMStudioBinPath() {
		try {
			String version = wrapped.getPersistentProperty(new QualifiedName("", MSVERSION));
			return MStudioPreferencePage.getMStudioBinPath(version);
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getMStudioVersion() {
		try {
			return wrapped.getPersistentProperty(new QualifiedName("", MSVERSION));
		} catch (CoreException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public boolean setMStudioVersion(String version) {
		try {
			String oldBinPath = getMStudioBinPath();
			wrapped.setPersistentProperty(new QualifiedName("", MSVERSION), version);
			updateMStudioDir(oldBinPath);
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void updateMStudioDir(String oldBinPath) {	
		try {
			if (!wrapped.hasNature(CProjectNature.C_NATURE_ID)) return;		
			if (!wrapped.hasNature(MgProjectNature.MG_NATURE_ID)) return;

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
					project.build(IncrementalProjectBuilder.CLEAN_BUILD,	monitor);
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
	
	public void addMgNature(IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = wrapped.getDescription();
		String[] natures = description.getNatureIds();

		String[] newNatures = new String[natures.length + 1];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length + 0] = MgProjectNature.MG_NATURE_ID;

		description.setNatureIds(newNatures);
		wrapped.setDescription(description, monitor);
	}

}

