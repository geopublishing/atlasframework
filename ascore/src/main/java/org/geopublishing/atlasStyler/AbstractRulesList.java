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

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.geotools.filter.AndImpl;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.util.SimpleInternationalString;
import org.geotools.util.WeakHashSet;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.PropertyIsEqualTo;

import de.schmitzm.geotools.GTUtil;
import de.schmitzm.geotools.feature.FeatureUtil;
import de.schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import de.schmitzm.geotools.styling.StyledLayerUtil;
import de.schmitzm.lang.LangUtil;

/**
 * Any styling or other cartographic pattern that can be expressed as (SLD)
 * styling {@link Rule}s is presented in AtlasStyler as a
 * {@link AbstractRulesList}
 * 
 * @author stefan
 * 
 */
public abstract class AbstractRulesList {
	public static final FilterFactory2 ff = FeatureUtil.FILTER_FACTORY2;

	// ** Do not change the value, it is needed to recognize SLD **//
	private static final String ALL_LABEL_CLASSES_ENABLED = "ALL_LABEL_CLASSES_ENABLED";

	// ** Do not change the value, it is needed to recognize SLD **//
	private static final String RL_FILTER_APPLIED_STR = "RL_FILTER_APPLIED";
	/**
	 * A Filter to mark that one class/rule is enabled
	 **/
	public static final PropertyIsEqualTo RL_FILTER_APPLIED_FILTER = ff.equals(
			ff.literal(RL_FILTER_APPLIED_STR),
			ff.literal(RL_FILTER_APPLIED_STR));

	private String title = this.getClass().getSimpleName().toString();

	/**
	 * A Filter to mark that one class/rule has been disabled. Sorry,
	 * AtlasStyler specifc, but used in Sl #createLegendSwing method
	 **/
	public static final PropertyIsEqualTo RL_DISABLED_FILTER = ff.equals(
			ff.literal("ALL_LABEL_CLASSES_DISABLED"), ff.literal("YES"));

	/**
	 * A Filter to mark that one class/rule is enabled
	 **/
	public static final PropertyIsEqualTo RL_ENABLED_FILTER = ff.equals(
			ff.literal(ALL_LABEL_CLASSES_ENABLED),
			ff.literal(ALL_LABEL_CLASSES_ENABLED));

	/**
	 * A Filter to mark that not ALL classes have been disabled by the
	 * {@link AbstractRuleList}{@link #setEnabled(boolean)} method. This filter
	 * is not used anymore and only for backward compatibility. Will be removed
	 * in 2.0
	 **/
	public static final PropertyIsEqualTo OldAllClassesEnabledFilter = ff
			.equals(ff.literal("1"), ff.literal("1"));

	/**
	 * A Filter to mark that not ALL classes have been disabled by the
	 * {@link AbstractRuleList}{@link #setEnabled(boolean)} method. This filter
	 * is not used anymore and only for backward compatibility. Will be removed
	 * in 2.0
	 **/
	public static final PropertyIsEqualTo oldAllClassesDisabledFilter = ff
			.equals(ff.literal("1"), ff.literal("2"));

	/**
	 * If used as a {@link Rule}'s name, the rule should not be imported, but
	 * rather just be ignored.
	 */
	static final String RULENAME_DONTIMPORT = "DONTIMPORT";

	/**
	 * To simplifly the usage, any ScaleDenominator above this value will be
	 * interpreted as Infinite.
	 */
	public static final double MAX_SCALEDENOMINATOR = 1E20;

	protected Filter addAbstractRlSettings(Filter filter) {
		filter = addRuleListFilterAppliedFilter(filter);
		filter = addRuleListEnabledDisabledFilter(filter);
		return filter;
	}

	/**
	 * If a rlFilter is defined, it is returned here, wrapped in a recognizable
	 * AND structure. @see {@link #parseRuleListFilterAppliedFilter(Filter)}
	 */
	Filter addRuleListFilterAppliedFilter(Filter filter) {

		if (getRlFilter() != null && getRlFilter() != Filter.INCLUDE) {

			And markerAndFilter = ff.and(RL_FILTER_APPLIED_FILTER,
					getRlFilter());

			filter = ff.and(markerAndFilter, filter);
		}

		return filter;
	}

