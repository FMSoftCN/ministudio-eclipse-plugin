package org.eclipse.cdt.fmsoft.hybridos.mstudio.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class MStudioSoCPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public MStudioSoCPreferencePage() {
		// TODO Auto-generated constructor stub
	}

	public MStudioSoCPreferencePage(String title) {
		super(title);
		// TODO Auto-generated constructor stub
	}

	public MStudioSoCPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		
		Label text = new Label(composite, SWT.WRAP);
		if (null != text){
			text.setFont(parent.getFont());
			text.setText("TODO, ...   for Soc Setting  ... ");
		}
		
		return composite;
	}

}
