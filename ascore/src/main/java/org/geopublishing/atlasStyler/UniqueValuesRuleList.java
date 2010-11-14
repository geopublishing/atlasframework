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
package org.geopublishing.atlasStyler;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.geotools.brewer.color.BrewerPalette;
import org.geotools.brewer.color.ColorBrewer;
import org.geotools.feature.visitor.UniqueVisitor;
import org.geotools.filter.AndImpl;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.StyleAttributeExtractor;
import org.geotools.styling.Symbolizer;
import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;

import schmitzm.geotools.FilterUtil;
import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import schmitzm.lang.LangUtil;
import schmitzm.swing.SwingUtil;
import skrueger.AttributeMetadataImpl;
import skrueger.geotools.StyledFeaturesInterface;

public abstract class UniqueValuesRuleList extends FeatureRuleList {
	/**
	 * Special unique value. When exporting by {@link #getRules()}, this value
	 * is translated to the special "all others" rule *
	 */
	public static final String ALLOTHERS_IDENTIFICATION_VALUE = "ALLOTHERS_RULE_ID";

	private static final Logger LOGGER = Logger
			.getLogger(UniqueValuesRuleList.class);

	/**
	 * The Filter contains information about: unique value; ValueFieldName,
	 * symbolizer, onOff
	 * 
	 * @param filter
	 *            {@link Filter} to interpret
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 * 
	 * @return String[] with propertyName, uniqueValue
	 */
	public static String[] interpretFilter(final Filter filter)
			throws RuntimeException {

		if (filter instanceof PropertyIsEqualTo) {
			// TODO Probably old unused code...
			final PropertyIsEqualTo propertEqualTo = (PropertyIsEqualTo) filter;
			final PropertyName pName = (PropertyName) propertEqualTo
					.getExpression1();
			final Literal value = (Literal) propertEqualTo.getExpression2();

			return new String[] { pName.getPropertyName(), value.toString() };
		} else if (filter instanceof Not) {
			// This must be the default/all others rule...
			final Not not = (Not) filter;
			final Or or = (Or) not.getFilter();

			StyleAttributeExtractor faex = new StyleAttributeExtractor();
			Set attbibs = (Set) faex.visit(or, null);
			String attName = (String) attbibs.iterator().next();

			return new String[] { attName, ALLOTHERS_IDENTIFICATION_VALUE };
		} else if (filter instanceof And) {
			// AND (NOT NULL, PROP=VALUE)
			And and = (AndImpl) filter;
			for (Filter f : and.getChildren()) {
				// Look for the Filter that has a PropertyIsEqualTo
				if (f instanceof PropertyIsEqualTo) {
					final PropertyIsEqualTo propertEqualTo = (PropertyIsEqualTo) f;
					final PropertyName pName = (PropertyName) propertEqualTo
							.getExpression1();
					final Literal value = (Literal) propertEqualTo
							.getExpression2();

					return new String[] { pName.getPropertyName(),
							value.toString() };
				}
			}
		}

		throw new RuntimeException("Filter not recognized.");
	}

	Integer countNew = 0;

	private final ArrayList<String> labels = new ArrayList<String>();

	private double maxScaleDenominator = Double.MAX_VALUE;

	private BrewerPalette palette = ColorBrewer.instance().getPalettes()[0];

	private String propertyFieldName = null;

	private List<SingleRuleList<? extends Symbolizer>> symbols = new ArrayList<SingleRuleList<? extends Symbolizer>>();

	/** A List of the unique values **/
	private final ArrayList<Object> values = new ArrayList<Object>();

	{
		try {
			palette = ColorBrewer.instance(ColorBrewer.SUITABLE_UNIQUE)
					.getPalettes()[0];
		} catch (final IOException e) {
			LOGGER.error(e);
			palette = ColorBrewer.instance().getPalettes()[0];
		}
	}

	public UniqueValuesRuleList(StyledFeaturesInterface<?> styledFeatures,
			GeometryForm geometryForm) {
		super(styledFeatures, geometryForm);
	}

