package org.geopublishing.atlasStyler.swing.importWizard;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AsSwingUtil;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.netbeans.spi.wizard.Summary;
import org.netbeans.spi.wizard.WizardPage.WizardResultProducer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import schmitzm.io.IOUtil;
import schmitzm.swing.SwingUtil;
import skrueger.geotools.StyledFS;

/**
 * This class is using the values collected during the {@link ImportWizard} to
 * export the {@link AtlasConfigEditable}.
 * 
 * 
 * 
 * @author Stefan A. Tzeggai
 */
public abstract class ImportWizardResultProducer implements
		WizardResultProducer {

	/**
	 * @param atlasConfig
	 *            The {@link AtlasConfigEditable} we are importing into
	 */
	public ImportWizardResultProducer() {
	}

	private static final Logger LOGGER = Logger
			.getLogger(ImportWizardResultProducer.class);

	@Override
	public boolean cancel(Map settings) {
		return true;
	}

	File setSldFileAndAskImportIfExists(Component owner, String sldFileName,
			StyledFS dbSfs, File sldFile) {
		File importedSldFile = null;

		String sldDir;
		if (sldFile == null) {
			sldDir = System.getProperty("user.home");
			sldFile = new File(sldDir + "/" + sldFileName);
		} else {
			sldDir = sldFile.getParentFile().getAbsolutePath();
		}
		dbSfs.setSldFile(sldFile);

		if (sldFile.exists()) {

			boolean askYesNo = SwingUtil.askYesNo(
					owner,
					ASUtil.R("ImportWizard.ImportExistingSLDQuestion",
							IOUtil.escapePath(sldFileName),
							IOUtil.escapePath(sldDir)));

			if (askYesNo == true) {
				dbSfs.loadStyle();
				importedSldFile = sldFile;
			}
		}

		// Handle if .SLD exists instead
		File SLDfile = IOUtil.changeFileExt(sldFile, "SLD");
		if (owner != null && !sldFile.exists() && SLDfile.exists()) {
			AVSwingUtil
					.showMessageDialog(
							owner,
							IOUtil.escapePath(SLDfile.getAbsolutePath())
									+ " exits.\nChange the ending to .sld to associate it with the layer. It will not be imported."); // i8n
		}

		return importedSldFile;
	}

	protected JPanel getErrorPanel(Exception e) {
		JPanel panel = new JPanel(new MigLayout("wrap 1"));

		String errorMsg = e.getLocalizedMessage();

		if (errorMsg == null)
			errorMsg = e.getMessage();

		if (errorMsg == null)
			errorMsg = e.getClass().getSimpleName();

		LOGGER.error("Import failed: ", e);

		panel.add(new JLabel(AsSwingUtil.R(
				"ImportWizard.ImportHasBeenAbortedWithError", errorMsg)));

		return panel;
	}

	protected Summary getAbortSummary() {
		JPanel aborted = new JPanel(new MigLayout());
		aborted.add(new JLabel(AsSwingUtil
				.R("ImportWizard.ImportHasBeenAbortedByTheUser")));

		return Summary.create(aborted, "abort");
	}

	/**
	 * Creates a report about an imported FeaatureSource
	 * 
	 * @param startTime
	 * @param countFeatures
	 * @param dbSfs
	 * @param importedSld
	 *            <code>null</code> if no .sldd has been impoerted.
	 */
	JPanel getSummaryPanel(long startTime, int countFeatures,
			final StyledFS dbSfs, File importedSld) {
		JPanel summaryPanel = new JPanel(new MigLayout("wrap 1"));

		summaryPanel.add(new JLabel(AsSwingUtil
				.R("ImportWizard.ImportWasSuccessfull")));

		LOGGER.debug("Count features from PG " + dbSfs.getTitle().toString()
				+ " took " + (System.currentTimeMillis() - startTime) + "ms");

		// summaryPanel.add(new JLabel("Features: " + countFeatures
		// + (countFeatures == -1 ? " => query not supported" : "")));
		summaryPanel.add(new JLabel(ASUtil.R(
				"ImportWizard.SummaryText.NumerOfFeatureImported",
				countFeatures, (countFeatures == -1 ? " => query not supported"
						: ""))));

		GeometryForm geometryForm = FeatureUtil.getGeometryForm(dbSfs
				.getFeatureSource());
		String geometryFormString = (geometryForm == GeometryForm.ANY ? ASUtil
				.R("ImportWizard.SummaryText.TypeOfGeometry.ANYmakesProblems")
				: geometryForm.toString());
		summaryPanel
				.add(new JLabel(ASUtil.R(
						"ImportWizard.SummaryText.TypeOfGeometry",
						geometryFormString)));

		// i8n
		summaryPanel.add(new JLabel("CRS: "
				+ dbSfs.getSchema().getCoordinateReferenceSystem().getName()
						.getCode()));

		if (importedSld == null) {
			summaryPanel.add(new JLabel(ASUtil
					.R("ImportWizard.SummaryText.DefaultStyleHasBeenApplied")));
		} else {
			summaryPanel
					.add(new JLabel(
							ASUtil.R(
									"ImportWizard.SummaryText.LoadedStyleFromFileAndAppliedSuccessfully",
									IOUtil.escapePath(importedSld
											.getAbsolutePath()))));
		}
		return summaryPanel;
	}

	int countFeatures(FeatureSource<SimpleFeatureType, SimpleFeature> wfsFS,
			boolean hard) throws IOException {
		int countFeatures = 0;
		if (hard)
			countFeatures = wfsFS.getFeatures(Query.FIDS).size();
		else
			countFeatures = wfsFS.getCount(Query.FIDS);
		if (countFeatures == 0) {
			throw new IllegalStateException(
					ASUtil.R("ImportWizard.Error.LayerContainsNoFeatures"));
		}
		return countFeatures;

	}

}
