package skrueger.creator.gui.importwizard;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.netbeans.spi.wizard.WizardPage;

import skrueger.atlas.AtlasConfig;
import skrueger.atlas.resource.icons.Icons;
import skrueger.creator.AtlasCreator;
import skrueger.creator.AtlasXMLFileFilter;
import skrueger.creator.GPProps;
import skrueger.creator.GPProps.Keys;

public class ImportWizardPage_GPA_SelectFolder extends WizardPage {
	/*
	 * The short description label that appears on the left side of the wizard
	 */
	static final String desc = AtlasCreator
			.R("ImportWizard.GPA.FolderSelection");
	JLabel explanationJLabel = new JLabel(AtlasCreator
			.R("ImportWizard.GPA.FolderSelection.Explanation"));

	private static final String validationImportSourceTypeFailedMsg_NotExists = AtlasCreator
			.R("ImportWizard.GPA.FolderSelection.ValidationError.NotExists");

	private static final String validationImportSourceTypeFailedMsg_CantRead = AtlasCreator
			.R("ImportWizard.GPA.FolderSelection.ValidationError.CantRead");

	private static final String validationImportSourceTypeFailedMsg_NotGpa = AtlasCreator
			.R("ImportWizard.GPA.FolderSelection.ValidationError.NotGPA");

	private JButton folderChooserJButton;
	JTextField folderJTextField;
	final static private JLabel folderTextFieldJLabel = new JLabel(AtlasCreator
			.R("ImportWizard.Folder.FolderTextBoxLabel"));

	public static String getDescription() {
		return desc;
	}

	public ImportWizardPage_GPA_SelectFolder() {
		initGui();
	}

	@Override
	protected String validateContents(final Component component,
			final Object event) {

		final String absPath = getFolderJTextField().getText();
		final File gpa = new File(absPath);

		if (!gpa.exists()) {
			return validationImportSourceTypeFailedMsg_NotExists;
		}

		if (!gpa.canRead()) {
			return validationImportSourceTypeFailedMsg_CantRead;
		}

		if (!new AtlasXMLFileFilter().accept(gpa)
				|| !AtlasConfig.isAtlasDir(gpa.getParentFile())) {
			return validationImportSourceTypeFailedMsg_NotGpa;
		}

		// Check the we are not import from ourself...

		return null;
	}

	private void initGui() {
		// setSize(ImportWizard.DEFAULT_WPANEL_SIZE);
		// setPreferredSize(ImportWizard.DEFAULT_WPANEL_SIZE);

		setLayout(new MigLayout("wrap 1"));

		add(explanationJLabel);
		add(folderTextFieldJLabel, "gapy unrelated");
		add(getFolderJTextField(), "growx, split 2, left");
		add(getFolderChooserJButton(), "right");
	}

	private JTextField getFolderJTextField() {
		if (folderJTextField == null) {
			folderJTextField = new JTextField(GPProps.get(
					GPProps.Keys.LAST_IMPORTED_GPA, ""));

			folderJTextField.setName(ImportWizard.IMPORT_GPA_FOLDER);
		}

		return folderJTextField;
	}

	public JButton getFolderChooserJButton() {
		if (folderChooserJButton == null) {
			folderChooserJButton = new JButton(new AbstractAction("",
					Icons.ICON_SEARCH) {

				@Override
				public void actionPerformed(final ActionEvent e) {

					final String pathname = GPProps.get(Keys.LAST_IMPORTED_GPA,
							"");
					final JFileChooser dc = new JFileChooser(new File(pathname));
					dc.addChoosableFileFilter(new AtlasXMLFileFilter());
					dc.setFileSelectionMode(JFileChooser.FILES_ONLY);
					dc.setAcceptAllFileFilterUsed(false);

					dc.setDialogTitle(AtlasCreator
							.R("ImportWizard.GPA.WhereFrom.DialogTitle"));
					dc.setMultiSelectionEnabled(false);

					if ((dc
							.showOpenDialog(ImportWizardPage_GPA_SelectFolder.this) != JFileChooser.APPROVE_OPTION)
							|| dc.getSelectedFile() == null) {
						return;
					}

					final File importGpaAtlasXML = dc.getSelectedFile();

					if (importGpaAtlasXML == null)
						return;

					getFolderJTextField().setText(
							importGpaAtlasXML.getAbsolutePath());

					/*
					 * Radically store the path into the properties now - it
					 * really sucks to select that path all the time
					 */
					GPProps.set(Keys.LAST_IMPORTED_GPA, importGpaAtlasXML
							.getAbsolutePath());
					GPProps.store();

				}

			});

		}

		return folderChooserJButton;
	}

}
