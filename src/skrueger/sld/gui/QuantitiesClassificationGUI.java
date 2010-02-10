/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package skrueger.sld.gui;

import hep.aida.bin.QuantileBin1D;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.CellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

import schmitzm.lang.LangUtil;
import schmitzm.swing.ExceptionDialog;
import schmitzm.swing.SwingUtil;
import skrueger.AttributeMetadata;
import skrueger.atlas.AVDialogManager;
import skrueger.atlas.gui.AtlasFeatureLayerFilterDialog;
import skrueger.atlas.gui.plaf.BasicMapLayerLegendPaneUI;
import skrueger.i8n.I8NUtil;
import skrueger.sld.ASUtil;
import skrueger.sld.AtlasStyler;
import skrueger.sld.classification.ClassificationChangeEvent;
import skrueger.sld.classification.ClassificationChangedAdapter;
import skrueger.sld.classification.QuantitiesClassification;
import skrueger.sld.classification.ClassificationChangeEvent.CHANGETYPES;
import skrueger.sld.classification.QuantitiesClassification.METHOD;
import skrueger.swing.AtlasDialog;
import cern.colt.list.DoubleArrayList;

/**
 * This {@link JDialog} presents the user with descriptive statistics and a
 * histogram for your data. He may also choose the classification method or edit
 * the class breaks manually.
 * 
 * @author SK
 * 
 */
public class QuantitiesClassificationGUI extends AtlasDialog {
	protected final static Logger LOGGER = Logger
			.getLogger(QuantitiesClassificationGUI.class);

	private static final long serialVersionUID = 1L;

	private static final BufferedImage ERROR_IMAGE = new BufferedImage(400,
			200, BufferedImage.TYPE_3BYTE_BGR);

	private static final BufferedImage WAIT_IMAGE = new BufferedImage(400, 200,
			BufferedImage.TYPE_3BYTE_BGR);

	private JPanel jContentPane = null;

	private JPanel jPanelLinksOben = null;

	private JLabel jLabel = null;

	private JLabel jLabelParameter = null;

	private JComboBox jComboBoxMethod = null;

	private JPanel jPanelData = null;

	private JButton jButtonExclusion = null;

	private JPanel jPanelDescriptiveStatistics = null;

	private JTable jTableStats = null;

	private JPanel jPanel3 = null;

	private JLabel jFreeChartJLabel = null;

	private JLabel jLabel2 = null;

	private JToggleButton jToggleButton = null;

	private JTable jTableBreakValues = null;

	private final QuantitiesClassification classifier;

	private JPanel jPanelHistParams = null;

	private JLabel jLabel3 = null;

	private JComboBox jComboBoxColumns = null;

	private JCheckBox jCheckBoxShowSD = null;

	private JLabel jLabelShowSD = null;

	private JCheckBox jCheckBoxShowMean = null;

	private JLabel jLabelShowMean = null;

	Integer histogramBins = 14;

	private final AtlasStyler atlasStyler;

