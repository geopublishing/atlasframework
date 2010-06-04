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
package org.geopublishing.atlasStyler.classification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.geopublishing.atlasStyler.AtlasStylerTest;
import org.geopublishing.atlasStyler.classification.QuantitiesClassification.METHOD;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory2;

import schmitzm.lang.LangUtil;
import skrueger.geotools.StyledFS;

public class QuantitiesClassificationTest {

	FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);

	private static FeatureSource<SimpleFeatureType, SimpleFeature> featureSource_polygon;

	@BeforeClass
	public static void setup() throws IOException {
		URL shpURL = AtlasStylerTest.class
				.getResource(AtlasStylerTest.COUNTRY_SHP_RESNAME);
		assertNotNull(AtlasStylerTest.COUNTRY_SHP_RESNAME + " not found!",
				shpURL);

		Map<String, Object> params = new HashMap<String, Object>();
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
	public void testQuantile() throws IOException, InterruptedException {

		final QuantitiesClassification clfcn = new QuantitiesClassification(
				new StyledFS(featureSource_polygon), "POP_CNTRY");
		clfcn.setRecalcAutomatically(false);
		clfcn.setMethod(METHOD.QUANTILES);
		clfcn.setNumClasses(10);
		clfcn.calculateClassLimitsBlocking();

		assertTrue(testBreaks(clfcn.getClassLimits(), -99999.0, 6782.0,
				62920.0, 260627.0, 1085777.0, 3084641.0, 5245515.0, 9951515.0,
				1.782752E7, 4.309962E7, 1.281008318E9));
	}

	@Test
	public void testQuantileNormalized() throws IOException,
			InterruptedException {

		final QuantitiesClassification clfcn = new QuantitiesClassification(
				new StyledFS(featureSource_polygon), "POP_CNTRY", "SQKM_CNTRY");
		clfcn.setRecalcAutomatically(false);
		clfcn.setMethod(METHOD.QUANTILES);
		clfcn.setNumClasses(5);
		clfcn.calculateClassLimitsBlocking();

		assertTrue(testBreaks(clfcn.getClassLimits(), -219.66456884905597,
				11.128059688187763, 39.738285449459624, 87.66258335206413,
				220.55727207025177, 30126.841370755155));

	}

	@Test
	public void testQuantileNormalizedFiltered() throws IOException,
			InterruptedException, CQLException {

		StyledFS styledFeaturesFiltered = new StyledFS(featureSource_polygon);
		styledFeaturesFiltered.setFilter(ECQL
				.toFilter("POP_CNTRY > 0 and POP_CNTRY < 500000"));
		final QuantitiesClassification clfcn = new QuantitiesClassification(
				styledFeaturesFiltered);

		clfcn.setRecalcAutomatically(false);

		clfcn.setValue_field_name("POP_CNTRY");
		clfcn.setNormalizer_field_name("SQKM_CNTRY");

		clfcn.setNumClasses(4);

		clfcn.setMethod(METHOD.QUANTILES);

		clfcn.calculateClassLimitsBlocking();

		assertTrue(testBreaks(clfcn.getClassLimits(), 0.025861767213758966,
				28.29196367998521, 109.31626340104614, 259.54752934208244,
				30126.841370755155));

	}

	@Test
	public void testQEqualInterval() throws IOException, InterruptedException {

		QuantitiesClassification clfcn = new QuantitiesClassification(
				new StyledFS(featureSource_polygon), "POP_CNTRY");
		clfcn.setRecalcAutomatically(false);
		clfcn.setMethod(METHOD.EI);
		clfcn.setNumClasses(4);
		clfcn.calculateClassLimitsBlocking();
		assertTrue(testBreaks(clfcn.getClassLimits(), -99999.0, 3.2017708025E8,
				6.404541595E8, 9.6073123875E8, 1.281008318E9));
		assertEquals("class widthds are not equal",
				3.2017708025E8 - (-99999.0), 6.404541595E8 - 3.2017708025E8,
				0.00000001);
	}

	@Test
	public void testNoDataValueviaAMD() throws IOException,
			InterruptedException {

		QuantitiesClassification clfcn = new QuantitiesClassification(
				new StyledFS(featureSource_polygon), "SQKM_CNTRY");

		clfcn.getStyledFeatures().getAttributeMetaDataMap().get("SQKM_CNTRY")
				.addNodataValue(0.0);

		clfcn.setRecalcAutomatically(false);
		clfcn.setMethod(METHOD.EI);
		clfcn.setNumClasses(4);
		clfcn.calculateClassLimitsBlocking();
		assertTrue(testBreaks(clfcn.getClassLimits(), 1.668, 4212986.250999999,
				8425970.833999999, 1.2638955416999998E7, 1.685194E7));
	}

	@Test
	public void testQEqualIntervalNormalizedFilteredAndBlockingClalculations()
			throws IOException, InterruptedException {
		// Filter exclude = ff.equals(ff.property("LANDLOCKED"),
		// ff.literal("N"));
		QuantitiesClassification clfcn = new QuantitiesClassification(
				new StyledFS(featureSource_polygon), "POP_CNTRY", null);
		clfcn.setRecalcAutomatically(false);

		clfcn.setMethod(METHOD.EI);

		clfcn.setNumClasses(4);

		clfcn.calculateClassLimitsBlocking();

		assertTrue(testBreaks(clfcn.getClassLimits(), -99999.0, 3.2017708025E8,
				6.404541595E8, 9.6073123875E8, 1.281008318E9));
	}

	@Test
	public void testChangeOfValueFieldNameAndReusingTheObject()
			throws IOException, InterruptedException, CQLException {

		final QuantitiesClassification clfcn = new QuantitiesClassification(
				new StyledFS(featureSource_polygon), "SQMI_CNTRY");
		clfcn.setRecalcAutomatically(false);
		clfcn.setMethod(METHOD.QUANTILES);
		clfcn.setNumClasses(3);
		clfcn.calculateClassLimitsBlocking();

		assertTrue(testBreaks(clfcn.getClassLimits(), 0.644,
				3692.8633333333373, 86534.47100000014, 6506534.0));

		/**
		 * Change ValueFieldName to POP,
		 */
		clfcn.setValue_field_name("POP_CNTRY");
		clfcn.setMethod(METHOD.QUANTILES);
		clfcn.setNumClasses(10);
		clfcn.calculateClassLimitsBlocking();
		assertTrue(testBreaks(clfcn.getClassLimits(), -99999.0, 6782.0,
				62920.0, 260627.0, 1085777.0, 3084641.0, 5245515.0, 9951515.0,
				1.782752E7, 4.309962E7, 1.281008318E9));

		/**
		 * Normalize POP with SQKM and add a filter.
		 */

		clfcn.getStyledFeatures().setFilter(
				ECQL.toFilter("SQKM_CNTRY > 0 and POP_CNTRY > 0"));
		clfcn.setNormalizer_field_name("SQKM_CNTRY");
		clfcn.setNumClasses(4);
		clfcn.calculateClassLimitsBlocking();

		assertTrue(testBreaks(clfcn.getClassLimits(), 0.025861767213758966,
				22.20006623432529, 69.62649327083018, 157.5165469904705,
				30126.841370755155));

		/**
		 * Finally reset NORMALIZER to null. Only POP is classified.
		 */

		clfcn.setNormalizer_field_name(null);

		clfcn.setMethod(METHOD.EI);

		clfcn.calculateClassLimitsBlocking();

		assertTrue(testBreaks(clfcn.getClassLimits(), 56.0, 3.202521215E8,
				6.40504187E8, 9.607562525E8, 1.281008318E9));
	}

	/**
	 * Utility method to easitly check the breaks
	 */
	private boolean testBreaks(TreeSet<Double> classLimits, Double... expected) {
		Double[] actual = classLimits.toArray(new Double[] {});
		for (int i = 0; i < expected.length; i++) {

			if (actual[i] > expected[i] + 0.00000001
					|| actual[i] < expected[i] - 0.00000001) {
				String msg = (i + 1) + "th class limit not as expected:"
						+ expected[i] + "   vs.  " + actual[i];
				System.err.println(msg);
				System.out.println("expected = "
						+ LangUtil.stringConcatWithSep(" , ", expected));
				System.out.println("actual   = "
						+ LangUtil.stringConcatWithSep(" , ", actual));
				return false;
			}

		}
		return true;
	}

}
