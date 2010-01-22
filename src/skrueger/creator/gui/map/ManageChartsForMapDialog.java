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
package skrueger.creator.gui.map;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;
import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.jfree.feature.style.FeatureChartStyle;
import schmitzm.swing.SwingUtil;
import skrueger.atlas.AVUtil;
import skrueger.atlas.dp.DataPool;
import skrueger.atlas.dp.layer.DpLayerVectorFeatureSource;
import skrueger.atlas.gui.map.AtlasMapLegend;
import skrueger.atlas.map.Map;
import skrueger.atlas.resource.icons.Icons;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.AtlasCreator;
import skrueger.creator.GPDialogManager;
import skrueger.creator.chartwizard.ChartWizard;
import skrueger.creator.gui.DesignAtlasChartJDialog;
import skrueger.swing.CancelButton;
import skrueger.swing.CancellableDialogAdapter;
import skrueger.swing.OkButton;

/**
 * This dialog allows to manage which charts are available in a map. It's not
 * modal and has an internal cache. Use
 * {@link #getInstanceFor(Component, DpLayerVectorFeatureSource, AtlasConfigEditable, Map, AtlasMapLegend)}
 * to create instances.
 * 
 * @author Stefan A. Krueger
 * 
 */
public class ManageChartsForMapDialog extends CancellableDialogAdapter {

	private static final Dimension PREFERRED_SIZE = new Dimension(500, 270);
	final JLabel explanationJLabel = new JLabel(AtlasCreator
			.R("ManageChartsForMapDialog.Explanation"));
	JTable chartsJTable;
	JButton upJButton, downJButton, delButton, addButton, editButton;
	private final DpLayerVectorFeatureSource dplv;
	private final Map map;
	private final AtlasMapLegend mapLegend;
	private ArrayList<String> visibleChartIDs;
	private List<FeatureChartStyle> allChartIDs;
	private final ArrayList<String> backup_AvailableChartIDsFor = new ArrayList<String>();
	private final ArrayList<FeatureChartStyle> backup_Charts = new ArrayList<FeatureChartStyle>();

	private AtlasConfigEditable atlasConfigEditable;

	/**
	 * Remembers all newly created ChartStyles until the windows is closed. Then
	 * they will be added to the other maps
	 **/
	protected HashSet<ChartStyle> newlyCreatedCharts = new HashSet<ChartStyle>();

