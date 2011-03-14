package org.geopublishing.atlasStyler;

import java.util.ArrayList;
import java.util.List;

import org.geotools.styling.ColorMap;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.opengis.filter.Filter;

import de.schmitzm.geotools.FilterUtil;
import de.schmitzm.geotools.styling.StyledRasterInterface;
import de.schmitzm.geotools.styling.StylingUtil;

public class RasterRulesList_DistinctValues extends RasterRulesList {

	public RasterRulesList_DistinctValues(StyledRasterInterface styledRaster) {
		super(styledRaster, ColorMap.TYPE_VALUES);
	}

	@Override
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

	@Override
	public List<Rule> getRules() {

		RasterSymbolizer rs = StylingUtil.STYLE_BUILDER
				.createRasterSymbolizer();
		rs.setColorMap(getColorMap());

		Rule rule = ASUtil.SB.createRule(rs);

		/** Saving the legend label */
		rule.setTitle("raster titel todo");
		rule.setTitle("raster name todo");

		// addFilters(rule);

		rule.symbolizers().clear();

		rule.symbolizers().add(rs);

		Filter filter = FilterUtil.ALLWAYS_TRUE_FILTER;

		// The order is important! This is parsed the reverse way. The last
		// thing added to the filter equals the first level in the XML.
		filter = addAbstractRlSettings(filter);

		rule.setFilter(filter);

		ArrayList<Rule> rList = new ArrayList<Rule>();
		rList.add(rule);

		return rList;

	}

	@Override
	public RulesListType getType() {
		return RulesListType.RASTER_COLORMAP_DISTINCTVALUES;
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
