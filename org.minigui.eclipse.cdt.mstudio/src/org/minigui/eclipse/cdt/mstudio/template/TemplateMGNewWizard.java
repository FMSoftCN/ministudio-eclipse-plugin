
package org.minigui.eclipse.cdt.mstudio.template;

import java.util.ArrayList;
import java.util.Arrays;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyManager;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyType;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.core.BuildListComparator;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.AbstractCWizard;
import org.eclipse.cdt.ui.wizards.EntryDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.cdt.ui.templateengine.Template;
import org.eclipse.cdt.ui.templateengine.TemplateEngineUI;
import org.eclipse.cdt.core.templateengine.TemplateInfo;

import org.minigui.eclipse.cdt.mstudio.MiniGUIMessages;
import org.minigui.eclipse.cdt.mstudio.wizards.MGWizardHandler;

public class TemplateMGNewWizard extends AbstractCWizard {
	
	private static final String MG_TYPE = "org.minigui.eclipse.cdt.mstudio.projectType";
	public static final String OTHERS_LABEL = MiniGUIMessages.getString("MGNewWizard.0");  
	public static final String EMPTY_PROJECT = "Empty MiniGUI Project";
	
	public EntryDescriptor[] createItems(boolean supportedOnly, IWizard wizard) {
		IBuildPropertyManager bpm = ManagedBuildManager.getBuildPropertyManager();
		IBuildPropertyType bpt = bpm.getPropertyType(MG_TYPE);
		IBuildPropertyValue[] vs = bpt.getSupportedValues();
		Arrays.sort(vs, BuildListComparator.getInstance());
		
		Template[] templates = TemplateEngineUI.getDefault().getTemplates();
		ArrayList<EntryDescriptor> items = new ArrayList<EntryDescriptor>();
		
		for (int i=0; i<vs.length; i++) {
			IToolChain[] tcs = ManagedBuildManager.getExtensionsToolChains(MGWizardHandler.ARTIFACT, vs[i].getId(), false);
			if (tcs == null || tcs.length == 0) continue;
			
			MGWizardHandler h = new MGWizardHandler(vs[i], parent, wizard);
			h.addTc(null);
			for (int j=0; j<tcs.length; j++) {
				if (isValid(tcs[j], supportedOnly, wizard)) {
					h.addTc(tcs[j]);
				}
			}
			if (h.getToolChainsCount() > 0) {
				// The project category item.
				items.add(new EntryDescriptor(vs[i].getId(), null, vs[i].getName(), true, h, null));

				for (int k=0; k < templates.length; k++) {
					TemplateInfo templateInfo = templates[k].getTemplateInfo();
					if(templateInfo.getProjectType().equals(MG_TYPE))
					{
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
