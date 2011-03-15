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
package org.geopublishing.atlasViewer.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.GpCoreUtil;
import org.geopublishing.atlasViewer.JNLPUtil;
import org.geopublishing.atlasViewer.http.AtlasProtocol;
import org.geopublishing.atlasViewer.map.Map;

import de.schmitzm.io.IOUtil;
import de.schmitzm.swing.ExceptionDialog;

/**
 * A panel that displays HTML Info about a given {@link Map} The HTML will be
 * antialiased if set by AVProps.Keys.antialiasingHTML
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 */
public class HTMLInfoJPane extends JEditorPane implements HTMLInfoPaneInterface {

  final static private Logger LOGGER = Logger.getLogger(HTMLInfoJPane.class);

  /**
   * Returns {@code this}.
   */
  @Override
  public JComponent getComponent() {
    return this;
  }

  /**
   * Returns {@code false}, because {@link JEditorPane} does not provide
   * scrolling.
   */
  @Override
  public boolean hasScrollPane() {
    return false;
  }

  private final HyperlinkListener LISTENER = new HyperlinkListener() {

    @Override
    public void hyperlinkUpdate(HyperlinkEvent ev) {
      if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        
        if (ev instanceof HTMLFrameHyperlinkEvent) {
          JEditorPane p = (JEditorPane) ev.getSource();
          HTMLDocument doc = (HTMLDocument) p.getDocument();
          doc.processHTMLFrameHyperlinkEvent((HTMLFrameHyperlinkEvent) ev);
        }
        
        boolean specialLinkPerformed = AtlasProtocol.performLink(
            HTMLInfoJPane.this, atlasConfig, getPage().toExternalForm(),
            ev.getDescription());
        if (specialLinkPerformed)
          return;
        
        // if atlas protocol link was not
        // performed, show the linked
        // site in JEditorPane
        showDocument(ev.getURL());

        // try {
        //
        // if (ev instanceof HTMLFrameHyperlinkEvent) {
        // JEditorPane p = (JEditorPane) ev.getSource();
        // HTMLDocument doc = (HTMLDocument) p.getDocument();
        // doc.processHTMLFrameHyperlinkEvent((HTMLFrameHyperlinkEvent) ev);
        // }
        //
        // /**
        // * Is this a pdf:// link?
        // */
        // String evDesc = ev.getDescription();
        //
        // String before = evDesc;
        // if (JNLPUtil.isAtlasDataFromJWS(atlasConfig)) {
        // /**
        // * If the atlas data is coming from JWS, and this evDesc
        // * contains "../../.." we will change it to "../..".
        // * Why? Because this URL references something that is
        // * expected next to the <code>atlas.xml</code> in the
        // * <code>ad</code> directory. In JWS, the
        // */
        // evDesc.replace("../../..", "../..");
        // evDesc.replace("..\\..\\..", "..\\..");
        // LOGGER.info("The URL to the PDF has been changed from "
        // + before + " to " + evDesc + " bacause of JWS.");
        // } else {
        // // // If we are started from DISK, AND the PDF URL
        // // starts
        // // // with "../../.." it is supposed to reference a root
        // // // PDF, which lies one level higher.
        // //
        // // evDesc.replace("../../..", "../../../..");
        // // evDesc.replace("..\\..\\..", "..\\..\\..\\..");
        // // LOGGER.info("The URL to the PDF has been changed from "
        // // + before + " to " + evDesc
        // // + " bacause of DISK.");
        //
        // }
        //
        // if (AtlasProtocol.PDF.test(evDesc)) {
        //
        // /**
        // * Extract the path to the PDF
        // */
        // String basePath = getPage().toExternalForm();
        // final int lastSlashPos = basePath.lastIndexOf('/');
        // if (lastSlashPos >= 0)
        // basePath = basePath.substring(0, lastSlashPos);
        //
        // String pdfPathOrName = AtlasProtocol.PDF.cutOff(evDesc);
        //
        // String pdfUrlString = basePath + "/" + pdfPathOrName;
        //
        // URL pdfUrl = new URL(pdfUrlString);
        //
        // LOGGER.debug("pdfPathOrName = " + pdfPathOrName);
        // LOGGER.debug("pdfUrl = " + pdfUrlString);
        //
        // AtlasProtocol.PDF.performPDF(HTMLInfoJPane.this,
        // pdfUrl, new File(pdfPathOrName).getName());
        //
        // } else if (AtlasProtocol.MAP.test(evDesc)
        // && (atlasConfig != null)) {
        // /**
        // * Is this a map:// link?
        // */
        //
        // AtlasProtocol.MAP
        // .performMap(
        // HTMLInfoJPane.this,
        // ev.getURL() != null ? AtlasProtocol.MAP
        // .cutOff(ev.getURL().getFile())
        // : AtlasProtocol.MAP.cutOff(ev
        // .getDescription()),
        // atlasConfig);
        //
        // } else if (AtlasProtocol.BROWSER.test(evDesc)) {
        // /**
        // * Is this a browser:// link?
        // */
        //
        // String targetPath = AtlasProtocol.BROWSER
        // .cutOff(evDesc);
        //
        // if (targetPath.startsWith("http://")
        // || targetPath.startsWith("https://")) {
        // /**
        // * We open an external URL like: http://www.bahn.de
        // */
        // AtlasProtocol.BROWSER.performBrowser(
        // HTMLInfoJPane.this, new URL(targetPath));
        // } else {
        // /**
        // * We open an internal HTML document and end up with
        // * something like:
        // * http://localhost:8282/ad/asas/info.html
        // */
        //
        // AtlasProtocol.BROWSER.performBrowser(
        // HTMLInfoJPane.this, IOUtil.extendURL(
        // IOUtil.getParentUrl(getPage()),
        // targetPath));
        // }
        //
        // } else {
        // // open the link in the local pane
        // showDocument(ev.getURL());
        // }
        //
        // } catch (Exception e) {
        // ExceptionDialog.show(HTMLInfoJPane.this, e, null,
        // "Error while handling a URL protocoll");
        // }
      }
    }
  };

  /**
   * This message is shows when no data can be found.
   */
  final String NODATA_MSG = GpCoreUtil.R("HTMLInfoPane.NODATA.MSG.sorry_no_info_available");

  private final AtlasConfig atlasConfig;

  /**
   * Constructs a {@link HTMLInfoJPane} which is an extension of a
   * {@link JEditorPane}. It can be used to display HTML contens from a
   * {@link URL}.
   * @param map {@link Map} to load HTML for via {@link Map#getInfoURL()}
   */
  public HTMLInfoJPane(Map map) {
    this(map.getInfoURL(), map.getAc());
  };

  /**
   * Constructs a {@link HTMLInfoJPane} which is an extension of a
   * {@link JEditorPane}. It can be used to display HTML contens from a
   * {@link URL}.
   * @param url Where to load the HTML from?
   * @param ac {@link AtlasConfig}
   */
  public HTMLInfoJPane(URL url, AtlasConfig ac) {
    this.atlasConfig = ac;
    setContentType("text/html");
    setEditable(false);

    showDocument(url);
    addListener();
  }

  /**
   * This method adds a {@link HyperlinkListener} that reacts to atlas specific
   * link protocoll:
   * <ul>
   * <li>pdf://</li>
   * <li>browser://</li>
   * <li>map://</li>
   * </ul>
   * @see AtlasViewerGUI.PROTOCOL_MAP
   * @see AtlasViewerGUI.PROTOCOL_PDF
   * @see AtlasViewerGUI.PROTOCOL_BROWSER
   */
  private void addListener() {

    addHyperlinkListener(LISTENER);
  }

  /**
   * Opens a standard, java compatible HTTP {@link URL} in the
   * {@link JEditorPane}
   * @param url
   */
  public void showDocument(URL url) {
    setEditable(false);
    setContentType("text/html");

    if (url == null) {
      setText(NODATA_MSG);
      return;
    }
    try {
      synchronized (url) {
        // Force the JEditorPane to reload the document
        getDocument().putProperty(Document.StreamDescriptionProperty, null);
        setPage(url);
      }
    } catch (IOException ioex) {
      LOGGER.error("*** failed to load URL: " + ioex.toString());
    }
  }

  /**
   * Loads a document in the html view.
   * @param url source url
   */
  @Override
  public void showDocument(String content) {
    setText(content);
  }

  /**
   * This page shall always be rendered with anti-aliasing.
   */
  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;

    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);

    super.paintComponent(g2);

    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_OFF);
  }

  /**
   * For Screenshots that are better printable
   */
  @Override
  public void print(Graphics g) {
    final Color backup = getBackground();
    setBackground(Color.white);
    super.print(g);
    setBackground(backup);
  }

}
