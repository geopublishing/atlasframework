package org.geopublishing.geopublisher.gui.export;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.netbeans.spi.wizard.WizardPage;

public class ExportWizardPage_Export extends WizardPage {

    public ExportWizardPage_Export() {
        initGui();
    }

    private void initGui() {
        setSize(ExportWizard.DEFAULT_WPANEL_SIZE);
        setPreferredSize(ExportWizard.DEFAULT_WPANEL_SIZE);
        setLayout(new MigLayout("wrap 1"));
    }

    public static String getDescription() {
        return GeopublisherGUI.R("ExportWizard.Ftp.Export");
    }

}
