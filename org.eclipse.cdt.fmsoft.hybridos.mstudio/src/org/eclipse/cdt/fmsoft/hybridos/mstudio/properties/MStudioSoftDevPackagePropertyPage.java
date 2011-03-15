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

package org.eclipse.cdt.fmsoft.hybridos.mstudio.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import org.eclipse.core.resources.IProject;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.properties.ManagedBuilderUIImages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo.PackageItem;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProject;


public class MStudioSoftDevPackagePropertyPage extends PropertyPage
	implements IWorkbenchPropertyPage {
	
	private static final String MS_DEP_ICON = "icons/hybrid_devp.gif";
	private static final String TITLE = MStudioMessages.getString("MStudioSoftDevPackagePropertyPage.titleLable");
	private static final String SHOW_SELECT =MStudioMessages.getString("MStudioSoftDevPackagePropertyPage.showSelect");
	private final static String SHOW_DESELECT =MStudioMessages.getString("MStudioSoftDevPackagePropertyPage.showDeselect");
	private static final String AFFECTED = MStudioMessages.getString("MStudioSoftDevPackagePropertyPage.affected");
	private static final String DEPEND = MStudioMessages.getString("MStudioSoftDevPackagePropertyPage.depend");
	private static final String MSP_SPACE = " ";
	private static final String EMPTY_STR = "";

	private static final Image treeIcon = MStudioPlugin.getImageDescriptor(MS_DEP_ICON).createImage();

	private Label title = null;
	private Label description = null;
	private Label tip = null;
	private Label contentDes = null;
	private Button selectAll = null;
	private Table table = null;
	private CheckboxTableViewer ctv = null;

	private List<PackageItem> pkgs = new ArrayList<PackageItem>();
	private MStudioEnvInfo msEnvInfo = MStudioPlugin.getDefault().getMStudioEnvInfo();
	private boolean hasInitializeTable = false;

	private class PakckageSorter extends ViewerSorter {
		 public int compare(Viewer viewer, Object e1, Object e2) {
			 String n1 = e1.toString();
			 String n2 = e2.toString();
			 return n1.compareTo(n2);
		 }
	}

	public MStudioSoftDevPackagePropertyPage() {
	}

	@Override
	protected Control createContents(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData());

		Composite composite1 = new Composite(composite, SWT.NONE);
		composite1.setLayout(new GridLayout());
		composite1.setLayoutData(new GridData());

		title = new Label(composite1, SWT.NONE);
		title.setFont(new Font(title.getFont().getDevice(), EMPTY_STR, 0, SWT.BOLD));
		title.setText(TITLE);

		description = new Label(composite1, SWT.NONE);
		description.setText(MStudioMessages.getString("MStudioSoftDevPackagePropertyPage.descriptionLable"));
		new Label(composite1, SWT.FULL_SELECTION|SWT.LINE_SOLID);
		tip = new Label(composite1, SWT.NONE);
		tip.setText(MStudioMessages.getString("MStudioSoftDevPackagePropertyPage.tipLable"));

		Composite composite2 = new Composite(composite, SWT.NONE);
		GridLayout gl = new GridLayout(2, false);
		gl.makeColumnsEqualWidth = true;
		composite2.setLayout(gl);
		composite2.setLayoutData(new GridData(GridData.FILL_BOTH));

		table = new Table(composite2, SWT.BORDER | SWT.CHECK | SWT.V_SCROLL);
		table.setLayoutData(new GridData(250, 350));

		contentDes = new Label(composite2, SWT.WRAP);
		// contentDes.setText(EMPTY_STR);
		contentDes.setText(MStudioMessages.getString("MStudioSoftDevPackagePropertyPage.keyPackages"));
		GridData gdx = new GridData(250, 350);
		gdx.verticalAlignment = SWT.TOP;
		contentDes.setLayoutData(gdx);

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
				return element == null ? EMPTY_STR : element.toString();
			}

			public Image getImage(Object element) {
				return treeIcon;
			}
		});
		ctv.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (selectAll.getSelection())
					selectAll.setSelection(false);

				Object pItem = event.getElement();

				if (isDefaultDepPkg((String)pItem)) {
					ctv.setChecked(pItem, true);
					return;
				}

				if (!event.getChecked()) {
					setAffectedPkgsChecked((String)pItem);
				} else {
					setDepPkgsChecked((String)pItem);
				}
			}
		});
		ctv.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				String obj = (String)((IStructuredSelection)(event.getSelection())).getFirstElement();

				if (isDefaultDepPkg(obj))
					return;

				if (obj != null)
					contentDes.setText(msEnvInfo.getAllSoftPkgs().get(obj).toString());
			}
		});
		ctv.setSorter(new PakckageSorter());


		selectAll = new Button(composite2, SWT.CHECK);
		selectAll.setText(MStudioMessages.getString("MStudioSoftDevPackagePropertyPage.selectAll"));
		selectAll.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		selectAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (selectAll.getSelection()) {
					ctv.setAllChecked(true);
					contentDes.setText(MStudioMessages.getString("MStudioSoftDevPackagePropertyPage.allPackages"));
				} else {
					ctv.setAllChecked(false);
					setDefaultDepPkg();
					contentDes.setText(MStudioMessages.getString("MStudioSoftDevPackagePropertyPage.keyPackages"));
				}
			}
		});

		return composite;
	}

	public void setVisible(boolean visible) {
		if (visible && !hasInitializeTable ) {
			getCheckboxTableViewerData();
			loadPersistentSettings();
			hasInitializeTable = true;
		}
		super.setVisible(visible);
	}

	public boolean performOk() {
		if (savePersistentSettings()) {
			resetProjectConfigurations((IProject) getElement());
			return true;
		}
		return false;
	}

	protected void performDefaults() {
		// loadPersistentSettings();
		setPerformDefaultsDepPkg();
		contentDes.setText(MStudioMessages.getString("MStudioSoftDevPackagePropertyPage.keyPackages"));
		super.performDefaults();
	}

	//==================================================================
	private void loadPersistentSettings() {

		MStudioProject mStudioProject = new MStudioProject((IProject)getElement());
		String[] s = mStudioProject.getDepPkgs();
		Object[] elements = msEnvInfo.getAllSoftPkgs().keySet().toArray();

		ctv.remove(elements);
		ctv.add(elements);
		ctv.setCheckedElements(s);

		if (elements.length == s.length && s != null && elements != null) {
			selectAll.setSelection(true);
		} else {
			selectAll.setSelection(false);
		}
	}

	private boolean savePersistentSettings() {

		MStudioProject mStudioProject = new MStudioProject((IProject)getElement());
		Object[] obj = ctv.getCheckedElements();
		String[] list = new String[obj.length];

		for (int i = 0; i < obj.length; i++) {
			list[i] = obj[i].toString();
		}

		// return mStudioProject.setDepPkgs(list);
		if (!mStudioProject.setDepPkgs(list)) {
			 MessageDialog.openError(this.getShell(),
					 MStudioMessages.getString("MStudioSoftDevPackagePropertyPage.error"),
					 MStudioMessages.getString("MStudioSoftDevPackagePropertyPage.errorMessages"));
			 return false;
		}

		return true;
	}

	private boolean dailogPkgsChecked(String title, String pkgName, List<String> listPkgs) {

		int count = listPkgs.size();
		if (count <= 0)
			return false;

		String messageInfo = new String(pkgName);
		messageInfo = messageInfo.concat(MSP_SPACE + title);

		String showSCAll = null;

		for (int i = 0; i < count; i++) {
			messageInfo = messageInfo.concat(MSP_SPACE + listPkgs.get(i));
		}

		if (title.equals(DEPEND))
			showSCAll = SHOW_SELECT;
		else
			showSCAll = SHOW_DESELECT;

		messageInfo = messageInfo.concat(showSCAll);
		MessageDialog.openInformation(this.getShell(), title, messageInfo);

		return true;
	}

	private void setAffectedPkgsChecked(String affectedPkgs) {

		for (Map.Entry<String, List<String>> info : msEnvInfo.getAffectedPkgs().entrySet()) {
			if (affectedPkgs.equals(info.getKey())) {
				List<String> affected = new ArrayList<String>(info.getValue());
				getAffectedPkgsChecked(affectedPkgs, affected);

				if (!dailogPkgsChecked(AFFECTED, affectedPkgs, affected))
					return;

				for (int i = 0; i < affected.size(); i++) {
					String affectedString = affected.get(i);
					Object pItem = (Object)affectedString;
					if (null == pItem)
						break;
					ctv.setChecked(pItem, false);
				}
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

	private void setDepPkgsChecked(String depPkgs) {

		for (Map.Entry<String, List<String>> info : msEnvInfo.getDepPkgs().entrySet()) {
			if (depPkgs.equals(info.getKey())) {
				List<String> dep = new ArrayList<String>(info.getValue());
				getDependPkgsChecked(depPkgs, dep);

				if (!dailogPkgsChecked(DEPEND, depPkgs, dep))
					return;

				for (int i = 0; i < dep.size(); i++) {
					String depString = dep.get(i);
					Object pItem = (Object)depString;
					if (null == pItem)
						break;
					ctv.setChecked(pItem, true);
				}
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

	private void getCheckboxTableViewerData() {

		for (Map.Entry<String, String> info : msEnvInfo.getAllSoftPkgs().entrySet()) {
			pkgs.add(new PackageItem(info.getKey(), info.getValue()));
		}
	}

	private void resetProjectConfigurations(IProject project) {

		IManagedProject managedProj = ManagedBuildManager.getBuildInfo(project).getManagedProject();
		IConfiguration[] cur_cfgs = managedProj.getConfigurations();
		MStudioEnvInfo einfo = MStudioPlugin.getDefault().getMStudioEnvInfo();
		List<String> depLibList = new ArrayList<String>();
		Object[] obj = ctv.getCheckedElements();

		for (int idx = 0; idx < obj.length; idx++) {
			String[] libs = einfo.getPackageLibs(obj[idx].toString());
			for (int c = 0; c < libs.length; c++) {
				if (!depLibList.contains(libs[c])) {
					depLibList.add(libs[c]);
				}
			}
		}

		String[] depLibs = depLibList.toArray(new String[depLibList.size()]);

		for (int i = 0; i < cur_cfgs.length; i++) {
			for (ITool t : cur_cfgs[i].getToolChain().getTools()) {
				try {
					if (t.getId().contains("c.link")) {
						IOption o = t.getOptionById("gnu.c.link.option.libs");
						cur_cfgs[i].setOption(t, o, depLibs);
					}
					if (t.getId().contains("cpp.link")) {
						IOption o = t.getOptionById("gnu.cpp.link.option.libs");
						cur_cfgs[i].setOption(t, o, depLibs);
					}
				} catch (BuildException e) {
					e.printStackTrace();
				}
			}
		}

		if (cur_cfgs.length > 0)
			ManagedBuildManager.saveBuildInfo(project, false);
	}

	private void setPerformDefaultsDepPkg() {
		MStudioProject mStudioProject = new MStudioProject((IProject)getElement());
		String[] s = mStudioProject.getDepPkgs();
		Object[] elements = msEnvInfo.getAllSoftPkgs().keySet().toArray();

		if (elements.length == s.length && s != null && elements != null) {
			selectAll.setSelection(true);
		} else {
			selectAll.setSelection(false);
			ctv.setCheckedElements(s);
		}
	}

	private void setDefaultDepPkg() {
		MStudioProject mStudioProject = new MStudioProject((IProject)getElement());
		String[] ddps = mStudioProject.getDepPkgs();

		if (null != ddps)
			ctv.setCheckedElements(ddps);
	}

	private boolean isDefaultDepPkg(String name) {
		MStudioProject mStudioProject = new MStudioProject((IProject)getElement());
		String[] ddps = mStudioProject.getDepPkgs();

		for (int i = 0; i < ddps.length; i++) {
			if (name.equals(ddps[i]))
				return true;
		}

		return false;
	}
}

