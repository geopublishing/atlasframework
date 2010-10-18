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
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.AtlasStyler.LANGUAGE_MODE;
import org.geotools.filter.AndImpl;
import org.geotools.filter.BinaryComparisonAbstract;
import org.geotools.filter.NotImpl;
import org.geotools.styling.Font;
import org.geotools.styling.LabelPlacement;
import org.geotools.styling.PointPlacement;
import org.geotools.styling.Rule;
import org.geotools.styling.TextSymbolizer;
import org.geotools.styling.visitor.DuplicatingStyleVisitor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.PropertyName;

import schmitzm.geotools.FilterUtil;
import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import schmitzm.geotools.gui.XMapPane;
import schmitzm.geotools.styling.StylingUtil;
import skrueger.geotools.StyledFeaturesInterface;
import skrueger.i8n.Translation;

public class TextRuleList extends AbstractRuleList {

	public static final FilterFactory2 ff = FeatureUtil.FILTER_FACTORY2;

	/**
	 * A Filter to mark that ALL classes have been disabled by the
	 * {@link TextRuleList}{@link #setEnabled(boolean)} method
	 **/
	public static final PropertyIsEqualTo allClassesDisabledFilter = ff.equals(
			ff.literal("ALL_LABEL_CLASSES_DISABLED"), ff.literal("YES"));

	/**
	 * A Filter to mark that not ALL classes have been disabled by the
	 * {@link TextRuleList}{@link #setEnabled(boolean)} method
	 **/
	public static final PropertyIsEqualTo allClassesEnabledFilter = ff.equals(
			ff.literal("ALL_LABEL_CLASSES_ENABLED"),
			ff.literal("ALL_LABEL_CLASSES_ENABLED"));

	/**
	 * A Filter to mark that not ALL classes have been disabled by the
	 * {@link TextRuleList}{@link #setEnabled(boolean)} method. This filter is
	 * not used anymore and only for backward compatibility.
	 **/
	public static final PropertyIsEqualTo oldAllClassesEnabledFilter = ff
			.equals(ff.literal("1"), ff.literal("1"));

	/**
	 * A Filter to mark that not ALL classes have been disabled by the
	 * {@link TextRuleList}{@link #setEnabled(boolean)} method. This filter is
	 * not used anymore and only for backward compatibility.
	 **/
	public static final PropertyIsEqualTo oldAllClassesDisabledFilter = ff
			.equals(ff.literal("1"), ff.literal("2"));

	/**
	 * A Filter to mark that not ALL classes have been disabled by the
	 * {@link TextRuleList}{@link #setEnabled(boolean)} method. This filter is
	 * not used anymore and only for backward compatibility.
	 **/
	public static final PropertyIsEqualTo oldClassesEnabledFilter = ff.equals(
			ff.literal("1"), ff.literal("1"));

	/**
	 * A Filter to mark that not ALL classes have been disabled by the
	 * {@link TextRuleList}{@link #setEnabled(boolean)} method. This filter is
	 * not used anymore and only for backward compatibility.
	 **/
	public static final PropertyIsEqualTo oldClassesDisabledFilter = ff.equals(
			ff.literal("1"), ff.literal("2"));

	/** A Filter to mark a {@link TextSymbolizer} class as disabled **/
	public final static PropertyIsEqualTo classDisabledFilter = ff.equals(
			ff.literal("LABEL_CLASS_DISABLED"), ff.literal("YES"));

	/** A Filter to mark a {@link TextSymbolizer} class as enabled **/
	public final static PropertyIsEqualTo classEnabledFilter = ff.equals(
			ff.literal("LABEL_CLASS_ENABLED"),
			ff.literal("LABEL_CLASS_ENABLED"));

	static final Filter DEFAULT_FILTER_ALL_OTHERS = FilterUtil.ALLWAYS_TRUE_FILTER;

	/** All default text rule names start with this **/
	static final String DEFAULT_CLASS_RULENAME = "DEFAULT";

	final static protected Logger LOGGER = Logger.getLogger(TextRuleList.class);

	public static final String RULE_CHANGE_EVENT_ENABLED_STRING = "Enabled or Disabled the complete TextRuleList";

	/** Stores whether the class specific {@link TextSymbolizer}s are enabled **/
	private List<Boolean> classesEnabled = new ArrayList<Boolean>();

