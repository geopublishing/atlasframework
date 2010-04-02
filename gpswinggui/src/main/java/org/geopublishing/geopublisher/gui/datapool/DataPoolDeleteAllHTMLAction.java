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
package org.geopublishing.geopublisher.gui.datapool;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.dp.DataPool;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geopublishing.atlasViewer.swing.BasicMapLayerLegendPaneUI;
import org.geopublishing.geopublisher.AtlasCreator;
import org.geopublishing.geopublisher.swing.GpSwingUtil;


public class DataPoolDeleteAllHTMLAction extends AbstractAction {

	static final Logger LOGGER = Logger.getLogger(DataPoolDeleteAction.class);

	private final Component owner;

	private final DataPoolJTable dpTable;

	private final int countExistingFiles;

	public DataPoolDeleteAllHTMLAction(DataPoolJTable dpTable, Component owner,
			int countExistingFiles) {
		super(AtlasCreator.R("DataPoolWindow_Action_DeleteAllDPEHTML_label",
				countExistingFiles), BasicMapLayerLegendPaneUI.ICON_REMOVE);

		this.dpTable = dpTable;
		this.owner = owner;
		this.countExistingFiles = countExistingFiles;
	}

	/**
	 * Delete all HTML files that belong to a {@link DpEntry}.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		// Determine which DPEntry is selected
		if (dpTable.getSelectedRow() == -1)
			return;
		DataPool dataPool = dpTable.getDataPool();
		DpEntry dpe = dataPool.get(dpTable.convertRowIndexToModel(dpTable
				.getSelectedRow()));

		if (!(dpe instanceof DpLayer))
			return;

		AVSwingUtil.askYesNo(owner, AtlasCreator.R(
				"DataPoolWindow_Action_DeleteAllDPEHTML_Question",
				countExistingFiles));

		DpLayer<?, ?> dpl = (DpLayer<?, ?>) dpe;
		List<File> infoFiles = GpSwingUtil.getHTMLFilesFor(dpl);

		// TODO Delete image directory?
		for (File f : infoFiles) {
			f.delete();
		}

		dpl.uncache();

		((AbstractTableModel) dpTable.getModel()).fireTableDataChanged();
		dpTable.setTableCellRenderers();

	}

}
