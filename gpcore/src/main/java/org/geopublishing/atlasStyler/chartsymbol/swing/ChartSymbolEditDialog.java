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
package org.geopublishing.atlasStyler.chartsymbol.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.svg.swing.SVGSelector;
import org.geopublishing.atlasStyler.swing.AttributesJComboBox;
import org.geopublishing.atlasStyler.swing.ColorTableCellRenderer;
import org.geotools.styling.Graphic;
import org.geotools.styling.visitor.DuplicatingStyleVisitor;
import org.opengis.feature.simple.SimpleFeatureType;

import de.schmitzm.geotools.feature.FeatureUtil;
import de.schmitzm.geotools.styling.StyledFeaturesInterface;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.geotools.styling.chartsymbols.ChartGraphic;
import de.schmitzm.geotools.styling.chartsymbols.ChartGraphic.ChartTyp;
import de.schmitzm.geotools.styling.chartsymbols.ChartGraphicChangeListener;
import de.schmitzm.geotools.styling.chartsymbols.ChartGraphicChangedEvent;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.CancellableDialogAdapter;
import de.schmitzm.swing.table.ColorEditor;

/**
 * Swing dialog to create/define a chartsymbol.
 * 
 * @see http://docs.geotools.org/latest/userguide/library/render/chart.html
 */
public class ChartSymbolEditDialog extends CancellableDialogAdapter {

	static private final Logger LOGGER = LangUtil
			.createLogger(ChartSymbolEditDialog.class);

	private JPanel jContentPane;

	private Graphic backup;

	private JPanel jPanelButtons;

	private final List<String> numericalAttributeNames;

	private final ChartGraphic chartGraphic;

	private final AtlasStylerVector asv;

	final ChartGraphicChangeListener listenerPropagateGraphicChangeToGuiEvent = new ChartGraphicChangeListener() {

		@Override
		public void changed(ChartGraphicChangedEvent e) {

			Graphic g = StylingUtil.STYLE_BUILDER.createGraphic(
					chartGraphic.getChartGraphic(), null, null);

			ChartSymbolEditDialog.this.firePropertyChange(
					SVGSelector.PROPERTY_UPDATED, null, g);
		}
	};

	public ChartSymbolEditDialog(Component parentWindow, Graphic importThis,
			final AtlasStylerVector asv) {
		super(parentWindow);
		this.asv = asv;

		// Copy the Graphic as a backup in case cancel is selected.
		DuplicatingStyleVisitor backupCopy = new DuplicatingStyleVisitor();
		importThis.accept(backupCopy);
		backup = (Graphic) backupCopy.getCopy();

		StyledFeaturesInterface<?> styledFeatures = asv.getStyledFeatures();
		SimpleFeatureType schema = styledFeatures.getSchema();
		numericalAttributeNames = FeatureUtil.getNumericalFieldNames(schema);

		chartGraphic = new ChartGraphic(importThis);

		// Register a listener to propagate ChartGraphic changes to the preview
		// windows (finally to the preview map)
		chartGraphic.addListener(listenerPropagateGraphicChangeToGuiEvent);

		initialize();
	}

	/**
	 * A property changed event with this ID is fired, when the ExternalGraphic
	 * has been changed.
	 */
	public static final String PROPERTY_UPDATED = "Property Updated event ID";

	protected static final int COLIDX_NAME = 0;
	protected static final int COLIDX_COLOR = 1;
	protected static final int COLIDX_DELETE = 2;

	private void initialize() {
		this.setSize(450, 450);
		this.setContentPane(getJContentPane());
	}