	/**
	 * Stores whether the class is only valid for a special language. If
	 * <code>null</code>, a class is not language-specific
	 **/
	private List<String> classesLanguages = new ArrayList<String>();

	/** Stores all {@link Filter}s for all classes **/
	private List<Filter> classesFilters = new ArrayList<Filter>();

	/** Stores all maxScale parameters for the classes **/
	List<Double> classesMaxScales = new ArrayList<Double>();

	/** Stores all minScale parameters for all classes **/
	List<Double> classesMinScales = new ArrayList<Double>();

	/** Stores all {@link TextSymbolizer}s for all classes **/
	List<TextSymbolizer> classesSymbolizers = new ArrayList<TextSymbolizer>();

	/**
	 * This defines whether all {@link TextSymbolizer} rules shall be disabled.
	 * By default, a new Style doesn't have labels activated
	 */
	boolean enabled = false;

	private List<String> classesRuleNames = new ArrayList<String>();

	/**
	 * @deprecated move the selIdx out of this class
	 */
	private int selIdx = 0;

	final private StyledFeaturesInterface<?> styledFeatures;

	public TextRuleList(StyledFeaturesInterface<?> styledFeatures) {
		this.styledFeatures = styledFeatures;
	}

	/**
	 * @return the index of the newly added class
	 */
	public int addDefaultClass(String lang) {

		TextSymbolizer defaultTextSymbolizer = createDefaultTextSymbolizer();

		pushQuite();

		try {

			// int idx = classesSymbolizers.size();
			// setClassSymbolizer(idx, defaultTextSymbolizer);
			// setClassFilter(idx, FILTER_DEFAULT_ALL_OTHERS_ID);
			// setClassEnabled(idx, true);
			// setClassLang(idx, lang);

			if (existsClass(DEFAULT_FILTER_ALL_OTHERS, lang)) {
				LOGGER.debug("Not adding a default class for " + lang
						+ " because an equal class already exits!");
				return -1;
			}

			String ruleName = DEFAULT_CLASS_RULENAME;
			if (lang != null)
				ruleName += "_" + lang;

			return addClass(defaultTextSymbolizer, ruleName,
					DEFAULT_FILTER_ALL_OTHERS, true, lang, null, null);

		} finally {
			popQuite(new RuleChangedEvent("Added a default TextSymbolizer",
					this));
		}
	}

	/**
	 * @return <code>true</code> if another class with the same filter and name
	 *         already exists.
	 */
	public boolean existsClass(Filter filter, String lang) {

		for (int i = 0; i < countClasses(); i++) {
			if (getClassLang(i) == null && lang != null)
				continue;
			if (getClassLang(i) != null && !getClassLang(i).equals(lang))
				continue;

			if (filter != null
					&& getClassFilter(i).toString().equals(filter.toString()))
				return true;
		}

		return false;
	}

	/**
	 * @return the index of the newly added class
	 */
	public int addDefaultClass() {
		return addDefaultClass(null);
	}

	/**
	 * 
	 * @param ts
	 * @param ruleName
	 * @param filter
	 * @param enabled
	 * @param lang
	 * @param minScale
	 *            <code>null</code> allowed will fall-back to 0.
	 * @param maxScale
	 *            <code>null</code> allowed will fall-back to Double.MAX_VALUE
	 * @return the index of the newly added class
	 */
	public int addClass(TextSymbolizer ts, String ruleName, Filter filter,
			boolean enabled, String lang, Double minScale, Double maxScale) {

		int index = classesSymbolizers.size();

		pushQuite();
		try {

			setClassSymbolizer(index, ts);
			setClassRuleName(index, ruleName);
			setClassFilter(index, filter);
			setClassEnabled(index, enabled);
			setClassLang(index, lang);

			setClassMinMaxScales(index, minScale, maxScale);

			setSelIdx(index);

			return index;

		} finally {
			popQuite(new RuleChangedEvent("Added a new class with position "
					+ index + " and ruleName " + ruleName, this));
		}
	}

