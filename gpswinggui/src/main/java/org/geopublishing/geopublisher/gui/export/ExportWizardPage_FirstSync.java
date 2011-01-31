package org.geopublishing.geopublisher.gui.export;

import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.netbeans.spi.wizard.WizardPage;

public class ExportWizardPage_FirstSync extends WizardPage {

    JLabel explanationLabel = new JLabel(
            "Hier kommt bald die Benutzerverwaltung hin");

    public ExportWizardPage_FirstSync() {
        initGui();
    }

    private void initGui() {
        setSize(ExportWizard.DEFAULT_WPANEL_SIZE);
        setPreferredSize(ExportWizard.DEFAULT_WPANEL_SIZE);
        setLayout(new MigLayout("wrap 1"));
        add(explanationLabel);

    }

    public static String getDescription() {
        return "First Sync";
    }

}
