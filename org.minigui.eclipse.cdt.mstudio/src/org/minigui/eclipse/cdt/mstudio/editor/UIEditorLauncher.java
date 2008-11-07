package org.minigui.eclipse.cdt.mstudio.editor;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorLauncher;

public class UIEditorLauncher implements IEditorLauncher {

	@Override
	public void open(IPath file) {
		// TODO Auto-generated method stub
		/*Open file*/
		System.out.println("open file:"+file);
	}

}