	/**
	 * Adds filters that indicate whether this class or all classes are
	 * disabled. Is added as the last filters so they are easily identified.
	 * 
	 * @param idx
	 *            the index of the label class (0=default)
	 * 
	 * @return modified filter
	 * 
	 * @see #parseAndRemoveEnabledDisabledFilters(Filter, int)
	 */
	protected Filter addEnabledDisabledFilters(Filter filter, int idx) {

		filter = addLanguageFilter(filter, idx);

		// Is this class enabled?
		if (isClassEnabled(idx)) {
			filter = ff.and(classEnabledFilter, filter);
		} else {
			filter = ff.and(classDisabledFilter, filter);
		}

		// Are all classes enabled?
		if (isEnabled()) {
			filter = ff.and(allClassesEnabledFilter, filter);
		} else {
			filter = ff.and(allClassesDisabledFilter, filter);
		}
		return filter;
	}

	private Filter addLanguageFilter(Filter filter, int idx) {
		// Is this class language specific?
		if (classesLanguages.get(idx) != null) {
			filter = ff.and(classLanguageFilter(classesLanguages.get(idx)),
					filter);
		} else {
		}
		return filter;
	}

	/**
	 * A piece of {@link Filter} that is only true, if the rendering language is
	 * set to the given language. @see XMapPane#setRenderLanguage
	 * 
	 * @param lang
	 *            if <code>null</code>, the rule will be true when no specific
	 *            rendering language has been set.
	 */
	protected static Filter classLanguageFilter(String lang) {

		if (lang == null)
			lang = XMapPane.ENV_LANG_DEFAULT;

		// set argument to set a default return value of 0
		Expression exEnv = ff.function("env", ff.literal(XMapPane.ENV_LANG),
				ff.literal(XMapPane.ENV_LANG_DEFAULT));

		Filter filter = ff.equals(ff.literal(lang), exEnv);

		return filter;

	}

	private TextSymbolizer createDefaultTextSymbolizer() {

		// If we already have a default symbolizer, we will return a duplication
		// of it.
		if (getSymbolizers().size() > 0) {
			DuplicatingStyleVisitor duplicatingStyleVisitor = new DuplicatingStyleVisitor(
					StylingUtil.STYLE_FACTORY);
			duplicatingStyleVisitor.visit(getSymbolizers().get(0));
			return (TextSymbolizer) duplicatingStyleVisitor.getCopy();
		}

		// final String[] fonts = g.getAvailableFontFamilyNames();
		// TODO Use the default classes font if available??
		// TODO better default font!! with multiple families
		Font sldFont = ASUtil.SB
				.createFont("Times New Roman", false, false, 11);
		List<String> valueFieldNamesPrefereStrings = ASUtil
				.getValueFieldNamesPrefereStrings(getStyledFeatures()
						.getSchema(), false);

		TextSymbolizer ts = ASUtil.SB.createTextSymbolizer();

		if (valueFieldNamesPrefereStrings.size() != 0) {
			ts.setLabel(FeatureUtil.FILTER_FACTORY2
					.property(valueFieldNamesPrefereStrings.get(0)));
		}

		ts.setFill(StylingUtil.STYLE_BUILDER.createFill(Color.black));

		// TODO better default font!! with multiple families
		ts.setFont(sldFont);

		// For Polygons we set the PointPlacement option X to 50%
		// by default
		if (FeatureUtil.getGeometryForm(getStyledFeatures().getSchema()) == GeometryForm.POLYGON) {
			LabelPlacement labelPlacement = ts.getLabelPlacement();
			if (labelPlacement instanceof PointPlacement) {
				PointPlacement pointPlacement = (PointPlacement) labelPlacement;
				pointPlacement.getAnchorPoint().setAnchorPointX(
						FeatureUtil.FILTER_FACTORY2.literal(".5"));
			}
		}

		return ts;
	}

	@Override
	public String getAtlasMetaInfoForFTSName() {
		return RulesListType.TEXT_LABEL.toString();
	}

	public boolean isClassEnabled(int index) {
		return classesEnabled.get(index);
	}

	public Filter getClassFilter(int index) {
		return classesFilters.get(index);
	}

	public String getClassLang(int index) {
		return classesLanguages.get(index);
	}

	private List<Filter> getClassesFilters() {
		return classesFilters;
	}

	/**
	 * @deprecated move the selIdx out of this class
	 */
	public String getRuleName() {
		return classesRuleNames.get(selIdx);
	}

	public List<String> getRuleNames() {
		return classesRuleNames;
	}

