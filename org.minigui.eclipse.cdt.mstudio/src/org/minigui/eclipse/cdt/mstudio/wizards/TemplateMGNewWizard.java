
package org.minigui.eclipse.cdt.mstudio.wizards;

/*
import java.util.ArrayList;

import org.eclipse.cdt.core.templateengine.TemplateInfo;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.AbstractCWizard;
import org.eclipse.cdt.ui.templateengine.Template;
import org.eclipse.cdt.ui.templateengine.TemplateEngineUI;
import org.eclipse.cdt.ui.wizards.EntryDescriptor;
import org.eclipse.jface.wizard.IWizard;


public class TemplateMGNewWizard extends AbstractCWizard {
	private static final String NAME = "MiniGUI Project"; 
	private static final String ID = "org.minigui.eclipse.cdt.mstudio.projectType"; 
	public static final String EMPTY_PROJECT = "Empty MiniGUI Project";
	
	public EntryDescriptor[] createItems(boolean supportedOnly, IWizard wizard) {
		
		Template[] templates = TemplateEngineUI.getDefault().getTemplates();
		ArrayList<EntryDescriptor> items = new ArrayList<EntryDescriptor>();
		
		MGWizardHandler h = new MGWizardHandler(parent, wizard);
		h.addTc(null); 
		IToolChain[] tcs = ManagedBuildManager.getRealToolChains();
		for (int i=0; i<tcs.length; i++)
			if (isValid(tcs[i], supportedOnly, wizard)) {
			System.out.println("add tool chain "+ tcs[i]);
				h.addTc(tcs[i]);
		}
		items.add(new EntryDescriptor(ID, null, NAME, true, h, null));	
		
		for (int k=0; k < templates.length; k++) {
			TemplateInfo templateInfo = templates[k].getTemplateInfo();
			if(templateInfo.getProjectType().equals(ID))
			{
				items.add(new EntryDescriptor(templates[k].getTemplateId(),
					ID,templates[k].getLabel(),templateInfo.isCategory(),h,null));
			}
		}
		return items.toArray(new EntryDescriptor[items.size()]);
	}
}
*/

import java.util.ArrayList;
import java.util.Arrays;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyManager;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyType;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.core.BuildListComparator;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.properties.Messages;
import org.eclipse.cdt.managedbuilder.ui.wizards.AbstractCWizard;
import org.eclipse.cdt.ui.wizards.EntryDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.cdt.ui.templateengine.Template;
import org.eclipse.cdt.ui.templateengine.TemplateEngineUI;
import org.eclipse.cdt.core.templateengine.TemplateInfo;

public class TemplateMGNewWizard extends AbstractCWizard {
	
	private static final String MG_TYPE = "org.minigui.eclipse.cdt.mstudio.projectType";
	public static final String OTHERS_LABEL = Messages.getString("CNewWizard.0");  
	public static final String EMPTY_PROJECT = "Empty MiniGUI Project";
	
	public EntryDescriptor[] createItems(boolean supportedOnly, IWizard wizard) {
		IBuildPropertyManager bpm = ManagedBuildManager.getBuildPropertyManager();
		//IBuildPropertyType bpt = bpm.getPropertyType(MBSWizardHandler.ARTIFACT);
		IBuildPropertyType bpt = bpm.getPropertyType(MG_TYPE);
		IBuildPropertyValue[] vs = bpt.getSupportedValues();
		Arrays.sort(vs, BuildListComparator.getInstance());
		System.out.println("---------------vs.length============="+vs.length);
		
		Template[] templates = TemplateEngineUI.getDefault().getTemplates();
		ArrayList<EntryDescriptor> items = new ArrayList<EntryDescriptor>();
		
		for (int i=0; i<vs.length; i++) {
			IToolChain[] tcs = ManagedBuildManager.getExtensionsToolChains(MGWizardHandler.ARTIFACT, vs[i].getId(), false);
			if (tcs == null || tcs.length == 0) continue;
			
			System.out.println("vs[i].getId()="+vs[i].getId());
			System.out.println("vs[i].getName()="+vs[i].getName());
			
			MGWizardHandler h = new MGWizardHandler(vs[i], parent, wizard);
			h.addTc(null);
			for (int j=0; j<tcs.length; j++) {
				if (isValid(tcs[j], supportedOnly, wizard)) {
					System.out.println("add tool chain "+ tcs[j]);
					h.addTc(tcs[j]);
				}
			}
			System.out.println(" tool chain count ="+h.getToolChainsCount());
			if (h.getToolChainsCount() > 0) {
				// The project category item.
				items.add(new EntryDescriptor(vs[i].getId(), null, vs[i].getName(), true, h, null));

				for (int k=0; k < templates.length; k++) {
					TemplateInfo templateInfo = templates[k].getTemplateInfo();
					if(templateInfo.getProjectType().equals(MG_TYPE))
					{
						System.out.println("add "+templateInfo.getProjectType());
						items.add(new EntryDescriptor(templates[k].getTemplateId(),
							//null,
							vs[i].getId(),
							templates[k].getLabel(),templateInfo.isCategory(),h,null));
					}
				}
			}
		}

		return (EntryDescriptor[])items.toArray(new EntryDescriptor[items.size()]);
	}
}
