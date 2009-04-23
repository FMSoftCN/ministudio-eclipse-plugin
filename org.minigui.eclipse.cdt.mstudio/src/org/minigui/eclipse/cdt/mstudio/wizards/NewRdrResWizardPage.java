package org.minigui.eclipse.cdt.mstudio.wizards;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import org.minigui.eclipse.cdt.mstudio.project.MgProjectNature;
import org.minigui.eclipse.cdt.mstudio.MStudioPlugin;

public class NewRdrResWizardPage extends WizardPage {

	private Text fileText;
	private Tree tree;	
	private String cFileName;
	private IProject selProject=null;
	private ISelection selection;
	private static URL imgURL;
	private static Image itemImage;
	
	static {
		try {
			imgURL= new URL(MStudioPlugin.getDefault().getBundle().getEntry("/"), "icons/mgproject.gif" );
			ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(imgURL); 
			itemImage = imageDescriptor.createImage();  
		} catch (MalformedURLException e) {	}
	}	

	protected NewRdrResWizardPage(ISelection selection) {
		super("MiniGUIUIWizardPage");
		setTitle("Create a MiniGUI UI File");
		this.selection = selection;
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		
		GridLayout layout = new GridLayout();		
		layout.numColumns = 3;
		layout.verticalSpacing = 9;	
		container.setLayout(layout);

		Label l1 = new Label(container, SWT.NULL);
		l1.setText("&File name:");

		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd1 = new GridData(GridData.FILL_HORIZONTAL);
		gd1.horizontalSpan = 2;
		fileText.setLayoutData(gd1);
		fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		Label l2 = new Label(container, SWT.NULL);
		GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
		gd2.horizontalSpan = 3;
		l2.setLayoutData(gd2);
		l2.setText("Please select a MiniGUI project for your UI File :");
		
		tree = new Tree(container, SWT.SINGLE | SWT.BORDER);
		GridData gd3 = new GridData(GridData.FILL_BOTH);
		gd3.horizontalSpan = 2;
		tree.setLayoutData(gd3);
		tree.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TreeItem[] tis = tree.getSelection();
				if (tis == null || tis.length == 0)
					return;
			
				selProject = (IProject)(tis[0].getData());
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
			IProject proj=null;
			if (obj instanceof IResource) {
				if (obj instanceof IProject)
					proj = (IProject) obj;
				else
					proj = ((IResource) obj).getProject();
			}
			try {
				if (proj != null && proj.hasNature(MgProjectNature.MG_NATURE_ID))
						selProject = proj;
			} catch (CoreException e) { }
		}
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final IProject[] projects = root.getProjects();
		try {
				for (int i = 0; i < projects.length; i++){
						IProject pro = projects[i];
						if (pro.hasNature(MgProjectNature.MG_NATURE_ID)){
								TreeItem ti = new TreeItem(tree, SWT.NONE);
								ti.setText(pro.getName());
								ti.setData(pro);
								ti.setImage(itemImage);
								if(selProject != null && pro.equals(selProject))
									tree.setSelection(ti);
						}
				}
		} catch (CoreException e) { }
		
		fileText.setFocus();
	}

	private void dialogChanged() {
	
		String fileName = getFileName();

		if (fileName.length() == 0) {
			updateStatus("File name must be specified");
			return;
		}
	
		if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus("File name must be valid");
			return;
		}
		
		IFolder uiFolder = selProject.getFolder("res");		
		if (uiFolder.exists()){
			IResource res_con = ResourcesPlugin.getWorkspace().getRoot().findMember
								(new Path(uiFolder.getFullPath().makeRelative().toString()));
			if (res_con != null && res_con.exists()){
				final IFile file = ((IContainer) res_con).getFile(new Path(fileName));
				updateStatus(file.toString());
				if (file.exists()) {
					updateStatus("File already exists");
					return;
				}		
			}
		}
	
		IFolder srcFolder = selProject.getFolder("src");		
		if (srcFolder.exists()){
			IResource src_con = ResourcesPlugin.getWorkspace().getRoot().findMember
								(new Path(srcFolder.getFullPath().makeRelative().toString()));
			if (src_con != null && src_con.exists()){
				int dotL = fileName.lastIndexOf('.');
				cFileName = fileName.substring(0,dotL)+".c";
				final IFile file = ((IContainer) src_con).getFile(new Path(cFileName));
				updateStatus(file.toString());
				if (file.exists()) {
					updateStatus("File already exists");
					return;
				}		
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

	public String getFileName() {
		return fileText.getText();
	}
	
	public IProject getSelectProject() {
		return selProject;
	}
	
}
