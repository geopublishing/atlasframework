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
/*
 * $Id$
 *
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package skrueger.atlas.gui.plaf;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicGraphicsUtils;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.icon.EmptyIcon;
import org.jdesktop.swingx.plaf.TaskPaneUI;

import schmitzm.swing.SwingUtil;
import skrueger.atlas.AtlasViewer;
import skrueger.atlas.gui.AtlasMapLayerLegend;
import skrueger.atlas.gui.MapLayerLegend;

/**
 * Base implementation of the <code>JXTaskPane</code> UI.
 * 
 * @author Mostly <a href="mailto:fred@swingx.com">Frederic Lavigne</a>
 */
public class BasicMapLayerLegendPaneUI extends TaskPaneUI {

	private static final Color LAYER_HIDDEN_COLOR = Color.decode("#efefef");

	static final Logger LOGGER = Logger
			.getLogger(BasicMapLayerLegendPaneUI.class);

	public static final ImageIcon ICON_EXPORT = SwingUtil
			.createImageIconFromResourcePath(BasicMapLayerLegendPaneUI.class,
					"export.png", null);

	public static final ImageIcon ICON_VISIBLE = SwingUtil
			.createImageIconFromResourcePath(BasicMapLayerLegendPaneUI.class,
					"visible.png", null);

	public static final ImageIcon ICON_HALFVISIBLE = SwingUtil
			.createImageIconFromResourcePath(BasicMapLayerLegendPaneUI.class,
					"visible_half.png", null);

	public static final ImageIcon ICON_NOTVISIBLE = SwingUtil
			.createImageIconFromResourcePath(BasicMapLayerLegendPaneUI.class,
					"not_visible.png", null);

	public static final ImageIcon ICON_REMOVE = SwingUtil
			.createImageIconFromResourcePath(BasicMapLayerLegendPaneUI.class,
					"remove.png", "");

	private static final ImageIcon ICON_INFO = SwingUtil
			.createImageIconFromResourcePath(BasicMapLayerLegendPaneUI.class,
					"info.png", "");

	public static final ImageIcon ICON_TOOL = SwingUtil
			.createImageIconFromResourcePath(BasicMapLayerLegendPaneUI.class,
					"tool.png", "");

	public static final ImageIcon ICON_UPARROW = SwingUtil
			.createImageIconFromResourcePath(BasicMapLayerLegendPaneUI.class,
					"up_arrow.png", "");

	public static final ImageIcon ICON_DOWNARROW = SwingUtil
			.createImageIconFromResourcePath(BasicMapLayerLegendPaneUI.class,
					"down_arrow.png", "");

	public static final ImageIcon ICON_RASTER = SwingUtil
			.createImageIconFromResourcePath(BasicMapLayerLegendPaneUI.class,
					"raster.png", "");

	public static final ImageIcon ICON_VECTOR = SwingUtil
			.createImageIconFromResourcePath(BasicMapLayerLegendPaneUI.class,
					"vector.png", "");

	public static final ImageIcon ICON_FILTER = SwingUtil
			.createImageIconFromResourcePath(BasicMapLayerLegendPaneUI.class,
					"filter.png", "");

	public static final ImageIcon ICON_REMOVE_FILTER = SwingUtil
			.createImageIconFromResourcePath(BasicMapLayerLegendPaneUI.class,
					"filter_remove.png", "");

	public static final ImageIcon ICON_STYLE = SwingUtil
			.createImageIconFromResourcePath(BasicMapLayerLegendPaneUI.class,
					"style.png", "");

	public static final ImageIcon ICON_TABLE = SwingUtil
			.createImageIconFromResourcePath(BasicMapLayerLegendPaneUI.class,
					"table.png", "");

	// TODO REDUCE STUPID CODE!
	public int controlEyeWidth;

	public int controlEyeX;

	public int controlEyeY;

	public int controlInfoWidth;

	public int controlInfoX;

	public int controlInfoY;

	public int controlToolWidth;

	public int controlToolX;

	public int controlToolY;

	public int controlExpandWidth;

	public int controlExpandX;

	public int controlExpandY;

	private static FocusListener focusListener = new RepaintOnFocus();

	public static ComponentUI createUI(JComponent c) {
		return new BasicMapLayerLegendPaneUI();
	}

	protected int titleHeight = 25;

	protected int roundHeight = 5;

	protected MapLayerLegend mapLayerLegend;

	protected boolean mouseOver;

	protected MouseInputListener mouseListener;

