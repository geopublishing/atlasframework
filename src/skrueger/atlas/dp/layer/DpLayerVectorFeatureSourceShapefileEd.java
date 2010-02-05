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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.styling.Style;
import org.geotools.styling.StyledLayer;
import org.opengis.feature.type.GeometryDescriptor;

import schmitzm.geotools.io.GeoImportUtil;
import schmitzm.geotools.styling.StylingUtil;
import schmitzm.io.IOUtil;
import skrueger.atlas.AVUtil;
import skrueger.atlas.AtlasConfig;
import skrueger.atlas.exceptions.AtlasException;
import skrueger.atlas.exceptions.AtlasImportException;
import skrueger.atlas.gui.internal.AtlasStatusDialog;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.AtlasCreator;
import skrueger.creator.GpUtil;
import skrueger.creator.dp.DpEditableInterface;
import skrueger.creator.dp.DpeImportUtil;
import skrueger.geotools.StyledLayerUtil;
import skrueger.sld.ASUtil;

public class DpLayerVectorFeatureSourceShapefileEd extends
		DpLayerVectorFeatureSourceShapefile implements DpEditableInterface {

	static private final Logger LOGGER = Logger
			.getLogger(DpLayerVectorFeatureSourceShapefileEd.class);

	/**
	 * Constructs a new {@link DpLayerVectorFeatureSource} and copies the given
	 * file and its associates to the ad/data/id - dir
	 * 
	 * @param guiInteraction
	 *            Use GUI to ask user or continue with default values
	 * @throws Exception
	 */
	public DpLayerVectorFeatureSourceShapefileEd(AtlasConfig ac, URL url,
			Component owner) throws AtlasImportException {
		super(ac);

		/**
		 * The target directory where all the files go to.
		 */
		File dataDir = null;

		try {

			// The file that has been selected for import will be the
			// "filename".. IF the user accepts any changes to clean the name!
			// Otherwise an AtlasImportException is thrown
			final String name = GpUtil.cleanFilenameWithUI(owner, new File(url
					.toURI()).getName());

			setFilename(name);

			/**
			 * Base name is cities if URL points to cities.gml or cities.shp
			 */
			// final String basename = name.substring(0, name.lastIndexOf("."));
			// LOGGER.debug("Basename is " + basename);

			// Set a directory
			setId(GpUtil.getRandomID("vector"));

			String dirname = getId()
					+ "_"
					+ getFilename()
							.substring(0, getFilename().lastIndexOf('.'));

			// setTitle(new Translation(getAc().getLanguages(), getFilename()));
			// setDesc(new Translation());

			setDataDirname(dirname);

			// Create sub directory to hold data, called the dataDirectory
			dataDir = new File(getAce().getDataDir(), dirname);
			dataDir.mkdirs();
			if (!dataDir.exists())
				throw new IOException("Couldn't create "
						+ dataDir.getAbsolutePath());

			DpeImportUtil.copyFilesWithOrWithoutGUI(this, url, owner, dataDir);

		} catch (Exception e) {
			// In case of any Exception, lets delete the datadir we just
			// created.
			if (dataDir != null) {
				try {
					FileUtils.deleteDirectory(dataDir);
				} catch (IOException e1) {
					LOGGER.error("Deleting the dataDir " + dataDir
							+ " (beacause the import failed) failed:", e);
				}
			}

			if (!(e instanceof AtlasImportException)) {
				LOGGER.error(e);
				throw new AtlasImportException(e);
			} else
				throw (AtlasImportException) e;

		}
	}

	@Override
	public void copyFiles(URL fromUrl, Component owner, File targetDir,
			AtlasStatusDialog status) throws URISyntaxException, IOException,
			TransformerException {

		/**
		 * Getting a DataStore for the VectorLayer
		 */
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("url", fromUrl);
		DataStore dataStore = DataStoreFinder.getDataStore(params);

		try {

			if (dataStore instanceof ShapefileDataStore) {
				ShapefileDataStore shapefileDS = (ShapefileDataStore) dataStore;

				/*******************************************************************
				 * Now copy all the files that belong to ESRI Shapefile
				 ******************************************************************/

				/**
				 * Copy projection-file and deal with upper-case/lower-case
				 * extensions
				 */
				URL prjURL = IOUtil.changeUrlExt(fromUrl, "prj");

				// final File prjFile = new File(prjFilename);
				try {
					AVUtil.copyUrl(prjURL, targetDir, true);
				} catch (FileNotFoundException e) {
					LOGGER.debug(prjURL + " not found, trying with capital '.PRJ'");

					try {
						prjURL = IOUtil.changeUrlExt(fromUrl, "PRJ");

						// Creating a destination File with small ending!
						final String basename = getFilename().substring(0,
								getFilename().lastIndexOf("."));
						AVUtil.copyUrl(prjURL, new File(targetDir, basename
								+ ".prj"), true);
					} catch (FileNotFoundException e2) {
						LOGGER
								.debug("No .prj or .PRJ file for Shapefile found. Asking the user how to proceed:");

						// Ask the user what to do, unless we run in
						// automatic
						// mode
						int importAsDefaultCRS;
						if (owner != null)
							importAsDefaultCRS = JOptionPane
									.showConfirmDialog(
											owner,
											AtlasCreator
													.R(
															"DpVector.Import.NoCRS.QuestionUseDefaultOrCancel",
															GeoImportUtil
																	.getDefaultCRS()
																	.getName()),
											AtlasCreator
													.R("DpVector.Import.NoCRS.Title"),
											JOptionPane.YES_NO_OPTION);
						else
							importAsDefaultCRS = JOptionPane.YES_OPTION;

						if (importAsDefaultCRS == JOptionPane.YES_OPTION) {
							// Force CRS (which creates a .prj and copy it
							shapefileDS.forceSchemaCRS(GeoImportUtil
									.getDefaultCRS());
							prjURL = IOUtil.changeUrlExt(fromUrl, "prj");

							AVUtil.copyUrl(prjURL, targetDir, true);

							/*
							 * Force Schema created a .prj file in the source
							 * folder. If shall be deleted after copy. TODO
							 * There will be a problem if the source is
							 * read-only
							 */
							IOUtil.urlToFile(prjURL).delete();
						} else
							throw (new AtlasException(AtlasCreator
									.R("DpVector.Import.NoCRS.CanceledMsg")));
					}

				}

				/**
				 * Copy main SHP file!
				 */
				final URL shpURL = IOUtil.changeUrlExt(fromUrl, "shp");
				AVUtil.copyUrl(shpURL, targetDir, true);

				final URL shxURL = IOUtil.changeUrlExt(fromUrl, "shx");
				AVUtil.copyURLNoException(shxURL, targetDir, true);

				final URL grxURL = IOUtil.changeUrlExt(fromUrl, "grx");
				AVUtil.copyURLNoException(grxURL, targetDir, true);

				final URL fixURL = IOUtil.changeUrlExt(fromUrl, "fix");
				AVUtil.copyURLNoException(fixURL, targetDir, true);

				final URL qixURL = IOUtil.changeUrlExt(fromUrl, "qix");
				AVUtil.copyURLNoException(qixURL, targetDir, true);

				final URL xmlURL = IOUtil.changeUrlExt(fromUrl, "shp.xml");
				AVUtil.copyURLNoException(xmlURL, targetDir, true);

				final URL dbfURL = IOUtil.changeUrlExt(fromUrl, "dbf");
				AVUtil.copyURLNoException(dbfURL, targetDir, true);

				/**
				 * Optionally copy a .cpg file that describes the
				 */
				final URL cpgURL = IOUtil.changeUrlExt(fromUrl, "cpg");
				AVUtil.copyURLNoException(cpgURL, targetDir, true);

				/**
				 * Try to copy an attached .sld / .SLD files or create a default
				 * Style.
				 * 
				 * 1. Copy the SLD. Check the SLD. If no SLD is provided, then
				 * create a dummy SLD.
				 */
				try {
					try {

						AVUtil.copyUrl(IOUtil.changeUrlExt(fromUrl, "sld"),
								targetDir, true);
					} catch (Exception e) {
						AVUtil.copyUrl(IOUtil.changeUrlExt(fromUrl, "SLD"),
								targetDir, true);
					}
				} catch (Exception e) {

					GeometryDescriptor geometryType;

					geometryType = getDefaultGeometry();
					Style defaultStyle = ASUtil.createDefaultStyle(this);
					File changeFileExt = IOUtil.changeFileExt(new File(
							targetDir + "/" + getFilename()), "sld");
					StylingUtil.saveStyleToSLD(defaultStyle, changeFileExt);

				}
//
////				// TODO is that a good idea? What if we do not have a .prj
////				// file?mmm
////				// then the froced CRS should be returned..
//				crs = shapefileDS.getFeatureSource().getSchema()
//						.getGeometryDescriptor().getCoordinateReferenceSystem();
				
				// Add the empty string as a default NODATA-Value to all textual layers
				StyledLayerUtil.addEmptyStringToAllTextualAttributes(getAttributeMetaDataMap(), getSchema());

			} else {
				throw new AtlasImportException(
						"dataStore was not of type Shapefile, ignoring it for now...");
			}

		} finally {
			if (dataStore != null)
				dataStore.dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see skrueger.creator.dp.DatapoolEditableInterface#getAce()
	 */
	public AtlasConfigEditable getAce() {
		return (AtlasConfigEditable) getAc();
	}

}