	@Override
	public List<Rule> getRules() {

		ArrayList<Rule> rules = new ArrayList<Rule>();

		for (int i = 0; i < classesSymbolizers.size(); i++) {
			TextSymbolizer tSymbolizer = classesSymbolizers.get(i);

			// The filter stored for this class. This filer already contains the
			// exclusion of NODATA values.
			Filter filter = classesFilters.get(i);

			// if (i == 0 || filter.equals(FILTER_DEFAULT_ALL_OTHERS_ID)) {
			if (getRuleName(i).startsWith(DEFAULT_CLASS_RULENAME)) {
				// The default symbolizer get's a special filter= not (2ndFilter
				// OR 3rdFilter OR 4thFilter) if other rules are defined.

				if (getRuleNames().size() > 1) {

					List<Filter> ors = new ArrayList<Filter>();
					for (int j = 0; j < getRuleNames().size(); j++) { // Default
						Filter otherFilter = getClassFilter(j);

						if (j == i || j == 0)
							continue;

						ors.add(addLanguageFilter(otherFilter, j));
					}
					if (ors.size() > 0) {
						filter = ASUtil.ff2.not(ASUtil.ff2.or(ors));
					}
				} else {
					// The default filter includes all, IF no other rules have
					// been defined.
					filter = DEFAULT_FILTER_ALL_OTHERS;
				}
			}

			Rule rule = ASUtil.SB.createRule(tSymbolizer);
			rule.setName(getRuleNames().get(i));
			rule.setMinScaleDenominator(classesMinScales.get(i));
			rule.setMaxScaleDenominator(classesMaxScales.get(i));

			// If there is a second label attribute, make a second Rule that
			// only displays the first label in case that the second is null
			final PropertyName firstPropertyName = StylingUtil
					.getFirstPropertyName(styledFeatures.getSchema(),
							tSymbolizer);
			final PropertyName secondPropertyName = StylingUtil
					.getSecondPropertyName(styledFeatures.getSchema(),
							tSymbolizer);
			if (secondPropertyName != null) {
				// Attention: This structure is also in AtlasStyler.import

				Filter secondLabelNodataFilter = ff.isNull(secondPropertyName);
				for (Object nodata : getStyledFeatures()
						.getAttributeMetaDataMap()
						.get(secondPropertyName.getPropertyName())
						.getNodataValues()) {
					secondLabelNodataFilter = ff.or(
							ff.equals(secondPropertyName, ff.literal(nodata)),
							secondLabelNodataFilter);
				}

				Filter secondNotEmpty = ASUtil.ff2.not(secondLabelNodataFilter);
				rule.setFilter(addEnabledDisabledFilters(
						ASUtil.ff.and(secondNotEmpty, filter), i));
				rules.add(rule);

				// Not the fallbackrule in case that the second attribute IS
				// empty.
				DuplicatingStyleVisitor duplicatingRuleVisitor = new DuplicatingStyleVisitor();
				duplicatingRuleVisitor.visit(rule);
				Rule fallbackRule = (Rule) duplicatingRuleVisitor.getCopy();
				fallbackRule.setName("DONTIMPORT");
				fallbackRule.setFilter(addEnabledDisabledFilters(
						ASUtil.ff.and(secondLabelNodataFilter, filter), i));
				final TextSymbolizer ts2 = (TextSymbolizer) fallbackRule
						.symbolizers().get(0);
				StylingUtil.setDoublePropertyName(ts2, firstPropertyName, null);
				rules.add(fallbackRule);
			} else {
				// No second labeling property set
				rule.setFilter(addEnabledDisabledFilters(filter, i));
				rules.add(rule);
			}

		}

		return rules;
	}

	public String getRuleName(int index) {
		return classesRuleNames.get(index);
	}

	/**
	 * @deprecated move the selIdx out of this class
	 */

	public int getSelIdx() {
		return selIdx;
	}

	public StyledFeaturesInterface<?> getStyledFeatures() {
		return styledFeatures;
	}

	/**
	 * @return the {@link TextSymbolizer} selected in the combo box
	 * @see #getSelIdx()
	 * @deprecated move the selIdx out of this class
	 */
	public TextSymbolizer getSymbolizer() {
		return classesSymbolizers.get(selIdx);
	}

	public List<TextSymbolizer> getSymbolizers() {
		return classesSymbolizers;
	}

	@Override
	public RulesListType getTypeID() {
		return RulesListType.TEXT_LABEL;
	}

