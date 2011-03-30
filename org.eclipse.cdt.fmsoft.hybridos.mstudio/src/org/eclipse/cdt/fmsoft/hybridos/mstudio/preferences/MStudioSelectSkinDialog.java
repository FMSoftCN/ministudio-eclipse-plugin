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
import java.net.URL;

import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.dialogs.MessageDialog;
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
		File skinFile = new File(file.getPath() + File.separator + fname);
		if (skinFile.isFile() && fname.toLowerCase().endsWith(
				MStudioSelectSkinDialog.SKIN_SUFFIX_NAME)) {
			File pngFile = new File(file.getPath() + File.separator 
					+ fname.substring(0, fname.length() - MStudioSelectSkinDialog.SKIN_SUFFIX_NAME.length())
					+ MStudioSelectSkinDialog.IMAGE_SUFFIX_NAME);
			return pngFile.isFile();
		}

		return false;
	}
}

public class MStudioSelectSkinDialog extends Dialog {

	protected static String SKIN_SUFFIX_NAME = ".skin";
	protected static String IMAGE_SUFFIX_NAME = ".png";
	private Shell shell = null;
	private Button cancelBtn = null;
	private Button okBtn = null;
	private String selectedSkinName = null;
	private Table skinTable = null;
	private CheckboxTableViewer ctv = null;
	private Label imgLabel = null;
	public String skinDefaultName = null;
	private int imageWidth = 200;
	private int imageHeight = 200;

	public MStudioSelectSkinDialog(Shell parent, int style) {
		super(parent, style);
	}

	public MStudioSelectSkinDialog(Shell parent) {
		this(parent, 0);
	}

