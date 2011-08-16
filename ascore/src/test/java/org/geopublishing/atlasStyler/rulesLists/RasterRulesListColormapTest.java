package org.geopublishing.atlasStyler.rulesLists;

import static org.junit.Assert.assertEquals;

import org.geotools.styling.ColorMap;
import org.junit.Test;

import de.schmitzm.testing.TestingClass;

public class RasterRulesListColormapTest extends TestingClass {
	
	@Test
	public void testRun() {
		System.out.println("all good");
	}

	protected void testExportAndImport(RasterRulesListColormap rl, RasterRulesListColormap rl2) {
		// Reimport
		{
			ColorMap colorMap2 = rl.getColorMap();
			rl2.importColorMap(colorMap2);
			assertEquals(rl.getColors().size(), rl2.getColors().size());
			assertEquals(rl.getColors().get(0), rl2.getColors().get(0));

			assertEquals(rl.getLabels().size(), rl2.getLabels().size());
			assertEquals(rl.getLabels().get(0), rl2.getLabels().get(0));
			assertEquals(rl.getOpacities().size(), rl2.getOpacities().size());
			assertEquals(rl.getOpacities().get(0), rl2.getOpacities().get(0));
			assertEquals(rl.getValues().size(), rl2.getValues().size());
			assertEquals(rl.getValues().get(0), rl2.getValues().get(0));
		}

	}
}
