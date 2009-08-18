package org.minigui.eclipse.cdt.mstudio.wizards;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.minigui.eclipse.cdt.mstudio.MiniGUIMessages;

public class MStudioDeployWizard extends Wizard implements IExportWizard {
	
    private IStructuredSelection selection;
    private MStudioDeployWizardPage mainPage;
	private static final String DIALOG_SETTINGS_SECTION = "MStudioDeployWizard";
	
	public MStudioDeployWizard() {
		// TODO Auto-generated constructor stub
		IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault().getDialogSettings();
		IDialogSettings section = workbenchSettings.getSection(DIALOG_SETTINGS_SECTION);
		if (section == null) {
			section = workbenchSettings.addNewSection(DIALOG_SETTINGS_SECTION);
		}
		setDialogSettings(section);
	}

	public void addPages() {
		super.addPages();
		mainPage = new MStudioDeployWizardPage(selection);
		addPage(mainPage);
	}

	
	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return mainPage.finish();
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub
		this.selection = selection;
		setWindowTitle(MiniGUIMessages.getString("MStudioDeployWizard.title"));
		setDefaultPageImageDescriptor(IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/exportdir_wiz.png"));
		setNeedsProgressMonitor(true);
	}

}
