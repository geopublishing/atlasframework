package org.geopublishing.atlasStyler;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.AbstractRulesList.RulesListType;
import org.geotools.data.FeatureSource;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Symbolizer;

import schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import schmitzm.lang.LangUtil;
import skrueger.geotools.StyledFeaturesInterface;
import skrueger.i8n.Translation;

/**
 * Creates instances of {@link AbstractRulesList} implementations. <br/>
 * TODO Make title a parameter in more methods and make them static (see
 * SingleSymbol)
 */
public class RuleListFactory {

	private static final Logger LOGGER = LangUtil
			.createLogger(AtlasStyler.class);

	private final StyledFeaturesInterface<?> styledFS;

	/**
	 * @param styledFeatures
	 *            Described the {@link FeatureSource} that the Rulelists will be
	 *            created for.
	 */
	public RuleListFactory(StyledFeaturesInterface<?> styledFeatures) {
		this.styledFS = styledFeatures;
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

	public GraduatedColorRuleList createGraduatedColorPolygonRulesList(
			boolean withDefaults) {
		GraduatedColorPolygonRuleList graduatedColorPolygonRuleList = new GraduatedColorPolygonRuleList(
				styledFS);
		return graduatedColorPolygonRuleList;
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

		Translation title = AtlasStyler.getRuleTitleFor(styledFS);

		switch (rlType) {
		case QUANTITIES_COLORIZED_LINE:
			return createGraduatedColorLineRulesList(withDefaults);
		case QUANTITIES_COLORIZED_POINT:
		case QUANTITIES_COLORIZED_POINT_FOR_POLYGON:
			return createGraduatedColorPointRulesList(withDefaults);
		case QUANTITIES_COLORIZED_POLYGON:
			return createGraduatedColorPolygonRulesList(withDefaults);

		case SINGLE_SYMBOL_LINE:
			return createSingleLineSymbolRulesList(title, withDefaults);
		case SINGLE_SYMBOL_POINT:
		case SINGLE_SYMBOL_POINT_FOR_POLYGON:
			return createSinglePointSymbolRulesList(title, withDefaults);
		case SINGLE_SYMBOL_POLYGON:
			return createSinglePolygonSymbolRulesList(title, withDefaults);

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

	static public SingleLineSymbolRuleList createSingleLineSymbolRulesList(
			Translation title, boolean withDefaults) {
		SingleLineSymbolRuleList singleLineSymbolRuleList = new SingleLineSymbolRuleList(
				title);

		if (withDefaults) {
			singleLineSymbolRuleList.addNewDefaultLayer();
		}
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
	 * @param title
	 */

	static public SinglePointSymbolRuleList createSinglePointSymbolRulesList(
			Translation title, boolean withDefaults) {

		SinglePointSymbolRuleList singlePointSymbolRuleList = new SinglePointSymbolRuleList(
				title);

		if (withDefaults) {
			singlePointSymbolRuleList.addNewDefaultLayer();
		}
		return singlePointSymbolRuleList;
	}

	static public SinglePolygonSymbolRuleList createSinglePolygonSymbolRulesList(
			Translation title, boolean withDefaults) {
		SinglePolygonSymbolRuleList singlePolygonSymbolRuleList = new SinglePolygonSymbolRuleList(
				title);

		if (withDefaults) {
			// We have already imported a Style and we fill this RuleList
			// with a default layer.
			singlePolygonSymbolRuleList.addNewDefaultLayer();
		}
		return singlePolygonSymbolRuleList;
	}

	/**
	 * Uses the styledFeature's {@link GeometryForm}
	 */
	public SingleRuleList<? extends Symbolizer> createSingleRulesList(
			Translation title, boolean withDefaults) {
		return createSingleRulesList(title, styledFS.getGeometryForm(),
				withDefaults);
	}

	static public SingleRuleList<? extends Symbolizer> createSingleRulesList(
			Translation title, final GeometryForm geometryForm,
			boolean withDefaults) {
		switch (geometryForm) {
		case LINE:
			return createSingleLineSymbolRulesList(title, withDefaults);
		case POINT:
			return createSinglePointSymbolRulesList(title, withDefaults);
		case POLYGON:
			return createSinglePolygonSymbolRulesList(title, withDefaults);
		case ANY:
			LOGGER.warn("Returned a polygon Single rule list for geometry type ANY");
			return createSinglePolygonSymbolRulesList(title, withDefaults);
		default:
			throw new IllegalArgumentException("Can't create for type "
					+ geometryForm);
		}
	}

	/**
	 * @return the RulesList that describes the labeling.
	 */
	public TextRuleList createTextRulesList(boolean withDefaults) {
		TextRuleList textRulesList = new TextRuleList(styledFS, withDefaults);
		return textRulesList;
	}

	public UniqueValuesLineRuleList createUniqueValuesLineRulesList(
			boolean withDefaults) {
		UniqueValuesLineRuleList uniqueValuesLineRuleList = new UniqueValuesLineRuleList(
				styledFS);
		return uniqueValuesLineRuleList;
	}

	public UniqueValuesPointRuleList createUniqueValuesPointRulesList(
			boolean withDefaults) {
		UniqueValuesPointRuleList uniqueValuesPointRuleList = new UniqueValuesPointRuleList(
				styledFS);
		return uniqueValuesPointRuleList;
	}

	public UniqueValuesPolygonRuleList createUniqueValuesPolygonRulesList(
			boolean withDefaults) {
		UniqueValuesPolygonRuleList uniqueValuesPolygonRuleList = new UniqueValuesPolygonRuleList(
				styledFS);
		return uniqueValuesPolygonRuleList;
	}

	/**
	 * Uses the styledFeature's {@link GeometryForm}
	 */
	public UniqueValuesRuleList createUniqueValuesRulesList(boolean withDefaults) {
		return createUniqueValuesRulesList(styledFS.getGeometryForm(),
				withDefaults);
	}

	public UniqueValuesRuleList createUniqueValuesRulesList(
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

	public AbstractRulesList importFts(FeatureTypeStyle fts,
			boolean withDefaults) throws AtlasParsingException {

		final String metaInfoString = fts.getName();
		if ((metaInfoString == null)) {
			throw new AtlasParsingException(ASUtil.R(
					"AtlasStyler.ImportError.FTSParsing.ftsNameNull",
					fts.toString()));
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

		throw new AtlasParsingException(ASUtil.R(
				"AtlasStyler.ImportError.FTSParsing.ftsNameUnparsable",
				metaInfoString));
	}

	private GraduatedColorRuleList importGraduatedColorRulesList(
			FeatureTypeStyle fts) {
		String metaInfoString = fts.getName();

		/***************************************************************
		 * Importing everything that starts with QUANTITIES
		 */
		if (!metaInfoString.startsWith("QUANTITIES_COLORIZED"))
			return null;

		GraduatedColorRuleList quantitiesRuleList = null;

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

		quantitiesRuleList.importFts(fts);

		return quantitiesRuleList;

	}

	private SingleRuleList<? extends Symbolizer> importSingleRuleList(
			FeatureTypeStyle fts) throws AtlasParsingException {

		final String metaInfoString = fts.getName();

		Translation title = AtlasStyler.getRuleTitleFor(styledFS);

		/***************************************************************
		 * Importing everything that starts with SINGLE
		 */
		if (metaInfoString.startsWith("SINGLE")) {

			SingleRuleList<? extends Symbolizer> singleRuleList = null;

			/***********************************************************
			 * Importing a SINGLE_SYMBOL_POINT RuleList
			 */
			if (metaInfoString.startsWith(RulesListType.SINGLE_SYMBOL_POINT
					.toString())) {
				singleRuleList = createSinglePointSymbolRulesList(title, false);
			} else
			/***********************************************************
			 * Importing a SINGLE_SYMBOL_LINE RuleList
			 */
			if (metaInfoString.startsWith(RulesListType.SINGLE_SYMBOL_LINE
					.toString())) {
				singleRuleList = createSingleLineSymbolRulesList(title, false);
			}
			/***********************************************************
			 * Importing a SINGLE_SYMBOL_POLYGON RuleList
			 */
			else if (metaInfoString
					.startsWith(RulesListType.SINGLE_SYMBOL_POLYGON.toString())) {
				singleRuleList = createSinglePolygonSymbolRulesList(title,
						false);
			} else {
				throw new AtlasParsingException("metaInfoString = "
						+ metaInfoString + ", but can not be recognized!");
			}

			singleRuleList.importFts(fts);
			return singleRuleList;
		}
		return null;
	}

	/***************************************************************
	 * Importing everything that starts with TEXT, most likely a
	 * RulesListType.TEXT_LABEL
	 */
	private AbstractRulesList importTextRulesList(FeatureTypeStyle fts) {
		if (!fts.getName().startsWith(RulesListType.TEXT_LABEL.toString()))
			return null;

		TextRuleList textRulesList = createTextRulesList(false);

		textRulesList.importFts(fts);

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

			uniqueRuleList.importFts(fts);

			return uniqueRuleList;
		}

		return null;
	}

}
