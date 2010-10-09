package org.geopublishing.geopublisher.dp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.geopublishing.atlasViewer.exceptions.AtlasException;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GPTestingUtil;
import org.geopublishing.geopublisher.GPTestingUtil.Atlas;
import org.geotools.feature.FeatureCollection;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;

public class DpLayerVectorFeatureSourceShapefileEdTest {

	@Test
	public void testImportArabic() throws AtlasException, FactoryException,
			TransformException, SAXException, IOException,
			ParserConfigurationException {
		AtlasConfigEditable ace = GPTestingUtil.getAtlasConfigE(Atlas.small);

		URL url = DpLayerVectorFeatureSourceShapefileEdTest.class
				.getResource("/arabicShapefiles/arabicdata.shp");
		assertNotNull(url);
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
	}

	@Test
	public void testImportArabicInHeader() throws AtlasException,
			FactoryException, TransformException, SAXException, IOException,
			ParserConfigurationException {

		URL url = DpLayerVectorFeatureSourceShapefileEdTest.class
				.getResource("/arabicShapefiles/arabicwitharabicinheader.shp");
		assertNotNull(url);

		List<String> illegalAtts = DpLayerVectorFeatureSourceShapefileEd
				.checkAttributeNames(url);

		assertEquals(1, illegalAtts.size());
		assertTrue(illegalAtts.get(0).startsWith("3:"));
	}

}
