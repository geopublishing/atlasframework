package org.geopublishing.atlasStyler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.IOException;
import java.util.List;
import java.util.TreeSet;

import org.geopublishing.atlasStyler.AbstractRuleList.RulesListType;
import org.geopublishing.atlasStyler.classification.QuantitiesClassification;
import org.geopublishing.atlasStyler.classification.QuantitiesClassification.METHOD;
import org.geopublishing.atlasStyler.swing.AsTestingUtil;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import schmitzm.swing.TestingUtil;
import skrueger.geotools.StyledFeatureCollection;

import com.vividsolutions.jts.geom.Point;

public class QuantitiesRuleListTest {
	FeatureCollection<SimpleFeatureType, SimpleFeature> features;

	@Before
	public void beforeTests() {
		features = FeatureCollections.newCollection();
		SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
		tb.setName("Dummy");
		tb.add("the_geom", Point.class);
		tb.add("allsame", Integer.class);
		tb.add("random", Double.class);

		SimpleFeatureBuilder b = new SimpleFeatureBuilder(tb.buildFeatureType());

		for (int i = 0; i < 100; i++) {
			features.add(b.buildFeature(null,
					new Object[] { null, 111, Math.random() }));
		}
	}

	@Test
	public void testNormaleQuantiles() throws SchemaException, IOException,
			InterruptedException {
		StyledFeatureCollection sfc = new StyledFeatureCollection(features,
				"someId", "titel", (Style) null);

		GraduatedColorPointRuleList ruleList = new GraduatedColorPointRuleList(
				sfc);

		QuantitiesClassification quantitiesClassification = new QuantitiesClassification(
				sfc, "random");
		quantitiesClassification.setRecalcAutomatically(false);
		quantitiesClassification.classificationMethod = METHOD.QUANTILES;
		quantitiesClassification.setNumClasses(5);
		quantitiesClassification.calculateClassLimitsBlocking();

		TreeSet<Double> classLimits = quantitiesClassification.getClassLimits();

		assertEquals(6, classLimits.size());

		ruleList.setClassLimits(classLimits);
		List<Rule> rules = ruleList.getRules();
		// +1 NODATA rule
		assertEquals(5 + 1, rules.size());
		assertTrue(rules.get(0).getTitle().contains(" - "));

	}

	/**
	 * Creates an QuantileClassifier and runs it on data that just has the same
	 * values. So no classes can be calculated. Then we set it to the rulelist,
	 * and we expect the RL it to deal with it nicely.
	 */
	@Test
	public void testQuantilesWhereTheyCannotBeCreated() throws SchemaException,
			IOException, InterruptedException {
		StyledFeatureCollection sfc = new StyledFeatureCollection(features,
				"someId", "titel", (Style) null);

		GraduatedColorPointRuleList ruleList = new GraduatedColorPointRuleList(
				sfc);

		QuantitiesClassification quantitiesClassification = new QuantitiesClassification(
				sfc, "allsame");
		quantitiesClassification.setRecalcAutomatically(false);
		quantitiesClassification.classificationMethod = METHOD.QUANTILES;
		quantitiesClassification.setNumClasses(5);
		quantitiesClassification.calculateClassLimitsBlocking();
		TreeSet<Double> classLimits = quantitiesClassification.getClassLimits();

		assertEquals(1, classLimits.size());

		ruleList.setClassLimits(classLimits, true);
		List<Rule> rules = ruleList.getRules();
		// 1 NODATA rule and one rule that just fits the only value available
		assertEquals(1 + 1, rules.size());
		assertEquals("111.0", rules.get(0).getDescription().getTitle()
				.toString());
	}

	@Test
	public void testImportSld_14() throws IOException {
		AtlasStyler as = new AtlasStyler(
				TestingUtil.TestDatasets.arabicInHeader.getFeatureSource());

		as.importStyle(AsTestingUtil.TestSld.textRulesDefaultLocalizedPre16
				.getStyle());

		assertTrue(as.getLastChangedRuleList() instanceof QuantitiesRuleList);
		QuantitiesRuleList<Number> colorRl = (QuantitiesRuleList<Number>) as
				.getLastChangedRuleList();

		List<FeatureTypeStyle> featureTypeStyles = as.getStyle()
				.featureTypeStyles();

		assertEquals(2, featureTypeStyles.size());

		// Text rules imported:
		FeatureTypeStyle textFs = featureTypeStyles.get(1);
		List<Rule> textRs = textFs.rules();
		assertEquals(3, textRs.size());

		// Colors!
		FeatureTypeStyle colorsFs = featureTypeStyles.get(0);
		List<Rule> colorsRs = colorsFs.rules();
		assertEquals(7, colorsRs.size());

		assertEquals(0.0, colorRl.getClassLimits().first());
		assertEquals(35000., colorRl.getClassLimits().last());

		// CHeck colors
		assertEquals(new Color(17, 17, 17), colorRl.getColors()[0]);
		assertEquals(new Color(252, 141, 89), colorRl.getColors()[1]);
		assertEquals(new Color(254, 224, 139), colorRl.getColors()[2]);

		assertEquals(METHOD.MANUAL, colorRl.getMethod());

		assertEquals("en{- 3 MBps/billion capita}", colorRl.getRuleTitles()
				.get(0));

		assertEquals(RulesListType.QUANTITIES_COLORIZED_POLYGON,
				colorRl.getTypeID());
		assertEquals("SURFACE", colorRl.getValue_field_name());
		assertEquals(null, colorRl.getNormalizer_field_name());
		assertEquals("[[ SURFACE IS NULL ]]", colorRl.getNoDataFilter()
				.toString());
	}
}
