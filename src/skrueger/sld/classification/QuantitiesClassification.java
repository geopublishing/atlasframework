/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the AtlasStyler SLD/SE Editor application - A Java Swing-based GUI to create OGC Styled Layer Descriptor (SLD 1.0) / OGC Symbology Encoding 1.1 (SE) XML files.
 * http://www.geopublishing.org
 * 
 * AtlasStyler SLD/SE Editor is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.sld.classification;

import hep.aida.bin.DynamicBin1D;

import java.io.IOException;
import java.util.TreeSet;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Attribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import schmitzm.lang.LangUtil;
import skrueger.geotools.StyledFeaturesInterface;
import skrueger.sld.ASUtil;
import skrueger.sld.AtlasStyler;
import skrueger.sld.classification.ClassificationChangeEvent.CHANGETYPES;

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
public class QuantitiesClassification extends FeatureClassification {

	protected Logger LOGGER = ASUtil.createLogger(this);

	final static DefaultComboBoxModel nClassesComboBoxModel = new DefaultComboBoxModel(
			new Integer[] { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 });

	/**
	 * This CONSTANT is only used in the JCombobox. NORMALIZER_FIELD String is
	 * null, and in the SLD a "null"
	 */
	public static final String NORMALIZE_NULL_VALUE_IN_COMBOBOX = "-";

