package org.geopublishing.atlasViewer.dp.layer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.net.URL;

import org.geotools.data.DataUtilities;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.map.DefaultMapLayer;
import org.geotools.styling.ColorMap;
import org.geotools.styling.ColorMapEntry;
import org.geotools.styling.ColorMapEntryImpl;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleVisitor;
import org.junit.Test;
import org.opengis.filter.expression.Expression;

import schmitzm.geotools.grid.GridUtil;
import schmitzm.geotools.styling.StylingUtil;
import schmitzm.swing.TestingUtil;

public class DpLayerRasterTest {

	private static final String RASTER_GEOTIFF_WITH_SLD__SLDLOCATION = "/rasterGeotiffWithSLD/geotiffwithsld.sld";
	private static final Style RASTER_GEOTIFF_WITH_SLD__SLDSTYLE = StylingUtil
			.loadSLD(DpLayerRasterTest.class
					.getResource(RASTER_GEOTIFF_WITH_SLD__SLDLOCATION))[0];
	private static final String RASTER_GEOTIFF_WITH_SLD__TIFLOCATION = "/rasterGeotiffWithSLD/geotiffwithsld.tif";
	private static final URL RASTER_GEOTIFF_WITH_SLD__URL = DpLayerRasterTest.class
			.getResource(RASTER_GEOTIFF_WITH_SLD__TIFLOCATION);
	
	private static final String RASTER_GEOTIFF_RGBONLY__TIFLOCATION = "/rasterGeotiffRGBWithoutSLD/geotiff_rgb_ohnesld.tif";
	private static final URL RASTER_GEOTIFF_RGBONLY__URL = DpLayerRasterTest.class
			.getResource(RASTER_GEOTIFF_RGBONLY__TIFLOCATION);

	public static void checkMapLayer_GEOTIFF_WITH_SLD(DefaultMapLayer mlayer)
			throws Throwable {
		BufferedImage bi = TestingUtil.visualize(mlayer);

		assertTrue("Some specific color at 50/50",
				TestingUtil.checkPixel(bi, 50, 50, 161, 125, 74, 255));
		assertTrue("Transparent in the top left corner",
				TestingUtil.checkPixel(bi, 1, 1, 0, 0, 0, 0));
	}

	@Test
	public void testJustColorsGeotiffRGB_GeoTiffReaderWithFileObject()
			throws Throwable {
		URL url = RASTER_GEOTIFF_RGBONLY__URL;
		GeoTiffReader gc = new GeoTiffReader(DataUtilities.urlToFile(url));
		assertNotNull(gc);

		Style createDefaultStyle = GridUtil.createDefaultStyle();
//		RasterSymbolizer rs = (RasterSymbolizer) createDefaultStyle.featureTypeStyles().get(0).rules().get(0).symbolizers().get(0);
//		ColorMap colorMap = rs.getColorMap();
//		ColorMapEntryImpl cme = new ColorMapEntryImpl();
		DefaultMapLayer mlayer = new DefaultMapLayer(gc, createDefaultStyle);

		checkMapLayer_GEOTIFF_RGB(mlayer);

		gc.dispose();
	}

	public static void checkMapLayer_GEOTIFF_RGB(DefaultMapLayer mlayer) throws Throwable {
		BufferedImage bi = TestingUtil.visualize(mlayer);

		assertTrue("Some specific color at 50/50",
				TestingUtil.checkPixel(bi, 50, 50, 126, 221, 42, 255));
		assertTrue("Some red color at 1/1",
				TestingUtil.checkPixel(bi, 1, 1, 230, 76, 0, 255));
	}

	@Test
	public void testTransparencyOfGeotiffWithSLD_GeoTiffReaderWithFileObject()
			throws Throwable {
		URL url = RASTER_GEOTIFF_WITH_SLD__URL;
		GeoTiffReader gc = new GeoTiffReader(DataUtilities.urlToFile(url));
		assertNotNull(gc);

		DefaultMapLayer mlayer = new DefaultMapLayer(gc,
				RASTER_GEOTIFF_WITH_SLD__SLDSTYLE);

		checkMapLayer_GEOTIFF_WITH_SLD(mlayer);

		gc.dispose();
	}

	@Test
	public void testTransparencyOfGeotiffWithSLD_GeoTiffReaderWithURL2FileObject()
			throws Throwable {
		URL url = RASTER_GEOTIFF_WITH_SLD__URL;
		GeoTiffReader gc = new GeoTiffReader(url);
		assertNotNull(gc);

		DefaultMapLayer mlayer = new DefaultMapLayer(gc,
				RASTER_GEOTIFF_WITH_SLD__SLDSTYLE);

		checkMapLayer_GEOTIFF_WITH_SLD(mlayer);

		gc.dispose();
	}

}
