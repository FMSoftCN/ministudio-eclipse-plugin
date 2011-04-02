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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.core.LaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;


public class MStudioSoCPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	public static String SKIN_PATH = "/usr/local/share/gvfb/res/skin/";
	private Table socTable = null;
	private Table infoTable = null;
	private CheckboxTableViewer socCtv = null;
	private TableViewer infoTv = null;
	private Combo resolutionCombo = null;
	private Button skinBtn = null;
	private String[] socType = null;
	private String[] socInfo = null;
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
	private final static String GAL_ENGINE_PROPERTY = "gal_engine";
	private final static String FBCON_SECTION = "fbcon";
	private String defaultSoc = null;

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
		setDescription(MStudioMessages.getString("MStudioSoCPreferencePage.desc"));
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
		titleType.setText(MStudioMessages.getString("MStudioSoCPreferencePage.socTypeLabel"));
		GridData typeGd = new GridData();
		typeGd.horizontalSpan = 2;
		titleType.setLayoutData(typeGd);
		// create socTable
		socTable = new Table(typeCom, SWT.BORDER 
				| SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE);
		GridData socGd = new GridData(170, 100);
		socTable.setLayoutData(socGd);

		socCtv = new CheckboxTableViewer(socTable);
		socCtv.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				socCtv.setAllChecked(false);
				socCtv.setChecked(event.getElement(), event.getChecked());
				
				Object[] selectedItems = socCtv.getCheckedElements();
				if (null != selectedItems && selectedItems.length > 0) {
					String tmpSoc = (String) selectedItems[0];
					setCurrentSoC(tmpSoc);
					resolutionCombo.removeAll();
					initResolutionCombo();
				}
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
		titleScreen.setText(MStudioMessages.getString("MStudioSoCPreferencePage.screenSettingsLabel"));
		GridData screenGd = new GridData();
		screenGd.horizontalAlignment = GridData.FILL;
		screenGd.horizontalSpan = 4;
		titleScreen.setLayoutData(screenGd);

		Label resolutionLabel = new Label(screenCom, SWT.NONE);
		resolutionLabel.setText(MStudioMessages.getString("MStudioSoCPreferencePage.resolutionLabel"));
		// create resolution combo
		resolutionCombo = new Combo(screenCom, SWT.READ_ONLY/*SWT.NONE*/); // TODO it later
		resolutionCombo.addSelectionListener(new SelectedChangeListener());
		resolutionCombo.setLayoutData(new GridData(210, 25));
		// create skin settings
		Composite skinCom = new Composite(composite, SWT.NONE);
		skinCom.setLayout(new GridLayout(2, false));
		skinCom.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label skinLabel = new Label(skinCom, SWT.NONE);
		skinLabel.setText(MStudioMessages.getString("MStudioSoCPreferencePage.skinSettingsLabel"));
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
		skinBtn.setText(MStudioMessages.getString("MStudioSoCPreferencePage.selectSkinLabel"));
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
		
		if (!MStudioSelectSkinDialog.hasSkinFile()) {
			skinBtn.setEnabled(false);
		}
		
		defaultSoc = getCurrentSoC();
		if (null == defaultSoc) {
			defaultSoc = "null";
		}
		initWidgetValues();
		return composite;
	}

	public static void setCurrentSoC(String name) {
		IPreferenceStore store = MStudioPlugin.getDefault().getPreferenceStore();
		store.putValue(MStudioPreferenceConstants.MSTUDIO_SOC_NAME, name);
		MStudioPlugin.getDefault().getMStudioEnvInfo().updateSoCName();
	}

	public static String getCurrentSoC() {
		IPreferenceStore store = MStudioPlugin.getDefault().getPreferenceStore();
		if (null == store || !store.contains(MStudioPreferenceConstants.MSTUDIO_SOC_NAME))
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
		if(resolution == null)
			return false;
		String regexResolution = "[1-9]+[0-9]*\\s*[*x×]\\s*[1-9]+[0-9]*\\s*-\\s*[1-9]*bpp";
		return resolution.matches(regexResolution);
	}

	private boolean initSocTable() {
		socType = MStudioPlugin.getDefault().getMStudioEnvInfo().getSoCPaths();
		if (null == socType)
			return false;
		socTypeCheckedPos = -1;
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
			String[] socSectionProperties = iniFile.getPropertyNames(SOC_SECTION);
			if (null != socSectionProperties) {
				for (int j = 0; j < socSectionProperties.length; j++) {
					socInfo[i] += socSectionProperties[j]+ ":"
							+ iniFile.getStringProperty(SOC_SECTION, socSectionProperties[j]) + "\n";
				}
			}
			// add minigui section info
			String[] miniguiSectionProperties = iniFile.getPropertyNames(MINIGUI_SECTION);
			if (null != miniguiSectionProperties) {
				for (int j = 0; j < miniguiSectionProperties.length; j++) {
					socInfo[i] += miniguiSectionProperties[j] + ":"
							+ iniFile.getStringProperty(MINIGUI_SECTION, miniguiSectionProperties[j]) + "\n";
				}
			}
			// add kernel section info
			String[] kernelSectionProperties = iniFile.getPropertyNames(KERNEL_SECTION);
			if (null != kernelSectionProperties) {
				for (int j = 0; j < kernelSectionProperties.length; j++) {
					socInfo[i] += kernelSectionProperties[j] + ":"
							+ iniFile.getStringProperty(KERNEL_SECTION, kernelSectionProperties[j]) + "\n";
				}
			}
			// add toolchain section info
			String[] toolchainSectionProperties = iniFile.getPropertyNames(TOOLCHAIN_SECTION);
			if (null != toolchainSectionProperties) {
				for (int j = 0; j < toolchainSectionProperties.length; j++) {
					socInfo[i] += toolchainSectionProperties[j] + ":"
							+ iniFile.getStringProperty(TOOLCHAIN_SECTION, toolchainSectionProperties[j]) + "\n";
				}
			}
		}
		if (socTypeCheckedPos >= 0 && socTypeCheckedPos < socType.length) {
			infoTv.setItemCount(0);
			infoTv.add(socInfo[socTypeCheckedPos]);
		}
		return true;
	}

	private boolean initResolutionCombo() {
		
		String defaultSize = MStudioPlugin.getDefault().getMStudioEnvInfo().getScreenSize();
		defaultSize = (defaultSize == null)? "" : defaultSize;
		List<String> rl = new ArrayList<String>();
		rl = MStudioPlugin.getDefault().getMStudioEnvInfo().getResolutions();
		if (null == rl) {
			if("" == defaultSize)
				return false;
			else{
				resolutionCombo.add(defaultSize);
				resolutionCombo.select(0);
			}
		} 
		else {
			boolean find = false;
			for(int i = 0; i < rl.size(); i++){
				String temp = rl.get(i).toString();
				resolutionCombo.add(temp);
				if(temp.equals(defaultSize)){
					resolutionCombo.select(i);
					find = true;
				}
			}
			if(!find && resolutionCombo.getItemCount() > 0)
				resolutionCombo.select(0);
		}
		return true;
	}

	private boolean initSkinNameLabel() {
		String MINIGUI_CFG_FILE_NAME = 
			MStudioPlugin.getDefault().getMStudioEnvInfo().getWorkSpaceMetadataPath() + "MiniGUI.cfg";

		MStudioParserIniFile iniFile = new MStudioParserIniFile(MINIGUI_CFG_FILE_NAME);
		if (null == iniFile) {
			return false;
		}
		String skinName = iniFile.getStringProperty(PC_XVFB_SECTION, SKIN_PROPERTY);
		if (null == skinName) {
			return false;
		}
		
		skinName = skinName.substring(skinName.lastIndexOf(File.separator) + File.separator.length());
		skinNameLabel.setText(skinName == null ? "" : skinName);
		
		return true;
	}

	// according to MiniGUI.cfg, get resolution, gvfb skin setting, etc.
	private boolean initWidgetValues() {
		String err = "";
		if (initSocTable())
			err += MStudioMessages.getString("MStudioSoCPreferencePage.error.initSocTypeTable");
		if (initInfoTable())
			err += MStudioMessages.getString("MStudioSoCPreferencePage.error.initSocInfoTable");
		if(initResolutionCombo())
			err += MStudioMessages.getString("MStudioSoCPreferencePage.error.initResolutionCombo");;
		if(initSkinNameLabel())
			err += MStudioMessages.getString("MStudioSoCPreferencePage.error.initSkinNameLabel");
		if(!err.equals(""))
			setErrorMessage(MStudioMessages.getString("MStudioSoCPreferencePage.error.initWidgetValues") +
					err + MStudioMessages.getString("MStudioSoCPreferencePage.error.initWidgetValuesb"));
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

	private boolean modifyProjectSetting(IProject project, String oldSocName,
			String newSocName) {

		if (null == project || null == oldSocName || null == newSocName)
			return false;

		MStudioPlugin.getDefault().getMStudioEnvInfo().updateSoCName();
		IManagedProject managedProj = ManagedBuildManager.getBuildInfo(project).getManagedProject();
		IConfiguration[] cur_cfgs = managedProj.getConfigurations();
		
		MStudioEnvInfo einfo = MStudioPlugin.getDefault().getMStudioEnvInfo();
		String crossToolPrefix = einfo.getToolChainPrefix();
		
		List<String> depLibList = new ArrayList<String> ();
		String[] pkgs = new MStudioProject(project).getDepPkgs();
		if (pkgs != null) {
			for (int idx = 0; idx < pkgs.length; idx++) {
				String[] libs = einfo.getPackageLibs(pkgs[idx]);
				if (libs != null) {
					for (int c = 0; c < libs.length; c++){
						depLibList.add(libs[c]);
					}
				}
			}
		}
		String[] depLibs = depLibList.toArray(new String[depLibList.size()]);
		
		for (int i = 0; i < cur_cfgs.length; i++) {

			// change the configure name
			String configureName = cur_cfgs[i].getName();
			if (null != configureName) {
				cur_cfgs[i].setName(configureName.replaceFirst(oldSocName,
						newSocName));
			}

			String description = cur_cfgs[i].getDescription();
			if (null != description) {
				cur_cfgs[i].setDescription(description.replaceFirst(oldSocName,
						newSocName));
			}
			
			for (ITool t : cur_cfgs[i].getToolChain().getTools()) {
				String toolCommand = t.getToolCommand();
				if (null == toolCommand) {
					continue;
				}
				String newToolCommand = toolCommand.substring(toolCommand.lastIndexOf("-") + 1);
				if (null != newToolCommand && !toolCommand.equals(newToolCommand)) {
					t.setToolCommand(einfo.getSOCBinPath() + crossToolPrefix + newToolCommand);
				}
				try {
					// change the include path settings
					if (t.getId().contains("c.compiler")) {
						IOption o = t.getOptionBySuperClassId("gnu.c.compiler.option.include.paths");
						String[] includePaths = o.getBasicStringListValue();
						if (null != includePaths) {
							for (int j = 0; j < includePaths.length; j++) {
								if (null != includePaths[j]) {
									includePaths[j] = includePaths[j].replaceFirst(
											oldSocName, newSocName);								
								}
							}
							cur_cfgs[i].setOption(t, o, includePaths);
						}
					}
					
					if (t.getId().contains("cpp.compiler")) {
						IOption o = t.getOptionBySuperClassId("gnu.cpp.compiler.option.include.paths");
						String[] includePaths = o.getBasicStringListValue();
						if (null != includePaths) {
							for (int j = 0; j < includePaths.length; j++) {
								if (null != includePaths[j]) {
									includePaths[j] = includePaths[j].replaceFirst(
											oldSocName, newSocName);								
								}
							}
							cur_cfgs[i].setOption(t, o, includePaths);
						}
					}

					// change the libs path settings
					if (t.getId().contains("c.link")) {
						IOption o = t.getOptionBySuperClassId("gnu.c.link.option.paths");
						String[] libPaths = o.getBasicStringListValue();
						if (null != libPaths) {
							for (int j = 0; j < libPaths.length; j++) {
								if (null != libPaths[j]) {
									libPaths[j] = libPaths[j].replaceFirst(
											oldSocName, newSocName);									
								}
							}
							cur_cfgs[i].setOption(t, o, libPaths);
						}
						
						if (null != (o = t.getOptionById("gnu.c.link.option.libs"))){
							cur_cfgs[i].setOption(t, o, depLibs);
						}
					}
					
					if (t.getId().contains("cpp.link")) {
						IOption o = t.getOptionBySuperClassId("gnu.cpp.link.option.paths");
						String[] libPaths = o.getBasicStringListValue();
						if (null != libPaths) {
							for (int j = 0; j < libPaths.length; j++) {
								if (null != libPaths[j]) {
									libPaths[j] = libPaths[j].replaceFirst(
											oldSocName, newSocName);									
								}
							}
							cur_cfgs[i].setOption(t, o, libPaths);
						}
						
						if (null != (o = t.getOptionById("gnu.cpp.link.option.libs"))){
							cur_cfgs[i].setOption(t, o, depLibs);
						}
					}
				} catch (BuildException e) {
					e.printStackTrace();
				}
			}
		}

		if (cur_cfgs.length > 0)
			ManagedBuildManager.saveBuildInfo(project, false);

		DebugUIPlugin dp = DebugUIPlugin.getDefault();
		ILaunchConfiguration[] configs = dp.getLaunchConfigurationManager().getApplicableLaunchConfigurations(null, project);
		Map<String, String> map = new HashMap<String, String>(2);
		String[] pcLibPath = {einfo.getPCLibraryPath()};
		
		map.put("MG_CFG_PATH", einfo.getWorkSpaceMetadataPath());
		map.put("LD_LIBRARY_PATH", pcLibPath[0]);
		
		ILaunchConfigurationWorkingCopy wc = null;
		try {
			wc = configs[0].getWorkingCopy();
			if (null != wc) {
				wc.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, map);
				wc.doSave();
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return true;
	}

	private boolean modifyOldProjectsSetting(String oldSocName, String newSocName) {
		// search the whole projects
		IProject[] projects = MStudioPlugin.getDefault().getMStudioEnvInfo().getMStudioProjects();
		for (int i = 0; i < projects.length; i++) {
			if (!modifyProjectSetting(projects[i], oldSocName, newSocName)) {
				return false;
			}
		}

		return true;
	}
	
	private void setProjectsSoC(String soc) {
		// 轮询修改各个工程的soc.name
		IProject[] projects = MStudioPlugin.getDefault().getMStudioEnvInfo().getMStudioProjects();
		if (null == projects) {
			return;
		}
		for (int i = 0; i < projects.length; i++) {
			MStudioProject prj = new MStudioProject(projects[i]);
			prj.setProjectSocName(soc);
		}
	}
	
	private boolean saveWidgetValues() {
		String MINIGUI_CFG_FILE_NAME = 
			MStudioPlugin.getDefault().getMStudioEnvInfo().getWorkSpaceMetadataPath() + "MiniGUI.cfg";
		String MINIGUI_TARGET_CFG_FILE_NAME = 
			MStudioPlugin.getDefault().getMStudioEnvInfo().getWorkSpaceMetadataPath() + "MiniGUI.cfg.target";

		String errStr = "";
		String resolutionSelected = resolutionCombo.getText();
		do {
			if (null == resolutionSelected || resolutionSelected == "" || !validateResolution(resolutionSelected)){
				errStr += MStudioMessages.getString("MStudioSoCPreferencePage.error.resolutionSetting");
				break;
			}
			if (null == skinNameLabel.getText()){
				errStr += "\n" + MStudioMessages.getString("MStudioSoCPreferencePage.error.skinSetting");
				break;
			}
			MStudioParserIniFile cfgTargetFile = new MStudioParserIniFile(MINIGUI_TARGET_CFG_FILE_NAME);
			if (null == cfgTargetFile){
				errStr += "\n" + MStudioMessages.getString("MStudioSoCPreferencePage.error.getCfgTargetFile");
				break;
			}
			MStudioParserIniFile cfgFile = new MStudioParserIniFile(MINIGUI_CFG_FILE_NAME);
			if (null == cfgFile){
				errStr += "\n" + MStudioMessages.getString("MStudioSoCPreferencePage.error.getCfgFile");
				break;
			}
			// save current soc
			Object[] selectedItems = socCtv.getCheckedElements();
			if (null == selectedItems || selectedItems.length <= 0) {
				errStr += "\n" + MStudioMessages.getString("MStudioSoCPreferencePage.error.selectSocType");
				break;
			}

			String newSoc = (String) selectedItems[0];
			if (!newSoc.equals(defaultSoc)) {
				setCurrentSoC(newSoc);
				if (!modifyOldProjectsSetting(defaultSoc, newSoc)) {
					errStr += "\n" + MStudioMessages.getString("MStudioSoCPreferencePage.error.changeOldProjectsSetting");
					break;
				}
				setProjectsSoC(newSoc);
				defaultSoc = newSoc;
			}

			// update MiniGUI.cfg.target file
			// set resolution and color depth
			cfgTargetFile.setStringProperty(SYSTEM_SECTION, DEFAULT_MODE_PROPERTY, resolutionSelected, null);
			String engineProperty = cfgTargetFile.getStringProperty(SYSTEM_SECTION, GAL_ENGINE_PROPERTY);
			cfgTargetFile.setStringProperty(engineProperty == null ? FBCON_SECTION : engineProperty, 
					DEFAULT_MODE_PROPERTY, resolutionSelected, null);

			// set skin info
			String skinName = skinNameLabel.getText();
			if (null != skinName && skinName.endsWith(".skin")) {
				skinName = SKIN_PATH + skinNameLabel.getText();
			} else {
				skinName = "";
			}
			cfgTargetFile.setStringProperty(PC_XVFB_SECTION, SKIN_PROPERTY, skinNameLabel.getText(), null);

			if (!cfgTargetFile.save()) {
				errStr += "\n" + MStudioMessages.getString("MStudioSoCPreferencePage.error.saveCfgTargetFile");
				break;
			}

			// update MiniGUI.cfg file
			// set resolution and color depth
			cfgFile.setStringProperty(SYSTEM_SECTION, DEFAULT_MODE_PROPERTY, resolutionSelected, null);
			cfgFile.setStringProperty(PC_XVFB_SECTION, DEFAULT_MODE_PROPERTY, resolutionSelected, null);

			// set skin info
			cfgFile.setStringProperty(PC_XVFB_SECTION, SKIN_PROPERTY, skinName, null);

			if (!cfgFile.save()) {
				errStr += "\n" + MStudioMessages.getString("MStudioSoCPreferencePage.error.saveCfgFile");
				break;
			}
		}while(false);
		if(errStr != ""){
			MessageDialog.openError(getShell(), MStudioMessages.getString("MStudioSoCPreferencePage.error.title"), errStr);
			return false;
		}
		return true;
	}

	protected void performDefaults() {
		socCtv.setItemCount(0);
		infoTv.setItemCount(0);
		resolutionCombo.removeAll();
		skinNameLabel.setText("");

		setCurrentSoC(defaultSoc);
		initWidgetValues();
		super.performDefaults();
	}

	public boolean performOk() {
		return saveWidgetValues();
	}
	
   public boolean performCancel() {
		setCurrentSoC(defaultSoc);
      return super.performCancel();
    }
}