	@Override
	public void fireEvent(final ClassificationChangeEvent e) {

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
			LOGGER
					.debug("Starting to calculate new class-limits on another thread due to "
							+ e.getType().toString());
			calculateClassLimitsWithWorker();
		}
	}

	private String value_field_name;

	private String normalizer_field_name;

	private DefaultComboBoxModel valueAttribsComboBoxModel;

	private DefaultComboBoxModel normlizationAttribsComboBoxModel;

	/**
	 * Count of digits the (quantile) classes are rounded to. A negative value
	 * means round to digits BEFORE comma! If {@code null} (this is the
	 * default!) no round is performed.
	 */
	private Integer limitsDigits = null;

	/**
	 * If the classification contains 5 classes, then we have to save 5+1
	 * breaks.
	 */
	protected volatile TreeSet<Double> breaks = null;

	private DynamicBin1D stats = null;

	final String handle = "statisticsQuery";

	static final public int MAX_FEATURES_DEFAULT = 10000;

	public static final METHOD DEFAULT_METHOD = METHOD.QUANTILES;

	/**
	 * Different Methods to classify
	 */
	public enum METHOD {
		EI, MANUAL, QUANTILES;

		public String getDesc() {
			return AtlasStyler
					.R("QuantitiesClassifiction.Method.ComboboxEntry."
							+ toString());
		}

		public String getToolTip() {
			return AtlasStyler
					.R("QuantitiesClassifiction.Method.ComboboxEntry."
							+ toString() + ".TT");
		}

	}

	/** The type of classification that is used. Quantiles by default * */
	public METHOD classificationMethod = DEFAULT_METHOD;

	private int numClasses = 5;

	volatile private boolean cancelCalculation;

	volatile private SwingWorker<TreeSet<Double>, String> calculateStatisticsWorker;

	protected volatile TreeSet<Double> classLimits;

	private boolean recalcAutomatically = true;

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
	 */
	public QuantitiesClassification(
			final StyledFeaturesInterface<?> styledFeatures) {
		this(styledFeatures, null, null);
	}

	@Override
	public int getNumClasses() {
		// if (numClasses <= 0 && breaks != null) {
		// LOGGER
		// .debug("getNumCLasses() sets numClasses to ( breaks.size()-1 = "
		// + (breaks.size() - 1)
		// + " ) because would return "
		// + numClasses + " otherwise");
		// numClasses = breaks.size() - 1;
		// }

		// return breaks.size() - 1;
		return numClasses;
	}

	/**
	 * @return A {@link ComboBoxModel} that contains a list of class numbers.<br/>
	 *         When we supported SD as a classification METHOD long ago, this
	 *         retured something dependent on the {@link #classificationMethod}.
	 *         Not it always returns a list of numbers.
	 */
	public ComboBoxModel getClassificationParameterComboBoxModel() {

		switch (classificationMethod) {
		case EI:
		case QUANTILES:
		default:
			nClassesComboBoxModel.setSelectedItem(numClasses);
			return nClassesComboBoxModel;

		}
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
		for (double i = 0; i < 100;) {
			final double percent = (i) * 0.01;
			final double equal = percent * max;
			breaks.add(equal);
			i = i + step;
		}
		breaks.add(max);
		breaks = roundLimits(breaks);

		return breaks;
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

		LOGGER.debug("getQuantileLimits numClasses ziel variable ist : "
				+ numClasses);

		breaks = new TreeSet<Double>();
		final Double step = 100. / new Double(numClasses);
		for (double i = 0; i < 100;) {
			final double percent = (i) * 0.01;
			final double quantile = stats.quantile(percent);
			breaks.add(quantile);
			i = i + step;
		}
		breaks.add(stats.max());
		breaks = roundLimits(breaks);

		// LOGGER.debug(breaks.size() + "  " + breaks);

		return breaks;
	}

	// ms-01.sn
	/**
	 * Rounds all elements of the {@link TreeSet} to the number of digits
	 * specified by {@link #limitsDigits}. The first break (start of the first
	 * interval) is rounded down and the last break (end of last interval) is
	 * rounded up, so that every possible value is still included in one
	 * interval.
	 * 
	 * @param breaksList
	 *            interval breaks
	 * @return a new {@link TreeSet}
	 */
	private TreeSet<Double> roundLimits(final TreeSet<Double> breaksList) {
		// No round -> use the original values
		if (limitsDigits == null)
			return breaksList;

		final TreeSet<Double> roundedBreaks = new TreeSet<Double>();
		for (final double value : breaksList) {
			int roundMode = 0; // normal round
			// begin of first interval must be rounded DOWN, so that all
			// values are included
			if (value == breaksList.first())
				roundMode = -1;
			// end of last interval must be rounded UP, so that all
			// values are included
			if (value == breaksList.last())
				roundMode = 1;

			// round value and put it into the new TreeSet
			roundedBreaks.add(LangUtil.round(value, limitsDigits, roundMode));
		}
		return roundedBreaks;
	}

	// ms-01.en

	/**
	 * This is where the magic happens. Here the attributes of the features are
	 * summarized in a {@link DynamicBin1D} class.
	 */
	synchronized public DynamicBin1D getStatistics()
			throws InterruptedException {

		cancelCalculation = false;

		if (value_field_name == null)
			throw new IllegalArgumentException("value field has to be set");
		if (normalizer_field_name == value_field_name)
			throw new RuntimeException(
					"value field and the normalizer field may not be equal.");

		if (stats == null) {

			/**
			 * Fires a START_CALCULATIONS event to inform listening JTables etc.
			 * about the change *
			 */
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// System.out.println("Fire STart Calculations");
					fireEvent(new ClassificationChangeEvent(
							CHANGETYPES.START_NEW_STAT_CALCULATION));
				}
			});

			// String[] propnames;
			// if (normalizer_field_name != null)
			// propnames = new String[] { value_field_name,
			// normalizer_field_name };
			// else
			// propnames = new String[] { value_field_name };

			// final FilterFactory2 ff = FeatureUtil.FILTER_FACTORY2;

			// TODO Of yourse it makes more sense to only get the properties we
			// // need, but it doesn't seem to work?! 2.10.09
			// final String typeName = featureSource.getName().getLocalPart();
			// //
			// final DefaultQuery query = new DefaultQuery(typeName, filter,
			// MAX_FEATURES_DEFAULT, propnames, handle+new Random().nextInt());
			// //
			// // FeatureCollection<SimpleFeatureType, SimpleFeature> features =
			// // featureSource
			// // .getFeatures(query);

			FeatureCollection<SimpleFeatureType, SimpleFeature> features = getStyledFeatures()
					.getFeatureCollectionFiltered();

			// int anz = features.getNumberOfAttributes();
			// stats = new QuantileBin1D(true, anz, 1.e-4, 1.e-3, 100,
			// rand);
			final DynamicBin1D stats_local = new DynamicBin1D();

			/**
			 * Iterating over the values and inserting them into the statistics
			 */
			final FeatureIterator<SimpleFeature> iterator = features.features();
			try {
				Double numValue, value2;
				while (iterator.hasNext()) {

					// Simulate a slow calculation
					// try {
					// Thread.sleep(4);
					// } catch (InterruptedException e) {
					// e.printStackTrace();
					// }

					final SimpleFeature f = iterator.next();
					final Object rawValue = f.getAttribute(value_field_name);

					if (rawValue == null)
						continue;

					// Remove the check to be faster... mmm.. smart?
					// if (!(rawValue instanceof Number)) {
					// throw new RuntimeException(
					// "The value returned for attrib "
					// + value_field_name
					// + " is not of type Double but "
					// + rawValue.getClass().getSimpleName());
					// }

					numValue = ((Number) rawValue).doubleValue();

					if (normalizer_field_name != null) {
						final Object rawValue2 = f
								.getAttribute(normalizer_field_name);
						if (rawValue2 == null)
							continue;
						value2 = ((Number) rawValue2).doubleValue();
						numValue = numValue / value2;
					}

					LOGGER.debug("addming " + numValue);
					stats_local.add(numValue);

					/**
					 * The calculation process has been stopped from external.
					 */
					if (cancelCalculation) {
						stats = null;
						throw new InterruptedException(
								"The statistics calculation has been externally interrupted by setting the 'cancelCalculation' flag.");
					}
				}

				stats = stats_local;

			} finally {
				features.close(iterator);
			}
		}

		return stats;
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

	/**
	 * Help the GC to clean up this object.
	 */
	public void dispose() {
		stats = null;
	}

	public METHOD getMethod() {
		return classificationMethod;
	}

	// ms-01.sn
	/**
	 * Setzs the number of digits the (quantile) class limits are rounded to. If
	 * set to {@code null} no round is performed.
	 * 
	 * @param digits
	 *            positive values means round to digits AFTER comma, negative
	 *            values means round to digits BEFORE comma
	 */
	public void setLimitsDigits(final Integer digits) {
		this.limitsDigits = digits;
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

	// ms-01.en

	public void setMethod(final METHOD newMethod) {
		if ((classificationMethod != null)
				&& (classificationMethod != newMethod)) {
			classificationMethod = newMethod;

			fireEvent(new ClassificationChangeEvent(CHANGETYPES.METHODS_CHG));
		}
	}

	public void setNumClasses(final Integer numClasses2) {
		if (numClasses2 != null && !numClasses2.equals(numClasses)) {
			numClasses = numClasses2;
			LOGGER.debug("QuanClassification set NumClasses to " + numClasses2
					+ " and fires event");
			fireEvent(new ClassificationChangeEvent(CHANGETYPES.NUM_CLASSES_CHG));
		}

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
			LOGGER
					.warn("calculateClassLimitsBlocking has been called but METHOD == MANUAL");
			// getStatistics();
			return getClassLimits();
		}
		if (value_field_name == null)
			throw new IllegalStateException("valueFieldName has to be set");
		if (getMethod() == null)
			throw new IllegalStateException("method has to be set");

		switch (classificationMethod) {
		case EI:
			return classLimits = getEqualIntervalLimits();

		case QUANTILES:
		default:
			return classLimits = getQuantileLimits();
		}
	}

	public void calculateClassLimitsWithWorker() {
		classLimits = new TreeSet<Double>();

		/**
		 * Do we have all necessary information to calculate ClassLimits?
		 */
		if (value_field_name == null)
			return;

		/**
		 * If there is another thread running, cancel it first. But remember,
		 * that swing-workers may not be reused!
		 */
		if (calculateStatisticsWorker != null
				&& !calculateStatisticsWorker.isDone()) {
			LOGGER.debug("Cancelling calculation on another thread");
			setCancelCalculation(true);
			calculateStatisticsWorker.cancel(true);
		}

		calculateStatisticsWorker = new SwingWorker<TreeSet<Double>, String>() {

			@Override
			protected TreeSet<Double> doInBackground() throws IOException,
					InterruptedException {
				return calculateClassLimitsBlocking();
			}

			@Override
			protected void done() {
				LOGGER
						.debug("DONE with calculateStatisticsWorker\n  new numCLasses = "
								+ classLimits.size() + " " + numClasses);
				try {
					final TreeSet<Double> newLimits = get();
					LOGGER.debug("newLimits = " + newLimits);
					setClassLimits(newLimits);

					if (!cancelCalculation)
						QuantitiesClassification.this
								.fireEvent(new ClassificationChangeEvent(
										CHANGETYPES.CLASSES_CHG));
				} catch (final Exception e) {
					LOGGER
							.info(
									"calculateStatisticsWorker finished with Exception:",
									e);
					// ExceptionDialog.show(null, e);
				}
			}

		};

		calculateStatisticsWorker.execute();

	}

	public TreeSet<Double> getClassLimits() {
		return classLimits;
	}

	public void setCancelCalculation(final boolean cancelCalculation) {
		this.cancelCalculation = cancelCalculation;

	}

	/**
	 * Return a cached {@link ComboBoxModel} that present all available
	 * attributes. Its connected to the
	 * {@link #createNormalizationFieldsComboBoxModel()}
	 */
	public ComboBoxModel getValueFieldsComboBoxModel() {
		if (valueAttribsComboBoxModel == null)
			valueAttribsComboBoxModel = new DefaultComboBoxModel(ASUtil
					.getNumericalFieldNames(getStyledFeatures().getSchema(),
							false).toArray());
		return valueAttribsComboBoxModel;
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
		for (final String fn : ASUtil.getNumericalFieldNames(
				getStyledFeatures().getSchema(), false)) {
			if (fn != valueAttribsComboBoxModel.getSelectedItem())
				normlizationAttribsComboBoxModel.addElement(fn);
			else {
				// System.out.println("Omittet field" + fn);
			}
		}
		return normlizationAttribsComboBoxModel;
	}

	/**
	 * @return the name of the {@link Attribute} used for the value. It may
	 *         additionally be normalized if #
	 */
	public String getValue_field_name() {
		return value_field_name;
	}

	/**
	 * @return the name of the {@link Attribute} used for the normalization of
	 *         the value. e.g. value = value field / normalization field
	 */
	public String getNormalizer_field_name() {
		return normalizer_field_name;
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
	 * Determine if you want the classification to be recalculated whenever if
	 * makes sense automatically. Events for START calculation and
	 * NEW_STATS_AVAIL will be fired.
	 */
	public boolean getRecalcAutomatically() {
		return recalcAutomatically;
	}

	public void setClassLimits(final TreeSet<Double> classLimits2) {
		this.classLimits = classLimits2;
		this.numClasses = classLimits2.size() - 1;
		fireEvent(new ClassificationChangeEvent(CHANGETYPES.CLASSES_CHG));
	}

	/**
	 * Will trigger recalculating the statistics including firing events
	 */
	public void onFilterChanged() {
		stats = null;
		if (getMethod() == METHOD.MANUAL) {
			fireEvent(new ClassificationChangeEvent(CHANGETYPES.CLASSES_CHG));
		} else
			calculateClassLimitsWithWorker();
	}

}