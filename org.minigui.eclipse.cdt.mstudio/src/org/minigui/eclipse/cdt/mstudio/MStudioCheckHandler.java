package org.minigui.eclipse.cdt.mstudio;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.minigui.eclipse.cdt.mstudio.preferences.MStudioPreferencePage;
import org.minigui.eclipse.cdt.mstudio.preferences.PreferenceConstants;
import org.minigui.eclipse.cdt.mstudio.project.MgProject;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

public class MStudioCheckHandler extends AbstractHandler implements IHandler {
	private static String builderVersion;
	private static String pluginVersion;
	private static String baseurl = "http://192.168.1.9:7777/update.php?msver=";
	private static String builderCmd = "guibuilder";
    private static String verDesc = "Builder Version:";
    private static String verChar = " -v";
    
	private void parseVersionLine(String line) {
	    System.out.println(line);
		StringBuffer buf = new StringBuffer();

		if (line.startsWith(verDesc)) {
		    buf.append(line);
		    buf.delete(0, verDesc.length());
		    builderVersion = buf.toString().trim();
			System.out.println(builderVersion);
		}
	}
	
	private boolean getBuilderVersion() {
		String defVersion = MStudioPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.MSVERSION_DEFAULT);
		String binPath = MStudioPreferencePage.getMStudioBinPath(defVersion);
		Path cmd = new Path (binPath + File.separatorChar +builderCmd);
	    Runtime r = Runtime.getRuntime();
	    Process p; 
	    
	    try {
		    p = r.exec(cmd.toOSString() + verChar);
		    BufferedReader is; 
		    is = new BufferedReader(new InputStreamReader(p.getInputStream()));
		    String line;
		    
		    while ((line = is.readLine()) != null) {
				if (line.startsWith(verDesc)) {
					parseVersionLine(line);
					return true;
				}
			    try {
				  p.waitFor();  // wait for process to complete
				} catch (InterruptedException e) {
				  System.err.println(e);
				  return false;
				}
		    }
	    } catch (IOException e) {
	    	return false;
	    }
	    
		return false;
	}

	private void getPluginVersion() {
		Bundle bundle = MStudioPlugin.getDefault().getBundle();
		String version = (String) bundle.getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION);
		Version v = org.osgi.framework.Version.parseVersion(version);
		pluginVersion = v.toString();
		System.out.println(pluginVersion);
	}
	
	private boolean getVersion() {
		getPluginVersion();
		return getBuilderVersion();
	}
	
	private String getUrl() {	
		baseurl = String.format("%s%s%s-%s%s", 
				baseurl, "guibuilder", builderVersion, "msplus", pluginVersion);
		return baseurl;
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (!getVersion())
			return null;
		
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		
        if( !java.awt.Desktop.isDesktopSupported() ) {
    		MessageDialog.openInformation(
    				window.getShell(),
    				"mStudio Plug-in",
    				"Desktop is not supported.");
        }
        else {
            java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

            if( !desktop.isSupported( java.awt.Desktop.Action.BROWSE ) ) {
        		MessageDialog.openInformation(
        				window.getShell(),
        				"mStudio Plug-in",
        				"Desktop doesn't support the browser action.");
            }
            else {
                try {
                    java.net.URI uri = new java.net.URI( getUrl() );
                    desktop.browse( uri );
                }
                catch ( Exception e ) {
            		MessageDialog.openInformation(
            				window.getShell(),
            				"mStudio Plug-in",
            				"Open the update site failure.");
                }
            }
        }
		return null;
	}

}
