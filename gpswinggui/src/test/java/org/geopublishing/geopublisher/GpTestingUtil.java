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
import org.geopublishing.atlasViewer.AVUtil;
import org.geopublishing.atlasViewer.AtlasConfig;
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

import schmitzm.geotools.GTUtil;
import schmitzm.geotools.gui.GeoMapPane;
import schmitzm.io.IOUtil;
import skrueger.geotools.MapPaneToolBar;

public class GpTestingUtil {

	/** An enumeration of available test-atlases **/
	public enum TestAtlas {
		// TODO Create atype "new" that creates a new atlas in tmp dir
		small("/atlases/ChartDemoAtlas/atlas.gpa"), rasters(
				"/atlases/rastersAtlas/atlas.gpa");

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
				return getAtlasConfigE(DataUtilities.urlToFile(getUrl())
						.getParent());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public URL getUrl() {
			return GpTestingUtil.class.getResource(getReslocation());
		}

		public File getFile() {
			return DataUtilities.urlToFile(getUrl());
		}
	}

	public static AtlasConfigEditable getAtlasConfigE(String atlasDir)
			throws FactoryException, TransformException, AtlasException,
			SAXException, IOException, ParserConfigurationException {

		AVUtil.initAtlasLogging();

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
		// switch (type) {
		// case iida2: {
		// if (SystemUtils.IS_OS_LINUX) {
		// // For Stefan:
		// return
		// getAtlasConfigE("/home/stefan/Desktop/GP/Atlanten/IIDA2/IIDA2 Arbeitskopie");
		// } else {
		// // For Martin:
		// return getAtlasConfigE("../../Daten/AndiAtlas_1.2");
		// }
		// }
		// }
		// throw new RuntimeException("JUnit testing data not found");
	}

	public static DpLayerVectorFeatureSource getCities() throws AtlasException,
			FactoryException, TransformException, SAXException, IOException,
			ParserConfigurationException {
		return getCities(getAtlasConfigE());
	}

	//
	// public static void showJMapPane(org.geotools.swing.JMapPane mapPane)
	// throws InterruptedException {
	// JDialog dialog = new JDialog();
	// mapPane.setMinimumSize(new Dimension(500, 500));
	// mapPane.setPreferredSize(new Dimension(500, 500));
	// mapPane.setSize(new Dimension(500, 500));
	// dialog.setContentPane(mapPane);
	// dialog.pack();
	// dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	//
	// TestingUtil.testGui(dialog);
	//
	// dialog.dispose();
	// }

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

		FileUtils.deleteDirectory(tempDir);

		return ace2;
	}

}
