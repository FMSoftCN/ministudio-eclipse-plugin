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

package org.eclipse.cdt.fmsoft.hybridos.mstudio.properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import org.eclipse.core.resources.IProject;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProject;


public class MStudioDeployPropertyPage extends PropertyPage
	implements IWorkbenchPropertyPage {

	private final static String MSDPP_BUTTON_TEXT = "Deploy this project to rootfs";
	private final static String MSDPP_BUTTON_TOOL_TIP_TEXT = "Deploy this project to rootfs";

	private Button deployToRootfs = null;

	public MStudioDeployPropertyPage() {
	}

	@Override
	protected Control createContents(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());

		GridData data = new GridData(GridData.FILL);
		composite.setLayoutData(data);

		//Init the button
		deployToRootfs = new Button(composite, SWT.CHECK);
		deployToRootfs.setText(MSDPP_BUTTON_TEXT);
		deployToRootfs.setToolTipText(MSDPP_BUTTON_TOOL_TIP_TEXT);

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

