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

package org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;

import org.eclipse.cdt.managedbuilder.buildproperties.IBuildProperty;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.cdt.managedbuilder.ui.properties.ManagedBuilderUIPlugin;
import org.eclipse.cdt.managedbuilder.ui.wizards.CfgHolder;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.ManagedBuildWizard;

import org.eclipse.cdt.ui.newui.CDTPrefUtil;
import org.eclipse.cdt.ui.templateengine.IWizardDataPage;
import org.eclipse.cdt.ui.templateengine.Template;
import org.eclipse.cdt.ui.templateengine.TemplateEngineUI;
import org.eclipse.cdt.ui.templateengine.TemplateEngineUIUtil;
import org.eclipse.cdt.ui.templateengine.pages.UIWizardPage;
import org.eclipse.cdt.ui.wizards.CWizardHandler;
import org.eclipse.cdt.ui.wizards.EntryDescriptor;
import org.eclipse.cdt.ui.wizards.IWizardItemsListListener;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProject;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProject.MStudioProjectTemplateType;


public class MStudioWizardHandler extends CWizardHandler {

	public final static String ARTIFACT = "org.eclipse.cdt.build.core.buildArtefactType";
	public final static String EMPTY_STR = "";

	private final static String PROPERTY = "org.eclipse.cdt.build.core.buildType";
	private final static String PROP_VAL = PROPERTY + ".debug";

	final String TEMPLATE_TYPE_EXE    = "MStudioExecutableCProject";
	final String TEMPLATE_TYPE_LIB    = "MStudioSimpleSharedLibCProject";
	final String TEMPLATE_TYPE_MGINIT = "MStudioMginitModuleCProject";
	final String TEMPLATE_TYPE_IAL    = "MStudioCustomIALCProject";

	private IWizard wizard = null;
	private IWizardPage startingPage = null;
	private IProjectType pt = null;
	private IToolChain[] savedToolChains = null;
	private EntryInfo entryInfo = null;
	private String propertyId = null;
	private List<String> preferredTCs = new ArrayList<String>();

	protected IWizardPage[] customPages = null;
	protected IWizardItemsListListener listener = null;
	protected CfgHolder[] cfgs = null;
	protected MStudioNewCAppSoCConfigWizardPage fConfigPage = null;
	protected SortedMap<String, IToolChain> full_tcs = new TreeMap<String, IToolChain>();

	protected static final class EntryInfo {

		private IWizardPage[] templatePages = null;
		private IWizardPage predatingPage = null;
		private IWizardPage followingPage = null;
		private Template template = null;
		private EntryDescriptor entryDescriptor = null;
		private boolean initialized = false;
		private boolean isValid = false;
		private String templateId = null;
		private SortedMap<String, IToolChain> tcs = null;

		public EntryInfo(EntryDescriptor dr, SortedMap<String, IToolChain> _tcs) {
			entryDescriptor = dr;
			tcs = _tcs;
		}

		public boolean isValid() {
			initialize();
			return isValid;
		}

		public Template getTemplate() {
			initialize();
			return template;
		}

		public EntryDescriptor getDescriptor() {
			return entryDescriptor;
		}

		private void initialize() {

			if (initialized)
				return;

			do {
				if (entryDescriptor == null)
					break;
				String path[] = entryDescriptor.getPathArray();
				if (path == null || path.length == 0)
					break;

				if (!entryDescriptor.isCategory() && path.length > 1
						&& (!path[0].equals(ManagedBuildWizard.OTHERS_LABEL))) {
					templateId = path[path.length - 1];
					Template templates[] = TemplateEngineUI.getDefault().getTemplates();
					if (templates.length == 0)
						break;
					for (int i = 0; i < templates.length; i++) {
						if (templates[i].getTemplateId().equals(templateId)) {
							template = templates[i];
							break;
						}
					}

					if (template == null)
						break;
				}
				isValid = true;
			} while (false);

			initialized = true;
		}

