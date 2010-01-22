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

import javax.swing.AbstractAction;

import skrueger.atlas.map.Map;
import skrueger.atlas.map.MapPool;
import skrueger.atlas.resource.icons.Icons;
import skrueger.creator.AtlasCreator;
import skrueger.creator.GPDialogManager;
import skrueger.creator.GpUtil;

public class MapPoolAddAction extends AbstractAction {

	private MapPoolJTable mapPoolJTable;
	private MapPool mapPool;

	public MapPoolAddAction(MapPoolJTable mapPoolJTable) {
		super(AtlasCreator.R("MapPoolWindow.Button_AddMap_label"),
				Icons.ICON_ADD_SMALL);

		this.mapPoolJTable = mapPoolJTable;
		this.mapPool = mapPoolJTable.getMapPool();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
//
//		// Setup empty translations
//		Translation name, desc, keywords;
//		List<String> languages = mapPoolJTable.getAce().getLanguages();
//		name = new Translation(languages, AtlasCreator
//				.R("MapPool.DummyMapName"));
//		desc = new Translation(languages, AtlasCreator
//				.R("MapPool.DummyMapDescription"));
//		keywords = new Translation();
//
//		final TranslationEditJPanel namePanel = new TranslationEditJPanel(
//				AtlasCreator.R("MapPreferences_translateTheMapsName"), name,
//				languages);
//		final TranslationEditJPanel descPanel = new TranslationEditJPanel(
//				AtlasCreator.R("MapPreferences_translateTheMapsDescription"),
//				desc, languages);
//		final TranslationEditJPanel keywordsPanel = new TranslationEditJPanel(
//				AtlasCreator.R("MapPreferences_translateTheMapsKeywords"),
//				desc, languages);
//
//		TranslationAskJDialog translationAskJDialog = new TranslationAskJDialog(
//				mapPoolJTable, namePanel, descPanel, keywordsPanel);
//		translationAskJDialog.setVisible(true);
//		if (translationAskJDialog.isCancelled()) {
//			return;
//		}

		// The map is created and automatically added to the mappool.
		Map newMap = new Map(GpUtil.getRandomID("map"), mapPoolJTable.getAce());
		mapPool.put(newMap);

		if (mapPool.size() == 1) {
			mapPool.setStartMapID(newMap.getId());
		}
		
		mapPoolJTable.select(newMap.getId());
		
		GPDialogManager.dm_EditMapEntry.getInstanceFor(newMap, this.mapPoolJTable, newMap);
		
		// TODO Try to select the new map/row. But how?!

	}

}
