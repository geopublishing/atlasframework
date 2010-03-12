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
package skrueger.creator.gui.map;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import skrueger.atlas.gui.plaf.BasicMapLayerLegendPaneUI;
import skrueger.atlas.map.Map;
import skrueger.atlas.map.MapPool;
import skrueger.creator.AtlasCreator;
import skrueger.creator.GPDialogManager;

/**
 * This {@link Action} allows the user to edit the general map properties.
 */
public class MapPoolPrefsAction extends AbstractAction {

	final private MapPool mapPool;
	private Map map;
	final Component owner;
	private MapPoolJTable mapPoolJTable;

	public MapPoolPrefsAction(Component owner, Map map, MapPool mapPool) {
		super(AtlasCreator.R("MapPoolWindow.Button_EditMap_label"),
				BasicMapLayerLegendPaneUI.ICON_TOOL);
		this.owner = owner;
		this.map = map;
		this.mapPool = mapPool;

	}

	public MapPoolPrefsAction(MapPoolJTable mapPoolJTable) {
		super(AtlasCreator.R("MapPoolWindow.Button_EditMap_label"),
				BasicMapLayerLegendPaneUI.ICON_TOOL);
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
