/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the AtlasStyler SLD/SE Editor application - A Java Swing-based GUI to create OGC Styled Layer Descriptor (SLD 1.0) / OGC Symbology Encoding 1.1 (SE) XML files.
 * http://www.geopublishing.org
 * 
 * AtlasStyler SLD/SE Editor is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.sld;

import java.awt.Color;
import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.geotools.brewer.color.BrewerPalette;
import org.geotools.brewer.color.ColorBrewer;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.visitor.UniqueVisitor;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Symbolizer;
import org.geotools.swing.ProgressWindow;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;

import schmitzm.swing.SwingUtil;
import skrueger.geotools.StyledFeaturesInterface;

public abstract class UniqueValuesRuleList extends FeatureRuleList {
	private static final Logger LOGGER = Logger
			.getLogger(UniqueValuesRuleList.class);

	/**
	 * Special unique value. When exporting by {@link #getRules()}, this value
	 * is translated to the special "all others" rule *
	 */
	public static final String ALLOTHERS_IDENTIFICATION_VALUE = "ALLOTHERS_RULE_ID";

	private String propertyFieldName = null;

	private final ArrayList<String> values1 = new ArrayList<String>();

	private List<SingleRuleList<? extends Symbolizer>> symbols = new ArrayList<SingleRuleList<? extends Symbolizer>>();

	private BrewerPalette palette = ColorBrewer.instance().getPalettes()[0];

	{
		try {
			palette = ColorBrewer.instance(ColorBrewer.SUITABLE_UNIQUE)
					.getPalettes()[0];
		} catch (final IOException e) {
			LOGGER.error(e);
			palette = ColorBrewer.instance().getPalettes()[0];
		}
	}

	private final ArrayList<String> labels = new ArrayList<String>();

	public UniqueValuesRuleList(StyledFeaturesInterface<?> styledFeatures) {
		super(styledFeatures);
	}

	@Override
	public String getAtlasMetaInfoForFTSName() {
		final String metaInfoString = getTypeID().toString();

		return metaInfoString;
	}

	/**
	 * @return the position of the "all others" rule in the rules. 0 is first
	 *         rule.
	 */
	public int getAllOthersRuleIdx() {
		return getValues().indexOf(ALLOTHERS_IDENTIFICATION_VALUE);
	}

	/**
	 * Represents this {@link UniqueValuesRuleList} as an array of OGC
	 * {@link Rule} elements. If the "all others" rule is activated, this method
	 * doesn't change a lot. The "all others" is a normal rule with // *
	 * #ALLOTHERS_IDENTIFICATION_VALUE
	 */
	@Override
	public List<Rule> getRules() {

		List<Rule> rules = new ArrayList<Rule>();
		int ruleCount = 0;

		final PropertyName propertyFieldName2 = ASUtil.ff2
				.property(getPropertyFieldName());

		for (final String uniqueValue : getValues()) {
			final Literal value = ASUtil.ff2.literal(uniqueValue);
			Filter filter = null;

			/*******************************************************************
			 * Checking for the special case, where the value == ALLOTHERS_RULE
			 */
			if (uniqueValue.equals(ALLOTHERS_IDENTIFICATION_VALUE)) {
				final List<Filter> filters = new ArrayList<Filter>();

				for (final String uV : getValues()) {
					if (!uV.equals(ALLOTHERS_IDENTIFICATION_VALUE)) {
						filter = ASUtil.ff2.equals(propertyFieldName2,
								ASUtil.ff2.literal(uV));
						filters.add(filter);
					}
				}

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
					filter = ASUtil.ff2.equals(propertyFieldName2, ASUtil.ff2
							.literal("-9090909090"));
					filters.add(filter);
				}
				// LOGGER.debug("filters size = "+filters.size());
				final Or allor = ASUtil.ff2.or(filters);
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
				filter = ASUtil.ff2.equals(propertyFieldName2, value);
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

			rule.setFilter(filter);
			rule.setTitle(getLabels().get(ruleCount));
			
			rules.add(rule);
			ruleCount++;
		}

		return rules;
	}

	public ArrayList<String> getValues() {
		return values1;
	}

	public boolean isWithDefaultSymbol() {
		return getValues().contains(ALLOTHERS_IDENTIFICATION_VALUE);
	}

	public int getNumClasses() {
		return getValues().size();
	}

	public List<SingleRuleList<? extends Symbolizer>> getSymbols() {
		return symbols;
	}

	public void setSymbols(
			final List<SingleRuleList<? extends Symbolizer>> symbols) {
		this.symbols = symbols;
	}

	public void setWithDefaultSymbol(final boolean withDefaultSymbol) {

		pushQuite();

		/***********************************************************************
		 * It has been activated.. create a default rule and insert it at the
		 * end
		 */
		if ((withDefaultSymbol == true)
				&& (!getValues().contains(ALLOTHERS_IDENTIFICATION_VALUE))) {

			getValues().add(ALLOTHERS_IDENTIFICATION_VALUE);
			getSymbols().add(getTemplate().copy());
			getLabels().add(AtlasStyler.R("UniqueValuesGUI.AllOthersLabel"));

		} else
		/***********************************************************************
		 * It has been disabled.. remove the default rule
		 */
		{
			removeValue(ALLOTHERS_IDENTIFICATION_VALUE);

		}

		popQuite(new RuleChangedEvent("WithDefaultRule set to "
				+ withDefaultSymbol, this));
	}

