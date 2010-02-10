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
