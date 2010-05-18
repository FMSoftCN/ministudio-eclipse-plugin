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
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.minigui.eclipse.cdt.mstudio.preferences.MStudioPreferencePage;
import org.minigui.eclipse.cdt.mstudio.preferences.PreferenceConstants;

import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

public class MStudioCheckHandler extends AbstractHandler implements IHandler {
	private static String builderVersion;
	private static String pluginVersion;
	private static String baseurl = "http://auth.minigui.com/mstudio/update/update.php?msver=";
    private static StringBuffer builderCmd = new StringBuffer("guibuilder");
    private static String verDesc = "Build-Version:";
    private static String verChar = " -v";
    
	private void parseVersionLine(String line) {
		StringBuffer buf = new StringBuffer();
	    buf.append(line);
	    buf.delete(0, verDesc.length());
	    builderVersion = buf.toString().trim();
	}
	
	private boolean getBuilderVersion() {
		String defVersion = MStudioPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.MSVERSION_DEFAULT);
		String binPath = MStudioPreferencePage.getMStudioBinPath(defVersion);
		Path cmd;

        if (System.getProperty("os.name").toLowerCase().indexOf("window") >= 0) {
            builderCmd.append(".exe");
        }
		
        if (binPath == null || binPath.equals("")) {
            binPath = System.getenv("GUIBUILDER_PATH");
        }
        if (binPath == null || binPath.equals("")) {
        	cmd = new Path (builderCmd.toString());
        }
        else 
        	cmd = new Path (binPath + File.separatorChar + builderCmd.toString());
		
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
	}
	
	private boolean getVersion() {
		getPluginVersion();
		return getBuilderVersion();
	}
	
	private String getUrl() {
		return String.format("%s%s%s-%s%s", 
				baseurl, "guibuilder", builderVersion, "msplus", pluginVersion);
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		if (!getVersion()) {
			MessageDialog.openInformation(
    				window.getShell(), "mStudio Plug-in",
    				MiniGUIMessages.getString("MStudioUpdateError.desc"));
			return null;
		}

		String url = getUrl();
//		System.out.println(url);
		Shell shell = new Shell(window.getShell());
		shell.setLayout(new FillLayout());
		shell.setText(MiniGUIMessages.getString("MStudioUpdateCaption.desc"));
		
		Browser browser = new Browser(shell, SWT.BORDER);
		FormData data = new FormData();
		data.left = new FormAttachment(0, 5);
		data.right = new FormAttachment(100, -5);
		browser.setLayoutData(data);
		browser.setUrl(url);
		
		shell.open();
		return null;
	}

}
