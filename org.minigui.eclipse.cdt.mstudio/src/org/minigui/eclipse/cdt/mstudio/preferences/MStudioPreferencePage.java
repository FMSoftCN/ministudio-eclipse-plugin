package org.minigui.eclipse.cdt.mstudio.preferences;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import org.minigui.eclipse.cdt.mstudio.MStudioPlugin;
import org.minigui.eclipse.cdt.mstudio.wizards.MStudioVersionWizard;
import org.minigui.eclipse.cdt.mstudio.project.MgProject;
import org.minigui.eclipse.cdt.mstudio.project.MgProjectNature;

class MsVersionListener extends SelectionAdapter
{
	public final static int ADD = 1;
	public final static int EDIT = 2;
	public final static int REMOVE = 3;
	public final static int DEFAULT = 4;
	public final static int SELECTION = 5;
	
	private MStudioPreferencePage m_prefpage;
	private int m_control;
	
	public MsVersionListener(MStudioPreferencePage prefpage, int control)
	{
		m_control = control;
		m_prefpage = prefpage;
	}
	
	public void widgetSelected(SelectionEvent e)
	{
		if (m_control == ADD) {
			MStudioVersionWizard versionWizard = new MStudioVersionWizard();
			WizardDialog versionDialog = new WizardDialog(m_prefpage.getShell(), versionWizard);
			versionDialog.create();

			versionDialog.setTitle("Add new miniStudio version");
			if (versionDialog.open() == WizardDialog.OK) {
				m_prefpage.addItem(versionWizard.getVersionName(), 
						versionWizard.getBinPath());
			}
		} else if (m_control == EDIT) {
			String[] current = m_prefpage.getCurrentItem();

			MStudioVersionWizard versionWizard = new MStudioVersionWizard();
			WizardDialog versionDialog = new WizardDialog(m_prefpage.getShell(), versionWizard);
			versionDialog.create();

			versionWizard.setVersionName(current[0]);
			versionWizard.setBinPath(current[1]);

			versionDialog.setTitle("Edit miniStudio Version");
			if (versionDialog.open() == WizardDialog.OK) {
				m_prefpage.updateItem(versionWizard.getVersionName(), 
						versionWizard.getBinPath());
			}
		} else if (m_control == REMOVE) {
			m_prefpage.removeItem();
		} else if (m_control == DEFAULT) {
			m_prefpage.setCurrentDefault();
		} else if (m_control == SELECTION) {
			Table table = (Table)e.widget;
			m_prefpage.enableButtons(table.getSelectionCount() > 0);
		}
	}
}