	public QuantitiesClassificationGUI(Component owner,
			QuantitiesClassification classifier, AtlasStyler atlasStyler) {
		super(SwingUtil.getParentWindow(owner));
		this.classifier = classifier;
		this.atlasStyler = atlasStyler;
		initialize();

		SwingUtil.setRelativeFramePosition(this, owner, SwingUtil.BOUNDS_OUTER,
				SwingUtil.NORTHEAST);
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(605, 421);
		this.setModal(true);
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
			gridBagConstraints31.gridx = 0;
			gridBagConstraints31.gridwidth = 3;
			gridBagConstraints31.fill = GridBagConstraints.BOTH;
			gridBagConstraints31.weightx = 0.0;
			gridBagConstraints31.weighty = 2.0;
			gridBagConstraints31.gridy = 5;
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.gridx = 1;
			gridBagConstraints21.fill = GridBagConstraints.BOTH;
			gridBagConstraints21.gridheight = 5;
			gridBagConstraints21.weighty = 0.0;
			gridBagConstraints21.weightx = 2.0;
			gridBagConstraints21.gridwidth = 2;
			gridBagConstraints21.gridy = 0;
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.gridx = 0;
			gridBagConstraints14.weightx = 0.5;
			gridBagConstraints14.gridwidth = 1;
			gridBagConstraints14.gridy = 4;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints11.gridy = 1;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.weightx = 0.5;
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			gridBagConstraints.gridy = 0;
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(getJPanelLinksOben(), gridBagConstraints);
			jContentPane.add(getJPanelDataExclusion(), gridBagConstraints11);
			jContentPane.add(getJPanelHistogramParameters(),
					gridBagConstraints14);
			jContentPane.add(getJPanelDescriptiveStatistics(),
					gridBagConstraints21);
			jContentPane.add(getJPanel3(), gridBagConstraints31);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelLinksOben() {
		if (jPanelLinksOben == null) {
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.fill = GridBagConstraints.NONE;
			gridBagConstraints4.gridy = 1;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints4.anchor = GridBagConstraints.WEST;
			gridBagConstraints4.gridx = 1;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.fill = GridBagConstraints.BOTH;
			gridBagConstraints3.gridy = 0;
			gridBagConstraints3.anchor = GridBagConstraints.WEST;
			gridBagConstraints3.weightx = 0.1;
			gridBagConstraints3.insets = new Insets(5, 10, 5, 5);
			gridBagConstraints3.gridx = 1;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 1;
			jLabelParameter = new JLabel(AtlasStyler
					.R("ComboBox.NumberOfClasses"));
			jLabelParameter.setToolTipText(AtlasStyler
					.R("ComboBox.NumberOfClasses.TT"));

			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 0;
			jLabel = new JLabel();
			jLabel.setText(AtlasStyler
					.R("QuantitiesClassificationGUI.Combobox.Method"));
			jPanelLinksOben = new JPanel();
			jPanelLinksOben
					.setBorder(BorderFactory
							.createTitledBorder(AtlasStyler
									.R("GraduatedColorQuantities.classification.BorderTitle")));
			jPanelLinksOben.setLayout(new GridBagLayout());
			jPanelLinksOben.add(jLabel, gridBagConstraints1);
			jPanelLinksOben.add(jLabelParameter, gridBagConstraints2);
			jPanelLinksOben.add(getJComboBoxMethod(), gridBagConstraints3);
			jPanelLinksOben.add(new NumClassesJComboBox(classifier),
					gridBagConstraints4);
		}
		return jPanelLinksOben;
	}

	/**
	 * Creates a {@link JComboBox} that offers to choose from one of the
	 * classification methods.
	 */
	private JComboBox getJComboBoxMethod() {
		if (jComboBoxMethod == null) {
			jComboBoxMethod = new ClassificationMethodJComboBox(classifier);
		}
		return jComboBoxMethod;
	}

	/**
	 * This method initializes jPanel1
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelDataExclusion() {
		if (jPanelData == null) {
			jPanelData = new JPanel(new MigLayout());
			jPanelData.setBorder(BorderFactory.createTitledBorder(AtlasStyler
					.R("QuantitiesClassificationGUI.Data.BorderTitle")));

			if (atlasStyler.getMapLayer() != null) {
				jPanelData.add(getJButtonExclusion());
			}

			jPanelData.add(getJButtonAttribTable());
			SwingUtil.setPreferredWidth(jPanelData, 100);
			// jPanel1.add(getJButtonSampling(), gridBagConstraints6);
		}
		return jPanelData;
	}

	/**
	 * This button opens the AttributeTable
	 * 
	 * @return
	 */
	private JButton getJButtonAttribTable() {
		JButton button = new JButton(new AbstractAction(AtlasStyler
				.R("QuantitiesClassificationGUI.Data.ShowAttribTableButton"),
				BasicMapLayerLegendPaneUI.ICON_TABLE) {

			@Override
			public void actionPerformed(ActionEvent e) {
				/*
				 * If possible, set the JMapPane as the parent GUI. If now
				 * available, use this dialog
				 */
				QuantitiesClassificationGUI owner = QuantitiesClassificationGUI.this;

				AVDialogManager.dm_AttributeTable.getInstanceFor(atlasStyler
						.getStyledFeatures(), owner, atlasStyler
						.getStyledFeatures(), atlasStyler.getMapLegend());
			}

		});
		return button;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonExclusion() {
		if (jButtonExclusion == null) {
			jButtonExclusion = new JButton();

			jButtonExclusion.setAction(new AbstractAction(AtlasStyler
					.R("QuantitiesClassificationGUI.DataExclusion.Button")) {

				public void actionPerformed(ActionEvent e) {

					final AtlasFeatureLayerFilterDialog filterDialog;
					try {

						// This is all ugly ;-/
						// TODO GP_Dialogmanager

						filterDialog = new AtlasFeatureLayerFilterDialog(
								QuantitiesClassificationGUI.this, atlasStyler
										.getStyledFeatures(), atlasStyler
										.getMapLegend().getGeoMapPane()
										.getMapPane(), atlasStyler
										.getMapLayer());

						// TODO listen to any filter changes?

						filterDialog.setVisible(true);

					} catch (Exception ee) {
						LOGGER.error(ee);
						ExceptionDialog.show(QuantitiesClassificationGUI.this,
								ee);
					}
				}

			});
			jButtonExclusion.setToolTipText(AtlasStyler
					.R("QuantitiesClassificationGUI.DataExclusion.Button.TT"));
		}
		return jButtonExclusion;
	}

	/**
	 * This method initializes jPanel2
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelDescriptiveStatistics() {
		if (jPanelDescriptiveStatistics == null) {
			jPanelDescriptiveStatistics = new JPanel(new MigLayout());
			jPanelDescriptiveStatistics
					.setBorder(BorderFactory
							.createTitledBorder(AtlasStyler
									.R("QuantitiesClassificationGUI.Statistics.BorderTitle")));
			final JScrollPane scrollPane = new JScrollPane(
					getJTableStatistics());
			SwingUtil.setPreferredWidth(scrollPane, 170);
			jPanelDescriptiveStatistics.add(scrollPane);
			SwingUtil.setMinimumWidth(jPanelDescriptiveStatistics, 190);

		}
		return jPanelDescriptiveStatistics;
	}

	/**
	 * This method initializes jTable
	 * 
	 * @return javax.swing.JTable
	 */
	private JTable getJTableStatistics() {
		if (jTableStats == null) {
			jTableStats = new JTable();

			/**
			 * Classification.DescriptiveStatistics.Count=Count:
			 * Classification.DescriptiveStatistics.Min=Minimum:
			 * Classification.DescriptiveStatistics.Max=Maximum:
			 * Classification.DescriptiveStatistics.Sum=Summe:
			 * Classification.DescriptiveStatistics.Mean=airthm. Mittel:
			 * Classification.DescriptiveStatistics.Median=Median:
			 * Classification.DescriptiveStatistics.SD=Standard deviation:
			 */

			jTableStats.setModel(new DefaultTableModel() {
				final String[] fieldNames = new String[] {
						AtlasStyler
								.R("Classification.DescriptiveStatistics.Count"),
						AtlasStyler
								.R("Classification.DescriptiveStatistics.Min"),
						AtlasStyler
								.R("Classification.DescriptiveStatistics.Max"),
						AtlasStyler
								.R("Classification.DescriptiveStatistics.Sum"),
						AtlasStyler
								.R("Classification.DescriptiveStatistics.Mean"),
						AtlasStyler
								.R("Classification.DescriptiveStatistics.Median"),
						AtlasStyler
								.R("Classification.DescriptiveStatistics.SD") };

				@Override
				public Class<?> getColumnClass(int columnIndex) {
					if (columnIndex == 0)
						return String.class;
					if (columnIndex == 1)
						return Double.class;
					return null;
				}

				@Override
				public int getColumnCount() {
					return 2;
				}

				@Override
				public int getRowCount() {
					return 7;
				}

				@Override
				public Object getValueAt(int rowIndex, int columnIndex) {
					if (columnIndex == 0)
						return fieldNames[rowIndex];

					try {
						QuantileBin1D stats = classifier.getStatistics();
						if (rowIndex == 0) // Count
							return stats.size();
						if (rowIndex == 1) // Min
							return stats.min();
						if (rowIndex == 2) // Max
							return stats.max();
						if (rowIndex == 3) // Sum
							return stats.sum();
						if (rowIndex == 4) // Mean
							return stats.mean();
						if (rowIndex == 5) // Median
							return stats.median();
						if (rowIndex == 6) // SD
							return stats.standardDeviation();
					} catch (Exception e) {
						LOGGER.error("While creating the statistics:", e);
					}
					return "ERR";
				}

				@Override
				public boolean isCellEditable(int rowIndex, int columnIndex) {
					return false;
				}

				@Override
				public void setValueAt(Object aValue, int rowIndex,
						int columnIndex) {
				}
			});

			jTableStats.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			jTableStats.setTableHeader(null);

			jTableStats.setDefaultRenderer(Double.class, ASUtil
					.getDoubleCellRenderer());

			/**
			 * When the classification is recalculated, also repaint the table.
			 * THis is actually only neede when a filter has been changed... but
			 * its not expensive.
			 */
			classifier.addListener(new ClassificationChangedAdapter() {

				@Override
				public void classifierCalculatingStatistics(
						ClassificationChangeEvent e) {
					jTableStats.setEnabled(false);
				}

				@Override
				public void classifierAvailableNewClasses(
						ClassificationChangeEvent e) {
					((DefaultTableModel) jTableStats.getModel())
							.fireTableDataChanged();
				}

			});
		}
		return jTableStats;
	}

	/**
	 * This method initializes jPanel3
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel3() {
		if (jPanel3 == null) {
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.fill = GridBagConstraints.BOTH;
			gridBagConstraints12.gridy = 3;
			gridBagConstraints12.weightx = 1.0;
			gridBagConstraints12.weighty = 1.0;
			gridBagConstraints12.gridwidth = 2;
			gridBagConstraints12.gridx = 1;
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 2;
			gridBagConstraints10.anchor = GridBagConstraints.EAST;
			gridBagConstraints10.insets = new Insets(0, 0, 0, 5);
			gridBagConstraints10.gridy = 0;
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 1;
			gridBagConstraints9.gridy = 0;
			jLabel2 = new JLabel();
			jLabel2.setText(AtlasStyler.R("Classification.BreakValues"));
			jLabel2.setToolTipText(AtlasStyler
					.R("Classification.BreakValues.TT"));
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.gridheight = 5;
			gridBagConstraints8.insets = new Insets(0, 0, 0, 0);
			gridBagConstraints8.fill = GridBagConstraints.BOTH;
			gridBagConstraints8.gridy = 0;
			jPanel3 = new JPanel();
			jPanel3.setLayout(new GridBagLayout());
			jPanel3.add(getHistogram(), gridBagConstraints8);
			jPanel3.add(jLabel2, gridBagConstraints9);
			// jPanel3.add(getJToggleButton(), gridBagConstraints10);
			jPanel3.add(new JScrollPane(getJTableBreakValues()),
					gridBagConstraints12);
		}
		return jPanel3;
	}

	/**
	 * @return A {@link JLabel} showing the histogram. The {@link JLabel} is
	 *         updated when new classes are available
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	private JLabel getHistogram() {
		if (jFreeChartJLabel == null) {
			jFreeChartJLabel = new JLabel();
			classifier.addListener(new ClassificationChangedAdapter() {

				@Override
				public void classifierAvailableNewClasses(
						ClassificationChangeEvent e) {
					LOGGER.info("Histogram should appead now");
					jFreeChartJLabel
							.setIcon(new ImageIcon(getHistogramImage()));
					jFreeChartJLabel.setCursor(null);
				}

				@Override
				public void classifierCalculatingStatistics(
						ClassificationChangeEvent e) {
					LOGGER.info("Histogram should vanish now");
					jFreeChartJLabel.setIcon(new ImageIcon(WAIT_IMAGE));
					jFreeChartJLabel.setCursor(Cursor
							.getPredefinedCursor(Cursor.WAIT_CURSOR));
				}

			});
			jFreeChartJLabel.setIcon(new ImageIcon(getHistogramImage()));
		}
		return jFreeChartJLabel;
	}

	/**
	 * This method creates the Histogram image with JFreeChart
	 * 
	 * @return
	 */
	BufferedImage getHistogramImage() {

		HistogramDataset hds = new HistogramDataset();
		DoubleArrayList valuesAL;
		try {
			valuesAL = classifier.getStatistics().elements();
			// new double[] {0.4,3,4,2,5.,22.,4.,2.,33.,12.}
			double[] elements = Arrays.copyOf(valuesAL.elements(), classifier
					.getStatistics().size());
			hds.addSeries(1, elements, histogramBins);
		} catch (Exception e) {
			LOGGER.error(e);
			return ERROR_IMAGE;
		}


		/**
		 * Label the x-axis. If a NormalizerField has been selected, this has to
		 * be presented here as well. Where possible use the AttributeMetaData
		 * information.
		 */
		String label_xachsis;
		{
			AttributeMetadata amdValue = atlasStyler.getAttributeMetaDataMap()
					.get(classifier.getValue_field_name());

			// AttributeMetadata amdValue = ASUtil.getAttributeMetadataFor(
			// atlasStyler, classifier.getValue_field_name());
			if (amdValue != null
					&& (!I8NUtil.isEmpty(amdValue.getTitle().toString()))) {
				label_xachsis = amdValue.getTitle().toString();
			} else
				label_xachsis = classifier.getValue_field_name();

			if (classifier.getNormalizer_field_name() != null) {
				// AttributeMetadata amdNorm = ASUtil.getAttributeMetadataFor(
				// atlasStyler, classifier.getNormalizer_field_name());
				AttributeMetadata amdNorm = atlasStyler
						.getAttributeMetaDataMap().get(
								classifier.getNormalizer_field_name());
				if (amdNorm != null
						&& (!I8NUtil.isEmpty(amdNorm.getTitle().toString()))) {
					label_xachsis += "/" + amdNorm.getTitle().toString();
				} else
					label_xachsis += "/"
							+ classifier.getNormalizer_field_name();
			}
		}

		/** Statically label the Y Axis **/
		String label_yachsis = AtlasStyler
				.R("QuantitiesClassificationGUI.Histogram.YAxisLabel");

		JFreeChart chart = org.jfree.chart.ChartFactory.createHistogram(null,
				label_xachsis, label_yachsis, hds, PlotOrientation.VERTICAL,
				false, true, true);

		/***********************************************************************
		 * Paint the classes into the JFreeChart
		 */
		int countLimits = 0;
		for (Double cLimit : classifier.getClassLimits()) {
			ValueMarker marker = new ValueMarker(cLimit);
			XYPlot plot = chart.getXYPlot();
			marker.setPaint(Color.orange);
			marker.setLabel(String.valueOf(countLimits));
			marker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
			marker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
			plot.addDomainMarker(marker);

			countLimits++;
		}

		/***********************************************************************
		 * Optionally painting SD and MEAN into the histogram
		 */
		try {
			if (jCheckBoxShowSD.isSelected()) {
				ValueMarker marker;
				marker = new ValueMarker(classifier.getStatistics()
						.standardDeviation(), Color.green.brighter(),
						new BasicStroke(1.5f));
				XYPlot plot = chart.getXYPlot();
				marker
						.setLabel(AtlasStyler
								.R("QuantitiesClassificationGUI.Histogram.SD.ShortLabel"));
				marker.setLabelAnchor(RectangleAnchor.BOTTOM_LEFT);
				marker.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
				plot.addDomainMarker(marker);
			}

			if (jCheckBoxShowMean.isSelected()) {
				ValueMarker marker;
				marker = new ValueMarker(classifier.getStatistics().mean(),
						Color.green.darker(), new BasicStroke(1.5f));
				XYPlot plot = chart.getXYPlot();
				marker
						.setLabel(AtlasStyler
								.R("QuantitiesClassificationGUI.Histogram.Mean.ShortLabel"));
				marker.setLabelAnchor(RectangleAnchor.BOTTOM_LEFT);
				marker.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
				plot.addDomainMarker(marker);
			}

		} catch (Exception e) {
			LOGGER.error("Painting SD and MEAN into the histogram", e);
		}

		/***********************************************************************
		 * Render the Chart
		 */
		BufferedImage image = chart.createBufferedImage(400, 200);

		return image;
	}

	/**
	 * This method initializes jToggleButton
	 * 
	 * @return javax.swing.JToggleButton
	 */
	private JToggleButton getJToggleButton() {
		if (jToggleButton == null) {
			jToggleButton = new JToggleButton();
			jToggleButton.setText(AtlasStyler
					.R("QuantitiesClassificationGUI.ShowInPercent.Button"));
			jToggleButton.setToolTipText(AtlasStyler
					.R("QuantitiesClassificationGUI.ShowInPercent.Button.TT"));
		}
		return jToggleButton;
	}

	/**
	 * This JTable shows the breaks of the classes. It is editable and has a
	 * opup menu to add/remove class-breaks.
	 */
	private JTable getJTableBreakValues() {
		if (jTableBreakValues == null) {
			jTableBreakValues = new JTable();

			jTableBreakValues.setModel(new DefaultTableModel() {

				@Override
				public Class<?> getColumnClass(int columnIndex) {
					if (columnIndex == 0)
						return Integer.class;
					if (columnIndex == 1)
						return String.class;
					return null;
				}

				@Override
				public int getColumnCount() {
					return 2;
				}

				@Override
				public String getColumnName(int columnIndex) {
					if (columnIndex == 0)
						return "#";
					if (columnIndex == 1)
						return AtlasStyler.R("Classification.BreakValues");
					return super.getColumnName(columnIndex);
				}

				@Override
				public int getRowCount() {
					return classifier.getClassLimits().size();
				}

				@Override
				public Object getValueAt(int rowIndex, int columnIndex) {
					if (columnIndex == 1)
						return NumberFormat
								.getNumberInstance()
								.format(
										classifier.getClassLimits().toArray()[rowIndex]);
					return rowIndex;
				}

				@Override
				public boolean isCellEditable(int rowIndex, int columnIndex) {
					if (columnIndex == 1)
						return true;
					return false;
				}

				/**
				 * This JTable is editable
				 */
				@Override
				public void setValueAt(Object aValue, int rowIndex,
						int columnIndex) {
					if (columnIndex == 1) {

						try {
							Number aValue2 = NumberFormat.getNumberInstance()
									.parse((String) aValue);

							if (classifier.getMethod() != METHOD.MANUAL) {
								classifier.setMethod(METHOD.MANUAL);
							}

							Object oldValue = classifier.getClassLimits()
									.toArray()[rowIndex];
							classifier.getClassLimits().remove(oldValue);
							classifier.getClassLimits().add(
									aValue2.doubleValue());
						} catch (Exception e) {
							return;
						} finally {
							classifier.fireEvent(new ClassificationChangeEvent(
									CHANGETYPES.CLASSES_CHG));
						}

					}
				}
			});

			classifier.addListener(new ClassificationChangedAdapter() {
				@Override
				public void classifierAvailableNewClasses(
						ClassificationChangeEvent e) {
					((DefaultTableModel) (jTableBreakValues.getModel()))
							.fireTableDataChanged();
					getJTableBreakValues().setEnabled(true);
				}

				@Override
				public void classifierCalculatingStatistics(
						ClassificationChangeEvent e) {
					getJTableBreakValues().setEnabled(false);
				}

			});

			jTableBreakValues.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			SwingUtil.setColumnLook(jTableBreakValues, 0, null, null, null, 30);
			SwingUtil.setColumnLook(jTableBreakValues, 1, ASUtil
					.getDoubleCellRenderer(), 30, null, null);

			((JLabel) jTableBreakValues.getDefaultRenderer(Integer.class))
					.setHorizontalAlignment(SwingConstants.CENTER);

			/**
			 * Adding a right click mouse menu...
			 */
			jTableBreakValues.addMouseListener(new MouseAdapter() {

				private void maybeShowPopup(MouseEvent e) {
					if (e.isPopupTrigger() && jTableBreakValues.isEnabled()) {
						Point p = new Point(e.getX(), e.getY());
						// int col = jTableBreakValues.columnAtPoint(p);
						int row = jTableBreakValues.rowAtPoint(p);
						//
						// // translate table index to model index
						// int mcol = jTableBreakValues.getColumn(
						// jTableBreakValues.getColumnName(col))
						// .getModelIndex();

						if (row >= 0 && row < jTableBreakValues.getRowCount()) {
							cancelCellEditing();

							// create popup menu...
							JPopupMenu contextMenu = createContextMenu(row);

							// ... and show it
							if (contextMenu != null
									&& contextMenu.getComponentCount() > 0) {
								contextMenu.show(jTableBreakValues, p.x, p.y);
							}
						}
					}
				}

				private JPopupMenu createContextMenu(final int rowIndex) {

					JPopupMenu contextMenu = new JPopupMenu();

					/**
					 * This action allows to add a new class-break at the
					 * position if the mouse
					 */
					JMenuItem insertMenu = new JMenuItem();
					insertMenu.setText(AtlasStyler
							.R("Classification.BreakValues.InsertNew"));

					insertMenu.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {

							// The value that the mouse is on
							Double value = (Double) jTableBreakValues
									.getModel().getValueAt(rowIndex, 1);

							// Calculate #newValue that is the middle between
							// the selected and the next/previous class-break.
							// Integer secondAffectedClassNumber = null;
							// Integer newClassNumber = null;

							Double newValue;
							if (rowIndex == 0) {
								// The click was on the first class

								if (jTableBreakValues.getModel().getRowCount() > 1) {
									Double valueAfter = (Double) jTableBreakValues
											.getModel().getValueAt(
													rowIndex + 1, 1);
									newValue = valueAfter
											+ ((value - valueAfter) / 2);

									// newClassNumber = 1;
									// secondAffectedClassNumber = 0;
								} else {
									// We have zero or one break
									newValue = value + 1;
									// newClassNumber = 0;
								}

							} else {
								// The click was between the first and the last
								// class
								Double valueBefore = (Double) jTableBreakValues
										.getModel().getValueAt(rowIndex - 1, 1);
								newValue = valueBefore
										+ ((value - valueBefore) / 2);

								// if (rowIndex < classifier.getClassLimits()
								// .size()) {
								// // newClassNumber = rowIndex;
								// // secondAffectedClassNumber = rowIndex - 1;
								// } else {
								// // The click was on the last class
								// // newClassNumber = rowIndex;
								// // secondAffectedClassNumber = rowIndex + 1;
								// }
							}

							// Editing the classbreaks always turn's the
							// classification method to MANUAL
							if (classifier.getMethod() != METHOD.MANUAL) {
								classifier.setMethod(METHOD.MANUAL);
							}

							// The newValue has been calculated. Now insert it
							// into the ClassLimits. Because ClassLimits is a
							// sorted TreeSet, it will automatically be ordered
							// correctly.
							classifier.getClassLimits().add(newValue);

							classifier.fireEvent(new ClassificationChangeEvent(
									CHANGETYPES.NUM_CLASSES_CHG));
							classifier.fireEvent(new ClassificationChangeEvent(
									CHANGETYPES.CLASSES_CHG));
						}

					});
					contextMenu.add(insertMenu);

					JMenuItem removeMenu = new JMenuItem();
					removeMenu.setText(AtlasStyler
							.R("Classification.BreakValues.RemoveBreak"));
					removeMenu.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {

							if (jTableBreakValues.getModel().getRowCount() <= 2) {
								// Hey! Please don't remove the last
								// breaks...\nthere would be no class left.
								return;
							}

							if (classifier.getMethod() != METHOD.MANUAL) {
								classifier.setMethod(METHOD.MANUAL);
							}

							Double value = (Double) jTableBreakValues
									.getModel().getValueAt(rowIndex, 1);
							classifier.getClassLimits().remove(value);

							classifier.fireEvent(new ClassificationChangeEvent(
									CHANGETYPES.NUM_CLASSES_CHG));
							classifier.fireEvent(new ClassificationChangeEvent(
									CHANGETYPES.CLASSES_CHG));
						}
					});
					contextMenu.add(removeMenu);

					JMenuItem copyMenu = new JMenuItem();
					copyMenu.setText(AtlasStyler
							.R("Classification.BreakValues.Copy"));
					copyMenu.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							Object value = jTableBreakValues.getModel()
									.getValueAt(rowIndex, 1);
							LangUtil.copyToClipboard(value == null ? "" : value
									.toString());
						}
					});
					contextMenu.add(copyMenu);

