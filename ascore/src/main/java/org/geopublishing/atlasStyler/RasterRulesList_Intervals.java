package org.geopublishing.atlasStyler;

import java.util.List;

import org.geopublishing.atlasStyler.rulesLists.AbstractRulesList;
import org.geopublishing.atlasStyler.rulesLists.RasterRulesList;
import org.geotools.styling.ColorMap;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;

import de.schmitzm.geotools.data.rld.RasterLegendData;
import de.schmitzm.geotools.styling.StyledRasterInterface;

public class RasterRulesList_Intervals extends RasterRulesList {

	public RasterRulesList_Intervals(StyledRasterInterface<?> styledRaster) {
		super(RulesListType.RASTER_COLORMAP_INTERVALS, styledRaster, ColorMap.TYPE_INTERVALS);
	}

	@Override
	public String getAtlasMetaInfoForFTSName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void parseMetaInfoString(String metaInfoString, FeatureTypeStyle fts) {
		// TODO Auto-generated method stub

	}

	@Override
	public void importRules(List<Rule> rules) {
		// TODO Auto-generated method stub

	}

	@Override
	public RasterLegendData getRasterLegendData() {
		// TODO Auto-generated method stub
		return null;
	}

}
