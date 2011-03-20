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

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.map.MapPool;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.gui.datapool.DataPoolDeleteAction;
import org.geopublishing.geopublisher.gui.internal.GPDialogManager;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.geopublishing.geopublisher.swing.GpSwingUtil;

import de.schmitzm.i18n.I18NUtil;

public class MapPoolEditHTMLAction extends AbstractAction {

	static final Logger LOGGER = Logger.getLogger(DataPoolDeleteAction.class);

	private MapPoolJTable mpTable;

	private Map map;

	public MapPoolEditHTMLAction(MapPoolJTable mpTable) {
		super(GeopublisherGUI.R("MapPoolWindow_Action_EditMapHTML_label"));

		this.mpTable = mpTable;
	}

	public MapPoolEditHTMLAction(Map map) {
		super(GeopublisherGUI.R("MapPoolWindow_Action_EditMapHTML_label"));
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

		final AtlasConfigEditable ace = (AtlasConfigEditable) map.getAc();

		List<File> infoFiles = GpSwingUtil.getHTMLFilesFor(map);

		ArrayList<String> tabTitles = new ArrayList<String>();
		for (String l : ace.getLanguages()) {
			tabTitles.add(GeopublisherGUI.R("Map.HTMLInfo.LanguageTabTitle",
					I18NUtil.getFirstLocaleForLang(l).getDisplayLanguage()));
		}

		final String title = GeopublisherGUI.R("Map.HTMLInfo.EditDialog.Title",
				map.getTitle().toString());
		// GpSwingUtil.openHTMLEditors(mpTable, ace, infoFiles, tabTitles,
		// title);

		String key = GpSwingUtil.openHTMLEditorsKey(infoFiles);
		Window instanceFor = GPDialogManager.dm_HtmlEditor.getInstanceFor(key,
				mpTable, ace, infoFiles, tabTitles, title);

		// TODO In case this HTML editor is beeing tried to be opened
		// twice, these listeneras are added twice.!
		instanceFor.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				super.windowClosed(e);
				/**
				 * Try to update a few cached values for the TODO nicer!
				 */
				map.resetMissingHTMLinfos();

				ace.getMapPool().fireChangeEvents(MapPoolEditHTMLAction.this,
						MapPool.EventTypes.changeMap, map);

			};
		});

	}

}
