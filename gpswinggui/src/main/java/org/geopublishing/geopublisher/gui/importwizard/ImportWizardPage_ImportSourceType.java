package org.geopublishing.geopublisher.gui.importwizard;

import java.awt.Component;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.geopublisher.GeopublisherGUI;
import org.netbeans.spi.wizard.WizardPage;


public class ImportWizardPage_ImportSourceType extends WizardPage {
	/*
	 * The short description label that appears on the left side of the wizard
	 */
	JLabel explanationJLabel = new JLabel(GeopublisherGUI
			.R("ImportWizard.ImportSourceType.Explanation"));

	private final String validationImportSourceTypeFailedMsg = GeopublisherGUI
			.R("ImportWizard.ImportSourceType.ValidationError");

	private JLabel explanationFileJLabel = new JLabel(GeopublisherGUI
			.R("ImportWizard.ImportSourceType.Explanation.File"));
	private JRadioButton fileJRadioButton;

	private JLabel explanationGpaJLabel = new JLabel(GeopublisherGUI
			.R("ImportWizard.ImportSourceType.Explanation.GPA"));
	private JRadioButton gpaJRadioButton;

	private ButtonGroup buttonGroup = new ButtonGroup();

	public static String getDescription() {
		return GeopublisherGUI.R("ImportWizard.ImportSourceType");
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
		setLayout(new MigLayout("wrap 1"));
		add(explanationJLabel);
		add(getFileJRadioButton(), "gapy unrelated");
		add(explanationFileJLabel);
		add(getGpaJRadioButton(), "gapy unrelated");
		add(explanationGpaJLabel);
	}

	private JRadioButton getFileJRadioButton() {
		if (fileJRadioButton == null) {
			fileJRadioButton = new JRadioButton(GeopublisherGUI
					.R("ImportWizard.ImportSourceType.File"));
			buttonGroup.add(fileJRadioButton);

			fileJRadioButton.setSelected(true); // TODO It's the default now,
												// but should be replaced with a
												// property
		}
		return fileJRadioButton;
	}

	private JRadioButton getGpaJRadioButton() {
		if (gpaJRadioButton == null) {
			gpaJRadioButton = new JRadioButton(GeopublisherGUI
					.R("ImportWizard.ImportSourceType.Gpa"));
			buttonGroup.add(gpaJRadioButton);
		}
		return gpaJRadioButton;
	}

}
