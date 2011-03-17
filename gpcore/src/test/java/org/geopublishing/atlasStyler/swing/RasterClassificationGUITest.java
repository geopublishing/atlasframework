package org.geopublishing.atlasStyler.swing;

import org.geopublishing.atlasStyler.AtlasStylerRaster;
import org.geopublishing.atlasStyler.classification.RasterClassification;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.schmitzm.geotools.testing.GTTestingUtil.TestDatasetsRaster;
import de.schmitzm.testing.TestingClass;
import de.schmitzm.testing.TestingUtil;

public class RasterClassificationGUITest extends TestingClass {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testShowGui() throws Throwable {
		if (!hasGui())
			return;

		AtlasStylerRaster asv = AsTestingUtil
				.getAtlasStyler(TestDatasetsRaster.arcAscii);
		RasterClassification classifier = new RasterClassification(
				asv.getStyledRaster());
		ClassificationGUI gui = new RasterClassificationGUI(null, classifier,
				asv, "junit test vector classification");

		TestingUtil.testGui(gui, 10);
	}

}