	/**
	 * The main constructor to create the GUI.
	 * 
	 * @param owner
	 *            The GUI this Dialog is related to.
	 * @param dplv
	 *            The {@link DpLayerVectorFeatureSource} that owns the
	 *            {@link ChartStyle}s that are beeing managed.
	 * @param ace
	 *            {@link AtlasConfigEditable} that contains the {@link DataPool}
	 *            etc.
	 * @param map
	 *            The {@link Map} that the order and visibility is managed for
	 * @param mapLegend
	 *            A reference to the {@link AtlasMapLegend}
	 */
	public ManageChartsForMapDialog(Component owner,
			DpLayerVectorFeatureSource dplv, AtlasMapLegend mapLegend) {

		super(owner, AtlasCreator.R(
				"ManageChartsForMapDialog.TitleForDPLayer", dplv.getTitle()
						.toString()));
		this.dplv = dplv;
		this.atlasConfigEditable = (AtlasConfigEditable) dplv.getAc();
		this.map = mapLegend.getMap();
		this.mapLegend = mapLegend;

		this.visibleChartIDs = map.getAvailableChartIDsFor(dplv.getId());
		this.allChartIDs = dplv.getCharts();

		/*
		 * Make backups of existing settings
		 */
		backup_AvailableChartIDsFor.addAll(map.getAvailableChartIDsFor(dplv
				.getId()));
		backup_Charts.addAll(dplv.getCharts());

		initGUI();

		pack();
		
		SwingUtil.setRelativeFramePosition(this, owner, SwingUtil.BOUNDS_OUTER, SwingUtil.NORTHWEST);

		// Directly create a new chart if there exist no charts yet.
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				if (backup_Charts.size() == 0)
					getAddButton().getAction().actionPerformed(null);
			}

		});
	}

	/**
	 * Abort any changes and close the {@link JDialog}
	 */
	public void cancel() {

		/*
		 * Put the values of the backups into the real list objects
		 */
		final ArrayList<String> availCharts = map.getAvailableCharts().get(
				dplv.getId());
		synchronized (availCharts) {
			availCharts.clear();
			availCharts.addAll(backup_AvailableChartIDsFor);
		}

		final List<FeatureChartStyle> charts = dplv.getCharts();
		synchronized (charts) {
			charts.clear();
			charts.addAll(backup_Charts);
		}

		mapLegend.showOrHideChartButton();
	}

	private void initGUI() {
		JPanel contentPane = new JPanel(new MigLayout("wrap 1, fillx")); //$NON-NLS-1$

		contentPane.add(explanationJLabel);
		contentPane.add(new JScrollPane(getChartsJTable()), "grow"); //$NON-NLS-1$
		contentPane.add(getAddButton(), "split 7, left"); //$NON-NLS-1$
		contentPane.add(getDelButton(), "left"); //$NON-NLS-1$
		contentPane.add(getEditButton(), "left, gap"); //$NON-NLS-1$
		contentPane.add(getUpJButton(), "left, sgx 1"); //$NON-NLS-1$
		contentPane.add(getDownJButton(), "left, sgx 1"); //$NON-NLS-1$
		contentPane.add(getOkButton(), "left, tag ok"); //$NON-NLS-1$
		contentPane.add(getCancelButton(), "left, tag cancel"); //$NON-NLS-1$

		contentPane.setPreferredSize(PREFERRED_SIZE);

		setContentPane(contentPane);
	}

	private JButton getCancelButton() {
		CancelButton cancelButton = new CancelButton();
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				cancelClose();
			}

		});
		return cancelButton;
	}

	private JButton getOkButton() {
		OkButton okButton = new OkButton();
		okButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				okClose();
			}

		});
		return okButton;
	}

	@Override
	public boolean okClose() {

		Set<Map> mapsUsingTheDpe = atlasConfigEditable.getMapPool()
				.getMapsUsing(dplv);
		mapsUsingTheDpe.remove(map);

		if (newlyCreatedCharts.size() > 0
				&& mapsUsingTheDpe.size() > 0
				&& AVUtil
						.askYesNo(
								ManageChartsForMapDialog.this, // i8n
								"Sollen die "
										+ newlyCreatedCharts.size()
										+ " neu erstellten Charts in den anderen "
										+ mapsUsingTheDpe.size()
										+ " Karten verfügbar sein, in denen das Layer verwendet wird?")) { // i8n
			for (ChartStyle cs : newlyCreatedCharts) {
				for (Map m : mapsUsingTheDpe) {
					ArrayList<String> avilChartForDpe = m
							.getAvailableChartIDsFor(dplv.getId());
					avilChartForDpe.add(cs.getID());
				}

			}
		}

		return super.okClose();
	}

	public JLabel getExplanationJLabel() {
		return explanationJLabel;
	}

	public JTable getChartsJTable() {
		if (chartsJTable == null) {
			chartsJTable = new JTable();

			chartsJTable.setModel(createAvailableChartsTableModel(dplv
					.getCharts(), map.getAvailableChartIDsFor(dplv.getId())));

			chartsJTable.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent evt) {
					if (SwingUtilities.isLeftMouseButton(evt)
							&& evt.getClickCount() == 2) {
						int row = getChartsJTable().rowAtPoint(evt.getPoint());

						final FeatureChartStyle chartStyle = getChartForRow(row);
						//
						// dplv.openChart(mapLegend != null ? mapLegend
						// : ManageChartsForMapDialog.this,
						// chartStyle.getID(), mapLegend);

						openDesignChartDialog(chartStyle);

					}
				}

			});
		}

		return chartsJTable;
	}

	/**
	 * Opens a {@link DesignAtlasChartJDialog} for the given {@link ChartStyle}.
	 * While the dialog is opened, a wait cursor is shown.
	 */
	public void openDesignChartDialog(final FeatureChartStyle chartStyle) {
		ManageChartsForMapDialog.this.setCursor(Cursor
				.getPredefinedCursor(Cursor.WAIT_CURSOR));

		GPDialogManager.dm_DesignCharts.getInstanceFor(chartStyle,
				ManageChartsForMapDialog.this, chartStyle, mapLegend, dplv,
				atlasConfigEditable);

		ManageChartsForMapDialog.this.setCursor(null);
	}

	/**
	 * Creates a {@link DefaultTableModel} that presents the
	 * 
	 * @param charts
	 * @param availableChartsFor
	 * @return
	 */
	private DefaultTableModel createAvailableChartsTableModel(
			List<FeatureChartStyle> charts, ArrayList<String> availableChartsFor) {

		DefaultTableModel model = new DefaultTableModel() {

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				switch (columnIndex) {
				case 0:
					return true;
				}
				return false;
			}

			@Override
			public int getColumnCount() {
				return 5;
			}

			@Override
			public String getColumnName(int column) {
				return AtlasCreator.R("ManageChartsForMapDialog.ColumnName." //$NON-NLS-1$
						+ (column + 1));
			}

			@Override
			public int getRowCount() {
				return dplv.getCharts().size();
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				switch (columnIndex) {
				case 0:
					return Boolean.class;
				case 3:
					return Icon.class;
				default:
					return super.getColumnClass(columnIndex);
				}
			}

			@Override
			/*
			 * This method is the only tricky one. The table will first list the
			 * ChartStyles visible for this map - in the same order that they
			 * will also be presented in the map. Then the invisible ChartStyles
			 * are listed (in any order).
			 */
			public Object getValueAt(int row, int column) {

				// Remember: The counts are 1-based, row is 0-based
				final int countVisibleCharts = visibleChartIDs.size();
				//
				// // Remember: The counts are 1-based, row is 0-based
				// final int countTotalCharts = dplv.getCharts().size();

				/*
				 * Depending on the Column, we now return the Value
				 */
				FeatureChartStyle rowChartStyle = getChartForRow(row);

				switch (column) {
				case 0:
					return (row < countVisibleCharts);
				case 1:
					return rowChartStyle.getTitleStyle().getLabel();
				case 2:
					return rowChartStyle.getDescStyle().getLabel();
				case 3:
					return rowChartStyle.getType().getIcon();
				case 4:
					return rowChartStyle.getAttributeCount();
				}

				return super.getValueAt(row, column);
			}

			@Override
			public void setValueAt(Object value, int row, int column) {

				switch (column) {
				case 0:
					String chartId = getChartForRow(row).getID();
					boolean isVisible = (Boolean) value;
					if (isVisible) {
						visibleChartIDs.add(chartId);
					} else {
						visibleChartIDs.remove(chartId);
					}
					// TEST: The model logic should know, that a setValueAt
					// needs a repaint! But it can't know, that the whole order
					// might have changed.
					((DefaultTableModel) getChartsJTable().getModel())
							.fireTableDataChanged();

					mapLegend.showOrHideChartButton();
					return;
				default:
					super.setValueAt(value, row, column);
				}
			}

		};

		return model;
	}

	/**
	 * Maps between a row index the table, to the {@link FeatureChartStyle} that
	 * is represented by it.
	 * 
	 * @param row
	 *            0-based table / tablemodel row index.
	 */
	private FeatureChartStyle getChartForRow(int row) {
		final int countVisibleCharts = visibleChartIDs.size();

		if (row < countVisibleCharts) {
			return dplv.getChartForID(visibleChartIDs.get(row));
		}
		/*
		 * Determine which of the total ChartStyle-IDs are not visible in this
		 * map:
		 */
		int countMisses = 0;

		for (FeatureChartStyle dplChart : allChartIDs) {
			if (visibleChartIDs.contains(dplChart.getID()))
				continue;
			if (countMisses == row - countVisibleCharts) {
				return dplChart;
			}
			countMisses++;
		}

		throw new RuntimeException("Can't determine the ChartStyle for row=" //$NON-NLS-1$
				+ row);
	}

	public JButton getUpJButton() {
		if (upJButton == null) {
			upJButton = new JButton(new AbstractAction() {

				/**
				 * Move the selected visible style one up in the order.
				 */
				@Override
				public void actionPerformed(ActionEvent e) {
					final int selectedRow = getChartsJTable().getSelectedRow();
					FeatureChartStyle chartForRow = getChartForRow(selectedRow);
					String id = chartForRow.getID();

					visibleChartIDs.remove(selectedRow);
					visibleChartIDs.add(selectedRow - 1, id);

					((DefaultTableModel) getChartsJTable().getModel())
							.fireTableDataChanged();

					getChartsJTable().getSelectionModel().addSelectionInterval(
							selectedRow - 1, selectedRow - 1);
				}

			});

			/*
			 * The button is disabled, if an invisible row or the topmost row is
			 * selected.
			 */
			getChartsJTable().getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {

						@Override
						public void valueChanged(ListSelectionEvent e) {
							if (e.getValueIsAdjusting())
								return;

							int selectedRow = getChartsJTable()
									.getSelectedRow();

							boolean enabled = (selectedRow >= 1)
									&& (selectedRow < visibleChartIDs.size());
							upJButton.setEnabled(enabled);
						}

					});

			upJButton.setIcon(Icons.getUpArrowIcon());
			upJButton.setEnabled(false);
		}

		return upJButton;
	}

	public JButton getDownJButton() {
		if (downJButton == null) {
			downJButton = new JButton(new AbstractAction() {

				/**
				 * Move the selected visible style one down in the order.
				 */
				@Override
				public void actionPerformed(ActionEvent e) {
					final int selectedRow = getChartsJTable().getSelectedRow();
					FeatureChartStyle chartForRow = getChartForRow(selectedRow);
					String id = chartForRow.getID();

					visibleChartIDs.remove(selectedRow);
					visibleChartIDs.add(selectedRow + 1, id);

					((DefaultTableModel) getChartsJTable().getModel())
							.fireTableDataChanged();

					getChartsJTable().getSelectionModel().addSelectionInterval(
							selectedRow + 1, selectedRow + 1);
				}

			});

			/*
			 * The button is disabled, if an invisible row or the last visible
			 * row is selected.
			 */
			getChartsJTable().getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {

						@Override
						public void valueChanged(ListSelectionEvent e) {
							if (e.getValueIsAdjusting())
								return;

							int selectedRow = getChartsJTable()
									.getSelectedRow();

							boolean enabled = (selectedRow >= 0)
									&& (selectedRow < visibleChartIDs.size() - 1);
							downJButton.setEnabled(enabled);
						}

					});

			downJButton.setIcon(Icons.getDownArrowIcon());
			downJButton.setEnabled(false);
		}

		return downJButton;
	}

	public JButton getDelButton() {
		if (delButton == null) {
			delButton = new JButton(new AbstractAction(AtlasCreator
					.R("ManageChartsForMapDialog.DeleteChart")) { //$NON-NLS-1$

						/**
						 * Remove the ChartStyle from the Map and the dpLayer
						 */
						@Override
						public void actionPerformed(ActionEvent e) {
							final int selectedRow = getChartsJTable()
									.getSelectedRow();
							FeatureChartStyle chart = getChartForRow(selectedRow);

							visibleChartIDs.remove(chart.getID());
							dplv.getCharts().remove(chart);

							/* Update the Chart button in the AtlasMapView */
							mapLegend.showOrHideChartButton();

							/**
							 * When the atlas is saved, all chart files are
							 * deleted and only the existing chart files are
							 * save. So we don't have to delete the chart file
							 * now.
							 */

							newlyCreatedCharts.remove(chart);

							chart = null;
							((DefaultTableModel) getChartsJTable().getModel())
									.fireTableRowsDeleted(selectedRow,
											selectedRow);
						}

					});

			/*
			 * The button is disabled, if any row is selected
			 */
			getChartsJTable().getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {

						@Override
						public void valueChanged(ListSelectionEvent e) {
							if (e.getValueIsAdjusting())
								return;

							boolean enabled = (getChartsJTable()
									.getSelectedRow() >= 0);
							delButton.setEnabled(enabled);
						}

					});
			delButton.setEnabled(false);
		}

		return delButton;
	}

	public JButton getEditButton() {
		if (editButton == null) {
			editButton = new JButton(new AbstractAction(AtlasCreator
					.R("ManageChartsForMapDialog.EditChart")) { //$NON-NLS-1$

						/**
						 * Edit the ChartStyle, starting with the wizard
						 */
						@Override
						public void actionPerformed(ActionEvent e) {
							final int selectedRow = getChartsJTable()
									.getSelectedRow();

							FeatureChartStyle chart = getChartForRow(selectedRow);
							openDesignChartDialog(chart);
						}

					});

			/*
			 * The button is disabled, if any row is selected
			 */
			getChartsJTable().getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {

						@Override
						public void valueChanged(ListSelectionEvent e) {
							if (e.getValueIsAdjusting())
								return;

							boolean enabled = (getChartsJTable()
									.getSelectedRow() >= 0);
							editButton.setEnabled(enabled);
						}

					});
			editButton.setEnabled(false);
		}

		return editButton;
	}

	public JButton getAddButton() {
		if (addButton == null) {
			addButton = new JButton(new AbstractAction(AtlasCreator
					.R("ManageChartsForMapDialog.AddChart")) { //$NON-NLS-1$

						@Override
						public void actionPerformed(ActionEvent e) {
							FeatureChartStyle newChart = ChartWizard
									.showWizard(dplv.getFeatureSource(), dplv
											.getAttributeMetaDataMap(),
											atlasConfigEditable.getLanguages());
							if (newChart == null)
								return;

							/**
							 * Adding the chart to the DpLayerVector's list of
							 * ChartStyles and make it visible in this map
							 * automatically.
							 */
							dplv.getCharts().add(newChart);
							visibleChartIDs.add(newChart.getID());

							/* Update the Chart button in the AtlasMapView */
							mapLegend.showOrHideChartButton();

							// Update the charts table
							((DefaultTableModel) getChartsJTable().getModel())
									.fireTableStructureChanged();

							newlyCreatedCharts.add(newChart);

							// Automatically open the EditChartDialog
							openDesignChartDialog(newChart);
						}

					});
			addButton.setToolTipText(AtlasCreator
					.R("LayerToolMenu.chartWizard"));
			// resources
		}
		return addButton;
	}


}
