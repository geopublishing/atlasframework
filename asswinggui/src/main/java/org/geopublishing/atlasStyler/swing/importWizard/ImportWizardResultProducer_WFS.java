package org.geopublishing.atlasStyler.swing.importWizard;

import java.io.File;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.swing.AtlasStylerGUI;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.netbeans.spi.wizard.DeferredWizardResult;
import org.netbeans.spi.wizard.ResultProgressHandle;
import org.netbeans.spi.wizard.Summary;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardPage.WizardResultProducer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import de.schmitzm.geotools.JTSUtil;
import de.schmitzm.geotools.io.GeoImportUtil;
import de.schmitzm.geotools.io.GtWfsServerSettings;
import de.schmitzm.geotools.styling.StyledFS;
import de.schmitzm.lang.LangUtil;

public class ImportWizardResultProducer_WFS extends ImportWizardResultProducer
		implements WizardResultProducer {
	protected final static Logger LOGGER = LangUtil
			.createLogger(ImportWizardResultProducer_WFS.class);

	public ImportWizardResultProducer_WFS() {
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
				final GtWfsServerSettings wfsServer = (GtWfsServerSettings) wizardData
						.get(ImportWizard.IMPORT_WFS_URL);
				final String typeName = (String) wizardData
						.get(ImportWizard.IMPORT_WFS_LAYERNAME);

				final AtlasStylerGUI asg = (AtlasStylerGUI) wizardData
						.get(ImportWizard.ATLAS_STYLER_GUI);

				try {
					long startTime = System.currentTimeMillis();

					progress.setBusy(wfsServer.getBaseUrl().getHost());

					// URL url = new URL(
					// "http://localhost:8085/geoserver/ows?service=wfs&version=1.1.0&request=GetCapabilities");

					// final URL url = new URL((String) cfgGui[0]);

					// final Map m = new HashMap();
					// m.put(WFSDataStoreFactory.URL.key, url);
					// m.put(WFSDataStoreFactory.LENIENT.key, new
					// Boolean(true));
					// m.put(WFSDataStoreFactory.TIMEOUT.key,
					// new java.lang.Integer(10000));
					// m.put(WFSDataStoreFactory.MAXFEATURES.key,
					// new java.lang.Integer(ASProps.get(ASProps.Keys.m)));

					final WFSDataStore wfsDs = (new WFSDataStoreFactory())
							.createDataStore(wfsServer);

					try {

						final FeatureSource<SimpleFeatureType, SimpleFeature> wfsFS = wfsDs
								.getFeatureSource(typeName);

						int countFeatures = countFeatures(wfsFS, true);

						String id = wfsServer.getTitle() + " " + typeName;

						{
							ReferencedEnvelope bounds = wfsFS.getBounds();
							LOGGER.debug("Layout Boudns as passed" + bounds);

							ReferencedEnvelope transformEnvelope = JTSUtil
									.transformEnvelope(
											wfsFS.getBounds(Query.ALL),
											GeoImportUtil.getDefaultCRS());
							LOGGER.debug("layer envelope in native WGS: "
									+ transformEnvelope);
						}

						CoordinateReferenceSystem crs = wfsDs
								.getFeatureTypeCRS(typeName);

						final StyledFS wfsSfs = new StyledFS(wfsFS, id);
						wfsSfs.setCRS(crs);

						wfsSfs.setDesc(typeName);
						wfsSfs.setTitle("WFS: " + id);

						String sldFilename = wfsFS.getName().getLocalPart()
								+ ".sld";
						File importedSld = setSldFileAndAskImportIfExists(asg,
								sldFilename, wfsSfs, null);

						final AtomicBoolean added = new AtomicBoolean(false);
						SwingUtilities.invokeAndWait(new Runnable() {

							@Override
							public void run() {
								added.set(asg.addLayer(wfsSfs));
							}
						});

						if (added.get() == false) {
							abort();
							return;
						}

						Summary summary = Summary.create(
								new JScrollPane(getSummaryPanelShapefile(startTime,
										countFeatures, wfsSfs, importedSld, asg)),
								"ok");

						progress.finished(summary);
						asg.addOpenDatastore(id, wfsDs);

					} catch (Exception e) {
						// If an exception occures, we dispose the DS directly.
						wfsDs.dispose();
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
