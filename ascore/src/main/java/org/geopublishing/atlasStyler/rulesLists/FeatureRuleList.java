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
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.RuleChangeListener;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geotools.filter.AndImpl;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Symbolizer;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.PropertyIsBetween;

import de.schmitzm.geotools.FilterUtil;
import de.schmitzm.geotools.GTUtil;
import de.schmitzm.geotools.feature.FeatureUtil;
import de.schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import de.schmitzm.geotools.styling.StyledFeaturesInterface;
import de.schmitzm.geotools.styling.StyledLayerUtil;

public abstract class FeatureRuleList extends AbstractRulesList {

	private static final Logger LOGGER = Logger
			.getLogger(FeatureRuleList.class);

	/**
	 * The children of this class define most metainfo. Some metainformation is
	 * the same for all children and is added here.
	 * 
	 * @param metaInfoString
	 *            The metaInfoString starting with a getType()-result and some
	 *            KVPs
	 * 
	 * @return an expanded {@link String} with more KVPs
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	@Override
	public String extendMetaInfoString() {
		String metaInfoString = super.extendMetaInfoString();
		metaInfoString += METAINFO_SEPERATOR_CHAR + KVP_VALUE_FIELD
				+ METAINFO_KVP_EQUALS_CHAR + getValue_field_name();
		metaInfoString += METAINFO_SEPERATOR_CHAR + KVP_NORMALIZATION_FIELD
				+ METAINFO_KVP_EQUALS_CHAR + getNormalizer_field_name();
		return metaInfoString;
	}

	/**
	 * @param filter
	 *            A {@link Filter}
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 * 
	 * @return <code>null</code> if it is not a "BetweenFilter"
	 */
	public static double[] interpretBetweenFilter(Filter filter) {

		if (filter instanceof AndImpl) {
			// This is a AND ( NOT ( NODATA ) , BETWEENFILTER) construction
			// We continue the interpretion with only the last filter
			Iterator<Filter> fi = ((AndImpl) filter).getFilterIterator();
			while (fi.hasNext())
				filter = fi.next();
		}

		if (filter instanceof PropertyIsBetween) {
			PropertyIsBetween betweenFilter = (PropertyIsBetween) filter;
			double lower = Double.parseDouble(betweenFilter.getLowerBoundary()
					.toString());
			double upper = Double.parseDouble(betweenFilter.getUpperBoundary()
					.toString());
			return new double[] { lower, upper };
		}
		throw new RuntimeException("Unparsable Filter " + filter);
	}

	/**
	 * The children of this class parse most metainfo. Some metainformation is
	 * the same for all children and is parsed here. This method must be called
	 * from all children.
	 * 
	 * @param metaInfoString
	 *            The metaInfoString cleared from with a getType()-result and
	 *            some KVPs
	 * 
	 * @return an expanded {@link String} with more KVPs
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	protected void parseMetaInfoString(String metaInfoString) {

		/***********************************************************************
		 * Parsing a list of Key-Value Pairs from the FeatureTypeStyleName
		 */
		String[] kvp = new String[] { "uninitialized", "uninit" };
		String value = "nothging yet";
		try {

			String[] params = metaInfoString.split(METAINFO_SEPERATOR_CHAR);
			for (String p : params) {
				kvp = p.split(METAINFO_KVP_EQUALS_CHAR);

				if (kvp[0].equalsIgnoreCase(KVP_NORMALIZATION_FIELD.toString())) {
					value = kvp[1];
					if (value.equalsIgnoreCase("null"))
						setNormalizer_field_name(null);
					else
						setNormalizer_field_name(FeatureUtil
								.findBestMatchingAttribute(
										getStyledFeatures().getSchema(), value)
								.getLocalPart());
				}

				if (kvp[0].equalsIgnoreCase(KVP_VALUE_FIELD.toString())) {
					value = kvp[1];
					if (value.equalsIgnoreCase("null"))
						setValue_field_name(null);
					else
						setValue_field_name(FeatureUtil
								.findBestMatchingAttributeFallBackFirstNumeric(
										getStyledFeatures().getSchema(), kvp[1])
								.getLocalPart());
				}
			}

		} catch (RuntimeException e) {
			LOGGER.error("KVP=" + kvp[0] + kvp[1]);
			LOGGER.error("VALUE=" + value);

			throw (e);
		}
	}

	/**
	 * @return the normalizer_field_name
	 */
	public String getNormalizer_field_name() {
		return normalizer_field_name;
	}

	/**
	 * @param normalizer_field_name
	 *            the normalizer_field_name to set
	 */
	final public void setNormalizer_field_name(String normalizer_field_name) {
		this.normalizer_field_name = normalizer_field_name;
	}

	/**
	 * @param value_field_name
	 *            the value_field_name to set
	 */
	public void setValue_field_name(String value_field_name) {
		this.value_field_name = value_field_name;
	}

	/**
	 * The {@link String} name of the attribute which contains the quantity
	 * values
	 */
	private String value_field_name;

	/**
	 * The {@link String} name of the attribute used to normalize the quantity
	 * attribute
	 */
	private String normalizer_field_name;

	/**
	 * @return the value_field_name
	 */
	final public String getValue_field_name() {
		return value_field_name;
	}

	/** KEY-name for the KVPs in the meta information * */
	protected static final String KVP_NORMALIZATION_FIELD = "NORM";

	/** KEY-name for the KVPs in the meta information * */
	private static final String KVP_VALUE_FIELD = "VALUE";

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

	public FeatureRuleList(RulesListType rulesListType,
			StyledFeaturesInterface<?> styledFeatures,
			GeometryForm geometryForm, boolean withDefaults) {
		super(rulesListType, geometryForm);

		this.styledFeatures = styledFeatures;

		// Initialize value_field_name if "withDefaults"
		Collection<String> numericalFieldNames = FeatureUtil
				.getNumericalFieldNames(getStyledFeatures().getSchema(), false);
		if (numericalFieldNames.size() > 0)
			value_field_name = numericalFieldNames.toArray(new String[] {})[0];

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
