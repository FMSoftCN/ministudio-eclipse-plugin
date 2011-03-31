package org.eclipse.cdt.fmsoft.hybridos.mstudio;
 
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;

public class MStudioStartup implements IStartup {
    public void earlyStartup() {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
            	MStudioEnvInfo.getInstance().updateMginitMemus();
            }
        });
     }
}
