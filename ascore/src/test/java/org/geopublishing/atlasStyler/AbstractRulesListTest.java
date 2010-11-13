package org.geopublishing.atlasStyler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.TreeSet;

import org.geopublishing.atlasStyler.swing.AsTestingUtil;
import org.geotools.styling.Style;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import schmitzm.geotools.styling.StylingUtil;
import schmitzm.swing.TestingUtil.TestDatasetsVector;

public class AbstractRulesListTest {

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

		testEnabledOnRuleLists(as, false, false, false, false);
		testEnabledOnRuleLists(as, true, true, true, true);
		testEnabledOnRuleLists(as, true, true, true, false);
		testEnabledOnRuleLists(as, false, false, false, true);
	}

	private void testEnabledOnRuleLists(AtlasStyler as, boolean sEnabled,
			boolean uEnabled, boolean gEnabled, boolean tEnabled)
			throws IOException, FileNotFoundException {

		as.reset();
		assertEquals(0, as.getRuleLists().size());

		{
			SingleRuleList singleRulesList = as.getRlf().createSingleRulesList(
					true);
			UniqueValuesRuleList uniqueRulesList = as.getRlf()
					.createUniqueValuesRulesList(true);
			// GraduatedColorRuleList needs al least one rule to store the
			// enabled/disabled information
			assertEquals(-1, uniqueRulesList.getAllOthersRuleIdx());
			uniqueRulesList.addDefaultRule();
			assertEquals(0, uniqueRulesList.getAllOthersRuleIdx());

			GraduatedColorRuleList gradColorsRulesList = as.getRlf()
					.createGraduatedColorRuleList(true);
			// GraduatedColorRuleList needs al least one rule to store the
			// enabled/disabled information
			TreeSet<Double> classLimits = new TreeSet<Double>();
			classLimits.add(1.);
			classLimits.add(2.);
			gradColorsRulesList.setClassLimits(classLimits);

			TextRuleList textRulesList = as.getRlf().createTextRulesList(true);

			singleRulesList.setEnabled(sEnabled);
			uniqueRulesList.setEnabled(uEnabled);
			gradColorsRulesList.setEnabled(gEnabled);
			textRulesList.setEnabled(tEnabled);
			//
			as.addRulesList(singleRulesList);
			as.addRulesList(uniqueRulesList);
			as.addRulesList(gradColorsRulesList);
			as.addRulesList(textRulesList);

			assertEquals(4, as.getRuleLists().size());
		}

		File tf = File.createTempFile("junit", ".sld");
		boolean saveStyleToSld = StylingUtil.saveStyleToSld(as.getStyle(), tf);
		assertTrue(saveStyleToSld);

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

		assertEquals(sEnabled, singleRulesList.isEnabled());
		assertEquals(tEnabled, textRulesList.isEnabled());
		assertEquals(gEnabled, gradColorsRulesList.isEnabled());
		assertEquals(uEnabled, uniqueRulesList.isEnabled());
	}

}
