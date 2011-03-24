package org.eclipse.cdt.fmsoft.hybridos.mstudio.importWizards;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProjectProperties;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.internal.wizards.datatransfer.ArchiveFileManipulations;
import org.eclipse.ui.internal.wizards.datatransfer.TarException;
import org.eclipse.ui.internal.wizards.datatransfer.TarFile;
import org.eclipse.ui.internal.wizards.datatransfer.TarLeveledStructureProvider;
import org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage;
import org.eclipse.ui.internal.wizards.datatransfer.ZipLeveledStructureProvider;

public class MStudioExtenalImportProjectWizardPage extends WizardProjectsImportPage{
	private static final String PROJECT_FILE_NAME = ".hproject";
	private static final String SOCNAME_PROPERTY = "socName";
	private List<ProjectRecord> validProjects = new ArrayList<ProjectRecord>();
	private String[] allSoc = MStudioPlugin.getDefault().getMStudioEnvInfo().getSoCPaths();
	private String curSocName = MStudioPlugin.getDefault().getMStudioEnvInfo().getCurSoCName();
	private String selectSocName = curSocName;
	private CheckboxTreeViewer projectListBox = null;
	private Button dirRadioButton = null;
	private String selectPath = null;

	private String tarFileSocName = null;
	private String zipFileSocName = null;
	
	
	public MStudioExtenalImportProjectWizardPage(){
		this("wizardExternalProjectsPage");
	}
	
