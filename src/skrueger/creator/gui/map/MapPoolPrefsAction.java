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
