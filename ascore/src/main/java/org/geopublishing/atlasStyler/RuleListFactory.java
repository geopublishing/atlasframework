package org.geopublishing.atlasStyler;

import java.awt.Color;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.AbstractRulesList.RulesListType;
import org.geotools.data.FeatureSource;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.styling.Description;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Symbolizer;
import org.opengis.util.InternationalString;

import schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import schmitzm.geotools.styling.StylingUtil;
import skrueger.geotools.StyledFeaturesInterface;
import skrueger.i8n.Translation;

/**
 * Creates instances of {@link AbstractRulesList} implementations.
 */
public class RuleListFactory {

	private static final Logger LOGGER = ASUtil.createLogger(AtlasStyler.class);

	private final StyledFeaturesInterface<?> styledFS;

	/**
	 * @param styledFeatures
	 *            Described the {@link FeatureSource} that the Rulelists will be
	 *            created for.
	 */
	public RuleListFactory(StyledFeaturesInterface<?> styledFeatures) {
		this.styledFS = styledFeatures;
	}

	/**
	 * Creates a RL according to the {@link GeometryForm} of the
	 * {@link StyledFeaturesInterface}. This may fail, since it could be of type
	 * ANY or NONE!
	 */
	public GraduatedColorRuleList createGraduatedColorRuleList(
			boolean withDefaults) {
		return createGraduatedColorRuleList(styledFS.getGeometryForm(),
				withDefaults);
	}

	public UniqueValuesRuleList createUniqueValuesRulesList(boolean withDefaults) {
		return createUniqueValuesRulesList(styledFS.getGeometryForm(),
				withDefaults);
	}

	private UniqueValuesRuleList createUniqueValuesRulesList(
			GeometryForm geometryForm, boolean withDefaults) {

		switch (geometryForm) {
		case LINE:
			return createUniqueValuesLineRulesList(withDefaults);
		case POINT:
			return createUniqueValuesPointRulesList(withDefaults);
		case ANY:
			LOGGER.warn("returns a GraduatedColorPolygonRuleList for GeometryForm ANY");
		case POLYGON:
			return createUniqueValuesPolygonRulesList(withDefaults);

		case NONE:
		default:
			throw new RuntimeException("Unrecognized GeometryForm or NONE");
		}
	}

	public GraduatedColorRuleList createGraduatedColorRuleList(
			GeometryForm geometryFrom, boolean withDefaults) {

		switch (geometryFrom) {
		case POINT:
			return createGraduatedColorPointRulesList(withDefaults);
		case LINE:
			return createGraduatedColorLineRulesList(withDefaults);
		case ANY:
			LOGGER.warn("returns a GraduatedColorPolygonRuleList for GeometryForm ANY");
		case POLYGON:
			return createGraduatedColorPolygonRulesList(withDefaults);

		case NONE:
		default:
			throw new RuntimeException("Unrecognized GeometryForm or NONE");
		}
	}

	public AbstractRulesList createRulesList(RulesListType rlType,
			boolean withDefaults) {

		switch (rlType) {
		case QUANTITIES_COLORIZED_LINE:
			return createGraduatedColorLineRulesList(withDefaults);
		case QUANTITIES_COLORIZED_POINT:
		case QUANTITIES_COLORIZED_POINT_FOR_POLYGON:
			return createGraduatedColorPointRulesList(withDefaults);
		case QUANTITIES_COLORIZED_POLYGON:
			return createGraduatedColorPolygonRulesList(withDefaults);

		case SINGLE_SYMBOL_LINE:
			return createSingleLineSymbolRulesList(withDefaults);
		case SINGLE_SYMBOL_POINT:
		case SINGLE_SYMBOL_POINT_FOR_POLYGON:
			return createSinglePointSymbolRulesList(withDefaults);
		case SINGLE_SYMBOL_POLYGON:
			return createSinglePolygonSymbolRulesList(withDefaults);

		case UNIQUE_VALUE_LINE:
			return createUniqueValuesLineRulesList(withDefaults);
		case UNIQUE_VALUE_POINT:
		case UNIQUE_VALUE_POINT_FOR_POLYGON:
			return createUniqueValuesPointRulesList(withDefaults);
		case UNIQUE_VALUE_POLYGON:
			return createUniqueValuesPolygonRulesList(withDefaults);

		case TEXT_LABEL:
			return createTextRulesList(withDefaults);

		default:
			throw new IllegalArgumentException("RulesListType not recognized");
		}

	}

