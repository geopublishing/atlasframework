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

import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Symbolizer;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import schmitzm.geotools.FilterUtil;
import skrueger.geotools.StyledFeaturesInterface;

public abstract class FeatureRuleList extends AbstractRuleList {
	/**
	 * Special NODATA value. When exporting by {@link #getRules()}, this value
	 * is translated to the special "NODATA" rule *
	 */
	public static final String NODATA_RULE_NAME = "NODATA_RULE_VALUES";

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
	 *         Kr&uuml;ger</a>
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

	abstract public SingleRuleList<? extends Symbolizer> getDefaultTemplate();

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
			noDataSymbol = getTemplate().copy();
//			for (Rule r: noDataSymbol.getRules()) {
//				r.setName(NODATA_RULE_NAME);
//			}
		}
		noDataSymbol.addListener(listenToNoDataRLChangesAndPropageToFeatureRL);
		return noDataSymbol;
	}

	private SingleRuleList<? extends Symbolizer> noDataSymbol = null;

	/**
	 * Will remove all other {@link Symbolizer}s and add the symbolizers of the
	 * given rule to the {@link noDataSymbol}.
	 */
	public void importNoDataRule(Rule r) {
		getNoDataSymbol().getSymbolizers().clear();
		getNoDataSymbol().addSymbolizers(r.symbolizers());
		getNoDataSymbol().getRules().get(0).setName(FeatureRuleList.NODATA_RULE_NAME);
	}

}
