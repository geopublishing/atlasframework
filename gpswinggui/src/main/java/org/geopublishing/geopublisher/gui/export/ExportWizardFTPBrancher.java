package org.geopublishing.geopublisher.gui.export;

import java.util.Map;

import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardBranchController;
import org.netbeans.spi.wizard.WizardPage;

import de.schmitzm.lang.LangUtil;

public class ExportWizardFTPBrancher extends WizardBranchController {

    public static final String AGB_ACCEPTED = "agb_accepted?";
    public static final String EMAIL_SET = "email_set?";
    public static String set_Email;
    public static final String USERNAME = "username?";
    public static final String PASSWORD = "password?";

    protected ExportWizardFTPBrancher() {
        super(new WizardPage[] {new ExportWizardPage_FtpExport()});
    }

    @Override
    protected Wizard getWizardForStep(String step, Map settings) {
        Class[] path = new Class[] {};
        Boolean isFirstSync = (Boolean) settings.get(ExportWizard.FTP_FIRST);
        set_Email = (String) settings.get(EMAIL_SET);

        if (isFirstSync != null && isFirstSync) {
            path = LangUtil.extendArray(path, ExportWizardPage_NewUser.class);
            path = LangUtil.extendArray(path, ExportWizardPage_CheckMail.class);
            path = LangUtil.extendArray(path, ExportWizardPage_Export.class);
        } else {
            path = LangUtil.extendArray(path, ExportWizardPage_Export.class);
        }
        return WizardPage.createWizard(path, ExportWizard.FINISHER);
    }

}
