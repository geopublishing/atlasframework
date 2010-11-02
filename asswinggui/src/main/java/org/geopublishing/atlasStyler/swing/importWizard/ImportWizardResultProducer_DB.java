package org.geopublishing.atlasStyler.swing.importWizard;

import java.io.File;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.swing.AtlasStylerGUI;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.jdbc.JDBCDataStore;
import org.netbeans.spi.wizard.DeferredWizardResult;
import org.netbeans.spi.wizard.ResultProgressHandle;
import org.netbeans.spi.wizard.Summary;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardPage.WizardResultProducer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import skrueger.geotools.StyledFS;
import skrueger.geotools.io.DbServerSettings;

public class ImportWizardResultProducer_DB extends ImportWizardResultProducer
		implements WizardResultProducer {

	/** Logger for debug messages. */
	protected static final Logger LOGGER = Logger
			.getLogger(ImportWizardResultProducer_DB.class);

	public ImportWizardResultProducer_DB() {
		super();
	}

	@Override
	public Object finish(Map wizardData) throws WizardException {

		/**
		 * Start the export as a DeferredWizardResult
		 */
		DeferredWizardResult result = new DeferredWizardResult(true) {

			private ResultProgressHandle progress;

			@Override
			public void start(Map wizardData, ResultProgressHandle progress) {
				this.progress = progress;

				// Read stuff from the wizard map
				final DbServerSettings dbServer = (DbServerSettings) wizardData
						.get(ImportWizard.IMPORT_DB);

				final String typeName = (String) wizardData
						.get(ImportWizard.IMPORT_DB_LAYERNAME);

				final AtlasStylerGUI asg = (AtlasStylerGUI) wizardData
						.get(ImportWizard.ATLAS_STYLER_GUI);

				try {
					progress.setBusy(dbServer.toString());

					final JDBCDataStore dbDs = (JDBCDataStore) DataStoreFinder
							.getDataStore(dbServer);
					try {

						final FeatureSource<SimpleFeatureType, SimpleFeature> dbFS = dbDs
								.getFeatureSource(typeName);

						// Tests against a table without a geometry.
						if (FeatureUtil.getGeometryForm(dbFS) == GeometryForm.NONE) {
							throw new IllegalStateException(
									ASUtil.R("ImportWizard.Error.AbortedBecauseNotGeometryColumnExits"));
						}

						if (dbFS.getSchema().getCoordinateReferenceSystem() == null) {
							throw new IllegalStateException(
									ASUtil.R("ImportWizard.Error.AbortedBecauseNoCrsDefined"));
						}

						long start = System.currentTimeMillis();

						int countFeatures = dbFS.getCount(Query.FIDS);
						if (countFeatures == 0) {
							throw new IllegalStateException(
									"The layer contains no features. AtlasStyler needs at least one feature."); // i8n
						}

						String id = dbServer.getTitle() + " " + typeName;

						String sldFileName = dbFS.getName().getLocalPart()
								+ ".sld";

						final StyledFS dbSfs = new StyledFS(dbFS, id);

						File importedSldFile = setSldFileAndAskImportIfExists(
								asg, sldFileName, dbSfs, null);

						dbSfs.setDesc(typeName);
						dbSfs.setTitle("DB: " + id);

						final AtomicBoolean added = new AtomicBoolean(false);
						SwingUtilities.invokeAndWait(new Runnable() {

							@Override
							public void run() {
								added.set(asg.addLayer(dbSfs));
							}
						});

						if (added.get() == false) {
							abort();
							return;
						}

						Summary summary = Summary
								.create(new JScrollPane(getSummaryPanel(start,
										countFeatures, dbSfs, importedSldFile)),
										"ok");

						progress.finished(summary);
						asg.addOpenDatastore(dbSfs.getId(), dbDs);

					} catch (Exception e) {
						// If an exception occures, we dispose the DS directly.
						dbDs.dispose();
						throw e;
					}
				} catch (Exception e) {
					progress.finished(Summary.create(getErrorPanel(e), "error"));
				}
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
