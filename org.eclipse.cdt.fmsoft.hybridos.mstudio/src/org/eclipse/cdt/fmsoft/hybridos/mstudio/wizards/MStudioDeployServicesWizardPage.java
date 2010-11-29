package org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.preferences.MStudioPreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

public class MStudioDeployServicesWizardPage extends WizardPage {

	private Table serviceTable=null;
	private CheckboxTableViewer ctv=null;
	
	private final String STORE_SERV_SPLIT = "\t";
	
	public MStudioDeployServicesWizardPage(String pageName) {
		super(pageName);
		init();

	}

	public MStudioDeployServicesWizardPage(String pageName, String title,
			ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		init();
	}

	private void init() {
		setTitle(MStudioMessages.getString("MStudioDeployWizardPage.selectServices.pageTitle"));
		setDescription(MStudioMessages.getString("MStudioDeployWizardPage.selectServices.desc"));
	}

	@Override
	public void createControl(Composite parent) {
		Composite topPanel;
		topPanel = new Composite(parent, SWT.NONE);
		topPanel.setLayout(new GridLayout());
		topPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		
		Label label1 = new Label(topPanel,SWT.NONE);
		label1.setText(MStudioMessages.getString("MStudioDeployWizardPage.selectServices.title"));
		
		serviceTable = new Table(topPanel, SWT.BORDER | SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL );
		GridData gd = new GridData(GridData.FILL_BOTH);
		serviceTable.setLayoutData(gd);
		
		ctv = new CheckboxTableViewer(serviceTable);		
		
		ctv.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				/*
				if(ctv.getCheckedElements().length<=0)
					setPageComplete(false);
				else
					setPageComplete(true);
					*/
			}
		});
		ctv.addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent event) {				
			}
		});
		initServiceTable();		
		setControl(topPanel);
		setPageComplete(true);
	}
	
	public void update(){
		ctv.setItemCount(0);
		initServiceTable();
	}
	
	private void initServiceTable(){
		IPreferenceStore store = MStudioPlugin.getDefault().getPreferenceStore();		
		if (!store.contains(MStudioPreferenceConstants.MSTUDIO_SOC_NAME))
			return;		
		String storeServ = store.getString(MStudioPreferenceConstants.MSTUDIO_DEFAULT_SERVICES);		
		String[] defaultSelServ = storeServ.split(STORE_SERV_SPLIT);
		List<String> s = MStudioPlugin.getDefault().getMStudioEnvInfo().getServices();
		if(null != s){
			String[] serv = (String[])s.toArray(new String[s.size()]);
			if(serv.length > 0){
				ctv.add(serv);			
				ctv.setCheckedElements(defaultSelServ);
			}
		}
		//init button next
		/*
		if(ctv.getCheckedElements().length > 0)
			setPageComplete(true);
		else
			setPageComplete(false);
			*/
	}	
	
	private void storeService(){
		IPreferenceStore store = MStudioPlugin.getDefault().getPreferenceStore();			
		String servToStore = new String(); 
		Object[] serv = ctv.getCheckedElements();
		if(serv.length >= 1){
			servToStore = serv[0].toString();
			for(int i=1; i<serv.length; i++){
				servToStore += STORE_SERV_SPLIT + serv[i].toString();
			}			
		}	
		store.setValue(MStudioPreferenceConstants.MSTUDIO_DEFAULT_SERVICES, servToStore);		
	}
	
	
	public String[] getDeployServices() {
		Object[] obj=ctv.getCheckedElements();
		if(obj == null)
			return null;
		ArrayList<String> lists=new ArrayList<String>();
		for(int i=0; i<obj.length; i++){
			lists.add(obj[i].toString());
		}
		return (String[])lists.toArray(new String[obj.length]);
	}	
	
	public IWizardPage getNextPage() {
		MStudioDeployWizard wizard = (MStudioDeployWizard) getWizard();
		if(wizard == null)
			return null;
		wizard.getDeployAutobootWizardPage().update();
		return wizard.getNextPage(this);
	}
}
