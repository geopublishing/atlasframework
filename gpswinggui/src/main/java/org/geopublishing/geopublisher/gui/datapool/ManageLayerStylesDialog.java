/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.gui.datapool;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasViewer.dp.DataPool.EventTypes;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;
import org.geopublishing.atlasViewer.dp.layer.LayerStyle;
import org.geopublishing.atlasViewer.exceptions.AtlasException;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.gui.internal.GPDialogManager;
import org.geopublishing.geopublisher.gui.map.ManageLayerStylesForMapDialog;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.geotools.styling.Style;

import schmitzm.geotools.styling.StylingUtil;
import schmitzm.io.IOUtil;
import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.swing.ExceptionDialog;
import schmitzm.swing.SwingUtil;
import skrueger.i8n.Translation;
import skrueger.swing.OkButton;
import skrueger.swing.TranslationAskJDialog;
import skrueger.swing.TranslationEditJPanel;

/**
 * This {@link JDialog} allows to manage additional styles for a {@link DpLayer}
 * . The difference to {@link ManageLayerStylesForMapDialog} is that, this
 * dialog given an overview of all additional styles and is not bound to one
 * {@link Map}.
 * 
 * @see ManageLayerStylesForMapDialog
 * 
 * @author Stefan A. Krueger
 * 
 */
public class ManageLayerStylesDialog extends JDialog {
	protected static Logger LOGGER = ASUtil
			.createLogger(ManageLayerStylesDialog.class);

	PropertyChangeListener dpListenerToUpdateTableModel;

	private static TranslationAskJDialog ask;

	

	private JPanel jContentPane = null;

	private JPanel jPanelTop = null;

	protected JTable jTable = null;

	private JPanel jPanel = null;

	protected JButton jButtonAdd = null;

	private JButton jButtonRemove = null;

	private JButton jButtonDefault = null;

	private JButton jButtonOk = null;

	protected JLabel jLabelExplanation = null;

	/**
	 * The {@link DpLayer} that we manage the {@link LayerStyle}s for.
	 */
	protected final DpLayerVectorFeatureSource dpLayer;

	private JScrollPane jScrollPane;

	protected final AtlasConfigEditable ace;

	/**
	 * @param dpLayer
	 *            The {@link DpLayerVectorFeatureSource} that we manage the
	 *            {@link LayerStyle}s for.
	 */
	public ManageLayerStylesDialog(Component owner,
			DpLayerVectorFeatureSource dpLayer, AtlasConfigEditable ace) {
		super(SwingUtil.getParentWindow(owner));
		this.dpLayer = dpLayer;
		this.ace = ace;
		initialize();
	}

	/**
	 * Since the registerKeyboardAction() method is part of the JComponent class
	 * definition, you must define the Escape keystroke and register the
	 * keyboard action with a JComponent, not with a JDialog. The JRootPane for
	 * the JDialog serves as an excellent choice to associate the registration,
	 * as this will always be visible. If you override the protected
	 * createRootPane() method of JDialog, you can return your custom JRootPane
	 * with the keystroke enabled:
	 */
	@Override
	protected JRootPane createRootPane() {
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		JRootPane rootPane = new JRootPane();
		rootPane.registerKeyboardAction(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				dispose();
			}

		}, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

