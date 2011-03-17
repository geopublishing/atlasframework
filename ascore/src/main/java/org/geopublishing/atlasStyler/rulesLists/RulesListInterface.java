package org.geopublishing.atlasStyler.rulesLists;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.RuleChangeListener;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geopublishing.atlasStyler.rulesLists.AbstractRulesList.RulesListType;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.PropertyIsEqualTo;

import de.schmitzm.geotools.feature.FeatureUtil;
import de.schmitzm.geotools.feature.FeatureUtil.GeometryForm;

public interface RulesListInterface {
	// ** Do not change the value, it is needed to recognize SLD **//
	static final String ALL_LABEL_CLASSES_ENABLED = "ALL_LABEL_CLASSES_ENABLED";

	public static final FilterFactory2 ff = FeatureUtil.FILTER_FACTORY2;

	/**
	 * To simplifly the usage, any ScaleDenominator above this value will be
	 * interpreted as Infinite.
	 */
	public static final double MAX_SCALEDENOMINATOR = 1E20;

	/**
	 * A Filter to mark that not ALL classes have been disabled by the
	 * {@link AbstractRuleList}{@link #setEnabled(boolean)} method. This filter
	 * is not used anymore and only for backward compatibility. Will be removed
	 * in 2.0
	 **/
	public static final PropertyIsEqualTo oldAllClassesDisabledFilter = ff
			.equals(ff.literal("1"), ff.literal("2"));
	/**
	 * A Filter to mark that not ALL classes have been disabled by the
	 * {@link AbstractRuleList}{@link #setEnabled(boolean)} method. This filter
	 * is not used anymore and only for backward compatibility. Will be removed
	 * in 2.0
	 **/
	public static final PropertyIsEqualTo OldAllClassesEnabledFilter = ff
			.equals(ff.literal("1"), ff.literal("1"));
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

	// ** Do not change the value, it is needed to recognize SLD **//
	static final String RL_FILTER_APPLIED_STR = "RL_FILTER_APPLIED";

	/**
	 * A Filter to mark that one class/rule is enabled
	 **/
	public static final PropertyIsEqualTo RL_FILTER_APPLIED_FILTER = ff.equals(
			ff.literal(RL_FILTER_APPLIED_STR),
			ff.literal(RL_FILTER_APPLIED_STR));
	/**
	 * If used as a {@link Rule}'s name, the rule should not be imported, but
	 * rather just be ignored.
	 */
	static final String RULENAME_DONTIMPORT = "DONTIMPORT";

	/**
	 * Adds a {@link RuleChangeListener} which listens to changes in the
	 * {@link Rule}. Very good to update previews.<br>
	 * <b>The listening class must keep a reference to the listener (e.g. make
	 * it a field variable) because the listeners are kept in a WeakHashSet.</b>
	 */
	public abstract void addListener(RuleChangeListener listener);

	/**
	 * Clears all {@link RuleChangeListener}s
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public abstract void clearListeners();

	/**
	 * Tells all {@link RuleChangeListener} that the {@link Rule}s represented
	 * by this {@link AbstractRulesList} implementation have changed.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public abstract void fireEvents(RuleChangedEvent rce);

	/**
	 * @return Returns the SLD {@link FeatureTypeStyle}s that represents this
	 *         RuleList. This method implemented here doesn't set the
	 *         FeatureTypeName. This is overridden in {@link FeatureRuleList}
	 */
	public abstract FeatureTypeStyle getFTS();

	public abstract GeometryForm getGeometryForm();

	/**
	 * Returns direct access to the {@link RuleChangeListener}s {@link HashSet}
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public abstract Set<RuleChangeListener> getListeners();

	public abstract double getMaxScaleDenominator();

	public abstract double getMinScaleDenominator();

	/**
	 * Gets a filter that is applied to the whole AbstractRulesList. If will be
	 * added to all filters of all rules. Returns <code>null</code> for
	 * Filter.INCLUDE
	 */
	public abstract Filter getRlFilter();

	/**
	 * @return Returns the SLD {@link Rule}s that it represents. The min- and
	 *         max-Scale Denominators are applied to all Rules.
	 */
	public abstract List<Rule> getRules();

	public abstract String getTitle();

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
	 * Imports the information stored in the FTS and then calls
	 * importRules(fts.rules());
	 */
	public abstract void importFts(FeatureTypeStyle fts);

	/**
	 * If <code>false</code>, all rules in this filter will always evaluate to
	 * false.
	 */
	public abstract boolean isEnabled();

	/**
	 * If quite, the RuleList will not fire {@link RuleChangedEvent}s
	 */
	public abstract boolean isQuite();

	/**
	 * Remove a QUITE-State from the event firing state stack
	 */
	public abstract void popQuite();

	public abstract void popQuite(RuleChangedEvent ruleChangedEvent);

	/**
	 * Add a QUITE-State to the event firing state stack
	 */
	public abstract void pushQuite();

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
	public abstract boolean removeListener(RuleChangeListener listener);

	/**
	 * If <code>false</code>, all rules in this filter will always evaluate to
	 * false.<br/>
	 * Allows to define whether all rules are enabled. If disabled, if doesn't
	 * throw away all information, but just disables rules with an Always-False
	 * filter.
	 */
	public abstract void setEnabled(boolean enabled);

	public abstract void setMaxScaleDenominator(double maxScaleDenominator);

	public abstract void setMinScaleDenominator(double minScaleDenominator);

	/**
	 * Sets a filter that is applied to the whole AbstractRulesList. If will be
	 * added to all filters of all rules. When changed, a
	 * {@link RuleChangedEvent} is fired. <code>Filter.INCLUDE</code> is changed
	 * to <code>null</code>
	 */
	public abstract void setRlFilter(Filter rlFilter);

	public abstract void setTitle(String title);

	/**
	 * The AtlasStyler stores meta information in the name tag of
	 * {@link FeatureTypeStyle}s.
	 * 
	 * @return a {@link String} that contains all information for this
	 *         particular RuleList
	 */
	// String extendMetaInfoString(String metaInfoString);

}