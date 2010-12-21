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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

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
		//System.out.println("in MStudioPlugin construct function");
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		//System.out.println("in MStudioPlugin --start-- function");
		super.start(context);
		plugin = this;
		listener = new MStudioResourceChangeListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener, 
				IResourceChangeEvent.POST_CHANGE);
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
			return fname.toLowerCase().endsWith("_res.cfg");
		}
	}

	public void updateProject(String projectName) throws CoreException {
		MStudioSocketServerThread serverSocket = MStudioSocketServerThread.get();
		if (serverSocket != null) {
			//please waiting for closing process
			serverSocket.closeProject(projectName);
		}
		
		// rename the file <project_name>_res.cfg .	
		IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		
		if (prj.hasNature(MStudioProjectNature.MSTUDIO_NATURE_ID))
		{
			String new_res_cfg = new MStudioProject(prj).getProgramCfgFile();
			if (null == new_res_cfg){
				return;
			}
			File newFile = new File (new_res_cfg);
			if (newFile.exists()){
				return;
			}
			IPath prjPath = new Path (new_res_cfg).removeLastSegments(1);
			if (null == prjPath || !prjPath.toFile().exists()){
				return;
			}
			String ofs[] = prjPath.toFile().list(new resCfgFilter());
			if (ofs != null && ofs.length > 0){
				File of =  prjPath.append(ofs[0]).toFile();
				if (of.exists()) {
					of.renameTo(newFile);
				}
			}
		}
		
	}

	public class MStudioResourceChangeListener implements IResourceChangeListener {
		
		IResourceDeltaVisitor visitor = null;
		
		public MStudioResourceChangeListener() {
			
			visitor = new IResourceDeltaVisitor() {
				
				public boolean visit(IResourceDelta delta) {
	
					IResource resource = delta.getResource();
					
					if (delta.getKind() == IResourceDelta.CHANGED
							&& resource.getType() == IResource.PROJECT) {
						try {
							System.out.println("MStudioResourceChangeListener : visit resource.Name = " + resource.getName());
							updateProject(resource.getName());
						} catch (CoreException e) {
							e.printStackTrace();
						}
					}
					
					return true;
				}
			};
		}

		public void resourceChanged(IResourceChangeEvent event) {
			IResourceDelta delta = event.getDelta();
			if (delta == null) {
				return;
			}

			try {
				delta.accept(visitor);
			} catch (CoreException e) { }
		}
	}

	public MStudioEnvInfo getMStudioEnvInfo () {
		return MStudioEnvInfo.getInstance();
	}
}

