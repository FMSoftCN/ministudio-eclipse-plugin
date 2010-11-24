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

package org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards;

import java.io.File;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.core.runtime.Path;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;


public class MStudioVersionWizardPage extends WizardPage {

	public final static int MSTUDIO_PATH_TYPE_BIN = 0;
	public final static int MSTUDIO_PATH_TYPE_INCLUDE = MSTUDIO_PATH_TYPE_BIN + 1;

	Text versionName = null;
	DirectoryFieldEditor binPath = null;

	public MStudioVersionWizardPage(String pageName) {
		super(pageName);
		setTitle(MStudioMessages.getString("MStudioVersionWizardPage.title"));
		setDescription(MStudioMessages.getString("MStudioVersionWizardPage.desc"));
	}

	public void createControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout gl = new GridLayout();
		gl.numColumns = 3;
		composite.setLayout(gl);

		new Label(composite, SWT.NULL);
		new Label(composite, SWT.NULL);
		new Label(composite, SWT.NULL);
		new Label(composite, SWT.NULL);
		new Label(composite, SWT.NULL);
		new Label(composite, SWT.NULL);

		Label versionNameLabel = new Label(composite, SWT.NULL);
		versionNameLabel.setText(
				MStudioMessages.getString("MStudioVersionWizardPage.versionNameLabel"));
		versionName = new Text(composite, SWT.BORDER | SWT.SINGLE);
		versionName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		GridData versionNameGridData = new GridData(GridData.FILL_HORIZONTAL);
		versionName.setLayoutData(versionNameGridData);

		new Label(composite, SWT.NULL);
		new Label(composite, SWT.NULL);
		new Label(composite, SWT.NULL);
		new Label(composite, SWT.NULL);
		new Label(composite, SWT.NULL);
		new Label(composite, SWT.NULL);
		new Label(composite, SWT.NULL);

		binPath = new DirectoryFieldEditor("bin", "Binary Path:", composite);
		binPath.getTextControl(composite).addModifyListener(
				new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						dialogChanged();
					}
				});

		new Label(composite, SWT.NULL);
		Label binPathDescription = new Label(composite, SWT.NULL);
		binPathDescription.setText(
				MStudioMessages.getString("MStudioVersionWizardPage.binPathDescription"));

		new Label(composite, SWT.NULL);
		new Label(composite, SWT.NULL);
		new Label(composite, SWT.NULL);
		new Label(composite, SWT.NULL);

		setControl(composite);
		setPageComplete(isValid());
	}

	private boolean isVersionNameValid() {
		return versionName.getText().length() > 0;
	}

	private boolean isBinPathValid() {
		Path subElementPath = new Path(binPath.getStringValue());
		String osname = System.getProperty("os.name").toLowerCase();
		StringBuffer cmd = new StringBuffer("guibuilder");

		if (osname.indexOf("window") >= 0) {
			//for linux: (osname.indexOf("nix") >= 0 || osname.indexOf("nux")>=0 )
			cmd.append(".exe");
		}

		String subElementOSString = subElementPath.append(cmd.toString()).toOSString();

		File subElementFile = new File(subElementOSString);
		return subElementFile.exists();
	}

	private boolean isValid() {
		return (isVersionNameValid() && isBinPathValid());
	}

	public void dialogChanged() {
		boolean isValid = isValid();

		if (!isValid) {
			String errorMessage = "";
			if (!isVersionNameValid())
				errorMessage += MStudioMessages.getString("MStudioVersionWizardPage.messagesEmpty");
			if (!isBinPathValid())
				errorMessage += MStudioMessages.getString("MStudioVersionWizardPage.messagesInvalid");
			setErrorMessage(errorMessage);
		} else {
			setErrorMessage(null);
		}

		setPageComplete(isValid);
	}

	public String getVersionName() {
		return versionName.getText();
	}

	public void setVersionName(String versionName) {
		this.versionName.setText(versionName);
		dialogChanged();
	}

	public String getBinPath() {
		return binPath.getStringValue();
	}

	public void setBinPath(String binPath) {
		this.binPath.setStringValue(binPath);
		dialogChanged();
	}
}

