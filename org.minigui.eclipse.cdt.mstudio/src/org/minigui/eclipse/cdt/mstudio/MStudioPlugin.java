package org.minigui.eclipse.cdt.mstudio;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
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
	private static MStudioResourceChangeListener listener;
	
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
		listener = new MStudioResourceChangeListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		MStudioSocketServerThread serverSocket = MStudioSocketServerThread.get();
		if (serverSocket != null) {
			serverSocket.closeServer();
		}
		super.stop(context);
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
		plugin = null;
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
	
	public void updateProject(String projectName) {
		MStudioSocketServerThread serverSocket = MStudioSocketServerThread.get();
		if (serverSocket != null) {
			//please waiting for closing process
			serverSocket.closeProject(projectName);
		}
	}
	
	public class MStudioResourceChangeListener implements IResourceChangeListener {

		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			// TODO Auto-generated method stub
			IResourceDelta delta = event.getDelta();
			if (delta == null) {
				return;
			}
			IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
				public boolean visit(IResourceDelta delta) {

					if (delta.getKind() == IResourceDelta.REMOVED) {	
							IResource resource = delta.getResource();
							if (resource.getType() == IResource.PROJECT) {
								updateProject(resource.getName());
							}
					}
					return true;
				}
			};
			
			try {
				delta.accept(visitor);
			} catch (CoreException e) {
			}
		}
	}
	
}