	private void createContents() {

		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setSize(420, 320);
		shell.setText(MStudioMessages.getString("MStudioSelectSkinDialog.selectSkinTitle"));
		shell.setLayout(new GridLayout());
		shell.setLayoutData(new GridData());

		Composite tableCom = new Composite(shell, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.horizontalSpacing = 20;
		tableCom.setLayout(layout);
		tableCom.setLayoutData(new GridData(GridData.FILL_BOTH));

		// create skinTable
		skinTable = new Table(tableCom, SWT.BORDER | SWT.CHECK | SWT.V_SCROLL
				| SWT.H_SCROLL);
		GridData gd = new GridData(140, 200);
		skinTable.setLayoutData(gd);

		ctv = new CheckboxTableViewer(skinTable);
		ctv.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				ctv.setAllChecked(false);
				ctv.setChecked(event.getElement(), event.getChecked());
			}
		});
		ctv.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				previewSkin(event);
			}
		});

		// create image label
		imgLabel = new Label(tableCom, SWT.NONE);
		GridData imageGd = new GridData(imageWidth, imageHeight);
		imgLabel.setLayoutData(imageGd);

		Composite btnCom = new Composite(shell, SWT.NONE);
		btnCom.setLayout(new GridLayout(2, true));
		btnCom.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));

		initSkinTable();
		initImageLabel();

		// create ok button
		okBtn = new Button(btnCom, SWT.NONE);
		okBtn.setLayoutData(new GridData(90, 30));
		okBtn.setText(MStudioMessages.getString("MStudioSelectSkinDialog.okButtonLabel"));
		okBtn.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!setSkinName()) {
					return;
				}
				shell.close();
				shell.dispose();
			}
		});

		// create cancel button
		cancelBtn = new Button(btnCom, SWT.NONE);
		cancelBtn.setLayoutData(new GridData(90, 30));
		cancelBtn.setText(MStudioMessages.getString("MStudioSelectSkinDialog.cancelButtonLabel"));
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
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return selectedSkinName;
	}
	
	public static boolean hasSkinFile() {
		File skinFile = new File(MStudioSoCPreferencePage.SKIN_PATH);
		String[] names = skinFile.list(new SkinFilter());
		if (null == names || names.length == 0) {
			return false;
		}
		
		return true;
	}
	
	// get all valid skin file
	private String[] getSkinFile() {
		File skinFile = new File(MStudioSoCPreferencePage.SKIN_PATH);
		String[] names = skinFile.list(new SkinFilter());
		if (null == names) {
			return null;
		}
		for (int i = 0; i < names.length; i++) {
			if (names[i].length() > SKIN_SUFFIX_NAME.length())
				names[i] = names[i].substring(0, names[i].length() - SKIN_SUFFIX_NAME.length());
		}
		return names;
	}

	private void initSkinTable() {
		
		String[] shortSkinNames = getSkinFile();
		if (null != shortSkinNames && shortSkinNames.length > 0) {
			ctv.add(shortSkinNames);
		} else {
			return;
		}

		// set the skin name's default state
		if (null == skinDefaultName
				|| skinDefaultName.length() < SKIN_SUFFIX_NAME.length()) {
			return;
		}
		
		String skinDefaultImg = skinDefaultName.substring(0, skinDefaultName.length()
				- SKIN_SUFFIX_NAME.length());
		if (null == skinDefaultImg) {
			return;
		}
		
		for (int i = 0; i < shortSkinNames.length; i++) {
			if (shortSkinNames[i].equals(skinDefaultImg)) {
				ctv.setChecked(shortSkinNames[i], true);
				skinTable.setSelection(i);
				break;
			}
		}
	}

	private void initImageLabel() {

		Object[] imageNames = ctv.getCheckedElements();
		if (null == imageNames || imageNames.length <= 0) {
			return;
		}
		String name = MStudioSoCPreferencePage.SKIN_PATH + imageNames[0] + IMAGE_SUFFIX_NAME;

		try {
			File file = new File(name);
			URL url = file.toURI().toURL();
			ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(url);
			Image img = imageDescriptor.createImage();
			imgLabel.setImage(img);

			ImageDescriptor autofitImageDescriptor = ImageDescriptor.createFromImageData(
					img.getImageData().scaledTo(imageWidth, imageHeight));
			Image autofitImg = autofitImageDescriptor.createImage();

			imgLabel.setImage(autofitImg);
		} catch (Exception e) {
			MessageDialog.openError(shell,
					MStudioMessages.getString("MStudioSelectSkinDialog.error.title"),
					e.getMessage());
		}
	}

	private boolean setSkinName() {

		TableItem[] items = skinTable.getItems();
		if (null == items) {
			MessageDialog.openError(shell,
					MStudioMessages.getString("MStudioSelectSkinDialog.error.title"),
					MStudioMessages.getString("MStudioSelectSkinDialog.error.selectSkinName"));
			return false;
		}

		selectedSkinName = "";
		for (int i = 0; i < items.length; i++) {
			if (null != items[i] && items[i].getChecked()) {
				selectedSkinName = items[i].getText() + SKIN_SUFFIX_NAME;
				break;
			}
		}
		return true;
	}

	private void previewSkin(SelectionChangedEvent event) {

		String skinName = (String) ((IStructuredSelection) (event.getSelection())).getFirstElement();
		if (null == skinName) {
			return;
		}
		
		String name = MStudioSoCPreferencePage.SKIN_PATH + skinName + IMAGE_SUFFIX_NAME;
		try {
			File file = new File(name);
			if (!file.isFile()) {
				MessageDialog.openError(shell, MStudioMessages.getString(
						"MStudioSelectSkinDialog.error.title"),
						MStudioMessages.getString("MStudioSelectSkinDialog.error.getSkinImg"));
			}
			URL url = file.toURI().toURL();
			ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(url);
			Image img = imageDescriptor.createImage();

			ImageDescriptor autofitImageDescriptor = ImageDescriptor.createFromImageData(
					img.getImageData().scaledTo(imageWidth, imageHeight));
			Image autofitImg = autofitImageDescriptor.createImage();

			imgLabel.setImage(autofitImg);
		} catch (Exception e) {
			MessageDialog.openError(shell, MStudioMessages.getString(
					"MStudioSelectSkinDialog.error.title"), e.getMessage());
		}
	}
}
