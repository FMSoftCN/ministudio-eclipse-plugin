package org.minigui.eclipse.cdt.mstudio.wizards;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.properties.ManagedBuilderUIImages;
import org.eclipse.cdt.managedbuilder.ui.wizards.CfgHolder;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.ui.newui.CDTPrefUtil;
import org.eclipse.cdt.ui.newui.UIMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.minigui.eclipse.cdt.mstudio.MiniGUIMessages;

public class MGConfigWizardPage extends WizardPage {

	public static final String PAGE_ID = "org.minigui.eclipse.cdt.mstudio.wizard.MGConfigWizardPage"; 

	private static final Image IMG = ManagedBuilderUIImages.get(ManagedBuilderUIImages.IMG_BUILD_CONFIG);
	private static final String TITLE = MiniGUIMessages.getString("MGConfigWizardPage.0"); 
	private static final String MESSAGE = MiniGUIMessages.getString("MGConfigWizardPage.1"); 
	private static final String COMMENT = MiniGUIMessages.getString("MGConfigWizardPage.12"); 
	private static final String EMPTY_STR = "";

	private Table table;
	private CheckboxTableViewer tv;
	private Label l_projtype;
	private Label l_chains;
	private Composite parent;
	private String propertyId;
	private String errorMessage = null;
	private String message = MESSAGE;
	public boolean isVisible = false;
	private MGWizardHandler handler;
	public boolean pagesLoaded = false;
	private IToolChain[] visitedTCs = null;
	IWizardPage[] customPages = null;

	public MGConfigWizardPage(MGWizardHandler h) {
		super(TITLE);
		setPageComplete(false);
		handler = h;
		setWizard(h.getWizard());
	}

	public CfgHolder[] getCfgItems(boolean getDefault) {
		CfgHolder[] its;
		if (getDefault || table == null || !isVisited())
			its = getDefaultCfgs(handler);
		else {
			ArrayList<CfgHolder> out = new ArrayList<CfgHolder>(table.getItemCount());
			for (TableItem ti : table.getItems()) {
				if (ti.getChecked())
					out.add((CfgHolder) ti.getData());
			}
			its = out.toArray(new CfgHolder[out.size()]);
		}
		return its;
	}

