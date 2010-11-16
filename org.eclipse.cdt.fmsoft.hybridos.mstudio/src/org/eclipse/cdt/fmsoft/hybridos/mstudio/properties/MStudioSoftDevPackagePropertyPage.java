package org.eclipse.cdt.fmsoft.hybridos.mstudio.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioEnvInfo;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
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
	private ArrayList<Button> groupButtonList;
	public MStudioSoftDevPackagePropertyPage() {
		groupButtonList=new ArrayList<Button>();
	}

	@Override
	protected Control createContents(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);
		
		Composite composite1 = new Composite(composite,SWT.NONE);
		composite1.setLayout(new GridLayout());
		composite1.setLayoutData(new GridData(GridData.FILL));
		
		title = new Label(composite1,SWT.NONE);
		title.setFont(new Font(title.getFont().getDevice(),"TimesRoman",0,SWT.BOLD));
		title.setText("Software Development Package");
		description = new Label(composite1,SWT.NONE);
		description.setText("Select software development packages for HybridOS");
		//description.setBounds(description.getBounds().x, description.getBounds().y, description.getBounds().width, description.getBounds().height*3);
		new Label(composite1,SWT.FULL_SELECTION|SWT.LINE_SOLID);
		tip = new Label(composite1,SWT.NONE);
		tip.setText("Please select the packages for your project");		
		
		Composite composite2 = new Composite(composite,SWT.NONE);
		composite2.setLayout(new FillLayout());		
		//Alert:Dont't use Class GridData in onther Layout Container except Class GridLayout.
		Composite composite2Left = new Composite(composite2,SWT.NONE);
		Composite composite2Right = new Composite(composite2,SWT.NONE);
		composite2Left.setLayout(new GridLayout());
		composite2Right.setLayout(new GridLayout());
		
		buttonGroup = new Group(composite2Left,SWT.NONE);
		buttonGroup.setLayout(new GridLayout());
		buttonGroup.setLayoutData(new GridData());
		
		contentDes = new Label(composite2Right,SWT.FILL|SWT.WRAP);
		MStudioEnvInfo envInfo = MStudioPlugin.getDefault().getMStudioEnvInfo();
		//MStudioEnvInfo envInfo=new MStudioEnvInfo();
		//clear the button array for insert
		groupButtonList.clear();		
		for(Map.Entry<String, String> softInfo : envInfo.getAllSoftPkgs().entrySet())
		{			
			Button newButton = new Button(buttonGroup,SWT.CHECK);
			newButton.setText(softInfo.getKey().toString());
			newButton.setToolTipText(softInfo.getKey().toString());
			newButton.addSelectionListener(new ButtonSelectionListen());
			groupButtonList.add(newButton);
		
		}
		selectAll = new Button(composite2Left,SWT.CHECK);
		selectAll.setText("Select All");
		selectAll.addSelectionListener(new ButtonSelectionListen());
		
		loadPersistentSettings();
		
		return composite;
	}
	
	private void loadPersistentSettings() {
		//PRIFiX
		MStudioProject mStudioProject = new MStudioProject((IProject)getElement());	
		//here only use ArrayList do not use List<String> ,if use would be error init
		ArrayList s = new ArrayList<String>();
		s.add(mStudioProject.getDepPkgs());
		
		//List<String> s = Arrays.asList(mStudioProject.getDepPkgs());
		Iterator<Button> button = groupButtonList.iterator();
		// judge the select all button is selected
		boolean isAllSelected=true;
		while(button.hasNext()){
			Button b = (Button)button.next();
			if(s.contains(b.getText())){
				b.setSelection(true);
			}
			else{
				b.setSelection(false);	
				isAllSelected = false;
			}				
		}
		//if there is no button in the button list,set the selectAll-Button's selection propertie is false
		if(0 >= groupButtonList.size()){
			isAllSelected = false;
		}
		selectAll.setSelection(isAllSelected);
	}
	
	private boolean savePersistentSettings() {
		 MStudioProject mStudioProject = new MStudioProject((IProject)getElement());
		 List<String> args = new ArrayList<String>();
		 /*
		  * this "for" code function is get the buttons from the control of its parent control 
		 for(Control button : buttonGroup.getChildren()){
			 if(((Button)button).getSelection()){
				 args.add(((Button)button).getText());
			 }
		 */
		 for(Control button : groupButtonList){
			 if(((Button)button).getSelection()){
				 args.add(((Button)button).getText());
			 }
		 }
		 return mStudioProject.setDepPkgs((String[])args.toArray(new String[args.size()]));
	}
	
	public boolean performOk() {
		return savePersistentSettings();
	}

	private class ButtonSelectionListen implements SelectionListener{

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// TODO Auto-generated method stub			
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			//when the button click ,now find the desciption of the button text to show on the right text zone
			if(e.getSource().equals(selectAll)){
				//when user click the selectAll button now all of the button will be checked.
				if(selectAll.getSelection()){
					/*
					for(Control button : buttonGroup.getChildren()){
						((Button) button).setSelection(true);
					}
					*/
					for(Control button : groupButtonList){
						((Button) button).setSelection(true);
					}
				}
				else{
					/*
					for(Control button : buttonGroup.getChildren()){
						((Button)button).setSelection(false);
					}
					*/
					for(Control button : groupButtonList){
						((Button) button).setSelection(false);
					}
				}
				return;
			}			
			contentDes.setText(MStudioPlugin.getDefault().getMStudioEnvInfo()
					.getAllSoftPkgs().get(((Button)(e.getSource())).getText()).toString());
		}		
	}
}
