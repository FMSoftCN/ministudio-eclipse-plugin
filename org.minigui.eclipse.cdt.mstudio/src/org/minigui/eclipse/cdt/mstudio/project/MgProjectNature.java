package org.minigui.eclipse.cdt.mstudio.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class MgProjectNature implements IProjectNature {

	public static final String MG_NATURE_ID = "org.minigui.eclipse.cdt.mstudio.project.MgProjectNature";
	
	private IProject fProject;

	public MgProjectNature() {
	}

	public MgProjectNature(IProject project) {
		setProject(project);
	}

	public void configure() throws CoreException {
	}

	public void deconfigure() throws CoreException {
	}

	public IProject getProject() {
		return fProject;
	}

	public void setProject(IProject project) {
		fProject = project;
	}
}

