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
package skrueger.creator.gui;

import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.WeakHashMap;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geotools.feature.FeatureCollection;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.opengis.feature.simple.SimpleFeatureType;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.gui.SelectableXMapPane;
import schmitzm.geotools.gui.XMapPane;
import schmitzm.jfree.chart.selection.DatasetSelectionModel;
import schmitzm.jfree.chart.style.ChartAxisStyle;
import schmitzm.jfree.chart.style.ChartPlotStyle;
import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.jfree.chart.style.ChartType;
import schmitzm.jfree.feature.AggregationFunction;
import schmitzm.jfree.feature.AggregationFunctionJComboBox;
import schmitzm.jfree.feature.FeatureDatasetSelectionModel;
import schmitzm.jfree.feature.style.FeatureChartAxisStyle;
import schmitzm.jfree.feature.style.FeatureChartStyle;
import schmitzm.jfree.feature.style.FeatureChartUtil;
import schmitzm.swing.ExceptionDialog;
import schmitzm.swing.JPanel;
import schmitzm.swing.SwingUtil;
import skrueger.AttributeMetadata;
import skrueger.atlas.AVDialogManager;
import skrueger.atlas.AVUtil;
import skrueger.atlas.AtlasViewer;
import skrueger.atlas.dp.layer.DpLayerVectorFeatureSource;
import skrueger.atlas.gui.AtlasChartJDialog;
import skrueger.atlas.gui.AtlasChartJPanel;
import skrueger.atlas.gui.MapLegend;
import skrueger.atlas.gui.internal.AtlasStatusDialog;
import skrueger.atlas.gui.map.AtlasMapLegend;
import skrueger.atlas.gui.plaf.BasicMapLayerLegendPaneUI;
import skrueger.atlas.swing.AtlasSwingWorker;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.AtlasCreator;
import skrueger.creator.GPDialogManager;
import skrueger.creator.chart.GeneralChartSettingsJPanel;
import skrueger.geotools.StyledFeaturesInterface;
import skrueger.geotools.selection.ChartSelectionSynchronizer;
import skrueger.geotools.selection.StyledFeatureLayerSelectionModel;
import skrueger.geotools.selection.StyledLayerSelectionModel;
import skrueger.geotools.selection.StyledLayerSelectionModelSynchronizer;
import skrueger.i8n.Translation;
import skrueger.sld.ASUtil;
import skrueger.sld.gui.AttributesJComboBox;
import skrueger.swing.CancellableDialogAdapter;
import skrueger.swing.ColorButton;
import skrueger.swing.TranslationEditJPanel;

/**
 * This {@link JDialog} allows to interactively edit a {@link FeatureChartStyle}
 * . The preview window automatically reflects the changes.
 * 
 * @author Stefan A. Krüger
 * 
 */
public class DesignAtlasChartJDialog extends CancellableDialogAdapter {
	final static Logger LOGGER = Logger
			.getLogger(DesignAtlasChartJDialog.class);

	/**
	 * The atlas configuration this chart belongs to.
	 */
	private final AtlasConfigEditable atlasConfigEditable;

	private JTabbedPane jTabbedPane;

	private JPanel generalSettingsJPanel;

	private HashMap<Integer, JPanel> legendTitleTranslationEditPanels = new HashMap<Integer, JPanel>();

	private HashMap<Integer, JButton> seriesColorButtons = new HashMap<Integer, JButton>();

	/**
	 * Remembers the position of panels in the {@link JTabbedPane} created by
	 * {@link #getTabbedPane()}. The key is calculated by
	 * <code>renderIdx * 1000 + seriesIdx</code>
	 **/
	private HashMap<Integer, Integer> rememberTabPos = new HashMap<Integer, Integer>();

	/** Automatically apply any changes to the {@link ChartStyle} to the chart? */
	private boolean autoUpdateMode = false;

	/**
	 * If set to <code>true</code>, the next update includes a reapplying the
	 * {@link ChartStyle} to the {@link FeatureCollection}
	 */
	private boolean reapplyChartStyleToFeatureCollection = false;

	/** A button to manually update the chart **/
	private JButton updateChartJButton;

	/** A {@link Checkbox} to switch between automatic or manual update mode **/
	private JCheckBox updateModeJCheckbox;

	private JCheckBox categoryDomainAxisJCheckBox;

	private HashMap<Integer, JCheckBox> shapesVisibleJCheckBoxes = new HashMap<Integer, JCheckBox>();

	/**
	 * The {@link FeatureChartStyle} that is being edited here. The GUI is
	 * changing the object directly.
	 **/
	private final FeatureChartStyle chartStyle;

	/**
	 * A backup of the original chartStyle created with
	 * {@link FeatureChartStyle#copyTo(ChartStyle)}
	 **/
	private FeatureChartStyle backupChartStyle;

	/**
	 * A reference to the {@link MapLegend} of the {@link XMapPane} that shows
	 * the geometries for the data-points.
	 **/
	private final AtlasMapLegend mapLegend;

	/**
	 * A reference to the {@link SelectableXMapPane} that shows the geometries
	 * for the data-points.
	 **/
	private SelectableXMapPane mapPane;

	/**
	 * The {@link StyledFeaturesInterface} this {@link FeatureChartStyle} is
	 * based on
	 **/
	private final StyledFeaturesInterface<?> styledLayer;

	/**
	 * This {@link AtlasChartJPanel} shows previews of the chart.
	 */
	private AtlasChartJPanel chartPanel;

	/**
	 * Creates a GUI that can change properties of a {@link FeatureChartStyle}.
	 * The GUI is automatically visible.
	 * 
	 * @param owner
	 *            A {@link Component} that this GUI is related to.
	 * @param chartStyle
	 *            An instance of {@link FeatureChartStyle} to edit
	 * @param mapLegend
	 *            A reference to a {@link MapLegend}. If <code>null</code>, the
	 *            selection will not be synchronized with any {@link XMapPane}.
	 * @param styledFeatures
	 *            The features to calculate the chart on
	 * @param atlasConfigEditable
	 *            The atlas configuration this chart is being created for.
	 */
	public DesignAtlasChartJDialog(final Component owner,
			final FeatureChartStyle chartStyle, final AtlasMapLegend mapLegend,
			final StyledFeaturesInterface<?> styledFeatures,
			AtlasConfigEditable atlasConfigEditable) {

		super(owner);
		this.chartStyle = chartStyle;

		this.mapLegend = mapLegend;

		this.styledLayer = styledFeatures;

		this.mapPane = mapLegend != null ? (mapLegend.getGeoMapPane() != null ? mapLegend
				.getGeoMapPane().getMapPane()
				: null)
				: null;

		// GPDialogManager.dm_MapComposer.getInstanceFor(mapLegend.getm,
		// factory)

		this.atlasConfigEditable = atlasConfigEditable;

		backup();

		try {
			initGUI();
			SwingUtil.setRelativeFramePosition(this, owner,
					SwingUtil.BOUNDS_OUTER, SwingUtil.NORTHEAST);
			setVisible(true);
		} catch (RuntimeException e) {
			cancel();
			throw (e);
		}
	}

