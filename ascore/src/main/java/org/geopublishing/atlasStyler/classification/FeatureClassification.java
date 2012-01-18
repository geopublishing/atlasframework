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
package org.geopublishing.atlasStyler.classification;

import hep.aida.bin.DynamicBin1D;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.classification.ClassificationChangeEvent.CHANGETYPES;
import org.geotools.data.DefaultQuery;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;
import org.opengis.feature.Attribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import cern.colt.list.DoubleArrayList;
import de.schmitzm.geotools.data.amd.AttributeMetadataImpl;
import de.schmitzm.geotools.feature.FeatureUtil;
import de.schmitzm.geotools.styling.StyledFeaturesInterface;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.lang.LimitedHashMap;

/**
 * A quantitative classification. The inveralls are defined by upper and lower
 * limits
 * 
 * 
 * @param <T>
 *            The type of the value field
 * 
 * @author stefan
 */
public class FeatureClassification extends Classification {

	/**
	 * This CONSTANT is only used in the JCombobox. NORMALIZER_FIELD String is
	 * null, and in the SLD a "null"
	 */
	public static final String NORMALIZE_NULL_VALUE_IN_COMBOBOX = "-";

	final static private Logger LOGGER = LangUtil
			.createLogger(FeatureClassification.class);

	private String normalizer_field_name;

	private DefaultComboBoxModel normlizationAttribsComboBoxModel;

	/**
	 * Caches up to 8 Statistics. Remember, that the {@link DynamicBin1D}
	 * actually keeps the data in memory!
	 */
	protected final Map<String, DynamicBin1D> staticStatsCache = Collections
			.synchronizedMap(new LimitedHashMap<String, DynamicBin1D>(8));

	protected boolean cacheEnabled = true;
	/**
	 * The styled Feature this {@link Classification} works on.
	 */
	private StyledFeaturesInterface<?> styledFeatures;

	/**
	 * The selected value attribute-field local-name.
	 */
	protected String value_field_name;

	private DefaultComboBoxModel valueAttribsComboBoxModel;

	/**
	 * @param featureSource
	 *            The featuresource to use for the statistics
	 */
	public FeatureClassification(final StyledFeaturesInterface<?> styledFeatures) {
		this(styledFeatures, null, null);
	}

	/**
	 * @param featureSource
	 *            The featuresource to use for the statistics
	 * @param value_field_name
	 *            The column that is used for the classification
	 */
	public FeatureClassification(
			final StyledFeaturesInterface<?> styledFeatures,
			final String value_field_name) {
		this(styledFeatures, value_field_name, null);
	}

	/**
	 * @param featureSource
	 *            The featuresource to use for the statistics
	 * @param layerFilter
	 *            The {@link Filter} that shall be applied whenever asking for
	 *            the {@link FeatureCollection}. <code>null</code> is not
	 *            allowed, use Filter.INCLUDE
	 * @param value_field_name
	 *            The column that is used for the classification
	 * @param normalizer_field_name
	 *            If <code>null</code>, no normalization will be used
	 */
	public FeatureClassification(StyledFeaturesInterface<?> styledFeatures,
			final String value_field_name, final String normalizer_field_name) {
		setStyledFeatures(styledFeatures);
		setValue_field_name(value_field_name);
		setNormalizer_field_name(normalizer_field_name);
	}

	@Override
	public BufferedImage createHistogramImage(boolean showMean, boolean showSd,
			int histogramBins, String label_xachsis)
			throws InterruptedException, IOException {
		HistogramDataset hds = new HistogramDataset();
		DoubleArrayList valuesAL;
		valuesAL = getStatistics().elements();
		// new double[] {0.4,3,4,2,5.,22.,4.,2.,33.,12.}
		double[] elements = Arrays.copyOf(valuesAL.elements(), getStatistics()
				.size());
		hds.addSeries(1, elements, histogramBins);

		/** Statically label the Y Axis **/
		String label_yachsis = ASUtil
				.R("QuantitiesClassificationGUI.Histogram.YAxisLabel");

		JFreeChart chart = org.jfree.chart.ChartFactory.createHistogram(null,
				label_xachsis, label_yachsis, hds, PlotOrientation.VERTICAL,
				false, true, true);

		/***********************************************************************
		 * Paint the classes into the JFreeChart
		 */
		int countLimits = 0;
		for (Double cLimit : getClassLimits()) {
			ValueMarker marker = new ValueMarker(cLimit);
			XYPlot plot = chart.getXYPlot();
			marker.setPaint(Color.orange);
			marker.setLabel(String.valueOf(countLimits));
			marker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
			marker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
			plot.addDomainMarker(marker);

			countLimits++;
		}

		/***********************************************************************
		 * Optionally painting SD and MEAN into the histogram
		 */
		try {
			if (showSd) {
				ValueMarker marker;
				marker = new ValueMarker(getStatistics().standardDeviation(),
						Color.green.brighter(), new BasicStroke(1.5f));
				XYPlot plot = chart.getXYPlot();
				marker.setLabel(ASUtil
						.R("QuantitiesClassificationGUI.Histogram.SD.ShortLabel"));
				marker.setLabelAnchor(RectangleAnchor.BOTTOM_LEFT);
				marker.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
				plot.addDomainMarker(marker);
			}

			if (showMean) {
				ValueMarker marker;
				marker = new ValueMarker(getStatistics().mean(),
						Color.green.darker(), new BasicStroke(1.5f));
				XYPlot plot = chart.getXYPlot();
				marker.setLabel(ASUtil
						.R("QuantitiesClassificationGUI.Histogram.Mean.ShortLabel"));
				marker.setLabelAnchor(RectangleAnchor.BOTTOM_LEFT);
				marker.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
				plot.addDomainMarker(marker);
			}

		} catch (Exception e) {
			LOGGER.error("Painting SD and MEAN into the histogram", e);
		}

		/***********************************************************************
		 * Render the Chart
		 */
		BufferedImage image = chart.createBufferedImage(400, 200);

		return image;
	}

