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

import org.apache.log4j.Logger;

public class ClassificationChangeEvent {
	final static private Logger LOGGER = Logger
			.getLogger(ClassificationChangeEvent.class);

	private final CHANGETYPES type;

	public enum CHANGETYPES {
		METHODS_CHG, VALUE_CHG, NORM_CHG, EXCLUDES_FILETER_CHG, NUM_CLASSES_CHG, START_NEW_STAT_CALCULATION, // Expensive
		// calculation
		// of
		// statistics
		// started
		CLASSES_CHG
		// New CLasLimits have been calculated
	}

	public ClassificationChangeEvent(CHANGETYPES type) {
		this.type = type;
	}

	public CHANGETYPES getType() {
		return type;
	}

}
