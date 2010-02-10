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
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.geotools.styling.Font;
import org.geotools.styling.LabelPlacement;
import org.geotools.styling.PointPlacement;
import org.geotools.styling.Rule;
import org.geotools.styling.TextSymbolizer;
import org.geotools.styling.visitor.DuplicatingStyleVisitor;
import org.opengis.filter.Filter;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.PropertyName;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import schmitzm.geotools.styling.StylingUtil;
import skrueger.geotools.StyledFeaturesInterface;
import skrueger.i8n.Translation;

public class TextRuleList extends AbstractRuleList {
	private static final Filter FILTER_DEFAULT_ALL_OTHERS_ID = ASUtil.allwaysTrueFilter;

	public static final String RULE_CHANGE_EVENT_ENABLED_STRING = "Enabled or Disabled the TextRuleList";

	final static protected Logger LOGGER = Logger.getLogger(TextRuleList.class);

	List<TextSymbolizer> symbolizers = new ArrayList<TextSymbolizer>();

	List<Filter> filterRules = new ArrayList<Filter>();
	List<Double> maxScales = new ArrayList<Double>();
	List<Double> minScales = new ArrayList<Double>();

	final private StyledFeaturesInterface<?> styledFeatures;

	public StyledFeaturesInterface<?> getStyledFeatures() {
		return styledFeatures;
	}

	public TextRuleList(StyledFeaturesInterface<?> styledFeatures) {
		this.styledFeatures = styledFeatures;
	}

	/* By default, a new Style doesn't have labels activated */
	boolean enabled = false;

	private List<String> ruleNames = new ArrayList<String>();

	private int selIdx = 0;

	public int getSelIdx() {
		return selIdx;
	}

