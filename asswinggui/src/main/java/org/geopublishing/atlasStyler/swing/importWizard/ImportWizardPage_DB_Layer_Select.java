package org.geopublishing.atlasStyler.swing.importWizard;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasStyler.ASUtil;
import org.netbeans.spi.wizard.WizardPage;

public class ImportWizardPage_DB_Layer_Select extends WizardPage {
	/*
	 * The short description label that appears on the left side of the wizard
	 */
	JLabel explanationJLabel = new JLabel(
			ASUtil.R("ImportWizard.DB.LayerSelection.Explanation"));

	JComboBox dbLayerJComboBox;
	final static private JLabel dbLayerSelectionLabel = new JLabel(
			ASUtil.R("ImportWizard.DB.LayerSelectionLabel"));

	public static String getDescription() {
		return ASUtil.R("ImportWizard.DB.DbLayerSelection");
	}

	public ImportWizardPage_DB_Layer_Select() {
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
		add(dbLayerSelectionLabel, "gapy unrelated");
		add(getDbLayerJComboBox(), "growx, left");
	}

	@Override
	protected void renderingPage() {
		super.renderingPage();
		String[] typeNames = (String[]) getWizardData(ImportWizard.TYPENAMES);
		getDbLayerJComboBox().setModel(new DefaultComboBoxModel(typeNames));

		getWizardDataMap().put(ImportWizard.IMPORT_DB_LAYERNAME,
				getDbLayerJComboBox().getSelectedItem());
	}

	private JComboBox getDbLayerJComboBox() {
		if (dbLayerJComboBox == null) {
			dbLayerJComboBox = new JComboBox();
			dbLayerJComboBox.setName(ImportWizard.IMPORT_DB_LAYERNAME);
			dbLayerJComboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					putWizardData(ImportWizard.IMPORT_DB_LAYERNAME,
							getDbLayerJComboBox().getSelectedItem());
				}
			});
		}

		return dbLayerJComboBox;
	}
}
