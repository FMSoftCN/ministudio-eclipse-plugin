package org.eclipse.cdt.fmsoft.hybridos.mstudio.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class MStudioProjectNature implements IProjectNature {

	public static final String MSTUDIO_NATURE_ID = "org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProjectNature";
	
	private IProject fProject;

	public MStudioProjectNature() {
	}

	public MStudioProjectNature(IProject project) {
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

