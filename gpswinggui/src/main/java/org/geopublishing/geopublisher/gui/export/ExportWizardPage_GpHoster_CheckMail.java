package org.geopublishing.geopublisher.gui.export;

import java.awt.Component;
import java.io.IOException;

import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.geopublisher.export.gphoster.GpHosterClient;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.netbeans.spi.wizard.WizardPage;

import de.schmitzm.swing.input.ManualInputOption.Password;

public class ExportWizardPage_GpHoster_CheckMail extends WizardPage {
	private final String validationFtpFailedPassword = GeopublisherGUI
			.R("ExportWizard.Ftp.ValidationError_Password");
	JLabel explanationJLabel = new JLabel(GeopublisherGUI.R(
			"ExportWizard.Ftp.CheckMail.Explanation", "XXX"));

	private Password PWField;

	/**
	 * The user is created via a rest call when this page is rendered. Whether
	 * this creation was successfull, is stored in this variable.
	 */
	private boolean createdUserWithSuccess = false;
	private IOException createdUserWithSuccessEx = null;

	public static String getDescription() {
		return GeopublisherGUI.R("ExportWizard.Ftp.CheckMail");
	}

	public ExportWizardPage_GpHoster_CheckMail() {
		// send mail
		initGui();
	}

	@Override
	protected void renderingPage() {

		GpHosterClient gphc = (GpHosterClient) getWizardData(ExportWizard.GPHC);
		String name = (String) getWizardData(ExportWizard.GPH_USERNAME);
		String email = (String) getWizardData(ExportWizard.GPH_EMAIL_FIELD);
		try {
			createdUserWithSuccess = gphc.userCreate(email, email);

			if (createdUserWithSuccess == false) {
				removeAll();
				add(new JLabel(
						"Could not create user. Please create a user online at hosting.geopublishing.org"),
						"");
			}

		} catch (IOException e) {
			createdUserWithSuccessEx = e;
			removeAll();
			add(new JLabel(
					"Could not create user. Please create a user online at hosting.geopublishing.org: "
							+ e.getLocalizedMessage()), "");

		}

	}

	private void initGui() {
		setSize(ExportWizard.DEFAULT_WPANEL_SIZE);
		setPreferredSize(ExportWizard.DEFAULT_WPANEL_SIZE);
		setLayout(new MigLayout("wrap 1"));
		add(explanationJLabel);
		add(getPWField(), "growx");
	}

	private Password getPWField() {
		if (PWField == null) {
			PWField = new Password(
					GeopublisherGUI.R("ExportWizard.FtpExport.Password"));
			PWField.setName(ExportWizard.GPH_PASSWORD);
		}
		return PWField;
	}

	@Override
	protected String validateContents(Component component, Object event) {

		if (!createdUserWithSuccess || createdUserWithSuccessEx != null)
			return "Service error. Account could not be created"; // i8n

		if (getPWField().getValue() == null) {
			return validationFtpFailedPassword;
		}

		// Because Schmitzm Password input component is not properly handled by
		// the wizard, we put the password into the wizardmap manually
		if (getPWField().getValue() != null)
			putWizardData(ExportWizard.GPH_PASSWORD, getPWField().getValue()
					.toString());

		return null;
	}
}