		public Template getInitializedTemplate(IWizardPage predatingPage,
				IWizardPage followingPage, Map<String, String> map) {

			getNextPage(predatingPage, followingPage);

			Template template = getTemplate();
			if (template != null) {
				Map<String, String> valueStore = template.getValueStore();
				// valueStore.clear();
				for (int i = 0; i < templatePages.length; i++) {
					IWizardPage page = templatePages[i];
					if (page instanceof UIWizardPage)
						valueStore.putAll(((UIWizardPage) page).getPageData());

					if (page instanceof IWizardDataPage)
						valueStore.putAll(((IWizardDataPage) page).getPageData());
				}
				if (map != null) {
					valueStore.putAll(map);
				}
			}

			return template;
		}

		public IWizardPage getNextPage(IWizardPage predatingPage, IWizardPage followingPage) {

			initialize();

			if (this.templatePages == null || this.predatingPage != predatingPage
					|| this.followingPage != followingPage) {
				this.predatingPage = predatingPage;
				this.followingPage = followingPage;

				if (template != null) {
					this.templatePages = template.getTemplateWizardPages(
							predatingPage, followingPage, predatingPage.getWizard());
				} else {
					templatePages = new IWizardPage[0];
					followingPage.setPreviousPage(predatingPage);
				}
			}

			if (templatePages.length != 0)
				return templatePages[0];

			return followingPage;
		}

		private boolean canFinish(IWizardPage predatingPage, IWizardPage followingPage) {

			getNextPage(predatingPage, followingPage);

			for (int i = 0; i < templatePages.length; i++) {
				if (!templatePages[i].isPageComplete())
					return false;
			}

			return true;
		}

		protected Set<String> tc_filter() {
			Set<String> full = tcs.keySet();

			if (entryDescriptor == null)
				return full;

			Set<String> out = new LinkedHashSet<String>(full.size());

			for (String s : full) {
				if (isToolChainAcceptable(s))
					out.add(s);
			}

			return out;
		}

		public boolean isToolChainAcceptable(String tcId) {

			if (template == null || template.getTemplateInfo() == null)
				return true;

			String[] ss = template.getTemplateInfo().getToolChainIds();
			if (ss == null || ss.length == 0)
				return true;

			Object ob = tcs.get(tcId);
			if (ob == null)
				return true; // sic ! This can occur with Other Toolchain only
			if (!(ob instanceof IToolChain))
				return false;

			String id1 = ((IToolChain) ob).getId();
			IToolChain sup = ((IToolChain) ob).getSuperClass();
			String id2 = sup == null ? null : sup.getId();

			for (int i = 0; i < ss.length; i++) {
				if ((ss[i] != null && ss[i].equals(id1))
						|| (ss[i] != null && ss[i].equals(id2)))
					return true;
			}

			return false;
		}

		public int getToolChainsCount() {
			return tc_filter().size();
		}
	}

	public MStudioWizardHandler(Composite p, IWizard w) {
		this("MiniGUI Project", p, w);
	}

	public MStudioWizardHandler(IProjectType _pt, Composite p, IWizard w) {
		super(p, MStudioMessages.getString("MStudioWizardHandler.0"), _pt.getName());
		pt = _pt;
		setWizard(w);
	}

	public MStudioWizardHandler(String name, Composite p, IWizard w) {
		super(p, MStudioMessages.getString("MStudioWizardHandler.0"), name);
		setWizard(w);
	}

	public MStudioWizardHandler(IBuildPropertyValue val, Composite p, IWizard w) {
		super(p, MStudioMessages.getString("MStudioWizardHandler.0"), val.getName());
		propertyId = val.getId();
		setWizard(w);
	}

	private void setWizard(IWizard w) {
		if (w != null) {
			if (w.getStartingPage() instanceof IWizardItemsListListener)
				listener = (IWizardItemsListListener) w.getStartingPage();
			wizard = w;
			startingPage = w.getStartingPage();
		}
	}

	protected IWizardPage getStartingPage() {
		return startingPage;
	}

	public Map<String, String> getMainPageData() {

		WizardNewProjectCreationPage page = (WizardNewProjectCreationPage) getStartingPage();
		Map<String, String> data = new HashMap<String, String>();
		String projName = page.getProjectName();

		projName = projName != null ? projName.trim() : EMPTY_STR;

		data.put("projectName", projName); //$NON-NLS-1$
		data.put("baseName", getBaseName(projName)); //$NON-NLS-1$
		data.put("baseNameUpper", getBaseName(projName).toUpperCase()); //$NON-NLS-1$
		data.put("baseNameLower", getBaseName(projName).toLowerCase()); //$NON-NLS-1$

		String location = page.getLocationPath().toOSString();
		if (location == null)
			location = EMPTY_STR;

		data.put("location", location);

		return data;
	}