	/**
	 * Tries to determine, whether this filter contains a layer filter. @see
	 * {@link #addRuleListFilterAppliedFilter}
	 */
	Filter parseRuleListFilterAppliedFilter(Filter filter) {
		if (filter instanceof And) {
			And and1 = (And) filter;

			if (and1.getChildren().get(0) instanceof And) {
				And and2 = (And) and1.getChildren().get(0);
				if (and2.getChildren().get(0).equals(RL_FILTER_APPLIED_FILTER)) {

					// Import the rule list filter
					setRlFilter(and2.getChildren().get(1));

					// return the rest
					return and1.getChildren().get(1);
				}
			}
		}
		return filter;
	}

	private Filter rlFilter = null;

	/**
	 * Gets a filter that is applied to the whole AbstractRulesList. If will be
	 * added to all filters of all rules. Returns <code>null</code> for
	 * Filter.INCLUDE
	 */
	public Filter getRlFilter() {
		if (rlFilter == Filter.INCLUDE)
			return null;
		return rlFilter;
	}

	double maxScaleDenominator = Double.MAX_VALUE;

	double minScaleDenominator = 0.0;

	public double getMaxScaleDenominator() {
		return maxScaleDenominator;
	}

	public double getMinScaleDenominator() {
		return minScaleDenominator;
	}

	public void setMaxScaleDenominator(double maxScaleDenominator) {
		// May not be smaller than 0
		if (maxScaleDenominator < 0.)
			maxScaleDenominator = 0.;

		if (maxScaleDenominator > MAX_SCALEDENOMINATOR)
			maxScaleDenominator = MAX_SCALEDENOMINATOR;

		// "Push" the MaxScaleDenominator when moving up
		if (maxScaleDenominator < minScaleDenominator)
			minScaleDenominator = maxScaleDenominator - 1.;

		if (this.maxScaleDenominator == maxScaleDenominator)
			return;

		this.maxScaleDenominator = maxScaleDenominator;
		fireEvents(new RuleChangedEvent(
				RuleChangedEvent.RULE_CHANGE_EVENT_MINMAXSCALE_STRING, this));
	}

	public void setMinScaleDenominator(double minScaleDenominator) {

		// May not be smaller than 0
		if (minScaleDenominator < 0.)
			minScaleDenominator = 0.;

		if (minScaleDenominator > MAX_SCALEDENOMINATOR)
			minScaleDenominator = MAX_SCALEDENOMINATOR;

		// "Push" the MaxScaleDenominator when moving up
		if (minScaleDenominator > maxScaleDenominator)
			maxScaleDenominator = minScaleDenominator + 1.;

		if (this.minScaleDenominator == minScaleDenominator)
			return;

		this.minScaleDenominator = minScaleDenominator;
		fireEvents(new RuleChangedEvent(
				RuleChangedEvent.RULE_CHANGE_EVENT_MINMAXSCALE_STRING, this));
	}

	/**
	 * Sets a filter that is applied to the whole AbstractRulesList. If will be
	 * added to all filters of all rules. When changed, a
	 * {@link RuleChangedEvent} is fired. <code>Filter.INCLUDE</code> is changed
	 * to <code>null</code>
	 */
	public void setRlFilter(Filter rlFilter) {
		if (rlFilter == this.rlFilter)
			return;
		this.rlFilter = rlFilter;
		if (this.rlFilter == Filter.INCLUDE)
			this.rlFilter = null;
		fireEvents(new RuleChangedEvent(
				RuleChangedEvent.RULE_CHANGE_EVENT_FILTER_STRING, this));

	}

	/**
	 * Returns <code>ff.and(RL_ENABLED_FILTER, filter)</code> or
	 * <code>ff.and(RL_DISABLED_FILTER, filter)</code>.<Br/>
	 * 
	 * @see #parseAbstractRlSettings(Filter)
	 * 
	 */
	private Filter addRuleListEnabledDisabledFilter(Filter filter) {
		// Are all classes enabled?
		if (isEnabled()) {
			filter = ff.and(RL_ENABLED_FILTER, filter);
		} else {
			filter = ff.and(RL_DISABLED_FILTER, filter);
		}

		return filter;
	}

	protected Filter parseAbstractRlSettings(Filter filter) {
		filter = parseRuleListEnabledDisabledFilter(filter);
		filter = parseRuleListFilterAppliedFilter(filter);
		return filter;
	}

