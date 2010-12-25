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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.AbstractRulesList.RulesListType;
import org.geotools.data.FeatureSource;
import org.geotools.map.MapLayer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Style;
import org.geotools.styling.Symbolizer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.expression.Literal;

import schmitzm.geotools.FilterUtil;
import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import schmitzm.geotools.styling.StylingUtil;
import schmitzm.lang.LangUtil;
import skrueger.AttributeMetadataInterface;
import skrueger.geotools.AttributeMetadataImplMap;
import skrueger.geotools.AttributeMetadataMap;
import skrueger.geotools.StyledFS;
import skrueger.geotools.StyledFeaturesInterface;
import skrueger.i8n.I8NUtil;
import skrueger.i8n.Translation;
import skrueger.versionnumber.ReleaseUtil;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

/**
 * The {@link AtlasStyler} is a class to create SLD documents for a
 * {@link FeatureSource}. It contains no GUI.
 * 
 * @author Stefan A. Tzeggai
 * 
 */
public class AtlasStyler {
	/**
	 * The {@link AtlasStyler} can run in two {@link LANGUAGE_MODE}s.
	 */
	public enum LANGUAGE_MODE {

		/**
		 * Use atlas extension which codes many translations into the title
		 * field of rules.
		 */
		ATLAS_MULTILANGUAGE,

		/**
		 * Follow OGC standard and put simple Strings into the title field of
		 * rules.
		 */
		OGC_SINGLELANGUAGE
	}

	/**
	 * List of {@link AbstractRulesList}s that {@link AtlasStyler} is combining
	 * to one {@link Style}.
	 */
	private final RulesListsList ruleLists = new RulesListsList();

	/** All {@link AtlasStyler} related files will be saved blow this path */
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

	// TODO ??
	/** These DIRNAMEs describe paths to application files on the local machines */
	final static String DIRNAME_ANY = "any";

	/** These DIRNAMEs describe paths to application files on the local machines */
	final static String DIRNAME_TEMPLATES = "templates";

	/**
	 * Default languageMode is LANGUAGE_MODE.OGC_SINGLELANGUAGE
	 */
	static LANGUAGE_MODE languageMode = LANGUAGE_MODE.OGC_SINGLELANGUAGE;

	/**
	 * List of LanguageCodes that the targeted Atlas is supposed to "speak".
	 */
	private static List<String> languages = new LinkedList<String>();

	private final static Logger LOGGER = LangUtil.createLogger(AtlasStyler.class);

	/**
	 * Key for a parameter of type List<Font> of additional Fonts that are
	 * available
	 */
	public final static String PARAM_FONTS_LIST_FONT = "PARAM_FONTS_LIST_FONT";

