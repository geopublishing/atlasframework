/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the AtlasViewer application - A GIS viewer application targeting at end-users with no GIS-experience. Its main purpose is to present the atlases created with the Geopublisher application.
 * http://www.geopublishing.org
 * 
 * AtlasViewer is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.atlas.dp.layer;

import java.awt.Component;
import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.shapefile.ShpFileType;
import org.geotools.data.shapefile.ShpFiles;
import org.geotools.data.shapefile.indexed.ShapeFileIndexer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.io.IOUtil;
import schmitzm.jfree.feature.style.FeatureChartStyle;
import skrueger.atlas.AtlasConfig;
import skrueger.atlas.AtlasViewer;
import skrueger.atlas.dp.DpEntryType;
import skrueger.atlas.exceptions.AtlasException;

/**
 * This extension of the {@link DpLayerVectorFeatureSource} is specialized to
 * read vector data from a ESRI Shapefile.
 * 
 * @author Stefan A. Krüger
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
		// long startT = new Date().getTime();

		URL localUrl = getUrl((Component) null); // TODO
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

				if (dataAccess == null) {

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

					dataAccess = DataStoreFinder.getDataStore(map);

					// TODO By default we take the first SimpleFeatureType. This
					// could/should be extended to be defined in the Atlas.XML
					setTypeName(dataAccess.getNames().get(0));
				}

				/**
				 * Reading the FeatureSource
				 */
				featureSource = dataAccess.getFeatureSource(getTypeName());

				/**
				 * Determining the CRS and saving it in the DpLayer
				 */
				crs = featureSource.getSchema().getCoordinateReferenceSystem();

				// Cache an Envelope of the BoundingBox of this FeatureSource
				envelope = dataAccess.getFeatureSource(getTypeName())
						.getBounds();

				/**
				 * Determine the file type
				 */
				// if (dataAccess instanceof IndexedShapefileDataStore) {
				// IndexedShapefileDataStore iSfDS = (IndexedShapefileDataStore)
				// dataAccess;
				// setType(iSfDS.isIndexed() ? DpEntryType.VECTOR_SHP);
				// }

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
		} finally {
			// System.out.println( new Date().getTime() - startT+ "ms");
		}
	}

	/**
	 * Creates a <code>.qix</code> index file for the Shapefile or recreates the
	 * index file, if it's modification time is older than the shapefile's<br>
	 * This method does nothing if running in atlas mode because we can't write
	 * files then, and also does GeoTools create a temporary index itself.
	 */
	private void checkIndex() {
		if (AtlasViewer.isRunning())
			return; // works!

		// not needed any more.. just a double check
		if (getUrl((Component) null).getProtocol().startsWith("jar"))
			return;

		ShpFiles shpFiles = new ShpFiles(getUrl((Component) null));
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

			try {
				URL cpgUrl = getCharsetUrl();

				// LOGGER.debug("Reading Charset from " + cpgUrl);

				String charsetName = IOUtil.readURLasString(cpgUrl);

				if (charsetName == "")
					return super.getCharset();
				//
				// LOGGER.info("Trying to interprete '" + charsetName
				// + "' as a charset.");

				charset = Charset.forName(charsetName);

			} catch (Exception e) {
				LOGGER.warn("Reading .cpg file failed for "+getFilename()+". Using default. ", e);
				return super.getCharset();
			}
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