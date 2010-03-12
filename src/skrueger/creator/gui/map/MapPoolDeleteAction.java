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

import java.awt.event.ActionEvent;
import java.util.LinkedList;

import javax.swing.AbstractAction;

import schmitzm.swing.ExceptionDialog;
import skrueger.atlas.AVUtil;
import skrueger.atlas.AtlasRefInterface;
import skrueger.atlas.dp.Group;
import skrueger.atlas.exceptions.AtlasException;
import skrueger.atlas.gui.plaf.BasicMapLayerLegendPaneUI;
import skrueger.atlas.map.Map;
import skrueger.atlas.map.MapPool;
import skrueger.creator.AtlasCreator;

public class MapPoolDeleteAction extends AbstractAction {

	private final MapPoolJTable mapPoolJTable;
	private MapPool mapPool;

	public MapPoolDeleteAction(MapPoolJTable mapPoolJTable) {
		super(AtlasCreator.R("MapPool.Action.DeleteMap"),
				BasicMapLayerLegendPaneUI.ICON_REMOVE);

		this.mapPoolJTable = mapPoolJTable;
		this.mapPool = mapPoolJTable.getMapPool();
	}

	public void actionPerformed(ActionEvent e) {
		int idx = mapPoolJTable.getSelectedRow();

		final Map map2delete = mapPool.get(mapPoolJTable
				.convertRowIndexToModel(idx));

		if (!AVUtil.askYesNo(mapPoolJTable, AtlasCreator.R(
				"MapPool.Action.DeleteMap.Question", map2delete.getTitle()))) {
			return;
		}

		// Delete references to this map in the Groups
		Group.findReferencesTo(mapPoolJTable.getAce().getFirstGroup(),
				map2delete, new LinkedList<AtlasRefInterface<?>>(), true);

		Map removed = mapPool.remove(map2delete.getId());
		if (removed == null) {
			ExceptionDialog.show(mapPoolJTable, new AtlasException(
					"The map we tried to delete is not in the MapPool?!"));
		}
	}

}
