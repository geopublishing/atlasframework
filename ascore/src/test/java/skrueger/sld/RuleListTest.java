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
package skrueger.sld;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.SinglePointSymbolRuleList;
import org.geopublishing.atlasStyler.SinglePolygonSymbolRuleList;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Style;
import org.junit.Test;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.styling.StylingUtil;
import schmitzm.junit.TestingClass;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
public class RuleListTest extends TestingClass {
	protected org.apache.log4j.Logger LOGGER = ASUtil.createLogger(this);

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

	@Test
	public void testSaveToStyle() throws IOException, TransformerException {

		Style style = FeatureUtil.createDefaultStyle(FeatureUtil
				.createFeatureType(Point.class).getGeometryDescriptor());
		File tempF = File.createTempFile("sld", "junit");
		System.out.println(tempF);
		StylingUtil.saveStyleToSld(style, tempF);
	}
}
