package org.geopublishing.atlasStyler.swing.importWizard;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasStyler.AsSwingUtil;
import org.netbeans.spi.wizard.WizardPage;

public class ImportWizardPage_WFS_Layer_Select extends WizardPage {
	/*
	 * The short description label that appears on the left side of the wizard
	 */
	JLabel explanationJLabel = new JLabel(
			AsSwingUtil.R("ImportWizard.WFS.LayerSelection.Explanation"));

	JComboBox wfsLayerJComboBox;
	final static private JLabel wfsLayerSelectionLabel = new JLabel(
			AsSwingUtil.R("ImportWizard.WFS.LayerSelectionLabel"));

	public static String getDescription() {
		return AsSwingUtil.R("ImportWizard.WFS.WfsLayerSelection");
	}

	public ImportWizardPage_WFS_Layer_Select() {
		initGui();
	}

	@Override
	protected String validateContents(final Component component,
			final Object event) {
		return null;
	}

	private void initGui() {
		setLayout(new MigLayout("wrap 1"));

		add(explanationJLabel);
		add(wfsLayerSelectionLabel, "gapy unrelated");
		add(getWfsLayerJComboBox(), "growx, left");
	}

	@Override
	protected void renderingPage() {
		super.renderingPage();
		String[] typeNames = (String[]) getWizardData(ImportWizard.TYPENAMES);
		getWfsLayerJComboBox().setModel(new DefaultComboBoxModel(typeNames));

		getWizardDataMap().put(ImportWizard.IMPORT_WFS_LAYERNAME,
				getWfsLayerJComboBox().getSelectedItem());
	}

	private JComboBox getWfsLayerJComboBox() {
		if (wfsLayerJComboBox == null) {
			wfsLayerJComboBox = new JComboBox();
			wfsLayerJComboBox.setName(ImportWizard.IMPORT_WFS_LAYERNAME);
			wfsLayerJComboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					putWizardData(ImportWizard.IMPORT_WFS_LAYERNAME,
							getWfsLayerJComboBox().getSelectedItem());
				}
			});
		}

		return wfsLayerJComboBox;
	}
}
