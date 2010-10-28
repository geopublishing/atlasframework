package org.geopublishing.atlasViewer.dp.layer;

import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStoreFinder;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.junit.Before;
import org.junit.Test;

import schmitzm.geotools.io.GeoImportUtil;

public class DpLayerRaster_GridCoverage2DTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void tear() throws Exception {
		URL jarWithGeotiff = DpLayerRaster_GridCoverage2D.class
				.getResource("/rasterGeotiffInjar/rn.jar");
		assertNotNull(jarWithGeotiff);

		URL geotiffInJar = new URL(
				"jar:"
						+ jarWithGeotiff.toString()
						+ "!/ad/data/raster_nega1102068562650/nega11.tif");
		
		GeoTiffReader geoTiffReader = new GeoTiffReader(geotiffInJar.openStream());
		
		assertNotNull(geoTiffReader);

		// jar:http://localhost/atlas/vector_landesgrenze_linie_01952848885.jar!/ad/data/vector_landesgrenze_linie_01952848885/landesgrenze_linie.shp

	}

}
