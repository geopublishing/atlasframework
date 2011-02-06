package org.geopublishing.geopublisher.gui.export;

import java.awt.Component;

import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.netbeans.spi.wizard.WizardPage;

import de.schmitzm.swing.input.ManualInputOption.Password;

public class ExportWizardPage_GpHoster_CheckMail extends WizardPage {
    private final String validationFtpFailedPassword = GeopublisherGUI
            .R("ExportWizard.Ftp.ValidationError_Password");
    JLabel explanationJLabel = new JLabel(GeopublisherGUI.R(
            "ExportWizard.Ftp.CheckMail.Explanation", ExportWizard.set_Email));

    private Password PWField;

    public static String getDescription() {
        return GeopublisherGUI.R("ExportWizard.Ftp.CheckMail");
    }

    public ExportWizardPage_GpHoster_CheckMail() {
        // send mail
        initGui();
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
            PWField.setName(ExportWizard.PASSWORD);
        }
        return PWField;
    }

    @Override
    protected String validateContents(Component component, Object event) {
        if (getPWField().getValue() == null) {
            return validationFtpFailedPassword;
        }
        return null;
    }
}
