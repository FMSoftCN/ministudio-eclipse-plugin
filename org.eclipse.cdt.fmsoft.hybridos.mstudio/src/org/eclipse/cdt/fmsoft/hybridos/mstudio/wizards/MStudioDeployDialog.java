package org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards;

import java.awt.Dimension;
import java.awt.Toolkit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class MStudioDeployDialog extends Dialog{

	private Shell shell;	
	private ProgressBar progress;
	private Label content;
	private Label description;
	private Text monitor;
	private Button cancelButton;
	public static boolean clickButton = false;
	
	public MStudioDeployDialog(Shell parent,int sytyle){
		super(parent);
	}
	public MStudioDeployDialog(Shell parent) {
		super(parent);
		shell = new Shell(parent, SWT.TITLE|SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL|SWT.SYSTEM_MODAL|SWT.PRIMARY_MODAL);
		shell.setLayout(new GridLayout());
		shell.setLayoutData(new GridData());
		shell.setActive();
		Dimension dem=Toolkit.getDefaultToolkit().getScreenSize();  
		int sHeight=dem.height;  
		int sWidth=dem.width;  
		int fHeight=shell.getSize().y;  
		int fWidth=shell.getSize().x;  
		shell.setBounds((sWidth-fWidth)/2, (sHeight-fHeight)/2, 400, 150);
		
		content = new Label(shell,SWT.WRAP);
		content.setLayoutData(new GridData(GridData.FILL_BOTH));
		if(content.getText()==null || content.getText()=="")
			content.setVisible(false);
		
		progress = new ProgressBar(shell, SWT.SMOOTH);
		progress.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
		description = new Label(shell,SWT.WRAP);
		description.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		cancelButton=new Button(shell,SWT.NONE);
		cancelButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		cancelButton.setText("cancel");
		cancelButton.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.close();
				shell.dispose();							
			}});
		
	}
	
	public void update(){
		shell.update();
	}
	public void setProgressvalue(final int value){	
		progress.getDisplay().asyncExec(new Runnable(){
			public void run(){				
				progress.setSelection(value);	
			}
		});		
	}
	public void setDescription(final String value){	
		description.getDisplay().asyncExec(new Runnable(){
			public void run(){
				description.setText(value);
			}
		});			
	}
	public void setTitle(String title){
		
	}
	public Shell getShell(){
		return this.shell;
	}
	public Display getDisplay(){
		return this.shell.getDisplay();
	}
	
	public void open(){		
		shell.open();
	}
	public void close(){
		shell.close();
	}
	public ProgressBar getProgressBar(){
		return this.progress;
	}
	public Label getDescription(){
		return this.description;
	}
	public Text getHistoryText(){
		return this.monitor;
	}
	public Label getContent(){
		return this.content;
	}
}