	public BrewerPalette getBrewerPalette() {
		return palette;
	}

	public void setBrewerPalette(final BrewerPalette palette) {
		this.palette = palette;
	}

	/**
	 * Set the first field used for the categorization.
	 * 
	 * @param newName
	 *            Give the name directly as a {@link PropertyName}
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public void setPropertyFieldName(final String newName,
			final boolean removeOldClasses) {

		// We may not use the Getter here!.. It generates content :-(
		if (propertyFieldName != null)
			if (propertyFieldName.equals(newName))
				return;

		propertyFieldName = newName;

		if (removeOldClasses) {
			for (int i = 0; i < values1.size(); i++) {
				if (values1.get(i) != ALLOTHERS_IDENTIFICATION_VALUE) {
					values1.remove(i);
					symbols.remove(i);
					labels.remove(i);
				}
			}
		}

		fireEvents(new RuleChangedEvent("The PropertyField changed to "
				+ getPropertyFieldName() + ". Other rules have been removed.",
				this));
	}

	public String getPropertyFieldName() {
		if (propertyFieldName == null) {
			final List<String> valueFieldNames = ASUtil
					.getValueFieldNames(getStyledFeatures().getSchema(), false);
			if (valueFieldNames.size() > 0)
				propertyFieldName = valueFieldNames.get(0);
			else
				throw new RuntimeException(
						"Not possible for featureTypes without attributes");
		}
		return propertyFieldName;
	}

	/**
	 * Sets a template Symbol used for this symbolization. Different from super.
	 * {@link #setTemplate(SingleRuleList)}, this doesn't fire an envent because
	 * we wait for the user to apply the template to some rules.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	@Override
	public void setTemplate(final SingleRuleList template) {
		this.template = template;
		// fireEvents(new RuleChangedEvent("Set template", this));
	}

	Integer countNew = 0;

	private double maxScaleDenominator = Double.MAX_VALUE;

	/**
	 * Returns a {@link Set} or {@link String} representations (usable for the
	 * {@link Filter}s) not yet included in any of the rule lists.
	 * 
	 * @param progressWindow
	 * @return
	 * @throws IOException
	 */
	public Set<String> getAllUniqueValuesThatAreNotYetIncluded(
			final ProgressWindow progressWindow) throws IOException {

		/**
		 * Searching for all values. The layerFilter is applied.
		 **/
		final FeatureCollection<SimpleFeatureType, SimpleFeature> aFeatureCollection = getStyledFeatures()
				.getFeatureCollectionFiltered();

		final UniqueVisitor uniqueVisitor = new UniqueVisitor(
				getPropertyFieldName());

		aFeatureCollection.accepts(uniqueVisitor, null);
		final Set<Object> vals = uniqueVisitor.getResult().toSet();

		// LOGGER.info("for prop name = "+getPropertyFieldName());

		final Set<String> uniques = new TreeSet<String>();

		for (final Object o : vals) {

			if (o == null)
				continue;

			if (o instanceof Number) {
				final Number number = (Number) o;
				if (!getValues().contains(number.toString()))
					uniques.add(number.toString());
			} else if (o instanceof String) {
				// Null or empty stings crash the PropertyEqualsImpl
				final String string = (String) o;

				if (string.trim().isEmpty())
					continue;

				if (!getValues().contains(string))
					uniques.add(string);
			} else {
				LOGGER.warn("Unrecognized value type = " + o.getClass());
			}
		}

		return uniques;
	}

	/**
	 * Adds all missing unique values to the list of rules...
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 * @throws IOException
	 */
	public void addAllValues(final Component parentGUI) throws IOException {
		if (getPropertyFieldName() == null) {
			JOptionPane
					.showMessageDialog(
							parentGUI,
							AtlasStyler
									.R("UniqueValuesRuleList.AddAllValues.Error.NoAttribSelected"));
			return;
		}

		final ProgressWindow progressWindow;
		if (parentGUI != null) {
			progressWindow = new ProgressWindow(parentGUI);
			progressWindow.setTitle(AtlasStyler
					.R("UniqueValuesRuleList.AddAllValues.SearchingMsg"));
			progressWindow.started();
		} else {
			progressWindow = null;
		}

		countNew = 0;

		final SwingWorker<Set<String>, Object> findUniques = new SwingWorker<Set<String>, Object>() {

			@Override
			protected Set<String> doInBackground() throws Exception {

				return getAllUniqueValuesThatAreNotYetIncluded(progressWindow);
			}

			@Override
			protected void done() {
				pushQuite();
				try {

					for (final String uniqueString : get()) {
						addUniqueValue(uniqueString);
						countNew++;
					}
				} catch (final Exception e) {
					LOGGER.warn("Error finding all unique values", e);
					if (progressWindow != null)
						progressWindow.exceptionOccurred(e);
				} finally {
					if (progressWindow != null) {
						progressWindow.complete();
						progressWindow.dispose();
					}

					/** Fire an event * */
					if (countNew > 0)
						popQuite(new RuleChangedEvent("Added " + countNew
								+ " values.", UniqueValuesRuleList.this));
					else popQuite();

					JOptionPane.showMessageDialog(parentGUI, AtlasStyler.R(
							"UniqueValuesRuleList.AddAllValues.DoneMsg",
							countNew));
				}

			}
		};

		findUniques.execute();
	}

