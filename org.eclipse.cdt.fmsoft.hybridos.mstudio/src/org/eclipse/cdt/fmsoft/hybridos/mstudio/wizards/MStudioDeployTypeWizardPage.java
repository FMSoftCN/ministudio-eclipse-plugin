package org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
	//the default values
	private MStudioDeployTargetType targetType = MStudioDeployTargetType.Target;
	private MStudioDeployBuildType buildType = MStudioDeployBuildType.Release;
	
	public String getTargetType() {
		return targetType.name();
	}
	
	public String getBuildType() {
		return buildType.name();
	}
	
	public MStudioDeployTypeWizardPage(String pageName) {
		super(pageName);
		init();
	}

	private void init() {
		setTitle(MStudioMessages.getString("MStudioDeployWizardPage.selectType.pageTitle"));
		setDescription(MStudioMessages.getString("MStudioDeployWizardPage.selectType.desc"));
	}

	public MStudioDeployTypeWizardPage(String pageName, String title,
			ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		init();
	}

	@Override
	public void createControl(Composite parent) {
		Composite topPanel;
		topPanel = new Composite(parent, SWT.NONE);
		topPanel.setLayout(new GridLayout());
		topPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
		Label typeTitle = new Label(topPanel,SWT.NONE);
		
		RadioGroupFieldEditor typeRadioGroup = new RadioGroupFieldEditor("deployType", "Select deploy type", 1,
			     			new String[][] {{MStudioDeployTargetType.Host.name(), MStudioDeployTargetType.Host.name()},
							{MStudioDeployTargetType.Target.name(), MStudioDeployTargetType.Target.name()}},topPanel);
		//set the default choice value
		PreferenceStore newTypePreferenceStore=new PreferenceStore();
		newTypePreferenceStore.setValue(getTargetType(), getTargetType());
		typeRadioGroup.setPreferenceStore(newTypePreferenceStore);
		typeRadioGroup.load();
		typeRadioGroup.setPropertyChangeListener(new IPropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent event) {
				if(!targetType.name().equals(event.getNewValue())){
					if(targetType == MStudioDeployTargetType.Target){
						targetType = MStudioDeployTargetType.Host;
						MStudioDeployWizard.deployTypeIsHost=true;
					}
					else
						targetType = MStudioDeployTargetType.Target;					
				}
			}});
		new Label(topPanel,SWT.FULL_SELECTION|SWT.LINE_SOLID);
		RadioGroupFieldEditor rootfsRadioGroup = new RadioGroupFieldEditor("rootfs","Select rootfs type",1,
							new String[][]{{MStudioDeployBuildType.Release.name(),MStudioDeployBuildType.Release.name()},
							{MStudioDeployBuildType.Debug.name(),MStudioDeployBuildType.Debug.name()}},topPanel);
		//set the default choice value
		PreferenceStore newPreferenceStore = new PreferenceStore();
		newPreferenceStore.setValue(getBuildType(), getBuildType());
		rootfsRadioGroup.setPreferenceStore(newPreferenceStore);
		rootfsRadioGroup.load();
		rootfsRadioGroup.setPropertyChangeListener(new IPropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent event) {
				if(!buildType.name().equals(event.getNewValue())){
					if(buildType == MStudioDeployBuildType.Release)
						buildType = MStudioDeployBuildType.Debug;
					else
						buildType = MStudioDeployBuildType.Release;					
				}	
			}			
		});
		
		setControl(topPanel);
		setPageComplete(true);
	}
	//ervery time init it or return to this page would be execute this function
	public IWizardPage getNextPage() {		
		MStudioDeployWizard wizard = (MStudioDeployWizard) this.getWizard();
        if (wizard == null) {
			return null;
		}
        wizard.getDeployExecuteableWizardPage().update();
        return wizard.getNextPage(this);
    }
}
