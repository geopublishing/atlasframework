package org.geopublishing.geopublisher.gui.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.geopublishing.atlasViewer.exceptions.AtlasException;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GpTestingUtil;
import org.geopublishing.geopublisher.GpTestingUtil.TestAtlas;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;

import schmitzm.junit.TestingClass;
import schmitzm.swing.TestingUtil;
public class MapPoolDuplicateActionTest extends TestingClass {

	@Test
	public void testDuplicateMap() throws AtlasException, FactoryException,
			TransformException, SAXException, IOException,
			ParserConfigurationException {
		AtlasConfigEditable ace = GpTestingUtil.getAtlasConfigE(TestAtlas.small);
		Map map1 = ace.getMapPool().get(0);
		File htmlDir1 = ace.getHtmlDirFor(map1);

		int count1 = htmlDir1.list().length;
		long size1 = FileUtils.sizeOfDirectory(htmlDir1);

		assertTrue("map html dir may not be empty for this test", count1 > 0);

		// if (TestingUtil.INTERACTIVE) {
		MapPoolDuplicateAction mapPoolDuplicateAction = new MapPoolDuplicateAction(
				new MapPoolJTable(ace));

		// Start copy now!
		if (TestingUtil.INTERACTIVE) {
			Map map2 = mapPoolDuplicateAction.actionPerformed(map1);

			assertFalse(map1.equals(map2));
			File htmlDir2 = ace.getHtmlDirFor(map2);
			assertFalse(htmlDir1.equals(htmlDir2));

			// Assert, that the files have been copied to.
			long size2 = FileUtils.sizeOfDirectory(htmlDir2);
			int count2 = htmlDir2.list().length;

			assertEquals(size1, size2);
			assertEquals(count1, count2);
			assertFalse("SVN files should have been omitted during copy",
					new File(htmlDir2, ".svn").exists());

			// Cleanup
			FileUtils.deleteDirectory(htmlDir2);
		}

	}

}