	public void importClassesFromStyle(AbstractRuleList symbRL, Component owner) {

		pushQuite();

		try {

			if (symbRL instanceof UniqueValuesRuleList) {
				UniqueValuesRuleList uniqueRL = (UniqueValuesRuleList) symbRL;
				if (uniqueRL.getNumClasses() <= (uniqueRL.isWithDefaultSymbol() ? 1
						: 0)) {
					if (owner != null) {
						JOptionPane
								.showMessageDialog(
										owner,
										AtlasStyler
												.R("TextRulesList.Labelclass.Action.LoadClassesFromSymbols.Error.NoClasses"));
					}
					return;
				}

				if (getRuleNames().size() > 1) {

					if (owner != null) {
						int res = JOptionPane
								.showConfirmDialog(
										owner,
										AtlasStyler
												.R("TextRulesList.Labelclass.Action.LoadClassesFromSymbols.AskOverwrite",
														getRuleNames().size() - 1,
														(uniqueRL.getValues()
																.size() - (uniqueRL
																.isWithDefaultSymbol() ? 1
																: 0))),
										AtlasStyler
												.R("TextRulesList.Labelclass.Action.LoadClassesFromSymbols.AskOverwrite.Title"),
										JOptionPane.YES_NO_OPTION);
						if (res != JOptionPane.YES_OPTION)
							return;
					}

					removeAllClassesButFirst();
				}

				for (int i = (uniqueRL.isWithDefaultSymbol() ? 1 : 0); i < uniqueRL
						.getValues().size(); i++) {
					Object val = uniqueRL.getValues().get(i);

					// Copy the first rules's settings to the new class:
					TextSymbolizer defaultTextSymbolizer = createDefaultTextSymbolizer();

					getSymbolizers().add(defaultTextSymbolizer);

					PropertyIsEqualTo filter = ASUtil.ff2.equals(ASUtil.ff2
							.property(uniqueRL.getPropertyFieldName()),
							ASUtil.ff2.literal(val));

					getClassesFilters().add(filter);

					setClassEnabled(1 + i
							- (uniqueRL.isWithDefaultSymbol() ? 1 : 0), true);

					getRuleNames().add(uniqueRL.getLabels().get(i));

					classesMaxScales.add(uniqueRL.getSymbols().get(i)
							.getMaxScaleDenominator());
					classesMinScales.add(uniqueRL.getSymbols().get(i)
							.getMinScaleDenominator());
				}
			}

			/***********************************************************************
			 * Importing Rules form GraduatedColorRuleList is a bit different
			 */
			else if (symbRL instanceof GraduatedColorRuleList) {
				GraduatedColorRuleList gradRL = (GraduatedColorRuleList) symbRL;
				if (gradRL.getNumClasses() <= 0) {
					if (owner != null) {
						JOptionPane
								.showMessageDialog(
										owner,
										AtlasStyler
												.R("TextRulesList.Labelclass.Action.LoadClassesFromSymbols.Error.NoClasses"));
					}
					return;
				}

				if (getRuleNames().size() > 1) {

					if (owner != null) {
						int res = JOptionPane
								.showConfirmDialog(
										owner,
										AtlasStyler
												.R("TextRulesList.Labelclass.Action.LoadClassesFromSymbols.AskOverwrite",
														getRuleNames().size() - 1,
														gradRL.getRules()
																.size()),
										AtlasStyler
												.R("TextRulesList.Labelclass.Action.LoadClassesFromSymbols.AskOverwrite.Title"),
										JOptionPane.YES_NO_OPTION);

						if (res != JOptionPane.YES_OPTION)
							return;
					}
					removeAllClassesButFirst();

				}

				int idx = 1; // the default rule is left untouched
				for (Rule r : gradRL.getRules()) {
					idx++;
					setClassEnabled(idx, true);

					// Copy the first rules's settings to the new class:
					TextSymbolizer defaultTextSymbolizer = createDefaultTextSymbolizer();
					StylingUtil.copyAllValues(defaultTextSymbolizer,
							getSymbolizers().get(0));
					getSymbolizers().add(defaultTextSymbolizer);

					Filter filter = r.getFilter();

					getClassesFilters().add(filter);

					classesMinScales.add(gradRL.getTemplate()
							.getMinScaleDenominator());
					classesMaxScales.add(gradRL.getTemplate()
							.getMaxScaleDenominator());

					// The title could be a Translation.. but we only want a
					// string!
					Translation t = new Translation();
					t.fromOneLine(r.getDescription().getTitle().toString());
					getRuleNames().add(t.toString());
				}

				JOptionPane
						.showMessageDialog(
								owner,
								AtlasStyler
										.R("TextRulesList.Labelclass.Action.LoadClassesFromSymbols.SuccesMsg",
												getRuleNames().size() - 1));

			}
		} finally {
			popQuite();
		}

	}

