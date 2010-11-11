package org.eclipse.cdt.fmsoft.hybridos.mstudio.preferences;

import java.io.File;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


public class MStudioDeployPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private DirectoryFieldEditor locationPath = null;
	private Label tipText = null;
	
	public MStudioDeployPreferencePage() {
	}

	public MStudioDeployPreferencePage(String title) {
		super(title);
	}

	public MStudioDeployPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	public void init(IWorkbench workbench) {
		setTitle (MStudioMessages.getString("MStudioDeployPreference.title"));
	}
	
	private String getChangedDeployLocation () {
		String l = locationPath.getStringValue();
	   File file = new File(l);
	   if (!file.exists()){
		   return null;
	   }
	   return l;
	}
     
   public void locationChanged() {
	   String l = locationPath.getStringValue();
	   File file = new File(l);
	   if (!file.exists()) {
		   updateTipMessage("Deploy Location Path is invalid ...");
	   } else {
		   updateTipMessage("");
	   }
 	}
   
   private void initializeByStoreData () {
	   IPreferenceStore store = MStudioPlugin.getDefault().getPreferenceStore();
		
		if (!store.contains(MStudioPreferenceConstants.MSTUDIO_DEPLOY_LOCATION))
			return;
		
		locationPath.setStringValue(store.getString(MStudioPreferenceConstants.MSTUDIO_DEPLOY_LOCATION));
   }
   
   private void saveToStoreData () {
		IPreferenceStore store = MStudioPlugin.getDefault().getPreferenceStore();
		
		String defaultLocation = getChangedDeployLocation();
		if (defaultLocation != null) {
			store.setValue(MStudioPreferenceConstants.MSTUDIO_DEPLOY_LOCATION, defaultLocation);
		}
		//TODO, save the services ....
   }
	
	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		tipText = createTipMsgContent (composite);
			
		GridLayout layout = new GridLayout();
      layout.numColumns = 3;
      composite.setLayout(layout);
      composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      createLocationPathContent (composite);
      
      initializeByStoreData ();
		
      return composite;
	}
	
	private Label createTipMsgContent(Composite parent) {
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 3;
		Label t = new Label(parent, SWT.NULL);
		t.setLayoutData(gridData);
		return  t;
	}
	
	protected void updateTipMessage (String tip) {
		Color c = Display.getCurrent() .getSystemColor(SWT.COLOR_RED);
		tipText.setForeground(c);
		tipText.setText(tip);
	}

	private DirectoryFieldEditor createLocationPathContent (Composite parent) {
		locationPath = new DirectoryFieldEditor("", "Deploy Location :", parent);
      locationPath.getTextControl(parent).addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				locationChanged();
				}});
      return locationPath;
	}
	
	public boolean performOk()
	{
		saveToStoreData();
		return true;
	}
	
}