	/**
	 * Returns a shorter filter. The method tries to identify
	 * #RL_DISABLED_FILTER or #RL_ENABLED_FILTER and sets the #enabled variable.
	 * It should always be parsed as the first FilterParser (FilterParsers eat
	 * the filter).
	 * 
	 * @see #addAbstractRlSettings(Filter)
	 */
	private Filter parseRuleListEnabledDisabledFilter(Filter filter) {
		if (!(filter instanceof AndImpl)) {
			setEnabled(true);
			LOGGER.warn("Couldn't interpret whether this RulesList is disabled or enabled. Assuming it is enabled. Expected an AndFilter, but was "
					+ filter);
			return filter;
		} else
			try {
				AndImpl andImpl = (AndImpl) filter;
				List<?> andChildren = andImpl.getChildren();
				final Object child0 = andChildren.get(0);
				if (child0.equals(RL_DISABLED_FILTER)) {
					setEnabled(false);
				} else if (child0.equals(oldAllClassesDisabledFilter)) {
					setEnabled(false);
				} else if (child0.equals(RL_ENABLED_FILTER)) {
					setEnabled(true);
				} else if (child0.equals(OldAllClassesEnabledFilter)) {
					setEnabled(true);
				} else
					throw new RuntimeException(child0.toString() + "\n"
							+ filter);

				// Returning just the right part of the filter
				filter = (Filter) andChildren.get(1);

			} catch (Exception e) {
				setEnabled(true);
				LOGGER.error(
						"Couldn't interpret whether this RulesList is disabled or enabled. Assuming it is enabled.",
						e);
			}

		return filter;
	}

	public AbstractRulesList(GeometryForm geometryForm) {
		this.geometryForm = geometryForm;
	}

	/**
	 * These enum names must not be changed anymore. We use them with
	 * .tostring().equals(...)
	 * 
	 * TODO 2013: QUANTITIES_SIZED_LINE, QUANTITIES_SIZED_POINT,
	 * UNIQUE_VALUE_COMBINATIONS_LINE, UNIQUE_VALUE_COMBINATIONS_POINT,
	 * UNIQUE_VALUE_COMBINATIONS_POLYGONE
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public enum RulesListType {

		// RASTER
		RASTER_COLORMAP_DISTINCTVALUES("/images/raster_colormap1.png",
				"StylerSelection.raster_colormap1"),

		// POINTs
		SINGLE_SYMBOL_POINT("/images/single_point_symbol.png",
				"StylerSelection.single_symbol"),

		SINGLE_SYMBOL_LINE("/images/single_line_symbol.png",
				"StylerSelection.single_symbol"),

		SINGLE_SYMBOL_POLYGON("/images/single_polygon_symbol.png",
				"StylerSelection.single_symbol"),

		SINGLE_SYMBOL_POINT_FOR_POLYGON("/images/single_point_symbol.png",
				"StylerSelection.single_symbol_centroids"),

		// Quant Colored
		QUANTITIES_COLORIZED_POINT("/images/point_graduated_colors.png",
				"StylerSelection.quantities_colored"),

		QUANTITIES_COLORIZED_LINE("/images/line_graduated_colors.png",
				"StylerSelection.quantities_colored"),

		QUANTITIES_COLORIZED_POLYGON("/images/polygon_graduated_colors.png",
				"StylerSelection.quantities_colored"),

		QUANTITIES_COLORIZED_POINT_FOR_POLYGON(
				"/images/point_graduated_colors.png",
				"StylerSelection.quantities_colored_centroids"),

		// UUNIQUE VALUES
		UNIQUE_VALUE_POINT("/images/point_unique_values.png",
				"StylerSelection.categories_unique_values"),

		UNIQUE_VALUE_LINE("/images/line_unique_values.png",
				"StylerSelection.categories_unique_values"),

		UNIQUE_VALUE_POLYGON("/images/polygon_unique_values.png",
				"StylerSelection.categories_unique_values"),

		UNIQUE_VALUE_POINT_FOR_POLYGON("/images/point_unique_values.png",
				"StylerSelection.categories_unique_values_centroids"),

		// TEST
		TEXT_LABEL("/images/text_labeling.png", "StylerSelection.textLabeling");

		private final String imgResLocation;
		private final String i8nKey;
		private ImageIcon imageIcon;

		RulesListType(String imgResLocation, String i8nKey) {
			this.imgResLocation = imgResLocation;
			this.i8nKey = i8nKey;
		}

		/**
		 * @return a localized title for this {@link RulesListType}
		 */
		public String getTitle() {
			return AtlasStylerVector.R(i8nKey);
		}

