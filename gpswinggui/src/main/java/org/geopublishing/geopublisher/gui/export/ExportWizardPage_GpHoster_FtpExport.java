package org.geopublishing.geopublisher.gui.export;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GPProps;
import org.geopublishing.geopublisher.export.gphoster.GpHosterClient;
import org.geopublishing.geopublisher.export.gphoster.SERVICE_STATUS;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.netbeans.spi.wizard.WizardPage;

import de.schmitzm.swing.input.ManualInputOption;
import de.schmitzm.swing.input.ManualInputOption.Password;
import de.schmitzm.swing.swingworker.AtlasSwingWorker;

public class ExportWizardPage_GpHoster_FtpExport extends WizardPage {
	final static protected Logger LOGGER = Logger
			.getLogger(ExportWizardPage_GpHoster_FtpExport.class);
	private static final String validationFtpFailedUserNotFound = GeopublisherGUI
			.R("ExportWizard.Ftp.ValidationError_UserNotFound",
					GeopublisherGUI.R("ExportWizard.Ftp.CreateNewUser"));
	JLabel explanationJLabel = new JLabel(
			GeopublisherGUI.R("ExportWizard.Ftp.Explanation"));
	JCheckBox firstSyncJCheckBox;
	private JTextField UserJTextField;
	private Password PWJTextField;
	// private Boolean isNewAtlasUpload;
	private AtomicBoolean isNewAtlasUpload;
	// private final GpHosterClient gphc;
	JLabel userNameLabel = new JLabel(
			GeopublisherGUI.R("ExportWizard.FtpExport.Username"));
	private final String validationFtpFailedUsername = GeopublisherGUI
			.R("ExportWizard.Ftp.ValidationError_Username");
	private final String validationFtpFailedPassword = GeopublisherGUI
			.R("ExportWizard.Ftp.ValidationError_Password");

	public ExportWizardPage_GpHoster_FtpExport() {
		// ace = (GeopublisherGUI.getInstance().getAce();
		initGui();
	}

	public static String getDescription() {
		return GeopublisherGUI.R("ExportWizard.Ftp");
	}

	private void initGui() {
		// setSize(ExportWizard.DEFAULT_WPANEL_SIZE);
		// setPreferredSize(ExportWizard.DEFAULT_WPANEL_SIZE);
		setLayout(new MigLayout("wrap 2"));
		add(explanationJLabel, "span 2");
		add(getFirstSyncJCheckBox(), "wrap");
		add(userNameLabel, "wrap");
		add(getUserJTextField(), "growx, wrap");
		add(getPWJTextField(), "growx");
	}

	private Password getPWJTextField() {
		if (PWJTextField == null) {
			PWJTextField = new ManualInputOption.PasswordViewable(
					GeopublisherGUI.R("ExportWizard.FtpExport.Password"),
					false, GPProps.get(GPProps.Keys.Password));
			PWJTextField.setName(ExportWizard.GPH_PASSWORD);
			// ((GpHosterClient) getWizardData(ExportWizard.GPHC))
			// .setPassword(PWJTextField.getValue().toString());
		}
		return PWJTextField;
	}

	private JCheckBox getFirstSyncJCheckBox() {
		if (firstSyncJCheckBox == null) {
			firstSyncJCheckBox = new JCheckBox(
					GeopublisherGUI.R("ExportWizard.Ftp.CreateNewUser"));
			firstSyncJCheckBox.setName(ExportWizard.FTP_FIRST);
		}
		return firstSyncJCheckBox;
	}

	@Override
	protected void renderingPage() {
		getFirstSyncJCheckBox()
				.setSelected(
						isFirstSync((AtlasConfigEditable) getWizardData(ExportWizard.ACE)));
		getUserJTextField().setEnabled(!getFirstSyncJCheckBox().isSelected());
		getPWJTextField().setEnabled(!getFirstSyncJCheckBox().isSelected());

	}

	private JTextField getUserJTextField() {
		if (UserJTextField == null) {
			UserJTextField = new JTextField(GPProps.get(GPProps.Keys.Username,
					""));
			UserJTextField.setName(ExportWizard.GPH_USERNAME);
			// ((GpHosterClient) getWizardData(ExportWizard.GPHC))
			// .setUserName(UserJTextField.getText());

			getFirstSyncJCheckBox().addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					getUserJTextField().setEnabled(
							!getFirstSyncJCheckBox().isSelected());
					getPWJTextField().setEnabled(
							!getFirstSyncJCheckBox().isSelected());
				}
			});
		}
		return UserJTextField;
	}

	@Override
	protected String validateContents(final Component component,
			final Object event) {

		// this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		// SwingUtil.getParentWindow(component).setCursor(
		// Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		if (getFirstSyncJCheckBox().isSelected())
			return null;
		if (getUserJTextField().getText().isEmpty()) {
			return validationFtpFailedUsername;
		}

		if (!(getPWJTextField().getValue().length > 0)) {
			return validationFtpFailedPassword;
		}

		// Because Schmitzm Password input component is not properly handled by
		// the wizard, we put the password into the wizardmap manually
		if (getPWJTextField().getValue() != null)
			putWizardData(ExportWizard.GPH_PASSWORD, getPWJTextField()
					.getValue().toString());

		return null;
	}

	private boolean isFirstSync(final AtlasConfigEditable ace) {

		if (ace == null)
			return false;

		if (isNewAtlasUpload == null) {

			AtlasSwingWorker<Void> worker = new AtlasSwingWorker<Void>(
					(Component) null, "checking for first Sync") {
				@Override
				protected Void doInBackground() throws Exception {
					if (isNewAtlasUpload == null) {
						if (((GpHosterClient) getWizardData(ExportWizard.GPHC))
								.checkService().equals(SERVICE_STATUS.OK)) {
							try {
								LOGGER.debug("Checking online whether "
										+ ace.getBaseName() + " is free");
								final boolean askGpHosterOnline = ((GpHosterClient) getWizardData(ExportWizard.GPHC))
										.atlasBasenameFree(ace.getBaseName());
								isNewAtlasUpload = new AtomicBoolean(
										askGpHosterOnline);
							} catch (IOException e) {
								LOGGER.error("isFirstSync failed", e);
							}
						}
					}
					return null;
				}
			};
			worker.executeModalNoEx();
		}
		return isNewAtlasUpload != null ? isNewAtlasUpload.get() : true;
		// if (isNewAtlasUpload == null) {
		// if (gphc.checkService().equals(SERVICE_STATUS.OK)) {
		// try {
		// isNewAtlasUpload = gphc
		// .atlasBasenameFree(ace.getBaseName());
		// } catch (IOException e) {
		// LOGGER.error("isFirstSync failed", e);
		// }
		// }
		// }
		// return isNewAtlasUpload;
	}
}