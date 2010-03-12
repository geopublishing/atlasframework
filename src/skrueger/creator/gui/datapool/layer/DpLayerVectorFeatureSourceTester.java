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
package skrueger.creator.gui.datapool.layer;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.shapefile.ShapefileDataStore;

import schmitzm.swing.ExceptionDialog;
import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.dp.DpEntryType;
import skrueger.atlas.dp.layer.DpLayerVectorFeatureSourceShapefileEd;
import skrueger.atlas.exceptions.AtlasImportException;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.dp.DpEntryTesterInterface;

/**
 * This is not a JUNit test case.. its a class determining whether this can be
 * loaded as a DpLayerVectorFeatureSource
 */
public class DpLayerVectorFeatureSourceTester implements DpEntryTesterInterface {
	static private final Logger LOGGER = Logger
			.getLogger(DpLayerVectorFeatureSourceTester.class);

	DataStore dataStore = null;
	
	public static final FileFilter FILEFILTER = new FileFilter () {

		@Override
		public boolean accept(File f) {
			return f.isDirectory() || f.getName().toLowerCase().endsWith(".shp");
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
				LOGGER.debug("Available DataStores : "
						+ nextDS.getClass().toString());
			}

			Map<Object, Object> params = new HashMap<Object, Object>();
			params.put("url", url);
			dataStore = DataStoreFinder.getDataStore(params);

			if (dataStore == null) {
				LOGGER.debug("DataStoreFinder failed.");
				return false;
			}

			/**
			 * We have to ask the user if there exist more than one layer in the
			 * DataStore TODO
			 */
			if (dataStore.getTypeNames().length != 1) {
				JOptionPane.showMessageDialog(owner,
						"Error while importing. Maybe no .prj file attached?");
				dataStore.dispose();
				return false;
			}

			if (dataStore instanceof ShapefileDataStore) {
				ShapefileDataStore shapefileDS = (ShapefileDataStore) dataStore;
				shapefileDS.dispose();
				return true;
			}

		} catch (IOException e1) {
			// datastore not disposed?!
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
			return test(owner, file.toURI().toURL());
		} catch (MalformedURLException e2) {
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
	public DpEntry create(AtlasConfigEditable ace, File file, Component owner) throws AtlasImportException {
		try {
			return new DpLayerVectorFeatureSourceShapefileEd(ace, file.toURI()
					.toURL(), owner);
		} catch (MalformedURLException e) {
			throw new AtlasImportException(e);
		}
	}
}
