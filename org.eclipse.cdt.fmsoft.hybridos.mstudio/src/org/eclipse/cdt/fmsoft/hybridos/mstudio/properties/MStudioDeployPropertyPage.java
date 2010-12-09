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

package org.eclipse.cdt.fmsoft.hybridos.mstudio.properties;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProject;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards.MStudioParserIniFile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.DirectoryFieldEditor;


public class MStudioDeployPropertyPage extends PropertyPage
	implements IWorkbenchPropertyPage {

//	private String selectProjectName=((IProject)getElement()).getName();
//	private String selectProjectPath=((IProject)getElement()).getLocation().toOSString();
	private final String RES_LOCATION = "/usr/local/share/";
	private final String BIN_LOCATION = "/usr/local/bin";
	private final String LIB_LOCATION = "/usr/local/lib";
	private final String CUSTOM_FILE_LOCATION = "/usr/local/share";
	private final String SECTION_PATH_INFO="path_info";
	private final String KEY_RESPKG_PATH="respkg_path";
	private final String KEY_USR_PATH="usr_path";
	
	private Button deployToRootfs = null;
	private Label resLabel;
	private Text resText;
	private Label binLabel;
	private Text binText;
	private Label libLabel;
	private Text libText;
	private Label customFileLabel;
	private Text customFileText;
	
	private Button deployButton;
	private Button removeButton;
	private List srcList;
	private List destList;
	private ArrayList<String> filesName = new ArrayList<String>();
	public MStudioDeployPropertyPage() {
	}

	@Override
	protected Control createContents(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		GridData data = new GridData(GridData.FILL);
		composite.setLayoutData(data);
		//Init the button
		deployToRootfs = new Button(composite, SWT.CHECK);
		deployToRootfs.setText(MStudioMessages.getString("MStudioDeployPropertyPage.buttonText"));
		deployToRootfs.setFont(new Font(deployToRootfs.getFont().getDevice(), "", 0, SWT.BOLD));
		new Label(composite,SWT.NONE);
		Label l1 = new Label(composite,SWT.NONE);
		l1.setText(MStudioMessages.getString("MStudioDeployPropertyPage.text0"));
		l1.setFont(new Font(l1.getFont().getDevice(), "", 0, SWT.BOLD));
		Label l2 = new Label(composite,SWT.NONE);
		l2.setText(MStudioMessages.getString("MStudioDeployPropertyPage.text1"));
		
		Composite com1 = new Composite(composite,SWT.NONE);
		com1.setLayout(new GridLayout(2,false));
		com1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		resLabel = new Label(com1,SWT.NONE);
		GridData gd = new GridData();
		resLabel.setText(MStudioMessages.getString("MStudioDeployPropertyPage.resLabel"));
		resText = new Text(com1,SWT.BORDER);
		resText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		binLabel = new Label(com1,SWT.NONE);
		binLabel.setText(MStudioMessages.getString("MStudioDeployPropertyPage.binLabel"));
		binText = new Text(com1,SWT.BORDER);
		binText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		libLabel = new Label(com1,SWT.NONE);
		libLabel.setText(MStudioMessages.getString("MStudioDeployPropertyPage.libLabel"));
		libText = new Text(com1,SWT.BORDER);
		libText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		customFileLabel = new Label(com1,SWT.NONE);
		customFileLabel.setText(MStudioMessages.getString("MStudioDeployPropertyPage.customFileLabel"));
		customFileText = new Text(com1,SWT.BORDER);
		customFileText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Composite com2 = new Composite(composite,SWT.NONE);
		com2.setLayout(new GridLayout(3,false));
		com2.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label title = new Label(com2,SWT.FILL);
		GridData gdTitle = new GridData();
		gdTitle.horizontalSpan=3;
		title.setLayoutData(gdTitle);
		title.setFont(new Font(title.getFont().getDevice(), "", 0, SWT.BOLD));
		title.setText(MStudioMessages.getString("MStudioDeployPropertyPage.labelTitle"));		
		
		Composite col1 = new Composite(com2,SWT.NONE);
		col1.setLayout(new GridLayout());
		col1.setLayoutData(new GridData(GridData.FILL_BOTH));
		Composite col2 = new Composite(com2,SWT.NONE);
		col2.setLayout(new GridLayout());
		col2.setLayoutData(new GridData(GridData.FILL));
		Composite col3 = new Composite(com2,SWT.NONE);
		col3.setLayout(new GridLayout());
		col3.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label topLabel1 = new Label(col1,SWT.NONE);
		topLabel1.setLayoutData(new GridData(GridData.FILL));
		topLabel1.setText(MStudioMessages.getString("MStudioDeployPropertyPage.srcListTitle"));
		srcList = new List(col1,SWT.V_SCROLL|SWT.MULTI|SWT.BORDER);
		srcList.setLayoutData(new GridData(GridData.FILL_BOTH));
		srcList.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				deployButton.setEnabled(true);
			}
		});
		deployButton = new Button(col2,SWT.NONE);
		deployButton.setText(MStudioMessages.getString("MStudioDeployPropertyPage.deployButtonText"));
		deployButton.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				if(srcList.getSelectionCount() <= 0)
					return;
				String[] srcListString = srcList.getSelection();
				srcList.remove(srcList.getSelectionIndices());
				for(int i = 0; i < srcListString.length; i++){
					destList.add(srcListString[i]);
				}
				if(srcList.getSelectionCount() <= 0 || srcList.getItemCount() <= 0)
					deployButton.setEnabled(false);
			}
		});
		removeButton = new Button(col2,SWT.NONE);
		removeButton.setText(MStudioMessages.getString("MStudioDeployPropertyPage.removeButtonText"));
		removeButton.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				if(destList.getSelectionCount() <= 0)
					return;
				String[] destListString = destList.getSelection();
				destList.remove(destList.getSelectionIndices());
				for(int i = 0; i < destListString.length; i++){
					srcList.add(destListString[i]);
				}
				if(destList.getItemCount() <= 0 || destList.getSelectionCount() <= 0)
					removeButton.setEnabled(false);
			}
		});
		
		Label topLabel2 = new Label(col3,SWT.NONE);
		topLabel2.setLayoutData(new GridData(GridData.FILL));
		topLabel2.setText(MStudioMessages.getString("MStudioDeployPropertyPage.destListTitle"));
		destList = new List(col3,SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
		destList.setLayoutData(new GridData(GridData.FILL_BOTH));
		destList.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e){
			}
			public void widgetSelected(SelectionEvent e){
				removeButton.setEnabled(true);
			}
		});
		initControls();
		loadPersistentSettings();
		
		return composite;
	}

	private void initControls(){
		if(srcList.getItemCount() <= 0 || srcList.getSelectionCount() <= 0){
			deployButton.setEnabled(false);
		}
		if(destList.getItemCount() <= 0 || destList.getSelectionCount() <= 0){
			removeButton.setEnabled(false);
		}
	}
	private void loadPersistentSettings() {
		MStudioProject mStudioProject = new MStudioProject((IProject)getElement());
		deployToRootfs.setSelection(mStudioProject.getDefaultDeployable());		
		//load the path
		//check .{projectname}.cfg file exists
		//PRIFIX...load from the file
		//paths is not null,the min length of paths is 0
		String[] paths = mStudioProject.getDeployPathInfo();
		if(paths.length == 4 ){
			resText.setText(paths[0]);
			binText.setText(paths[1]);
			libText.setText(paths[2]);
			customFileText.setText(paths[3]);
		}
		else{
//			set the default values
			resText.setText(RES_LOCATION + ((IProject)getElement()).getName() + "/res");
			binText.setText(BIN_LOCATION);
			libText.setText(LIB_LOCATION);
			customFileText.setText(CUSTOM_FILE_LOCATION);
		}
		//load the files form project
		listProjectFiles(mStudioProject.getProject().getLocation().toOSString());
		String[] files = mStudioProject.getDeployCustomFiles();
		ArrayList<String> tempFiles = new ArrayList<String>();
		//delete the save files when the src files is not exist
		for(int i = 0; i < files.length; i++){
			if(filesName.contains(files[i]))
				tempFiles.add(files[i]);
		}
		files = null;
		//delete the item from srcList which the destList contains
		filesName.removeAll(tempFiles);
		//add the filesName to the control
		for(int i = 0;i < filesName.size(); i++)
			srcList.add(filesName.get(i));
		for(int i = 0;i < tempFiles.size(); i++)
			destList.add(tempFiles.get(i));
		
	}
	
	public void listProjectFiles(String filePath){
		try{
			File f = new File(filePath);
			if(f.isDirectory()){
				String[] dirList = f.list(new FileFilter());					
				String dir = f.getPath();
				if(dirList == null)
					return;
				if(dirList.length <= 0)
					return;
				for(int i = 0; i < dirList.length; i++)
					dirList[i] = dir + f.separator + dirList[i];
				
				for(int i = 0; i < dirList.length; i++)
					listProjectFiles(dirList[i]);
			}
			else{
				filesName.add(f.getName());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private boolean savePersistentSettings() {
		 MStudioProject mStudioProject = new MStudioProject((IProject)getElement());
		 if(!mStudioProject.setDefaultDeployable(deployToRootfs.getSelection()))
			 return false;		
		 String paths[] = new String[]{resText.getText(),binText.getText(),libText.getText(),customFileText.getText()};
		 if(!mStudioProject.setDeployPathInfo(paths))
			 return false;
		 String cfgFileName = ((IProject)getElement()).getLocation().toOSString()+File.separator+"."+((IProject)getElement()).getName()+"_res.cfg";
		 MStudioParserIniFile cfg = new MStudioParserIniFile(cfgFileName);
		 if (null == cfg)
			 return false;

		 cfg.setStringProperty(SECTION_PATH_INFO, KEY_RESPKG_PATH,MStudioPlugin.getDefault().getMStudioEnvInfo().getRootfsPath() + resText.getText(), null);
		 cfg.setStringProperty(SECTION_PATH_INFO, KEY_USR_PATH,MStudioPlugin.getDefault().getMStudioEnvInfo().getRootfsPath() + customFileText.getText(), null);
		 if(!cfg.save())
			 return false;
		 return mStudioProject.setDeployCustomFiles(destList.getItems());
	}

	public boolean performOk() {
		if(savePersistentSettings()){
			MessageDialog.openInformation(getShell(), MStudioMessages.getString("MStudioDeployPropertyPage.saveSuccessDialogTitle"), 
					MStudioMessages.getString("MStudioDeployPropertyPage.saveSuccessDialogcontent"));
			return true;
		}
		else{
			MessageDialog.openError(getShell(), MStudioMessages.getString("MStudioDeployPropertyPage.saveErrorDialogTitle"),
					MStudioMessages.getString("MStudioDeployPropertyPage.saveErrorDialogcontent"));
			return false;
		}
	}
	public class FileFilter implements FilenameFilter{
		@Override
		public boolean accept(File dir, String name) {
			File ff = new File(dir.getPath()+File.separator+name);
			ArrayList<String> filterStr=new ArrayList<String>(5);
			filterStr.add("renderer");
			filterStr.add("text");
			filterStr.add("image");
			filterStr.add("ui");
			filterStr.add("Debug4Host");
			String[] fileFilter=new String[]{".c",".C",".h",".H",".o",".O",".cpp",".Cpp",".CPP",".exe"};
//			filter the director
			if(filterStr.contains(name) && ff.isDirectory())
				return false;
//			filter the file ,contains .c .0 .exe .cpp .h ...
			for(int i = 0; i < fileFilter.length; i++){
				if(name.endsWith(fileFilter[i]))
					return false;
			}
					
			return true;
		}
	}
}
