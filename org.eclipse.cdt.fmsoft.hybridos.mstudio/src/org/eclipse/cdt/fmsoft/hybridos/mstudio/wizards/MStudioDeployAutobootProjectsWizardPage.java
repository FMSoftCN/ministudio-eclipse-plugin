package org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.TableLayout;
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

public class MStudioDeployAutobootProjectsWizardPage extends WizardPage {

	private Table table=null;
	private CheckboxTableViewer ctv=null;
	private Button selectAll=null;
	
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
		table=new Table(com2, SWT.BORDER | SWT.CHECK | SWT.V_SCROLL|SWT.H_SCROLL|SWT.IMAGE_BMP|SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);	
		new TableColumn(table,SWT.NONE).setText("Start");
		new TableColumn(table,SWT.NONE).setText("Program name");
		
		GridData layData2=new GridData(GridData.FILL_BOTH);
		layData2.verticalSpan=2;
		table.setLayoutData(layData2);
		
		Button upButton=new Button(com2,SWT.NONE);
		upButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		upButton.setText("Move Up");
		Button downButton=new Button(com2,SWT.NONE);
		downButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		downButton.setText("Move Down");
		
		//ctv = new CheckboxTableViewer(table);
		selectAll=new Button(topPanel,SWT.CHECK);
		selectAll.setText("Select All");
		//TODO
		//initAutoStartProgrames();
		
		setControl(topPanel);
		setPageComplete(true);

	}
	//init the table data
	private void initAutoStartProgrames(){
		
	}
	
	public IProject[] getDeployAutobootProjects() {
		return null;
	}
}
