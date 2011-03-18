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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geopublishing.atlasStyler.classification.ClassificationChangeEvent.CHANGETYPES;

public abstract class Classification {
	/**
	 * If the classification contains 5 classes, then we have to save 5+1
	 * breaks.
	 */
	protected volatile TreeSet<Double> breaks = new TreeSet<Double>();
	protected DynamicBin1D stats = null;

	public final AtomicBoolean cancelCalculation = new AtomicBoolean(false);

	ClassificationChangeEvent lastOpressedEvent = null;

	/**
	 * Count of digits the classes are rounded to. A negative value means round
	 * to digits BEFORE decimal point! If {@code null} (this is the default!) no
	 * round is performed.
	 */
	private Integer limitsDigits = null;

	private final Set<ClassificationChangedListener> listeners = new HashSet<ClassificationChangedListener>();

	private final static Logger LOGGER = Logger.getLogger(Classification.class);

	private CLASSIFICATION_METHOD method = CLASSIFICATION_METHOD.DEFAULT_METHOD;

	/**
	 * Counts the number of NODATA values found and excluded from the
	 * classification
	 **/
	AtomicLong noDataValuesCount = new AtomicLong();

	protected int numClasses = 5;

	/**
	 * If {@link #quite} == <code>true</code> no {@link RuleChangedEvent} will
	 * be fired.
	 */
	private boolean quite = false;

	private boolean recalcAutomatically = true;

	protected Stack<Boolean> stackQuites = new Stack<Boolean>();

	public void addListener(ClassificationChangedListener l) {
		listeners.add(l);
	}
	
	/**
	 * If greate 1, only every nth value is evaluated. 
	 */
	private int subsampling = 1;

	/**
	 * Calculates the {@link TreeSet} of classLimits, blocking the thread.
	 */
	public final TreeSet<Double> calculateClassLimitsBlocking()
			throws IOException, InterruptedException {
		/**
		 * Do we have all necessary information to calculate ClassLimits?
		 */
		if (getMethod() == CLASSIFICATION_METHOD.MANUAL) {
			LOGGER.warn("calculateClassLimitsBlocking has been called but METHOD == MANUAL");
			return getClassLimits();
		}
		if (getMethod() == null)
			throw new IllegalStateException("method has to be set");

		switch (getMethod()) {
		case EI:
			return getEqualIntervalLimits();

		case QUANTILES:
		default:
			return getQuantileLimits();
		}
	}

	public abstract BufferedImage createHistogramImage(boolean showMean,
			boolean showSd, int histogramBins, String xAxisLabel)
			throws InterruptedException, IOException;

	public void dispose() {
		listeners.clear();
		stats.clear();
	}

	/**
	 * @return A {@link ComboBoxModel} that contains a list of class numbers.<br/>
	 *         When we supported SD as a classification METHOD long ago, this
	 *         retured something dependent on the {@link #method}. Not it always
	 *         returns a list of numbers.
	 */
	public ComboBoxModel getClassificationParameterComboBoxModel() {

		DefaultComboBoxModel nClassesComboBoxModel = new DefaultComboBoxModel(
				new Integer[] { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 });

		switch (getMethod()) {
		case EI:
		case QUANTILES:
		default:
			nClassesComboBoxModel.setSelectedItem(numClasses);
			return nClassesComboBoxModel;

		}
	}

	public Double getMax() {
		try {
			return getStatistics().max();
		} catch (Exception e) {
			LOGGER.error("Error calculating statistics", e);
			return null;
		}
	}

	abstract protected DynamicBin1D getStatistics()
			throws InterruptedException, IOException;

	public Double getMean() {
		try {
			return getStatistics().mean();
		} catch (Exception e) {
			LOGGER.error("Error calculating statistics", e);
			return null;
		}
	}

	public Double getMedian() {
		try {
			return getStatistics().median();
		} catch (Exception e) {
			LOGGER.error("Error calculating statistics", e);
			return null;
		}
	}

	public Double getMin() {
		try {
			return getStatistics().min();
		} catch (Exception e) {
			LOGGER.error("Error calculating statistics", e);
			return null;
		}
	}

