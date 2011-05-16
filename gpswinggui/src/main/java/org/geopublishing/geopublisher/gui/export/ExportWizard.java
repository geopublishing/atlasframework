/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.gui.export;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.export.GpHosterServerSettings;
import org.geopublishing.geopublisher.export.gphoster.GpFtpAtlasExport;
import org.geopublishing.geopublisher.export.gphoster.GpHosterClient;
import org.geopublishing.geopublisher.gui.EditAtlasParamsDialog;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardBranchController;
import org.netbeans.spi.wizard.WizardPage;

import de.schmitzm.lang.LangUtil;

/**
 * This {@link Wizard} provides an easy way for the Geopublisher user to export the atlas.
 * 
 * @see {@link ExportWizardResultProducer} which finally runs the export.
 * @see {@link ExportWizardPage_Save}
 * @see {@link ExportWizardPage_TargetPlattformsSelection}
 * @see {@link ExportWizardPage_ExportFolder}
 * @see {@link ExportWizardPage_JRECopy}
 * @see {@link ExportWizardPage_JNLPDefinition}
 * 
 * @author Stefan A. Tzeggai
 */
public class ExportWizard extends WizardBranchController {
	private static final Wizard FTP_BRANCH = new ExportWizardFTPBrancher().createWizard();

	final static protected Logger LOGGER = Logger.getLogger(ExportWizard.class);

	public static final ExportWizardResultProducer FINISHER = new ExportWizardResultProducer();

	/** Used for a all-the-same look of the panels **/
	final public static Dimension DEFAULT_WPANEL_SIZE = new Dimension(470, 370);

	final public static String SAVE_AUTOMATICALLY = "saveAtlas";
	final public static String ACE = "ace";

	/** Used to identify the JWS check-box in the wizard-data **/
	final public static String JWS_CHECKBOX = "exportJWS?";
	/** Used to identify the DISK check-box in the wizard-data **/
	final public static String DISK_CHECKBOX = "exportDISK?";
	/** Used to identify the FTP check-box in the wizard-data **/
	final public static String FTP_CHECKBOX = "exportFTP?";

	/** Used to identify the DISK ZIP check-box in the wizard-data **/
	public static final String DISKZIP_CHECKBOX = "zipDISK";

	public static final String COPYJRE = "copyJRE";

	public static final String EXPORTFOLDER = "exportFolderAbsolutePath";

	public static final String JNLPURL = "jnlpCodebase";

	public static final String AGB_ACCEPTED = "agb_accepted?";
	public static final String GPH_EMAIL_FIELD = "email_set?";
	public static final String GPH_USERNAME = "gph_username";
	public static final String GPH_PASSWORD = "gph_password";

	/**
	 * Decides wheter the user wants to make his Atlas public, or not.
	 * 
	 * @see ExportWizardPage_GpHoster_ExportOptions
	 */
	public static final String GpHosterAuth = "public?";

	/** Used to identify whether a Sync to FTP is a first one **/
	public static final String FTP_FIRST = "firstSync?";

	public static final String GPHC = "GpHosterCLient instance key";

	protected static final String RESULTPRODUCER_WORKING = "once the deferred export task has started, we put it in the wizardmap with this key. this allows for a button that cancels the export.";

	// public static Boolean isNewAtlasUpload = null;

	static Map<Object, Object> initialProperties = new HashMap<Object, Object>();

	/**
	 * This constructor also defines the default (first) steps of the wizard until it branches.
	 */
	protected ExportWizard() {
		super(new WizardPage[] { new ExportWizardPage_Save(), new ExportWizardPage_TargetPlattformsSelection() });

		// Create the one and only instance of GpHoster!
		// The settings to use are defined in the properties
		GpHosterServerSettings gpss = GpFtpAtlasExport.getSelectedGpHosterServerSettings();
		final GpHosterClient gphc = new GpHosterClient(gpss);
		initialProperties.put(GPHC, gphc);
	}

	/**
	 * @return whether all required meta-data has been supplied that is need for a successful export. (Title, Desc,
	 *         Creator/Vendor in ALL languages and AtlasBasename)
	 */
	private static boolean checkForRequiredMetadata(Component owner, AtlasConfigEditable ace) {

		for (String lang : ace.getLanguages()) {
			if (ace.getBaseName() == null || ace.getTitle().get(lang) == null || ace.getTitle().get(lang).equals("")
					|| ace.getDesc().get(lang) == null || ace.getDesc().get(lang).equals("")
					|| ace.getCreator().get(lang) == null || ace.getCreator().get(lang).equals("")) {
				AVSwingUtil.showMessageDialog(owner, GeopublisherGUI.R("Export.Error.MissingMetaData"));
				return false;
			}
		}
		return true;
	}

	/**
	 * Exports an {@link AtlasConfigEditable} and returns a {@link File} pointing to the export directory or
	 * <code>null</code>.
	 * 
	 * return a {@link File} or a {@link JPanel}
	 */
	public static Object showWizard(Component owner, AtlasConfigEditable atlasConfigEditable) {

		while (!checkForRequiredMetadata(owner, atlasConfigEditable)) {
			EditAtlasParamsDialog editAtlasParamsDialog = new EditAtlasParamsDialog(owner, atlasConfigEditable);
			editAtlasParamsDialog.setVisible(true);
			if (editAtlasParamsDialog.isCancelled())
				return null;
		}

		ExportWizard chartStartWizard = new ExportWizard(); // It's a special
		// WizardBranchControler
		Wizard wiz = chartStartWizard.createWizard();

		/**
		 * This wizard shall start with featureSource and attributeMetaDataMap set in the initialProperties map:
		 */

		initialProperties.put(ACE, atlasConfigEditable);

		// Find a nice position for the window
		// Window parent = SwingUtil.getParentWindow(owner);
		// parent.getBounds()

		final Object showWizard = WizardDisplayer.showWizard(wiz, null, null, initialProperties);

		return showWizard;
	}

	/**
	 * It's being called all the time, so the Wizard can figure out which is the next step.
	 */
	@Override
	public Wizard getWizardForStep(String step, Map wizardData) {

		// System.out.print("Step: '" + step + "'  ");

		Boolean isJws = (Boolean) wizardData.get(ExportWizard.JWS_CHECKBOX);
		Boolean isDisk = (Boolean) wizardData.get(ExportWizard.DISK_CHECKBOX);
		Boolean isFtp = (Boolean) wizardData.get(ExportWizard.FTP_CHECKBOX);

		Class<WizardPage>[] path = new Class[] {};

		if (isDisk != null && isDisk || isJws != null && isJws)
			path = (Class<WizardPage>[]) LangUtil.extendArray(path, ExportWizardPage_ExportFolder.class);
		if (isDisk != null && isDisk)
			path = (Class<WizardPage>[]) LangUtil.extendArray(path, ExportWizardPage_JRECopy.class);
		if (isJws != null && isJws)
			path = (Class<WizardPage>[]) LangUtil.extendArray(path, ExportWizardPage_JNLPDefinition.class);
		if (isFtp != null && isFtp)
			return FTP_BRANCH;

		// Last page:
		path = (Class<WizardPage>[]) LangUtil.extendArray(path, ExportWizardPage_WaitExporting.class);

		// System.out.println(path.length + " "
		// + LangUtil.stringConcatWithSep("->  ", path));
		return WizardPage.createWizard(path, FINISHER);
	}
}