	protected PropertyChangeListener propertyListener;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void installUI(JComponent c) {
		super.installUI(c);
		mapLayerLegend = (MapLayerLegend) c;

		installDefaults();
		installListeners();
		installKeyboardActions();
	}

	/**
	 * Installs default properties. Following properties are installed:
	 * <ul>
	 * <li>TaskPane.background</li>
	 * <li>TaskPane.foreground</li>
	 * <li>TaskPane.font</li>
	 * <li>TaskPane.borderColor</li>
	 * <li>TaskPane.titleForeground</li>
	 * <li>TaskPane.titleBackgroundGradientStart</li>
	 * <li>TaskPane.titleBackgroundGradientEnd</li>
	 * <li>TaskPane.titleOver</li>
	 * <li>TaskPane.specialTitleOver</li>
	 * <li>TaskPane.specialTitleForeground</li>
	 * <li>TaskPane.specialTitleBackground</li>
	 * </ul>
	 */
	protected void installDefaults() {
		mapLayerLegend.setOpaque(true);
		mapLayerLegend.setBorder(createPaneBorder());

		mapLayerLegend.setBorder(createPaneBorder());

		((JComponent) mapLayerLegend.getContentPane())
				.setBorder(createContentPaneBorder());

		LookAndFeel.installColorsAndFont(mapLayerLegend, "TaskPane.background",
				"TaskPane.foreground", "TaskPane.font");

		LookAndFeel.installColorsAndFont((JComponent) mapLayerLegend
				.getContentPane(), "TaskPane.background",
				"TaskPane.foreground", "TaskPane.font");
	}

	/**
	 * Installs listeners for UI delegate.
	 */
	protected void installListeners() {
		mouseListener = createMouseInputListener();
		mapLayerLegend.addMouseMotionListener(mouseListener);
		mapLayerLegend.addMouseListener(mouseListener);

		mapLayerLegend.addFocusListener(focusListener);
		propertyListener = createPropertyListener();
		mapLayerLegend.addPropertyChangeListener(propertyListener);
	}

	/**
	 * Installs keyboard actions to allow task pane to react on hot keys.
	 */
	protected void installKeyboardActions() {
		InputMap inputMap = (InputMap) UIManager.get("TaskPane.focusInputMap");
		if (inputMap != null) {
			SwingUtilities.replaceUIInputMap(mapLayerLegend,
					JComponent.WHEN_FOCUSED, inputMap);
		}

		ActionMap map = getActionMap();
		if (map != null) {
			SwingUtilities.replaceUIActionMap(mapLayerLegend, map);
		}
	}

	ActionMap getActionMap() {
		ActionMap map = new ActionMapUIResource();
		map.put("toggleExpanded", new ToggleExpandedAction());
		return map;
	}

	@Override
	public void uninstallUI(JComponent c) {
		uninstallListeners();
		super.uninstallUI(c);
	}

	/**
	 * Uninstalls previously installed listeners to free component for garbage
	 * collection.
	 */
	protected void uninstallListeners() {
		mapLayerLegend.removeMouseListener(mouseListener);
		mapLayerLegend.removeMouseMotionListener(mouseListener);
		mapLayerLegend.removeFocusListener(focusListener);
		mapLayerLegend.removePropertyChangeListener(propertyListener);
	}

	/**
	 * Creates new toggle listener.
	 * 
	 * @return MouseInputListener reacting on toggle events of task pane.
	 */
	protected MouseInputListener createMouseInputListener() {
		return new ToggleListener();
	}

	/**
	 * Creates property change listener for task pane.
	 * 
	 * @return Property change listener reacting on changes to the task pane.
	 */
	protected PropertyChangeListener createPropertyListener() {
		return new ChangeListener();
	}

	/**
	 * Evaluates whenever given mouse even have occurred within borders of task
	 * pane.
	 * 
	 * @param event
	 *            Evaluated event.
	 * @return True if event occurred within task pane area, false otherwise.
	 */
	protected boolean isInBorder(MouseEvent event) {
		return event.getY() < getTitleHeight(event.getComponent());
	}

	/**
	 * Gets current title height. Default value is 25.
	 * 
	 * @return Current title height.
	 * @deprecated. Use getTitleHeight(java.awt.Component) instead. Kept only
	 *              for compatibility reasons. Will be removed before 1.0
	 *              release.
	 */
	protected int getTitleHeight() {
		return titleHeight;
	}

