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

package org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;


public class MStudioVersionWizard extends Wizard {

	String versionName = null;
	String binPath = null;

	MStudioVersionWizardPage wizardPage = null;

	public MStudioVersionWizard(String title) {
		setWindowTitle(title);
	}

	public boolean performFinish() {
		versionName = wizardPage.getVersionName();
		binPath = wizardPage.getBinPath();

		return true;
	}

	public void addPages() {
		wizardPage = new MStudioVersionWizardPage(
				MStudioMessages.getString("MStudioVersionWizardPage.name"));
		addPage(wizardPage);
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		wizardPage.setVersionName(versionName);
	}

	public String getBinPath() {
		return binPath;
	}

	public void setBinPath(String binPath) {
		wizardPage.setBinPath(binPath);
	}
}

