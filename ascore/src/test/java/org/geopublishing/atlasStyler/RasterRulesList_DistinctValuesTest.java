package org.geopublishing.atlasStyler;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.io.IOException;

import org.geopublishing.atlasStyler.rulesLists.RasterRulesListColormapTest;
import org.geopublishing.atlasStyler.rulesLists.RasterRulesListRGB;
import org.geopublishing.atlasStyler.rulesLists.RasterRulesList_DistinctValues;
import org.geotools.styling.ColorMap;
import org.geotools.styling.FeatureTypeStyle;
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
	
	@Test
	public void testRasterRGBImportExport2() {

		RasterRulesListRGB rl = new RasterRulesListRGB(styledRaster, true);
		rl.setOpacity(0.6);
		rl.setEnabled(false);
		rl.setRed(3);
		rl.setGreen(2);
		rl.setBlue(1);
		rl.setMaxScaleDenominator(200);
		rl.setMinScaleDenominator(100);

		FeatureTypeStyle fts = rl.getFTS();

		RasterRulesListRGB rl2 = new RasterRulesListRGB(styledRaster, true);
		rl2.importFts(fts);

		assertEquals(.6, rl2.getOpacity(), 0.00000001);
		assertEquals(false, rl2.isEnabled());
		
		assertEquals(100, rl2.getMinScaleDenominator(),0);
		assertEquals(200, rl2.getMaxScaleDenominator(),0);
		
		assertEquals(3, rl2.getRed());
		assertEquals(2, rl2.getGreen());
		assertEquals(1, rl2.getBlue());
	}
}
