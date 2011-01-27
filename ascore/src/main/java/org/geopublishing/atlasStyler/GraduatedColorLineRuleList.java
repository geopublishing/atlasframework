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
package org.geopublishing.atlasStyler;

import org.apache.log4j.Logger;
import org.geotools.styling.FeatureTypeStyle;

import de.schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import de.schmitzm.geotools.styling.StyledFeaturesInterface;
import de.schmitzm.lang.LangUtil;

public class GraduatedColorLineRuleList extends GraduatedColorRuleList {
	protected Logger LOGGER = LangUtil.createLogger(this);

	public GraduatedColorLineRuleList(StyledFeaturesInterface<?> styledFeatures) {
		super(styledFeatures, GeometryForm.LINE);
	}

	@Override
	public RulesListType getType() {
		return RulesListType.QUANTITIES_COLORIZED_LINE;
	}

	@Override
	public void importTemplate(FeatureTypeStyle importFTS) {
		setTemplate(ASUtil.importLineTemplateFromFirstRule(importFTS));

	}
}
