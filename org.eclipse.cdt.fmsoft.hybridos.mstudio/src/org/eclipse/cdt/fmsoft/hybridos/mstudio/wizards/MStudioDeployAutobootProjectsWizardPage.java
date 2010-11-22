package org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo.MiniGUIRunMode;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.views.markers.internal.TableView;

public class MStudioDeployAutobootProjectsWizardPage extends WizardPage {

	private TableViewer tv=null;
	private Table table=null;
	private CheckboxTableViewer ctv=null;
	private Button selectAll=null;
	Button upButton,downButton;
	
	public MStudioDeployAutobootProjectsWizardPage(String pageName) {
		super(pageName);
		init();
	}

	private void init() {
		setTitle(MStudioMessages.getString("MStudioDeployWizardPage.selectAutobootProjects.pageTitle"));
		setDescription(MStudioMessages.getString("MStudioDeployWizardPage.selectAutobootProjects.desc"));
	}

	public MStudioDeployAutobootProjectsWizardPage(String pageName,
			String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		init();
	}

	@Override
	public void createControl(Composite parent) {
		Composite topPanel;
		topPanel = new Composite(parent, SWT.NONE);
		topPanel.setLayout(new GridLayout());
		topPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite com1=new Composite(topPanel,SWT.NONE);
		com1.setLayout(new GridLayout());
		com1.setLayoutData(new GridData(GridData.FILL_BOTH));
		Label title=new Label(com1,SWT.FILL);
		title.setText("Auto start programes");
		title.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite com2=new Composite(topPanel,SWT.NONE);
		
		com2.setLayout(new GridLayout(2,false));
		com2.setLayoutData(new GridData(GridData.FILL_BOTH));
		tv=new TableViewer(com2,SWT.MULTI|SWT.CHECK|SWT.V_SCROLL|SWT.H_SCROLL|SWT.FULL_SELECTION);
		//table=new Table(com2, SWT.BORDER | SWT.CHECK | SWT.V_SCROLL|SWT.H_SCROLL|SWT.IMAGE_BMP|SWT.FULL_SELECTION);
		table=tv.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		//tv.setContentProvider(new IContentProvider());
		new TableColumn(table,SWT.NONE).setText("Start");
		new TableColumn(table,SWT.NONE).setText("Program name");
		
		GridData layData2=new GridData(GridData.FILL_BOTH);
		layData2.verticalSpan=2;
		table.setLayoutData(layData2);
		
		upButton=new Button(com2,SWT.NONE);
		upButton.setLayoutData(new GridData(GridData.FILL));
		upButton.setText("Move Up");
		downButton=new Button(com2,SWT.NONE);
		downButton.setLayoutData(new GridData(GridData.FILL));
		downButton.setText("Move Down");
		
		ctv = new CheckboxTableViewer(table);
		
		selectAll=new Button(topPanel,SWT.CHECK);
		selectAll.setText("Select All");
		//TODO
		initAutoStartProgrames();
		
		setControl(topPanel);
		setPageComplete(true);

	}
	public void update(){
		
	}
	//init the table data
	private void initAutoStartProgrames(){
		if(MStudioEnvInfo.getInstance().getMgRunMode() == MiniGUIRunMode.thread.name()){
			upButton.setEnabled(false);
			downButton.setEnabled(false);
			selectAll.setEnabled(false);	
			//table.addControlListener(new tableSingleCheckListener());
			
			ctv.addCheckStateListener(new tableSingleCheckListener());
		}
		else{
			upButton.setEnabled(true);
			downButton.setEnabled(true);
			selectAll.setEnabled(true);
			ctv.addCheckStateListener(new tableMultyCheckListener());
		}
	}
	
	public IProject[] getDeployAutobootProjects() {
		
		return null;
	}
	public class tableSingleCheckListener implements ICheckStateListener{
		public void checkStateChanged(CheckStateChangedEvent event) {
			ctv.setAllChecked(false);
			ctv.setChecked(event.getElement(), event.getChecked());
		}		
	}
	public class tableMultyCheckListener implements ICheckStateListener{
		public void checkStateChanged(CheckStateChangedEvent event) {
			
		}
	}
}
