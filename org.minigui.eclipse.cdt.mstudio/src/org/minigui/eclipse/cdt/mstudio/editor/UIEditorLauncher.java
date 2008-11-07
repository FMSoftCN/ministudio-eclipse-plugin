package org.minigui.eclipse.cdt.mstudio.editor;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.utils.spawner.EnvironmentReader;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorLauncher;

public class UIEditorLauncher implements IEditorLauncher {

	public void open(IPath file) {

		CommandLauncher launcher = new CommandLauncher();
		launcher.showCommand(true);
		
		String errMsg = null;
		
		//FIXME , this must be the ui-builder command ....
		// gedit for testing ...
		Path buildCommand = new Path("gedit");
		
		List<String> args = new ArrayList<String>();
		
		args.add(getFileName(file, false));
		IPath workingDir = removeFileName(file);
		
		Properties envProps = EnvironmentReader.getEnvVars();
		envProps.setProperty("CWD", workingDir.toOSString());
		envProps.setProperty("PWD", workingDir.toOSString());

		Process p = launcher.execute(buildCommand, (String[])args.toArray(new String[args.size()]),
				createEnvStringList(envProps), workingDir);
		if (p != null) {
			//TODO, monitor this process ...
			
			System.out.println("p != null");
			
		} else {
			errMsg = launcher.getErrorMessage();
		}


	}
	
	private static String getFileName(IPath path, boolean noextension) {
		if (path == null)
			return null;
		if (path.hasTrailingSeparator())
			return "";
		if (noextension)
			path = path.removeFileExtension();
		
		return path.lastSegment();
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
		Enumeration names = envProps.propertyNames();
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