	/**
	 * Adds all missing unique values to the list of rules.
	 * 
	 * @return the number of newly added unique values
	 */
	public int addAllValues(SwingWorker<?, ?> sw) {
		if (getPropertyFieldName() == null)
			return 0;

		countNew = 0;
		pushQuite();
		for (final Object uniqueString : getAllUniqueValuesThatAreNotYetIncluded()) {
			if (sw != null && sw.isCancelled())
				return 0;
			addUniqueValue(uniqueString);
			countNew++;
		}

		/** Fire an event * */
		if (countNew > 0)
			popQuite(new RuleChangedEvent("Added " + countNew + " values.",
					UniqueValuesRuleList.this));
		else
			popQuite();

		return countNew;
	}

	/**
	 * @param uniqueValue
	 *            Unique value to all to the list.
	 * 
	 * @return <code>false</code> is the value already exists
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public boolean addUniqueValue(final Object uniqueValue)
			throws IllegalArgumentException {

		if (getValues().contains(uniqueValue)) {
			LOGGER.warn("The unique Value '" + uniqueValue
					+ "' can't be added, it is allready in the list");
			return false;
		}

		// Just to ensure internal data structure integrity
		test();

		getSymbols().add(getTemplate().copy());
		getLabels().add(uniqueValue.toString());
		getValues().add(uniqueValue);

		fireEvents(new RuleChangedEvent("Added value: '" + uniqueValue + "'",
				this));

		// Just to ensure internal data structure integrity
		test();

		return true;
	}

	/**
	 * Apply the ColorPalette to all symbols at once
	 * 
	 * @param componentForGui
	 *            If not <code>null</code>, a message may appear
	 * 
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public void applyPalette(final JComponent componentForGui) {
		pushQuite();

		boolean warnedOnce = false;

		final Color[] colors = getBrewerPalette().getColors();

		for (int i = 0; i < getValues().size(); i++) {

			int idx = i;
			while (idx >= getBrewerPalette().getMaxColors()) {
				idx -= getBrewerPalette().getMaxColors();
				if ((componentForGui != null) && (!warnedOnce)) {

					final String msg = AtlasStyler
							.R("UniqueValuesGUI.WarningDialog.more_classes_than_colors.msg",
									getBrewerPalette().getMaxColors(),
									getValues().size());
					JOptionPane
							.showMessageDialog(SwingUtil
									.getParentWindowComponent(componentForGui),
									msg);
					warnedOnce = true;
				}
			}

			getSymbols().get(i).setColor(colors[idx]);
		}

		popQuite(new RuleChangedEvent("Applied a COLORPALETTE to all rules",
				this));
	}

	/**
	 * Applies the Template to ALL values
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public void applyTemplate() {
		applyTemplate(getValues());
	}

	/**
	 * Applies the template to the given ist of categories.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 * 
	 * @param values
	 *            List of {@link String} values that shall be affected by this.
	 */
	public void applyTemplate(final List<Object> values) {
		pushQuite();

		for (final Object catString : values) {

			final int idx = getValues().indexOf(catString);
			final SingleRuleList oldSymbol = getSymbols().get(idx);
			final Color oldColor = oldSymbol.getColor();
			final SingleRuleList<? extends Symbolizer> newSymbol = getTemplate()
					.copy();
			newSymbol.setColor(oldColor);
			getSymbols().remove(idx);
			getSymbols().add(idx, newSymbol);
		}

		popQuite(new RuleChangedEvent("Applied the template to "
				+ values.size() + " rules.", this));
	}

	/**
	 * @return the position of the "all others" rule in the rules. 0 is first
	 *         rule.
	 */
	public int getAllOthersRuleIdx() {
		// Just to ensure internal data structure integrity
		test();
		return getValues().indexOf(ALLOTHERS_IDENTIFICATION_VALUE);
	}

