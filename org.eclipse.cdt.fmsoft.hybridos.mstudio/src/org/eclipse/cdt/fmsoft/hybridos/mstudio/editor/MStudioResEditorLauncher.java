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

package org.eclipse.cdt.fmsoft.hybridos.mstudio.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;

import org.eclipse.ui.IEditorLauncher;

import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.utils.spawner.EnvironmentReader;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProject;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioSocketServerThread;


public class MStudioResEditorLauncher implements IEditorLauncher {

	private final static String MSEL_GUIBUILDER_PATH = "GUIBUILDER_PATH";
	private final static String MSEL_TASK_COLLECTING = "hybridStudio Runner - Collecting Data";
	private final static String MSEL_TASK_STARTING   = "hybridStudio Runner - Starting GUIBuilder: ";
	private final static String MSEL_EMPTY_STR = "";

	public MStudioResEditorLauncher() {
	}

	public void open(IPath file) {

		IProgressMonitor monitor = new NullProgressMonitor();
		SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
		subMonitor.newChild(1).subTask(MSEL_TASK_COLLECTING);

		CommandLauncher launcher = new CommandLauncher();
		launcher.showCommand(true);

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getFileForLocation(file).getProject();
		String binPath = new MStudioProject(project).getMStudioBinPath();

		if (binPath == null || binPath.equals(MSEL_EMPTY_STR)) {
			binPath = System.getenv(MSEL_GUIBUILDER_PATH);
		}

		StringBuffer cmd = new StringBuffer("guibuilder");

		if (System.getProperty("os.name").toLowerCase().indexOf("window") >= 0) {
			cmd.append(".exe");
		}

		Path editCommand = null;

		if (binPath == null || binPath.equals(MSEL_EMPTY_STR)) {
			editCommand = new Path (cmd.toString());
		} else {
			editCommand = new Path (binPath+ File.separatorChar + cmd.toString());
		}

		List<String> args = new ArrayList<String>();
		IPath projectDir = removeFileName(file).removeLastSegments(1);

		MStudioEnvInfo info = MStudioPlugin.getDefault().getMStudioEnvInfo();
		args.add("-project");
		args.add(projectDir.toOSString());
		args.add("-project-name");
		args.add(projectDir.lastSegment());
		args.add("-screen-size");
		String screenSize = info.getScreenSize();
		if (null != screenSize){
			args.add(info.getScreenSize());
		}

		//add port information
		MStudioSocketServerThread serverThread = MStudioSocketServerThread.getInstance();
		if (serverThread != null) {
			args.add("-addr");
			args.add(serverThread.getAddress());
			args.add("-port");
			args.add(Integer.toString(serverThread.getPort()));
		}

		IPath workingDir = projectDir;
		Properties envProps = EnvironmentReader.getEnvVars();
		envProps.setProperty("CWD", workingDir.toOSString());
		envProps.setProperty("PWD", workingDir.toOSString());

		Process p = launcher.execute(editCommand, (String[])args.toArray(new String[args.size()]),
				createEnvStringList(envProps), workingDir);
		if (p != null) {
			subMonitor.newChild(1).subTask(MSEL_TASK_STARTING + launcher.getCommandLine());
			if (serverThread != null)
				serverThread.addBuilderProcs(projectDir.lastSegment(), p);
		}
	}

	private static IPath removeFileName(IPath path) {

		if (path == null)
			return null;

		if (path.hasTrailingSeparator())
			return path;

		return path.removeLastSegments(1);
	}

	private static String[] createEnvStringList(Properties envProps) {

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

