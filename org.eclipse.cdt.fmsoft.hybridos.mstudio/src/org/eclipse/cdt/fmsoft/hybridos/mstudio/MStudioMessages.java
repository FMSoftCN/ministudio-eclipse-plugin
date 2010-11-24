/*********************************************************************
 * Copyright (C) 2005 - 2010, Beijing FMSoft Technology Co., Ltd.
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

import java.text.MessageFormat;

import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * @since 2.0
 */
public class MStudioMessages {

	// Bundle ID
	private static final String BUNDLE_ID = "org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioResources"; 

	//Resource bundle.
	private static ResourceBundle resourceBundle = null;

	static {
		try {
			resourceBundle = ResourceBundle.getBundle(BUNDLE_ID);
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	public static String getFormattedString(String key, String arg) {
		return MessageFormat.format(getString(key), (Object[])new String[] { arg });
	}

	public static String getFormattedString(String key, String[] args) {
		return MessageFormat.format(getString(key), (Object[])args);
	}

	public static String getString(String key) {
		try {
			return resourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
		} catch (NullPointerException e) {
			return "@#" + key + "#@"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private MStudioMessages() {
		// No constructor
	}
}

