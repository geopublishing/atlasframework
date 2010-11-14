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
package org.geopublishing.geopublisher.gui.map;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.map.MapPool;
import org.geopublishing.atlasViewer.swing.Icons;
import org.geopublishing.geopublisher.gui.internal.GPDialogManager;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;



/**
 * This {@link Action} allows the user to edit the general map properties.
 */
public class MapPoolPrefsAction extends AbstractAction {

	final private MapPool mapPool;
	private Map map;
	final Component owner;
	private MapPoolJTable mapPoolJTable;

	public MapPoolPrefsAction(Component owner, Map map, MapPool mapPool) {
		super(GeopublisherGUI.R("MapPoolWindow.Button_EditMap_label"),
				Icons.ICON_TOOL);
		this.owner = owner;
		this.map = map;
		this.mapPool = mapPool;

	}

	public MapPoolPrefsAction(MapPoolJTable mapPoolJTable) {
		super(GeopublisherGUI.R("MapPoolWindow.Button_EditMap_label"),
				Icons.ICON_TOOL);
		this.mapPoolJTable = mapPoolJTable;
		this.mapPool = mapPoolJTable.getMapPool();

		owner = mapPoolJTable;

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (map == null) {
			int idx = mapPoolJTable.getSelectedRow();
			this.map = mapPool.get(mapPoolJTable.convertRowIndexToModel(idx));
		}

		GPDialogManager.dm_EditMapEntry.getInstanceFor(map, owner, map);
		
	}

}
