package org.geopublishing.atlasStyler.swing;

import org.geopublishing.atlasViewer.ExportableLayer;
import org.geopublishing.atlasViewer.swing.MapLayerLegend;
import org.geopublishing.atlasViewer.swing.MapLegend;
import org.geotools.map.MapLayer;

import de.schmitzm.geotools.gui.GeoMapPane;
import de.schmitzm.geotools.gui.MapPaneToolBar;
import de.schmitzm.geotools.styling.StyledLayerInterface;

public class AtlasStylerMapLegend extends MapLegend {

	private static final long serialVersionUID = 8491101165362101286L;

	public AtlasStylerMapLegend(GeoMapPane geoMapPane, MapPaneToolBar mapPaneToolBar) {
		super(geoMapPane, mapPaneToolBar);
	}
	

	/**
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
	 * @return <code>null</code> if no legend should be visible for this layer.
	 *         Generally constructs a {@link MapLayerLegend} with the given
	 *         parameters. This method may be overwritten by sub classes.
	 */
	protected MapLayerLegend createMapLayerLegend(MapLayer mapLayer,
			ExportableLayer exportable, StyledLayerInterface<?> styledObj,
			MapLegend layerPanel) {

		return new AtlasStylerMapLayerLegend(mapLayer, exportable, styledObj, this);
	}


}
