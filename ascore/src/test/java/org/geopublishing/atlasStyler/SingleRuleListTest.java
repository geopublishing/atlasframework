package org.geopublishing.atlasStyler;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.geopublishing.atlasStyler.rulesLists.SingleRuleList;
import org.geopublishing.atlasStyler.swing.AsTestingUtil;
import org.geotools.styling.Style;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.schmitzm.geotools.testing.GTTestingUtil.TestDatasetsVector;
import de.schmitzm.testing.TestingClass;

public class SingleRuleListTest extends TestingClass {

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testTitles() throws TransformerException, IOException {
		AtlasStylerVector as = AsTestingUtil
				.getAtlasStyler(TestDatasetsVector.polygonSnow);

		SingleRuleList<?> singleRulesList = as.getRlf().createSingleRulesList(
				AtlasStylerVector.getRuleTitleFor(as.getStyledFeatures()), false);

		singleRulesList.setLabel("RuleTitle");
		singleRulesList.setTitle("RulesListTitle");

		as.addRulesList(singleRulesList);

		Style s = as.getStyle();

		// This Style should have both titles set...

		as = AsTestingUtil.getAtlasStyler(TestDatasetsVector.polygonSnow);
		as.importStyle(s);

		assertEquals(1, as.getRuleLists().size());

		SingleRuleList srl = (SingleRuleList) as.getRuleLists().get(0);

		assertEquals(singleRulesList.getTitle(), srl.getTitle());
		assertEquals("RulesListTitle", srl.getTitle());

		assertEquals(singleRulesList.getLabel(), srl.getLabel());
		assertEquals("RuleTitle", srl.getLabel());

		// test whether copy copies the titles correctly
		assertEquals("RulesListTitle", ((SingleRuleList) srl.copy()).getTitle());
		assertEquals("RuleTitle", ((SingleRuleList) srl.copy()).getLabel());

	}
}
