/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package skrueger.creator.chartwizard;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardPage;

import schmitzm.jfree.chart.style.ChartAxisStyle;
import schmitzm.jfree.chart.style.ChartLabelStyle;
import schmitzm.jfree.chart.style.ChartPlotStyle;
import schmitzm.jfree.chart.style.ChartRendererStyle;
import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.jfree.chart.style.ChartType;
import schmitzm.jfree.feature.style.FeatureBasicChartStyle;
import schmitzm.jfree.feature.style.FeatureChartStyle;
import schmitzm.jfree.feature.style.FeatureScatterChartStyle;
import skrueger.AttributeMetadata;
import skrueger.creator.AtlasCreator;
import skrueger.creator.GpUtil;
import skrueger.geotools.AttributeMetadataMap;
import skrueger.i8n.I8NUtil;
import skrueger.i8n.Translation;

/**
 * The WizardResultProducer generates the final return value of this Wizard: An
 * instance of FeatureBasicChartStyle or <code>null</code>. It also handles the
 * cancel action.
 */
public class ChartWizardResultProducer implements
		WizardPage.WizardResultProducer {
	final static protected Logger LOGGER = Logger
			.getLogger(ChartWizardResultProducer.class);

	public ChartWizardResultProducer() {
		LOGGER.debug("Creating ChartWizardResultProducer");
	}

	public Object finish(Map wizardData) throws WizardException {
		String newID = GpUtil.getRandomID("chartstyle"); // Only numbers are not
		// valid XML agains
		// a NCName field.

		AttributeMetadataMap amdm = (AttributeMetadataMap) wizardData
				.get(ChartWizard.ATTRIBUTEMETADATAMAP);
		// FeatureSource<SimpleFeatureType, SimpleFeature> featureSource =
		// (FeatureSource<SimpleFeatureType, SimpleFeature>) wizardData
		// .get(ChartWizard.FEATURESOURCE);

		ChartType chartType = (ChartType) wizardData.get(ChartWizard.CHARTTYPE);

		FeatureChartStyle chartStyle = (FeatureChartStyle) wizardData
				.get(ChartWizard.EDITCHART);

		if (chartStyle == null) {
			/*
			 * This wizard is not editing a ChartStyle, but creates a new one...
			 */
			LOGGER.debug("Creating a new ChartStyle (not editing one)");

			switch (chartType) {
			// supported types
			case LINE:
			case BAR:
			case AREA:
				chartStyle = new FeatureBasicChartStyle(newID, chartType);
				// chartStyle.getRendererStyle(0).setSeriesShapesVisible(0,
				// true);
				break;
			case SCATTER:
				chartStyle = new FeatureScatterChartStyle(newID);

				// Automatically activate the 0-axis and the center property
				if (chartStyle.getPlotStyle() == null) {
					chartStyle.setPlotStyle(new ChartPlotStyle());
				}
				ChartPlotStyle plotStyle = chartStyle.getPlotStyle();
				plotStyle.setCenterOriginSymetrically(true);
				plotStyle.setCrosshairVisible(true);

				break;
			default:
				LOGGER.warn("Creating a chart of type " + chartType
						+ " is not yet imlpemented.");
				return null;
			}
			
		}

		ChartRendererStyle rs = new ChartRendererStyle();
		/**
		 * Applying the Axis/Attribute/Series properties
		 */
		int maxDimensions = chartType.getMaxDimensions();
		for (Integer index = 0; maxDimensions < 0 || index < maxDimensions; index++) {
			final String attrName = (String) wizardData
					.get(ChartWizard.ATTRIBUTE_ + index);

			if (attrName == null)
				break;

			chartStyle.setAttributeName(index, attrName);

			chartStyle.setAttributeName(index, attrName);
//			chartStyle.setAttributeNormalized(index, (Boolean) wizardData
//					.get(ChartWizard.NORMALIZE_ + index));

			/*
			 * Ensure we always have a default Translation for the Title of this
			 * series
			 */
//			AttributeMetadata attribMetadata = ASUtil.getAttributeMetadataFor(
//					amdm, attrName);
			AttributeMetadata attribMetadata = amdm.get(attrName);
			
			if (I8NUtil.isEmpty(attribMetadata.getTitle())) {
				attribMetadata.setTitle(new Translation((List<String>) (wizardData.get(ChartWizard.LANGUAGES)), attrName));
			}

			/*
			 * Set options specific for the range attributes
			 */
			if (index > 0) {
				// This is NOT the DOMAIN AXIS (index - 1)
				rs.setSeriesLegendVisible(index - 1, true);

				rs.setSeriesLegendLabel(index - 1, new ChartLabelStyle(
						attribMetadata.getTitle().copy(), null));
				rs.setSeriesLegendTooltip(index - 1, new ChartLabelStyle(
						attribMetadata.getDesc().copy(), null));
				if (index == 1) {
					chartStyle.setAxisStyle(ChartStyle.RANGE_AXIS,
							new ChartAxisStyle(attribMetadata.getTitle().copy(), null, 0., 0.));
					chartStyle.getAxisStyle(ChartStyle.RANGE_AXIS)
							.setUnitString(attribMetadata.getUnit());
				}
			} else {
				// This is the DOMAIN AXIS (index = 0)
				chartStyle.setAxisStyle(index, new ChartAxisStyle(
						attribMetadata.getTitle().copy(), null, 0., 0.));
			}
		}

		chartStyle.setRendererStyle(0, rs);

		chartStyle.setSortDomainAxis(wizardData
				.get(ChartWizard.SORT_DOMAIN_AXIS) != null
				&& (Boolean) wizardData.get(ChartWizard.SORT_DOMAIN_AXIS));
		chartStyle.setForceCategories(wizardData
				.get(ChartWizard.DOMAIN_FORCE_CATEGORY) != null
				&& (Boolean) wizardData.get(ChartWizard.DOMAIN_FORCE_CATEGORY));

		Translation title = (Translation) wizardData.get(ChartWizard.TITLE);
		Translation desc = (Translation) wizardData.get(ChartWizard.DESC);

		chartStyle.setTitleStyle(new ChartLabelStyle(title, Color.black));
		chartStyle.setDescStyle(new ChartLabelStyle(desc, Color.gray));

		// LOGGER.debug("The just created/edited chart has editChart.getAttributeCount() "+chartStyle.getAttributeCount());

//		System.out.println(chartStyle);

		return chartStyle;
	}

	public boolean cancel(Map settings) {
		boolean dialogShouldClose = JOptionPane.showConfirmDialog(null,
				AtlasCreator.R("ChartWizard.CancelWizardQuestion")) == JOptionPane.OK_OPTION; // i8n
		return dialogShouldClose;
	}

};
