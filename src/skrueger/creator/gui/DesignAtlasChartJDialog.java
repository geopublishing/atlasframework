package skrueger.creator.gui;

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

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geotools.feature.FeatureCollection;
import org.jfree.chart.JFreeChart;
import org.opengis.feature.simple.SimpleFeatureType;

import schmitzm.geotools.gui.GeoPositionLabel;
import schmitzm.geotools.gui.SelectableXMapPane;
import schmitzm.jfree.chart.selection.DatasetSelectionModel;
import schmitzm.jfree.chart.style.ChartAxisStyle;
import schmitzm.jfree.chart.style.ChartPlotStyle;
import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.jfree.chart.style.ChartType;
import schmitzm.jfree.feature.FeatureDatasetSelectionModel;
import schmitzm.jfree.feature.style.FeatureChartStyle;
import schmitzm.jfree.feature.style.FeatureChartUtil;
import schmitzm.swing.ExceptionDialog;
import schmitzm.swing.JPanel;
import schmitzm.swing.SwingUtil;
import skrueger.atlas.AVDialogManager;
import skrueger.atlas.AVUtil;
import skrueger.atlas.AtlasViewer;
import skrueger.atlas.dp.layer.DpLayerVectorFeatureSource;
import skrueger.atlas.gui.AtlasChartJPanel;
import skrueger.atlas.gui.MapLegend;
import skrueger.atlas.gui.internal.AtlasStatusDialog;
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
import skrueger.swing.CancelButton;
import skrueger.swing.CancellableDialogAdapter;
import skrueger.swing.ColorButton;
import skrueger.swing.OkButton;
import skrueger.swing.TranslationEditJPanel;

/**
 * This {@link JDialog} allows to interactively edit a ChartStyĺe. The preview
 * window automatically reflects the changes.
 * 
 * @author Stefan A. Krüger
 * 
 */
public class DesignAtlasChartJDialog extends CancellableDialogAdapter {
	final static org.apache.log4j.Logger LOGGER = Logger
			.getLogger(DesignAtlasChartJDialog.class);

	private JTabbedPane jTabbedPane;
	private JPanel generalSettingsJPanel;
	private final AtlasConfigEditable atlasConfigEditable;
	private HashMap<Integer, JPanel> legendTitleTranslationEditPanels = new HashMap<Integer, JPanel>();
	private HashMap<Integer, JButton> seriesColorButtons = new HashMap<Integer, JButton>();
	private HashMap<Integer, Integer> rememberTabPos = new HashMap<Integer, Integer>();

	/** Automatically apply any changes to the chartStyle to the chart? */
	private boolean autoUpdateMode = false;

	/**
	 * If set to <code>true</code>, the next update includes a reapplying the
	 * {@link ChartStyle} to the {@link FeatureCollection}
	 */
	private boolean reapplyChartStyleToFeatureCollection = false;

	private JButton updateChartJButton;

	private JCheckBox updateModeJCheckbox;

	private HashMap<Integer, JCheckBox> normalizeJCheckboxs = new HashMap<Integer, JCheckBox>();

	private JCheckBox categoryDomainAxisJCheckBox;

	private HashMap<Integer, JCheckBox> shapesVisibleJCheckBoxes = new HashMap<Integer, JCheckBox>();

	private final FeatureChartStyle chartStyle;

	private final MapLegend mapLegend;

	private final StyledFeaturesInterface<?> styledLayer;

	private AtlasChartJPanel chartPanel;

	private FeatureChartStyle backupChartStyle;

	private OkButton okButton;

	private SelectableXMapPane mapPane;