		/**
		 * @return An example image for this {@link RulesListType}
		 */
		public ImageIcon getImage() {
			if (imageIcon == null) {
				URL resource = getClass().getResource(imgResLocation);
				if (resource != null) {
					imageIcon = new ImageIcon(resource);
				}
			}
			return imageIcon;
		}

		/**
		 * @return the RulesListTypes that make sense to create them for a given
		 *         {@link GeometryForm}. If paramter schema is empty, the list
		 *         does not filter against available attributes.
		 */
		RulesListType[] rlts;

		/**
		 * Returns an Array of vector RulesLists available for the given Schema
		 * 
		 * @param gf
		 * @param schema
		 * @return
		 */
		public static RulesListType[] valuesFor(GeometryForm gf,
				SimpleFeatureType schema) {

			boolean hasText = true;
			boolean hasNumeric = true;
			if (schema != null) {
				hasText = FeatureUtil.getValueFieldNames(schema).size()
						- FeatureUtil.getNumericalFieldNames(schema).size() > 0;
				hasNumeric = FeatureUtil.getNumericalFieldNames(schema).size() > 0;
			}

			if (gf == GeometryForm.POINT) {
				RulesListType[] rtls = new RulesListType[] { SINGLE_SYMBOL_POINT };
				if (hasText || hasNumeric)
					rtls = LangUtil.extendArray(rtls, TEXT_LABEL,
							UNIQUE_VALUE_POINT);
				if (hasNumeric)
					rtls = LangUtil.extendArray(rtls,
							QUANTITIES_COLORIZED_POINT);
				return rtls;
			}

			if (gf == GeometryForm.LINE) {

				RulesListType[] rtls = new RulesListType[] { SINGLE_SYMBOL_LINE };

				if (hasText || hasNumeric)
					rtls = LangUtil.extendArray(rtls, UNIQUE_VALUE_LINE,
							TEXT_LABEL);
				if (hasNumeric)
					rtls = LangUtil
							.extendArray(rtls, QUANTITIES_COLORIZED_LINE);
				return rtls;

			}

			if (gf == GeometryForm.POLYGON) {

				RulesListType[] rtls = new RulesListType[] {
						SINGLE_SYMBOL_POLYGON, SINGLE_SYMBOL_POINT_FOR_POLYGON, };

				if (hasText || hasNumeric)
					rtls = LangUtil.extendArray(rtls, UNIQUE_VALUE_POLYGON,
							UNIQUE_VALUE_POINT_FOR_POLYGON, TEXT_LABEL);
				if (hasNumeric)
					rtls = LangUtil.extendArray(rtls,
							QUANTITIES_COLORIZED_POLYGON,
							QUANTITIES_COLORIZED_POINT_FOR_POLYGON);
				return rtls;

			}

			if (gf == GeometryForm.ANY) {

				RulesListType[] rtls = new RulesListType[] {
						SINGLE_SYMBOL_POINT, SINGLE_SYMBOL_LINE,
						SINGLE_SYMBOL_POLYGON };

				if (hasText || hasNumeric)
					rtls = LangUtil
							.extendArray(rtls, UNIQUE_VALUE_POLYGON,
									UNIQUE_VALUE_POLYGON, UNIQUE_VALUE_LINE,
									TEXT_LABEL);
				if (hasNumeric)
					rtls = LangUtil.extendArray(rtls,
							QUANTITIES_COLORIZED_POLYGON,
							QUANTITIES_COLORIZED_POINT,
							QUANTITIES_COLORIZED_LINE);
				return rtls;

			}
			// if (gf == GeometryForm.NONE) {
			return new RulesListType[0];
			// }
		}

		public String getImageResLocation() {
			return imgResLocation;
		}

		public static RulesListType[] valuesFor(AtlasStyler as) {
			if (as instanceof AtlasStylerVector) {
				AtlasStylerVector asv = (AtlasStylerVector) as;
				return valuesFor(((AtlasStylerVector) as));
			} else
				return valuesFor((AtlasStylerRaster) as);
		}

		public static RulesListType[] valuesFor(AtlasStylerRaster as) {
			return new RulesListType[] { RASTER_COLORMAP_DISTINCTVALUES };
		}
		