	/**
	 * Returns a {@link Set} not yet included in any of the rule lists.
	 */
	public Set<Object> getAllUniqueValuesThatAreNotYetIncluded() {

		SwingUtil.checkNotOnEDT();

		final UniqueVisitor uniqueVisitor = new UniqueVisitor(
				getPropertyFieldName());

		try {
			getStyledFeatures().getFeatureCollectionFiltered().accepts(
					uniqueVisitor, null);
			final Set<Object> vals = uniqueVisitor.getResult().toSet();

			// now filter the null values and the ones that are already part of
			// the
			// list
			final Set<Object> uniques = new TreeSet<Object>();
			for (final Object o : vals) {
				if (o == null)
					continue;
				if (values.contains(o))
					continue;
				uniques.add(o);
			}

			return uniques;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	@Override
	public String getAtlasMetaInfoForFTSName() {
		final String metaInfoString = getType().toString();

		return metaInfoString;
	}

	public BrewerPalette getBrewerPalette() {
		return palette;
	}

	/**
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public ArrayList<String> getLabels() {
		return labels;
	}

	/**
	 * Return the {@link Filter} that will catch all NODATA values.
	 */
	@Override
	public Filter getNoDataFilter() {
		String attributeLocalName = getPropertyFieldName();
		AttributeMetadataImpl amd = getStyledFeatures()
				.getAttributeMetaDataMap().get(attributeLocalName);

		List<Filter> ors = new ArrayList<Filter>();
		ors.add(ff2.isNull(ff2.property(attributeLocalName)));

		if (amd.getNodataValues() != null)
			for (Object ndValue : amd.getNodataValues()) {
				ors.add(ff2.equals(ff2.property(attributeLocalName),
						ff2.literal(ndValue)));
			}

		return FilterUtil.correctOrForValidation(ff2.or(ors));
	}

	/**
	 * Return a {@link SingleRuleList} that shall be used to paint all NODATA
	 * values. If <code>null</code>, then all features matching the
	 * {@link #getNoDataFilter()} shall not be painted at all.
	 */
	@Override
	public SingleRuleList<? extends Symbolizer> getNoDataSymbol() {
		return null; // TODO nice default?!
	}

	public int getNumClasses() {
		return getValues().size();
	}

	/**
	 * Returns the name of the field that provides the labeling attribute.
	 * Returns the first usable attribute if non has been selected before.
	 */
	public String getPropertyFieldName() {
		if (propertyFieldName == null) {
			final List<String> valueFieldNames = FeatureUtil
					.getValueFieldNames(getStyledFeatures().getSchema());
			if (valueFieldNames.size() > 0)
				propertyFieldName = valueFieldNames.get(0);
			else
				throw new RuntimeException(
						"Not possible for featureTypes without attributes");
		}
		return propertyFieldName;
	}

	/**
	 * Represents this {@link UniqueValuesRuleList} as an array of OGC
	 * {@link Rule} elements. If the "all others" rule is activated, this method
	 * doesn't change a lot. The "all others" is a normal rule with // *
	 * #ALLOTHERS_IDENTIFICATION_VALUE
	 */
	@Override
	public List<Rule> getRules() {

		// Just to ensure internal data structure integrity
		test();

		List<Rule> rules = new ArrayList<Rule>();
		int ruleCount = 0;

		final PropertyName propertyName = ASUtil.ff2
				.property(getPropertyFieldName());

		for (final Object uniqueValue : getValues()) {
			final Literal value = ASUtil.ff2.literal(uniqueValue);
			Filter filter = null;

			/*******************************************************************
			 * Checking for the special case, where the value == ALLOTHERS_RULE
			 */
			if (uniqueValue.equals(ALLOTHERS_IDENTIFICATION_VALUE)) {
				final List<Filter> filters = new ArrayList<Filter>();

				for (final Object uV : getValues()) {
					if (!uV.equals(ALLOTHERS_IDENTIFICATION_VALUE)) {
						filter = ASUtil.ff2.equals(propertyName,
								ASUtil.ff2.literal(uV));

						filters.add(filter);
					}
				}
				// Exclude the NODATA values
				filters.add(getNoDataFilter());

				if (filters.size() == 0) {
					/**
					 * If no other rules are defined, this HAS BEEN the output:
					 * <code>
					 <sld:Rule>
					 <sld:Title>others</sld:Title>
					 <ogc:Filter>
					 <ogc:Not>
					 <ogc:Or>
					 <ogc:PropertyIsEqualTo>
					 <ogc:Literal>1</ogc:Literal>
					 <ogc:Literal>2</ogc:Literal>
					 </ogc:PropertyIsEqualTo>
					 </ogc:Or>
					 </ogc:Not>
					 </ogc:Filter>
					 </code>
					 */
					// filters.add(Utilities.allwaysFalseFilter);
					// TODO, what are we doing there?
					LOGGER.warn("What is going on here?");
					filter = ASUtil.ff2.equals(propertyName,
							ASUtil.ff2.literal("-9090909090"));
					filters.add(filter);
				}
				// LOGGER.debug("filters size = "+filters.size());

				final Or allor = FilterUtil.correctOrForValidation(ASUtil.ff2
						.or(filters));

				filter = ASUtil.ff2.not(allor);
				/**
				 * If we have at least one other filter, it look like this:
				 * <code>
				 <ogc:Filter>
				 <ogc:Not>
				 <ogc:Or>
				 <ogc:PropertyIsEqualTo>
				 <ogc:PropertyName>Surface</ogc:PropertyName>
				 <ogc:Literal>2</ogc:Literal>
				 </ogc:PropertyIsEqualTo>
				 </ogc:Or>
				 </ogc:Not>
				 </ogc:Filter>
				 * </code>
				 * 
				 */
			} else {
				filter = ASUtil.ff2.equals(propertyName, value);
				// Exclude the NODATA values
				filter = ff2.and(ff2.not(getNoDataFilter()), filter);
			}

			/**
			 * Turning arround the order of the symbolizers. TODO We had a
			 * method for this somewhere, eh?
			 */
			final SingleRuleList symbolRL = getSymbols().get(ruleCount);
			final Symbolizer[] symbolizersArrayWrongOrder = (Symbolizer[]) symbolRL
					.getSymbolizers().toArray(
							new Symbolizer[symbolRL.getSymbolizers().size()]);

			final Symbolizer[] symbolizersArray = new Symbolizer[symbolizersArrayWrongOrder.length];

			for (int i = 0; i < symbolizersArrayWrongOrder.length; i++) {
				symbolizersArray[symbolizersArrayWrongOrder.length - i - 1] = symbolizersArrayWrongOrder[i];
			}

			Rule rule = ASUtil.SB.createRule(symbolizersArray);
			// rules[ruleCount].setSymbolizers(symbolizersArray);

			rule.setMaxScaleDenominator(maxScaleDenominator);

			rule.setTitle(getLabels().get(ruleCount));

			// Adding the filter to the Rule.
			filter = addAbstractRlSettings(filter);
			rule.setFilter(filter);

			rules.add(rule);
			ruleCount++;
		}

		return rules;
	}

	public List<SingleRuleList<? extends Symbolizer>> getSymbols() {
		return symbols;
	}

	public ArrayList<Object> getValues() {
		return values;
	}

	public boolean isWithDefaultSymbol() {
		return getValues().contains(ALLOTHERS_IDENTIFICATION_VALUE);
	}

	@Override
	public void parseMetaInfoString(final String metaInfoString,
			final FeatureTypeStyle fts) {
	}

	/**
	 * Removes a single value
	 * 
	 * @param value
	 *            Removes this value
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	private void removeValue(final Object value) {

		// Just to ensure internal data structure integrity
		test();

		final int idx = getValues().indexOf(value);

		if (idx >= 0) {
			getValues().remove(idx);
			getLabels().remove(idx);
			getSymbols().remove(idx);
		} else {
			LOGGER.warn("Asked to remove a value that doesn't exist !?: '"
					+ value + "'");
		}

		// Just to ensure internal data structure integrity
		test();

		fireEvents(new RuleChangedEvent("Removed value " + value, this));
		// LOGGER.debug("remove = "+strVal+" size left = "+getValues().size());
	}

	/**
	 * Remove the given list of {@link String} values.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public void removeValues(final List<Object> values) {
		pushQuite();

		try {

			// System.out.println(values);

			for (final Object strVal : values.toArray(new Object[] {})) {
				removeValue(strVal);
			}

		} finally {
			popQuite(new RuleChangedEvent(values.size()
					+ " categories have been removed", this));
		}
	}

	public void setBrewerPalette(final BrewerPalette palette) {
		this.palette = palette;
	}

	public void setMaxScaleDenominator(double maxScaleDenominator) {
		this.maxScaleDenominator = maxScaleDenominator;
	}

	/**
	 * Set the first field used for the categorization.
	 * 
	 * @param newName
	 *            Give the name directly as a {@link PropertyName}
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public void setPropertyFieldName(final String newName,
			final boolean removeOldClasses) {

		// We may not use the Getter here!.. It generates content :-(
		if (propertyFieldName != null)
			if (propertyFieldName.equals(newName))
				return;

		propertyFieldName = newName;

		if (removeOldClasses) {
			for (int i = 0; i < values.size(); i++) {
				if (values.get(i) != ALLOTHERS_IDENTIFICATION_VALUE) {
					values.remove(i);
					symbols.remove(i);
					labels.remove(i);
				}
			}
		}

		fireEvents(new RuleChangedEvent("The PropertyField changed to "
				+ getPropertyFieldName() + ". Other rules have been removed.",
				this));

		// Just to ensure internal data structure integrity
		test();
	}

	public void setSymbols(
			final List<SingleRuleList<? extends Symbolizer>> symbols) {
		this.symbols = symbols;
	}

	public void addDefaultRule() {
		setDefaultRuleEnabled(true);
	}

	public void removeDefaultRule() {
		setDefaultRuleEnabled(false);
	}

	public void setDefaultRuleEnabled(final boolean withDefaultSymbol) {

		pushQuite();

		try {

			/***********************************************************************
			 * It has been activated.. create a default rule and insert it at
			 * the end
			 */
			if ((withDefaultSymbol == true)
					&& (!getValues().contains(ALLOTHERS_IDENTIFICATION_VALUE))) {

				getValues().add(ALLOTHERS_IDENTIFICATION_VALUE);
				getSymbols().add(getTemplate().copy());
				getLabels()
						.add(AtlasStyler.R("UniqueValuesGUI.AllOthersLabel"));

			} else
			/***********************************************************************
			 * It has been disabled.. remove the default rule
			 */
			{
				removeValue(ALLOTHERS_IDENTIFICATION_VALUE);

			}

		} finally {
			popQuite(new RuleChangedEvent("WithDefaultRule set to "
					+ withDefaultSymbol, this));
		}
	}

