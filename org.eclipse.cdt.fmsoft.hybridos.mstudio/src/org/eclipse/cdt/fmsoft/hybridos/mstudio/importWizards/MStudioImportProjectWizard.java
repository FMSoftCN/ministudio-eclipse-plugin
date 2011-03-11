/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.fmsoft.hybridos.mstudio.importWizards;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

public class MStudioImportProjectWizard extends Wizard implements IImportWizard {
	
	private MStudioExtenalImportProjectWizardPage mainPage;

	private static final String EXTERNAL_PROJECT_SECTION = "MStudioImportProjectWizard";
	private IStructuredSelection currentSelection = null;
	private String initialPath = null;
	
	public MStudioImportProjectWizard() {
		super();
	}

	public MStudioImportProjectWizard(String initialPath)
    {
        super();
        this.initialPath = initialPath;
        setNeedsProgressMonitor(true);
        IDialogSettings workbenchSettings = IDEWorkbenchPlugin.getDefault()
        		.getDialogSettings();
        
		IDialogSettings wizardSettings = workbenchSettings
		        .getSection(EXTERNAL_PROJECT_SECTION);
		if (wizardSettings == null) {
			wizardSettings = workbenchSettings
		            .addNewSection(EXTERNAL_PROJECT_SECTION);
		}
		setDialogSettings(wizardSettings);        
    }
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {
            return mainPage.createProjects();
	}
	 
	public boolean performCancel() {
    	mainPage.performCancel();
        return true;
    }
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle(MStudioMessages.getString("MStudioImportProjectWizard.windowTitle"));
        setDefaultPageImageDescriptor(
				IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/importproj_wiz.png"));
        this.currentSelection = currentSelection;
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizard#addPages()
     */
    public void addPages() {
        super.addPages(); 
        mainPage = new MStudioExtenalImportProjectWizardPage(
				MStudioMessages.getString("MStudioExtenalImportProjectWizardPage.pageTitle"),
				initialPath, currentSelection);
        addPage(mainPage);
    }

}
