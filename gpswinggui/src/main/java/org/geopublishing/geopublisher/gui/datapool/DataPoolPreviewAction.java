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

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.media.DpMedia;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;


public class DataPoolPreviewAction extends AbstractAction {
	static final Logger LOGGER = Logger.getLogger(DataPoolPreviewAction.class);

	private final Component owner;

	private final DataPoolJTable dpTable;

	public DataPoolPreviewAction(DataPoolJTable dpTable, Component owner) {
		super(GeopublisherGUI.R("DataPoolWindow_Action_ShowDPM_label"));

		this.dpTable = dpTable;
		this.owner = owner;
	}

	@Override
	/*
	 * Delete a {@link DpEntry} from the {@link DataPool} and also remove all
	 * references to it from the {@link MapPool} or the {@link Group}s
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 * Tzeggai</a>
	 */
	public void actionPerformed(ActionEvent e) {

		// Determine which DPEntry is selected
		if (dpTable.getSelectedRow() == -1)
			return;

		DpEntry dpe = dpTable.getDataPool().get(
				dpTable.convertRowIndexToModel(dpTable.getSelectedRow()));

		if (dpe instanceof DpMedia) {
			DpMedia dpm = (DpMedia) dpe;
			
			dpm.show(owner);
		}
	}

}
