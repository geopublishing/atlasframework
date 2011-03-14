package org.geopublishing.atlasStyler.swing;

import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.schmitzm.geotools.testing.GTTestingUtil.TestDatasetsVector;
import de.schmitzm.testing.TestingClass;
import de.schmitzm.testing.TestingUtil;

public class AtlasStylerPaneTest extends TestingClass {

	private AtlasStylerVector atlasStylerPolygon;

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
		if (!TestingUtil.hasGui())
			return;

		StylerDialog asd = new StylerDialog(null, atlasStylerPolygon, null);
		TestingUtil.testGui(asd);

	}
}
