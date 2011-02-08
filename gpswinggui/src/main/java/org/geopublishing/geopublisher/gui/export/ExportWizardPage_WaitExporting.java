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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.netbeans.spi.wizard.DeferredWizardResult;
import org.netbeans.spi.wizard.WizardPage;

public class ExportWizardPage_WaitExporting extends WizardPage {
	/*
	 * The short description label that appears on the left side of the wizard
	 */
	JLabel explanationJLabel = new JLabel(
			GeopublisherGUI.R("ExportWizard.ExportWait"));

	public ExportWizardPage_WaitExporting() {
		setPreferredSize(ExportWizard.DEFAULT_WPANEL_SIZE);
		setSize(ExportWizard.DEFAULT_WPANEL_SIZE);

		setLayout(new MigLayout("wrap 1"));
		add(explanationJLabel);
		add(cancelExportButton);
	}

	JButton cancelExportButton = new JButton(new AbstractAction("cancel") {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			cancelExportButton.setEnabled(false);
			DeferredWizardResult result = (DeferredWizardResult) getWizardData(ExportWizard.RESULTPRODUCER_WORKING);
			if (result != null)
				result.abort();

		}

	});

	@Override
	protected void renderingPage() {
		cancelExportButton.setEnabled(true);
	};

	public static String getDescription() {
		return GeopublisherGUI.R("ExportWizard.ExportWait");
	}

}