		public static RulesListType[] valuesFor(AtlasStylerVector asv) {
			return valuesFor(asv.getStyledFeatures().getGeometryForm(), asv.getStyledFeatures().getSchema());
		}
	}

	RuleChangedEvent lastOpressedEvent = null;

	/** The geometry form this RuleList is designed for. **/
	final private GeometryForm geometryForm;

	// This is a WeakHashSet, so references to the listeners have to exist in
	// the classes adding the listeners. They shall not be anonymous instances.
	final WeakHashSet<RuleChangeListener> listeners = new WeakHashSet<RuleChangeListener>(
			RuleChangeListener.class);

	final protected Logger LOGGER = LangUtil.createLogger(this);

	/**
	 * If {@link #quite} == <code>true</code> no {@link RuleChangedEvent} will
	 * be fired.
	 */
	private boolean quite = false;
	Stack<Boolean> stackQuites = new Stack<Boolean>();

	/**
	 * Adds a {@link RuleChangeListener} which listens to changes in the
	 * {@link Rule}. Very good to update previews.<br>
	 * <b>The listening class must keep a reference to the listener (e.g. make
	 * it a field variable) because the listeners are kept in a WeakHashSet.</b>
	 * 
	 * @param listener
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public void addListener(RuleChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * Clears all {@link RuleChangeListener}s
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public void clearListeners() {
		listeners.clear();
	}

	/**
	 * Tells all {@link RuleChangeListener} that the {@link Rule}s represented
	 * by this {@link AbstractRulesList} implementation have changed.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public void fireEvents(RuleChangedEvent rce) {

		if (quite) {
			lastOpressedEvent = rce;
			return;
		} else {
			lastOpressedEvent = null;
		}

		for (RuleChangeListener l : listeners) {
			try {
				l.changed(rce);
			} catch (Exception e) {
				LOGGER.error("While fireEvents: " + rce, e);
			}
		}
	}

	/**
	 * The AtlasStyler stores meta information in the name tag of
	 * {@link FeatureTypeStyle}s.
	 * 
	 * @return a {@link String} that contains all information for this
	 *         particular RuleList
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public abstract String getAtlasMetaInfoForFTSName();

	/**
	 * If <code>false</code>, all rules in this filter will always evaluate to
	 * false.
	 */
	private boolean enabled = true;

	/**
	 * Imports the information stored in the FTS and then calls
	 * importRules(fts.rules());
	 */
	public void importFts(FeatureTypeStyle fts) {
		pushQuite();
		try {
			// This also imports the template from the first rule.
			String metaInfoString = fts.getName();
			parseMetaInfoString(metaInfoString, fts);

			final String title = GTUtil.descriptionTitle(fts.getDescription());
			if (title != null)
				setTitle(title);
			else
				setTitle(getType().toString());
			importMinMaxDenominators(fts);
			importRules(fts.rules());
		} finally {
			this.popQuite();
		}
	}

	/**
	 * Imports the min/max scale of the first rule found. is ca
	 */
	private void importMinMaxDenominators(FeatureTypeStyle fts) {
		List<Rule> rs = fts.rules();
		if (rs.size() > 0) {
			setMinScaleDenominator(rs.get(0).getMinScaleDenominator());
			setMaxScaleDenominator(rs.get(0).getMaxScaleDenominator());
		}

	}

	abstract void parseMetaInfoString(String metaInfoString,
			FeatureTypeStyle fts);

	abstract void importRules(List<Rule> rules);

	/**
	 * @return Returns the SLD {@link FeatureTypeStyle}s that represents this
	 *         RuleList. This method implemented here doesn't set the
	 *         FeatureTypeName. This is overridden in {@link FeatureRuleList}
	 */
	public FeatureTypeStyle getFTS() {
		List<Rule> rules = applyScaleDominators(getRules());
		rules = applyHideDisabledRulesLists(rules);
		FeatureTypeStyle ftstyle = ASUtil.SB.createFeatureTypeStyle("Feature",
				rules.toArray(new Rule[] {}));
		ftstyle.setName(getAtlasMetaInfoForFTSName());
		ftstyle.getDescription().setTitle(new SimpleInternationalString(title));
		return ftstyle;
	}

	private List<Rule> applyHideDisabledRulesLists(List<Rule> rules) {
		for (Rule r : rules) {

			// If this RuleList is disabled, add a HIDE IN LEGEND hint to the
			// Legend, so schmitzm will ignore the layer
			if (!isEnabled()) {

				// Do not add it again if it is already added:
				if (r.getName() != null
						&& r.getName().contains(
								StyledLayerUtil.HIDE_IN_LAYER_LEGEND_HINT))
					continue;

				// Add the HIDEME marker
				r.setName(r.getName() + "_"
						+ StyledLayerUtil.HIDE_IN_LAYER_LEGEND_HINT);
			}
		}

		return rules;

	}

	/**
	 * Returns direct access to the {@link RuleChangeListener}s {@link HashSet}
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public Set<RuleChangeListener> getListeners() {
		return listeners;
	}

	/**
	 * @return Returns the SLD {@link Rule}s that it represents. The min- and
	 *         max-Scale Denominators are applied to all Rules.
	 */
	public abstract List<Rule> getRules();

	/**
	 * Sets the min/max Scale denomintors . Should be applied to every rule
	 * created by this RulesLists.
	 */
	protected Rule applyScaleDominators(Rule rule) {
		rule.setMaxScaleDenominator(getMaxScaleDenominator());
		rule.setMinScaleDenominator(getMinScaleDenominator());
		return rule;
	}

	/**
	 * Sets the min/max Scale denomintors . Should be applied to every rule
	 * created by this RulesLists.
	 */
	protected List<Rule> applyScaleDominators(List<Rule> rules) {
		for (Rule r : rules) {
			applyScaleDominators(r);

			// If this RuleList is disabled, add a HIDE IN LEGEND hint to the
			// Legend, so schmitzm will ignore the layer
			if (!isEnabled()) {
				if (r.getName() != null
						&& !r.getName().contains(
								StyledLayerUtil.HIDE_IN_LAYER_LEGEND_HINT))
					r.setName(r.getName() + "_"
							+ StyledLayerUtil.HIDE_IN_LAYER_LEGEND_HINT);
			}
		}

		return rules;
	}

	/**
	 * When importing a {@link Style}, the {@link AtlasStylerVector} recognizes
	 * its RuleLists by reading meta information from the
	 * {@link FeatureTypeStyle}s name. That information starts with a basic
	 * identifier for the RuleList type.
	 * 
	 * @return An identifier string for that RuleList type.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public abstract RulesListType getType();

	/**
	 * If quite, the RuleList will not fire {@link RuleChangedEvent}s
	 */
	public boolean isQuite() {
		return quite;
	}

	/**
	 * Remove a QUITE-State from the event firing state stack
	 */
	public void popQuite() {
		setQuite(stackQuites.pop());
		if (quite == false) {
			if (lastOpressedEvent != null)
				fireEvents(lastOpressedEvent);
		} else {
			LOGGER.debug("not firing event because there are "
					+ stackQuites.size() + " 'quites' still on the stack");
		}

	}

	public void popQuite(RuleChangedEvent ruleChangedEvent) {
		setQuite(stackQuites.pop());
		if (quite == false)
			fireEvents(ruleChangedEvent);
		else {
			lastOpressedEvent = ruleChangedEvent;
			LOGGER.debug("not firing event " + ruleChangedEvent
					+ " because there are " + stackQuites.size()
					+ " 'quites' still on the stack");
		}
	}

	/**
	 * Add a QUITE-State to the event firing state stack
	 */
	public void pushQuite() {
		stackQuites.push(quite);
		setQuite(true);
	}

	/**
	 * Removes a {@link RuleChangeListener} which listens to changes in the
	 * {@link Rule}.
	 * 
	 * @param listener
	 *            {@link RuleChangeListener} to remove
	 * @return <code>false</code> if {@link RuleChangeListener} not found
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public boolean removeListener(RuleChangeListener listener) {
		return listeners.remove(listener);
	}

	/**
	 * If quite, the RuleList will not fire {@link RuleChangedEvent}s.
	 */
	private void setQuite(boolean b) {
		quite = b;
	}

	public GeometryForm getGeometryForm() {
		return geometryForm;
	}

	/**
	 * If <code>false</code>, all rules in this filter will always evaluate to
	 * false.<br/>
	 * Allows to define whether all rules are enabled. If disabled, if doesn't
	 * throw away all information, but just disables rules with an Always-False
	 * filter.
	 */
	public void setEnabled(boolean enabled) {
		if (enabled == this.enabled)
			return;
		this.enabled = enabled;
		fireEvents(new RuleChangedEvent(
				RuleChangedEvent.RULE_CHANGE_EVENT_ENABLED_STRING, this));
	}

	/**
	 * If <code>false</code>, all rules in this filter will always evaluate to
	 * false.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	public void setTitle(String title) {
		if (title == this.title)
			return;
		this.title = title;
		fireEvents(new RuleChangedEvent(
				RuleChangedEvent.RULE_CHANGE_EVENT_TITLE_STRING, this));
	}

	public String getTitle() {
		return title;
	}

}
