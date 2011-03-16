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

import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.RuleChangeListener;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Symbolizer;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import de.schmitzm.geotools.FilterUtil;
import de.schmitzm.geotools.GTUtil;
import de.schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import de.schmitzm.geotools.styling.StyledFeaturesInterface;
import de.schmitzm.geotools.styling.StyledLayerUtil;

public abstract class FeatureRuleList extends AbstractRulesList {

	public static final String METAINFO_KVP_EQUALS_CHAR = "#";

	public static final String METAINFO_SEPERATOR_CHAR = ":";

	/**
	 * When importing {@link Rule}s, rules with this name are interpreted as the
	 * "NODATA" rules. Usually there is only one.
	 */
	public static final String NODATA_RULE_NAME = "NODATA_RULE";

	/**
	 * When importing {@link Rule}s, rules with this name are interpreted as the
	 * "NODATA" rule. If running in Atlas/GP mode, this rule will NOT appear in
	 * the legend.
	 */
	public static final String NODATA_RULE_NAME_HIDEINLEGEND = NODATA_RULE_NAME
			+ "_" + StyledLayerUtil.HIDE_IN_LAYER_LEGEND_HINT;

	/**
	 * When importing {@link Rule}s, rules with this name are interpreted as the
	 * "NODATA" rule. If running in Atlas/GP mode, this rule will appear in the
	 * legend.
	 */
	public static final String NODATA_RULE_NAME_SHOWINLEGEND = NODATA_RULE_NAME;

	protected FilterFactory2 ff2 = FilterUtil.FILTER_FAC2;

	/**
	 * This {@link RuleChangeListener} is added to the template in
	 * {@link #getNoDataSymbol()} and will propagate any template changes to
	 * this rule list.
	 */
	private final RuleChangeListener listenToNoDataRLChangesAndPropageToFeatureRL = new RuleChangeListener() {

		@Override
		public void changed(RuleChangedEvent e) {
			FeatureRuleList.this.fireEvents(new RuleChangedEvent(
					"nodata symbology changed", FeatureRuleList.this));
		}
	};

	private SingleRuleList<? extends Symbolizer> noDataSymbol = null;

	final private StyledFeaturesInterface<?> styledFeatures;

	private SingleRuleList<? extends Symbolizer> template;

	public FeatureRuleList(RulesListType rulesListType, StyledFeaturesInterface<?> styledFeatures,
			GeometryForm geometryForm) {
		super(rulesListType, geometryForm);
		this.styledFeatures = styledFeatures;
	}

	// abstract public SingleRuleList<? extends Symbolizer>
	// getDefaultTemplate();
	public SingleRuleList<? extends Symbolizer> getDefaultTemplate() {
		return ASUtil.getDefaultTemplate(getGeometryForm());
	}

	/***************************************************************************
	 * @return Returns the SLD {@link FeatureTypeStyle}s that represents this
	 *         RuleList. This method implemented here does set the
	 *         FeatureTypeName in the {@link FeatureTypeStyle}.
	 */
	@Override
	public FeatureTypeStyle getFTS() {
		FeatureTypeStyle fts = super.getFTS();
		fts.featureTypeNames().add(styledFeatures.getSchema().getName());
		return fts;
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

	public StyledFeaturesInterface<?> getStyledFeatures() {
		return styledFeatures;
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
	 * Uses the Symbolizers of the {@link Rule} to create a NoDataSymbol of type
	 * {@link SingleRuleList}. The label for NODATA is stored in the rule's
	 * title field.<br/>
	 * This does not import any NODATA values. In Geopublisher the NODATA values
	 * are stored in the atlas.xml. But it would maybe be a good idea to import
	 * them here?!
	 */
	public void importNoDataRule(Rule r) {
		getNoDataSymbol().getSymbolizers().clear();
		getNoDataSymbol().addSymbolizers(r.symbolizers());
		getNoDataSymbol().setLabel(GTUtil.descriptionTitle(r.getDescription()));
		getNoDataSymbol().setTitle("NODATARULE");

		if (r.getName().toString()
				.equals(FeatureRuleList.NODATA_RULE_NAME_HIDEINLEGEND)) {
			getNoDataSymbol().setVisibleInLegend(false);
		}

		// Stub on how to parse the NODAT values...
		// Filter filter = parseAbstractRlSettings(r.getFilter());
		// Or ors = (Or) filter;
		// for (Filter ndf : ors.getChildren()) {
		// if (ndf instanceof IsNullImpl) {
		// }
		// }
	}

	/***************************************************************************
	 * ABSTRACT METHODS BEGIN HERE
	 * 
	 * @return
	 **************************************************************************/

	abstract public void importTemplate(FeatureTypeStyle importFTS);

	/**
	 * Define how to draw NODATA values (null, NaN, Inf) by setting a Color and
	 * Opacity that will be used {@link SingleRuleList}
	 */
	public void setNoDataSymbol(Color color, double opacity) {

		if (color == null)
			throw new IllegalArgumentException("Color may not be null!");

		// getNoDataSymbol().getSymbolizers().clear();
		// Symbolizer symbolizer = getGeometryForm()

		// Style style = FeatureUtil.createDefaultStyle(getGeometryForm());
		// Symbolizer symbolizer =
		// style.featureTypeStyles().get(0).rules().get(0).symbolizers().get(0);

		SingleRuleList<? extends Symbolizer> rl = ASUtil
				.getDefaultNoDataSymbol(getGeometryForm(), opacity, color,
						color);
		// rl.copyTo(getNoDataSymbol());

		noDataSymbol = rl;
	}

	/**
	 * Define how to draw NODATA values (null, NaN, Inf) by setting a
	 * {@link SingleRuleList}
	 */
	public void setNoDataSymbol(
			SingleRuleList<? extends Symbolizer> noDataRuleList) {
		if (noDataRuleList == null) {
			noDataSymbol = null;
		} else {
			// Maybe better: noDataSymbol = noDataRuleList; ?
			noDataRuleList.copyTo(getNoDataSymbol());
		}
	}

	/**
	 * Define how to draw NODATA values (null, NaN, Inf) by setting a single
	 * {@link Symbolizer}
	 */
	public void setNoDataSymbol(Symbolizer noDataSymbolizer) {
		if (noDataSymbolizer == null) {
			noDataSymbol = null;
		} else {
			// Maybe better: noDataSymbol = noDataRuleList; ?
			getNoDataSymbol().getSymbolizers().clear();
			getNoDataSymbol().addSymbolizer(noDataSymbolizer);
		}
	}

	/**
	 * Sets a template Symbol used for this color graduation
	 * 
	 * @param template
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public void setTemplate(SingleRuleList<? extends Symbolizer> template) {
		this.template = template;
		fireEvents(new RuleChangedEvent("Set template", this));
	}

}
