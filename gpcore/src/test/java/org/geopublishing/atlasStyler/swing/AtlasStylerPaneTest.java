package org.geopublishing.atlasStyler.swing;

import org.geopublishing.atlasStyler.AtlasStyler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import schmitzm.junit.TestingClass;
import schmitzm.swing.TestingUtil;
import schmitzm.swing.TestingUtil.TestDatasetsVector;

public class AtlasStylerPaneTest extends TestingClass {

	private AtlasStyler atlasStylerPolygon;

	@Before
	public void setUp() throws Exception {
		atlasStylerPolygon = AsTestingUtil
				.getAtlasStyler(TestDatasetsVector.kreise);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAtlasStylerPane() throws Throwable {
		if (!TestingUtil.isInteractive())
			return;

		StylerDialog asd = new StylerDialog(null, atlasStylerPolygon, null);
		TestingUtil.testGui(asd, 10);

	}
}
