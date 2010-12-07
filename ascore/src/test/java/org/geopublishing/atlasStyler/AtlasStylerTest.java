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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.geotools.data.FeatureSource;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Style;
import org.geotools.styling.Symbolizer;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.styling.StylingUtil;
import schmitzm.junit.TestingClass;
import schmitzm.swing.TestingUtil;
import skrueger.geotools.StyledFS;
public class AtlasStylerTest extends TestingClass {

	private static FeatureSource<SimpleFeatureType, SimpleFeature> featureSource_polygon;

	@BeforeClass
	public static void setup() throws IOException {
		featureSource_polygon = TestingUtil.TestDatasetsVector.countryShp
				.getFeatureSource();
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

		assertNotNull(as1);

		AtlasStyler as2 = new AtlasStyler(new StyledFS(featureSource_polygon));
		assertNotNull(as2);

		AtlasStyler as3 = new AtlasStyler(new StyledFS(featureSource_polygon),
				null, null, null, false);
		assertNotNull(as3);
	}

	@Test
	public void testCreateAndStoreAndLoad_UniqueValuesRL() throws IOException,
			TransformerException {
		StyledFS styledFeatures = new StyledFS(featureSource_polygon);
		AtlasStyler as = new AtlasStyler(styledFeatures);

		String propName = featureSource_polygon.getSchema()
				.getAttributeDescriptors().get(1).getLocalName();

		assertEquals("FIPS_CNTRY", propName);
		final UniqueValuesPolygonRuleList uniqueRL1 = as.getRlf()
				.createUniqueValuesPolygonRulesList(true);
		as.addRulesList(uniqueRL1);

		uniqueRL1.addDefaultRule();
		uniqueRL1.setPropertyFieldName(propName, true);

		Set<Object> allNew = uniqueRL1
				.getAllUniqueValuesThatAreNotYetIncluded();

		int numFeatures = 251;

		assertEquals(numFeatures, allNew.size());

		for (Object s : allNew) {
			uniqueRL1.addUniqueValue(s);
		}

		assertEquals(numFeatures + 1, uniqueRL1.getValues().size());
		assertEquals(numFeatures + 1, uniqueRL1.getLabels().size());
		assertEquals(numFeatures + 1, uniqueRL1.getSymbols().size());

		// Now disable the Default Symbols and expect one rule less
		uniqueRL1.setDefaultRuleEnabled(false);
		assertEquals(numFeatures, uniqueRL1.getValues().size());
		assertEquals(numFeatures, uniqueRL1.getLabels().size());
		assertEquals(numFeatures, uniqueRL1.getSymbols().size());

		FeatureTypeStyle fts1 = uniqueRL1.getFTS();
		assertTrue(StylingUtil.validates(fts1));
		assertTrue(fts1.getName().startsWith("UNIQUE"));

		Style style1 = as.getStyle();

		as = null;
		// Create a new AtlasStyler
		AtlasStyler as2 = new AtlasStyler(styledFeatures, style1, null, null,
				null);
		assertEquals(1, as2.getRuleLists().size());
		UniqueValuesPolygonRuleList uniqueRL2 = (UniqueValuesPolygonRuleList) as2
				.getRuleLists().get(0);

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

		assertTrue(StylingUtil.validates(uniqueRL1.getFTS()));
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
	//
	// @Test
	// public void testAskToTransferTemplates() {
	// if (!TestingUtil.INTERACTIVE)
	// return;
	//
	// AtlasStyler as = new AtlasStyler(featureSource_polygon);
	//
	// as.setLastChangedRuleList(as.getSingleLineSymbolRulesList());
	// as.getUniqueValuesLineRulesList();
	//
	//
	// as.setLastChangedRuleList(as.getGraduatedColorLineRulesList());
	// as.getSingleLineSymbolRulesList();
	//
	// as.setLastChangedRuleList(as.getGraduatedColorPointRulesList());
	// as.getSinglePointSymbolRulesList();
	//
	// as.setLastChangedRuleList(as.getUniqueValuesLineRulesList());
	// as.getSinglePointSymbolRulesList();
	//
	// System.out.println("s");
	//
	// }

}
