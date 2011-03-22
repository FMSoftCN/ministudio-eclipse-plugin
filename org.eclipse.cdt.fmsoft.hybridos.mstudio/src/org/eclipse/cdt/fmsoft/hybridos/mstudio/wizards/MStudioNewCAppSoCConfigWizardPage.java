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
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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

import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.properties.ManagedBuilderUIImages;
import org.eclipse.cdt.managedbuilder.ui.wizards.CfgHolder;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo.PackageItem;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.preferences.MStudioSoCPreferencePage;


public class MStudioNewCAppSoCConfigWizardPage extends WizardPage {

	public final static String PAGE_ID = "org.eclipse.cdt.fmsoft.hybridos.mstudio.wizard.MGConfigWizardPage";

	private static final String MS_DEP_ICON = "icons/hybrid_devp.gif";
	private final static String TITLE = MStudioMessages.getString("MStudioNewCAppSoCConfigWizardPage.0");
	private final static String MESSAGE = MStudioMessages.getString("MStudioNewCAppSoCConfigWizardPage.1");
	private final static String SHOW_SELECT_ALL = MStudioMessages.getString("MStudioNewCAppSoCConfigWizardPage.14");
	private final static String SHOW_CANCEL_ALL = MStudioMessages.getString("MStudioNewCAppSoCConfigWizardPage.15");
	private final static String AFFECTED = MStudioMessages.getString("MStudioNewCAppSoCConfigWizardPage.affected");
	private final static String DEPEND = MStudioMessages.getString("MStudioNewCAppSoCConfigWizardPage.depend");
	private final static String EMPTY_STR = "";
	private final static String MSW_SPACE = " ";
	
	private static final Image treeIcon = MStudioPlugin.getImageDescriptor(MS_DEP_ICON).createImage();

	public boolean isVisible = false;
	public boolean pagesLoaded = false;

	private List<PackageItem> pkgs = new ArrayList<PackageItem>();
	private List<String> selectedPackages = new ArrayList<String>();

	private String[] defaultDepPkg = null;

	private Label packageDesc = null;
	private Button buttonCheck = null;
	private Composite msSocParent = null;
	private CheckboxTableViewer ctv = null;
	private boolean ctvHasInitialized = false;
	private boolean socCancelOnlyOne = false;
	private boolean isDispkayPackageDesc = false;
	private String errorMessage = null;
	private String message = MESSAGE;

	private MStudioWizardHandler handler = null;
	private MStudioEnvInfo msEnvInfo = MStudioPlugin.getDefault().getMStudioEnvInfo();
	private String socName = null;

	private class PakckageSorter extends ViewerSorter {
		 public int compare(Viewer viewer, Object e1, Object e2) {
			 String n1 = ((PackageItem)e1).getName();
			 String n2 = ((PackageItem)e2).getName();
			 return n1.compareTo(n2);
		 }
	}

	public MStudioNewCAppSoCConfigWizardPage(MStudioWizardHandler wh) {
		super(TITLE);
		setPageComplete(false);
		handler = wh;
		setWizard(wh.getWizard());
		socName = msEnvInfo.getCurSoCName();
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
		cmpstSocType.setLayout(new GridLayout(2, false));
		cmpstSocType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(cmpstSocType, SWT.NULL);
		label.setText(MStudioMessages.getString("MStudioNewCAppSoCConfigWizardPage.4"));

		String[] socType = msEnvInfo.getSoCPaths();
		final Combo combo = new Combo(cmpstSocType, SWT.READ_ONLY);
		combo.setItems(socType);
		isDispkayPackageDesc = false;
		if (null != socName && !socName.equals("null")) {
			socCancelOnlyOne = false;
			isDispkayPackageDesc = true;
			combo.setText(socName);
			combo.setEnabled(false);
		} else {
			combo.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					socName = combo.getText();
					MStudioSoCPreferencePage.setCurrentSoC(socName);
					socCancelOnlyOne = true;
					msEnvInfo.updateSoCName();
					msEnvInfo.setDefaultDepPackages(msEnvInfo.getProjectTypeName());
					getDefaultLibsName();
					setCheckboxTableViewerData();
					packageDesc.setText(MStudioMessages.getString(
							"MStudioNewCAppSoCConfigWizardPage.keyPackages"));
					setPageComplete(isCustomPageComplete());
					update();
				}
			});
		}
		//setupLabel(cmpstSocType, EMPTY_STR, GridData.BEGINNING);

		Composite cLabel = new Composite(msSocParent, SWT.NONE);
		cLabel.setLayout(new GridLayout());
		cLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		setupLabel(cLabel,
				MStudioMessages.getString("MStudioNewCAppSoCConfigWizardPage.5"),
				GridData.BEGINNING);