	/**
	 * Parses a list of {@link Rule}s and configures this {@link TextRuleList}
	 * with it.
	 */
	public void importRules(List<Rule> rules) {

		pushQuite();
		try {

			int idx = 0;
			for (Rule rule : rules) {

				Filter filter = rule.getFilter();
				try {
					// When using two label properties, we have two rules for
					// one
					// label class. So we drop it.
					String ruleName = rule.getName();
					if (ruleName != null && ruleName.equals("DONTIMPORT"))
						continue;

					filter = parseAndRemoveEnabledDisabledFilters(filter, idx);

					if (ruleName.startsWith(DEFAULT_CLASS_RULENAME)
							|| filter.equals(oldClassesEnabledFilter)
							|| filter.equals(oldClassesDisabledFilter)
							|| idx == 0) {
						// Do not store the filter imported for default rules.
						getClassesFilters().add(DEFAULT_FILTER_ALL_OTHERS);

						// If this has been an old default rule, update the
						// ruleName to the new
						ruleName = DEFAULT_CLASS_RULENAME;
						if (getClassLang(idx) != null)
							ruleName += "_" + getClassLang(idx);
					} else {
						getClassesFilters().add(filter);
					}

					// getClassLang(idx);

					final TextSymbolizer textSymb = (TextSymbolizer) rule
							.getSymbolizers()[0];

					getSymbolizers().add(textSymb);
					getRuleNames().add(ruleName);

					classesMaxScales.add(rule.getMaxScaleDenominator());
					classesMinScales.add(rule.getMinScaleDenominator());

					idx++;
				} catch (Exception e) {
					e.printStackTrace();
					LOGGER.error("Error parsing textSymbolizerClassfilter '  "
							+ filter + "  '", e);
				}
			}

			/**
			 * @deprecated move the selIdx out of this class
			 */
			setSelIdx(0);

		} finally {
			popQuite();
		}

	}

	/**
	 * Are all {@link TextSymbolizer} classes disabled/enabled.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	private void removeAllClassesButFirst() {
		TextSymbolizer backupS = getSymbolizers().get(0);
		getSymbolizers().clear();
		getSymbolizers().add(backupS);

		String backupRN = getRuleNames().get(0);
		getRuleNames().clear();
		getRuleNames().add(backupRN);

		Filter backupFR = getClassFilter(0);
		getClassesFilters().clear();
		getClassesFilters().add(backupFR);

		Double backupMin = classesMinScales.get(0);
		classesMinScales.clear();
		classesMinScales.add(backupMin);

		Double backupMax = classesMaxScales.get(0);
		classesMaxScales.clear();
		classesMaxScales.add(backupMax);

		Boolean backupEnabled = classesEnabled.get(0);
		classesEnabled.clear();
		classesEnabled.add(backupEnabled);
	}

	/**
	 * @param classIdx
	 *            Label class idx, 0 = default/all others
	 */
	public void removeClassEnabled(int classIdx) {
		classesEnabled.remove(classIdx);
	}

	/**
	 * @param classIdx
	 *            Label class idx, 0 = default/all others
	 */
	public void removeClassMaxScale(int classIdx) {
		classesMaxScales.remove(classIdx);
	}

	/**
	 * @param classIdx
	 *            Label class idx, 0 = default/all others
	 */
	public void removeClassMinScale(int classIdx) {
		classesMinScales.remove(classIdx);
	}

