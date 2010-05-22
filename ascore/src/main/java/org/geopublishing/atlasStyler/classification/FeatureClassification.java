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

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geotools.feature.FeatureCollection;

import skrueger.geotools.StyledFeaturesInterface;

public abstract class FeatureClassification extends Classification {
	static public final int MAX_NUMBER_OF_COLORS_ALLOWED = 11;

	protected Logger LOGGER = ASUtil.createLogger(this);

	private StyledFeaturesInterface<?> styledFeatures;
	
	/** Remember to apply the associated Filter whenever you access the {@link FeatureCollection} **/
	public StyledFeaturesInterface<?> getStyledFeatures() {
		return styledFeatures;
	}
	
	public FeatureClassification(
			StyledFeaturesInterface<?> styledFeatures) {
				this.setStyledFeatures(styledFeatures);
	}

	public void setStyledFeatures(StyledFeaturesInterface<?> styledFeatures) {
		this.styledFeatures = styledFeatures;
	}

}
