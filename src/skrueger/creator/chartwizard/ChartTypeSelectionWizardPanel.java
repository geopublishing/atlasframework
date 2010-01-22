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
package skrueger.creator.chartwizard;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.netbeans.spi.wizard.WizardPage;

import schmitzm.jfree.chart.style.ChartType;
import skrueger.creator.AtlasCreator;
import skrueger.i8n.I8NUtil;
import skrueger.sld.ASUtil;

/**
 * This {@link WizardPage} allows to choose the basic type of diagram.
 * 
 * @author Stefan A. Krüger
 */
public class ChartTypeSelectionWizardPanel extends WizardPage {
	final static Logger LOGGER = Logger
			.getLogger(ChartTypeSelectionWizardPanel.class);

	private static final ChartType[] allowedChartTypes = new ChartType[] {
			ChartType.BAR, ChartType.AREA, ChartType.LINE,
			/* ChartType.PIE, */
			/* ChartType.POINT, */
			ChartType.SCATTER };
	private JLabel previewImage;
	private JLabel descriptionLabel;
	private JComboBox typeSelectionJComboBox;

	/**
	 * The short description label that appears on the left side of the wizard
	 */
	static String desc = AtlasCreator.R("ChartWizardPanel.chartType");

	@Override
	protected void renderingPage() {
		removeAll();
		initGUI();
	};

	private void initGUI() {
		setLayout(new MigLayout("wrap 1, w " + ChartWizard.WIDTH_DEFAULT
				+ " , h " + ChartWizard.HEIGHT_DEFAULT));

		add(getPreviewImage(), "sgx");
		add(getDescriptionLabel(), "sgx");
		add(getTypeSelectionJComboBox(), "sgx, bottom, pushy");
	}

	@Override
	protected String validateContents(Component component, Object event) {
		ChartType ct = ((ChartType) getTypeSelectionJComboBox()
				.getSelectedItem());

		final Integer IST = (Integer) (getWizardData(ChartWizard.NUMBER_OF_ATTRIBS));
		final Integer SOLL = ct.getMinDimensions();
		if (SOLL != null && SOLL > IST) {
			return AtlasCreator
					.R(
							"ChartWizard.ChartTypeSelection.ValidationError.TooFewAttribs",
							IST, SOLL, ct.getTitle());
		}
		// LOGGER.debug("\n\nIST/SOLL " + IST + " "+ SOLL);

		final Integer ISTN = (Integer) (getWizardData(ChartWizard.NUMBER_OF_NUMERIC_ATTRIBS));
		final Integer SOLLN = ct.getMinDimensions()
				- (ct.isCategoryAllowedForDomainAxis() ? 1 : 0);
		// LOGGER.debug("\nISTN/SOLLN " + ISTN + " "+ SOLLN);
		if (SOLLN != null && SOLLN > ISTN) {
			return AtlasCreator
					.R(
							"ChartWizard.ChartTypeSelection.ValidationError.TooFewNumericAttribs",
							ISTN, SOLLN, ct.getTitle());
		}

		return null;
	};

	/**
	 * This {@link JComboBox} allows to select from the different
	 * {@link ChartType ChartTypes}
	 * 
	 * @return
	 */
	public JComboBox getTypeSelectionJComboBox() {
		if (typeSelectionJComboBox == null) {

			typeSelectionJComboBox = new JComboBox(allowedChartTypes);
			typeSelectionJComboBox.setName(ChartWizard.CHARTTYPE);

			if (getWizardData(ChartWizard.CHARTTYPE) != null)
				getTypeSelectionJComboBox().setSelectedItem(
						getWizardData(ChartWizard.CHARTTYPE));

			typeSelectionJComboBox.setRenderer(new DefaultListCellRenderer() {

				@Override
				public Component getListCellRendererComponent(JList list,
						Object value, int index, boolean isSelected,
						boolean cellHasFocus) {
					JLabel protoType = (JLabel) super
							.getListCellRendererComponent(list, value, index,
									isSelected, cellHasFocus);

					ChartType ct = (ChartType) value;
					if (I8NUtil.isEmpty(ct.getTitle())) {
						protoType.setText(ct.toString());
					} else
						protoType.setText(ct.getTitle());

					protoType.setIcon(ct.getIcon());
					return protoType;
				}

			});

			ASUtil.addMouseWheelForCombobox(typeSelectionJComboBox);

		}
		return typeSelectionJComboBox;
	}

	public JLabel getDescriptionLabel() {
		if (descriptionLabel == null) {
			descriptionLabel = new JLabel(AtlasCreator
					.R("ChartTypeSelectionPanel.Explanation."
							+ getTypeSelectionJComboBox().getSelectedItem()
									.toString()));

			getTypeSelectionJComboBox().addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {

					descriptionLabel.setText(AtlasCreator
							.R("ChartTypeSelectionPanel.Explanation."
									+ getTypeSelectionJComboBox()
											.getSelectedItem().toString()));
				}

			});
		}

		return descriptionLabel;
	}

	/**
	 * Creates a JLabel with a hypothetical preview image of the selected
	 * {@link ChartType}
	 */
	public JLabel getPreviewImage() {
		if (previewImage == null) {
			final ChartType chartType = (ChartType) getTypeSelectionJComboBox()
					.getSelectedItem();
			if (chartType == null)
				return new JLabel("nix");
			previewImage = new JLabel(chartType.getPreviewIcon());

			getTypeSelectionJComboBox().addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					ChartType ct = (ChartType) e.getItem();

					previewImage.setIcon(ct.getPreviewIcon());
				}

			});
		}
		return previewImage;
	}

	public static void main(String[] args) {
		JDialog d = new JDialog();
		d.setContentPane(new ChartTypeSelectionWizardPanel());
		d.pack();
		d.setVisible(true);
	}

	public static String getDescription() {
		return desc;
	}

}
