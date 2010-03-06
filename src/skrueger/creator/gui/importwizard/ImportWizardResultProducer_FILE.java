package skrueger.creator.gui.importwizard;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import org.netbeans.spi.wizard.DeferredWizardResult;
import org.netbeans.spi.wizard.ResultProgressHandle;
import org.netbeans.spi.wizard.Summary;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardPage.WizardResultProducer;

import skrueger.atlas.dp.DpEntry;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.AtlasCreator;
import skrueger.creator.GPDialogManager;
import skrueger.creator.dp.DpEntryFactory;
import skrueger.creator.exceptions.AtlasImportCancelledException;

public class ImportWizardResultProducer_FILE extends ImportWizardResultProducer
		implements WizardResultProducer {

	public ImportWizardResultProducer_FILE(AtlasConfigEditable atlasConfig) {
		super(atlasConfig);
	}

	@Override
	public Object finish(Map wizardData) throws WizardException {

		// Read stuff from the wizard map
		final String selectedFilePath = (String) wizardData
				.get(ImportWizard.IMPORT_FILE);

		final File importFile = new File(selectedFilePath);

		final Component owner = (Component) wizardData
				.get(ImportWizard.GUI_OWNER_COMPONENT);

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
					final DpEntry created = DpEntryFactory.create(
							atlasConfigEditable, importFile, owner); // Null

					if (created == null)
						abort();

					atlasConfigEditable.getDataPool().pushQuite();
					atlasConfigEditable.getDataPool().add(created);
					// not
					// cool!

					JPanel summaryPanel = new JPanel(new MigLayout("wrap 1"));
					summaryPanel.add(new JLabel(AtlasCreator
							.R("ImportWizard.ImportWasSuccessfull")));

					summaryPanel.add(new JLabel(
							"Sie können die Ebene nun bearbeiten:"),
							"gapy unrel, split 2");
					summaryPanel.add(new JButton(new AbstractAction(
							AtlasCreator
									.R("DataPoolWindow_Action_EditDPE_label")) {

						@Override
						public void actionPerformed(ActionEvent e) {
							GPDialogManager.dm_EditDpEntry.getInstanceFor(
									created, owner);
						}

					}));

					Summary summary = Summary.create(new JScrollPane(
							summaryPanel), "ok"); // i8n

					atlasConfigEditable.getDataPool().popQuite();
					progress.finished(summary);
				} catch (AtlasImportCancelledException e) {
					abort();
				} catch (Exception e) {
					progress
							.finished(Summary.create(getErrorPanel(e), "error"));
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
