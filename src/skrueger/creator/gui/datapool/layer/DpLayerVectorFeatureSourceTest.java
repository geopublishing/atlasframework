/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the Geopublisher application - An authoring tool to facilitate the publication and distribution of geoproducts in form of online and/or offline end-user GIS.
 * http://www.geopublishing.org
 * 
 * Geopublisher is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
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

import org.apache.log4j.Logger;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.shapefile.ShapefileDataStore;

import schmitzm.swing.ExceptionDialog;
import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.dp.layer.DpLayerVectorFeatureSourceShapefileEd;
import skrueger.atlas.exceptions.AtlasImportException;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.dp.DpEntryTesterInterface;

/**
 * This is not a JUNit test case.. its a class determining whether this can be
 * loaded as a DpLayerVectorFeatureSource
 */
public class DpLayerVectorFeatureSourceTest implements DpEntryTesterInterface {
	static private final Logger LOGGER = Logger
			.getLogger(DpLayerVectorFeatureSourceTest.class);

	DataStore dataStore = null;

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
