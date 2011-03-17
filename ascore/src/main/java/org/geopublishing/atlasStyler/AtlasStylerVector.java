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

import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.rulesLists.AbstractRulesList;
import org.geopublishing.atlasStyler.rulesLists.FeatureRuleList;
import org.geopublishing.atlasStyler.rulesLists.RulesListInterface;
import org.geopublishing.atlasStyler.rulesLists.SingleRuleList;
import org.geotools.data.FeatureSource;
import org.geotools.map.MapLayer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Style;
import org.geotools.styling.Symbolizer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.expression.Literal;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

import de.schmitzm.geotools.FilterUtil;
import de.schmitzm.geotools.data.amd.AttributeMetadataImplMap;
import de.schmitzm.geotools.data.amd.AttributeMetadataInterface;
import de.schmitzm.geotools.data.amd.AttributeMetadataMap;
import de.schmitzm.geotools.feature.FeatureUtil;
import de.schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import de.schmitzm.geotools.styling.StyledFS;
import de.schmitzm.geotools.styling.StyledFeaturesInterface;
import de.schmitzm.geotools.styling.StyledLayerInterface;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.lang.LangUtil;

/**
 * The {@link AtlasStylerVector} is a class to create SLD documents for a
 * {@link FeatureSource}. It contains no GUI.
 * 
 * @author Stefan A. Tzeggai
 * 
 */
public class AtlasStylerVector extends AtlasStyler {

	/** All {@link AtlasStylerVector} related files will be saved blow this path */
	private static File applicationPreferencesDir;

	/**
	 * Default size of a symbol in any AtlasStyler previews
	 */
	public static final Dimension DEFAULT_SYMBOL_PREVIEW_SIZE = new Dimension(
			30, 30);

	/** These DIRNAMEs describe paths to application files on the local machines */
	final static String DIRNAME_LINE = "line";

	/** These DIRNAMEs describe paths to application files on the local machines */
	final static String DIRNAME_POINT = "point";

	/***************************************************************************
	 * Paths
	 */

	/** These DIRNAMEs describe paths to application files on the local machines */
	final static String DIRNAME_POLYGON = "polygon";

	// // TODO ??
	// /** These DIRNAMEs describe paths to application files on the local
	// machines */
	// final static String DIRNAME_ANY = "any";

	private final static Logger LOGGER = LangUtil
			.createLogger(AtlasStylerVector.class);

	/**
	 * @return A {@link File} pointing to USER_HOME_DIR/.AtlasSLDEditor
	 */
	public static File getApplicationPreferencesDir() {
		if (applicationPreferencesDir == null) {
			applicationPreferencesDir = new File(new File(
					System.getProperty("user.home")), ".AtlasStyler");
			applicationPreferencesDir.mkdirs();
		}
		return applicationPreferencesDir;
	}

	/**
	 * @return a {@link File} that points to the base folder for AtlasStyler
	 *         templates
	 */
	private static File getBaseSymbolsDir() {
		return new File(AtlasStylerVector.getApplicationPreferencesDir(),
				DIRNAME_TEMPLATES);
	}

	/**
	 * Returns the list of default font families that are expected to work on
	 * any system. The returned {@link List} contains alternative names for all
	 * the font-families.
	 */
	public static List<Literal>[] getDefaultFontFamilies() {

		ArrayList<Literal>[] fontFamilies = new ArrayList[5];

		/**
		 * Every group represents the aliases of similar fonts on different
		 * systems. @see http://www.ampsoft.net/webdesign-l/WindowsMacFonts.html
		 */
		fontFamilies[0] = new ArrayList<Literal>();
		fontFamilies[0].add(FilterUtil.FILTER_FAC.literal("Arial"));
		fontFamilies[0].add(FilterUtil.FILTER_FAC.literal("Helvetica"));
		fontFamilies[0].add(FilterUtil.FILTER_FAC.literal("sans-serif"));

		fontFamilies[1] = new ArrayList<Literal>();
		fontFamilies[1].add(FilterUtil.FILTER_FAC.literal("Arial Black"));
		fontFamilies[1].add(FilterUtil.FILTER_FAC.literal("Gadget"));
		fontFamilies[1].add(FilterUtil.FILTER_FAC.literal("sans-serif"));

		fontFamilies[2] = new ArrayList<Literal>();
		fontFamilies[2].add(FilterUtil.FILTER_FAC.literal("Courier New"));
		fontFamilies[2].add(FilterUtil.FILTER_FAC.literal("Courier"));
		fontFamilies[2].add(FilterUtil.FILTER_FAC.literal("monospace"));

		fontFamilies[3] = new ArrayList<Literal>();
		fontFamilies[3].add(FilterUtil.FILTER_FAC.literal("Times New Roman"));
		fontFamilies[3].add(FilterUtil.FILTER_FAC.literal("Times"));
		fontFamilies[3].add(FilterUtil.FILTER_FAC.literal("serif"));

		fontFamilies[4] = new ArrayList<Literal>();
		fontFamilies[4].add(FilterUtil.FILTER_FAC.literal("Impact"));
		fontFamilies[4].add(FilterUtil.FILTER_FAC.literal("Charcoal"));
		fontFamilies[4].add(FilterUtil.FILTER_FAC.literal("sans-serif"));

		return fontFamilies;
	}