		return rootPane;
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(800, 210);
		this.setContentPane(getJContentPane());
		setModal(true);
		SwingUtil.centerFrameOnScreenRandom(ManageLayerStylesDialog.this);
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.fill = GridBagConstraints.BOTH;
			gridBagConstraints3.gridy = 1;
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.weighty = 1.0;
			gridBagConstraints3.gridx = 0;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.gridy = 2;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 0;
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(getJPanelTop(), gridBagConstraints);
			jContentPane.add(getJPanel(), gridBagConstraints2);
			jContentPane.add(getJScrollPane(), gridBagConstraints3);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
	protected JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getTable());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelTop() {
		if (jPanelTop == null) {
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 0;
			gridBagConstraints9.gridy = 0;
			jLabelExplanation = new JLabel();
			jLabelExplanation
					.setText("Manage the available styles for this layer."); // i8n
			jPanelTop = new JPanel();
			jPanelTop.setLayout(new GridBagLayout());
			jPanelTop.add(jLabelExplanation, gridBagConstraints9);
		}
		return jPanelTop;
	}

	/**
	 * This method initializes jTable
	 * 
	 * @return javax.swing.JTable
	 */
	protected JTable getTable() {
		if (jTable == null) {
			jTable = new JTable();

			jTable.setDefaultRenderer(Object.class,
					new DefaultTableCellRenderer() {

						/**
						 * A renderer to mark the default style in gray
						 */
						@Override
						public Component getTableCellRendererComponent(
								JTable table, Object value, boolean isSelected,
								boolean hasFocus, int row, int column) {
							final JComponent fromSuper = (JComponent) super
									.getTableCellRendererComponent(table,
											value, isSelected, hasFocus, row,
											column);

							// TODO doesn't work for the third column. Why?

							final Color c = isSelected ? table
									.getSelectionBackground() : table
									.getBackground();
							if (row == 0) {
								fromSuper.setBackground(c.darker());
							} else {
								fromSuper.setBackground(c);
							}

							return fromSuper;
						}

					});

			// Create the model the first time
			jTable.setModel(getTableModel());

			jTable.setToolTipText(AtlasStyler
					.R("ManageLayerStylesDialog.table.TT"));

			/*******************************************************************
			 * Listening to clicks on the JTable. Allows to translate the
			 * style's meta information
			 */
			jTable.addMouseListener(getTableMouseListenerForTranslation());

			jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		}
		return jTable;
	}

	private PropertyChangeListener getDpListener() {
		/**
		 * Adds a listener to update the TableModel whenever the DataPool has
		 * changed. Backups and restores the selected row.
		 */
		if (dpListenerToUpdateTableModel == null) {

			dpListenerToUpdateTableModel = new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					int backup = getTable().getSelectedColumn();

					AbstractTableModel m = (AbstractTableModel) getTable()
							.getModel();
					m.fireTableDataChanged();

					if (backup < m.getRowCount())
						getTable().getSelectionModel().addSelectionInterval(
								backup, backup);
				}

			};
		}

		return dpListenerToUpdateTableModel;
	}

	@Override
	public void setVisible(boolean b) {
		if (b)
			ace.getDataPool().addChangeListener(getDpListener());
		else
			ace.getDataPool().removeChangeListener(getDpListener());
		super.setVisible(b);
	};

	/**
	 * @return A listener that opens a translation dialog when a doubleclicked.
	 */
	protected MouseListener getTableMouseListenerForTranslation() {

		return new MouseAdapter() {

			private TranslationAskJDialog ask;

			@Override
			public void mouseClicked(MouseEvent e) {
				final int col = jTable.columnAtPoint(e.getPoint());

				// Only act on the first two columns
				if (col > 1)
					return;

				if (e.getClickCount() == 2) {
					final int row = jTable.rowAtPoint(e.getPoint());

					if (row == 0) {
						EditDpEntryGUI editDpEntryGUI = new EditDpEntryGUI(
								ManageLayerStylesDialog.this, dpLayer);
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

					final int idx = row - 1;

					final Translation styleName = dpLayer.getLayerStyles().get(
							idx).getTitle();
					final Translation styleNameBackup = styleName.clone();

					final Translation styleDesc = dpLayer.getLayerStyles().get(
							idx).getDesc();
					final Translation styleDescBackup = styleDesc.clone();

					TranslationEditJPanel transNameLabel = new TranslationEditJPanel(
							GeopublisherGUI.R("LayerStyle.Edit.Title"), styleName,
							dpLayer.getAtlasConfig().getLanguages());

					TranslationEditJPanel transDescLabel = new TranslationEditJPanel(
							GeopublisherGUI.R("LayerStyle.Edit.Desc"), styleDesc,
							dpLayer.getAtlasConfig().getLanguages());

					ask = new TranslationAskJDialog(
							ManageLayerStylesDialog.this, transNameLabel,
							transDescLabel);
					ask.addPropertyChangeListener(new PropertyChangeListener() {

						public void propertyChange(PropertyChangeEvent evt) {
							if (evt
									.getPropertyName()
									.equals(
											TranslationAskJDialog.PROPERTY_CANCEL_AND_CLOSE)) {
								ask = null;

								dpLayer.getLayerStyles().get(idx).setTitle(
										styleNameBackup);
								dpLayer.getLayerStyles().get(idx).setDesc(
										styleDescBackup);
							}
							if (evt
									.getPropertyName()
									.equals(
											TranslationAskJDialog.PROPERTY_APPLY_AND_CLOSE)) {
							}
							// TODO update the GUI by listener to the datpool..
							// done yet?

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

	protected TableModel getTableModel() {
		TableModel model = new DefaultTableModel() {

			@Override
			public Class<?> getColumnClass(int columnIndex) {
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
							.R("ManageLayerStylesDialog.Columns.ActiveInMaps.Label");
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

						String mapListString = "";

						// Tell the user in which maps this default style is
						// used
						Collection<Map> maps = dpLayer.getAtlasConfig().getMapPool()
								.values();
						for (Map map : maps) {
							// Check every map what additional styles are
							// configured for THIS dpLayer
							List<String> additionalStyles = map
									.getAdditionalStyles().get(dpLayer.getId());

							if ((additionalStyles != null)
									&& (additionalStyles.size() > 0)) {
								// At least one additional Style is available
								continue;
							}

							mapListString += "'" + map.getTitle().toString()
									+ "' ";

							// for (String styleId : additionalStyles) {
							// if (styleId.equals(layerStyle.getID()) {
							// continue;
							// }
							// }

						}

						return mapListString;
					}
					return null;
				}

				LayerStyle layerStyle = dpLayer.getLayerStyles().get(
						rowIndex - 1);
				if (columnIndex == 0)
					return layerStyle.getTitle().toString();
				if (columnIndex == 1)
					return layerStyle.getDesc().toString();

				if (columnIndex == 2) {

					// List all maps where this Style is made available to the
					// user.

					String mapListString = "";
					for (Map map2 : dpLayer.getAtlasConfig().getMapPool().values()) {
						java.util.Map<String, ArrayList<String>> additionalStyles = map2
								.getAdditionalStyles();

						List<String> stylesForLayer = additionalStyles
								.get(dpLayer.getId());

						if (stylesForLayer == null)
							continue;

						for (String styleId : stylesForLayer) {
							if (styleId.equals(layerStyle.getID())) {

								// A star signals, that this style is started
								// with
								String selectedStyleID = map2
										.getSelectedStyleID(dpLayer.getId());
								if ((styleId.equals(selectedStyleID))
										|| (stylesForLayer.size() == 1)) {
									mapListString += "*'"
											+ map2.getTitle().toString() + "' ";
								} else {
									mapListString += "'"
											+ map2.getTitle().toString() + "' ";
								}

								continue;
							}
						}

					}

					return mapListString;
				}
				return null;
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return false;
			}

		};

		return model;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 4;
			gridBagConstraints8.anchor = GridBagConstraints.EAST;
			gridBagConstraints8.weightx = 1.0;
			gridBagConstraints8.gridy = 0;
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 3;
			gridBagConstraints7.anchor = GridBagConstraints.EAST;
			gridBagConstraints7.weightx = 1.0;
			gridBagConstraints7.gridy = 0;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 2;
			gridBagConstraints6.anchor = GridBagConstraints.WEST;
			gridBagConstraints6.weightx = 1.0;
			gridBagConstraints6.gridy = 0;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 1;
			gridBagConstraints5.anchor = GridBagConstraints.WEST;
			gridBagConstraints5.weightx = 1.0;
			gridBagConstraints5.gridy = 0;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.anchor = GridBagConstraints.WEST;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.gridy = 0;
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.add(getJButtonAdd(), gridBagConstraints4);
			jPanel.add(getJButtonRemove(), gridBagConstraints5);
			jPanel.add(getJButtonDefault(), gridBagConstraints6);
			jPanel.add(getJButtonOk(), gridBagConstraints7);
			String dialogTitle = "manage styles for "// i8n
					+ dpLayer.getTitle().toString();
			setTitle(dialogTitle);
		}
		return jPanel;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	protected JButton getJButtonAdd() {
		if (jButtonAdd == null) {
			jButtonAdd = new JButton();
			jButtonAdd.setText(GeopublisherGUI.R("LayerStyle.New.Button.Label"));
			jButtonAdd.setToolTipText(GeopublisherGUI
					.R("LayerStyle.New.Button.TT"));

			jButtonAdd.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					/**
					 * We don't have a relation to any map here, thats why we
					 * don't link the returned style with a map.
					 * 
					 * @see ManagleLayerStylesForMapDialog
					 */
					createNewStyle(dpLayer, null, ManageLayerStylesDialog.this);

					ace.getDataPool().fireChangeEvents(EventTypes.changeDpe);
				}

			});
		}
		return jButtonAdd;
	}

	/**
	 * Creates a new {@link LayerStyle} by asking the user to translate the
	 * corresponding description and title fields. The Style is saved to an .sld
	 * file, and the ID is added to the list of additional styles for this
	 * {@link DpLayer}.
	 * 
	 * @param dplayer
	 * @param styleUsedRightNow
	 * @param owner
	 * 
	 * @return A new Style
	 */
	public LayerStyle createNewStyle(DpLayer<?, ? extends ChartStyle> dplayer,
			Style styleUsedRightNow, Component owner) {
		List<String> langs = dplayer.getAtlasConfig().getLanguages();

		final Translation name = new Translation();
		final Translation desc = new Translation();

		TranslationEditJPanel nameTransLabel = new TranslationEditJPanel(
				GeopublisherGUI.R("LayerStyle.Edit.Title"), name, dplayer.getAtlasConfig()
						.getLanguages());

		TranslationEditJPanel descTransLabel = new TranslationEditJPanel(
				GeopublisherGUI.R("LayerStyle.Edit.Desc"), desc, langs);

		ask = new TranslationAskJDialog(owner, nameTransLabel, descTransLabel);

		ask.addPropertyChangeListener(new PropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(
						TranslationAskJDialog.PROPERTY_CANCEL_AND_CLOSE)) {
					ask = null;
				}
			}

		});

		ask.setVisible(true);
		if (ask == null)
			return null; // canceled

		// Find a free three digit number
		int counter = 0;

		try {
			AtlasConfigEditable ace = (AtlasConfigEditable) dplayer.getAtlasConfig();
			while (IOUtil.changeFileExt(
					new File(new File(ace.getDataDir(), dplayer
							.getDataDirname()), dplayer.getFilename()),
					String.format("%03d", counter) + ".sld").exists()) {
				counter++;
			}
			File file = IOUtil.changeFileExt(new File(new File(
					ace.getDataDir(), dplayer.getDataDirname()), dplayer
					.getFilename()), String.format("%03d", counter) + ".sld");

			/*******************************************************************
			 * Which Style to take as a template? .. Maybe we got one passed
			 * over
			 */
			Style style;

			if (styleUsedRightNow != null) {
				style = styleUsedRightNow;
				JOptionPane.showMessageDialog(owner,
						"Saving the active style as a new additional style"); // i8n
			} else if (dplayer.getStyle() != null) {
				style = dplayer.getStyle();
				JOptionPane.showMessageDialog(owner,
						"Using the default style to create a new style"); // i8n
			} else {
				style = ASUtil.createDefaultStyle(dplayer);
				JOptionPane.showMessageDialog(owner,
						"Creating a new style. Default style doesn't exist!"); // i8n
			}
			StylingUtil.saveStyleToSLD(style, file);

			String fileName = file.getName();

			LayerStyle newLayerStyĺe = new LayerStyle(fileName, name, desc,
					dplayer);

			/**
			 * Tell the DpLayer about the new additional style.
			 */
			dplayer.getLayerStyles().add(newLayerStyĺe);

			return newLayerStyĺe;

		} catch (Exception e1) {
			LOGGER.error(e1);
			ExceptionDialog.show(ManageLayerStylesDialog.this, e1);
		}
		return null;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonRemove() {
		if (jButtonRemove == null) {
			jButtonRemove = new JButton();
			jButtonRemove.setText(GeopublisherGUI
					.R("LayerStyle.Remove.Button.Label"));
			jButtonRemove.setEnabled(false);
			jButtonRemove.setToolTipText(GeopublisherGUI
					.R("LayerStyle.Remove.Button.TT"));
			jButtonRemove.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					// The first row is the default style which doesn't count.
					int idx = getTable().getSelectedRow() - 1;

					LayerStyle lsRemove = dpLayer.getLayerStyles().get(idx);
					if (lsRemove == null) {
						return;
					}

					if (!AVSwingUtil.askYesNo(ManageLayerStylesDialog.this,
							GeopublisherGUI.R("LayerStyle.Remove.Action.Question",
									lsRemove.getTitle()))) {
						return;
					}

					/**
					 * 1. Close any open MapComposer instances...
					 */
					if (!GPDialogManager.dm_MapComposer.closeAllInstances())
						return;

					/**
					 * 2. Remove any references to the Style
					 */
					for (Map map : ace.getMapPool().values()) {
						ArrayList<String> lsIDList = map.getAdditionalStyles()
								.get(dpLayer.getId());

						if (lsIDList != null
								&& lsIDList.contains(lsRemove.getID())) {
							lsIDList.remove(lsRemove.getID());
						}

						/**
						 * What if the style has been maked as the
						 * selcted/default style?!
						 */
						if (map.getSelectedStyle(dpLayer.getId()) != null
								&& map.getSelectedStyle(dpLayer.getId())
										.getID().equals(lsRemove.getID())) {
							if (map.getAdditionalStyles().get(dpLayer.getId())
									.size() > 0) {
								// Replace the selection with another additional
								// style if one exists
								map.setSelectedStyleID(dpLayer.getId(), map
										.getAdditionalStyles().get(
												dpLayer.getId()).get(0));
							} else {
								// Reset the selection to null
								map.setSelectedStyleID(dpLayer.getId(), null);
							}
						}
					}

					/**
					 * 3. Remove the file from the disk
					 */
					File styleFile = new File(new File(ace.getDataDir(),
							dpLayer.getDataDirname()), lsRemove.getFilename());
					if (styleFile.exists() && !styleFile.delete()) {
						ExceptionDialog.show(ManageLayerStylesDialog.this,
								new AtlasException(
										"The style's file could not be deleted.\n"
												+ styleFile.getAbsolutePath())); // i8nAC
					}

					/**
					 * 4. Remove the Style from the list of styles for this
					 * layer
					 */
					lsRemove = dpLayer.getLayerStyles().remove(idx);

					/**
					 * Update the table
					 */
					ace.getDataPool().fireChangeEvents(EventTypes.changeDpe);
				}

			});

			getTable().getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {

						public void valueChanged(ListSelectionEvent e) {
							jButtonRemove.setEnabled(getTable()
									.getSelectedRow() > 0);
						}

					});

		}
		return jButtonRemove;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonDefault() {
		if (jButtonDefault == null) {
			jButtonDefault = new JButton();
			jButtonDefault.setText("default"); // i8nAC
			jButtonDefault
					.setToolTipText("make the selected style the default style for this layer"); // i8nAC

			jButtonDefault.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					String msg = "<html>Do you really want to use this style as default?<br/>The old default style will be lost.<br>Note: Title and description will not be copied.";
					String title = "Use as default?";

					int res = JOptionPane.showConfirmDialog(
							ManageLayerStylesDialog.this, msg, title,
							JOptionPane.YES_NO_OPTION);
					if (res != JOptionPane.YES_OPTION)
						return;

					int idx = getTable().getSelectedRow() - 1;
					LayerStyle style = dpLayer.getLayerStyles().get(idx);

					File srcFile = new File(new File(
							((AtlasConfigEditable) dpLayer.getAtlasConfig())
									.getDataDir(), dpLayer.getDataDirname()),
							style.getFilename());

					File destFile = IOUtil.changeFileExt(new File(new File(
							((AtlasConfigEditable) dpLayer.getAtlasConfig())
									.getDataDir(), dpLayer.getDataDirname()),
							dpLayer.getFilename()), ".sld");

					try {
						FileUtils.copyFile(srcFile, destFile);
					} catch (IOException e1) {
						ExceptionDialog.show(ManageLayerStylesDialog.this, e1);
					}

					// Fires an event, but to change should be visible in
					// today's GUI
					ace.getDataPool().fireChangeEvents(EventTypes.changeDpe);
				}

			});

			getTable().getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {

						public void valueChanged(ListSelectionEvent e) {
							jButtonDefault.setEnabled(getTable()
									.getSelectedRow() > 0);
						}

					});

		}
		return jButtonDefault;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	protected JButton getJButtonOk() {
		if (jButtonOk == null) {
			jButtonOk = new OkButton();
			jButtonOk.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					performOKButton();
				}

			});
		}
		return jButtonOk;
	}

	/**
	 * Is overwritten in *ForLayer to take care of the selectedStyle (which is
	 * saved in the map)
	 */
	protected void performOKButton() {
		dispose();
	}

}
