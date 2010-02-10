/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package skrueger.sld.classification;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import skrueger.sld.ASUtil;

public abstract class Classification {
	protected Logger LOGGER = ASUtil.createLogger(this);

	private Set<ClassificationChangedListener> listeners = new HashSet<ClassificationChangedListener>();

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
	 * Fires the given {@link ClassificationChangeEvent} to all listeners.
	 */
	public void fireEvent(final ClassificationChangeEvent e) {

		LOGGER.debug("Classification fires event: " + e.getType());

		// SwingUtilities.invokeLater(new Runnable() {
		// public void run() {

		for (ClassificationChangedListener l : listeners) {
			switch (e.getType()) {
			case NORM_CHG:
				l.classifierNormalizationChanged(e);
				break;
			case VALUE_CHG:
				l.classifierValueFieldChanged(e);
				break;
			case METHODS_CHG:
				l.classifierMethodChanged(e);
				break;
			case EXCLUDES_FILETER_CHG:
				l.classifierExcludeFilterChanged(e);
				break;
			case NUM_CLASSES_CHG:
				l.classifierNumClassesChanged(e);
				break;
			case START_NEW_STAT_CALCULATION:
				l.classifierCalculatingStatistics(e);
				break;
			case CLASSES_CHG:
				l.classifierAvailableNewClasses(e);
				break;

			}
		}
		// }
		// });

	}
}
