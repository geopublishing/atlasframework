package org.geopublishing.atlasStyler;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.geotools.map.MapLayer;
import org.geotools.styling.Style;
import org.geotools.styling.Symbolizer;

import de.schmitzm.geotools.styling.StyledGridCoverageReaderInterface;

public class AtlasStylerRaster extends AtlasStyler {
	
	private final static Logger LOGGER = Logger.getLogger(AtlasStylerRaster.class);
	
	StyledGridCoverageReaderInterface styledRaster; 

	public AtlasStylerRaster(StyledGridCoverageReaderInterface styledRaster, Style loadStyle, MapLayer mapLayer, HashMap<String, Object> params,
			Boolean withDefaults) {
		super(mapLayer, params, withDefaults);
		this.styledRaster = styledRaster;
		
		this.rlf = new RuleListFactory(styledRaster);
		
		if (loadStyle != null) {
			// Correct propertynames against the Schema
			importStyle(loadStyle);
		} else {
			if (styledRaster.getStyle() != null) {
				importStyle(styledRaster.getStyle());
			} else {

				if (withDefaults != null && withDefaults == true) {
					final SingleRuleList<? extends Symbolizer> defaultRl = rlf
							.createSingleRulesList(
									getRuleTitleFor(styledRaster), true);
					LOGGER.debug("Added default rulelist: " + defaultRl);
					addRulesList(defaultRl);
				}
			}
		}

	}

	@Override
	public AbstractRulesList copyRulesList(AbstractRulesList rl) {
		return null;
	}

	@Override
	public Style sanitize(Style style) {
		return style;
	}

}
