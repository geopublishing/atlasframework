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
import java.util.List;

import javax.swing.AbstractAction;

import skrueger.atlas.map.Map;
import skrueger.atlas.map.MapPool;
import skrueger.atlas.resource.icons.Icons;
import skrueger.creator.AtlasCreator;
import skrueger.creator.GpUtil;

public class MapPoolDuplicateAction extends AbstractAction {

	private MapPoolJTable mapPoolJTable;
	private MapPool mapPool;

	public MapPoolDuplicateAction(MapPoolJTable mapPoolJTable) {
		super(AtlasCreator.R("MapPool.Action.Duplicate"),
				Icons.ICON_DUPLICATE_SMALL);

		this.mapPoolJTable = mapPoolJTable;
		this.mapPool = mapPoolJTable.getMapPool();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		List<String> languages = mapPoolJTable.getAce().getLanguages();

		int idx = mapPoolJTable.getSelectedRow();

		final Map map = mapPool.get(mapPoolJTable.convertRowIndexToModel(idx));

		Map newMap = map.copy();
		newMap.setId(GpUtil.getRandomID("map"));

		for (String s : languages) {

			// Title
			String newS = AtlasCreator.R(
					"MapPool.Action.Duplicate.NewTranslation", newMap
							.getTitle().get(s));
			newMap.getTitle().put(s, newS);

		}

		mapPool.add(newMap);

	}

}
