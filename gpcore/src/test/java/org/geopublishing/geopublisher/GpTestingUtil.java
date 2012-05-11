/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.GpCoreUtil;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;
import org.geopublishing.atlasViewer.exceptions.AtlasException;
import org.geopublishing.atlasViewer.http.Webserver;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.swing.AtlasMapLegend;
import org.geopublishing.atlasViewer.swing.AtlasViewerGUI;
import org.geopublishing.geopublisher.GpTestingUtil.TestAtlas;
import org.geotools.data.DataUtilities;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;

import de.schmitzm.geotools.GTUtil;
import de.schmitzm.geotools.gui.GeoMapPane;
import de.schmitzm.geotools.gui.MapPaneToolBar;
import de.schmitzm.geotools.testing.GTTestingUtil;
import de.schmitzm.io.IOUtil;
import de.schmitzm.testing.TestingUtil;

public class GpTestingUtil extends GTTestingUtil {

	/** An enumeration of available test-atlases **/
	public enum TestAtlas {

		// TODO Create a type "new" which creates a new empty atlas in tmp dir
		// on getAce()

		small("/atlases/ChartDemoAtlas/atlas.gpa"), rasters(
				"/atlases/rastersAtlas/atlas.gpa"), charts(
						"/atlases/chartAtlas/atlas.gpa");

		private final String resourceLocation;

		TestAtlas(String resourceLocation) {
			this.resourceLocation = resourceLocation;
		}

		public String getReslocation() {
			return resourceLocation;
		}

		public AtlasConfigEditable getAce() {
			// System.out.println("Start loading test atlas config ...");
			try {
				return getAtlasConfigE(getFile().getParent());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public URL getUrl() {
			URL resourceUrl = GpUtil.class.getResource(getReslocation());
			if (resourceUrl == null)
				throw new RuntimeException("The test-resource "
						+ getReslocation() + " could not be found in classpath");
			else
				System.out.println("URL for " + getReslocation() + " is "
						+ resourceLocation);
			return resourceUrl;
		}

		/**
		 * Returns a {@link File} to a <code>atlas.gpa</code>. If the atlas
		 * comes from the classpath, it is copied to a temp directory first.
		 */
		public File getFile() {
			try {
				URL url = getUrl();
				File urlToFile = DataUtilities.urlToFile(url);
				// Unzip to /tmp
				File td = TestingUtil.getNewTempDir();
				if (urlToFile != null) {
					// // Load Atlas from directory

					FileUtils.copyDirectory(urlToFile.getParentFile(), td);
					return new File(td, "atlas.gpa");
				} else {
					File fileFromJarFileUrl = IOUtil.getFileFromJarFileUrl(url);
					IOUtil.unzipArchive(fileFromJarFileUrl, td);
					return new File(td, getReslocation());
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	static AtlasConfigEditable getAtlasConfigE(String atlasDir)
			throws FactoryException, TransformException, AtlasException,
			SAXException, IOException, ParserConfigurationException {

		GpCoreUtil.initAtlasLogging();

		AtlasViewerGUI.setupResLoMan(new String[] { atlasDir });

		/***********************************************************************
		 * Remove the old geopublisher.properties file, so we always start with
		 * the default
		 */
		GPProps.resetProperties(null);

		AtlasConfigEditable atlasConfig = new AtlasConfigEditable(new File(
				atlasDir));

		GTUtil.initEPSG();

		Webserver webserver = new Webserver();

		new AMLImportEd().parseAtlasConfig(null, atlasConfig, false);

		assertNotNull("AtlasConfig is null after parseAtlasConfig!",
				atlasConfig);
		assertNotNull("MapPool is null after parseAtlasConfig!",
				atlasConfig.getMapPool());
		assertNotNull("DataPool is null after parseAtlasConfig!",
				atlasConfig.getDataPool());

		return atlasConfig;
	}

	/**
	 * Creates a directory in /tmp that can be used to export an atlas.
	 */
	public static File createAtlasExportTesttDir() {
		File atlasExportTesttDir = new File(IOUtil.getTempDir(),
				"junitTestAtlasExport" + System.currentTimeMillis());
		atlasExportTesttDir.mkdirs();
		return atlasExportTesttDir;
	}

	/**
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * @throws TransformException
	 * @throws FactoryException
	 * @throws AtlasException
	 * @Deprecated use {@link #getAtlasConfigE(TestAtlas)} with
	 *             {@link TestAtlas.iida2}
	 */
	public static AtlasConfigEditable getAtlasConfigE() throws AtlasException,
			FactoryException, TransformException, SAXException, IOException,
			ParserConfigurationException {
		return getAtlasConfigE(TestAtlas.small);
	}

	public static DpLayerVectorFeatureSource getCities(AtlasConfigEditable ace) {
		return (DpLayerVectorFeatureSource) ace.getDataPool().get(
				"vector_village_all_v1.501530158160");
	}

	public static AtlasMapLegend getAtlasMapLegend(AtlasConfigEditable ace) {
		Map map = ace.getMapPool().get(ace.getMapPool().getStartMapID());
		GeoMapPane gmp = new GeoMapPane();
		MapPaneToolBar mptb = new MapPaneToolBar(gmp.getMapPane());
		return new AtlasMapLegend(gmp, map, ace, mptb);
	}

	public static AtlasConfigEditable getAtlasConfigE(TestAtlas type)
			throws AtlasException, FactoryException, TransformException,
			SAXException, IOException, ParserConfigurationException {

		return type.getAce();
	}

	public static DpLayerVectorFeatureSource getCities() throws AtlasException,
			FactoryException, TransformException, SAXException, IOException,
			ParserConfigurationException {
		return getCities(getAtlasConfigE());
	}

	public static AtlasConfigEditable saveAndLoad(AtlasConfigEditable ace)
			throws Exception {
		File tempDir = new File(IOUtil.getTempDir(), "testAtlasImportExport/"
				+ AtlasConfig.ATLASDATA_DIRNAME);
		tempDir.mkdirs();

		File atlasXmlFile = new File(tempDir,
				AtlasConfigEditable.ATLAS_XML_FILENAME);

		AMLExporter amlExporter = new AMLExporter(ace);
		amlExporter.setAtlasXml(atlasXmlFile);
		boolean saved = amlExporter.saveAtlasConfigEditable();

		assertTrue(saved);

		AtlasConfigEditable ace2 = new AMLImportEd().parseAtlasConfig(null,
				atlasXmlFile.getParentFile().getParentFile());
		if (tempDir.exists()) {
			FileUtils.deleteDirectory(tempDir);
		}

		return ace2;
	}

}
