package org.geopublishing.atlasStyler.rulesLists;

import org.geotools.styling.ColorMap;

import de.schmitzm.geotools.data.rld.RasterLegendData;
import de.schmitzm.geotools.styling.StyledRasterInterface;

public class RasterRulesList_Ramps extends RasterRulesList {

	public RasterRulesList_Ramps(StyledRasterInterface<?> styledRaster, boolean withDefaults) {
		super(RulesListType.RASTER_COLORMAP_RAMPS, styledRaster, ColorMap.TYPE_RAMP);
	}

	@Override
	/**
	 * For distinct values, the RasterLegend is 1:1 relation to the values 
	 */
	public RasterLegendData getRasterLegendData() {
		RasterLegendData rld = new RasterLegendData(true);
//TODO
//		for (int i = 0; i < getNumClasses(); i++) {
//			rld.put(getValues().get(i), getLabels().get(i));
//		}
//
		return rld;
	}

}
