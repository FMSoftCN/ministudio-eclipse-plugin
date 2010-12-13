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

import java.net.URI;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProjectNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class MStudioNewCCAppWizard extends MStudioNewCAppWizard {
	
	public MStudioNewCCAppWizard (){
		
	}
	
	public IProject createIProject(final String name, final URI location) throws CoreException {

		//System.out.println("Now convert Project From C to C++");
		super.createIProject(name, location);
		if (newProject != null){
			try {
				CCorePlugin.getDefault().convertProjectFromCtoCC(newProject, null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return newProject;
	}

	public String[] getNatures() {
		return new String[] {CCProjectNature.CC_NATURE_ID, CProjectNature.C_NATURE_ID, MStudioProjectNature.MSTUDIO_NATURE_ID};
	}
}