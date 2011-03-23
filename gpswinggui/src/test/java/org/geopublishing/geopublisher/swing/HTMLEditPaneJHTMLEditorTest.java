package org.geopublishing.geopublisher.swing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.geopublishing.atlasViewer.exceptions.AtlasException;
import org.geopublishing.atlasViewer.http.Webserver;
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

public class HTMLEditPaneJHTMLEditorTest extends TestingClass {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testWindowsPath() throws AtlasException, FactoryException,
			TransformException, SAXException, IOException,
			ParserConfigurationException {
		AtlasConfigEditable ace = GpTestingUtil
				.getAtlasConfigE(TestAtlas.small);

		File f = new File(ace.getAtlasDir(), "ad/html/map_01357691812");
		assertTrue(f.exists());

		String browserURLString = ace.getBrowserURLString(f);
		assertNotNull(browserURLString);
		assertEquals("http://localhost:" + Webserver.PORT
				+ "/ad/html/map_01357691812/", browserURLString);

	}

}
