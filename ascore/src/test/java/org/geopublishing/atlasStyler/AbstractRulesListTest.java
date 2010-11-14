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

		testRuleLists(as, false, "S", false, "U", false, "G", false, "T");
		testRuleLists(as, true, "single", true, "unique", true, "gradcolors",
				true, "textLabeling");
		testRuleLists(as, true, "S", true, "U", true, "G", false, "T");
		testRuleLists(as, false, "S", false, "U", false, "G", true, "T");
	}

	private void testRuleLists(AtlasStyler as, boolean sEnabled, String sTitle,
			boolean uEnabled, String uTitle, boolean gEnabled, String gTitle,
			boolean tEnabled, String tTitle) throws IOException,
			FileNotFoundException {

		as.reset();
		assertEquals(0, as.getRuleLists().size());

		{
			SingleRuleList<?> singleRulesList = as.getRlf()
					.createSingleRulesList(true);
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

		// Compre on/off
		assertEquals(sEnabled, singleRulesList.isEnabled());
		assertEquals(tEnabled, textRulesList.isEnabled());
		assertEquals(gEnabled, gradColorsRulesList.isEnabled());
		assertEquals(uEnabled, uniqueRulesList.isEnabled());

		// Compare Titles
		assertEquals(sTitle, singleRulesList.getTitle());
		assertEquals(tTitle, textRulesList.getTitle());
		assertEquals(uTitle, uniqueRulesList.getTitle());
		assertEquals(gTitle, gradColorsRulesList.getTitle());

	}

}
