package org.geopublishing.atlasStyler;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.io.IOException;

import org.geopublishing.atlasStyler.rulesLists.RasterRulesListColormapTest;
import org.geopublishing.atlasStyler.rulesLists.RasterRulesList_Intervals;
import org.geotools.styling.ColorMap;
import org.junit.Before;
import org.junit.Test;

import de.schmitzm.geotools.data.rld.RasterLegendData;
import de.schmitzm.geotools.styling.StyledGridCoverageReader;
import de.schmitzm.geotools.styling.StyledLayerUtil;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.geotools.testing.GTTestingUtil;

public class RasterRulesList_IntervalsTest extends RasterRulesListColormapTest {

	private StyledGridCoverageReader styledRaster;

	@Before
	public void setup() throws IOException {
		styledRaster = GTTestingUtil.TestDatasetsRaster.arcAscii.getStyled();
	}

	@Test
	public void testColorMapImport1() {
		ColorMap cm = StylingUtil.STYLE_BUILDER
				.createColorMap(new String[] { "A", "B", "ignore", "ignore2" },
						new double[] { 1, 2, 3, 3 }, new Color[] { Color.red,
								Color.blue, Color.green, Color.green },
						ColorMap.TYPE_INTERVALS);
		RasterRulesList_Intervals rl = new RasterRulesList_Intervals(
				styledRaster, false);
		rl.importColorMap(cm);

		assertEquals(3, rl.getValues().size());

		assertEquals(2, rl.getLabels().size());
		assertEquals(2, rl.getColors().size());
		assertEquals(2, rl.getOpacities().size());
		assertEquals(2, rl.getNumClasses());

		assertEquals(Color.blue, rl.getColors().get(0));
		assertEquals("B", rl.getLabels().get(1).toString());

		testExportAndImport(rl, new RasterRulesList_Intervals(styledRaster,
				false));

		RasterLegendData rld = StyledLayerUtil.generateRasterLegendData(
				rl.getColorMap(), true, null);
		assertEquals(2, rld.size());
		assertEquals("A", rld.get(1.).toString());
		assertEquals("B", rld.get(2.).toString());
	}

}
