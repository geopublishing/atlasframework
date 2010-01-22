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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import skrueger.geotools.StyledFS;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class AtlasStylerTest {

	private static FeatureSource<SimpleFeatureType, SimpleFeature> featureSource_polygon;

	@Before
	public void setup() throws IOException {
		URL shpURL = AtlasStylerTest.class.getClassLoader().getResource(
				"data/shp countries/country.shp");
		assertNotNull(shpURL);
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("url", shpURL);
		DataStore dataStore = DataStoreFinder.getDataStore(params);
		featureSource_polygon = dataStore.getFeatureSource(dataStore
				.getTypeNames()[0]);
	}

	@After
	public void after() {
		featureSource_polygon.getDataStore().dispose();
	}

	@Test
	public void testAvgNN() {
		double calcAvgNN = FeatureUtil.calcAvgNN(new StyledFS(featureSource_polygon) );
	}

	@Test
	public void testGetNumericalFieldNames() {
		// GraduatedColorRuleList grColors = new GraduatedColorRuleList(
		// featureSource_polygon);
		Collection<String> numericalFieldNames = ASUtil
				.getNumericalFieldNames(featureSource_polygon.getSchema(),false);
		System.out.println(numericalFieldNames);

		String[] strings = numericalFieldNames.toArray(new String[] {});
		assertEquals(3, strings.length);
		assertEquals("POP_CNTRY", strings[0]);
		assertEquals("SQKM_CNTRY", strings[1]);
		assertEquals("SQMI_CNTRY", strings[2]);
	}
	
	@Test
	public void testFeatureTypeChecks() throws Exception {

		GeometryDescriptor defaultGeometry = FeatureUtil.createFeatureType(
				Polygon.class).getGeometryDescriptor();
		assertTrue(FeatureUtil.getGeometryForm(defaultGeometry) == GeometryForm.POLYGON);
		assertFalse(FeatureUtil.getGeometryForm(defaultGeometry) == GeometryForm.LINE);
		assertFalse(FeatureUtil.getGeometryForm(defaultGeometry) == GeometryForm.POINT);

		defaultGeometry = FeatureUtil.createFeatureType(Point.class)
				.getGeometryDescriptor();
		assertTrue(FeatureUtil.getGeometryForm(defaultGeometry) == GeometryForm.POINT);
		assertFalse(FeatureUtil.getGeometryForm(defaultGeometry) == GeometryForm.POLYGON);
		assertFalse(FeatureUtil.getGeometryForm(defaultGeometry) == GeometryForm.LINE);
	}

}
