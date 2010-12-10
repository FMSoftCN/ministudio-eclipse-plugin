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

import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards.MStudioParserIniFile;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.preferences.MStudioSelectSkinDialog;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProject;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;

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
	private String[] socInfo = null;
	private String[] resolution = null;
	private String[] colorDepth = null;
	private Label skinNameLabel = null;
	private int socTypeCheckedPos = -1;
	private final static String SOC_PATH_PREFIX = "/opt/hybridos/";
	private final static String SOC_CONFIG_FILE = "/.hybridos.cfg";
	private final static String TOOLCHAIN_SECTION = "toolchain";
	private final static String SYSTEM_SECTION = "system";
	private final static String SOC_SECTION = "soc";
	private final static String MINIGUI_SECTION = "minigui";
	private final static String KERNEL_SECTION = "kernel";
	private final static String PC_XVFB_SECTION = "pc_xvfb";
	private final static String DEFAULT_MODE_PROPERTY = "defaultmode";
	private final static String SKIN_PROPERTY = "skin";
	private final static String MINIGUI_CFG_FILE_NAME = Platform
			.getInstanceLocation().getURL().getPath()
			+ ".metadata/MiniGUI.cfg";
	private final static String MINIGUI_TARGET_CFG_FILE_NAME = Platform
			.getInstanceLocation().getURL().getPath()
			+ ".metadata/MiniGUI.cfg.target";

	public MStudioSoCPreferencePage() {
	}

	public MStudioSoCPreferencePage(String title) {
		super(title);
	}

	public MStudioSoCPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
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
		typeGd.horizontalSpan = 2;
		titleType.setLayoutData(typeGd);

		// create socTable
		socTable = new Table(typeCom, SWT.BORDER | SWT.CHECK | SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.SINGLE);
		GridData socGd = new GridData(170, 100);
		socTable.setLayoutData(socGd);

		socCtv = new CheckboxTableViewer(socTable);
		socCtv.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				socCtv.setAllChecked(false);
				socCtv.setChecked(event.getElement(), event.getChecked());
			}
		});
		socCtv.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				showSocInfo();
			}
		});

		// create system info list
		infoTable = new Table(typeCom, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData infoGd = new GridData(200, 100);
		infoTable.setLayoutData(infoGd);
		infoTv = new TableViewer(infoTable);

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
		titleScreen.setLayoutData(screenGd);

		Label resolutionLabel = new Label(screenCom, SWT.NONE);
		resolutionLabel.setText(MStudioMessages
				.getString("MStudioSoCPreferencePage.resolutionLabel"));

		// create resolution combo
		resolutionCombo = new Combo(screenCom, SWT.NONE);
		resolutionCombo.addSelectionListener(new SelectedChangeListener());
		resolutionCombo.setLayoutData(new GridData(110, 25));

		Label colorLabel = new Label(screenCom, SWT.NONE);
		colorLabel.setText(MStudioMessages
				.getString("MStudioSoCPreferencePage.colorDepthLabel"));

		// create colordepth combo
		colorCombo = new Combo(screenCom, SWT.NONE);
		colorCombo.addSelectionListener(new SelectedChangeListener());
		colorCombo.setLayoutData(new GridData(60, 25));

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
		GridData skinNameGd = new GridData();
		skinGd.horizontalAlignment = GridData.FILL;
		skinNameGd.widthHint = 330;
		skinNameLabel.setLayoutData(skinNameGd);

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
				skinDialog.skinDefaultName = skinNameLabel.getText();
				String skinName = (String) skinDialog.open();
				if (null != skinName) {
					setSkinNameLabelText(skinName);
				}
			}
		});

		if (!initWidgetValues()) {
			MessageDialog
					.openError(
							getShell(),
							MStudioMessages
									.getString("MStudioSoCPreferencePage.error.title"),
							MStudioMessages
									.getString("MStudioSoCPreferencePage.error.initWidgetValues"));
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
		String regexString = "^[1-9]+[0-9]*[xX][1-9]+[0-9]*$";
		return Pattern.matches(regexString, resolution);
	}

	private boolean validateColorDepth(String colorDepth) {
		String regexString = "^(([1-9])|((1|2)[0-9])|30|31|32)$";
		return Pattern.matches(regexString, colorDepth);
	}

	private boolean initSocTable() {

		socType = MStudioPlugin.getDefault().getMStudioEnvInfo().getSoCPaths();
		if (null == socType) {
			return false;
		}

		socCtv.add(socType);
		String currentSoc = getCurrentSoC();
		for (int i = 0; i < socType.length; i++) {
			if (socType[i].equals(currentSoc)) {
				socCtv.setChecked(socType[i], true);
				socTable.setSelection(i);
				socTypeCheckedPos = i;
				break;
			}
		}
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

			socInfo[i] = "";
			// add soc section info
			String[] socSectionProperties = iniFile
					.getPropertyNames(SOC_SECTION);
			if (null != socSectionProperties) {
				for (int j = 0; j < socSectionProperties.length; j++) {
					socInfo[i] += socSectionProperties[j]
							+ ":"
							+ iniFile.getStringProperty(SOC_SECTION,
									socSectionProperties[j]) + "\n";
				}
			}

			// add minigui section info
			String[] miniguiSectionProperties = iniFile
					.getPropertyNames(MINIGUI_SECTION);
			if (null != miniguiSectionProperties) {
				for (int j = 0; j < miniguiSectionProperties.length; j++) {
					socInfo[i] += miniguiSectionProperties[j]
							+ ":"
							+ iniFile.getStringProperty(MINIGUI_SECTION,
									miniguiSectionProperties[j]) + "\n";
				}
			}

			// add kernel section info
			String[] kernelSectionProperties = iniFile
					.getPropertyNames(KERNEL_SECTION);
			if (null != kernelSectionProperties) {
				for (int j = 0; j < kernelSectionProperties.length; j++) {
					socInfo[i] += kernelSectionProperties[j]
							+ ":"
							+ iniFile.getStringProperty(KERNEL_SECTION,
									kernelSectionProperties[j]) + "\n";
				}
			}

			// add toolchain section info
			String[] toolchainSectionProperties = iniFile
					.getPropertyNames(TOOLCHAIN_SECTION);
			if (null != toolchainSectionProperties) {
				for (int j = 0; j < toolchainSectionProperties.length; j++) {
					socInfo[i] += toolchainSectionProperties[j]
							+ ":"
							+ iniFile.getStringProperty(TOOLCHAIN_SECTION,
									toolchainSectionProperties[j]) + "\n";
				}
			}
		}

		if (socTypeCheckedPos >= 0 && socTypeCheckedPos < socType.length) {
			infoTv.setItemCount(0);
			infoTv.add(socInfo[socTypeCheckedPos]);
		}
		return true;
	}

	private String[] getResolutionData() {
		return new String[] { "320x240", "640x480", "1024x768" };
	}

	private String[] getColorDepthData() {
		return new String[] { "8", "16", "24", "32" };
	}

	private boolean initResolutionCombo() {
		resolution = getResolutionData();

		for (int i = 0; i < resolution.length; i++) {
			resolutionCombo.add(resolution[i].toString());
		}

		// set the default value
		resolutionCombo.select(0);
		return true;
	}

	private boolean initColorCombo() {
		colorDepth = getColorDepthData();

		for (int i = 0; i < colorDepth.length; i++) {
			colorCombo.add(colorDepth[i].toString());
		}

		// set the default value
		colorCombo.select(0);
		return true;
	}

	private boolean initSkinNameLabel() {
		String cfgName = MStudioPlugin.getDefault().getMStudioEnvInfo()
				.getPCMgCfgFileName();
		MStudioParserIniFile iniFile = new MStudioParserIniFile(cfgName);
		if (null == iniFile)
			return false;

		String skinName = iniFile.getStringProperty(PC_XVFB_SECTION,
				SKIN_PROPERTY);
		skinNameLabel.setText(skinName == null ? "" : skinName);
		return true;
	}

	// according to MiniGUI.cfg, get resolution, gvfb skin setting, etc.
	private boolean initWidgetValues() {

		if (!initSocTable()) {
			MessageDialog
					.openError(
							getShell(),
							MStudioMessages
									.getString("MStudioSoCPreferencePage.error.title"),
							MStudioMessages
									.getString("MStudioSoCPreferencePage.error.initSocTypeTable"));
			return false;
		}

		if (!initInfoTable()) {
			MessageDialog
					.openError(
							getShell(),
							MStudioMessages
									.getString("MStudioSoCPreferencePage.error.title"),
							MStudioMessages
									.getString("MStudioSoCPreferencePage.error.initSocInfoTable"));
			return false;
		}

		if (!initResolutionCombo()) {
			MessageDialog
					.openError(
							getShell(),
							MStudioMessages
									.getString("MStudioSoCPreferencePage.error.title"),
							MStudioMessages
									.getString("MStudioSoCPreferencePage.error.initResolutionCombo"));
			return false;
		}

		if (!initColorCombo()) {
			MessageDialog
					.openError(
							getShell(),
							MStudioMessages
									.getString("MStudioSoCPreferencePage.error.title"),
							MStudioMessages
									.getString("MStudioSoCPreferencePage.error.initColorDepthCombo"));
			return false;
		}

		if (!initSkinNameLabel()) {
			MessageDialog
					.openError(
							getShell(),
							MStudioMessages
									.getString("MStudioSoCPreferencePage.error.title"),
							MStudioMessages
									.getString("MStudioSoCPreferencePage.error.initSkinNameLabel"));
			return false;
		}

		return true;
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
	
	private boolean modifyProjectSetting(IProject project, String oldSocName, String newSocName) {

		// change the include settings	
		// change the libs settings
		
		if (null == project || null == oldSocName || null == newSocName) 
			return false;
		
		MStudioEnvInfo einfo = MStudioPlugin.getDefault().getMStudioEnvInfo();
		String crossToolPrefix = einfo.getToolChainPrefix(); 
//		String socName = einfo.getCurSoCName();
//		String configSuffix = newSocName;
		final String hostName = "Host";
		final String locateInclude = "../include/";
		
//		List<String> depLibList = new ArrayList<String> ();
//		String[] pkgs = new MStudioProject(project).getDepPkgs();
//		for (int idx = 0; idx < pkgs.length; idx++) {
//			String[] libs = einfo.getPackageLibs(pkgs[idx]);
//			for (int c = 0; c < libs.length; c++){
//				depLibList.add(libs[c]);
//			}
//		}
		
//		String[] depLibs          = depLibList.toArray(new String[depLibList.size()]);
		String[] pcIncludePath    = { einfo.getPCIncludePath(), locateInclude };
		String[] pcLibPath        = { einfo.getPCLibraryPath(), locateInclude };
		String[] crossIncludePath = { einfo.getCrossIncludePath() };
		String[] crossLibPath     = { einfo.getCrossLibraryPath() };
		
		IManagedProject managedProj = ManagedBuildManager.getBuildInfo(project).getManagedProject();
		IConfiguration[] cur_cfgs = managedProj.getConfigurations();
		
		for (int i = 0; i < cur_cfgs.length; i++) {
//			if (cur_cfgs[i].getName()) {
//				
//			}
			
			cur_cfgs[i].setName(cur_cfgs[i].getName() + "4" + hostName);
			for (ITool t : cur_cfgs[i].getToolChain().getTools() ) {
					try {
						if ( t.getId().contains("c.compiler") ) {
							IOption o = t.getOptionById("gnu.c.compiler.option.include.paths");
							cur_cfgs[i].setOption(t, o, pcIncludePath);
						}
						if (t.getId().contains("c.link")){
							IOption o = t.getOptionById("gnu.c.link.option.paths");
							cur_cfgs[i].setOption(t, o, pcLibPath);
//							o = t.getOptionById("gnu.c.link.option.libs");
//							cur_cfgs[i].setOption(t, o, depLibs);
						}
					} catch (BuildException e) {
						e.printStackTrace();
					}
			}
			
			String id = CDataUtil.genId(cur_cfgs[i].getId());
			IConfiguration newconfig = managedProj.createConfiguration(cur_cfgs[i], id);
			newconfig.setName(cur_cfgs[i].getName() + "4" + newSocName);
			newconfig.setDescription(newconfig.getName());
			for (ITool t : newconfig.getToolChain().getTools() ) {
				t.setToolCommand(crossToolPrefix + t.getToolCommand());
				try {
					if ( t.getId().contains("c.compiler") ) {
						IOption o = t.getOptionById("gnu.c.compiler.option.include.paths");
						newconfig.setOption(t, o, crossIncludePath);
					}
					if (t.getId().contains("c.link")){
						IOption o = t.getOptionById("gnu.c.link.option.paths");
						newconfig.setOption(t, o, crossLibPath);
//						o = t.getOptionById("gnu.c.link.option.libs");
//						newconfig.setOption(t, o, depLibs);
					}
				} catch (BuildException e) {
					e.printStackTrace();
				}
			}
		}
		
		if (cur_cfgs.length > 0)
			ManagedBuildManager.saveBuildInfo(project, false);
		
		return true;
	}
	
	private boolean modifyOldProjectsSetting(String oldSocName, String newSocName) {
		
		// search the whole projects 
		IProject[] projects = MStudioPlugin.getDefault().getMStudioEnvInfo().getMStudioProjects();
		for (int i=0; i<projects.length; i++) {
			if(!modifyProjectSetting(projects[i], oldSocName, newSocName)) {
				return false;
			}
		}
		
		return true;
	}

	private boolean saveWidgetValues() {

		String resolutionSelected = resolutionCombo.getText();
		if (null == resolutionSelected
				|| !validateResolution(resolutionSelected)) {
			MessageDialog
					.openError(
							getShell(),
							MStudioMessages
									.getString("MStudioSoCPreferencePage.error.title"),
							MStudioMessages
									.getString("MStudioSoCPreferencePage.error.resolutionSetting"));
			return false;
		}

		String colorDepthSelected = colorCombo.getText();
		if (null == colorDepthSelected
				|| !validateColorDepth(colorDepthSelected)) {
			MessageDialog
					.openError(
							getShell(),
							MStudioMessages
									.getString("MStudioSoCPreferencePage.error.title"),
							MStudioMessages
									.getString("MStudioSoCPreferencePage.error.colorDepthSetting"));
			return false;
		}

		if (null == skinNameLabel.getText() || "" == skinNameLabel.getText()) {
			MessageDialog
					.openError(
							getShell(),
							MStudioMessages
									.getString("MStudioSoCPreferencePage.error.title"),
							MStudioMessages
									.getString("MStudioSoCPreferencePage.error.skinSetting"));
			return false;
		}

		MStudioParserIniFile cfgTargetFile = new MStudioParserIniFile(
				MINIGUI_TARGET_CFG_FILE_NAME);
		if (null == cfgTargetFile) {
			MessageDialog
					.openError(
							getShell(),
							MStudioMessages
									.getString("MStudioSoCPreferencePage.error.title"),
							MStudioMessages
									.getString("MStudioSoCPreferencePage.error.getCfgTargetFile"));
			return false;
		}

		MStudioParserIniFile cfgFile = new MStudioParserIniFile(
				MINIGUI_CFG_FILE_NAME);
		if (null == cfgFile) {
			MessageDialog
					.openError(
							getShell(),
							MStudioMessages
									.getString("MStudioSoCPreferencePage.error.title"),
							MStudioMessages
									.getString("MStudioSoCPreferencePage.error.getCfgFile"));
			return false;
		}

		// save current soc
		Object[] selectedItems = socCtv.getCheckedElements();
		if (null == selectedItems || selectedItems.length <= 0) {
			MessageDialog
					.openError(
							getShell(),
							MStudioMessages
									.getString("MStudioSoCPreferencePage.error.title"),
							MStudioMessages
									.getString("MStudioSoCPreferencePage.error.selectSocType"));
			return false;
		}
		
		String oldSoc = getCurrentSoC();
		String newSoc = (String) selectedItems[0];
		/*if (!newSoc.equals(oldSoc))*/ {
			setCurrentSoC(newSoc);
//			if (!modifyOldProjectsSetting(oldSoc, newSoc)) {
//				MessageDialog
//						.openError(
//								getShell(),
//								MStudioMessages
//										.getString("MStudioSoCPreferencePage.error.title"),
//								MStudioMessages
//										.getString("MStudioSoCPreferencePage.error.changeOldProjectsSetting"));
//				return false;
//			}
		}

		// update MiniGUI.cfg and MiniGUI.cfg.target file
		// set resolution and color depth
		String temp = resolutionSelected + "-" + colorDepthSelected + "bpp";
		cfgTargetFile.setStringProperty(SYSTEM_SECTION, DEFAULT_MODE_PROPERTY,
				temp, null);
		cfgTargetFile.setStringProperty(PC_XVFB_SECTION, DEFAULT_MODE_PROPERTY,
				temp, null);

		cfgFile.setStringProperty(SYSTEM_SECTION, DEFAULT_MODE_PROPERTY, temp,
				null);
		cfgFile.setStringProperty(PC_XVFB_SECTION, DEFAULT_MODE_PROPERTY, temp,
				null);

		// update MiniGUI.cfg.target file
		// set skin info
		cfgTargetFile.setStringProperty(PC_XVFB_SECTION, SKIN_PROPERTY,
				skinNameLabel.getText(), null);
		if (!cfgTargetFile.save()) {
			MessageDialog
					.openError(
							getShell(),
							MStudioMessages
									.getString("MStudioSoCPreferencePage.error.title"),
							MStudioMessages
									.getString("MStudioSoCPreferencePage.error.saveCfgTargetFile"));
		}

		// update MiniGUI.cfg file
		// set skin info
		cfgFile.setStringProperty(PC_XVFB_SECTION, SKIN_PROPERTY, skinNameLabel
				.getText(), null);
		cfgFile.save();
		if (!cfgFile.save()) {
			MessageDialog
					.openError(
							getShell(),
							MStudioMessages
									.getString("MStudioSoCPreferencePage.error.title"),
							MStudioMessages
									.getString("MStudioSoCPreferencePage.error.saveCfgFile"));
		}

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

		if (saveWidgetValues())
			return true;
		else {
			MessageDialog
					.openError(
							getShell(),
							MStudioMessages
									.getString("MStudioSoCPreferencePage.error.title"),
							MStudioMessages
									.getString("MStudioSoCPreferencePage.error.saveWidgetValues"));
			return false;
		}
	}
}
