package org.eclipse.cdt.fmsoft.hybridos.mstudio.templateengine.process.processes;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.processes.CreateSourceFolder;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;

public class MStudioCreateSourceFolder extends CreateSourceFolder {

	public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor) throws ProcessFailureException {
		String projectName = args[0].getSimpleValue();
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (!project.exists()) {
			return;
		}		
		
		MStudioProject mstudioProject = new MStudioProject(project);
		if (mstudioProject.isMiniGUIEntryType())
			super.process(template, args, processId, monitor);
	}

}
