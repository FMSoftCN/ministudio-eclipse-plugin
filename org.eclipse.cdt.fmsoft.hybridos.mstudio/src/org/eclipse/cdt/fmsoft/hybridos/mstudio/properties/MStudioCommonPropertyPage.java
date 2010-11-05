package org.eclipse.cdt.fmsoft.hybridos.mstudio.properties;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.preferences.MStudioToolsPreferencePage;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;


public class MStudioCommonPropertyPage extends PropertyPage {

	private Combo versioncombo;
	private int oldVersionIndex;

	public MStudioCommonPropertyPage() {
		super();
	}

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		addControls(composite);

		loadPersistentSettings();

		return composite;
	}

	private void addControls(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		composite.setLayoutData(data);

		Label label = new Label(composite, SWT.NONE);
		label.setText("Use MStudio Version: ");
		versioncombo = new Combo(composite, SWT.READ_ONLY);

		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		versioncombo.setLayoutData(data);

	}

	private void loadPersistentSettings() {

		versioncombo.add("<Default>");

		String[] versions = MStudioToolsPreferencePage.getMStudioVersions();
		MStudioProject mStudioProject = new MStudioProject((IProject)getElement());
		String currentVersion = mStudioProject.getMStudioVersion();

		versioncombo.select(0);
		if (versions != null) {
			for (int i = 0; i < versions.length; ++i) {
				versioncombo.add(versions[i]);
				 if (versions[i].equals(currentVersion)) {
					 	versioncombo.select(i + 1);
				 }
			}
		}
		setOldVersionToSelectedVersion();
	}

	private boolean savePersistentSettings() {
		 MStudioProject mStudioProject = new MStudioProject((IProject)getElement());
		 return mStudioProject.setMStudioVersion(versioncombo.getItem(versioncombo.getSelectionIndex()));
	}

	private void setOldVersionToSelectedVersion() {
		oldVersionIndex = versioncombo.getSelectionIndex();
	}

	private boolean versionHasChanged() {
		return (versioncombo.getSelectionIndex() != oldVersionIndex);
	}

	protected void performDefaults() {
		versioncombo.select(0);
	}

	public boolean performOk() {
		if (savePersistentSettings()) {
			if (versionHasChanged()) {
				if (!requestFullBuild())
					return false;
				setOldVersionToSelectedVersion();
			}
		} else {
			return false;
		}
		return true;
	}

	private boolean requestFullBuild() {
		boolean accepted = false;
		Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		MessageDialog dialog = new MessageDialog (shell, "MStudio Version Changed", null,
				"The project's MStudio version has changed. A rebuild of the project is required for changes to take effect. Do a full rebuild now?",
				MessageDialog.QUESTION, new String[] {
						IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL }, 2);
		switch (dialog.open()) {
		case 2:
			accepted = false;
			break;
		case 0:
			(new MStudioProject((IProject)getElement())).scheduleRebuild();
			accepted = true;
			break;
		case 1:
			accepted = true;
			break;
		}
		return accepted;
	}
	
}
