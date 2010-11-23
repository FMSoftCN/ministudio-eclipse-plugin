package org.eclipse.cdt.fmsoft.hybridos.mstudio.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;
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
	
//		FIXME, for windows ...
//		if (System.getProperty("os.name").toLowerCase().indexOf("window") >= 0) {
//			......
//        }
		
		return file.exists();
	}
	
	public void updateElement(UIElement element, Map parameters) {
		if (mginitHasRunning())
			element.setText(MStudioMessages.getString("MStudioMenu.mginit.stop.label"));
		else
			element.setText(MStudioMessages.getString("MStudioMenu.mginit.start.label"));
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		final String commandId = "org.eclipse.cdt.fmsoft.hybridos.mstudio.commands.mginitservice";
		
		commandService.refreshElements(commandId, null);
		
		if (mginitHasRunning()) {
			stopMginit();
		} else {
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
	
	private void startMginit()
	{
		if(mginitHasRunning())
		{
			try {
				CommandLauncher launcher = new CommandLauncher();
				launcher.showCommand(true);
				StringBuffer cmd = new StringBuffer("mginit");
				
//				FIXME, for windows .....
//				if (System.getProperty("os.name").toLowerCase().indexOf("window") >= 0) {
//					cmd.append(".exe");
//				}
				MStudioEnvInfo envInfo = MStudioPlugin.getDefault().getMStudioEnvInfo();
				String binPath = envInfo.getMginitBinPath();
				if(binPath == null)
					return;
				Path editCommand;
				if (binPath == null || binPath.equals("")){
					editCommand = new Path (cmd.toString());
				} else {
		        	editCommand = new Path (binPath + File.separatorChar +cmd.toString());
		        }
		        
				List<String> args = new ArrayList<String>();	
		      
				//mginit server would be run with args,
				//arg -c is direct to the mginit config file url
				
				args.add("-c");
				args.add(envInfo.getMginitCfgFile().toString());
				IPath workingDir= new Path (binPath);
				
				Properties envProps = EnvironmentReader.getEnvVars();
				envProps.setProperty("CWD", workingDir.toOSString());
				envProps.setProperty("PWD", workingDir.toOSString());	        
				
				miniguiServer= launcher.execute(editCommand, 
						(String[])args.toArray(new String[args.size()]), 
						createEnvStringList(envProps), workingDir);		        

			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
	}
	
	
	private void stopMginit()
	{
		try{
			//if mginitServer exist kill it
			if(miniguiServer != null){			
				miniguiServer.destroy();
				miniguiServer = null;
			} else {				
				//find mginit process in all processes ,if find kill it.
				List<String> args = new ArrayList<String>();
				List<String> returnValues=new ArrayList<String>();
				args.add("pgrep");
				args.add("-o");
				args.add("mginit");
				Process p = Runtime.getRuntime().exec((String [])args.toArray(new String[args.size()]));
				InputStream is = p.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String line="";
				while((line=br.readLine())!=null){
					returnValues.add(line.toString());
				}
				br.close();
				if(returnValues.size() > 0){
					for(int i = 0; i < returnValues.size(); i++) {
						args.clear();
						args.add("kill");
						args.add(returnValues.get(i).toString());
						Runtime.getRuntime().exec((String [])args.toArray(new String[args.size()]));						
					}
				}
				p.destroy();
			}				
			//find the temp file, if exist delete it
			File file=new File(mginitTmpFile);
			if(file.exists()){
				file.delete();
			}
		}catch(Exception ex){
			ex.printStackTrace();
			System.out.println(ex.toString());		
		}
	}

}
