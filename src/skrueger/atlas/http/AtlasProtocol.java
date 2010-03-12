/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package skrueger.atlas.http;

import java.awt.Component;
import java.awt.Window;
import java.net.URL;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import schmitzm.swing.SwingUtil;
import skrueger.atlas.AVUtil;
import skrueger.atlas.AtlasConfig;
import skrueger.atlas.AtlasViewer;
import skrueger.atlas.gui.AtlasAboutDialog;
import skrueger.atlas.gui.AtlasPopupDialog;
import skrueger.atlas.gui.ClickInfoDialog;
import skrueger.atlas.gui.HTMLBrowserWindow;
import skrueger.atlas.map.Map;

/**
 * Defines the different AtlasViewer-specific protocol types... See
 * http://www.geopublishing.org/Internal_Links for a description.
 * 
 * @author stefan
 * 
 */
public enum AtlasProtocol {

	MAP, PDF, BROWSER, IMAGE, HTML;

	final static private Logger LOGGER = Logger.getLogger(AtlasProtocol.class);

	@Override
	public String toString() {
		switch (this) {
		case BROWSER:
			return "browser";
		case HTML:
			return "html";
		case MAP:
			return "map";
		case IMAGE:
			return "img";
		case PDF:
			return "pdf";
		default:
			throw new IllegalArgumentException("Protocol Enum not known!?");
		}
	}

	/**
	 * @param fullURLString
	 *            e.g. pdf://aba.pdf
	 * @return aba.pad
	 */
	public String cutOff(String fullURLString) {
		return fullURLString.substring(toString().length() + 3);
	}

	/**
	 * Tests whether the given {@link String} is a <code>protocol://</code> like
	 * string.
	 * 
	 * @param urlString
	 *            A {@link String} to test, e.g.: img://asd.asd
	 */
	public boolean test(String urlString) {
		if (urlString == null)
			return false;
		if (urlString.toLowerCase().startsWith(this.toString()))
			return true;
		return false;
	}

	/**
	 * Opens the external Browser for the given {@link URL}
	 */
	public void performBrowser(Component owner, URL url) {
		AVUtil.lauchHTMLviewer(owner, url);
	}

	/**
	 * Opens the PDF
	 */
	public void performPDF(Component owner, URL url, String title) {
		AVUtil.launchPDFViewer(owner, url, title);
	}

	/**
	 * Opens the HTML URL inside an internal {@link HTMLBrowserWindow}
	 */
	public void performHtml(Component owner, URL url, String title,
			AtlasConfig atlasConfig) {
		final HTMLBrowserWindow browserWindow = new HTMLBrowserWindow(owner,
				url, title, atlasConfig);
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				browserWindow.setVisible(true);
			}

		});
	}
//
//	/**
//	 * Make the AtlasViewer open the map with the given id.
//	 * 
//	 * @param owner
//	 *            A Component linking to the parent GUI.
//	 * @param url
//	 *            <code>map://1232123123</code>
//	 * @param atlasConfig
//	 */
//	public void performMap(Component owner, String href, AtlasConfig atlasConfig) {
//		String mapId = AtlasProtocol.MAP.cutOff(href);
//		performMap(owner, mapId, atlasConfig);
//	}

	/**
	 * Make the AtlasViewer open the map with the given id.
	 * 
	 * @param owner
	 *            A Component linking to the parent GUI.
	 * @param url
	 *            <code>map://1232123123</code>
	 * @param atlasConfig
	 */
	public void performMap(Component owner, String mapId,
			AtlasConfig atlasConfig) {

		Map map = atlasConfig.getMapPool().get(mapId);
		if (map == null) {
			LOGGER.error(mapId + " is not a known mapID.");
			AVUtil.showMessageDialog(owner, AtlasViewer.R(
					"AtlasMapLink.TargetMapIDCantBeFound", mapId));
			return;
		}

		/**
		 * If no atlas viewer is open ATM, we just post a message about what
		 * would happen.
		 */
		if (!AtlasViewer.isRunning()) {
			AVUtil.showMessageDialog(owner, AtlasViewer.R(
					"AtlasMapLink.NotOpeningBecauseNoAtlasViewerOpen", map
							.getTitle()));
			return;
		}

		AtlasViewer.getInstance().setMap(map);

		// Determine if we should close the dialog.
		Window parentWindow = SwingUtil.getParentWindow(owner);
		if (parentWindow instanceof HTMLBrowserWindow
				|| parentWindow instanceof AtlasAboutDialog
				|| parentWindow instanceof ClickInfoDialog
				|| parentWindow instanceof AtlasPopupDialog) {
			parentWindow.dispose();
		}

	}

}
