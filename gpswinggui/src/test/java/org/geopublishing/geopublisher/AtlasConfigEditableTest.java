package org.geopublishing.geopublisher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.exceptions.AtlasException;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.map.MapRef;
import org.geopublishing.geopublisher.GpTestingUtil.TestAtlas;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;

import schmitzm.junit.TestingClass;
public class AtlasConfigEditableTest extends TestingClass {

	@Test
	public void testGetUsedMaps() throws AtlasException, FactoryException,
			TransformException, SAXException, IOException,
			ParserConfigurationException {
		AtlasConfigEditable ace = GpTestingUtil.getAtlasConfigE(TestAtlas.small);

		assertNotNull(ace.getJnlpBaseUrl());
		assertEquals(AtlasConfig.HTTP_WWW_GEOPUBLISHING_ORG_ATLASES_DEFAULT,
				ace.getJnlpBaseUrl());

		int countMaps = ace.getUsedMaps().size();

		Map newMap = new Map(ace);
		ace.getMapPool().add(newMap);

		// Should still be the same, as the new map is not added to the group
		// tree yet
		assertEquals(countMaps, ace.getUsedMaps().size());

		ace.getRootGroup().add(new MapRef(newMap, ace.getMapPool()));

		// Should be +1 as the new map is now added to the group tree
		assertEquals(countMaps + 1, ace.getUsedMaps().size());

		// Should still be +1 as the added map was already part of the group
		// tree
		ace.getRootGroup().add(newMap);

		ace.getRootGroup().removeAllChildren();
		// Should still be 1 as one map is always exported
		assertEquals(1, ace.getUsedMaps().size());
	}

}
