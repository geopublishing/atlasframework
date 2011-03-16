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
package org.geopublishing.atlasStyler.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListCellRenderer;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.RuleListFactory;
import org.geopublishing.atlasStyler.rulesLists.RulesListInterface;
import org.geopublishing.atlasStyler.rulesLists.SingleRuleList;

import de.schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import de.schmitzm.i18n.Translation;
import de.schmitzm.swing.swingworker.AtlasSwingWorker;

/**
 * A GUI class that unites the different types of SymbolLists we have: symbols
 * from the homedirectory, symbols from the web, symbols froma JAR.
 * 
 * @author stefan
 * 
 */
public abstract class JScrollPaneSymbols extends JScrollPane {

	final GeometryForm geometryForm;
	/**
	 * The size of one cell in the table
	 */
	public static final Dimension size = new Dimension(150,
			AtlasStylerVector.DEFAULT_SYMBOL_PREVIEW_SIZE.height + 9);

	public static final Dimension SYMBOL_SIZE = AtlasStylerVector.DEFAULT_SYMBOL_PREVIEW_SIZE;

	public static final String PROPERTY_SYMBOL_SELECTED = "SYMBOL_SELECTED";

	private final Logger LOGGER = Logger.getLogger(JScrollPaneSymbols.class);

	private volatile JList jListSymbols;

	protected JPopupMenu popupMenu;

	protected MouseEvent mouseCLickEvent;

	public JScrollPaneSymbols(GeometryForm geoForm) {
		geometryForm = geoForm;
		initialize();
	}

	/**
	 * static caches for images
	 */
	// final static protected HashMap<String, BufferedImage> imageCache = new
	// HashMap<String, BufferedImage>();

	/**
	 * static caches for images
	 */
	final static protected HashMap<String, JPanel> symbolPreviewComponentsCache = new HashMap<String, JPanel>();

	private void initialize() {
		setViewportView(getJListSymbols());
	}

