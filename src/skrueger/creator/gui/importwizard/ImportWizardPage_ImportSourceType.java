package skrueger.creator.gui.importwizard;

import java.awt.Component;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import net.miginfocom.swing.MigLayout;

import org.netbeans.spi.wizard.WizardPage;

import skrueger.creator.AtlasCreator;

public class ImportWizardPage_ImportSourceType extends WizardPage {
	/*
	 * The short description label that appears on the left side of the wizard
	 */
	static final String desc = AtlasCreator.R("ImportWizard.ImportSourceType");
	JLabel explanationJLabel = new JLabel(AtlasCreator
			.R("ImportWizard.ImportSourceType.Explanation"));

	private static final String validationImportSourceTypeFailedMsg = AtlasCreator
			.R("ImportWizard.ImportSourceType.ValidationError");

	JLabel explanationFileJLabel = new JLabel(AtlasCreator
			.R("ImportWizard.ImportSourceType.Explanation.File"));
	JRadioButton fileJRadioButton;

	JLabel explanationGpaJLabel = new JLabel(AtlasCreator
			.R("ImportWizard.ImportSourceType.Explanation.GPA"));
	JRadioButton gpaJRadioButton;

	ButtonGroup buttonGroup = new ButtonGroup();

	public static String getDescription() {
		return desc;
	}

	public ImportWizardPage_ImportSourceType() {
		initGui();
	}

	@Override
	protected String validateContents(final Component component,
			final Object event) {
		if (buttonGroup.getSelection() == null)
			return validationImportSourceTypeFailedMsg;

		// Store the selection in the wizard data
		if (gpaJRadioButton.isSelected()) {
			getWizardDataMap().put(ImportWizard.IMPORT_SOURCE_TYPE,
					ImportWizard.SOURCETYPE.gpa);
		} else if (fileJRadioButton.isSelected()) {
			getWizardDataMap().put(ImportWizard.IMPORT_SOURCE_TYPE,
					ImportWizard.SOURCETYPE.file);
		}

		return null;
	}

	private void initGui() {
//		setSize(ImportWizard.DEFAULT_WPANEL_SIZE);
//		setPreferredSize(ImportWizard.DEFAULT_WPANEL_SIZE);

		setLayout(new MigLayout("wrap 1"));
		add(explanationJLabel);
		add(getFileJRadioButton(), "gapy unrelated");
		add(explanationFileJLabel);
		add(getGpaJRadioButton(), "gapy unrelated");
		add(explanationGpaJLabel);

	}

	private JRadioButton getFileJRadioButton() {
		if (fileJRadioButton == null) {
			fileJRadioButton = new JRadioButton(AtlasCreator
					.R("ImportWizard.ImportSourceType.File"));
			buttonGroup.add(fileJRadioButton);
			// fileJRadioButton.setName(ImportWizard.DISK_CHECKBOX);
			// fileJRadioButton.setSelected(GPProps.getBoolean(Keys.LastImportDisk));
		}
		return fileJRadioButton;
	}

	private JRadioButton getGpaJRadioButton() {
		if (gpaJRadioButton == null) {
			gpaJRadioButton = new JRadioButton(AtlasCreator
					.R("ImportWizard.ImportSourceType.Gpa"));
			buttonGroup.add(gpaJRadioButton);
			// gpaJRadioButton.setName(ImportWizard.JWS_CHECKBOX);
			// gpaJRadioButton.setSelected(GPProps.getBoolean(Keys.LastImportJWS));

		}
		return gpaJRadioButton;
	}

}
