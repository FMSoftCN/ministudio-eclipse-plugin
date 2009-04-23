/**
 * 
 */
package org.minigui.eclipse.cdt.mstudio.wizards;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
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
public class NewRdrResWizard extends Wizard implements INewWizard {
	private NewRdrResWizardPage page;
	private ISelection selection;

	public NewRdrResWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	public void addPages() {
		page = new NewRdrResWizardPage(selection);
		addPage(page);
	}
	
	public boolean performFinish() {
		final String uiFileName = page.getFileName();
		final IProject project = page.getSelectProject();
	
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(project, uiFileName, monitor);
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

	private void doFinish(IProject project, String uiFileName,	IProgressMonitor monitor) throws CoreException {
		// create the renderer res folder and file ...
		monitor.beginTask("Creating " + uiFileName, 2);		
		IFolder uiFolder = project.getFolder("res/rdr");
		if(!uiFolder.exists()){
			try {
				uiFolder.create(true, true, new NullProgressMonitor());
			} catch (CoreException e){ }		
		}
		final IFile uiFile = uiFolder.getFile(new Path(uiFileName));
   if (!uiFile.exists())
       uiFile.create(null, true, monitor);

		
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

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}
