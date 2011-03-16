/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasStyler.rulesLists;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.QuantitiesRuleList;
import org.geopublishing.atlasStyler.RuleChangeListener;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geopublishing.atlasStyler.classification.CLASSIFICATION_METHOD;
import org.geotools.brewer.color.BrewerPalette;
import org.geotools.brewer.color.PaletteType;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Symbolizer;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;

import de.schmitzm.geotools.FilterUtil;
import de.schmitzm.geotools.GTUtil;
import de.schmitzm.geotools.data.amd.AttributeMetadataImpl;
import de.schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import de.schmitzm.geotools.styling.StyledFeaturesInterface;
import de.schmitzm.geotools.styling.StylingUtil;

/**
 * 
 * This abstract class represents a list of {@link Rule Rules} meant to style a
 * quantity with graduating colors.
 * 
 * @author Stefan A. Tzeggai
 * 
 */
public abstract class GraduatedColorRuleList extends QuantitiesRuleList<Double> {
	/** KEY-name for the KVPs in the meta information * */
	private static final String KVP_METHOD = "METHOD";
	/** KEY-name for the KVPs in the meta information * */
	private static final String KVP_PALTETTE = "PALETTE";

	private static final Logger LOGGER = Logger
			.getLogger(GraduatedColorRuleList.class);

	/**
	 * The {@link BrewerPalette} used in this {@link QuantitiesRuleList}
	 */
	private BrewerPalette brewerPalette = ASUtil.getPalettes(new PaletteType(
			true, false), -1)[0];

	/**
	 * This {@link RuleChangeListener} is added to the template in
	 * {@link #getTemplate()} and will propagate any template changes to this
	 * {@link GraduatedColorRuleList}
	 */
	private final RuleChangeListener listenToTemplateRLChangesAndPropageToGraduateColorsRL = new RuleChangeListener() {

		@Override
		public void changed(RuleChangedEvent e) {
			GraduatedColorRuleList.this.fireEvents(new RuleChangedEvent(
					"template changed", GraduatedColorRuleList.this));
		}
	};

	public GraduatedColorRuleList(RulesListType rulesListType,
			StyledFeaturesInterface<?> styledFeatures, GeometryForm geometryForm) {
		super(rulesListType, styledFeatures, geometryForm);
	}

	/**
	 * Together with {@link #parseMetaInfoString(String, FeatureTypeStyle)} this
	 * allows loading and saving the RL
	 */
	@Override
	public String getAtlasMetaInfoForFTSName() {
		String metaInfoString = getType().toString();

		metaInfoString = extendMetaInfoString(metaInfoString);

		metaInfoString += METAINFO_SEPERATOR_CHAR + KVP_METHOD
				+ METAINFO_KVP_EQUALS_CHAR + getMethod();

		metaInfoString += METAINFO_SEPERATOR_CHAR + KVP_PALTETTE
				+ METAINFO_KVP_EQUALS_CHAR + getBrewerPalette().getName();

		// LOGGER.debug("metainfo= " + metaInfoString);

		return metaInfoString;
	}

	public BrewerPalette getBrewerPalette() {

		/**
		 * For whatever reason, the brewerPalette.getPaletteSuitability() can be
		 * greater than brewerPalette.getMaxColors(). So we try that first.
		 */
		int maxColors = brewerPalette.getPaletteSuitability() != null ? brewerPalette
				.getPaletteSuitability().getMaxColors() : brewerPalette
				.getMaxColors();

		if (getNumClasses() > maxColors) {

			LOGGER.info("Reducing the Number of classes from "
					+ getNumClasses() + " to " + maxColors
					+ " because we don't have a betterPalette");
		}
		return brewerPalette;
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

		return super.getColors();
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
		if (amd1 != null && amd1.getNodataValues() != null) {
			for (Object ndValue : amd1.getNodataValues()) {
				ors.add(ff2.equals(ff2.property(attributeLocalName),
						ff2.literal(ndValue)));
			}
		}

		// Checking the normalization attribute for NODATA values
		String normalizerLocalName = getNormalizer_field_name();
		if (normalizerLocalName != null) {
			AttributeMetadataImpl amd2 = getStyledFeatures()
					.getAttributeMetaDataMap().get(normalizerLocalName);

			ors.add(ff2.isNull(ff2.property(normalizerLocalName)));

			// As we are dividing by this value, always add the zero also!
			ors.add(ff2.equals(ff2.property(normalizerLocalName),
					ff2.literal(0)));

			if (amd2 != null && amd2.getNodataValues() != null)
				for (Object ndValue : amd2.getNodataValues()) {
					ors.add(ff2.equals(ff2.property(normalizerLocalName),
							ff2.literal(ndValue)));
				}
		}

		Filter filter = FilterUtil.correctOrForValidation(ff2.or(ors));

		// The NODATA rule also need to be enabled/disabled accoring to the
		// general stat eof the RuleList
		filter = addAbstractRlSettings(filter);

		return filter;
	}

