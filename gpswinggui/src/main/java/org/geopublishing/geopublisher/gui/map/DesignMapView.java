/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.gui.map;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.DpRef;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.swing.AtlasMapView;
import org.geopublishing.atlasViewer.swing.AtlasViewerGUI;
import org.geopublishing.atlasViewer.swing.MapLegend;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GPProps;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.geotools.map.MapContext;

import skrueger.geotools.MapView;
import skrueger.geotools.StyledLayerInterface;

/**
 * This is the {@link MapView} used in the {@link GeopublisherGUI} to preview the
 * {@link Map}s.<br>
 * It allows the HTML folder to be changed and can be edited.<br>
 */
public class DesignMapView extends AtlasMapView {
	final static private Logger LOGGER = Logger.getLogger(DesignMapView.class);

	protected AtlasConfigEditable ace;

	private DesignHTMLInfoJPane designInfoPanel;

	/**
	 * Opens a {@link DesignMapView} to edit/layout the {@link Map} Remembers
	 * its existence in openInstances
	 * 
	 * Call setMap() after construction, and then Call initialize()
	 * 
	 * @param owner
	 *            The parent frame
	 * @param ace_
	 */
	public DesignMapView(Component owner, AtlasConfigEditable ace_) {
		super(owner, ace_);
		ace = ace_;

		// Show Exceptions in the map window
		getGeoMapPane().getMapPane().setShowExceptions(true);

		// Using the AtlasCreator settings
		getGeoMapPane().getMapPane().setAntiAliasing(
				GPProps.getInt(GPProps.Keys.antialiasingMaps, 1) == 1);
	}

	/**
	 * This sidepanel is different . It uses a {@link DesignAtlasMapLegend}
	 * instead of a simple {@link MapLegend}
	 */
	@Override
	public JComponent getSidePane() {

		if (layerManager == null) {
			JTabbedPane tabbedPane;
			tabbedPane = new JTabbedPane();
			
			final DesignAtlasMapLegend designLayerPanel = new DesignAtlasMapLegend(
					getGeoMapPane(), map, ace, getToolBar());
			
			layerManager = designLayerPanel;
			JScrollPane scrollPane = new JScrollPane(designLayerPanel);
			tabbedPane.addTab(AtlasViewerGUI
					.R("AtlasMapView.tabbedPane.LayersTab_label"),
					scrollPane);
			
			tabbedPane.setToolTipTextAt(0, AtlasViewerGUI
					.R("AtlasMapView.tabbedPane.LayersTab_tt"));

			JScrollPane scrollpane2 = new JScrollPane(getDesignInfoPanel());
			tabbedPane.addTab(AtlasViewerGUI
					.R("AtlasMapView.tabbedPane.InfoTab_label"),
					scrollpane2);
			
			tabbedPane.setToolTipTextAt(1, AtlasViewerGUI
					.R("AtlasMapView.tabbedPane.InfoTab_tt"));

			add(tabbedPane, BorderLayout.CENTER);

			leftSide = tabbedPane;
			
		}
		return leftSide;
	}

	/**
	 * Updates the list of layers in this map according to the underlying
	 * {@link MapContext} of the preview map.
	 */
	public void updateMapLayersFromMapContext(Map map2) {
		if (layerManager == null) {
			return;
		}

		map2.clearLayerList();

		List<StyledLayerInterface<?>> styledObjects = layerManager
				.getStyledObjects();

		for (StyledLayerInterface<?> o : styledObjects) {
			if (o == null)
				continue;
			String id = o.getId();
			DpEntry<?> dpEntry = ace.getDataPool().get(id);
			if (dpEntry == null) {
				LOGGER
						.warn("strange.. deleted from datapool while the mapview was open???");
			} else {
				map2.add(new DpRef(dpEntry));
			}
		}

	}

	/**
	 * Lazily initializes the {@link DesignHTMLInfoJPane} for this map.
	 */
	public DesignHTMLInfoJPane getDesignInfoPanel() {
		if (designInfoPanel == null) {
			designInfoPanel = new DesignHTMLInfoJPane(ace, map);
		}
		return designInfoPanel;
	}

	/**
	 * The trick here is to calculate the position of the divider according to a
	 * ratio, BEFORE the {@link JSplitPane} is visible and has any width! In the
	 * Atlas it works to use the owner, which is instance of {@link JFrame}.
	 * 
	 * @return The number in pixels from the left
	 */
	@Override
	protected int calcAbsoluteWidthForDivider(Double ratio) {
		int width = getWidth();
		if (width <= 0) {
			width = GPProps.getInt(GPProps.Keys.mapComposerWidth, 800);
		}
		return (int) (width * ratio);
	}

}
