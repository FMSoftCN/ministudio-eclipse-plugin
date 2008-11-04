package org.minigui.eclipse.cdt.mstudio.wizards;

import java.io.File;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class MStudioVersionWizardPage extends WizardPage {

	public final static int MG_PATH_TYPE_BIN = 0;
	public final static int MG_PATH_TYPE_INCLUDE = MG_PATH_TYPE_BIN + 1;

	Text versionName;
	DirectoryFieldEditor binPath;

	public MStudioVersionWizardPage(String pageName) {
		super(pageName);
		setTitle("MStudio veriosn Select");
		setDescription("Specify the Name and Bin of the UI-builder.");
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);

		GridLayout gl = new GridLayout();
		gl.numColumns = 3;
		composite.setLayout(gl);
		
		new Label(composite, SWT.NULL);
		new Label(composite, SWT.NULL);
		new Label(composite, SWT.NULL);
		new Label(composite, SWT.NULL);
		new Label(composite, SWT.NULL);
		new Label(composite, SWT.NULL);
		
		Label versionNameLabel = new Label(composite, SWT.NULL);
		versionNameLabel.setText("Version Name:");
		versionName = new Text(composite, SWT.BORDER | SWT.SINGLE);
		versionName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		GridData versionNameGridData = new GridData(GridData.FILL_HORIZONTAL);
		versionName.setLayoutData(versionNameGridData);
		
		new Label(composite, SWT.NULL);
		new Label(composite, SWT.NULL);
		new Label(composite, SWT.NULL);
		new Label(composite, SWT.NULL);
		new Label(composite, SWT.NULL);
		new Label(composite, SWT.NULL);
		new Label(composite, SWT.NULL);

		binPath = new DirectoryFieldEditor("bin", "Bin Path:", composite);
		binPath.getTextControl(composite).addModifyListener(
				new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						dialogChanged();
					}
				});
		/*
		binPath.setPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {

			}
		});
		*/
		new Label(composite, SWT.NULL);
		Label binPathDescription = new Label(composite, SWT.NULL);
		binPathDescription.setText("Path containing tools 'UI-builder','ui2c',etc.\n");

		new Label(composite, SWT.NULL);
		new Label(composite, SWT.NULL);
		new Label(composite, SWT.NULL);
		new Label(composite, SWT.NULL);

		setControl(composite);
		setPageComplete(isValid());
	}

	private boolean isVersionNameValid() {
		return versionName.getText().length() > 0;
	}

	private boolean isBinPathValid() {
		//TODO , for ui-builder ,ui2c,...
		String subElement = "UI-builder";
		Path subElementPath = new Path(binPath.getStringValue());
		String subElementOSString = subElementPath.append(subElement)
				.toOSString();

		File subElementFile = new File(subElementOSString);
		return subElementFile.exists();
	}

	private boolean isValid() {
		return (isVersionNameValid() && isBinPathValid());
	}

	public void dialogChanged() {
		boolean isValid = isValid();

		if (!isValid) {
			String errorMessage = "";
			if (!isVersionNameValid())
				errorMessage += "Version Name is empty. ";
			if (!isBinPathValid())
				errorMessage += "Bin Path is invalid. ";
			setErrorMessage(errorMessage);
		} else {
			setErrorMessage(null);
		}

		setPageComplete(isValid);
	}

	public String getVersionName() {
		return versionName.getText();
	}

	public void setVersionName(String versionName) {
		this.versionName.setText(versionName);
		dialogChanged();
	}

	public String getBinPath() {
		return binPath.getStringValue();
	}

	public void setBinPath(String binPath) {
		this.binPath.setStringValue(binPath);
		dialogChanged();
	}

}