	/**
	 * Gets current title height. Default value is 25 if not specified
	 * otherwise. Method checks provided component for user set font
	 * (!instanceof FontUIResource), if font is set, height will be calculated
	 * from font metrics instead of using internal preset height.
	 * 
	 * @return Current title height.
	 */
	protected int getTitleHeight(Component c) {
		if (c instanceof JXTaskPane) {
			JXTaskPane taskPane = (JXTaskPane) c;
			Font font = taskPane.getFont();
			int height = titleHeight;

			if (font != null && !(font instanceof FontUIResource)) {
				height = taskPane.getFontMetrics(font).getHeight();
			}

			Icon icon = taskPane.getIcon();

			if (icon != null) {
				height = Math.max(height, icon.getIconHeight() + 4);
			}

			return height;
		}

		return titleHeight;
	}

	/**
	 * Creates new border for task pane.
	 * 
	 * @return Fresh border on every call.
	 */
	protected Border createPaneBorder() {
		return new PaneBorder();
	}

	@Override
	public Dimension getPreferredSize(JComponent c) {
		Component component = mapLayerLegend.getComponent(0);
		if (!(component instanceof JXCollapsiblePane)) {
			// something wrong in this JXTaskPane
			return super.getPreferredSize(c);
		}

		JXCollapsiblePane collapsible = (JXCollapsiblePane) component;
		Dimension dim = collapsible.getPreferredSize();

		Border groupBorder = mapLayerLegend.getBorder();
		if (groupBorder instanceof PaneBorder) {
			Dimension border = ((PaneBorder) groupBorder)
					.getPreferredSize(mapLayerLegend);
			dim.width = Math.max(dim.width, border.width);
			dim.height += border.height;
		} else {
			dim.height += getTitleHeight(c);
		}

		return dim;
	}

	/**
	 * Creates content pane border.
	 * 
	 * @return Fresh content pane border initialized with current value of
	 *         TaskPane.borderColor on every call.
	 */
	protected Border createContentPaneBorder() {
		Color borderColor = UIManager.getColor("TaskPane.borderColor");
		return new CompoundBorder(new ContentPaneBorder(borderColor),
				BorderFactory.createEmptyBorder(2, 1, 1, 6));
		// return new CompoundBorder(new ContentPaneBorder(borderColor),
		// BorderFactory.createEmptyBorder(5, 7, 5, 7));
	}

	@Override
	public Component createAction(Action action) {
		JXHyperlink link = new JXHyperlink(action) {
			@Override
			public void updateUI() {
				super.updateUI();
				// ensure the ui of this link is correctly update on l&f changes
				configure(this);
			}
		};
		configure(link);
		return link;
	}

	/**
	 * Configures internally used hyperlink on new action creation and on every
	 * call to <code>updateUI()</code>.
	 * 
	 * @param link
	 *            Configured hyperlink.
	 */
	protected void configure(JXHyperlink link) {
		link.setOpaque(false);
		link.setBorder(null);
		link.setBorderPainted(false);
		link.setFocusPainted(true);
		link.setForeground(UIManager.getColor("TaskPane.titleForeground"));
	}

