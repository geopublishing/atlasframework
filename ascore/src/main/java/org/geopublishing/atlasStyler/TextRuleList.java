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
package org.geopublishing.atlasStyler;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.geotools.filter.AndImpl;
import org.geotools.styling.Font;
import org.geotools.styling.LabelPlacement;
import org.geotools.styling.PointPlacement;
import org.geotools.styling.Rule;
import org.geotools.styling.TextSymbolizer;
import org.geotools.styling.visitor.DuplicatingStyleVisitor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.PropertyName;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import schmitzm.geotools.styling.StylingUtil;
import skrueger.geotools.StyledFeaturesInterface;
import skrueger.i8n.Translation;

public class TextRuleList extends AbstractRuleList {
	private static final Filter FILTER_DEFAULT_ALL_OTHERS_ID = ASUtil.allwaysTrueFilter;

	public static final String RULE_CHANGE_EVENT_ENABLED_STRING = "Enabled or Disabled the complete TextRuleList";

	final static protected Logger LOGGER = Logger.getLogger(TextRuleList.class);

	/** Stores all {@link TextSymbolizer}s for all classes **/
	List<TextSymbolizer> classesSymbolizers = new ArrayList<TextSymbolizer>();

	/** Stores all {@link Filter} for all classes **/
	private List<Filter> classesFilters = new ArrayList<Filter>();

	/** Stores all maxScale parameters for the classes **/
	List<Double> classesMaxScales = new ArrayList<Double>();

	/** Stores all minScale parameters for all classes **/
	List<Double> classesMinScales = new ArrayList<Double>();

	/** Stores whether the class specific {@link TextSymbolizer}s are enabled **/
	private List<Boolean> classesEnabled = new ArrayList<Boolean>();

	final private StyledFeaturesInterface<?> styledFeatures;

	public StyledFeaturesInterface<?> getStyledFeatures() {
		return styledFeatures;
	}

	public TextRuleList(StyledFeaturesInterface<?> styledFeatures) {
		this.styledFeatures = styledFeatures;
	}

	/**
	 * This defines whether all {@link TextSymbolizer} rules shall be disabled.
	 * By default, a new Style doesn't have labels activated
	 */
	boolean enabled = false;

	private List<String> ruleNames = new ArrayList<String>();

	private int selIdx = 0;

	public static final FilterFactory2 ff = FeatureUtil.FILTER_FACTORY2;

	/** A Filter to mark a {@link TextSymbolizer} class as enabled **/
	public final static PropertyIsEqualTo classEnabledFilter = ff.equals(ff
			.literal("LABEL_CLASS_ENABLED"), ff.literal("LABEL_CLASS_ENABLED"));

