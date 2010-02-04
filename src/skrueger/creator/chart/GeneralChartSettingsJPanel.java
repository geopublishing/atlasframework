package skrueger.creator.chart;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import schmitzm.jfree.chart.style.ChartLabelStyle;
import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.jfree.feature.style.FeatureChartStyle;
import schmitzm.swing.JPanel;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.AtlasCreator;
import skrueger.i8n.Translation;
import skrueger.sld.ASUtil;
import skrueger.sld.AtlasStyler;
import skrueger.swing.ColorButton;
import skrueger.swing.TranslationEditJPanel;

public class GeneralChartSettingsJPanel extends JPanel {
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
		super(new MigLayout("wrap 2"));
		this.chartStyle = chartStyle;
		this.atlasConfigEditable = atlasConfigEditable;

		add(getTitleTranslationEditPanel(), "span 2, sgx");
		add(getDescTranslationEditPanel(), "span 2, sgx");
		add(getColorsPanel(), "span 2, sgx");
		
	}

	private JPanel getColorsPanel() {
		JPanel colorsPanel = new JPanel(new MigLayout("wrap 2, align center"));
		colorsPanel.add(getTitleColorButton());
		colorsPanel.add(getSubTitleColorButton());
		// colorsPanel.add(getChartBackgroundColorButton());
		// colorsPanel.add(getChartBackgroundColorJCheckbox(), "span 3, right");

		colorsPanel.setBorder(BorderFactory.createTitledBorder(AtlasStyler.R("colors"))); 
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

						chartStyle.setBackground(ASUtil.showColorChooser(
								chartBackgroundColorJButton,
								"Hintergrundfarbe des Charts", chartStyle // i8n
										.getBackground()));
						chartBackgroundColorJButton.setColor(chartStyle
								.getBackground());

						GeneralChartSettingsJPanel.this.firePropertyChange(
								"chartStyle", null, chartStyle);
					}
					GeneralChartSettingsJPanel.this.firePropertyChange(
							"chartStyle", null, chartStyle);
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
							ASUtil.showColorChooser(subTitleColorJButton,
									"Farbe des Untertitels", chartStyle
											.getDescStyle().getPaint()));

					subTitleColorJButton.setColor(chartStyle.getDescStyle()
							.getPaint());

					GeneralChartSettingsJPanel.this.firePropertyChange(
							"chartStyle color", null, chartStyle);
				}

			});

			/*
			 * Ensure that there are no NULLs
			 */
			if (chartStyle.getDescStyle() == null)
				chartStyle.setDescStyle(new ChartLabelStyle());
			if (chartStyle.getDescStyle().getPaint() == null)
				chartStyle.getDescStyle().setPaint(Color.black);

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
					chartStyle.setBackground(ASUtil.showColorChooser(
							chartBackgroundColorJButton,
							"Hintergrundfarbe des Charts", chartStyle
									.getBackground()));
					chartBackgroundColorJButton.setColor(chartStyle
							.getBackground());

					GeneralChartSettingsJPanel.this.firePropertyChange(
							"chartStyle", null, chartStyle);
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
							ASUtil.showColorChooser(titleColorJButton,
									"Farbe der Diagrammüberschrift", chartStyle
											.getTitleStyle().getPaint()));

					titleColorJButton.setColor(chartStyle
							.getTitleStyle().getPaint());

					GeneralChartSettingsJPanel.this.firePropertyChange(
							"chartStyle", null, chartStyle);
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
					"chartStyle", null, chartStyle);
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
					"chartStyle", null, chartStyle);
		}
	};

}
