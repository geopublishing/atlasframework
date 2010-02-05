/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the AtlasViewer application - A GIS viewer application targeting at end-users with no GIS-experience. Its main purpose is to present the atlases created with the Geopublisher application.
 * http://www.geopublishing.org
 * 
 * AtlasViewer is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.atlas.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.geotools.feature.FeatureCollection;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.Style;

import com.vividsolutions.jts.geom.Coordinate;

import schmitzm.geotools.GTUtil;
import schmitzm.geotools.gui.SelectableXMapPane;
import schmitzm.geotools.map.event.FeatureSelectedEvent;
import schmitzm.geotools.map.event.ObjectSelectionEvent;
import schmitzm.geotools.styling.StylingUtil;
import schmitzm.swing.SwingUtil;
import skrueger.atlas.AtlasConfig;
import skrueger.atlas.map.Map;
import skrueger.geotools.MapContextManagerInterface;
import skrueger.geotools.XMapPane;

/**
 * This Dialog wraps a {@link ClickInfoPanel} that shows information when the
 * mouse clicked inside the {@link Map} and has hit a visible features.
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
 * 
 */
public class ClickInfoDialog extends JDialog {
	final ClickInfoPanel clickInfoPanel;
	private final AtlasConfig atlasConfig;

	Boolean userMovedTheWindow = false;
	protected int lastHeight;
	protected int lastWidth;
	protected boolean rightGlue = false;
	protected boolean bottomGlue = false;

	private static final Logger LOGGER = Logger
			.getLogger(ClickInfoDialog.class);

	/**
	 * This Dialog wraps a {@link ClickInfoPanel} that shows information when
	 * the mouse clicked inside the {@link Map} and has hit a visible features.
	 * 
	 * @param parentGUI
	 *            A {@link Component} that links to the parent GUI
	 * 
	 * @param modal
	 * 
	 * @param atlasConfig
	 *            may be <code>null</code> if not in atlas context
	 * 
	 * @param layerManager
	 *            A {@link MapContextManagerInterface} to communicate with the
	 *            geotools {@link MapContext}
	 */
	public ClickInfoDialog(final Component parentGUI, final boolean modal,
			final MapContextManagerInterface layerManager,
			AtlasConfig atlasConfig) {
		super(SwingUtil.getParentWindow(parentGUI));
		this.atlasConfig = atlasConfig;

		if (layerManager == null) {
			throw new IllegalArgumentException(
					"MapContextManagerInterface may not be null!");
		}

		setModal(modal);

		clickInfoPanel = new ClickInfoPanel(layerManager, ClickInfoDialog.this,
				this.atlasConfig);

		// ****************************************************************************
		// Ensure, that the VideoPlayer is never left unattended.
		// ****************************************************************************
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				// clickInfoPanel.disposeVideoPlayer();
				super.windowClosing(e);
			}

		});

		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentMoved(ComponentEvent e) {
				userMovedTheWindow = true;
				// LOGGER.debug("Userinteraction TRUE!");
				checkGlue(e);
			}

			@Override
			public void componentResized(ComponentEvent e) {
				final int newWidth = e.getComponent().getWidth();
				final int newHeight = e.getComponent().getHeight();

				/*
				 * If rightGlue and the new width is less, move the window
				 * right!
				 */
				if (rightGlue && newWidth < lastWidth) {
					int dif = (lastWidth - newWidth);
					setLocation(getLocation().x + dif, getLocation().y);
				}

				/*
				 * If leftGlue all is automatically OK
				 */
				if (bottomGlue && newHeight < lastHeight) {
					int dif = (lastHeight - newHeight);
					setLocation(getLocation().x, getLocation().y + dif);
				}

				lastHeight = newHeight;
				lastWidth = newWidth;
				checkGlue(e);
			}

			private void checkGlue(final ComponentEvent e) {
				if ((e.getComponent().getWidth() + getLocation().x) == getToolkit()
						.getScreenSize().width) {
					/**
					 * The Window is now glued to the right.
					 */
					rightGlue = true;
				} else
					rightGlue = false;

				/**
				 * Bottom GLUE is not yet supported because we can't properly
				 * determine the bottom insets for windows + ubuntu
				 */
				//
				// int bottom = getToolkit().getScreenInsets(
				// ClickInfoDialog.this.getGraphicsConfiguration()).bottom;
				//
				// if ((e.getComponent().getHeight() + getLocation().y) ==
				// (getToolkit()
				// .getScreenSize().height - bottom)) {
				// /**
				// * The Window is now glued to the bottom
				// */
				// bottomGlue = true;
				// } else
				// bottomGlue = false;
				//
				// LOGGER.debug(bottom + "sd"
				// + getToolkit().getScreenSize().height + " sds "
				// + (e.getComponent().getHeight() + getLocation().y));
			}

		});

		final JPanel cp = new JPanel(new BorderLayout());

		/**
		 * Adding the Panel with a JScrollPane
		 */
		cp.add(new JScrollPane(clickInfoPanel), BorderLayout.CENTER);

		setContentPane(cp);

		setDefaultCloseOperation(HIDE_ON_CLOSE);
	}

	/**
	 * Update with info-dialog with the new {@link ObjectSelectionEvent}
	 */
	public void setSelectionEvent(
			final ObjectSelectionEvent<?> objectSelectionEvent) {
		clickInfoPanel.setSelectionEvent(objectSelectionEvent);

		SelectableXMapPane source = objectSelectionEvent.getSource();
		
		// If it is a feature, let it blink for a moment
		if (source instanceof XMapPane
				&& objectSelectionEvent instanceof FeatureSelectedEvent) {

			XMapPane mapPane = (XMapPane) objectSelectionEvent.getSource();
			mapPane.blink(((FeatureSelectedEvent) objectSelectionEvent)
					.getSelectionResult());
		}
		;
	}

	/**
	 * Since the registerKeyboardAction() method is part of the JComponent class
	 * definition, you must define the Escape keystroke and register the
	 * keyboard action with a JComponent, not with a JDialog. The JRootPane for
	 * the JDialog serves as an excellent choice to associate the registration,
	 * as this will always be visible. If you override the protected
	 * createRootPane() method of JDialog, you can return your custom JRootPane
	 * with the keystroke enabled:
	 */
	@Override
	protected JRootPane createRootPane() {
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		JRootPane rootPane = new JRootPane();
		rootPane.registerKeyboardAction(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}

		}, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

		return rootPane;
	}
}
