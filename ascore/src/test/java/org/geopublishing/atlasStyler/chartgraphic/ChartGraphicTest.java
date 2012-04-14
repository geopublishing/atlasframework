package org.geopublishing.atlasStyler.chartgraphic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;
import java.util.Vector;

import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.swing.AsTestingUtil;
import org.geotools.styling.ExternalGraphic;
import org.junit.Test;

import de.schmitzm.geotools.feature.FeatureUtil;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.geotools.testing.GTTestingUtil.TestDatasetsVector;
import de.schmitzm.testing.TestingClass;

public class ChartGraphicTest extends TestingClass {

	@Test
	public void testGetChartGraphic() throws Exception {
		ExternalGraphic chartGraphic = new ChartGraphic().getChartGraphic();

		assertNotNull(chartGraphic);
	}

	@Test
	public void testInOutUrl1() throws Exception {
		extracted("http://chart?cht=bvg&chd=t:${STTCL * 100. / 22.0}&chco=ff0000&chf=bg,s,FFFFFF00&chs=60x61");
	}

	@Test
	public void testInOutUrl2() throws Exception {
		extracted("http://chart?cht=bvg&chd=t:${ObjectID * 100. / 100.0}|${STTID * 100. / 100.0}|${STTCL * 100. / 100.0}&chco=990099,ff0000,000066&chf=bg,s,FFFFFF00&chs=60x62");
	}

	private void extracted(String url) throws MalformedURLException {

		ExternalGraphic eg = StylingUtil.STYLE_BUILDER.createExternalGraphic(
				url, "application/chart");

		ExternalGraphic chartGraphic = new ChartGraphic(
				StylingUtil.STYLE_BUILDER.createGraphic(eg, null, null))
				.getChartGraphic();

		assertNotNull(chartGraphic);

		assertEquals(url,chartGraphic.getLocation().toString());
	}

	@Test
	public void testGetChartGraphicLive() throws Exception {
		AtlasStylerVector atlasStyler = AsTestingUtil
				.getAtlasStyler(TestDatasetsVector.countryShp);
		Vector<String> numericalFieldNames = FeatureUtil
				.getNumericalFieldNames(atlasStyler.getStyledFeatures()
						.getSchema());

		ChartGraphic chartGraphic = new ChartGraphic();
		chartGraphic.addAttribute(numericalFieldNames.get(0));
		chartGraphic.addAttribute(numericalFieldNames.get(1));
		chartGraphic.addAttribute(numericalFieldNames.get(2));

		ExternalGraphic gr = chartGraphic.getChartGraphic();

		assertNotNull(gr);

		System.out.println(gr.getLocation());
	}

	public void testIsChart() throws Exception {

	}
}