	/** A Filter to mark a {@link TextSymbolizer} class as disabled **/
	public final static PropertyIsEqualTo classDisabledFilter = ff.equals(ff
			.literal("LABEL_CLASS_DISABLED"), ff.literal("YES"));

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
			ff.literal("ALL_LABEL_CLASSES_ENABLED"), ff
					.literal("ALL_LABEL_CLASSES_ENABLED"));

	public int getSelIdx() {
		return selIdx;
	}

	public void setSelIdx(int selIdx) {
		this.selIdx = selIdx;
	}

	/**
	 * Are all {@link TextSymbolizer} classes disabled/enabled.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	public void addDefaultClass() {
		if (classesSymbolizers.size() != 0) {
			LOGGER.debug("Not adding a default because we are not empty!");
			return;
		}
//
//		TextSymbolizer defaultTextSymbolizer = StylingUtil.STYLE_BUILDER
//				.createTextSymbolizer();
//
//		// For TextLabels for Polygons we set the PointPlacement option X to 50%
//		// by default
//		if (FeatureUtil.getGeometryForm(getStyledFeatures().getSchema()) == GeometryForm.POLYGON) {
//			LabelPlacement labelPlacement = defaultTextSymbolizer
//					.getLabelPlacement();
//			if (labelPlacement instanceof PointPlacement) {
//				PointPlacement pointPlacement = (PointPlacement) labelPlacement;
//				pointPlacement.getAnchorPoint().setAnchorPointX(
//						FeatureUtil.FILTER_FACTORY2.literal(".5"));
//			}
//		}
		
		TextSymbolizer defaultTextSymbolizer = createDefaultTextSymbolizer();
		
		getSymbolizers().add(defaultTextSymbolizer);
		getRuleNames().add("Default/all others"); //i8n
		getClassesFilters().add(FILTER_DEFAULT_ALL_OTHERS_ID);
		setClassEnabled(0, true);
		classesMaxScales.add(Double.MAX_VALUE);
		classesMinScales.add(1.);

		fireEvents(new RuleChangedEvent("Added a default TextSymbolizer", this));
	}

	public void setClassEnabled(int index, boolean b) {
		while (classesEnabled.size()-1 < index ) {
			classesEnabled.add(true);
		}
		classesEnabled.set(index, b);
		fireEvents(new RuleChangedEvent(
				"a text symbolizer class enablement has been set to " + b, this));
	}

	public TextSymbolizer createDefaultTextSymbolizer() {
		
		// If we already have a default symbolizer, we will return a duplication of it.
		if (getSymbolizers().size()>0 ) {
			DuplicatingStyleVisitor duplicatingStyleVisitor = new DuplicatingStyleVisitor(
					StylingUtil.STYLE_FACTORY);
			duplicatingStyleVisitor.visit(getSymbolizers().get(0));
			return (TextSymbolizer) duplicatingStyleVisitor.getCopy(); 
		}

		// final GraphicsEnvironment g = GraphicsEnvironment
		// .getLocalGraphicsEnvironment();
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

	public List<Filter> getClassesFilters() {
		return classesFilters;
	}

	public void setClassesFilters(List<Filter> filterRules) {
		this.classesFilters = filterRules;
		fireEvents(new RuleChangedEvent("setFilterRules", this));
	}

	public List<TextSymbolizer> getSymbolizers() {
		return classesSymbolizers;
	}

	public void setSymbolizers(List<TextSymbolizer> symbolizers) {
		this.classesSymbolizers = symbolizers;
		fireEvents(new RuleChangedEvent("setSymbolizers", this));
	}

	@Override
	public String getAtlasMetaInfoForFTSName() {
		return RulesListType.TEXT_LABEL.toString();
	}

	@Override
	public List<Rule> getRules() {

		ArrayList<Rule> rules = new ArrayList<Rule>();

		for (int i = 0; i < classesSymbolizers.size(); i++) {
			TextSymbolizer tSymbolizer = classesSymbolizers.get(i);

			// The filter stored for this class. This filer already contains the
			// exclusion of NODATA values.
			Filter filter = classesFilters.get(i);

			if (i == 0) {
				// The default symbolizer get's a special filter= not (2ndFilter
				// OR 3rdFilter OR 4thFilter) if other rules are defined.

				if (getRuleNames().size() > 1) {
					List<Filter> ors = new ArrayList<Filter>();
					for (int j = 1; j < getRuleNames().size(); j++) { // Default
						ors.add(getClassesFilters().get(j));
					}
					filter = ASUtil.ff2.not(ASUtil.ff2.or(ors));
				} else {
					// The default filter includes all, IF no other rules have
					// been defined.
					filter = Filter.INCLUDE;
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
						.getAttributeMetaDataMap().get(
								secondPropertyName.getPropertyName())
						.getNodataValues()) {
					secondLabelNodataFilter = ff.or(ff.equals(
							secondPropertyName, ff.literal(nodata)),
							secondLabelNodataFilter);
				}

				Filter secondNotEmpty = ASUtil.ff2.not(secondLabelNodataFilter);
				rule.setFilter(addEnabledDisabledFilters(ASUtil.ff.and(
						secondNotEmpty, filter), i));
				rules.add(rule);

				// Not the fallbackrule in case that the second attribute IS
				// empty.
				DuplicatingStyleVisitor duplicatingRuleVisitor = new DuplicatingStyleVisitor();
				duplicatingRuleVisitor.visit(rule);
				Rule fallbackRule = (Rule) duplicatingRuleVisitor.getCopy();
				fallbackRule.setName("DONTIMPORT");
				fallbackRule.setFilter(addEnabledDisabledFilters(ASUtil.ff.and(
						secondLabelNodataFilter, filter), i));
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

	/**
	 * Adds filters that indicate whether this class or all classes are
	 * disabled. Is added as the last filters so they are easierer identified.
	 * 
	 * @param idx
	 *            the index of the label class (0=default)
	 * 
	 * @return modified filter
	 * 
	 * @see #removeEnabledDisabledFilters(Filter, int)
	 */
	private Filter addEnabledDisabledFilters(Filter filter, int idx) {

		// Is this class enabled?
		if (classesEnabled.get(idx)) {
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

	/**
	 * Interpretes the filters added by
	 * {@link #addEnabledDisabledFilters(Filter, int)}
	 * 
	 * @param idx
	 *            the index of the label class (0=default)
	 * 
	 * @return The simpler filter that was inside all the mess.
	 */
	protected Filter removeEnabledDisabledFilters(Filter filter, int idx) {

		try {
			if (((AndImpl) filter).getChildren().get(0).equals(
					allClassesDisabledFilter)) {
				setEnabled(false);
				filter = (Filter) ((AndImpl) filter).getChildren().get(1);
			} else if (((AndImpl) filter).getChildren().get(0).equals(
					allClassesEnabledFilter)) {
				setEnabled(true);
				filter = (Filter) ((AndImpl) filter).getChildren().get(1);
			} else throw new RuntimeException();
		} catch (Exception e) {
			setEnabled(true);
			LOGGER
					.warn("Couldn't property interpret whether this TextRulesList is completely disabled or enabled. Assuming it is enabled.",e);
		}

		try {
			if (((AndImpl) filter).getChildren().get(0).equals(
					classDisabledFilter)) {
				setClassEnabled(idx, false);
				filter = (Filter) ((AndImpl) filter).getChildren().get(1);
			} else if (((AndImpl) filter).getChildren().get(0).equals(
					classEnabledFilter)) {
				setClassEnabled(idx, true);
				filter = (Filter) ((AndImpl) filter).getChildren().get(1);
			}else throw new RuntimeException();
		} catch (Exception e) {
			setClassEnabled(idx, true);
			LOGGER
					.warn("Couldn't property interpret whether this TextRulesList CLASS is disabled or enabled. Assuming it is enabled.",e);
		}

		return filter;
	}

	@Override
	public RulesListType getTypeID() {
		return RulesListType.TEXT_LABEL;
	}

	public List<String> getRuleNames() {
		return ruleNames;
	}

	public void setRuleNames(List<String> ruleNames) {
		this.ruleNames = ruleNames;
		// No Event needed! Its just a name...
	}

	/**
	 * @return the {@link TextSymbolizer} selected in the combo box
	 * @see #getSelIdx() 
	 */
	public TextSymbolizer getSymbolizer() {
		return classesSymbolizers.get(selIdx);
	}

	public String getRuleName() {
		return ruleNames.get(selIdx);
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
												.R(
														"TextRulesList.Labelclass.Action.LoadClassesFromSymbols.AskOverwrite",
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
					String val = uniqueRL.getValues().get(i);

					// Copy the first rules's settings to the new class:
					TextSymbolizer defaultTextSymbolizer = createDefaultTextSymbolizer();

					getSymbolizers().add(defaultTextSymbolizer);

					PropertyIsEqualTo filter = ASUtil.ff2.equals(ASUtil.ff2
							.property(uniqueRL.getPropertyFieldName()),
							ASUtil.ff2.literal(val));

					getClassesFilters().add(filter);

					setClassEnabled(1 + i
							- (uniqueRL.isWithDefaultSymbol() ? 1 : 0), true);

					getRuleNames().add(val);

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
												.R(
														"TextRulesList.Labelclass.Action.LoadClassesFromSymbols.AskOverwrite",
														getRuleNames().size() - 1,
														gradRL.getRules().size()),
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
										.R(
												"TextRulesList.Labelclass.Action.LoadClassesFromSymbols.SuccesMsg",
												getRuleNames().size() - 1));

			}
		} finally {
			popQuite();
		}

	}

	private void removeAllClassesButFirst() {
		TextSymbolizer backupS = getSymbolizers().get(0);
		getSymbolizers().clear();
		getSymbolizers().add(backupS);

		String backupRN = getRuleNames().get(0);
		getRuleNames().clear();
		getRuleNames().add(backupRN);

		Filter backupFR = getClassesFilters().get(0);
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

	public boolean getClassEnabled(int index) {
		return classesEnabled.get(index);
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

				// When using two label properties, we have two rules for one
				// label class. So we drop it.
				if (rule.getName() != null
						&& rule.getName().equals("DONTIMPORT"))
					continue;


				final TextSymbolizer textSymb = (TextSymbolizer) rule
						.getSymbolizers()[0];

				getSymbolizers().add(textSymb);
				getRuleNames().add(rule.getName());

				classesMaxScales.add(rule.getMaxScaleDenominator());
				classesMinScales.add(rule.getMinScaleDenominator());

				Filter filter = rule.getFilter();
				try {
					
				filter = removeEnabledDisabledFilters(filter, idx);
				} catch (Exception e) {
					System.out.println(e);
				}

				getClassesFilters().add(filter);
				
				idx++;
			}

			setSelIdx(0);

		} finally {
			popQuite();
		}

	}

	/**
	 * @param classIdx Label class idx, 0 = default/all others
	 */
	public void removeClassMinScale(int classIdx) {
		classesMinScales.remove(classIdx);
	}

	/**
	 * @param classIdx Label class idx, 0 = default/all others
	 */
	public void removeClassMaxScale(int classIdx) {
		classesMaxScales.remove(classIdx);
	}

	/**
	 * @param classIdx Label class idx, 0 = default/all others
	 */
	public void removeClassEnabledScale(int classIdx) {
		classesEnabled.remove(classIdx);
	}
	
}
