/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.gui.export;

import java.awt.Component;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.map.MapPool;
import org.geopublishing.atlasViewer.swing.AtlasViewerGUI;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.netbeans.spi.wizard.WizardPage;

import schmitzm.jfree.chart.style.ChartStyle;

public class ExportWizardPage_Save extends WizardPage {

	JLabel explanationJLabel = new JLabel(GeopublisherGUI
			.R("ExportWizard.StartPage.Explanation"));
	JLabel saveExplJLabel = new JLabel(GeopublisherGUI
			.R("ExportWizard.StartPage.Save.Explanation"));
	private final String validationFailedMsg_HasToSave = GeopublisherGUI
			.R("ExportWizard.StartPage.ValidationError.Save");
	private final String validationFailedMsg_NoMaps = AtlasViewerGUI
			.R("AtlasViewer.error.noMapInAtlas");
	private JCheckBox saveJCheckbox;

	private boolean firstRender = true;

	public ExportWizardPage_Save() {
	}

	public static String getDescription() {
		return GeopublisherGUI.R("ExportWizard.StartPage");
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
							GeopublisherGUI
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
			saveJCheckbox = new JCheckBox(GeopublisherGUI
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
