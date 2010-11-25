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

package org.eclipse.cdt.fmsoft.hybridos.mstudio;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.Path;

import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.preferences.MStudioToolsPreferencePage;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.preferences.MStudioPreferenceConstants;


public class MStudioCheckUpdate extends AbstractHandler implements IHandler {

	private final static String MSCU_BASE_URL = "http://auth.minigui.com/mstudio/update/update.php?msver=";
	private final static String MSCU_GUIBUILDER = "guibuilder";
	private final static String MSCU_EMPTY = "";

	private static String verDesc = "Build-Version:";
	private static String verChar = " -v";
	private static String builderVersion = null;
	private static String pluginVersion = null;
	private static StringBuffer builderCmd = new StringBuffer(MSCU_GUIBUILDER);
	
	private void parseVersionLine(String line) {
		StringBuffer buf = new StringBuffer();
		buf.append(line);
		buf.delete(0, verDesc.length());
		builderVersion = buf.toString().trim();
	}

	private boolean getBuilderVersion() {
		String defVersion = MStudioPlugin.getDefault().getPreferenceStore()
							.getString(MStudioPreferenceConstants.MSVERSION_DEFAULT);
		String binPath = MStudioToolsPreferencePage.getMStudioBinPath(defVersion);
		Path cmd = null;

		if (System.getProperty("os.name").toLowerCase().indexOf("window") >= 0) {
			builderCmd.append(".exe");
		}

		if (binPath == null || binPath.equals(MSCU_EMPTY)) {
			binPath = System.getenv("GUIBUILDER_PATH");
		}
		if (binPath == null || binPath.equals(MSCU_EMPTY)) {
			cmd = new Path (builderCmd.toString());
		} else {
			cmd = new Path (binPath + File.separatorChar + builderCmd.toString());
		}

		Runtime r = Runtime.getRuntime();
		Process p = null; 
		try {
			p = r.exec(cmd.toOSString() + verChar);
			BufferedReader is; 
			is = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;

			while ((line = is.readLine()) != null) {
				if (line.startsWith(verDesc)) {
					parseVersionLine(line);
					return true;
				}
				try {
				  p.waitFor();  // wait for process to complete
				} catch (InterruptedException e) {
				  System.err.println(e);
				  return false;
				}
			}
		} catch (IOException e) {
			return false;
		}
		
		return false;
	}

	private void getPluginVersion() {
		Bundle bundle = MStudioPlugin.getDefault().getBundle();
		String version = (String) bundle.getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION);
		Version v = org.osgi.framework.Version.parseVersion(version);
		pluginVersion = v.toString();
	}

	private boolean getVersion() {
		getPluginVersion();
		return getBuilderVersion();
	}

	private String getUrl() {
		return String.format("%s%s%s-%s%s", 
				MSCU_BASE_URL, MSCU_GUIBUILDER, builderVersion, "msplus", pluginVersion);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		if (!getVersion()) {
			MessageDialog.openInformation(
					window.getShell(),
					"mStudio Plug-in",
					MStudioMessages.getString("MStudioUpdateError.desc"));
			return null;
		}

		String url = getUrl();
		Shell shell = new Shell(window.getShell());
		shell.setLayout(new FillLayout());
		shell.setText(MStudioMessages.getString("MStudioUpdateCaption.desc"));

		Browser browser = new Browser(shell, SWT.BORDER);
		FormData data = new FormData();
		data.left = new FormAttachment(0, 5);
		data.right = new FormAttachment(100, -5);
		browser.setLayoutData(data);
		browser.setUrl(url);

		shell.open();

		return null;
	}
}

