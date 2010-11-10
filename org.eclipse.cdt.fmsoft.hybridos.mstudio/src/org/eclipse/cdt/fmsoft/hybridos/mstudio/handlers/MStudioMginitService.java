package org.eclipse.cdt.fmsoft.hybridos.mstudio.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.utils.spawner.EnvironmentReader;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

public class MStudioMginitService extends AbstractHandler implements IElementUpdater {
	
	private Process miniguiServer=null;
	//this file would be create when mginit server is running
	private final static String mginitTmpFile="/var/tmp/mginit";
	
	private boolean mginitHasRunning()
	{
		File file = new File(mginitTmpFile);
		
		if (System.getProperty("os.name").toLowerCase().indexOf("window") >= 0) {
            //TODO .... for windows ...
        }
		return file.exists();
	}
	
	@Override
	public void updateElement(UIElement element, Map parameters) {
		if (mginitHasRunning())
			element.setText(MStudioMessages.getString("MStudioMenu.mginit.stop.label"));
		else
			element.setText(MStudioMessages.getString("MStudioMenu.mginit.start.label"));
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		final String commandId = "org.eclipse.cdt.fmsoft.hybridos.mstudio.commands.mginitservice";
		
		commandService.refreshElements(commandId, null);
		
		if (mginitHasRunning()) {
			stopMginit();
		}
		else {
			startMginit();
		}
		
		return null;
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
	/*
	private static IPath removeFileName(IPath path) {
        if (path == null)
            return null;
        if (path.hasTrailingSeparator())
            return path;
        return path.removeLastSegments(1);
    }
    */

	private void startMginit()
	{
		File files=new File(mginitTmpFile);
		if(!files.exists())
		{
			try {
				/*
				IProgressMonitor monitor = new NullProgressMonitor();
		    	SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
		    	subMonitor.newChild(1).subTask("mStudio Runner - Collecting Data");
		    	*/
		        CommandLauncher launcher = new CommandLauncher();
		        launcher.showCommand(true);
		        //IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		        
		        //IFile ifile = root.getFileForLocation(file); 
		        StringBuffer cmd = new StringBuffer("mginit");

		        // FIXME, for windows .....
		        if (System.getProperty("os.name").toLowerCase().indexOf("window") >= 0) {
		            cmd.append(".exe");
		        }
		        MStudioEnvInfo envInfo = new MStudioEnvInfo();
		        String binPath = envInfo.getMginitBinPath();
		        if(binPath == null)
		        	return;
		        //binPath="/home/jxzhang/work/mg-samples/trunk/mginit";
		        Path editCommand;
		        if (binPath == null || binPath.equals("")) {
		        	editCommand = new Path (cmd.toString());
		        }
		        else
		        	editCommand = new Path (binPath + File.separatorChar +cmd.toString());
		        
		        List<String> args = new ArrayList<String>();	
		        
		        //mginit server would be run with args,arg -c is direct to the mginit config file url
		        args.add("-c");
		        args.add(envInfo.getMginitCfgFile().toString());
		        IPath workingDir= new Path (binPath);
		        
		        Properties envProps = EnvironmentReader.getEnvVars();
		        envProps.setProperty("CWD", workingDir.toOSString());
		        envProps.setProperty("PWD", workingDir.toOSString());	        
		        
		        miniguiServer= launcher.execute(editCommand, (String[])args.toArray(new String[args.size()]), 
		            		createEnvStringList(envProps), workingDir);		        

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	private void stopMginit()
	{
		if(miniguiServer != null)
		{			
			miniguiServer.destroy();
			miniguiServer = null;
		}
		
		try{
			File file=new File(mginitTmpFile);
			if(file.exists())
			{
				file.delete();
			}
		}catch(Exception ex){
			ex.printStackTrace();
			System.out.println(ex.toString());		
		}
	}

}
