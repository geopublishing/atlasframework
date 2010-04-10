/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.JDialog;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.apache.log4j.xml.DOMConfigurator;
import org.geopublishing.atlasViewer.AVUtil;
import org.geopublishing.atlasViewer.AVUtil.OSfamiliy;
import org.geopublishing.atlasViewer.dp.AMLImport;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;
import org.geopublishing.atlasViewer.exceptions.AtlasException;
import org.geopublishing.atlasViewer.http.Webserver;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.swing.AtlasMapLegend;
import org.geopublishing.atlasViewer.swing.AtlasViewerGUI;
import org.geotools.data.DataUtilities;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;

import schmitzm.geotools.gui.GeoMapPane;
import schmitzm.io.IOUtil;
import schmitzm.swing.ExceptionDialog;
import skrueger.geotools.MapPaneToolBar;

public class GPTestingUtil extends TestCase {

	/**
	 * Set to <code>true</code> to also run the interactive tests that will not
	 * finish without your GUI input
	 **/
	public static final boolean INTERACTIVE = false;

	/** An enumeration of available test-atlases **/
	public enum Atlas {
		small, iida2
	}

	public static int showHeap() {

		// Get current size of heap in bytes
		long heapSize = Runtime.getRuntime().totalMemory();

		// Get maximum size of heap in bytes. The heap cannot grow beyond this
		// size.
		// Any attempt will result in an OutOfMemoryException.
		long heapMaxSize = Runtime.getRuntime().maxMemory();

		// Get amount of free memory within the heap in bytes. This size will
		// increase
		// after garbage collection and decrease as new objects are created.
		long heapFreeSize = Runtime.getRuntime().freeMemory();

		long used = (heapSize - heapFreeSize);

		int perc = (int) (used * 100. / heapMaxSize);

		return perc;
	}

	public static AtlasConfigEditable getAtlasConfigE(String atlasDir)
			throws FactoryException, TransformException, AtlasException,
			SAXException, IOException, ParserConfigurationException {

		AtlasViewerGUI.setupResLoMan(new String[] { atlasDir });

		/***********************************************************************
		 * Remove the old geopublisher.properties file, so we always start with
		 * the default
		 */
		GPProps.resetProperties(null);

		AtlasConfigEditable atlasConfig = new AtlasConfigEditable(new File(
				atlasDir));

		AVUtil.cacheEPSG();

		Webserver webserver = new Webserver(false);

		AMLImport.parseAtlasConfig(null, atlasConfig, false);

		assertNotNull(atlasConfig);
		assertNotNull(atlasConfig.getMapPool());
		assertNotNull(atlasConfig.getDataPool());

		return atlasConfig;
	}

	public static File getAtlasExportTesttDir() {
		File atlasExportTesttDir = new File(IOUtil.getTempDir(),
				"junitTestAtlasExport");

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
	 * @Deprecated use {@link #getAtlasConfigE(Atlas)} with {@link Atlas.iida2}
	 */
	public static AtlasConfigEditable getAtlasConfigE() throws AtlasException,
			FactoryException, TransformException, SAXException, IOException,
			ParserConfigurationException {
		return getAtlasConfigE(Atlas.small);
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

	public static AtlasConfigEditable getAtlasConfigE(Atlas type)
			throws AtlasException, FactoryException, TransformException,
			SAXException, IOException, ParserConfigurationException {

		try {
			URL log4jURL = AtlasViewerGUI.class.getClassLoader().getResource(
					"/" + "av_log4j.xml");
			System.out.println("Configuring log4j from " + log4jURL);
			if (log4jURL == null) {
				log4jURL = GPTestingUtil.class.getClassLoader().getResource(
						"av_log4j.xml");
				System.out.println("Configuring log4j from " + log4jURL);
			}
			DOMConfigurator.configure(log4jURL);
		} catch (Throwable e) {
			ExceptionDialog.show(null, e);
		}

		System.out.println("Start loading test atlas config ...");
		switch (type) {
		case small:
			String atlasChartDemoAtlasURL = "/atlases/ChartDemoAtlas/atlas.gpa";
			URL resourceURL = GPTestingUtil.class
					.getResource(atlasChartDemoAtlasURL);
			assertNotNull(atlasChartDemoAtlasURL + " not found as resource!",
					resourceURL);
			return getAtlasConfigE(DataUtilities.urlToFile(resourceURL).getParent());
		case iida2:
			if (AVUtil.getOSType() == OSfamiliy.linux) {
				// For Stefan:
				return getAtlasConfigE("/home/stefan/Desktop/GP/Atlanten/IIDA2/IIDA2 Arbeitskopie");
			} else {
				// For Martin:
				return getAtlasConfigE("../../Daten/AndiAtlas_1.2");
			}
		}
		throw new RuntimeException("JUnit testing data not found");
	}

	public static DpLayerVectorFeatureSource getCities() throws AtlasException,
			FactoryException, TransformException, SAXException, IOException,
			ParserConfigurationException {
		return getCities(getAtlasConfigE());
	}

	public static void showJMapPane(org.geotools.swing.JMapPane mapPane)
			throws InterruptedException {
		JDialog dialog = new JDialog();
		mapPane.setMinimumSize(new Dimension(500, 500));
		mapPane.setPreferredSize(new Dimension(500, 500));
		mapPane.setSize(new Dimension(500, 500));
		dialog.setContentPane(mapPane);
		dialog.pack();
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		if (GPTestingUtil.INTERACTIVE) {
			dialog.setModal(true);
			dialog.setVisible(true);
		} else {
			dialog.setVisible(true);
			// Thread.sleep(3000);
		}

		dialog.dispose();
	}

}
