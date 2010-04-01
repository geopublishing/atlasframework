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
package org.geopublishing.geopublisher.chart;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.AtlasCreator;
import org.jfree.chart.ChartPanel;

import schmitzm.jfree.chart.style.ChartLabelStyle;
import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.swing.JPanel;
import skrueger.i8n.Translation;
import skrueger.swing.ColorButton;
import skrueger.swing.TranslationEditJPanel;

public class GeneralChartSettingsJPanel extends JPanel {
	
	// Identifies the property change event
	public static final String PROPERTYNAME_CHART_STYLE = "chartStyle";
	
	static final protected Logger LOGGER = Logger
			.getLogger(GeneralChartSettingsJPanel.class);
	private final ChartStyle chartStyle;
	private TranslationEditJPanel titleTranslationEditPanel;
	private TranslationEditJPanel descTranslationEditPanel;
	private final AtlasConfigEditable atlasConfigEditable;
	private ColorButton titleColorJButton;
	private ColorButton subTitleColorJButton;
	private ColorButton chartBackgroundColorJButton;
	private JCheckBox chartBackgroundJCheckBox;

	public GeneralChartSettingsJPanel(ChartStyle chartStyle,
			AtlasConfigEditable atlasConfigEditable) {
		super(new MigLayout("w "+(ChartPanel.DEFAULT_WIDTH-40)+",wrap 2","[grow]"));
		this.chartStyle = chartStyle;
		this.atlasConfigEditable = atlasConfigEditable;

		add(getTitleTranslationEditPanel(), "span 2, growx");
		add(getDescTranslationEditPanel(), "span 2, growx");
		add(getColorsPanel(), "span 2, growx");
		
	}

	private JPanel getColorsPanel() {
		JPanel colorsPanel = new JPanel(new MigLayout("wrap 2, align center"), AtlasStyler.R("colors.border.title"));
		colorsPanel.add(getTitleColorButton());
		colorsPanel.add(getSubTitleColorButton());

		return colorsPanel;
	}

