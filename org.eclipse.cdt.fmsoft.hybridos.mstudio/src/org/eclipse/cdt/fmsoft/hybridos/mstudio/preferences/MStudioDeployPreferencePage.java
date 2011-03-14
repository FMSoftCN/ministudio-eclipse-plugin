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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards.MStudioParserIniFile;


public class MStudioDeployPreferencePage extends PreferencePage
	implements IWorkbenchPreferencePage {

	private final static String MSDPP_SPLIT = "\t";
	private final static String MSDPP_PATH_INVALID =  
		MStudioMessages.getString("MStudioDeployPreferencePage.pathTip");
	private final static String MSDPP_DPLY_LOCAL = 
		MStudioMessages.getString("MStudioDeployPreferencePage.deployLocation");
	private final static String MSDPP_GAL_INVALID =  
		MStudioMessages.getString("MStudioDeployPreferencePage.galEngine_selectValid");
	private final static String MSDPP_IAL_INVALID =  
		MStudioMessages.getString("MStudioDeployPreferencePage.ialEngine_selectValid");
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
	private CheckboxTableViewer ctv = null;
	private String selectedGalEngine;
	private String selectedIalEngine;
	private MStudioEnvInfo envInfo = MStudioPlugin.getDefault().getMStudioEnvInfo();

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
		
		List<String> s = envInfo.getServices();
		if (envInfo.getMgRunMode() != "process" && s.contains("mginit")){
			s.remove("mginit");
		}
		allServList = s;
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
		boolean isValid = false;
		if (!file.exists())
			updateTipMessage(MSDPP_PATH_INVALID);
		else{
			updateTipMessage(MSDPP_EMPTY_STR);
			isValid &= true;
		}
		
		if(galCom.getSelectionIndex() >= 0 && galCom.getItemCount() > 0)
			updateTipMessage(MSDPP_GAL_INVALID);
		else
			updateTipMessage(MSDPP_EMPTY_STR);
		if(ialCom.getSelectionIndex() >= 0 && ialCom.getItemCount() > 0)
			updateTipMessage(MSDPP_IAL_INVALID);
		else
			updateTipMessage(MSDPP_EMPTY_STR);
			/*
			isValid &= true;
		setValid(isValid);
		*/
	}

	private void initializeByStoreData() {
		IPreferenceStore store = MStudioPlugin.getDefault().getPreferenceStore();
		if(null == store)
			return;
		String locationValue = store.getString(MStudioPreferenceConstants.MSTUDIO_DEPLOY_LOCATION);
		locationPath.setStringValue(locationValue);
		File locationFile = new File(locationValue);
		if (!locationFile.exists() || locationValue == null || locationValue == "")
			updateTipMessage(MSDPP_PATH_INVALID);
		//	setValid(false);
			
		String storeServ = store.getString(MStudioPreferenceConstants.MSTUDIO_DEFAULT_SERVICES);
		String[] defaultSelServ = (storeServ == null)? null : storeServ.split(MSDPP_SPLIT);
		if(defaultSelServ == null)
			return;
		selServList.clear();
		for (int i = 0; i < defaultSelServ.length; i++) {
			selServList.add(defaultSelServ[i]);
		}
		ctv.setCheckedElements(defaultSelServ);

		String tagetMgconfigureFile = envInfo.getWorkSpaceMetadataPath() + "MiniGUI.cfg.target";
		MStudioParserIniFile file = new MStudioParserIniFile(tagetMgconfigureFile);
		if (file == null)
			return; 
		selectedGalEngine = file.getStringProperty("system", "gal_engine");
		if (selectedGalEngine != null) {
			String[] galItems = galCom.getItems();
			for (int i = 0; i < galItems.length; i ++){
				if (selectedGalEngine.equals(galItems[i])){
					galCom.select(i);
					break;
				}
			}
		}
		
		selectedIalEngine = file.getStringProperty("system", "ial_engine");
		if (selectedIalEngine != null) {
			String[] ialItems = ialCom.getItems();
			for (int i = 0; i < ialItems.length; i ++){
				if (selectedIalEngine.equals(ialItems[i])){
					ialCom.select(i);
					break;
				}
			}
		}
	}

	private boolean saveToStoreData() {
		IPreferenceStore store = MStudioPlugin.getDefault().getPreferenceStore();
		String locationToStore = getChangedDeployLocation();

		if (locationToStore == null) 
			return false;
		store.setValue(MStudioPreferenceConstants.MSTUDIO_DEPLOY_LOCATION, locationToStore);
		
		String servToStore = new String();

		for (Iterator<String> it = selServList.iterator(); it.hasNext(); ) {
			servToStore += it.next() + MSDPP_SPLIT;
		}

		store.setValue(MStudioPreferenceConstants.MSTUDIO_DEFAULT_SERVICES, servToStore);
		
		String tagetMgconfigureFile = envInfo.getWorkSpaceMetadataPath() + "MiniGUI.cfg.target";
		MStudioParserIniFile file = new MStudioParserIniFile(tagetMgconfigureFile);
		if (file == null)
			return false; 
		
		if (selectedGalEngine == null) 
			return false;
		file.setStringProperty("system", "gal_engine", selectedGalEngine, null);
		
		if (selectedIalEngine == null)
			return false;
		file.setStringProperty("system", "ial_engine", selectedIalEngine, null);
		return file.save();
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

		Button checkSelAll = new Button(composite, SWT.None);
		checkSelAll.setText(MStudioMessages.getString("MStudioDeployPreferencePage.checkSelAll"));
		checkSelAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				for (Iterator<Button> it = btnList.iterator(); it.hasNext(); ) {
					Button btn = it.next();
					btn.setSelection(true);
				}
				selServList.clear();
				selServList.addAll(allServList);
				ctv.setCheckedElements(selServList.toArray());
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
		galCom.setItems(envInfo.getGalOptions());
		galCom.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				selectedGalEngine = galCom.getItem(galCom.getSelectionIndex());
				//System.out.println("selected Gal Engine = " + selectedGalEngine);
		}
		});
		
		Label ialT = new Label(engineC, SWT.NULL);
		ialT.setText(MSDPP_IAL_ENGINE);
		ialCom =  new Combo(engineC, SWT.READ_ONLY);
		ialCom.setItems(envInfo.getIalOptions());
		ialCom.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				selectedIalEngine = ialCom.getItem(ialCom.getSelectionIndex());
				//System.out.println("selected Ial Engine = " + selectedIalEngine);
			}
		});
	}

	private Control createServicesContent(Composite parent) {

		Composite com = new Composite(parent, SWT.NULL);
		com.setLayout(new GridLayout());
		com.setLayoutData(new GridData(GridData.FILL_BOTH));

		Table table = new Table(com, SWT.BORDER | SWT.CHECK | SWT.V_SCROLL);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		
		ctv = new CheckboxTableViewer(table);
		ctv.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return (Object[])inputElement;
			}
			public void dispose(){}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		});
		ctv.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				return element == null ? "" : (String)element;
			}

			public Image getImage(Object element) {
				return null;
			}
		});
		ctv.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getChecked()) {
					selServList.add((String)event.getElement());
				} else {
					selServList.remove((String)event.getElement());
				}
			}
		});
		
		if (allServList != null) {
			ctv.setInput(allServList.toArray());
		}

		com.layout(true);

		return com;
	}

	protected void performDefaults() {
		locationPath.setStringValue("");
		galCom.deselectAll();
		ialCom.deselectAll();
		
		initializeByStoreData();
		super.performDefaults();
	}

	public boolean performOk() {
		return saveToStoreData();
		//return true;
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

