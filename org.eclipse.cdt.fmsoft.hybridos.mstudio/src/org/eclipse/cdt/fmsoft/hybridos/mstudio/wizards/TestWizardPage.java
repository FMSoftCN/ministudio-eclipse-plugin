package org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class TestWizardPage extends WizardPage {

	protected TestWizardPage(String pageName) {
		super(pageName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
		parent.setLayout(new GridLayout());
		parent.setLayoutData(new GridData());
		Label text=new Label(parent,SWT.NONE);
		text.setText("test");
	}

}
