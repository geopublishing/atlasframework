package org.geopublishing.atlasStyler.swing.importWizard;

import java.io.File;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasStyler.ASUtil;
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

import schmitzm.geotools.JTSUtil;
import schmitzm.geotools.io.GeoImportUtil;
import skrueger.geotools.StyledFS;
import skrueger.geotools.io.WfsServerSettings;

public class ImportWizardResultProducer_WFS extends ImportWizardResultProducer
		implements WizardResultProducer {

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
				final WfsServerSettings wfsServer = (WfsServerSettings) wizardData
						.get(ImportWizard.IMPORT_WFS_URL);
				final String typeName = (String) wizardData
						.get(ImportWizard.IMPORT_WFS_LAYERNAME);

				final AtlasStylerGUI asg = (AtlasStylerGUI) wizardData
						.get(ImportWizard.ATLAS_STYLER_GUI);

				try {
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

						String id = wfsServer.getTitle() + " " + typeName;

						File sldFile = new File(System.getProperty("user.home")
								+ "/" + wfsFS.getName().getLocalPart() + ".sld");

						{
							ReferencedEnvelope bounds = wfsFS.getBounds();
							System.out.println("Layout Boudns as passed"
									+ bounds);

							ReferencedEnvelope transformEnvelope = JTSUtil
									.transformEnvelope(
											wfsFS.getBounds(Query.ALL),
											GeoImportUtil.getDefaultCRS());
							System.out.println("layer envelope in native WGS: "
									+ transformEnvelope);
						}

						final StyledFS wfsSfs = new StyledFS(wfsFS, sldFile, id);

						CoordinateReferenceSystem crs = wfsDs
								.getFeatureTypeCRS(typeName);
						wfsSfs.setCRS(crs);

						wfsSfs.setDesc(typeName);
						wfsSfs.setTitle("WFS: " + id);

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

						JPanel summaryPanel = new JPanel(
								new MigLayout("wrap 1"));

						summaryPanel.add(new JLabel(ASUtil
								.R("ImportWizard.ImportWasSuccessfull")));

						Summary summary = Summary.create(new JScrollPane(
								summaryPanel), "ok");

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
