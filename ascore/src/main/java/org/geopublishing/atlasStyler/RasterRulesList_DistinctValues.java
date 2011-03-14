package org.geopublishing.atlasStyler;

import java.util.ArrayList;
import java.util.List;

import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.opengis.filter.Filter;

import de.schmitzm.geotools.FilterUtil;
import de.schmitzm.geotools.styling.StyledRasterInterface;

public class RasterRulesList_DistinctValues extends RasterRulesList {

	public RasterRulesList_DistinctValues(StyledRasterInterface styledRaster) {
		super(styledRaster);
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

			setRs((RasterSymbolizer) rule.symbolizers().get(0));
			//
			// final Description description = rule.getDescription();
			// final InternationalString title2 = description.getTitle();
			// setLabel(title2.toString());
			// } catch (final NullPointerException e) {
			// LOGGER.warn("The title style to import has been null!");
			// setLabel("");
			// } catch (final Exception e) {
			// LOGGER.error("The title style to import could not been set!", e);
			// setLabel("");
			// }

			// Analyse the filters...
			Filter filter = rule.getFilter();
			filter = parseAbstractRlSettings(filter);

		} finally {
			popQuite();
		}
	}

	@Override
	public List<Rule> getRules() {

		RasterSymbolizer rs = null;

		Rule rule = ASUtil.SB.createRule(rs);

		/** Saving the legend label */
		rule.setTitle("raster titel todo");
		rule.setTitle("raster name todo");

		// addFilters(rule);

		rule.symbolizers().clear();
		rule.symbolizers().add(getRs());
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

}
