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

package org.eclipse.cdt.fmsoft.hybridos.mstudio.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;


public class MStudioProjectNature implements IProjectNature {

	public final static String MSTUDIO_NATURE_ID =
		"org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProjectNature";

	private IProject projectNature = null;

	public MStudioProjectNature() {
	}

	public MStudioProjectNature(IProject project) {
		setProject(project);
	}

	public void configure() throws CoreException {
	}

	public void deconfigure() throws CoreException {
	}

	public IProject getProject() {
		return projectNature;
	}

	public void setProject(IProject project) {
		projectNature = project;
	}
}

