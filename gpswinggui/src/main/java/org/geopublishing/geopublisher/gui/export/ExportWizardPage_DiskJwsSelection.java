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

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.geopublisher.GeopublisherGUI;
import org.netbeans.spi.wizard.WizardPage;

import skrueger.creator.GPProps;
import skrueger.creator.GPProps.Keys;


public class ExportWizardPage_DiskJwsSelection extends WizardPage {
	private final String validationJwsOrDiskFailedMsg = GeopublisherGUI
			.R("ExportWizard.JwsOrDisk.ValidationError");

	JLabel explanationJLabel = new JLabel(GeopublisherGUI
			.R("ExportWizard.JwsOrDisk.Explanation"));
	JLabel explanationJwsJLabel = new JLabel(GeopublisherGUI
			.R("ExportWizard.JwsOrDisk.Explanation.Jws"));
	JLabel explanationDiskJLabel = new JLabel(GeopublisherGUI
			.R("ExportWizard.JwsOrDisk.Explanation.Disk"));
	JCheckBox diskJCheckbox;
	JCheckBox jwsJCheckbox;

	public static String getDescription() {
		return GeopublisherGUI.R("ExportWizard.JwsOrDisk");
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
			jwsJCheckbox = new JCheckBox(GeopublisherGUI
					.R("ExportWizard.JwsOrDisk.JwsCheckbox"));
			jwsJCheckbox.setName(ExportWizard.JWS_CHECKBOX);
			jwsJCheckbox.setSelected(GPProps.getBoolean(Keys.LastExportJWS));

		}
		return jwsJCheckbox;
	}

	private JCheckBox getDiskJCheckbox() {
		if (diskJCheckbox == null) {
			diskJCheckbox = new JCheckBox(GeopublisherGUI
					.R("ExportWizard.JwsOrDisk.DiskCheckbox"));
			diskJCheckbox.setName(ExportWizard.DISK_CHECKBOX);
			diskJCheckbox.setSelected(GPProps.getBoolean(Keys.LastExportDisk));
		}
		return diskJCheckbox;
	}

}
