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
package org.geopublishing.geopublisher.gui.importwizard;

import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.netbeans.spi.wizard.Summary;
import org.netbeans.spi.wizard.WizardPage.WizardResultProducer;


/**
 * This class is using the values collected during the {@link ImportWizard} to
 * export the {@link AtlasConfigEditable}.
 * 
 * 
 * 
 * @author Stefan A. Tzeggai
 */
public abstract class ImportWizardResultProducer implements
		WizardResultProducer {

	/** The {@link AtlasConfigEditable} we are importing into **/
	protected final AtlasConfigEditable atlasConfigEditable;

	private ImportWizardResultProducer() {
		atlasConfigEditable = null;
	}

	/**
	 * @param atlasConfig
	 *            The {@link AtlasConfigEditable} we are importing into
	 */
	public ImportWizardResultProducer(AtlasConfigEditable atlasConfig) {
		this.atlasConfigEditable = atlasConfig;
	}

	private static final Logger LOGGER = Logger
			.getLogger(ImportWizardResultProducer.class);

	@Override
	public boolean cancel(Map settings) {
		return true;
	}

	protected  JPanel getErrorPanel(Exception e) {
		JPanel panel = new JPanel(new MigLayout("wrap 1"));

		panel.add(new JLabel("<html>"+e.getLocalizedMessage()+"</html>"));

		return panel;
	}


	protected Summary getAbortSummary() {
		JPanel aborted = new JPanel(new MigLayout());
		aborted
				.add(new JLabel(
						"The import has been aborted by the user.")); //i8n

		return Summary.create(aborted, "abort");
	}
}
