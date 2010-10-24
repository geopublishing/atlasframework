package org.geopublishing.atlasStyler.swing.importWizard;

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
import java.awt.Component;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.swing.AtlasStylerGUI;
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
 * @author Stefan A. Tzeggai
 */
public class ImportWizard extends WizardBranchController {
	final static protected Logger LOGGER = Logger.getLogger(ImportWizard.class);

	enum SOURCETYPE {
		/** Import DPEs from the file system **/
		file,
		/** Import PostGIS layer **/
		postgis,
		/** Import WFS layer **/
		wfs;
	}

	/** Identifies the selected {@link SOURCETYPE} in the wizard data map **/
	final public static String IMPORT_SOURCE_TYPE = "import_source_type";

	public static final String IMPORT_FILE = "Absolute path to the file the user wants to import";

	public static final String GUI_OWNER_COMPONENT = "a component that belongs to this wizard";

	public static final String ATLAS_STYLER_GUI = "AtlasStylerGui instance";

	public static final String IMPORT_WFS_URL = "WfsServerSetting";

	public static final String IMPORT_WFS_LAYERNAME = "namespace:localname of the layer to import";

	public static final String TYPENAMES = "String[] of availbale typenames in that WFS";

	protected static final String IMPORT_DB = "DbServerSetting object";
	protected static final String IMPORT_DB_LAYERNAME = "[schema.]tablename of the db layer to import";

//	public static final String DB_LIST = "DbSettingsList";
//	public static final String WFS_LIST = "WfsSettingsList";

	/**
	 * This constructor also defines the default (first) steps of the wizard
	 * until it branches.
	 */
	protected ImportWizard() {
		super(new WizardPage[] { new ImportWizardPage_ImportSourceType() });
	}

	/**
	 * Imports an {@link AtlasConfigEditable} and returns a {@link File}
	 * pointing to the export directory or <code>null</code>.
	 * 
	 * return a {@link File} or a {@link JPanel}
	 */
	public static Object showWizard(Component owner, AtlasStylerGUI asg) {

		ImportWizard chartStartWizard = new ImportWizard();
		Wizard wiz = chartStartWizard.createWizard();

		Map<Object, Object> initialProperties = new HashMap<Object, Object>();

		// TODO This is
		// actually
		// wrong but somehow works. i want the
		// WIzards component
		initialProperties.put(GUI_OWNER_COMPONENT, owner);

//		initialProperties.put(DB_LIST, ASProps.get(Keys.dbList));
//
//		initialProperties.put(WFS_LIST, ASProps.get(Keys.wfsList));

		initialProperties.put(ATLAS_STYLER_GUI, asg);

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

		Class<?>[] path = new Class[] {};

		if (importSourceType != null) {

			switch (importSourceType) {
			case file:
				path = LangUtil.extendArray(path,
						ImportWizardPage_FILE_Select.class);
				return WizardPage.createWizard(path,
						new ImportWizardResultProducer_FILE());
			case wfs:
				path = LangUtil.extendArray(path,
						ImportWizardPage_WFS_Select.class,
						ImportWizardPage_WFS_Layer_Select.class);
				return WizardPage.createWizard(path,
						new ImportWizardResultProducer_WFS());
			case postgis:
				path = LangUtil.extendArray(path,
						ImportWizardPage_DB_Select.class,
						ImportWizardPage_DB_Layer_Select.class);
				return WizardPage.createWizard(path,
						new ImportWizardResultProducer_DB());
			}
		}

		// That path is never followed! Its a hack.
		return WizardPage
				.createWizard(new Class[] { ImportWizardPage_ImportSourceType.class });

	}

}
