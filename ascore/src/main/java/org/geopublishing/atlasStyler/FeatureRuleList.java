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

import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Symbolizer;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import schmitzm.geotools.FilterUtil;
import schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import skrueger.geotools.StyledFeaturesInterface;
import skrueger.geotools.StyledLayerUtil;

public abstract class FeatureRuleList extends AbstractRuleList {

	/**
	 * When importing {@link Rule}s, rules with this name are interpreted as the
	 * "NODATA" rule.
	 */
	public static final String NODATA_RULE_NAME = "NODATA_RULE";

	/**
	 * When importing {@link Rule}s, rules with this name are interpreted as the
	 * "NODATA" rule. If running in Atlas/GP mode, this rule will appear in the
	 * legend.
	 */
	public static final String NODATA_RULE_NAME_SHOWINLEGEND = NODATA_RULE_NAME;

	/**
	 * When importing {@link Rule}s, rules with this name are interpreted as the
	 * "NODATA" rule. If running in Atlas/GP mode, this rule will NOT appear in
	 * the legend.
	 */
	public static final String NODATA_RULE_NAME_HIDEINLEGEND = NODATA_RULE_NAME
			+ "_" + StyledLayerUtil.HIDE_IN_LAYER_LEGEND_HINT;

	final private StyledFeaturesInterface<?> styledFeatures;

	public FeatureRuleList(StyledFeaturesInterface<?> styledFeatures) {
		this.styledFeatures = styledFeatures;
	}

	public StyledFeaturesInterface<?> getStyledFeatures() {
		return styledFeatures;
	}

	/**
	 * This {@link RuleChangeListener} is added to the template in
	 * {@link #getNoDataSymbol()} and will propagate any template changes to
	 * this rule list.
	 */
	private RuleChangeListener listenToNoDataRLChangesAndPropageToFeatureRL = new RuleChangeListener() {

		@Override
		public void changed(RuleChangedEvent e) {
			FeatureRuleList.this.fireEvents(new RuleChangedEvent(
					"nodata symbology changed", FeatureRuleList.this));
		}
	};

	protected FilterFactory2 ff2 = FilterUtil.FILTER_FAC2;

	private SingleRuleList<? extends Symbolizer> template;

	public static final String METAINFO_SEPERATOR_CHAR = ":";

	public static final String METAINFO_KVP_EQUALS_CHAR = "#";

	/***************************************************************************
	 * @return Returns the SLD {@link FeatureTypeStyle}s that represents this
	 *         RuleList. This method implemented here does set the
	 *         FeatureTypeName in the {@link FeatureTypeStyle}.
	 */
	@Override
	public FeatureTypeStyle getFTS() {
		FeatureTypeStyle ftstyle = ASUtil.SB.createFeatureTypeStyle(
				styledFeatures.getSchema().getTypeName(), getRules().toArray(
						new Rule[] {}));
		ftstyle.setName(getAtlasMetaInfoForFTSName());
		return ftstyle;
	}

	/***************************************************************************
	 * TEMPLATE STUFF
	 */

	public SingleRuleList<? extends Symbolizer> getTemplate() {
		if (template == null)
			return getDefaultTemplate();
		return template;
	}

	/**
	 * Sets a template Symbol used for this color graduation
	 * 
	 * @param template
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Tzeggai</a>
	 */
	public void setTemplate(SingleRuleList<? extends Symbolizer> template) {
		this.template = template;
		fireEvents(new RuleChangedEvent("Set template", this));
	}

	/***************************************************************************
	 * ABSTRACT METHODS BEGIN HERE
	 * 
	 * @return
	 **************************************************************************/

	abstract public void importTemplate(FeatureTypeStyle importFTS);

	// abstract public SingleRuleList<? extends Symbolizer>
	// getDefaultTemplate();
	public SingleRuleList<? extends Symbolizer> getDefaultTemplate() {
		return ASUtil.getDefaultTemplate(getGeometryForm());
	}

	/**
	 * Return the {@link Filter} that will catch all NODATA values
	 */
	abstract public Filter getNoDataFilter();

	/**
	 * Return a {@link SingleRuleList} that shall be used to paint all NODATA
	 * values. If <code>null</code>, then all features matching the
	 * {@link #getNoDataFilter()} shall not be painted at all.
	 */
	public SingleRuleList<? extends Symbolizer> getNoDataSymbol() {
		if (noDataSymbol == null) {
			noDataSymbol = ASUtil.getDefaultNoDataSymbol(getGeometryForm());
		}
		noDataSymbol.addListener(listenToNoDataRLChangesAndPropageToFeatureRL);
		return noDataSymbol;
	}

	/**
	 * Must be overwritten in the class that is specific for line, point or
	 * polygons
	 **/
	public abstract GeometryForm getGeometryForm();

	private SingleRuleList<? extends Symbolizer> noDataSymbol = null;

	/**
	 * Will remove all other {@link Symbolizer}s and add the symbolizers of the
	 * given rule to the {@link noDataSymbol}.
	 */
	public void importNoDataRule(Rule r) {
		getNoDataSymbol().getSymbolizers().clear();
		getNoDataSymbol().addSymbolizers(r.symbolizers());
		getNoDataSymbol().setTitle(r.getDescription().getTitle().toString());

		if (r.getName().toString().equals(
				FeatureRuleList.NODATA_RULE_NAME_HIDEINLEGEND)) {
			getNoDataSymbol().setVisibleInLegend(false);
		}
	}

}
