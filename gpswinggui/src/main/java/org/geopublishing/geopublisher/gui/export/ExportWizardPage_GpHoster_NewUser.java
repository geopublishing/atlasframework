package org.geopublishing.geopublisher.gui.export;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.geopublisher.export.GpHosterServerSettings;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.netbeans.spi.wizard.WizardPage;

import de.schmitzm.net.mail.MailUtil;

public class ExportWizardPage_GpHoster_NewUser extends WizardPage {
	JLabel explanationJLabel = new JLabel(
			GeopublisherGUI.R("ExportWizard.Ftp.New_User.Explanation"));
	JLabel agb = new JLabel(GeopublisherGUI.R("ExportWizard.Ftp.New_User.AGB"));

	JCheckBox acceptAgb;
	JTextField eMailField;
	JTextField usernameField;
	private final String validationFtpFailedAgbNotAccepted = GeopublisherGUI
			.R("ExportWizard.Ftp.New_User.ValidationError_AGB");
	private final String validationFtpFailedNoValidEmail = GeopublisherGUI
			.R("ExportWizard.Ftp.New_User.ValidationError_Email");
	private final String validationFtpFailedNoValidUsername = GeopublisherGUI
			.R("ExportWizard.Ftp.New_User.ValidationError_Username");

	public ExportWizardPage_GpHoster_NewUser() {
	}

	@Override
	protected void renderingPage() {
		removeAll();
		initGui();
	}

	private void initGui() {
		setSize(ExportWizard.DEFAULT_WPANEL_SIZE);
		setPreferredSize(ExportWizard.DEFAULT_WPANEL_SIZE);
		setLayout(new MigLayout("wrap 2"));
		add(explanationJLabel, "span 2");
		add(new JLabel("Username"), "sgx, right");
		add(getUsernameField(), "growx");
		add(new JLabel("Email"), "sgx, right");
		add(getEMailField(), "growx");
		add(getAcceptAgb());
		add(agb);
	}

	private Component getUsernameField() {
		if (usernameField == null) {
			usernameField = new JTextField();
			usernameField.setName(ExportWizard.GPH_USERNAME);
		}
		return usernameField;
	}

	private JCheckBox getAcceptAgb() {
		if (acceptAgb == null) {
			acceptAgb = new JCheckBox();
			acceptAgb.setName(ExportWizard.AGB_ACCEPTED);
		}
		return acceptAgb;
	}

	private JTextField getEMailField() {
		if (eMailField == null) {
			eMailField = new JTextField();
			eMailField.setName(ExportWizard.GPH_EMAIL_FIELD);
		}
		return eMailField;
	}

	@Override
	protected String validateContents(Component component, Object event) {
		// mmm.. should be read from the wiazrd map, sinve the texfield has a
		// name
		// GpHosterClient gphc = (GpHosterClient)
		// getWizardData(ExportWizard.GPHC);
		// if (eMailField != null)
		// gphc.setUserName(eMailField.getText());

		if (acceptAgb != null && !acceptAgb.isSelected()) {
			return validationFtpFailedAgbNotAccepted;
		}
		if (eMailField != null && eMailField.getText().isEmpty()) {
			return validationFtpFailedNoValidEmail;
		}

		if (eMailField != null) {
			if (!MailUtil.EMAIL_ADDRESS_REGEX.matcher(eMailField.getText())
					.find())
				return validationFtpFailedNoValidEmail;
		}
		if (usernameField != null) {
			if (!GpHosterServerSettings.checkString(usernameField.getText()))
				return validationFtpFailedNoValidUsername;
			if (usernameField.getText().length() < 2)
				return validationFtpFailedNoValidUsername;
		}

		return null;
	}

	public static String getDescription() {
		return GeopublisherGUI.R("ExportWizard.Ftp.New_User");
	}
}
