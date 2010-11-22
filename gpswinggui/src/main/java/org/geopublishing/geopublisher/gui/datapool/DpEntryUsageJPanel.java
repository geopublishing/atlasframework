/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.gui.datapool;

import javax.swing.JLabel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;

import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.swing.JPanel;
import skrueger.swing.Cancellable;

public class DpEntryUsageJPanel extends JPanel implements Cancellable {

	private final AtlasConfigEditable ace;
	private final DpEntry<? extends ChartStyle> dpe;

	public DpEntryUsageJPanel(final DpEntry<? extends ChartStyle> dpe) {

		super(new MigLayout("width 100%, wrap 1", "[grow]"));
		this.dpe = dpe;

		ace = (AtlasConfigEditable) dpe.getAtlasConfig();

		backup();

		add(new JLabel(R("EditDpEntryGUI.usage.Maps.border")));
		MapusageTable mapUt = new MapusageTable(dpe, ace.getMapPool());
		// mapUsage.
		add(new JScrollPane(mapUt), "height 150");

		add(new JLabel(R("EditDpEntryGUI.usage.Menu.border")), "gapy unrel");

		MenuusageTable menuUt = new MenuusageTable(dpe, ace.getRootGroup());
		add(new JScrollPane(menuUt), "height 150");

		if (menuUt.getModel().getRowCount() == 0
				&& mapUt.getModel().getRowCount() == 0) {
			add(new JLabel(R("EditDpEntryGUI.usage.notexported")), "gapy unrel");
		}
		else add(new JLabel(R("EditDpEntryGUI.usage.exported")), "gapy unrel");
	}

	protected String R(String string, Object... obj) {
		return GeopublisherGUI.R(string, obj);
	}

	boolean backupExportable = false;

	private void backup() {
		backupExportable = dpe.isExportable();

	}

	@Override
	public void cancel() {
		dpe.setExportable(backupExportable);

	}

}
