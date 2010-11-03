package org.geopublishing.atlasStyler.swing;

import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.UniqueValuesRuleList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import schmitzm.swing.TestingUtil;

public class UniqueValuesAddGUITest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testUniqueValuesAddGUI() throws Throwable {

		AtlasStyler atlasStyler = new AtlasStyler(
				TestingUtil.TestDatasetsVector.kreise.getFeatureSource());
		final UniqueValuesRuleList rl = atlasStyler
				.getUniqueValuesPolygonRuleList();
		
		if (TestingUtil.INTERACTIVE) {
			UniqueValuesAddGUI dialog = new UniqueValuesAddGUI(null, rl);
			TestingUtil.testGui(dialog,1);
		}

	}

}
