package org.minigui.eclipse.cdt.mstudio.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildProperty;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

import org.minigui.eclipse.cdt.mstudio.MiniGUIMessages;

public class MGWizardHandler extends CWizardHandler {
	public static final String ARTIFACT = "org.eclipse.cdt.build.core.buildArtefactType";
	public static final String EMPTY_STR = "";

	private static final String PROPERTY = "org.eclipse.cdt.build.core.buildType";
	private static final String PROP_VAL = PROPERTY + ".debug";
	private static final String tooltip = MiniGUIMessages
			.getString("MGWizardHandler.1")
			+ MiniGUIMessages.getString("MGWizardHandler.2")
			+ MiniGUIMessages.getString("MGWizardHandler.3")
			+ MiniGUIMessages.getString("MGWizardHandler.4")
			+ MiniGUIMessages.getString("MGWizardHandler.5");

	protected SortedMap<String, IToolChain> full_tcs = new TreeMap<String, IToolChain>();
	private String propertyId = null;
	private IProjectType pt = null;
	protected IWizardItemsListListener listener;
	protected MGConfigWizardPage fConfigPage;
	private IToolChain[] savedToolChains = null;
	private IWizard wizard;
	private IWizardPage startingPage;
	private EntryInfo entryInfo;
	protected CfgHolder[] cfgs = null;
	protected IWizardPage[] customPages;
	private List<String> preferredTCs = new ArrayList<String>();

