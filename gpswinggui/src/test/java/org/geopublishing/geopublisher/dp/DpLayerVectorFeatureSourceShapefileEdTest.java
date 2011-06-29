package org.geopublishing.geopublisher.dp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.geopublishing.atlasViewer.exceptions.AtlasException;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GpTestingUtil;
import org.geopublishing.geopublisher.GpTestingUtil.TestAtlas;
import org.geotools.feature.FeatureCollection;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;

import de.schmitzm.testing.TestingClass;
public class DpLayerVectorFeatureSourceShapefileEdTest extends TestingClass {

	@Test
	public void testImportArabicInHeader() throws AtlasException,
			FactoryException, TransformException, SAXException, IOException,
			ParserConfigurationException {

		// URL url = DpLayerVectorFeatureSourceShapefileEdTest.class
		// .getResource("/arabicShapefiles/arabicwitharabicinheader.shp");
		// assertNotNull(url);
		URL url = GpTestingUtil.TestDatasetsVector.arabicInHeader.getUrl();

		List<String> illegalAtts = DpLayerVectorFeatureSourceShapefileEd
				.checkAttributeNames(url);

		assertEquals(1, illegalAtts.size());
		assertTrue(illegalAtts.get(0).startsWith("3:"));
	}

	@Test
	public void testImportArabic() throws AtlasException, FactoryException,
			TransformException, SAXException, IOException,
			ParserConfigurationException, URISyntaxException {
		AtlasConfigEditable ace = GpTestingUtil.getAtlasConfigE(TestAtlas.small);

		URL url = GpTestingUtil.TestDatasetsVector.arabicInHeader.getUrl();
		
		url = GpTestingUtil.copyShapefileToTemp(url);

		System.out.println(url);
		DpLayerVectorFeatureSourceShapefileEd dpl = new DpLayerVectorFeatureSourceShapefileEd(
				ace, url, null);

		FeatureCollection<SimpleFeatureType, SimpleFeature> fc = dpl
				.getGeoObject().getFeatures();
		Iterator<SimpleFeature> fi = fc.iterator();
		try {
			SimpleFeature f = fi.next();
			Object arabic = f.getAttribute(3);
			assertEquals("وكالة الحوض المائي للوكوس", arabic);
		} finally {
			fc.close(fi);
		}
		ace.deleteAtlas();
	}

	@Test
	public void testParseGeocommonsReadme()
	{
		//TODO
	}

}
