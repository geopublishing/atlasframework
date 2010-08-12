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

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CancellationException;

import javax.swing.AbstractAction;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AVUtil;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.map.MapPool;
import org.geopublishing.atlasViewer.swing.AtlasSwingWorker;
import org.geopublishing.atlasViewer.swing.Icons;
import org.geopublishing.atlasViewer.swing.internal.AtlasStatusDialog;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GpUtil;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.geotools.swing.ExceptionMonitor;

public class MapPoolDuplicateAction extends AbstractAction {
	final static private Logger LOGGER = Logger
			.getLogger(MapPoolDuplicateAction.class);

	private MapPoolJTable mapPoolJTable;
	private MapPool mapPool;

	public MapPoolDuplicateAction(MapPoolJTable mapPoolJTable) {
		super(GeopublisherGUI.R("MapPool.Action.Duplicate"),
				Icons.ICON_DUPLICATE_SMALL);

		this.mapPoolJTable = mapPoolJTable;
		this.mapPool = mapPoolJTable.getMapPool();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		int idx = mapPoolJTable.getSelectedRow();

		final Map map = mapPool.get(mapPoolJTable.convertRowIndexToModel(idx));

		actionPerformed(map);
		
	}

	/**
	 * @return the new map or null
	 */
	public Map actionPerformed(final Map map) {
		List<String> languages = mapPoolJTable.getAce().getLanguages();

		Map newMap = map.copy();
		newMap.setId(GpUtil.getRandomID("map"));

		for (String s : languages) {
			// Title
			String newS = GeopublisherGUI.R(
					"MapPool.Action.Duplicate.NewTranslation", newMap
							.getTitle().get(s));
			newMap.getTitle().put(s, newS);
		}

		// Copy .html files for this map too
		AtlasConfigEditable ace = (AtlasConfigEditable) map.getAc();
		final File htmlDir = ace.getHtmlDirFor(map);
		final File newHtmlDir = ace.getHtmlDirFor(newMap);

		AtlasStatusDialog statusDialog = new AtlasStatusDialog(mapPoolJTable,
				null, AVUtil.R("dialog.title.wait"));
		AtlasSwingWorker<Void> swingWorker = new AtlasSwingWorker<Void>(
				statusDialog) {

			@Override
			protected Void doInBackground() throws Exception {
				FileUtils.copyDirectory(htmlDir, newHtmlDir);
				return null;
			}

		};

		try {
			swingWorker.executeModal();

		} catch (CancellationException e1) {
			try {
				FileUtils.deleteDirectory(newHtmlDir);
			} catch (IOException e2) {
				LOGGER.error(e2);
				ExceptionMonitor.show(mapPoolJTable, e2,
						"While deleting the new map's HTML files");
				return null;
			}
		} catch (Exception e1) {
			ExceptionMonitor.show(mapPoolJTable, e1,
					"While copying the new map's HTML files");

			try {
				FileUtils.deleteDirectory(newHtmlDir);
			} catch (IOException e3) {
				LOGGER.error(e3);
				ExceptionMonitor.show(mapPoolJTable, e3,
						"While deleting the new map's HTML files");
			}

			return null;
		}

		mapPool.add(newMap);
		
		return newMap;
	}

}
