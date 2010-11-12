package org.eclipse.cdt.fmsoft.hybridos.mstudio.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


public class MStudioDeployPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Label tipText;
	private DirectoryFieldEditor locationPath = null;
	private List<Button> btnList = new ArrayList<Button> ();; 
	private List<String> allServList = new ArrayList<String>();
	private List<String> selServList = new ArrayList<String>(); 
	
	private final String STORE_SERV_SPLIT = "\t";
	
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
		
		MStudioEnvInfo envInfo = MStudioPlugin.getDefault().getMStudioEnvInfo();
		allServList = envInfo.getServices();
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
		
		String storeServ = store.getString(MStudioPreferenceConstants.MSTUDIO_DEFAULT_SERVICES);
		selServList = Arrays.asList(storeServ.split(STORE_SERV_SPLIT));
   }
   
   private void saveToStoreData () {
		IPreferenceStore store = MStudioPlugin.getDefault().getPreferenceStore();
		
		String locationToStore = getChangedDeployLocation();
		if (locationToStore != null) {
			store.setValue(MStudioPreferenceConstants.MSTUDIO_DEPLOY_LOCATION, locationToStore);
		}
		
		String servToStore = new String(); 
		for (Iterator<String> it = selServList.iterator(); it.hasNext(); ){
			servToStore += it.next() + STORE_SERV_SPLIT;
		}
		store.setValue(MStudioPreferenceConstants.MSTUDIO_DEFAULT_SERVICES, servToStore);
   }
	
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		new Label (composite, SWT.NULL);
		
		Composite lc = new Composite (composite, SWT.NULL);
		lc.setLayout(new GridLayout());
		lc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createLocationPathContent (lc);
		tipText = createTipMsgContent (lc);
		GridData gd1 = new GridData(GridData.FILL_HORIZONTAL);
		gd1.horizontalSpan = 3;
		tipText.setLayoutData(gd1);
		
		final Label seperator = new Label (composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		seperator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		new Label (composite, SWT.NULL);
		
		final Label servCap = new Label (composite, SWT.NONE);
		servCap.setText("Select Deploy Services :");
		
		Composite sc = new Composite (composite, SWT.NULL);
		sc.setLayout(new GridLayout());
		sc.setLayoutData(new GridData(GridData.FILL_BOTH));
		createServicesContent(sc);
		
		final Button checkSelAll = new Button (composite, SWT.CHECK);
		checkSelAll.setText("Select All");
		
      initializeByStoreData ();
		
      return composite;
	}
	
	private Label createTipMsgContent(Composite parent) {
		Label t = new Label(parent, SWT.NULL);
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
	
	private Control createServicesContent (Composite parent) {
		Composite com = new Composite (parent, SWT.BORDER);
		com.setLayout(new GridLayout());
		com.setLayoutData(new GridData(GridData.FILL_BOTH));
		Color c = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
		com.setBackground(c);
		
		if (allServList != null){
			btnList.clear();
			for (Iterator<String> it = allServList.iterator(); it.hasNext(); ){
				String serv = it.next();
				Button btn = new Button (com, SWT.CHECK);
				btn.setText(serv);
				btn.setBackground(c);
				btnList.add(btn);
			}
		}
		com.layout(true);
		return com;
	}
	
	public boolean performOk()
	{
		saveToStoreData();
		return true;
	}
	
}
