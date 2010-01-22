/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the Geopublisher application - An authoring tool to facilitate the publication and distribution of geoproducts in form of online and/or offline end-user GIS.
 * http://www.geopublishing.org
 * 
 * Geopublisher is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.creator.gui.export;

import java.awt.Component;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import org.netbeans.spi.wizard.WizardPage;

import schmitzm.jfree.chart.style.ChartStyle;
import skrueger.atlas.AtlasViewer;
import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.map.MapPool;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.AtlasCreator;

public class ExportWizardPage_Save extends WizardPage {

	/*
	 * The short description label that appears on the left side of the wizard
	 */
	static String desc = AtlasCreator.R("ExportWizard.StartPage");

	JLabel explanationJLabel = new JLabel(AtlasCreator
			.R("ExportWizard.StartPage.Explanation"));
	JLabel saveExplJLabel = new JLabel(AtlasCreator
			.R("ExportWizard.StartPage.Save.Explanation"));
	private static final String validationFailedMsg_HasToSave = AtlasCreator
			.R("ExportWizard.StartPage.ValidationError.Save");
	private static final String validationFailedMsg_NoMaps = AtlasViewer
			.R("AtlasViewer.error.noMapInAtlas");
	private JCheckBox saveJCheckbox;

	private boolean firstRender = true;

	public ExportWizardPage_Save() {
	}

	public static String getDescription() {
		return desc;
	}

	@Override
	protected void renderingPage() {
		if (firstRender) {
			firstRender = false;
			initGui();
		}
	}

	private void initGui() {
		setPreferredSize(ExportWizard.DEFAULT_WPANEL_SIZE);
		setSize(ExportWizard.DEFAULT_WPANEL_SIZE);

		setLayout(new MigLayout("wrap 1"));

		add(explanationJLabel);

		AtlasConfigEditable ace = (AtlasConfigEditable) getWizardData(ExportWizard.ACE);
		final List<DpEntry<? extends ChartStyle>> notInGroupNorMap = ace
				.listNotReferencedInGroupTreeNorInAnyMap();

		add(saveExplJLabel, "gapy unrelated");
		add(getSaveJCheckbox());

		if (!notInGroupNorMap.isEmpty()) {
			add(
					new JLabel(
							AtlasCreator
									.R("InconsistancyWarning.DPEsNotReferencedFromGroupTreeOrMap")),
					"gapy unrelated");

			final JList table = new JList(new AbstractListModel() {

				@Override
				public int getSize() {
					return notInGroupNorMap.size();
				}

				@Override
				public Object getElementAt(int i) {
					DpEntry dpe = notInGroupNorMap.get(i);
					String dpeDescription = dpe.getType().getLine1() + ": "
							+ dpe.getTitle() + " (" + dpe.getFilename() + ")";
					return dpeDescription;
				}

			});

			add(new JScrollPane(table), "grow");
		}

	}

	public JCheckBox getSaveJCheckbox() {
		if (saveJCheckbox == null) {
			saveJCheckbox = new JCheckBox(AtlasCreator
					.R("ExportWizard.StartPage.Save.CheckBoxLabel"));
			saveJCheckbox.setName(ExportWizard.SAVE_AUTOMATICALLY);
			saveJCheckbox.setSelected(true);
		}

		return saveJCheckbox;
	}

	@Override
	protected String validateContents(final Component component,
			final Object event) {

		AtlasConfigEditable ace = (AtlasConfigEditable) getWizardData(ExportWizard.ACE);

		// If not valid StartMap has been selected, we don't allow to open
		// the preview.
		final MapPool mapPool = ace.getMapPool();

		if (mapPool.getStartMapID() == null
				|| mapPool.get(mapPool.getStartMapID()) == null) {
			return validationFailedMsg_NoMaps;
		}

		if (!getSaveJCheckbox().isSelected())
			return validationFailedMsg_HasToSave;

		return null;
	}
}