	/**
	 * Ensures expanded group is visible. Issues delayed request for scrolling
	 * to visible.
	 */
	protected void ensureVisible() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mapLayerLegend.scrollRectToVisible(new Rectangle(mapLayerLegend
						.getWidth(), mapLayerLegend.getHeight()));
			}
		});
	}

	/**
	 * Focus listener responsible for repainting of the taskpane on focus
	 * change.
	 */
	static class RepaintOnFocus implements FocusListener {
		public void focusGained(FocusEvent e) {
			e.getComponent().repaint();
		}

		public void focusLost(FocusEvent e) {
			e.getComponent().repaint();
		}
	}

	/**
	 * Change listener responsible for change handling.
	 */
	class ChangeListener implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent evt) {
			// if group is expanded but not animated
			// or if animated has reached expanded state
			// scroll to visible if scrollOnExpand is enabled
			// if
			// ((JXTaskPane.EXPANDED_CHANGED_KEY.equals(evt.getPropertyName())
			// && Boolean.TRUE.equals(evt.getNewValue()) && !group
			// .isAnimated())
			// || (JXCollapsiblePane.ANIMATION_STATE_KEY.equals(evt
			// .getPropertyName()) && "expanded".equals(evt
			// .getNewValue()))) {
			// if (group.isScrollOnExpand()) {
			// ensureVisible();
			// }
			// } else if (JXTaskPane.ICON_CHANGED_KEY
			// .equals(evt.getPropertyName())
			// || JXTaskPane.TITLE_CHANGED_KEY.equals(evt
			// .getPropertyName())
			// || JXTaskPane.SPECIAL_CHANGED_KEY.equals(evt
			// .getPropertyName())) {
			// // icon, title, special must lead to a repaint()
			// group.repaint();
			// }
		}
	}

	/**
	 * Mouse listener responsible for handling of toggle events.
	 */
	class ToggleListener extends MouseInputAdapter {
		@Override
		public void mouseEntered(MouseEvent e) {
			if (isInBorder(e)) {
				e.getComponent().setCursor(
						Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			} else {
				mouseOver = false;
				mapLayerLegend.repaint(0, 0, mapLayerLegend.getWidth(),
						getTitleHeight(mapLayerLegend));
			}
		}

		@Override
		public void mouseExited(MouseEvent e) {
			e.getComponent().setCursor(null);
			mouseOver = false;
			mapLayerLegend.repaint(0, 0, mapLayerLegend.getWidth(),
					getTitleHeight(mapLayerLegend));
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			if (isInBorder(e)) {
				e.getComponent().setCursor(
						Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

				// **************************************************************
				// Did we move over the eye?
				// **************************************************************
				if (insideClick(e, controlEyeX, controlEyeY, controlEyeWidth,
						controlEyeWidth)) {

					mapLayerLegend.setToolTipText(AtlasViewer
							.R("LayerBar.Icons.eye.tooltip"));

					mapLayerLegend.repaint(controlEyeX, controlEyeY,
							controlEyeWidth, controlEyeWidth);
					return;
				} else if (insideClick(e, controlToolX, controlToolY,
						controlToolWidth, controlToolWidth)) {

					mapLayerLegend.setToolTipText(AtlasViewer
							.R("LayerBar.Icons.tool.tooltip"));

					mapLayerLegend.repaint(controlToolX, controlToolY,
							controlToolWidth, controlToolWidth);
					return;
				} else if (((mapLayerLegend).getInfoURL() != null)
						&& insideClick(e, controlInfoX, controlInfoY,
								controlInfoWidth, controlInfoWidth)) {

					mapLayerLegend.setToolTipText(AtlasViewer
							.R("LayerBar.Icons.info.tooltip"));

					mapLayerLegend.repaint(controlInfoX, controlInfoY,
							controlInfoWidth, controlInfoWidth);
					return;
				} else {
					mapLayerLegend.setToolTipText("<html>"
							+ mapLayerLegend.getLegendTooltip() + "</html>");
				}

				mouseOver = true;
			} else {
				e.getComponent().setCursor(null);
				mouseOver = false;
			}

			mapLayerLegend.repaint(0, 0, mapLayerLegend.getWidth(),
					getTitleHeight(mapLayerLegend));
		}

		/**
		 * One click will toggle exand state Doubleclick undoes the previous
		 * signaled expand toggle and zooms to layer boundaries
		 */
		@Override
		public void mouseClicked(MouseEvent evt) {
			processMouseClickedEvent(evt);
		}

	}

	/**
	 * Check if the {@link MouseEvent} happened inside a region
	 * 
	 * @param evt
	 *            {@link MouseEvent}
	 * @return inside?
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	private boolean insideClick(MouseEvent evt, int x, int y, int w, int h) {

		int xx = evt.getPoint().x;
		int yy = evt.getPoint().y;

		if ((xx <= x + w) && (xx >= x) && (yy >= y) && (yy <= y + h)) {
			return true;
		}

		return false;
	}

	public void processMouseClickedEvent(MouseEvent evt) {

		// **********************************************************************
		// The right button shall open the tools menu
		// **********************************************************************
		if (evt.getButton() == MouseEvent.BUTTON3) {
			mapLayerLegend.getToolMenu().show(mapLayerLegend, controlToolX,
					controlToolY + controlToolWidth);
			return;
		}

		if (evt.getClickCount() == 1) { // Single-click always

			// ******************************************************************
			// Did we hit him on the eye?
			// ******************************************************************
			if (insideClick(evt, controlEyeX, controlEyeY, controlEyeWidth,
					controlEyeWidth)) {

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						mapLayerLegend.setCollapsed(!mapLayerLegend
								.toggleVisibility());
					}
				});

				mapLayerLegend.repaint(controlEyeX, controlEyeY,
						controlEyeWidth, controlEyeWidth);
				return;
			}

			// ******************************************************************
			// Did we hit him on the INFO icon?
			// ******************************************************************
			if (mapLayerLegend.getInfoURL() != null) {

				if (insideClick(evt, controlInfoX, controlInfoY,
						controlInfoWidth, controlInfoWidth)) {

					mapLayerLegend.clickedInfoButton();

					mapLayerLegend.repaint(controlInfoX, controlInfoY,
							controlInfoWidth, controlInfoWidth);
					return;
				}

			}

			// ******************************************************************
			// Did we hit the Tool icon ?
			// ******************************************************************
			if (insideClick(evt, controlToolX, controlToolY, controlToolWidth,
					controlToolWidth)) {
				mapLayerLegend.getToolMenu().show(mapLayerLegend, controlToolX,
						controlToolY + controlToolWidth);
				return;
			}

			// ******************************************************************
			// Did we hit the Expand icon ?
			// ******************************************************************
			if (insideClick(evt, controlExpandX, controlExpandY,
					controlExpandWidth, controlExpandWidth)) {
				boolean bbb = mapLayerLegend.isCollapsed();
				mapLayerLegend.setCollapsed(!bbb);

				if (mapLayerLegend instanceof AtlasMapLayerLegend) {
					/**
					 * We have to catch changes to the "Expand-State", because
					 * in the GP we save this state and restore it in the AV.
					 */
					AtlasMapLayerLegend alpg = (AtlasMapLayerLegend) mapLayerLegend;

					alpg.setMinimized(!bbb);
					alpg.getAtlasMapLegend().recreateLayerList(
							alpg.getMapLayer());
				}
				return;
			}
		} else if (evt.getClickCount() == 2) { // Double-click

			// ******************************************************************
			// No action when double-click on the buttons, otherwise
			// zoom to layer
			// ******************************************************************
			if (insideClick(evt, controlExpandX, controlExpandY,
					controlExpandWidth, controlExpandWidth)) {
				return;
			}
			if (insideClick(evt, controlEyeX, controlEyeY, controlEyeWidth,
					controlEyeWidth)) {
				return;
			}
			if (insideClick(evt, controlToolX, controlToolY, controlToolWidth,
					controlToolWidth)) {
				return;
			}
			if (insideClick(evt, controlInfoX, controlInfoY, controlInfoWidth,
					controlInfoWidth)) {
				return;
			}

			// Zoom to layer
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					(mapLayerLegend).zoomTo();
					if (!(mapLayerLegend).isLayerVisible()) {
						mapLayerLegend.setCollapsed(!(mapLayerLegend)
								.toggleVisibility());
					}
					if (mapLayerLegend.isCollapsed())
						mapLayerLegend.setCollapsed(false);
				}
			});
		}
	}

	/**
	 * Toggle expanded action.
	 */
	class ToggleExpandedAction extends AbstractAction {
		/**
		 * Serial version UID.
		 */
		private static final long serialVersionUID = 5676859881615358815L;

		public ToggleExpandedAction() {
			super("toggleExpanded");
		}

		public void actionPerformed(ActionEvent e) {
			mapLayerLegend.setCollapsed(!mapLayerLegend.isCollapsed());
		}

		@Override
		public boolean isEnabled() {
			return mapLayerLegend.isVisible();
		}
	}

	/**
	 * Toggle icon.
	 */
	protected static class ChevronIcon implements Icon {
		boolean up = true;

		public ChevronIcon(boolean up) {
			this.up = up;
		}

		public int getIconHeight() {
			return 3;
		}

		public int getIconWidth() {
			return 6;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			if (up) {
				g.drawLine(x + 3, y, x, y + 3);
				g.drawLine(x + 3, y, x + 6, y + 3);
			} else {
				g.drawLine(x, y, x + 3, y + 3);
				g.drawLine(x + 3, y + 3, x + 6, y);
			}
		}
	}

	/**
	 * The border around the content pane
	 */
	protected static class ContentPaneBorder implements Border, UIResource {
		Color color;

		public ContentPaneBorder(Color color) {
			this.color = color;
		}

		public Insets getBorderInsets(Component c) {
			return new Insets(0, 1, 1, 1);
		}

		public boolean isBorderOpaque() {
			return true;
		}

		public void paintBorder(Component c, Graphics g, int x, int y,
				int width, int height) {
			g.setColor(color);
			g.drawLine(x, y, x, y + height - 1);
			g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);
			g.drawLine(x + width - 1, y, x + width - 1, y + height - 1);
		}
	}

	/**
	 * The border of the taskpane group paints the "text", the "icon", the
	 * "expanded" status and the "special" type.
	 * 
	 */
	protected class PaneBorder implements Border, UIResource {

		protected Color borderColor;

		protected Color titleForeground;

		protected Color specialTitleBackground;

		protected Color specialTitleForeground;

		protected Color titleBackgroundGradientStart;

		protected Color titleBackgroundGradientEnd;

		protected Color titleOver;

		protected Color specialTitleOver;

		protected JLabel label;

		/**
		 * Creates new instance of individual pane border.
		 */
		public PaneBorder() {
			borderColor = UIManager.getColor("TaskPane.borderColor");

			titleForeground = UIManager.getColor("TaskPane.titleForeground");

			specialTitleBackground = UIManager
					.getColor("TaskPane.specialTitleBackground");
			specialTitleForeground = UIManager
					.getColor("TaskPane.specialTitleForeground");

			titleBackgroundGradientStart = UIManager
					.getColor("TaskPane.titleBackgroundGradientStart");
			titleBackgroundGradientEnd = UIManager
					.getColor("TaskPane.titleBackgroundGradientEnd");

			titleOver = UIManager.getColor("TaskPane.titleOver");
			if (titleOver == null) {
				titleOver = specialTitleBackground.brighter();
			}
			specialTitleOver = UIManager.getColor("TaskPane.specialTitleOver");
			if (specialTitleOver == null) {
				specialTitleOver = specialTitleBackground.brighter();
			}

			label = new JLabel();
			label.setOpaque(false);
			label.setIconTextGap(8);
		}

		public Insets getBorderInsets(Component c) {
			return new Insets(getTitleHeight(c), 0, 0, 0);
		}

		/**
		 * Overwritten to always return <code>true</code> to speed up painting.
		 * Don't use transparent borders unless providing UI delegate that
		 * provides proper return value when calling this method.
		 * 
		 * @see javax.swing.border.Border#isBorderOpaque()
		 */
		public boolean isBorderOpaque() {
			return true;
		}

		/**
		 * Calculates the preferred border size, its size so all its content
		 * fits.
		 * 
		 * @param group
		 *            Selected group.
		 */
		public Dimension getPreferredSize(JXTaskPane group) {
			// calculate the title width so it is fully visible
			// it starts with the title width
			configureLabel(group);
			Dimension dim = label.getPreferredSize();
			// add the title left offset
			dim.width += 3;
			// add the controls width
			dim.width += getTitleHeight(group);
			// and some space between label and controls
			dim.width += 3;

			dim.height = getTitleHeight(group);
			return dim;
		}

		/**
		 * Paints background of the title. This may differ based on properties
		 * of the group.
		 * 
		 * @param layerLegend
		 *            Selected layer.
		 * @param g
		 *            Target graphics.
		 */
		protected void paintTitleBackground(MapLayerLegend layerLegend,
				Graphics g) {
			if (layerLegend.isSpecial()) {
				g.setColor(specialTitleBackground);
			} else {
				g.setColor(titleBackgroundGradientStart);
			}

			if (layerLegend instanceof AtlasMapLayerLegend) {
				AtlasMapLayerLegend atlasLayerLegend = (AtlasMapLayerLegend) layerLegend;
				if (atlasLayerLegend.isHiddenInLegend()) {
					g.setColor(LAYER_HIDDEN_COLOR);
				}
			}

			g.fillRect(0, 0, layerLegend.getWidth(),
					getTitleHeight(layerLegend) - 1);
		}

		/**
		 * Paints current group title.
		 * 
		 * @param group
		 *            Selected group.
		 * @param g
		 *            Target graphics.
		 * @param textColor
		 *            Title color.
		 * @param x
		 *            X coordinate of the top left corner.
		 * @param y
		 *            Y coordinate of the top left corner.
		 * @param width
		 *            Width of the box.
		 * @param height
		 *            Height of the box.
		 */
		protected void paintTitle(JXTaskPane group, Graphics g,
				Color textColor, int x, int y, int width, int height) {
			configureLabel(group);
			label.setForeground(textColor);
			if (group.getFont() != null
					&& !(group.getFont() instanceof FontUIResource)) {
				label.setFont(group.getFont());
			}

			/**
			 * Here we make the title shorter to leave room for the new buttons
			 */
			int buttonAnz = 3;
			if (((MapLayerLegend) group).getInfoURL() == null)
				buttonAnz--;
			label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, buttonAnz
					* controlToolWidth + 7));

			g.translate(x, y);
			label.setBounds(0, 0, width, height);
			label.paint(g);
			g.translate(-x, -y);
		}

		/**
		 * Configures label for the group using its title, font, icon and
		 * orientation.
		 * 
		 * @param group
		 *            Selected group.
		 */
		protected void configureLabel(JXTaskPane group) {
			label.applyComponentOrientation(group.getComponentOrientation());
			label.setFont(group.getFont());
			label.setText(group.getTitle());
			label.setIcon(group.getIcon() == null ? new EmptyIcon() : group
					.getIcon());
		}

		/**
		 * Paints expanded controls. Default implementation does nothing.
		 * 
		 * @param group
		 *            Expanded group.
		 * @param g
		 *            Target graphics.
		 * @param x
		 *            X coordinate of the top left corner.
		 * @param y
		 *            Y coordinate of the top left corner.
		 * @param width
		 *            Width of the box.
		 * @param height
		 *            Height of the box.
		 */
		protected void paintExpandedControls(JXTaskPane group, Graphics g,
				int x, int y, int width, int height) {
		}

		/**
		 * Gets current paint color.
		 * 
		 * @param mapLayerLegend
		 *            Selected MapLayerLegend.
		 * @return Color to be used for painting provided group.
		 */
		protected Color getPaintColor(JXTaskPane mapLayerLegend) {
			Color paintColor;
			if (isMouseOverBorder()) {
				if (mouseOver) {
					if (mapLayerLegend.isSpecial()) {
						paintColor = Color.red;
					} else {
						paintColor = Color.red;
					}
				} else {
					if (mapLayerLegend.isSpecial()) {
						paintColor = specialTitleForeground;
					} else {
						paintColor = mapLayerLegend.getForeground() == null
								|| mapLayerLegend.getForeground() instanceof ColorUIResource ? titleForeground
								: mapLayerLegend.getForeground();
					}
				}
			} else {
				if (mapLayerLegend.isSpecial()) {
					paintColor = specialTitleForeground;
				} else {
					paintColor = mapLayerLegend.getForeground() == null
							|| mapLayerLegend.getForeground() instanceof ColorUIResource ? titleForeground
							: mapLayerLegend.getForeground();
				}
			}
			return paintColor;
		}

		/*
		 * @see javax.swing.border.Border#paintBorder(java.awt.Component,
		 * java.awt.Graphics, int, int, int, int)
		 */
		public void paintBorder(Component c, Graphics g, int x, int y,
				int width, int height) {

			MapLayerLegend layerLegend = (MapLayerLegend) c;

			// calculate position of title and toggle controls
			controlExpandWidth = getTitleHeight(layerLegend) - 2
					* getRoundHeight();
			controlExpandX = layerLegend.getWidth()
					- getTitleHeight(layerLegend);
			controlExpandY = getRoundHeight() - 1;
			controlEyeWidth = controlInfoWidth = controlToolWidth = controlExpandWidth;
			controlExpandY = controlEyeY = controlInfoY = controlToolY = getRoundHeight() - 1;
			controlExpandX = layerLegend.getWidth() - getTitleHeight();
			controlToolX = controlExpandX - (controlExpandWidth + 2) - 1;
			controlEyeX = controlToolX - (controlExpandWidth + 2) - 1;
			controlInfoX = controlEyeX - (controlExpandWidth + 2) - 1;

			int titleX = 3;
			int titleY = 0;
			int titleWidth = layerLegend.getWidth()
					- getTitleHeight(layerLegend) - 3;
			int titleHeight = getTitleHeight(layerLegend);

			if (!layerLegend.getComponentOrientation().isLeftToRight()) {
				controlExpandX = layerLegend.getWidth() - controlExpandX
						- controlExpandWidth;
				titleX = layerLegend.getWidth() - titleX - titleWidth;
			}

			// paint the title background
			paintTitleBackground(layerLegend, g);

			// paint the the toggles
			paintExpandedControls(layerLegend, g, controlExpandX,
					controlExpandY, controlExpandWidth, controlExpandWidth);

			paintEyeControls(layerLegend, g, controlEyeX, controlEyeY,
					controlEyeWidth, controlEyeWidth);

			if (layerLegend.getInfoURL() != null) {
				ICON_INFO.paintIcon(layerLegend, g, controlInfoX, controlInfoY);
			}

			paintToolControls(layerLegend, g, controlToolX, controlToolY,
					controlToolWidth, controlToolWidth);

			// paint the title text and icon
			Color paintColor = getPaintColor(layerLegend);

			// focus painted same color as text
			if (layerLegend.hasFocus()) {
				paintFocus(g, paintColor, 3, 3, width - 6,
						getTitleHeight(layerLegend) - 6);
			}

			paintTitle(layerLegend, g, paintColor, titleX, titleY, titleWidth,
					titleHeight);

		}

		/**
		 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
		 *         Kr&uuml;ger</a>
		 */
		public void paintToolControls(JXTaskPane group, Graphics g, int x,
				int y, int width, int height) {
			ICON_TOOL.paintIcon(group, g, x, y);
		}

		/**
		 * Paint the the Eye in oen of three states: full visible, half
		 * transparent, not visible
		 * 
		 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
		 *         Kr&uuml;ger</a>
		 */
		public void paintEyeControls(MapLayerLegend mapLayerLegend, Graphics g,
				int x, int y, int width, int height) {

			ImageIcon icon = null;
			if (mapLayerLegend.isLayerVisible()) {

				if (mapLayerLegend.isTransparent())
					icon = ICON_HALFVISIBLE;
				else
					icon = ICON_VISIBLE;

				icon.paintIcon(mapLayerLegend, g, x, y);

				/**
				 * Paint a filter symbol above if the layer is filtered
				 */
				if (mapLayerLegend.isFiltered()) {
					ICON_FILTER.paintIcon(mapLayerLegend, g, x + 3, y - 2);
				}

			} else {
				ICON_NOTVISIBLE.paintIcon(mapLayerLegend, g, x, y);
			}

		}

		/**
		 * Paints oval 'border' area around the control itself.
		 * 
		 * @param group
		 *            Expanded group.
		 * @param g
		 *            Target graphics.
		 * @param x
		 *            X coordinate of the top left corner.
		 * @param y
		 *            Y coordinate of the top left corner.
		 * @param width
		 *            Width of the box.
		 * @param height
		 *            Height of the box.
		 */
		protected void paintRectAroundControls(JXTaskPane group, Graphics g,
				int x, int y, int width, int height, Color highColor,
				Color lowColor) {
			if (mouseOver) {
				int x2 = x + width;
				int y2 = y + height;
				g.setColor(highColor);
				g.drawLine(x, y, x2, y);
				g.drawLine(x, y, x, y2);
				g.setColor(lowColor);
				g.drawLine(x2, y, x2, y2);
				g.drawLine(x, y2, x2, y2);
			}
		}

		/**
		 * Paints oval 'border' area around the control itself.
		 * 
		 * @param group
		 *            Expanded group.
		 * @param g
		 *            Target graphics.
		 * @param x
		 *            X coordinate of the top left corner.
		 * @param y
		 *            Y coordinate of the top left corner.
		 * @param width
		 *            Width of the box.
		 * @param height
		 *            Height of the box.
		 */
		protected void paintOvalAroundControls(JXTaskPane group, Graphics g,
				int x, int y, int width, int height) {
			if (group.isSpecial()) {
				g.setColor(specialTitleBackground.brighter());
				g.drawOval(x, y, width, height);
			} else {
				g.setColor(titleBackgroundGradientStart);
				g.fillOval(x, y, width, height);

				g.setColor(titleBackgroundGradientEnd.darker());
				g.drawOval(x, y, width, width);
			}
		}

		/**
		 * Paints controls for the group.
		 * 
		 * @param group
		 *            Expanded group.
		 * @param g
		 *            Target graphics.
		 * @param x
		 *            X coordinate of the top left corner.
		 * @param y
		 *            Y coordinate of the top left corner.
		 * @param width
		 *            Width of the box.
		 * @param height
		 *            Height of the box.
		 */
		protected void paintChevronControls(JXTaskPane group, Graphics g,
				int x, int y, int width, int height) {
			ChevronIcon chevron;
			if (group.isCollapsed()) {
				chevron = new ChevronIcon(false);
			} else {
				chevron = new ChevronIcon(true);
			}
			int chevronX = x + width / 2 - chevron.getIconWidth() / 2;
			int chevronY = y + (height / 2 - chevron.getIconHeight());
			chevron.paintIcon(group, g, chevronX, chevronY);
			chevron.paintIcon(group, g, chevronX, chevronY
					+ chevron.getIconHeight() + 1);
		}

		/**
		 * Paints focused group.
		 * 
		 * @param g
		 *            Target graphics.
		 * @param paintColor
		 *            Focused group color.
		 * @param x
		 *            X coordinate of the top left corner.
		 * @param y
		 *            Y coordinate of the top left corner.
		 * @param width
		 *            Width of the box.
		 * @param height
		 *            Height of the box.
		 */
		protected void paintFocus(Graphics g, Color paintColor, int x, int y,
				int width, int height) {
			g.setColor(paintColor);
			BasicGraphicsUtils.drawDashedRect(g, x, y, width, height);
		}

		/**
		 * Default implementation returns false.
		 * 
		 * @return true if this border wants to display things differently when
		 *         the mouse is over it
		 */
		protected boolean isMouseOverBorder() {
			return false;
		}
	}

	/**
	 * Gets size of arc used to round corners.
	 * 
	 * @return size of arc used to round corners of the panel.
	 */
	protected int getRoundHeight() {
		return roundHeight;
	}

}
