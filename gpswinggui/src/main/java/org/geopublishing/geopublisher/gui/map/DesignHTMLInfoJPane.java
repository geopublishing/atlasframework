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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.map.MapPool.EventTypes;
import org.geopublishing.atlasViewer.swing.HTMLInfoJPane;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.AtlasCreator;

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
