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

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory2;

import skrueger.geotools.StyledFS;
import skrueger.sld.classification.QuantitiesClassification;
import skrueger.sld.classification.QuantitiesClassification.METHOD;

public class QuantClassificationTest extends TestCase{

	FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);

	private static FeatureSource<SimpleFeatureType, SimpleFeature> featureSource_polygon;

	@Before
	public void setup() throws IOException {
		URL shpURL = AtlasStylerTest.class.getClassLoader().getResource(
				"data/shp countries/country.shp");
		assertNotNull(shpURL);
		Map params = new HashMap();
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

		TreeSet breaks = clfcn.getClassLimits();

		System.out.println(breaks.toString());

		TreeSet<Double> ts = new TreeSet<Double>();
		Collections.addAll(ts, new Double[] { -99999.0, 6782.0, 62920.0,
				260627.0, 1085777.0, 3084641.0, 5245515.0, 9951515.0,
				1.782752E7, 4.309962E7, 1.281008318E9 });
		assertEquals(ts, breaks);
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
		TreeSet breaks = clfcn.getClassLimits();

		System.out.println(breaks.toString());

		TreeSet<Double> ts = new TreeSet<Double>();
		Collections.addAll(ts, new Double[] { -219.66456884905597,
				11.128059688187763, 39.738285449459624, 87.66258335206413,
				220.55727207025177, 30126.841370755155 });
		assertEquals(ts, breaks);

	}

	@Test
	public void testQuantileFiltered() throws IOException, InterruptedException {

		// Filter exclude = ff.less(ff.property("POP_CNTRY"), ff.literal(100));

		final QuantitiesClassification clfcn = new QuantitiesClassification(
				new StyledFS(featureSource_polygon), "SQKM_CNTRY", null);
		clfcn.setRecalcAutomatically(false);
		clfcn.setMethod(METHOD.QUANTILES);
		clfcn.setNumClasses(4);
		clfcn.calculateClassLimitsBlocking();
		TreeSet breaks = clfcn.getClassLimits();

		System.out.println(breaks.toString());

		TreeSet<Double> ts = new TreeSet<Double>();
		Collections.addAll(ts, new Double[] { 7.266, 2580.6890000000003,
				73614.2695, 397767.9295, 1.685194E7 });
		assertEquals(ts, breaks);

	}

	@Test
	public void testQuantileNormalizedFiltered() throws IOException,
			InterruptedException {

		// Filter exclude = ff.less(ff.property("POP_CNTRY"), ff.literal(100));
		final QuantitiesClassification clfcn = new QuantitiesClassification(new StyledFS(featureSource_polygon));

		clfcn.setRecalcAutomatically(false);

		clfcn.setValue_field_name("POP_CNTRY");
		clfcn.setNormalizer_field_name("SQKM_CNTRY");

		clfcn.setNumClasses(4);

		clfcn.setMethod(METHOD.QUANTILES);

		clfcn.calculateClassLimitsBlocking();

		TreeSet breaks = clfcn.getClassLimits();

		System.out.println(breaks.toString());

		TreeSet<Double> ts = new TreeSet<Double>();
		Collections.addAll(ts, new Double[] { 0.025861767213758966,
				23.569531723266053, 70.13948425858133, 158.32320324580488,
				30126.841370755155 });

	}

	@Test
	public void testQEqualInterval() throws IOException, InterruptedException {

		QuantitiesClassification clfcn = new QuantitiesClassification(new StyledFS(featureSource_polygon), "POP_CNTRY");
		clfcn.setRecalcAutomatically(false);
		clfcn.setMethod(METHOD.EI);
		clfcn.setNumClasses(4);
		clfcn.calculateClassLimitsBlocking();
		TreeSet breaks = clfcn.getClassLimits();

		System.out.println(breaks.toString());

		TreeSet<Double> ts = new TreeSet<Double>();
		Collections.addAll(ts, new Double[] { 0.0, 3.202520795E8, 6.40504159E8,
				9.607562385E8, 1.281008318E9 });
		assertEquals(ts, breaks);
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

		TreeSet breaks = clfcn.getClassLimits();

		System.out.println(breaks.toString());

		TreeSet<Double> ts = new TreeSet<Double>();
		Collections.addAll(ts, new Double[] { 0.0, 1.32857425E7, 2.6571485E7,
				3.98572275E7, 5.314297E7 });
		assertEquals(ts, breaks);
	}

	@Test
	public void testChangeOfValueFieldNameAndReusingTheObject()
			throws IOException, InterruptedException {

		final QuantitiesClassification clfcn = new QuantitiesClassification(new StyledFS(featureSource_polygon), "SQMI_CNTRY");
		clfcn.setRecalcAutomatically(false);
		clfcn.setMethod(METHOD.QUANTILES);
		clfcn.setNumClasses(3);
		clfcn.calculateClassLimitsBlocking();
		TreeSet breaks = clfcn.getClassLimits();

		System.out.println(breaks.toString());

		TreeSet<Double> ts = new TreeSet<Double>();
		Collections.addAll(ts, new Double[] { 0.644, 47357.563134600554,
				403577.3362677895, 759797.1094009784, 1116016.8825341675,
				1472236.6556673564, 1828456.4288005454, 2184676.201933734,
				6506534.0 });
		assertEquals(ts, breaks);

		/**
		 * Change ValueFieldName to POP,
		 */
		clfcn.setValue_field_name("POP_CNTRY");
		clfcn.setMethod(METHOD.QUANTILES);
		clfcn.setNumClasses(10);
		clfcn.calculateClassLimitsBlocking();
		breaks = clfcn.getClassLimits();

		System.out.println(breaks.toString());

		ts = new TreeSet<Double>();
		Collections.addAll(ts, new Double[] { -99999.0, 6782.0, 62920.0,
				260627.0, 1085777.0, 3084641.0, 5245515.0, 9951515.0,
				1.782752E7, 4.309962E7, 1.281008318E9 });
		assertEquals(ts, breaks);

		/**
		 * Normalize POP with SQKM and add a filter.
		 */

		// Filter exclude = ff.less(ff.property("POP_CNTRY"), ff.literal(100));
		clfcn.setNormalizer_field_name("SQKM_CNTRY");
		clfcn.setNumClasses(4);
		clfcn.calculateClassLimitsBlocking();

		breaks = clfcn.getClassLimits();

		System.out.println(breaks.toString());

		ts = new TreeSet<Double>();
		Collections.addAll(ts, new Double[] { 0.025861767213758966,
				23.569531723266053, 70.13948425858133, 158.32320324580488,
				30126.841370755155 });
		assertEquals(ts, breaks);

		/**
		 * Finally reset NORMALIZER and EXCLUDE FILTER to null. Only POP is
		 * classified. Automatic Calc is activated again.
		 */

		clfcn.setNormalizer_field_name(null);

		clfcn.setMethod(METHOD.EI);

		clfcn.calculateClassLimitsBlocking();

		breaks = clfcn.getClassLimits();

		System.out.println(breaks.toString());

		ts = new TreeSet<Double>();
		Collections.addAll(ts, new Double[] { 0.0, 3.202520795E8, 6.40504159E8,
				9.607562385E8, 1.281008318E9 });
		assertEquals(ts, breaks);
	}

}