	protected JList getJListSymbols() {
		if (jListSymbols == null) {
			jListSymbols = new JList();

			jListSymbols.setModel(new DefaultListModel());

			jListSymbols
					.setToolTipText("Double click with left mouse-button to use this symbol. Right mouse button opens a menu.");// i8n

			jListSymbols.setCellRenderer(new ListCellRenderer() {

				@Override
				public Component getListCellRendererComponent(JList list,
						Object value, int index, boolean isSelected,
						boolean cellHasFocus) {

					SingleRuleList rl = (SingleRuleList) value;
					String key = JScrollPaneSymbols.this.getClass()
							.getSimpleName()
							+ rl.getStyleName()
							+ rl.getStyleTitle() + rl.getStyleAbstract();

					JPanel fullCell = getOrCreateComponent(rl, key);
					if (isSelected) {
						fullCell.setBorder(BorderFactory.createEtchedBorder(
								Color.YELLOW, Color.BLACK));

						// fullCell.setBackground(Color.yellow);
					} else {
						fullCell.setBorder(BorderFactory.createEtchedBorder(
								Color.WHITE, Color.GRAY));
						// fullCell.setBackground(Color.white);
					}

					return fullCell;
				}

			});

			// The JList has to react on click
			jListSymbols.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					int i = jListSymbols.locationToIndex(e.getPoint());
					if (i < 0) {
						// Clicked where not symbol is
						return;
					}

					if ((e.getClickCount() == 2)
							&& (e.getButton() == MouseEvent.BUTTON1)) {
						final RulesListInterface rl = (RulesListInterface) jListSymbols
								.getModel().getElementAt(i);

						new AtlasSwingWorker<Void>(JScrollPaneSymbols.this) {

							@Override
							protected Void doInBackground() throws Exception {
								JScrollPaneSymbols.this
										.firePropertyChange(
												JScrollPaneSymbols.PROPERTY_SYMBOL_SELECTED,
												null, rl);
								return null;
							}

						}.executeModalNoEx();

					}

				}

				@Override
				public void mousePressed(MouseEvent evt) {
					if (evt.isPopupTrigger()) {
						mouseCLickEvent = evt;
						if (getPopupMenu() != null)
							getPopupMenu().show(evt.getComponent(), evt.getX(),
									evt.getY());
					}
				}

			});

			// The JList has to react to movement
			jListSymbols.addMouseMotionListener(new MouseMotionAdapter() {

				@Override
				public void mouseMoved(MouseEvent me) {
					Point p = new Point(me.getPoint());
					jListSymbols.setSelectedIndex(jListSymbols
							.locationToIndex(p));
					jListSymbols.repaint();
				}
			});

		}
		return jListSymbols;
	}

	protected abstract JPopupMenu getPopupMenu();

	/**
	 * @return An {@link Icon} representing this {@link JScrollPaneSymbols}
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	protected abstract Icon getIcon();

	/**
	 * @return a tooltipto display in the {@link JTabbedPane}
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	protected abstract String getToolTip();

	/**
	 * @return A description for this collection of symbols
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	protected abstract String getDesc();

	/**
	 * Do some tricks with the JScrollPane to show the results automatically.
	 */
	void updateJScrollPane() {
		// setViewportView(getJListSymbols());
		// doLayout();
		// repaint();
	}

	/**
	 * Renders the preview of the symbol and from the url and put the result in
	 * a cache.
	 * 
	 * @param rl
	 *            SingleRuleList to render
	 * @param key
	 */
	JPanel getOrCreateComponent(SingleRuleList rl, String key) {

		JPanel fullCell = symbolPreviewComponentsCache.get(key);
		if (fullCell == null) {

			fullCell = new JPanel(new BorderLayout());
			fullCell.setSize(size);

			fullCell.setBackground(Color.white);

			BufferedImage symbolImage;
			// BufferedImage symbolImage = imageCache.get(key);
			// // LOGGER.info("Looking for "+key);
			// if (symbolImage == null) {
			// LOGGER.info("A symbol for " + key
			// + " was not found in cache.");
			symbolImage = rl.getImage(SYMBOL_SIZE);
			// imageCache.put(key, symbolImage);
			// }
			ImageIcon image = new ImageIcon(symbolImage);

			fullCell.add(new JLabel(image), BorderLayout.WEST);

			JPanel infos = new JPanel(new BorderLayout());
			JPanel nameAuthor = new JPanel(new BorderLayout());

			JLabel styleName = new JLabel(rl.getStyleName());
			nameAuthor.add(styleName, BorderLayout.WEST);

			JLabel styleAuthor = new JLabel(rl.getStyleTitle());
			styleAuthor.setFont(styleAuthor.getFont().deriveFont(9f)
					.deriveFont(Font.ITALIC));
			nameAuthor.add(styleAuthor, BorderLayout.EAST);

			infos.add(nameAuthor, BorderLayout.NORTH);

			JLabel description = new JLabel(rl.getStyleAbstract());
			infos.add(description, BorderLayout.CENTER);
			description.setFont(description.getFont().deriveFont(8f));
			symbolPreviewComponentsCache.put(key, fullCell);
			fullCell.add(infos, BorderLayout.CENTER);
		}
		return fullCell;

	}

	/**
	 * The String KEY is {@link #getToolTip()}.toString + geomForm.toString
	 */
	final static HashMap<String, List<SingleRuleList<?>>> cachedRuleLists = new HashMap<String, List<SingleRuleList<?>>>();

	/**
	 * Rescans the online or local folder for symbols in background.
	 * 
	 * @param reset
	 *            Shall all cahces {@link JList} be cleared before rescan.
	 *            Otherwise caches will be used to speed up the list.
	 */
	public void rescan(boolean reset) {
		final String key = getRuleListCacheKey();

		if (reset) {
			getJListSymbols().setModel(new DefaultListModel());
			// imageCache.clear();
			symbolPreviewComponentsCache.clear();
			cachedRuleLists.remove(key);
		}

		if (cachedRuleLists.get(key) == null) {
			cachedRuleLists.put(key, getWorker().executeModalNoEx());
		}

		// Add new or cached RuleLists to the GUI model
		addNewRuleListsToModel();
	}

	/**
	 * Key used for {@link #cachedRuleLists}
	 */
	private String getRuleListCacheKey() {
		return getToolTip() + geometryForm.toString();
	}

	/**
	 * Adds the List of RulesLists to the {@link JScrollPane} model.
	 */
	protected void addNewRuleListsToModel() {

		final DefaultListModel model = (DefaultListModel) getJListSymbols()
				.getModel();

		model.clear();

		// Check for existance?! Not needed since we use clear
		// final Enumeration<?> name2 = model.elements();
		// while (name2.hasMoreElements()) {
		// final String styleName = ((SingleRuleList) name2.nextElement())
		// .getStyleName();
		// if (styleName.equals(newNameWithOUtSLD)) {
		// // A Symbol with the same StyleName already
		// // exits
		// continue;
		// }
		// }

		for (SingleRuleList rl : cachedRuleLists.get(getRuleListCacheKey())) {
			model.addElement(rl);
		}

	}

	static String nameWithoutSld(URL url) {

		/*******************************************************
		 * Checking if a Style with the same name allready exists
		 */
		// Name without .sld
		final String newNameWithOUtSLD = url.getFile().substring(0,
				url.getFile().length() - 4);

		return newNameWithOUtSLD;
	}

	abstract protected AtlasSwingWorker<List<SingleRuleList<?>>> getWorker();

	protected void cacheUrl(List<SingleRuleList<?>> entriesForTheList, URL url) {
		final SingleRuleList symbolRuleList = RuleListFactory
				.createSingleRulesList(new Translation(), geometryForm, false);

		boolean b = symbolRuleList.loadURL(url);
		if (b) {

			String key = this.getClass().getSimpleName()
					+ symbolRuleList.getStyleName()
					+ symbolRuleList.getStyleTitle()
					+ symbolRuleList.getStyleAbstract();

			// here we render the SLD to an image. This can create additional
			// URL request (slow!) when external graphics are being used.
			getOrCreateComponent(symbolRuleList, key);

			// Ad the ruleList to the lists of RLs to add the the model
			entriesForTheList.add(symbolRuleList);

		} else {
			// Load failed
			LOGGER.warn("Loading " + url + " failed");
		}
	}
}
