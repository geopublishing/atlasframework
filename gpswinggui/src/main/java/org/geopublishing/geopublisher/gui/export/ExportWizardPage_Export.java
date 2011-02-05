package org.geopublishing.geopublisher.gui.export;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.geopublisher.export.gphoster.GpHosterClient;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.netbeans.spi.wizard.WizardPage;

public class ExportWizardPage_Export extends WizardPage {

    private final GpHosterClient gphc;
    private JLabel explanationLabel;
    private JCheckBox makePublicCheckBox;

    public ExportWizardPage_Export() {
        gphc = ExportWizardFTPBrancher.gphc;
        initGui();
    }

    private void initGui() {
        setSize(ExportWizard.DEFAULT_WPANEL_SIZE);
        setPreferredSize(ExportWizard.DEFAULT_WPANEL_SIZE);
        setLayout(new MigLayout("wrap 1"));
        add(getExplanationLabel());
        add(getMakePublicCheckBox());
    }

    private JLabel getExplanationLabel() {
        if (explanationLabel == null) {
            explanationLabel = new JLabel(
                    GeopublisherGUI.R("ExportWizard.Ftp.Export.Explanation"));
        }
        return explanationLabel;
    }

    private JCheckBox getMakePublicCheckBox() {
        if (makePublicCheckBox == null) {
            makePublicCheckBox = new JCheckBox(
                    GeopublisherGUI.R("ExportWizard.Ftp.Export.Public"));
            makePublicCheckBox.setName(ExportWizardFTPBrancher.MAKE_PUBLIC);
        }
        return makePublicCheckBox;
    }

    public static String getDescription() {
        return GeopublisherGUI.R("ExportWizard.Ftp.Export");
    }

}
