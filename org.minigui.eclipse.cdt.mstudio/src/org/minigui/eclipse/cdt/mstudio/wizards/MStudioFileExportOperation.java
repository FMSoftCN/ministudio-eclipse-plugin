package org.minigui.eclipse.cdt.mstudio.wizards;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

public class MStudioFileExportOperation implements IRunnableWithProgress {

	private IPath path;
	private IProgressMonitor monitor;
	private List resourcesToExport;
	private IOverwriteQuery overwriteCallback;
	private IResource resource;
	private List errorTable = new ArrayList(1);
	
	private static final int DEFAULT_BUFFER_SIZE = 16*1024;   

	private static final int OVERWRITE_NOT_SET = 0;
    private static final int OVERWRITE_NONE = 1;
    private static final int OVERWRITE_ALL = 2;
	private int overwriteState = OVERWRITE_NOT_SET;
	private boolean createLeadupStructure = true;
	private boolean createContainerDirectories = true;
	
	public MStudioFileExportOperation(IResource res, String destinationPath, 
			IOverwriteQuery overwriteImplementor) {
		super();
		resource = res;
		path = new Path(destinationPath);
		overwriteCallback = overwriteImplementor;
	}
	
	public MStudioFileExportOperation(IResource res, List resources,
			String destinationPath, IOverwriteQuery overwriteImplementor) {
		this(res, destinationPath, overwriteImplementor);
		resourcesToExport = resources;
	}
	
	protected int countChildrenOf(IResource parentResource) throws CoreException {
		if (parentResource.getType() == IResource.FILE) {
			return 1;
		}
		
		int count = 0;
		
		if (parentResource.isAccessible()) {
			IResource[] children = ((IContainer) parentResource).members();
			for (int i = 0; i < children.length; i ++) {
				count += countChildrenOf(children[i]);
			}
		}
		
		return count;
	}
	
	protected int countSelectedResources() throws CoreException {
		int result = 0; 
		Iterator resources = resourcesToExport.iterator();
		
		while(resources.hasNext()) {
			result += countChildrenOf((IResource) resources.next());
		}
		
		return result;
	}
	
	protected void createLeadupDirectoriesFor(IResource childResource) {
		IPath resourcePath = childResource.getFullPath().removeLastSegments(1);
		
		for (int i = 0; i < resourcePath.segmentCount(); i++) {
			path = path.append(resourcePath.segment(i));
			createFolder(path);
		}
	}
	
	protected void exportAllResources() throws InterruptedException {
		if (resource.getType() == IResource.FILE) {
			exportFile((IFile) resource, path);
		} else {
			try {
				exportChildren(((IContainer) resource).members(), path);
			} catch (CoreException e) {
				errorTable.add(e.getStatus());
			}
		}
	}
	
	protected void exportChildren(IResource[] children, IPath currentPath)
			throws InterruptedException {
		for (int i = 0; i < children.length; i ++) {
			IResource child = children[i];
			if (!child.isAccessible()) {
				continue;
			}
			
			if (child.getType() == IResource.FILE) {
				exportFile((IFile) child, currentPath);
			} else {
				IPath destination = currentPath.append(child.getName());
				createFolder(destination);
				try {
					exportChildren(((IContainer) child).members(), destination);
				} catch (CoreException e) {
					errorTable.add(e.getStatus());
				}
			}
		}
	}
	
	protected void exportFile(IFile file, IPath location) 
			throws InterruptedException {
		IPath fullPath = location.append(file.getName());
		monitor.subTask(file.getFullPath().toString());
		String properPathString = fullPath.toOSString();
		File targetFile = new File(properPathString);

		if (targetFile.exists()) {
			if (!targetFile.canWrite()) {
				errorTable.add(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID,
						0, NLS.bind("cannot overwirte ", targetFile.getAbsolutePath()), null));
				monitor.worked(1); //MiniGUIMessages.DataTransfer_cannotOverwrite
				return;
			}
			
			if (overwriteState == OVERWRITE_NONE) {
				return;
			}
			
			if (overwriteState != OVERWRITE_ALL) {
				String overwriteAnswer = overwriteCallback.queryOverwrite(properPathString);	
				
				if (overwriteAnswer.equals(IOverwriteQuery.CANCEL)) {
					throw new InterruptedException();
				}
				
				if (overwriteAnswer.equals(IOverwriteQuery.NO)) {
					monitor.worked(1);
					return;
				}
				
				if (overwriteAnswer.equals(IOverwriteQuery.NO_ALL)) {
					monitor.worked(1);
					overwriteState = OVERWRITE_NONE;
					return;
				}
				
				if (overwriteAnswer.equals(IOverwriteQuery.ALL)) {
					overwriteState = OVERWRITE_ALL;
				}
			}
		}