	/**
	 * Stores a copy of {@link FeatureChartStyle} in
	 * {@link DesignAtlasChartJDialog#backupChartStyle}.
	 */
	private void backup() {
		backupChartStyle = (FeatureChartStyle) chartStyle.copy();
	}

	/**
	 * Copies all values of the {@link DesignAtlasChartJDialog#backupChartStyle}
	 * to the original {@link FeatureChartStyle} without replacing the object.
	 */
	@Override
	public void cancel() {
		backupChartStyle.copyTo(chartStyle);
	}

	protected void initGUI() {
		setTitle(chartStyle.getTitleStyle().getLabel());

		JPanel contentPane = new JPanel(new MigLayout("wrap 1"));

		contentPane.add(getChartPanel(mapLegend, styledLayer));

		// Adds an ACTION button to zoom to the full extent of the chart
		chartPanel.getToolBar().add(
				AtlasChartJDialog.createZoomToFullChartExtentButton(chartPanel,
						chartStyle), 0);

		contentPane.add(getTabbedPane());

		getChartPanel(mapLegend, styledLayer).addZoomToFeatureExtends(mapPane,
				mapLegend, styledLayer, getChartPanel(mapLegend, styledLayer));

		contentPane.add(getButtonsPane());

		setContentPane(contentPane);

		// super.addZoomToFeatureExtends();

		SwingUtil.setPreferredWidth(this, ChartPanel.DEFAULT_WIDTH);

		pack();
	}

	/**
	 * Creates the buttons
	 */
	private Component getButtonsPane() {
		JPanel buttons = new JPanel(new MigLayout());

		buttons.add(getUpdateModeJCheckbox());
		buttons.add(getUpdateChartJButton());

		JButton attributes = new JButton(
				new AbstractAction(AtlasViewer.R("LayerToolMenu.table"),
						BasicMapLayerLegendPaneUI.ICON_TABLE) {

					@Override
					public void actionPerformed(ActionEvent e) {

						AVDialogManager.dm_AttributeTable.getInstanceFor(
								styledLayer, DesignAtlasChartJDialog.this,
								styledLayer, mapLegend);
					}

				});
		buttons.add(attributes);

		buttons.add(getOkButton(), "right, tag ok");
		buttons.add(getCancelButton(), "right, tag cancel");

		return buttons;
	}

	/**
	 * Returns a labeled {@link JCheckBox} that controls the auto/manual preview
	 * mode.
	 */
	private Component getUpdateModeJCheckbox() {
		if (updateModeJCheckbox == null) {

			updateModeJCheckbox = new JCheckBox(new AbstractAction(AtlasCreator
					.R("DesignAtlasChartJDialog.previewModeCheckboxLabel")) {

				@Override
				public void actionPerformed(ActionEvent e) {

					setAutoUpdateMode(!isAutoUpdateMode());
					getUpdateChartJButton().setEnabled(!isAutoUpdateMode());

					/* If it has just been switched on, trigger an update */
					if (autoUpdateMode) {
						updateChart();
					}
				}
			});

			updateModeJCheckbox.setToolTipText(AtlasCreator
					.R("DesignAtlasChartJDialog.previewModeCheckboxLabel.TT"));
		}
		return updateModeJCheckbox;
	}

	/**
	 * This {@link JButton} can be used to manually apply the {@link ChartStyle}
	 * to the {@link JFreeChart}
	 */
	private JButton getUpdateChartJButton() {

		if (updateChartJButton == null) {

			updateChartJButton = new JButton(new AbstractAction(AtlasCreator
					.R("DesignAtlasChartJDialog.previewChartButton")) {

				@Override
				public void actionPerformed(ActionEvent e) {
					updateChart();
				}

			});

			updateChartJButton.setEnabled(!isAutoUpdateMode());
		}
		return updateChartJButton;
	}