	public void createControl(Composite p) {
		parent = new Composite(p, SWT.NONE);
		parent.setFont(parent.getFont());
		parent.setLayout(new GridLayout());
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite c1 = new Composite(parent, SWT.NONE);
		c1.setLayout(new GridLayout(2, false));
		c1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		setupLabel(c1, MiniGUIMessages.getString("MGConfigWizardPage.4"),
				GridData.BEGINNING);
		l_projtype = setupLabel(c1, EMPTY_STR, GridData.FILL_HORIZONTAL);
		setupLabel(c1, MiniGUIMessages.getString("MGConfigWizardPage.5"),
				GridData.BEGINNING);
		l_chains = setupLabel(c1, EMPTY_STR, GridData.FILL_HORIZONTAL);
		setupLabel(c1, MiniGUIMessages.getString("MGConfigWizardPage.6"),
				GridData.BEGINNING);
		setupLabel(c1, EMPTY_STR, GridData.BEGINNING);

		Composite c2 = new Composite(parent, SWT.NONE);
		c2.setLayout(new GridLayout(2, false));
		c2.setLayoutData(new GridData(GridData.FILL_BOTH));

		table = new Table(c2, SWT.BORDER | SWT.CHECK | SWT.V_SCROLL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(gd);

		tv = new CheckboxTableViewer(table);
		tv.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return (Object[]) inputElement;
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}
		});
		tv.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				return element == null ? EMPTY_STR : ((CfgHolder) element)
						.getName();
			}

			public Image getImage(Object element) {
				return IMG;
			}
		});
		tv.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				setPageComplete(isCustomPageComplete());
				update();
			}
		});
		Composite c = new Composite(c2, SWT.NONE);
		c.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		c.setLayout(new GridLayout());

		Button b1 = new Button(c, SWT.PUSH);
		b1.setText(MiniGUIMessages.getString("MGConfigWizardPage.7"));
		b1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		b1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				tv.setAllChecked(true);
				setPageComplete(isCustomPageComplete());
				update();
			}
		});

		Button b2 = new Button(c, SWT.PUSH);
		b2.setText(MiniGUIMessages.getString("MGConfigWizardPage.8"));
		b2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		b2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				tv.setAllChecked(false);
				setPageComplete(isCustomPageComplete());
				update();
			}
		});

		// dummy placeholder
		new Label(c, 0).setLayoutData(new GridData(GridData.FILL_BOTH));

		Button b3 = new Button(c, SWT.PUSH);
		b3.setText(MiniGUIMessages.getString("MGConfigWizardPage.13"));
		b3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		b3.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				advancedDialog();
			}
		});

		Group gr = new Group(parent, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gr.setLayoutData(gd);
		gr.setLayout(new FillLayout());
		Label lb = new Label(gr, SWT.NONE);
		lb.setText(COMMENT);

		setControl(parent);
	}

	/**
	 * 
	 * @param handler
	 * @return
	 */
	static public CfgHolder[] getDefaultCfgs(MGWizardHandler handler) {
		String id = handler.getPropertyId();
		IProjectType pt = handler.getProjectType();
		ArrayList<CfgHolder> out = new ArrayList<CfgHolder>();
		for (IToolChain tc : handler.getSelectedToolChains()) {
			CfgHolder[] cfgs = null;
			if (id != null)
				cfgs = CfgHolder.cfgs2items(ManagedBuildManager
						.getExtensionConfigurations(tc,
								MGWizardHandler.ARTIFACT, id));
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
				if (cfgs[j].isSystem()
						|| (handler.supportedOnly() && !cfgs[j].isSupported()))
					continue;
				out.add(cfgs[j]);
			}
		}
		return out.toArray(new CfgHolder[out.size()]);
	}

	private boolean isVisited() {
		if (table == null || handler == null)
			return false;

		return Arrays.equals(handler.getSelectedToolChains(), visitedTCs);
	}

	public boolean isCustomPageComplete() {
		if (!isVisited())
			return true;

		if (table.getItemCount() == 0) {
			errorMessage = UIMessages.getString("MGConfigWizardPage.10");
			message = errorMessage;
			return false;
		}
		if (tv.getCheckedElements().length == 0) {
			errorMessage = UIMessages.getString("MGConfigWizardPage.11"); 
			message = errorMessage;
			return false;
		}
		errorMessage = null;
		message = MESSAGE;
		return true;
	}

	public void setVisible(boolean visible) {
		parent.setVisible(visible);
		isVisible = visible;
		if (visible && handler != null && !isVisited()) {
			tv.setInput(CfgHolder.unique(getDefaultCfgs(handler)));
			tv.setAllChecked(true);
			String s = EMPTY_STR;
			visitedTCs = handler.getSelectedToolChains();
			for (int i = 0; i < visitedTCs.length; i++) {
				s = s + ((visitedTCs[i] == null) ? "" : visitedTCs[i].getUniqueRealName());
				if (i < visitedTCs.length - 1)
					s = s + "\n"; 
			}
			l_chains.setText(s);
			l_projtype.setText(handler.getName());
			setPageComplete(isCustomPageComplete());
			l_chains.getParent().pack();
		}
		if (visible) {
			parent.getParent().layout(true, true);
			update();
		}
	}

	// ------------------------
	private Label setupLabel(Composite c, String name, int mode) {
		Label l = new Label(c, SWT.WRAP);
		l.setText(name);
		GridData gd = new GridData(mode);
		gd.verticalAlignment = SWT.TOP;
		l.setLayoutData(gd);
		Composite p = l.getParent();
		l.setFont(p.getFont());
		return l;
	}

	public String getName() {
		return TITLE;
	}

	public Control getControl() {
		return parent;
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
		if (getWizard() instanceof NewMiniGUIAppWizard) {
			NewMiniGUIAppWizard nmWizard = (NewMiniGUIAppWizard) getWizard();
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
		return MBSCustomPageManager.getNextPage(PAGE_ID);
	}
}