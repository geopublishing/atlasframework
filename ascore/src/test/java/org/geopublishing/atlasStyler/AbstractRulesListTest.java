package org.geopublishing.atlasStyler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import java.util.TreeSet;

import org.geopublishing.atlasStyler.swing.AsTestingUtil;
import org.geotools.styling.Style;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengis.filter.Filter;
import org.opengis.filter.PropertyIsEqualTo;

import schmitzm.geotools.FilterUtil;
import schmitzm.geotools.styling.StylingUtil;
import schmitzm.junit.TestingClass;
import schmitzm.swing.TestingUtil.TestDatasetsVector;
public class AbstractRulesListTest extends TestingClass {

	PropertyIsEqualTo f1 = FilterUtil.FILTER_FAC2.equals(
			FilterUtil.FILTER_FAC2.literal(3),
			FilterUtil.FILTER_FAC2.literal(3));

	PropertyIsEqualTo f2 = FilterUtil.FILTER_FAC2.equals(
			FilterUtil.FILTER_FAC2.literal(3),
			FilterUtil.FILTER_FAC2.literal(3));

	PropertyIsEqualTo f3 = FilterUtil.FILTER_FAC2.equals(
			FilterUtil.FILTER_FAC2.literal(3),
			FilterUtil.FILTER_FAC2.literal(3));

	Random rand = new Random();

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testEnabledDisabledFilterViaSaveAndLoad() throws IOException {
		AtlasStyler as = AsTestingUtil
				.getAtlasStyler(TestDatasetsVector.polygonSnow);

		testRuleLists(as, false, "S", f1, false, "U", f1, false, "G", f3,
				false, "T", f1);
		testRuleLists(as, true, "single", f1, true, "unique", f3, true,
				"gradcolors", f3, true, "textLabeling", f1);
		testRuleLists(as, true, "S", null, true, "U", null, true, "G", null,
				false, "T", null);
		testRuleLists(as, false, "S", null, false, "U", null, false, "G", f2,
				true, "T", f3);
	}

