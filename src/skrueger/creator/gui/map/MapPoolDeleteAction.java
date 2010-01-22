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
