package org.eclipse.cdt.fmsoft.hybridos.mstudio.importWizards;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProjectProperties;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage;

public class MStudioExtenalImportProjectWizardPage extends WizardProjectsImportPage{
	private static final String PROJECT_FILE_NAME = ".hproject";
	
	public MStudioExtenalImportProjectWizardPage(){
		this("wizardExternalProjectsPage");
	}
	public MStudioExtenalImportProjectWizardPage(String pageName){
		super(pageName);
		setPageComplete(false);
		setTitle(MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.ImportProjectsTitle"));
		setDescription(MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.ImportProjectsDescription"));
	}
	public MStudioExtenalImportProjectWizardPage(String pageName, String initialPath, IStructuredSelection currentSelection){
		this(pageName);
	}
	
	public void updateProjectsList(final String path){
		super.updateProjectsList(path);	
		
		super.getProjectsList().refresh(true);
	}
	
	public ProjectRecord[] getValidProjects() {
		ProjectRecord[] selectedProjects = super.getValidProjects();
		List<ProjectRecord> validProjects = new ArrayList<ProjectRecord>();
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
				if(MStudioPlugin.getDefault().getMStudioEnvInfo().getCurSoCName().
						equals(mpp.getProjectSocName())){
					validProjects.add(selectedProjects[i]);					
				}
			}
		}
		return (ProjectRecord[]) validProjects.toArray(new ProjectRecord[validProjects.size()]);
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
				return null;
			String line = null;
			while ((line = br.readLine()) != null) {
				if(line == "")
					continue;
				int idx = line.indexOf('=');
				String temp = line.substring(0, idx);
				if(temp != null && temp.equals(key))
					return line.substring(idx + 1);
			}
			br.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{			
		}
		return "";
	}
}