	/**
	 * @return A {@link File} object pointing to the directory where the local
	 *         LINE symbol library is saved.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public static File getLineSymbolsDir() {
		final File dir = new File(getBaseSymbolsDir(), DIRNAME_LINE);
		dir.mkdirs();
		return dir;
	}

	/**
	 * @return A {@link File} object pointing to the directory where the local
	 *         POINT symbol library is saved.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public static File getPointSymbolsDir() {
		final File dir = new File(getBaseSymbolsDir(), DIRNAME_POINT);
		dir.mkdirs();
		return dir;
	}

	/**
	 * @return A {@link File} object pointing to the directory where the local
	 *         POLYGON symbol library is saved.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public static File getPolygonSymbolsDir() {
		final File dir = new File(getBaseSymbolsDir(), DIRNAME_POLYGON);
		dir.mkdirs();
		return dir;
	}

	public static File getSymbolsDir(final GeometryForm defaultGeometry) {

		switch (defaultGeometry) {
		case LINE:
			return getLineSymbolsDir();
		case POINT:
			return getPointSymbolsDir();
		case POLYGON:
			return getPolygonSymbolsDir();
		case ANY:
		}

		final String msg = "GeometryForm not recognized = " + defaultGeometry;
		LOGGER.error(msg);
		throw new IllegalArgumentException(msg);
	}

	/**
	 * Holds optional meta-information about the data-source. If not set, this
	 * empty Map is used. Otherwise meta like column name description will be
	 * looked up here.
	 **/
	private AttributeMetadataMap<? extends AttributeMetadataInterface> attributeMetaDataMap = new AttributeMetadataImplMap();

	private final StyledFeaturesInterface<?> styledFeatures;

	/**
	 * Create an AtlasStyler object for any {@link FeatureSource}
	 */
	public AtlasStylerVector(
			FeatureSource<SimpleFeatureType, SimpleFeature> featureSource) {
		this(new StyledFS(featureSource));
	}

	/**
	 * Create an AtlasStyler object for any {@link FeatureSource} and import the
	 * given {@link Style}.
	 */
	public AtlasStylerVector(
			FeatureSource<SimpleFeatureType, SimpleFeature> featureSource,
			Style style) {
		this(new StyledFS(featureSource), style, null, null, null);
	}

	/**
	 * Create an {@link AtlasStylerVector} object for any
	 * {@link StyledFeaturesInterface}
	 */
	public AtlasStylerVector(StyledFeaturesInterface<?> styledFeatures) {
		this(styledFeatures, null, null, null, null);
	}

