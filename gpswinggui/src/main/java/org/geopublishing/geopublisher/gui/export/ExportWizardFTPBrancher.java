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
	public static final String MAKE_PUBLIC = "public?";

	protected ExportWizardFTPBrancher() {
		super(new ExportWizardPage_GpHoster_FtpExport());
	}

	@Override
	protected Wizard getWizardForStep(String step, Map wizardData) {
		Class[] path = new Class[] {};
		set_Email = (String) wizardData.get(EMAIL_SET);

		Boolean isJws = (Boolean) wizardData.get(ExportWizard.JWS_CHECKBOX);
		Boolean isDisk = (Boolean) wizardData.get(ExportWizard.DISK_CHECKBOX);
		Boolean isFirstSync = (Boolean) wizardData.get(ExportWizard.FTP_FIRST);
		// AtlasConfigEditable ace = (AtlasConfigEditable) wizardData
		// .get(ExportWizard.ACE);

		if (isFirstSync != null && isFirstSync) {
			path = LangUtil.extendArray(path,
					ExportWizardPage_GpHoster_NewUser.class);
			path = LangUtil.extendArray(path,
					ExportWizardPage_GpHoster_CheckMail.class);
			path = LangUtil.extendArray(path,
					ExportWizardPage_GpHoster_ExportOptions.class);
		} else {
			path = LangUtil.extendArray(path,
					ExportWizardPage_GpHoster_ExportOptions.class);
		}

		if (isDisk != null && isDisk || isJws != null && isJws)
			path = LangUtil.extendArray(path,
					ExportWizardPage_ExportFolder.class);
		if (isDisk != null && isDisk)
			path = LangUtil.extendArray(path, ExportWizardPage_JRECopy.class);
		if (isJws != null && isJws)
			path = LangUtil.extendArray(path,
					ExportWizardPage_JNLPDefinition.class);

		// Last page:
		path = LangUtil.extendArray(path, ExportWizardPage_WaitExporting.class);

		return WizardPage.createWizard(path, ExportWizard.FINISHER);
	}

}
