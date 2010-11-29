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

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class MStudioDeployProjects extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		MStudioDeployWizard deployWizard = new MStudioDeployWizard();	
		IWorkbenchWindow window;
		
		try {
			window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		} catch(ExecutionException e) {
			e.printStackTrace();
			return null;
		}
		WizardDialog dialog = new WizardDialog(window.getShell(), deployWizard);	
		try{
			dialog.create();
		}catch(Exception e){
			MessageDialog.openError(window.getShell(), MStudioMessages.getString("MStudioDeployProject.error.title"), MStudioMessages.getString("MStudioDeployProject.error.content") + e.toString());
		}
		if (dialog.open() == WizardDialog.OK) {
		}				
		return null;
	}

}
