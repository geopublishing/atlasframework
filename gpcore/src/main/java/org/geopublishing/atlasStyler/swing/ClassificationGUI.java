/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasStyler.swing;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.CellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.classification.CLASSIFICATION_METHOD;
import org.geopublishing.atlasStyler.classification.Classification;
import org.geopublishing.atlasStyler.classification.ClassificationChangeEvent;
import org.geopublishing.atlasStyler.classification.ClassificationChangeEvent.CHANGETYPES;
import org.geopublishing.atlasStyler.classification.ClassificationChangedAdapter;

import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.AtlasDialog;
import de.schmitzm.swing.JPanel;
import de.schmitzm.swing.SwingUtil;

/**
 * This {@link JDialog} presents the user with descriptive statistics and a
 * histogram for your data. He may also choose the classification method or edit
 * the class breaks manually.
 * 
 * @author SK
 * 
 */
public abstract class ClassificationGUI extends AtlasDialog {
	protected final static Logger LOGGER = Logger
			.getLogger(ClassificationGUI.class);

	protected static final BufferedImage ERROR_IMAGE = new BufferedImage(400,
			200, BufferedImage.TYPE_3BYTE_BGR);

	private static final BufferedImage WAIT_IMAGE = new BufferedImage(400, 200,
			BufferedImage.TYPE_3BYTE_BGR);

	private JPanel jContentPane = null;

	private JPanel jPanelLinksOben = null;

	private JLabel jLabelMethodSelection = null;

	private JLabel jLabelParameter = null;

	private JComboBox jComboBoxMethod = null;

	private JPanel jPanelDescriptiveStatistics = null;

	private JTable jTableStats = null;

	private JPanel panelLowerPart = null;

	private JLabel jFreeChartJLabel = null;

	private JToggleButton jToggleButton = null;

	private JTable jTableBreakValues = null;

	protected final Classification classifier;

	private JPanel jPanelHistParams = null;

	private JLabel jLabelHistogrammColumns = null;

	private JComboBox jComboBoxColumns = null;

	private JCheckBox jCheckBoxShowSD = null;

	private JCheckBox jCheckBoxShowMean = null;

	Integer histogramBins = 59;

	protected final AtlasStyler atlasStyler;

