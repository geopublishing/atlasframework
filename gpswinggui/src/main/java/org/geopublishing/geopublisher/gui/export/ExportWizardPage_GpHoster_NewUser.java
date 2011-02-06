package org.geopublishing.geopublisher.gui.export;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.geopublisher.export.gphoster.GpHosterClient;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.netbeans.spi.wizard.WizardPage;

public class ExportWizardPage_GpHoster_NewUser extends WizardPage {
    JLabel explanationJLabel = new JLabel(
            GeopublisherGUI.R("ExportWizard.Ftp.New_User.Explanation"));
    JLabel agb = new JLabel(GeopublisherGUI.R("ExportWizard.Ftp.New_User.AGB"));

    JCheckBox acceptAgb;
    JTextField eMailField;
    private final String validationFtpFailedAgbNotAccepted = GeopublisherGUI
            .R("ExportWizard.Ftp.New_User.ValidationError_AGB");
    private final String validationFtpFailedNoEmail = GeopublisherGUI
            .R("ExportWizard.Ftp.New_User.ValidationError_Email");
    private final GpHosterClient gphc;

    public ExportWizardPage_GpHoster_NewUser() {
        gphc = ExportWizardFTPBrancher.gphc;
        initGui();
    }

    private void initGui() {
        setSize(ExportWizard.DEFAULT_WPANEL_SIZE);
        setPreferredSize(ExportWizard.DEFAULT_WPANEL_SIZE);
        setLayout(new MigLayout("wrap 1"));
        add(explanationJLabel);
        add(getEMailField(), "growx");
        add(getAcceptAgb());
        add(agb);
    }

    private JCheckBox getAcceptAgb() {
        if (acceptAgb == null) {
            acceptAgb = new JCheckBox();
            acceptAgb.setName(ExportWizardFTPBrancher.AGB_ACCEPTED);
        }
        return acceptAgb;
    }

    private JTextField getEMailField() {
        if (eMailField == null) {
            eMailField = new JTextField();
            eMailField.setName(ExportWizardFTPBrancher.EMAIL_SET);
        }
        return eMailField;
    }

    @Override
    protected String validateContents(Component component, Object event) {
        if (acceptAgb != null && !acceptAgb.isSelected()) {
            return validationFtpFailedAgbNotAccepted;
        }
        if (eMailField != null && eMailField.getText().isEmpty()) {
            return validationFtpFailedNoEmail;
        }
        return null;
    }

    public static String getDescription() {
        return GeopublisherGUI.R("ExportWizard.Ftp.New_User");
    }
}