	/**
	 * Return a {@link ComboBoxModel} that present all available attributes.
	 * That excludes the attribute selected in
	 * {@link #getValueFieldsComboBoxModel()}.
	 */
	public ComboBoxModel createNormalizationFieldsComboBoxModel() {
		normlizationAttribsComboBoxModel = new DefaultComboBoxModel();
		normlizationAttribsComboBoxModel
				.addElement(NORMALIZE_NULL_VALUE_IN_COMBOBOX);
		normlizationAttribsComboBoxModel
				.setSelectedItem(NORMALIZE_NULL_VALUE_IN_COMBOBOX);
		for (final String fn : FeatureUtil.getNumericalFieldNames(
				getStyledFeatures().getSchema(), false)) {
			if (fn != valueAttribsComboBoxModel.getSelectedItem())

				if (FeatureUtil.checkAttributeNameRestrictions(fn))
					normlizationAttribsComboBoxModel.addElement(fn);
				else {
					LOGGER.info("Hidden attribut " + fn
							+ " in createNormalizationFieldsComboBoxModel");
				}
			else {
				// System.out.println("Omittet field" + fn);
			}
		}
		return normlizationAttribsComboBoxModel;
	}

	/**
	 * Help the GC to clean up this object.
	 */
	@Override
	public void dispose() {
		super.dispose();
	}

	/**
	 * @return A combination of StyledFeatures, Value_Field and Norm_Field. This
	 *         String is the Key for the {@link #staticStatsCache}.
	 */
	protected String getKey() {
		return "ID=" + getStyledFeatures().getId() + "TITLE=" + getStyledFeatures().getTitle() + " VALUE="
				+ value_field_name + " NORM=" + normalizer_field_name
				+ " FILTER=" + getStyledFeatures().getFilter();
	}

	/**
	 * @return the name of the {@link Attribute} used for the normalization of
	 *         the value. e.g. value = value field / normalization field
	 */
	public String getNormalizer_field_name() {
		return normalizer_field_name;
	}

	/**
	 * This is where the magic happens. Here the attributes of the features are
	 * summarized in a {@link DynamicBin1D} class.
	 * 
	 * @throws IOException
	 */
	@Override
	synchronized public DynamicBin1D getStatistics()
			throws InterruptedException, IOException {

		cancelCalculation.set(false);

		if (value_field_name == null)
			throw new IllegalArgumentException("value field has to be set");
		if (normalizer_field_name == value_field_name)
			throw new RuntimeException(
					"value field and the normalizer field may not be equal.");

		stats = staticStatsCache.get(getKey());
		// stats = null;

		if (stats == null || !cacheEnabled) {
			// Old style.. asking for ALL attributes
			// FeatureCollection<SimpleFeatureType, SimpleFeature> features =
			// getStyledFeatures()
			// .getFeatureCollectionFiltered();

			Filter filter = getStyledFeatures().getFilter();
			DefaultQuery query = new DefaultQuery(getStyledFeatures()
					.getSchema().getTypeName(), filter);
			List<String> propNames = new ArrayList<String>();
			propNames.add(value_field_name);
			if (normalizer_field_name != null)
				propNames.add(normalizer_field_name);
			query.setPropertyNames(propNames);
			FeatureCollection<SimpleFeatureType, SimpleFeature> features = getStyledFeatures()
					.getFeatureSource().getFeatures(query);

			// Forget about the count of NODATA values
			noDataValuesCount.set(0);

			final DynamicBin1D stats_local = new DynamicBin1D();

			// get the AttributeMetaData for the given attribute to filter
			// NODATA values
			final AttributeMetadataImpl amd = getStyledFeatures()
					.getAttributeMetaDataMap().get(value_field_name);
			final AttributeMetadataImpl amdNorm = getStyledFeatures()
					.getAttributeMetaDataMap().get(normalizer_field_name);

			// // Simulate a slow calculation
			// try {
			// Thread.sleep(40);
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }

			/**
			 * Iterating over the values and inserting them into the statistics
			 */
			final FeatureIterator<SimpleFeature> iterator = features.features();
			try {
				Double numValue, valueNormDivider;
				while (iterator.hasNext()) {

					/**
					 * The calculation process has been stopped from external.
					 */
					if (cancelCalculation.get()) {
						stats = null;
						throw new InterruptedException(
								"The statistics calculation has been externally interrupted by setting the 'cancelCalculation' flag.");
					}

					final SimpleFeature f = iterator.next();

					// Filter VALUE for NODATA
					final Object filtered = amd.fiterNodata(f
							.getAttribute(value_field_name));
					if (filtered == null) {
						noDataValuesCount.incrementAndGet();
						continue;
					}

					numValue = ((Number) filtered).doubleValue();

					if (normalizer_field_name != null) {

						// Filter NORMALIZATION DIVIDER for NODATA
						Object filteredNorm = amdNorm.fiterNodata(f
								.getAttribute(normalizer_field_name));
						if (filteredNorm == null) {
							noDataValuesCount.incrementAndGet();
							continue;
						}

						valueNormDivider = ((Number) filteredNorm)
								.doubleValue();
						if (valueNormDivider == 0.
								|| valueNormDivider.isInfinite()
								|| valueNormDivider.isNaN()) {
							// Even if it is not defined as a NODATA value,
							// division by null is not definied.
							noDataValuesCount.incrementAndGet();
							continue;
						}

						numValue = numValue / valueNormDivider;
					}

					stats_local.add(numValue);

				}

				stats = stats_local;

				if (cacheEnabled)
					staticStatsCache.put(getKey(), stats);

			} finally {
				features.close(iterator);
			}
		}

