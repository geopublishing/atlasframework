package org.geopublishing.atlasStyler.swing.importWizard;

import java.io.File;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.swing.AtlasStylerGUI;
import org.netbeans.spi.wizard.DeferredWizardResult;
import org.netbeans.spi.wizard.ResultProgressHandle;
import org.netbeans.spi.wizard.Summary;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardPage.WizardResultProducer;

public class ImportWizardResultProducer_FILE extends ImportWizardResultProducer
		implements WizardResultProducer {

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
				try {
					progress.setBusy(importFile.getName());

					boolean added = asg
							.addShapeLayer(new File(selectedFilePath));

					if (added == false) {
						abort();
						return;
					}
					
					JPanel summaryPanel = new JPanel(new MigLayout("wrap 1"));
					summaryPanel.add(new JLabel(ASUtil
							.R("ImportWizard.ImportWasSuccessfull")));

					Summary summary = Summary.create(new JScrollPane(
							summaryPanel), "ok");

					progress.finished(summary);
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
