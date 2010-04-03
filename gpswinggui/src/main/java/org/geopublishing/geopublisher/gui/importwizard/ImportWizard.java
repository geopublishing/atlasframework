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
package org.geopublishing.geopublisher.gui.importwizard;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardBranchController;
import org.netbeans.spi.wizard.WizardPage;

import schmitzm.lang.LangUtil;

/**
 * This {@link Wizard} provides an easy way for the Geopublisher user to import
 * all kinds of data into the atlas.
 * 
 * @see {@link ImportWizardResultProducer} which finally runs the import.
 * @see {@link ImportWizardPage_ImportTypeSelect} which allows to choose
 *      different import sources
 * 
 * @author Stefan A. Krüger
 */
public class ImportWizard extends WizardBranchController {
	final static protected Logger LOGGER = Logger.getLogger(ImportWizard.class);

	private AtlasConfigEditable ace;

	/** Used for a all-the-same look of the panels **/
	final public static Dimension DEFAULT_WPANEL_SIZE = new Dimension(470, 370);

	enum SOURCETYPE {
		/** Import maps or DPEs from another atlas **/
		gpa,
		/** Import DPEs from the file system (like D'n'D) **/
		file
	}

	/** Identifies the selected {@link SOURCETYPE} in the wizard data map **/
	final public static String IMPORT_SOURCE_TYPE = "import_source_type";

	/** Identifies the ACE to import into in the wizard data map **/
	final public static String ACE = "atlas_config_editable";

	/** Gpa Import : Identifies the path to the atlas.xml in the wizard data map **/
	public static final String IMPORT_GPA_FOLDER = "jtextfield mit pfad zu *.gpa";

	public static final Object IMPORT_GPA_IDLIST = "list of map and dp id to import from the external atlas";

	public static final Object IMPORT_GPA_ATLASCONFIG = "AtlasConfig object of the external atlas";

	public static final String IMPORT_FILE = "Absolute path to the file the user wants to import";

	public static final Object GUI_OWNER_COMPONENT = "a component that belongs to this wizard";

	/**
	 * This constructor also defines the default (first) steps of the wizard
	 * until it branches.
	 */
	protected ImportWizard(AtlasConfigEditable ace) {
		super(new WizardPage[] { new ImportWizardPage_ImportSourceType() });
		this.ace = ace;
	}

	/**
	 * Imports an {@link AtlasConfigEditable} and returns a {@link File}
	 * pointing to the export directory or <code>null</code>.
	 * 
	 * return a {@link File} or a {@link JPanel}
	 */
	public static Object showWizard(Component owner,
			AtlasConfigEditable atlasConfigEditable) {

		ImportWizard chartStartWizard = new ImportWizard(atlasConfigEditable);
		Wizard wiz = chartStartWizard.createWizard();

		Map<Object, Object> initialProperties = new HashMap<Object, Object>();
		initialProperties.put(ACE, atlasConfigEditable);

		initialProperties.put(GUI_OWNER_COMPONENT, owner); // TODO This is
															// actually
		// wrong but somehow works. i want the
		// WIzards component

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

		SOURCETYPE importSourceType = (SOURCETYPE) wizardData
				.get(ImportWizard.IMPORT_SOURCE_TYPE);

		Class[] path = new Class[] {};

		if (importSourceType != null) {

			switch (importSourceType) {
			case file:
				path = LangUtil.extendArray(path,
						ImportWizardPage_FILE_Select.class);
				return WizardPage.createWizard(path,
						new ImportWizardResultProducer_FILE(ace));
			case gpa:
				path = LangUtil.extendArray(path,
						ImportWizardPage_GPA_SelectFolder.class);
				path = LangUtil
						.extendArray(
								path,
								ImportWizardPage_GPA_Select_DPEs_And_Maps_To_Import.class);
				return WizardPage.createWizard(path,
						new ImportWizardResultProducer_GPA(ace));
			}
			// LOGGER.debug("getWizardForStep " + step + " returns " + path);
		}

		// That path is never followed! Its a hack.
		return WizardPage
				.createWizard(new Class[] { ImportWizardPage_ImportSourceType.class });

	}

}
