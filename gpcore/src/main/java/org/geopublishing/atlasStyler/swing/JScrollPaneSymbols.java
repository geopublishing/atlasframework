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
package org.geopublishing.atlasStyler.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.WeakHashMap;

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
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AbstractRuleList;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.SingleRuleList;


/**
 * A GUI class that unites the different types of SymbolLists we have: symbols
 * from the homedirectory, symbols from the web, symbols froma JAR.
 * 
 * @author stefan
 * 
 */
public abstract class JScrollPaneSymbols extends JScrollPane {

	/**
	 * The size of one cell in the table
	 */
	public static final Dimension size = new Dimension(150,
			AtlasStyler.DEFAULT_SYMBOL_PREVIEW_SIZE.height + 9);

	public static final Dimension SYMBOL_SIZE = AtlasStyler.DEFAULT_SYMBOL_PREVIEW_SIZE;

	public static final String PROPERTY_SYMBOL_SELECTED = "SYMBOL_SELECTED";

	private Logger LOGGER = ASUtil.createLogger(this);

	final static protected Map<String, BufferedImage> weakImageCache = new WeakHashMap<String, BufferedImage>();

	private volatile JList jListSymbols;

	protected JPopupMenu popupMenu;

	protected MouseEvent mouseCLickEvent;

	public JScrollPaneSymbols() {
		super();

		initialize();
	}

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

				public Component getListCellRendererComponent(JList list,
						Object value, int index, boolean isSelected,
						boolean cellHasFocus) {

					SingleRuleList rl = (SingleRuleList) value;

					JPanel fullCell = new JPanel(new BorderLayout());
					fullCell.setSize(size);

					fullCell.setBorder(BorderFactory.createEmptyBorder(3, 5, 3,
							5));
					fullCell.setBackground(Color.white);

					String key = JScrollPaneSymbols.this.getClass()
							.getSimpleName()
							+ rl.getStyleName()
							+ rl.getStyleTitle()
							+ rl.getStyleAbstract();
					BufferedImage symbolImage = weakImageCache.get(key);
					// LOGGER.info("Looking for "+key);
					if (symbolImage == null) {
						// LOGGER.info("A symbol for " + key
						// + " was not found in cache. Rendering on EDT");
						symbolImage = rl.getImage(SYMBOL_SIZE);
						weakImageCache.put(key, symbolImage);
					}
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

					fullCell.add(infos, BorderLayout.CENTER);

					return fullCell;
				}

			});

			// The JList has to react on click
			jListSymbols.addMouseListener(new MouseListener() {

				public void mouseClicked(MouseEvent e) {
					int i = jListSymbols.locationToIndex(e.getPoint());

					if ((e.getClickCount() == 2)
							&& (e.getButton() == MouseEvent.BUTTON1)) {
						final AbstractRuleList rl = (AbstractRuleList) jListSymbols
								.getModel().getElementAt(i);

						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								JScrollPaneSymbols.this
										.firePropertyChange(
												JScrollPaneSymbols.PROPERTY_SYMBOL_SELECTED,
												null, rl);
							}
						});
					}

				}

				public void mousePressed(MouseEvent evt) {
					if (evt.isPopupTrigger()) {
						mouseCLickEvent = evt;
						if (getPopupMenu() != null)
							getPopupMenu().show(evt.getComponent(), evt.getX(),
									evt.getY());
					}
				}

				public void mouseReleased(MouseEvent evt) {
					if (evt.isPopupTrigger()) {
						mouseCLickEvent = evt;
						if (getPopupMenu() != null)
							getPopupMenu().show(evt.getComponent(), evt.getX(),
									evt.getY());
					}
				}

				public void mouseEntered(MouseEvent e) {
				}

				public void mouseExited(MouseEvent e) {
				}

			});

			// contentPane.setLayout(new FlowLayout(FlowLayout.LEFT));
			// jListSymbols.setPreferredSize(size);
		}
		return jListSymbols;
	}

	protected abstract JPopupMenu getPopupMenu();

	/**
	 * @return An {@link Icon} representing this {@link JScrollPaneSymbols}
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	protected abstract Icon getIcon();

	/**
	 * @return a tooltipto display in the {@link JTabbedPane}
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	protected abstract String getToolTip();

	/**
	 * @return A description for this collection of symbols
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	protected abstract String getDesc();

}
