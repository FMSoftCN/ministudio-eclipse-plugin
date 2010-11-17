/*********************************************************************
 * Copyright (C) 2005 - 2010, Beijing FMSoft Technology Co., Ltd.
 * Room 902, Floor 9, Taixing, No.11, Huayuan East Road, Haidian
 * District, Beijing, P. R. CHINA 100191.
 * All rights reserved.
 *
 * This software is the confidentail and proprietary information of
 * Beijing FMSoft Technology Co., Ltd. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance you entered into with FMSoft.
 *
 *			http://www.minigui.com
 *
 *********************************************************************/

package org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
// import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.dialogs.PreferencesUtil;

import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
// import org.eclipse.cdt.managedbuilder.ui.properties.ManagedBuilderUIImages;
import org.eclipse.cdt.managedbuilder.ui.wizards.CfgHolder;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.ui.newui.CDTPrefUtil;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.preferences.MStudioPreferenceConstants;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;


public class MStudioNewCAppSoCConfigWizardPage extends WizardPage {

	public static final String PAGE_ID = "org.eclipse.cdt.fmsoft.hybridos.mstudio.wizard.MGConfigWizardPage";

	// private static final Image IMG = ManagedBuilderUIImages.get(ManagedBuilderUIImages.IMG_BUILD_CONFIG);
	private static final String TITLE = MStudioMessages.getString("MStudioNewCAppSoCConfigWizardPage.0");
	private static final String MESSAGE = MStudioMessages.getString("MStudioNewCAppSoCConfigWizardPage.1");
	private static final String COMMENT = MStudioMessages.getString("MStudioNewCAppSoCConfigWizardPage.12");
	private static final String EMPTY_STR = "";

	private Table table = null;
	private CheckboxTableViewer ctv = null;
	private Label packageDesc = null;
	private Composite msSocParent = null;
	private String propertyId = null;
	private String errorMessage = null;
	private String message = MESSAGE;
	public boolean isVisible = false;
	private MStudioWizardHandler handler = null;
	public boolean pagesLoaded = false;
	private IToolChain[] visitedTCs = null;
	private MStudioEnvInfo msEnvInfo = MStudioPlugin.getDefault().getMStudioEnvInfo();
	IWizardPage[] customPages = null;
	private String socName = MStudioEnvInfo.getCurSoCName();
	private Button buttonCheck = null;

	protected static final class PackageItem {
		String name = null;
		String description = null;

		public PackageItem(String name, String desc) {
			this.name = name;
			this.description = desc;
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}
	}

	public MStudioNewCAppSoCConfigWizardPage(MStudioWizardHandler wh) {
		super(TITLE);
		setPageComplete(false);
		handler = wh;
		setWizard(wh.getWizard());
	}

	public CfgHolder[] getCfgItems(boolean getDefault) {
		return getDefaultCfgs(handler);
	}

