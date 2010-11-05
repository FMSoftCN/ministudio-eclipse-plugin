package org.eclipse.cdt.fmsoft.hybridos.mstudio.handlers;

import java.util.Map;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

public class MStudioMginitService extends AbstractHandler implements
		IElementUpdater {
	private static boolean isRunning = false;
	
	@Override
	public void updateElement(UIElement element, Map parameters) {
		// TODO Auto-generated method stub
		if (isRunning)
			element.setText(MStudioMessages.getString("MStudioMenu.mginit.stop.label"));
		else
			element.setText(MStudioMessages.getString("MStudioMenu.mginit.start.label"));
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		final String commandId = "org.eclipse.cdt.fmsoft.hybridos.mstudio.commands.mginitservice";
		
		isRunning = mginitHasRunning();
		commandService.refreshElements(commandId, null);
		
		if (isRunning) {
			stopMginit();
		}
		else {
			startMginit();
		}
		
		return null;
	}

	private void startMginit()
	{
		//TODO:
	}
	
	private void stopMginit()
	{
		//TODO:
	}
	
	private boolean mginitHasRunning()
	{
		//TODO:check mginit program running status
		
		return false;
	}
}