	public Long getCount() {
		try {
			return Long.valueOf(getStatistics().size());
		} catch (Exception e) {
			LOGGER.error("Error calculating statistics", e);
			return null;
		}
	}

	// public abstract TreeSet<Double> getClassLimits();

	/**
	 * Fires the given {@link ClassificationChangeEvent} to all listeners.
	 */
	final public void fireEvent(final ClassificationChangeEvent evt) {

		if (quite) {
			lastOpressedEvent = evt;
			return;
		} else {
			lastOpressedEvent = null;
		}

		if (evt == null)
			return;

		if (evt.getType() == CHANGETYPES.START_NEW_STAT_CALCULATION
				&& getMethod() == CLASSIFICATION_METHOD.MANUAL)
			return;

		LOGGER.debug("Classification fires event: " + evt.getType());

		if (evt.getType() == CHANGETYPES.NODATAVALUE_CHANGED) {
			// Force expensive recreation of the statistics
			stats = null;
		}

		for (ClassificationChangedListener l : listeners) {
			switch (evt.getType()) {
			case NORM_CHG:
				l.classifierNormalizationChanged(evt);
				break;
			case VALUE_CHG:
				l.classifierValueFieldChanged(evt);
				break;
			case METHODS_CHG:
				l.classifierMethodChanged(evt);
				break;
			case EXCLUDES_FILTER_CHG:
				l.classifierExcludeFilterChanged(evt);
				break;
			case NUM_CLASSES_CHG:
				l.classifierNumClassesChanged(evt);
				break;
			case NODATAVALUE_CHANGED:
				break;
			case START_NEW_STAT_CALCULATION:
				l.classifierCalculatingStatistics(evt);
				break;
			case CLASSES_CHG:
				l.classifierAvailableNewClasses(evt);
				break;

			}
		}

		boolean calcNewStats = true;

		if (getMethod() == CLASSIFICATION_METHOD.MANUAL) {
			// Never calculate statistics (including new class breaks?!) when we
			// are manual. TODO Maybe that should be moved to the stuff after
			// getStats...
			calcNewStats = false;
		} else if (evt.getType() == CHANGETYPES.START_NEW_STAT_CALCULATION) {
			calcNewStats = false;
		} else if (evt.getType() == CHANGETYPES.CLASSES_CHG) {
			calcNewStats = false;
		}

		if ((calcNewStats) && (recalcAutomatically)) {
			LOGGER.debug("Starting to calculate new class-limits on another thread due to "
					+ evt.getType().toString());
			calculateClassLimits();
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
	final public TreeSet<Double> getEqualIntervalLimits() throws IOException {

		breaks = new TreeSet<Double>();
		final Double step = 100. / numClasses;
		final double max = getMax();
		final double min = getMin();
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

	public CLASSIFICATION_METHOD getMethod() {
		return method;
	}

	/**
	 * Counts the number of NODATA values found and excluded from the
	 * classification
	 **/
	public long getNoDataValuesCount() {
		return noDataValuesCount.longValue();
	}

	/**
	 * @return Returns the number of classes
	 */
	public int getNumClasses() {
		return numClasses;
	}

	public Double getSum() {
		try {
			return getStatistics().sum();
		} catch (Exception e) {
			LOGGER.error("Error calculating statistics", e);
			return null;
		}
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
	public TreeSet<Double> getQuantileLimits() {

		try {
			getStatistics();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

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
		breaks = ASUtil.roundLimits(breaks, getClassValueDigits());

		return breaks;
	}

	public Double getSD() {
		try {
			return getStatistics().standardDeviation();
		} catch (Exception e) {
			LOGGER.error("Error calculating statistics", e);
			return null;
		}
	}

	public boolean isQuite() {
		return quite;
	}

	/**
	 * Determine if you want the classification to be recalculated whenever if
	 * makes sense automatically. Events for START calculation and
	 * NEW_STATS_AVAIL will be fired.
	 */
	public boolean isRecalcAutomatically() {
		return recalcAutomatically;
	}

	/**
	 * Remove a QUITE-State from the event firing state stack
	 */
	public void popQuite() {
		setQuite(stackQuites.pop());
		if (isQuite() == false) {
			if (lastOpressedEvent != null)
				fireEvent(lastOpressedEvent);
			// Not anymore.. if lastOpressedEvent == null, there is no reason to
			// send an event now
			// else
			// fireEvents(new RuleChangedEvent("Not quite anymore", this));
		} else {
			LOGGER.debug("not firing event because there are "
					+ stackQuites.size() + " 'quites' still on the stack");
		}

	}

	public void popQuite(ClassificationChangeEvent changedEvent) {
		setQuite(stackQuites.pop());
		if (isQuite() == false)
			fireEvent(changedEvent);
		else {
			lastOpressedEvent = changedEvent;
			LOGGER.debug("not firing event " + changedEvent
					+ " because there are " + stackQuites.size()
					+ " 'quites' still on the stack");
		}
	}

	/**
	 * Add a QUITE-State to the event firing state stack
	 */
	public void pushQuite() {
		stackQuites.push(isQuite());
		setQuite(true);
	}

	public void removeListener(ClassificationChangedListener l) {
		listeners.remove(l);
	}

	/**
	 * Rplaces all values in the classification limits that can make problems
	 * when exported to XML. Geoserver 2.0.1 is not compatible with -Inf, Inf,
	 * Na
	 * 
	 * @param replacement
	 *            A number that will replace Inf+ as abs(replacement), and Inf-
	 *            as -abs(replacement)
	 */
	final public TreeSet<Double> removeNanAndInf(Double replacement) {

		// Filter class limits against -Inf and +Inf. GS doesn't like them
		TreeSet<Double> newClassLimits = new TreeSet<Double>();
		for (Double l : getClassLimits()) {
			if (l == Double.NEGATIVE_INFINITY)
				l = -1. * Math.abs(replacement);
			if (l == Double.POSITIVE_INFINITY)
				l = Math.abs(replacement);
			if (l == Double.NaN)
				l = 0.;
			newClassLimits.add(l);
		}
		setClassLimits(newClassLimits);
		return newClassLimits;
	}

	final public void setClassLimits(final TreeSet<Double> classLimits_) {

		// if (classLimits_.size() == 1) {
		// // Special case: Create a second classLimit with the same value!
		// classLimits_.add(classLimits_.first());
		// }

		this.breaks = classLimits_;
		this.numClasses = classLimits_.size() - 1;

		fireEvent(new ClassificationChangeEvent(CHANGETYPES.CLASSES_CHG));
	}

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
	final public void setLimitsDigits(final Integer digits) {
		this.limitsDigits = digits;
	}

	final public void setMethod(final CLASSIFICATION_METHOD newMethod) {
		if ((method != null) && (method != newMethod)) {
			method = newMethod;

			fireEvent(new ClassificationChangeEvent(CHANGETYPES.METHODS_CHG));
		}
	}

	final public void setNumClasses(Integer numClasses2) {
		if (numClasses2 != null && !numClasses2.equals(numClasses)) {
			numClasses = numClasses2;
			// LOGGER.debug("QuanClassification set NumClasses to " +
			// numClasses2
			// + " and fires event");
			fireEvent(new ClassificationChangeEvent(CHANGETYPES.NUM_CLASSES_CHG));
		}
	}

	final public void setQuite(boolean quite) {
		this.quite = quite;
	}

	/**
	 * Determine if you want the classification to be recalculated whenever if
	 * makes sense automatically. Events for START calculation and
	 * NEW_STATS_AVAIL will be fired if set to <code>true</code> Default is
	 * <code>true</code>. Switching it to false can be usefull for tests. If set
	 * to <false> you have to call {@link #calculateClassLimitsBlocking()} to
	 * update the statistics.
	 */
	final public void setRecalcAutomatically(final boolean b) {
		recalcAutomatically = b;
	}

	/**
	 * 
	 */
	public void calculateClassLimits() {
		pushQuite();
		TreeSet<Double> newLimits;
		try {
			newLimits = calculateClassLimitsBlocking();
			setClassLimits(newLimits);
			popQuite();
		} catch (InterruptedException e) {
			setQuite(stackQuites.pop());
		} catch (IOException exception) {
			setQuite(stackQuites.pop());
		} finally {
		}
	}

	public void setSubsampling(int subsampling) {
		this.subsampling = subsampling;
	}

	public int getSubsampling() {
		return subsampling;
	}
}
