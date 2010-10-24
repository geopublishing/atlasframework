package org.geopublishing.atlasStyler.swing.importWizard;

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

	/**
	 * @param atlasConfig
	 *            The {@link AtlasConfigEditable} we are importing into
	 */
	public ImportWizardResultProducer() {
	}

	private static final Logger LOGGER = Logger
			.getLogger(ImportWizardResultProducer.class);

	@Override
	public boolean cancel(Map settings) {
		return true;
	}

	protected JPanel getErrorPanel(Exception e) {
		JPanel panel = new JPanel(new MigLayout("wrap 1"));

		panel.add(new JLabel("<html>" + e.getLocalizedMessage() + "</html>"));

		return panel;
	}

	protected Summary getAbortSummary() {
		JPanel aborted = new JPanel(new MigLayout());
		aborted.add(new JLabel("The import has been aborted by the user.")); // i8n

		return Summary.create(aborted, "abort");
	}
}
