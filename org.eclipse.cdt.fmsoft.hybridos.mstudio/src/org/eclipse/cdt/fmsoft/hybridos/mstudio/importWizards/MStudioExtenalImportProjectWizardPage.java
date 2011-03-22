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
import org.eclipse.jface.viewers.IStructuredSelection;
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
	
	public void updateProjectsList(final String path){
		super.updateProjectsList(path);	
		
		validProjects.clear();
		final String curSocName = MStudioPlugin.getDefault().getMStudioEnvInfo().getCurSoCName();
		ProjectRecord[] selectedProjects = super.getValidProjects();
		final boolean dirSelected = super.getProjectFromDirectoryRadio().getSelection();
		if(dirSelected){
			for (int i = 0; i < selectedProjects.length; i++) {
				String projectPath = getProjectPath(selectedProjects[i]);
				if(projectPath == null)
					continue;
				File f = new File(projectPath + File.separatorChar + PROJECT_FILE_NAME);
				if(!f.exists() || !f.canRead())
					continue;
				else{
					MStudioProjectProperties mpp = new MStudioProjectProperties(f.getPath());
					if(mpp == null) 
						continue;
					if(curSocName.equals(mpp.getProjectSocName())){
						validProjects.add(selectedProjects[i]);					
					}
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
		super.getProjectsList().refresh(true);
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
	
	public boolean collectZipProjectFromProvider(Object entry, int level, 
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
							if(is != null &&
								getProviderSocName(is, MStudioExtenalImportProjectWizardPage.SOCNAME_PROPERTY).
										equals(MStudioPlugin.getDefault().getMStudioEnvInfo().getCurSoCName())){
									validProjects.add(project);
								return true;
							}
							return false;
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
	
	public boolean collectTarProjectFromProvider(Object entry, int level, 
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
							if(is != null &&
								getProviderSocName(is, MStudioExtenalImportProjectWizardPage.SOCNAME_PROPERTY).
										equals(MStudioPlugin.getDefault().getMStudioEnvInfo().getCurSoCName())){
									validProjects.add(project);
								return true;
							}
							return false;
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
}