	public MStudioExtenalImportProjectWizardPage(String pageName){
		super(pageName);
		setPageComplete(false);
		setTitle(MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.ImportProjectsTitle"));
		setDescription(MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.ImportProjectsDescription"));
	}
	
	public MStudioExtenalImportProjectWizardPage(String pageName, 
			String initialPath, IStructuredSelection currentSelection){
		this(pageName);
	}
	
	public void createControl(Composite parent) {
		super.createControl(parent);
		
		projectListBox = super.getProjectsList();
		dirRadioButton = super.getProjectFromDirectoryRadio();
		projectListBox.addCheckStateListener(new ProjectListCheckStateListener());
	}
	
	private boolean checkHasSoc(String socName){
		if(allSoc == null)
			return false;
		for(int j = 0; j < allSoc.length; j++){
			if(allSoc[j].contains(socName)){
				return true;
			}
		}
		return false;
	}
	
	public void updateProjectsList(final String path){
		this.selectPath = path;
		super.updateProjectsList(path);	
		
		validProjects.clear();
		ProjectRecord[] selectedProjects = super.getValidProjects();//.getProjectRecords(); for eclipse 3.7
		boolean dirSelected = dirRadioButton.getSelection();
		if(dirSelected){
			for (int i = 0; i < selectedProjects.length; i++) {
				String projectSocName = getProjectSocName(selectedProjects[i]);
				if(projectSocName == null || projectSocName.equals(""))
					continue;
				if((curSocName == null && checkHasSoc(projectSocName)) ||
						(curSocName != null && curSocName.equals(projectSocName))){
					validProjects.add(selectedProjects[i]);
				}
			}
		}
		else if(!dirSelected && ArchiveFileManipulations.isTarFile(path)){
			for(int i = 0; i < selectedProjects.length; i++){
				TarFile tarFile = getSpecifiedTarSourceFile(path);
				if (tarFile == null)
					continue;
				TarLeveledStructureProvider tarProvider = new TarLeveledStructureProvider(tarFile);
				Object child = tarProvider.getRoot();
				
				collectTarProjectFromProvider(child, -1, tarProvider, selectedProjects[i]);
			}
		}
		else if (!dirSelected && ArchiveFileManipulations.isZipFile(path)){
			for(int i = 0; i < selectedProjects.length; i++){
				ZipFile zipFile = getSpecifiedZipSourceFile(path);
				if (zipFile == null)
					continue;
				ZipLeveledStructureProvider zipProvider = new ZipLeveledStructureProvider(zipFile);
				Object child = zipProvider.getRoot();
				
				collectZipProjectFromProvider(child, -1, zipProvider, selectedProjects[i]);
			}
		}
		
		projectListBox.refresh(true);
		projectListBox.setCheckedElements(new Object[0]);
		Object[] element = (Object[])(projectListBox.getCheckedElements());
		for(int i = 0; i < element.length; i++){
			projectListBox.setChecked(element[i], false);
		}
	}
	
	public ProjectRecord[] getValidProjects() {
		return (ProjectRecord[])validProjects.toArray(new ProjectRecord[validProjects.size()]);
	}
	
	private String getProjectPath(ProjectRecord project){
		if(project == null)
			return null;
		String label = project.getProjectLabel();
		final String leftP = "(";
		final String rightP = ")";
		return label.substring(label.indexOf(leftP) + 1, label.lastIndexOf(rightP));
	}
	
	private String getProjectSocName(ProjectRecord project){
		String projectPath = getProjectPath(project);
		if(projectPath == null)
			return null;
		File f = new File(projectPath + File.separatorChar + PROJECT_FILE_NAME);
		if(!f.exists() || !f.canRead())
			return null;
		MStudioProjectProperties mpp = new MStudioProjectProperties(f.getPath());
		if(mpp == null) 
			return null;
		return mpp.getProjectSocName();
	}
	
	private String getProviderSocName(InputStream is, String key){
		if(is == null)
			return "";
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			if(br == null)
				return "";
			String line = null;
			while ((line = br.readLine()) != null) {
				if(line == "")
					continue;
				int idx = line.indexOf('=');
				if(idx <= 0)
					continue;
				String temp = line.substring(0, idx);
				if(temp != null && temp.equals(key) && line.length() > idx)
					return line.substring(idx + 1);
			}
			br.close();
		}catch(Exception ex){
			System.out.println(ex.toString());
		}finally{
		}
		return "";
	}
	
	private ZipFile getSpecifiedZipSourceFile(String fileName) {
		if (fileName.length() == 0) {
			return null;
		}
		try {
			return new ZipFile(fileName);
		} catch (ZipException e) {
			MessageDialog.openError(getContainer().getShell(),
					MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.ZipImport.errorTitle"),
					MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.ZipImport.badFormat"));
		} catch (IOException e) {
			MessageDialog.openError(getContainer().getShell(),
					MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.ZipImport.errorTitle"),
					MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.ZipImport.couldNotRead"));
		}
		return null;
	}
	
	private TarFile getSpecifiedTarSourceFile(String fileName) {
		if (fileName.length() == 0) {
			return null;
		}
		try {
			return new TarFile(fileName);
		} catch (TarException e) {
			MessageDialog.openError(getContainer().getShell(),
					MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.ZipImport.errorTitle"),
					MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.TarImport.badFormat"));			
		} catch (IOException e) {
			MessageDialog.openError(getContainer().getShell(),
					MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.ZipImport.errorTitle"),
					MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.ZipImport.couldNotRead"));
		}
		return null;
	}
	
	private boolean collectZipProjectFromProvider(Object entry, int level, 
			ZipLeveledStructureProvider zipProvider, ProjectRecord project) {
		List children = zipProvider.getChildren(entry);
		if (children == null) {
			children = new ArrayList(1);
		}
		Iterator childrenEnum = children.iterator();
		while (childrenEnum.hasNext()) {
			Object child = childrenEnum.next();
			if (zipProvider.isFolder(child)){
				 if(zipProvider.getLabel(child).equals(project.getProjectName())) {
					List pchild = new ArrayList();
					pchild = zipProvider.getChildren(child);
					Iterator pChildrenEnum = pchild.iterator();
					while(pChildrenEnum.hasNext()){
						Object pc = pChildrenEnum.next();
						if(zipProvider.getLabel(pc).equals(MStudioExtenalImportProjectWizardPage.PROJECT_FILE_NAME)){
							InputStream is = zipProvider.getContents(pc);
							if(is == null)
								break;
							String socName = getProviderSocName(is, MStudioExtenalImportProjectWizardPage.SOCNAME_PROPERTY);
							if(socName == null || socName.equals(""))
								break;
							if((curSocName == null && checkHasSoc(socName)) ||
									(curSocName != null && curSocName.equals(socName))){
								validProjects.add(project);
								break;
							}
						}
					}
				 }
				 else{
					 collectZipProjectFromProvider(child, level + 1, zipProvider, project);;
				 }
			}
		}
		return false;
	}
	
	private boolean collectTarProjectFromProvider(Object entry, int level, 
			TarLeveledStructureProvider tarProvider, ProjectRecord project) {
		List children = tarProvider.getChildren(entry);
		if (children == null) {
			children = new ArrayList(1);
		}
		Iterator childrenEnum = children.iterator();
		while (childrenEnum.hasNext()) {
			Object child = childrenEnum.next();
			if (tarProvider.isFolder(child)){
				 if(tarProvider.getLabel(child).equals(project.getProjectName())) {
					List pchild = new ArrayList();
					pchild = tarProvider.getChildren(child);
					Iterator pChildrenEnum = pchild.iterator();
					while(pChildrenEnum.hasNext()){
						Object pc = pChildrenEnum.next();
						if(tarProvider.getLabel(pc).equals(MStudioExtenalImportProjectWizardPage.PROJECT_FILE_NAME)){
							InputStream is = tarProvider.getContents(pc);
							if(is == null)
								break;
							String socName = getProviderSocName(is, MStudioExtenalImportProjectWizardPage.SOCNAME_PROPERTY);
							if(socName == null || socName.equals(""))
								break;
							if((curSocName == null && checkHasSoc(socName)) ||
									(curSocName != null && curSocName.equals(socName))){
								validProjects.add(project);
								break;
							}
						}
					}
				 }
				 else{
					 collectTarProjectFromProvider(child, level + 1, tarProvider, project);;
				 }
			}
		}
		return false;
	}
	
	public String getSocName(){
		return selectSocName;
	}
	
	public boolean createProjects() {
		if(!super.createProjects())
			return false;
		MStudioPlugin.getDefault().getMStudioEnvInfo().setCurrentSoC(getSocName());
		return true;
	}
	
	private String getSocNameFromTarFile(Object entry, int level,
			TarLeveledStructureProvider tarProvider, ProjectRecord project){
		List children = tarProvider.getChildren(entry);
		if (children == null) {
			children = new ArrayList(1);
		}
		Iterator childrenEnum = children.iterator();
		while (childrenEnum.hasNext()) {
			Object child = childrenEnum.next();
			if (tarProvider.isFolder(child)){
				 if(tarProvider.getLabel(child).equals(project.getProjectName())) {
					List pchild = new ArrayList();
					pchild = tarProvider.getChildren(child);
					Iterator pChildrenEnum = pchild.iterator();
					while(pChildrenEnum.hasNext()){
						Object pc = pChildrenEnum.next();
						if(tarProvider.getLabel(pc).equals(MStudioExtenalImportProjectWizardPage.PROJECT_FILE_NAME)){
							InputStream is = tarProvider.getContents(pc);
							if(is == null)
								return null;
							//return getProviderSocName(is, MStudioExtenalImportProjectWizardPage.SOCNAME_PROPERTY);
							tarFileSocName = getProviderSocName(is, MStudioExtenalImportProjectWizardPage.SOCNAME_PROPERTY);
							return tarFileSocName;
						}
					}
				 }
				 else{
					 getSocNameFromTarFile(child, level + 1, tarProvider, project);;
				 }
			}
		}
		return tarFileSocName;		
	}
	
	private String getSocNameFromZipFile(Object entry, int level, 
			ZipLeveledStructureProvider zipProvider, ProjectRecord project){
		List children = zipProvider.getChildren(entry);
		if (children == null) {
			children = new ArrayList(1);
		}
		Iterator childrenEnum = children.iterator();
		while (childrenEnum.hasNext()) {
			Object child = childrenEnum.next();
			if (zipProvider.isFolder(child)){
				//find the current project directory
				 if(zipProvider.getLabel(child).equals(project.getProjectName())) {
					List pchild = new ArrayList();
					pchild = zipProvider.getChildren(child);
					Iterator pChildrenEnum = pchild.iterator();
					//find the .hproject file from children files of the project directory
					while(pChildrenEnum.hasNext()){
						Object pc = pChildrenEnum.next();
						//find the .hproject file
						if(zipProvider.getLabel(pc).equals(MStudioExtenalImportProjectWizardPage.PROJECT_FILE_NAME)){
							InputStream is = zipProvider.getContents(pc);
							//get the inputstream of read the .hproject error
							if(is == null)
								return null;
							zipFileSocName = getProviderSocName(is, MStudioExtenalImportProjectWizardPage.SOCNAME_PROPERTY);
							return zipFileSocName;
						}
					}
				 }
				 else{
					 // read the children file from the directory
					 getSocNameFromZipFile(child, level + 1, zipProvider, project);;
				 }
			}
		}
		return zipFileSocName;		
	}
	
	public class ProjectListCheckStateListener implements ICheckStateListener{
		@Override
		public void checkStateChanged(CheckStateChangedEvent event) {
			curSocName = null;
			
			if(projectListBox.getCheckedElements().length <= 0){
				selectSocName = null;
				return;
			}
			if(curSocName != null || !event.getChecked())
				return;
			boolean dirSelected = dirRadioButton.getSelection();
			ProjectRecord selectedItem = (ProjectRecord)event.getElement();
			String socName = null;
			if(dirSelected){
				 socName = getProjectSocName(selectedItem);
			}
			if(!dirSelected && selectPath != null && !selectPath.equals("") 
					&& ArchiveFileManipulations.isTarFile(selectPath)){
				TarFile tarFile = getSpecifiedTarSourceFile(selectPath);
				if (tarFile == null)
					socName = null;
				else{
					TarLeveledStructureProvider tarProvider = new TarLeveledStructureProvider(tarFile);
					Object child = tarProvider.getRoot();
					socName = getSocNameFromTarFile(child, -1, tarProvider, selectedItem);
				}
			}
			else if(!dirSelected && selectPath != null && !selectPath.equals("") 
					&& ArchiveFileManipulations.isZipFile(selectPath)){
				ZipFile zipFile = getSpecifiedZipSourceFile(selectPath);
				if (zipFile == null)
					socName = null;
				else{
					ZipLeveledStructureProvider zipProvider = new ZipLeveledStructureProvider(zipFile);
					Object child = zipProvider.getRoot();
					getSocNameFromZipFile(child, -1, zipProvider,selectedItem);
				}
			}
			if(socName == null){
				MessageDialog.openError(getShell(), 
						MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.readSocName.errorTitle"), 
						MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.readSocName.errorContent"));
				projectListBox.setChecked(selectedItem, false);
				return;
			}
			if(!checkHasSoc(socName)){
				MessageDialog.openError(getShell(), 
						MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.checkSocName.errorTitle"), 
						MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.checkSocName.errorContent"+ socName));
				projectListBox.setChecked(selectedItem, false);
			}
			else{
				if(selectSocName == null)
					selectSocName = socName;
				else{
					if(!selectSocName.equals(socName)){
						MessageDialog.openError(getShell(), 
								MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.selectProject.errorTitle"), 
								MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.selectProject.errorContent"));
						projectListBox.setChecked(selectedItem, false);
					}
				}
			}
			
			setPageComplete(projectListBox.getCheckedElements().length > 0);
		}
	}
}
