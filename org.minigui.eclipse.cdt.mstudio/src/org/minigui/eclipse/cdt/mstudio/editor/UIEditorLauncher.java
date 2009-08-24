package org.minigui.eclipse.cdt.mstudio.editor;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.utils.spawner.EnvironmentReader;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorLauncher;

import org.minigui.eclipse.cdt.mstudio.MStudioSocketServerThread;
import org.minigui.eclipse.cdt.mstudio.project.MgProject;


public class UIEditorLauncher implements IEditorLauncher {

	public UIEditorLauncher()
	{
	}
	
	public void open(IPath file) 
	{
		CommandLauncher launcher = new CommandLauncher();
		launcher.showCommand(true);
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile ifile = root.getFileForLocation(file); 
		IProject project = ifile.getProject();		
		String binPath = new MgProject(project).getMStudioBinPath();		
		
		Path editCommand = new Path (binPath+"/"+"guibuilder");
		List<String> args = new ArrayList<String>();
		
		args.add(file.toString());
		//args.add(getFileName(file, false));
		//add port information
		MStudioSocketServerThread serverThread = MStudioSocketServerThread.getInstance();
		if (serverThread != null) {
			args.add(" -addr ");
			args.add(serverThread.getAddress());
			args.add(" -port ");
			args.add(Integer.toString(serverThread.getPort()));
			System.out.println(args);
		}
		
		IPath workingDir = removeFileName(file).removeLastSegments(1);
		
		Properties envProps = EnvironmentReader.getEnvVars();
		envProps.setProperty("CWD", workingDir.toOSString());
		envProps.setProperty("PWD", workingDir.toOSString());

		//for (int i = 0 ; i < args.size(); i++){
		//	System.out.println("args "+ i + " : " + ((String[])args.toArray(new String[args.size()]))[i]);
		//}
		
		Process p = launcher.execute(editCommand, (String[])args.toArray(new String[args.size()]),
				createEnvStringList(envProps), workingDir);

		if (p != null) {
			if (serverThread != null)
				serverThread.addBuilderProcs(p);
			//TODO, monitor this process ...
		} else {
			//TODO for error ...
		}
	}

	private static IPath removeFileName(IPath path) {
		if (path == null)
			return null;
		if (path.hasTrailingSeparator())
			return path;
		return path.removeLastSegments(1);
	}
	
	private static String[] createEnvStringList(Properties envProps)
	{
		String[] env = null;
		List<String> envList = new ArrayList<String>();
		Enumeration<?> names = envProps.propertyNames();
		if (names != null) {
			while (names.hasMoreElements()) {
				String key = (String) names.nextElement();
				envList.add(key + "=" + envProps.getProperty(key));
			}
			env = (String[]) envList.toArray(new String[envList.size()]);
		}
		return env;
	}
}