	public DesignAtlasChartJDialog(final Component owner,
			final FeatureChartStyle chartStyle, final MapLegend mapLegend,
			final StyledFeaturesInterface<?> styledFeatures_,
			AtlasConfigEditable atlasConfigEditable) {

		super(SwingUtil.getParentWindow(owner));
		this.chartStyle = chartStyle;
		this.mapLegend = mapLegend;
		this.styledLayer = styledFeatures_;

		this.mapPane = mapLegend != null ? (mapLegend.getGeoMapPane() != null ? mapLegend
				.getGeoMapPane().getMapPane()
				: null)
				: null;

		this.atlasConfigEditable = atlasConfigEditable;

		backup();

		initGUI();

		SwingUtil.setRelativeFramePosition(this, owner, SwingUtil.BOUNDS_OUTER,
				SwingUtil.NORTHEAST);
	}

	private void backup() {
		backupChartStyle = (FeatureChartStyle) chartStyle.copy();
	}

	@Override
	public void cancel() {
		backupChartStyle.copyTo(chartStyle);
	}

	protected void initGUI() {
		setTitle(chartStyle.getTitleStyle().getLabel());

		JPanel contentPane = new JPanel(new MigLayout("wrap 1"));

		contentPane.add(getChartPanel(mapLegend, styledLayer));

		contentPane.add(getTabbedPane());

		getChartPanel(mapLegend, styledLayer).addZoomToFeatureExtends(mapPane,
				mapLegend, styledLayer, getChartPanel(mapLegend, styledLayer));

		contentPane.add(getButtonsPane());

		setContentPane(contentPane);

		// super.addZoomToFeatureExtends();

		pack();

		setVisible(true);
	}