	public ClassificationGUI(Component owner, Classification classifier,
			AtlasStyler atlasStyler, String title) {
		super(SwingUtil.getParentWindow(owner), title);
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
		this.setModal(true);
		this.setContentPane(getJContentPane());
		pack();
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel(new MigLayout("flowy, gap 1, inset 1"));

			jContentPane.add(getJPanelLinksOben(), "sgx1, split 3");
			jContentPane.add(getJPanelData(), "sgx1");
			jContentPane.add(getJPanelHistogramParameters());
			jContentPane
					.add(getJPanelLowerPart(), "gap related, spanx 2, wrap");
			jContentPane.add(getJPanelDescriptiveStatistics(), "right, top");

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
			jLabelParameter = new JLabel(
					AtlasStylerVector.R("ComboBox.NumberOfClasses"));

			jLabelParameter.setToolTipText(ASUtil
					.R("ComboBox.NumberOfClasses.TT"));

			jLabelMethodSelection = new JLabel(
					ASUtil.R("QuantitiesClassificationGUI.Combobox.Method"));

			jPanelLinksOben = new JPanel(
					new MigLayout("wrap 2, gap 1, inset 1"),
					ASUtil.R("GraduatedColorQuantities.classification.BorderTitle"));

			jPanelLinksOben.add(jLabelMethodSelection);
			jPanelLinksOben.add(getJComboBoxMethod());
			jPanelLinksOben.add(jLabelParameter);
			jPanelLinksOben.add(new NumClassesJComboBox(classifier));
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

	private JPanel getJPanelDescriptiveStatistics() {
		if (jPanelDescriptiveStatistics == null) {
			jPanelDescriptiveStatistics = new JPanel(new MigLayout(
					"gap 1, inset 1"));
			jPanelDescriptiveStatistics
					.setBorder(BorderFactory.createTitledBorder(ASUtil
							.R("QuantitiesClassificationGUI.Statistics.BorderTitle")));
			final JScrollPane scrollPane = new JScrollPane(
					getJTableStatistics());
			SwingUtil.setPreferredWidth(scrollPane, 250);
			SwingUtil.setPreferredHeight(scrollPane, 140);
			jPanelDescriptiveStatistics.add(scrollPane);
			SwingUtil.setMinimumWidth(jPanelDescriptiveStatistics, 2);

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
			 * Classification.DescriptiveStatistics.Subsampled=Subsampling:
			 * Classification.DescriptiveStatistics.Count=Count:
			 * Classification.DescriptiveStatistics.Min=Minimum:
			 * Classification.DescriptiveStatistics.Max=Maximum:
			 * Classification.DescriptiveStatistics.Sum=Summe:
			 * Classification.DescriptiveStatistics.Mean=airthm. Mittel:
			 * Classification.DescriptiveStatistics.Median=Median:
			 * Classification.DescriptiveStatistics.SD=Standard deviation:
			 * Classification.DescriptiveStatistics.Excluded=Excluded:
			 */

			jTableStats.setModel(new DefaultTableModel() {
				final String[] fieldNames = new String[] {
						ASUtil.R("Classification.DescriptiveStatistics.Subsampling"),
						ASUtil.R("Classification.DescriptiveStatistics.Count"),
						ASUtil.R("Classification.DescriptiveStatistics.Min"),
						ASUtil.R("Classification.DescriptiveStatistics.Max"),
						ASUtil.R("Classification.DescriptiveStatistics.Sum"),
						ASUtil.R("Classification.DescriptiveStatistics.Mean"),
						ASUtil.R("Classification.DescriptiveStatistics.Median"),
						ASUtil.R("Classification.DescriptiveStatistics.SD"),
						ASUtil.R("Classification.DescriptiveStatistics.Excluded") };

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
					return 9;
				}

				@Override
				public Object getValueAt(int rowIndex, int columnIndex) {
					if (columnIndex == 0)
						return fieldNames[rowIndex];

					try {
						if (rowIndex == 0) {
							// Subsampling to every nth values
							if (classifier.getSubsampling() > 1)
								return ASUtil.R("Classification.DescriptiveStatistics.SubsamplingEveryNthValue",classifier.getSubsampling());
							else
								return ASUtil.R("Classification.DescriptiveStatistics.SubsamplingAll");
						}
						if (rowIndex == 1) // Count
							return classifier.getCount();
						if (rowIndex == 2) // Min
							return classifier.getMin();
						if (rowIndex == 3) // Max
							return classifier.getMax();
						if (rowIndex == 4) // Sum
							return classifier.getSum();
						if (rowIndex == 5) // Mean
							return classifier.getMean();
						if (rowIndex == 6) // Median
							return classifier.getMedian();
						if (rowIndex == 7) // SD
							return classifier.getSD();
						if (rowIndex == 8) // NODATA
							return classifier.getNoDataValuesCount();
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

			jTableStats.setDefaultRenderer(Double.class,
					ASUtil.getDoubleCellRenderer());

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
					((DefaultTableModel) getJTableStatistics().getModel())
							.fireTableStructureChanged();
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
	private JPanel getJPanelLowerPart() {
		if (panelLowerPart == null) {
			panelLowerPart = new JPanel(new MigLayout("flowy, gap 1, inset 1"));
			JLabel jLabelBreaksTable = new JLabel(
					AtlasStylerVector.R("Classification.BreakValues"));
			jLabelBreaksTable.setToolTipText(AtlasStylerVector
					.R("Classification.BreakValues.TT"));

			JLabel jLabelBreaksExplanation = new JLabel(
					"<html><font size='-2'>"
							+ AtlasStylerVector
									.R("Classification.BreakValues.TT")
							+ "</html>");

			panelLowerPart.add(getHistogram(), "wrap");
			panelLowerPart.add(jLabelBreaksTable, "split 3");
			panelLowerPart.add(jLabelBreaksExplanation);
			panelLowerPart.add(new JScrollPane(getJTableBreakValues()));

			SwingUtil.setPreferredHeight(panelLowerPart, (int) getHistogram()
					.getPreferredSize().getHeight());

			SwingUtil.setPreferredWidth(panelLowerPart, (int) getHistogram()
					.getPreferredSize().getWidth() + 200);

		}
		return panelLowerPart;
	}

	/**
	 * @return A {@link JLabel} showing the histogram. The {@link JLabel} is
	 *         updated when new classes are available
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
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

	protected abstract BufferedImage getHistogramImage();

	/**
	 * This method initializes jToggleButton
	 * 
	 * @return javax.swing.JToggleButton
	 */
	private JToggleButton getJToggleButton() {
		if (jToggleButton == null) {
			jToggleButton = new JToggleButton();
			jToggleButton.setText(ASUtil
					.R("QuantitiesClassificationGUI.ShowInPercent.Button"));
			jToggleButton.setToolTipText(ASUtil
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

			jTableBreakValues
					.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			jTableBreakValues.setModel(new DefaultTableModel() {

				@Override
				public Class<?> getColumnClass(int columnIndex) {
					if (columnIndex == 0)
						return Integer.class;
					if (columnIndex == 1)
						return Double.class;
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
						return ASUtil.R("Classification.BreakValues");
					return super.getColumnName(columnIndex);
				}

				@Override
				public int getRowCount() {
					return classifier.getClassLimits().size();
				}

				@Override
				public Object getValueAt(int rowIndex, int columnIndex) {
					if (columnIndex == 1)
						return classifier.getClassLimits().toArray()[rowIndex];
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
							Double aValue2 = (Double) aValue;

							if (classifier.getMethod() != CLASSIFICATION_METHOD.MANUAL) {
								classifier
										.setMethod(CLASSIFICATION_METHOD.MANUAL);
							}

							Object oldValue = classifier.getClassLimits()
									.toArray()[rowIndex];
							classifier.getClassLimits().remove(oldValue);
							classifier.getClassLimits().add(aValue2);
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
			SwingUtil.setColumnLook(jTableBreakValues, 1,
					ASUtil.getDoubleCellRenderer(), 30, null, null);

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
							jTableBreakValues.getSelectionModel()
									.addSelectionInterval(row, row);
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
					insertMenu.setText(ASUtil
							.R("Classification.BreakValues.InsertNew"));

					insertMenu.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {

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
							if (classifier.getMethod() != CLASSIFICATION_METHOD.MANUAL) {
								classifier
										.setMethod(CLASSIFICATION_METHOD.MANUAL);
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
					removeMenu.setText(ASUtil
							.R("Classification.BreakValues.RemoveBreak"));
					removeMenu.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {

							if (jTableBreakValues.getModel().getRowCount() <= 2) {
								// Hey! Please don't remove the last
								// breaks...\nthere would be no class left.
								return;
							}

							if (classifier.getMethod() != CLASSIFICATION_METHOD.MANUAL) {
								classifier
										.setMethod(CLASSIFICATION_METHOD.MANUAL);
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
					copyMenu.setText(ASUtil
							.R("Classification.BreakValues.Copy"));
					copyMenu.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							Object value = jTableBreakValues.getModel()
									.getValueAt(rowIndex, 1);
							LangUtil.copyToClipboard(value == null ? "" : value
									.toString());
						}
					});
					contextMenu.add(copyMenu);

					JMenuItem pasteMenu = new JMenuItem();
					pasteMenu.setText(ASUtil
							.R("Classification.BreakValues.Paste"));
					if (ASUtil.isClipboardContainingText(this)
							&& getJTableBreakValues().getModel()
									.isCellEditable(rowIndex, 1)) {

						pasteMenu.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								String value = ASUtil
										.getClipboardContents(ClassificationGUI.this);
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
			jLabelHistogrammColumns = new JLabel(
					ASUtil.R("QuantitiesClassificationGUI.HistogramParameters.NoOfColums"));
			jLabelHistogrammColumns
					.setToolTipText(ASUtil
							.R("QuantitiesClassificationGUI.HistogramParameters.NoOfColums.TT"));
			jPanelHistParams = new JPanel(new MigLayout("gap 1, inset 1"));
			jPanelHistParams.add(jLabelHistogrammColumns);
			jPanelHistParams.add(getJComboBoxColumns(), "gap rel");
			jPanelHistParams.add(getJCheckBoxShowSD(), "gap unrel");
			jPanelHistParams.add(getJCheckBoxShowMean(), "gap unrel");
		}
		return jPanelHistParams;
	}

	/**
	 * This method initializes jComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	protected JComboBox getJComboBoxColumns() {
		if (jComboBoxColumns == null) {
			jComboBoxColumns = new JComboBox();

			Integer[] items = new Integer[100];
			for (Integer i = 1; i <= 100; i++) {
				items[i - 1] = i;
			}

			jComboBoxColumns.setModel(new DefaultComboBoxModel(items));
			jComboBoxColumns.setSelectedIndex(histogramBins);

			jComboBoxColumns.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						histogramBins = (Integer) e.getItem();
						jFreeChartJLabel.setIcon(new ImageIcon(
								getHistogramImage()));
					}
				}
			});

			// Hier dreht was falsch!
			SwingUtil.addMouseWheelForCombobox(jComboBoxColumns, false);
		}
		return jComboBoxColumns;
	}

	/**
	 * This method initializes jCheckBox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	protected JCheckBox getJCheckBoxShowSD() {
		if (jCheckBoxShowSD == null) {
			jCheckBoxShowSD = new JCheckBox(
					new AbstractAction(
							ASUtil.R("QuantitiesClassificationGUI.HistogramParameters.ShowSD")) {

						@Override
						public void actionPerformed(ActionEvent e) {
							jFreeChartJLabel.setIcon(new ImageIcon(
									getHistogramImage()));
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
	protected JCheckBox getJCheckBoxShowMean() {
		if (jCheckBoxShowMean == null) {
			jCheckBoxShowMean = new JCheckBox(
					new AbstractAction(
							ASUtil.R("QuantitiesClassificationGUI.HistogramParameters.ShowMean")) {

						@Override
						public void actionPerformed(ActionEvent e) {
							// THe getHistogrammImage() function checks the
							// state of
							// this checkbox
							jFreeChartJLabel.setIcon(new ImageIcon(
									getHistogramImage()));
						}

					});
		}
		return jCheckBoxShowMean;
	}

	abstract protected JPanel getJPanelData();

	public Classification getClassifier() {
		return classifier;
	}

}
