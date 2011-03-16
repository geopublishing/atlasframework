package org.geopublishing.atlasStyler.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.classification.FeatureClassification;
import org.geopublishing.atlasViewer.swing.AVDialogManager;
import org.geopublishing.atlasViewer.swing.Icons;

import de.schmitzm.geotools.data.amd.AttributeMetadataInterface;
import de.schmitzm.geotools.gui.AtlasFeatureLayerFilterDialog;
import de.schmitzm.i18n.I18NUtil;
import de.schmitzm.swing.ExceptionDialog;
import de.schmitzm.swing.JPanel;

public class FeatureClassificationGUI extends ClassificationGUI {

	private JButton jButtonExclusion = null;

	public FeatureClassificationGUI(Component owner,
			FeatureClassification classifier, AtlasStylerVector atlasStyler,
			String title) {
		super(owner, classifier, atlasStyler, title);
	}

	/**
	 * This button opens the AttributeTable for features
	 */
	private JButton getJButtonAttribTable() {
		JButton button = new JButton(
				new AbstractAction(
						ASUtil.R("QuantitiesClassificationGUI.Data.ShowAttribTableButton"),
						Icons.ICON_TABLE) {

					@Override
					public void actionPerformed(ActionEvent e) {
						/*
						 * If possible, set the JMapPane as the parent GUI. If
						 * now available, use this dialog
						 */
						ClassificationGUI owner = FeatureClassificationGUI.this;

						AVDialogManager.dm_AttributeTable.getInstanceFor(
								getAtlasStyler().getStyledFeatures(), owner,
								getAtlasStyler().getStyledFeatures(), null); // TODO
						// TODO
						// atlasStyler.getMapLegend()
						// TODO
						// TODO
					}

				});
		return button;
	}

	private AtlasStylerVector getAtlasStyler() {
		return (AtlasStylerVector) atlasStyler;
	}

	private JPanel jPanelData = null;

	/**
	 * This method initializes jPanel1
	 * 
	 * @return javax.swing.JPanel
	 */
	@Override
	protected JPanel getJPanelData() {
		if (jPanelData == null) {
			jPanelData = new JPanel(new MigLayout("gap 1, inset 1"));
			jPanelData.setBorder(BorderFactory.createTitledBorder(ASUtil
					.R("QuantitiesClassificationGUI.Data.BorderTitle")));

			if (atlasStyler.getMapLayer() != null) {
				jPanelData.add(getJButtonExclusion());
			}

			jPanelData.add(getJButtonAttribTable());
			// SwingUtil.setPreferredWidth(jPanelData, 100);
			// jPanel1.add(getJButtonSampling(), gridBagConstraints6);
		}
		return jPanelData;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonExclusion() {
		if (jButtonExclusion == null) {
			jButtonExclusion = new JButton();

			jButtonExclusion.setAction(new AbstractAction(ASUtil
					.R("QuantitiesClassificationGUI.DataExclusion.Button")) {

				@Override
				public void actionPerformed(ActionEvent e) {

					final AtlasFeatureLayerFilterDialog filterDialog;
					try {

						// This is all ugly ;-/
						// TODO GP_Dialogmanager

						filterDialog = new AtlasFeatureLayerFilterDialog(
								FeatureClassificationGUI.this, getAtlasStyler()
										.getStyledFeatures(), null,
								getAtlasStyler().getMapLayer());
						// ,
						// atlasStyler TODO TODO
						// .getMapLegend().getGeoMapPane()
						// .getMapPane() TODO TODO

						// TODO listen to any filter changes?

						filterDialog.setVisible(true);

					} catch (Exception ee) {
						LOGGER.error(ee);
						ExceptionDialog.show(FeatureClassificationGUI.this, ee);
					}
				}

			});
			jButtonExclusion.setToolTipText(ASUtil
					.R("QuantitiesClassificationGUI.DataExclusion.Button.TT"));
		}
		return jButtonExclusion;
	}

	/**
	 * This method creates the Histogram image with JFreeChart
	 */
	@Override
	protected BufferedImage getHistogramImage() {
		try {

			/**
			 * Label the x-axis. If a NormalizerField has been selected, this
			 * has to be presented here as well. Where possible use the
			 * AttributeMetaData information.
			 */
			String label_xachsis;
			{
				AttributeMetadataInterface amdValue = getAtlasStyler()
						.getAttributeMetaDataMap().get(
								getClassifier().getValue_field_name());

				// AttributeMetadata amdValue = ASUtil.getAttributeMetadataFor(
				// atlasStyler, getValue_field_name());
				if (amdValue != null
						&& (!I18NUtil.isEmpty(amdValue.getTitle().toString()))) {
					label_xachsis = amdValue.getTitle().toString();
				} else
					label_xachsis = getClassifier().getValue_field_name();

				if (getClassifier().getNormalizer_field_name() != null) {
					// AttributeMetadata amdNorm =
					// ASUtil.getAttributeMetadataFor(
					// atlasStyler, getNormalizer_field_name());
					AttributeMetadataInterface amdNorm = getAtlasStyler()
							.getAttributeMetaDataMap().get(
									getClassifier().getNormalizer_field_name());
					if (amdNorm != null
							&& (!I18NUtil
									.isEmpty(amdNorm.getTitle().toString()))) {
						label_xachsis += "/" + amdNorm.getTitle().toString();
					} else
						label_xachsis += "/"
								+ getClassifier().getNormalizer_field_name();
				}
			}

			return classifier.createHistogramImage(getJCheckBoxShowMean()
					.isSelected(), getJCheckBoxShowSD().isSelected(),
					histogramBins, label_xachsis);
		} catch (Exception e) {
			LOGGER.error("Error creating histogram image", e);
			return ERROR_IMAGE;
		}

	}

	private FeatureClassification getClassifier() {
		return (FeatureClassification) classifier;
	}
}
