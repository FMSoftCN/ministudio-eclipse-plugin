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

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;

import org.eclipse.osgi.util.TextProcessor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.CWizardHandler;
import org.eclipse.cdt.ui.wizards.EntryDescriptor;
import org.eclipse.cdt.ui.wizards.IWizardItemsListListener;
import org.eclipse.cdt.ui.wizards.IWizardWithMemory;
import org.eclipse.cdt.internal.ui.CPluginImages;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.template.MStudioNewWizardTemplate;

@SuppressWarnings("restriction")
public class MStudioNewCAppProjectSelectWizardPage extends WizardNewProjectCreationPage
		implements IWizardItemsListListener {

	public final static String PAGE_ID = "org.eclipse.cdt.managedbuilder.ui.wizard.NewModelProjectWizardPage";
	public final static String DESC = "EntryDescriptor";

	private final static Image  IMG_CATEGORY = CPluginImages.get(CPluginImages.IMG_OBJS_SEARCHFOLDER);
	private final static Image  IMG_ITEM = CPluginImages.get(CPluginImages.IMG_OBJS_VARIABLE);
	private final static String EXTENSION_POINT_ID = "org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioWizard";
	private final static String ELEMENT_NAME = "wizard";
	private final static String CLASS_NAME = "class";

	public MStudioWizardHandler h_selected = null;

	private Tree tree = null;
	private Composite composite = null;
	private MStudioEnvInfo mseInfo = MStudioPlugin.getDefault().getMStudioEnvInfo();

	public MStudioNewCAppProjectSelectWizardPage(String pageName) {
		super(pageName);
		setPageComplete(false);
	}

	public void createControl(Composite parent) {
		super.createControl(parent);

		createDynamicGroup((Composite) getControl());
		MStudioWizardHandler handler = updateData(tree, composite,
						MStudioNewCAppProjectSelectWizardPage.this, getWizard());
		setDefaultProjectType(tree);
		switchTo(handler, getDescriptor(tree));

		setErrorMessage(null);
		setMessage(null);

		setPageComplete(validatePage());
	}

	private void createDynamicGroup(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayoutData(new GridData(GridData.FILL_BOTH));
		c.setLayout(new GridLayout(1, true));

		Label label = new Label(c, SWT.NONE);
		label.setText(MStudioMessages.getString("MGMainWizardPage.0")); //$NON-NLS-1$
		label.setFont(parent.getFont());
		label.setLayoutData(new GridData(GridData.BEGINNING));

		tree = new Tree(c, SWT.SINGLE | SWT.BORDER);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TreeItem[] tis = tree.getSelection();
				if (tis == null || tis.length == 0)
					return;
				mseInfo.setDefaultDepPackages(tis[0].getText());
				switchTo((MStudioWizardHandler) tis[0].getData(),
						(EntryDescriptor) tis[0].getData(DESC));
				setPageComplete(validatePage());
			}
		});
		tree.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				for (int i = 0; i < tree.getItemCount(); i++) {
					if (tree.getItem(i).getText().compareTo(e.result) == 0)
						return;
				}
				e.result = MStudioMessages.getString("MGMainWizardPage.0"); //$NON-NLS-1$
			}
		});
		composite = c;
	}

	public IWizardPage getNextPage() {
		return (h_selected == null) ? null : h_selected.getSpecificPage();
	}

	public URI getProjectLocation() {
		return useDefaults() ? null : getLocationURI();
	}

	protected boolean validatePage() {
		setMessage(null);

		if (null == mseInfo.getSoCPaths()) {
			setErrorMessage(MStudioMessages.getString("MGMainWizardPage.errorMessage")); 
			return false;
		}

		if (!super.validatePage()) {
			// setErrorMessage(MStudioMessages.getString("MGMainWizardPage.13"));
			return false;
		}

		if (getProjectName().indexOf('#') >= 0) {
			setErrorMessage(MStudioMessages.getString("MGMainWizardPage.11")); //$NON-NLS-1$
			return false;
		}

		boolean bad = true; // should we treat existing project as error

		IProject handle = getProjectHandle();
		if (handle.exists()) {
			if (getWizard() instanceof IWizardWithMemory) {
				IWizardWithMemory w = (IWizardWithMemory) getWizard();
				if (w.getLastProjectName() != null
						&& w.getLastProjectName().equals(getProjectName()))
					bad = false;
			}
			if (bad) {
				setErrorMessage(MStudioMessages.getString("MGMainWizardPage.10")); //$NON-NLS-1$
				return false;
			}
		}

		if (bad) { // skip this check if project already created
			try {
				IFileStore fs;
				URI p = getProjectLocation();
				if (p == null) {
					fs = EFS.getStore(ResourcesPlugin.getWorkspace().getRoot()
							.getLocationURI());
					fs = fs.getChild(getProjectName());
				} else
					fs = EFS.getStore(p);
				IFileInfo f = fs.fetchInfo();
				if (f.exists()) {
					if (f.isDirectory()) {
						setMessage(
								MStudioMessages.getString("MGMainWizardPage.7"), IMessageProvider.WARNING); 
						return true;
					}
					setErrorMessage(MStudioMessages.getString("MGMainWizardPage.6")); 
					return false;
				}
			} catch (CoreException e) {
				CUIPlugin.log(e.getStatus());
			}
		}

		if (!useDefaults()) {
			IStatus locationStatus = ResourcesPlugin.getWorkspace()
					.validateProjectLocationURI(handle, getLocationURI());
			if (!locationStatus.isOK()) {
				setErrorMessage(locationStatus.getMessage());
				return false;
			}
		}

		if (tree.getItemCount() == 0) {
			setErrorMessage(MStudioMessages.getString("MGMainWizardPage.3")); 
			return false;
		}

		// it is not an error, but we cannot continue
		if (h_selected == null) {
			setErrorMessage(null);
			return false;
		}

		String s = h_selected.getErrorMessage();
		if (s != null) {
			setErrorMessage(s);
			return false;
		}

		setErrorMessage(null);
		return true;
	}

	private void setDefaultProjectType(Tree tree) {
		TreeItem[] tis = tree.getSelection();
		if (tis == null || tis.length == 0)
			return;
		mseInfo.setDefaultDepPackages(tis[0].getText());
	}

	public static MStudioWizardHandler updateData(Tree tree, Composite compos,
			IWizardItemsListListener ls, IWizard wizard) {
		// remember selected item
		TreeItem[] sel = tree.getSelection();
		String savedStr = (sel.length > 0) ? sel[0].getText() : null;

		tree.removeAll();
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT_ID);
		if (extensionPoint == null)
			return null;
		
		IExtension[] extensions = extensionPoint.getExtensions();
		if (extensions == null)
			return null;
		
		List<EntryDescriptor> items = new ArrayList<EntryDescriptor>();
		for (int i = 0; i < extensions.length; ++i) {
			IConfigurationElement[] elements = extensions[i] .getConfigurationElements();
			for (IConfigurationElement element : elements) {
				if (element.getName().equals(ELEMENT_NAME)) {

					MStudioNewWizardTemplate w = null;
					try {
						w = (MStudioNewWizardTemplate) element.createExecutableExtension(CLASS_NAME);
					} catch (CoreException e) {
						System.out.println(MStudioMessages.getString("MGMainWizardPage.5") 
								+ e.getLocalizedMessage()); //$NON-NLS-1$
						return null;
					}
					if (w == null)
						return null;
					w.setDependentControl(compos, ls);
					// for (EntryDescriptor ed : w.createItems(show_sup.getSelection(), wizard)) {
					for (EntryDescriptor ed : w.createItems(false, wizard)) {
						items.add(ed);
					}
				}
			}
		}
		// If there is a EntryDescriptor which is default for category, make
		// sure it is in the front of the list.
		for (int i = 0; i < items.size(); ++i) {
			EntryDescriptor ed = items.get(i);	
			if (ed.isCategory()) {
				items.remove(i);
				items.add(0, ed);
				break;
			}
		}
		
		for (int i = 0; i < items.size(); ++i) {
			EntryDescriptor ed = items.get(i);	
			if (ed.getName().equals("mginit Module Project") && 
					MStudioEnvInfo.getInstance().getMgRunMode() != "process"){
				items.remove(i);
				break;
			}
		}
		
		// bug # 211935 : allow items filtering.
		if (ls != null) // NULL means call from prefs
			items = ls.filterItems(items);

		addItemsToTree(tree, items);

		if (tree.getItemCount() > 0) {
			TreeItem target = null;
			// try to search item which was selected before
			if (savedStr != null) {
				TreeItem[] all = tree.getItems();
				for (TreeItem element : all) {
					if (savedStr.equals(element.getText())) {
						target = element;
						break;
					}
				}
			}
			if (target == null) {
				target = tree.getItem(0);
				if (target.getItemCount() != 0)
					target = target.getItem(0);
			}
			tree.setSelection(target);
			return (MStudioWizardHandler) target.getData();
		}
		return null;
	}

	private static void addItemsToTree(Tree tree, List<EntryDescriptor> items) {

		ArrayList<TreeItem> placedTreeItemsList = new ArrayList<TreeItem>(items.size());
		ArrayList<EntryDescriptor> placedEntryDescriptorsList =
											new ArrayList<EntryDescriptor>(items.size());
		for (EntryDescriptor wd : items) {
			if (wd.getParentId() == null) {
				wd.setPath(wd.getId());
				TreeItem ti = new TreeItem(tree, SWT.NONE);
				ti.setText(TextProcessor.process(wd.getName()));
				ti.setData(wd.getHandler());
				ti.setData(DESC, wd);
				ti.setImage(calcImage(wd));
				placedTreeItemsList.add(ti);
				placedEntryDescriptorsList.add(wd);
			}
		}
		while (true) {
			boolean found = false;
			Iterator<EntryDescriptor> it2 = items.iterator();
			while (it2.hasNext()) {
				EntryDescriptor wd1 = it2.next();
				if (wd1.getParentId() == null)
					continue;
				for (int i = 0; i < placedEntryDescriptorsList.size(); i++) {
					EntryDescriptor wd2 = placedEntryDescriptorsList.get(i);
					if (wd2.getId().equals(wd1.getParentId())) {
						found = true;
						wd1.setParentId(null);
						CWizardHandler h = wd2.getHandler();
						if (h == null && wd1.getHandler() == null
								&& !wd1.isCategory())
							break;
						wd1.setPath(wd2.getPath() + "/" + wd1.getId()); //$NON-NLS-1$
						wd1.setParent(wd2);
						if (h != null) {
							if (wd1.getHandler() == null && !wd1.isCategory())
								wd1.setHandler((CWizardHandler) h.clone());
							// if (!h.isApplicable(wd1))
							// break;
						}
						TreeItem p = placedTreeItemsList.get(i);
						TreeItem ti = new TreeItem(p, SWT.NONE);
						ti.setText(wd1.getName());
						ti.setData(wd1.getHandler());
						ti.setData(DESC, wd1);
						ti.setImage(calcImage(wd1));
						placedTreeItemsList.add(ti);
						placedEntryDescriptorsList.add(wd1);
						break;
					}
				}
			}
			// repeat iterations until all items are placed.
			if (!found)
				break;
		}
		// orphan elements (with not-existing parentId) are ignored
	}

	private void switchTo(MStudioWizardHandler h, EntryDescriptor ed) {

		if (h == null)
			h = (MStudioWizardHandler) ed.getHandler();

		if (ed.isCategory())
			h = null;

		try {
			if (h != null)
				h.initialize(ed);
		} catch (CoreException e) {
			CUIPlugin.log(e);
			h = null;
		}

		if (h_selected != null)
			h_selected.handleUnSelection();

		h_selected = h;
		if (h != null)
			h_selected.handleSelection();
	}

	public static EntryDescriptor getDescriptor(Tree _tree) {
		TreeItem[] sel = _tree.getSelection();
		if (sel == null || sel.length == 0)
			return null;

		return (EntryDescriptor) sel[0].getData(DESC);
	}

	public void toolChainListChanged(int count) {
		setPageComplete(validatePage());
		getWizard().getContainer().updateButtons();
	}

	public boolean isCurrent() {
		return isCurrentPage();
	}

	private static Image calcImage(EntryDescriptor ed) {
		if (ed.getImage() != null)
			return ed.getImage();

		if (ed.isCategory())
			return IMG_CATEGORY;

		return IMG_ITEM;
	}

	@SuppressWarnings("unchecked")
	public List filterItems(List items) {
		return items;
	}
}

