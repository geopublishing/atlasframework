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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.AbstractRuleList.RulesListType;
import org.geotools.data.FeatureSource;
import org.geotools.map.MapLayer;
import org.geotools.styling.Description;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.visitor.DuplicatingStyleVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.expression.Literal;
import org.opengis.util.InternationalString;

import schmitzm.geotools.FilterUtil;
import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import schmitzm.geotools.styling.StylingUtil;
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
	protected static Logger LOGGER = ASUtil.createLogger(AtlasStyler.class);

	/**
	 * Default size of a symbol in any AtlasStyler previews
	 */
	public static final Dimension DEFAULT_SYMBOL_PREVIEW_SIZE = new Dimension(
			30, 30);

	/**
	 * If false, the {@link AtlasStyler} only fires {@link StyleChangedEvent}s
	 * when the dialog is closed.
	 */
	boolean automaticPreview = ASProps.getInt(ASProps.Keys.automaticPreview, 1) == 1;

	/***************************************************************************
	 * Listeners to style changes
	 */
	Set<StyleChangeListener> listeners = new HashSet<StyleChangeListener>();

	/**
	 * This listener is attached to all rule lists and propagates any events as
	 * {@link StyleChangedEvent}s to the listeners of the {@link AtlasStyler} *
	 */
	private final RuleChangeListener listenerFireStyleChange = new RuleChangeListener() {

		public void changed(final RuleChangedEvent e) {
			xxxstyle = null;

			// Only the lastChangedRule will be used to create the Style
			final AbstractRuleList someRuleList = e.getSourceRL();

			if (!(someRuleList instanceof TextRuleList)) {
				lastChangedRuleList = someRuleList;
			}

			fireStyleChangedEvents();
		}
	};

	/***************************************************************************
	 * Paths
	 */

	/** All {@link AtlasStyler} related files will be saved blow this path */
	private static File applicationPreferencesDir;

	/** These DIRNAMEs describe paths to application files on the local machines */
	final static String DIRNAME_TEMPLATES = "templates";

	/** These DIRNAMEs describe paths to application files on the local machines */
	final static String DIRNAME_POINT = "point";

	/** These DIRNAMEs describe paths to application files on the local machines */
	final static String DIRNAME_LINE = "line";

	/** These DIRNAMEs describe paths to application files on the local machines */
	final static String DIRNAME_POLYGON = "polygon";

	/**
	 * The cache for the {@link Style} that is generated when
	 * {@link #getStyle()} is called.
	 */
	protected Style xxxstyle = null;

	/***************************************************************************
	 * {@link AbstractRuleList}s aka RuleLists that this {@link AtlasStyler}
	 * keeps track of
	 */
	private GraduatedColorPointRuleList graduatedColorPointRuleList;

	private GraduatedColorLineRuleList graduatedColorLineRuleList;

	private GraduatedColorPolygonRuleList graduatedColorPolygonRuleList;

	private SinglePointSymbolRuleList singlePointSymbolRuleList;

	private SingleLineSymbolRuleList singleLineSymbolRuleList;

	private SinglePolygonSymbolRuleList singlePolygonSymbolRuleList;

	private UniqueValuesPointRuleList uniqueValuesPointRuleList;

	private UniqueValuesLineRuleList uniqueValuesLineRuleList;

	private UniqueValuesPolygonRuleList uniqueValuesPolygonRuleList;

	private TextRuleList textRulesList;

	/**
	 * The {@link AtlasStyler} can run in two {@link LANGUAGE_MODE}s.
	 */
	public enum LANGUAGE_MODE {

		/**
		 * Follow OGC standard and put simple Strings into the title field of
		 * rules.
		 */
		OGC_SINGLELANGUAGE,

		/**
		 * Use atlas extension which codes many translations into the title
		 * field of rules.
		 */
		ATLAS_MULTILANGUAGE
	}

	/**
	 * Default languageMode is LANGUAGE_MODE.OGC_SINGLELANGUAGE
	 */
	static LANGUAGE_MODE languageMode = LANGUAGE_MODE.OGC_SINGLELANGUAGE;

	/**
	 * The {@link AtlasStyler} can run in two {@link LANGUAGE_MODE}s.
	 */
	public void setLanguageMode(final LANGUAGE_MODE languageMode) {
		AtlasStyler.languageMode = languageMode;
	}

	/**
	 * The {@link AtlasStyler} can run in two {@link LANGUAGE_MODE}s.
	 */
	public static LANGUAGE_MODE getLanguageMode() {
		return languageMode;
	}

	/**
	 * List of LanguageCodes that the targeted Atlas is supposed to "speak".
	 */
	private static List<String> languages = new LinkedList<String>();

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

	/**
	 * If we are running in {@link LANGUAGE_MODE} ATLAS_MULTILANGUAGE, these are
	 * the supported languages.
	 */
	public final static List<String> getLanguages() {
		return languages;
	}

	public final void setLanguages(final List<String> languages) {
		AtlasStyler.languages = languages;
	}

	/***************************************************************************
	 * @return The last {@link AbstractRuleList} where change has been observed
	 *         via the {@link RuleChangeListener}. Never return the labeling
	 *         TextRulesList. {@link #listenerFireStyleChange}
	 */
	public AbstractRuleList getLastChangedRuleList() {
		return lastChangedRuleList;
	}

	private AbstractRuleList lastChangedRuleList;

	/** If true, no Events will be fired to listeners */
	private boolean quite = true;

	/**
	 * Holds optional meta-information about the datasource. If not set, this
	 * empty Map is used. Otherwise meta like column name description will be
	 * looked up here.
	 **/
	private AttributeMetadataMap attributeMetaDataMap = new AttributeMetadataImplMap();

	private Translation title;

	private final StyledFeaturesInterface<?> styledFeatures;

	// private final MapLegend mapLegend;
	private final MapLayer mapLayer;

	private Double avgNN = null;

	/**
	 * *Backup of the {@link Style} as it was before the AtlasStyle touched it.
	 * Used when {@link #cancel()} is called.
	 */
	private Style backupStyle;

	/**
	 * A list of fonts that will be available for styling.
	 */
	private List<Font> fonts = new ArrayList<Font>();

	/**
	 * Key for a parameter of type List<String> that contains the languages the
	 * SLD is created for (not SLD standard, only used if AS is run within GP)
	 */
	public final static String PARAM_LANGUAGES_LIST_STRING = "PARAM_LANGUAGES_LIST_STRING";

	/**
	 * Key for a parameter of type List<Font> of additional Fonts that are
	 * available
	 */
	public final static String PARAM_FONTS_LIST_FONT = "PARAM_FONTS_LIST_FONT";

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
	 */
	public AtlasStyler(final StyledFeaturesInterface<?> styledFeatures,
			Style loadStyle, final MapLayer mapLayer,
			HashMap<String, Object> params) {
		this.styledFeatures = styledFeatures;
		// this.mapLegend = mapLegend;
		this.mapLayer = mapLayer;

		// If no params were passed, use an empty List, so we don't have to
		// check against null
		if (params == null)
			params = new HashMap<String, Object>();

		setFonts((List<Font>) params.get(PARAM_FONTS_LIST_FONT));

		// Calculating the averge distance to the next neightbou. this costs
		// time!// TODO in another thread
		// with GUI!// TODO in another thread
		// with GUI! O check attributeMetaDataMap for that?!
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
				importStyle(ASUtil.createDefaultStyle(styledFeatures));
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
	 * Create an {@link AtlasStyler} object for any
	 * {@link StyledFeaturesInterface}
	 */
	public AtlasStyler(StyledFeaturesInterface<?> styledFeatures) {
		this(styledFeatures, null, null, null);
	}

	/**
	 * Create an AtlasStyler object for any {@link FeatureSource}
	 */
	public AtlasStyler(
			FeatureSource<SimpleFeatureType, SimpleFeature> featureSource) {
		this(new StyledFS(featureSource));
	}
	
	/**
	 * Create an AtlasStyler object for any {@link FeatureSource} and import the given {@link Style}.
	 */
	public AtlasStyler(
			FeatureSource<SimpleFeatureType, SimpleFeature> featureSource, Style style) {
		this(new StyledFS(featureSource), style, null, null);
	}


	/**
	 * Before loading a style we have to forget everything we might have
	 * imported before.have
	 */
	public void reset() {

		xxxstyle = null;

		lastChangedRuleList = null;

		graduatedColorPointRuleList = null;

		graduatedColorLineRuleList = null;

		graduatedColorPolygonRuleList = null;

		singlePointSymbolRuleList = null;

		singleLineSymbolRuleList = null;

		singlePolygonSymbolRuleList = null;

		uniqueValuesPointRuleList = null;

		uniqueValuesLineRuleList = null;

		uniqueValuesPolygonRuleList = null;

		textRulesList = null;
	}

	/**
	 * Tries to interpret a {@link Style} as {@link AbstractRuleList}s. Only
	 * {@link FeatureTypeStyle}s with parameter <code>name</code> starting with
	 * {@link RulesListType.SINGLE_SYMBOL_POINT} can be interpreted.
	 * 
	 * @param importStyle
	 *            {@link Style} to import.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public void importStyle(Style importStyle) {

		final DuplicatingStyleVisitor dupl = new DuplicatingStyleVisitor();

		/**
		 * Backup the existing Style
		 */
		if (backupStyle == null) {
			dupl.visit(importStyle);
			backupStyle = (Style) dupl.getCopy();
		}

		// Makes a copy of the style before importing it. otherwise we might get
		// the same obejct and not recognize changes later...
		dupl.visit(importStyle);
		importStyle = (Style) dupl.getCopy();

		// Forget anything we might have imported before
		reset();

		AbstractRuleList importedThisAbstractRuleList = null;

		int countImportedFeatureTypeStyles = 0;
		try {
			setQuite(true); // Quite the AtlasStyler!
			// TODO Just for debugging at steve's PC. May be removed anytime.
			try {
				StylingUtil.saveStyleToSLD(importStyle, new File(
						"/home/stefan/Desktop/goingToImport.sld"));
			} catch (final TransformerException e) {
				LOGGER.warn("Transforming to XML failed!", e);
			} catch (final Throwable e) {
			}

			for (final FeatureTypeStyle fts : importStyle.featureTypeStyles()) {

				final String metaInfoString = fts.getName();

				final int anzRules = fts.rules().size();
//				LOGGER.info("Importing: '" + metaInfoString
//						+ "', has #Rules = " + anzRules);

				if ((metaInfoString == null)) {
					LOGGER
							.warn("This FeatureTypeStyle can't be proppery imported! It has not been created with AtlasStyler");
					continue;
				}

				/***************************************************************
				 * Importing everything that starts with SINGLE
				 */
				if (metaInfoString.startsWith("SINGLE")) {
					final Rule rule = fts.rules().get(0);

					final List<? extends Symbolizer> symbs = rule.symbolizers();

					SingleRuleList<? extends Symbolizer> singleRuleList = null;

					/***********************************************************
					 * Importing a SINGLE_SYMBOL_POINT RuleList
					 */
					if (metaInfoString
							.startsWith(RulesListType.SINGLE_SYMBOL_POINT
									.toString())) {
						singleRuleList = getSinglePointSymbolRulesList();

					} else
					/***********************************************************
					 * Importing a SINGLE_SYMBOL_LINE RuleList
					 */
					if (metaInfoString
							.startsWith(RulesListType.SINGLE_SYMBOL_LINE
									.toString())) {
						singleRuleList = getSingleLineSymbolRulesList();
					}
					/***********************************************************
					 * Importing a SINGLE_SYMBOL_POLYGON RuleList
					 */
					else if (metaInfoString
							.startsWith(RulesListType.SINGLE_SYMBOL_POLYGON
									.toString())) {
						singleRuleList = getSinglePolygonSymbolRulesList();
					} else {
						throw new RuntimeException("metaInfoString = "
								+ metaInfoString + ", but is not recognized!");
					}

					singleRuleList.setMaxScaleDenominator(rule
							.getMaxScaleDenominator());
					singleRuleList.setMinScaleDenominator(rule
							.getMinScaleDenominator());

					singleRuleList.pushQuite();
					try {

						// singleRuleList.setStyleTitle(importStyle.getTitle());
						// singleRuleList.setStyleAbstract(importStyle.getAbstract());

						singleRuleList.getSymbolizers().clear();

						/**
						 * This stuff is the same for all three SINGLE_RULES
						 * types
						 */
						singleRuleList.addSymbolizers(symbs);

						singleRuleList.reverseSymbolizers();

						// We had some stupid AbstractMethodException here...
						try {
							final Description description = rule
									.getDescription();
							final InternationalString title2 = description
									.getTitle();
							singleRuleList.setTitle(title2.toString());
						} catch (final NullPointerException e) {
							LOGGER
									.warn("The title style to import has been null!");
							singleRuleList.setTitle("");
						} catch (final Exception e) {
							LOGGER
									.error(
											"The title style to import could not been set!",
											e);
							singleRuleList.setTitle("");
						}

						importedThisAbstractRuleList = singleRuleList;

					} finally {
						singleRuleList.popQuite();
					}
				}

				/***************************************************************
				 * Importing everything that starts with QUANTITIES
				 */
				else if (metaInfoString.startsWith("UNIQUE")) {

					UniqueValuesRuleList uniqueRuleList = null;

					/***********************************************************
					 * Importing a UNIQUE_VALUE_POINT RuleList
					 */
					if (metaInfoString
							.startsWith(RulesListType.UNIQUE_VALUE_POINT
									.toString())) {

						uniqueRuleList = getUniqueValuesPointRulesList();

					} else if (metaInfoString
							.startsWith(RulesListType.UNIQUE_VALUE_LINE
									.toString())) {

						uniqueRuleList = getUniqueValuesLineRulesList();
					} else if (metaInfoString
							.startsWith(RulesListType.UNIQUE_VALUE_POLYGON
									.toString())) {

						uniqueRuleList = getUniqueValuesPolygonRuleList();
					} else {
						throw new RuntimeException("metaInfoString = "
								+ metaInfoString + ", but is not recognized!");
					}

					uniqueRuleList.pushQuite();
					try {

						uniqueRuleList.parseMetaInfoString(metaInfoString, fts);

						/***********************************************************
						 * Parsing information in the RULEs
						 * 
						 * title, unique values, symbols=>singleRuleLists,
						 * template?
						 */
						int countRules = 0;
						uniqueRuleList.setWithDefaultSymbol(false);
						for (final Rule r : fts.rules()) {

							if (r.getName() != null
									&& r.getName().toString().startsWith(
											FeatureRuleList.NODATA_RULE_NAME)) {
								// This rule defines the NoDataSymbol
								uniqueRuleList.importNoDataRule(r);
								continue;
							}

							uniqueRuleList.test();

							// Interpret Filter!
							final String[] strings = UniqueValuesRuleList
									.interpretFilter(r.getFilter());

							uniqueRuleList.setPropertyFieldName(strings[0],
									false);

							final Symbolizer[] symbolizers = r.getSymbolizers();

							final SingleRuleList<? extends Symbolizer> singleRLprototype = uniqueRuleList
									.getDefaultTemplate();

							// Forget bout generics here!!!
							final SingleRuleList symbolRL = singleRLprototype
									.copy();

							symbolRL.getSymbolizers().clear();
							for (final Symbolizer symb : symbolizers) {
								final Vector symbolizers2 = symbolRL
										.getSymbolizers();
								symbolizers2.add(symb);
							}
							symbolRL.reverseSymbolizers();
							
							

							// Finally set all three values into the RL
							uniqueRuleList.getLabels().add(
									r.getDescription().getTitle().toString());
							uniqueRuleList.getSymbols().add(symbolRL);
							uniqueRuleList.getValues().add(strings[1]);

							uniqueRuleList.test();
							
							countRules++;
						}

						LOGGER.debug("Imported " + countRules
								+ " UNIQUE rules ");
					} finally {
						uniqueRuleList.popQuite();
					}

					importedThisAbstractRuleList = uniqueRuleList;
				}

				/***************************************************************
				 * Importing everything that starts with QUANTITIES
				 */
				else if (metaInfoString.startsWith("QUANTITIES")) {

					QuantitiesRuleList<Double> quantitiesRuleList = null;

					/***********************************************************
					 * Importing a QUANTITIES_COLORIZED_POINT RuleList
					 */
					if (metaInfoString
							.startsWith(RulesListType.QUANTITIES_COLORIZED_POINT
									.toString())) {

						quantitiesRuleList = getGraduatedColorPointRulesList();

					}

					/***********************************************************
					 * Importing a QUANTITIES_COLORIZED_LINE RuleList
					 */
					else if (metaInfoString
							.startsWith(RulesListType.QUANTITIES_COLORIZED_LINE
									.toString())) {

						quantitiesRuleList = getGraduatedColorLineRulesList();
					}

					/***********************************************************
					 * Importing a QUANTITIES_COLORIZED_POLYGON RuleList
					 */
					else if (metaInfoString
							.startsWith(RulesListType.QUANTITIES_COLORIZED_POLYGON
									.toString())) {

						quantitiesRuleList = getGraduatedColorPolygonRuleList();
					}

					else {
						throw new RuntimeException("metaInfoString = "
								+ metaInfoString + ", but is not recognized!");
					}

					quantitiesRuleList.pushQuite();
					try { // popQuite

						// This also imports the template from the first rule.
						quantitiesRuleList.parseMetaInfoString(metaInfoString,
								fts);

						/***********************************************************
						 * Parsing information in the RULEs
						 * 
						 * title, class limits
						 */
						int countRules = 0;
						final TreeSet<Double> classLimits = new TreeSet<Double>();
						double[] ds = null;
						for (final Rule r : fts.rules()) {

							if (r.getName().toString().startsWith(
									FeatureRuleList.NODATA_RULE_NAME)) {
								// This rule defines the NoDataSymbol
								quantitiesRuleList.importNoDataRule(r);
								continue;
							}

							// set Title
							quantitiesRuleList.getRuleTitles().put(countRules,
									r.getDescription().getTitle().toString());

							// Class Limits
							ds = QuantitiesRuleList.interpretBetweenFilter(r
									.getFilter());
							classLimits.add(ds[0]);

							countRules++;
						}
						if (ds != null) {
							// The last limit is only added if there have been
							// any
							// rules
							classLimits.add(ds[1]);
						}
						quantitiesRuleList.setClassLimits(classLimits, false);

						/**
						 * Now determine the colors stored inside the
						 * symbolizers.
						 */
						for (int ri = 0; ri < countRules; ri++) {
							// Import the dominant color from the symbolizers
							// (they can differ from the palette colors, because
							// they might have been changed manually.
							for (final Symbolizer s : fts.rules().get(ri)
									.getSymbolizers()) {

								final Color c = StylingUtil
										.getSymbolizerColor(s);

								if (c != null) {
									System.out.println("Rule "+ri+" has color "+c);
									quantitiesRuleList.getColors()[ri] = c;
									break;
								}
							}

						}

						importedThisAbstractRuleList = quantitiesRuleList;

					} finally {
						quantitiesRuleList.popQuite();
					}
				}

				/***************************************************************
				 * Importing everything that starts with TEXT, most likely a
				 * RulesListType.TEXT_LABEL
				 */
				else if (metaInfoString.startsWith(RulesListType.TEXT_LABEL
						.toString())) {
					final TextRuleList textRulesList = getTextRulesList();
					textRulesList.importRules(fts.rules());
				}

				else {
//					LOGGER
//							.info("Importing a FTS failed because the Name field was not recognized. Name='"
//									+ metaInfoString
//									+ "'. An empty AtlasStyler will start if no other FTS are defined.");

					/**
					 * Adding default layers to all SingleRules
					 */
					switch (FeatureUtil.getGeometryForm(styledFeatures
							.getSchema())) {
					case LINE:
						getSingleLineSymbolRulesList().addNewDefaultLayer();
						break;
					case POINT:
						getSinglePointSymbolRulesList().addNewDefaultLayer();
						break;
					case POLYGON:
						getSinglePolygonSymbolRulesList().addNewDefaultLayer();
						break;
					}

					continue;
					// throw new RuntimeException("Not yet implemented");
				}
				countImportedFeatureTypeStyles++;
			}

		} catch (final Exception importError) {
			LOGGER.warn("Import error: " + importError.getLocalizedMessage(),
					importError);
			// TODO Inform about import failure
		}

		final int ist = countImportedFeatureTypeStyles;
		final int soll = importStyle.featureTypeStyles().size();
//		if (ist < soll) {
//			LOGGER.debug("Only " + ist + " of all " + soll
//					+ " Rulelists have been recognized fully...");
//		}

		setQuite(false);
		if (importedThisAbstractRuleList != null) {
			LOGGER
					.debug("Imported a valid FeatureTypeStyle for Symbolization, fireing StyleChangedEvents... ");
			fireStyleChangedEvents(importedThisAbstractRuleList);
		}

	}

	/**
	 * Adds a {@link StyleChangeListener} to the {@link AtlasStyler} which gets
	 * called whenever the {@link Style} has changed.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public void addListener(final StyleChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * Informs all {@link StyleChangeListener}s about changed in the
	 * {@link Style}
	 * 
	 * Use this for {@link TextRuleList}
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public void fireStyleChangedEvents() {
		fireStyleChangedEvents(false);
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

		xxxstyle = null;
		xxxstyle = getStyle();
		if (xxxstyle == null)
			return;

		for (final StyleChangeListener l : listeners) {
			// LOGGER.debug("fires a StyleChangedEvent... ");
			try {
				l.changed(new StyleChangedEvent(xxxstyle));
			} catch (Exception e) {
				LOGGER.error(e);
			}
		}
	}

	/**
	 * Explicitly informs all {@link StyleChangeListener}s about changed in the
	 * {@link Style} due to the gicen parameter <code>ruleList</code>
	 * 
	 * Sets the parameter ruleList as the lastChangedSymbolizerRuleList
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	private void fireStyleChangedEvents(final AbstractRuleList ruleList) {
		if (!(ruleList instanceof TextRuleList)) {
			lastChangedRuleList = ruleList;
		}

		fireStyleChangedEvents();
	}

	/**
	 * @return <code>true</code> means, that {@link AtlasStyler} will fire
	 *         {@link StyleChangedEvent}s
	 */
	public boolean isQuite() {
		return quite;
	}

	/**
	 * <code>true</code> means, that {@link AtlasStyler} will fire
	 * {@link StyleChangedEvent}s
	 */
	public void setQuite(final boolean quite) {
		this.quite = quite;
	}

	/***************************************************************************
	 * @return A full {@link Style} that represents the last RuleList that has
	 *         been changed.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public Style getStyle() {
		if (xxxstyle == null) {

			// Create an empty Style without any FeatureTypeStlyes
			xxxstyle = ASUtil.SB.createStyle();

			xxxstyle.setName("AtlasStyler "
					+ ReleaseUtil.getVersionInfo(ASUtil.class));

			if (lastChangedRuleList == null) {

				LOGGER
						.warn("Returning empty style because no lastChangedRuleList==null");

				xxxstyle
						.getDescription()
						.setTitle(
								"AS:Returning empty style because no lastChangedRuleList==null");
				return xxxstyle;
			}

			// LOGGER.info("*** The Style is generated from "
			// + lastChangedRuleList.getClass().getSimpleName());

			if (avgNN != null) {

				/**
				 * Applying automatic MaxScaleDenominators
				 */
				GeometryForm geom = FeatureUtil.getGeometryForm(styledFeatures
						.getSchema());

				if (lastChangedRuleList instanceof SingleRuleList) {
					SingleRuleList srl = (SingleRuleList) lastChangedRuleList;
					srl.setMaxScaleDenominator(StylingUtil
							.getMaxScaleDenominator(avgNN, geom));
				} else if (lastChangedRuleList instanceof UniqueValuesRuleList) {
					UniqueValuesRuleList srl = (UniqueValuesRuleList) lastChangedRuleList;
					srl.setMaxScaleDenominator(StylingUtil
							.getMaxScaleDenominator(avgNN, geom));
				} else if (lastChangedRuleList instanceof QuantitiesRuleList) {
					QuantitiesRuleList srl = (QuantitiesRuleList) lastChangedRuleList;
				}

			}

			xxxstyle.featureTypeStyles().add(getLastChangedRuleList().getFTS());

			xxxstyle.featureTypeStyles().add(getTextRulesList().getFTS());
			//
			// // TODO Remove
			try {
				StylingUtil.saveStyleToSLD(xxxstyle, new File(
						"/home/stefan/Desktop/update.sld"));
			} catch (final TransformerException e) {
				LOGGER.error("Transforming to XML failed!", e);
			} catch (final Throwable e) {
			}

		}
		return xxxstyle;
	}

	public GraduatedColorRuleList getGraduatedColorRuleList(
			final GeometryDescriptor geometryAttributeType) {

		if (FeatureUtil.getGeometryForm(geometryAttributeType) == GeometryForm.POINT) {
			return getGraduatedColorPointRulesList();
		} else if (FeatureUtil.getGeometryForm(geometryAttributeType) == GeometryForm.LINE) {
			return getGraduatedColorLineRulesList();
		} else if (FeatureUtil.getGeometryForm(geometryAttributeType) == GeometryForm.POLYGON) {
			return getGraduatedColorPolygonRuleList();
		}

		throw new RuntimeException("Unrecognized geometryAttributeType");
	}

	public GraduatedColorRuleList getGraduatedColorLineRulesList() {
		if (graduatedColorLineRuleList == null) {
			graduatedColorLineRuleList = new GraduatedColorLineRuleList(
					getStyledFeatures());

			graduatedColorLineRuleList.addListener(listenerFireStyleChange);
			fireStyleChangedEvents(graduatedColorLineRuleList);
		}
		return graduatedColorLineRuleList;
	}

	public GraduatedColorRuleList getGraduatedColorPolygonRuleList() {
		if (graduatedColorPolygonRuleList == null) {
			graduatedColorPolygonRuleList = new GraduatedColorPolygonRuleList(
					getStyledFeatures());

			graduatedColorPolygonRuleList.addListener(listenerFireStyleChange);
			fireStyleChangedEvents(graduatedColorPolygonRuleList);
		}
		return graduatedColorPolygonRuleList;
	}

	public GraduatedColorRuleList getGraduatedColorPointRulesList() {
		if (graduatedColorPointRuleList == null) {
			graduatedColorPointRuleList = new GraduatedColorPointRuleList(
					getStyledFeatures());

			graduatedColorPointRuleList.addListener(listenerFireStyleChange);
			fireStyleChangedEvents(graduatedColorPointRuleList);
		}

		/**
		 * Trying it this way: If we switched from a SinglePointSymbolRuleList,
		 * let it be this GraduatedPointColorRuleList's template.
		 */
		if (lastChangedRuleList instanceof SinglePointSymbolRuleList) {
			final int res = JOptionPane
					.showConfirmDialog(
							null,
							R("AtlasStyler.SwitchRuleListType.CopySingleSymbolAsTemplate"));
			if (res == JOptionPane.YES_OPTION) {
				final SinglePointSymbolRuleList singleRL = (SinglePointSymbolRuleList) lastChangedRuleList;
				graduatedColorPointRuleList.setTemplate(singleRL.copy());
			}
		}

		return graduatedColorPointRuleList;
	}

	public SinglePointSymbolRuleList getSinglePointSymbolRulesList() {
		if (singlePointSymbolRuleList == null) {
			Translation title2 = getRuleTileFor(styledFeatures);

			singlePointSymbolRuleList = new SinglePointSymbolRuleList(title2);

			if (lastChangedRuleList != null) {
				// We have already imported a Style and we fill this RuleList
				// with a default layer.
				singlePointSymbolRuleList.addNewDefaultLayer();
			}

			singlePointSymbolRuleList.addListener(listenerFireStyleChange);
			fireStyleChangedEvents(singlePointSymbolRuleList);
		}

		/**
		 * Trying it this way: If we switched from a
		 * GraduatedColorPointRuleList, let this SinglePointSymbolRuleList be
		 * the GraduatedColorPointRuleList's template.
		 */
		if (lastChangedRuleList instanceof GraduatedColorPointRuleList) {
			final int res = JOptionPane
					.showConfirmDialog(
							null,
							"Do you want to use the GraduatedColorPointRuleList template as SinglePointSymbol?"); // i8n
			if (res == JOptionPane.YES_OPTION) {
				final GraduatedColorPointRuleList gradColorRL = (GraduatedColorPointRuleList) lastChangedRuleList;
				final SingleRuleList<?> template = gradColorRL.getTemplate();
				singlePointSymbolRuleList
						.setSymbolizers(((SinglePointSymbolRuleList) template)
								.getSymbolizers());
			}
		}

		return singlePointSymbolRuleList;
	}

	public SingleLineSymbolRuleList getSingleLineSymbolRulesList() {
		if (singleLineSymbolRuleList == null) {
			Translation title2 = getRuleTileFor(styledFeatures);
			singleLineSymbolRuleList = new SingleLineSymbolRuleList(title2);

			if (lastChangedRuleList != null) {
				// We have already imported a Style and we fill this RuleList
				// with a default layer.
				singleLineSymbolRuleList.addNewDefaultLayer();
			}

			singleLineSymbolRuleList.addListener(listenerFireStyleChange);
			fireStyleChangedEvents(singleLineSymbolRuleList);
		}
		return singleLineSymbolRuleList;
	}

	public SinglePolygonSymbolRuleList getSinglePolygonSymbolRulesList() {
		if (singlePolygonSymbolRuleList == null) {
			Translation title2 = getRuleTileFor(styledFeatures);
			singlePolygonSymbolRuleList = new SinglePolygonSymbolRuleList(
					title2);

			if (lastChangedRuleList != null) {
				// We have already imported a Style and we fill this RuleList
				// with a default layer.
				singlePolygonSymbolRuleList.addNewDefaultLayer();
			}

			singlePolygonSymbolRuleList.addListener(listenerFireStyleChange);
			fireStyleChangedEvents(singlePolygonSymbolRuleList);
		}
		return singlePolygonSymbolRuleList;
	}

	/**
	 * Because the rule title may not be empty, we check different sources here.
	 * the translated title of the styled layer would be first choice.
	 * 
	 * @return never <code>null</code> and never ""
	 */
	private Translation getRuleTileFor(StyledFeaturesInterface<?> sf) {

		if (!I8NUtil.isEmpty(sf.getTitle()))
			return sf.getTitle();

		// Fallback, use URL filename
		final Translation translation = new Translation(getLanguages(), sf
				.getSchema().getName().getLocalPart());

		return translation;
	}

	public UniqueValuesPointRuleList getUniqueValuesPointRulesList() {
		if (uniqueValuesPointRuleList == null) {
			uniqueValuesPointRuleList = new UniqueValuesPointRuleList(
					getStyledFeatures());
			uniqueValuesPointRuleList.addListener(listenerFireStyleChange);
			fireStyleChangedEvents(uniqueValuesPointRuleList);
		}
		return uniqueValuesPointRuleList;
	}

	public UniqueValuesLineRuleList getUniqueValuesLineRulesList() {
		if (uniqueValuesLineRuleList == null) {
			uniqueValuesLineRuleList = new UniqueValuesLineRuleList(
					getStyledFeatures());
			uniqueValuesLineRuleList.addListener(listenerFireStyleChange);
			fireStyleChangedEvents(uniqueValuesLineRuleList);
		}
		return uniqueValuesLineRuleList;
	}

	public UniqueValuesPolygonRuleList getUniqueValuesPolygonRuleList() {
		if (uniqueValuesPolygonRuleList == null) {
			uniqueValuesPolygonRuleList = new UniqueValuesPolygonRuleList(
					getStyledFeatures());
			uniqueValuesPolygonRuleList.addListener(listenerFireStyleChange);
			fireStyleChangedEvents(uniqueValuesPolygonRuleList);
		}
		return uniqueValuesPolygonRuleList;
	}

	/**
	 * @return the RulesList that describes the labelling.
	 */
	public TextRuleList getTextRulesList() {
		if (textRulesList == null) {
			textRulesList = new TextRuleList(getStyledFeatures());
			textRulesList.addListener(listenerFireStyleChange);
			fireStyleChangedEvents();
		}
		return textRulesList;
	}

	/**
	 * @return an {@link GraduatedColorRuleList} for the given
	 *         {@link GeometryAttributeType}
	 * 
	 * @param geometryAttributeType
	 *            {@link GeometryAttributeType} that defines
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */

	/**
	 * @return A {@link File} pointing to USER_HOME_DIR/.AtlasSLDEditor
	 */
	public static File getApplicationPreferencesDir() {
		if (applicationPreferencesDir == null) {
			applicationPreferencesDir = new File(new File(System
					.getProperty("user.home")), ".AtlasStyler");
			applicationPreferencesDir.mkdirs();
		}
		return applicationPreferencesDir;
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
	 * @return a {@link File} that points to the base folder for AtlasStyler
	 *         templates
	 */
	private static File getBaseSymbolsDir() {
		return new File(AtlasStyler.getApplicationPreferencesDir(),
				DIRNAME_TEMPLATES);
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

	/**
	 * Disposes the {@link AtlasStyler}. Tries to help the Java GC by removing
	 * dependencies.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public void dispose() {
		xxxstyle = null;
		listeners.clear();
	}

	/**
	 * Convenience method to indicate if the {@link FeatureSource} is of type
	 * Polygon.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public boolean isPolygon() {
		return FeatureUtil.getGeometryForm(getStyledFeatures()
				.getFeatureSource()) == GeometryForm.POLYGON;
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
	 * {@link LineString}.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public boolean isLineString() {
		return FeatureUtil.getGeometryForm(getStyledFeatures()
				.getFeatureSource()) == GeometryForm.LINE;
	}

	public void setLastChangedRuleList(
			final AbstractRuleList lastChangedRuleList) {
		this.lastChangedRuleList = lastChangedRuleList;
		if (lastChangedRuleList != null) {
			LOGGER.info("Changing LCRL manually to "
					+ lastChangedRuleList.getClass().getSimpleName());
		} else {
			LOGGER.info("Changing LCRL to null");
		}
	}

	public static File getSymbolsDir(final GeometryDescriptor defaultGeometry) {

		switch (FeatureUtil.getGeometryForm(defaultGeometry)) {
		case LINE:
			return getLineSymbolsDir();
		case POINT:
			return getPointSymbolsDir();
		case POLYGON:
			return getPolygonSymbolsDir();
		}

		final String msg = "GeometryAttributeType not recognized = "
				+ defaultGeometry;
		LOGGER.error(msg);
		throw new IllegalArgumentException(msg);
	}

	public boolean isAutomaticPreview() {
		return automaticPreview;
	}

	public void setAutomaticPreview(final boolean automaticPreview) {
		ASProps.set(ASProps.Keys.automaticPreview, automaticPreview ? 1 : 0);
		this.automaticPreview = automaticPreview;
	}

	public void setAttributeMetaDataMap(
			final AttributeMetadataMap attributeMetaDataMap) {
		this.attributeMetaDataMap = attributeMetaDataMap;
	}

	public AttributeMetadataMap getAttributeMetaDataMap() {
		return attributeMetaDataMap;
	}

	public void setTitle(final Translation title) {
		this.title = title;
	}

	public Translation getTitle() {
		return title;
	}

	/**
	 * May return <code>null</code>, if no {@link MapLayer} is connected to the
	 * {@link AtlasStyler}.
	 */
	public MapLayer getMapLayer() {
		return mapLayer;
	}

	public StyledFeaturesInterface<?> getStyledFeatures() {
		return styledFeatures;
	}

	// /**
	// * May return <code>null</code>, if no {@link MapLegend} is connected to
	// the
	// * {@link AtlasStyler}.
	// */
	// public MapLegend getMapLegend() {
	// return mapLegend;
	// }

	/**
	 * Fires a {@link StyleChangedEvent} with the backup style to all listeners.
	 * Mainly used when cancelling any activity
	 */
	public void cancel() {
		xxxstyle = backupStyle;

		for (final StyleChangeListener l : listeners) {
			// LOGGER.debug("fires a StyleChangedEvent... ");
			l.changed(new StyleChangedEvent(backupStyle));
		}
	}

	/**
	 * @Deprecated use AsUtil.R
	 */
	public static String R(String key, final Object... values) {
		return ASUtil.R(key, values);
	}

	/**
	 * A list of fonts that will be available for styling.
	 */
	public List<Font> getFonts() {
		return fonts;
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

}
