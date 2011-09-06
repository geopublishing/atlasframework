package org.geopublishing.atlasStyler.rulesLists;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.io.IOException;

import org.geotools.styling.ColorMap;
import org.junit.Before;
import org.junit.Test;

import de.schmitzm.geotools.data.rld.RasterLegendData;
import de.schmitzm.geotools.styling.StyledGridCoverageReader;
import de.schmitzm.geotools.styling.StyledLayerUtil;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.geotools.testing.GTTestingUtil;

public class RasterRulesList_DistinctValuesTest extends RasterRulesListColormapTest {
	
	private StyledGridCoverageReader styledRaster;

	@Before
	public void setup() throws IOException {
		styledRaster = GTTestingUtil.TestDatasetsRaster.arcAscii.getStyled();
	}

	@Test
	public void testColorMapImport1() {
		ColorMap cm = StylingUtil.STYLE_BUILDER.createColorMap(new String[] {"A","B","C"}, new double[] {1,2,3}, new Color[]{Color.red, Color.blue, Color.green}, ColorMap.TYPE_VALUES);
		RasterRulesList_DistinctValues rl = new RasterRulesList_DistinctValues(styledRaster);
		rl.importColorMap(cm);
		
		assertEquals(3,rl.getValues().size());
		
		assertEquals(3,rl.getLabels().size());
		assertEquals(3,rl.getColors().size());
		assertEquals(3,rl.getOpacities().size());
		assertEquals(3,rl.getNumClasses());
		
		assertEquals(Color.red, rl.getColors().get(0));
		assertEquals("C", rl.getLabels().get(2).toString());
		
		testExportAndImport(rl, new RasterRulesList_DistinctValues(
				styledRaster));
		
		RasterLegendData rld = StyledLayerUtil.generateRasterLegendData(rl.getColorMap(), true,null);
		assertEquals(3,rld.size());
		assertEquals("A",rld.get(1.).toString());
		assertEquals("B",rld.get(2.).toString());
		assertEquals("C",rld.get(3.).toString());
	}
	
}
