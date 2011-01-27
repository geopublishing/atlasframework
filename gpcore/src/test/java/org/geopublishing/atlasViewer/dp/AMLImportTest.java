package org.geopublishing.atlasViewer.dp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GpTestingUtil;
import org.geopublishing.geopublisher.GpTestingUtil.TestAtlas;
import org.junit.Test;

import de.schmitzm.geotools.LogoPosition;
import de.schmitzm.geotools.gui.ScalePanel;
import de.schmitzm.testing.TestingClass;

public class AMLImportTest extends TestingClass {

	public void testSaveAndLoad() throws Exception {
		AtlasConfigEditable ace = GpTestingUtil
				.getAtlasConfigE(TestAtlas.small);

		assertNotNull(ace
				.getResource("ad/data/vector_01367156967_join10/join10.shp"));

		AtlasConfigEditable ace2 = GpTestingUtil.saveAndLoad(ace);

		assertNotNull(ace2
				.getResource("ad/data/vector_01367156967_join10/join10.shp"));
	}

	@Test
	public void testImportExport_MetricNotVisible_MapPosition()
			throws Exception {
		AtlasConfigEditable ace = GpTestingUtil
				.getAtlasConfigE(TestAtlas.small);

		Map map1_0 = ace.getMapPool().get(0);
		map1_0.setScaleUnits(ScalePanel.ScaleUnits.METRIC);
		map1_0.setScaleVisible(false);

		ace.setMaplogoPosition(LogoPosition.TOPLEFT);

		AtlasConfigEditable ace2 = GpTestingUtil.saveAndLoad(ace);

		assertEquals(LogoPosition.TOPLEFT, ace2.getMaplogoPosition());

		Map map2_0 = ace2.getMapPool().get(0);
		assertEquals(map1_0.getScaleUnits(), map2_0.getScaleUnits());
		assertEquals(map1_0.isScaleVisible(), map2_0.isScaleVisible());

		map1_0 = ace.getMapPool().get(0);
		map1_0.setScaleUnits(ScalePanel.ScaleUnits.US);
		map1_0.setScaleVisible(true);
		map1_0.setPreviewMapExtendInGeopublisher(true);

		ace.setMaplogoPosition(LogoPosition.BOTTOMLEFT);
		ace2 = GpTestingUtil.saveAndLoad(ace);
		assertEquals(LogoPosition.BOTTOMLEFT, ace2.getMaplogoPosition());

		map2_0 = ace2.getMapPool().get(0);
		assertEquals(map1_0.getScaleUnits(), map2_0.getScaleUnits());
		assertEquals(map1_0.isScaleVisible(), map2_0.isScaleVisible());
		assertEquals(map1_0.isPreviewMapExtendInGeopublisher(),
				map2_0.isPreviewMapExtendInGeopublisher());

		ace.dispose();
		ace2.dispose();
	}
}
