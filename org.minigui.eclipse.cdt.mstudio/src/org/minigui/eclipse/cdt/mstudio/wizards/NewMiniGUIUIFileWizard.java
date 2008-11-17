/**
 * 
 */
package org.minigui.eclipse.cdt.mstudio.wizards;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * @author xwyan
 *
 */
public class NewMiniGUIUIFileWizard extends Wizard implements INewWizard {
	private NewMiniGUIUIFileWizardPage page;
	private ISelection selection;

	public NewMiniGUIUIFileWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	public void addPages() {
		page = new NewMiniGUIUIFileWizardPage(selection);
		addPage(page);
	}
	
	public boolean performFinish() {
		final String uiContainerName = page.getUiContainerName();
		final String uiFileName = page.getUiFileName();
		final String srcContainerName = page.getSrcContainerName();
		final String srcFileName = page.getSrcFileName();
		final String hdrContainerName = page.getHdrContainerName();
		final String hdrFileName = page.getHdrFileName();
		
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(uiContainerName, uiFileName, srcContainerName, srcFileName, 
											hdrContainerName, hdrFileName, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};

		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}

	private void doFinish(String uiContainerName, String uiFileName, String srcContainerName, String srcFileName,	
			String hdrContainerName, String hdrFileName,	IProgressMonitor monitor) throws CoreException {
		// create the ui file
		monitor.beginTask("Creating " + uiFileName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		
		IResource uiRes = root.findMember(new Path(uiContainerName));
		if (!uiRes.exists() || !(uiRes instanceof IContainer)) {
			throwCoreException("Container \"" + uiContainerName + "\" does not exist.");
		}
		IContainer uiCon = (IContainer) uiRes;
		if(!uiCon.exists()){
			try {
				((IFolder)uiCon).create(true, true, new NullProgressMonitor());
			} catch (CoreException e){ }		
		}
		final IFile uiFile = uiCon.getFile(new Path(uiFileName));
   if (!uiFile.exists())
       uiFile.create(null, true, monitor);

   //create the c file
		IResource srcRes = root.findMember(new Path(srcContainerName));
		if (!srcRes.exists() || !(srcRes instanceof IContainer)) {
			throwCoreException("Container \"" + srcContainerName + "\" does not exist.");
		}
		IContainer srcCon = (IContainer) srcRes;		
		if(!srcCon.exists()){
			try {
				((IFolder)srcCon).create(true, true, new NullProgressMonitor());
			} catch (CoreException e){ }		
		}
		final IFile srcFile = srcCon.getFile(new Path(srcFileName));
		if (!srcFile.exists())
				srcFile.create(null, true, monitor);
		   
		//create the h file for resource id ...
		IResource hdrRes = root.findMember(new Path(hdrContainerName));
		if (!hdrRes.exists() || !(hdrRes instanceof IContainer)) {
			throwCoreException("Container \"" + hdrContainerName + "\" does not exist.");
		}
		IContainer hdrCon = (IContainer) hdrRes;
		if(!hdrCon.exists()){
				try {
					((IFolder)hdrCon).create(true, true, new NullProgressMonitor());
				} catch (CoreException e){ }		
		}
		final IFile hdrFile = hdrCon.getFile(new Path(hdrFileName));
		if (!hdrFile.exists())
				hdrFile.create(null, true, monitor);
		
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page =
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                try {
                    IDE.openEditor(page, uiFile, true);
                } catch (PartInitException e) {
                }
			}
		});
		monitor.worked(1);
	}
	
	private void throwCoreException(String message) throws CoreException {
		IStatus status =
			new Status(IStatus.ERROR, "org.minigui.eclipse.cdt.mstudio", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}
