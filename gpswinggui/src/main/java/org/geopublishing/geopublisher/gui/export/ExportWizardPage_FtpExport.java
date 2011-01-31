package org.geopublishing.geopublisher.gui.export;

import java.awt.Component;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.geopublishing.geopublisher.GPProps;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.netbeans.spi.wizard.WizardPage;

import de.schmitzm.io.IOUtil;

public class ExportWizardPage_FtpExport extends WizardPage {
    final static protected Logger LOGGER = Logger
            .getLogger(ExportWizardPage_FtpExport.class);
    private JTextField UrlJTextField;
    private final String validationFtpFailedMsg = GeopublisherGUI
            .R("ExportWizard.Ftp.ValidationError");
    JLabel explanationJLabel = new JLabel(
            GeopublisherGUI.R("ExportWizard.Ftp.Explanation"));

    public ExportWizardPage_FtpExport() {
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
        boolean urlExists = false;;
        try {
            urlExists = IOUtil
                    .urlExists(new URL("http://www.geopublishing.org"));
        } catch (MalformedURLException e) {
            LOGGER.error("", e);
        }
        if (!urlExists)
            return validationFtpFailedMsg;
        return null;
    }
}