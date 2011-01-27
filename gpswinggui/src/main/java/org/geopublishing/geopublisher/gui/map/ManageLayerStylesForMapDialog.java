/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.gui.map;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.dp.DataPool.EventTypes;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;
import org.geopublishing.atlasViewer.dp.layer.LayerStyle;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.swing.Icons;
import org.geopublishing.atlasViewer.swing.MapLegend;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.gui.datapool.EditDpEntryGUI;
import org.geopublishing.geopublisher.gui.datapool.ManageLayerStylesDialog;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.geotools.map.MapContext;
import org.geotools.styling.Style;

import de.schmitzm.i18n.Translation;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.CancelButton;
import de.schmitzm.swing.TranslationAskJDialog;
import de.schmitzm.swing.TranslationEditJPanel;

/**
 * This {@link JDialog} allows to manage the additional {@link Style}s available
 * for a {@link Map}.
 * 
 * @see ManageLayerStylesDialog
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 * 
 */
public class ManageLayerStylesForMapDialog extends ManageLayerStylesDialog {
	protected Logger LOGGER = LangUtil.createLogger(this);

	

	private JPanel jContentPane = null;

	private final Map map;

	private JPanel jPanel = null;

	private JButton jButtonUp = null;

	private JButton jButtonDown = null;

	/**
	 * Holds the list of styles that are available/activated for this map
	 */
	private ArrayList<String> listOfAvailAdditionalStyles;

	private final Style styleUsedRightNow;

	/**
	 * In case we cancel the dialog.
	 */
	private ArrayList<String> backup;

	/**
	 * In case we cancel the dialog.
	 */
	private String backupSelectedStyleID;

	private CancelButton jButtonCancel;

	private final MapLegend mapLegend;