	/**
	 * A helper method to debug and ensure internal data structure integrity.
	 */
	void test() {
		// A Test!
		if (getSymbols().size() - getLabels().size() + getValues().size() != getSymbols()
				.size()) {
			System.out.println("Symbols : "
					+ LangUtil.stringConcatWithSep(" ", getSymbols()));
			System.out.println("Labels:   "
					+ LangUtil.stringConcatWithSep(" ", getLabels()));
			System.out.println("Values : "
					+ LangUtil.stringConcatWithSep(" ", getValues()));
			throw new RuntimeException(
					"UniquevaluesRuleList is not in balance!");
		}
	}

	@Override
	public void importRules(List<Rule> rules) {

		// String metaInfoString = fts.getName();
		// pushQuite();
		// try {
		//
		// parseMetaInfoString(metaInfoString, fts);
		//
		// /***********************************************************
		// * Parsing information in the RULEs
		// *
		// * title, unique values, symbols=>singleRuleLists, template?
		// */
		int countRules = 0;
		setDefaultRuleEnabled(false);
		for (final Rule r : rules) {

			if (r.getName() != null
					&& r.getName().toString()
							.startsWith(FeatureRuleList.NODATA_RULE_NAME)) {
				// This rule defines the NoDataSymbol
				importNoDataRule(r);
				continue;
			}

			test();

			// Interpret Filter!
			Filter filter = r.getFilter();

			filter = parseAbstractRlSettings(filter);

			final String[] strings = UniqueValuesRuleList
					.interpretFilter(filter);

			setPropertyFieldName(strings[0], false);

			final Symbolizer[] symbolizers = r.getSymbolizers();

			final SingleRuleList<? extends Symbolizer> singleRLprototype = getDefaultTemplate();

			// Forget bout generics here!!!
			final SingleRuleList<?> symbolRL = singleRLprototype.copy();

			symbolRL.getSymbolizers().clear();
			for (final Symbolizer symb : symbolizers) {
				final Vector symbolizers2 = symbolRL.getSymbolizers();
				symbolizers2.add(symb);
			}
			symbolRL.reverseSymbolizers();

			// Finally set all three values into the RL
			getLabels().add(r.getDescription().getTitle().toString());
			getSymbols().add(symbolRL);
			getValues().add(strings[1]);

			test();

			countRules++;
		}

		LOGGER.debug("Imported " + countRules + " UNIQUE rules ");
		// } finally {
		// popQuite();
		// }
	}

}