	/**
	 * Creates the buttons
	 */
	private Component getButtonsPane() {
		JPanel buttons = new JPanel(new MigLayout("width 100%"));

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

		okButton = getOkButton();
		buttons.add(okButton, "right, tag ok");

		buttons.add(new CancelButton(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				cancelClose();
			}

		}), "right, tag ok");

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
			jTabbedPane.add(new JScrollPane(getGeneralSettingsPanel()));
			jTabbedPane.setTitleAt(countTabsPos++, AtlasCreator
					.R("DesignAtlasChartJDialog.Tabs.General.Title"));

			jTabbedPane.add(new JScrollPane(getChartPlotStylePanel()));
			jTabbedPane.setTitleAt(countTabsPos++, AtlasCreator
					.R("DesignAtlasChartJDialog.Tabs.ChartPlotStyle.Title"));

			jTabbedPane.add(new JScrollPane(
					getAxisPanel(ChartStyle.DOMAIN_AXIS)));
			jTabbedPane.setTitleAt(countTabsPos++, AtlasCreator
					.R("DesignAtlasChartJDialog.Tabs.DomainAxis.Title"));

			jTabbedPane
					.add(new JScrollPane(getAxisPanel(ChartStyle.RANGE_AXIS)));
			jTabbedPane.setTitleAt(countTabsPos++, AtlasCreator
					.R("DesignAtlasChartJDialog.Tabs.RangeAxis1.Title"));

			int renderIdx = 0;
			// if (chartStyle.getType() != ChartType.SCATTER) {
			for (int seriesIdx = 0; seriesIdx < chartStyle.getRendererStyle(
					renderIdx).getSeriesCount(); seriesIdx++) {

				jTabbedPane.add(new JScrollPane(getRendererSettingsPanel(
						renderIdx, seriesIdx)));

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
		schmitzm.swing.JPanel panel = new schmitzm.swing.JPanel(new MigLayout(
				"wrap 1", "[grow]"));

		// Initialize with non-nulls
		if (chartStyle.getPlotStyle() == null)
			chartStyle.setPlotStyle(new ChartPlotStyle());
		if (chartStyle.getPlotStyle().isDomainGridlineVisible() == null)
			chartStyle.getPlotStyle().setDomainGridlineVisible(true);
		if (chartStyle.getPlotStyle().isRangeGridlineVisible() == null)
			chartStyle.getPlotStyle().setRangeGridlineVisible(true);
		if (chartStyle.getPlotStyle().isCrosshairVisible() == null)
			chartStyle.getPlotStyle().setCrosshairVisible(false);
		if (chartStyle.getPlotStyle().getCrosshairPaint() == null)
			chartStyle.getPlotStyle().setCrosshairPaint(Color.BLACK);

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
			// Domain Axis
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
		{
			// Crosshair linessadsd
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
		}

		{
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
			panel.add(getNormalizationPanel((FeatureChartStyle)chartStyle), "growx, sgx");


		return panel;
	}

	/*
	 * Adds a listener to update the chart when the label is changed
	 */
	ActionListener actionListenerDomainLabelChanges = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			fireChartChangedEvent();
		}

	};

	/**
	 * A {@link JPanel} which allows the user to configure the appearance of the
	 * domain axis
	 */
	private JPanel getAxisPanel(int axisNr) {
		JPanel axisPane = new JPanel(new MigLayout("wrap 2"));

		/*
		 * Ensure that there are no NULLs
		 */
		if (chartStyle.getAxisStyle(axisNr) == null) {
			chartStyle.setAxisStyle(axisNr, new ChartAxisStyle());
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
			/* prepare data: */
			// String attributeName = chartStyle
			// .getAttributeName(ChartStyle.DOMAIN_AXIS);
			// AttributeMetaData attributeMetadataFor = ASUtil
			// .getAttributeMetadataFor(styledLayer, attributeName);
			SimpleFeatureType schema = styledLayer.getFeatureSource()
					.getSchema();
			// AttributeDescriptor attributeType =
			// schema.getAttributeType(ASUtil
			// .getAttribIndex(schema, attributeName));
			// Translation descTranslation = attributeMetadataFor.getDesc();

			final List<String> fieldNames;

			if (chartStyle.getType().isCategoryAllowedForDomainAxis()) {
				fieldNames = ASUtil.getValueFieldNames(styledLayer
						.getFeatureSource(), false);
			} else
				fieldNames = ASUtil.getNumericalFieldNames(schema, false);
			final AttributesJComboBox attribComboBox = new AttributesJComboBox(
					schema, styledLayer.getAttributeMetaDataMap(), fieldNames);

			attribComboBox.setSelectedItem(chartStyle
					.getAttributeName(ChartStyle.DOMAIN_AXIS));

			/** build GUI... */
			final JPanel attPanel = new JPanel(new MigLayout("wrap 1"));

			attPanel.setBorder(BorderFactory.createTitledBorder(AtlasCreator
					.R("DesignAtlasChartJDialog.SeriesDataBorderTitle")));

			attPanel.add(attribComboBox, "growx");
//			attPanel.add(getNormalizeJCheckboxFor(ChartStyle.DOMAIN_AXIS),
//					"growx, right");

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

					final String attName = (String) attribComboBox
							.getSelectedItem();

					chartStyle
							.setAttributeName(ChartStyle.DOMAIN_AXIS, attName);

					// LOGGER.debug("Setting attribute " + seriesIdx + 1 +
					// " to "
					// + chartStyle.getAttributeName(seriesIdx + 1));

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

					/**
					 * If a non-numerical attribute is selected, deactivate and
					 * unselect the normalized button
					 */
					/*
					 * You may only force categories, if a numeric att is
					 * selected
					 */
					getCategoryJCheckbox().setEnabled(
							attribComboBox.isNumericalAttribSelected());
					if (!attribComboBox.isNumericalAttribSelected()) {
						getCategoryJCheckbox().setSelected(true);
					}

//					/*
//					 * You may only use normalization , if a numeric att is
//					 * selected
//					 */
//					getNormalizeJCheckboxFor(0).setEnabled(
//							attribComboBox.isNumericalAttribSelected());
//					if (!attribComboBox.isNumericalAttribSelected()) {
//						getNormalizeJCheckboxFor(0).setSelected(false);
//					}

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
				.addTranslationChangeListener(actionListenerDomainLabelChanges);

		axisLabelTranslationJPanel.setBorder(BorderFactory
				.createTitledBorder(AtlasCreator
						.R("DesignAtlasChartJDialog.AxisSettings.AxisLabel")));

		axisPane.add(axisLabelTranslationJPanel, "span 2, growx");

		/*
		 * Now add a UNIT JTextField
		 */
		JPanel unitPanel = new JPanel(new MigLayout());
		final JTextField unitTextfield = new JTextField(5);
		unitTextfield.setText(axisStyle.getUnitString());
		unitTextfield.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent e) {
				react();
			}

			void react() {
				axisStyle.setUnitString(unitTextfield.getText());
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
		unitPanel.add(unitTextfield);
		unitPanel.setBorder(BorderFactory.createTitledBorder(AtlasCreator
				.R("Unit")));
		axisPane.add(unitPanel, "sgy");

		/*
		 * Now add a ANGLE JTextField
		 */
		JPanel anglePanel = new JPanel();

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

		anglePanel.setBorder(BorderFactory.createTitledBorder(AtlasCreator
				.R("DesignAtlasChartJDialog.AxisSettings.ValueLabelAngle")));
		anglePanel.add(angleSlider);
		axisPane.add(anglePanel, "sgy, growx");

		return axisPane;
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

		JPanel rendererSettingsPanel = new JPanel(new MigLayout("wrap 2"));

		/*
		 * Color...
		 */
		rendererSettingsPanel
				.add(getSeriesColorButton(rendererIndex, seriesIdx));

		if (chartStyle.getType() != ChartType.POINT
				&& chartStyle.getType() != ChartType.SCATTER) {
			// The possibility to show/hide shapes is only given for non point
			// layers. otherwise set true!
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

		/* prepare data: */
		// String attributeName = chartStyle.getAttributeName(seriesIdx + 1);

		// AttributeMetadata attributeMetadataFor =
		// styledLayer.getAttributeMetaDataMap().get(attributeName);

		SimpleFeatureType schema = styledLayer.getFeatureSource().getSchema();
		// AttributeDescriptor attributeType = schema.getAttributeType(ASUtil
		// .getAttribIndex(schema, attributeName));
		// Translation descTranslation = attributeMetadataFor.getDesc();

		final AttributesJComboBox attribComboBox = new AttributesJComboBox(
				schema, styledLayer.getAttributeMetaDataMap(), ASUtil
						.getNumericalFieldNames(schema, false));
		attribComboBox.setSelectedItem(chartStyle
				.getAttributeName(seriesIdx + 1));

		/** build GUI... */
		final JPanel attPanel = new JPanel(new MigLayout("wrap 1, fillx"));

		attPanel.setBorder(BorderFactory.createTitledBorder(AtlasCreator
				.R("DesignAtlasChartJDialog.SeriesDataBorderTitle")));
		//
		// attPanel.add(
		// new JLabel(AtlasCreator.R(
		// "AttributeSelectionWizardPanel.NthAttribute",
		// (seriesIdx + 2))), "right");

		attPanel.add(attribComboBox, "growx");
//		attPanel.add(getNormalizeJCheckboxFor(seriesIdx + 1), "growx");
		//
		// final TranslationEditJPanel titleTransJPanel = new
		// TranslationEditJPanel(attributeMetadataFor.getTitle(),
		// atlasConfigEditable.getLanguages(),
		// AtlasCreator.R("AttributeTitle"));
		// attPanel.add(titleTransJPanel, "growx");
		// attPanel.add(new JLabel(attributeMetadataFor.getTitle().toString()),
		// "left");
		//
		// final TranslationEditJPanel descTransJPanel = new
		// TranslationEditJPanel(attributeMetadataFor.getDesc(),
		// atlasConfigEditable.getLanguages(), AtlasCreator.R("AttributeDesc"));
		// attPanel.add(descTransJPanel, "growx");
		// attPanel.add(new JLabel(AtlasCreator.R("AttributeDesc")), "split 2");
		// attPanel.add(new JLabel(descTranslation.toString()), "left");

		/**
		 * When the attribute is changed, a lot of things happen:
		 */
		attribComboBox.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {

				if (e.getStateChange() != ItemEvent.SELECTED)
					return;

				chartStyle.setAttributeName(seriesIdx + 1,
						(String) attribComboBox.getSelectedItem());

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

				// initGUI();

				fireChartChangedEvent(true);
			}

		});

		return attPanel;
	}
	
	/**
	 * A panel to control the normalization of all attributes at once
	 * @param featureChartStyle
	 * @return
	 */
	private JPanel getNormalizationPanel(final FeatureChartStyle featureChartStyle) {
		JPanel normPanel = new JPanel(new MigLayout("wrap 2, align center"));
		
		JCheckBox cb = new JCheckBox( AtlasCreator
				.R("AttributeSelectionPanel.NormalizeCheckbox")); 
	
		
		cb.setToolTipText(AtlasCreator
				.R("AttributeSelectionPanel.NormalizeCheckbox.TT"));
		

		
		cb.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				final boolean normalize = e.getStateChange() == ItemEvent.SELECTED;
				
				for (int idx = 0; idx < featureChartStyle.getAttributeCount(); idx++)
					featureChartStyle.setAttributeNormalized(idx, normalize);
				
				LOGGER.debug("Setting setAttributeNormalized for all attribs to " + normalize);
				
				fireChartChangedEvent(true);
			}

		});		
		
		// If the first attribute is normalized, all are!
		cb.setSelected(featureChartStyle.isAttributeNormalized(0));
		
		normPanel.add(cb);
		// normPanel.add(getChartBackgroundColorButton());
		// normPanel.add(getChartBackgroundColorJCheckbox(), "span 3, right");

		normPanel.setBorder(BorderFactory.createTitledBorder(AtlasCreator.R("Normalize.Border.title"))); 
		return normPanel;
	}


	/**
	 * Returns the normlizeCheckBox for the Nth attribute (0 = DOMAIN)
	 */
	private JCheckBox getNormalizeJCheckboxFor(final int variableIndex) {
		if (normalizeJCheckboxs.get(variableIndex) == null) {
			JCheckBox cb = new JCheckBox(AtlasCreator
					.R("AttributeSelectionPanel.NormalizeCheckbox"));
			cb.setToolTipText(AtlasCreator
					.R("AttributeSelectionPanel.NormalizeCheckbox.TT"));

			cb.setSelected(chartStyle.isAttributeNormalized(variableIndex));

			// cb.setName(ChartWizard.NORMALIZE_ + idx);
			normalizeJCheckboxs.put(variableIndex, cb);

			cb.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					final boolean normalize = e.getStateChange() == ItemEvent.SELECTED;
					chartStyle.setAttributeNormalized(variableIndex, normalize);
					LOGGER.debug("Setting setAttributeNormalized for attrib "
							+ variableIndex + " to " + normalize);
					fireChartChangedEvent(true);
				}

			});

		}
		return normalizeJCheckboxs.get(variableIndex);
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

			final JPanel legendSettingsJPanel = new schmitzm.swing.JPanel(
					new MigLayout("wrap 2", "[grow]"));

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

			/*
			 * Border
			 */
			legendSettingsJPanel.setBorder(BorderFactory
					.createTitledBorder(AtlasCreator
							.R("DesignAtlasChartJDialog.LegendBorderTitle")));

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
							if (evt.getPropertyName().equals("chartStyle")) {
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

			if (isReapplyChartStyleToFeatureCollection()) {

				reapplyChartStyleToFeatureCollection = false;

				AtlasStatusDialog statusDialog = new AtlasStatusDialog(
						DesignAtlasChartJDialog.this, AtlasViewer.R("dialog.title.wait"), AtlasViewer.R("dialog.title.wait")); 
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
		}

		finally {
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
	protected AtlasChartJPanel getChartPanel(final MapLegend mapLegend,
			final StyledFeaturesInterface<?> styledLayer_) {

		if (chartPanel == null) {

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
