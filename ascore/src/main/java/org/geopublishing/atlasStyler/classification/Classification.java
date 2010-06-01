/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasStyler.classification;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.RuleChangedEvent;


public abstract class Classification {
//	protected Logger LOGGER = ASUtil.createLogger(this);
	protected Logger LOGGER = Logger.getLogger(Classification.class);

	private Set<ClassificationChangedListener> listeners = new HashSet<ClassificationChangedListener>();
	
	/** Counts the number of NODATA values found and excluded from the classification **/
	long noDataValuesCount = 0;
	
	/** Counts the number of NODATA values found and excluded from the classification **/
	public long getNoDataValuesCount() {
		return noDataValuesCount;
	}
	
	/** resets the number of NODATA values found and excluded from the classification **/
	public void resetNoDataCount() {
		noDataValuesCount = 0;
	}
	
	/** Adds one to the number of NODATA values found and excluded from the classification **/
	public void increaseNoDataValue() {
		noDataValuesCount++;
	}

	/**
	 * @return Returns the number of classes
	 */
	public abstract int getNumClasses();

	public void addListener(ClassificationChangedListener l) {
		listeners.add(l);
	}

	public void removeListener(ClassificationChangedListener l) {
		listeners.remove(l);
	}
	
	/**
	 * If {@link #quite} == <code>true</code> no {@link RuleChangedEvent} will
	 * be fired.
	 */
	private boolean quite = false;

	ClassificationChangeEvent lastOpressedEvent = null;
	protected Stack<Boolean> stackQuites = new Stack<Boolean>();

	/**
	 * Add a QUITE-State to the event firing state stack
	 */
	public void pushQuite() {
		stackQuites.push(isQuite());
		setQuite(true);
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
	 * Fires the given {@link ClassificationChangeEvent} to all listeners.
	 */
	public void fireEvent(final ClassificationChangeEvent evt) {
		
		if (quite) {
			lastOpressedEvent = evt;
			return;
		} else {
			lastOpressedEvent = null;
		}
		
		if (evt == null) return;
		
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
			case EXCLUDES_FILETER_CHG:
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

	public void setQuite(boolean quite) {
		this.quite = quite;
	}

	public boolean isQuite() {
		return quite;
	}
	
	public void dispose() {
		listeners.clear();
	}

}