	@Override
	public List<Rule> getRules() {

		ArrayList<Double> classLimitsAsArray = getClassLimitsAsArrayList();

		if (classLimitsAsArray.size() == 1) {
			// Special case
			classLimitsAsArray.add(classLimitsAsArray.get(0));
		}

		if (classLimitsAsArray.size() == 0) {
			return new ArrayList<Rule>();
		}

		ArrayList<Rule> rules = new ArrayList<Rule>();

		Literal lowerRange = null;
		Literal upperRange = null;
		Filter filter = null;

		/**
		 * One Rule for every Class:
		 */
		for (int i = 0; i < getNumClasses(); i++) {
			Rule rule;

			Expression value;
			if (getNormalizer_field_name() == null) {
				value = ff2.property(getValue_field_name());
				lowerRange = ff2.literal(classLimitsAsArray.get(i));
				upperRange = ff2.literal(classLimitsAsArray.get(i + 1));

			} else {
				value = ff2.divide(ff2.property(getValue_field_name()),
						ff2.property(getNormalizer_field_name()));

				// If we have a normalizing field, we always go for double
				lowerRange = ff2.literal(classLimitsAsArray.get(i));
				upperRange = ff2.literal(classLimitsAsArray.get(i + 1));
			}

			if (lowerRange.equals(upperRange)) {
				// If Upper and Lower are the same, use a PropertyEquals Filter
				// instead
				filter = ff2.equals(value, lowerRange);
			} else {
				// Normally use a between filter
				filter = ff2.between(value, lowerRange, upperRange);
			}

			SingleRuleList<? extends Symbolizer> clone = getTemplate().copy();

			final Color newColor = getColors()[i];

			clone.setColor(newColor);

			rule = clone.getRule();

			// Exclude the NODATA values for the rule
			if (getNoDataFilter() != Filter.EXCLUDE)
				filter = ff2.and(ff2.not(getNoDataFilter()), filter);

			// Add the general on/off switch as the last AND filter
			filter = addAbstractRlSettings(filter);
			rule.setFilter(filter);

			rule.setTitle(getRuleTitles().get(i));

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

				noDataRule.setFilter(getNoDataFilter());
				rules.add(noDataRule);
			}
		}

		return rules;
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

	/**
	 * Together with {@link #getAtlasMetaInfoForFTSName()} this allows loading
	 * and saving the RL
	 */
	@Override
	public void parseMetaInfoString(String metaInfoString,
			FeatureTypeStyle importFTS) {

		metaInfoString = metaInfoString
				.substring(getType().toString().length());

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

				setMethod(CLASSIFICATION_METHOD.valueOf(kvp[1]));

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

	@Override
	public void importRules(List<Rule> rules) {
		/***********************************************************
		 * Parsing information in the RULEs
		 * 
		 * title, class limits
		 */
		int countRules = 0;
		final TreeSet<Double> classLimits = new TreeSet<Double>();
		double[] ds = null;
		for (final Rule r : rules) {

			if (r.getName().toString()
					.startsWith(FeatureRuleList.NODATA_RULE_NAME)) {
				// This rule defines the NoDataSymbol
				this.importNoDataRule(r);
				continue;
			}

			// set Title
			this.getRuleTitles().put(countRules,
					GTUtil.descriptionTitle(r.getDescription()));

			// Class Limits
			Filter filter = r.getFilter();

			// Reving and preceeding Enabled/Disabled filter
			filter = parseAbstractRlSettings(filter);

			ds = interpretBetweenFilter(filter);
			classLimits.add(ds[0]);

			countRules++;
		}
		if (ds != null) {
			// The last limit is only added if there have been
			// any rules
			classLimits.add(ds[1]);
		}
		setClassLimits(classLimits, false);

		/**
		 * Now determine the colors stored inside the symbolizers.
		 */
		for (int ri = 0; ri < countRules; ri++) {
			// Import the dominant color from the symbolizers
			// (they can differ from the palette colors, because
			// they might have been changed manually.
			for (final Symbolizer s : rules.get(ri).getSymbolizers()) {

				final Color c = StylingUtil.getSymbolizerColor(s);

				if (c != null) {
					// LOGGER.debug("Rule " + ri + " has color " + c);
					this.getColors()[ri] = c;
					break;
				}
			}

		}
	}

}
