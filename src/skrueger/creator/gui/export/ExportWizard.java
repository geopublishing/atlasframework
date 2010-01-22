/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the Geopublisher application - An authoring tool to facilitate the publication and distribution of geoproducts in form of online and/or offline end-user GIS.
 * http://www.geopublishing.org
 * 
 * Geopublisher is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.creator.gui.export;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardBranchController;
import org.netbeans.spi.wizard.WizardPage;

import schmitzm.lang.LangUtil;
import skrueger.atlas.AVUtil;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.AtlasCreator;
import skrueger.creator.gui.EditAtlasParamsDialog;

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
	final public static String JWS_CHECKBOX = "exportJWS?";
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
				AVUtil.showMessageDialog(owner, AtlasCreator
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
