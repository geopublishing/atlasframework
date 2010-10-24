package org.geopublishing.atlasStyler.swing.importWizard;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasStyler.ASProps;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.swing.OpenDataFileChooser;
import org.geopublishing.atlasViewer.swing.Icons;
import org.netbeans.spi.wizard.WizardPage;

public class ImportWizardPage_FILE_Select extends WizardPage {
	/*
	 * The short description label that appears on the left side of the wizard
	 */
	JLabel explanationJLabel = new JLabel(
			ASUtil.R("ImportWizard.FILE.FileSelection.Explanation"));

	private final String validationImportSourceTypeFailedMsg_NotExists = ASUtil
			.R("ImportWizard.FILE.FileSelection.ValidationError.NotExists");

	private final String validationImportSourceTypeFailedMsg_CantRead = ASUtil
			.R("ImportWizard.FILE.FileSelection.ValidationError.CantRead");

	private final String validationImportSourceTypeFailedMsg_NotImportable = ASUtil
			.R("ImportWizard.FILE.FileSelection.ValidationError.NotImportable");

	private JButton fileChooserJButton;
	JTextField fileJTextField;
	final static private JLabel fileTextFieldJLabel = new JLabel(
			ASUtil.R("ImportWizard.FILE.FileTextBoxLabel"));

	public static String getDescription() {
		return ASUtil.R("ImportWizard.FILE.FileSelection");
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
		
		// TODO: Check whether importable
//		if (!DpEntryFactory.test(file, ImportWizardPage_FILE_Select.this)) {
//			return validationImportSourceTypeFailedMsg_NotImportable;
//		}

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
			fileJTextField = new JTextField(ASProps.get(
					ASProps.Keys.LAST_IMPORTED_FILE, ""));

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

					File getLastOpenDir = null;
					String lastFilePath = ASProps
							.get(ASProps.Keys.lastImportDirectory);
					if (lastFilePath != null)
						getLastOpenDir = new File(lastFilePath);

					OpenDataFileChooser chooser = new OpenDataFileChooser(
							getLastOpenDir);

					// properties
					chooser.setVisible(true);
					int result = chooser
							.showOpenDialog(ImportWizardPage_FILE_Select.this);

					final File selectedFile = chooser.getSelectedFile();

					if (selectedFile == null
							|| result != JFileChooser.APPROVE_OPTION)
						return;

					ASProps.set(ASProps.Keys.lastImportDirectory,
							selectedFile.getAbsolutePath());
					
					 getFileJTextField().setText(
							 selectedFile.getAbsolutePath());
					
					 /*
					 * Radically store the path into the properties now - it
					 * really sucks to select that path all the time
					 */
					 ASProps.set(org.geopublishing.atlasStyler.ASProps.Keys.LAST_IMPORTED_FILE,
					 selectedFile.getAbsolutePath());
					 ASProps.store();
//
//					AtlasSwingWorker<Void> openFileWorker = new AtlasSwingWorker<Void>(
//							ImportWizardPage_FILE_Select.this) {
//
//						@Override
//						protected Void doInBackground() throws IOException,
//								InterruptedException {
//							AtlasStylerGUI asg = (AtlasStylerGUI) getWizardData(ImportWizard.ATLAS_STYLER_GUI);
//							addShapeLayer(selectedFile);
//							return null;
//						}
//
//					};
//					try {
//						openFileWorker.executeModal();
//					} catch (CancellationException e1) {
//						e1.printStackTrace();
//					} catch (InterruptedException e1) {
//						e1.printStackTrace();
//					} catch (ExecutionException e1) {
//						e1.printStackTrace();
//					}

					//
					// final String pathname = ASProps.get(
					// Keys.LAST_IMPORTED_FILE, "");
					// final JFileChooser dc = new JFileChooser(new
					// File(pathname));
					//
					// dc.addChoosableFileFilter(DpLayerVectorFeatureSourceTester.FILEFILTER);
					// dc.addChoosableFileFilter(DpLayerRasterTester.FILEFILTER);
					// dc.addChoosableFileFilter(DpMediaPDFTester.FILEFILTER);
					// dc.addChoosableFileFilter(DpEntryFactory.FILEFILTER_ALL_DPE_IMPORTABLE);
					//
					// dc.setFileSelectionMode(JFileChooser.FILES_ONLY);
					// dc.setAcceptAllFileFilterUsed(false);
					//
					// dc.setDialogTitle(ASUtil
					// .R("ImportWizard.FILE.WhereFrom.DialogTitle"));
					// dc.setMultiSelectionEnabled(false);
					//
					// if ((dc.showOpenDialog(ImportWizardPage_FILE_Select.this)
					// != JFileChooser.APPROVE_OPTION)
					// || dc.getSelectedFile() == null) {
					// return;
					// }
					//
					// final File importFileAtlasXML = dc.getSelectedFile();
					//
					// if (importFileAtlasXML == null)
					// return;
					//
					// getFileJTextField().setText(
					// importFileAtlasXML.getAbsolutePath());
					//
					// /*
					// * Radically store the path into the properties now - it
					// * really sucks to select that path all the time
					// */
					// ASProps.set(Keys.LAST_IMPORTED_FILE,
					// importFileAtlasXML.getAbsolutePath());
					// ASProps.store();

				}

			});

		}

		return fileChooserJButton;
	}

}
