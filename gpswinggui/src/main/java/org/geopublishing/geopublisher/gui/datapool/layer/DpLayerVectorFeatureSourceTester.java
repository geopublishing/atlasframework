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
package org.geopublishing.geopublisher.gui.datapool.layer;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.DpEntryType;
import org.geopublishing.atlasViewer.exceptions.AtlasImportException;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.dp.DpEntryTesterInterface;
import org.geopublishing.geopublisher.dp.DpLayerVectorFeatureSourceShapefileEd;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.shapefile.ShapefileDataStore;

import de.schmitzm.geotools.io.GeoImportUtil;
import de.schmitzm.jfree.feature.style.FeatureChartStyle;
import de.schmitzm.swing.ExceptionDialog;

/**
 * This is not a JUNit test case.. its a class determining whether this can be
 * loaded as a DpLayerVectorFeatureSource
 */
public class DpLayerVectorFeatureSourceTester implements DpEntryTesterInterface {
	static private final Logger LOGGER = Logger
			.getLogger(DpLayerVectorFeatureSourceTester.class);

	DataStore dataStore = null;

	public static final FileFilter FILEFILTER = new FileFilter() {

		@Override
		public boolean accept(File f) {
			return f.isDirectory()
					|| f.getName().toLowerCase().endsWith(".shp")
					|| f.getName().toLowerCase().endsWith(".zip");
		}

		@Override
		public String getDescription() {
			return DpEntryType.VECTOR.getDesc();
		}

	};

	/**
	 * @param url
	 * @return true if the url can be imported as a DpLayerVectorFeatureSource
	 */
	public boolean test(Component owner, URL url) {

		try {
			final Iterator<DataStoreFactorySpi> availableDataStores = DataStoreFinder
					.getAvailableDataStores();
			while (availableDataStores.hasNext()) {
				final DataStoreFactorySpi nextDS = availableDataStores.next();
				// LOGGER.debug("Available DataStores : "
				// + nextDS.getClass().toString());
			}

			if (url.getFile().toLowerCase().endsWith("zip")) {
				// Falls es sich um eine .ZIP datei handelt, wird sie entpackt.
				url = GeoImportUtil.uncompressShapeZip(url);
			}

			Map<Object, Object> params = new HashMap<Object, Object>();
			params.put("url", url);

			// When importing any shapefiles, we do not want to create .fix
			// files automatically. The files could be read only.
			// params.put(ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key,
			// Boolean.FALSE);

			dataStore = DataStoreFinder.getDataStore(params);

			if (dataStore == null) {
				LOGGER.debug("DataStoreFinder failed.");
				return false;
			}

			try {
				/**
				 * We have to ask the user if there exist more than one layer in
				 * the DataStore TODO
				 */
				if (dataStore.getTypeNames().length != 1) {
					JOptionPane.showMessageDialog(owner,
							"Error while importing. File contains no layers.");
					return false;
				}

				if (dataStore instanceof ShapefileDataStore) {
//					ShapefileDataStore shapefileDS = (ShapefileDataStore) dataStore;
					return true;
				}
			} finally {
				dataStore.dispose();
			}

		} catch (IOException e1) {
			ExceptionDialog.show(owner, e1);
		} catch (Exception e) {
			ExceptionDialog.show(owner, e);
		}
		return false;
	}

	/**
	 * @return true if the url can be imported as a DpLayerVectorFeatureSource
	 */
	@Override
	public boolean test(Component owner, File file) {
		try {
			return test(owner, DataUtilities.fileToURL(file));
		} catch (Exception e2) {
			ExceptionDialog.show(owner, e2);
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * skrueger.atlas.datapool.DatapoolEntryTester#create(skrueger.atlas.AtlasConfig
	 * , java.io.File)
	 */
	@Override
	public DpEntry<FeatureChartStyle> create(AtlasConfigEditable ace,
			File file, Component owner) throws AtlasImportException {
		try {
			return new DpLayerVectorFeatureSourceShapefileEd(ace, DataUtilities
					.fileToURL(file), owner);
		} catch (Exception e) {
			throw new AtlasImportException(e);
		}
	}
}
