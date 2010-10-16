/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasViewer.dp.layer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.dp.DpEntryType;
import org.geopublishing.atlasViewer.exceptions.AtlasException;
import org.geopublishing.atlasViewer.swing.AtlasViewerGUI;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.shapefile.ShpFileType;
import org.geotools.data.shapefile.ShpFiles;
import org.geotools.data.shapefile.indexed.ShapeFileIndexer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.io.GeoImportUtil;
import schmitzm.io.IOUtil;
import schmitzm.jfree.feature.style.FeatureChartStyle;
import schmitzm.swing.ExceptionDialog;

/**
 * This extension of the {@link DpLayerVectorFeatureSource} is specialized to
 * read vector data from a ESRI Shapefile.
 * 
 * @author Stefan A. Tzeggai
 * 
 */
public class DpLayerVectorFeatureSourceShapefile extends
		DpLayerVectorFeatureSource {

	private static final Logger LOGGER = Logger
			.getLogger(DpLayerVectorFeatureSourceShapefile.class);

	public DpLayerVectorFeatureSourceShapefile(AtlasConfig ac) {
		super(ac);
	}

	/**
	 * @return a {@link FeatureSource}
	 */
	@Override
	public FeatureSource<SimpleFeatureType, SimpleFeature> getGeoObject() {

		URL localUrl = getUrl(); // TODO
		if (localUrl == null) {
			final AtlasException atlasException = new AtlasException(
					"Could not find ID:" + getId() + " / Title:'" + getTitle()
							+ "' in the resources.");
			setBrokenException(atlasException);
			return null;
		}

		try {

			if (featureSource == null) {
				// First access to the FeatureStore

				if (dataStore == null) {

					checkIndex();

					// First-time access to the DataStore
					final Map<String, Object> map = new HashMap<String, Object>();
					map.put("url", localUrl);

					/**
					 * Force which charset the DBF contains.
					 */
					Charset forceCharset = getCharset();
					map.put(ShapefileDataStoreFactory.DBFCHARSET.key,
							forceCharset);
					map.put("charset", forceCharset);

					// LOGGER.debug("Setting charset to " + forceCharset +
					// " for "
					// + getTitle());

					dataStore = DataStoreFinder.getDataStore(map);

					// TODO By default we take the first SimpleFeatureType. This
					// could/should be extended to be defined in the Atlas.XML
					setTypeName(dataStore.getNames().get(0));
				}

				/**
				 * Reading the FeatureSource
				 */
				featureSource = dataStore.getFeatureSource(getTypeName());

				/**
				 * Determining the CRS and saving it in the DpLayer
				 */
				crs = featureSource.getSchema().getCoordinateReferenceSystem();

				if (crs == null) {
					// boolean deletePrj = false;
					try {
						((ShapefileDataStore) dataStore)
								.forceSchemaCRS(GeoImportUtil.getDefaultCRS());
						// deletePrj = true;
					} catch (FileNotFoundException fe) {
						ExceptionDialog.show(null, fe, "",
								"Unable to set default CRS for Shapefile "
										+ getFilename());
					}
					featureSource = dataStore.getFeatureSource(getTypeName());
					// crs =
					// featureSource.getSchema().getCoordinateReferenceSystem();
					// if (deletePrj) {
					// try {
					// DataUtilities.urlToFile(
					// DataUtilities.changeUrlExt(getUrl(),"prj") ).delete();
					// }
					// catch (Exception e) {
					// ExceptionDialog.show("Deleting a temporary", e);
					// }
					// }
					crs = featureSource.getSchema()
							.getCoordinateReferenceSystem();
				}

				// Cache an Envelope of the BoundingBox of this FeatureSource
				envelope = dataStore.getFeatureSource(getTypeName())
						.getBounds();

				/**
				 * Determine the file type
				 */

				switch (FeatureUtil.getGeometryForm(featureSource.getSchema())) {
				case POINT:
					setType(DpEntryType.VECTOR_SHP_POINT);
					break;
				case LINE:
					setType(DpEntryType.VECTOR_SHP_LINE);
					break;
				case POLYGON:
					setType(DpEntryType.VECTOR_SHP_POLY);
					break;
				}
			}

			return featureSource;

		} catch (Exception e) {
			setBrokenException(e);
			return null;
		}
	}

	/**
	 * Creates a <code>.qix</code> index file for the Shapefile or recreates the
	 * index file, if it's modification time is older than the shapefile's<br>
	 * This method does nothing if running in atlas mode because we can't write
	 * files then, and also does GeoTools create a temporary index itself.
	 */
	private void checkIndex() {
		if (AtlasViewerGUI.isRunning())
			return; // works!

		// not needed any more.. just a double check
		if (getUrl().getProtocol().startsWith("jar"))
			return;

		ShpFiles shpFiles = new ShpFiles(getUrl());
		File qixFile;
		try {
			qixFile = DataUtilities.urlToFile(new URL(shpFiles
					.get(ShpFileType.QIX)));
			File shpFile = DataUtilities.urlToFile(new URL(shpFiles
					.get(ShpFileType.SHP)));

			if (qixFile == null) {
				System.out.println("why is the QIX url null?");
			}

			if (qixFile == null || !qixFile.exists()
					|| shpFile.lastModified() > qixFile.lastModified()) {

				ShapeFileIndexer indexer = new ShapeFileIndexer();
				indexer.setShapeFileName(shpFiles);
				indexer.index(true, null);
			}

		} catch (Exception e) {
			LOGGER.error("Creating a spatial index for " + getFilename()
					+ " failed.", e);
		}
	}

	/**
	 * If a .cpg file is available, GP tries to interpret the charset described
	 * inside. If it fails, the default is returned.
	 */
	@Override
	public Charset getCharset() {

		if (charset == null) {

			charset = GeoImportUtil.readCharset(getUrl());

//			URL[] cpgUrls = GeoImportUtil.getCharsetUrls( getUrl());
//			for (URL cpgUrl : cpgUrls) {
//				try {
//
//					String charsetName = IOUtil.readURLasString(cpgUrl);
//
//					if (charsetName.equals(""))
//						continue;
//					
//					charset = Charset.forName(charsetName);
//					break;
//
//				} catch (Exception e) {
////					 LOGGER.warn("Reading .cpg file failed for "+getFilename()+". Using default. ");
//				}
//			}

			return super.getCharset();
		}

		// LOGGER.debug("charset of "+getId()+" is "+charset);

		return charset;
	}


	@Override
	public DpLayer<FeatureSource<SimpleFeatureType, SimpleFeature>, FeatureChartStyle> copy() {
		DpLayerVectorFeatureSourceShapefile clone = new DpLayerVectorFeatureSourceShapefile(
				ac);

		copyTo(clone);

		return clone;
	}

}
