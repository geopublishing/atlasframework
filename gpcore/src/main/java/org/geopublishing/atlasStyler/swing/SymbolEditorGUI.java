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
package org.geopublishing.atlasStyler.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.RuleChangeListener;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geopublishing.atlasStyler.SingleRuleList;
import org.geopublishing.atlasViewer.resource.icons.Icons;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.Graphic;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Symbolizer;
import org.opengis.feature.type.GeometryDescriptor;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.swing.JPanel;
import schmitzm.swing.SwingUtil;
import skrueger.geotools.LegendIconFeatureRenderer;
import skrueger.swing.CancelButton;
import skrueger.swing.CancellableDialogAdapter;
import skrueger.swing.OkButton;
import skrueger.swing.SmallButton;

import com.vividsolutions.jts.geom.Point;

public class SymbolEditorGUI extends CancellableDialogAdapter {

	protected Logger LOGGER = ASUtil.createLogger(this);

	protected static final String PROPERTY_LAYER_SELECTION = "LAYER_SELECTION";

	protected static final String PROPERTY_SYMBOL_CHANGED = "SYMBOL_CHANGED";
	protected static final String PROPERTY_LAYERS_CHANGED = "LAYERS_CHANGED";

	

	private static final String DIALOG_TITLE = AtlasStyler
			.R("SymbolEditor.Title");

	private static final Dimension SYMBOL_SIZE = new Dimension(60, 60);

	private JPanel jContentPane = null;

	private JPanel jPanelPreview = null;

	private JPanel jPanelProperties = null;

	private JTable jTableLayers = null;

	private JButton jButtonNewLayer = null;

	private JButton jButtonRemoveLayer = null;

	private JButton jButtonLayerDown = null;

	private JButton jButtonLayerUp = null;

	private JButton jButtonOk = null;

	private JButton jButtonCancel = null;

	private JLabel jLabelPreviewIcon = null;

	private final SingleRuleList<Symbolizer> singleSymbolRuleList;

	private DefaultTableModel tableModelLayers;

	protected Symbolizer selectedSymbolizer;

	protected SingleRuleList<?> backup;

	public void cancel() {
		backup.copyTo(singleSymbolRuleList);
	}

	/**
	 * This little {@link PropertyChangeListener} propagates changes in the
	 * properties JPanel to the {@link SymbolEditorGUI} and to any
	 * {@link RuleChangeListener}
	 */
	protected PropertyChangeListener propagateL = new PropertyChangeListener() {

		public void propertyChange(PropertyChangeEvent evt) {
			if (!evt.getPropertyName().equals(AbstractEditGUI.PROPERTY_UPDATED))
				return;

			SymbolEditorGUI.this.firePropertyChange(PROPERTY_SYMBOL_CHANGED,
					null, null);

			singleSymbolRuleList.fireEvents(new RuleChangedEvent(
					"SymbolEditorGUI propagator", singleSymbolRuleList));

			LOGGER.debug("Propagating...");
		}
	};

