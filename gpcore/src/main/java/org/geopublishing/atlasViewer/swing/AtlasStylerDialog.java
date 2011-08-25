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
package org.geopublishing.atlasViewer.swing;

import java.awt.Component;
import java.util.HashMap;

import org.geopublishing.atlasStyler.AtlasStylerRaster;
import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.StyleChangeListener;
import org.geopublishing.atlasStyler.StyleChangedEvent;
import org.geopublishing.atlasStyler.swing.StylerDialog;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;
import org.geopublishing.atlasViewer.dp.layer.DpLayerRaster_Reader;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;
import org.geopublishing.atlasViewer.dp.layer.LayerStyle;
import org.geopublishing.atlasViewer.map.Map;
import org.geotools.map.MapLayer;
import org.geotools.styling.Style;

import de.schmitzm.geotools.styling.StyledGridCoverageReaderInterface;
import de.schmitzm.geotools.styling.StyledLayerInterface;
import de.schmitzm.i18n.Translation;
import de.schmitzm.swing.SwingUtil;

public class AtlasStylerDialog extends StylerDialog {

	protected MapLayer mapLayer;
	protected final DpLayerVectorFeatureSource dpLayerVector;
	protected final StyledGridCoverageReaderInterface dpLayerRaster;
	protected AtlasMapLegend atlasMapLegend;
	protected Map map;

	/**
	 * @returns the {@link #dpLayerRaster} is set. Otherwise the
	 *          {@link #dpLayerVector}
	 */
	public StyledLayerInterface<?> getStyledLayer() {
		if (dpLayerRaster != null)
			return dpLayerRaster;
		if (dpLayerVector != null)
			return dpLayerVector;
		return null;
	}

	// If null if this Style doesn't come from a layerStyle
	protected LayerStyle layerStyle;

	/**
	 * Constructor to user with vector data
	 * 
	 * @param owner
	 * @param dpLayer
	 * @param atlasMapLegend
	 * @param mapLayer
	 * @param layerStyle
	 */
	public AtlasStylerDialog(Component owner,
			final DpLayerVectorFeatureSource dpLayer,
			final AtlasMapLegend atlasMapLegend, final MapLayer mapLayer,
			final LayerStyle layerStyle) {
		super(owner,
				new AtlasStylerVector(dpLayer,
						layerStyle != null ? layerStyle.getStyle()
								: dpLayer.getStyle(),
						// atlasMapLegend,
						mapLayer, getParamMap(dpLayer), true),
				atlasMapLegend == null ? null : atlasMapLegend.getGeoMapPane()
						.getMapPane());

		this.dpLayerVector = dpLayer;
		this.dpLayerRaster = null;
		init(owner, dpLayer, atlasMapLegend, mapLayer, layerStyle);
	}

	/**
	 * Constructor to user with raster data
	 * 
	 * @param owner
	 * @param dpLayer
	 *            Source of raster data
	 * @param atlasMapLegend
	 * @param mapLayer
	 * @param layerStyle
	 */
	public AtlasStylerDialog(Component owner,
			final DpLayerRaster_Reader dpLayer,
			final AtlasMapLegend atlasMapLegend, final MapLayer mapLayer,
			final LayerStyle layerStyle) {
		super(owner,
				new AtlasStylerRaster(dpLayer,
						layerStyle != null ? layerStyle.getStyle()
								: dpLayer.getStyle(),
						// atlasMapLegend,
						mapLayer, getParamMap(dpLayer), true),
				atlasMapLegend == null ? null : atlasMapLegend.getGeoMapPane()
						.getMapPane());

		this.dpLayerVector = null;
		this.dpLayerRaster = dpLayer;
		init(owner, dpLayer, atlasMapLegend, mapLayer, layerStyle);
	}

	private void init(Component owner, final DpLayer dpLayer,
			final AtlasMapLegend atlasMapLegend, final MapLayer mapLayer,
			final LayerStyle layerStyle) {
		getAtlasStyler().setOwner(this);

		this.layerStyle = layerStyle;

		AtlasMapLayerLegend atlasLayerLegend = (AtlasMapLayerLegend) atlasMapLegend
				.getLayerLegendForId(dpLayer.getId());

		this.map = atlasLayerLegend.getMap();

		this.atlasMapLegend = atlasMapLegend;

		// This listener informs the MapLayerLegend,
		// resulting in a new legend and repained JMapPane
		if (atlasMapLegend != null)
			getAtlasStyler().addListener(new StyleChangeListener() {

				@Override
				public void changed(StyleChangedEvent e) {

//					if (e instanceof RasterStyleChangedEvent) {
//						((RasterStyleChangedEvent) e).getLegendMetadata()
//								.copyTo(dpLayerRaster.getLegendMetaData());
//					}

					Style style = e.getStyle();
					if (layerStyle == null) {
						// The edited Style was a default Style
						dpLayer.setStyle(style);
					} else {
						// The edited Style came from a LayerStyle
						layerStyle.setStyle(style);
					}

					MapLayerLegend layerLegendForId = atlasMapLegend.getLayerLegendForId(dpLayer.getId());
					layerLegendForId
							.updateStyle(style);
				}

			});

		this.mapLayer = mapLayer;

		// If we have a layerstyle, use it's title in the title of the dialog
		// and the AtlasStyler object.
		if (layerStyle != null) {
			/***********************************************************************
			 * We create the AtlasStyler. It will automatically load the Style
			 * visible in the MapLayer ATM.
			 */
			String titleWithAdditionalStyleName = AtlasStylerVector.R(
					"AtlasStylerDialog.Title.LayerXStyleY", dpLayer.getTitle()
							.toString(), layerStyle.getTitle().toString());
			getAtlasStyler().setTitle(
					new Translation(titleWithAdditionalStyleName));
			setTitle(titleWithAdditionalStyleName);
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
		SwingUtil.setRelativeFramePosition(this, owner, SwingUtil.BOUNDS_OUTER,
				SwingUtil.WEST);
	}

	private static HashMap<String, Object> getParamMap(DpLayer dpLayer_) {
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put(AtlasStylerVector.PARAM_LANGUAGES_LIST_STRING, dpLayer_
				.getAtlasConfig().getLanguages());
		params.put(AtlasStylerVector.PARAM_FONTS_LIST_FONT, dpLayer_
				.getAtlasConfig().getFonts());
		return params;
	}

	@Override
	public boolean okClose() {
		super.okClose();
		/**
		 * If we are editing an additional Style, we have to store the changes
		 * there!
		 */
		if (layerStyle != null) {
			layerStyle.setStyle(getAtlasStyler().getStyle());

			// if (getAtlasStyler() instanceof AtlasStylerRaster) {
			// ((AtlasStylerRaster)getAtlasStyler()).getRasterLegendData().copyTo(dpLayerRaster.getLegendMetaData());
			// }

			/**
			 * If may be, that the selectedTab/ selectedItem in the legend Panel
			 * might have been set to another Tab/Item. For that case we set it
			 * back to the one we are editing.
			 */
			map.setSelectedStyleID(getStyledLayer().getId(), layerStyle.getID());

		} else {
			getStyledLayer().setStyle(getAtlasStyler().getStyle());
		}

		return super.okClose();
	}

	@Override
	public void cancel() {

		// The restored Style will be put into the MapLayer.
		super.cancel();

		if (layerStyle == null) {

			getStyledLayer().setStyle(mapLayer.getStyle());
		} else {
			if (isVector()) {
				// The edited Style came from a LayerStyle
				layerStyle.setStyle(mapLayer.getStyle());
			} else {
			}
		}
	}

}
