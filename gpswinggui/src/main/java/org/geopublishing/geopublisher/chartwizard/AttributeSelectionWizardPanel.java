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
package org.geopublishing.geopublisher.chartwizard;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.swing.AttributesJComboBox;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.geotools.data.FeatureSource;
import org.netbeans.spi.wizard.WizardPage;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.jfree.chart.style.ChartType;
import skrueger.geotools.AttributeMetadataMap;

public class AttributeSelectionWizardPanel extends WizardPage {
	final static protected Logger LOGGER = Logger
			.getLogger(AttributeSelectionWizardPanel.class);

	private JLabel descriptionPanel;

	private HashMap<Integer, AttributesJComboBox> attributJComboBoxes = new HashMap<Integer, AttributesJComboBox>();

	// private List<String> numericalAttNames;

	private HashMap<Integer, JCheckBox> normalizeJCheckboxs = new HashMap<Integer, JCheckBox>();
	private HashMap<Integer, JLabel> lables = new HashMap<Integer, JLabel>();

	private HashMap<Integer, JPanel> settingsPanels = new HashMap<Integer, JPanel>();

	private ChartType lastChartType;

	/*
	 * The short description label that appears on the left side of the wizard
	 */
	static String desc = GeopublisherGUI.R("AttributeSelectionWizardPanel.Title");

	@Override
	protected String validateContents(Component component, Object event) {

		ChartType chartType = (ChartType) getWizardData(ChartWizard.CHARTTYPE);

		for (int idx = 0; idx < chartType.getMinDimensions(); idx++) {
			if (getAttribJComboBoxFor(idx).getSelectedIndex() == -1)
				return GeopublisherGUI
						.R(
								"AttributeSelectionWizardPanel.ValidationFailed.NeedAtLeastXAttributes",
								chartType.getMinDimensions());
		}

		return null;
	}

	public static String getDescription() {
		return desc;
	}

	private void initGUI() {

		removeAll();

		setLayout(new MigLayout("wrap 3, w " + ChartWizard.WIDTH_DEFAULT));

		add(getDescriptionPanel(), "span 3");

		ChartType chartType = (ChartType) getWizardData(ChartWizard.CHARTTYPE);

		int maxDimensions = chartType.getMaxDimensions();
		if (maxDimensions == -1)
			maxDimensions = 4;
		int minDimensions = chartType.getMinDimensions();

		for (Integer index = 0; index < maxDimensions; index++) {

			boolean enabled = (index < minDimensions);

			add(getLabelFor(index), "top, , gapy u");
			getLabelFor(index).setEnabled(enabled);

			add(getAttribJComboBoxFor(index), "top, gapy u");
			getAttribJComboBoxFor(index).setEnabled(enabled);

			add(getSettingsPanel(index), "top, gapy u");
			getSettingsPanel(index).setEnabled(enabled);
		}
	}

	private JLabel getLabelFor(Integer index) {
		if (lables.get(index) == null) {
			JLabel label = new JLabel(GeopublisherGUI.R(
					"AttributeSelectionWizardPanel.NthAttribute", index + 1));
			lables.put(index, label);
		}
		return lables.get(index);
	}

	/**
	 * A {@link JPanel} containing the GUI with options that define the handling
	 * of the domain axis data.
	 */
	private JPanel getSettingsPanel(int idx) {
		if (settingsPanels.get(idx) == null) {
			
			ChartType chartType = (ChartType) getWizardData(ChartWizard.CHARTTYPE);

			JPanel settingsPanel;
			if (idx == 0) {
				settingsPanel = new JPanel(new MigLayout("wrap 1, insets 0"));
//				settingsPanel.add(getNormalizeJCheckboxFor(idx));
				
				// For scatter plots they makes no sense: 
				if (!(chartType == ChartType.SCATTER)) {
					settingsPanel.add(getDomainSortedJCheckbox());
					settingsPanel.add(getCategoryJCheckbox());
				}
			} else {
				settingsPanel = new JPanel(new MigLayout("wrap 1, insets 0"));
//				settingsPanel.add(getNormalizeJCheckboxFor(idx));
			}

			settingsPanels.put(idx, settingsPanel);

		}
		return settingsPanels.get(idx);

	}

//	/**
//	 * Returns the normlizeCheckBox for idx
//	 */
//	private JCheckBox getNormalizeJCheckboxFor(int idx) {
//		if (normalizeJCheckboxs.get(idx) == null) {
//			JCheckBox cb = new JCheckBox(AtlasCreator
//					.R("AttributeSelectionPanel.NormalizeCheckbox"));
//			cb.setToolTipText(AtlasCreator
//					.R("AttributeSelectionPanel.NormalizeCheckbox.TT"));
//			
//			final ChartType chartType = (ChartType)getWizardData(ChartWizard.CHARTTYPE);
//			if (chartType == ChartType.SCATTER) {
//				cb.setSelected(true);
//			} else {
//				cb.setSelected(false);
//			}
//
//			cb.setName(ChartWizard.NORMALIZE_ + idx);
//			normalizeJCheckboxs.put(idx, cb);
//
//			getAttribJComboBoxFor(0).addItemListener(new ItemListener() {
//
//				@Override
//				public void itemStateChanged(ItemEvent e) {
//
//					// LOGGER.debug(normalizeJCheckboxs.get(0));
//					// if (e.getSource() != normalizeJCheckboxs.get(0))
//					// return;
//
//					if (getAttribJComboBoxFor(0).isNumericalAttribSelected()) {
//						/* A numerical attribute */
//						normalizeJCheckboxs.get(0).setEnabled(true);
//					} else {
//						/* A numerical attribute */
//						normalizeJCheckboxs.get(0).setEnabled(false);
//						normalizeJCheckboxs.get(0).setSelected(false);
//					}
//				}
//			});
//		}
//		return normalizeJCheckboxs.get(idx);
//	}

