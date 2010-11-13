package org.geopublishing.atlasStyler.swing;

import org.geopublishing.atlasStyler.AtlasStyler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import schmitzm.swing.TestingUtil;
import schmitzm.swing.TestingUtil.TestDatasetsVector;

public class AddRulesListDialogTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAddRulesListDialog() throws Throwable {

		if (!TestingUtil.isInteractive())
			return;
		AtlasStyler as = AsTestingUtil
				.getAtlasStyler(TestDatasetsVector.countryShp);

		AddRulesListDialog addRulesListDialog = new AddRulesListDialog(null, as);

		TestingUtil.testGui(addRulesListDialog, 100);
	}
}