	/**
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel(new MigLayout());

			jContentPane.add(getChartTypCombobox(), "gap 20, split 3");
			jContentPane.add(getAddAttributeJComboBox());
			jContentPane.add(getAddAttributeJButton(), "wrap");

			jContentPane.add(new JScrollPane(getAttributesJTable()), "wrap");

			jContentPane.add(getChartImageSizePanel(), "wrap");
			jContentPane.add(getScalingPanel(), "wrap");

			jContentPane.add(getJPanelButtons(), "bottom");
		}
		return jContentPane;
	}

	JPanel scalingPanel;

	private Component getScalingPanel() {
		if (scalingPanel == null) {
			scalingPanel = new JPanel(new MigLayout());

			final JTextField inputMaxValue = new JTextField(20);

			inputMaxValue.getDocument().addDocumentListener(
					new DocumentListener() {

						@Override
						public void removeUpdate(DocumentEvent e) {
							extracted();
						}

						private void extracted() {
							try {
								chartGraphic.setMaxValue(Double
										.valueOf(inputMaxValue.getText()));
							} catch (Exception e) {
								chartGraphic.setMaxValue(null);
							}
						}

						@Override
						public void insertUpdate(DocumentEvent e) {
							extracted();
						}

						@Override
						public void changedUpdate(DocumentEvent e) {
							extracted();
						}
					});
			if (chartGraphic.getMaxValue() != null)
				inputMaxValue.setText("" + chartGraphic.getMaxValue());
			else
				inputMaxValue.setText("" + 100);

			scalingPanel.add(new JLabel("Max Value:"));
			scalingPanel.add(inputMaxValue, "wrap");

		}
		return scalingPanel;
	}

	JPanel chartImageSizePanel;

	private Component getChartImageSizePanel() {
		if (chartImageSizePanel == null) {

			// Width
			final JTextField inputWidth = new JTextField(5);
			inputWidth.getDocument().addDocumentListener(
					new DocumentListener() {

						@Override
						public void removeUpdate(DocumentEvent e) {
							extracted();
						}

						private void extracted() {
							try {
								chartGraphic.setImageWidth(Integer
										.valueOf(inputWidth.getText()));
							} catch (Exception e) {
								chartGraphic.setImageWidth(null);
							}
						}

						@Override
						public void insertUpdate(DocumentEvent e) {
							extracted();
						}

						@Override
						public void changedUpdate(DocumentEvent e) {
							extracted();
						}
					});
			if (chartGraphic.getImageWidth() != null)
				inputWidth.setText("" + chartGraphic.getImageWidth());
			else
				inputWidth.setText("" + ChartGraphic.DEFAULT_WIDTH);

			// Height
			final JTextField inputHeight = new JTextField(5);
			inputHeight.getDocument().addDocumentListener(
					new DocumentListener() {

						@Override
						public void removeUpdate(DocumentEvent e) {
							extracted();
						}

						private void extracted() {
							try {
								chartGraphic.setImageHeight(Integer
										.valueOf(inputHeight.getText()));
							} catch (Exception e) {
								chartGraphic.setImageHeight(null);
							}
						}

						@Override
						public void insertUpdate(DocumentEvent e) {
							extracted();
						}

						@Override
						public void changedUpdate(DocumentEvent e) {
							extracted();
						}
					});
			if (chartGraphic.getImageHeight() != null)
				inputHeight.setText("" + chartGraphic.getImageHeight());
			else
				inputHeight.setText("" + ChartGraphic.DEFAULT_HEIGHT);

			chartImageSizePanel = new JPanel(new MigLayout());
			chartImageSizePanel.add(new JLabel(
					"Define a base size for the chart image."), "wrap");
			chartImageSizePanel.add(inputWidth, "split 4");
			chartImageSizePanel.add(new JLabel("px width"), "");
			chartImageSizePanel.add(inputHeight, "");
			chartImageSizePanel.add(new JLabel("px height"), "wrap");

		}
		return chartImageSizePanel;
	}

	JButton addAttributeJButton;

	private JButton getAddAttributeJButton() {
		if (addAttributeJButton == null) {
			addAttributeJButton = new JButton(new AbstractAction("+") {

				@Override
				public void actionPerformed(ActionEvent e) {
					String selectedAtt = (String) getAddAttributeJComboBox()
							.getSelectedItem();
					if (selectedAtt != null) {
						chartGraphic.addAttribute(selectedAtt);

						repaintTable();
					}
				}

			});
		}
		return addAttributeJButton;
	}

	private void repaintTable() {
		// Repaint the Table
		((DefaultTableModel) getAttributesJTable().getModel())
				.fireTableDataChanged();
	}

	AttributesJComboBox addAttributeJComboBox;

	/**
	 * A {@link JComboBox} to select another numerical attribute for inclusino
	 * in the chartdata
	 */
	private AttributesJComboBox getAddAttributeJComboBox() {
		if (addAttributeJComboBox == null) {
			addAttributeJComboBox = new AttributesJComboBox(asv,
					numericalAttributeNames);
		}
		return addAttributeJComboBox;
	}

	JTable attributesJTable;

