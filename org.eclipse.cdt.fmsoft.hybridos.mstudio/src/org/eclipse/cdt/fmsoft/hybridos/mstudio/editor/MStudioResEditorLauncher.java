package org.eclipse.cdt.fmsoft.hybridos.mstudio.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioSocketServerThread;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProject;
import org.eclipse.cdt.utils.spawner.EnvironmentReader;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ui.IEditorLauncher;


public class MStudioResEditorLauncher implements IEditorLauncher {

    public MStudioResEditorLauncher() { }

    public void open(IPath file) 
    {
    	IProgressMonitor monitor = new NullProgressMonitor();
    	SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
    	subMonitor.newChild(1).subTask("mStudio Runner - Collecting Data");
    	
        CommandLauncher launcher = new CommandLauncher();
        launcher.showCommand(true);
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IFile ifile = root.getFileForLocation(file); 
        IProject project = ifile.getProject();	
        String binPath = new MStudioProject(project).getMStudioBinPath();
        if (binPath == null || binPath.equals("")) {
            binPath = System.getenv("GUIBUILDER_PATH");
        }

        StringBuffer cmd = new StringBuffer("guibuilder");

        if (System.getProperty("os.name").toLowerCase().indexOf("window") >= 0) {
            cmd.append(".exe");
        }

        Path editCommand;
        if (binPath == null || binPath.equals("")) {
        	editCommand = new Path (cmd.toString());
        }
        else
        	editCommand = new Path (binPath+ File.separatorChar +cmd.toString());
        
        List<String> args = new ArrayList<String>();	

        IPath projectDir = removeFileName(file).removeLastSegments(1);
        args.add("-project");

        args.add(projectDir.toOSString());
        args.add("-project-name");
        args.add(projectDir.lastSegment());

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

        try {
            Process p = launcher.execute(editCommand, (String[])args.toArray(new String[args.size()]),
                    createEnvStringList(envProps), workingDir, monitor);
            if (p != null) {
            	subMonitor.newChild(1).subTask("mStudio Runner - Starting GUIBuilder: " + launcher.getCommandLine());
                if (serverThread != null)
                    serverThread.addBuilderProcs(projectDir.lastSegment(), p);
            } 
        } catch (CoreException e) {
        	System.out.println(e);
        }
        monitor.done();
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
