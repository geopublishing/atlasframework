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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.QuantitiesRulesListsInterface;
import org.geopublishing.atlasStyler.classification.ClassificationChangeEvent.CHANGETYPES;
import org.geotools.data.DefaultQuery;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Attribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

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
public class QuantitiesClassification extends FeatureClassification implements QuantitiesRulesListsInterface {

	/**
	 * This CONSTANT is only used in the JCombobox. NORMALIZER_FIELD String is
	 * null, and in the SLD a "null"
	 */
	public static final String NORMALIZE_NULL_VALUE_IN_COMBOBOX = "-";

	/**
	 * Rplaces all values in the classification limits that can make problems
	 * when exported to XML. Geoserver 2.0.1 is not compatible with -Inf, Inf,
	 * Na
	 * 
	 * @param replacement
	 *            A number that will replace Inf+ as abs(replacement), and Inf-
	 *            as -abs(replacement)
	 */
	public static TreeSet<Double> removeNanAndInf(
			QuantitiesClassification classifier, Double replacement) {

		// Filter class limits against -Inf and +Inf. GS doesn't like them
		TreeSet<Double> newClassLimits = new TreeSet<Double>();
		for (Double l : classifier.getClassLimits()) {
			if (l == Double.NEGATIVE_INFINITY)
				l = -1. * Math.abs(replacement);
			if (l == Double.POSITIVE_INFINITY)
				l = Math.abs(replacement);
			if (l == Double.NaN)
				l = 0.;
			newClassLimits.add(l);
		}
		classifier.setClassLimits(newClassLimits);
		return newClassLimits;
	}

	volatile private boolean cancelCalculation;

	/**
	 * If the classification contains 5 classes, then we have to save 5+1
	 * breaks.
	 */
	protected volatile TreeSet<Double> breaks = null;
	// protected volatile TreeSet<Double> classLimits;

	final String handle = "statisticsQuery";

	/**
	 * Count of digits the (quantile) classes are rounded to. A negative value
	 * means round to digits BEFORE comma! If {@code null} (this is the
	 * default!) no round is performed.
	 */
	private Integer limitsDigits = null;

	protected Logger LOGGER = LangUtil.createLogger(this);

	private String normalizer_field_name;

	private DefaultComboBoxModel normlizationAttribsComboBoxModel;

	private int numClasses = 5;

	private boolean recalcAutomatically = true;

	private DynamicBin1D stats = null;

	protected String value_field_name;

	private DefaultComboBoxModel valueAttribsComboBoxModel;

	private final LimitedHashMap<String, DynamicBin1D> staticStatsCache = new LimitedHashMap<String, DynamicBin1D>(
			20);

	private METHOD method;

	/**
	 * @param featureSource
	 *            The featuresource to use for the statistics
	 */
	public QuantitiesClassification(
			final StyledFeaturesInterface<?> styledFeatures) {
		this(styledFeatures, null, null);
	}

