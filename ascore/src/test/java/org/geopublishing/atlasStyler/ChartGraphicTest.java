package org.geopublishing.atlasStyler;

import static org.junit.Assert.assertNotNull;

import java.util.Vector;

import org.geopublishing.atlasStyler.swing.AsTestingUtil;
import org.geotools.styling.ExternalGraphic;
import org.junit.Test;

import de.schmitzm.geotools.feature.FeatureUtil;
import de.schmitzm.geotools.testing.GTTestingUtil.TestDatasetsVector;
import de.schmitzm.testing.TestingClass;

public class ChartGraphicTest extends TestingClass {

	@Test
	public void testGetChartGraphic() throws Exception {
		ExternalGraphic chartGraphic = new ChartGraphic().getChartGraphic();

		assertNotNull(chartGraphic);
	}

	@Test
	public void testGetChartGraphicLive() throws Exception {
		AtlasStylerVector atlasStyler = AsTestingUtil
				.getAtlasStyler(TestDatasetsVector.countryShp);
		Vector<String> numericalFieldNames = FeatureUtil
				.getNumericalFieldNames(atlasStyler.getStyledFeatures()
						.getSchema());

		ChartGraphic chartGraphic = new ChartGraphic();
		chartGraphic.addAttribute(numericalFieldNames
				.get(0));
		chartGraphic.addAttribute(numericalFieldNames
				.get(1));
		chartGraphic.addAttribute(numericalFieldNames
				.get(2));

		ExternalGraphic gr = chartGraphic.getChartGraphic();

		assertNotNull(gr);
		
		System.out.println(gr.getLocation());
	}

	public void testIsChart()
	 throws Exception {
	
	}
}
