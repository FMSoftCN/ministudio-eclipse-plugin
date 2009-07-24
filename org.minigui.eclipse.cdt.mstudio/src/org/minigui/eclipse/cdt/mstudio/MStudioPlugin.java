package org.minigui.eclipse.cdt.mstudio;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.minigui.eclipse.cdt.mstudio.MStudioSocketServerThread;

/**
 * The activator class controls the plug-in life cycle
 */
public class MStudioPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.minigui.eclipse.cdt.mstudio";

	// The shared instance
	private static MStudioPlugin plugin;
	
	/**
	 * The constructor
	 */
	public MStudioPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
    	
		MStudioSocketServerThread instance = 
			MStudioSocketServerThread.getInstance();
		/* This server thread starts only once. */
		if ( instance.Started != 0 ) {
			instance.closeServer();
		}
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static MStudioPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
