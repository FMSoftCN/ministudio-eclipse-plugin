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

package org.eclipse.cdt.fmsoft.hybridos.mstudio.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;


public class MStudioCommonPreferencePage extends PreferencePage
	implements IWorkbenchPreferencePage {

	private final static String MSCPP_HYBRIDOS_LOGO = "icons/hybridos-logo.png";
	private final static String LSP = System.getProperty("line.separator");
	private final static String MSCPP_HYBRIDOS_INTRO =
		"HybridOS is an operating system dedicated to " + LSP
		+ "embedded device. It is based on Linux kernel, " + LSP
		+ "stable open source software (e.g. Gtk+, SDL, " + LSP
		+ "SQLite, and so on),and FMSoft's mature embedded " + LSP
		+ "software technologies, like embedded windowing " + LSP
		+ "system (MiniGUI), embedded web browser (mDolphin)," + LSP
		+ " and embedded J2SE solution.";

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
		//setSize(new Point (200, 400));
	}

	protected Control createContents(Composite parent) {

		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new FillLayout(SWT.VERTICAL));

		Label imgLabel = new Label(composite, SWT.NONE);
		if (null != imgLabel) {
			MStudioPlugin.getDefault();
			Image img = MStudioPlugin.getImageDescriptor(MSCPP_HYBRIDOS_LOGO).createImage();
			imgLabel.setImage(img);
		}

		Label intro = new Label(composite, SWT.WRAP);
		if (null != intro) {
			intro.setFont(parent.getFont());
			intro.setText(MSCPP_HYBRIDOS_INTRO);
			intro.setSize(200, 200);
		}

		return composite;
	}
}

