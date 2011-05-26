package org.minigui.eclipse.cdt.mstudio.wizards;

import org.eclipse.jface.wizard.Wizard;

public class MStudioVersionWizard	extends Wizard {
		String versionName;
		String binPath;

		MStudioVersionWizardPage wizardPage;

		public boolean performFinish() {
			versionName = wizardPage.getVersionName();
			binPath = wizardPage.getBinPath();
			return true;
		}

		public void addPages() {
			wizardPage = new MStudioVersionWizardPage("miniStudio Version");
			addPage(wizardPage);
		}
		
		public String getVersionName() {
			return versionName;
		}

		public void setVersionName(String versionName) {
			wizardPage.setVersionName(versionName);
		}

		public String getBinPath() {
			return binPath;
		}

		public void setBinPath(String binPath) {
			wizardPage.setBinPath(binPath);
		}

}