	public SingleRuleList createSingleRulesList(
			final GeometryForm geometryForm, boolean withDefaults) {
		switch (geometryForm) {
		case LINE:
			return createSingleLineSymbolRulesList(withDefaults);
		case POINT:
			return createSinglePointSymbolRulesList(withDefaults);
		case POLYGON:
			return createSinglePolygonSymbolRulesList(withDefaults);
		default:
			throw new IllegalArgumentException("Can create for type "
					+ geometryForm);
		}
	}

	public SingleLineSymbolRuleList createSingleLineSymbolRulesList(
			boolean withDefaults) {
		Translation title = AtlasStyler.getRuleTileFor(styledFS);
		SingleLineSymbolRuleList singleLineSymbolRuleList = new SingleLineSymbolRuleList(
				title);

		if (withDefaults) {
			singleLineSymbolRuleList.addNewDefaultLayer();
		}

		// singleLineSymbolRuleList.addListener(listenerFireStyleChange);

		// askToTransferTemplates(lastChangedRuleList,
		// singleLineSymbolRuleList);
		// fireStyleChangedEvents(singleLineSymbolRuleList);
		// } else
		// askToTransferTemplates(lastChangedRuleList,
		// singleLineSymbolRuleList);
		return singleLineSymbolRuleList;
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

	public SinglePointSymbolRuleList createSinglePointSymbolRulesList(
			boolean withDefaults) {
		Translation title2 = AtlasStyler.getRuleTileFor(styledFS);

		SinglePointSymbolRuleList singlePointSymbolRuleList = new SinglePointSymbolRuleList(
				title2);

		if (withDefaults) {
			singlePointSymbolRuleList.addNewDefaultLayer();
		}

		// singlePointSymbolRuleList.addListener(listenerFireStyleChange);
		//
		// askToTransferTemplates(lastChangedRuleList,
		// singlePointSymbolRuleList);
		//
		// fireStyleChangedEvents(singlePointSymbolRuleList);
		// } else
		// askToTransferTemplates(lastChangedRuleList,
		// singlePointSymbolRuleList);

		return singlePointSymbolRuleList;
	}

	public SinglePolygonSymbolRuleList createSinglePolygonSymbolRulesList(
			boolean withDefaults) {
		Translation title = AtlasStyler.getRuleTileFor(styledFS);
		SinglePolygonSymbolRuleList singlePolygonSymbolRuleList = new SinglePolygonSymbolRuleList(
				title);

		if (withDefaults) {
			// We have already imported a Style and we fill this RuleList
			// with a default layer.
			singlePolygonSymbolRuleList.addNewDefaultLayer();
		}

		// singlePolygonSymbolRuleList.addListener(listenerFireStyleChange);
		//
		// askToTransferTemplates(lastChangedRuleList,
		// singlePolygonSymbolRuleList);
		// fireStyleChangedEvents(singlePolygonSymbolRuleList);
		// } else
		// askToTransferTemplates(lastChangedRuleList,
		// singlePolygonSymbolRuleList);
		return singlePolygonSymbolRuleList;
	}

	public UniqueValuesLineRuleList createUniqueValuesLineRulesList(
			boolean withDefaults) {
		UniqueValuesLineRuleList uniqueValuesLineRuleList = new UniqueValuesLineRuleList(
				styledFS);
		// uniqueValuesLineRuleList.addListener(listenerFireStyleChange);
		//
		// askToTransferTemplates(lastChangedRuleList,
		// uniqueValuesLineRuleList);
		// fireStyleChangedEvents(uniqueValuesLineRuleList);
		// } else
		//
		// askToTransferTemplates(lastChangedRuleList,
		// uniqueValuesLineRuleList);
		return uniqueValuesLineRuleList;
	}

	public UniqueValuesPointRuleList createUniqueValuesPointRulesList(
			boolean withDefaults) {
		// if (uniqueValuesPointRuleList == null) {
		UniqueValuesPointRuleList uniqueValuesPointRuleList = new UniqueValuesPointRuleList(
				styledFS);
		// uniqueValuesPointRuleList.addListener(listenerFireStyleChange);
		//
		// askToTransferTemplates(lastChangedRuleList,
		// uniqueValuesPointRuleList);
		// fireStyleChangedEvents(uniqueValuesPointRuleList);
		// } else
		// askToTransferTemplates(lastChangedRuleList,
		// uniqueValuesPointRuleList);
		return uniqueValuesPointRuleList;
	}

	public UniqueValuesPolygonRuleList createUniqueValuesPolygonRulesList(
			boolean withDefaults) {
		UniqueValuesPolygonRuleList uniqueValuesPolygonRuleList = new UniqueValuesPolygonRuleList(
				styledFS);
		// uniqueValuesPolygonRuleList.addListener(listenerFireStyleChange);
		//
		// askToTransferTemplates(lastChangedRuleList,
		// uniqueValuesPolygonRuleList);
		// fireStyleChangedEvents(uniqueValuesPolygonRuleList);
		// } else
		// askToTransferTemplates(lastChangedRuleList,
		// uniqueValuesPolygonRuleList);
		return uniqueValuesPolygonRuleList;
	}

	public AbstractRulesList importFts(FeatureTypeStyle fts,
			boolean withDefaults) throws AtlasParsingException {

		// final int anzRules = fts.rules().size();
		// LOGGER.info("Importing: '" + metaInfoString
		// + "', has #Rules = " + anzRules);

		final String metaInfoString = fts.getName();
		if ((metaInfoString == null)) {
			throw new AtlasParsingException(
					"'featureStyleName' is empty! This FeatureTypeStyle can't be imported. It has not been created with AtlasStyler.");
		}

		// Singles
		AbstractRulesList importedThisAbstractRuleList = importSingleRuleList(fts);
		if (importedThisAbstractRuleList != null)
			return importedThisAbstractRuleList;

		// Uniques
		importedThisAbstractRuleList = importUniqueValuesRulesList(fts);
		if (importedThisAbstractRuleList != null)
			return importedThisAbstractRuleList;

		// Quantities
		importedThisAbstractRuleList = importGraduatedColorRulesList(fts);
		if (importedThisAbstractRuleList != null)
			return importedThisAbstractRuleList;

		// Labeling
		importedThisAbstractRuleList = importTextRulesList(fts);
		if (importedThisAbstractRuleList != null)
			return importedThisAbstractRuleList;

		throw new AtlasParsingException(
				"This FeatureTypeStyle could not be imported. metaInfoString = "
						+ metaInfoString);

		//
		// else {
		// LOGGER.info("Importing a FTS failed because the name field (our metadata) could not be interpreted: '"
		// + metaInfoString);
		//
		// /**
		// * Adding default layers to all SingleRules
		// */
		// switch (FeatureUtil.getGeometryForm(styledFS.getSchema())) {
		// case LINE:
		// getSingleLineSymbolRulesList().addNewDefaultLayer();
		// break;
		// case POINT:
		// getSinglePointSymbolRulesList().addNewDefaultLayer();
		// break;
		// case POLYGON:
		// getSinglePolygonSymbolRulesList().addNewDefaultLayer();
		// break;
		//
		// // TODO NONE AND ANY!
		//
		// }
		//
		// continue;
		// }
		// countImportedFeatureTypeStyles++;

	}

	/***************************************************************
	 * Importing everything that starts with TEXT, most likely a
	 * RulesListType.TEXT_LABEL
	 */
	private AbstractRulesList importTextRulesList(FeatureTypeStyle fts) {
		if (!fts.getName().startsWith(RulesListType.TEXT_LABEL.toString()))
			return null;

		final TextRuleList textRulesList = createTextRulesList(false);

		textRulesList.importRules(fts.rules());

		if (!textRulesList.hasDefault()) {
			LOGGER.info("Imported a TextRuleList without a DEFAULT class.");

			int addDefaultClass = textRulesList.addDefaultClass();
			if (textRulesList.isEnabled()) {
				LOGGER.info("Added the default CLASS as disabled.");
				textRulesList.setClassEnabled(addDefaultClass, false);
			} else {
				LOGGER.info("Added the default CLASS as enabled.");
			}
		}
		return textRulesList;

	}

	/**
	 * @return the RulesList that describes the labeling.
	 */
	public TextRuleList createTextRulesList(boolean withDefaults) {
		TextRuleList textRulesList = new TextRuleList(styledFS, withDefaults);
		return textRulesList;
	}

	public GraduatedColorRuleList createGraduatedColorPolygonRulesList(
			boolean withDefaults) {
		GraduatedColorPolygonRuleList graduatedColorPolygonRuleList = new GraduatedColorPolygonRuleList(
				styledFS);
		return graduatedColorPolygonRuleList;
	}

	public GraduatedColorRuleList createGraduatedColorLineRulesList(
			boolean withDefaults) {
		GraduatedColorLineRuleList graduatedColorLineRuleList = new GraduatedColorLineRuleList(
				styledFS);
		return graduatedColorLineRuleList;
	}

	public GraduatedColorRuleList createGraduatedColorPointRulesList(
			boolean withDefaults) {
		GraduatedColorPointRuleList graduatedColorPointRuleList = new GraduatedColorPointRuleList(
				styledFS);
		return graduatedColorPointRuleList;
	}

	private AbstractRulesList importGraduatedColorRulesList(FeatureTypeStyle fts) {
		String metaInfoString = fts.getName();

		/***************************************************************
		 * Importing everything that starts with QUANTITIES
		 */
		if (!metaInfoString.startsWith("QUANTITIES"))
			return null;

		QuantitiesRuleList<Double> quantitiesRuleList = null;

		/***********************************************************
		 * Importing a QUANTITIES_COLORIZED_POINT RuleList
		 */
		if (metaInfoString.startsWith(RulesListType.QUANTITIES_COLORIZED_POINT
				.toString())) {

			quantitiesRuleList = createGraduatedColorPointRulesList(false);

		}

		/***********************************************************
		 * Importing a QUANTITIES_COLORIZED_LINE RuleList
		 */
		else if (metaInfoString
				.startsWith(RulesListType.QUANTITIES_COLORIZED_LINE.toString())) {

			quantitiesRuleList = createGraduatedColorLineRulesList(false);
		}

		/***********************************************************
		 * Importing a QUANTITIES_COLORIZED_POLYGON RuleList
		 */
		else if (metaInfoString
				.startsWith(RulesListType.QUANTITIES_COLORIZED_POLYGON
						.toString())) {

			quantitiesRuleList = createGraduatedColorPolygonRulesList(false);
		}

		else {
			throw new RuntimeException("metaInfoString = " + metaInfoString
					+ ", but is not recognized!");
		}

		quantitiesRuleList.pushQuite();
		try {

			// This also imports the template from the first rule.
			quantitiesRuleList.parseMetaInfoString(metaInfoString, fts);

			/***********************************************************
			 * Parsing information in the RULEs
			 * 
			 * title, class limits
			 */
			int countRules = 0;
			final TreeSet<Double> classLimits = new TreeSet<Double>();
			double[] ds = null;
			for (final Rule r : fts.rules()) {

				if (r.getName().toString()
						.startsWith(FeatureRuleList.NODATA_RULE_NAME)) {
					// This rule defines the NoDataSymbol
					quantitiesRuleList.importNoDataRule(r);
					continue;
				}

				// set Title
				quantitiesRuleList.getRuleTitles().put(countRules,
						r.getDescription().getTitle().toString());

				// Class Limits
				ds = QuantitiesRuleList.interpretBetweenFilter(r.getFilter());
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
			 * Now determine the colors stored inside the symbolizers.
			 */
			for (int ri = 0; ri < countRules; ri++) {
				// Import the dominant color from the symbolizers
				// (they can differ from the palette colors, because
				// they might have been changed manually.
				for (final Symbolizer s : fts.rules().get(ri).getSymbolizers()) {

					final Color c = StylingUtil.getSymbolizerColor(s);

					if (c != null) {
						// System.out.println("Rule " + ri
						// + " has color " + c);
						quantitiesRuleList.getColors()[ri] = c;
						break;
					}
				}

			}

			return quantitiesRuleList;

		} finally {
			quantitiesRuleList.popQuite();
		}

	}

	private AbstractRulesList importUniqueValuesRulesList(FeatureTypeStyle fts) {
		final String metaInfoString = fts.getName();
		/***************************************************************
		 * Importing everything that starts with QUANTITIES
		 */
		if (metaInfoString.startsWith("UNIQUE")) {

			UniqueValuesRuleList uniqueRuleList = null;

			/***********************************************************
			 * Importing a UNIQUE_VALUE_POINT RuleList
			 */
			if (metaInfoString.startsWith(RulesListType.UNIQUE_VALUE_POINT
					.toString())) {
				uniqueRuleList = createUniqueValuesPointRulesList(false);
			} else if (metaInfoString
					.startsWith(RulesListType.UNIQUE_VALUE_LINE.toString())) {
				uniqueRuleList = createUniqueValuesLineRulesList(false);
			} else if (metaInfoString
					.startsWith(RulesListType.UNIQUE_VALUE_POLYGON.toString())) {
				uniqueRuleList = createUniqueValuesPolygonRulesList(false);
			} else {
				throw new RuntimeException("metaInfoString = " + metaInfoString
						+ ", but is not recognized!");
			}

			uniqueRuleList.pushQuite();
			try {

				uniqueRuleList.parseMetaInfoString(metaInfoString, fts);

				/***********************************************************
				 * Parsing information in the RULEs
				 * 
				 * title, unique values, symbols=>singleRuleLists, template?
				 */
				int countRules = 0;
				uniqueRuleList.setWithDefaultSymbol(false);
				for (final Rule r : fts.rules()) {

					if (r.getName() != null
							&& r.getName()
									.toString()
									.startsWith(
											FeatureRuleList.NODATA_RULE_NAME)) {
						// This rule defines the NoDataSymbol
						uniqueRuleList.importNoDataRule(r);
						continue;
					}

					uniqueRuleList.test();

					// Interpret Filter!
					final String[] strings = UniqueValuesRuleList
							.interpretFilter(r.getFilter());

					uniqueRuleList.setPropertyFieldName(strings[0], false);

					final Symbolizer[] symbolizers = r.getSymbolizers();

					final SingleRuleList<? extends Symbolizer> singleRLprototype = uniqueRuleList
							.getDefaultTemplate();

					// Forget bout generics here!!!
					final SingleRuleList<?> symbolRL = singleRLprototype.copy();

					symbolRL.getSymbolizers().clear();
					for (final Symbolizer symb : symbolizers) {
						final Vector symbolizers2 = symbolRL.getSymbolizers();
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

				LOGGER.debug("Imported " + countRules + " UNIQUE rules ");
			} finally {
				uniqueRuleList.popQuite();
			}

			return uniqueRuleList;
		}

		return null;
	}

	private SingleRuleList<? extends Symbolizer> importSingleRuleList(
			FeatureTypeStyle fts) throws AtlasParsingException {

		final String metaInfoString = fts.getName();

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
			if (metaInfoString.startsWith(RulesListType.SINGLE_SYMBOL_POINT
					.toString())) {
				singleRuleList = createSinglePointSymbolRulesList(false);
			} else
			/***********************************************************
			 * Importing a SINGLE_SYMBOL_LINE RuleList
			 */
			if (metaInfoString.startsWith(RulesListType.SINGLE_SYMBOL_LINE
					.toString())) {
				singleRuleList = createSingleLineSymbolRulesList(false);
			}
			/***********************************************************
			 * Importing a SINGLE_SYMBOL_POLYGON RuleList
			 */
			else if (metaInfoString
					.startsWith(RulesListType.SINGLE_SYMBOL_POLYGON.toString())) {
				singleRuleList = createSinglePolygonSymbolRulesList(false);
			} else {
				throw new AtlasParsingException("metaInfoString = "
						+ metaInfoString + ", but can not be recognized!");
			}

			singleRuleList
					.setMaxScaleDenominator(rule.getMaxScaleDenominator());
			singleRuleList
					.setMinScaleDenominator(rule.getMinScaleDenominator());

			singleRuleList.pushQuite();
			try {

				// singleRuleList.setStyleTitle(importStyle.getTitle());
				// singleRuleList.setStyleAbstract(importStyle.getAbstract());

				// not needed: singleRuleList.getSymbolizers().clear();

				/**
				 * This stuff is the same for all three SINGLE_RULES types
				 */
				singleRuleList.addSymbolizers(symbs);
				singleRuleList.reverseSymbolizers();

				// We had some stupid AbstractMethodException here...
				try {
					final Description description = rule.getDescription();
					final InternationalString title2 = description.getTitle();
					singleRuleList.setTitle(title2.toString());
				} catch (final NullPointerException e) {
					LOGGER.warn("The title style to import has been null!");
					singleRuleList.setTitle("");
				} catch (final Exception e) {
					LOGGER.error(
							"The title style to import could not been set!", e);
					singleRuleList.setTitle("");
				}

				return singleRuleList;

			} finally {
				singleRuleList.popQuite();
			}
		}

		return null;

	}

	//
	// try {
	// setQuite(true); // Quite the AtlasStyler!
	//
	// final String metaInfoString = fts.getName();
	//
	// final int anzRules = fts.rules().size();
	// // LOGGER.info("Importing: '" + metaInfoString
	// // + "', has #Rules = " + anzRules);
	//
	// if ((metaInfoString == null)) {
	// LOGGER.warn("This FeatureTypeStyle can't be proppery imported! It has not been created with AtlasStyler");
	// continue;
	// }
	//
	// /***************************************************************
	// * Importing everything that starts with SINGLE
	// */
	// if (metaInfoString.startsWith("SINGLE")) {
	// final Rule rule = fts.rules().get(0);
	//
	// final List<? extends Symbolizer> symbs = rule.symbolizers();
	//
	// SingleRuleList<? extends Symbolizer> singleRuleList = null;
	//
	// /***********************************************************
	// * Importing a SINGLE_SYMBOL_POINT RuleList
	// */
	// if (metaInfoString
	// .startsWith(RulesListType.SINGLE_SYMBOL_POINT
	// .toString())) {
	// singleRuleList = getSinglePointSymbolRulesList();
	// } else
	// /***********************************************************
	// * Importing a SINGLE_SYMBOL_LINE RuleList
	// */
	// if (metaInfoString
	// .startsWith(RulesListType.SINGLE_SYMBOL_LINE
	// .toString())) {
	// singleRuleList = getSingleLineSymbolRulesList();
	// }
	// /***********************************************************
	// * Importing a SINGLE_SYMBOL_POLYGON RuleList
	// */
	// else if (metaInfoString
	// .startsWith(RulesListType.SINGLE_SYMBOL_POLYGON
	// .toString())) {
	// singleRuleList = getSinglePolygonSymbolRulesList();
	// } else {
	// throw new RuntimeException("metaInfoString = "
	// + metaInfoString + ", but is not recognized!");
	// }
	//
	// singleRuleList.setMaxScaleDenominator(rule
	// .getMaxScaleDenominator());
	// singleRuleList.setMinScaleDenominator(rule
	// .getMinScaleDenominator());
	//
	// singleRuleList.pushQuite();
	// try {
	//
	// // singleRuleList.setStyleTitle(importStyle.getTitle());
	// // singleRuleList.setStyleAbstract(importStyle.getAbstract());
	//
	// singleRuleList.getSymbolizers().clear();
	//
	// /**
	// * This stuff is the same for all three SINGLE_RULES
	// * types
	// */
	// singleRuleList.addSymbolizers(symbs);
	//
	// singleRuleList.reverseSymbolizers();
	//
	// // We had some stupid AbstractMethodException here...
	// try {
	// final Description description = rule
	// .getDescription();
	// final InternationalString title2 = description
	// .getTitle();
	// singleRuleList.setTitle(title2.toString());
	// } catch (final NullPointerException e) {
	// LOGGER.warn("The title style to import has been null!");
	// singleRuleList.setTitle("");
	// } catch (final Exception e) {
	// LOGGER.error(
	// "The title style to import could not been set!",
	// e);
	// singleRuleList.setTitle("");
	// }
	//
	// importedThisAbstractRuleList = singleRuleList;
	//
	// } finally {
	// singleRuleList.popQuite();
	// }
	// }
	//
	// /***************************************************************
	// * Importing everything that starts with QUANTITIES
	// */
	// else if (metaInfoString.startsWith("UNIQUE")) {
	//
	// UniqueValuesRuleList uniqueRuleList = null;
	//
	// /***********************************************************
	// * Importing a UNIQUE_VALUE_POINT RuleList
	// */
	// if (metaInfoString
	// .startsWith(RulesListType.UNIQUE_VALUE_POINT
	// .toString())) {
	//
	// uniqueRuleList = createUniqueValuesPointRulesList();
	//
	// } else if (metaInfoString
	// .startsWith(RulesListType.UNIQUE_VALUE_LINE
	// .toString())) {
	//
	// uniqueRuleList = createUniqueValuesLineRulesList();
	// } else if (metaInfoString
	// .startsWith(RulesListType.UNIQUE_VALUE_POLYGON
	// .toString())) {
	//
	// uniqueRuleList = createUniqueValuesPolygonRulesList();
	// } else {
	// throw new RuntimeException("metaInfoString = "
	// + metaInfoString + ", but is not recognized!");
	// }
	//
	// uniqueRuleList.pushQuite();
	// try {
	//
	// uniqueRuleList.parseMetaInfoString(metaInfoString, fts);
	//
	// /***********************************************************
	// * Parsing information in the RULEs
	// *
	// * title, unique values, symbols=>singleRuleLists,
	// * template?
	// */
	// int countRules = 0;
	// uniqueRuleList.setWithDefaultSymbol(false);
	// for (final Rule r : fts.rules()) {
	//
	// if (r.getName() != null
	// && r.getName()
	// .toString()
	// .startsWith(
	// FeatureRuleList.NODATA_RULE_NAME)) {
	// // This rule defines the NoDataSymbol
	// uniqueRuleList.importNoDataRule(r);
	// continue;
	// }
	//
	// uniqueRuleList.test();
	//
	// // Interpret Filter!
	// final String[] strings = UniqueValuesRuleList
	// .interpretFilter(r.getFilter());
	//
	// uniqueRuleList.setPropertyFieldName(strings[0],
	// false);
	//
	// final Symbolizer[] symbolizers = r.getSymbolizers();
	//
	// final SingleRuleList<? extends Symbolizer> singleRLprototype =
	// uniqueRuleList
	// .getDefaultTemplate();
	//
	// // Forget bout generics here!!!
	// final SingleRuleList<?> symbolRL = singleRLprototype
	// .copy();
	//
	// symbolRL.getSymbolizers().clear();
	// for (final Symbolizer symb : symbolizers) {
	// final Vector symbolizers2 = symbolRL
	// .getSymbolizers();
	// symbolizers2.add(symb);
	// }
	// symbolRL.reverseSymbolizers();
	//
	// // Finally set all three values into the RL
	// uniqueRuleList.getLabels().add(
	// r.getDescription().getTitle().toString());
	// uniqueRuleList.getSymbols().add(symbolRL);
	// uniqueRuleList.getValues().add(strings[1]);
	//
	// uniqueRuleList.test();
	//
	// countRules++;
	// }
	//
	// LOGGER.debug("Imported " + countRules
	// + " UNIQUE rules ");
	// } finally {
	// uniqueRuleList.popQuite();
	// }
	//
	// importedThisAbstractRuleList = uniqueRuleList;
	// }
	//
	// /***************************************************************
	// * Importing everything that starts with QUANTITIES
	// */
	// else if (metaInfoString.startsWith("QUANTITIES")) {
	//
	// QuantitiesRuleList<Double> quantitiesRuleList = null;
	//
	// /***********************************************************
	// * Importing a QUANTITIES_COLORIZED_POINT RuleList
	// */
	// if (metaInfoString
	// .startsWith(RulesListType.QUANTITIES_COLORIZED_POINT
	// .toString())) {
	//
	// quantitiesRuleList = createGraduatedColorPointRulesList(boolean
	// withDefaults);
	//
	// }
	//
	// /***********************************************************
	// * Importing a QUANTITIES_COLORIZED_LINE RuleList
	// */
	// else if (metaInfoString
	// .startsWith(RulesListType.QUANTITIES_COLORIZED_LINE
	// .toString())) {
	//
	// quantitiesRuleList = createGraduatedColorLineRulesList(boolean
	// withDefaults);
	// }
	//
	// /***********************************************************
	// * Importing a QUANTITIES_COLORIZED_POLYGON RuleList
	// */
	// else if (metaInfoString
	// .startsWith(RulesListType.QUANTITIES_COLORIZED_POLYGON
	// .toString())) {
	//
	// quantitiesRuleList = createGraduatedColorPolygonRuleList(boolean
	// withDefaults);
	// }
	//
	// else {
	// throw new RuntimeException("metaInfoString = "
	// + metaInfoString + ", but is not recognized!");
	// }
	//
	// quantitiesRuleList.pushQuite();
	// try {
	//
	// // This also imports the template from the first rule.
	// quantitiesRuleList.parseMetaInfoString(metaInfoString,
	// fts);
	//
	// /***********************************************************
	// * Parsing information in the RULEs
	// *
	// * title, class limits
	// */
	// int countRules = 0;
	// final TreeSet<Double> classLimits = new TreeSet<Double>();
	// double[] ds = null;
	// for (final Rule r : fts.rules()) {
	//
	// if (r.getName()
	// .toString()
	// .startsWith(
	// FeatureRuleList.NODATA_RULE_NAME)) {
	// // This rule defines the NoDataSymbol
	// quantitiesRuleList.importNoDataRule(r);
	// continue;
	// }
	//
	// // set Title
	// quantitiesRuleList.getRuleTitles().put(countRules,
	// r.getDescription().getTitle().toString());
	//
	// // Class Limits
	// ds = QuantitiesRuleList.interpretBetweenFilter(r
	// .getFilter());
	// classLimits.add(ds[0]);
	//
	// countRules++;
	// }
	// if (ds != null) {
	// // The last limit is only added if there have been
	// // any
	// // rules
	// classLimits.add(ds[1]);
	// }
	// quantitiesRuleList.setClassLimits(classLimits, false);
	//
	// /**
	// * Now determine the colors stored inside the
	// * symbolizers.
	// */
	// for (int ri = 0; ri < countRules; ri++) {
	// // Import the dominant color from the symbolizers
	// // (they can differ from the palette colors, because
	// // they might have been changed manually.
	// for (final Symbolizer s : fts.rules().get(ri)
	// .getSymbolizers()) {
	//
	// final Color c = StylingUtil
	// .getSymbolizerColor(s);
	//
	// if (c != null) {
	// // System.out.println("Rule " + ri
	// // + " has color " + c);
	// quantitiesRuleList.getColors()[ri] = c;
	// break;
	// }
	// }
	//
	// }
	//
	// importedThisAbstractRuleList = quantitiesRuleList;
	//
	// } finally {
	// quantitiesRuleList.popQuite();
	// }
	// }
	//
	// /***************************************************************
	// * Importing everything that starts with TEXT, most likely a
	// * RulesListType.TEXT_LABEL
	// */
	// else if (metaInfoString.startsWith(RulesListType.TEXT_LABEL
	// .toString())) {
	// final TextRuleList textRulesList = getTextRulesList();
	// textRulesList.importRules(fts.rules());
	// }
	//
	// else {
	// LOGGER.info("Importing a FTS failed because the name field (our metadata) could not be interpreted: '"
	// + metaInfoString);
	//
	// /**
	// * Adding default layers to all SingleRules
	// */
	// switch (FeatureUtil.getGeometryForm(styledFeatures
	// .getSchema())) {
	// case LINE:
	// getSingleLineSymbolRulesList().addNewDefaultLayer();
	// break;
	// case POINT:
	// getSinglePointSymbolRulesList().addNewDefaultLayer();
	// break;
	// case POLYGON:
	// getSinglePolygonSymbolRulesList().addNewDefaultLayer();
	// break;
	//
	// // TODO NONE AND ANY!
	//
	// }
	//
	// continue;
	// }
	// // countImportedFeatureTypeStyles++;
	// } catch (final Exception importError) {
	// LOGGER.warn(
	// "Import error: " + importError.getLocalizedMessage(),
	// importError);
	// // TODO Inform about import failure
	// } finally {
	// setQuite(false);
	// }

}
