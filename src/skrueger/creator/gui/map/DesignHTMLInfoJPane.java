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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import skrueger.atlas.gui.map.HTMLInfoJPane;
import skrueger.atlas.map.Map;
import skrueger.atlas.map.MapPool.EventTypes;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.AtlasCreator;
import skrueger.i8n.I8NUtil;
import skrueger.i8n.Translation;

/**
 * Extends an {@link HTMLInfoJPane} allows Drag'n'Drop to insert HTML Infomation
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
 * 
 */
public class DesignHTMLInfoJPane extends HTMLInfoJPane {

	private final Map map;

	private final AtlasConfigEditable ace;

	/**
	 * Update this preview if the map has been changed in the map pool.
	 */
	final private PropertyChangeListener listenForMapChanges = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {

			// Is it a map changed?
			if (!evt.getPropertyName().equals(EventTypes.changeMap.toString()))
				return;

			// Is it out this map?
			if (!evt.getNewValue().equals(map))
				return;

			showDocument(map.getInfoURL());
		}
	};

	@Override
	public void showDocument(URL url) {
		if (url == null) {
			setEditable(false);
			setContentType("text/html");
			
			setText(AtlasCreator.R("DesignHTMLPanel.NoHTML.Message", I8NUtil
					.getLocaleFor(Translation.getActiveLang())
					.getDisplayLanguage()));
		} else
			super.showDocument(url);
	}

	public DesignHTMLInfoJPane(AtlasConfigEditable ace_, Map map_) {
		super(map_);
		this.ace = ace_;
		this.map = map_;

		// // Make the DatapoolJList accept Drops form the file system to
		// import...
		// final ImportHtmlInfoDropTargetListener importByDropTargetListener
		// = new ImportHtmlInfoDropTargetListener(
		// SwingUtil.getParentWindow(this), map, ace, this);
		//
		// @SuppressWarnings("unused")
		// DropTarget dt = new DropTarget(this, importByDropTargetListener);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {

				/**
				 * If the lines under the mouse is not selected, select it
				 * first...
				 */
				if (SwingUtilities.isRightMouseButton(evt)) {
					JPopupMenu popupMenu = new JPopupMenu();

					/**
					 * Edit HTML info files...
					 */
					popupMenu.add(new MapPoolEditHTMLAction(map));

					/**
					 * Delete HTML info files...
					 */
					popupMenu.add(new MapPoolDeleteAllHTMLAction(
							DesignHTMLInfoJPane.this, map));

					popupMenu.show(DesignHTMLInfoJPane.this, evt.getX(), evt
							.getY());

				}
			}

		});

		/** As this is a WeakHashMapSet, we don't have to remove it... **/
		ace.getMapPool().addChangeListener(listenForMapChanges);
	}
}
