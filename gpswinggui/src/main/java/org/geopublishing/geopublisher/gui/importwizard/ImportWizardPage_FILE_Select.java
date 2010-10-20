package org.geopublishing.geopublisher.gui.importwizard;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasViewer.swing.Icons;
import org.geopublishing.geopublisher.GPProps;
import org.geopublishing.geopublisher.GPProps.Keys;
import org.geopublishing.geopublisher.dp.DpEntryFactory;
import org.geopublishing.geopublisher.dp.DpLayerRasterTester;
import org.geopublishing.geopublisher.dp.media.DpMediaPDFTester;
import org.geopublishing.geopublisher.gui.datapool.layer.DpLayerVectorFeatureSourceTester;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.netbeans.spi.wizard.WizardPage;



public class ImportWizardPage_FILE_Select extends WizardPage {
	/*
	 * The short description label that appears on the left side of the wizard
	 */
	JLabel explanationJLabel = new JLabel(GeopublisherGUI
			.R("ImportWizard.FILE.FileSelection.Explanation"));

	private final String validationImportSourceTypeFailedMsg_NotExists = GeopublisherGUI
			.R("ImportWizard.FILE.FileSelection.ValidationError.NotExists");

	private final String validationImportSourceTypeFailedMsg_CantRead = GeopublisherGUI
			.R("ImportWizard.FILE.FileSelection.ValidationError.CantRead");

	private final String validationImportSourceTypeFailedMsg_NotImportable = GeopublisherGUI
			.R("ImportWizard.FILE.FileSelection.ValidationError.NotImportable");

	private JButton fileChooserJButton;
	JTextField fileJTextField;
	final static private JLabel fileTextFieldJLabel = new JLabel(GeopublisherGUI
			.R("ImportWizard.FILE.FileTextBoxLabel"));

	public static String getDescription() {
		return GeopublisherGUI.R("ImportWizard.FILE.FileSelection");
	}

	public ImportWizardPage_FILE_Select() {
		initGui();
	}

	@Override
	protected String validateContents(final Component component,
			final Object event) {

		final String absPath = getFileJTextField().getText();
		final File file = new File(absPath);

		if (!file.exists()) {
			return validationImportSourceTypeFailedMsg_NotExists;
		}

		if (!file.canRead()) {
			return validationImportSourceTypeFailedMsg_CantRead;
		}

		if (!DpEntryFactory.test(file, ImportWizardPage_FILE_Select.this)) {
			return validationImportSourceTypeFailedMsg_NotImportable;
		}

		// Check the we are not import from ourself...

		return null;
	}

	private void initGui() {
		// setSize(ImportWizard.DEFAULT_WPANEL_SIZE);
		// setPreferredSize(ImportWizard.DEFAULT_WPANEL_SIZE);

		setLayout(new MigLayout("wrap 1"));

		add(explanationJLabel);
		add(fileTextFieldJLabel, "gapy unrelated");
		add(getFileChooserJButton(), "split 2");
		add(getFileJTextField(), "growx, left");
	}

	private JTextField getFileJTextField() {
		if (fileJTextField == null) {
			fileJTextField = new JTextField(GPProps.get(
					GPProps.Keys.LAST_IMPORTED_FILE, ""));

			fileJTextField.setName(ImportWizard.IMPORT_FILE);
		}

		return fileJTextField;
	}

	public JButton getFileChooserJButton() {
		if (fileChooserJButton == null) {
			fileChooserJButton = new JButton(new AbstractAction("",
					Icons.ICON_SEARCH) {

				@Override
				public void actionPerformed(final ActionEvent e) {

					final String pathname = GPProps.get(
							Keys.LAST_IMPORTED_FILE, "");
					final JFileChooser dc = new JFileChooser(new File(pathname));

					dc
							.addChoosableFileFilter(DpLayerVectorFeatureSourceTester.FILEFILTER);
					dc.addChoosableFileFilter(DpLayerRasterTester.FILEFILTER);
					dc.addChoosableFileFilter(DpMediaPDFTester.FILEFILTER);
					dc
							.addChoosableFileFilter(DpEntryFactory.FILEFILTER_ALL_DPE_IMPORTABLE);

					dc.setFileSelectionMode(JFileChooser.FILES_ONLY);
					dc.setAcceptAllFileFilterUsed(false);

					dc.setDialogTitle(GeopublisherGUI
							.R("ImportWizard.FILE.WhereFrom.DialogTitle"));
					dc.setMultiSelectionEnabled(false);

					if ((dc.showOpenDialog(ImportWizardPage_FILE_Select.this) != JFileChooser.APPROVE_OPTION)
							|| dc.getSelectedFile() == null) {
						return;
					}

					final File importFileAtlasXML = dc.getSelectedFile();

					if (importFileAtlasXML == null)
						return;

					getFileJTextField().setText(
							importFileAtlasXML.getAbsolutePath());

					/*
					 * Radically store the path into the properties now - it
					 * really sucks to select that path all the time
					 */
					GPProps.set(Keys.LAST_IMPORTED_FILE, importFileAtlasXML
							.getAbsolutePath());
					GPProps.store();

				}

			});

		}

		return fileChooserJButton;
	}

}
