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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.runtime.CoreException;

public class MStudioNewCCAppWizard extends MStudioNewCAppWizard {
	
	public boolean performFinish() {
		if (newProject != null){
			try {
				CCorePlugin.getDefault().convertProjectFromCtoCC(newProject, null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return super.performFinish();
	}
	
}