	private void testRuleLists(AtlasStyler as, boolean sEnabled, String sTitle,
			Filter sFilter, boolean uEnabled, String uTitle, Filter uFilter,
			boolean gEnabled, String gTitle, Filter gFilter, boolean tEnabled,
			String tTitle, Filter tFilter) throws IOException,
			FileNotFoundException {

		// Create random min/max values to apply and test
		double sMin = rand.nextDouble();
		double sMax = rand.nextDouble();
		double gMin = rand.nextDouble();
		double gMax = rand.nextDouble();
		double uMin = rand.nextDouble();
		double uMax = rand.nextDouble();
		double tMin = rand.nextDouble();
		double tMax = rand.nextDouble();

		as.reset();
		assertEquals(0, as.getRuleLists().size());

		{
			SingleRuleList<?> singleRulesList = as.getRlf()
					.createSingleRulesList(true);
			singleRulesList.setMinScaleDenominator(sMin);
			singleRulesList.setMaxScaleDenominator(sMax);

			// RL_FILTER_APPLIED_STR

			UniqueValuesRuleList uniqueRulesList = as.getRlf()
					.createUniqueValuesRulesList(true);
			// GraduatedColorRuleList needs al least one rule to store the
			// enabled/disabled information
			assertEquals(-1, uniqueRulesList.getAllOthersRuleIdx());
			uniqueRulesList.addDefaultRule();
			uniqueRulesList.setMinScaleDenominator(uMin);
			uniqueRulesList.setMaxScaleDenominator(uMax);

			assertEquals(0, uniqueRulesList.getAllOthersRuleIdx());

			GraduatedColorRuleList gradColorsRulesList = as.getRlf()
					.createGraduatedColorRuleList(true);
			// GraduatedColorRuleList needs al least one rule to store the
			// enabled/disabled information
			TreeSet<Double> classLimits = new TreeSet<Double>();
			classLimits.add(1.);
			classLimits.add(2.);
			gradColorsRulesList.setClassLimits(classLimits);
			gradColorsRulesList.setMinScaleDenominator(gMin);
			gradColorsRulesList.setMaxScaleDenominator(gMax);

			TextRuleList textRulesList = as.getRlf().createTextRulesList(true);
			textRulesList.setMinScaleDenominator(tMin);
			textRulesList.setMaxScaleDenominator(tMax);

			// Enabled/Disabled setzen
			singleRulesList.setEnabled(sEnabled);
			uniqueRulesList.setEnabled(uEnabled);
			gradColorsRulesList.setEnabled(gEnabled);
			textRulesList.setEnabled(tEnabled);

			// Set title
			singleRulesList.setTitle(sTitle);
			uniqueRulesList.setTitle(uTitle);
			gradColorsRulesList.setTitle(gTitle);
			textRulesList.setTitle(tTitle);

			// Set filters
			singleRulesList.setRlFilter(sFilter);
			uniqueRulesList.setRlFilter(uFilter);
			gradColorsRulesList.setRlFilter(gFilter);
			textRulesList.setRlFilter(tFilter);

			// Ruleliste add to AS
			as.addRulesList(singleRulesList);
			as.addRulesList(uniqueRulesList);
			as.addRulesList(gradColorsRulesList);
			as.addRulesList(textRulesList);

			assertEquals(4, as.getRuleLists().size());
		}

		File tf = File.createTempFile("junit", ".sld");
		boolean saveStyleToSld = StylingUtil.saveStyleToSld(as.getStyle(), tf);
		assertTrue(saveStyleToSld);
		StylingUtil.validates(as.getStyle());

		// New AtlasStyler, load the Styles
		as = new AtlasStyler(TestDatasetsVector.polygonSnow.getFeatureSource());
		Style[] importStyle = StylingUtil.loadSLD(tf);
		as.importStyle(importStyle[0]);

		SingleRuleList singleRulesList = (SingleRuleList) as.getRuleLists()
				.get(0);
		UniqueValuesRuleList uniqueRulesList = (UniqueValuesRuleList) as
				.getRuleLists().get(1);
		GraduatedColorRuleList gradColorsRulesList = (GraduatedColorRuleList) as
				.getRuleLists().get(2);
		TextRuleList textRulesList = (TextRuleList) as.getRuleLists().get(3);

		// Compare on/off
		assertEquals(sEnabled, singleRulesList.isEnabled());
		assertEquals(tEnabled, textRulesList.isEnabled());
		assertEquals(gEnabled, gradColorsRulesList.isEnabled());
		assertEquals(uEnabled, uniqueRulesList.isEnabled());

		// Compare Titles
		assertEquals(sTitle, singleRulesList.getTitle());
		assertEquals(tTitle, textRulesList.getTitle());
		assertEquals(uTitle, uniqueRulesList.getTitle());
		assertEquals(gTitle, gradColorsRulesList.getTitle());

		// Filters
		assertEquals(sFilter, singleRulesList.getRlFilter());
		assertEquals(gFilter, gradColorsRulesList.getRlFilter());
		assertEquals(uFilter, uniqueRulesList.getRlFilter());
		assertEquals(tFilter, textRulesList.getRlFilter());

		// Min/Max scales
		assertEquals(sMin, singleRulesList.getMinScaleDenominator(), 0.00001);
		assertEquals(sMax, singleRulesList.getMaxScaleDenominator(), 0.00001);
		assertEquals(gMin, gradColorsRulesList.getMinScaleDenominator(),
				0.00001);
		assertEquals(gMax, gradColorsRulesList.getMaxScaleDenominator(),
				0.00001);
//		assertEquals(gMin, gradColorsRulesList.getTemplate().getMinScaleDenominator(),
//				0.00001);
//		assertEquals(gMax, gradColorsRulesList.getTemplate().getMaxScaleDenominator(),
//				0.00001);
		
		assertEquals(uMin, uniqueRulesList.getMinScaleDenominator(), 0.00001);
		assertEquals(uMax, uniqueRulesList.getMaxScaleDenominator(), 0.00001);
//		assertEquals(uMin, uniqueRulesList.getTemplate().getMinScaleDenominator(), 0.00001);
//		assertEquals(uMax, uniqueRulesList.getTemplate().getMaxScaleDenominator(), 0.00001);
		
		assertEquals(tMin, textRulesList.getMinScaleDenominator(), 0.00001);
		assertEquals(tMax, textRulesList.getMaxScaleDenominator(), 0.00001);

	}
}