	/**
	 * Key for a parameter of type List<String> that contains the languages the
	 * SLD is created for (not SLD standard, only used if AS is run within GP)
	 */
	public final static String PARAM_LANGUAGES_LIST_STRING = "PARAM_LANGUAGES_LIST_STRING";

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
		return new File(AtlasStyler.getApplicationPreferencesDir(),
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
	 * The {@link AtlasStyler} can run in two {@link LANGUAGE_MODE}s. TODO
	 * remove static
	 */
	public static LANGUAGE_MODE getLanguageMode() {
		return languageMode;
	}

	/**
	 * If we are running in {@link LANGUAGE_MODE} ATLAS_MULTILANGUAGE, these are
	 * the supported languages.<br/>
	 * TODO remove static
	 */
	public final static List<String> getLanguages() {
		return languages;
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
	 * @Deprecated use AsUtil.R
	 */
	public static String R(String key, final Object... values) {
		return ASUtil.R(key, values);
	}

	/**
	 * Holds optional meta-information about the data-source. If not set, this
	 * empty Map is used. Otherwise meta like column name description will be
	 * looked up here.
	 **/
	private AttributeMetadataMap<? extends AttributeMetadataInterface> attributeMetaDataMap = new AttributeMetadataImplMap();

	/**
	 * If false, the {@link AtlasStyler} only fires {@link StyleChangedEvent}s
	 * when the dialog is closed.
	 */
	boolean automaticPreview = ASProps.getInt(ASProps.Keys.automaticPreview, 1) == 1;

	private final Double avgNN = null;

	/**
	 * *Backup of the {@link Style} as it was before the AtlasStyle touched it.
	 * Used when {@link #cancel()} is called.
	 */
	private Style backupStyle;

	/**
	 * A list of fonts that will be available for styling in extension to the
	 * default font families. {@link #getDefaultFontFamilies()}
	 */
	private List<Font> fonts = new ArrayList<Font>();

	private AbstractRulesList lastChangedRuleList;

	/**
	 * This listener is attached to all rule lists and propagates any events as
	 * {@link StyleChangedEvent}s to the listeners of the {@link AtlasStyler} *
	 */
	private final RuleChangeListener listenerFireStyleChange = new RuleChangeListener() {

		@Override
		public void changed(final RuleChangedEvent e) {
			styleCached = null;

			// SPEED OPTIMIZATION: Here we check whether the RuleChangedEvent is
			// a min/max change. Next we check if a preview pane is set..
			// TODO not finished SPEED OPTIMIZATION
			if (RuleChangedEvent.RULE_CHANGE_EVENT_MINMAXSCALE_STRING.equals(e
					.getReason())) {
				Object ov = e.getOldValue();
				Object nv = e.getNewValue();
				// if (getPreviewScale() != null) {
				//
				// }
			}

			// Only the lastChangedRule will be used to create the Style
			final AbstractRulesList someRuleList = e.getSourceRL();

			if (!(someRuleList instanceof TextRuleList)) {
				lastChangedRuleList = someRuleList;
			}

			fireStyleChangedEvents();
		}
	};

	/***************************************************************************
	 * Listeners to style changes
	 */
	Set<StyleChangeListener> listeners = new HashSet<StyleChangeListener>();

	// private final MapLegend mapLegend;
	private final MapLayer mapLayer;

	/** If true, no Events will be fired to listeners */
	private boolean quite = true;

	private final StyledFeaturesInterface<?> styledFeatures;

	private Translation title;

	/**
	 * The cache for the {@link Style} that is generated when
	 * {@link #getStyle()} is called.
	 */
	protected Style styleCached = null;

	/**
	 * Create an AtlasStyler object for any {@link FeatureSource}
	 */
	public AtlasStyler(
			FeatureSource<SimpleFeatureType, SimpleFeature> featureSource) {
		this(new StyledFS(featureSource));
	}

	/**
	 * If not <code>null</code>, swing dialogs might popup.
	 */
	private Component owner = null;

	/**
	 * This factory is used to create empty or default rule lists.
	 */
	private final RuleListFactory rlf;

	/**
	 * A list of all exceptions/problems that occurred during import.
	 */
	final private List<Exception> importErrorLog = new ArrayList<Exception>();

	/**
	 * Create an AtlasStyler object for any {@link FeatureSource} and import the
	 * given {@link Style}.
	 */
	public AtlasStyler(
			FeatureSource<SimpleFeatureType, SimpleFeature> featureSource,
			Style style) {
		this(new StyledFS(featureSource), style, null, null, null);
	}

	/**
	 * Create an {@link AtlasStyler} object for any
	 * {@link StyledFeaturesInterface}
	 */
	public AtlasStyler(StyledFeaturesInterface<?> styledFeatures) {
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
	public AtlasStyler(final StyledFeaturesInterface<?> styledFeatures,
			Style loadStyle, final MapLayer mapLayer,
			HashMap<String, Object> params, Boolean withDefaults) {
		this.styledFeatures = styledFeatures;
		this.rlf = new RuleListFactory(styledFeatures);

		// this.mapLegend = mapLegend;
		this.mapLayer = mapLayer;

		// If no params were passed, use an empty List, so we don't have to
		// check against null
		if (params == null)
			params = new HashMap<String, Object>();

		setFonts((List<Font>) params.get(PARAM_FONTS_LIST_FONT));

		// Calculating the averge distance to the next neightbou. this costs
		// time!// TODO in another thread
		// with GUI!// use attributeMetaDataMap to cache that?!
		// avgNN = FeatureUtil.calcAvgNN(styledFeatures);

		setTitle(styledFeatures.getTitle());
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

		/***********************************************************************
		 * Configuring the AtlasStyler translation settings.
		 * 
		 */
		AtlasStyler.languages = (List<String>) params
				.get(PARAM_LANGUAGES_LIST_STRING);

		if (languages == null || languages.size() == 0) {
			setLanguageMode(AtlasStyler.LANGUAGE_MODE.OGC_SINGLELANGUAGE);
		} else {
			setLanguageMode(AtlasStyler.LANGUAGE_MODE.ATLAS_MULTILANGUAGE);
			setLanguages(languages);
		}

	}

	/**
	 * Adds a {@link StyleChangeListener} to the {@link AtlasStyler} which gets
	 * called whenever the {@link Style} has changed.
	 */
	public void addListener(final StyleChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * Fires a {@link StyleChangedEvent} with the backup style to all listeners.
	 * Mainly used when cancelling any activity
	 */
	public void cancel() {
		styleCached = backupStyle;

		for (final StyleChangeListener l : listeners) {
			// LOGGER.debug("fires a StyleChangedEvent... ");
			l.changed(new StyleChangedEvent(backupStyle));
		}
	}

	/**
	 * Disposes the {@link AtlasStyler}. Tries to help the Java GC by removing
	 * dependencies.
	 */
	public void dispose() {
		reset();
		listeners.clear();
	}

	/**
	 * Informs all {@link StyleChangeListener}s about changed in the
	 * {@link Style}
	 * 
	 * Use this for {@link TextRuleList}
	 */
	public void fireStyleChangedEvents() {
		fireStyleChangedEvents(false);
	}

	/**
	 * Explicitly informs all {@link StyleChangeListener}s about changed in the
	 * {@link Style} due to the gicen parameter <code>ruleList</code>
	 * 
	 * Sets the parameter ruleList as the lastChangedRuleList
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	private void fireStyleChangedEvents(final AbstractRulesList ruleList) {
		if (!(ruleList instanceof TextRuleList)) {
			lastChangedRuleList = ruleList;
		}

		fireStyleChangedEvents();
	}

	/**
	 * Informs all {@link StyleChangeListener}s about changed in the
	 * {@link Style}
	 * 
	 * Use this for {@link TextRuleList}
	 * 
	 * @param forced
	 *            Can be used to override isAutomaticPreview()
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public void fireStyleChangedEvents(final boolean forced) {
		if (isQuite() && !forced) {
			// LOGGER.info("NOT FIREING EVENT because we are quite");
			return;
		}

		if ((!forced) && (!isAutomaticPreview())) {
			// LOGGER
			// .info("NOT FIREING EVENT because automaticPreview is deselected");
			return;
		}

		// LOGGER.info(" FIREING EVENT to " + listeners.size());

		styleCached = null;
		styleCached = getStyle();
		if (styleCached == null)
			return;

		for (final StyleChangeListener l : listeners) {
			// LOGGER.debug("fires a StyleChangedEvent... ");
			try {
				l.changed(new StyleChangedEvent(styleCached));
			} catch (Exception e) {
				LOGGER.error(e);
			}
		}
	}

	public AttributeMetadataMap<? extends AttributeMetadataInterface> getAttributeMetaDataMap() {
		return attributeMetaDataMap;
	}

	/**
	 * Returns a list of available fonts: A combination of the the
	 * AtlasStyler.getDefaultFontFamilies() and the fonts passed to
	 * {@link AtlasStyler} on construction.
	 */
	public List<Literal>[] getAvailableFonts() {
		List<Literal>[] fontFamilies = AtlasStyler.getDefaultFontFamilies();

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

	/**
	 * A list of fonts that will be available for styling in extension to the
	 * default font families. #getDefaultFontFamilies
	 */
	public List<Font> getFonts() {
		return fonts;
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
	void askToTransferTemplates(AbstractRulesList oldRl, FeatureRuleList newRl) {
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
	void askToTransferTemplates(AbstractRulesList oldRl, SingleRuleList newRl) {

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
			FeatureRuleList oldFeatureRl = (FeatureRuleList) lastChangedRuleList;
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

	/***************************************************************************
	 * @return The last {@link AbstractRulesList} where change has been observed
	 *         via the {@link RuleChangeListener}. Never return the labeling
	 *         TextRulesList. {@link #listenerFireStyleChange}
	 */
	public AbstractRulesList getLastChangedRuleList() {
		return lastChangedRuleList;
	}

	/**
	 * May return <code>null</code>, if no {@link MapLayer} is connected to the
	 * {@link AtlasStyler}.
	 */
	public MapLayer getMapLayer() {
		return mapLayer;
	}

	/**
	 * Because the rule title may not be empty, we check different sources here.
	 * the translated title of the styled layer would be first choice.
	 * 
	 * @return never <code>null</code> and never ""
	 */
	public static Translation getRuleTitleFor(StyledFeaturesInterface<?> sf) {

		if (!I8NUtil.isEmpty(sf.getTitle()))
			return sf.getTitle();

		// Fallback, use URL filename
		final Translation translation = new Translation(getLanguages(), sf
				.getSchema().getName().getLocalPart());

		return translation;
	}

	/***************************************************************************
	 * @return A full {@link Style} that represents the last RuleList that has
	 *         been changed.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public Style getStyle() {
		if (styleCached == null) {

			// Create an empty Style without any FeatureTypeStlyes
			styleCached = ASUtil.SB.createStyle();
			styleCached.setName("AtlasStyler "
					+ ReleaseUtil.getVersion(ASUtil.class));

			if (getRuleLists().size() == 0) {
				final String msfg = "AS:Returning empty style because atlasStyler has no rules!";
				LOGGER.error(msfg);
				styleCached.getDescription().setTitle(msfg);
				return styleCached;
			}

			// TODO Somewhere else!
			if (avgNN != null) {

				/**
				 * Applying automatic MaxScaleDenominators
				 */
				GeometryForm geom = FeatureUtil.getGeometryForm(styledFeatures
						.getSchema());

				lastChangedRuleList.setMaxScaleDenominator(StylingUtil
						.getMaxScaleDenominator(avgNN, geom));
				// if (lastChangedRuleList instanceof SingleRuleList) {
				// SingleRuleList srl = (SingleRuleList) lastChangedRuleList;
				// } else if (lastChangedRuleList instanceof
				// UniqueValuesRuleList) {
				// UniqueValuesRuleList srl = (UniqueValuesRuleList)
				// lastChangedRuleList;
				// srl.setMaxScaleDenominator(StylingUtil
				// .getMaxScaleDenominator(avgNN, geom));
				// } else if (lastChangedRuleList instanceof QuantitiesRuleList)
				// {
				// QuantitiesRuleList srl = (QuantitiesRuleList)
				// lastChangedRuleList;
				// }

			}

			// TODO handle textRuleLists special at the end?
			for (AbstractRulesList ruleList : getRuleLists()) {
				styleCached.featureTypeStyles().add(ruleList.getFTS());
			}

			// styleCached.featureTypeStyles().add(createTextRulesList().getFTS());

			// Just for debugging
			Level level = Logger.getRootLogger().getLevel();
			try {
				Logger.getRootLogger().setLevel(Level.OFF);
				StylingUtil.saveStyleToSld(styleCached, new File(
						"/home/stefan/Desktop/update.sld"));
			} catch (final Throwable e) {
			} finally {
				Logger.getRootLogger().setLevel(level);
			}

			styleCached = StylingUtil.correctPropertyNames(styleCached,
					getStyledFeatures().getSchema());

			// Just for debugging
			level = Logger.getRootLogger().getLevel();
			try {
				Logger.getRootLogger().setLevel(Level.OFF);
				StylingUtil.saveStyleToSld(styleCached, new File(
						"/home/stefan/Desktop/update_fixed.sld"));
			} catch (final Throwable e) {
			} finally {
				Logger.getRootLogger().setLevel(level);
			}

		}
		return styleCached;
		//
		// // Create an empty Style without any FeatureTypeStlyes
		// styleCached = ASUtil.SB.createStyle();
		//
		// styleCached.setName("AtlasStyler "
		// + ReleaseUtil.getVersionInfo(ASUtil.class));
		//
		// if (lastChangedRuleList == null) {
		//
		// LOGGER.warn("Returning empty style because no lastChangedRuleList==null");
		//
		// styleCached.getDescription()
		// .setTitle(
		// "AS:Returning empty style because no lastChangedRuleList==null");
		// return styleCached;
		// }
		//
		// if (avgNN != null) {
		//
		// /**
		// * Applying automatic MaxScaleDenominators
		// */
		// GeometryForm geom = FeatureUtil.getGeometryForm(styledFeatures
		// .getSchema());
		//
		// if (lastChangedRuleList instanceof SingleRuleList) {
		// SingleRuleList srl = (SingleRuleList) lastChangedRuleList;
		// srl.setMaxScaleDenominator(StylingUtil
		// .getMaxScaleDenominator(avgNN, geom));
		// } else if (lastChangedRuleList instanceof UniqueValuesRuleList) {
		// UniqueValuesRuleList srl = (UniqueValuesRuleList)
		// lastChangedRuleList;
		// srl.setMaxScaleDenominator(StylingUtil
		// .getMaxScaleDenominator(avgNN, geom));
		// } else if (lastChangedRuleList instanceof QuantitiesRuleList) {
		// QuantitiesRuleList srl = (QuantitiesRuleList) lastChangedRuleList;
		// }
		//
		// }
		//
		// styleCached.featureTypeStyles().add(getLastChangedRuleList().getFTS());
		//
		// styleCached.featureTypeStyles().add(createTextRulesList().getFTS());
		//
		// // Just for debugging
		// Level level = Logger.getRootLogger().getLevel();
		// try {
		// Logger.getRootLogger().setLevel(Level.OFF);
		// StylingUtil.saveStyleToSld(styleCached, new File(
		// "/home/stefan/Desktop/update.sld"));
		// } catch (final Throwable e) {
		// } finally {
		// Logger.getRootLogger().setLevel(level);
		// }
		// }
	}

	public StyledFeaturesInterface<?> getStyledFeatures() {
		return styledFeatures;
	}

	public Translation getTitle() {
		return title;
	}

	/**
	 * Tries to interpret a {@link Style} as {@link AbstractRulesList}s. Only
	 * {@link FeatureTypeStyle}s with parameter <code>name</code> starting with
	 * {@link RulesListType.SINGLE_SYMBOL_POINT} can be interpreted.
	 * 
	 * @param importStyle
	 *            {@link Style} to import.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public void importStyle(Style importStyle) {
		reset();

		// Backup
		if (backupStyle == null) {
			backupStyle = StylingUtil.clone(importStyle);
		}

		// Makes a copy of the style before importing it. otherwise we might get
		// the same object and not recognize changes later...
		importStyle = StylingUtil.clone(importStyle);

		// Forget all RuleLists we might have imported before

		// Just for debugging at steve's PC.
		Level level = Logger.getRootLogger().getLevel();
		try {
			Logger.getRootLogger().setLevel(Level.OFF);
			StylingUtil.saveStyleToSld(importStyle, new File(
					"/home/stefan/Desktop/goingToImport.sld"));
		} catch (final Throwable e) {
		} finally {
			Logger.getRootLogger().setLevel(level);
		}

		for (final FeatureTypeStyle fts : importStyle.featureTypeStyles()) {
			try {
				setQuite(true); // Quite the AtlasStyler!

				AbstractRulesList importedThisAbstractRuleList = rlf.importFts(
						fts, false);
				if (importedThisAbstractRuleList != null) {

					ruleLists.add(importedThisAbstractRuleList);

					importedThisAbstractRuleList
							.addListener(listenerFireStyleChange);
					fireStyleChangedEvents(importedThisAbstractRuleList);
				} else
					throw new AtlasParsingException("Importing fts " + fts
							+ " retuned null. No more information available.");

			} catch (final Exception importError) {
				LOGGER.warn(
						"Import error: " + importError.getLocalizedMessage(),
						importError);

				getImportErrorLog().add(importError);
			} finally {
				setQuite(false);
			}

		}
		final int ist = getRuleLists().size();
		final int soll = importStyle.featureTypeStyles().size();
		if (ist < soll) {
			LOGGER.debug("Only " + ist + " of all " + soll
					+ " RuleLists have been recognized fully...");
		}

		if (getRuleLists().size() > 0) {
			LOGGER.debug("Imported "
					+ getRuleLists().size()
					+ " valid FeatureTypeStyles, fireing StyleChangedEvents... ");
			fireStyleChangedEvents(getRuleLists().get(0));
		}

	}

	public boolean isAutomaticPreview() {
		return automaticPreview;
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

	/**
	 * @return <code>true</code> means, that {@link AtlasStyler} will fire
	 *         {@link StyleChangedEvent}s
	 */
	public boolean isQuite() {
		return quite;
	}

	/**
	 * Before loading a style we have to forget everything we might have
	 * imported before. Does not remove the listeners!
	 */
	public void reset() {

		getRuleLists().clear();

		styleCached = null;

		lastChangedRuleList = null;

	}

	public void setAttributeMetaDataMap(
			final AttributeMetadataMap<? extends AttributeMetadataInterface> attributeMetaDataMap) {
		this.attributeMetaDataMap = attributeMetaDataMap;
	}

	public void setAutomaticPreview(final boolean automaticPreview) {
		ASProps.set(ASProps.Keys.automaticPreview, automaticPreview ? 1 : 0);
		this.automaticPreview = automaticPreview;
	}

	/**
	 * A list of fonts that will be available for styling. If set to
	 * <code>null</code>, {@link #getFonts()} will return a new and empty
	 * ArayList<Font>
	 */
	public void setFonts(List<Font> fonts) {
		if (fonts == null)
			fonts = new ArrayList<Font>();
		else
			this.fonts = fonts;
	}

	/**
	 * The {@link AtlasStyler} can run in two {@link LANGUAGE_MODE}s.
	 */
	public void setLanguageMode(final LANGUAGE_MODE languageMode) {
		AtlasStyler.languageMode = languageMode;
	}

	public final void setLanguages(final List<String> languages) {
		AtlasStyler.languages = languages;
	}

	/**
	 * Reset the {@link List} of supported Languages to the passed
	 * {@link String}
	 * 
	 * @param langs
	 *            new {@link List} of lang codes
	 */
	public void setLanguages(final String... langs) {
		if (langs.length > 0)
			languages.clear();

		for (String code : langs) {
			code = code.trim();
			if (I8NUtil.isValidISOLangCode(code)) {
				languages.add(code);
			} else
				throw new IllegalArgumentException("The ISO Language code '"
						+ code + "' is not known/valid." + "\nIgnoring it.");
		}

	}

	public void setLastChangedRuleList(
			final AbstractRulesList lastChangedRuleList) {
		this.lastChangedRuleList = lastChangedRuleList;
		if (lastChangedRuleList != null) {
			LOGGER.info("Changing LCRL manually to "
					+ lastChangedRuleList.getClass().getSimpleName());
		} else {
			LOGGER.info("Changing LCRL to null");
		}
	}

	/**
	 * <code>true</code> means, that {@link AtlasStyler} will fire
	 * {@link StyleChangedEvent}s
	 */
	public void setQuite(final boolean quite) {
		this.quite = quite;
	}

	public void setTitle(final Translation title) {
		this.title = title;
	}

	/**
	 * If not <code>null</code>, swing dialogs might popup.
	 */
	public void setOwner(Component owner) {
		this.owner = owner;
	}

	public Component getOwner() {
		return owner;
	}

	/**
	 * This factory is used to create rule-lists. Returns a
	 * {@link RuleListFactory}.
	 */
	public RuleListFactory getRlf() {
		return rlf;
	}

	/**
	 * List of {@link AbstractRulesList}s that {@link AtlasStyler} is combining
	 * to one {@link Style}.
	 */
	public RulesListsList getRuleLists() {
		return ruleLists;
	}

	/**
	 * Adds an {@link AbstractRulesList} to the {@link #ruleLists} and adds a
	 * listener to it.
	 */
	public void addRulesList(AbstractRulesList rulelist) {
		getRuleLists().add(rulelist);

		rulelist.addListener(listenerFireStyleChange);
		fireStyleChangedEvents(rulelist);
	}

	/**
	 * A list of all exceptions/problems that occurred during import.
	 */
	public List<Exception> getImportErrorLog() {
		return importErrorLog;
	}

}
