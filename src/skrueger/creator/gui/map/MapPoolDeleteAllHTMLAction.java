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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;

import skrueger.atlas.AVUtil;
import skrueger.atlas.gui.plaf.BasicMapLayerLegendPaneUI;
import skrueger.atlas.map.Map;
import skrueger.atlas.map.MapPool;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.AtlasCreator;
import skrueger.creator.gui.datapool.DataPoolDeleteAction;

/**
 * An action that deletes the index_LANGCODE.html files for all configured
 * languages. Ask the user for confirmation.
 */
public class MapPoolDeleteAllHTMLAction extends AbstractAction {

	static final Logger LOGGER = Logger.getLogger(DataPoolDeleteAction.class);

	final private Map map;

	final private Component owner;

	final private AtlasConfigEditable ace;

	public MapPoolDeleteAllHTMLAction(final MapPoolJTable mpTable) {
		super(AtlasCreator.R("MapPoolWindow_Action_DeleteAllMapHTML_label",
				mpTable.getAce().getLanguages().size()
						- mpTable.getMapPool().get(
								mpTable.convertRowIndexToModel(mpTable
										.getSelectedRow()))
								.getMissingHTMLLanguages().size()),
				BasicMapLayerLegendPaneUI.ICON_REMOVE);

		owner = mpTable;
		final MapPool mapPool = mpTable.getMapPool();
		map = mapPool.get(mpTable.convertRowIndexToModel(mpTable
				.getSelectedRow()));
		ace = mpTable.getAce();

		setEnabled(map.getMissingHTMLLanguages().size() == 0);
	}

	public MapPoolDeleteAllHTMLAction(final Component owner_, final Map map_) {
		super(AtlasCreator.R("MapPoolWindow_Action_DeleteAllMapHTML_label",
				map_.getAc().getLanguages().size()
						- map_.getMissingHTMLLanguages().size()),
				BasicMapLayerLegendPaneUI.ICON_REMOVE);

		ace = (AtlasConfigEditable) map_.getAc();
		map = map_;
		owner = owner_;

		setEnabled(map.getMissingHTMLLanguages().size() == 0);
	}

	/**
	 * Delete all HTML files for a {@link Map} object
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {

		if (!AVUtil.askYesNo(owner, AtlasCreator.R(
				"MapPoolWindow_Action_DeleteAllMapHTML_Question", ace
						.getLanguages().size()
						- map.getMissingHTMLLanguages().size())))
			return;

		final List<File> infoFiles = ace.getHTMLFilesFor(map);

		// TODO Delete image directory?
		for (final File f : infoFiles) {
			f.delete();
		}

		// Forget about any HTML pages we had remembered.
		map.resetMissingHTMLinfos();

		/**
		 * Inform the mappool about the change
		 */
		ace.getMapPool().fireChangeEvents(MapPoolDeleteAllHTMLAction.this,
				MapPool.EventTypes.changeMap, map);

	}

}