		try {
			write(file, fullPath);
		} catch (IOException e) {
			errorTable.add(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, 
					NLS.bind("deploy error", fullPath, e.getMessage()), e));
		} catch (CoreException e) {
			errorTable.add(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, 
					NLS.bind("deploy error", fullPath, e.getMessage()), e));//MINIGUIMessages.DataTransfer_errorExporting
		}

		monitor.worked(1);
		ModalContext.checkCanceled(monitor);
	}

	protected void exportSpecifiedResources() throws InterruptedException {
		Iterator resources = resourcesToExport.iterator();
		IPath initPath = (IPath) path.clone();
		
		while(resources.hasNext()) {
			IResource currentResource = (IResource) resources.next();
			if (!currentResource.isAccessible()) {
				continue;
			}
			
			path = initPath;
			
			if (resource == null) {
				if (createLeadupStructure) {
					createLeadupDirectoriesFor(currentResource);
				}
			} else {
				IPath containersToCreate = currentResource.getFullPath()
						.removeFirstSegments(resource.getFullPath().segmentCount())
						.removeLastSegments(1);
				
				for (int i = 0; i < containersToCreate.segmentCount(); i ++) {
					path = path.append(containersToCreate.segment(i));
					createFolder(path);
				}
			}
			
			if (currentResource.getType() == IResource.FILE) {
				exportFile((IFile) currentResource, path);
			} else {
				if (createContainerDirectories) {
					path = path.append(currentResource.getName());
					createFolder(path);
				}
				
				try {
					exportChildren(((IContainer) currentResource).members(), path);
				} catch (CoreException e) {
					errorTable.add(e.getStatus());
				}
			}
		}
	}

	public IStatus getStatus() {
		IStatus[] errors = new IStatus[errorTable.size()];
		errorTable.toArray(errors);
		return new MultiStatus(PlatformUI.PLUGIN_ID, IStatus.OK, errors,
				"deploy problems", null);
	}
	
	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		// TODO Auto-generated method stub
		this.monitor = monitor;
		
		if (resource != null) {
			if (createLeadupStructure) {
				createLeadupDirectoriesFor(resource);
			}
			
			if (createContainerDirectories
					&& resource.getType() != IResource.FILE) {
				path = path.append(resource.getName());
				createFolder(path);
			}
		}
		
		try {
			int totalWork = IProgressMonitor.UNKNOWN;
			try {
				if (resourcesToExport == null) {
					totalWork = countChildrenOf(resource);
				} else {
					totalWork = countSelectedResources();
				}
			} catch (CoreException e) {
				errorTable.add(e.getStatus());
			}
			
			monitor.beginTask("export title", totalWork);
			if (resourcesToExport == null) {
				exportAllResources();
			} else {
				exportSpecifiedResources();
			}
		} finally {
			monitor.done();
		}
		
		
	}
	
	public void setCreatecontainerDirectories(boolean value) {
		createContainerDirectories = value;
	}
	
	public void setCreateLeadupStructure(boolean value) {
		createLeadupStructure = value;
	}

	public void setOverwriteFiles(boolean value) {
		if (value) {
			overwriteState = OVERWRITE_ALL;
		}
	}
	
	/*============file system operation ============*/
	public void createFolder(IPath destinationPath) {
		new File(destinationPath.toOSString()).mkdir();
	}
	
	public void write(IResource resource, IPath destinationPath)
		throws CoreException, IOException {
		if (resource.getType() == IResource.FILE) {
			writeFile((IFile) resource, destinationPath);
		} else {
			writeChildren((IContainer) resource, destinationPath);
		}
	}
	
	protected void writeFile(IFile file, IPath destinationPath)
		throws IOException, CoreException {
		OutputStream output = null;
		InputStream contentStream = null;
		
		try {
			contentStream = new BufferedInputStream(file.getContents(false));
			output = new BufferedOutputStream (
					new FileOutputStream(destinationPath.toOSString()));
			
			int available = contentStream.available();
			available = available <= 0? DEFAULT_BUFFER_SIZE : available;
			int chunkSize = Math.min(DEFAULT_BUFFER_SIZE, available);
			byte[] readBuffer = new byte[chunkSize];
			int n = contentStream.read(readBuffer);
			
			while ( n > 0 ) {
				output.write(readBuffer, 0, n);
				n = contentStream.read(readBuffer);
			}
		} finally {
			if (contentStream != null) {
				try {
					contentStream.close();
				} catch (IOException e) {
					IDEWorkbenchPlugin.log(
							"Error closing input stream for file: " + file.getLocation(), e);
				}
			}
			if (output != null) {
				output.close();
			}
		}
	}
	
	protected void writeResource(IResource resource, IPath destinationPath)
		throws CoreException, IOException {
		if (resource.getType() == IResource.FILE) {
			writeFile((IFile) resource, destinationPath);
		} else {
			createFolder(destinationPath);
			writeChildren((IContainer) resource, destinationPath);
		}
	}
	
	protected void writeChildren(IContainer folder, IPath destinationPath)
			throws CoreException, IOException {
		if (folder.isAccessible()) {
			IResource[] children = folder.members();
			for (int i = 0; i < children.length; i++) {
				IResource child = children[i];
				writeResource(child, destinationPath.append(child.getName()));
			}
		}
	}
	
}
