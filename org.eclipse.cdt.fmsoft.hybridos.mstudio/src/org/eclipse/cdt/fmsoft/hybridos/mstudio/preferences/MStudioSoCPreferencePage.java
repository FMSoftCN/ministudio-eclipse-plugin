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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards.MStudioParserIniFile;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards.MStudioDeployAutobootProjectsWizardPage.TableListener; //import org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards.MStudioDeployExecutableProjectsWizardPage.SelectedChangeListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.preferences.MStudioSelectSkinDialog;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class MStudioSoCPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private Table socTable = null;
	private Table infoTable = null;
	private CheckboxTableViewer socCtv = null;
	private TableViewer infoTv = null;
	private Combo resolutionCombo = null;
	private Combo colorCombo = null;
	private Button skinBtn;
	private String[] socType = null;
	// private String socTypeSelected = null;
	private String[] socInfo = null;
	private String[] resolution = null;
	// private String resolutionSelected = null;
	private String[] colorDepth = null;
	// private String colorDepthSelected = null;
	private Label skinNameLabel = null;
	private final static String SOC_PATH_PREFIX = "/opt/hybridos/";
	private final static String SOC_CONFIG_FILE = "/.hybridos.cfg";
	private final static String SOC_CFG_SECTION_TOOLCHAIN = "toolchain";
	private final static String SOC_CFG_PROPERTY_GCC_VERSION = "gcc_version";
	private final static String SOC_CFG_PROPERTY_GLIBC_VERSION = "glibc_version";
	private final static String SYSTEM_SECTION = "system";
	private final static String SKIN_SECTION = "skin";
	private final static String DEFAULT_MODE_PROPERTY = "defaultmode";

	public MStudioSoCPreferencePage() {
	}

	public MStudioSoCPreferencePage(String title) {
		super(title);
	}

	public MStudioSoCPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init(IWorkbench workbench) {
		setTitle(MStudioMessages.getString("MStudioSoCPreferencePage.title"));
		setDescription(MStudioMessages
				.getString("MStudioSoCPreferencePage.desc"));
	}

	@Override
	protected Control createContents(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite typeCom = new Composite(composite, SWT.NONE);
		typeCom.setLayout(new GridLayout(2, false));
		typeCom.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL));

		Label titleType = new Label(typeCom, SWT.FILL);
		titleType.setText(MStudioMessages
				.getString("MStudioSoCPreferencePage.socTypeLabel"));
		GridData typeGd = new GridData();
		// typeGd.horizontalAlignment = GridData.FILL;
		typeGd.horizontalSpan = 2;
		titleType.setLayoutData(typeGd);

		// create socTable
		socTable = new Table(typeCom, SWT.BORDER | SWT.CHECK | SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.SINGLE);
		GridData socGd = new GridData(200, 200);
		// socGd.verticalSpan = 2;
		socTable.setLayoutData(socGd);

		socCtv = new CheckboxTableViewer(socTable);
		socCtv.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
			}
		});
		socCtv.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				showSocInfo();
			}
		});

		// initSocTable();

		// create system info list
		infoTable = new Table(typeCom, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData infoGd = new GridData(200, 200);
		// systemGd.verticalSpan = 2;
		infoTable.setLayoutData(infoGd);
		infoTv = new TableViewer(infoTable);

		// initSystemTable();

		// create screen settings
		Composite screenCom = new Composite(composite, SWT.NONE);
		screenCom.setLayout(new GridLayout(4, false));
		screenCom.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label titleScreen = new Label(screenCom, SWT.FILL);
		titleScreen.setText(MStudioMessages
				.getString("MStudioSoCPreferencePage.screenSettingsLabel"));
		GridData screenGd = new GridData();
		screenGd.horizontalAlignment = GridData.FILL;
		screenGd.horizontalSpan = 4;
		// screenGd.verticalSpan = 5;
		titleScreen.setLayoutData(screenGd);

		Label resolutionLabel = new Label(screenCom, SWT.NONE);
		resolutionLabel.setText(MStudioMessages
				.getString("MStudioSoCPreferencePage.resolutionLabel"));

		// create resolution combo
		resolutionCombo = new Combo(screenCom, SWT.NONE);
		resolutionCombo.addSelectionListener(new SelectedChangeListener());
		resolutionCombo.setLayoutData(new GridData(110, 20));
		initResolutionCombo();

		Label colorLabel = new Label(screenCom, SWT.NONE);
		colorLabel.setText(MStudioMessages
				.getString("MStudioSoCPreferencePage.colorDepthLabel"));

		// create colordepth combo
		colorCombo = new Combo(screenCom, SWT.NONE);
		colorCombo.addSelectionListener(new SelectedChangeListener());
		colorCombo.setLayoutData(new GridData(60, 20));
		// initColorCombo();

		// create skin settings
		Composite skinCom = new Composite(composite, SWT.NONE);
		skinCom.setLayout(new GridLayout(2, false));
		skinCom.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label skinLabel = new Label(skinCom, SWT.NONE);
		skinLabel.setText(MStudioMessages
				.getString("MStudioSoCPreferencePage.skinSettingsLabel"));
		GridData skinGd = new GridData();
		skinGd.horizontalAlignment = GridData.FILL;
		skinGd.horizontalSpan = 2;
		skinLabel.setLayoutData(skinGd);

		// create skin name label
		skinNameLabel = new Label(skinCom, SWT.NONE);
		// skinNameLabel.setText("");
		GridData skinNameGd = new GridData();
		skinGd.horizontalAlignment = GridData.FILL;
		skinNameGd.widthHint = 300;
		// skinNameGd.verticalSpan = 2;
		skinNameLabel.setLayoutData(skinNameGd);

		// initSkinNameLabel();

		// create select skin button
		skinBtn = new Button(skinCom, SWT.NONE);
		skinBtn.setText(MStudioMessages
				.getString("MStudioSoCPreferencePage.selectSkinLabel"));
		skinBtn.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {

				MStudioSelectSkinDialog skinDialog = new MStudioSelectSkinDialog(
						skinBtn.getShell());
				String skinName = (String) skinDialog.open();
				if (null != skinName) {
					setSkinNameLabelText(skinName);
				}
			}
		});

		if (!initWidgetValues()) {
			// messagebox();
		}
		return composite;
	}

	public static void setCurrentSoC(String name) {
		IPreferenceStore store = MStudioPlugin.getDefault()
				.getPreferenceStore();
		store.putValue(MStudioPreferenceConstants.MSTUDIO_SOC_NAME, name);
	}

	public static String getCurrentSoC() {
		IPreferenceStore store = MStudioPlugin.getDefault()
				.getPreferenceStore();
		if (!store.contains(MStudioPreferenceConstants.MSTUDIO_SOC_NAME))
			return null;
		return store.getString(MStudioPreferenceConstants.MSTUDIO_SOC_NAME);
	}

	protected class SelectedChangeListener implements SelectionListener {
		public void widgetDefaultSelected(SelectionEvent e) {
		}

		public void widgetSelected(SelectionEvent e) {
		}
	}

	private void setSkinNameLabelText(String name) {
		skinNameLabel.setText(name);
	}

	private boolean validateResolution(String resolution) {
		String regexString = "[1-9]+[0-9]*[xX][1-9]+[0-9]*";
		return Pattern.matches(regexString, resolution);
	}

	private boolean validateColorDepth(String colorDepth) {
		String regexString = "[1-9]+[0-9]";
		return Pattern.matches(regexString, colorDepth);
	}

	private boolean initSocTable() {

		socType = MStudioPlugin.getDefault().getMStudioEnvInfo().getSoCPaths();
		socCtv.add(socType);
		return true;
	}

	private boolean initInfoTable() {

		if (null == socType)
			return false;
		socInfo = new String[socType.length];

		for (int i = 0; i < socType.length; i++) {
			String path = SOC_PATH_PREFIX + socType[i] + SOC_CONFIG_FILE;
			MStudioParserIniFile iniFile = new MStudioParserIniFile(path);
			if (null == iniFile)
				return false;

			socInfo[i] = SOC_CFG_PROPERTY_GCC_VERSION
					+ ":"
					+ iniFile.getStringProperty(SOC_CFG_SECTION_TOOLCHAIN,
							SOC_CFG_PROPERTY_GCC_VERSION)
					+ "\n"
					+ SOC_CFG_PROPERTY_GLIBC_VERSION
					+ ":"
					+ iniFile.getStringProperty(SOC_CFG_SECTION_TOOLCHAIN,
							SOC_CFG_PROPERTY_GLIBC_VERSION);
		}
		return true;
	}

	private boolean initResolutionCombo() {
		resolution = new String[] { "320x240", "640x480", "1024x768" };

		for (int i = 0; i < resolution.length; i++) {
			resolutionCombo.add(resolution[i].toString());
		}

		// set the default value
		resolutionCombo.select(0);
		return true;
	}

	private boolean initColorCombo() {
		colorDepth = new String[] { "8", "16", "24", "32" };

		for (int i = 0; i < colorDepth.length; i++) {
			colorCombo.add(colorDepth[i].toString());
		}

		// set the default value
		colorCombo.select(0);
		return true;
	}

	private boolean initSkinNameLabel() {
		// TODO
		return true;
	}

	// according to MiniGUI.cfg, get resolution, gvfb skin setting, etc.
	private boolean initWidgetValues() {

		if (initSocTable() && initInfoTable() && initResolutionCombo()
				&& initColorCombo() && initSkinNameLabel())
			return true;
		else
			return false;
	}

	private void showSocInfo() {

		TableItem[] items = socTable.getItems();
		if (null == items) {
			return;
		}

		infoTv.setItemCount(0);
		for (int i = 0; i < items.length; i++) {
			if (socTable.isSelected(i)) {
				infoTv.add(socInfo[i]);
				break;
			}
		}
	}

	private boolean saveWidgetValues() {

		String resolutionSelected = resolutionCombo.getText();
		if (null == resolutionSelected) {
			return false;
		}

		String colorDepthSelected = colorCombo.getText();
		if (null == colorDepthSelected) {
			return false;
		}

		if (null == skinNameLabel.getText()) {
			return false;
		}

		String cfgTargetName = MStudioPlugin.getDefault().getMStudioEnvInfo()
				.getCrossMgCfgFileName();
		MStudioParserIniFile cfgTargetFile = new MStudioParserIniFile(
				cfgTargetName);
		if (null == cfgTargetFile)
			return false;

		String cfgName = MStudioPlugin.getDefault().getMStudioEnvInfo()
				.getPCMgCfgFileName();
		MStudioParserIniFile cfgFile = new MStudioParserIniFile(cfgName);
		if (null == cfgFile)
			return false;

		// save current soc
		TableItem[] items = socTable.getItems();
		for (int i = 0; i < items.length; i++) {
			if (items[i].getChecked()) {
				String tmp = items[i].getText();
				String tmp2 = getCurrentSoC();
				// setCurrentSoC(items[i].getText());
				break;
			}
		}

		// update MiniGUI.cfg and MiniGUI.cfg.target file
		// save resolution and color depth
		cfgTargetFile.setStringProperty(SYSTEM_SECTION, DEFAULT_MODE_PROPERTY,
				resolutionSelected + "-" + colorDepthSelected + "bpp", null);
		cfgFile.setStringProperty(SYSTEM_SECTION, DEFAULT_MODE_PROPERTY,
				resolutionSelected + "-" + colorDepthSelected + "bpp", null);

		// update MiniGUI.cfg and MiniGUI.cfg.target file
		// save skin info
		cfgTargetFile.setStringProperty(SKIN_SECTION, DEFAULT_MODE_PROPERTY,
				skinNameLabel.getText(), null);
		cfgFile.setStringProperty(SKIN_SECTION, DEFAULT_MODE_PROPERTY,
				skinNameLabel.getText(), null);

		return true;
	}

	protected void performDefaults() {

		socCtv.setItemCount(0);
		infoTv.setItemCount(0);
		colorCombo.removeAll();
		resolutionCombo.removeAll();

		initWidgetValues();
		super.performDefaults();
	}

	public boolean performOk() {

		// if(!validateResolution() || !validateColorDepth())
		{
			// message
			// return false;
		}

		if (saveWidgetValues())
			return true;
		else {
			// message
			return false;
		}
	}
}
