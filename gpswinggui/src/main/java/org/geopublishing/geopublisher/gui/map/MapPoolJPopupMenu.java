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
package org.geopublishing.geopublisher.gui.map;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.resource.icons.Icons;
import org.geopublishing.geopublisher.GeopublisherGUI;

import skrueger.creator.GPDialogManager;


public class MapPoolJPopupMenu extends JPopupMenu {

	/**
	 * This {@link JPopupMenu} is shown by the {@link MapPoolJTable} and allows
	 * to do some manipulations on the maps like "make default map" or "delete"
	 * 
	 * @param mpt
	 *            {@link MapPoolJTable}
	 */
	public MapPoolJPopupMenu(final MapPoolJTable mpt) {
		
		// ID of the map under the mouse
		final String mapId = mpt.getMapPool().get(
				mpt
						.convertRowIndexToModel(mpt
								.getSelectedRow())).getId();
		
		
		/**
		 * A menuitem to open the MapComposer
		 */
		AbstractAction openMapComposer = new AbstractAction("<html><b>"+GeopublisherGUI.R("MapPoolWindow.Action.OpenInMapComposer")+"</b></html>", Icons.ICON_MAP_SMALL) {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Map map = mpt.getMapPool().get(mapId);
				GPDialogManager.dm_MapComposer.getInstanceFor(map, mpt, map);
			}
		};
		add(openMapComposer);

		/**
		 * The Preferences Action
		 */
		add(new JMenuItem(new MapPoolPrefsAction(mpt)));

		/**
		 * The Make Default Action!
		 */
		JMenuItem menuItemMakeDefault = new JMenuItem(new AbstractAction(
				GeopublisherGUI.R("MapPool.Action.MakeStartMap")) {

			public void actionPerformed(ActionEvent e) {
				/**
				 * Set the selected (?) map under the cursor as the default map
				 * for this atlas.
				 */
				mpt.getMapPool().setStartMapID(
						mapId);

				mpt.repaint();
			}

		});
		menuItemMakeDefault.setToolTipText(GeopublisherGUI
				.R("MapPool.Action.MakeStartMap.TT"));
		add(menuItemMakeDefault);

		addSeparator();

		add(new MapPoolEditHTMLAction(mpt));

		/**
		 * Determine the number of HTML files that exist
		 */

		if (mpt.getSelectedRow() != -1) {
			Map map = mpt.getMapPool().get(
					mpt.convertRowIndexToModel(mpt.getSelectedRow()));

			int countExisting = mpt.getAce().getLanguages().size()
					- map.getMissingHTMLLanguages().size();
			if (countExisting > 0)
				add(new MapPoolDeleteAllHTMLAction(mpt));
		}

		addSeparator();

		/**
		 * The New/Add Action
		 */
		add(new JMenuItem(new MapPoolAddAction(mpt)));

		/**
		 * The Duplicate Action
		 */
		add(new JMenuItem(new MapPoolDuplicateAction(mpt)));

		/**
		 * The Delete Action
		 */
		add(new JMenuItem(new MapPoolDeleteAction(mpt)));

	}
}