	/**
	 * The {@link JTabbedPane} that manages the different types of
	 * chartStyle-settings.
	 */
	private JTabbedPane getTabbedPane() {
		if (jTabbedPane == null) {
			jTabbedPane = new JTabbedPane();

			int countTabsPos = 0;
			jTabbedPane.add(new JScrollPane(getGeneralSettingsPanel(),
					ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
			jTabbedPane.setTitleAt(countTabsPos++, AtlasCreator
					.R("DesignAtlasChartJDialog.Tabs.General.Title"));

			jTabbedPane.add(new JScrollPane(getChartPlotStylePanel(),
					ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
			jTabbedPane.setTitleAt(countTabsPos++, AtlasCreator
					.R("DesignAtlasChartJDialog.Tabs.ChartPlotStyle.Title"));

			jTabbedPane.add(new JScrollPane(
					getAxisPanel(ChartStyle.DOMAIN_AXIS),
					ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
			jTabbedPane.setTitleAt(countTabsPos++, AtlasCreator
					.R("DesignAtlasChartJDialog.Tabs.DomainAxis.Title"));

			jTabbedPane.add(new JScrollPane(
					getAxisPanel(ChartStyle.RANGE_AXIS),
					ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
			jTabbedPane.setTitleAt(countTabsPos++, AtlasCreator
					.R("DesignAtlasChartJDialog.Tabs.RangeAxis1.Title"));

			int renderIdx = 0;
			// if (chartStyle.getType() != ChartType.SCATTER) {
			for (int seriesIdx = 0; seriesIdx < chartStyle.getRendererStyle(
					renderIdx).getSeriesCount(); seriesIdx++) {

				jTabbedPane.add(new JScrollPane(getRendererSettingsPanel(
						renderIdx, seriesIdx),
						ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
						ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));

				jTabbedPane.setTitleAt(countTabsPos, chartStyle.getType()
						.getObjectName());

				rememberTabPos
						.put(renderIdx * 1000 + seriesIdx, countTabsPos++);

				updateTabIcon(renderIdx, seriesIdx);

			}

		}
		return jTabbedPane;
	}

	private JPanel getChartPlotStylePanel() {
		JPanel panel = new JPanel(new MigLayout("w "
				+ (ChartPanel.DEFAULT_WIDTH - 40) + ", wrap 1"));

		// Initialize with non-nulls
		if (chartStyle.getPlotStyle() == null)
			chartStyle.setPlotStyle(new ChartPlotStyle());
		if (chartStyle.getPlotStyle().isDomainGridlineVisible() == null)
			chartStyle.getPlotStyle().setDomainGridlineVisible(true);
		if (chartStyle.getPlotStyle().isRangeGridlineVisible() == null)
			chartStyle.getPlotStyle().setRangeGridlineVisible(true);

		JPanel grids = new JPanel(new MigLayout(), AtlasCreator
				.R("DesignAtlasChartJDialog.Tabs.ChartPlotStyle.Grid.Border"));
		{
			// Domain Axis
			JCheckBox cb = new JCheckBox(
					AtlasCreator
							.R("DesignAtlasChartJDialog.Tabs.ChartPlotStyle.Grid.Domain.visible"));
			cb.setSelected(chartStyle.getPlotStyle().isDomainGridlineVisible());

			cb.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					chartStyle.getPlotStyle().setDomainGridlineVisible(
							e.getStateChange() == ItemEvent.SELECTED);
					fireChartChangedEvent();
				}

			});

			grids.add(cb);
		}

		{
			JCheckBox cb = new JCheckBox(
					AtlasCreator
							.R("DesignAtlasChartJDialog.Tabs.ChartPlotStyle.Grid.Range.visible"));
			cb.setSelected(chartStyle.getPlotStyle().isRangeGridlineVisible());

			cb.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					chartStyle.getPlotStyle().setRangeGridlineVisible(
							e.getStateChange() == ItemEvent.SELECTED);
					fireChartChangedEvent();
				}

			});

			grids.add(cb);
		}

		JPanel axisCross = new JPanel(
				new MigLayout(),
				AtlasCreator
						.R("DesignAtlasChartJDialog.Tabs.ChartPlotStyle.ZeroAxis.Border"));

		// For BAR charts drawing the cross of axis or choosing its color has no
		// function
		if (chartStyle.getType() != ChartType.BAR) {

			// Fill empty values with defaults
			if (chartStyle.getPlotStyle().isCrosshairVisible() == null)
				chartStyle.getPlotStyle().setCrosshairVisible(false);
			if (chartStyle.getPlotStyle().getCrosshairPaint() == null)
				chartStyle.getPlotStyle().setCrosshairPaint(Color.BLACK);

			// Draw cross of axis / Crosshair line
			JCheckBox cb = new JCheckBox(
					AtlasCreator
							.R("DesignAtlasChartJDialog.Tabs.ChartPlotStyle.ZeroAxis.visible"));
			cb.setSelected(chartStyle.getPlotStyle().isCrosshairVisible());
			cb.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					chartStyle.getPlotStyle().setCrosshairVisible(
							e.getStateChange() == ItemEvent.SELECTED);
					fireChartChangedEvent();
				}
			});
			axisCross.add(cb);

			// Color Button for ZeroAxis cross-hair
			final ColorButton colorButton = new ColorButton(chartStyle
					.getPlotStyle().getCrosshairPaint());

			colorButton
					.setAction(new AbstractAction(
							AtlasCreator
									.R("DesignAtlasChartJDialog.Tabs.ChartPlotStyle.ZeroAxis.Color")) {

						@Override
						public void actionPerformed(final ActionEvent e) {
							chartStyle
									.getPlotStyle()
									.setCrosshairPaint(
											ASUtil
													.showColorChooser(
															DesignAtlasChartJDialog.this,
															AtlasCreator
																	.R("DesignAtlasChartJDialog.Tabs.ChartPlotStyle.ZeroAxis.ColorDialog.Title"),
															chartStyle
																	.getPlotStyle()
																	.getCrosshairPaint()));
							colorButton.setColor(chartStyle.getPlotStyle()
									.getCrosshairPaint());

							fireChartChangedEvent();
						}

					});

			axisCross.add(colorButton);
		}

