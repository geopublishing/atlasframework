package org.geopublishing.atlasStyler.rulesLists;

import java.util.List;

import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geotools.styling.ColorMap;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.opengis.filter.Filter;

import de.schmitzm.geotools.data.rld.RasterLegendData;
import de.schmitzm.geotools.styling.StyledRasterInterface;

public class RasterRulesList_Ramps extends RasterRulesList {


	public RasterRulesList_Ramps(StyledRasterInterface<?> styledRaster) {
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

	@Override
	public
	void importRules(List<Rule> rules) {
		pushQuite();

		if (rules.size() > 1) {
			LOGGER.warn("Importing a " + this.getClass().getSimpleName()
					+ " with " + rules.size() + " rules");
		}

		Rule rule = rules.get(0);

		try {
			RasterSymbolizer rs = (RasterSymbolizer) rule.symbolizers().get(0);
			ColorMap cm = rs.getColorMap();

			importValuesLabelsQuantitiesColors(cm);

			// Analyse the filters...
			Filter filter = rule.getFilter();
			filter = parseAbstractRlSettings(filter);

		} finally {
			popQuite();
		}
	}

	/**
	 * @param row
	 * @param delta
	 *            -1 to move the row one up
	 */
	public void move(int row, int delta) {

		getValues().add(row + delta, getValues().remove(row));
		getLabels().add(row + delta, getLabels().remove(row));
		getColors().add(row + delta, getColors().remove(row));
		getOpacities().add(row + delta, getOpacities().remove(row));
		fireEvents(new RuleChangedEvent("Index " + row + " moved up to "
				+ (row - 1), this));
	}

}
