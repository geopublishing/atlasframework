package org.geopublishing.atlasViewer.map;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;

import org.geopublishing.atlasViewer.exceptions.AtlasException;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GpTestingUtil;
import org.geopublishing.geopublisher.GpTestingUtil.TestAtlas;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;

import de.schmitzm.testing.TestingClass;

public class MapTest extends TestingClass {
	private AtlasConfigEditable ace;

	@Before
	public void setup() throws Exception {
		ace = GpTestingUtil.getAtlasConfigE(TestAtlas.small);
	}
	
	@After
	public void cleanup(){
		ace.deleteAtlas();
	}

	@Test
	public void testCopy() throws AtlasException, FactoryException,
			TransformException, SAXException, IOException,
			ParserConfigurationException {

		for (int mi = 0; mi < ace.getMapPool().size(); mi++) {
			Map map = ace.getMapPool().get(mi);

			map.setPreviewMapExtendInGeopublisher(new Random().nextBoolean());

			Map mapCopy = map.copy();

			checkEquals(map, mapCopy);
		}

	}

	private void checkEquals(Map map, Map mapCopy) {
		assertEquals(map.getAc().getTitle(), mapCopy.getAc().getTitle());
		assertEquals(map.getMinimizedInLegendMap(),
				mapCopy.getMinimizedInLegendMap());
		assertEquals(map.isGridPanelVisible(), mapCopy.isGridPanelVisible());
		assertEquals(map.getDefaultMapArea(), mapCopy.getDefaultMapArea());
		assertEquals(map.getMaxExtend(), mapCopy.getMaxExtend());
		assertEquals(map.getLeftRightRatio(), mapCopy.getLeftRightRatio());
		assertEquals(map.isScaleVisible(), mapCopy.isScaleVisible());
		assertEquals(map.isPreviewMapExtendInGeopublisher(),
				mapCopy.isPreviewMapExtendInGeopublisher());
		assertEquals(map.getAdditionalStyles().size(), mapCopy
				.getAdditionalStyles().size());
		assertEquals(map.getLayers().size(), mapCopy.getLayers().size());
	}

	@Test
	public void testSaveAndLoad() throws Exception {

		ace.getMapPool().get(0).setPreviewMapExtendInGeopublisher(true);

		AtlasConfigEditable ace2 = GpTestingUtil.saveAndLoad(ace);

		for (int mi = 0; mi < ace.getMapPool().size(); mi++) {
			Map map = ace.getMapPool().get(mi);
			Map map2 = ace2.getMapPool().get(mi);

			checkEquals(map, map2);
		}
		ace2.deleteAtlas();
	}
}
