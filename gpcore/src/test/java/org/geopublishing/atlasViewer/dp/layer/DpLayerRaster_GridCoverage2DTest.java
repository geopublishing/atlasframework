package org.geopublishing.atlasViewer.dp.layer;

import static org.junit.Assert.assertNotNull;

import java.net.URL;

import org.geotools.gce.geotiff.GeoTiffReader;
import org.junit.Test;

public class DpLayerRaster_GridCoverage2DTest {

	@Test
	public void asdasd() throws Exception {
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