public class MStudioPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	private Label label;
	private Table table;
	private Button removeButton;
	private Button editButton;
	private Button defaultButton;

	public MStudioPreferencePage() {	
		
	}

	public void init(IWorkbench workbench) {

	}
	
	public static String[] getMStudioVersions() {
		Vector<String> versions = new Vector<String>();

		IPreferenceStore store = MStudioPlugin.getDefault().getPreferenceStore();
		if (!store.contains(PreferenceConstants.MSVERSION_COUNT))
			return null;
		
		int count = store.getInt(PreferenceConstants.MSVERSION_COUNT);
		for (int i=0; i<count; ++i) {
			String name = PreferenceConstants.MSVERSION_NAME + "." + Integer.toString(i);
			if (store.contains(name))
				versions.add(store.getString(name));
		}
		
		return (String[])versions.toArray(new String[versions.size()]);
	}
	
	public static String getMStudioBinPath(String version) {
		IPreferenceStore store = MStudioPlugin.getDefault().getPreferenceStore();
		if (!store.contains(PreferenceConstants.MSVERSION_COUNT))
			return null;
		
		String defaultVersionName = store.getString(PreferenceConstants.MSVERSION_DEFAULT);
		String defaultVersionBinPath = "";
		int count = store.getInt(PreferenceConstants.MSVERSION_COUNT);
		for (int i=0; i<count; ++i) {
			String nameKey = PreferenceConstants.MSVERSION_NAME + "." + Integer.toString(i);
			String binpathKey = PreferenceConstants.MSVERSION_BINPATH + "." + Integer.toString(i);
			String name = "";
			String binpath = "";
			
			if (store.contains(nameKey))
				name = store.getString(nameKey);
			if (store.contains(binpathKey))
				binpath = store.getString(binpathKey);
			
			if (name.equals(version))
				return binpath;
			if (name.equals(defaultVersionName)) {
				defaultVersionBinPath = binpath;
			}
		}
		
		return defaultVersionBinPath;
	}
	
	protected Control createContents(Composite parent) {

		Composite composite = new Composite(parent, SWT.NULL);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		
		addMsBuildsSection(composite);
		updateItems();
		enableButtons(table.getSelectionCount() > 0);

		return composite;
	}
	
	private void addMsBuildsSection(Composite parent) {
		GridData gridData;

		label = new Label(parent, SWT.CENTER);
		label.setText("miniStudio Versions:");
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
		gridData.horizontalSpan = 2;
		label.setLayoutData(gridData);

		table = new Table(parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		TableColumn column;
		column = new TableColumn(table, SWT.LEFT);
		column.setText("Version Name");
		column.setWidth(150);
		column.setResizable(true);
		column = new TableColumn(table, SWT.LEFT);
		column.setText("Binary Path");
		column.setResizable(true);
		column.setWidth(150);
		
		gridData = new GridData();
		gridData.verticalSpan = 5;
		gridData.widthHint = 250;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		
		table.setLayoutData(gridData);
		table.addSelectionListener(new MsVersionListener(this, MsVersionListener.SELECTION));
		
		Button addButton = new Button(parent, SWT.NONE);
		addButton.addSelectionListener(new MsVersionListener(this, MsVersionListener.ADD)); 
		addButton.setText("Add...");
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		addButton.setLayoutData(gridData);
		
		editButton = new Button(parent, SWT.NONE);
		editButton.addSelectionListener(new MsVersionListener(this, MsVersionListener.EDIT));
		editButton.setText("Edit...");
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		editButton.setLayoutData(gridData);
		
		removeButton = new Button(parent, SWT.NONE);
		removeButton.addSelectionListener(new MsVersionListener(this, MsVersionListener.REMOVE));
		removeButton.setText("Remove");
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		removeButton.setLayoutData(gridData);
		
		defaultButton = new Button(parent, SWT.NONE);
		defaultButton.addSelectionListener(new MsVersionListener(this, MsVersionListener.DEFAULT));
		defaultButton.setText("Default");
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		defaultButton.setLayoutData(gridData);

		Composite spacer = new Composite(parent, SWT.NONE);
		gridData = new GridData();
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalAlignment = GridData.FILL;
		spacer.setLayoutData(gridData);
	}

	public void addItem(String name, String binPath) {
		TableItem item = new TableItem(table, SWT.NONE);
		item.setText(new String[] {name, binPath});
	}
	
	public void removeItem() {
		if (table.getSelectionCount() > 0) {
			int removeindex = table.getSelectionIndex(); 
			int defindex = getDefaultIndex();
			table.remove(removeindex);
			
			if (removeindex == defindex)
				setDefaultIndex(0);
		}	
		enableButtons(table.getSelectionCount() > 0);
	}
	
	public void updateItem(String name, String binPath) {	
		TableItem[] items = table.getSelection();
		if (items.length == 0)
			return;
		items[0].setText(new String[] {name, binPath});
	}
	
	private void updateItems()
	{
		IPreferenceStore store = MStudioPlugin.getDefault().getPreferenceStore();
		
		if (!store.contains(PreferenceConstants.MSVERSION_COUNT))
			return;
		
		int defaultVersionIndex = 0;
		String defaultVersionName = store.getString(PreferenceConstants.MSVERSION_DEFAULT);
		
		int count = store.getInt(PreferenceConstants.MSVERSION_COUNT);
		for (int i=0; i<count; ++i) {
			String nameKey = PreferenceConstants.MSVERSION_NAME + "." + Integer.toString(i);
			String binpathKey = PreferenceConstants.MSVERSION_BINPATH + "." + Integer.toString(i);
			String name = "";
			String binpath = "";
			
			if (store.contains(nameKey))
				name = store.getString(nameKey);
			if (store.contains(binpathKey))
				binpath = store.getString(binpathKey);
			addItem(name, binpath);
			
			if (name.equals(defaultVersionName))
				defaultVersionIndex = i;
		}	
		setDefaultIndex(defaultVersionIndex);
	}

	public String[] getCurrentItem() {
		TableItem[] items = table.getSelection();
        if (items.length == 0)
            return null;
        return new String[] {items[0].getText(0), items[0].getText(1), items[0].getText(2)};
	}
	
	private void setDefaultIndex(int index) {
		for (int i=0; i<table.getItemCount(); ++i) {
			TableItem item = table.getItem(i); 
			Font fnt = item.getFont();
			FontData fntdata = fnt.getFontData()[0];
			
			if (i == index) {
				int style = fntdata.getStyle() | SWT.BOLD;
				fntdata.setStyle(style);
				item.setFont(new Font(fnt.getDevice(), fntdata));				
			} else if ((fntdata.getStyle() & SWT.BOLD) != 0) {
				int style = fntdata.getStyle() & ~SWT.BOLD;
				fntdata.setStyle(style);
				item.setFont(new Font(fnt.getDevice(), fntdata));
			}
		}
	}
	
	private int getDefaultIndex() {
		for (int i=0; i<table.getItemCount(); ++i) {
			TableItem item = table.getItem(i); 
			Font fnt = item.getFont();
			FontData fntdata = fnt.getFontData()[0];
			
			if ((fntdata.getStyle() & SWT.BOLD) != 0)
				return i; 
		}
		
		if (table.getItemCount() > 0)
			return 0;
		
		return -1;
	}

	public void setCurrentDefault() {
		setDefaultIndex(table.getSelectionIndex());
	}

	public void enableButtons(boolean enabled) {
		removeButton.setEnabled(enabled);
		defaultButton.setEnabled(enabled);
		editButton.setEnabled(enabled);
	}
	
	private String getDefaultMsVersionName() {
		int index = getDefaultIndex();
		if (index == -1)
			return null;

		return table.getItem(index).getText(0);
	}
	
	private static IProject[] getMgProjects() {
		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		Vector<IProject> mgProjects = new Vector<IProject>();
		for (int i = 0; i < allProjects.length; ++i) {
			try {
				if (allProjects[i].hasNature (MgProjectNature.MG_NATURE_ID))
					mgProjects.add(allProjects[i]);
			} catch (CoreException ex) {}
		}
		IProject[] result = new IProject[mgProjects.size()];
		for (int i = 0; i < result.length; ++i)
			result[i] = (IProject)mgProjects.elementAt(i);
		return result;
	}

	public boolean performOk()
	{
		IPreferenceStore store = MStudioPlugin.getDefault().getPreferenceStore();
		
		// Fetch all project's GUIBuilder paths
		IProject[] pros = getMgProjects();
		MgProject[] mgProjects = new MgProject[pros.length];
		String[] oldBinPaths = new String[pros.length];
		for (int i=0; i<pros.length; ++i) {
			mgProjects[i] = new MgProject(pros[i]);
			oldBinPaths[i] = mgProjects[i].getMStudioBinPath();
		}
		
		String defaultVersion = getDefaultMsVersionName();
		if (defaultVersion != null)
			store.setValue(PreferenceConstants.MSVERSION_DEFAULT, defaultVersion);
		
		store.setValue(PreferenceConstants.MSVERSION_COUNT, table.getItemCount());
		for (int i=0; i<table.getItemCount(); ++i) {
			store.setValue(PreferenceConstants.MSVERSION_NAME + "." + Integer.toString(i),
					table.getItem(i).getText(0));
			store.setValue(PreferenceConstants.MSVERSION_BINPATH + "." + Integer.toString(i),
					table.getItem(i).getText(1));
		}

		// updates all the MiniGUI projects and collect projects that need rebuild
		Vector outdated = new Vector();
		
		for (int i=0; i < mgProjects.length; ++i) {
			mgProjects[i].updateMStudioDir(oldBinPaths[i]);
			if ((mgProjects[i].getMStudioBinPath() == null && oldBinPaths[i] != null)
				|| (mgProjects[i].getMStudioBinPath() != null && !mgProjects[i].getMStudioBinPath().equals(oldBinPaths[i]))) {
				outdated.add(mgProjects[i]);
			}
		}
		
		if (!outdated.isEmpty())
			askForRebuild(outdated);
	
		return true;
	}
	
	private void askForRebuild(final Vector<IProject> projects) {
		Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		MessageDialog dialog = new MessageDialog(shell, "miniStudio Versions Changed", null,
				"Some projects' miniStudio versions have changed. A rebuild of the projects is required for changes to take effect. Do a full rebuild now?", 
				MessageDialog.QUESTION, 
				new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL}, 0);
		if (dialog.open() == 0) {
			WorkspaceJob rebuild = new WorkspaceJob("Rebuild projects") {
	    		public boolean belongsTo(Object family) {
	    			return ResourcesPlugin.FAMILY_MANUAL_BUILD.equals(family);
	    		}
				public IStatus runInWorkspace(IProgressMonitor monitor) {
					Iterator i = projects.iterator();
					while (i.hasNext()) {
                        MgProject project = (MgProject)i.next();
                        project.scheduleRebuild();
                    }
					return Status.OK_STATUS;
				}
			};
			rebuild.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
			rebuild.setUser(true);
			rebuild.schedule();
		}
	}

}