	/**
	 * This checkbox defines whether the the domain axis will treat numerical
	 * data as category data
	 */
	private Component getCategoryJCheckbox() {
		final JCheckBox cb = new JCheckBox(GeopublisherGUI
				.R("AttributeSelectionPanel.DomainForceCategoryCheckbox"));
		cb.setToolTipText(GeopublisherGUI
				.R("AttributeSelectionPanel.DomainForceCategoryCheckbox"));

		cb.setName(ChartWizard.DOMAIN_FORCE_CATEGORY);

		// cb
		// .setSelected((Boolean)
		// getWizardData(ChartWizard.DOMAIN_FORCE_CATEGORY));

		getAttribJComboBoxFor(0).addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() != ItemEvent.SELECTED)
					return;

				if (getAttribJComboBoxFor(0).isNumericalAttribSelected()) {
					/* A numerical attribute */
					cb.setEnabled(true);
				} else {
					/* A numerical attribute */
					cb.setEnabled(false);
					cb.setSelected(true);
				}
			}
		});
		return cb;
	}

	//
	// /**
	// * Caches the List of numerical attributes because it can be expensive
	// with
	// * network datastores.
	// *
	// * @param featureSource
	// * @return
	// */
	// private List<String> getNumericaAttribs(FeatureSource featureSource) {
	// if (numericalAttNames == null)
	// numericalAttNames = FeatureUtil.getNumericalFieldNames(featureSource);
	// return numericalAttNames;
	// }

	private JCheckBox getDomainSortedJCheckbox() {
		JCheckBox cb = new JCheckBox(GeopublisherGUI
				.R("AttributeSelectionPanel.DomainSortCheckbox"));
		cb.setToolTipText(GeopublisherGUI
				.R("AttributeSelectionPanel.DomainSortCheckbox.TT"));

		cb.setName(ChartWizard.SORT_DOMAIN_AXIS);
		/*
		 * Initialize with the value from the map
		 */
		// cb.setSelected((Boolean)getWizardData(ChartWizard.SORT_DOMAIN_AXIS));
		return cb;
	}

	public JLabel getDescriptionPanel() {
		if (descriptionPanel == null) {
			ChartType chartType = (ChartType) getWizardData(ChartWizard.CHARTTYPE);
			descriptionPanel = new JLabel(GeopublisherGUI
					.R("AttributeSelectionPanel.Explanation", chartType
							.getTitle()));

		}

		return descriptionPanel;
	}

	/**
	 * Somewhat ugly, we can only access the WizardData, once the Panel has been
	 * displayed. So we have to make all changes that depend on earlier
	 * decisions here:
	 */
	@Override
	protected void renderingPage() {

		if (lastChartType != getWizardData(ChartWizard.CHARTTYPE)) {
			/*
			 * We have to clean all cahced Components then, because otherwise
			 * the listerners...
			 */
			attributJComboBoxes.clear();
			normalizeJCheckboxs.clear();
			settingsPanels.clear();
			lastChartType = (ChartType) getWizardData(ChartWizard.CHARTTYPE);
			// LOGGER.debug("CHART TYPE CHANGED CLEARED");
			initGUI();
		} else {
			// LOGGER.debug("CHART TYPE CHANGED NOT CLEARED");
		}

	};

	/**
	 * Creates the {@link AttributesJComboBox} for the second and more
	 * attributes
	 * 
	 * @param idx
	 * @return
	 */
	public AttributesJComboBox getAttribJComboBoxFor(final int idx) {
		if (attributJComboBoxes.get(idx) == null) {

			/*
			 * Extract values from the wizardDataMap
			 */
			final FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = (FeatureSource<SimpleFeatureType, SimpleFeature>) getWizardData(ChartWizard.FEATURESOURCE);
			AttributeMetadataMap attributeMetaDataMap = (AttributeMetadataMap) getWizardData(ChartWizard.ATTRIBUTEMETADATAMAP);

			Vector<String> dataArray;
			/*
			 * Does the ChartType allow non-numerical values on the domain axis?
			 */
			final ChartType chartType = (ChartType) getWizardData(ChartWizard.CHARTTYPE);
			if (idx == 0 && chartType.isCategoryAllowedForDomainAxis()) {
				dataArray = ASUtil.getValueFieldNames(featureSource, false);
			} else {
				dataArray = FeatureUtil.getNumericalFieldNames(featureSource.getSchema(), false);
			}

			/* Add the null as an item to the optional drop down lists only */
			if (idx > 0 && idx > chartType.getMinDimensions() - 1)
				dataArray.add(0,null);

			final AttributesJComboBox jcombo = new AttributesJComboBox(
					featureSource.getSchema(), attributeMetaDataMap,
					new DefaultComboBoxModel(dataArray));

			jcombo.setName(ChartWizard.ATTRIBUTE_ + idx);

			/*
			 * Add a Listener that activates or de-activates the JComboBoxes
			 */
			jcombo.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() != ItemEvent.SELECTED)
						return;

					String selection = null;

					Object[] selectedObjects = e.getItemSelectable()
							.getSelectedObjects();
					if (selectedObjects != null && selectedObjects.length > 0)
						selection = (String) selectedObjects[0];

					/*
					 * When setting it to something, we remove the NULL from the
					 * previous JComoBox
					 */
					if (selection != null && idx > 1) {
						// LOGGER.debug("sel != null && idx-1=" + (idx - 1));
						// LOGGER.debug("removing the null from " + (idx - 1));
						Object backupSelection = getAttribJComboBoxFor(idx - 1)
								.getSelectedItem();
						getAttribJComboBoxFor(idx - 1).setModel(
								new DefaultComboBoxModel(ASUtil
										.getValueFieldNames(featureSource, false)));
						getAttribJComboBoxFor(idx - 1).setSelectedItem(
								backupSelection);
					} else if (selection == null
							&& idx - 1 > chartType.getMinDimensions() - 1) {
						// LOGGER.debug("adding the null to " + (idx - 1));
						Object backupSelection = getAttribJComboBoxFor(idx - 1)
								.getSelectedItem();
						Vector<String> dataArray = ASUtil.getValueFieldNames(
								featureSource, false);
						dataArray.insertElementAt(null, 0);
						getAttribJComboBoxFor(idx - 1).setModel(
								new DefaultComboBoxModel(dataArray));
						getAttribJComboBoxFor(idx - 1).setSelectedItem(
								backupSelection);

						// LOGGER.debug("adding the null ");
					}

					if (attributJComboBoxes.containsKey(idx + 1)) {
						// LOGGER.debug(idx + 1 + " exists.");
						/* If a next JComboBox exists */
						if (selection != null) {
							// LOGGER.debug("sel != null. setting it to enabled.");

							getAttribJComboBoxFor(idx + 1).setEnabled(true);
							getLabelFor(idx + 1).setEnabled(true);
//							getNormalizeJCheckboxFor(idx + 1).setEnabled(true);

						} else {
							// LOGGER
							// .debug("sel == null. setting it to disabled.");
							getAttribJComboBoxFor(idx + 1).setEnabled(false);
							getLabelFor(idx + 1).setEnabled(false);
//							getNormalizeJCheckboxFor(idx + 1).setEnabled(false);

							getAttribJComboBoxFor(idx + 1).setSelectedItem(
									(Object) null);
						}
					}
				}
			});

			jcombo.setSelectedItem(null);

			attributJComboBoxes.put(idx, jcombo);

		}
		return attributJComboBoxes.get(idx);
	}

	public static void main(String[] args) {
		JDialog d = new JDialog();
		d.setContentPane(new AttributeSelectionWizardPanel());
		d.pack();
		d.setVisible(true);
	}

	@Override
	protected CustomComponentListener createCustomComponentListener() {
		return new CCL();
	}

	private static final class CCL extends CustomComponentListener implements
			ActionListener {
		private CustomComponentNotifier notifier;

		@Override
		public boolean accept(Component c) {
			return c instanceof AttributesJComboBox;
		}

		@Override
		public void startListeningTo(Component c, CustomComponentNotifier n) {
			notifier = n;
			((AttributesJComboBox) c).addActionListener(this);
		}

		@Override
		public void stopListeningTo(Component c) {
			((AttributesJComboBox) c).removeActionListener(this);
		}

		@Override
		public Object valueFor(Component c) {
			return ((AttributesJComboBox) c).getSelectedItem();
		}

		public void actionPerformed(ActionEvent e) {
			notifier.userInputReceived((Component) e.getSource(), e);
		}
	}

}
