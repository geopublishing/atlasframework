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