	/**
	 * Interprets the filters added by
	 * {@link #addEnabledDisabledFilters(Filter, int)}
	 * 
	 * @param idx
	 *            the index of the label class (0=default)
	 * 
	 * @return The simpler filter that was inside all the mess.
	 */
	protected Filter parseAndRemoveEnabledDisabledFilters(Filter filter, int idx) {

		final Filter fullFilter = filter;

		/**
		 * Interpreting whether all classes at once are disabled/enabled.
		 */
		try {
			List<?> andChildren = ((AndImpl) filter).getChildren();
			if (andChildren.get(0).equals(allClassesDisabledFilter)) {
				setEnabled(false);
			} else if (andChildren.get(0).equals(oldAllClassesDisabledFilter)) {
				setEnabled(false);
			} else if (andChildren.get(0).equals(allClassesEnabledFilter)) {
				setEnabled(true);
			} else if (andChildren.get(0).equals(oldAllClassesEnabledFilter)) {
				setEnabled(true);
			} else
				throw new RuntimeException(andChildren.get(0).toString() + "\n"
						+ fullFilter.toString());

			filter = (Filter) andChildren.get(1);

		} catch (Exception e) {
			setEnabled(true);
			LOGGER.warn(
					"Couldn't interpret whether this TextRulesList is completely disabled or enabled. Assuming it is enabled.",
					e);
		}

		/**
		 * Interpreting whether the class is disable/enables
		 */
		try {

			if (filter.equals(oldClassesEnabledFilter)) {
				setClassEnabled(idx, true);
			} else if (filter.equals(oldClassesDisabledFilter)) {
				setClassEnabled(idx, false);
			} else {
				List<?> andChildren = ((AndImpl) filter).getChildren();
				if (andChildren.get(0).equals(classDisabledFilter)) {
					setClassEnabled(idx, false);
				} else if (andChildren.get(0).equals(classEnabledFilter)) {
					setClassEnabled(idx, true);
				} else {
					throw new RuntimeException(andChildren.get(0).toString()
							+ "\n" + fullFilter.toString());
				}
				filter = (Filter) andChildren.get(1);
			}

		} catch (Exception e) {
			setClassEnabled(idx, true);
			LOGGER.warn(
					"Couldn't interpret whether this TextRulesList CLASS is disabled or enabled. Assuming it is enabled.",
					e);
		}

		filter = parseAndRemoveLanguageFilter(filter, idx);

		return filter;
	}

	private Filter parseAndRemoveLanguageFilter(Filter filter, int idx) {
		/**
		 * Interpreting whether this class is language specific
		 */

		if (!(filter instanceof AndImpl)) {
			setClassLang(idx, null);
			return filter;
		}

		try {
			AndImpl andFilter = (AndImpl) filter;

			List<?> andChildren = andFilter.getChildren();
			// System.out.println(filter);

			Filter envEqualsLang = (Filter) andChildren.get(0);

			Function envFunction = (Function) ((BinaryComparisonAbstract) envEqualsLang)
					.getExpression2();

			Expression langExp = (Expression) ((BinaryComparisonAbstract) envEqualsLang)
					.getExpression1();

			if (!envFunction.getName().equals("env"))
				throw new RuntimeException();

			// if (langExp.toString().equals(XMapPane.ENV_LANG_DEFAULT)){
			// setClassLang(idx, null);
			// } else {
			setClassLang(idx, langExp.toString());
			// }

			filter = (Filter) andChildren.get(1);

		} catch (Exception e) {
			setClassLang(idx, null);
			//
			// if (filter instanceof BinaryComparisonAbstract) {
			// // All good, this is an old filter created with AS pre 1.5
			// } else if (filter instanceof NotImpl) {
			// // All good, this is the default filter without a language.
			// } else {
			LOGGER.warn(
					"Couldn't interpret whether this TextRulesList CLASS is language-specific or not. Assuming it is not.",
					e);
			// }
		}
		return filter;
	}

	private void setClassRuleName(int index, String ruleName) {
		while (classesRuleNames.size() - 1 < index) {
			classesRuleNames.add("");
		}
		classesRuleNames.set(index, ruleName);

		fireEvents(new RuleChangedEvent(
				"a text CLASS rulename has been set to " + ruleName, this));
	}

	public void setClassMinMaxScales(int index, Double minValue, Double maxValue) {
		setClassMinScale(index, minValue);
		setClassMaxScale(index, maxValue);
	}

	public void setClassMaxScale(int index, Double maxValue) {
		while (classesMaxScales.size() - 1 < index) {
			classesMaxScales.add(0.);
		}

		if (maxValue == null)
			maxValue = Double.MAX_VALUE;
		classesMaxScales.set(index, maxValue);

		fireEvents(new RuleChangedEvent(
				"a text CLASS MaxScale has been set to " + maxValue, this));
	}