		return stats;
	}

	/**
	 * Remember to apply the associated Filter whenever you access the
	 * {@link FeatureCollection}
	 **/
	public StyledFeaturesInterface<?> getStyledFeatures() {
		return styledFeatures;
	}

	/**
	 * @return the name of the {@link Attribute} used for the value. It may
	 *         additionally be normalized if #
	 */
	public String getValue_field_name() {
		return value_field_name;
	}

	/**
	 * Return a cached {@link ComboBoxModel} that present all available
	 * attributes. Its connected to the
	 */
	public ComboBoxModel getValueFieldsComboBoxModel() {
		if (valueAttribsComboBoxModel == null)
			valueAttribsComboBoxModel = new DefaultComboBoxModel(FeatureUtil
					.getNumericalFieldNames(getStyledFeatures().getSchema(),
							false).toArray());
		return valueAttribsComboBoxModel;
	}

	/**
	 * Will trigger recalculating the statistics including firing events
	 */
	public void onFilterChanged() {
		stats = null;
		if (getMethod() == CLASSIFICATION_METHOD.MANUAL) {
			fireEvent(new ClassificationChangeEvent(CHANGETYPES.CLASSES_CHG));
		} else
			calculateClassLimits();
	}

	/**
	 * Change the LocalName of the {@link Attribute} that shall be used as a
	 * normalizer for the value {@link Attribute}. If <code>null</code> is
	 * passed, the value will not be normalized.
	 * 
	 * @param normalizer_field_name
	 *            {@link Double}.
	 */
	public void setNormalizer_field_name(String normalizer_field_name) {
		// This max actually be set to null!!
		if (this.normalizer_field_name != normalizer_field_name) {
			this.normalizer_field_name = normalizer_field_name;
			stats = null;

			// Das durfte sowieso nie passieren
			if (normalizer_field_name == value_field_name) {
				normalizer_field_name = null;
				throw new IllegalStateException(
						"Die GUI sollte nicht erlauben, dass VALUE und NORMALIZATION field gleich sind.");
			}

			fireEvent(new ClassificationChangeEvent(CHANGETYPES.NORM_CHG));
		}
	}

	public void setStyledFeatures(StyledFeaturesInterface<?> styledFeatures) {
		this.styledFeatures = styledFeatures;
	}

	/**
	 * Change the LocalName of the {@link Attribute} that shall be used for the
	 * values. <code>null</code> is not allowed.
	 * 
	 * @param value_field_name
	 *            {@link Double}.
	 */
	public void setValue_field_name(final String value_field_name) {
		// IllegalArgumentException("null is not a valid value field name");
		if ((value_field_name != null)
				&& (this.value_field_name != value_field_name)) {
			this.value_field_name = value_field_name;
			stats = null;

			if (normalizer_field_name == value_field_name) {
				normalizer_field_name = null;
			}

			fireEvent(new ClassificationChangeEvent(CHANGETYPES.VALUE_CHG));
		}
	}

	public void setCacheEnabled(boolean cacheEnabled) {
		this.cacheEnabled = cacheEnabled;
	}

	public boolean isCacheEnabled() {
		return cacheEnabled;
	}

}
