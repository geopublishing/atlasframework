package org.geopublishing.geopublisher.dp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.dp.AMLImport;
import org.geopublishing.atlasViewer.dp.layer.DpLayerRaster_Reader;
import org.geopublishing.atlasViewer.exceptions.AtlasException;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GpTestingUtil;
import org.geopublishing.geopublisher.GpTestingUtil.TestAtlas;
import org.geopublishing.geopublisher.export.JarExportUtil;
import org.geotools.map.DefaultMapLayer;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;

import rachel.loader.JarResourceLoader;
import schmitzm.junit.TestingClass;
import schmitzm.swing.TestingUtil;
public class DpLayerRasterEdTest extends TestingClass {

	static AtlasConfigEditable ace;
	private static final String RASTER_AAIGRID_SLDTRANSPARENT_ID_IN_RASTERATLAS = "raster_00993740883";
	private static final String RASTER_GEOTIFF_MIT_SLD_ID_IN_RASTERATLAS = "raster_01619177922";

	private static final String RASTER_GEOTIFF_RGB_OHNESLD_ID_IN_RASTERATLAS = "raster_02067770230";

	/**
	 * TODO Use maven test classifier to make DpLayerRasterTest.checkMapLayer..
	 * methods available here
	 */
	public static void checkMapLayer_GEOTIFF_RGB(DefaultMapLayer mlayer)
			throws Throwable {
		BufferedImage bi = TestingUtil.visualize(mlayer);

		assertTrue("Some specific color at 50/50",
				TestingUtil.checkPixel(bi, 50, 50, 126, 221, 42, 255));
		assertTrue("Some red color at 1/1",
				TestingUtil.checkPixel(bi, 1, 1, 230, 76, 0, 255));
	}

	/**
	 * TODO Use maven test classifier to make DpLayerRasterTest.checkMapLayer..
	 * methods available here
	 */

	public static void checkMapLayer_GEOTIFF_WITH_SLD(DefaultMapLayer mlayer)
			throws Throwable {
		BufferedImage bi = TestingUtil.visualize(mlayer);

		assertTrue("Some specific color at 50/50",
				TestingUtil.checkPixel(bi, 50, 50, 161, 125, 74, 255));
		assertTrue("Transparent in the top left corner",
				TestingUtil.checkPixel(bi, 1, 1, 0, 0, 0, 0));
	}

	@BeforeClass
	public static void setupAndLoadAtlas() throws AtlasException,
			FactoryException, TransformException, SAXException, IOException,
			ParserConfigurationException {
		ace = GpTestingUtil.getAtlasConfigE(TestAtlas.rasters);
	}

	File atlasExportTesttDir;

	/**
	 * TODO Use maven test classifier to make DpLayerRasterTest.checkMapLayer..
	 * methods available here
	 */
	public void checkMapLayer_RASTER_AAIGRIDSLDTRANSP(DefaultMapLayer mlayer)
			throws Throwable {
		BufferedImage bi = TestingUtil.visualize(mlayer);

		assertTrue("Some blue color at 20/30",
				TestingUtil.checkPixel(bi, 20, 30, 129, 180, 217, 255));
		assertTrue("Some blue color at 24/34",
				TestingUtil.checkPixel(bi, 24, 34, 124, 175, 215, 255));
		assertTrue("Transparency at 10/10",
				TestingUtil.checkPixel(bi, 10, 10, 0, 0, 0, 0));
	}

	private void exportAtlas() throws IOException, Exception {
		prepareExportTestDirectory();

		JarExportUtil jeu = new JarExportUtil(ace, atlasExportTesttDir, true,
				false, false);
		jeu.export(null);
	}

	private AtlasConfig loadAtlas() throws IOException {
		AtlasConfig acLoaded = new AtlasConfig();
		File exportedAtlasDir = new File(atlasExportTesttDir, "DISK/"
				+ JarExportUtil.DISK_SUB_DIR);

		// Alle Dateien des Ordner zum ClassPath / ResourceLoader hinzufügen:
		for (String fileName : exportedAtlasDir.list()) {
			if (!fileName.endsWith("jar"))
				continue;

			File exportedJarFile = new File(exportedAtlasDir, fileName);

			assertTrue(exportedJarFile.exists());
			assertTrue(!exportedJarFile.isDirectory());

			acLoaded.getResLoMan().addResourceLoader(
					new JarResourceLoader(exportedJarFile));
		}

		new AMLImport().parseAtlasConfig(null, acLoaded, false);

		return acLoaded;
	}

