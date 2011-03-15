/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasViewer.http;

import java.awt.Component;
import java.awt.Window;
import java.io.File;
import java.net.URL;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.GpCoreUtil;
import org.geopublishing.atlasViewer.JNLPUtil;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geopublishing.atlasViewer.swing.AtlasAboutDialog;
import org.geopublishing.atlasViewer.swing.AtlasPopupDialog;
import org.geopublishing.atlasViewer.swing.AtlasViewerGUI;
import org.geopublishing.atlasViewer.swing.ClickInfoDialog;
import org.geopublishing.atlasViewer.swing.HTMLBrowserWindow;

import de.schmitzm.io.IOUtil;
import de.schmitzm.swing.ExceptionDialog;
import de.schmitzm.swing.SwingUtil;

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

	/**
	 * Checks whether an URL is any of the special atlas protocols.
	 */
	public static boolean check(String urlString) {
	  for ( AtlasProtocol prot : values() )
	    if ( prot.test(urlString) )
	      return true;
	  return false;
	}

	/**
     * Checks a destination URL for special protocol information (e.g. "browser://..." or
     * "pdf://...") and (if found) performs the corresponding action.
     * @param parent       parent component
     * @param atlasConfig  atlas configuration
     * @param currURL      current URL (to determine relative pathes)
     * @param destURL      destination URL to check
     * @return <code>true</code> if special protocol was found and action was performed;
     *         <code>false</code> if link is a standard link, which should be performed by
     *         the browser 
     */
    public static boolean performLink(Component parent, AtlasConfig atlasConfig, String currURL, String destURL) {
        try {
          String evDesc = destURL;
          String before = evDesc;
          
          if (JNLPUtil.isAtlasDataFromJWS(atlasConfig)) {
            /**
             * If the atlas data is coming from JWS, and this evDesc contains
             * "../../.." we will change it to "../..". Why? Because this URL
             * references something that is expected next to the
             * <code>atlas.xml</code> in the <code>ad</code> directory. In JWS, the
             */
            evDesc.replace("../../..", "../..");
            evDesc.replace("..\\..\\..", "..\\..");
            LOGGER.info("The URL to the PDF has been changed from " + before +
                        " to " + evDesc + " bacause of JWS.");
          } else {
            // // If we are started from DISK, AND the PDF URL
            // starts
            // // with "../../.." it is supposed to reference a root
            // // PDF, which lies one level higher.
            //
            // evDesc.replace("../../..", "../../../..");
            // evDesc.replace("..\\..\\..", "..\\..\\..\\..");
            // LOGGER.info("The URL to the PDF has been changed from "
            // + before + " to " + evDesc
            // + " bacause of DISK.");
          }

          /**
           * Is this a pdf:// link?
           */
          if (AtlasProtocol.PDF.test(evDesc)) {
            /**
             * Extract the path to the PDF
             */
            String basePath = currURL;
            final int lastSlashPos = basePath.lastIndexOf('/');
            if (lastSlashPos >= 0)
              basePath = basePath.substring(0, lastSlashPos);

            String pdfPathOrName = AtlasProtocol.PDF.cutOff(evDesc);

            String pdfUrlString = basePath + "/" + pdfPathOrName;

            URL pdfUrl = new URL(pdfUrlString);

            LOGGER.debug("pdfPathOrName = " + pdfPathOrName);
            LOGGER.debug("pdfUrl = " + pdfUrlString);
            AtlasProtocol.PDF.performPDF(parent, pdfUrl,
                new File(pdfPathOrName).getName());
            return (true); // stop standard link processing
          } 
          
          /**
           * Is this a map:// link?
           */
          if (AtlasProtocol.MAP.test(evDesc) && (atlasConfig != null)) {
            AtlasProtocol.MAP.performMap(parent,
                AtlasProtocol.MAP.cutOff(destURL.toString()), atlasConfig);
            return true; // stop standard link processing
          }
          
          /**
           * Is this a browser:// link?
           */
          if (AtlasProtocol.BROWSER.test(evDesc)) {
            String targetPath = AtlasProtocol.BROWSER.cutOff(evDesc);
            if (targetPath.startsWith("http://") ||
                targetPath.startsWith("https://")) {
              /**
               * We open an external URL like: http://www.bahn.de
               */
              AtlasProtocol.BROWSER.performBrowser(parent,new URL(targetPath));
            } else {
              /**
               * We open an internal HTML document and end up with something like:
               * http://localhost:8282/ad/asas/info.html
               */
              AtlasProtocol.BROWSER.performBrowser(parent,
                  IOUtil.extendURL(IOUtil.getParentUrl( new URL(destURL) ), targetPath));
            }
            return true; // stop standard link processing
          }
        } catch (Exception e) {
          ExceptionDialog.show(parent, e, null,
              "Error while handling a URL protocoll");
        }

        // open the link in the local pane
        // do nothing -> standard process will open the linked
        // document
        return false;
      }
	
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
		AVSwingUtil.lauchHTMLviewer(owner, url);
	}

	/**
	 * Opens the PDF
	 */
	public void performPDF(Component owner, URL url, String title) {
		AVSwingUtil.launchPDFViewer(owner, url, title);
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
			AVSwingUtil.showMessageDialog(owner,
					GpCoreUtil.R("AtlasMapLink.TargetMapIDCantBeFound", mapId));
			return;
		}

		/**
		 * If no atlas viewer is open ATM, we just post a message about what
		 * would happen.
		 */
		if (!AtlasViewerGUI.isRunning()) {
			AVSwingUtil.showMessageDialog(owner, GpCoreUtil.R(
					"AtlasMapLink.NotOpeningBecauseNoAtlasViewerOpen",
					map.getTitle()));
			return;
		}

		AtlasViewerGUI.getInstance().setMap(map);

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
