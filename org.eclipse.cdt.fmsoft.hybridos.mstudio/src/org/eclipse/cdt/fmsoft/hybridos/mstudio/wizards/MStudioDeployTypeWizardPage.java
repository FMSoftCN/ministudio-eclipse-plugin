package org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class MStudioDeployTypeWizardPage extends WizardPage {
	enum MStudioDeployTargetType {
        Host,
        Target
	};

	enum MStudioDeployBuildType {
        Debug,
        Release 
	};

	private MStudioDeployTargetType targetType = MStudioDeployTargetType.Target;
	private MStudioDeployBuildType buildType = MStudioDeployBuildType.Release;
	
	private Composite topPanel;
	
	public String getTargetType() {
		return targetType.name();
	}
	
	public String getBuildType() {
		return buildType.name();
	}
	
	public MStudioDeployTypeWizardPage(String pageName) {
		super(pageName);
		// TODO Auto-generated constructor stub
		setTitle(MStudioMessages.getString("MStudioDeployWizardPage.selectType.pageTitle"));
		setDescription(MStudioMessages.getString("MStudioDeployWizardPage.selectType.desc"));
	}

	public MStudioDeployTypeWizardPage(String pageName, String title,
			ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
		Composite topPanel;
		topPanel = new Composite(parent, SWT.NONE);
		topPanel.setLayout(new GridLayout());
		topPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		setControl(topPanel);
		setPageComplete(true);
	}

}
