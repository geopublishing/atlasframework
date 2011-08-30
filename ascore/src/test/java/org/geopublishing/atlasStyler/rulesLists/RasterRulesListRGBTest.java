package org.geopublishing.atlasStyler.rulesLists;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.geopublishing.atlasStyler.rulesLists.RasterRulesListRGB;
import org.geotools.styling.FeatureTypeStyle;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.schmitzm.geotools.styling.StyledGridCoverageReader;
import de.schmitzm.geotools.testing.GTTestingUtil;
import de.schmitzm.testing.TestingClass;

public class RasterRulesListRGBTest extends TestingClass {

	private StyledGridCoverageReader styledRaster;

	@Before
	public void setup() throws IOException {
		styledRaster = GTTestingUtil.TestDatasetsRaster.geotiffWithSld
				.getStyled();
	}

	@Test
	public void testRasterRGBImportExport1() {

		RasterRulesListRGB rl = new RasterRulesListRGB(styledRaster, true);

		assertEquals(1, rl.getRed());
		assertEquals(2, rl.getGreen());
		assertEquals(3, rl.getBlue());

		FeatureTypeStyle fts = rl.getFTS();

		RasterRulesListRGB rl2 = new RasterRulesListRGB(styledRaster, true);
		rl2.importFts(fts);

		assertEquals(1, rl2.getRed());
		assertEquals(2, rl2.getGreen());
		assertEquals(3, rl2.getBlue());
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
