/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.chartwizard;

import java.awt.LayoutManager;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geotools.data.FeatureSource;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardBranchController;
import org.netbeans.spi.wizard.WizardPage;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.jfree.chart.style.ChartType;
import schmitzm.jfree.feature.style.FeatureChartStyle;
import skrueger.AttributeMetadataImpl;
import skrueger.geotools.AttributeMetadataMap;
import skrueger.i8n.Translation;

/**
 * Static class that creates a chart-definition-wizard for a given
 * {@link FeatureSource} and (optional) a Map of {@link AttributeMetadataImpl}. The
 * static method {@link #showWizard(FeatureSource, Map)} returns
 * <code>null</code> or an instance of {@link FeatureChartStyle}
 * 
 * @author Stefan Alfons Krueger
 * 
 */
public class ChartWizard extends WizardBranchController {

	private static final ChartWizardResultProducer FINISHER = new ChartWizardResultProducer();

	final static protected Logger LOGGER = Logger.getLogger(ChartWizard.class);

	/**
	 * List of two-letter ISO language codes that this {@link ChartWizard} will
	 * asks translations for. Defaults to only the active language
	 */
	private static List<String> languages = Arrays
			.asList(new String[] { Translation.getActiveLang() });

	// Probably we really want to create these lazily if they are
	// needed, but the example is clearer this way...</font>
	Class<?>[] singleAttribPath = new Class[] {
			AttributeSelectionWizardPanel.class,
			ChartTitleDescriptionWizardPanel.class };

	// Probably we really want to create these lazily if they are
	// needed, but the example is clearer this way...</font>
	private Class[] doubleAttribPath = new Class[] {
			AttributeSelectionWizardPanel.class,
			ChartTitleDescriptionWizardPanel.class };

	protected ChartWizard() {
		// Create the base pages - these are also WizardPage subclasses
		super(new WizardPage[] { new ChartTypeSelectionWizardPanel() });
	}

	public static final String LANGUAGES = "languages";
	public static final String ATTRIBUTEMETADATAMAP = "attributeMetadataMap";
	public static final String FEATURESOURCE = "featureSource";
	public static final String EDITCHART = "nullOrChartStyleToEdit_FetureChartType";

	public static final String CHARTTYPE = "typeOfThisChart";
	public static final String ATTRIBUTE_ = "moreAttributes";
	public static final Object NUMBER_OF_ATTRIBS = "count_atributes";
	public static final Object NUMBER_OF_NUMERIC_ATTRIBS = "count_numeric_atributes";

	public static final String TITLE = "chartTitle_Translation";
	public static final String DESC = "chartDescription_Translation";

	/**
	 * Sometimes the {@link LayoutManager} needs to know the width we are
	 * working on to make long lines wrap propperly.
	 */
	public static final int WIDTH_DEFAULT = 580;
	public static final int HEIGHT_DEFAULT = 350;

	/** Is initialized with <code>true</code> **/
	public static final String SORT_DOMAIN_AXIS = "sortDomainAxisData_Boolean";

	/** Is initialized with <code>false</code> **/
	public static final String DOMAIN_FORCE_CATEGORY = "forceCategoryDataEvenIfNumeric_Boolean";

//	public static final String NORMALIZE_ = "normlizeSettingForIdx_Boolean";

	public static final String TYPECHANGED = "kklökljö";

	/**
	 * Opens a Wizard that allows to edit an existing instance of
	 * {@link FeatureChartStyle}
	 * 
	 * @param featureSource
	 *            Where the data comes from
	 * @param attributeMetaDataMap
	 *            Map of {@link AttributeMetadataImpl}
	 * @param languages
	 *            Number of languages that are supported by the atlas
	 * @return Changed {@link FeatureChartStyle}
	 */
	public static FeatureChartStyle showWizard(
			FeatureChartStyle chartEdit,
			final FeatureSource<SimpleFeatureType, SimpleFeature> featureSource,
			final AttributeMetadataMap attributeMetaDataMap,
			List<String> languages) {
		setLanguages(languages);
		return showWizard(chartEdit, featureSource, attributeMetaDataMap);
	}

	/**
	 * Opens a Wizard that allows to create a new instance of
	 * {@link FeatureChartStyle}
	 * 
	 * @param featureSource
	 *            Where the data comes from
	 * @param attributeMetaDataMap
	 *            Map of {@link AttributeMetadataImpl}
	 * @param languages
	 *            Number of languages that are supported by the atlas
	 * @return <code>null</code> or a newly created {@link FeatureChartStyle}
	 */
	public static FeatureChartStyle showWizard(
			final FeatureSource<SimpleFeatureType, SimpleFeature> featureSource,
			final AttributeMetadataMap attributeMetaDataMap,
			List<String> languages) {
		setLanguages(languages);
		return showWizard(null, featureSource, attributeMetaDataMap);
	}

	/**
	 * Opens a Wizard that allows to create a new instance of
	 * {@link FeatureChartStyle}
	 * 
	 * @param featureSource
	 *            Where the data comes from
	 * @param attributeMetaDataMap
	 *            Map of {@link AttributeMetadataImpl}
	 * @return <code>null</code> or a newly created {@link FeatureChartStyle}
	 */
	public static FeatureChartStyle showWizard(
			final FeatureSource<SimpleFeatureType, SimpleFeature> featureSource,
			final AttributeMetadataMap attributeMetaDataMap) {
		return showWizard(null, featureSource, attributeMetaDataMap);
	}

