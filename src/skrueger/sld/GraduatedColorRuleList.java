/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package skrueger.sld;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.geotools.brewer.color.BrewerPalette;
import org.geotools.brewer.color.PaletteType;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Symbolizer;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;

import skrueger.geotools.StyledFeaturesInterface;
import skrueger.sld.classification.QuantitiesClassification;
import skrueger.sld.classification.QuantitiesClassification.METHOD;

/**
 * 
 * This abstract class represents a list of {@link Rule Rules} meant to style a
 * quantity with graduating colors.
 * 
 * @author Stefan A. Krüger
 * 
 */
public abstract class GraduatedColorRuleList extends QuantitiesRuleList<Double> {
	private static final Logger LOGGER = Logger
			.getLogger(GraduatedColorRuleList.class);
	/** KEY-name for the KVPs in the meta information * */
	private static final String KVP_METHOD = "METHOD";

	/** KEY-name for the KVPs in the meta information * */
	private static final String KVP_PALTETTE = "PALETTE";

	// /** KEY-name for the KVPs in the meta information * */
	// private static final String KVP_EXCLUDE_FILTER = "EXCLUDE";

	/**
	 * The {@link BrewerPalette} used in this {@link QuantitiesRuleList}
	 */
	private BrewerPalette brewerPalette = ASUtil.getPalettes(new PaletteType(true,false),-1)[0];
//	{
//		// Setting a default palette
//		try {
//			brewerPalette = ColorBrewer.instance(ColorBrewer.QUALITATIVE)
//					.getPalettes()[0];
//		} catch (IOException e) {
//			throw new RuntimeException(
//					"Colorbrewer palettes are not available.");
//		}
//	}

	protected METHOD method = QuantitiesClassification.DEFAULT_METHOD;

	public GraduatedColorRuleList(StyledFeaturesInterface<?> styledFeatures) {
		super(styledFeatures);
	}

	public METHOD getMethod() {
		return method;
	}

	@Override
	public List<Rule> getRules() {

		ArrayList<Double> classLimitsAsArray = getClassLimitsAsArrayList();

		if (classLimitsAsArray.size() == 0) {
			return new ArrayList<Rule>();
		}

		ArrayList<Rule> rules = new ArrayList<Rule>();

		Literal lowerRange = null;
		Literal upperRange = null;
		Filter filter = null;

		for (int i = 0; i < getNumClasses(); i++) {
			Rule rule;

			Expression value;
			if (getNormalizer_field_name() == null) {
				value = ff2.property(getValue_field_name());
				lowerRange = ff2.literal(classLimitsAsArray.get(i));
				upperRange = ff2.literal(classLimitsAsArray.get(i + 1));

			} else {
				value = ff2.divide(ff2.property(getValue_field_name()), ff2
						.property(getNormalizer_field_name()));

				// If we have a normalizing field, we always go for double
				lowerRange = ff2.literal(classLimitsAsArray.get(i));
				upperRange = ff2.literal(classLimitsAsArray.get(i + 1));
			}

			filter = ff2.between(value, lowerRange, upperRange);

			SingleRuleList<? extends Symbolizer> clone = getTemplate().copy();

			final Color newColor = getColors()[i];
			
//			LOGGER.debug("Color = " + newColor);
			
			clone.setColor(newColor);

			rule = clone.getRules().get(0);

			rule.setFilter(filter);

			rule.setTitle((String) getRuleTitles().get(i));

			rule.setName("AS: " + (i + 1) + "/" + (getClassLimits().size() - 1)
					+ " " + this.getClass().getSimpleName());

			rules.add(rule);

		}

		return rules;
	}

	public BrewerPalette getBrewerPalette() {

		/**
		 * For whatever reason, the brewerPalette.getPaletteSuitability() can be
		 * greater than brewerPalette.getMaxColors(). So we try that first.
		 */
		int maxColors = brewerPalette.getPaletteSuitability() != null ? brewerPalette
				.getPaletteSuitability().getMaxColors()
				: brewerPalette.getMaxColors();

		if (getNumClasses() > maxColors) {

			LOGGER.info("Reducing the Number of classes from "
					+ getNumClasses() + " to " + maxColors
					+ " because we don't have a betterPalette");
		}

		return brewerPalette;
	}

