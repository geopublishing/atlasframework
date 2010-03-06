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
package skrueger.sld.gui;

import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import org.apache.log4j.Logger;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;

import schmitzm.geotools.gui.SelectableXMapPane;
import schmitzm.geotools.gui.XMapPaneEvent;
import schmitzm.geotools.map.event.JMapPaneListener;
import schmitzm.geotools.map.event.ObjectSelectionEvent;
import schmitzm.swing.SwingUtil;
import skrueger.atlas.gui.ClickInfoDialog;
import skrueger.atlas.gui.MapLegend;
import skrueger.geotools.MapContextManagerInterface;
import skrueger.geotools.MapPaneToolBar;
import skrueger.geotools.MapView;
import skrueger.sld.ASProps;
import skrueger.sld.ASUtil;

public class StylerMapView extends MapView {
	protected Logger LOGGER = ASUtil.createLogger(this);

	private MapLegend layerManager;

	private JScrollPane leftSide;

	private ClickInfoDialog clickInfoDialog;

	private JMapPaneListener infoClickMapPaneListener;

	private final Window owner;

	public StylerMapView(Window owner) {
		super(owner);
		this.owner = owner;
		initialize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see skrueger.atlas.gui.MapView#initialize()
	 */
	@Override
	public void initialize() {
		super.initialize(); // important!

		Border insideBorder = getGeoMapPane().getMapPane().getBorder();
		getGeoMapPane().getMapPane().setBorder(
				BorderFactory.createCompoundBorder(BorderFactory
						.createMatteBorder(0, 0, 0, 0, getGeoMapPane()
								.getBackground()), insideBorder));

		// ****************************************************************************
		// Show selected features / information when clicked on a Info-Frame
		// ****************************************************************************
		clickInfoDialog = new ClickInfoDialog(owner, false, layerManager, null);
		infoClickMapPaneListener = new JMapPaneListener() {

			public void performMapPaneEvent(XMapPaneEvent evt) {

				/**
				 * This only reacts if the INFO tool has been selected in the
				 * toolbar, AND the event has not been thrown by the
				 * FilterDialog.
				 */
				if (getToolBar().getSelectedTool() == MapPaneToolBar.TOOL_INFO
						&& evt.getSourceObject() instanceof SelectableXMapPane
						&& evt instanceof ObjectSelectionEvent) {

					final ObjectSelectionEvent<?> e2 = (ObjectSelectionEvent<?>) evt;
					clickInfoDialog.setSelectionEvent(e2);

					if (!clickInfoDialog.isVisible()) {
						// TODO Richtig schoen oben rechts in die Ecke
						SwingUtil.setRelativeFramePosition(clickInfoDialog,
								SwingUtil.getParentFrame(StylerMapView.this),
								0.93, .08);
					}

					clickInfoDialog.setVisible(true);
				}
			}

		};
		getGeoMapPane().getMapPane().addMapPaneListener(
				infoClickMapPaneListener);

		/***********************************************************************
		 * Set the ASProps for Antialiasing
		 */
		getGeoMapPane().getMapPane().setAntiAliasing(
				ASProps.getInt(ASProps.Keys.antialiasingMaps, 1) == 1);
		
		getGeoMapPane().getMapPane().setShowExceptions(true);
	}

	/**
	 * Called to fill the left side of the {@link MapView}<br>
	 * Is supposed to set {@link #layerManager} variable.
	 */
	@Override
	public JScrollPane getSidePane() {

		if (leftSide == null) {
			leftSide = new JScrollPane();
			leftSide.setViewportView(getLayerManager());
		}
		return leftSide;
	}

	public MapContextManagerInterface getMapManager() {
		return getLayerManager();
	}

	public MapLegend getLayerManager() {

		if (layerManager == null) {

			layerManager = new MapLegend(getGeoMapPane(), getToolBar());

			// layerManager.addMapLayerListListener(new MapLayerListListener() {
			//
			// public void layerAdded(MapLayerListEvent event) {
			// // Adjust the size of the LayerPanel
			// Dimension isSize = layerManager.getSize();
			// Dimension sollSize = new Dimension(250, (int) isSize
			// .getHeight());
			//
			// layerManager.setMinimumSize(sollSize);
			// layerManager.setMaximumSize(sollSize);
			// layerManager.setPreferredSize(sollSize);
			//
			// leftSide.setMinimumSize(sollSize);
			// leftSide.setMaximumSize(sollSize);
			// leftSide.setPreferredSize(sollSize);
			//
			// getSplitPane().setDividerLocation(-1);
			// leftSide.setViewportView(getLayerManager());
			//
			// layerManager.invalidate();
			// layerManager.doLayout();
			// layerManager.repaint();
			// invalidate();
			// doLayout();
			// repaint();
			// LOGGER.debug("Maybe the sizes are better now?");
			// }
			//
			// public void layerChanged(MapLayerListEvent event) {
			// }
			//
			// public void layerMoved(MapLayerListEvent event) {
			// }
			//
			// public void layerRemoved(MapLayerListEvent event) {
			// }
			//
			// });

			// ****************************************************************************
			// Depending on the Title of the insered Layers, the width of the
			// tabbedPane has to be updated
			// ****************************************************************************
			layerManager.addMapLayerListListener(new MapLayerListListener() {

				public void layerAdded(MapLayerListEvent event) {
					getSplitPane().setDividerLocation(0.5);
				}

				public void layerChanged(MapLayerListEvent event) {
				}

				public void layerMoved(MapLayerListEvent event) {
				}

				public void layerRemoved(MapLayerListEvent event) {
					getSplitPane().setDividerLocation(0.5);
				}

			});
		}

		return layerManager;
	}

}
