/*********************************************************************
 * Copyright (C) 2002 ~ 2010, Beijing FMSoft Technology Co., Ltd.
 * Room 902, Floor 9, Taixing, No.11, Huayuan East Road, Haidian
 * District, Beijing, P. R. CHINA 100191.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Beijing FMSoft Technology Co., Ltd. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance you entered into with FMSoft.
 *
 *			http://www.minigui.com
 *
 *********************************************************************/

package org.eclipse.cdt.fmsoft.hybridos.mstudio.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards.MStudioParserIniFile;
import org.eclipse.core.runtime.Platform;


public class MStudioDeployPreferencePage extends PreferencePage
	implements IWorkbenchPreferencePage {

	private final static String MSDPP_SPLIT = "\t";
	private final static String MSDPP_PATH_INVALID =  
		MStudioMessages.getString("MStudioDeployPreferencePage.pathTip");
	private final static String MSDPP_DPLY_LOCAL = 
		MStudioMessages.getString("MStudioDeployPreferencePage.deployLocation");
	private final static String MSDPP_GAL_ENGINE = 
		MStudioMessages.getString("MStudioDeployPreferencePage.galEngine");
	private final static String MSDPP_IAL_ENGINE = 
		MStudioMessages.getString("MStudioDeployPreferencePage.ialEngine");
	private final static String MSDPP_EMPTY_STR = "";

	private Label tipText = null;
	private DirectoryFieldEditor locationPath = null;
	private List<Button> btnList = new ArrayList<Button>();
	private List<String> allServList = new ArrayList<String>();
	private List<String> selServList = new ArrayList<String>();
	private Combo galCom = null;
	private Combo ialCom = null;
	private String selectedGalEngine;
	private String selectedIalEngine;

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
		setTitle(MStudioMessages.getString("MStudioDeployPreferencePage.title"));

		MStudioEnvInfo envInfo = MStudioPlugin.getDefault().getMStudioEnvInfo();
		allServList = envInfo.getServices();
	}

	private String getChangedDeployLocation() {
		String l = locationPath.getStringValue();
		File file = new File(l);
		if (!file.exists()) {
		   return null;
		}
		return l;
	}

	public void locationChanged() {

		String l = locationPath.getStringValue();
		File file = new File(l);

		if (!file.exists()) {
			updateTipMessage(MSDPP_PATH_INVALID);
			setValid(false);
		} else {
			updateTipMessage(MSDPP_EMPTY_STR);
			setValid(true);
		}
	}

	private void initializeByStoreData() {

		IPreferenceStore store = MStudioPlugin.getDefault().getPreferenceStore();
		if (!store.contains(MStudioPreferenceConstants.MSTUDIO_DEPLOY_LOCATION))
			return;

		locationPath.setStringValue(store.getString(MStudioPreferenceConstants.MSTUDIO_DEPLOY_LOCATION));

		String storeServ = store.getString(MStudioPreferenceConstants.MSTUDIO_DEFAULT_SERVICES);
		String[] defaultSelServ = storeServ.split(MSDPP_SPLIT);

		selServList.clear();

		for (Iterator<Button> it = btnList.iterator(); it.hasNext(); ) {
			boolean find = false;
			Button btn = it.next();

			for (int i = 0; i < defaultSelServ.length; i++) {
				if (defaultSelServ[i].equals(btn.getText())) {
					selServList.add(defaultSelServ[i]);
					find = true;
				}
			}

			btn.setSelection(find);
		}
		
		String metaDataPath = Platform.getInstanceLocation().getURL().getPath() + ".metadata/";
		String tagetMgconfigureFile = metaDataPath + "MiniGUI.cfg.target";
		MStudioParserIniFile file = new MStudioParserIniFile(tagetMgconfigureFile);
		if (file == null)
			return; 
		selectedGalEngine = file.getStringProperty("system", "gal_engine");
		if (selectedGalEngine == null)
			return;
		String[] galItems = galCom.getItems();
		for (int i = 0; i < galItems.length; i ++){
			if (selectedGalEngine.equals(galItems[i])){
				galCom.select(i);
				break;
			}
		}
		
		selectedIalEngine = file.getStringProperty("system", "ial_engine");
		if (selectedIalEngine == null)
			return;
		String[] ialItems = ialCom.getItems();
		for (int i = 0; i < ialItems.length; i ++){
			if (selectedIalEngine.equals(ialItems[i])){
				ialCom.select(i);
				break;
			}
		}
	}

	private void saveToStoreData() {

		IPreferenceStore store = MStudioPlugin.getDefault().getPreferenceStore();
		String locationToStore = getChangedDeployLocation();

		if (locationToStore != null) {
			store.setValue(MStudioPreferenceConstants.MSTUDIO_DEPLOY_LOCATION, locationToStore);
		}

		String servToStore = new String();

		for (Iterator<String> it = selServList.iterator(); it.hasNext(); ) {
			servToStore += it.next() + MSDPP_SPLIT;
		}

		store.setValue(MStudioPreferenceConstants.MSTUDIO_DEFAULT_SERVICES, servToStore);
		
		String metaDataPath = Platform.getInstanceLocation().getURL().getPath() + ".metadata/";
		String tagetMgconfigureFile = metaDataPath + "MiniGUI.cfg.target";
		MStudioParserIniFile file = new MStudioParserIniFile(tagetMgconfigureFile);
		if (file == null)
			return; 
		
		if (selectedGalEngine != null) {
			file.setStringProperty("system", "gal_engine", selectedGalEngine, null);
		}
		if (selectedIalEngine != null) {
			file.setStringProperty("system", "ial_engine", selectedIalEngine, null);
		}
		
		file.save();
	}

	protected Control createContents(Composite parent) {

		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		new Label(composite, SWT.NULL);

		Composite lc = new Composite(composite, SWT.NULL);
		lc.setLayout(new GridLayout());
		lc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createLocationPathContent(lc);
		tipText = createTipMsgContent(lc);

		GridData gd1 = new GridData(GridData.FILL_HORIZONTAL);
		gd1.horizontalSpan = 3;
		tipText.setLayoutData(gd1);
		
		final Label seperator0 = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		seperator0.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		createEnginesContent(composite);
		
		final Label seperator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		seperator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Label servCap = new Label(composite, SWT.NONE);
		servCap.setText(MStudioMessages.getString("MStudioDeployPreferencePage.servCap"));

		Composite sc = new Composite(composite, SWT.NULL);
		sc.setLayout(new GridLayout());
		sc.setLayoutData(new GridData(GridData.FILL_BOTH));
		createServicesContent(sc);

		final Button checkSelAll = new Button(composite, SWT.None);
		checkSelAll.setText(MStudioMessages.getString("MStudioDeployPreferencePage.checkSelAll"));
		checkSelAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				for (Iterator<Button> it = btnList.iterator(); it.hasNext(); ) {
					Button btn = it.next();
					btn.setSelection(true);
				}
				selServList.clear();
				selServList.addAll(allServList);
			}
		});

		initializeByStoreData();

		return composite;
	}

	private Label createTipMsgContent(Composite parent) {
		Label t = new Label(parent, SWT.NULL);
		return  t;
	}

	protected void updateTipMessage(String tip) {
		Color c = Display.getCurrent() .getSystemColor(SWT.COLOR_RED);
		tipText.setForeground(c);
		tipText.setText(tip);
	}

	private DirectoryFieldEditor createLocationPathContent(Composite parent) {

		locationPath = new DirectoryFieldEditor(MSDPP_EMPTY_STR, MSDPP_DPLY_LOCAL, parent);
		locationPath.getTextControl(parent).addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				locationChanged();
			}});

		return locationPath;
	}
	
	private void createEnginesContent (Composite parent){
		MStudioEnvInfo einfo = MStudioPlugin.getDefault().getMStudioEnvInfo();
		
		Composite engineC = new Composite(parent, SWT.NULL);
	   GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 8;
		engineC.setLayout(layout);
		
		Label galT = new Label(engineC, SWT.NULL);
		galT.setText(MSDPP_GAL_ENGINE);
		galCom =  new Combo(engineC, SWT.READ_ONLY);
		galCom.setItems(einfo.getGalOptions());
		galCom.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				selectedGalEngine = galCom.getItem(galCom.getSelectionIndex());
				System.out.println("selected Gal Engine = " + selectedGalEngine);
			}
		});
		
		Label ialT = new Label(engineC, SWT.NULL);
		ialT.setText(MSDPP_IAL_ENGINE);
		ialCom =  new Combo(engineC, SWT.READ_ONLY);
		ialCom.setItems(einfo.getIalOptions());
		ialCom.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				selectedIalEngine = ialCom.getItem(ialCom.getSelectionIndex());
				System.out.println("selected Ial Engine = " + selectedIalEngine);
			}
		});
	}

	private SelectionAdapter btnSelAdp = new SelectionAdapter() {

		public void widgetSelected(SelectionEvent e) {
			selServList.clear();

			for (Iterator<Button> it = btnList.iterator(); it.hasNext(); ) {
				Button btn = it.next();
				if (btn.getSelection()) {
					selServList.add(btn.getText());
				}
			}
		}
	};

	private Control createServicesContent(Composite parent) {

		Composite com = new Composite(parent, SWT.BORDER);
		com.setLayout(new GridLayout());
		com.setLayoutData(new GridData(GridData.FILL_BOTH));
		Color c = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
		com.setBackground(c);

		if (allServList != null) {
			btnList.clear();

			for (Iterator<String> it = allServList.iterator(); it.hasNext(); ) {
				String serv = it.next();
				Button btn = new Button(com, SWT.CHECK);
				btn.setText(serv);
				btn.setBackground(c);
				btn.addSelectionListener(btnSelAdp);
				btnList.add(btn);
			}
		}

		com.layout(true);

		return com;
	}

	protected void performDefaults() {
		initializeByStoreData();
		super.performDefaults();
	}

	public boolean performOk() {
		saveToStoreData();
		return true;
	}
	
	public static String[] systemServices(){
		IPreferenceStore store = MStudioPlugin.getDefault().getPreferenceStore();
		if (store.contains(MStudioPreferenceConstants.MSTUDIO_DEFAULT_SERVICES)){
			String storeServ = store.getString(MStudioPreferenceConstants.MSTUDIO_DEFAULT_SERVICES);		
			return storeServ.split(MSDPP_SPLIT);
		}
		return new String[0];
	}
	
	public static String deployLocation(){
	    IPreferenceStore store = MStudioPlugin.getDefault().getPreferenceStore();
	    if (store.contains(MStudioPreferenceConstants.MSTUDIO_DEPLOY_LOCATION))
	    {
		    return store.getString(MStudioPreferenceConstants.MSTUDIO_DEPLOY_LOCATION);	    
	    }		
		return "";
	}


}

