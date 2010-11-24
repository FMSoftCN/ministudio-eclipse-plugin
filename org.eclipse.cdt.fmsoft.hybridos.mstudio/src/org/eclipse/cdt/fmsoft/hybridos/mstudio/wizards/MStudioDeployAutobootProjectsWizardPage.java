package org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards;

import java.util.List;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo.MiniGUIRunMode;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class MStudioDeployAutobootProjectsWizardPage extends WizardPage {

	private Table table=null;
	private Button selectAll=null;
	private Button upButton,downButton;
	private IProject[] projects;
	
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
		com1.setLayoutData(new GridData());
		Label title=new Label(com1,SWT.FILL);
		title.setText("Auto start programes");
		title.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite com2=new Composite(topPanel,SWT.NONE);		
		com2.setLayout(new GridLayout(2,false));
		com2.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridData dg=new GridData(GridData.FILL_BOTH);
		dg.verticalSpan=2;
		
		table=new Table(com2, SWT.CHECK|SWT.V_SCROLL|SWT.H_SCROLL|SWT.FULL_SELECTION|SWT.FILL|SWT.MULTI|SWT.BORDER);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayout(new GridLayout());
		table.setLayoutData(dg);
		TableColumn col1=new TableColumn(table,SWT.NONE);
		col1.setText("Start");
		col1.setWidth(50);
		TableColumn col2=new TableColumn(table,SWT.NONE); 	
		col2.setText("Program Name");
		col2.setWidth(100);
		
		upButton=new Button(com2,SWT.NONE);
		upButton.setLayoutData(new GridData(GridData.FILL));
		upButton.setText("Move Up");
		upButton.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
			public void widgetSelected(SelectionEvent e) {
				int index=table.getSelectionIndex();
				if(projects != null && index < projects.length){
					IProject p=projects[index];
					projects[index]=projects[index-1];
					projects[index-1]=p;
					table.setSelection(index-1);
					updateTable();
					updateButtons();
				}				
			}
			});
		downButton=new Button(com2,SWT.NONE);
		downButton.setLayoutData(new GridData(GridData.FILL));
		downButton.setText("Move Down");
		downButton.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				int index=table.getSelectionIndex();
				if(projects != null && index < projects.length){
					IProject p=projects[index];
					projects[index]=projects[index+1];
					projects[index-1]=p;
					table.setSelection(index+1);
					updateTable();
					updateButtons();
				}
			}
			
		});
				
		selectAll=new Button(topPanel,SWT.CHECK);
		selectAll.setText("Select All");
		selectAll.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				if(selectAll.getSelection()){
					table.selectAll();
				}
				else{					
				}
			}});
		
		initAutoStartProgrames();		
		setControl(topPanel);
		setPageComplete(true);

	}
	public void update(){
		table.clearAll();
		initAutoStartProgrames();
		updateButtons();
		MStudioDeployWizard.deployCanFinish=true;
	}
	private void updateButtons() {
		if(MStudioEnvInfo.getInstance().getMgRunMode() == MiniGUIRunMode.thread.name()){
			upButton.setEnabled(false);
			downButton.setEnabled(false);
			selectAll.setEnabled(false);
		}
		else{			
			int index=table.getSelectionIndex();	
			if(index < 0){
				upButton.setEnabled(false);
				downButton.setEnabled(false);
				return;
			}
			if(index == 0){
				upButton.setEnabled(false);
				downButton.setEnabled(true);
				return;
			}
			if(index >0 && index < table.getItemCount()-1){
				upButton.setEnabled(true);
				downButton.setEnabled(true);
				return;
			}
			if(index == table.getItemCount()-1){
				upButton.setEnabled(true);
				downButton.setEnabled(false);
				return;
			}
		}
	}
	private void updateTable(){
		if(projects!=null){
			for(int i=0;i<projects.length;i++){
				TableItem item=new TableItem(table,SWT.NONE);
				item.setText(1,projects[i].getName());
			}
		}
	}
	//init the table data
	private void initAutoStartProgrames(){
		MStudioDeployWizard wizard = (MStudioDeployWizard) getWizard();
		if(wizard==null)
			return;
		projects=wizard.getDeployExeProjects();		
		if(projects!=null){
			for(int i=0;i<projects.length;i++){
				TableItem item=new TableItem(table,SWT.NONE);
			}
		}		
		updateButtons();
	}
	
	public IProject[] getDeployAutobootProjects() {
		return projects;
	}
	public class tableSelectChangedListener implements ISelectionChangedListener{
		public void selectionChanged(SelectionChangedEvent event) {
			table.getSelectionIndex();
		}		
	}	
	
	public class TableViewerContentProvider implements IStructuredContentProvider{
		public Object[] getElements(Object inputElement) {
			if(inputElement instanceof List)
				return ((List)inputElement).toArray();
			return new Object[0];
		}
		public void dispose() {
			
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			//TODO
		}
		
	}
	public class TableViewerLabelProvider implements ITableLabelProvider{

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {			
			//if the element is class or orther object here would be ocurr an error	
			return element.toString();
		}

		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}
		
	}
}
