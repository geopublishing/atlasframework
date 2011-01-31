package org.geopublishing.geopublisher.gui.export;

import java.awt.Component;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GPProps;
import org.geopublishing.geopublisher.export.GpFtpAtlasExport;
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
    JCheckBox firstSyncJCheckBox;
    private final AtlasConfigEditable ace;

    public ExportWizardPage_FtpExport() {
        ace = GeopublisherGUI.getInstance().getAce();
        initGui();
    }

    private boolean checkFirstExport() {
        final String path = GpFtpAtlasExport.GEOPUBLISHING_ORG
                + ace.getBaseName() + ".fingerprint";
        boolean isFirstExport = !urlIsOnline(path);
        return isFirstExport;

    }

    public static String getDescription() {
        return GeopublisherGUI.R("ExportWizard.Ftp");
    }

    private void initGui() {
        setSize(ExportWizard.DEFAULT_WPANEL_SIZE);
        setPreferredSize(ExportWizard.DEFAULT_WPANEL_SIZE);
        setLayout(new MigLayout("wrap 1"));
        add(explanationJLabel);
        add(getFirstSyncJCheckBox());
        add(getUrlJTextField());
        getUrlJTextField().setText("http://geopublishing.org");
    }

    private JCheckBox getFirstSyncJCheckBox() {
        if (firstSyncJCheckBox == null) {
            firstSyncJCheckBox = new JCheckBox("Create new User");
            firstSyncJCheckBox.setName(ExportWizard.FTP_FIRST);
            firstSyncJCheckBox.setSelected(checkFirstExport());
        }
        return firstSyncJCheckBox;
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
        urlExists = urlIsOnline("http://www.geopublishing.org");
        if (!urlExists)
            return validationFtpFailedMsg;
        return null;
    }

    private boolean urlIsOnline(String path) {
        boolean urlExists = false;
        try {
            urlExists = IOUtil.urlExists(new URL(path));
        } catch (MalformedURLException e) {
            LOGGER.error("", e);
        }
        return urlExists;
    }
}