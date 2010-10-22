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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.styling.Symbolizer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.styling.StylingUtil;
import skrueger.geotools.StyledFS;

public class AtlasStylerTest {
	public final static String COUNTRY_SHP_RESNAME = "/data/shp countries/country.shp";
	private static FeatureSource<SimpleFeatureType, SimpleFeature> featureSource_polygon;

	/**
	 * A tiny Shapefile with an .sld that contains a QuantilesClassification
	 * with manually adapted colors
	 */
	public final static String SNOWPOLYGON_RESNAME = "/data/polygonSnowShape/polygonLayerSnow.shp";

	@AfterClass
	public static void after() {
		featureSource_polygon.getDataStore().dispose();
	}

	@BeforeClass
	public static void setup() throws IOException {
		URL shpURL = AtlasStylerTest.class.getResource(COUNTRY_SHP_RESNAME);
		assertNotNull(COUNTRY_SHP_RESNAME + " not found!", shpURL);
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("url", shpURL);
		DataStore dataStore = DataStoreFinder.getDataStore(params);
		featureSource_polygon = dataStore.getFeatureSource(dataStore
				.getTypeNames()[0]);
	}

	@Test
	@Ignore
	public void testAvgNN() throws IOException {
		double calcAvgNN = FeatureUtil.calcAvgNN(new StyledFS(
				featureSource_polygon));
		assertTrue("Der average NN Wert sollte größer 0 sein", calcAvgNN > 0);
	}

	@Test
	public void testConstructors() {
		AtlasStyler as1 = new AtlasStyler(featureSource_polygon);
		AtlasStyler as2 = new AtlasStyler(new StyledFS(featureSource_polygon));
		AtlasStyler as3 = new AtlasStyler(new StyledFS(featureSource_polygon),
				null, null, null);
	}

	@Test
	public void testCreateAndStoreAndLoad_UniqueValuesRL() throws IOException,
			TransformerException {
		StyledFS styledFeatures = new StyledFS(featureSource_polygon);
		AtlasStyler as = new AtlasStyler(styledFeatures);

		String propName = featureSource_polygon.getSchema()
				.getAttributeDescriptors().get(1).getLocalName();

		assertEquals("FIPS_CNTRY", propName);
		final UniqueValuesPolygonRuleList uniqueRL1 = as
				.getUniqueValuesPolygonRuleList();

		uniqueRL1.setPropertyFieldName(propName, true);

		uniqueRL1.setWithDefaultSymbol(true);
		Set<Object> allNew = uniqueRL1
				.getAllUniqueValuesThatAreNotYetIncluded(null);

		int numFeatures = 251;

		assertEquals(numFeatures, allNew.size());

		for (Object s : allNew) {
			uniqueRL1.addUniqueValue(s);
		}

		assertEquals(numFeatures + 1, uniqueRL1.getValues().size());
		assertEquals(numFeatures + 1, uniqueRL1.getLabels().size());
		assertEquals(numFeatures + 1, uniqueRL1.getSymbols().size());

		// Now disable the Default Symbols and expect one rule less
		uniqueRL1.setWithDefaultSymbol(false);
		assertEquals(numFeatures, uniqueRL1.getValues().size());
		assertEquals(numFeatures, uniqueRL1.getLabels().size());
		assertEquals(numFeatures, uniqueRL1.getSymbols().size());

		org.geotools.styling.Style style1 = as.getStyle();

		as = null;
		// Create a new AtlasStyler
		AtlasStyler as2 = new AtlasStyler(styledFeatures, style1, null, null);
		UniqueValuesPolygonRuleList uniqueRL2 = as2
				.getUniqueValuesPolygonRuleList();

		assertEquals(uniqueRL1.isWithDefaultSymbol(),
				uniqueRL2.isWithDefaultSymbol());
		assertEquals(uniqueRL1.getValues().size(), uniqueRL2.getValues().size());
		assertEquals(uniqueRL1.getValues(), uniqueRL2.getValues());

		assertEquals(uniqueRL1.getLabels().size(), uniqueRL2.getLabels().size());
		assertEquals(uniqueRL1.getLabels(), uniqueRL2.getLabels());

		List<SingleRuleList<? extends Symbolizer>> ss1 = uniqueRL1.getSymbols();
		List<SingleRuleList<? extends Symbolizer>> ss2 = uniqueRL2.getSymbols();
		assertEquals(uniqueRL1.getSymbols().size(), ss2.size());

		for (int i = 0; i < uniqueRL1.getSymbols().size(); i++) {
			SingleRuleList<? extends Symbolizer> s1 = ss1.get(i);
			SingleRuleList<? extends Symbolizer> s2 = ss2.get(i);
			String xml1 = StylingUtil.toXMLString(s1.getFTS());
			String xml2 = StylingUtil.toXMLString(s2.getFTS());
			assertEquals(xml1, xml2);
		}
	}

	@Test
	public void testGetNumericalFieldNames() {
		Collection<String> numericalFieldNames = FeatureUtil
				.getNumericalFieldNames(featureSource_polygon.getSchema(),
						false);
		System.out.println(numericalFieldNames);

		String[] strings = numericalFieldNames.toArray(new String[] {});
		assertEquals(3, strings.length);
		assertEquals("POP_CNTRY", strings[0]);
		assertEquals("SQKM_CNTRY", strings[1]);
		assertEquals("SQMI_CNTRY", strings[2]);
	}


}