	/**
	 * This {@link JCheckBox} controls whether a background color is set to
	 * <code>null</code>.
	 */
	private JCheckBox getChartBackgroundColorJCheckbox() {
		if (chartBackgroundJCheckBox == null) {
			chartBackgroundJCheckBox = new JCheckBox(new AbstractAction(
					"automatisch") {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (chartBackgroundJCheckBox.isSelected()) {
						chartStyle.setBackground(null);
						getChartBackgroundColorButton().setEnabled(false);
					} else {
						getChartBackgroundColorButton().setEnabled(true);

						chartStyle.setBackground(AVSwingUtil.showColorChooser(
								chartBackgroundColorJButton,
								"Hintergrundfarbe des Charts", chartStyle // i8n
										.getBackground()));
						chartBackgroundColorJButton.setColor(chartStyle
								.getBackground());

						GeneralChartSettingsJPanel.this.firePropertyChange(
								PROPERTYNAME_CHART_STYLE, null, chartStyle);
					}
					GeneralChartSettingsJPanel.this.firePropertyChange(
							PROPERTYNAME_CHART_STYLE, null, chartStyle);
				}

			});
		}
		return chartBackgroundJCheckBox;
	}

	/**
	 * A change event of property "chartStyle" is fired when the color is
	 * changed.
	 */
	private ColorButton getSubTitleColorButton() {

		if (subTitleColorJButton == null) {
			subTitleColorJButton = new ColorButton(new AbstractAction(
					"Untertitel") {

				@Override
				public void actionPerformed(ActionEvent e) {
					chartStyle.getDescStyle().setPaint(
							AVSwingUtil.showColorChooser(subTitleColorJButton,
									"Farbe des Untertitels", chartStyle
											.getDescStyle().getPaint()));

					subTitleColorJButton.setColor(chartStyle.getDescStyle()
							.getPaint());
					

					GeneralChartSettingsJPanel.this.firePropertyChange(
							PROPERTYNAME_CHART_STYLE, null, chartStyle);
				}

			});

			/*
			 * Ensure that there are no NULLs
			 */
			if (chartStyle.getDescStyle() == null) {
				chartStyle.setDescStyle(new ChartLabelStyle());
				chartStyle.getDescStyle().setPaint(Color.gray);
			}

			subTitleColorJButton.setColor(chartStyle.getDescStyle().getPaint());

		}
		return subTitleColorJButton;
	}

	/**
	 * A change event of property "chartStyle" is fired when the color is
	 * changed.
	 */
	private JButton getChartBackgroundColorButton() {

		if (chartBackgroundColorJButton == null) {
			chartBackgroundColorJButton = new ColorButton(new AbstractAction(
					"Hintergrund") {

				@Override
				public void actionPerformed(ActionEvent e) {
					chartStyle.setBackground(AVSwingUtil.showColorChooser(
							chartBackgroundColorJButton,
							"Hintergrundfarbe des Charts", chartStyle
									.getBackground()));
					chartBackgroundColorJButton.setColor(chartStyle
							.getBackground());

					GeneralChartSettingsJPanel.this.firePropertyChange(
							PROPERTYNAME_CHART_STYLE, null, chartStyle);
				}

			});

			/*
			 * Ensure that there are no NULLs
			 */
			if (chartStyle.getBackground() == null)
				chartStyle.setBackground(Color.WHITE);

			chartBackgroundColorJButton.setColor(chartStyle.getBackground());

		}
		return chartBackgroundColorJButton;
	}

	/**
	 * A change event of property "chartStyle" is fired when the color is
	 * changed.
	 */
	private ColorButton getTitleColorButton() {

		if (titleColorJButton == null) {
			titleColorJButton = new ColorButton(new AbstractAction("Überschrift") {

				@Override
				public void actionPerformed(ActionEvent e) {
					chartStyle.getTitleStyle().setPaint(
							AVSwingUtil.showColorChooser(titleColorJButton,
									"Farbe der Diagrammüberschrift", chartStyle
											.getTitleStyle().getPaint()));

					titleColorJButton.setColor(chartStyle
							.getTitleStyle().getPaint());

					GeneralChartSettingsJPanel.this.firePropertyChange(
							PROPERTYNAME_CHART_STYLE, null, chartStyle);
				}

			});

			/*
			 * Ensure that there are no NULLs
			 */
			if (chartStyle.getTitleStyle() == null)
				chartStyle.setTitleStyle(new ChartLabelStyle());
			if (chartStyle.getTitleStyle().getPaint() == null)
				chartStyle.getTitleStyle().setPaint(Color.black);

			titleColorJButton.setColor( chartStyle
					.getTitleStyle().getPaint());

		}
		return titleColorJButton;
	}

	public TranslationEditJPanel getTitleTranslationEditPanel() {
		if (titleTranslationEditPanel == null) {

			Translation translation = chartStyle.getTitleStyle()
					.getLabelTranslation();
			if (translation == null)
				translation = new Translation();

			titleTranslationEditPanel = new TranslationEditJPanel(translation,
					getLanguages());
		}
		titleTranslationEditPanel.setBorder(BorderFactory
				.createTitledBorder(AtlasCreator
						.R("ManageChartsForMapDialog.ColumnName.2")));
		titleTranslationEditPanel.getTranslation()
				.addTranslationChangeListener(actionListenerTitle);

		return titleTranslationEditPanel;
	}

	private List<String> getLanguages() {
		return atlasConfigEditable.getLanguages();
	}

	public TranslationEditJPanel getDescTranslationEditPanel() {
		if (descTranslationEditPanel == null) {

			Translation translation = chartStyle.getDescStyle()
					.getLabelTranslation();
			if (translation == null)
				translation = new Translation();

			descTranslationEditPanel = new TranslationEditJPanel(translation,
					getLanguages());
		}
		descTranslationEditPanel.setBorder(BorderFactory
				.createTitledBorder(AtlasCreator
						.R("ManageChartsForMapDialog.ColumnName.3")));

		descTranslationEditPanel.getTranslation().addTranslationChangeListener(
				actionListenerDescPanel);

		return descTranslationEditPanel;
	}

	/*
	 * Editing the desc text will automatically update the chart
	 */
	ActionListener actionListenerDescPanel = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			GeneralChartSettingsJPanel.this.firePropertyChange(
					PROPERTYNAME_CHART_STYLE, null, chartStyle);
		}
	};

	/*
	 * Editing the title text will automatically update the chart
	 */
	ActionListener actionListenerTitle = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			LOGGER.debug("ACtionlistener for the tpanel called "
					+ chartStyle.getTitleStyle()
							.getLabelTranslation());
			GeneralChartSettingsJPanel.this.firePropertyChange(
					PROPERTYNAME_CHART_STYLE, null, chartStyle);
		}
	};

}
