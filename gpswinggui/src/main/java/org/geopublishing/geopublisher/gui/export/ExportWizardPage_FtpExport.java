package org.geopublishing.geopublisher.gui.export;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTextField;

import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GPProps;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.netbeans.spi.wizard.WizardPage;

public class ExportWizardPage_FtpExport extends WizardPage {

    private JTextField UrlJTextField;
    JLabel explanationJLabel = new JLabel(
            GeopublisherGUI.R("ExportWizard.Ftp.Explanation"));

    public ExportWizardPage_FtpExport() {
        final AtlasConfigEditable ace = (AtlasConfigEditable) super
                .getWizardData(ExportWizard.ACE);
        initGui();
    }

    public static String getDescription() {
        return GeopublisherGUI.R("ExportWizard.Ftp");
    }

    private void initGui() {
        setSize(ExportWizard.DEFAULT_WPANEL_SIZE);
        setPreferredSize(ExportWizard.DEFAULT_WPANEL_SIZE);
        add(explanationJLabel);
        add(getUrlJTextField());
        getUrlJTextField().setText("http://geopublishing.org");
    }

    private JTextField getUrlJTextField() {
        if (UrlJTextField == null) {
            UrlJTextField = new JTextField(GPProps.get(
                    GPProps.Keys.LastExportFolder, ""));

            UrlJTextField.setName(ExportWizard.EXPORTFOLDER);
        }

        return UrlJTextField;
    }

    @Override
    protected String validateContents(final Component component,
            final Object event) {

        return null;
    }

}