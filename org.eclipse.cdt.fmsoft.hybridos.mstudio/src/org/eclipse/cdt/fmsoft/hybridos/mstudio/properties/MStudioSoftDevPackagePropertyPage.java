/*********************************************************************
 * Copyright (C) 2005 - 2010, Beijing FMSoft Technology Co., Ltd.
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

import org.eclipse.cdt.managedbuilder.ui.properties.ManagedBuilderUIImages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo.PackageItem;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProject;


public class MStudioSoftDevPackagePropertyPage extends PropertyPage
	implements IWorkbenchPropertyPage {

	private static final String EMPTY_STR = "";
	private static final Image IMG = ManagedBuilderUIImages.get(ManagedBuilderUIImages.IMG_BUILD_CONFIG);

	private Label title = null;
	private Label description = null;
	private Label tip = null;
	private Label contentDes = null;
	private Button selectAll = null;
	private Table table = null;
	private CheckboxTableViewer ctv = null;
	private List<PackageItem> pkgs = new ArrayList<PackageItem>();
	private MStudioEnvInfo msEnvInfo = MStudioPlugin.getDefault().getMStudioEnvInfo();

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
		title.setFont(new Font(title.getFont().getDevice(), "TimesRoman", 0, SWT.BOLD));
		title.setText("Software Development Package");

		description = new Label(composite1, SWT.NONE);
		description.setText("Select software development packages for HybridOS");
		new Label(composite1, SWT.FULL_SELECTION|SWT.LINE_SOLID);
		tip = new Label(composite1, SWT.NONE);
		tip.setText("Please select the packages for your project");

		Composite composite2 = new Composite(composite, SWT.NONE);
		GridLayout gl = new GridLayout(2, false);
		gl.makeColumnsEqualWidth = true;
		composite2.setLayout(gl);
		composite2.setLayoutData(new GridData(GridData.FILL_BOTH));

		table = new Table(composite2, SWT.BORDER | SWT.CHECK | SWT.V_SCROLL);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));

		contentDes = new Label(composite2, SWT.WRAP);
		contentDes.setText("No Selected Packeg");
		GridData gdx = new GridData(GridData.FILL_BOTH);
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
				return IMG;
			}
		});
		ctv.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (selectAll.getSelection())
					selectAll.setSelection(false);

				Object pItem = event.getElement();

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
				if (obj != null)
					contentDes.setText(msEnvInfo.getAllSoftPkgs().get(obj).toString());
			}
		});

		selectAll = new Button(composite2, SWT.CHECK);
		selectAll.setText("Select All");
		selectAll.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		selectAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (selectAll.getSelection())
					ctv.setAllChecked(true);
				else
					ctv.setAllChecked(false);
			}
		});

		loadPersistentSettings();
		getCheckboxTableViewerData();

		return composite;
	}

	public boolean performOk() {
		return savePersistentSettings();
	}

	protected void performDefaults() {
		loadPersistentSettings();
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
			 MessageDialog.openError(this.getShell(), "Error", "store default oucurrend an error");
			 return false;
		} else {
			 MessageDialog.openInformation(this.getShell(), "stored", "depend paksge store ok !");
			 return true;
		}
	}

	private boolean dailogPkgsChecked(String title, String pkgName, List<String> listPkgs) {

		int count = listPkgs.size();
		if (count <= 0)
			return false;

		String messageInfo = new String(pkgName);
		messageInfo = messageInfo.concat(" " + title);

		for (int i = 0; i < count; i++) {
			messageInfo = messageInfo.concat(" " + listPkgs.get(i));
		}
		messageInfo = messageInfo.concat(", will select all!");
		MessageDialog.openInformation(this.getShell(), title, messageInfo);

		return true;
	}

	private void setAffectedPkgsChecked(String affectedPkgs) {

		for (Map.Entry<String, List<String>> info : msEnvInfo.getAffectedPkgs().entrySet()) {
			if (affectedPkgs.equals(info.getKey())) {
				List<String> affected = info.getValue();

				if (!dailogPkgsChecked("affected", affectedPkgs, affected))
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

	private void setDepPkgsChecked(String depPkgs) {

		for (Map.Entry<String, List<String>> info : msEnvInfo.getDepPkgs().entrySet()) {
			if (depPkgs.equals(info.getKey())) {
				List<String> dep = info.getValue();

				if (!dailogPkgsChecked("depend", depPkgs, dep))
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

	private void getCheckboxTableViewerData() {

		for (Map.Entry<String, String> info : msEnvInfo.getAllSoftPkgs().entrySet()) {
			pkgs.add(new PackageItem(info.getKey(), info.getValue()));
		}
	}
}

