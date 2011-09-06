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
import org.opengis.filter.expression.Expression;
import org.opengis.style.ChannelSelection;
import org.opengis.style.ContrastMethod;
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

	private int[] channels = new int[3];
	private ContrastMethod[] channelMethod = new ContrastMethod[3];
	private Expression[] gammaValue = new Expression[3];
	private ContrastMethod rsMethod = ContrastMethod.NONE;;
	private Expression rsGamma;

	public RasterRulesListRGB(StyledRasterInterface<?> styledRaster,
			boolean withDefaults) {
		super(RulesListType.RASTER_RGB, styledRaster);

		if (withDefaults) {
			channels[0] = 1;
			channels[1] = Math.min(2, getStyledRaster().getBandCount());
			channels[2] = Math.min(3, getStyledRaster().getBandCount());

			channelMethod[0] = ContrastMethod.NORMALIZE;
			channelMethod[1] = ContrastMethod.NORMALIZE;
			channelMethod[2] = ContrastMethod.NORMALIZE;
		}

	}

	@Override
	public void applyOpacity() {
		// Gibt nur eine Rule, wo die Opacity bei getRules gesetzt wird.
	}

	/**
	 * 1-based
	 * 
	 * red=1, green=2, blue=3
	 */
	public int getChannel(int channel) {
		return channels[channel - 1];
	}

	/**
	 * 1-based
	 * 
	 * @param channel
	 *            red=1, green=2, blue=3
	 */
	public void setChannel(int channel, int value) {
		this.channels[channel - 1] = value;
		fireEvents(new RuleChangedEvent("Channel selection changed", this));
	}

	/**
	 * 1-based
	 * 
	 * red=1, green=2, blue=3
	 */
	public ContrastMethod getChannelMethod(int channel) {
		return channelMethod[channel - 1];
	}

	/**
	 * 1-based
	 * 
	 * @param channel
	 *            red=1, green=2, blue=3
	 */
	public void setChannelMethod(int channel, ContrastMethod contrastMethod) {
		if (contrastMethod == null) // Yes they really do use null to indicate
									// no
									// ContrastMethod is set
			channelMethod[channel - 1] = ContrastMethod.NONE;
		else
			channelMethod[channel - 1] = contrastMethod;
		fireEvents(new RuleChangedEvent("Contrastmethod for channel changed",
				this));
	}

	/**
	 * 1-based
	 * 
	 * red=1, green=2, blue=3
	 */
	public Expression getGammaValue(int channel) {
		if (gammaValue[channel - 1] == null)
			return FilterUtil.FILTER_FAC2.literal(1.0); // gammaValue of null
														// means no gammaValue
														// set. Default is 1.0
		return gammaValue[channel - 1];
	}

	public void setGammaValue(int channel, Double value) {
		gammaValue[channel - 1] = ff.literal(value);
		fireEvents(new RuleChangedEvent("GammaValue for channel changed", this));
	}

	public Expression getRSGamma() {
		if (rsGamma == null)
			return FilterUtil.FILTER_FAC2.literal(1.0);
		return rsGamma;
	}

	public ContrastMethod getRSMethod() {
		return rsMethod;
	}

	public void setRSGamma(Double value) {
		rsGamma = ff.literal(value);
		fireEvents(new RuleChangedEvent(
				"GammaValue for RasterSymbolizer changed", this));
	}

	public void setRSMethod(ContrastMethod contrastMethod) {
		if (contrastMethod == null) // Yes they really do use null to indicate
									// no ContrastMethod is set
			rsMethod = ContrastMethod.NONE;
		else
			rsMethod = contrastMethod;
		fireEvents(new RuleChangedEvent(
				"Contrastmethod for RasterSymbolizer changed", this));
	}

	@Override
	public List<Rule> getRules() {
		RasterSymbolizer rs = StylingUtil.STYLE_BUILDER
				.createRasterSymbolizer();

		if (getOpacity() != null)
			rs.setOpacity(ff.literal(getOpacity()));

		ContrastEnhancement rsCe = StylingUtil.STYLE_FACTORY
				.createContrastEnhancement();
		// rsCe.setMethod(getRSMethod()); //does not work in geotools < 8.0
		if (getRSMethod() == null)
			rsCe.setType(FilterUtil.FILTER_FAC2.literal("NONE"));
		else
			rsCe.setType(FilterUtil.FILTER_FAC2.literal(getRSMethod().name()));
		rsCe.setGammaValue(getRSGamma());

		ContrastEnhancement redCe = StylingUtil.STYLE_FACTORY
				.createContrastEnhancement();
		redCe.setMethod(getChannelMethod(1));
		redCe.setGammaValue(getGammaValue(1));
		SelectedChannelType redT = StylingUtil.STYLE_FACTORY
				.createSelectedChannelType(String.valueOf(getChannel(1)), redCe);

		ContrastEnhancement greenCe = StylingUtil.STYLE_FACTORY
				.createContrastEnhancement();
		greenCe.setMethod(getChannelMethod(2));
		greenCe.setGammaValue(getGammaValue(2));
		SelectedChannelType greenT = StylingUtil.STYLE_FACTORY
				.createSelectedChannelType(String.valueOf(getChannel(2)),
						greenCe);

		ContrastEnhancement blueCe = StylingUtil.STYLE_FACTORY
				.createContrastEnhancement();
		blueCe.setMethod(getChannelMethod(3));
		blueCe.setGammaValue(getGammaValue(3));
		SelectedChannelType blueT = StylingUtil.STYLE_FACTORY
				.createSelectedChannelType(String.valueOf(getChannel(3)),
						blueCe);

		ChannelSelection cs = StylingUtil.STYLE_FACTORY.channelSelection(redT,
				greenT, blueT);
		if (getChannelMethod(1) != null)
			((ContrastEnhancement) cs.getRGBChannels()[0]
					.getContrastEnhancement()).setType(ff
					.literal(getChannelMethod(1).name()));
		if (getChannelMethod(2) != null)
			((ContrastEnhancement) cs.getRGBChannels()[1]
					.getContrastEnhancement()).setType(ff
					.literal(getChannelMethod(2).name()));
		if (getChannelMethod(3) != null)
			((ContrastEnhancement) cs.getRGBChannels()[2]
					.getContrastEnhancement()).setType(ff
					.literal(getChannelMethod(3).name()));

		rs.setChannelSelection(cs);
		rs.setContrastEnhancement(rsCe);

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

					setRSMethod(rs.getContrastEnhancement().getMethod());

					if (rs.getContrastEnhancement().getGammaValue() == null)
						setRSGamma(1.0);
					else {
						setRSGamma(Double.valueOf(rs.getContrastEnhancement()
								.getGammaValue().toString()));
					}

					ChannelSelection cs = rs.getChannelSelection();
					if (cs == null)
						continue;

					try {
						SelectedChannelType[] rgbChannels = cs.getRGBChannels();
						setChannel(1, Integer.valueOf(rgbChannels[0]
								.getChannelName()));
						
						// null is returned when no method is specified
						if (rgbChannels[0].getContrastEnhancement().getMethod() != null) {
							setChannelMethod(
									1,
									ContrastMethod.valueOf(rgbChannels[0]
											.getContrastEnhancement()
											.getMethod().name().toString()));
						} else {
							setChannelMethod(1, ContrastMethod.NONE); 
						}
						
						if (rgbChannels[0].getContrastEnhancement()
								.getGammaValue() != null) {
							setGammaValue(
									1,
									Double.valueOf(rgbChannels[0]
											.getContrastEnhancement()
											.getGammaValue().toString()));
						} else
							setGammaValue(1, 1.0);

						setChannel(2, Integer.valueOf(rgbChannels[1]
								.getChannelName()));
						if (rgbChannels[1].getContrastEnhancement().getMethod() != null) {
							setChannelMethod(
									2,
									ContrastMethod.valueOf(rgbChannels[1]
											.getContrastEnhancement()
											.getMethod().name().toString()));
						} else {
							setChannelMethod(2, ContrastMethod.NONE);
						}
						if (rgbChannels[1].getContrastEnhancement()
								.getGammaValue() != null) {
							setGammaValue(
									2,
									Double.valueOf(rgbChannels[1]
											.getContrastEnhancement()
											.getGammaValue().toString()));
						} else
							setGammaValue(2, 1.0);

						setChannel(3, Integer.valueOf(rgbChannels[2]
								.getChannelName()));
						if (rgbChannels[2].getContrastEnhancement().getMethod() != null) {
							setChannelMethod(
									3,
									ContrastMethod.valueOf(rgbChannels[2]
											.getContrastEnhancement()
											.getMethod().name().toString()));
						} else {
							setChannelMethod(3, ContrastMethod.NONE);
						}
						if (rgbChannels[2].getContrastEnhancement()
								.getGammaValue() != null) {
							setGammaValue(
									3,
									Double.valueOf(rgbChannels[2]
											.getContrastEnhancement()
											.getGammaValue().toString()));
						} else
							setGammaValue(2, 1.0);

					} catch (Exception e) {
						log.error("RGB channels didn't contain 3 channels??", e);
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

}
