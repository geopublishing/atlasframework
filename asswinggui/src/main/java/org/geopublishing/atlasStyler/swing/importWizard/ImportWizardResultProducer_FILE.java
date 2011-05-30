package org.geopublishing.atlasStyler.swing.importWizard;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.swing.AtlasStylerGUI;
import org.geopublishing.atlasViewer.exceptions.AtlasImportException;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStore;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.netbeans.spi.wizard.DeferredWizardResult;
import org.netbeans.spi.wizard.ResultProgressHandle;
import org.netbeans.spi.wizard.Summary;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardPage.WizardResultProducer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import de.schmitzm.geotools.io.GeoImportUtil;
import de.schmitzm.geotools.styling.StyledFS;
import de.schmitzm.geotools.styling.StyledGridCoverageReader;
import de.schmitzm.io.IOUtil;
import de.schmitzm.swing.SwingUtil;

public class ImportWizardResultProducer_FILE extends ImportWizardResultProducer
		implements WizardResultProducer {

	final static Logger log = Logger
			.getLogger(ImportWizardResultProducer_FILE.class);

	public ImportWizardResultProducer_FILE() {
		super();
	}

	@Override
	public Object finish(Map wizardData) throws WizardException {

		// Read stuff from the wizard map
		final String selectedFilePath = (String) wizardData
				.get(ImportWizard.IMPORT_FILE);

		final AtlasStylerGUI asg = (AtlasStylerGUI) wizardData
				.get(ImportWizard.ATLAS_STYLER_GUI);

		final File importFile = new File(selectedFilePath);

		/**
		 * Start the export as a DeferredWizardResult
		 */
		DeferredWizardResult result = new DeferredWizardResult(true) {

			private ResultProgressHandle progress;

			@Override
			public void start(Map wizardData, ResultProgressHandle progress) {
				this.progress = progress;

				long startTime = System.currentTimeMillis();
				progress.setBusy(importFile.getName());
				SwingUtil.checkNotOnEDT();
				Summary summary = null;

				try {

					final String fileExtension = IOUtil.getFileExt(importFile,
							false).toLowerCase();

					if (Arrays.asList(ASUtil.FILTER_SHAPE.getExtensions())
							.contains(fileExtension)) {
						// Import Shape
						summary = importShape(asg, importFile, progress,
								startTime);
					} else if (Arrays.asList(
							ASUtil.FILTER_RASTERSUPPORTED.getExtensions())
							.contains(fileExtension)) {
						summary = importRaster(asg, importFile, progress,
								startTime);
					}

					if (summary != null)
						progress.finished(summary);

				} catch (Exception e) {
					progress.finished(Summary.create(getErrorPanel(e), "error"));
				}
			}

			private Summary importRaster(AtlasStylerGUI asg, File importFile,
					ResultProgressHandle progress, long startTime)
					throws DataSourceException {

				GeoTiffReader reader = new GeoTiffReader(
						IOUtil.fileToURL(importFile));

				final StyledGridCoverageReader styledReader = new StyledGridCoverageReader(
						reader, importFile.toString(), importFile.getName(),
						null);

				File sldFile = IOUtil.changeFileExt(importFile, "sld");

				File importedSld = setSldFileAndAskImportIfExists(asg, IOUtil
						.changeFileExt(importFile, "sld").getName(),
						styledReader, sldFile);

				boolean added = asg.addLayer(styledReader);

				if (added == false) {
					abort();
					return null;
				}

				// TODO Better Raster import feedback, like x and y and BANDS! i8n
				return Summary.create(new JScrollPane(new JLabel(
						"Rasterimport OK")), "ok");
			}

			/**
			 * Import a Shapefile selected by the Wizard into the AtlasStylerGUI
			 */
			private Summary importShape(final AtlasStylerGUI asg,
					File importFile, ResultProgressHandle progress,
					long startTime) throws FileNotFoundException, IOException {
				URL urlToShape;

				if (importFile.getName().toLowerCase().endsWith("zip")) {
					urlToShape = GeoImportUtil.uncompressShapeZip(importFile);
					importFile = DataUtilities.urlToFile(urlToShape);
				} else {
					urlToShape = DataUtilities.fileToURL(importFile);
				}

				Map<Object, Object> params = new HashMap<Object, Object>();
				params.put("url", urlToShape);

				final File fixFile = IOUtil.changeFileExt(importFile, "fix");
				if (fixFile.canWrite()) {
					if (fixFile.delete())
						log.info("Deleted existing " + fixFile
								+ ", so that it will be regenerated.");
				}

				final File qixFile = IOUtil.changeFileExt(importFile, "qix");
				if (qixFile.canWrite()) {
					if (qixFile.delete()) {
						log.info("Deleted existing " + fixFile
								+ ", so that it will be regenerated.");
					}
				}

				/*
				 * Test whether we have write permissions to create any .fix
				 * file
				 */
				if (!IOUtil.canWriteOrCreate(qixFile)
						|| !IOUtil.canWriteOrCreate(fixFile)) {
					// If the file is not writable, we shall not try to
					// create
					// an index. Even if the file already exists, it
					// could
					// be
					// that the index has to be regenerated.
					params.put(
							ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key,
							Boolean.FALSE);
				}

				ShapefileDataStore dataStore = (ShapefileDataStore) DataStoreFinder
						.getDataStore(params);

				if (dataStore == null)
					throw new RuntimeException(
							"Could not read as ShapefileDataStore: "
									+ importFile.getAbsolutePath());

				Charset stringCharset = GeoImportUtil.readCharset(urlToShape);
				if (stringCharset != null)
					dataStore.setStringCharset(stringCharset);

				// test for any .prj file
				CoordinateReferenceSystem prjCRS = null;
				File prjFile = IOUtil.changeFileExt(importFile, "prj");
				if (prjFile.exists()) {
					try {
						prjCRS = GeoImportUtil.readProjectionFile(prjFile);
					} catch (Exception e) {
						prjCRS = null;
						if (!AVSwingUtil
								.askOKCancel(
										asg,
										ASUtil
												.R("AtlasStylerGUI.importShapePrjBrokenWillCreateDefaultFor",
														e.getMessage(),
														prjFile.getName(),
														GeoImportUtil
																.getDefaultCRS()
																.getName()))) {
							dataStore.dispose();
							abort();
							return null;
						}
					}
				} else {
					if (!AVSwingUtil
							.askOKCancel(
									asg,
									ASUtil
											.R("AtlasStylerGUI.importShapePrjNotFoundWillCreateDefaultFor",
													prjFile.getName(),
													GeoImportUtil
															.getDefaultCRS()
															.getName()))) {
						dataStore.dispose();
						abort();
						return null;
					}
				}

				if (prjCRS == null) {
					dataStore.forceSchemaCRS(GeoImportUtil.getDefaultCRS());
				}

				/**
				 * Check for broken/old .qix index file and try to recreate it.
				 */
				if (GeoImportUtil.isOldBrokenQix(dataStore)) {
					try {
						log.info(IOUtil.escapePath(urlToShape)
								+ " has a broken .qix file. Trying to recreate the quad tree...");
						IndexedShapefileDataStore idxShpDs = (IndexedShapefileDataStore) dataStore;
						// idxShpDs.dispose();
						// dataStore = (ShapefileDataStore)
						// DataStoreFinder
						// .getDataStore(params);
						// idxShpDs = (IndexedShapefileDataStore)
						// dataStore;
						try {
							idxShpDs.createSpatialIndex();
						} catch (Exception e) {
							throw new IllegalStateException(
									"The Shapefile has a broken .qix file and a new one can not be created. Please delete or fix the .qix file manually.", //i8n
									e);
						}
						throw new AtlasImportException(
								"The Shapefile had a broken .qix file which has now been repaired. Please reimport the Shapefile."); //i8n

					} finally {
						dataStore.dispose();
					}
				}

				// After optionally forcing the CRS we get the FS
				FeatureSource<SimpleFeatureType, SimpleFeature> fs = dataStore
						.getFeatureSource(dataStore.getTypeNames()[0]);

				int countFeatures = countFeatures(fs, true);

				String id = urlToShape.toString();
				StyledFS styledFS = new StyledFS(fs, id);

				styledFS.setTitle(importFile.getName());
				styledFS.setDesc("");

				File sldFile = IOUtil.changeFileExt(importFile, "sld");

				File importedSld = setSldFileAndAskImportIfExists(asg, IOUtil
						.changeFileExt(importFile, "sld").getName(), styledFS,
						sldFile);

				asg.addOpenDatastore(styledFS.getId(), dataStore);

				boolean added = asg.addLayer(styledFS);

				if (added == false) {
					abort();
					return null;
				}

				return Summary.create(
						new JScrollPane(getSummaryPanelShapefile(startTime,
								countFeatures, styledFS, importedSld)), "ok");

			}

			/**
			 * If the user aborts the export, we tell it to JarImportUtil
			 * instance
			 */
			@Override
			public void abort() {
				// jarImportUtil.abort();
				progress.finished(getAbortSummary());
			};
		};

		return result;
	}

}
