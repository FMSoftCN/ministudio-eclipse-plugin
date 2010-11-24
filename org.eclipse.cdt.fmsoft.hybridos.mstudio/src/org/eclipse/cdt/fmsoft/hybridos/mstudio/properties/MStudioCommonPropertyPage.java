/*********************************************************************
 * Copyright (C) 2005 - 2010, Beijing FMSoft Technology Co., Ltd.
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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.resources.IProject;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProject;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.preferences.MStudioToolsPreferencePage;


public class MStudioCommonPropertyPage extends PropertyPage {

	private final static String MSCPP_DEFAULT = "<Default>";

	private int oldVersionIndex = 0;
	private Combo versioncombo = null;

	public MStudioCommonPropertyPage() {
		super();
	}

	protected Control createContents(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);

		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		addControls(composite);
		loadPersistentSettings();

		return composite;
	}

	private void addControls(Composite parent) {

		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		composite.setLayoutData(data);

		Label label = new Label(composite, SWT.NONE);
		label.setText(MStudioMessages.getString("MStudioCommonPropertyPage.version"));
		versioncombo = new Combo(composite, SWT.READ_ONLY);

		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		versioncombo.setLayoutData(data);
	}

	private void loadPersistentSettings() {

		versioncombo.add(MSCPP_DEFAULT);

		String[] versions = MStudioToolsPreferencePage.getMStudioVersions();
		MStudioProject mStudioProject = new MStudioProject((IProject)getElement());
		String currentVersion = mStudioProject.getMStudioVersion();

		versioncombo.select(0);
		if (versions != null) {
			for (int i = 0; i < versions.length; ++i) {
				versioncombo.add(versions[i]);
				 if (versions[i].equals(currentVersion)) {
						versioncombo.select(i + 1);
				 }
			}
		}
		setOldVersionToSelectedVersion();
	}

	private boolean savePersistentSettings() {
		 MStudioProject mStudioProject = new MStudioProject((IProject)getElement());
		 return mStudioProject.setMStudioVersion(versioncombo.getItem(versioncombo.getSelectionIndex()));
	}

	private void setOldVersionToSelectedVersion() {
		oldVersionIndex = versioncombo.getSelectionIndex();
	}

	private boolean versionHasChanged() {
		return (versioncombo.getSelectionIndex() != oldVersionIndex);
	}

	protected void performDefaults() {
		versioncombo.select(0);
	}

	public boolean performOk() {

		if (savePersistentSettings()) {
			if (versionHasChanged()) {
				if (!requestFullBuild())
					return false;
				setOldVersionToSelectedVersion();
			}
		} else {
			return false;
		}

		return true;
	}

	private boolean requestFullBuild() {

		boolean accepted = false;
		Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		MessageDialog dialog = new MessageDialog(shell,
				MStudioMessages.getString("MStudioCommonPropertyPage.dialogTitle"),
				null,
				MStudioMessages.getString("MStudioCommonPropertyPage.dialogMessages"),
				MessageDialog.QUESTION,
				new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL},
				2);

		switch (dialog.open()) {
			case 2:
				accepted = false;
				break;

			case 0:
				(new MStudioProject((IProject)getElement())).scheduleRebuild();
				accepted = true;
				break;

			case 1:
				accepted = true;
				break;
		}

		return accepted;
	}
}

