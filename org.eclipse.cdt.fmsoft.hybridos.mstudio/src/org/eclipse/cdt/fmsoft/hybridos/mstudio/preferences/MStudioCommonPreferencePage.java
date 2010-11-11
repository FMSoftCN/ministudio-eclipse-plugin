package org.eclipse.cdt.fmsoft.hybridos.mstudio.preferences;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.project.MStudioProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

public class MStudioCommonPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	private String hybridosIntroduction = "HybridOS is an operating system dedicated to embedded device. " +
	"It is based on Linux kernel, stable open source software (e.g. Gtk+, SDL, SQLite, and so on), " +
	"and FMSoft's mature embedded software technologies, like embedded windowing system (MiniGUI), " +
	"embedded web browser (mDolphin), and embedded J2SE solution."; 

	public MStudioCommonPreferencePage() {
		noDefaultAndApplyButton();
	}

	public MStudioCommonPreferencePage(String title) {
		super(title);
		noDefaultAndApplyButton();
	}

	public MStudioCommonPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
		noDefaultAndApplyButton();
	}
	
	public void init(IWorkbench workbench) {

	}
	
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		
		Label imgLabel = new Label(composite, SWT.NONE);
		if (null != imgLabel) {
			MStudioPlugin.getDefault();
			Image img = MStudioPlugin.getImageDescriptor("icons/hybridos-logo.png").createImage();
			imgLabel.setImage(img);			
		}
		
		Label intro = new Label(composite, SWT.WRAP);
		if (null != intro){
			intro.setFont(parent.getFont());
         intro.setText(hybridosIntroduction);
		}
		return composite;
	}

}
