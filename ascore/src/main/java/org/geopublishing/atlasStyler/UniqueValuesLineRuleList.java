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

import schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import schmitzm.lang.LangUtil;
import skrueger.geotools.StyledFeaturesInterface;

public class UniqueValuesLineRuleList extends UniqueValuesRuleList {
	
	protected Logger LOGGER = LangUtil.createLogger(this);

	public UniqueValuesLineRuleList(StyledFeaturesInterface<?> styledFeatures) {
		super(styledFeatures,GeometryForm.LINE);
	}

//
//	@SuppressWarnings("unchecked")
//	@Override
//	public SingleRuleList getDefaultTemplate() {
//		return ASUtil.getDefaultLineTemplate();
//	}

	@Override
	public RulesListType getType() {
		return RulesListType.UNIQUE_VALUE_LINE;
	}

	@Override
	public void importTemplate(FeatureTypeStyle importFTS) {
		setTemplate(ASUtil.importLineTemplateFromFirstRule(importFTS));

	}
}
