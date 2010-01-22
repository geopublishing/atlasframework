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

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.netbeans.spi.wizard.WizardPage;

import skrueger.creator.AtlasCreator;
import skrueger.creator.GPProps;
import skrueger.creator.GPProps.Keys;

public class ExportWizardPage_DiskJwsSelection extends WizardPage {
	/*
	 * The short description label that appears on the left side of the wizard
	 */
	static final String desc = AtlasCreator.R("ExportWizard.JwsOrDisk");

	private static final String validationJwsOrDiskFailedMsg = AtlasCreator
			.R("ExportWizard.JwsOrDisk.ValidationError");

	JLabel explanationJLabel = new JLabel(AtlasCreator
			.R("ExportWizard.JwsOrDisk.Explanation"));
	JLabel explanationJwsJLabel = new JLabel(AtlasCreator
			.R("ExportWizard.JwsOrDisk.Explanation.Jws"));
	JLabel explanationDiskJLabel = new JLabel(AtlasCreator
			.R("ExportWizard.JwsOrDisk.Explanation.Disk"));
	JCheckBox diskJCheckbox;
	JCheckBox jwsJCheckbox;

	public static String getDescription() {
		return desc;
	}

	public ExportWizardPage_DiskJwsSelection() {
		initGui();
	}

	@Override
	protected String validateContents(final Component component,
			final Object event) {
		if (!getJwsJCheckbox().isSelected() && !getDiskJCheckbox().isSelected())
			return validationJwsOrDiskFailedMsg;
		return null;
	}

	private void initGui() {
		setSize(ExportWizard.DEFAULT_WPANEL_SIZE);
		setPreferredSize(ExportWizard.DEFAULT_WPANEL_SIZE);

		setLayout(new MigLayout("wrap 1"));
		add(explanationJLabel);
		add(getDiskJCheckbox(), "gapy unrelated");
		add(explanationDiskJLabel);
		add(getJwsJCheckbox(), "gapy unrelated");
		add(explanationJwsJLabel);

	}

	private JCheckBox getJwsJCheckbox() {
		if (jwsJCheckbox == null) {
			jwsJCheckbox = new JCheckBox(AtlasCreator
					.R("ExportWizard.JwsOrDisk.JwsCheckbox"));
			jwsJCheckbox.setName(ExportWizard.JWS_CHECKBOX);
			jwsJCheckbox.setSelected(GPProps.getBoolean(Keys.LastExportJWS));

		}
		return jwsJCheckbox;
	}

	private JCheckBox getDiskJCheckbox() {
		if (diskJCheckbox == null) {
			diskJCheckbox = new JCheckBox(AtlasCreator
					.R("ExportWizard.JwsOrDisk.DiskCheckbox"));
			diskJCheckbox.setName(ExportWizard.DISK_CHECKBOX);
			diskJCheckbox.setSelected(GPProps.getBoolean(Keys.LastExportDisk));
		}
		return diskJCheckbox;
	}

}
