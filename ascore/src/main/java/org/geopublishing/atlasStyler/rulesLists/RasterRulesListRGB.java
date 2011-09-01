package org.geopublishing.atlasStyler.rulesLists;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geotools.styling.ContrastEnhancement;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Symbolizer;
import org.opengis.filter.Filter;
import org.opengis.style.ChannelSelection;
import org.opengis.style.SelectedChannelType;

import de.schmitzm.geotools.FilterUtil;
import de.schmitzm.geotools.styling.StyledRasterInterface;
import de.schmitzm.geotools.styling.StylingUtil;

/**
 * This RulesList styles three out of three or more band of a multiband-raster
 * as a RGB image.
 */
public class RasterRulesListRGB extends RasterRulesList {

	final static Logger log = Logger.getLogger(RasterRulesListRGB.class);
	/**
	 * 1-based
	 */
	private int red = 1;
	/**
	 * 1-based
	 */
	private int green = 2;
	/**
	 * 1-based
	 */
	private int blue = 3;

	public RasterRulesListRGB(StyledRasterInterface<?> styledRaster,
			boolean withDefaults) {
		super(RulesListType.RASTER_RGB, styledRaster);

		if (withDefaults) {
			red = 1;
			green = Math.min(2, getStyledRaster().getBandCount());
			blue = Math.min(3, getStyledRaster().getBandCount());
		} 
	}

	@Override
	public void applyOpacity() {
		// Gibt nur eine Rule, wo die Opacity bei getRules gesetzt wird.
	}

	@Override
	public List<Rule> getRules() {
		RasterSymbolizer rs = StylingUtil.STYLE_BUILDER
				.createRasterSymbolizer();

		if (getOpacity() != null)
			rs.setOpacity(ff.literal(getOpacity()));

		ContrastEnhancement ce = StylingUtil.STYLE_FACTORY
				.createContrastEnhancement();

		SelectedChannelType redT = StylingUtil.STYLE_FACTORY
				.createSelectedChannelType(String.valueOf(red), ce);
		SelectedChannelType greenT = StylingUtil.STYLE_FACTORY
				.createSelectedChannelType(String.valueOf(green), ce);
		SelectedChannelType blueT = StylingUtil.STYLE_FACTORY
				.createSelectedChannelType(String.valueOf(blue), ce);
		ChannelSelection cs = StylingUtil.STYLE_FACTORY.channelSelection(redT,
				greenT, blueT);
		rs.setChannelSelection(cs);

		/**
		 * Rule mit dem oben erstellten Symbolizer zusammensetzen
		 */

		Rule rule = ASUtil.SB.createRule(rs);

		Filter filter = FilterUtil.ALLWAYS_TRUE_FILTER;

		// The order is important! This is parsed the reverse way. The last
		// thing added to the filter equals the first level in the XML.
		filter = addAbstractRlSettings(filter);

		rule.setFilter(filter);

		ArrayList<Rule> rList = new ArrayList<Rule>();
		rList.add(rule);
		/** Saving the legend label */
		rule.setTitle("TITLE" + getType().getTitle());
		rule.setName("NAME" + getType().getTitle());

		return rList;
	}

	@Override
	public void importRules(List<Rule> rules) {
		pushQuite();

		try {

			if (rules.size() < 1)
				return;
			Rule r = rules.get(0);
			
			// Analyse the filters...
			parseAbstractRlSettings(r.getFilter());

			for (Symbolizer s : r.getSymbolizers()) {
				if (s instanceof RasterSymbolizer) {

					RasterSymbolizer rs = (RasterSymbolizer) s;

					if (rs.getOpacity() != null)
						setOpacity(Double.valueOf(rs.getOpacity()
								.evaluate(null).toString()));

					ChannelSelection cs = rs.getChannelSelection();
					if (cs == null)
						continue;

					try {
						SelectedChannelType[] rgbChannels = cs.getRGBChannels();
						red = Integer.valueOf(rgbChannels[0].getChannelName());
						green = Integer
								.valueOf(rgbChannels[1].getChannelName());
						blue = Integer.valueOf(rgbChannels[2].getChannelName());
					} catch (Exception e) {
						log.error("RGB channels didn't contain 3 channels??");
						continue;
					}

					return;
				}
			}
		} finally {
			popQuite();
		}

	}

	@Override
	public void parseMetaInfoString(String metaInfoString, FeatureTypeStyle fts) {

		if (!metaInfoString.startsWith(getType().toString())) {
			// When importing a no ATlasStyler ColorMap
			return;
		}

		metaInfoString = metaInfoString
				.substring(getType().toString().length());

	}

	public void setRed(int red) {
		this.red = red;
		fireEvents(new RuleChangedEvent("Red channel selection changed", this));
	}

	/**
	 * 1-based
	 */
	public int getRed() {
		return red;
	}

	public void setGreen(int green) {
		this.green = green;
		fireEvents(new RuleChangedEvent("Green channel selection changed", this));

	}

	/**
	 * 1-based
	 */
	public int getGreen() {
		return green;
	}

	public void setBlue(int blue) {
		this.blue = blue;
		fireEvents(new RuleChangedEvent("Blue channel selection changed", this));
	}

	/**
	 * 1-based
	 */
	public int getBlue() {
		return blue;
	}

}
