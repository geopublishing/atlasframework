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
package org.geopublishing.atlasViewer.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.map.MapContext;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.gui.XMapPane;
import schmitzm.geotools.map.event.FeatureSelectedEvent;
import schmitzm.geotools.map.event.GridCoverageValueSelectedEvent;
import schmitzm.geotools.map.event.ObjectSelectionEvent;
import schmitzm.swing.SwingUtil;
import skrueger.geotools.MapContextManagerInterface;

import com.vividsolutions.jts.geom.Point;

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

		XMapPane source = objectSelectionEvent.getSource();

		// Set the title of the dialog to the translated title the layer
		try {
			setTitle(atlasConfig.getDataPool().get(
					objectSelectionEvent.getSourceLayer().getTitle())
					.getTitle().toString());
		} catch (Exception e) {
			LOGGER.error(e);
		}

		XMapPane mapPane = (XMapPane) objectSelectionEvent.getSource();
		// If it is a feature, let it blink for a moment
		if (source instanceof XMapPane
				&& objectSelectionEvent instanceof FeatureSelectedEvent) {

			mapPane.blink(((FeatureSelectedEvent) objectSelectionEvent)
					.getSelectionResult());
		} else {
			// Create a fake Feature and let it blink a moment
			final GridCoverageValueSelectedEvent gridSelection = (GridCoverageValueSelectedEvent) objectSelectionEvent;

			// TODO Help Martin, warum kann ich kein feake Feature mit correctem CRS erstellen? 
			Point2D selectionPoint = gridSelection.getSelectionPoint();

			CoordinateReferenceSystem crs = mapPane.getMapContext()
					.getCoordinateReferenceSystem();
			
			SimpleFeatureType fakeFeatureType = FeatureUtil.createFeatureType(
					Point.class, crs);
			
			SimpleFeature fakeFeature = FeatureUtil.createFeature(
					fakeFeatureType, true, "fake raster selection",
					new DirectPosition2D(crs, selectionPoint.getX(),
							selectionPoint.getY()));

			System.out.println("crs = " + fakeFeature.getFeatureType().getCoordinateReferenceSystem());

			mapPane.blink(fakeFeature);
		}
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
