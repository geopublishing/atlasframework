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
package org.geopublishing.atlasStyler.rulesLists;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geotools.styling.FeatureTypeStyle;

import de.schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import de.schmitzm.geotools.styling.StyledFeaturesInterface;
import de.schmitzm.lang.LangUtil;

public class GraduatedColorPointRuleList extends GraduatedColorRuleList {
	protected Logger LOGGER = LangUtil.createLogger(this);

	public GraduatedColorPointRuleList(StyledFeaturesInterface<?> styledFeatures) {
		super(RulesListType.QUANTITIES_COLORIZED_POINT, styledFeatures,GeometryForm.POINT);
	}

	@Override
	public void importTemplate(FeatureTypeStyle importFTS) {
		setTemplate(ASUtil.importPointTemplateFromFirstRule(importFTS));
	}

}
