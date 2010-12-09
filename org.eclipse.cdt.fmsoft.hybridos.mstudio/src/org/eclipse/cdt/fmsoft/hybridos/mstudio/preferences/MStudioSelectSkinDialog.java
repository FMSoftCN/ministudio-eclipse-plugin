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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

class SkinFilter implements FilenameFilter {

	public boolean accept(File file, String fname) {
		return (file.isDirectory());
	}
}

public class MStudioSelectSkinDialog extends Dialog {

	private static String SKIN_PATH = "/usr/local/share/gvfb/res/skin/";
	private Shell shell;
	private Button cancelBtn;
	private Button okBtn;
	public static boolean clickButton = false;
	String skinName = null;
	private Table skinTable = null;
	private Label imgLabel = null;
	private CheckboxTableViewer ctv = null;
	private List<String> imageList = null;

	public MStudioSelectSkinDialog(Shell parent, int style) {
		super(parent, style);
	}

	public MStudioSelectSkinDialog(Shell parent) {
		this(parent, 0); // your default style bits go here (not the Shell's
		// style bits)
	}

	private void createContents() {

		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setSize(500, 400);
		shell.setText(MStudioMessages
				.getString("MStudioSelectSkinDialog.selectSkinTitle"));
		shell.setLayout(new GridLayout());
		shell.setLayoutData(new GridData());

		Composite tableCom = new Composite(shell, SWT.NONE);
		GridLayout layout = new GridLayout(2, true);
		layout.marginWidth = 10;
		layout.horizontalSpacing = 50;
		tableCom.setLayout(layout);
		tableCom.setLayoutData(new GridData(GridData.FILL_BOTH));

		// create skinTable
		skinTable = new Table(tableCom, SWT.BORDER | SWT.CHECK | SWT.V_SCROLL
				| SWT.H_SCROLL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		// gd.verticalSpan = 10;
		skinTable.setLayoutData(gd);

		ctv = new CheckboxTableViewer(skinTable);
		ctv.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
			}
		});
		ctv.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				previewSkin(event);
			}
		});

		initSkinTable();

		// create image label
		imgLabel = new Label(tableCom, SWT.NONE);
		// MStudioPlugin.getDefault();
		// Image img =
		// MStudioPlugin.getImageDescriptor("icons/hybridos-logo.png"
		// ).createImage();
		// imgLabel.setImage(img);
		GridData imageGd = new GridData(GridData.FILL_BOTH);
		imgLabel.setLayoutData(imageGd);

		Composite btnCom = new Composite(shell, SWT.NONE);
		btnCom.setLayout(new GridLayout(2, false));
		btnCom.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));

		// create ok button
		okBtn = new Button(btnCom, SWT.NONE);
		okBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		okBtn.setText(MStudioMessages
				.getString("MStudioSelectSkinDialog.okButtonLabel"));
		okBtn.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				// if (select none) {
				// messagebox;
				// return;
				// }// TODO
				setSkinName();
				shell.close();
				shell.dispose();
			}
		});

		// create cancel button
		cancelBtn = new Button(btnCom, SWT.NONE);
		cancelBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		cancelBtn.setText(MStudioMessages
				.getString("MStudioSelectSkinDialog.cancelButtonLabel"));
		cancelBtn.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.close();
				shell.dispose();
			}
		});

	}

	public Object open() {

		createContents();
		shell.open();
		// shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return skinName;
	}

	// get all valid skin file
	private String[] getSkinFile() {
		File skinFile = new File(SKIN_PATH);
		return skinFile.list(new SkinFilter());
	}

	private void initSkinTable() {
		// String[] imageNames = null;
		// imageList = new ArrayList<String>();
		// imageList.add("icons/mginit.gif");
		// imageNames = (String[])imageList.toArray(new
		// String[imageList.size()]);
		String[] imageNames = getSkinFile();
		if (null != imageNames && imageNames.length > 0) {
			ctv.add(imageNames);
			// ctv.setCheckedElements(imageNames);
		} else {
			// messagebox
		}
	}

	private void setSkinName() {
		TableItem[] items = skinTable.getItems();
		if (null == items) {
			return;
		}
		for (int i = 0; i < items.length; i++) {
			if (items[i].getChecked()) {
				skinName = items[i].getText();
				break;
			}
		}

	}

	private void previewSkin(SelectionChangedEvent event) {

		String name = SKIN_PATH
				+ (String) ((IStructuredSelection) (event.getSelection()))
						.getFirstElement();

		try {
			File file = new File(name);
			URL url = file.toURI().toURL();
			ImageDescriptor imageDescriptor = ImageDescriptor
					.createFromURL(url);
			Image img = imageDescriptor.createImage();
			imgLabel.setImage(img);
		} catch (Exception e) {
			// message
		} finally {

		}
	}
}
