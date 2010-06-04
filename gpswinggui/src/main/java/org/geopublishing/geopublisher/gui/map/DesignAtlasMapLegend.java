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

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasViewer.ExportableLayer;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.DataPool.EventTypes;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.swing.AtlasMapLegend;
import org.geopublishing.atlasViewer.swing.MapLayerLegend;
import org.geopublishing.atlasViewer.swing.MapLegend;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.gui.DesignAtlasChartJDialog;
import org.geotools.map.MapLayer;

import schmitzm.geotools.gui.FeatureLayerFilterDialog;
import schmitzm.geotools.gui.GeoMapPane;
import schmitzm.geotools.gui.XMapPaneEvent;
import schmitzm.geotools.map.event.FeatureSelectedEvent;
import schmitzm.geotools.map.event.JMapPaneListener;
import schmitzm.jfree.chart.style.ChartStyle;
import skrueger.geotools.MapPaneToolBar;
import skrueger.geotools.StyledLayerInterface;

public class DesignAtlasMapLegend extends AtlasMapLegend {
	protected Logger LOGGER = ASUtil.createLogger(this);

	private final AtlasConfigEditable ace;

	/**
	 * This listener will update the whole legend whenever the DPE changed. The
	 * listener is added when the legend is created, and removed when the legend
	 * is disposed.
	 */
	private final PropertyChangeListener dataPoolChangesListener = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals(EventTypes.changeDpe.toString())
					|| evt.getPropertyName().equals(
							EventTypes.removeDpe.toString())) {
				// rememberLegend.clear();
				recreateLayerList();
			}
		}

	};

	@Override
	protected boolean selectionButtonShallAppearFor(
			final DpLayer<?, ? extends ChartStyle> dpl) {
		return super.selectionButtonShallAppearFor(dpl)
				|| dpl instanceof DpLayerVectorFeatureSource
				&& DesignAtlasChartJDialog
						.isOpenForLayer((DpLayerVectorFeatureSource) dpl);
	}

	public DesignAtlasMapLegend(final GeoMapPane geoMapPane, Map map,
			final AtlasConfigEditable ace, MapPaneToolBar mapPaneToolBar) {
		super(geoMapPane, map, ace, mapPaneToolBar);
		this.ace = ace;

		/** Listen to changes in the datapool **/
		ace.getDataPool().addChangeListener(dataPoolChangesListener);

		/***********************************************************************
		 * This Listener reacts to the FilterDialog. If a Filter is applied, it
		 * is saved in the corresponding DPLayerVector.
		 **********************************************************************/
		geoMapPane.getMapPane().addMapPaneListener(new JMapPaneListener() {

			public void performMapPaneEvent(XMapPaneEvent e) {
				if (e instanceof FeatureSelectedEvent) {
					FeatureSelectedEvent fse = (FeatureSelectedEvent) e;
					if (fse.getSourceObject() instanceof FeatureLayerFilterDialog) {
						FeatureLayerFilterDialog fDialog = (FeatureLayerFilterDialog) fse
								.getSourceObject();

						String layerTitle = fse.getSourceLayer().getTitle();
						DpEntry dpe = ace.getDataPool().get(layerTitle);
						if (dpe instanceof DpLayerVectorFeatureSource) {
							DpLayerVectorFeatureSource dplv = (DpLayerVectorFeatureSource) dpe;
							dplv.setFilter(fDialog.getFilter());
						}
					}
				}

			}
		});
	}

	//
	// /**
	// * This is an overridden method. It calls the super method first and only
	// * add a second listener which saves the selected additional style.
	// *
	// *
	// * @param mapLayer
	// * The GeoTools {@link MapLayer} that will be affected by style
	// * changes.
	// * @param availableStyles
	// * A {@link List} of IDs of AdditionalStyles
	// * @param dpLayer
	// * The {@link DpLayer} that holds all the additional styles
	// *
	// * @return a new {@link JTabbedPane} that will represent all available
	// * Styles for a {@link DpLayer}.
	// */
	// @Override
	// protected Component createTabbedSylesPane(final MapLayer mapLayer,
	// final ArrayList<String> availableStyles, final DpLayer<?,ChartStyle>
	// dpLayer) {
	//
	// final Component legendPanel = super.createTabbedSylesPane(mapLayer,
	// availableStyles, dpLayer);
	//
	// return legendPanel;
	// }

	/**
	 * @param gmp
	 *            The {@link GeoMapPane} that the legend is working on
	 * @param mapLayer
	 *            The maplayer presented by this {@link MapLayerLegend}
	 * @param exportable
	 *            <code>null</code> or instance of {@link ExportableLayer} if
	 *            the layer can be exported
	 * @param styledObj
	 *            the {@link StyledLayerInterface} object that is presented by
	 *            this {@link MapLayerLegend}
	 * @param layerPanel
	 *            The parent {@link MapLegend} or {@link DesignAtlasMapLegend}
	 * 
	 * @return Generally constructs a {@link MapLayerLegend} with the given
	 *         paramters. This method is overridden by
	 *         {@link DesignAtlasMapLegend} to create DesignLayerPaneGroup
	 *         objects.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Tzeggai</a>
	 */
	@Override
	protected MapLayerLegend createMapLayerLegend(MapLayer mapLayer,
			ExportableLayer exportable, StyledLayerInterface<?> styledObj,
			MapLegend layerPanel) {

		String id = styledObj.getId();
		DpLayer<?, ChartStyle> dpLayer = (DpLayer<?, ChartStyle>) ace
				.getDataPool().get(id);

		if (dpLayer == null) {
			throw new RuntimeException("Can't find the layer id " + id
					+ " in the datapool");
		}

		DesignAtlasMapLayerLegend designLayerPaneGroup = new DesignAtlasMapLayerLegend(
				mapLayer, exportable, (DesignAtlasMapLegend) layerPanel,
				dpLayer, getMap());

		/**
		 * Check if the designLayerPaneGroup should come up minimized
		 */
		Boolean minimized = getMap().getMinimizedInLegendMap().get(
				dpLayer.getId());
		designLayerPaneGroup
				.setCollapsed(minimized != null ? minimized : false);

		/**
		 * Check if the designLayerPaneGroup is marked as "hideInLegend" in AV?
		 */
		Boolean hide = getMap().getHideInLegendMap().get(dpLayer.getId());
		if (hide != null && hide == true)
			designLayerPaneGroup.setBackground(Color.red);

		return designLayerPaneGroup;
	}

	public AtlasConfigEditable getAce() {
		return ace;
	}

	/**
	 * Helpt the GarbageCollection...
	 */
	public void dispose() {

		// In atlasMapView, the unneeded styledobjects are uncached if not
		// needed by the next map.. but for the DesignMapView we dispose them by
		// hand...
		for (StyledLayerInterface<?> styledObj : rememberId2StyledLayer
				.values()) {
			styledObj.uncache();
		}

		super.dispose();

		// The DataPool listeners are held in a WeakHashMap.. so no need to
		// remove
		// getAce().getDataPool().removeChangeListener(dataPoolChangesListener);
	}

}