	public void setClassMinScale(int index, Double minValue) {
		while (classesMinScales.size() - 1 < index) {
			classesMinScales.add(0.);
		}
		if (minValue == null)
			minValue = 0.;
		classesMinScales.set(index, minValue);

		fireEvents(new RuleChangedEvent(
				"a text CLASS MinScale has been set to " + minValue, this));
	}

	private void setClassSymbolizer(int index, TextSymbolizer ts) {
		while (classesSymbolizers.size() - 1 < index) {
			classesSymbolizers.add(ts);
		}
		classesSymbolizers.set(index, ts);

		fireEvents(new RuleChangedEvent(
				"a text CLASS symbolizer has been set to " + ts, this));
	}

	/**
	 * @param lang
	 *            may be <code>null</code>
	 */
	void setClassLang(int index, String lang) {

		while (classesLanguages.size() - 1 < index) {
			classesLanguages.add(null);
		}

		if (XMapPane.ENV_LANG_DEFAULT.equals(lang)) {
			lang = null;
		}

		if (index == 0 && lang != null)
			throw new RuntimeException(
					"The default class may not be language specific");

		classesLanguages.set(index, lang);
		fireEvents(new RuleChangedEvent(
				"a text symbolizer CLASS language has been set to " + lang,
				this));
	}

	public void setClassFilter(int index, Filter filter) {
		while (classesFilters.size() - 1 < index) {
			classesFilters.add(Filter.EXCLUDE);
		}

		classesFilters.set(index, filter);
		fireEvents(new RuleChangedEvent(
				"a text symbolizer class FILTER has been set to " + filter,
				this));
	}

	public void setClassEnabled(int index, boolean b) {
		while (classesEnabled.size() - 1 < index) {
			classesEnabled.add(true);
		}
		classesEnabled.set(index, b);
		fireEvents(new RuleChangedEvent(
				"a text symbolizer class enablement has been set to " + b, this));
	}

	// public void setClassesFilters(List<Filter> filterRules) {
	// this.classesFilters = filterRules;
	// fireEvents(new RuleChangedEvent("setFilterRules", this));
	// }

	/**
	 * Allows to define whether all {@link TextSymbolizer}s are enabled. If
	 * disabled, if doesn't throw away all informtion, but just disables al
	 * textsymbolizers with an Allways-False filter.
	 */
	public void setEnabled(boolean enabled) {
		if (enabled == this.enabled)
			return;
		this.enabled = enabled;
		fireEvents(new RuleChangedEvent(RULE_CHANGE_EVENT_ENABLED_STRING, this));
	}

	public void setRuleNames(List<String> ruleNames) {
		this.classesRuleNames = ruleNames;
		// No Event needed! Its just a name...
	}

	/**
	 * @deprecated move the selIdx out of this class
	 */
	public void setSelIdx(int selIdx) {
		this.selIdx = selIdx;
	}

	public void setSymbolizers(List<TextSymbolizer> symbolizers) {
		this.classesSymbolizers = symbolizers;
		fireEvents(new RuleChangedEvent("setSymbolizers", this));
	}

	/**
	 * @return the number of text classes defined.
	 */
	public int countClasses() {
		return classesFilters.size();
	}

	/**
	 * Remove a text symbolizer class.
	 */
	public void removeClass(int idx) {
		getSymbolizers().remove(idx);
		getClassesFilters().remove(idx);
		getRuleNames().remove(idx);
		removeClassMinScale(idx);
		removeClassMaxScale(idx);
		removeClassEnabled(idx);
	}

	/**
	 * A list of languages that a default class has already been defined for
	 * 
	 * @return
	 */
	public ArrayList<String> getDefaultLanguages() {
		ArrayList<String> usedLangs = new ArrayList<String>();

		if (AtlasStyler.languageMode == LANGUAGE_MODE.OGC_SINGLELANGUAGE)
			return usedLangs;

		for (String lang : AtlasStyler.getLanguages()) {
			if (existsClass(DEFAULT_FILTER_ALL_OTHERS, lang)) {
				usedLangs.add(lang);
			}
		}
		return usedLangs;
	}

}
