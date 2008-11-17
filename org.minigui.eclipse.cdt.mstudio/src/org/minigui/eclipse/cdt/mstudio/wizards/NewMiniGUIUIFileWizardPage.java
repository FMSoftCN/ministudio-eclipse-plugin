package org.minigui.eclipse.cdt.mstudio.wizards;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


public class NewMiniGUIUIFileWizardPage extends WizardPage {

	private Text fileText;
	
	private String res_con;
	private String src_con;
	private String hdr_con;
	private String cFileName;
	
	private ISelection selection;

	protected NewMiniGUIUIFileWizardPage(ISelection selection) {
		super("MiniGUIUIWizardPage");
		setTitle("Create a MiniGUI UI File");
		this.selection = selection;
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;	
		Label label = new Label(container, SWT.NULL);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);

		label = new Label(container, SWT.NULL);
		label.setText("&File name:");

		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fileText.setLayoutData(gd);
		fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		initialize();
		dialogChanged();
		setControl(container);
	}

	private void initialize() {
		if (selection != null && selection.isEmpty() == false
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1)
				return;
			Object obj = ssel.getFirstElement();
			if (!(obj instanceof IResource) && (obj instanceof IAdaptable))
				obj = ((IAdaptable) obj).getAdapter(IResource.class);
			
			if (obj instanceof IResource) {
				IProject project;
				if (obj instanceof IProject)
					project = (IProject) obj;
				else
					project = ((IResource) obj).getProject();
				
				//res folder
				IFolder uiFolder = project.getFolder("res");
				
				if (!uiFolder.exists()){
					try {
					uiFolder.create(true, true, new NullProgressMonitor());
					} catch (CoreException e){ }					
				}
				
				res_con = uiFolder.getFullPath().makeRelative().toString();
				
				// src folder
				IFolder srcFolder = project.getFolder("src");
				
				if (!srcFolder.exists()){
					try {
					srcFolder.create(true, true, new NullProgressMonitor());
					} catch (CoreException e){ }					
				}
				
				src_con = srcFolder.getFullPath().makeRelative().toString();
				
				//hdr folder
				IFolder hdrFolder = project.getFolder("header");
				
				if (!hdrFolder.exists()){
					try {
					hdrFolder.create(true, true, new NullProgressMonitor());
					} catch (CoreException e){ }					
				}
				hdr_con = hdrFolder.getFullPath().makeRelative().toString();	
			}
		}
		fileText.setFocus();
	}

	private void dialogChanged() {
		IResource container = ResourcesPlugin.getWorkspace().getRoot()
				.findMember(new Path(res_con));
		IResource src_container = ResourcesPlugin.getWorkspace().getRoot()
				.findMember(new Path(src_con));
		
		String fileName = getUiFileName();

		if (fileName.length() == 0) {
			updateStatus("File name must be specified");
			return;
		}

		if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus("File name must be valid");
			return;
		}

		if (container != null && container.exists()){
			final IFile file = ((IContainer) container).getFile(new Path(fileName));
			updateStatus(file.toString());
			if (file.exists()) {
				updateStatus("File already exists");
				return;
			}		
		}

		if(src_container != null && src_container.exists()){
			int dotL = fileName.lastIndexOf('.');
			cFileName = fileName.substring(0,dotL)+".c";
			final IFile c_file = ((IContainer) src_container).getFile(new Path(cFileName));
			if (c_file.exists()) {
				updateStatus("File already exists");
				return;
			}
		}

		String ext = "";
		int dotLoc = fileName.lastIndexOf('.');
		if (dotLoc != -1) {
			ext = fileName.substring(dotLoc + 1);
		}
		if (ext.equalsIgnoreCase("mui") == false) {
			updateStatus("File extension must be \"mui\"");
			return;
		}

		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getUiContainerName() {
		return res_con;
	}

	public String getUiFileName() {
		return fileText.getText();
	}
	
	public String getSrcContainerName() {
		return src_con;
	}

	public String getSrcFileName() {
		return cFileName;
	}

	public String getHdrContainerName() {
		return hdr_con;
	}

	public String getHdrFileName() {
		return "mrc_id.h";
	}
	
}