//		setupLabel(cLabel,
//				MStudioMessages.getString("MStudioNewCAppSoCConfigWizardPage.6"),
//				GridData.BEGINNING);

		Composite cmpstPkgDesc = new Composite(msSocParent, SWT.NONE);
		cmpstPkgDesc.setLayout(new GridLayout(2, true));
		cmpstPkgDesc.setLayoutData(new GridData(GridData.FILL_BOTH));

		Table table = new Table(cmpstPkgDesc, SWT.BORDER | SWT.CHECK | SWT.V_SCROLL);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

		packageDesc = new Label(cmpstPkgDesc, SWT.WRAP | SWT.BORDER);
		packageDesc.setLayoutData(new GridData(GridData.FILL_BOTH));
		if (isDispkayPackageDesc)
			packageDesc.setText(MStudioMessages.getString(
					"MStudioNewCAppSoCConfigWizardPage.keyPackages"));

		ctv = new CheckboxTableViewer(table);
		ctv.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return (Object[])inputElement;
			}
			public void dispose() { }
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { }
		});
		ctv.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				return element == null ? EMPTY_STR : ((PackageItem)element).getName();
			}

			public Image getImage(Object element) {
				return treeIcon;
			}
		});
		ctv.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				PackageItem itm = (PackageItem) event.getElement();

				if (isDefaultDepPkg(itm.getName())) {
					ctv.setChecked(itm, true);
					return;
				}

				if (buttonCheck.getSelection())
					buttonCheck.setSelection(false);

				if (!event.getChecked()) {
					setAffectedPkgsChecked(itm.getName());
				} else {
					setDependPkgsChecked(itm.getName());
				}

				setPageComplete(isCustomPageComplete());
				update();
			}
		});
		ctv.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object obj = ((IStructuredSelection)(ctv.getSelection())).getFirstElement();
				PackageItem pck = (PackageItem)obj;

				if (isDefaultDepPkg(pck.getName()))
					return;

				if (pck != null && pck.getDescription() != null)
					packageDesc.setText(pck.getDescription());
			}
		});
		ctv.setSorter(new PakckageSorter());

		buttonCheck = new Button(cmpstPkgDesc, SWT.CHECK);
		buttonCheck.setText(MStudioMessages.getString("MStudioNewCAppSoCConfigWizardPage.7"));
		buttonCheck.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		buttonCheck.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (buttonCheck.getSelection()) {
					ctv.setAllChecked(true);
					addAllselectedPackages();
					packageDesc.setText(MStudioMessages.getString("MStudioNewCAppSoCConfigWizardPage.allPackages"));
				} else {
					ctv.setAllChecked(false);
					selectedPackages.clear();
					setDefaultDepPkg();
					packageDesc.setText(MStudioMessages.getString("MStudioNewCAppSoCConfigWizardPage.keyPackages"));
				}
				setPageComplete(isCustomPageComplete());
				update();
			}
		});

		setControl(msSocParent);
	}

	static public CfgHolder[] getDefaultCfgs(MStudioWizardHandler handler) {

		String id = handler.getPropertyId();
		IProjectType pt = handler.getProjectType();
		ArrayList<CfgHolder> out = new ArrayList<CfgHolder>();

		for (IToolChain tc : handler.getSelectedToolChains()) {
			CfgHolder[] cfgs = null;
			if (id != null) {
				cfgs = CfgHolder.cfgs2items(ManagedBuildManager
						.getExtensionConfigurations(tc, MStudioWizardHandler.ARTIFACT, id));
			} else if (pt != null) {
				cfgs = CfgHolder.cfgs2items(ManagedBuildManager
						.getExtensionConfigurations(tc, pt));
			} else { // Create default configuration for StdProject
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

	public boolean isCustomPageComplete() {
		if (socName == null) {
			errorMessage = MStudioMessages.getString("MStudioNewCAppSoCConfigWizardPage.8");
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

		if (visible && handler != null) {
			setPageComplete(isCustomPageComplete());
		}

		if (visible) {
			msSocParent.getParent().layout(true, true);
			getDefaultLibsName();
			if (!ctvHasInitialized) {
				setCheckboxTableViewerData();
				ctvHasInitialized = true;
			} else {
				setDefaultDepPkg();
			}
			update();
		}
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

	public IWizardPage getNextPage() {
		pagesLoaded = true;
		return null;
	}

	public String[] getSelectedPackages() {
		return selectedPackages.toArray(new String[selectedPackages.size()]);
	}

	protected void update() {
		getWizard().getContainer().updateButtons();
		getWizard().getContainer().updateMessage();
		getWizard().getContainer().updateTitleBar();
	}

	private boolean dailogPkgsChecked(String title, String pkgName, List<String> listPkgs) {

		int count = listPkgs.size();
		if (count <= 0)
			return false;

		String messageInfo = new String(pkgName);
		messageInfo = messageInfo.concat(MSW_SPACE + title);

		for (int i = 0; i < count; i++) {
			messageInfo = messageInfo.concat(MSW_SPACE + listPkgs.get(i));
		}

		String showSCAll = null;

		if (title.equals(DEPEND))
			showSCAll = SHOW_SELECT_ALL;
		else
			showSCAll = SHOW_CANCEL_ALL;

		messageInfo = messageInfo.concat(showSCAll);
		MessageDialog.openInformation(getShell(), title, messageInfo);

		return true;
	}

	private void setAffectedPkgsChecked(String affectedPkgs) {

		for (Map.Entry<String, List<String>> info : msEnvInfo.getAffectedPkgs().entrySet()) {
			if (affectedPkgs.equals(info.getKey())) {
				List<String> affected = new ArrayList<String>(info.getValue());
				getAffectedPkgsChecked(affectedPkgs, affected);
				containsCheckedAffectedElements(affected);

				if (!dailogPkgsChecked(AFFECTED, affectedPkgs, affected))
					return;

				for (int i = 0; i < affected.size(); i++) {
					String affString = affected.get(i);
					PackageItem pItem = getPackedItem(affString);
					if (null == pItem)
						break;
					ctv.setChecked(pItem, false);
					selectedPackages.remove(affString);
				}
			}
		}
		selectedPackages.remove(affectedPkgs);
	}

	private void containsCheckedAffectedElements(List<String> affected) {
		if (null == defaultDepPkg || 0 == defaultDepPkg.length)
			return;

		Object[] elements = msEnvInfo.getAllSoftPkgs().keySet().toArray();
		Object[] obj = ctv.getCheckedElements();
		List<String> list = new ArrayList<String>();

		list.clear();
		for (int i = 0; i < elements.length; i++) {
			list.add(elements[i].toString());
		}
		for (int i = 0; i < obj.length; i++) {
			PackageItem pItem = (PackageItem)obj[i];
			String name = pItem.getName();
			if (list.contains(name))
				list.remove(name);
		}

		for (int j = 0; j < list.size(); j++) {
			String elem = list.get(j);
			for (int i = 0; i < affected.size(); i++) {
				String pkgName = affected.get(i);
				if (elem.equals(pkgName))
					affected.remove(pkgName);
			}
		}
	}

	private void getAffectedPkgsChecked(String affName, List<String> affected) {

		for (Map.Entry<String, List<String>> info : msEnvInfo.getAffectedPkgs().entrySet()) {
			if (affName.equals(info.getKey())) {
				List<String> affectedList = info.getValue();

				for (int i = 0; i < affectedList.size(); i++) {
					String affString = affectedList.get(i);
					getAffectedPkgsChecked(affString, affected);
					if (affected.contains(affString))
						continue;
					affected.add(affString);
				}
			}
		}
	}

	private void setDependPkgsChecked(String depPkgs) {

		for (Map.Entry<String, List<String>> info : msEnvInfo.getDepPkgs().entrySet()) {
			if (depPkgs.equals(info.getKey())) {
				List<String> dep = new ArrayList<String>(info.getValue());
				getDependPkgsChecked(depPkgs, dep);
				containsCheckedDependElements(dep);

				if (!dailogPkgsChecked(DEPEND, depPkgs, dep))
					return;

				for (int i = 0; i < dep.size(); i++) {
					String depString = dep.get(i);
					PackageItem pItem = getPackedItem(depString);
					if (null == pItem)
						break;
					ctv.setChecked(pItem, true);
					if (selectedPackages.contains(depString))
						continue;
					selectedPackages.add(depString);
				}
			}
		}
		selectedPackages.add(depPkgs);
	}

	private void getDefaultLibsName() {
		String[] keylibs = msEnvInfo.getDefaultKeyLibrary();

		if (null != keylibs && 0 != keylibs.length) {
			String pkgsName = getDependPkgsName(keylibs[0]);
			if (null == pkgsName)
				return;

			List<String> defaultPkgs = new ArrayList<String>();
			defaultPkgs.add(pkgsName);
			getDependPkgsChecked(pkgsName, defaultPkgs);

			defaultDepPkg = defaultPkgs.toArray(new String[defaultPkgs.size()]);
			msEnvInfo.setDefaultDepPackages(defaultDepPkg);
		}
	}

	private String getDependPkgsName(String libName) {
		for (Map.Entry<String, List<String>> info : msEnvInfo.getDefaultLibs().entrySet()) {
			List<String> lib = info.getValue();

			for (int i = 0; i < lib.size(); i++) {
				String libString = lib.get(i);
				if (libString.equals(libName))
					return info.getKey();
			}
		}

		return null;
	}

	private void containsCheckedDependElements(List<String> depend) {
		if (null == defaultDepPkg || 0 == defaultDepPkg.length)
			return;

		Object[] obj = ctv.getCheckedElements();
		for (int j = 0; j < obj.length; j++) {
			PackageItem pItem = (PackageItem)obj[j];
			String element = pItem.getName();
			for (int i = 0; i < depend.size(); i++) {
				String pkgName = depend.get(i);
				if (element.equals(pkgName))
					depend.remove(pkgName);
			}
		}
	}

	private void getDependPkgsChecked(String depName, List<String> depend) {

		for (Map.Entry<String, List<String>> info : msEnvInfo.getDepPkgs().entrySet()) {
			if (depName.equals(info.getKey())) {
				List<String> dep = info.getValue();

				for (int i = 0; i < dep.size(); i++) {
					String depString = dep.get(i);
					getDependPkgsChecked(depString, depend);
					if (depend.contains(depString))
						continue;
					depend.add(depString);
				}
			}
		}
	}

	private PackageItem getPackedItem(String findString) {

		for (int i = 0; i < pkgs.size(); i++) {
			PackageItem pItem = pkgs.get(i);
			if (findString.equals(pItem.getName()))
				return pItem;
		}

		return null;
	}

	private void setCheckboxTableViewerData() {
		pkgs.clear();
		for (Map.Entry<String, String> info : msEnvInfo.getAllSoftPkgs().entrySet()) {
			pkgs.add(new PackageItem(info.getKey(), info.getValue()));
		}
		ctv.remove(pkgs.toArray());
		ctv.setInput(pkgs.toArray());
		setDefaultDepPkg();
	}

	private void setDefaultDepPkg() {
		if (null == defaultDepPkg || 0 == defaultDepPkg.length)
			return;

		ctv.setAllChecked(false);
		for (int i = 0; i < defaultDepPkg.length; i++) {
			PackageItem pItem = getPackedItem(defaultDepPkg[i]);
			if (null == pItem)
				break;
			ctv.setChecked(pItem, true);
			if (selectedPackages.contains(defaultDepPkg[i]))
				continue;
			selectedPackages.add(defaultDepPkg[i]);
		}
	}

	private boolean isDefaultDepPkg(String name) {
		if (null == defaultDepPkg || 0 == defaultDepPkg.length)
			return false;

		for (int i = 0; i < defaultDepPkg.length; i++) {
			if (name.equals(defaultDepPkg[i]))
				return true;
		}

		return false;
	}

	private void clearCheckboxTableViewerData() {
		msEnvInfo.clearAllSoftPkgs();
		ctv.remove(pkgs.toArray());
		pkgs.clear();
	}

	private void addAllselectedPackages() {
		selectedPackages.clear();
		for (int i = 0; i < pkgs.size(); i++) {
		 	selectedPackages.add(pkgs.get(i).getName());
		}
	}

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

	public boolean doCancel() {

		if (socCancelOnlyOne && socName != null) {
			MStudioSoCPreferencePage.setCurrentSoC("null");
			clearCheckboxTableViewerData();
			socName = null;
			ctv = null;
			msEnvInfo.setSoCNameNull();
		}

		return true;
	}
}