	/**
	 * @param uniqueString
	 *            Unique value to all to the list.
	 * 
	 * @return <code>false</code> is the value already exists
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public boolean addUniqueValue(final String uniqueString)
			throws IllegalArgumentException {

		if (getValues().contains(uniqueString)) {
			LOGGER.warn("The unique String " + uniqueString
					+ " can't be added, it is allready in the list");
			return false;
		}

		getSymbols().add(getTemplate().copy());
		getLabels().add(uniqueString);
		getValues().add(uniqueString);

		fireEvents(new RuleChangedEvent("Added value: '" + uniqueString + "'",
				this));

		return true;
	}

	/**
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public ArrayList<String> getLabels() {
		return labels;
	}

	/**
	 * Applies the template to the given ist of categories.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 * 
	 * @param values
	 *            List of {@link String} values that shall be affected by this.
	 */
	public void applyTemplate(final List<String> values) {
		pushQuite();

		for (final String catString : values) {

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
	 * Applies the Template to ALL values
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public void applyTemplate() {
		applyTemplate(getValues());
	}

	/**
	 * Apply the ColorPalette to all symbols at once
	 * 
	 * @param componentForGui
	 *            If not <code>null</code>, a message may appear
	 * 
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
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
							.R(
									"UniqueValuesGUI.WarningDialog.more_classes_than_colors.msg",
									getBrewerPalette().getMaxColors(),
									getValues().size());
					JOptionPane.showMessageDialog(SwingUtil
							.getParentWindowComponent(componentForGui), msg);
					warnedOnce = true;
				}
			}

			getSymbols().get(i).setColor(colors[idx]);
		}

		popQuite(new RuleChangedEvent("Applied a COLORPALETTE to all rules",
				this));
	}

	/**
	 * Remove the given list of {@link String} values.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public void removeValues(final List<String> values) {
		pushQuite();

		for (final String strVal : values.toArray(new String[values.size()])) {
			removeValue(strVal);
		}

		popQuite(new RuleChangedEvent(values.size()
				+ " categories have been removed", this));

	}

	/**
	 * Removes a single value
	 * 
	 * @param strVal
	 *            Removes this value
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	private void removeValue(final String strVal) {

		final int idx = getValues().indexOf(strVal);

		if (idx >= 0) {
			getValues().remove(idx);
			getLabels().remove(idx);
			getSymbols().remove(idx);
		} else {
			LOGGER.warn("Asked to remove a value that doesn't exist !?: '"
					+ strVal + "'");
		}

		fireEvents(new RuleChangedEvent("Removed value " + strVal, this));
		// LOGGER.debug("remove = "+strVal+" size left = "+getValues().size());
	}

	public void parseMetaInfoString(final String metaInfoString,
			final FeatureTypeStyle fts) {
		// TODO Do we have metainfo?
	}

	/**
	 * The Filter contains information about: unique value; ValueFieldName,
	 * symbolizer, onOff
	 * 
	 * @param filter
	 *            {@link Filter} to interpret
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 * 
	 * @return String[] with propertyName, uniqueValue
	 */
	public static String[] interpretFilter(final Filter filter)
			throws RuntimeException {

		if (filter instanceof PropertyIsEqualTo) {
			final PropertyIsEqualTo propertEqualTo = (PropertyIsEqualTo) filter;
			final PropertyName pName = (PropertyName) propertEqualTo
					.getExpression1();
			final Literal value = (Literal) propertEqualTo.getExpression2();

			return new String[] { pName.getPropertyName(), value.toString() };
		} else if (filter instanceof Not) {
			// This must be the default/all others rule...
			final Not not = (Not) filter;
			final Or or = (Or) not.getFilter();

			// if (or.getChildren() != null) {
			// Other rules exist, OR is not empty
			final PropertyIsEqualTo propertEqualTo = (PropertyIsEqualTo) or
					.getChildren().get(0);
			final PropertyName pName = (PropertyName) propertEqualTo
					.getExpression1();
			return new String[] { pName.getPropertyName(),
					ALLOTHERS_IDENTIFICATION_VALUE };
			// } else {
			// return new String[] { pName.getPropertyName(),
			// ALLOTHERS_IDENTIFICATION_VALUE };
			// }

		}

		throw new RuntimeException("Filter not recognized.");
	}

	public void setMaxScaleDenominator(double maxScaleDenominator) {
		this.maxScaleDenominator = maxScaleDenominator;
	}

}