	public void createControl(Composite parent) {
		msSocParent = new Composite(parent, SWT.NONE);
		msSocParent.setFont(msSocParent.getFont());
		msSocParent.setLayout(new GridLayout());
		msSocParent.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite cmpstSocType = new Composite(msSocParent, SWT.NONE);
		cmpstSocType.setLayout(new GridLayout(2, true));
		cmpstSocType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		setupLabel(cmpstSocType, MStudioMessages.getString("MStudioNewCAppSoCConfigWizardPage.4"),
				GridData.BEGINNING);

		String[] socType = MStudioPlugin.getDefault().getMStudioEnvInfo().getSoCPaths();
		final Combo combo = new Combo(cmpstSocType, SWT.READ_ONLY);
		combo.setItems(socType);
		if (null != socName) {
			combo.setText(socName);
			combo.setEnabled(false);
		} else {
			combo.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					socName = combo.getText();
					IPreferenceStore store = MStudioPlugin.getDefault().getPreferenceStore();
					store.putValue(MStudioPreferenceConstants.MSTUDIO_SOC_NAME, socName);
					msEnvInfo.updateSoCName();
					setCheckboxTableViewerData();
					setPageComplete(isCustomPageComplete());
					update();
				}
			});
		}
		setupLabel(cmpstSocType, EMPTY_STR, GridData.BEGINNING);

		Composite cLabel = new Composite(msSocParent, SWT.NONE);
		cLabel.setLayout(new GridLayout());
		cLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		setupLabel(cLabel,
				MStudioMessages.getString("MStudioNewCAppSoCConfigWizardPage.5"),
				GridData.BEGINNING);
		setupLabel(cLabel,
				MStudioMessages.getString("MStudioNewCAppSoCConfigWizardPage.6"),
				GridData.BEGINNING);

		Composite cmpstPkgDesc = new Composite(msSocParent, SWT.NONE);
		GridLayout gl = new GridLayout(2, true);
		cmpstPkgDesc.setLayout(gl);
		cmpstPkgDesc.setLayoutData(new GridData(GridData.FILL_BOTH));

		table = new Table(cmpstPkgDesc, SWT.BORDER | SWT.CHECK | SWT.V_SCROLL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(gd);

		packageDesc = new Label(cmpstPkgDesc, SWT.WRAP);
		packageDesc.setText(EMPTY_STR);
		GridData gdx = new GridData(GridData.FILL_BOTH);
		gdx.verticalAlignment = SWT.TOP;
		packageDesc.setLayoutData(gdx);

		ctv = new CheckboxTableViewer(table);
		ctv.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return (Object[]) inputElement;
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		ctv.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				return element == null ? EMPTY_STR : ((PackageItem)element).getName();
			}

			// public Image getImage(Object element) {
			// 	return IMG;
			// }
		});
		ctv.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (buttonCheck.getSelection())
					buttonCheck.setSelection(false);
				setPageComplete(isCustomPageComplete());
				update();
			}
		});
		ctv.addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent event) {
				Object obj = ((IStructuredSelection)(ctv.getSelection())).getFirstElement();
				PackageItem pck = (PackageItem) obj;
				if (pck != null && pck.getDescription() != null)
					packageDesc.setText(pck.getDescription());
			}
		});

		setCheckboxTableViewerData();

		buttonCheck = new Button(cmpstPkgDesc, SWT.CHECK);
		buttonCheck.setText(MStudioMessages.getString("MStudioNewCAppSoCConfigWizardPage.7"));
		buttonCheck.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		buttonCheck.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (buttonCheck.getSelection())
					ctv.setAllChecked(true);
				else
					ctv.setAllChecked(false);
				setPageComplete(isCustomPageComplete());
				update();
			}
		});

		setControl(msSocParent);
	}

	private void setCheckboxTableViewerData() {
		List<PackageItem> pkgs = new ArrayList<PackageItem>();

		for (Map.Entry<String, String> info : msEnvInfo.getAllSoftPkgs().entrySet()) {
			pkgs.add(new PackageItem (info.getKey(), info.getValue()));
		}

		ctv.setInput(pkgs.toArray());
	}

	/**
	 *
	 * @param handler
	 * @return
	 */
	static public CfgHolder[] getDefaultCfgs(MStudioWizardHandler handler) {
		String id = handler.getPropertyId();
		IProjectType pt = handler.getProjectType();
		ArrayList<CfgHolder> out = new ArrayList<CfgHolder>();

		for (IToolChain tc : handler.getSelectedToolChains()) {
			CfgHolder[] cfgs = null;
			if (id != null)
				cfgs = CfgHolder.cfgs2items(ManagedBuildManager
						.getExtensionConfigurations(tc, MStudioWizardHandler.ARTIFACT, id));
			else if (pt != null)
				cfgs = CfgHolder.cfgs2items(ManagedBuildManager
						.getExtensionConfigurations(tc, pt));
			else { // Create default configuration for StdProject
				cfgs = new CfgHolder[1];
				cfgs[0] = new CfgHolder(tc, null);
			}
			if (cfgs == null)
				return null;

			for (int j = 0; j < cfgs.length; j++) {
				if (cfgs[j].isSystem() || (handler.supportedOnly() && !cfgs[j].isSupported()))
					continue;
				out.add(cfgs[j]);
			}
		}
		return out.toArray(new CfgHolder[out.size()]);
	}

	private boolean isVisited() {
		if (table == null || handler == null)
			return false;

		// return Arrays.equals(handler.getSelectedToolChains(), visitedTCs);
		return true;
	}

	public boolean isCustomPageComplete() {
		if (!isVisited())
			return true;

		if (socName == null) {
			errorMessage = MStudioMessages.getString("MStudioNewCAppSoCConfigWizardPage.8");
			message = errorMessage;
			return false;
		}
		if (table.getItemCount() == 0) {
			errorMessage = MStudioMessages.getString("MStudioNewCAppSoCConfigWizardPage.10");
			message = errorMessage;
			return false;
		}
		if (ctv.getCheckedElements().length == 0) {
			errorMessage = MStudioMessages.getString("MStudioNewCAppSoCConfigWizardPage.11");
			message = errorMessage;
			return false;
		}
		errorMessage = null;
		message = MESSAGE;
		return true;
	}

	public void setVisible(boolean visible) {
		msSocParent.setVisible(visible);
		isVisible = visible;
		if (visible && handler != null && !isVisited()) {
			setPageComplete(isCustomPageComplete());
		}
		if (visible) {
			msSocParent.getParent().layout(true, true);
			update();
		}
	}

	// ------------------------
	private Label setupLabel(Composite composite, String name, int mode) {
		Label label = new Label(composite, SWT.WRAP);
		label.setText(name);
		GridData gd = new GridData(mode);
		gd.verticalAlignment = SWT.TOP;
		label.setLayoutData(gd);
		Composite cmpst = label.getParent();
		label.setFont(cmpst.getFont());
		return label;
	}

	public String getName() {
		return TITLE;
	}

	public Control getControl() {
		return msSocParent;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getMessage() {
		return message;
	}

	public String getTitle() {
		return TITLE;
	}

	protected void update() {
		getWizard().getContainer().updateButtons();
		getWizard().getContainer().updateMessage();
		getWizard().getContainer().updateTitleBar();
	}

	/**
	 * Edit properties
	 */
	private void advancedDialog() {
		if (getWizard() instanceof MStudioNewCAppWizard) {
			MStudioNewCAppWizard nmWizard = (MStudioNewCAppWizard) getWizard();
			IProject newProject = nmWizard.getProject(true, false);
			if (newProject != null) {
				boolean oldManage = CDTPrefUtil.getBool(CDTPrefUtil.KEY_NOMNG);
				// disable manage configurations button
				CDTPrefUtil.setBool(CDTPrefUtil.KEY_NOMNG, true);
				try {
					int res = PreferencesUtil.createPropertyDialogOn(
							getWizard().getContainer().getShell(), newProject,
							propertyId, null, null).open();
					if (res != Window.OK) {
						// if user presses cancel, remove the project.
						nmWizard.performCancel();
					}
				} finally {
					CDTPrefUtil.setBool(CDTPrefUtil.KEY_NOMNG, oldManage);
				}
			}
		}
	}

	public IWizardPage getNextPage() {
		pagesLoaded = true;
		//return MBSCustomPageManager.getNextPage(PAGE_ID);
		return null;
	}
}
