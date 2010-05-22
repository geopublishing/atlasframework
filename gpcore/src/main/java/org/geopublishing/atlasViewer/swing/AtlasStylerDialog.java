/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasViewer.swing;

import java.awt.Component;

import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.StyleChangeListener;
import org.geopublishing.atlasStyler.StyleChangedEvent;
import org.geopublishing.atlasStyler.swing.StylerDialog;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;
import org.geopublishing.atlasViewer.dp.layer.LayerStyle;
import org.geopublishing.atlasViewer.map.Map;
import org.geotools.map.MapLayer;
import org.geotools.styling.Style;

import schmitzm.swing.SwingUtil;

public class AtlasStylerDialog extends StylerDialog {

	protected final MapLayer mapLayer;
	protected final DpLayerVectorFeatureSource dpLayer;
	protected final AtlasMapLegend atlasMapLegend;
	protected final Map map;

	// If null if this Style doesn't come from a layerStyle
	protected final LayerStyle layerStyle;

	public AtlasStylerDialog(Component owner,
			final DpLayerVectorFeatureSource dpLayer,
			final AtlasMapLegend atlasMapLegend, final MapLayer mapLayer,
			final LayerStyle layerStyle) {
		super(owner, new AtlasStyler(dpLayer, layerStyle != null ? layerStyle
				.getStyle() : dpLayer.getStyle(), 
//				atlasMapLegend,
				mapLayer, dpLayer.getAtlasConfig().getLanguages()));
		this.dpLayer = dpLayer;

		this.layerStyle = layerStyle;

		AtlasMapLayerLegend atlasLayerLegend = (AtlasMapLayerLegend) atlasMapLegend
				.getLayerLegendForId(dpLayer.getId());
		
		this.map = atlasLayerLegend.getMap();
		
		this.atlasMapLegend = atlasMapLegend;

		// This listener informs the MapLayerLegend,
		// resulting in a new legend and repained JMapPane
		getAtlasStyler().addListener(new StyleChangeListener() {

			@Override
			public void changed(StyleChangedEvent e) {
				Style style = getAtlasStyler().getStyle();
				if (layerStyle == null) {
					// The edited Style was a default Style
					dpLayer.setStyle(style);
				} else {
					// The edited Style came from a LayerStyle
					layerStyle.setStyle(style);
				}

				atlasMapLegend.getLayerLegendForId(dpLayer.getId())
						.updateStyle(style);
			}

		});

		this.mapLayer = mapLayer;

		if (layerStyle != null) {

			/***********************************************************************
			 * We create the AtlasStyler. It will automatically load the Style
			 * visible in the MapLayer ATM.
			 */
			setTitle(AtlasStyler.R("AtlasStylerDialog.Title.LayerXStyleY",
					dpLayer.getTitle().toString(), layerStyle.getTitle()
							.toString()));

		}

		/**
		 * While a layer is being edited, automatically make it's legend
		 * visible.
		 */
		if (mapLayer.isVisible() == false) {
			mapLayer.setVisible(true);
			map.setHiddenFor(dpLayer.getId(), false);
		}
		map.getMinimizedInLegendMap().put(dpLayer.getId(), false);

		
		/**
		 * Position left outside of the actual parent frame
		 */
		SwingUtil.setRelativeFramePosition(this, owner, SwingUtil.BOUNDS_OUTER, SwingUtil.WEST);
	}

	@Override
	public boolean okClose() {
		/**
		 * If we are editing an additional Style, we have to store the changes
		 * there!
		 */
		if (layerStyle != null) {
			layerStyle.setStyle(getAtlasStyler().getStyle());

			/**
			 * If may be, that the selectedTab/ selectedItem in the legend Panel
			 * might have been set to another Tab/Item. For that case we set it
			 * back to the one we are editing.
			 */
			map.setSelectedStyleID(dpLayer.getId(), layerStyle.getID());

		} else {
			dpLayer.setStyle(getAtlasStyler().getStyle());
		}

		return super.okClose();
	}

	@Override
	public void cancel() {

		// The restored Style will se put into the MapLayer.
		super.cancel();

		if (layerStyle == null) {
			// The edited Style was a default Style
			dpLayer.setStyle(mapLayer.getStyle());
		} else {
			// The edited Style came from a LayerStyle
			layerStyle.setStyle(mapLayer.getStyle());
		}
	}
	

}
