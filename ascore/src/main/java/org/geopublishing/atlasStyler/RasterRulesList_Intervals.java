package org.geopublishing.atlasStyler;

import org.geopublishing.atlasStyler.rulesLists.RasterRulesList;
import org.geotools.styling.ColorMap;
import org.geotools.styling.FeatureTypeStyle;

import de.schmitzm.geotools.data.rld.RasterLegendData;
import de.schmitzm.geotools.styling.StyledRasterInterface;

public class RasterRulesList_Intervals extends RasterRulesList {

	public RasterRulesList_Intervals(StyledRasterInterface<?> styledRaster, boolean withDefaults) {
		super(RulesListType.RASTER_COLORMAP_INTERVALS, styledRaster, ColorMap.TYPE_INTERVALS);
		
		if (withDefaults) {
			// TODO automatically min to max?
		}
	}

	@Override
	public String getAtlasMetaInfoForFTSName() {
		return "";
	}

	@Override
	public void parseMetaInfoString(String metaInfoString, FeatureTypeStyle fts) {
	}

	@Override
	public RasterLegendData getRasterLegendData() {
		return new RasterLegendData(true);
	}

}
