package org.eclipse.cdt.fmsoft.hybridos.mstudio.properties;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

public class MStudioDeployPropertyPage extends PropertyPage implements
		IWorkbenchPropertyPage {

	public MStudioDeployPropertyPage() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Control createContents(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);
		//TODO: create controls
		
		loadPersistentSettings();
		return composite;
	}

	private void loadPersistentSettings() {
		MStudioProject mStudioProject = new MStudioProject((IProject)getElement());
		boolean defDeployable = mStudioProject.getDefaultDeployable();
		//TODO set control default selection type
	}
	
	private boolean savePersistentSettings() {
		 MStudioProject mStudioProject = new MStudioProject((IProject)getElement());
		 //TODO
		 return mStudioProject.setDefaultDeployable(false);
	}
	
	public boolean performOk() {
		return savePersistentSettings();
	}

}
