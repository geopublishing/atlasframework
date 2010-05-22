/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasStyler;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.classification.QuantitiesClassification;
import org.geopublishing.atlasStyler.classification.QuantitiesClassification.METHOD;
import org.geotools.brewer.color.BrewerPalette;
import org.geotools.brewer.color.PaletteType;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Symbolizer;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;

import skrueger.AttributeMetadataImpl;
import skrueger.geotools.StyledFeaturesInterface;

/**
 * 
 * This abstract class represents a list of {@link Rule Rules} meant to style a
 * quantity with graduating colors.
 * 
 * @author Stefan A. Tzeggai
 * 
 */
public abstract class GraduatedColorRuleList extends QuantitiesRuleList<Double> {
	private static final Logger LOGGER = Logger
			.getLogger(GraduatedColorRuleList.class);
	/** KEY-name for the KVPs in the meta information * */
	private static final String KVP_METHOD = "METHOD";

	/** KEY-name for the KVPs in the meta information * */
	private static final String KVP_PALTETTE = "PALETTE";

	/**
	 * The {@link BrewerPalette} used in this {@link QuantitiesRuleList}
	 */
	private BrewerPalette brewerPalette = ASUtil.getPalettes(new PaletteType(
			true, false), -1)[0];

	protected METHOD method = QuantitiesClassification.DEFAULT_METHOD;

	/**
	 * This {@link RuleChangeListener} is added to the template in
	 * {@link #getTemplate()} and will propagate any template changes to this
	 * {@link GraduatedColorRuleList}
	 */
	private RuleChangeListener listenToTemplateRLChangesAndPropageToGraduateColorsRL = new RuleChangeListener() {

		@Override
		public void changed(RuleChangedEvent e) {
			GraduatedColorRuleList.this.fireEvents(new RuleChangedEvent(
					"template changed", GraduatedColorRuleList.this));
		}
	};

	public GraduatedColorRuleList(StyledFeaturesInterface<?> styledFeatures) {
		super(styledFeatures);
	}

	public METHOD getMethod() {
		return method;
	}

	/**
	 * Overriding it to add a listener that will automatically propagate the
	 * rulelistechange to the graduate color rule list.
	 */
	@Override
	public SingleRuleList<? extends Symbolizer> getTemplate() {
		SingleRuleList<? extends Symbolizer> temp = super.getTemplate();

		// We may add this listener as often as we want, because the listeners
		// are registered in a WeakHashSet
		temp.addListener(listenToTemplateRLChangesAndPropageToGraduateColorsRL);
		return temp;
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

			clone.setColor(newColor);

			rule = clone.getRule();

			// Exclude the NODATA values for the rule
			if (getNoDataFilter() != Filter.EXCLUDE)
				filter = ff2.and(ff2.not(getNoDataFilter()), filter);

			rule.setFilter(filter);

			rule.setTitle((String) getRuleTitles().get(i));

			rule.setName("AS: " + (i + 1) + "/" + (getClassLimits().size() - 1)
					+ " " + this.getClass().getSimpleName());

			rules.add(rule);

		}

		// The last rule(s) are the NODATA rules

		{
			SingleRuleList<? extends Symbolizer> copyOfNoDataSymbol = getNoDataSymbol()
					.copy();
			List<Rule> rules2 = copyOfNoDataSymbol.getRules();
			for (Rule noDataRule : rules2) {

				if (copyOfNoDataSymbol.isVisibleInLegend())
					noDataRule
							.setName(FeatureRuleList.NODATA_RULE_NAME_SHOWINLEGEND);
				else
					noDataRule
							.setName(FeatureRuleList.NODATA_RULE_NAME_HIDEINLEGEND);

				// Use the min/max scale denominators from the template alsofor
				// the nodata values.. no matter what it's template has
				noDataRule.setMinScaleDenominator(getTemplate()
						.getMinScaleDenominator());
				noDataRule.setMaxScaleDenominator(getTemplate()
						.getMaxScaleDenominator());

				noDataRule.setFilter(getNoDataFilter());
				rules.add(noDataRule);
			}
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
				throw new RuntimeException(" numClasses (" + getNumClasses()
						+ ") > getBrewerPalette().getMaxColors() ("
						+ getBrewerPalette().getMaxColors() + ")");
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

		// LOGGER.debug("metainfo= " + metaInfoString);

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

				for (BrewerPalette ppp : ASUtil.getPalettes(new PaletteType(
						true, false), getNumClasses())) {
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

		}

		/***********************************************************************
		 * Determining the template by analyzing the first rule
		 */
		importTemplate(importFTS);

	}

	/**
	 * Return the {@link Filter} that will catch all NODATA values.
	 */
	@Override
	public Filter getNoDataFilter() {

		// Checking the value attribute for NODATA values
		String attributeLocalName = getValue_field_name();
		AttributeMetadataImpl amd1 = getStyledFeatures()
				.getAttributeMetaDataMap().get(attributeLocalName);

		List<Filter> ors = new ArrayList<Filter>();
		ors.add(ff2.isNull(ff2.property(attributeLocalName)));
		if (amd1 != null && amd1.getNodataValues() != null)
			for (Object ndValue : amd1.getNodataValues()) {
				ors.add(ff2.equals(ff2.property(attributeLocalName), ff2
						.literal(ndValue)));
			}

		// Checking the normalization attribute for NODATA values
		String normalizerLocalName = getNormalizer_field_name();
		if (normalizerLocalName != null) {
			AttributeMetadataImpl amd2 = getStyledFeatures()
					.getAttributeMetaDataMap().get(normalizerLocalName);

			ors.add(ff2.isNull(ff2.property(normalizerLocalName)));

			// As we are dividing by this value, always add the zero also!
			ors.add(ff2.equals(ff2.property(normalizerLocalName), ff2
					.literal(0)));

			if (amd2 != null && amd2.getNodataValues() != null)
				for (Object ndValue : amd2.getNodataValues()) {
					ors.add(ff2.equals(ff2.property(normalizerLocalName), ff2
							.literal(ndValue)));
				}
		}

		return ff2.or(ors);
	}

}
