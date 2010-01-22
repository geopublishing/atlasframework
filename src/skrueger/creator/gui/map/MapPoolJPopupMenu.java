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
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import skrueger.atlas.map.Map;
import skrueger.atlas.resource.icons.Icons;
import skrueger.creator.AtlasCreator;
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
		AbstractAction openMapComposer = new AbstractAction("<html><b>"+AtlasCreator.R("MapPoolWindow.Action.OpenInMapComposer")+"</b></html>", Icons.ICON_MAP_SMALL) {
			
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
				AtlasCreator.R("MapPool.Action.MakeStartMap")) {

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
		menuItemMakeDefault.setToolTipText(AtlasCreator
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
