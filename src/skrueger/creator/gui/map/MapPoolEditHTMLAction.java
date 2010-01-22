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
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;

import skrueger.atlas.map.Map;
import skrueger.atlas.map.MapPool;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.AtlasCreator;
import skrueger.creator.gui.SimplyHTMLUtil;
import skrueger.creator.gui.datapool.DataPoolDeleteAction;
import skrueger.i8n.I8NUtil;

public class MapPoolEditHTMLAction extends AbstractAction {

	static final Logger LOGGER = Logger.getLogger(DataPoolDeleteAction.class);

	private MapPoolJTable mpTable;

	private Map map;

	public MapPoolEditHTMLAction(MapPoolJTable mpTable) {
		super(AtlasCreator.R("MapPoolWindow_Action_EditMapHTML_label"));

		this.mpTable = mpTable;
	}

	public MapPoolEditHTMLAction(Map map) {
		super(AtlasCreator.R("MapPoolWindow_Action_EditMapHTML_label"));
		this.map = map;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (map == null) {
			// Determine which DPEntry is selected
			if (mpTable.getSelectedRow() == -1)
				return;
			MapPool dataPool = mpTable.getMapPool();

			map = dataPool.get(mpTable.convertRowIndexToModel(mpTable
					.getSelectedRow()));
		}

		AtlasConfigEditable ace = (AtlasConfigEditable) map.getAc();

		List<File> infoFiles = ace.getHTMLFilesFor(map);

		ArrayList<String> tabTitles = new ArrayList<String>();
		for (String l : ace.getLanguages()) {
			tabTitles.add(AtlasCreator.R("Map.HTMLInfo.LanguageTabTitle",
					I8NUtil.getLocaleFor(l).getDisplayLanguage()));
		}

		SimplyHTMLUtil.openHTMLEditors(mpTable, ace, infoFiles, tabTitles,
				AtlasCreator.R("Map.HTMLInfo.EditDialog.Title", map.getTitle()
						.toString()));

		map.resetMissingHTMLinfos();
		
		ace.getMapPool().fireChangeEvents(MapPoolEditHTMLAction.this,
				MapPool.EventTypes.changeMap, map);

	}

}