		{
			JCheckBox cb = new JCheckBox(
					AtlasCreator
							.R("DesignAtlasChartJDialog.Tabs.ChartPlotStyle.ZeroAxis.Center"));

			cb.setSelected(chartStyle.getPlotStyle()
					.isCenterOriginSymetrically());

			cb.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					chartStyle.getPlotStyle().setCenterOriginSymetrically(
							e.getStateChange() == ItemEvent.SELECTED);
					fireChartChangedEvent();
				}

			});

			axisCross.add(cb);
		}

		panel.add(grids, "growx, sgx");
		panel.add(axisCross, "growx, sgx");

		if (chartStyle instanceof FeatureChartStyle)
			panel.add(getNormalizationPanel(), "growx, sgx");

		return panel;
	}

	/**
	 * A listener to update the chart when the label is changed while typing. We
	 * need a reference to this listener as the listeners of {@link Translation}
	 * are kept in a {@link WeakHashMap}.
	 */
	private ActionListener listenToDomainLabelChangesAndUpdateChart = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			fireChartChangedEvent();
		}

	};

	private HashMap<Integer, JTextField> unitTextFields = new HashMap<Integer, JTextField>();

	/**
	 * A {@link JPanel} which allows the user to configure the appearance of the
	 * domain axis
	 */
	private JPanel getAxisPanel(int axisNr) {
		final JPanel axisPane = new JPanel(new MigLayout("w "
				+ (ChartPanel.DEFAULT_WIDTH - 40) + ",wrap 2", "[grow]"));

		/*
		 * Ensure that there are no NULLs
		 */
		if (chartStyle.getAxisStyle(axisNr) == null) {
			chartStyle.setAxisStyle(axisNr, new FeatureChartAxisStyle(
					chartStyle));
			fireChartChangedEvent();
		}

		final ChartAxisStyle axisStyle = chartStyle.getAxisStyle(axisNr);
		if (axisStyle.getLabelTranslation() == null) {
			axisStyle.setLabelTranslation(new Translation(atlasConfigEditable
					.getLanguages(), ""));
			fireChartChangedEvent();
		}
		if (axisStyle.getLabelAngle() == null) {
			axisStyle.setLabelAngle(0.);
			fireChartChangedEvent();
		}

		/*
		 * If this is the DOMAIN axis, allow to change the 0. Attribute/Variable
		 */
		if (axisNr == ChartStyle.DOMAIN_AXIS) {
			SimpleFeatureType schema = styledLayer.getFeatureSource()
					.getSchema();

			final List<String> fieldNames;

			if (chartStyle.getType().isCategoryAllowedForDomainAxis()) {
				fieldNames = ASUtil.getValueFieldNames(styledLayer
						.getFeatureSource(), false);
			} else
				fieldNames = FeatureUtil.getNumericalFieldNames(schema, false);
			final AttributesJComboBox attribComboBox = new AttributesJComboBox(
					schema, styledLayer.getAttributeMetaDataMap(), fieldNames);

			final String domainAxisAttributeLocalName = chartStyle
					.getAttributeName(ChartStyle.DOMAIN_AXIS);
			attribComboBox.setSelectedItem(domainAxisAttributeLocalName);

			/** build GUI... */
			final JPanel attPanel = new JPanel(new MigLayout("wrap 1"));

			attPanel.setBorder(BorderFactory.createTitledBorder(AtlasCreator
					.R("DesignAtlasChartJDialog.SeriesDataBorderTitle")));

			attPanel.add(attribComboBox);

			// A JPanel showing the NODATA values for the selected attribute
			final NoDataPanel noDataPanel = new NoDataPanel(styledLayer
					.getAttributeMetaDataMap(), domainAxisAttributeLocalName,
					styledLayer.getSchema());
			// Update the chart whenever the NODATA values changes
			noDataPanel.addPropertyChangeListener(new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if (evt.getPropertyName().equals(
							NoDataPanel.PROPERTY_NODATAVALUES))
						fireChartChangedEvent(true);
				}
			});
			attPanel.add(noDataPanel);

			if (chartStyle.getType() != ChartType.SCATTER) {
				attPanel.add(getDomainSortedJCheckbox(), "growx, right");
				attPanel.add(getCategoryJCheckbox(), "growx, right");
			}

			/**
			 * When the attribute is changed, a lot of things happen:
			 */
			attribComboBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {

					if (e.getStateChange() != ItemEvent.SELECTED)
						return;

					final String attLocalName = (String) attribComboBox
							.getSelectedItem();

					chartStyle.setAttributeName(ChartStyle.DOMAIN_AXIS,
							attLocalName);

					// attribute Metadata for the new attribute
					AttributeMetadata atm = styledLayer
							.getAttributeMetaDataMap().get(attLocalName);

					chartStyle.setNoDataValues(ChartStyle.DOMAIN_AXIS, atm
							.getNodataValues());

					/**
					 * Update the legend labeling with the AttributeMetaData
					 * from the new newly selected attribute
					 */
					{
						AVUtil.applyDefaultTitleAndTranslationToAxis(
								styledLayer, chartStyle,
								ChartStyle.DOMAIN_AXIS, 0, atlasConfigEditable
										.getLanguages());
					}

					/*
					 * You may only force categories, if a numeric att is
					 * selected
					 */
					getCategoryJCheckbox().setEnabled(
							attribComboBox.isNumericalAttribSelected());
					if (!attribComboBox.isNumericalAttribSelected()) {
						getCategoryJCheckbox().setSelected(true);
					}

					// Update the NODATA Panel
					noDataPanel.setAttribute(attLocalName);

					getUnitTextFieldForAxis(ChartStyle.DOMAIN_AXIS).setText(
							atm.getUnit());

					fireChartChangedEvent(true);
				}

			});

			axisPane.add(attPanel, "span 2, growx");

		}

		/*
		 * Axis label
		 */

		Translation domainLabelTranslation = axisStyle.getLabelTranslation();

		final TranslationEditJPanel axisLabelTranslationJPanel = new TranslationEditJPanel(
				domainLabelTranslation, atlasConfigEditable.getLanguages());

		domainLabelTranslation
				.addTranslationChangeListener(listenToDomainLabelChangesAndUpdateChart);

		axisLabelTranslationJPanel.setBorder(BorderFactory
				.createTitledBorder(AtlasCreator
						.R("DesignAtlasChartJDialog.AxisSettings.AxisLabel")));

		axisPane.add(axisLabelTranslationJPanel, "span 2, growx");

		/*
		 * Now add a box to enter a unit string
		 */
		JPanel unitPanel = new JPanel(new MigLayout(), AtlasCreator.R("Unit"));

		/** A textfield that shows the unit (untranslatable). **/
		final JTextField unitTextfield = getUnitTextFieldForAxis(axisNr);

		unitPanel.add(new JLabel(AtlasCreator
				.R("DesignChartDialog.Unit.Explanation")), "wrap");
		unitPanel.add(unitTextfield);
		axisPane.add(unitPanel, "sgy");

		/*
		 * Now add a ANGLE slider or radio buttons
		 */
		JPanel anglePanel = new JPanel(new MigLayout());

		anglePanel.add(new JLabel(AtlasCreator
				.R("DesignAtlasChartJDialog.AxisSettings.ValueLabelAngle")),
				"wrap");

		boolean isANumberAxis = true;
		try {
			isANumberAxis = Number.class.isAssignableFrom(styledLayer
					.getSchema().getDescriptor(
							chartStyle.getAttributeName(axisNr)).getType()
					.getBinding());
		} catch (Exception e) {
			LOGGER
					.warn("Could not determine wherther this is a number axis",
							e);
		}

		if (isANumberAxis) {
			JRadioButton horiz = new JRadioButton(new AbstractAction(
					AtlasCreator.R("horizontal")) {

				@Override
				public void actionPerformed(ActionEvent e) {
					axisStyle.setValuesAngle(0.);
					fireChartChangedEvent();
				}
			});
			horiz.setSelected(axisStyle.getValuesAngle() == 0.);

			JRadioButton vertical = new JRadioButton(new AbstractAction(
					AtlasCreator.R("vertical")) {

				@Override
				public void actionPerformed(ActionEvent e) {
					axisStyle.setValuesAngle(90.);
					fireChartChangedEvent();
				}
			});
			vertical.setSelected(axisStyle.getValuesAngle() == 90.);

			ButtonGroup bg = new ButtonGroup();
			bg.add(horiz);
			bg.add(vertical);

			anglePanel.add(horiz, "wrap");
			anglePanel.add(vertical, "wrap");

		} else {

			final JSlider angleSlider = new JSlider(0, 90, axisStyle
					.getValuesAngle().intValue());

			/*
			 * A numerical axis may only define angle 0 or 90
			 */
			Hashtable<Integer, JComponent> labelTable = new Hashtable<Integer, JComponent>();
			labelTable.put(new Integer(0), new JLabel("0\u00b0"));
			labelTable.put(new Integer(90), new JLabel("90\u00b0"));

			angleSlider.setLabelTable(labelTable);
			angleSlider.setPaintLabels(true);
			angleSlider.setMajorTickSpacing(15);
			angleSlider.setPaintTicks(true);
			angleSlider.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					final Double newAngle = new Double(angleSlider.getValue());
					axisStyle.setValuesAngle(newAngle);
					// LOGGER.debug("Setting angle to "+newAngle);
					fireChartChangedEvent();
				}

			});

			anglePanel.add(angleSlider);
		}
		axisPane.add(anglePanel, "sgy, growx 2000");

		return axisPane;
	}

	private JTextField getUnitTextFieldForAxis(int axisNr) {
		if (unitTextFields.get(axisNr) == null) {
			final JTextField unitTextField = new JTextField(15);

			final ChartAxisStyle axisStyle = chartStyle.getAxisStyle(axisNr);

			unitTextField.setText(axisStyle.getUnitString());
			unitTextField.getDocument().addDocumentListener(
					new DocumentListener() {

						@Override
						public void changedUpdate(DocumentEvent e) {
							react();
						}

						void react() {
							axisStyle.setUnitString(unitTextField.getText());
							fireChartChangedEvent();
						}

						@Override
						public void insertUpdate(DocumentEvent e) {
							react();
						}

						@Override
						public void removeUpdate(DocumentEvent e) {
							react();
						}

					});

			unitTextFields.put(axisNr, unitTextField);

		}
		return unitTextFields.get(axisNr);
	}

	/**
	 * This checkbox defines whether the the domain axis will treat numerical
	 * data as category data
	 */
	private JCheckBox getCategoryJCheckbox() {

		if (categoryDomainAxisJCheckBox == null) {

			categoryDomainAxisJCheckBox = new JCheckBox(AtlasCreator
					.R("AttributeSelectionPanel.DomainForceCategoryCheckbox"));
			categoryDomainAxisJCheckBox.setToolTipText(AtlasCreator
					.R("AttributeSelectionPanel.DomainForceCategoryCheckbox"));

			categoryDomainAxisJCheckBox.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					chartStyle.setForceCategories(categoryDomainAxisJCheckBox
							.isSelected());
					fireChartChangedEvent(true);
				}

			});

			categoryDomainAxisJCheckBox.setSelected(chartStyle
					.isForceCategories());

		}
		return categoryDomainAxisJCheckBox;
	}

	private JCheckBox getDomainSortedJCheckbox() {
		JCheckBox cb = new JCheckBox(AtlasCreator
				.R("AttributeSelectionPanel.DomainSortCheckbox"));
		cb.setToolTipText(AtlasCreator
				.R("AttributeSelectionPanel.DomainSortCheckbox.TT"));

		cb.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {

				chartStyle
						.setSortDomainAxis(e.getStateChange() == ItemEvent.SELECTED);

				fireChartChangedEvent(true);
			}

		});
		cb.setSelected(chartStyle.isSortDomainAxis());
		return cb;
	}

	private void updateTabIcon(int rendererIdx, int seriesIdx) {
		Integer countTabsPos = rememberTabPos.get(rendererIdx * 1000
				+ seriesIdx);
		if (countTabsPos == null) {
			return;
			// throw new RuntimeException("No tab known for renderer "
			// + rendererIdx + " and series " + seriesIdx);
		}

		if (countTabsPos >= jTabbedPane.getTabCount()) {
			return;
		}

		Color seriesColor = chartStyle.getRendererStyle(rendererIdx)
				.getSeriesPaint(seriesIdx);
		if (seriesColor == null) {
			chartStyle.getRendererStyle(rendererIdx).setSeriesPaint(seriesIdx,
					getRandomColor());
			fireChartChangedEvent();
		}

		BufferedImage seriesImage = new BufferedImage(12, 12,
				BufferedImage.TYPE_INT_RGB);
		final Graphics graphics = seriesImage.getGraphics();
		graphics.setColor(seriesColor);
		graphics.fillRect(0, 0, 11, 11);
		ImageIcon seriesColorIcon = new ImageIcon(seriesImage);
		jTabbedPane.setIconAt(countTabsPos, seriesColorIcon);
	}

	/**
	 * Returns a Renderer panel. A listener to propagate PropertyChanges of type
	 * "chartStyle" is registered.
	 * 
	 * @param rendererIndex
	 * @return
	 */
	private JPanel getRendererSettingsPanel(int rendererIndex, int seriesIdx) {

		JPanel rendererSettingsPanel = new JPanel(new MigLayout("w "
				+ (ChartPanel.DEFAULT_WIDTH - 40) + ",wrap 2"));

		/*
		 * Color...
		 */
		rendererSettingsPanel
				.add(getSeriesColorButton(rendererIndex, seriesIdx));

		// The possibility to show/hide shapes is only given for non point
		// layers. otherwise set true!
		if (chartStyle.getType() != ChartType.POINT
				&& chartStyle.getType() != ChartType.SCATTER
				&& chartStyle.getType() != ChartType.BAR) {
			rendererSettingsPanel.add(getShapesVisibleJCheckBoxFor(
					rendererIndex, seriesIdx));
		} else {
			chartStyle.getRendererStyle(rendererIndex).setSeriesShapesVisible(
					seriesIdx, true);
			rendererSettingsPanel.add(new JLabel());
		}

		rendererSettingsPanel.add(getSeriesDataSelectionJPanel(rendererIndex,
				seriesIdx), "span 2, growx");

		/*
		 * Legend...
		 */
		rendererSettingsPanel.add(getLegendTitleTranslationEditPanel(
				rendererIndex, seriesIdx), "span 2, growx");

		rendererSettingsPanel
				.addPropertyChangeListener(new PropertyChangeListener() {

					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						if (evt.getPropertyName().equals("chartStyle")) {
							fireChartChangedEvent();
						}
					}

				});

		return rendererSettingsPanel;
	}

	/**
	 * Returns or creates a {@link JCheckBox} that controlls the shapeVisible
	 * attribute.
	 * 
	 * @param rendererIndex
	 * @param seriesIdx
	 * @return
	 */
	private JCheckBox getShapesVisibleJCheckBoxFor(final int rendererIndex,
			final int seriesIdx) {

		if (shapesVisibleJCheckBoxes.get(rendererIndex * 1000 + seriesIdx) == null) {
			final JCheckBox cb = new JCheckBox();

			/* */
			final Boolean preset = chartStyle.getRendererStyle(rendererIndex)
					.isSeriesShapesVisible(seriesIdx);
			cb.setSelected(preset != null ? preset : false);

			cb.setText("Show shapes"); // i8n

			cb.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					chartStyle.getRendererStyle(rendererIndex)
							.setSeriesShapesVisible(seriesIdx, cb.isSelected());
					fireChartChangedEvent();
				}

			});

			shapesVisibleJCheckBoxes.put(rendererIndex * 1000 + seriesIdx, cb);
		}
		return shapesVisibleJCheckBoxes.get(rendererIndex * 1000 + seriesIdx);
	}

	/**
	 * A small {@link JPanel} describing the attribute that is used here.
	 */
	private JPanel getSeriesDataSelectionJPanel(final int rendererIndex,
			final int seriesIdx) {

		SimpleFeatureType schema = styledLayer.getFeatureSource().getSchema();

		final AttributesJComboBox attribComboBox = new AttributesJComboBox(
				schema, styledLayer.getAttributeMetaDataMap(), FeatureUtil
						.getNumericalFieldNames(schema, false));
		final String attributeName = chartStyle.getAttributeName(seriesIdx + 1);
		attribComboBox.setSelectedItem(attributeName);

		/** build a panel... */
		final JPanel attPanel = new JPanel(new MigLayout("flowy, wrap 2"),
				AtlasCreator.R("DesignAtlasChartJDialog.SeriesDataBorderTitle"));

		attPanel.add(attribComboBox);

		// A Panel that will list all NODATA-Value
		final NoDataPanel noDataPanel = new NoDataPanel(styledLayer
				.getAttributeMetaDataMap(), attributeName, styledLayer
				.getSchema());
		// Update the chart whenever the NODATA values changes
		noDataPanel.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(
						NoDataPanel.PROPERTY_NODATAVALUES))
					fireChartChangedEvent(true);
			}
		});
		attPanel.add(noDataPanel);

		final JPanel panelAggregationWeight = new JPanel(new MigLayout());

		if (chartStyle.getType() == ChartType.BAR) {
			// bei pie auch, wenn wir das mal haben

			final AggregationFunctionJComboBox aggregationFunctionJComboBox = new AggregationFunctionJComboBox();
			aggregationFunctionJComboBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {

					int idx = seriesIdx + 1;
					AggregationFunction aggFunc = (AggregationFunction) aggregationFunctionJComboBox
							.getSelectedItem();
					// System.out.println(idx+"="+aggFunc);
					chartStyle.setAttributeAggregation(idx, aggFunc);

					// AGgregation funktion kann auch auf die axsen UNIT wirken

					if (idx == ChartStyle.RANGE_AXIS) {
						String unit = styledLayer.getAttributeMetaDataMap()
								.get(attributeName).getUnit();

						getUnitTextFieldForAxis(ChartStyle.RANGE_AXIS).setText(
								unit);

						// Grey-out the weighting attribute if the aggregation
						// method doesn't support it.
						panelAggregationWeight.setEnabled(aggFunc != null
								&& aggFunc.isWeighted());
					}

					fireChartChangedEvent(true);
				}
			});

			aggregationFunctionJComboBox.setSelectedItem(chartStyle
					.getAttributeAggregation(seriesIdx + 1));

			JPanel panelAggregationMethod = new JPanel(new MigLayout());
			JLabel aggMethodlabel = new JLabel(AtlasCreator
					.R("DesignAtlasChartJDialog.SeriesData.Aggregation.Label"));
			aggMethodlabel.setToolTipText(AtlasCreator
					.R("DesignAtlasChartJDialog.SeriesData.Aggregation.TT"));
			panelAggregationMethod.add(aggMethodlabel, "gap unrel, w 150");

			panelAggregationMethod.add(aggregationFunctionJComboBox);
			attPanel.add(panelAggregationMethod);

			panelAggregationWeight
					.add(
							new JLabel(
									AtlasCreator
											.R("DesignAtlasChartJDialog.SeriesData.Aggregation.WeightLabel")),
							"gap unrel, w 150");
			final AttributesJComboBox weightFunctionAttributeComboBox = new AttributesJComboBox(
					schema, styledLayer.getAttributeMetaDataMap(), FeatureUtil
							.getNumericalFieldNames(schema, false));

			// Initialize weight combobox
			String attributeAggregationWeightAttributeName_InChartStyle = chartStyle
					.getAttributeAggregationWeightAttributeName(seriesIdx + 1);

			if (attributeAggregationWeightAttributeName_InChartStyle == null) {
				attributeAggregationWeightAttributeName_InChartStyle = (String) weightFunctionAttributeComboBox
						.getItemAt(0);
				chartStyle.setAttributeAggregationWeightAttributeName(
						seriesIdx + 1,
						attributeAggregationWeightAttributeName_InChartStyle);
			}

			weightFunctionAttributeComboBox
					.setSelectedItem(attributeAggregationWeightAttributeName_InChartStyle);

			weightFunctionAttributeComboBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					String weightAttName = (String) weightFunctionAttributeComboBox
							.getSelectedItem();
					chartStyle.setAttributeAggregationWeightAttributeName(
							seriesIdx + 1, weightAttName);

					AttributeMetadata atm = styledLayer
							.getAttributeMetaDataMap().get(weightAttName);
					chartStyle.setWeightAttributeNoDataValues(seriesIdx - 1,
							atm.getNodataValues());

					fireChartChangedEvent(true);
				}
			});

			panelAggregationWeight.add(weightFunctionAttributeComboBox);

			// Initialize the correct enabled/disabled state
			AggregationFunction aggFunc = chartStyle
					.getAttributeAggregation(seriesIdx + 1);
			panelAggregationWeight.setEnabled(aggFunc != null
					&& aggFunc.isWeighted());

			attPanel.add(panelAggregationWeight);

		}

		// When the attribute is changed, a lot of things happen:
		attribComboBox.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {

				if (e.getStateChange() != ItemEvent.SELECTED)
					return;

				String attLocalName = (String) attribComboBox.getSelectedItem();

				chartStyle.setAttributeName(seriesIdx + 1, attLocalName);
				AttributeMetadata atm = styledLayer.getAttributeMetaDataMap()
						.get(attLocalName);

				chartStyle
						.setNoDataValues(seriesIdx + 1, atm.getNodataValues());

				// LOGGER.debug("Setting attribute " + seriesIdx + 1 + " to "
				// + chartStyle.getAttributeName(seriesIdx + 1));

				/**
				 * Update the legend labeling with the AttributeMetaData from
				 * the new newly selected attribute
				 */
				{
					AVUtil.applyDefaultTitleAndTranslationToLegend(styledLayer,
							chartStyle, rendererIndex, seriesIdx,
							atlasConfigEditable.getLanguages());
				}

				noDataPanel.setAttribute(attLocalName);

				if (seriesIdx == 0) {
					String unit = atm.getUnit();

					getUnitTextFieldForAxis(seriesIdx + 1).setText(unit);
				}

				fireChartChangedEvent(true);
			}

		});

		return attPanel;
	}

	/**
	 * A panel to control the normalization of all attributes at once
	 * 
	 * @param featureChartStyle
	 * @return
	 */
	private JPanel getNormalizationPanel() {
		JPanel normPanel = new JPanel(new MigLayout("wrap 1"));

		// An explaining text
		normPanel
				.add(new JLabel(AtlasCreator.R("Normalize.Chart.Explanation")));

		// The check-box in the next line
		JCheckBox cb = new JCheckBox(AtlasCreator
				.R("AttributeSelectionPanel.NormalizeCheckbox"));

		cb.setToolTipText(AtlasCreator
				.R("AttributeSelectionPanel.NormalizeCheckbox.TT"));

		cb.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				final boolean normalize = e.getStateChange() == ItemEvent.SELECTED;

				for (int idx = 0; idx < chartStyle.getAttributeCount(); idx++)
					chartStyle.setAttributeNormalized(idx, normalize);

				// LOGGER.debug("Setting setAttributeNormalized for all attribs to "
				// + normalize);

				fireChartChangedEvent(true);
			}

		});

		// If the first attribute is normalized, all are!
		cb.setSelected(chartStyle.isAttributeNormalized(0));

		normPanel.add(cb);

		normPanel.setBorder(BorderFactory.createTitledBorder(AtlasCreator
				.R("Normalize.Border.title")));
		return normPanel;
	}

	/**
	 * Called when the {@link ChartStyle} has been changed. Might lead to an
	 * update of the {@link JFreeChart}.<br/>
	 * 
	 * @param forceReapplyToFC
	 *            if <code>true</code>, the {@link ChartStyle} will be
	 *            re-applied to the {@link FeatureCollection}.
	 */
	protected void fireChartChangedEvent(boolean forceReapplyToFC) {
		setReapplyChartStyleToFeatureCollection(forceReapplyToFC);
		fireChartChangedEvent();
	}

	/**
	 * A {@link JButton} allowing to change the color of the series.
	 */
	private JButton getSeriesColorButton(final int rendererIndex,
			final int seriesIndex) {

		if (seriesColorButtons.get(rendererIndex * 1000 + seriesIndex) == null) {

			final ColorButton colorButton = new ColorButton();

			colorButton.setAction(new AbstractAction(AtlasCreator
					.R("DesignAtlasChartJDialog.SeriesColorButton")) {

				@Override
				public void actionPerformed(final ActionEvent e) {
					chartStyle.getRendererStyle(rendererIndex).setSeriesPaint(
							seriesIndex,
							ASUtil.showColorChooser(
									DesignAtlasChartJDialog.this,
									"Zeichenfarbe des Balkendiagramms", // i8n
									chartStyle.getRendererStyle(rendererIndex)
											.getSeriesPaint(seriesIndex)));
					colorButton.setColor(chartStyle.getRendererStyle(
							rendererIndex).getSeriesPaint(seriesIndex));

					fireChartChangedEvent();
				}

			});

			/*
			 * Ensure that there are no NULLs
			 */
			if (chartStyle.getRendererStyle(rendererIndex).getSeriesPaint(
					seriesIndex) == null) {
				chartStyle.getRendererStyle(rendererIndex).setSeriesPaint(
						seriesIndex, getRandomColor());
				fireChartChangedEvent();
			}

			colorButton.setColor(chartStyle.getRendererStyle(rendererIndex)
					.getSeriesPaint(seriesIndex));

			seriesColorButtons.put(rendererIndex * 1000 + seriesIndex,
					colorButton);

		}
		return seriesColorButtons.get(rendererIndex * 1000 + seriesIndex);
	}

	ActionListener actionListenerLegendTitleChanged = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			fireChartChangedEvent();
		}

	};

	ActionListener actionListenerLegendTooltipChanges = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			fireChartChangedEvent();
		}

	};

	/**
	 * A private reference to the listeners. When nulled in dispose, the
	 * WeakHashMap forgets about them
	 */
	private Vector<ChartSelectionSynchronizer> insertedListeners = new Vector<ChartSelectionSynchronizer>();

	/**
	 * Contains a {@link JCheckBox} to show/hide the series in the legend. Then
	 * a {@link TranslationEditJPanel} for LabelTitle and LabelToolTip.
	 */
	public JPanel getLegendTitleTranslationEditPanel(final int rendererIndex,
			final int seriesIndex) {
		if (legendTitleTranslationEditPanels.get(rendererIndex * 1000
				+ seriesIndex) == null) {

			final JPanel legendSettingsJPanel = new JPanel(new MigLayout(
					"wrap 2", "[grow]"));

			Translation legendTitleTranslation = chartStyle.getRendererStyle(
					rendererIndex).getSeriesLegendLabel(seriesIndex)
					.getLabelTranslation();

			final TranslationEditJPanel legendTitleTranslationJPanel = new TranslationEditJPanel(
					legendTitleTranslation, atlasConfigEditable.getLanguages());

			Translation legendTooltipTranslation = chartStyle.getRendererStyle(
					rendererIndex).getSeriesLegendTooltip(seriesIndex)
					.getLabelTranslation();
			final TranslationEditJPanel legendTooltipTranslationJPanel = new TranslationEditJPanel(
					legendTooltipTranslation, atlasConfigEditable
							.getLanguages());

			/*
			 * The checkbox
			 */
			final JCheckBox inLegendCB = new JCheckBox(
					new AbstractAction(
							AtlasCreator
									.R("DesignAtlasChartJDialog.SeriesInLegendCheckbox")) {

						@Override
						public void actionPerformed(ActionEvent e) {
							final boolean visible = !chartStyle
									.getRendererStyle(rendererIndex)
									.isSeriesLegendVisible(seriesIndex);
							chartStyle.getRendererStyle(rendererIndex)
									.setSeriesLegendVisible(seriesIndex,
											visible);

							/*
							 * Enable/Disable the translation stuff
							 */
							legendTitleTranslationJPanel.setEnabled(visible);
							legendTooltipTranslationJPanel.setEnabled(visible);

							fireChartChangedEvent();
						}

					});

			inLegendCB.setSelected(chartStyle.getRendererStyle(rendererIndex)
					.isSeriesLegendVisible(seriesIndex));

			legendTitleTranslationJPanel.setEnabled(chartStyle
					.getRendererStyle(rendererIndex).isSeriesLegendVisible(
							seriesIndex));
			legendTooltipTranslationJPanel.setEnabled(chartStyle
					.getRendererStyle(rendererIndex).isSeriesLegendVisible(
							seriesIndex));

			legendSettingsJPanel.add(inLegendCB, "span 2");

			/*
			 * The title
			 */

			legendTitleTranslation
					.addTranslationChangeListener(actionListenerLegendTitleChanged);

			legendTitleTranslationJPanel
					.setBorder(BorderFactory
							.createTitledBorder(AtlasCreator
									.R("DesignAtlasChartJDialog.SeriesLegendLabel.BorderTitle")));
			legendSettingsJPanel.add(legendTitleTranslationJPanel, "span 2");

			/*
			 * the toolstip
			 */

			legendTooltipTranslation
					.addTranslationChangeListener(actionListenerLegendTooltipChanges);

			legendTooltipTranslationJPanel
					.setBorder(BorderFactory
							.createTitledBorder(AtlasCreator
									.R("DesignAtlasChartJDialog.SeriesLegendLabel.BorderTitle.TT")));
			legendSettingsJPanel.add(legendTooltipTranslationJPanel, "span 2");

			// /*
			// * Border
			// */
			// legendSettingsJPanel.setBorder(BorderFactory
			// .createTitledBorder(AtlasCreator
			// .R("DesignAtlasChartJDialog.LegendBorderTitle")));

			legendTitleTranslationEditPanels.put(rendererIndex * 1000
					+ seriesIndex, legendSettingsJPanel);
		}

		return legendTitleTranslationEditPanels.get(rendererIndex * 1000
				+ seriesIndex);
	}

	/**
	 * A {@link JPanel} containing the most general {@link ChartStyle} settings
	 */
	private JPanel getGeneralSettingsPanel() {
		if (generalSettingsJPanel == null) {
			generalSettingsJPanel = new GeneralChartSettingsJPanel(chartStyle,
					atlasConfigEditable);

			generalSettingsJPanel
					.addPropertyChangeListener(new PropertyChangeListener() {

						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							if (evt
									.getPropertyName()
									.equals(
											GeneralChartSettingsJPanel.PROPERTYNAME_CHART_STYLE)) {
								fireChartChangedEvent();
							}
						}

					});

		}
		return generalSettingsJPanel;
	}

	/**
	 * Called when the {@link ChartStyle} has been changed. Might lead to an
	 * update of the {@link JFreeChart}.
	 */
	void fireChartChangedEvent() {
		if (isAutoUpdateMode()) {
			updateChart();
		}
	}

	/**
	 * Apply the settings to the chart-preview. If
	 * {@link #isReapplyChartStyleToFeatureCollection()}, the {@link ChartStyle}
	 * is applied to the {@link FeatureCollection}.
	 */
	void updateChart() {
		AtlasChartJPanel panel = getChartPanel(mapLegend, styledLayer);

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		try {

			// Check if normalization is enabled, and then configure the
			// visualization of unit strings accordingly.
			// If the first attribute is normalized, all are!
			{
				boolean visible = !chartStyle.isAttributeNormalized(0);
				for (int axisIdx = 0; axisIdx < chartStyle.getAxisCount(); axisIdx++) {
					chartStyle.getAxisStyle(axisIdx).setUnitVisible(visible);
				}
			}

			if (isReapplyChartStyleToFeatureCollection()) {

				reapplyChartStyleToFeatureCollection = false;

				AtlasStatusDialog statusDialog = new AtlasStatusDialog(
						DesignAtlasChartJDialog.this, AtlasViewer
								.R("dialog.title.wait"), AtlasViewer
								.R("dialog.title.wait"));
				AtlasSwingWorker<JFreeChart> asw = new AtlasSwingWorker<JFreeChart>(
						statusDialog) {
					@Override
					protected JFreeChart doInBackground() throws Exception {
						return chartStyle.applyToFeatureCollection(styledLayer
								.getFeatureCollection());
					}
				};
				JFreeChart newChart = asw.executeModal();

				panel.setChart(newChart);

			} else {
				chartStyle.applyToChart(panel.getChart());
			}

			getOkButton().setEnabled(true);
		} catch (Exception e) {
			ExceptionDialog
					.show(DesignAtlasChartJDialog.this, e, null,
							"Failed to apply the chart style to the data. Please check data and settings.");

			getOkButton().setEnabled(false);
		} finally {
			setCursor(null);
		}
		/*
		 * Update the icons of the JTabbedPane
		 */
		for (int renderIdx = 0; renderIdx < chartStyle.getRendererCount(); renderIdx++) {
			for (int seriesIdx = 0; seriesIdx < chartStyle.getRendererStyle(
					renderIdx).getSeriesCount(); seriesIdx++) {
				updateTabIcon(renderIdx, seriesIdx);
			}
		}

	}

	public static Color getRandomColor() {
		final Random random = new Random();
		return new Color(100 + random.nextInt(100), 100 + random.nextInt(100),
				100 + random.nextInt(100));
	}

	/**
	 * Shall changes to the {@link ChartStyle} automatically update the preview?
	 */
	public void setAutoUpdateMode(boolean autoUpdateMode) {
		this.autoUpdateMode = autoUpdateMode;
	}

	/**
	 * Will changes to the {@link ChartStyle} automatically update the preview?
	 */
	public boolean isAutoUpdateMode() {
		return autoUpdateMode;
	}

	/**
	 * Will the next call to {@link #updateChart()} apply the {@link ChartStyle}
	 * to the {@link FeatureCollection}? This is needed when attributes have
	 * changed.
	 */
	public void setReapplyChartStyleToFeatureCollection(
			boolean repplyChartStyleToFeatureCollection) {
		this.reapplyChartStyleToFeatureCollection = repplyChartStyleToFeatureCollection;
	}

	/**
	 * Will the next call to {@link #updateChart()} apply the {@link ChartStyle}
	 * to the {@link FeatureCollection}?
	 */
	public boolean isReapplyChartStyleToFeatureCollection() {
		return reapplyChartStyleToFeatureCollection;
	}

	/**
	 * Lazily creates the {@link AtlasChartJPanel}
	 */
	protected AtlasChartJPanel getChartPanel(final AtlasMapLegend mapLegend,
			final StyledFeaturesInterface<?> styledLayer_) {

		if (chartPanel == null) {

			// Check if normalization is enabled, and then configure the
			// visualization of unit strings accordingly.
			// If the first attribute is normalized, all are!
			// This check is also done in updateChart method.
			{
				boolean visible = !chartStyle.isAttributeNormalized(0);
				for (int axisIdx = 0; axisIdx < chartStyle.getAxisCount(); axisIdx++) {
					chartStyle.getAxisStyle(axisIdx).setUnitVisible(visible);
				}
			}

			JFreeChart chart;

			chart = chartStyle.applyToFeatureCollection(styledLayer_
					.getFeatureCollection());

			chartPanel = new AtlasChartJPanel(chart, styledLayer_, mapLegend);

			// Connect to every chart selection model to refresh the
			// panel on selection changes
			for (final DatasetSelectionModel<?, ?, ?> selModel : FeatureChartUtil
					.getFeatureDatasetSelectionModelFor(chart)) {
				selModel.addSelectionListener(chartPanel);
			}

			final StyledLayerSelectionModel<?> anySelectionModel = mapLegend != null ? mapLegend
					.getRememberSelection(styledLayer.getId())
					: null;

			if ((anySelectionModel instanceof StyledFeatureLayerSelectionModel)) {
				final StyledFeatureLayerSelectionModel selectionModel = (StyledFeatureLayerSelectionModel) anySelectionModel;

				// get the selectionmodel(s) of the chart
				final List<FeatureDatasetSelectionModel<?, ?, ?>> datasetSelectionModelFor = FeatureChartUtil
						.getFeatureDatasetSelectionModelFor(chart);

				for (final FeatureDatasetSelectionModel dsm : datasetSelectionModelFor) {

					// create a synchronizer

					final ChartSelectionSynchronizer synchronizer = new ChartSelectionSynchronizer(
							selectionModel, dsm);

					insertedListeners.add(synchronizer);

					selectionModel
							.addSelectionListener((StyledLayerSelectionModelSynchronizer) synchronizer);
					dsm.addSelectionListener(synchronizer);

					selectionModel.refreshSelection();
				}
			}

			// addZoomToFeatureExtends();

		}
		return chartPanel;
	}

	public static boolean isOpenForLayer(DpLayerVectorFeatureSource dpl) {

		for (FeatureChartStyle fcs : dpl.getCharts()) {
			if (GPDialogManager.dm_DesignCharts.isVisibleFor(fcs))
				return true;
		}
		return false;
	}

	@Override
	public void dispose() {

		if (isDisposed)
			return;

		for (ChartSelectionSynchronizer d : insertedListeners) {
			d.setEnabled(false);
			d = null;
		}
		insertedListeners.clear();

		if (chartPanel != null)
			chartPanel.dispose();
		chartPanel = null;
		super.dispose();
	}

}
