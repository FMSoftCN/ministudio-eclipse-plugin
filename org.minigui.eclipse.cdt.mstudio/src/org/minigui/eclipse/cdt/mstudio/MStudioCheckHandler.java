package org.minigui.eclipse.cdt.mstudio;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class MStudioCheckHandler extends AbstractHandler implements IHandler {
	private static String version;
	
	private static boolean getVersion() {
		version = "1.0.0";
		return true;
	}
	
	private static String getUrl() {	
		String baseurl = "http://www.minigui.org";
		return baseurl + "";
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
