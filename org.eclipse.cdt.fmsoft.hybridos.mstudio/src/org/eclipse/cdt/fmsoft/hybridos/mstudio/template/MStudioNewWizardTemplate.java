
package org.eclipse.cdt.fmsoft.hybridos.mstudio.template;

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
import org.eclipse.cdt.fmsoft.hybridos.mstudio.MStudioMessages;
import org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards.MStudioWizardHandler;


public class MStudioNewWizardTemplate extends AbstractCWizard {
	
	private static final String[] MSTUDIO_TYPE = {"org.eclipse.cdt.fmsoft.hybridos.mstudio.buildArtefactType.exe",
									"org.eclipse.cdt.fmsoft.hybridos.mstudio.buildArtefactType.sharedLib"};

	public static final String OTHERS_LABEL = MStudioMessages.getString("MStudioNewWizard.0");  
	public static final String EMPTY_PROJECT = "Empty mStudio Project";
	
	public EntryDescriptor[] createItems(boolean supportedOnly, IWizard wizard) {
		ArrayList<EntryDescriptor> items = new ArrayList<EntryDescriptor>();
		IBuildPropertyManager bpm = ManagedBuildManager.getBuildPropertyManager();
		
		for (int size = 0; size < MSTUDIO_TYPE.length; size++) {
			IBuildPropertyType bpt = bpm.getPropertyType(MSTUDIO_TYPE[size]);
			
			IBuildPropertyValue[] vs = bpt.getSupportedValues();
			Arrays.sort(vs, BuildListComparator.getInstance());
			
			Template[] templates = TemplateEngineUI.getDefault().getTemplates();
			
			for (int bpvIdx=0; bpvIdx<vs.length; bpvIdx++) {
				IToolChain[] tcs = ManagedBuildManager.getExtensionsToolChains(MStudioWizardHandler.ARTIFACT, vs[bpvIdx].getId(), false);
				if (tcs == null || tcs.length == 0) continue;
				
				MStudioWizardHandler h = new MStudioWizardHandler(vs[bpvIdx], parent, wizard);
				h.addTc(null);
				for (int tcsIdx=0; tcsIdx<tcs.length; tcsIdx++) {
					if (isValid(tcs[tcsIdx], supportedOnly, wizard)) {
						h.addTc(tcs[tcsIdx]);
					}
				}
				if (h.getToolChainsCount() > 0) {
					// The project category item.
					items.add(new EntryDescriptor(vs[bpvIdx].getId(), null, vs[bpvIdx].getName(), true, h, null));

					for (int tmplIdx=0; tmplIdx < templates.length; tmplIdx++) {
						TemplateInfo templateInfo = templates[tmplIdx].getTemplateInfo();
						if(templateInfo.getProjectType().equals(MSTUDIO_TYPE[size]))
						{
							items.add(new EntryDescriptor(templates[tmplIdx].getTemplateId(),
								//null,
								vs[bpvIdx].getId(),
								templates[tmplIdx].getLabel(),templateInfo.isCategory(),h, null));
						}
					}
				}
			}
		}
		
		return (EntryDescriptor[])items.toArray(new EntryDescriptor[items.size()]);
	}
}
