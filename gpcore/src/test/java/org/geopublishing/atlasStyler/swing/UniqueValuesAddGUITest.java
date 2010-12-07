package org.geopublishing.atlasStyler.swing;

import org.geopublishing.atlasStyler.RuleListFactory;
import org.geopublishing.atlasStyler.UniqueValuesRuleList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import schmitzm.junit.TestingClass;
import schmitzm.swing.TestingUtil;
public class UniqueValuesAddGUITest extends TestingClass {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testUniqueValuesAddGUI() throws Throwable {

		// AtlasStyler atlasStyler = new AtlasStyler();
		// final UniqueValuesRuleList rl = atlasStyler.getRlf()
		// .createUniqueValuesRuleList();

		final UniqueValuesRuleList rl = new RuleListFactory(
				TestingUtil.TestDatasetsVector.kreise.getStyledFS())
				.createUniqueValuesRulesList(true);

		if (TestingUtil.INTERACTIVE) {
			UniqueValuesAddGUI dialog = new UniqueValuesAddGUI(null, rl);
			TestingUtil.testGui(dialog, 1);
		}

	}

}