	public void setSelIdx(int selIdx) {
		this.selIdx = selIdx;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void addDefaultClass() {
		if (symbolizers.size() != 0) {
			LOGGER.debug("Not adding a default because we are not empty!");
			return;
		}

		TextSymbolizer defaultTextSymbolizer = StylingUtil.STYLE_BUILDER
				.createTextSymbolizer();

		// For TextLabels for Polygons we set the PointPlacement option X to 50% by default 
		if (FeatureUtil.getGeometryForm(getStyledFeatures().getSchema()) == GeometryForm.POLYGON) {
			LabelPlacement labelPlacement = defaultTextSymbolizer.getLabelPlacement();
			if (labelPlacement instanceof PointPlacement) {
				PointPlacement pointPlacement = (PointPlacement) labelPlacement;
				pointPlacement.getAnchorPoint().setAnchorPointX(
						FeatureUtil.FILTER_FACTORY2.literal(".5"));
			}
		}
		getSymbolizers().add(defaultTextSymbolizer);
		getRuleNames().add("Default/all others");
		getFilterRules().add(FILTER_DEFAULT_ALL_OTHERS_ID);
		maxScales.add(Double.MAX_VALUE);
		minScales.add(1.);

		fireEvents(new RuleChangedEvent("Added a default TextSymbolizer", this));
	}

	public TextSymbolizer createDefaultTextSymbolizer() {

		Color color = Color.black;

		// final GraphicsEnvironment g = GraphicsEnvironment
		// .getLocalGraphicsEnvironment();
		// final String[] fonts = g.getAvailableFontFamilyNames();
		// TODO Use the default classes font if available??
		// TODO TODO TODO better default font!! with multiple families
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

		ts.setFill(StylingUtil.STYLE_BUILDER.createFill(color));

		// TODO TODO TODO better default font!! with multiple families
		ts.setFont(sldFont);


		return ts;
	}

	/**
	 * Allows to define whether the {@link TextSymbolizer} is enabled. If
	 * disabled, if doesn't throw away all informtion, but jsut disables it with
	 * an Allways-False filter.
	 */
	public void setEnabled(boolean enabled) {
		if (enabled == this.enabled)
			return;
		this.enabled = enabled;
		fireEvents(new RuleChangedEvent(RULE_CHANGE_EVENT_ENABLED_STRING, this));
	}

	public List<Filter> getFilterRules() {
		return filterRules;
	}

	public void setFilterRules(List<Filter> filterRules) {
		this.filterRules = filterRules;
		fireEvents(new RuleChangedEvent("setFilterRules", this));
	}

	public List<TextSymbolizer> getSymbolizers() {
		return symbolizers;
	}

	public void setSymbolizers(List<TextSymbolizer> symbolizers) {
		this.symbolizers = symbolizers;
		fireEvents(new RuleChangedEvent("setSymbolizers", this));
	}

	@Override
	public String getAtlasMetaInfoForFTSName() {
		return RulesListType.TEXT_LABEL.toString();
	}

	@Override
	public List<Rule> getRules() {

		ArrayList<Rule> rules = new ArrayList<Rule>();

		for (int i = 0; i < symbolizers.size(); i++) {
			TextSymbolizer tSymbolizer = symbolizers.get(i);

			// LOGGER.info("This textsymbolizer has priority = "
			// + tSymbolizer.getPriority());

			// The filter stored for the labeling class
			Filter filter = filterRules.get(i);

			// The default symbolizer get's a special filter= not OR OR OR OR
			if (i == 0) {
				if (getRuleNames().size() > 1) {
					List<Filter> ors = new ArrayList<Filter>();
					for (int j = 1; j < getRuleNames().size(); j++) { // Default
						ors.add(getFilterRules().get(j));
					}
					filter = ASUtil.ff2.not(ASUtil.ff2.or(ors));
				} else {
					// DEFAULT
					// filterApply = Utilities.allwaysTrueFilter;
					filter = ASUtil.allwaysTrueFilter;
				}

			} else {
				// do nothing: filter = filter;
			}

			/**
			 * We save the stuff to keep the settings, but actually all filters
			 * will always be false
			 */
			if (!isEnabled()) {
//				LOGGER.debug("Using AND allwaysFalseFilter");
				filter = ASUtil.ff2.and(ASUtil.allwaysFalseFilter, filter);
			}

			Rule rule = ASUtil.SB.createRule(tSymbolizer);
			rule.setName(getRuleNames().get(i));
			rule.setMinScaleDenominator(minScales.get(i));
			rule.setMaxScaleDenominator(maxScales.get(i));

			// If there is a second label attribute, make a second Rule that
			// only displays the first label in case that the second is null
			final PropertyName firstPropertyName = StylingUtil
					.getFirstPropertyName(styledFeatures.getSchema(),
							tSymbolizer);
			final PropertyName secondPropertyName = StylingUtil
					.getSecondPropertyName(styledFeatures.getSchema(),
							tSymbolizer);
			if (secondPropertyName != null) {
				// Attention: This structure is alsop in AtlasStyler.import

				final PropertyIsNull secondEmptyNull = ASUtil.ff2
						.isNull(secondPropertyName); // TODO add tests against
				// more user-defined
				// null values
				final Filter secondEmptyString = ASUtil.ff2.like(
						secondPropertyName, "");

				final Or secondEmpty = ASUtil.ff2.or(secondEmptyNull,
						secondEmptyString);

				Filter secondNotEmpty = ASUtil.ff2.not(secondEmpty);
				rule.setFilter(ASUtil.ff.and(secondNotEmpty, filter));
				rules.add(rule);

				// Not the fallbackrule in case that the second attribute IS
				// empty.
				DuplicatingStyleVisitor duplicatingRuleVisitor = new DuplicatingStyleVisitor();
				duplicatingRuleVisitor.visit(rule);
				Rule fallbackRule = (Rule) duplicatingRuleVisitor.getCopy();
				fallbackRule.setName("DONTIMPORT");
				fallbackRule.setFilter(ASUtil.ff.and(secondEmpty, filter));
				final TextSymbolizer ts2 = (TextSymbolizer) fallbackRule
						.symbolizers().get(0);
				StylingUtil.setDoublePropertyName(ts2, firstPropertyName, null);
				rules.add(fallbackRule);
			} else {
				// No second labeling property set
				rule.setFilter(filter);
				rules.add(rule);
			}

		}

		return rules;
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

	public int getSelectedIdx() {
		return selIdx;
	}

	public TextSymbolizer getSymbolizer() {
		return symbolizers.get(selIdx);
	}

	public String getRuleName() {
		return ruleNames.get(selIdx);
	}

	public void importClassesFromStyle(AbstractRuleList symbRL, Component owner) {

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

				StylingUtil.copyAllValues(defaultTextSymbolizer,
						getSymbolizers().get(0));
				// DuplicatingStyleVisitor duplicatingStyleVisitor = new
				// DuplicatingStyleVisitor(StylingUtil.STYLE_FACTORY);
				// duplicatingStyleVisitor.visit(defaultTextSymbolizer);
				// getSymbolizers().get(0) = (TextSymbolizer)
				// duplicatingStyleVisitor.getCopy();

				getSymbolizers().add(defaultTextSymbolizer);

				// createSchmitzMFilter(uniqueRL.getPropertyFieldName(), val)

				PropertyIsEqualTo filter = ASUtil.ff2.equals(ASUtil.ff2
						.property(uniqueRL.getPropertyFieldName()), ASUtil.ff2
						.literal(val));

				getFilterRules().add(filter);

				getRuleNames().add(val);

				maxScales.add(uniqueRL.getSymbols().get(i)
						.getMaxScaleDenominator());
				minScales.add(uniqueRL.getSymbols().get(i)
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
													gradRL.getNumClasses()),
									AtlasStyler
											.R("TextRulesList.Labelclass.Action.LoadClassesFromSymbols.AskOverwrite.Title"),
									JOptionPane.YES_NO_OPTION);

					if (res != JOptionPane.YES_OPTION)
						return;
				}
				removeAllClassesButFirst();

			}

			for (Rule r : gradRL.getRules()) {

				// Copy the first rules's settings to the new class:
				TextSymbolizer defaultTextSymbolizer = createDefaultTextSymbolizer();
				StylingUtil.copyAllValues(defaultTextSymbolizer,
						getSymbolizers().get(0));
				getSymbolizers().add(defaultTextSymbolizer);

				Filter filter = r.getFilter();

				getFilterRules().add(filter);

				minScales.add(gradRL.getTemplate().getMinScaleDenominator());
				maxScales.add(gradRL.getTemplate().getMaxScaleDenominator());

				// The title could be a Translation.. but we only want a string!
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
	}

	private void removeAllClassesButFirst() {
		TextSymbolizer backupS = getSymbolizers().get(0);
		getSymbolizers().clear();
		getSymbolizers().add(backupS);

		String backupRN = getRuleNames().get(0);
		getRuleNames().clear();
		getRuleNames().add(backupRN);

		Filter backupFR = getFilterRules().get(0);
		getFilterRules().clear();
		getFilterRules().add(backupFR);

		Double backupMin = minScales.get(0);
		minScales.clear();
		minScales.add(backupMin);

		Double backupMax = maxScales.get(0);
		maxScales.clear();
		maxScales.add(backupMax);

	}

}
