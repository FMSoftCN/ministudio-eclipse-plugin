package org.minigui.eclipse.cdt.mstudio.wizards;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.minigui.eclipse.cdt.mstudio.MStudioPlugin;
import org.minigui.eclipse.cdt.mstudio.MiniGUIMessages;

public class MStudioDeployWizard extends Wizard implements IExportWizard {
	
    private IStructuredSelection selection;
    private MStudioDeployWizardPage mainPage;
	private static final String DIALOG_SETTINGS_SECTION = "MStudioDeployWizard";
	
	public MStudioDeployWizard() {
		// TODO Auto-generated constructor stub
		IDialogSettings mstudioSettings = MStudioPlugin.getDefault().getDialogSettings();
		IDialogSettings section = mstudioSettings.getSection(DIALOG_SETTINGS_SECTION);
		if (section == null) {
			section = mstudioSettings.addNewSection(DIALOG_SETTINGS_SECTION);
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
		
		try {
			URL imgUrl = new URL(MStudioPlugin.getDefault().getBundle().getEntry("/"), "icons/mgproject.gif");
			setDefaultPageImageDescriptor(ImageDescriptor.createFromURL(imgUrl));
		} catch (MalformedURLException e) {	}
		
		setNeedsProgressMonitor(true);
	}
}
