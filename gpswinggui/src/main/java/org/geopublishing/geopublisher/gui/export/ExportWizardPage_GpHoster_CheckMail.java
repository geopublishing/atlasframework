package org.geopublishing.geopublisher.gui.export;

import java.awt.Component;
import java.io.IOException;

import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.geopublisher.export.gphoster.GpHosterClient;
import org.geopublishing.geopublisher.export.gphoster.GpHosterClient.CREATE_USER_RESULT;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.netbeans.spi.wizard.WizardPage;

import de.schmitzm.swing.input.ManualInputOption.PasswordViewable;

public class ExportWizardPage_GpHoster_CheckMail extends WizardPage {
	private final String validationFtpFailedPassword = GeopublisherGUI
			.R("ExportWizard.Ftp.ValidationError_Password");
	JLabel explanationJLabel = new JLabel(GeopublisherGUI.R(
			"ExportWizard.Ftp.CheckMail.Explanation", "XXX"));

	private PasswordViewable pwField;

	/**
	 * The user is created via a rest call when this page is rendered. Whether
	 * this creation was successfull, is stored in this variable.
	 */
	private CREATE_USER_RESULT createdUserWithSuccess = CREATE_USER_RESULT.ERROR;
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
			createdUserWithSuccess = gphc.userCreate(name, email);

			if (createdUserWithSuccess == CREATE_USER_RESULT.ERROR) {
				removeAll();
				add(new JLabel(
						"Could not create user. Please create a user online at hosting.geopublishing.org"),
						"");
			} else if (createdUserWithSuccess == CREATE_USER_RESULT.EXITSALREADY_PWDSENT) {
				add(new JLabel(
						"User already existed. A password reminder has been sent."),
						"", 0);
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

	private PasswordViewable getPWField() {
		if (pwField == null) {
			pwField = new PasswordViewable(
					GeopublisherGUI.R("ExportWizard.FtpExport.Password"));
			pwField.setName(ExportWizard.GPH_PASSWORD);
		}
		return pwField;
	}

	@Override
	protected String validateContents(Component component, Object event) {

		if (createdUserWithSuccess == CREATE_USER_RESULT.ERROR
				|| createdUserWithSuccessEx != null)
			return "Service error. Account could not be created"; // i8n

		if (getPWField().getValue() == null) {
			return validationFtpFailedPassword;
		}

		// Because Schmitzm Password input component is not properly handled by
		// the wizard, we put the password into the wizardmap manually
		if (getPWField().getValue() != null)
			putWizardData(ExportWizard.GPH_PASSWORD,
					String.valueOf(getPWField().getValue()));

		return null;
	}
}
