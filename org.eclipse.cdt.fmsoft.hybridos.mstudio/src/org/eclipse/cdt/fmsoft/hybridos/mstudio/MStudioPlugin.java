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

package org.eclipse.cdt.fmsoft.hybridos.mstudio;

import java.io.File;
import java.io.FilenameFilter;

import org.osgi.framework.BundleContext;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioSocketServerThread;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProject;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProjectNature;


/**
 * The activator class controls the plug-in life cycle
 */
public class MStudioPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.fmsoft.hybridos.mstudio";

	// The shared instance
	private static MStudioPlugin plugin = null;
	private static MStudioResourceChangeListener listener = null;

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
	
	class resCfgFilter implements FilenameFilter {

		public boolean accept(File file, String fname) {
			System.out.println("resCfgFilter : "+ fname);
			return fname.toLowerCase().endsWith("_res.cfg");
		}
	}

	public void updateProject(String projectName) throws CoreException {
		MStudioSocketServerThread serverSocket = MStudioSocketServerThread.get();
		if (serverSocket != null) {
			//please waiting for closing process
			serverSocket.closeProject(projectName);
		}
		
		// TODO
		// change the name of <project_name>_res.cfg .
		// maybe need to changed some code in main()
		// or maybe not to changed the file name.
		/*
		IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (prj.hasNature(MStudioProjectNature.MSTUDIO_NATURE_ID)){
			// this is the new file
			String prj_res_cfg = new MStudioProject(prj).getProgramCfg();
			if (null != prj_res_cfg){
				String ofs[] = prj.getFullPath().toFile().list(new resCfgFilter());
				if (ofs != null && ofs.length > 0){
					System.out.println("old file : " + ofs[0]);
					File of =  new File(ofs[0]);
					if (of.exists()) {
						of.renameTo(new File(prj_res_cfg));
					}
				}
			}
		}
		*/
	}

	public class MStudioResourceChangeListener implements IResourceChangeListener {

		public void resourceChanged(IResourceChangeEvent event) {
			IResourceDelta delta = event.getDelta();
			if (delta == null) {
				return;
			}

			IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
				public boolean visit(IResourceDelta delta) {
					
					IResource resource = delta.getResource();

					if (delta.getKind() == IResourceDelta.CHANGED
							&& resource.getType() == IResource.PROJECT) {
						try {
							updateProject(resource.getName());
						} catch (CoreException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
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

	public MStudioEnvInfo getMStudioEnvInfo () {
		return MStudioEnvInfo.getInstance();
	}
}