	/***************************************************************************
	 * Constructor that starts styling a {@link StyledFeaturesInterface}. Loads
	 * the given paramter <code>loadStyle</code> {@link Style} at construction
	 * time. The parameters mapLegend and maplegend are needed to open the
	 * Filter dialog.
	 * 
	 * @param StyledFeaturesInterface
	 *            Where the features come from.
	 * @param mapLegend
	 *            may be <code>null</code>
	 * @param mapLayer
	 *            may be <code>null</code>
	 * @param withDefaults
	 *            If <code>true</code>, and no RuleList can be imported, a
	 *            default Rulelist will be added.
	 */
	public AtlasStylerVector(final StyledFeaturesInterface<?> styledFeatures,
			Style loadStyle, final MapLayer mapLayer,
			HashMap<String, Object> params, Boolean withDefaults) {
		super(mapLayer, params, withDefaults);

		setTitle(styledFeatures.getTitle());

		this.styledFeatures = styledFeatures;
		this.rlf = new RuleListFactory(styledFeatures);

		if (loadStyle != null) {
			// Correct propertynames against the Schema
			loadStyle = StylingUtil.correctPropertyNames(loadStyle,
					styledFeatures.getSchema());
			importStyle(loadStyle);
		} else {
			if (styledFeatures.getStyle() != null) {
				importStyle(styledFeatures.getStyle());
			} else {

				if (withDefaults != null && withDefaults == true) {
					final SingleRuleList<? extends Symbolizer> defaultRl = rlf
							.createSingleRulesList(
									getRuleTitleFor(styledFeatures), true);
					LOGGER.debug("Added default rulelist: " + defaultRl);
					addRulesList(defaultRl);
				}
			}
		}
		setAttributeMetaDataMap(styledFeatures.getAttributeMetaDataMap());
	}

	/**
	 * When switching the ruleliste GUI, the user may be asked to use a single
	 * symbol as a template in another {@link FeatureRuleList}.
	 * 
	 * @param oldRl
	 *            previously selected RuleList
	 * @param newRl
	 *            newly selected RuleList
	 */
	void askToTransferTemplates(RulesListInterface oldRl, FeatureRuleList newRl) {
		if (oldRl == null || newRl == null || oldRl == newRl)
			return;
		if (oldRl.getGeometryForm() != newRl.getGeometryForm())
			return;
		/**
		 * Trying it this way: If we switched from a SinglePointSymbolRuleList,
		 * let it be this GraduatedPointColorRuleList's template.
		 */
		if (oldRl instanceof SingleRuleList) {
			SingleRuleList<Symbolizer> oldSingleRl = (SingleRuleList<Symbolizer>) oldRl;
			final SingleRuleList<Symbolizer> singleRL = oldSingleRl;

			if (!StylingUtil.isStyleDifferent(singleRL.getFTS(), newRl
					.getTemplate().getFTS()))
				return;

			int res = JOptionPane.NO_OPTION;

			if (owner != null) {
				res = JOptionPane
						.showConfirmDialog(
								owner,
								R("AtlasStyler.SwitchRuleListType.CopySingleSymbolAsTemplate"));
			}
			if (res == JOptionPane.YES_OPTION) {
				newRl.setTemplate(singleRL.copy());
			}
		}

		/**
		 * Trying it this way: If we switched from a SinglePointSymbolRuleList,
		 * let it be this GraduatedPointColorRuleList's template.
		 */
		if (oldRl instanceof FeatureRuleList) {
			FeatureRuleList oldFeatureRl = (FeatureRuleList) oldRl;
			final SingleRuleList<? extends Symbolizer> oldTemplate = oldFeatureRl
					.getTemplate();

			if (!StylingUtil.isStyleDifferent(oldTemplate.getFTS(), newRl
					.getTemplate().getFTS()))
				return;

			int res = JOptionPane.NO_OPTION;

			if (owner != null) {
				res = JOptionPane.showConfirmDialog(null,
						R("AtlasStyler.SwitchRuleListType.CopyTemplate"));
			}

			if (res == JOptionPane.YES_OPTION) {
				newRl.setTemplate(oldTemplate.copy());
			}
		}

	}