					JMenuItem pasteMenu = new JMenuItem();
					pasteMenu.setText(AtlasStyler
							.R("Classification.BreakValues.Paste"));
					if (ASUtil.isClipboardContainingText(this)
							&& getJTableBreakValues().getModel()
									.isCellEditable(rowIndex, 1)) {

						pasteMenu.addActionListener(new ActionListener() {

							public void actionPerformed(ActionEvent e) {
								String value = ASUtil
										.getClipboardContents(QuantitiesClassificationGUI.this);
								getJTableBreakValues().getModel().setValueAt(
										value, rowIndex, 1);

								classifier
										.fireEvent(new ClassificationChangeEvent(
												CHANGETYPES.CLASSES_CHG));

							}
						});
					} else {
						pasteMenu.setEnabled(false);
					}
					contextMenu.add(pasteMenu);

					return contextMenu;
				}

				@Override
				public void mousePressed(MouseEvent e) {
					maybeShowPopup(e);
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					maybeShowPopup(e);
				}
			});

		}
		return jTableBreakValues;
	};

	private void cancelCellEditing() {
		CellEditor ce = getJTableBreakValues().getCellEditor();
		if (ce != null) {
			ce.cancelCellEditing();
		}
	}

	/**
	 * This method initializes jPanelHistParams
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelHistogramParameters() {
		if (jPanelHistParams == null) {
			GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
			gridBagConstraints20.gridx = 5;
			gridBagConstraints20.insets = new Insets(0, 0, 0, 5);
			gridBagConstraints20.gridy = 0;
			jLabelShowMean = new JLabel(
					AtlasStyler
							.R("QuantitiesClassificationGUI.HistogramParameters.ShowMean"));
			GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
			gridBagConstraints19.gridx = 4;
			gridBagConstraints19.insets = new Insets(0, 20, 0, 0);
			gridBagConstraints19.gridy = 0;
			GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
			gridBagConstraints18.gridx = 3;
			gridBagConstraints18.gridy = 0;
			jLabelShowSD = new JLabel(
					AtlasStyler
							.R("QuantitiesClassificationGUI.HistogramParameters.ShowSD"));
			GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
			gridBagConstraints17.gridx = 2;
			gridBagConstraints17.insets = new Insets(0, 20, 0, 0);
			gridBagConstraints17.gridy = 0;
			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
			gridBagConstraints16.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints16.gridy = 0;
			gridBagConstraints16.weightx = 1.0;
			gridBagConstraints16.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints16.gridx = 1;
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			gridBagConstraints15.gridx = 0;
			gridBagConstraints15.gridy = 0;
			jLabel3 = new JLabel(
					AtlasStyler
							.R("QuantitiesClassificationGUI.HistogramParameters.NoOfColums"));
			jLabel3
					.setToolTipText(AtlasStyler
							.R("QuantitiesClassificationGUI.HistogramParameters.NoOfColums.TT"));
			jPanelHistParams = new JPanel();
			jPanelHistParams.setLayout(new GridBagLayout());
			jPanelHistParams.add(jLabel3, gridBagConstraints15);
			jPanelHistParams.add(getJComboBoxColumns(), gridBagConstraints16);
			jPanelHistParams.add(getJCheckBoxShowSD(), gridBagConstraints17);
			jPanelHistParams.add(jLabelShowSD, gridBagConstraints18);
			jPanelHistParams.add(getJCheckBoxShowMean(), gridBagConstraints19);
			jPanelHistParams.add(jLabelShowMean, gridBagConstraints20);
		}
		return jPanelHistParams;
	}

	/**
	 * This method initializes jComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxColumns() {
		if (jComboBoxColumns == null) {
			jComboBoxColumns = new JComboBox();

			Integer[] items = new Integer[100];
			for (Integer i = 1; i <= 100; i++) {
				items[i - 1] = i;
			}

			jComboBoxColumns.setModel(new DefaultComboBoxModel(items));
			jComboBoxColumns.setSelectedIndex(histogramBins);

			jComboBoxColumns.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						histogramBins = (Integer) e.getItem();
						jFreeChartJLabel.setIcon(new ImageIcon(
								getHistogramImage()));
					}
				}
			});

			// Hier dreht was falsch!
			ASUtil.addMouseWheelForCombobox(jComboBoxColumns, false);
		}
		return jComboBoxColumns;
	}

	/**
	 * This method initializes jCheckBox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCheckBoxShowSD() {
		if (jCheckBoxShowSD == null) {
			jCheckBoxShowSD = new JCheckBox();
			jCheckBoxShowSD.setAction(new AbstractAction() {

				public void actionPerformed(ActionEvent e) {
					// THe getHistogrammImage() function checks the state of
					// this checkbox
					jFreeChartJLabel
							.setIcon(new ImageIcon(getHistogramImage()));
				}

			});
		}
		return jCheckBoxShowSD;
	}

	/**
	 * This method initializes jCheckBox1
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCheckBoxShowMean() {
		if (jCheckBoxShowMean == null) {
			jCheckBoxShowMean = new JCheckBox();
			jCheckBoxShowMean.setAction(new AbstractAction() {

				public void actionPerformed(ActionEvent e) {
					// THe getHistogrammImage() function checks the state of
					// this checkbox
					jFreeChartJLabel
							.setIcon(new ImageIcon(getHistogramImage()));
				}

			});
		}
		return jCheckBoxShowMean;
	}

}
