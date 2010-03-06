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
