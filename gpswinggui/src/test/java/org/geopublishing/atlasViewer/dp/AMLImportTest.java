package org.geopublishing.atlasViewer.dp;

import static org.junit.Assert.assertEquals;

import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GPTestingUtil;
import org.geopublishing.geopublisher.GPTestingUtil.Atlas;
import org.junit.Test;

import schmitzm.geotools.gui.ScalePanel;

public class AMLImportTest {

	/**
	 * This tests sets some parameters on a ACE, saves and loads it, and compares the values. The idea of this test is to ensure that saving and loading from/to AtlasMarkup XML language works.
	 */
	@Test
	public void testImportExport() throws Exception {
		AtlasConfigEditable ace = GPTestingUtil.getAtlasConfigE(Atlas.small);

		Map map1_0 = ace.getMapPool().get(0);

		map1_0.setScaleUnits(ScalePanel.ScaleUnits.US);
		map1_0.setScaleVisible(false);

		AtlasConfigEditable ace2 = GPTestingUtil.saveAndLoad(ace);

		Map map2_0 = ace2.getMapPool().get(0);
		assertEquals(map1_0.getScaleUnits(), map2_0.getScaleUnits());
		assertEquals(map1_0.isScaleVisible(), map2_0.isScaleVisible());
	}
}
