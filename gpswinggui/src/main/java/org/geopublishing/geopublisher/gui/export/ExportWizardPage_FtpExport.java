package org.geopublishing.geopublisher.gui.export;

import java.awt.Component;
import java.awt.Cursor;
import java.io.IOException;

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

import de.schmitzm.swing.input.ManualInputOption.Password;
import de.schmitzm.swing.swingworker.AtlasSwingWorker;

public class ExportWizardPage_FtpExport extends WizardPage {
    final static protected Logger LOGGER = Logger
            .getLogger(ExportWizardPage_FtpExport.class);
    private final String validationFtpFailedMsg_Offline = GeopublisherGUI
            .R("ExportWizard.Ftp.ValidationError_Offline");
    private final String validationFtpFailedMsg_GpFtpDown = GeopublisherGUI
            .R("ExportWizard.Ftp.ValidationError_GpFtpDown");
    private final String validationFtpFailedMsg_GpHosterDown = GeopublisherGUI
            .R("ExportWizard.Ftp.ValidationError_GpHosterDown");
    JLabel explanationJLabel = new JLabel(
            GeopublisherGUI.R("ExportWizard.Ftp.Explanation"));
    JCheckBox firstSyncJCheckBox;
    private final AtlasConfigEditable ace;
    private JTextField UserJTextField;
    private Password PWJTextField;
    private Boolean isNewAtlasUpload;
    private final GpHosterClient gphc = new GpHosterClient();
    JLabel userNameLabel = new JLabel(
            GeopublisherGUI.R("ExportWizard.FtpExport.Username"));
    private final String validationFtpFailedUsername = GeopublisherGUI
            .R("ExportWizard.Ftp.ValidationError_Username");
    private final String validationFtpFailedPassword = GeopublisherGUI
            .R("ExportWizard.Ftp.ValidationError_Password");

    public ExportWizardPage_FtpExport() {
        ace = GeopublisherGUI.getInstance().getAce();
        initGui();
    }

    public static String getDescription() {
        return GeopublisherGUI.R("ExportWizard.Ftp");
    }

    private void initGui() {
        setSize(ExportWizard.DEFAULT_WPANEL_SIZE);
        setPreferredSize(ExportWizard.DEFAULT_WPANEL_SIZE);
        setLayout(new MigLayout("wrap 2"));
        add(explanationJLabel, "span 2");
        add(getFirstSyncJCheckBox(), "wrap");
        add(userNameLabel, "wrap");
        add(getUserJTextField(), "growx, wrap");
        add(getPWJTextField(), "growx");
    }

    private Password getPWJTextField() {
        if (PWJTextField == null) {
            PWJTextField = new Password(
                    GeopublisherGUI.R("ExportWizard.FtpExport.Password"),
                    false, GPProps.get(GPProps.Keys.Password));
            PWJTextField.setName(ExportWizardFTPBrancher.PASSWORD);
        }
        return PWJTextField;
    }

    private JCheckBox getFirstSyncJCheckBox() {
        if (firstSyncJCheckBox == null) {
            firstSyncJCheckBox = new JCheckBox("Create new User");
            firstSyncJCheckBox.setName(ExportWizard.FTP_FIRST);
            firstSyncJCheckBox.setSelected(isFirstSync(ace));
        }
        return firstSyncJCheckBox;
    }

    private JTextField getUserJTextField() {
        if (UserJTextField == null) {
            UserJTextField = new JTextField(GPProps.get(GPProps.Keys.Username,
                    ""));
            UserJTextField.setName(ExportWizardFTPBrancher.USERNAME);
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

        try {
            SERVICE_STATUS service = gphc.checkService();
            if (!service.equals(SERVICE_STATUS.OK)) {
                if (service.equals(SERVICE_STATUS.GPHOSTER_FTP_DOWN))
                    return validationFtpFailedMsg_GpFtpDown;
                if (service.equals(SERVICE_STATUS.SYSTEM_OFFLINE))
                    return validationFtpFailedMsg_Offline;
                if (service.equals(SERVICE_STATUS.GPHOSTER_REST_DOWN))
                    return validationFtpFailedMsg_GpHosterDown;
            }
            return null;
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    private boolean isFirstSync(final AtlasConfigEditable ace) {
        AtlasSwingWorker<Boolean> isfirstSync = new AtlasSwingWorker<Boolean>(
                (Component) null, "checking for first Sync") {
            @Override
            protected Boolean doInBackground() throws Exception {
                if (isNewAtlasUpload == null) {
                    if (gphc.checkService().equals(SERVICE_STATUS.OK)) {
                        try {
                            isNewAtlasUpload = gphc.atlasBasenameFree(ace
                                    .getBaseName());
                        } catch (IOException e) {
                            LOGGER.error("isFirstSync failed", e);
                        }
                    }
                }
                return isNewAtlasUpload;
            }
        };
        isfirstSync.executeModalNoEx();
        return isNewAtlasUpload;
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