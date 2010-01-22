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
package skrueger.sld;

import org.apache.log4j.Logger;
import org.geotools.styling.FeatureTypeStyle;

import skrueger.geotools.StyledFeaturesInterface;

public class UniqueValuesPolygonRuleList extends UniqueValuesRuleList {
	private Logger LOGGER = Logger.getLogger(UniqueValuesPolygonRuleList.class);

	public UniqueValuesPolygonRuleList(StyledFeaturesInterface<?> styledFeatures) {
		super(styledFeatures);
	}

	@Override
	public SingleRuleList getDefaultTemplate() {
		return ASUtil.getDefaultPolygonTemplate();
	}

	@Override
	public void importTemplate(FeatureTypeStyle importFTS) {
		setTemplate(ASUtil.importPolygonTemplateFromFirstRule(importFTS));
	}

	@Override
	public RulesListType getTypeID() {
		return RulesListType.UNIQUE_VALUE_POLYGON;
	}
}