	/**
	 * When switching the ruleliste GUI, the user may be asked to use a selected
	 * template as a new {@link SingleRuleList}
	 * 
	 * @param oldRl
	 *            previously selected RuleList
	 * @param newRl
	 *            newly selected RuleList
	 */
	void askToTransferTemplates(RulesListInterface oldRl, SingleRuleList newRl) {

		if (oldRl == null || oldRl == newRl)
			return;
		if (oldRl.getGeometryForm() != newRl.getGeometryForm())
			return;

		/**
		 * Trying it this way: If we switched from a
		 * GraduatedColorPointRuleList, let this SinglePointSymbolRuleList be
		 * the GraduatedColorPointRuleList's template.
		 */
		if (oldRl instanceof FeatureRuleList) {
			FeatureRuleList oldFeatureRl = (FeatureRuleList) getLastChangedRuleList();
			final SingleRuleList oldTemplate = oldFeatureRl.getTemplate();

			if (!StylingUtil.isStyleDifferent(oldTemplate.getFTS(),
					newRl.getFTS()))
				return;

			int res = JOptionPane.NO_OPTION;
			if (owner != null) {
				JOptionPane.showConfirmDialog(null,
						R("AtlasStyler.SwitchRuleListType.CopyTemplate"));
			}

			if (res == JOptionPane.YES_OPTION) {
				newRl.setSymbolizers(oldTemplate.getSymbolizers());
			}
		}

		/**
		 * Trying it this way: If we switched from a SinglePointSymbolRuleList,
		 * let it be this GraduatedPointColorRuleList's template.
		 */
		if (oldRl instanceof SingleRuleList) {
			// TODO Untested, since it can't happen yet
			SingleRuleList oldSingleRl = (SingleRuleList) oldRl;

			if (!StylingUtil.isStyleDifferent(oldSingleRl.getFTS(),
					newRl.getFTS()))
				return;

			int res = JOptionPane.NO_OPTION;

			if (owner != null) {
				res = JOptionPane
						.showConfirmDialog(
								null,
								R("AtlasStyler.SwitchRuleListType.CopySingleSymbolAsSingleSymbol"));
			}
			if (res == JOptionPane.YES_OPTION)
				newRl.setSymbolizers(oldSingleRl.getSymbolizers());
		}
	}

	/**
	 * Creates a copy of any given RulesList
	 */
	@Override
	public AbstractRulesList copyRulesList(RulesListInterface rl) {
		FeatureTypeStyle fts = rl.getFTS();
		try {
			return getRlf().importFts(fts, false);
		} catch (AtlasStylerParsingException e) {
			LOGGER.warn("Trying to copy RL=" + rl + " failed.", e);
			return null;
		}
	}

	public AttributeMetadataMap<? extends AttributeMetadataInterface> getAttributeMetaDataMap() {
		return attributeMetaDataMap;
	}

	/**
	 * Returns a list of available fonts: A combination of the the
	 * AtlasStyler.getDefaultFontFamilies() and the fonts passed to
	 * {@link AtlasStylerVector} on construction.
	 */
	public List<Literal>[] getAvailableFonts() {
		List<Literal>[] fontFamilies = AtlasStylerVector
				.getDefaultFontFamilies();

		/**
		 * Add user defined Fonts. One Family for every extra font.
		 */
		List<Font> extraFonts = getFonts();

		int i = fontFamilies.length;
		for (Font f : extraFonts) {
			fontFamilies = LangUtil.extendArray(fontFamilies,
					new ArrayList<Literal>());
			fontFamilies[i].add(FilterUtil.FILTER_FAC.literal(f.getName()));
			i++;
		}
		return fontFamilies;
	}

	@Override
	StyleChangedEvent getStyleChangeEvent() {
		return new StyleChangedEvent(getStyle());
	}

	public StyledFeaturesInterface<?> getStyledFeatures() {
		return styledFeatures;
	}

	@Override
	public StyledLayerInterface<?> getStyledInterface() {
		return styledFeatures;
	}

	/**
	 * Convenience method to indicate if the {@link FeatureSource} is of type
	 * {@link LineString}.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public boolean isLineString() {
		return FeatureUtil.getGeometryForm(getStyledFeatures()
				.getFeatureSource()) == GeometryForm.LINE;
	}

	/**
	 * Convenience method to indicate if the {@link FeatureSource} is of type
	 * {@link Polygon}.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public boolean isPoint() {
		return FeatureUtil.getGeometryForm(getStyledFeatures()
				.getFeatureSource()) == GeometryForm.POINT;
	}

	/**
	 * Convenience method to indicate if the {@link FeatureSource} is of type
	 * Polygon.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public boolean isPolygon() {

		// TODO rethink?! ANY != POLYGON
		if (FeatureUtil.getGeometryForm(getStyledFeatures().getFeatureSource()) == GeometryForm.ANY)
			return true;

		return FeatureUtil.getGeometryForm(getStyledFeatures()
				.getFeatureSource()) == GeometryForm.POLYGON;
	}

	@Override
	public Style sanitize(Style style) {
		return StylingUtil.correctPropertyNames(styleCached,
				getStyledFeatures().getSchema());
	}

	public void setAttributeMetaDataMap(
			final AttributeMetadataMap<? extends AttributeMetadataInterface> attributeMetaDataMap) {
		this.attributeMetaDataMap = attributeMetaDataMap;
	}

}