	public void setBrewerPalette(BrewerPalette newPalette) {

		// Only react to real changes
		if (newPalette.getDescription() != null
				&& newPalette.getDescription().equals(
						brewerPalette.getDescription())) {
			return;
		}

		this.brewerPalette = newPalette;
		setColors(null);
		fireEvents(new RuleChangedEvent("Set brewer palette", this));
	}

	/**
	 * @return An Array of Colors for the classes. The length of the array
	 *         equals the number of classes.
	 */
	@Override
	public Color[] getColors() {

		if (super.getColors() == null) {
			if (getNumClasses() > getBrewerPalette().getMaxColors()) {
				throw new RuntimeException(
						" numClasses ("+getNumClasses()+") > getBrewerPalette().getMaxColors() ("+getBrewerPalette().getMaxColors()+")");
			}

			setColors(getBrewerPalette().getColors(getNumClasses()));
		}

		final Color[] colors = super.getColors();
		return colors;
	}

	public void setMethod(METHOD method) {
		this.method = method;
	}

	/**
	 * Together with {@link #parseMetaInfoString(String, FeatureTypeStyle)} this
	 * allows loading and saving the RL
	 */
	@Override
	public String getAtlasMetaInfoForFTSName() {
		String metaInfoString = getTypeID().toString();

		metaInfoString = extendMetaInfoString(metaInfoString);

		metaInfoString += METAINFO_SEPERATOR_CHAR + KVP_METHOD
				+ METAINFO_KVP_EQUALS_CHAR + getMethod();

		metaInfoString += METAINFO_SEPERATOR_CHAR + KVP_PALTETTE
				+ METAINFO_KVP_EQUALS_CHAR + getBrewerPalette().getName();
		//
		// metaInfoString += METAINFO_SEPERATOR_CHAR + KVP_EXCLUDE_FILTER
		// + METAINFO_KVP_EQUALS_CHAR + getExcludeFilterRule();

//		LOGGER.debug("metainfo= " + metaInfoString);

		return metaInfoString;
	}

	/**
	 * Together with {@link #getAtlasMetaInfoForFTSName()} this allows loading
	 * and saving the RL
	 */
	@Override
	public void parseMetaInfoString(String metaInfoString,
			FeatureTypeStyle importFTS) {

		metaInfoString = metaInfoString.substring(getTypeID().toString()
				.length());

		super.parseMetaInfoString(metaInfoString);

		/***********************************************************************
		 * Parsing a list of Key-Value Pairs from the FeatureTypeStyleName
		 */
		String[] params = metaInfoString.split(METAINFO_SEPERATOR_CHAR);
		for (String p : params) {
			String[] kvp = p.split(METAINFO_KVP_EQUALS_CHAR);

			if (kvp[0].equalsIgnoreCase(KVP_METHOD.toString())) {

				// We had a typo error in AtlasStyler 1.1 - to correctly import
				// old styled, we have to correct it here:
				if (kvp[1].equals("QANTILES"))
					kvp[1] = "QUANTILES";

				setMethod(METHOD.valueOf(kvp[1]));

			}

			else

			if (kvp[0].equalsIgnoreCase(KVP_PALTETTE)) {
				String brewerPaletteName = kvp[1];

				BrewerPalette foundIt = null;

				for (BrewerPalette ppp : ASUtil.getPalettes(new PaletteType(true,false), getNumClasses())) {
					if (ppp.getName().equals(brewerPaletteName)) {
						foundIt = ppp;
						break;
					}
				}
				if (foundIt == null) {
					LOGGER.warn("Couldn't find the palette with the name '"
							+ brewerPaletteName + "'.");
				} else {
					setBrewerPalette(foundIt);
				}
			}

			// else
			//
			// if (kvp[0].equalsIgnoreCase(KVP_EXCLUDE_FILTER)) {
			// // FeatureOperationTreeFilter exFilter = new
			// // FeatureOperationTreeFilter(
			// // kvp[1]);
			// setExcludeFilterRule(kvp[1]);
			// }

		}

		/***********************************************************************
		 * Determining the template by analyzing the first rule
		 */
		importTemplate(importFTS);

		/***********************************************************************
		 * TODO import anything ?limits from rules...
		 */

	}

}
