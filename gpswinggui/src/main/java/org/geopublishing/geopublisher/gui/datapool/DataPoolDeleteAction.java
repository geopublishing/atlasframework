/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.gui.datapool;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.dp.DataPool;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.Group;
import org.geopublishing.atlasViewer.map.MapPool;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geopublishing.atlasViewer.swing.plaf.BasicMapLayerLegendPaneUI;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.gui.internal.GPDialogManager;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.geopublishing.geopublisher.swing.GpSwingUtil;


/**
 * Delete a {@link DpEntry} from the {@link DataPool} and also remove all
 * references to it from the {@link MapPool} or the {@link Group}s
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
 * Kr&uuml;ger</a>
 **/
public class DataPoolDeleteAction extends AbstractAction {

	static final Logger LOGGER = Logger.getLogger(DataPoolDeleteAction.class);

	private final Component owner;

	private final DataPoolJTable dpTable;

	public DataPoolDeleteAction(DataPoolJTable dpTable, Component owner) {
		super(GeopublisherGUI.R("DataPoolWindow_Action_DeleteDPE_label"),
				BasicMapLayerLegendPaneUI.ICON_REMOVE);

		this.dpTable = dpTable;
		this.owner = owner;
	}

	@Override
	/**
	 * Delete a {@link DpEntry} from the {@link DataPool} and also remove all
	 * references to it from the {@link MapPool} or the {@link Group}s
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 * Kr&uuml;ger</a>
	 */
	public void actionPerformed(ActionEvent e) {

		// Determine which DPEntry is selected
		if (dpTable.getSelectedRow() == -1)
			return;
		DataPool dataPool = dpTable.getDataPool();
		int modelIndex = dpTable.convertRowIndexToModel(dpTable
				.getSelectedRow());
		DpEntry dpe = dataPool.get(modelIndex);

		if (AVSwingUtil.askYesNo(owner, GeopublisherGUI.R(
				"Action_DeleteDPE_reallyDeleteQuestion", dpe.getTitle()
						.toString(), dpe.getFilename())) == false)
			return;

		// TODO Only close the windows that reference this dplayer!
		if (!GPDialogManager.dm_MapComposer.closeAllInstances())
			return;

		GpSwingUtil.deleteDpEntry(owner, (AtlasConfigEditable) dpe
				.getAtlasConfig(), dpe, true);

	}
}