	/**
	 * @param featureSource
	 *            The featuresource to use for the statistics
	 * @param value_field_name
	 *            The column that is used for the classification
	 */
	public QuantitiesClassification(
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
	 *            If null, no normalization will be used
	 */
	public QuantitiesClassification(StyledFeaturesInterface<?> styledFeatures,
			final String value_field_name, final String normalizer_field_name) {
		super(styledFeatures);
		this.value_field_name = value_field_name;
		this.normalizer_field_name = normalizer_field_name;
	}

	/**
	 * Calculates the {@link TreeSet} of classLimits, blocking the thread.
	 */
	public TreeSet<Double> calculateClassLimitsBlocking() throws IOException,
			InterruptedException {
		/**
		 * Do we have all necessary information to calculate ClassLimits?
		 */
		if (getMethod() == METHOD.MANUAL) {
			LOGGER.warn("calculateClassLimitsBlocking has been called but METHOD == MANUAL");
			return getClassLimits();
		}
		if (value_field_name == null)
			throw new IllegalStateException("valueFieldName has to be set");
		if (getMethod() == null)
			throw new IllegalStateException("method has to be set");

		switch (method) {
		case EI:
			return getEqualIntervalLimits();

		case QUANTILES:
		default:
			return getQuantileLimits();
		}
	}

	public void calculateClassLimitsBlockingQuite() {
		/**
		 * Do we have all necessary information to calculate ClassLimits?
		 */
		if (value_field_name == null)
			return;

		pushQuite();
		TreeSet<Double> newLimits;
		try {
			newLimits = calculateClassLimitsBlocking();
			setClassLimits(newLimits);
			// } catch (InterruptedException e) {
			// setQuite(stackQuites.pop());
			// } catch (CancellationException e) {
			// setQuite(stackQuites.pop());
			// } catch (IOException exception) {
			// setQuite(stackQuites.pop());
		} catch (IOException e) {
			LOGGER.error(e);
		} catch (InterruptedException e) {
			LOGGER.error(e);
		} finally {
			popQuite();
		}

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
		stats = null;
	}

	@Override
	public void fireEvent(final ClassificationChangeEvent e) {

		if (isQuite()) {
			lastOpressedEvent = e;
			return;
		} else {
			lastOpressedEvent = null;
		}

		if (e.getType() == CHANGETYPES.START_NEW_STAT_CALCULATION
				&& getMethod() == METHOD.MANUAL)
			return;

		super.fireEvent(e);

		boolean calcNewStats = true;

		if (getMethod() == METHOD.MANUAL) {
			// Never calculate statistics (including new class breaks?!) when we
			// are manual. TODO Maybe that should be moved to the stuff after
			// getStats...
			calcNewStats = false;
		} else if (e.getType() == CHANGETYPES.START_NEW_STAT_CALCULATION) {
			calcNewStats = false;
		} else if (e.getType() == CHANGETYPES.CLASSES_CHG) {
			calcNewStats = false;
		}

		if ((calcNewStats) && (recalcAutomatically)) {
			LOGGER.debug("Starting to calculate new class-limits on another thread due to "
					+ e.getType().toString());
			calculateClassLimitsBlockingQuite();
		}
	}

	/**
	 * @return A {@link ComboBoxModel} that contains a list of class numbers.<br/>
	 *         When we supported SD as a classification METHOD long ago, this
	 *         retured something dependent on the {@link #method}.
	 *         Not it always returns a list of numbers.
	 */
	public ComboBoxModel getClassificationParameterComboBoxModel() {

		DefaultComboBoxModel nClassesComboBoxModel = new DefaultComboBoxModel(
				new Integer[] { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 });

		switch (method) {
		case EI:
		case QUANTILES:
		default:
			nClassesComboBoxModel.setSelectedItem(numClasses);
			return nClassesComboBoxModel;

		}
	}

	public TreeSet<Double> getClassLimits() {
		return breaks;
	}

	/**
	 * Returns the number of digits the (quantile) class limits are rounded to.
	 * Positive values means round to digits AFTER comma, Negative values means
	 * round to digits BEFORE comma.
	 * 
	 * @return {@code null} if no round is performed
	 */
	public Integer getClassValueDigits() {
		return this.limitsDigits;
	}

	/**
	 * Equal Interval Classification method divides a set of attribute values
	 * into groups that contain an equal range of values. This method better
	 * communicates with continuous set of data. The map designed by using equal
	 * interval classification is easy to accomplish and read . It however is
	 * not good for clustered data because you might get the map with many
	 * features in one or two classes and some classes with no features because
	 * of clustered data.
	 * 
	 * @return nClasses + 1 breaks
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public TreeSet<Double> getEqualIntervalLimits() throws IOException,
			InterruptedException {

		getStatistics();

		breaks = new TreeSet<Double>();
		final Double step = 100. / numClasses;
		final double max = stats.max();
		final double min = stats.min();
		for (double i = 0; i < 100;) {
			final double percent = (i) * 0.01;
			final double equal = min + (percent * (max - min));
			breaks.add(equal);
			i = i + step;
		}
		breaks.add(max);
		breaks = ASUtil.roundLimits(breaks, limitsDigits);

		return breaks;
	}

	public METHOD getMethod() {
		return method;
	}

	/**
	 * @return the name of the {@link Attribute} used for the normalization of
	 *         the value. e.g. value = value field / normalization field
	 */
	public String getNormalizer_field_name() {
		return normalizer_field_name;
	}

	@Override
	public int getNumClasses() {
		return numClasses;
	}

	/**
	 * Quantiles classification method distributes a set of values into groups
	 * that contain an equal number of values. This method places the same
	 * number of data values in each class and will never have empty classes or
	 * classes with too few or too many values. It is attractive in that this
	 * method always produces distinct map patterns.
	 * 
	 * @return nClasses + 1 breaks
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public TreeSet<Double> getQuantileLimits() throws IOException,
			InterruptedException {

		getStatistics();

		// LOGGER.debug("getQuantileLimits numClasses ziel variable ist : "
		// + numClasses);

		breaks = new TreeSet<Double>();
		final Double step = 100. / new Double(numClasses);
		for (double i = 0; i < 100;) {
			final double percent = (i) * 0.01;
			final double quantile = stats.quantile(percent);
			breaks.add(quantile);
			i = i + step;
		}
		breaks.add(stats.max());
		breaks = ASUtil.roundLimits(breaks, limitsDigits);

		// // Special case: Create a second classLimit with the same value!
		// if (breaks.size() == 1) {
		// breaks.add(breaks.first());
		// }

		return breaks;
	}

	/**
	 * Determine if you want the classification to be recalculated whenever if
	 * makes sense automatically. Events for START calculation and
	 * NEW_STATS_AVAIL will be fired.
	 */
	public boolean getRecalcAutomatically() {
		return recalcAutomatically;
	}

	/**
	 * This is where the magic happens. Here the attributes of the features are
	 * summarized in a {@link DynamicBin1D} class.
	 * 
	 * @throws IOException
	 */
	synchronized public DynamicBin1D getStatistics()
			throws InterruptedException, IOException {

		cancelCalculation = false;

		if (value_field_name == null)
			throw new IllegalArgumentException("value field has to be set");
		if (normalizer_field_name == value_field_name)
			throw new RuntimeException(
					"value field and the normalizer field may not be equal.");

		stats = staticStatsCache.get(getKey());
		// stats = null;

		if (stats == null) {
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
			resetNoDataCount();

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
					if (cancelCalculation) {
						stats = null;
						throw new InterruptedException(
								"The statistics calculation has been externally interrupted by setting the 'cancelCalculation' flag.");
					}

					final SimpleFeature f = iterator.next();

					// Filter VALUE for NODATA
					final Object filtered = amd.fiterNodata(f
							.getAttribute(value_field_name));
					if (filtered == null) {
						increaseNoDataValue();
						continue;
					}

					numValue = ((Number) filtered).doubleValue();

					if (normalizer_field_name != null) {

						// Filter NORMALIZATION DIVIDER for NODATA
						Object filteredNorm = amdNorm.fiterNodata(f
								.getAttribute(normalizer_field_name));
						if (filteredNorm == null) {
							increaseNoDataValue();
							continue;
						}

						valueNormDivider = ((Number) filteredNorm)
								.doubleValue();
						if (valueNormDivider == 0.
								|| valueNormDivider.isInfinite()
								|| valueNormDivider.isNaN()) {
							// Even if it is not defined as a NODATA value,
							// division by null is not definied.
							increaseNoDataValue();
							continue;
						}

						numValue = numValue / valueNormDivider;
					}

					stats_local.add(numValue);

				}

				stats = stats_local;

				staticStatsCache.put(getKey(), stats);

			} finally {
				features.close(iterator);
			}
		}

