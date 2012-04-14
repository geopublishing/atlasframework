package org.geopublishing.atlasStyler.swing;

import org.geotools.data.FeatureSource;
import org.geotools.styling.Graphic;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import de.schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.geotools.testing.GTTestingUtil.TestDatasetsVector;
import de.schmitzm.testing.TestingClass;
import de.schmitzm.testing.TestingUtil;

public class GraphicEditGUITest extends TestingClass {

	FeatureSource<SimpleFeatureType, SimpleFeature> fs;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGraphicEditGUI() throws Throwable {
		if (!TestingUtil.isInteractive())
			return;

		Graphic g = StylingUtil.STYLE_BUILDER.createGraphic();
		GraphicEditGUI graphicEditGUI = new GraphicEditGUI(
				AsTestingUtil.getAtlasStyler(TestDatasetsVector.countryShp), g,
				GeometryForm.POINT);
		TestingUtil.testGui(graphicEditGUI, 220);
	}

}
