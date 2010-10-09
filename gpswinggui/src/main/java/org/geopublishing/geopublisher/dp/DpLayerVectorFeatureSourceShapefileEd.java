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
package org.geopublishing.geopublisher.dp;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasViewer.AVUtil;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.AtlasStatusDialogInterface;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSourceShapefile;
import org.geopublishing.atlasViewer.exceptions.AtlasImportException;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.DpEditableInterface;
import org.geopublishing.geopublisher.GpUtil;
import org.geopublishing.geopublisher.swing.GpSwingUtil;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.NameImpl;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;

import com.sun.org.omg.CORBA.AttributeDescription;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.io.GeoImportUtil;
import schmitzm.geotools.styling.StylingUtil;
import schmitzm.io.IOUtil;
import schmitzm.lang.LangUtil;
import skrueger.AttributeMetadataImpl;
import skrueger.geotools.StyledLayerUtil;
import skrueger.geotools.io.GeoImportUtilURL;
import skrueger.i8n.Translation;

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
			if (url.getFile().toLowerCase().endsWith("zip")) {
				// Falls es sich um eine .ZIP datei handelt, wird sie entpackt.
				url = GeoImportUtil.uncompressShapeZip(url);
			}

			// The file that has been selected for import will be the
			// "filename".. IF the user accepts any changes to clean the name!
			// Otherwise an AtlasImportException is thrown
			final String name = GpSwingUtil.cleanFilenameWithUI(owner,
					new File(url.toURI()).getName());
			setFilename(name);

			// Check for "illegal" attribute names.
			List<String> illegalAtts = checkAttributeNames(url);
			if (illegalAtts.size() > 0) {
				String illegalAttsString = LangUtil.stringConcatWithSep(", ",
						illegalAtts.toArray());
				if (!AVSwingUtil.askOKCancel(owner, GpUtil.R(
						"DbfAttributeNames.Illegal.ProceedQuestion",
						illegalAttsString))) {
					throw new AtlasImportException(GpUtil.R(
							"DbfAttributeNames.Illegal.ProceedQuestion.Denied",
							illegalAttsString));
				}
			}

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

			parseGeocommonsReadme(DataUtilities.extendURL(
					DataUtilities.getParentUrl(url), "README"), 1);

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

	/**
	 * Checks whether any of the attribute names do not follow the DBF standard.
	 * 
	 * @throws IOException
	 */
	static List<String> checkAttributeNames(URL url) throws IOException {
		ArrayList<String> illegalAttributeNames = new ArrayList<String>();

		ShapefileDataStore store = new ShapefileDataStore(url);
		SimpleFeatureType schema2 = store.getSchema();
		for (int aindex = 0; aindex < schema2.getAttributeCount(); aindex++) {
			AttributeDescriptor ad = schema2.getDescriptor(aindex);
			String localName = ad.getLocalName();
			boolean ok = FeatureUtil.checkAttributeNameRestrictions(localName);
			if (!ok)
				illegalAttributeNames.add(aindex + ":" + localName);
		}

		return illegalAttributeNames;
	}

	@Override
	public void copyFiles(URL urlToShape, Component owner, File targetDir,
			AtlasStatusDialogInterface status) throws URISyntaxException,
			IOException, TransformerException {
		//
		// if (urlToShape.getFile().toLowerCase().endsWith("zip")) {
		// // Falls es sich um eine .ZIP datei handelt, wird sie entpackt.
		// urlToShape = GeoImportUtil.uncompressShapeZip(urlToShape
		// .openStream());
		// }

		/**
		 * Getting a DataStore for the VectorLayer
		 */
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("url", urlToShape);
		DataStore dataStore = DataStoreFinder.getDataStore(params);

		try {

			if (dataStore instanceof ShapefileDataStore) {
				// ShapefileDataStore shapefileDS = (ShapefileDataStore)
				// dataStore;

				/*******************************************************************
				 * Now copy all the files that belong to ESRI Shapefile
				 ******************************************************************/

				/**
				 * Copy projection-file and deal with upper-case/lower-case
				 * extensions
				 */
				URL prjURL = IOUtil.changeUrlExt(urlToShape, "prj");

				// final File prjFile = new File(prjFilename);
				try {
					IOUtil.copyUrl(prjURL, targetDir, true);
				} catch (FileNotFoundException e) {
					LOGGER.debug(prjURL
							+ " not found, trying with capital '.PRJ'");

					try {
						prjURL = IOUtil.changeUrlExt(urlToShape, "PRJ");

						// Creating a destination File with small ending!
						final String basename = getFilename().substring(0,
								getFilename().lastIndexOf("."));
						IOUtil.copyUrl(prjURL, new File(targetDir, basename
								+ ".prj"), true);
					} catch (FileNotFoundException e2) {
						LOGGER.debug("No .prj or .PRJ file for Shapefile found.");

						// Ask the user what to do, unless we run in
						// automatic
						// mode
						// int importAsDefaultCRS;

						if (status != null) {
							// We have a modal atlas status dialog open. Just
							// write a warning to the status dialog.
							status.warningOccurred(getFilename(), "", GpUtil.R(
									"DpVector.Import.NoCRS.WarningMsg",
									GeoImportUtil.getDefaultCRS().getName()));
							// importAsDefaultCRS = JOptionPane.YES_OPTION;
						}
					}
				}

				/**
				 * Copy main SHP file!
				 */
				final URL shpURL = IOUtil.changeUrlExt(urlToShape, "shp");
				IOUtil.copyUrl(shpURL, targetDir, true);

				final URL shxURL = IOUtil.changeUrlExt(urlToShape, "shx");
				IOUtil.copyURLNoException(shxURL, targetDir, true);

				final URL grxURL = IOUtil.changeUrlExt(urlToShape, "grx");
				IOUtil.copyURLNoException(grxURL, targetDir, true);

				final URL fixURL = IOUtil.changeUrlExt(urlToShape, "fix");
				IOUtil.copyURLNoException(fixURL, targetDir, true);

				final URL qixURL = IOUtil.changeUrlExt(urlToShape, "qix");
				IOUtil.copyURLNoException(qixURL, targetDir, true);

				final URL xmlURL = IOUtil.changeUrlExt(urlToShape, "shp.xml");
				IOUtil.copyURLNoException(xmlURL, targetDir, true);

				final URL dbfURL = IOUtil.changeUrlExt(urlToShape, "dbf");
				IOUtil.copyURLNoException(dbfURL, targetDir, true);

				/**
				 * Optionally copy a .cpg file that describes the
				 */
				final URL cpgURL = IOUtil.changeUrlExt(urlToShape, "cpg");
				IOUtil.copyURLNoException(cpgURL, targetDir, true);

				/**
				 * Try to copy an attached .sld / .SLD files or create a default
				 * Style.
				 * 
				 * 1. Copy the SLD. Check the SLD. If no SLD is provided, then
				 * create a dummy SLD.
				 */
				try {
					try {

						IOUtil.copyUrl(IOUtil.changeUrlExt(urlToShape, "sld"),
								targetDir, true);
					} catch (Exception e) {
						IOUtil.copyUrl(IOUtil.changeUrlExt(urlToShape, "SLD"),
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

				parseGeocommonsReadme(DataUtilities.extendURL(
						DataUtilities.getParentUrl(urlToShape), "README"), 2);

				// Add the empty string as a default NODATA-Value to all textual
				// layers
				StyledLayerUtil.addEmptyStringToAllTextualAttributes(
						getAttributeMetaDataMap(), getSchema());

			} else {
				throw new AtlasImportException(
						"dataStore was not of type Shapefile, ignoring it for now...");
			}

		} finally {
			if (dataStore != null)
				dataStore.dispose();
		}
	}

	/**
	 * Tries to find and parse a README at the given {@link URL}. The README is
	 * expected to be in the un-offical GeoCommons README format.<br/>
	 * <br/>
	 * <code>
FEC, Individual donations to Obama campaign by county, USA, Oct 2008

The poly shapefile shows individual campaign donations geocoded and then aggregated to county level. There were more than 236 thousand individual donation records that were geocoded based on the monthly Campaign Finance report submitted to Federal Election Commission (FEC) by Obama campaign on 20th Oct, 2008.  

Attributes:
NAME: County name - County Name
Sum_NSofar: Total so far (Jan to Sept 2008) in the county  - Total so far (Jan to Sept 2008) in the county for the same set of donors who donated in Sert 08.
Big caveat: these totals reflect summation of only records from the month of Sept 08. For eg., if someone in county x donated in Aug 08. 
Sum_NQtr: Total donations for Sept, 2008 - Total of individual donation by county during the month of Sept, 2008
Count_: Count of individual donation records - Total number of individual donation records by county
FIPS: FIPS - FIPS code

exported on Tue Nov 10 19:54:53 -0500 2009
</code> or <code>

satxu's places



A Map by Satxu




Attributes:
name0: name - 
descripti0: description - 

exported on Wed Apr 28 23:51:34 -0400 2010
</code>
	 * 
	 * @param schema
	 * @param phase
	 *            1 or 2. 1 is before the shapefile has been copied into the
	 *            internal folder, 2 is after. In the first phase, the title and
	 *            description are set according the the first lines in the
	 *            readme. In phase 2, the attributes descriptors are set into
	 *            the AMD map.
	 */
	private void parseGeocommonsReadme(URL geocommonsReadmeURL, int phase) {
		try {
			InputStream openStream = geocommonsReadmeURL.openStream();
			try {
				InputStreamReader inStream = new InputStreamReader(openStream);
				try {
					BufferedReader inReader = new BufferedReader(inStream);
					try {

						String title = "";
						while (title.trim().isEmpty()) {
							title = inReader.readLine();
						}
						if (phase == 1)
							setTitle(new Translation(title));

						String desc = "";
						String oneLine = "";
						while (!oneLine.startsWith("Attributes:")) {
							oneLine = inReader.readLine();
							if (oneLine != null)
								oneLine = oneLine.trim();
							if (!oneLine.isEmpty()
									&& !oneLine.startsWith("Attributes:"))
								desc += oneLine + " ";
						}
						if (phase == 1)
							setDesc(new Translation(desc));

						if (phase != 2)
							return;
						String attLine = "";
						while (attLine != null) {
							attLine = inReader.readLine();
							attLine = attLine.trim();

							if (attLine.contains(":")) {
								String[] split = attLine.split(":");
								String attName = split[0].trim();
								String attDesc = split[1].trim();

								// Sometimes the attdesc contains the same
								// information twice... filter that
								try {
									if (attDesc.contains(" - ")) {
										String[] split2 = attDesc.split(" - ");
										if (split2[0].trim().equals(
												split2[1].trim()))
											attDesc = split2[0].trim();
									}
								} catch (Exception e) {
									LOGGER.warn(
											"While parsing GeoCommons README",
											e);
								}

								NameImpl findBestMatchingAttribute = FeatureUtil
										.findBestMatchingAttribute(getSchema(),
												attName);
								if (findBestMatchingAttribute == null)
									continue;
								AttributeMetadataImpl amd = getAttributeMetaDataMap()
										.get(findBestMatchingAttribute);
								if (amd != null) {
									amd.setTitle(new Translation(attDesc));
								}

							}

						}

					} finally {
						inReader.close();
					}
				} finally {
					inStream.close();
				}
			} finally {
				openStream.close();
			}
		} catch (Exception e) {
			if (e instanceof FileNotFoundException
					&& e.getMessage().contains("README")) {
				// NOthing bad... Just not a Geocommons file.
			} else {
				LOGGER.warn("Parsing GeoComons README failed, probably not in GeoCommons format");

			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see skrueger.creator.dp.DatapoolEditableInterface#getAce()
	 */
	public AtlasConfigEditable getAce() {
		return (AtlasConfigEditable) getAtlasConfig();
	}

}