	private void prepareExportTestDirectory() throws IOException {
		System.out.println("atlasExportTesttDir="
				+ atlasExportTesttDir.getAbsolutePath());
		FileUtils.deleteDirectory(atlasExportTesttDir);
		assertTrue(atlasExportTesttDir.mkdir());
	}

	@Before
	public void setupTest() {
		atlasExportTesttDir = GpTestingUtil.createAtlasExportTesttDir();
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(atlasExportTesttDir);
	}

	@Test
	public void testTransparencyOfAAIGrid_Transparent_With_SLD_GP()
			throws Throwable {

		DpLayerRaster_Reader rasterAAIGrid_TransparentWithSLD = (DpLayerRaster_Reader) ace
				.getDataPool().get(
						RASTER_AAIGRID_SLDTRANSPARENT_ID_IN_RASTERATLAS);
		DefaultMapLayer mlayer = new DefaultMapLayer(
				rasterAAIGrid_TransparentWithSLD.getGeoObject(),
				rasterAAIGrid_TransparentWithSLD.getStyle());
		checkMapLayer_RASTER_AAIGRIDSLDTRANSP(mlayer);
	}

	@Test
	public void testTransparencyOfGeotiffRGBonly_GP() throws Throwable {
		DpLayerRaster_Reader raster = (DpLayerRaster_Reader) ace.getDataPool()
				.get(RASTER_GEOTIFF_RGB_OHNESLD_ID_IN_RASTERATLAS);
		DefaultMapLayer mlayer = new DefaultMapLayer(raster.getGeoObject(),
				raster.getStyle());
		checkMapLayer_GEOTIFF_RGB(mlayer);
	}

	@Test
	public void testTransparencyOfGeotiffWithSLD_AVJARafterExport()
			throws Throwable {
		// assertNotNull(atlasExportTesttDir);
		exportAtlas();

		{
			// Test for existance of exported raster jar
			File exportedAtlasDir = new File(atlasExportTesttDir, "DISK/"
					+ JarExportUtil.DISK_SUB_DIR);
			String rasterJarFilename = RASTER_GEOTIFF_MIT_SLD_ID_IN_RASTERATLAS
					+ ".jar";
			File rasterGeotiffMitSldJarFile = new File(exportedAtlasDir,
					rasterJarFilename);
			assertTrue(rasterGeotiffMitSldJarFile.exists());
		}

		AtlasConfig acLoaded = loadAtlas();
		assertEquals(3, acLoaded.getDataPool().size());

		{ // Test Raster 1
			DpLayerRaster_Reader rasterGeoTIFFmitSLD = (DpLayerRaster_Reader) acLoaded
					.getDataPool()
					.get(RASTER_GEOTIFF_MIT_SLD_ID_IN_RASTERATLAS);
			DefaultMapLayer mlayer = new DefaultMapLayer(
					rasterGeoTIFFmitSLD.getGeoObject(),
					rasterGeoTIFFmitSLD.getStyle());
			checkMapLayer_GEOTIFF_WITH_SLD(mlayer);
		}

		{ // Test Raster 2
			DpLayerRaster_Reader rasterGeoTIFF_RGBonly = (DpLayerRaster_Reader) acLoaded
					.getDataPool().get(
							RASTER_GEOTIFF_RGB_OHNESLD_ID_IN_RASTERATLAS);
			DefaultMapLayer mlayer = new DefaultMapLayer(
					rasterGeoTIFF_RGBonly.getGeoObject(),
					rasterGeoTIFF_RGBonly.getStyle());
			checkMapLayer_GEOTIFF_RGB(mlayer);
		}

		{ // Test Raster 3
			DpLayerRaster_Reader rasterAAIGrid_SLDTransp = (DpLayerRaster_Reader) acLoaded
					.getDataPool().get(
							RASTER_AAIGRID_SLDTRANSPARENT_ID_IN_RASTERATLAS);
			DefaultMapLayer mlayer = new DefaultMapLayer(
					rasterAAIGrid_SLDTransp.getGeoObject(),
					rasterAAIGrid_SLDTransp.getStyle());
			checkMapLayer_RASTER_AAIGRIDSLDTRANSP(mlayer);
		}

	}

	@Test
	public void testTransparencyOfGeotiffWithSLD_GP() throws Throwable {
		DpLayerRaster_Reader rasterGeoTIFFmitSLD = (DpLayerRaster_Reader) ace
				.getDataPool().get(RASTER_GEOTIFF_MIT_SLD_ID_IN_RASTERATLAS);
		DefaultMapLayer mlayer = new DefaultMapLayer(
				rasterGeoTIFFmitSLD.getGeoObject(),
				rasterGeoTIFFmitSLD.getStyle());
		checkMapLayer_GEOTIFF_WITH_SLD(mlayer);
	}
}