		return stats;
	}

	/**
	 * @return A combination of StyledFeatures, Value_Field and Norm_Field. This
	 *         String is the Key for the {@link #staticStatsCache}.
	 */
	private String getKey() {
		return "ID=" + getStyledFeatures().getId() + " VALUE="
				+ value_field_name + " NORM=" + normalizer_field_name
				+ " FILTER=" + getStyledFeatures().getFilter();
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
		if (getMethod() == METHOD.MANUAL) {
			fireEvent(new ClassificationChangeEvent(CHANGETYPES.CLASSES_CHG));
		} else
			calculateClassLimitsBlockingQuite();
	}

	public void setCancelCalculation(final boolean cancelCalculation) {
		this.cancelCalculation = cancelCalculation;

	}

	public void setClassLimits(final TreeSet<Double> classLimits_) {

		// if (classLimits_.size() == 1) {
		// // Special case: Create a second classLimit with the same value!
		// classLimits_.add(classLimits_.first());
		// }

		this.breaks = classLimits_;
		this.numClasses = classLimits_.size() - 1;

		fireEvent(new ClassificationChangeEvent(CHANGETYPES.CLASSES_CHG));
	}

	// ms-01.sn
	/**
	 * Setzs the number of digits the (quantile) class limits are rounded to. If
	 * set to {@code null} no round is performed.
	 * 
	 * @param digits
	 *            positive values means round to digits AFTER comma, negative
	 *            values means round to digits BEFORE comma
	 * 
	 *            TODO abgleichen mit QuantitiesRuleListe#setClassDigits
	 */
	public void setLimitsDigits(final Integer digits) {
		this.limitsDigits = digits;
	}

	public void setMethod(final METHOD newMethod) {
		if ((method != null)
				&& (method != newMethod)) {
			method = newMethod;

			fireEvent(new ClassificationChangeEvent(CHANGETYPES.METHODS_CHG));
		}
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

	public void setNumClasses(final Integer numClasses2) {
		if (numClasses2 != null && !numClasses2.equals(numClasses)) {
			numClasses = numClasses2;
			// LOGGER.debug("QuanClassification set NumClasses to " +
			// numClasses2
			// + " and fires event");
			fireEvent(new ClassificationChangeEvent(CHANGETYPES.NUM_CLASSES_CHG));
		}

	}

	/**
	 * Determine if you want the classification to be recalculated whenever if
	 * makes sense automatically. Events for START calculation and
	 * NEW_STATS_AVAIL will be fired if set to <code>true</code> Default is
	 * <code>true</code>. Switching it to false can be usefull for tests. If set
	 * to <false> you have to call {@link #calculateClassLimitsBlocking()} to
	 * update the statistics.
	 */
	public void setRecalcAutomatically(final boolean b) {
		recalcAutomatically = b;
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

}