	protected static final class EntryInfo {
		private SortedMap<String, IToolChain> tcs;
		private EntryDescriptor entryDescriptor;
		private Template template;
		private boolean initialized;
		private boolean isValid;
		private String templateId;
		private IWizardPage[] templatePages;
		private IWizardPage predatingPage;
		private IWizardPage followingPage;

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
					Template templates[] = TemplateEngineUI.getDefault()
							.getTemplates();
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
						valueStore.putAll(((IWizardDataPage) page)
								.getPageData());
				}
				if (map != null) {
					valueStore.putAll(map);
				}
			}
			return template;
		}

		public IWizardPage getNextPage(IWizardPage predatingPage,
				IWizardPage followingPage) {
			initialize();
			if (this.templatePages == null
					|| this.predatingPage != predatingPage
					|| this.followingPage != followingPage) {
				this.predatingPage = predatingPage;
				this.followingPage = followingPage;
				if (template != null) {
					this.templatePages = template.getTemplateWizardPages(
							predatingPage, followingPage, predatingPage
									.getWizard());
				} else {
					templatePages = new IWizardPage[0];
					followingPage.setPreviousPage(predatingPage);
				}
			}

			if (templatePages.length != 0)
				return templatePages[0];
			return followingPage;
		}

		private boolean canFinish(IWizardPage predatingPage,
				IWizardPage followingPage) {
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
			for (String s : full)
				if (isToolChainAcceptable(s))
					out.add(s);
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

	public MGWizardHandler(Composite p, IWizard w) {
		this("MiniGUI Project", p, w);
	}

	public MGWizardHandler(IProjectType _pt, Composite p, IWizard w) {
		super(p, MiniGUIMessages.getString("MGWizardHandler.0"), _pt.getName());
		pt = _pt;
		setWizard(w);
	}

	public MGWizardHandler(String name, Composite p, IWizard w) {
		super(p, MiniGUIMessages.getString("MGWizardHandler.0"), name);
		setWizard(w);
	}

	public MGWizardHandler(IBuildPropertyValue val, Composite p, IWizard w) {
		super(p, MiniGUIMessages.getString("MGWizardHandler.0"), val.getName());
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
		if (table == null) {
			table = new Table(parent, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
			table.getAccessible().addAccessibleListener(
					new AccessibleAdapter() {
						public void getName(AccessibleEvent e) {
							if (e.result == null)
								e.result = head;
						}
					});
			table.setToolTipText(tooltip);
			if (entryInfo != null) {
				int counter = 0;
				int position = 0;
				for (String s : entryInfo.tc_filter()) {
					TableItem ti = new TableItem(table, SWT.NONE);
					Object obj = full_tcs.get(s);
					String id = CDTPrefUtil.NULL;
					if (obj instanceof IToolChain) {
						IToolChain tc = (IToolChain) obj;
						String name = tc.getUniqueRealName();
						id = tc.getId();
						ti.setText(name);
						ti.setData(tc);
					} else { // NULL for -NO TOOLCHAIN-
						ti.setText(s);
					}
					if (position == 0 && preferred.contains(id))
						position = counter;
					counter++;
				}
				if (counter > 0)
					table.select(position);
			}
			table.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					handleToolChainSelection();
				}
			});
		}
		updatePreferred(preferred);
		loadCustomPages();
		table.setVisible(true);
		parent.layout();
		if (fConfigPage != null)
			fConfigPage.pagesLoaded = false;
	}

	private void handleToolChainSelection() {
		loadCustomPages();
		// Notify listener, if any.
		if (listener != null)
			listener.toolChainListChanged(table.getSelectionCount());
	}

	private void loadCustomPages() {
		if (!(getWizard() instanceof NewMiniGUIAppWizard))
			return; // not probable

		NewMiniGUIAppWizard wz = (NewMiniGUIAppWizard) getWizard();

		if (customPages == null) {
			MBSCustomPageManager.init();
			MBSCustomPageManager.addStockPage(getStartingPage(), NewMiniGUIAppWizardPage.PAGE_ID);
			MBSCustomPageManager.addStockPage(getConfigPage(), MGConfigWizardPage.PAGE_ID);

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

	private void setCustomPagesFilter(NewMiniGUIAppWizard wz) {
		String[] natures = wz.getNatures();
		if (natures == null || natures.length == 0)
			MBSCustomPageManager.addPageProperty(MBSCustomPageManager.PAGE_ID,
					MBSCustomPageManager.NATURE, null);
		else if (natures.length == 1)
			MBSCustomPageManager.addPageProperty(MBSCustomPageManager.PAGE_ID,
					MBSCustomPageManager.NATURE, natures[0]);
		else {
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
			MBSCustomPageManager
					.addPageProperty(MBSCustomPageManager.PAGE_ID,
							MBSCustomPageManager.PROJECT_TYPE, getProjectType()
									.getId());

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
			if (y.size() > 0)
				MBSCustomPageManager.addPageProperty(MBSCustomPageManager.PAGE_ID,
						MBSCustomPageManager.PROJECT_TYPE, y);
			else
				MBSCustomPageManager.addPageProperty(MBSCustomPageManager.PAGE_ID,
						MBSCustomPageManager.PROJECT_TYPE, null);
		}
	}

	public void handleUnSelection() {
		if (table != null) {
			table.setVisible(false);
		}
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
			cfgs = MGConfigWizardPage.getDefaultCfgs(this);

		if (cfgs == null || cfgs.length == 0	|| cfgs[0].getConfiguration() == null) {
			throw new CoreException(new Status(IStatus.ERROR,
					ManagedBuilderUIPlugin.getUniqueIdentifier(),
					MiniGUIMessages.getString("MGWizardHandler.6"))); 
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
		doCustom(project);
	}

	protected void doTemplatesPostProcess(IProject prj) {
		if (entryInfo == null)
			return;

		Template template = entryInfo.getInitializedTemplate(getStartingPage(),
				getConfigPage(), getMainPageData());

		if (template == null)
			return;

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

	protected MGConfigWizardPage getConfigPage() {
		if (fConfigPage == null) {
			fConfigPage = new MGConfigWizardPage(this);
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
		preferredTCs.clear();
		int x = table.getItemCount();
		for (int i = 0; i < x; i++) {
			TableItem ti = table.getItem(i);
			IToolChain tc = (IToolChain) ti.getData();
			String id = (tc == null) ? CDTPrefUtil.NULL : tc.getId();
			if (prefs.contains(id)) {
				ti.setImage(IMG1);
				preferredTCs.add(tc.getName());
			} else
				ti.setImage(IMG0);
		}
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
		if (full_tcs.size() == 0 || table.getSelection().length == 0)
			return new IToolChain[] { null };
		TableItem[] tis = table.getSelection();
		if (tis == null || tis.length == 0)
			return new IToolChain[0];
		IToolChain[] ts = new IToolChain[tis.length];
		for (int i = 0; i < tis.length; i++) {
			ts[i] = (IToolChain) tis[i].getData();
		}
		return ts;
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
		TableItem[] tis = table.getSelection();
		if (tis == null || tis.length == 0)
			return MiniGUIMessages.getString("MGWizardHandler.7"); //$NON-NLS-1$
		return null;
	}

	protected void doCustom(IProject newProject) {
		IRunnableWithProgress[] operations = MBSCustomPageManager
				.getOperations();
		if (operations != null)
			for (int k = 0; k < operations.length; k++)
				try {
					wizard.getContainer().run(false, true, operations[k]);
				} catch (InvocationTargetException e) {
					ManagedBuilderUIPlugin.log(e);
				} catch (InterruptedException e) {
					ManagedBuilderUIPlugin.log(e);
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

		ICProjectDescription prjd = CoreModel.getDefault()
				.getProjectDescription(newProject, true);
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
		if (modified)
			try {
				CoreModel.getDefault().setProjectDescription(newProject, prjd);
			} catch (CoreException e) {
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
					ManagedBuilderUIPlugin.getUniqueIdentifier(),
					"inappropriate descriptor")); //$NON-NLS-1$
		}
		entryInfo = info;
	}

	/**
	 * Clones itself.
	 */
	public Object clone() {
		MGWizardHandler clone = (MGWizardHandler) super.clone();
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

}