	private String getBaseName(String name) {

		String baseName = name;
		int dot = baseName.lastIndexOf('.');

		if (dot != -1) {
			baseName = baseName.substring(dot + 1);
		}

		dot = baseName.indexOf(' ');

		if (dot != -1) {
			baseName = baseName.substring(0, dot);
		}

		return baseName;
	}

	public void handleSelection() {

		List<String> preferred = CDTPrefUtil.getPreferredTCs();

		updatePreferred(preferred);
		loadCustomPages();

		if (fConfigPage != null)
			fConfigPage.pagesLoaded = false;
	}

	private void loadCustomPages() {

		if (!(getWizard() instanceof MStudioNewCAppWizard))
			return; // not probable

		MStudioNewCAppWizard wz = (MStudioNewCAppWizard) getWizard();

		if (customPages == null) {
			MBSCustomPageManager.init();
			MBSCustomPageManager.addStockPage(getStartingPage(), MStudioNewCAppProjectSelectWizardPage.PAGE_ID);
			MBSCustomPageManager.addStockPage(getConfigPage(), MStudioNewCAppSoCConfigWizardPage.PAGE_ID);

			// load all custom pages specified via extensions
			try {
				MBSCustomPageManager.loadExtensions();
			} catch (BuildException e) {
				e.printStackTrace();
			}

			customPages = MBSCustomPageManager.getCustomPages();

			if (customPages == null)
				customPages = new IWizardPage[0];

			for (int k = 0; k < customPages.length; k++)
				customPages[k].setWizard(wz);
		}

		setCustomPagesFilter(wz);
	}

	private void setCustomPagesFilter(MStudioNewCAppWizard wz) {

		String[] natures = wz.getNatures();

		if (natures == null || natures.length == 0) {
			MBSCustomPageManager.addPageProperty(MBSCustomPageManager.PAGE_ID,
					MBSCustomPageManager.NATURE, null);
		} else if (natures.length == 1) {
			MBSCustomPageManager.addPageProperty(MBSCustomPageManager.PAGE_ID,
					MBSCustomPageManager.NATURE, natures[0]);
		} else {
			TreeSet<String> x = new TreeSet<String>();
			for (int i = 0; i < natures.length; i++)
				x.add(natures[i]);
			MBSCustomPageManager.addPageProperty(MBSCustomPageManager.PAGE_ID,
					MBSCustomPageManager.NATURE, x);
		}
		// Project type can be obtained either from Handler (for old-style
		// projects),
		// or multiple values will be got from separate ToolChains (for
		// new-style).
		boolean ptIsNull = (getProjectType() == null);
		if (!ptIsNull)
			MBSCustomPageManager.addPageProperty(MBSCustomPageManager.PAGE_ID,
							MBSCustomPageManager.PROJECT_TYPE, getProjectType().getId());

		IToolChain[] tcs = getSelectedToolChains();
		int n = (tcs == null) ? 0 : tcs.length;
		ArrayList<IToolChain> x = new ArrayList<IToolChain>();
		TreeSet<String> y = new TreeSet<String>();

		for (int i = 0; i < n; i++) {
			if (tcs[i] == null) // --- NO TOOLCHAIN ---
				continue; // has no custom pages.
			x.add(tcs[i]);

			IConfiguration cfg = tcs[i].getParent();
			if (cfg == null)
				continue;
			IProjectType pt = cfg.getProjectType();
			if (pt != null)
				y.add(pt.getId());
		}

		MBSCustomPageManager.addPageProperty(MBSCustomPageManager.PAGE_ID,
				MBSCustomPageManager.TOOLCHAIN, x);

		if (ptIsNull) {
			if (y.size() > 0) {
				MBSCustomPageManager.addPageProperty(MBSCustomPageManager.PAGE_ID,
						MBSCustomPageManager.PROJECT_TYPE, y);
			} else {
				MBSCustomPageManager.addPageProperty(MBSCustomPageManager.PAGE_ID,
						MBSCustomPageManager.PROJECT_TYPE, null);
			}
		}
	}

	public void handleUnSelection() {
		if (fConfigPage != null)
			fConfigPage.pagesLoaded = false;
	}

