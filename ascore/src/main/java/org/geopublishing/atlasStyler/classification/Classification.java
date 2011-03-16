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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import javax.swing.ComboBoxModel;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geopublishing.atlasStyler.classification.ClassificationChangeEvent.CHANGETYPES;

public abstract class Classification {
	ClassificationChangeEvent lastOpressedEvent = null;

	public void setMethod(CLASSIFICATION_METHOD method) {
		this.method = method;
	}

	private CLASSIFICATION_METHOD method = CLASSIFICATION_METHOD.DEFAULT_METHOD;

	public CLASSIFICATION_METHOD getMethod() {
		return method;
	}

	public abstract ComboBoxModel getClassificationParameterComboBoxModel();

	private final Set<ClassificationChangedListener> listeners = new HashSet<ClassificationChangedListener>();

	// protected Logger LOGGER = LangUtil.createLogger(this);
	protected Logger LOGGER = Logger.getLogger(Classification.class);

	/**
	 * Counts the number of NODATA values found and excluded from the
	 * classification
	 **/
	long noDataValuesCount = 0;

	/**
	 * If {@link #quite} == <code>true</code> no {@link RuleChangedEvent} will
	 * be fired.
	 */
	private boolean quite = false;

	protected Stack<Boolean> stackQuites = new Stack<Boolean>();

	public void addListener(ClassificationChangedListener l) {
		listeners.add(l);
	}

	public void dispose() {
		listeners.clear();
	}

	/**
	 * Fires the given {@link ClassificationChangeEvent} to all listeners.
	 */
	public void fireEvent(final ClassificationChangeEvent evt) {

		if (quite) {
			lastOpressedEvent = evt;
			return;
		} else {
			lastOpressedEvent = null;
		}

		if (evt == null)
			return;

		LOGGER.debug("Classification fires event: " + evt.getType());

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
			case START_NEW_STAT_CALCULATION:
				l.classifierCalculatingStatistics(evt);
				break;
			case CLASSES_CHG:
				l.classifierAvailableNewClasses(evt);
				break;

			}
		}

	}

	/**
	 * Counts the number of NODATA values found and excluded from the
	 * classification
	 **/
	public long getNoDataValuesCount() {
		return noDataValuesCount;
	}

	/**
	 * @return Returns the number of classes
	 */
	public abstract int getNumClasses();

	/**
	 * Adds one to the number of NODATA values found and excluded from the
	 * classification
	 **/
	public void increaseNoDataValue() {
		noDataValuesCount++;
	}

	public boolean isQuite() {
		return quite;
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
	public TreeSet<Double> removeNanAndInf(Double replacement) {

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

	protected int numClasses = 5;

	protected boolean recalcAutomatically = true;

	public void setClassLimits(final TreeSet<Double> classLimits_) {

		// if (classLimits_.size() == 1) {
		// // Special case: Create a second classLimit with the same value!
		// classLimits_.add(classLimits_.first());
		// }

		this.breaks = classLimits_;
		this.numClasses = classLimits_.size() - 1;

		fireEvent(new ClassificationChangeEvent(CHANGETYPES.CLASSES_CHG));
	}

	protected volatile boolean cancelCalculation;

	/**
	 * If the classification contains 5 classes, then we have to save 5+1
	 * breaks.
	 */
	protected volatile TreeSet<Double> breaks = new TreeSet<Double>();

	/**
	 * resets the number of NODATA values found and excluded from the
	 * classification
	 **/
	public void resetNoDataCount() {
		noDataValuesCount = 0;
	}

	public void setQuite(boolean quite) {
		this.quite = quite;
	}

	public abstract void setNumClasses(Integer newNum);

	public abstract TreeSet<Double> getClassLimits();

	public abstract BufferedImage createHistogramImage(boolean showMean,
			boolean showSd, int histogramBins, String xAxisLabel)
			throws InterruptedException, IOException;

	abstract public Long getCount();

	abstract public Double getMean();

	abstract public Double getMedian();

	abstract public Double getMin();

	abstract public Double getSum();

	abstract public Double getSD();

	abstract public Double getMax();
}
