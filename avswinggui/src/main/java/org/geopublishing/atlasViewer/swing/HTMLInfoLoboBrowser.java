/**
 * 
 */
package org.geopublishing.atlasViewer.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.GpCoreUtil;
import org.geopublishing.atlasViewer.http.AtlasProtocol;
import org.geopublishing.atlasViewer.http.AtlasProtocolException;
import org.geopublishing.atlasViewer.map.Map;
import org.lobobrowser.html.FormInput;
import org.lobobrowser.html.HtmlRendererContext;
import org.lobobrowser.html.UserAgentContext;
import org.lobobrowser.html.gui.FrameSetPanel;
import org.lobobrowser.html.gui.HtmlBlockPanel;
import org.lobobrowser.html.gui.HtmlPanel;
import org.lobobrowser.html.test.SimpleHtmlRendererContext;
import org.lobobrowser.html.test.SimpleUserAgentContext;
import org.w3c.dom.html2.HTMLElement;

import de.schmitzm.swing.event.PipedMouseListener;

/**
 * An HTML view based on the {@link HtmlPanel} of <i>LOBO browser Project</i>.
 */
public class HTMLInfoLoboBrowser extends HtmlPanel implements
		HTMLInfoPaneInterface {
	/**
	 * 
	 */
	private static final long serialVersionUID = 381089767702329569L;

	final static private Logger LOGGER = Logger.getLogger(HTMLInfoJPane.class);

	/**
	 * This message is shows when no data can be found.
	 */
	final String NODATA_MSG = GpCoreUtil
			.R("HTMLInfoPane.NODATA.MSG.sorry_no_info_available");

	/** Configuration of the atlas */
	protected AtlasConfig atlasConfig = null;
	/** Popup menu for html view. */
	protected JPopupMenu popupMenu = null;

	/** HTML context used for the HtmlPanel */
	private SimpleHtmlRendererContext htmlContext;

	/**
	 * Constructs a new instance.
	 * 
	 * @param map
	 *            {@link Map} to load HTML for via {@link Map#getInfoURL()}
	 */
	public HTMLInfoLoboBrowser(Map map) {
		this(map.getInfoURL(), map.getAc());
	};

	/**
	 * Constructs a new instance.
	 * 
	 * @param url
	 *            Where to load the HTML from?
	 * @param ac
	 *            {@link AtlasConfig}
	 */
	public HTMLInfoLoboBrowser(URL url, AtlasConfig ac) {
		super();
		this.atlasConfig = ac;
		SimpleUserAgentContext simpleUserAgentContext = new SimpleUserAgentContext();

		htmlContext = new SimpleHtmlRendererContext(this,
				simpleUserAgentContext) {

			/**
			 * Indicates whether navigation (via
			 * {@link #submitForm(String, URL, String, String, FormInput[])})
			 * should be asynchronous. This is overwritten here to avoid
			 * problems with MigLayout rendering. MigLayout has a problem
			 * waiting for a panel to determine its size.
			 */
			protected boolean isNavigationAsynchronous() {
				return false;
			}

			@Override
			public boolean onContextMenu(HTMLElement element, MouseEvent event) {
				if (HTMLInfoLoboBrowser.this.popupMenu != null) {
					HTMLInfoLoboBrowser.this.popupMenu.show(
							event.getComponent(), event.getX(), event.getY());
					return false;
				}
				return super.onContextMenu(element, event);
			}
		};
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
	 * 
	 * @param url
	 *            source url
	 */
	@Override
	public void showDocument(URL url) {
		if (url == null) {
			this.showDocument(NODATA_MSG);
			return;
		}

		// When we had edit a page, there must be a navigate(.)
		// and reload(.) to show the new content (why??)
		// Note: because currentURL changes on navigate, we can
		// not do this check AFTER navigate(.)!!
		if (htmlContext.getCurrentURL() != null
				&& htmlContext.getCurrentURL().equalsIgnoreCase(url.toString())) {
			htmlContext.navigate(url, "");
			htmlContext.reload();
		} else
			htmlContext.navigate(url, "");

		// Document d = (Document) this.getRootNode();
		// HTMLDocumentImpl rootNode2 = (HTMLDocumentImpl)
		// htmlContext.getHtmlPanel().getRootNode();

	}

	/**
	 * Loads a document in the html view.
	 * 
	 * @param url
	 *            source url
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
	 * Returns {@code true}, because {@link HtmlPanel} already provides
	 * scrolling.
	 */
	@Override
	public boolean hasScrollPane() {
		return true;
	}

	/**
	 * Connects a popup menu to the html view.
	 */
	@Override
	public void connectPopupMenu(JPopupMenu menu) {
		this.popupMenu = menu;
	}

	/**
	 * Returns the html context used for the browser.
	 */
	private SimpleHtmlRendererContext getContext() {
		return this.htmlContext;
	}

	/**
	 * Modifies the {@link HtmlBlockPanel} created by the super-method. The
	 * existing {@link MouseListener MouseListeners} are replaced by
	 * {@link PipedMouseListener PipedMouseListeners} to catch the
	 * {@link AtlasProtocolException} when clicking on a link with special
	 * {@link AtlasProtocol}. Instead of following the link, the special atlas
	 * protocol is performed using ({@link #performSpecialLink(String)}.
	 */
	@Override
	protected HtmlBlockPanel createHtmlBlockPanel(UserAgentContext ucontext,
			HtmlRendererContext rcontext) {
		HtmlBlockPanel blockPanel = super.createHtmlBlockPanel(ucontext,
				rcontext);
		pipeMouseListeners(blockPanel);
		return blockPanel;
	}

	/**
	 * Modifies the {@link FrameSetPanel} created by the super-method. The
	 * existing {@link MouseListener MouseListeners} are replaced by
	 * {@link PipedMouseListener PipedMouseListeners} to catch the
	 * {@link AtlasProtocolException} when clicking on a link with special
	 * {@link AtlasProtocol}. Instead of following the link, the special atlas
	 * protocol is performed using ({@link #performSpecialLink(String)}.
	 */
	@Override
	protected FrameSetPanel createFrameSetPanel() {
		FrameSetPanel framesetPanel = super.createFrameSetPanel();
		pipeMouseListeners(framesetPanel);
		return framesetPanel;
	}

	/**
	 * Replaces all {@link MouseListener MouseListeners} of a {@link Component}
	 * with a {@link PipedMouseListener}, which catches an
	 * {@link AtlasProtocolException} to perform atlas protocol links.
	 * 
	 * @see #performSpecialLink(String)
	 */
	private void pipeMouseListeners(Component comp) {
		for (MouseListener l : comp.getMouseListeners()) {
			comp.removeMouseListener(l);
			comp.addMouseListener(new PipedMouseListener(l) {
				@Override
				public void mouseReleased(MouseEvent e) {
					try {
						super.mouseReleased(e);
					} catch (AtlasProtocolException err) {
						// LOGGER.debug("Haha!!! Abgefangen!");
						performSpecialLink(err.getURL());
					}
				}
			});
		}
	}

	/**
	 * Performs a link by
	 * {@link GpCoreUtil#performSpecialHTMLLink(java.awt.Component, AtlasConfig, String, String)}
	 * 
	 * @param destURL
	 */
	protected void performSpecialLink(String destURL) {
		boolean specialLinkPerformed = AtlasProtocol.performLink(this,
				atlasConfig, getContext().getCurrentURL(), destURL);
		if (!specialLinkPerformed)
			throw new AtlasProtocolException(destURL,
					"Atlas protocol could not be performed: " + destURL);
	}

}