	private JTable getAttributesJTable() {
		if (attributesJTable == null) {
			attributesJTable = new JTable();
			TableModel dataModel = new DefaultTableModel() {

				@Override
				public String getColumnName(int column) {
					if (column == COLIDX_NAME)
						return "Attribute name"; // i8n
					if (column == COLIDX_COLOR)
						return "color"; // i8n
					if (column == COLIDX_DELETE)
						return "remove"; // i8n
					return super.getColumnName(column);
				}

				@Override
				public int getColumnCount() {
					return 3;
				}

				public Class<?> getColumnClass(int column) {
					return getValueAt(0, column).getClass();
				}

				@Override
				public boolean isCellEditable(int row, int column) {
					if (column == COLIDX_COLOR)
						return true;
					return false;
				}

				@Override
				public int getRowCount() {
					return chartGraphic.getAttributes().size();
				}

				@Override
				public void setValueAt(Object aValue, int row, int column) {
					if (column == COLIDX_COLOR) {
						chartGraphic.setColor(row, (Color) aValue);
					}
				}

				@Override
				public Object getValueAt(final int row, final int column) {
					if (column == COLIDX_NAME)
						return chartGraphic.getAttributes().get(row);

					if (column == COLIDX_COLOR)
						return chartGraphic.getColor(row);

					if (column == COLIDX_DELETE)
						return new JButton(new AbstractAction("X") {

							@Override
							public void actionPerformed(ActionEvent e) {
								chartGraphic.removeAttribute(row);
								repaintTable();
							}
						});

					return super.getValueAt(row, column);
				}

			};
			attributesJTable.setModel(dataModel);
			attributesJTable.setDefaultRenderer(Component.class,
					new TableCellRenderer() {

						@Override
						public Component getTableCellRendererComponent(
								JTable table, Object value, boolean isSelected,
								boolean hasFocus, int row, int column) {
							return (Component) value;
						}
					});

			attributesJTable.setDefaultRenderer(Color.class,
					new ColorTableCellRenderer());

			attributesJTable.addMouseListener(new JTableButtonMouseListener(
					attributesJTable));

			TableColumn col = attributesJTable.getColumnModel().getColumn(
					COLIDX_COLOR);
			col.setCellEditor(new ColorEditor());
		}
		return attributesJTable;
	}

	JComboBox chartTypCombobox;

	private JComboBox getChartTypCombobox() {
		if (chartTypCombobox == null) {
			chartTypCombobox = new JComboBox(ChartTyp.values());
			chartTypCombobox.setRenderer(new DefaultListCellRenderer() {

				@Override
				public Component getListCellRendererComponent(JList list,
						Object value, int index, boolean isSelected,
						boolean cellHasFocus) {
					if (value instanceof ChartTyp)
						value = ((ChartTyp) value).getTitle();
					return super.getListCellRendererComponent(list, value,
							index, isSelected, cellHasFocus);
				}
			});

			// Preselect the setting in the ChartGraphic
			chartTypCombobox.setSelectedItem(chartGraphic.getChartType());

			// Propagate the combo box selection to the ChartGraphic
			chartTypCombobox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					chartGraphic.setChartType((ChartTyp) e.getItem());
				}
			});
		}
		return chartTypCombobox;
	}

	@Override
	public void cancel() {
		// Reset any changes by promoting the backed-up symbol
		firePropertyChange(SVGSelector.PROPERTY_UPDATED, null, backup);
	}

	/**
	 * This method initializes a panel with OK and Close buttons
	 */
	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			jPanelButtons = new JPanel(new MigLayout());
			jPanelButtons.add(getOkButton(), "tag ok");
			jPanelButtons.add(getCancelButton(), "tag cancel");
		}
		return jPanelButtons;
	}

	class JTableButtonMouseListener extends java.awt.event.MouseAdapter {
		private JTable table;

		public JTableButtonMouseListener(JTable table) {
			this.table = table;
		}

		private void forwardEventToButton(MouseEvent e) {
			TableColumnModel columnModel = this.table.getColumnModel();
			int column = columnModel.getColumnIndexAtX(e.getX());
			int row = e.getY() / this.table.getRowHeight();
			Object value;

			MouseEvent tfcEvent;

			if (row >= this.table.getRowCount() || row < 0
					|| column >= this.table.getColumnCount() || column < 0) {
				return;
			}

			value = this.table.getValueAt(row, column);

			if (!(value instanceof JButton)) {
				return;
			} else {
				JButton tfc = (JButton) value;
				tfcEvent = (MouseEvent) SwingUtilities.convertMouseEvent(
						this.table, e, tfc);
				tfc.dispatchEvent(tfcEvent);
				if (e.getButton() == MouseEvent.BUTTON1) {
					// System.out.println("alles klar");
					tfc.getAction().actionPerformed(
							new ActionEvent(tfc, -1, ""));
					// this.table.repaint();
				}
			}
		}

		public void mouseClicked(MouseEvent e) {
			forwardEventToButton(e);
		}

	}
}
