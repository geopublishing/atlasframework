/**
 * 
 */
package org.geopublishing.atlasViewer.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JComponent;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.GpCoreUtil;
import org.geopublishing.atlasViewer.JNLPUtil;
import org.geopublishing.atlasViewer.http.AtlasProtocol;
import org.geopublishing.atlasViewer.http.AtlasProtocolException;
import org.geopublishing.atlasViewer.map.Map;
import org.lobobrowser.html.HtmlRendererContext;
import org.lobobrowser.html.UserAgentContext;
import org.lobobrowser.html.gui.HtmlBlockPanel;
import org.lobobrowser.html.gui.HtmlPanel;
import org.lobobrowser.html.test.SimpleHtmlRendererContext;
import org.lobobrowser.html.test.SimpleUserAgentContext;
import org.w3c.dom.html2.HTMLElement;

import de.schmitzm.io.IOUtil;
import de.schmitzm.swing.ExceptionDialog;
import de.schmitzm.swing.event.PipedMouseListener;

/**
 * An HTML view based on the {@link JWebBrowser} of <i>The DJ Project</i>.
 */
public class HTMLInfoLoboBrowser extends HtmlPanel implements
    HTMLInfoPaneInterface {
  final static private Logger LOGGER = Logger.getLogger(HTMLInfoJPane.class);

  /**
   * This message is shows when no data can be found.
   */
  final String NODATA_MSG = GpCoreUtil.R("HTMLInfoPane.NODATA.MSG.sorry_no_info_available");

  /** Configuration of the atlas */
  protected AtlasConfig atlasConfig = null;

  /** HTML context used for the HtmlPanel */
  private SimpleHtmlRendererContext htmlContext;

  /**
   * Constructs a new instance.
   * @param map {@link Map} to load HTML for via {@link Map#getInfoURL()}
   */
  public HTMLInfoLoboBrowser(Map map) {
    this(map.getInfoURL(), map.getAc());
  };

  /**
   * Constructs a new instance.
   * @param url Where to load the HTML from?
   * @param ac {@link AtlasConfig}
   */
  public HTMLInfoLoboBrowser(URL url, AtlasConfig ac) {
    super();
    this.atlasConfig = ac;
    htmlContext = new SimpleHtmlRendererContext(this,
                                                new SimpleUserAgentContext());
    showDocument(url);
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
    htmlContext.navigate(url, "");
  }

  /**
   * Loads a document in the html view.
   * @param url source url
   */
  @Override
  public void showDocument(String content) {
    setHtml(content, "", htmlContext);
  }

  /**
   * Returns {@code this}.
   */
  @Override
  public JComponent getComponent() {
    return this;
  }

  /**
   * Returns {@code true}, because {@link JWebBrowser} already provides
   * scrolling.
   */
  @Override
  public boolean hasScrollPane() {
    return false;
  }

  /**
   * Returns the html context used for the browser.
   */
  private SimpleHtmlRendererContext getContext() {
    return this.htmlContext;
  }

  /**
   * Modifies the {@link HtmlBlockPanel} created by the super-method. The existing
   * {@link MouseListener MouseListeners} are replaced by {@link PipedMouseListener PipedMouseListeners}
   * to catch the {@link AtlasProtocolException} when clicking on a link with special
   * {@link AtlasProtocol}. Instead of following the link, the special atlas protocol
   * is performed ({@link #performSpecialLink(String)}.
   */
  @Override
  protected HtmlBlockPanel createHtmlBlockPanel(UserAgentContext ucontext, HtmlRendererContext rcontext) {
    HtmlBlockPanel blockPanel = super.createHtmlBlockPanel(ucontext, rcontext);
    for (MouseListener l : blockPanel.getMouseListeners()) {
      blockPanel.removeMouseListener(l);
      blockPanel.addMouseListener( new PipedMouseListener(l) {
          @Override
          public void mouseReleased(MouseEvent e) {
            try {
              super.mouseReleased(e);
            } catch (AtlasProtocolException err) {
                LOGGER.debug("Haha!!! Abgefangen!");
                performSpecialLink(err.getURL());
            }
          }
        }
      );
    }
    return blockPanel;
  }

  /**
   * Performs a link by {@link GpCoreUtil#performSpecialHTMLLink(java.awt.Component, AtlasConfig, String, String)}.
   * @param destURL
   */
  protected void performSpecialLink(String destURL) {
    boolean specialLinkPerformed = AtlasProtocol.performLink(
        this,
        atlasConfig,
        getContext().getCurrentURL(),
        destURL
    );
    if ( !specialLinkPerformed )
      throw new AtlasProtocolException(destURL, "Atlas protocol could not be performed: "+destURL);
  }

}