	/**
	 * @param selectorGUI
	 */
	public SymbolEditorGUI(Component owner,
			SingleRuleList<? extends Symbolizer> singleSymbolRuleList) {
		super(owner);
		this.singleSymbolRuleList = (SingleRuleList<Symbolizer>) singleSymbolRuleList;

		backup = singleSymbolRuleList.clone(false);

		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		// addWindowListener(new WindowAdapter() {
		//
		// @Override
		// public void windowClosing(WindowEvent e) {
		// cancel();
		// }
		//
		// });
		this.setTitle(DIALOG_TITLE);
		this.setContentPane(getJContentPane());
		this.pack();

		SwingUtil.setRelativeFramePosition(this, getParent(),
				SwingUtil.BOUNDS_OUTER, SwingUtil.NORTHEAST);
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel(new BorderLayout());
			JPanel upper = new JPanel(new MigLayout("flowy"));

			// Has to initialized before getJPanelPreview
			final JPanel jPanelProperties2 = getJPanelProperties();

			upper.add(getJPanelPreview(), "center, split 2, growx");
			upper.add(getJPanelLayers(), "wrap");
			upper.add(jPanelProperties2, "top");

			jContentPane.add(upper, BorderLayout.CENTER);
			jContentPane.add(getJPanelButtons(), BorderLayout.SOUTH);
		}
		return jContentPane;
	}

	/**
	 * Return a {@link BufferedImage} that represent the symbol.
	 * 
	 * @param bg
	 *            Paints the background of the {@link Image} in that color or
	 *            not.
	 */
	public BufferedImage getSymbolImage(Symbolizer symb, Color bg) {

		// TODO Caching?
		// System.out.println("generating a new layer image");

		Rule rule = CommonFactoryFinder.getStyleFactory(null).createRule();

		rule.setSymbolizers(new Symbolizer[] { symb });

		final LegendIconFeatureRenderer renderer = LegendIconFeatureRenderer
				.getInstance();

		BufferedImage image;

		try {
			image = renderer.createImageForRule(rule, FeatureUtil
					.createFeatureType(Point.class), SYMBOL_SIZE, bg);
		} catch (Exception e) {
			LOGGER.warn(e.getMessage());
			image = new BufferedImage(SYMBOL_SIZE.width, SYMBOL_SIZE.height,
					BufferedImage.TYPE_INT_ARGB);
		}

		return image;
	}

	final PropertyChangeListener listenerUpdatePreview = new PropertyChangeListener() {

		public void propertyChange(PropertyChangeEvent evt) {

			if ((evt.getPropertyName().equals(PROPERTY_SYMBOL_CHANGED))
					|| (evt.getPropertyName().equals(PROPERTY_LAYERS_CHANGED))) {

				jLabelPreviewIcon.setIcon(new ImageIcon(singleSymbolRuleList
						.getImage(new Dimension(SYMBOL_SIZE.width,
								SYMBOL_SIZE.height))));
				// Do not! fire a new event! The preview is updated. That is the
				// sense of the listener
				// singleSymbolRuleList.fireEvents(new RuleChangedEvent(
				// "Updating Preview in SymbolEditorGUI",
				// singleSymbolRuleList));

			}
		}

	};

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelPreview() {
		if (jPanelPreview == null) {
			jLabelPreviewIcon = new JLabel(new ImageIcon(singleSymbolRuleList
					.getImage(new Dimension(SYMBOL_SIZE.width,
							SYMBOL_SIZE.height))));

			SymbolEditorGUI.this
					.addPropertyChangeListener(listenerUpdatePreview);

			jPanelPreview = new JPanel(new MigLayout(), AtlasStyler
					.R("SymbolSelector.Preview.BorderTitle"));
			jPanelPreview.add(jLabelPreviewIcon, "center");

		}
		return jPanelPreview;
	}

	/**
	 * This method initializes jPanel1
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelLayers() {
		JPanel jPanelLayers;
		jPanelLayers = new JPanel(new MigLayout(), AtlasStyler
				.R("SymbolEditor.SymbolLayers"));
		jPanelLayers.add(new JScrollPane(getJTableLayers()), "wrap, top, w "
				+ (SYMBOL_SIZE.width + 30) + ", h " + (SYMBOL_SIZE.height * 3)); // ,"top, wrap, growy, shrinkx"
		// jPanelLayers.add(new JScrollPane(getJTableLayers()),"wrap");
		// //,"top, wrap, growy, shrinkx"

		jPanelLayers.add(getJButtonNewLayer(), "split 4");
		jPanelLayers.add(getJButtonRemoveLayer());
		jPanelLayers.add(getJButtonLayerDown());
		jPanelLayers.add(getJButtonLayerUp());
		return jPanelLayers;
	}

	private Symbolizer guiIsUpToDateForThisSymbolizer = null;
	
	/**
	 * This method initializes jPanel2
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelProperties() {
		if (jPanelProperties == null) {
			jPanelProperties = new JPanel(new BorderLayout());
			jPanelProperties.setBorder(BorderFactory
					.createTitledBorder(AtlasStyler
							.R("SymbolEditor.Properties")));

			addPropertyChangeListener(new PropertyChangeListener() {


				public void propertyChange(PropertyChangeEvent evt) {
					if ((evt.getPropertyName().equals(PROPERTY_LAYER_SELECTION))
//							|| (evt.getPropertyName()
//									.equals(PROPERTY_LAYERS_CHANGED))
									) 
						
					{

						if (selectedSymbolizer == null) {
							jPanelProperties.removeAll();
							jPanelProperties.add(new JLabel(AtlasStyler
									.R("SymbolEditor.SelectALayer")),
									BorderLayout.CENTER);
						} else {
							
							// Check, that the selected symbolizer really changed.. otherwise we are updating the GUI too often
							if (selectedSymbolizer == guiIsUpToDateForThisSymbolizer) return; 

							GeometryDescriptor ruleGeometry = singleSymbolRuleList
									.getGeometryDescriptor();

//							JTable tableLayers = getJTableLayers();
//							int selectedRow = tableLayers.getSelectedRow();

							JComponent symbolEditGUI = null;


								switch (FeatureUtil
										.getGeometryForm(ruleGeometry)) {
								case POINT:
									Graphic graphic = ((PointSymbolizer) selectedSymbolizer)
											.getGraphic();
									symbolEditGUI = new GraphicEditGUI_Mig(
											graphic,
											FeatureUtil
													.createFeatureType(Point.class));
									break;
								case LINE:
									symbolEditGUI = new LineSymbolEditGUI(
											((org.geotools.styling.LineSymbolizer) selectedSymbolizer));
									break;
								case POLYGON:
									symbolEditGUI = new PolygonSymbolEditGUI(
											((PolygonSymbolizer) selectedSymbolizer));
									break;
								default:
									throw new IllegalStateException(
											"unrecognized type");
								}

								symbolEditGUI
										.addPropertyChangeListener(propagateL);
//								LOGGER.debug("Adding propagateL to "
//										+ symbolEditGUI);


							/**
							 * Update the jPanelProperties (if we have a GUI)
							 */
							jPanelProperties.removeAll();
							if (symbolEditGUI != null) {
								jPanelProperties.add(symbolEditGUI,
										BorderLayout.NORTH);

							}
						}
						
						// Re-Layout the GUI
						pack();
						jPanelProperties.repaint();
						
						guiIsUpToDateForThisSymbolizer = selectedSymbolizer;

					}
				}

			});
		}
		return jPanelProperties;
	}

	/**
	 * This method initializes jTable
	 * 
	 * @return javax.swing.JTable
	 */
	private JTable getJTableLayers() {
		if (jTableLayers == null) {
			jTableLayers = new JTable(getLayersTableModel());

			jTableLayers.setRowHeight(SYMBOL_SIZE.height + 2);

			jTableLayers.getColumnModel().getColumn(0).setMaxWidth(20);
			jTableLayers.getColumnModel().getColumn(1).setMaxWidth(
					SYMBOL_SIZE.width + 2);

			jTableLayers.setDefaultRenderer(BufferedImage.class,
					new TableCellRenderer() {

						public Component getTableCellRendererComponent(
								JTable table, Object value, boolean isSelected,
								boolean hasFocus, int row, int column) {
							if (value != null)
								return new JLabel(new ImageIcon(
										(BufferedImage) value));
							else
								return new JLabel("add a layer to your symbol");
							// TODO// Default// Image
							// that
							// tells
							// the
							// user
							// to
						}

					});

			// Listens for removed or new table entries (= PointSymbolizers)
			addPropertyChangeListener(new PropertyChangeListener() {

				public void propertyChange(PropertyChangeEvent evt) {
					if (evt.getPropertyName().equals(PROPERTY_LAYERS_CHANGED)) {
						
						int selectedRow = getJTableLayers().getSelectedRow();
						getLayersTableModel().fireTableDataChanged();
						getJTableLayers().getSelectionModel()
								.addSelectionInterval(selectedRow, selectedRow);
						
					} else if ((evt.getPropertyName()
									.equals(PROPERTY_SYMBOL_CHANGED)))
					{

						for (int i = 0; i < getLayersTableModel().getRowCount(); i++) {
							getLayersTableModel().fireTableCellUpdated(i, 1);
						}
						
					}
				}

			});

			jTableLayers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			jTableLayers.getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {

						public void valueChanged(ListSelectionEvent e) {

							if (e.getValueIsAdjusting())
								return;

							int idx = jTableLayers.getSelectedRow();

							if (idx == -1) {
								selectedSymbolizer = null;
							} else {
								selectedSymbolizer = (Symbolizer) singleSymbolRuleList
										.getSymbolizers().get(idx);
							}

							SymbolEditorGUI.this.firePropertyChange(
									PROPERTY_LAYER_SELECTION, null, null);
						}

					});

			if (getLayersTableModel().getRowCount() > 0) {
				jTableLayers.getSelectionModel().addSelectionInterval(0, 0);
			}

		}
		return jTableLayers;
	}

	protected DefaultTableModel getLayersTableModel() {
		if (tableModelLayers == null) {

			tableModelLayers = new DefaultTableModel() {

				@Override
				public Class<?> getColumnClass(int columnIndex) {
					if (columnIndex == 0)
						return String.class;
					if (columnIndex == 1)
						return ImageIcon.class;
					return null;
				}

				@Override
				public int getColumnCount() {
					return 2;
				}

				@Override
				public String getColumnName(int columnIndex) {
					if (columnIndex == 0)
						return AtlasStyler.R("SymbolEditor.TableColumns.Order");
					if (columnIndex == 1)
						return AtlasStyler
								.R("SymbolEditor.TableColumns.LayerPreview");

					return null;
				}

				@Override
				public int getRowCount() {
					return singleSymbolRuleList.getSymbolizers().size();
				}

				@Override
				public Object getValueAt(int rowIndex, int columnIndex) {
					if (columnIndex == 0)
						return rowIndex + 1;

					if (columnIndex == 1) {
						Vector<? extends Symbolizer> symbolizers = singleSymbolRuleList
								.getSymbolizers();

						ImageIcon imageIcon = new ImageIcon(getSymbolImage(
								symbolizers.get(rowIndex), jTableLayers
										.getBackground()));

						return imageIcon;

					}
					return null;
				}

				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
				}

			};
		}
		return tableModelLayers;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonNewLayer() {
		if (jButtonNewLayer == null) {
			jButtonNewLayer = new SmallButton(new AbstractAction(" + ") {

				public void actionPerformed(ActionEvent e) {

					singleSymbolRuleList.addNewDefaultLayer();

					// Select the newly created layer
					jTableLayers.getSelectionModel().addSelectionInterval(0,
							singleSymbolRuleList.getSymbolizers().size() - 1);

					SymbolEditorGUI.this.firePropertyChange(
							PROPERTY_LAYERS_CHANGED, null, null);
				}

			});
			jButtonNewLayer.setToolTipText(AtlasStyler
					.R("SymbolEditor.Action.AddSymbolLayer.TT"));
		}
		return jButtonNewLayer;
	}

	/**
	 * This method initializes a button to remove the selected layer.
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonRemoveLayer() {
		if (jButtonRemoveLayer == null) {
			jButtonRemoveLayer = new SmallButton(new AbstractAction(" - ") {

				public void actionPerformed(ActionEvent e) {
					if (jTableLayers.getSelectedRow() >= 0) {
						// Something is selected
						singleSymbolRuleList.removeSymbolizer(jTableLayers
								.getSelectedRow());

						jTableLayers.getSelectionModel().clearSelection();
						jButtonLayerUp.setEnabled(false);
						jButtonLayerDown.setEnabled(false);
						jButtonRemoveLayer.setEnabled(false);
						selectedSymbolizer = null;
						//
						// // Clear the cache of all GUIs, as they are chached
						// with
						// // their RowIndex as key (Sadly a PointSymbolizer
						// // doesn't have a name or title)
						// // editPropertiesGUICache.clear();

						if (getLayersTableModel().getRowCount() > 0) {
							jTableLayers.getSelectionModel()
									.addSelectionInterval(0, 0);
						}

						SymbolEditorGUI.this.firePropertyChange(
								PROPERTY_LAYERS_CHANGED, null, null);
												
					}
				}

			});
			jTableLayers.getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {

						public void valueChanged(ListSelectionEvent e) {
							if (e.getValueIsAdjusting())
								return;
							if (((ListSelectionModel) e.getSource())
									.getMinSelectionIndex() < 0) {
								jButtonRemoveLayer.setEnabled(false);
							} else {
								jButtonRemoveLayer.setEnabled(true);
							}
						}

					});
			jButtonRemoveLayer.setEnabled(false);
			jButtonRemoveLayer.setToolTipText(AtlasStyler
					.R("SymbolEditor.Action.RemoveSymbolLayer.TT"));
		}
		return jButtonRemoveLayer;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonLayerDown() {
		if (jButtonLayerDown == null) {
			jButtonLayerDown = new SmallButton(new AbstractAction("", Icons
					.getDownArrowIcon()) {

				public void actionPerformed(ActionEvent e) {
					if (jTableLayers.getSelectedRow() >= 0) {
						// Something is selected
						Vector<Symbolizer> symbolizers = singleSymbolRuleList
								.getSymbolizers();

						if (jTableLayers.getSelectedRow() < symbolizers.size() - 1) {
							Symbolizer symbolizer = symbolizers
									.remove(jTableLayers.getSelectedRow());
							symbolizers.insertElementAt(symbolizer,
									jTableLayers.getSelectedRow() + 1);
							jTableLayers.getSelectionModel()
									.addSelectionInterval(0,
											jTableLayers.getSelectedRow() + 1);
							//
							// // Clear the cache of all GUIs, as they are
							// // chached with
							// // their RowIndex as key (Sadly a
							// // PointSymbolizer
							// // doesn't have a name or title)
							// editPropertiesGUICache.clear();

							SymbolEditorGUI.this.firePropertyChange(
									PROPERTY_LAYERS_CHANGED, null, null);
						}

					}
				}

			});
			jTableLayers.getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {

						public void valueChanged(ListSelectionEvent e) {
							if (e.getValueIsAdjusting())
								return;
							ListSelectionModel lsm = ((ListSelectionModel) e
									.getSource());
							if ((lsm.getMinSelectionIndex() < 0)
									|| (lsm.getMinSelectionIndex() >= jTableLayers
											.getModel().getRowCount() - 1)) {
								jButtonLayerDown.setEnabled(false);
							} else {
								jButtonLayerDown.setEnabled(true);
							}
						}

					});

			jButtonLayerDown.setEnabled(false);
			jButtonLayerDown.setToolTipText(AtlasStyler
					.R("SymbolEditor.Action.MoveUpSymbolLayerDown.TT"));
		}
		return jButtonLayerDown;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonLayerUp() {
		if (jButtonLayerUp == null) {
			jButtonLayerUp = new SmallButton(new AbstractAction("", Icons
					.getUpArrowIcon()) {

				public void actionPerformed(ActionEvent e) {
					if (jTableLayers.getSelectedRow() >= 0) {
						// Something is selected
						Vector<Symbolizer> symbolizers = singleSymbolRuleList
								.getSymbolizers();

						if (jTableLayers.getSelectedRow() > 0) {
							Symbolizer symbolizer = symbolizers
									.remove(jTableLayers.getSelectedRow());
							symbolizers.insertElementAt(symbolizer,
									jTableLayers.getSelectedRow() - 1);
							jTableLayers.getSelectionModel()
									.addSelectionInterval(0,
											jTableLayers.getSelectedRow() - 1);
							//
							// // Clear the cache of all GUIs, as they are
							// // chached with
							// // their RowIndex as key (Sadly a
							// // PointSymbolizer
							// // doesn't have a name or title)
							// editPropertiesGUICache.clear();

							SymbolEditorGUI.this.firePropertyChange(
									PROPERTY_LAYERS_CHANGED, null, null);
						}

					}
				}

			});
			jTableLayers.getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {

						public void valueChanged(ListSelectionEvent e) {
							if (e.getValueIsAdjusting())
								return;
							if (((ListSelectionModel) e.getSource())
									.getMinSelectionIndex() < 1) {
								jButtonLayerUp.setEnabled(false);
							} else {
								jButtonLayerUp.setEnabled(true);
							}
						}

					});
			jButtonLayerUp.setEnabled(false);
			jButtonLayerUp.setToolTipText(AtlasStyler
					.R("SymbolEditor.Action.MoveUpSymbolLayerUp.TT"));
		}
		return jButtonLayerUp;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonOk() {
		if (jButtonOk == null) {
			jButtonOk = new OkButton(new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					okClose();
				}

			});
		}
		return jButtonOk;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonCancel() {
		if (jButtonCancel == null) {
			jButtonCancel = new CancelButton(new AbstractAction() {

				public void actionPerformed(ActionEvent e) {
					cancelClose();
				}

			});
		}
		return jButtonCancel;
	}

	@Override
	public void dispose() {
		
		if (isDisposed) return;

		// Remove the listener that updated the preview and sends the events on
		// to the next layer
		LOGGER.debug("Removing the updatePreviewPanelIconOnChange Listener");
		removePropertyChangeListener(listenerUpdatePreview);
		//
		// if (editPropertiesGUICache != null) {
		// editPropertiesGUICache.clear();
		// }
		super.dispose();
	}

	/**
	 * This method initializes jPanel1
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelButtons() {
		schmitzm.swing.JPanel jPanelButtons = new JPanel(new MigLayout());
		jPanelButtons.add(getJButtonOk(), "tag ok");
		jPanelButtons.add(getJButtonCancel(), "tag cancel");
		return jPanelButtons;
	}

}
