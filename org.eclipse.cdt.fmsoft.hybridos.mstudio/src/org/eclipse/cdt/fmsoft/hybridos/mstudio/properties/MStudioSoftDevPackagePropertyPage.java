package org.eclipse.cdt.fmsoft.hybridos.mstudio.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProject;
import org.eclipse.cdt.managedbuilder.ui.properties.ManagedBuilderUIImages;
import org.eclipse.core.resources.IProject;
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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
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
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

public class MStudioSoftDevPackagePropertyPage extends PropertyPage implements
		IWorkbenchPropertyPage {

	private Label title;
	private Label description;
	private Label tip;
	private Group buttonGroup;
	private Label contentDes;
	private Button selectAll;
	private Table table;
	private ArrayList<String> groupButtonList;
	private CheckboxTableViewer ctv;
	
	private static final String EMPTY_STR = "";
	private static final Image IMG = ManagedBuilderUIImages.get(ManagedBuilderUIImages.IMG_BUILD_CONFIG);
	
	public MStudioSoftDevPackagePropertyPage() {
		groupButtonList=new ArrayList<String>();
	}

	@Override
	protected Control createContents(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());	
		composite.setLayoutData(new GridData());
		
		Composite composite1 = new Composite(composite,SWT.NONE);
		composite1.setLayout(new GridLayout());
		composite1.setLayoutData(new GridData());
		
		title = new Label(composite1,SWT.NONE);
		title.setFont(new Font(title.getFont().getDevice(),"TimesRoman",0,SWT.BOLD));
		title.setText("Software Development Package");
		
		description = new Label(composite1,SWT.NONE);
		description.setText("Select software development packages for HybridOS");
		new Label(composite1,SWT.FULL_SELECTION|SWT.LINE_SOLID);
		tip = new Label(composite1,SWT.NONE);
		tip.setText("Please select the packages for your project");		
		
		Composite composite2 = new Composite(composite,SWT.NONE);
		GridLayout gl = new GridLayout(2,false);
		gl.makeColumnsEqualWidth = true;
		composite2.setLayout(gl);		
		composite2.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		table = new Table(composite2, SWT.BORDER | SWT.CHECK | SWT.V_SCROLL|SWT.H_SCROLL);
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
		ctv.addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent event) {
				String obj = (String)((IStructuredSelection)(event.getSelection())).getFirstElement();
				if (obj != null)
					contentDes.setText(MStudioPlugin.getDefault().getMStudioEnvInfo().getAllSoftPkgs().get(obj).toString());				
			}
		});
		selectAll = new Button(composite2,SWT.CHECK);
		selectAll.setText("Select All");
		selectAll.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		selectAll.addSelectionListener(new ButtonSelectionListen());
		
		loadPersistentSettings();
		
		return composite;
	}
	
	private void loadPersistentSettings() {
		MStudioProject mStudioProject = new MStudioProject((IProject)getElement());	
		String[] s = mStudioProject.getDepPkgs();
		Object[] elements=MStudioPlugin.getDefault().getMStudioEnvInfo().getAllSoftPkgs().keySet().toArray();
		ctv.add(elements);
		//init
		ctv.setCheckedElements(s);		
		//System.out.println(s.length+"");
		//Object[] obj = (Object[])ctv.getCheckedElements();		
		if( elements.length == s.length && s != null && elements != null){
			//System.out.println(elements.length+"");
			selectAll.setSelection(true);
		}
		else
			selectAll.setSelection(false);
	}
	
	private boolean savePersistentSettings() {
		 MStudioProject mStudioProject = new MStudioProject((IProject)getElement());
		 Object[] obj = ctv.getCheckedElements();
		 String[] list=new String[obj.length];
		 for(int i=0;i<obj.length;i++){
			 list[i]=obj[i].toString();
		 }
		 //return mStudioProject.setDepPkgs(list);
		 if(!mStudioProject.setDepPkgs(list)){
			 MessageDialog.openError(this.getShell(), "Error", "store default oucurrend an error");
			 return false;
		 }
		 else{
			 MessageDialog.openInformation(this.getShell(), "stored", "store ok");
			 return true;
		 }
	}
	
	public boolean performOk() {
		return savePersistentSettings();
	}

	private class ButtonSelectionListen implements SelectionListener{
		public void widgetDefaultSelected(SelectionEvent e) {
			// TODO Auto-generated method stub			
		}
		public void widgetSelected(SelectionEvent e) {			
			if(((Button)e.getSource()).getSelection()){
				ctv.setAllChecked(true);
			}	
			else{
				ctv.setAllChecked(false);
			}
		}		
	}
}