	/**
	 * Opens a Wizard that allows to create a new instance of
	 * {@link FeatureChartStyle}
	 * 
	 * @param editChart
	 *            If not <code>null</code>, the wizard will be filled with the
	 *            settings from this Wizard instance. When finished, this
	 *            instance will be returned, with modified settings.
	 * @param featureSource
	 *            Where the data comes from
	 * @param attributeMetaDataMap
	 *            Map of {@link AttributeMetadataImpl}
	 * @return <code>null</code> or a newly created {@link FeatureChartStyle}
	 */
	public static FeatureChartStyle showWizard(
			FeatureChartStyle editChart,
			final FeatureSource<SimpleFeatureType, SimpleFeature> featureSource,
			final AttributeMetadataMap attributeMetaDataMap) {

		// /**
		// * Here the steps (sub-classes of WindzardPanels) of the wizard(s) are
		// * defined:
		// */
		// Wizard wiz = WizardPage.createWizard(new Class[] {
		// ChartTypeSelectionPanel.class, AttributSelectionPanel.class },
		// wizResultProducer);

		ChartWizard chartStartWizard = new ChartWizard(); // It's a special

		// WizardBranchControler
		// ;-)
		Wizard wiz = chartStartWizard.createWizard();

		/*
		 * When the wizard starts it shall already contain some initial values
		 * in the wiazard-data map.
		 */
		Map<Object, Object> initialProperties = new HashMap<Object, Object>();
		initialProperties.put(FEATURESOURCE, featureSource);
		initialProperties.put(LANGUAGES, getLanguages());
		initialProperties.put(ATTRIBUTEMETADATAMAP, attributeMetaDataMap);

		/*
		 * We cache the total number if attributes and numerical attributes
		 */
		initialProperties.put(NUMBER_OF_ATTRIBS, ASUtil.getValueFieldNames(
				featureSource, false).size());
		initialProperties.put(NUMBER_OF_NUMERIC_ATTRIBS, FeatureUtil
				.getNumericalFieldNames(featureSource.getSchema(), false).size());

		if (editChart == null) {
			/*
			 * Initialize the wizard-data map with defaults
			 */

			initialProperties.put(SORT_DOMAIN_AXIS, true);
			initialProperties.put(DOMAIN_FORCE_CATEGORY, false);

		} else {
			/*
			 * Initialize the wizard-data map with the values from the given
			 * FeatureChartStyle
			 */
			initialProperties.put(EDITCHART, editChart);

			initialProperties.put(CHARTTYPE, editChart.getType());
			initialProperties.put(SORT_DOMAIN_AXIS, editChart
					.isSortDomainAxis());
			initialProperties.put(DOMAIN_FORCE_CATEGORY, editChart
					.isForceCategories());

			for (Integer index = 0; index < editChart.getAttributeCount(); index++) {
				initialProperties.put(ChartWizard.ATTRIBUTE_ + index, editChart
						.getAttributeName(index));
//				initialProperties.put(ChartWizard.NORMALIZE_ + index, editChart
//						.isAttributeNormalized(index));
			}

			/* The title of the chart */
			initialProperties.put(ChartWizard.TITLE, editChart.getTitleStyle()
					.getLabelTranslation());
			/* The sub-title of the chart */
			initialProperties.put(ChartWizard.DESC, editChart.getDescStyle()
					.getLabelTranslation());
		}

		return (FeatureChartStyle) WizardDisplayer.showWizard(wiz, null, null,
				initialProperties);
	}

	/**
	 * It's being called all the time, so the Wizard can figure out which is the
	 * next step.
	 */
	@Override
	public Wizard getWizardForStep(String step, Map data) {
		// LOGGER.debug("Get Wizard For Step " + step + " with " + data);
		// The class name is the default ID for instantiated WizardPages
		// LOGGER.debug("Get Wizard For Step " + step);
		// if ("skrueger.charts.ChartTypeSelectionPanel".equals(step)) {
		final ChartType ct = (ChartType) data.get(CHARTTYPE);
		// LOGGER.debug("ChartType is " + ct);
		if (ChartType.PIE == ct) {
			// LOGGER.debug("Go to DoubleAttribSelection!");
			// Create a wizard for this sequence of steps
			Wizard singleAttribPathWiz = WizardPage.createWizard(
					singleAttribPath, FINISHER);
			return singleAttribPathWiz;
		} else {
			// LOGGER.debug("Go to SingleAttribSelection");
			// Create a wizard for this sequence of steps
			Wizard doubleAttribPathWiz = WizardPage.createWizard(
					doubleAttribPath, FINISHER);
			return doubleAttribPathWiz;
		}
		// }
		// return super.getWizardForStep(step, data);
	}

	public static void setLanguages(List<String> languages) {
		ChartWizard.languages = languages;
	}

	public static List<String> getLanguages() {
		return languages;
	}

}
