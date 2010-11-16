package org.geopublishing.atlasStyler;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.AbstractRulesList.RulesListType;
import org.geotools.data.FeatureSource;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Symbolizer;

import schmitzm.geotools.feature.FeatureUtil.GeometryForm;
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

	/**
	 * Uses the styledFeature's {@link GeometryForm}
	 */
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

	public SingleRuleList<? extends Symbolizer> createSingleRulesList(
			final GeometryForm geometryForm, boolean withDefaults) {
		switch (geometryForm) {
		case LINE:
			return createSingleLineSymbolRulesList(withDefaults);
		case POINT:
			return createSinglePointSymbolRulesList(withDefaults);
		case POLYGON:
			return createSinglePolygonSymbolRulesList(withDefaults);
		case ANY:
			LOGGER.warn("Returned a polygon Single rule list for geometry type ANY");
			return createSinglePolygonSymbolRulesList(withDefaults);
		default:
			throw new IllegalArgumentException("Can't create for type "
					+ geometryForm);
		}
	}

	/**
	 * Uses the styledFeature's {@link GeometryForm}
	 */
	public SingleRuleList<? extends Symbolizer> createSingleRulesList(
			boolean withDefaults) {
		return createSingleRulesList(styledFS.getGeometryForm(), withDefaults);
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

	private SingleRuleList<? extends Symbolizer> importSingleRuleList(
			FeatureTypeStyle fts) throws AtlasParsingException {

		final String metaInfoString = fts.getName();

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

			singleRuleList.importFts(fts);
			return singleRuleList;
		}
		return null;
	}

}
