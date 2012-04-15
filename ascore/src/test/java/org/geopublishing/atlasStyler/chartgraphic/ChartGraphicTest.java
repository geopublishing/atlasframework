package org.geopublishing.atlasStyler.chartgraphic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;
import java.util.Vector;

import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.swing.AsTestingUtil;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.Graphic;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Style;
import org.junit.Test;

import com.vividsolutions.jts.geom.Point;

import de.schmitzm.geotools.feature.FeatureUtil;
import de.schmitzm.geotools.styling.StyledLayerUtil;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.geotools.styling.chartsymbols.ChartGraphic;
import de.schmitzm.geotools.testing.GTTestingUtil.TestDatasetsVector;
import de.schmitzm.swing.JPanel;
import de.schmitzm.testing.TestingClass;
import de.schmitzm.testing.TestingUtil;

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

	@Test
	public void testInOutUrl3() throws Exception {
		extracted("http://chart?cht=p3&chd=t:${SQKM_CNTRY * 100 / (SQKM_CNTRY+POP_CNTRY)},${POP_CNTRY * 100 / (SQKM_CNTRY+POP_CNTRY)}&chco=ff0000,cc00cc&chf=bg,s,FFFFFF00&chs=33x44");
	}

	private void extracted(String url) throws MalformedURLException {

		ExternalGraphic eg = StylingUtil.STYLE_BUILDER.createExternalGraphic(
				url, "application/chart");

		ExternalGraphic chartGraphic = new ChartGraphic(
				StylingUtil.STYLE_BUILDER.createGraphic(eg, null, null))
				.getChartGraphic();

		assertNotNull(chartGraphic);

		assertEquals(url, chartGraphic.getLocation().toString());
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

	@Test
	public void testChartGraphicInLegend() throws Throwable {
		// String url =
		// "http://chart?cht=bvg&chd=t:11|40|90&chco=990099,ff0000,000066&chf=bg,s,FFFFFF00&chs=60x62";
		// String url =
		// "http://chart?cht=p3&amp;chd=t:SQKM_CNTRY,SQMI_CNTRY&amp;chco=ff0000,990099&amp;chf=bg,s,FFFFFF00&amp;chs=60x62";
		String url = "http://chart?cht=bvg&chd=t:${POP_CNTRY * 100. / 100.0}|${SQKM_CNTRY * 100. / 100.0}|${SQMI_CNTRY * 100. / 100.0}&chco=990099,ff0000,000066&chf=bg,s,FFFFFF00&chs=60x62";

		ExternalGraphic eg = StylingUtil.STYLE_BUILDER.createExternalGraphic(
				url, "application/chart");

		ExternalGraphic chartGraphic = new ChartGraphic(
				StylingUtil.STYLE_BUILDER.createGraphic(eg, null, null))
				.getChartGraphic();

		Graphic gr = StylingUtil.STYLE_BUILDER.createGraphic(chartGraphic,
				null, null);

		PointSymbolizer ps = StylingUtil.STYLE_BUILDER
				.createPointSymbolizer(gr);

		Style style = StylingUtil.STYLE_BUILDER.createStyle(ps);

		JPanel legendPanel = StyledLayerUtil.createLegendSwingPanel(style,
				FeatureUtil.createFeatureType(Point.class), 20, 15, null);

		TestingUtil.testGui(legendPanel, 50);
	}
}