	public void addTc(IToolChain tc) {
		if (tc == null || tc.isAbstract() || tc.isSystemObject())
			return;

		full_tcs.put(tc.getUniqueRealName(), tc);
	}

	public void createProject(IProject project, boolean defaults,
			boolean onFinish) throws CoreException {

		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		ICProjectDescription des = mngr.createProjectDescription(project, false, !onFinish);
		ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);

		cfgs = fConfigPage.getCfgItems(false);
		if (cfgs == null || cfgs.length == 0)
			cfgs = MStudioNewCAppSoCConfigWizardPage.getDefaultCfgs(this);

		if (cfgs == null || cfgs.length == 0 || cfgs[0].getConfiguration() == null) {
			throw new CoreException(new Status(IStatus.ERROR,
					ManagedBuilderUIPlugin.getUniqueIdentifier(),
					MStudioMessages.getString("MStudioWizardHandler.6"))); 
		}

		Configuration cf = (Configuration) cfgs[0].getConfiguration();
		ManagedProject mProj = new ManagedProject(project, cf.getProjectType());
		info.setManagedProject(mProj);

		cfgs = CfgHolder.unique(cfgs);
		cfgs = CfgHolder.reorder(cfgs);

		ICConfigurationDescription cfgDebug = null;
		ICConfigurationDescription cfgFirst = null;

		for (int i = 0; i < cfgs.length; i++) {
			cf = (Configuration) cfgs[i].getConfiguration();
			String id = ManagedBuildManager.calculateChildId(cf.getId(), null);
			Configuration config = new Configuration(mProj, cf, id, false, true);
			CConfigurationData data = config.getConfigurationData();
			ICConfigurationDescription cfgDes = des.createConfiguration(
					ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
			config.setConfigurationDescription(cfgDes);
			config.exportArtifactInfo();

			IBuilder bld = config.getEditableBuilder();
			if (bld != null) {
				bld.setManagedBuildOn(true);
			}

			config.setName(cfgs[i].getName());
			config.setArtifactName(removeSpaces(project.getName()));

			IBuildProperty b = config.getBuildProperties().getProperty(PROPERTY);
			if (cfgDebug == null && b != null && b.getValue() != null
					&& PROP_VAL.equals(b.getValue().getId()))
				cfgDebug = cfgDes;
			if (cfgFirst == null) // select at least first configuration
				cfgFirst = cfgDes;
		}

		mngr.setProjectDescription(project, des);
		doTemplatesPostProcess(project);

		createTargetConfiguration(project);
		doCustom(project);
	}

