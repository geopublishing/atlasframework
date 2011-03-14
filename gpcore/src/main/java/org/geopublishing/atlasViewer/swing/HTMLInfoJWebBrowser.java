/**
 * 
 */
package org.geopublishing.atlasViewer.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.JNLPUtil;
import org.geopublishing.atlasViewer.http.AtlasProtocol;
import org.geopublishing.atlasViewer.map.Map;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserListener;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserNavigationEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowOpeningEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowWillOpenEvent;
import chrriis.dj.sweet.NSOption;

import de.schmitzm.io.IOUtil;
import de.schmitzm.swing.ExceptionDialog;
import de.schmitzm.swing.JPanel;

/**
 * An HTML view based on the {@link JWebBrowser} of <i>The DJ Project</i>.
 */
public class HTMLInfoJWebBrowser extends JWebBrowser implements HTMLInfoPaneInterface {
  final static private Logger LOGGER = Logger.getLogger(HTMLInfoJPane.class);

  /**
   * This message is shows when no data can be found.
   */
  final String NODATA_MSG = AtlasViewerGUI
          .R("HTMLInfoPane.NODATA.MSG.sorry_no_info_available");

  /** Configuration of the atlas */
  protected AtlasConfig atlasConfig = null;
  
  /**
   * Constructs a new instance.
   * @param map
   *            {@link Map} to load HTML for via {@link Map#getInfoURL()}
   * 
   */
  public HTMLInfoJWebBrowser(Map map) {
     this(map.getInfoURL(), map.getAc());
   };

   /**
    * Constructs a new instance.
    * @param url
    *            Where to load the HTML from?
    * @param ac
    *            {@link AtlasConfig}
    */
   public HTMLInfoJWebBrowser(URL url, AtlasConfig ac) {
     super();  
     // Initialize the native part for the browser interface.
     // Multiple calls do not take effect! 
     NativeInterface.open();
     
     this.atlasConfig = ac;
     this.setBarsVisible(false);
     showDocument(url);
     addWebBrowserListener(LISTENER);
   }

//   /**
//    * This page shall always be rendered with anti-aliasing.
//    */
//   @Override
//   protected void paintComponent(Graphics g) {
//       Graphics2D g2 = (Graphics2D) g;
//
//       g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//               RenderingHints.VALUE_ANTIALIAS_ON);
//
//       super.paintComponent(g2);
//
//       g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//               RenderingHints.VALUE_ANTIALIAS_OFF);
//   }

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
   
  /**
   * Loads a document in the html view. 
   * @param url source url
   */
  @Override
  public void showDocument(URL url) {
    if (url == null) {
      this.showDocument(NODATA_MSG);
      return;
    }
    navigate(url.toString());
  }

  /**
   * Loads a document in the html view. 
   * @param url source url
   */
  @Override
  public void showDocument(String content) {
    setHTMLContent(content);
  }

  /**
   * Returns {@code this}.
   */
  @Override
  public JComponent getComponent() {
    return this;
  }

  /**
   * Returns {@code true}, because {@link JWebBrowser}
   * already provides scrolling.
   */
  @Override
  public boolean hasScrollPane() {
    return false;
  }
  
  private final WebBrowserListener LISTENER = new WebBrowserAdapter() {
    public void commandReceived(WebBrowserEvent e, String command, String[] args) {
      LOGGER.debug("commandRecveived "+e+"  "+command+"  "+args);
      
    }

    private void blockLocationChange(WebBrowserNavigationEvent ev) {
      ev.consume();   
//      // The URL Changing event is special: it is synchronous so disposal must be deferred.   
//      SwingUtilities.invokeLater(new Runnable() {   
//        public void run() {   
//          getWebBrowserWindow().dispose();   
//        }   
//      });   
    }
    
    public void locationChanging(WebBrowserNavigationEvent ev) {
      try {
        /**
         * Is this a pdf:// link?
         */
        String evDesc = ev.getNewResourceLocation();
        String before = evDesc;
        if (JNLPUtil.isAtlasDataFromJWS(atlasConfig)) {
            /**
             * If the atlas data is coming from JWS, and this evDesc
             * contains "../../.." we will change it to "../..".
             * Why? Because this URL references something that is
             * expected next to the <code>atlas.xml</code> in the
             * <code>ad</code> directory. In JWS, the
             */
            evDesc.replace("../../..", "../..");
            evDesc.replace("..\\..\\..", "..\\..");
            LOGGER.info("The URL to the PDF has been changed from "
                    + before + " to " + evDesc + " bacause of JWS.");
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

        if (AtlasProtocol.PDF.test(evDesc)) {
            /**
             * Extract the path to the PDF
             */
            String basePath = getResourceLocation();
            final int lastSlashPos = basePath.lastIndexOf('/');
            if (lastSlashPos >= 0)
                basePath = basePath.substring(0, lastSlashPos);

            String pdfPathOrName = AtlasProtocol.PDF.cutOff(evDesc);

            String pdfUrlString = basePath + "/" + pdfPathOrName;

            URL pdfUrl = new URL(pdfUrlString);

            LOGGER.debug("pdfPathOrName = " + pdfPathOrName);
            LOGGER.debug("pdfUrl = " + pdfUrlString);
            AtlasProtocol.PDF.performPDF(HTMLInfoJWebBrowser.this,
                    pdfUrl, new File(pdfPathOrName).getName());
            blockLocationChange(ev); // stop standard link processing
        } else if (AtlasProtocol.MAP.test(evDesc)
                && (atlasConfig != null)) {
            /**
             * Is this a map:// link?
             */

            AtlasProtocol.MAP
                    .performMap(
                            HTMLInfoJWebBrowser.this,
                            ev.getNewResourceLocation() != null ? AtlasProtocol.MAP
                                    .cutOff(ev.getNewResourceLocation())
                                    : AtlasProtocol.MAP.cutOff(ev
                                            .getNewResourceLocation()),
                            atlasConfig);
            blockLocationChange(ev); // stop standard link processing
        } else if (AtlasProtocol.BROWSER.test(evDesc)) {
            /**
             * Is this a browser:// link?
             */
            String targetPath = AtlasProtocol.BROWSER
                    .cutOff(evDesc);

            if (targetPath.startsWith("http://")
                    || targetPath.startsWith("https://")) {
                /**
                 * We open an external URL like: http://www.bahn.de
                 */
                AtlasProtocol.BROWSER.performBrowser(
                        HTMLInfoJWebBrowser.this, new URL(targetPath));
            } else {
                /**
                 * We open an internal HTML document and end up with
                 * something like:
                 * http://localhost:8282/ad/asas/info.html
                 */

                AtlasProtocol.BROWSER.performBrowser(
                        HTMLInfoJWebBrowser.this, IOUtil.extendURL(
                                IOUtil.getParentUrl(new URL(getResourceLocation())),
                                targetPath));
            }
            blockLocationChange(ev); // stop standard link processing
        } else {
            // open the link in the local pane
            // do nothing -> standard process will open the linked document            
        }
      } catch (Exception e) {
          ExceptionDialog.show(HTMLInfoJWebBrowser.this, e, null,
                  "Error while handling a URL protocoll");
      }
    }
  };

}
