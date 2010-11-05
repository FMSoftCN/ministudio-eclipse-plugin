package org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class MStudioDeployProjects extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		
		MStudioDeployWizard deployWizard = new MStudioDeployWizard();
		IWorkbenchWindow window;
		
		try {
			window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		} catch(ExecutionException e) {
			e.printStackTrace();
			return null;
		}
		
		WizardDialog dialog = new WizardDialog(window.getShell(), deployWizard);
		dialog.create();
		
		if (dialog.open() == WizardDialog.OK) {
			//TODO: deploy projects
		}
		return null;
	}

}