	/**
	 * This {@link JDialog} allows to manage the additional {@link Style}s
	 * available for a {@link Map}.
	 * 
	 * @param owner
	 *            {@link Window} that the {@link JDialog} belongs to
	 * @param dplv
	 *            The {@link DpLayerVectorFeatureSource} we are managing
	 * @param map
	 *            {@link Map} that the settings are made for
	 * @param styleUsedRightNow
	 *            The style active for this {@link DpLayer} in the
	 *            {@link MapContext} at the moment. This is only used when a new
	 *            style is added. May be <code>null</code>.
	 */
	@SuppressWarnings("unchecked")
	public ManageLayerStylesForMapDialog(Component owner,
			DpLayerVectorFeatureSource dplv, AtlasConfigEditable ace, Map map,
			Style styleUsedRightNow, MapLegend mapLegend) {
		super(owner, dplv, ace);
		this.map = map;
		this.styleUsedRightNow = styleUsedRightNow;
		this.mapLegend = mapLegend;

		/**
		 * The 1-Map -> N-Layers -> M-available-additional-Styles
		 */
		listOfAvailAdditionalStyles = map.getAdditionalStyles().get(
				dplv.getId());
		if (listOfAvailAdditionalStyles == null) {
			listOfAvailAdditionalStyles = new ArrayList<String>();
			map.getAdditionalStyles().put(dplv.getId(),
					listOfAvailAdditionalStyles);
		}

		// This works:
		backup = (ArrayList<String>) listOfAvailAdditionalStyles.clone();
		backupSelectedStyleID = map.getSelectedStyleID(dplv.getId());

		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(440, 240);
		this.setContentPane(getJContentPane());

		String dialogTitle = GeopublisherGUI.R(
				"ManageLayerStylesForMapDialog.dialogTitle", dpLayer.getTitle()
						.toString(), map.getTitle().toString());
		this.setTitle(dialogTitle);

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				cancel();
			}
		});
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.fill = GridBagConstraints.BOTH;
			gridBagConstraints21.gridy = 1;
			gridBagConstraints21.weightx = 1.0;
			gridBagConstraints21.weighty = 1.0;
			gridBagConstraints21.gridx = 0;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.gridy = 0;
			jLabelExplanation = new JLabel();
			jLabelExplanation.setText(GeopublisherGUI
					.R("ManageLayerStylesForMapDialog.explanationLabel.text"));
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.gridy = 2;
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(getJPanel(), gridBagConstraints);
			jContentPane.add(jLabelExplanation, gridBagConstraints1);
			jContentPane.add(getJScrollPane(), gridBagConstraints21);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 4;
			gridBagConstraints7.gridy = 0;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 3;
			gridBagConstraints6.gridy = 0;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 2;
			gridBagConstraints5.gridy = 0;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 1;
			gridBagConstraints4.gridy = 0;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.gridy = 0;
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.add(getJButtonAdd(), gridBagConstraints3);
			jPanel.add(getJButtonUp(), gridBagConstraints4);
			jPanel.add(getJButtonDown(), gridBagConstraints5);
			jPanel.add(getJButtonOk(), gridBagConstraints6);
			jPanel.add(getJButtonCancel(), gridBagConstraints7);
		}
		return jPanel;
	}

	private JButton getJButtonCancel() {
		if (jButtonCancel == null) {
			jButtonCancel = new CancelButton();
			jButtonCancel.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					cancel();
				}

			});
		}
		return jButtonCancel;
	}

	protected void cancel() {
		map.getAdditionalStyles().put(dpLayer.getId(), backup);
		map.getSelectedStyleIDs().put(dpLayer.getId(), backupSelectedStyleID);
		mapLegend.recreateLayerList(dpLayer.getId());
		dispose();
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	@Override
	protected JButton getJButtonAdd() {
		if (jButtonAdd == null) {
			jButtonAdd = new JButton();
			jButtonAdd.setText("+");
			jButtonAdd.setToolTipText(GeopublisherGUI
					.R("ManageLayerStylesForMapDialog.buttonAddStyle.ToolTip"));

			jButtonAdd.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					LayerStyle style = createNewStyle(dpLayer,
							styleUsedRightNow,
							ManageLayerStylesForMapDialog.this);

					if (style != null) {
						listOfAvailAdditionalStyles.add(style.getFilename());
						getTable().setModel(getTableModel());
						getJScrollPane().setViewportView(getTable());
					}

				}

			});
		}
		return jButtonAdd;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonUp() {
		if (jButtonUp == null) {
			jButtonUp = new JButton();
			jButtonUp.setIcon(Icons.getUpArrowIcon());
			jButtonUp.setEnabled(false);

			jButtonUp.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					int styleIdx = getTable().getSelectedRow() - 1;
					if ((styleIdx >= 0)
							&& (styleIdx < listOfAvailAdditionalStyles.size())) {

						if ((styleIdx > 0)) {
							String element = listOfAvailAdditionalStyles
									.remove(styleIdx);
							listOfAvailAdditionalStyles.add(styleIdx - 1,
									element);
							getTable().getSelectionModel()
									.setSelectionInterval(styleIdx, styleIdx);

							// Update the GUI
							ace.getDataPool().fireChangeEvents(
									EventTypes.changeDpe);
						}

					}
				}

			});
		}
		return jButtonUp;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonDown() {
		if (jButtonDown == null) {
			jButtonDown = new JButton();
			jButtonDown.setIcon(Icons.getDownArrowIcon());
			jButtonDown.setEnabled(false);

			jButtonDown.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					int styleIdx = getTable().getSelectedRow() - 1;
					if ((styleIdx >= 0)
							&& (styleIdx < listOfAvailAdditionalStyles.size())) {

						if ((styleIdx + 1 < listOfAvailAdditionalStyles.size())) {
							String element = listOfAvailAdditionalStyles
									.remove(styleIdx);
							listOfAvailAdditionalStyles.add(styleIdx + 1,
									element);
							getTable().getSelectionModel()
									.setSelectionInterval(styleIdx + 3,
											styleIdx + 3); // The indices are

							ace.getDataPool().fireChangeEvents(
									EventTypes.changeDpe);
						}

					}
				}

			});

		}
		return jButtonDown;
	}

	/**
	 * This method initializes jTable
	 * 
	 * @return javax.swing.JTable
	 */
	@Override
	protected JTable getTable() {
		if (jTable == null) {
			jTable = super.getTable();
			jTable.setModel(getTableModel());
			jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			jTable.getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {

						public void valueChanged(ListSelectionEvent e) {
							int styleIdx = getTable().getSelectedRow() - 1;
							if ((styleIdx >= 0)
									&& (styleIdx < listOfAvailAdditionalStyles
											.size())) {

								getJButtonUp().setEnabled((styleIdx > 0));
								getJButtonDown()
										.setEnabled(
												(styleIdx + 1 < listOfAvailAdditionalStyles
														.size()));

							} else {
								getJButtonUp().setEnabled(false);
								getJButtonDown().setEnabled(false);
							}
						}

					});
		}
		return jTable;
	}

	@Override
	protected TableModel getTableModel() {
		TableModel model = new DefaultTableModel() {

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if (columnIndex == 2)
					return Boolean.class;
				return String.class;
			}

			@Override
			public int getColumnCount() {
				return 3;
			}

			@Override
			public String getColumnName(int columnIndex) {
				if (columnIndex == 0)
					return GeopublisherGUI
							.R("ManageLayerStylesDialog.Columns.Title.Label");
				if (columnIndex == 1)
					return GeopublisherGUI
							.R("ManageLayerStylesDialog.Columns.Desc.Label");
				if (columnIndex == 2)
					return GeopublisherGUI
							.R("ManageLayerStylesDialog.Columns.AvailableInThisMap.Label");
				return null;
			}

			@Override
			public int getRowCount() {
				return dpLayer.getLayerStyles().size() + 1;
			}

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {

				/** The first row is handled first * */
				if (rowIndex == 0) {
					if (columnIndex == 0) {
						// Returns the title of the DpLayer
						return dpLayer.getTitle();
					}
					if (columnIndex == 1)
						// Returns the desc of the DpLayer
						return dpLayer.getDesc();
					if (columnIndex == 2) {

						boolean useDefaultStyle = false;

						if ((dpLayer.getLayerStyles().size() == 0)
								|| (listOfAvailAdditionalStyles.size() == 0)) {
							useDefaultStyle = true;
						}

						return useDefaultStyle;
					}
					return null;
				} else
					rowIndex--;

				/**
				 * We first list the active ones which are listed in
				 * listOfAdditionalStyles *
				 */
				if (rowIndex < listOfAvailAdditionalStyles.size()) {
					String styleID = listOfAvailAdditionalStyles.get(rowIndex);
					LayerStyle ls = dpLayer.getLayerStyleByID(styleID);

					if (columnIndex == 0)
						return ls.getTitle().toString();
					if (columnIndex == 1)
						return ls.getDesc().toString();
					if (columnIndex == 2)
						return true;
				} else

				/** The other styles that are not available for this map * */
				{
					int unusedIdx = rowIndex
							- listOfAvailAdditionalStyles.size();
					int idx = -1;
					List<LayerStyle> layerStyles = dpLayer.getLayerStyles();

					for (LayerStyle ls : layerStyles) {
						if (!listOfAvailAdditionalStyles.contains(ls
								.getFilename()))
							idx++;
						if (idx == unusedIdx) {
							// We have found the n-th element that is not used
							// in the listOfAdditionalStyles

							if (columnIndex == 0)
								return ls.getTitle().toString();
							if (columnIndex == 1)
								return ls.getDesc().toString();
							if (columnIndex == 2)
								return false;
						}
					}

				}
				return null;
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {

				/**
				 * The third colum has a clickable checkbox
				 * (available/visibility of the style in the map) and therefor
				 * is editable.
				 */
				if (columnIndex == 2) {
					if (rowIndex == 0) {
						// The default jCheckBox can only be switched on in
						// special cases.
						if ((dpLayer.getLayerStyles().size() > 0)
								&& (listOfAvailAdditionalStyles.size() > 0)) {
							return true;
						} else
							return false;
					}
					return true;
				}

				return false;
			}

			@Override
			public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

				try {

					/**
					 * Only the third column is editable
					 */
					if (columnIndex != 2)
						return;

					Boolean onOff = (Boolean) aValue;

					/***********************************************************
					 * Enabling / Disabling the default style
					 */
					if (rowIndex == 0) {
						if (onOff == true) {
							listOfAvailAdditionalStyles.clear();

							JOptionPane
									.showMessageDialog(
											ManageLayerStylesForMapDialog.this,
											"By enabling the default style, all\n"
													+ "other styles are set to hidden."); // i8nAC
						} else {
							throw new RuntimeException(
									"We can't switch the default style off manually!"); // i8nlog
						}
						preview();
						return;
					}

					// Count the default layer
					rowIndex--;

					/***********************************************************
					 * Deactivating a style
					 */
					if (onOff == false) {
						listOfAvailAdditionalStyles.remove(rowIndex);
						return;
					} else

					if (onOff == true) {
						/** We just switched one on.. */
						{
							// index in the virtual list of unused styles
							int unusedIdx = rowIndex
									- listOfAvailAdditionalStyles.size();

							int idx = -1;
							List<LayerStyle> layerStyles = dpLayer
									.getLayerStyles();

							for (LayerStyle ls : layerStyles) {
								if (!listOfAvailAdditionalStyles.contains(ls
										.getFilename()))
									idx++;
								if (idx == unusedIdx) {
									// We have found the n-th element that is
									// not used in the listOfAdditionalStyles
									// this is the style that we want to enable

									listOfAvailAdditionalStyles.add(ls
											.getFilename());

									return;
								}
							}

						}
					}

				} finally {
					// Update the table to reflect the changed .. this also
					// reorders the rows...
					int backup = getTable().getSelectedRow();
					getTable().setModel(getTableModel());
					getJScrollPane().setViewportView(getTable());
					getTable().getSelectionModel().addSelectionInterval(backup,
							backup);

					preview();
				}
			}

		};

		return model;
	}

	protected void preview() {
		// final Style style ;
		// if (id == null) {
		// style = dpLayer.getStyle();
		// }else {
		// final LayerStyle layerStyleByID = dpLayer.getLayerStyleByID(id);
		// style = layerStyleByID.getStyle();
		// }
		// mapLegend.getMapLayerFor(dpLayer.getId()).setStyle(
		// style);
		// mapLegend.getGeoMapPane().refreshMap();

		mapLegend.recreateLayerList(dpLayer.getId());

	}

	/**
	 * @return A listener that opens a translation dialog when a doubleclicked.
	 */
	@Override
	protected MouseListener getTableMouseListenerForTranslation() {

		return new MouseAdapter() {

			private TranslationAskJDialog ask;

			@Override
			public void mouseClicked(MouseEvent e) {
				final int col = jTable.columnAtPoint(e.getPoint());

				// Only act on the first two columns
				if (col > 1)
					return;

				final int row = jTable.rowAtPoint(e.getPoint());

				if (e.getClickCount() == 1) {
					/**
					 * SINGLE-CLICK
					 */

					/**
					 * Update the Map'Style by recreating the MapLegend
					 */
					if (row == 0) {
						// map.setSelectedStyleID(dpLayer.getId(), null);
						// preview(null);
					} else if (row - 1 < listOfAvailAdditionalStyles.size()) {
						final String styleID = listOfAvailAdditionalStyles
								.get(row - 1);
						map.setSelectedStyleID(dpLayer.getId(), styleID);
						preview();
					}
					// mapLegend.updateLegendIcon(styledObj)

				} else if (e.getClickCount() >= 2) {
					/**
					 * DOUBLE-CLICK
					 */

					if (row == 0) {
						EditDpEntryGUI editDpEntryGUI = new EditDpEntryGUI(
								ManageLayerStylesForMapDialog.this, dpLayer);
						editDpEntryGUI.setVisible(true);

						if (!editDpEntryGUI.isCancelled()) {
							// Update the GUIs
							if (!editDpEntryGUI.isCancelled()) {
								ace.getDataPool().fireChangeEvents(
										EventTypes.changeDpe);
							}
						}
						return;
					}

					final int rowIndex = row - 1;

					LayerStyle layerStyle = null;
					if (rowIndex < listOfAvailAdditionalStyles.size()) {
						/**
						 * We first list the active ones which are listed in
						 * listOfAdditionalStyles *
						 */
						String styleID = listOfAvailAdditionalStyles
								.get(rowIndex);
						layerStyle = dpLayer.getLayerStyleByID(styleID);
					} else {
						/**
						 * The other styles that are not available for this map
						 */
						int unusedIdx = rowIndex
								- listOfAvailAdditionalStyles.size();
						int idx = -1;
						List<LayerStyle> layerStyles = dpLayer.getLayerStyles();

						for (LayerStyle ls : layerStyles) {
							if (!listOfAvailAdditionalStyles.contains(ls
									.getFilename()))
								idx++;
							if (idx == unusedIdx) {
								// We have found the n-th element that is not
								// used in the listOfAdditionalStyles
								layerStyle = ls;
								break;
							}
						}
					}

					if (layerStyle == null)
						throw new IllegalArgumentException(
								"No corresponding additional style found for the row that was clicked.");

					final Translation styleName = layerStyle.getTitle();
					final Translation styleNameBackup = styleName.copy();

					final Translation styleDesc = layerStyle.getDesc();
					final Translation styleDescBackup = styleDesc.copy();

					TranslationEditJPanel transNameLabel = new TranslationEditJPanel(
							GeopublisherGUI.R("LayerStyle.Edit.Title"), styleName,
							dpLayer.getAtlasConfig().getLanguages());

					TranslationEditJPanel transDescLabel = new TranslationEditJPanel(
							GeopublisherGUI.R("LayerStyle.Edit.Desc"), styleDesc,
							dpLayer.getAtlasConfig().getLanguages());

					ask = new TranslationAskJDialog(
							ManageLayerStylesForMapDialog.this, transNameLabel,
							transDescLabel);

					final LayerStyle finalLayerStyle = layerStyle; // Just for
					// stupid
					// bug
					// checks
					ask.addPropertyChangeListener(new PropertyChangeListener() {

						public void propertyChange(PropertyChangeEvent evt) {
							if (evt
									.getPropertyName()
									.equals(
											TranslationAskJDialog.PROPERTY_CANCEL_AND_CLOSE)) {

								finalLayerStyle.setTitle(styleNameBackup);
								finalLayerStyle.setDesc(styleDescBackup);
							}
							if (evt
									.getPropertyName()
									.equals(
											TranslationAskJDialog.PROPERTY_APPLY_AND_CLOSE)) {
							}
							// getTable().setModel(getTableModel()); //TODO
							// remove line if all good withour

						}

					});

					ask.setVisible(true);

					// Update the GUIs
					if (!ask.isCancelled()) {
						ace.getDataPool()
								.fireChangeEvents(EventTypes.changeDpe);
					}
				}
			}
		};
	}

	/**
	 * Is overwritten in *ForLayer to take care of the selectedStyle (which is
	 * saved in the map)
	 */
	@Override
	protected void performOKButton() {

		// Wenn vorher kein selectedStyle gesetzt war, dann jetzt einen setzen.
		if (backup.size() == 0 && listOfAvailAdditionalStyles.size() > 0) {
			map.setSelectedStyleID(dpLayer.getId(), listOfAvailAdditionalStyles
					.get(0));
		} else
		// If we don't have an add style available, select null!
		if (listOfAvailAdditionalStyles.size() == 0) {
			map.setSelectedStyleID(dpLayer.getId(), null);
		}

		preview();

		super.performOKButton();
	}

}
