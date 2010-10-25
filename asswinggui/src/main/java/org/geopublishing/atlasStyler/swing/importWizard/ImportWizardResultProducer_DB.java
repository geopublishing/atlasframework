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
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.jdbc.JDBCDataStore;
import org.netbeans.spi.wizard.DeferredWizardResult;
import org.netbeans.spi.wizard.ResultProgressHandle;
import org.netbeans.spi.wizard.Summary;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardPage.WizardResultProducer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import skrueger.geotools.StyledFS;
import skrueger.geotools.io.DbServerSettings;

public class ImportWizardResultProducer_DB extends ImportWizardResultProducer
		implements WizardResultProducer {

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

						String id = dbServer.getTitle() + " " + typeName;

						File sldFile = new File(System.getProperty("user.home")
								+ "/" + dbFS.getName().getLocalPart() + ".sld");

						final StyledFS dbSfs = new StyledFS(dbFS, sldFile, id);

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

						JPanel summaryPanel = new JPanel(
								new MigLayout("wrap 1"));

						summaryPanel.add(new JLabel(ASUtil
								.R("ImportWizard.ImportWasSuccessfull")));

						Summary summary = Summary.create(new JScrollPane(
								summaryPanel), "ok");

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
