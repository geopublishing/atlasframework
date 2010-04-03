/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
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
import org.geopublishing.geopublisher.GeopublisherGUI;
import org.geopublishing.geopublisher.gui.EditAtlasParamsDialog;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardBranchController;
import org.netbeans.spi.wizard.WizardPage;

import schmitzm.lang.LangUtil;

/**
 * This {@link Wizard} provides an easy way for the Geopublisher user to export
 * the atlas.
 * 
 * @see {@link ExportWizardResultProducer} which finally runs the export.
 * @see {@link ExportWizardPage_Save}
 * @see {@link ExportWizardPage_DiskJwsSelection}
 * @see {@link ExportWizardPage_ExportFolder}
 * @see {@link ExportWizardPage_JRECopy}
 * @see {@link ExportWizardPage_JNLPDefinition}
 * 
 * @author Stefan A. Krüger
 */
public class ExportWizard extends WizardBranchController {
	final static protected Logger LOGGER = Logger.getLogger(ExportWizard.class);

	private static final ExportWizardResultProducer FINISHER = new ExportWizardResultProducer();

	/** Used for a all-the-same look of the panels **/
	final public static Dimension DEFAULT_WPANEL_SIZE = new Dimension(470, 370);

	final public static String SAVE_AUTOMATICALLY = "saveAtlas";
	final public static String ACE = "ace";
	
	/** Used to identify the JWS check-box in the wizard-data **/
	final public static String JWS_CHECKBOX = "exportJWS?";
	/** Used to identify the DISK check-box in the wizard-data **/
	final public static String DISK_CHECKBOX = "exportDISK?";
	public static final String COPYJRE = "copyJRE";

	public static final String EXPORTFOLDER = "exportFolderAbsolutePath";

	public static final String JNLPURL = "jnlpCodebase";

	/**
	 * This constructor also defines the default (first) steps of the wizard
	 * until it branches.
	 */
	protected ExportWizard() {
		super(new WizardPage[] { new ExportWizardPage_Save(),
				new ExportWizardPage_DiskJwsSelection(),
				new ExportWizardPage_ExportFolder() });
	}

	/**
	 * @return whether all required meta-data has been supplied that is need for
	 *         a successful export. (Title, Desc, Creator/Vendor in ALL
	 *         languages)
	 */
	private static boolean checkForRequiredMetadata(Component owner,
			AtlasConfigEditable ace) {
		for (String lang : ace.getLanguages()) {
			if (ace.getTitle().get(lang) == null
					|| ace.getTitle().get(lang).equals("")
					|| ace.getDesc().get(lang) == null
					|| ace.getDesc().get(lang).equals("")
					|| ace.getCreator().get(lang) == null
					|| ace.getCreator().get(lang).equals("")) {
				AVSwingUtil.showMessageDialog(owner, GeopublisherGUI
						.R("Export.Error.MissingMetaData"));
				return false;
			}
		}

		return true;
	}

	/**
	 * Exports an {@link AtlasConfigEditable} and returns a {@link File}
	 * pointing to the export directory or <code>null</code>.
	 * 
	 * return a {@link File} or a {@link JPanel}
	 */
	public static Object showWizard(Component owner,
			AtlasConfigEditable atlasConfigEditable) {

		while (!checkForRequiredMetadata(owner, atlasConfigEditable)) {
			EditAtlasParamsDialog editAtlasParamsDialog = new EditAtlasParamsDialog(
					owner, atlasConfigEditable);
			editAtlasParamsDialog.setVisible(true);
			if (editAtlasParamsDialog.isCancelled())
				return null;
		}

		ExportWizard chartStartWizard = new ExportWizard(); // It's a special
		// WizardBranchControler
		Wizard wiz = chartStartWizard.createWizard();

		/**
		 * This wizard shall start with featureSource and attributeMetaDataMap
		 * set in the initialProperties map:
		 */
		Map<Object, Object> initialProperties = new HashMap<Object, Object>();
		initialProperties.put(ACE, atlasConfigEditable);
		
		// Find a nice position for the window
//		Window parent = SwingUtil.getParentWindow(owner);
//		parent.getBounds()

		final Object showWizard = WizardDisplayer.showWizard(wiz, null, null,
				initialProperties);

		return showWizard;
	}

	/**
	 * It's being called all the time, so the Wizard can figure out which is the
	 * next step.
	 */
	@Override
	public Wizard getWizardForStep(String step, Map wizardData) {

		Boolean isJws = (Boolean) wizardData.get(ExportWizard.JWS_CHECKBOX);
		Boolean isDisk = (Boolean) wizardData.get(ExportWizard.DISK_CHECKBOX);

		Class[] path = new Class[] {};

		if (isDisk != null && isDisk)
			path = LangUtil.extendArray(path, ExportWizardPage_JRECopy.class);
		if (isJws != null && isJws)
			path = LangUtil.extendArray(path,
					ExportWizardPage_JNLPDefinition.class);

//		LOGGER.debug("getWizardForStep " + step + " returns " + path);
		return WizardPage.createWizard(path, FINISHER);
	}

}