	private void createTargetConfiguration(IProject project) {

		MStudioEnvInfo einfo = MStudioPlugin.getDefault().getMStudioEnvInfo();
		String crossToolPrefix = einfo.getToolChainPrefix(); 
		String socName = einfo.getCurSoCName();
		String configSuffix = (socName != null && socName.length() > 0)? socName : "Target";
		final String hostName = "Host";
		final String locateInclude = "../include/";
		
		List<String> depLibList = new ArrayList<String> ();
		String[] pkgs = new MStudioProject(project).getDepPkgs();
		for (int idx = 0; idx < pkgs.length; idx++) {
			String[] libs = einfo.getPackageLibs(pkgs[idx]);
			for (int c = 0; c < libs.length; c++){
				depLibList.add(libs[c]);
			}
		}
		String[] depLibs          = depLibList.toArray(new String[depLibList.size()]);
		String[] pcIncludePath    = { einfo.getPCIncludePath(), locateInclude };
		String[] pcLibPath        = { einfo.getPCLibraryPath()};
		String[] crossIncludePath = { einfo.getCrossIncludePath(), locateInclude };
		String[] crossLibPath     = { einfo.getCrossLibraryPath() };
		
		IManagedProject managedProj = ManagedBuildManager.getBuildInfo(project).getManagedProject();
		IConfiguration[] cur_cfgs = managedProj.getConfigurations();
		for (int i = 0; i < cur_cfgs.length; i++) {
			String id = CDataUtil.genId(cur_cfgs[i].getId());
			IConfiguration newconfig = managedProj.createConfiguration(cur_cfgs[i], id);
			newconfig.setName(cur_cfgs[i].getName() + "4" + configSuffix);
			newconfig.setDescription(newconfig.getName());
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
							o = t.getOptionById("gnu.c.link.option.libs");
							cur_cfgs[i].setOption(t, o, depLibs);
						}
					} catch (BuildException e) {
						e.printStackTrace();
					}
			}
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
						o = t.getOptionById("gnu.c.link.option.libs");
						newconfig.setOption(t, o, depLibs);
					}
				} catch (BuildException e) {
					e.printStackTrace();
				}
			}
		}
		
		if (cur_cfgs.length > 0)
			ManagedBuildManager.saveBuildInfo(project, false);
	}

	protected void doTemplatesPostProcess(IProject prj) {

		if (entryInfo == null)
			return;

		Template template = entryInfo.getInitializedTemplate(getStartingPage(),
				getConfigPage(), getMainPageData());

		if (template == null)
			return;

		// save the type by template 
		MStudioProject mprj = new MStudioProject(prj);
		String tempName = template.getTemplateId();
		boolean isMgProject = false;
		MStudioProjectTemplateType MgType = MStudioProjectTemplateType.exe;
		String[] pkgs = mprj.getDepPkgs();
		
		for (int i = 0; i < pkgs.length; i++){
			if (pkgs[i].equals("minigui")){
				isMgProject = true;
				break;
			}
		}
		
		if (TEMPLATE_TYPE_EXE.equals(tempName)){
			MgType = MStudioProjectTemplateType.exe;
		} else if (TEMPLATE_TYPE_LIB.equals(tempName)){
			MgType =  MStudioProjectTemplateType.normallib;
		} else if (TEMPLATE_TYPE_MGINIT.equals(tempName)){
			MgType =  MStudioProjectTemplateType.mginitmodule;
		} else if (TEMPLATE_TYPE_IAL.equals(tempName)){
			MgType =  MStudioProjectTemplateType.dlcustom;
		} 

		mprj.initProjectTypeInfo(isMgProject, MgType);

		List<IConfiguration> configs = new ArrayList<IConfiguration>();
		for (int i = 0; i < cfgs.length; i++) {
			configs.add((IConfiguration) cfgs[i].getConfiguration());
		}

		template.getTemplateInfo().setConfigurations(configs);

		IStatus[] statuses = template.executeTemplateProcesses(null, false);
		if (statuses.length == 1
				&& statuses[0].getException() instanceof ProcessFailureException) {
			TemplateEngineUIUtil.showError(statuses[0].getMessage(), statuses[0].getException());
		}
	}

	protected MStudioNewCAppSoCConfigWizardPage getConfigPage() {
		if (fConfigPage == null) {
			fConfigPage = new MStudioNewCAppSoCConfigWizardPage(this);
		}

		return fConfigPage;
	}

	public IWizardPage getSpecificPage() {
		return entryInfo.getNextPage(getStartingPage(), getConfigPage());
	}

	/**
	 * Mark preferred toolchains with specific images
	 * @
	 */

	public void updatePreferred(List<String> prefs) {
	}

	public List<String> getPreferredTCNames() {
		return preferredTCs;
	}

	public String getHeader() {
		return head;
	}

	public boolean isDummy() {
		return false;
	}

	public boolean supportsPreferred() {
		return true;
	}

	public boolean isChanged() {

		if (savedToolChains == null)
			return true;

		IToolChain[] tcs = getSelectedToolChains();
		if (savedToolChains.length != tcs.length)
			return true;

		for (int i = 0; i < savedToolChains.length; i++) {
			boolean found = false;
			for (int j = 0; j < tcs.length; j++) {
				if (savedToolChains[i] == tcs[j]) {
					found = true;
					break;
				}
			}
			if (!found)
				return true;
		}

		return false;
	}

	public void saveState() {
		savedToolChains = getSelectedToolChains();
	}

	public IToolChain[] getSelectedToolChains() {

		if (entryInfo != null) {
			int i = 0;
			Set <String> tcString = entryInfo.tc_filter();
			IToolChain[] ts = new IToolChain[tcString.size()];

			for (String s : tcString) {
				Object obj = full_tcs.get(s);
				if (obj instanceof IToolChain) {
					ts[i++] = (IToolChain) obj;
				}
			}

			return ts;
		}

		return new IToolChain[0];
	}

	public int getToolChainsCount() {
		if (entryInfo == null)
			return full_tcs.size();
		else
			return entryInfo.tc_filter().size();
	}

	public String getPropertyId() {
		return propertyId;
	}

	public IProjectType getProjectType() {
		return pt;
	}

	public IWizard getWizard() {
		return wizard;
	}

	public CfgHolder[] getCfgItems(boolean defaults) {
		getConfigPage(); // ensure that page is created
		return fConfigPage.getCfgItems(defaults);
	}

	public String getErrorMessage() {
		return null;
	}

	protected void doCustom(IProject newProject) {

		IRunnableWithProgress[] operations = MBSCustomPageManager.getOperations();

		if (operations != null){
			for (int k = 0; k < operations.length; k++){
				try {
					wizard.getContainer().run(false, true, operations[k]);
				} catch (InvocationTargetException e) {
					ManagedBuilderUIPlugin.log(e);
				} catch (InterruptedException e) {
					ManagedBuilderUIPlugin.log(e);
				}
			}
		}
	}

	public void postProcess(IProject newProject, boolean created) {
		deleteExtraConfigs(newProject);
		// calls are required only if the project was
		// created before for <Advanced Settings> feature.
		if (created) {
			doTemplatesPostProcess(newProject);
			doCustom(newProject);
		}
	}

	/**
	 * Deletes configurations
	 * 
	 * @param newProject
	 *            - affected project
	 */
	private void deleteExtraConfigs(IProject newProject) {

		if (isChanged())
			return; // no need to delete
		if (listener != null && listener.isCurrent())
			return; // nothing to delete
		if (fConfigPage == null || !fConfigPage.pagesLoaded)
			return;

		ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(newProject, true);
		if (prjd == null)
			return;

		ICConfigurationDescription[] all = prjd.getConfigurations();
		if (all == null)
			return;

		CfgHolder[] req = getCfgItems(false);
		boolean modified = false;

		for (int i = 0; i < all.length; i++) {
			boolean found = false;
			for (int j = 0; j < req.length; j++) {
				if (all[i].getName().equals(req[j].getName())) {
					found = true;
					break;
				}
			}
			if (!found) {
				modified = true;
				prjd.removeConfiguration(all[i]);
			}
		}

		if (modified) {
			try {
				CoreModel.getDefault().setProjectDescription(newProject, prjd);
			} catch (CoreException e) {
				System.out.println("CoreModel.getDefault().setProjectDescription(newProject, prjd);");
			}
		}
	}

	public boolean isApplicable(EntryDescriptor data) {
		EntryInfo info = new EntryInfo(data, full_tcs);
		return info.isValid() && (info.getToolChainsCount() > 0);
	}

	public void initialize(EntryDescriptor data) throws CoreException {

		EntryInfo info = new EntryInfo(data, full_tcs);

		if (!info.isValid()) {
			throw new CoreException(new Status(IStatus.ERROR,
					ManagedBuilderUIPlugin.getUniqueIdentifier(), "inappropriate descriptor")); //$NON-NLS-1$
		}

		entryInfo = info;
	}

	/**
	 * Clones itself.
	 */
	public Object clone() {

		MStudioWizardHandler clone = (MStudioWizardHandler) super.clone();

		if (clone != null) {
			clone.propertyId = propertyId;
			clone.pt = pt;
			clone.listener = listener;
			clone.wizard = wizard;
			clone.entryInfo = entryInfo; // the same !
			clone.fConfigPage = fConfigPage; // the same !
			clone.full_tcs = full_tcs; // the same !
		}

		return clone;
	}

	public boolean canFinish() {

		if (entryInfo == null)
			return false;

		if (!getConfigPage().isCustomPageComplete())
			return false;

		if (!entryInfo.canFinish(startingPage, getConfigPage()))
			return false;

		if (customPages != null)
			for (int i = 0; i < customPages.length; i++)
				if (!customPages[i].isPageComplete())
					return false;

		return super.canFinish();
	}

	public boolean canCreateWithoutToolchain() {
		return true;
	}

	public String[] getCreateDevPackage() {
		if (null != fConfigPage)
			return fConfigPage.getSelectedPackages();
		else 
			return new String[0];
	}
	
	public boolean doCancel(){
		if (null != fConfigPage){
			return fConfigPage.doCancel();
		}
		return true;
	}
}

