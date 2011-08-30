package org.geopublishing.atlasStyler.rulesLists;

import java.util.List;

import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;

import de.schmitzm.geotools.styling.StyledRasterInterface;

public class RasterRulesListRGB extends RasterRulesList {

	public RasterRulesListRGB(
			StyledRasterInterface<?> styledRaster, boolean withDefaults) {
		super(RulesListType.RASTER_RGB, styledRaster);
		
		if (withDefaults) {
			// Setzte 1->1
			// Setzte 2->2
			// Setzte 3->3
		}
		
		// getStyledRaster().getBandCount();
	}

	@Override
	public void applyOpacity() {
		// TODO Auto-generated method stub
	}

	@Override
	public List<Rule> getRules() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void importRules(List<Rule> rules) {
		// TODO Auto-generated method stub

	}

	@Override
	public void parseMetaInfoString(String metaInfoString, FeatureTypeStyle fts) {
		// TODO Auto-generated method stub

	}

}
