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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Style;
import org.junit.Test;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.styling.StylingUtil;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class RuleListTest {
	protected org.apache.log4j.Logger LOGGER = ASUtil.createLogger(this);

	@Test
	public void testSaveToStyle() throws IOException, TransformerException {

		Style style = FeatureUtil.createDefaultStyle(FeatureUtil
				.createFeatureType(Point.class).getGeometryDescriptor());
		File tempF = File.createTempFile("sld", "junit");
		System.out.println(tempF);
		StylingUtil.saveStyleToSLD(style, tempF);
	}

	@Test
	public void testClone_SinglePointSymbolRuleList() throws IOException,
			TransformerException, CloneNotSupportedException {

		// Create Symbolizer
		Style style = FeatureUtil.createDefaultStyle(FeatureUtil
				.createFeatureType(Point.class).getGeometryDescriptor());
		PointSymbolizer symb = (PointSymbolizer) style.featureTypeStyles().get(0)
				.rules().get(0).symbolizers().get(0);
		assertNotNull(symb);

		// Add it to RuleList
		SinglePointSymbolRuleList list = new SinglePointSymbolRuleList("");
		list.addSymbolizer(symb);

		// Clone
		SinglePointSymbolRuleList clonedList = list.clone(false);

		PointSymbolizer psCloned = clonedList.getSymbolizers().get(0);
		assertNotNull(psCloned);

		PointSymbolizer ps = list.getSymbolizers().get(0);
		assertNotNull(ps);

		assertNotSame(ps, psCloned);
	}

	@Test
	public void testClone_SinglePolygonSymbolRuleList() throws IOException,
			TransformerException, CloneNotSupportedException {

		// Create Symbolizer
		Style style = FeatureUtil.createDefaultStyle(FeatureUtil
				.createFeatureType(Polygon.class).getGeometryDescriptor());
		PolygonSymbolizer symb = (PolygonSymbolizer) style.featureTypeStyles()
				.get(0).rules().get(0).getSymbolizers()[0];
		assertNotNull(symb);

		// Add it to RuleList
		SinglePolygonSymbolRuleList list = new SinglePolygonSymbolRuleList("");
		list.addSymbolizer(symb);

		// Clone
		SinglePolygonSymbolRuleList clonedList = list.clone(false);

		PolygonSymbolizer psCloned = clonedList.getSymbolizers().get(0);
		assertNotNull(psCloned);

		PolygonSymbolizer ps = list.getSymbolizers().get(0);
		assertNotNull(ps);

		assertNotSame(ps, psCloned);
	}
}
