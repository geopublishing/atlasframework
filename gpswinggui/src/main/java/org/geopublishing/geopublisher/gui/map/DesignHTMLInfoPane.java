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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import org.geopublishing.atlasViewer.GpCoreUtil;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.map.MapPool.EventTypes;
import org.geopublishing.atlasViewer.swing.HTMLInfoPaneInterface;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;

import de.schmitzm.i18n.I18NUtil;
import de.schmitzm.i18n.Translation;

/**
 * Extends an {@link HTMLInfoPane} allows Drag'n'Drop to insert HTML Infomation
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 * 
 */
public class DesignHTMLInfoPane implements HTMLInfoPaneInterface {

	private final Map map;
	
	private final HTMLInfoPaneInterface htmlPane;

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
		    htmlPane.showDocument(GeopublisherGUI.R("DesignHTMLPanel.NoHTML.Message", I18NUtil
					.getFirstLocaleForLang(Translation.getActiveLang())
					.getDisplayLanguage()));
		} else
			htmlPane.showDocument(url);
	}
	
    @Override
    public void showDocument(String content) {
      htmlPane.showDocument(content);
    }

    public JComponent getComponent() {
	  return htmlPane.getComponent();
	}

    public boolean hasScrollPane() {
      return htmlPane.hasScrollPane();
    }
    
    public void connectPopupMenu(JPopupMenu menu) {
      htmlPane.connectPopupMenu(menu);
    }

    public DesignHTMLInfoPane(AtlasConfigEditable ace_, Map map_) {
      this(ace_, map_, GpCoreUtil.createHTMLInfoPane(map_));
    }
    
    public DesignHTMLInfoPane(AtlasConfigEditable ace_, Map map_, HTMLInfoPaneInterface htmlPane_) {
		this.ace = ace_;
		this.map = map_;
		this.htmlPane = htmlPane_;

		// // Make the DatapoolJList accept Drops form the file system to
		// import...
		// final ImportHtmlInfoDropTargetListener importByDropTargetListener
		// = new ImportHtmlInfoDropTargetListener(
		// SwingUtil.getParentWindow(this), map, ace, this);
		//
		// @SuppressWarnings("unused")
		// DropTarget dt = new DropTarget(this, importByDropTargetListener);
		
        JPopupMenu popupMenu = new JPopupMenu();
        /** Edit HTML info files... */
        popupMenu.add(new MapPoolEditHTMLAction(map));
        /** Delete HTML info files... */
        popupMenu.add(new MapPoolDeleteAllHTMLAction(
                getComponent(), map));
        connectPopupMenu(popupMenu);
        
		/** As this is a WeakHashMapSet, we don't have to remove it... **/
		ace.getMapPool().addChangeListener(listenForMapChanges);
	}

}
