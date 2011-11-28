package org.geopublishing.geopublisher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.exceptions.AtlasException;
import org.geopublishing.atlasViewer.internal.AMLUtil;
import org.geopublishing.geopublisher.GpTestingUtil.TestAtlas;
import org.junit.After;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import de.schmitzm.testing.TestingUtil;

public class AMLExporterTest extends AMLExporter {

	public AMLExporterTest() throws ParserConfigurationException,
			AtlasException, FactoryException, TransformException, SAXException,
			IOException, TransformerException {
		super(GpTestingUtil.getAtlasConfigE(TestAtlas.small));
	}

	@After
	public void cleanup() {
		getAce().deleteAtlas();
	}

	@Test
	public void testFonts() throws AtlasException, FactoryException,
			TransformException, SAXException, IOException,
			ParserConfigurationException, TransformerException {

		// Create a DOM builder and parse the fragment
		final DocumentBuilderFactory factory = DocumentBuilderFactory
				.newInstance();
		Document document = null;
		document = factory.newDocumentBuilder().newDocument();

		Node fontsE = exportFonts(document);
		int length = fontsE.getChildNodes().getLength();
		assertEquals(1, length);

		Node namedItem = fontsE.getChildNodes().item(0).getAttributes()
				.getNamedItem(AMLUtil.ATT_FONT_FILENAME);

		assertEquals("futura_book.ttf", namedItem.getTextContent());
	}

	@Test
	public void testWriteBuildxml() throws AtlasException, FactoryException,
			TransformException, SAXException, IOException,
			ParserConfigurationException, TransformerException {

		File newTempDir = TestingUtil.getNewTempDir();

		// With default name should return null
		getAce().setBaseName(AtlasConfig.DEFAULTBASENAME);
		File created = writeBuildxml(newTempDir, getAce().getBaseName());
		assertNull(created);

		getAce().setBaseName(
				AtlasConfig.DEFAULTBASENAME + System.currentTimeMillis());
		created = writeBuildxml(newTempDir, getAce().getBaseName());
		assertNotNull(created);
		assertTrue(created.exists());

		created = writeBuildxml(newTempDir, getAce().getBaseName() + "/");
		assertNotNull(created);
		assertTrue(created.exists());
		FileUtils.deleteDirectory(newTempDir);

	}
	
}
