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

import javax.swing.AbstractAction;

import org.geopublishing.atlasViewer.dp.DataPool;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVector;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geopublishing.geopublisher.GeopublisherGUI;
import org.geopublishing.geopublisher.EditAttributesJDialog;

import skrueger.creator.GPDialogManager;


public class DataPoolEditColumnsAction extends AbstractAction {

	private final Component owner;
	private final DataPoolJTable dpTable;

	// static protected HashMap<String, JDialog> editAttribsJDialogs = new
	// HashMap<String, JDialog>();

	public DataPoolEditColumnsAction(DataPoolJTable dpTable, Component owner) {

		super(GeopublisherGUI.R("DataPoolWindow_Action_EditColumns_label"));

		this.dpTable = dpTable;
		this.owner = owner;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Determine which DPEntry is selected
		if (dpTable.getSelectedRow() == -1)
			return;
		DataPool dataPool = dpTable.getDataPool();
		DpEntry dpe = dataPool.get(dpTable.convertRowIndexToModel(dpTable
				.getSelectedRow()));
		if (!(dpe instanceof DpLayerVector)) {
			AVSwingUtil.showMessageDialog(owner,
					"You can only edit the columns of vector data"); // i8n
			return;
		}

		final DpLayerVectorFeatureSource dplv = (DpLayerVectorFeatureSource) dpe;

		EditAttributesJDialog d = GPDialogManager.dm_EditAttribute
				.getInstanceFor(dplv, owner, dplv);

	}

}
