package org.eclipse.cdt.fmsoft.hybridos.mstudio.properties;


import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

public class MStudioDeployPropertyPage extends PropertyPage implements
		IWorkbenchPropertyPage {
	
	private static String deployButtonText="Deploy this project to rootfs";
	private static String deployButtonToolTipText="Deploy this project to rootfs";
	private Button deployToRootfs;
	public MStudioDeployPropertyPage() {
		
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		
		GridData data = new GridData(GridData.FILL);
		composite.setLayoutData(data);
		//Init the button
		deployToRootfs = new Button(composite,SWT.CHECK);
		deployToRootfs.setText(deployButtonText);		
		deployToRootfs.setToolTipText(deployButtonToolTipText);
		loadPersistentSettings();
		return composite;
	}

	private void loadPersistentSettings() {
		MStudioProject mStudioProject = new MStudioProject((IProject)getElement());		
		deployToRootfs.setSelection(mStudioProject.getDefaultDeployable());		
	}
	
	private boolean savePersistentSettings() {
		 MStudioProject mStudioProject = new MStudioProject((IProject)getElement());
		 return mStudioProject.setDefaultDeployable(deployToRootfs.getSelection());
	}
	
	public boolean performOk() {
		return savePersistentSettings();
	}

}
