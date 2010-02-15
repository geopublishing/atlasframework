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
package skrueger.atlas.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.identity.FeatureIdImpl;
import org.jfree.chart.JFreeChart;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.identity.Identifier;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.gui.GeotoolsGUIUtil;
import schmitzm.geotools.gui.SelectableXMapPane;
import schmitzm.jfree.chart.SelectableChartPanel;
import schmitzm.jfree.chart.SelectableChartPanel.WindowSelectionMode;
import schmitzm.jfree.chart.selection.DatasetSelectionChangeEvent;
import schmitzm.jfree.chart.selection.DatasetSelectionListener;
import schmitzm.jfree.feature.FeatureChartPanel;
import schmitzm.jfree.feature.FeatureDatasetSelectionModel;
import schmitzm.jfree.feature.style.FeatureChartUtil;
import schmitzm.swing.ExceptionDialog;
import skrueger.atlas.AVDialogManager;
import skrueger.atlas.AtlasConfig;
import skrueger.atlas.AtlasViewer;
import skrueger.atlas.gui.plaf.BasicMapLayerLegendPaneUI;
import skrueger.atlas.resource.icons.Icons;
import skrueger.geotools.MapView;
import skrueger.geotools.StyledFeaturesInterface;
import skrueger.geotools.selection.ChartSelectionSynchronizer;
import skrueger.geotools.selection.StyledFeatureLayerSelectionModel;
import skrueger.geotools.selection.StyledLayerSelectionModel;
import skrueger.geotools.selection.StyledLayerSelectionModelSynchronizer;
import skrueger.swing.Disposable;
import skrueger.swing.SmallButton;
import skrueger.swing.SmallToggleButton;

/**
 * Provides a JPanel that show charts. This static class manages all instances
 * of the dialogs. ChartJDialogs can only be created via
 * {@link #getInstanceFor(Component, AtlasChart, MapLegend)} which uses a cache
 * to return any an dialog if it exists.
 */
public class AtlasChartJPanel extends JPanel implements
		DatasetSelectionListener, Disposable {
	static final Logger LOGGER = Logger.getLogger(AtlasChartJPanel.class);

	/**
	 * A reference to the {@link JFreeChart} that is passed on to the
	 * {@link SelectableChartPanel}
	 */
	private JFreeChart chart;

	static final ImageIcon ICON_SELECTION_ADD = new ImageIcon(MapView.class
			.getResource("resource/icons/selection_add.png"));

	static final ImageIcon ICON_SELECTION_CLEAR = new ImageIcon(MapView.class
			.getResource("resource/icons/selection_clear.png"));

	static final ImageIcon ICON_SELECTION_REMOVE = new ImageIcon(MapView.class
			.getResource("resource/icons/selection_remove.png"));

	static final ImageIcon ICON_SELECTION_SET = new ImageIcon(MapView.class
			.getResource("resource/icons/selection_set.png"));

	static final ImageIcon ICON_ZOOM = new ImageIcon(MapView.class
			.getResource("resource/icons/zoom_in.png"));

	static final ImageIcon ICON_FULL_EXTEND = new ImageIcon(MapView.class
			.getResource("resource/icons/zoom_full_extend.png"));

	private MapLegend mapLegend;

	/** A chart panel that **/
	private SelectableChartPanel selectableChartPanel;

	private StyledLayerSelectionModel<?> selectionModel;
	private final StyledFeaturesInterface<?> styledLayer;
	private JToolBar toolBar;

	private BufferedImage mapImageIcon;

	/** Keeps references to the listener for WeakHashSet **/
	private Set<DatasetSelectionListener> insertedListeners = new HashSet<DatasetSelectionListener>();

	/**
	 * Instantiates am {@link AtlasChartJPanel} and sets the mouse selection
	 * mode to WindowSelectionMode.SELECT_SET
	 */
	public AtlasChartJPanel(JFreeChart chart,
			StyledFeaturesInterface<?> styledLayer, MapLegend mapLegend) {

		this.chart = chart;
		this.styledLayer = styledLayer;
		this.mapLegend = mapLegend;
		// TODO react to filter changes?! or what!?
		// mapLegend.getGeoMapPane().getMapPane().addMapPaneListener(
		// new JMapPaneListener() {
		//
		// @Override
		// public void performMapPaneEvent(JMapPaneEvent e) {
		// }
		//
		// });

		// **********************************************************************
		// If we find a mapIcon we will set it to be always rendered into the
		// map
		// **********************************************************************
		final URL mapIconURL = AtlasConfig.getResLoMan().getResourceAsUrl(
				AtlasConfig.MAPICON_RESOURCE_NAME);
		if (mapIconURL != null) {
			try {
				mapImageIcon = ImageIO.read(mapIconURL);
			} catch (IOException e) {
				LOGGER.error(e);
			}

		} else {
//			LOGGER.info("No " + AtlasConfig.MAPICON_RESOURCE_NAME
//					+ " found. Not displaying any icon on top of the chart");
		}

		initGUI();

	}

	/**
	 * Returns a cached {@link SelectableChartPanel} - usually a
	 * {@link FeatureChartPanel} - that has been constructed abound the original
	 * {@link JFreeChart} chart. The anonymous extension of
	 * {@link SelectableChartPanel} adds map-icon rendering to the chart.
	 * 
	 * @return
	 */
	public SelectableChartPanel getSelectableChartPanel() {
		if (selectableChartPanel == null) {
			selectableChartPanel = new FeatureChartPanel(chart) {
				@Override
				public void paint(Graphics g) {
					super.paint(g);

					if (mapImageIcon != null) {
						g.drawImage(mapImageIcon, (int) (selectableChartPanel
								.getBounds().getWidth()
								- mapImageIcon.getWidth() - 10),
								(int) (selectableChartPanel.getBounds()
										.getHeight()
										- mapImageIcon.getHeight() - 10), null);

					}

				}

				@Override
				public void print(Graphics g) {
					super.print(g);

					if (mapImageIcon != null) {
						g.drawImage(mapImageIcon, (int) (selectableChartPanel
								.getBounds().getWidth()
								- mapImageIcon.getWidth() - 10),
								(int) (selectableChartPanel.getBounds()
										.getHeight()
										- mapImageIcon.getHeight() - 10), null);

					}
				}
			};
		}
		return selectableChartPanel;
	}

	/**
	 * Returns the {@link JFreeChart} that is visualized in the
	 * {@link SelectableChartPanel}
	 */
	public JFreeChart getChart() {
		return getSelectableChartPanel().getChart();
	}

	/**
	 * Returns the {@link JFreeChart} that is visualized in the
	 * {@link SelectableChartPanel}
	 */
	public void setChart(JFreeChart newChart) {
		this.chart = newChart;
		getSelectableChartPanel().setChart(chart);
	}

	protected StyledLayerSelectionModel<?> getSelectionModel() {
		if (selectionModel == null) {
			selectionModel = mapLegend != null ? mapLegend
					.getRememberSelection(styledLayer.getId()) : null;

			if ((selectionModel != null && selectionModel instanceof StyledFeatureLayerSelectionModel)) {
				StyledFeatureLayerSelectionModel featureSelectionModel = (StyledFeatureLayerSelectionModel) selectionModel;

				// get the selectionmodel(s) of the chart
				List<FeatureDatasetSelectionModel<?, ?, ?>> datasetSelectionModelFor = FeatureChartUtil
						.getFeatureDatasetSelectionModelFor(getSelectableChartPanel()
								.getChart());

				for (FeatureDatasetSelectionModel dsm : datasetSelectionModelFor) {

					// create a synchronizer
					ChartSelectionSynchronizer synchronizer = new ChartSelectionSynchronizer(
							featureSelectionModel, dsm);

					featureSelectionModel
							.addSelectionListener((StyledLayerSelectionModelSynchronizer) synchronizer);
					dsm.addSelectionListener(synchronizer);

					featureSelectionModel.refreshSelection();
				}
			}

		}
		return selectionModel;
	}

	/**
	 * Creates a {@link JToolBar} that has buttons to interact with the Chart
	 * and it's SelectionModel.
	 * 
	 * @return
	 */
	public JToolBar getToolBar() {
		if (toolBar == null) {

			toolBar = new JToolBar();
			toolBar.setFloatable(false);

			ButtonGroup bg = new ButtonGroup();
			
			/**
			 * Add an Action to ZOOM/MOVE in the chart
			 */
			JToggleButton zoomToolButton = new SmallToggleButton(
					new AbstractAction("", ICON_ZOOM) {

						@Override
						public void actionPerformed(ActionEvent e) {
							getSelectableChartPanel().setWindowSelectionMode(
									WindowSelectionMode.ZOOM_IN_CHART);
						}

					}, AtlasViewer.R("AtlasChartJPanel.zoom.tt"));
			toolBar.add(zoomToolButton);
			bg.add(zoomToolButton);

			toolBar.addSeparator();

			/**
			 * Add an Action not change the selection but just move through the
			 * chart
			 */
			JToggleButton setSelectionButton = new SmallToggleButton(
					new AbstractAction("", ICON_SELECTION_SET) {

						@Override
						public void actionPerformed(ActionEvent e) {
							getSelectableChartPanel().setWindowSelectionMode(
									WindowSelectionMode.SELECT_SET);
						}

					});
			setSelectionButton.setToolTipText(AtlasViewer
					.R("MapPaneButtons.Selection.SetSelection.TT"));
			toolBar.add(setSelectionButton);
			bg.add(setSelectionButton);

			/**
			 * Add an Action to ADD the selection.
			 */
			JToggleButton addSelectionButton = new SmallToggleButton(
					new AbstractAction("", ICON_SELECTION_ADD) {

						@Override
						public void actionPerformed(ActionEvent e) {
							getSelectableChartPanel().setWindowSelectionMode(
									WindowSelectionMode.SELECT_ADD);
						}

					});
			addSelectionButton.setToolTipText(AtlasViewer
					.R("MapPaneButtons.Selection.AddSelection.TT"));
			toolBar.add(addSelectionButton);
			bg.add(addSelectionButton);

			/**
			 * Add an Action to REMOVE the selection.
			 */
			JToggleButton removeSelectionButton = new SmallToggleButton(
					new AbstractAction("", ICON_SELECTION_REMOVE) {

						@Override
						public void actionPerformed(ActionEvent e) {
							getSelectableChartPanel().setWindowSelectionMode(
									WindowSelectionMode.SELECT_REMOVE);
						}

					});
			removeSelectionButton.setToolTipText(AtlasViewer
					.R("MapPaneButtons.Selection.RemoveSelection.TT"));
			toolBar.add(removeSelectionButton);
			bg.add(removeSelectionButton);

			toolBar.addSeparator();

			/**
			 * Add a normal Button to clear the selection. The Chart's selection
			 * models are cleared. If a relation to a JMapPane exists, they will
			 * be synchronized.
			 */
			final SmallButton clearSelectionButton = new SmallButton(
					new AbstractAction("", ICON_SELECTION_CLEAR) {

						@Override
						public void actionPerformed(ActionEvent e) {
							// getSelectionModel().clearSelection();

							// get the selectionmodel(s) of the chart
							List<FeatureDatasetSelectionModel<?, ?, ?>> datasetSelectionModelFor = FeatureChartUtil
									.getFeatureDatasetSelectionModelFor(getSelectableChartPanel()
											.getChart());

							for (FeatureDatasetSelectionModel dsm : datasetSelectionModelFor) {
								dsm.clearSelection();
							}

						}

					}, AtlasViewer
							.R("MapPaneButtons.Selection.ClearSelection.TT"));

			{
				// Add listeners to the selection model, so we knwo when to
				// disable/enable the button

				// get the selectionmodel(s) of the chart
				List<FeatureDatasetSelectionModel<?, ?, ?>> datasetSelectionModelFor = FeatureChartUtil
						.getFeatureDatasetSelectionModelFor(getSelectableChartPanel()
								.getChart());
				for (final FeatureDatasetSelectionModel selModel : datasetSelectionModelFor) {
					DatasetSelectionListener listener_ClearSelectionButtonEnbled = new DatasetSelectionListener() {

						@Override
						public void selectionChanged(
								DatasetSelectionChangeEvent e) {
							if (!e.getSource().getValueIsAdjusting()) {

								// Update the clearSelectionButton
								clearSelectionButton.setEnabled(selModel
										.getSelectedFeatures().size() > 0);
							}
						}
					};
					insertedListeners.add(listener_ClearSelectionButtonEnbled);
					selModel
							.addSelectionListener(listener_ClearSelectionButtonEnbled);

					clearSelectionButton.setEnabled(selModel
							.getSelectedFeatures().size() > 0);
				}
			}

			toolBar.add(clearSelectionButton);

			toolBar.addSeparator();

			/**
			 * Add a normal Button which opens the Chart'S print dialog
			 */
			SmallButton printChartButton = new SmallButton(new AbstractAction(
					"", Icons.ICON_PRINT_24) {

				@Override
				public void actionPerformed(ActionEvent e) {

					getSelectableChartPanel().createChartPrintJob();

				}

			});
			printChartButton.setToolTipText(AtlasViewer
					.R("AtlasChartJPanel.PrintChartButton.TT"));
			toolBar.add(printChartButton);

			/**
			 * Add a normal Button which opens the Chart's export/save dialog
			 */
			SmallButton saveChartAction = new SmallButton(new AbstractAction(
					"", Icons.ICON_SAVEAS_24) {

				@Override
				public void actionPerformed(ActionEvent e) {

					try {
						getSelectableChartPanel().doSaveAs();
					} catch (IOException e1) {
						LOGGER.info("Saving a chart to file failed", e1);
						ExceptionDialog.show(AtlasChartJPanel.this, e1);
					}

				}

			});
			saveChartAction.setToolTipText(AtlasViewer
					.R("AtlasChartJPanel.SaveChartButton.TT"));
			toolBar.add(saveChartAction);

			//
			// A JButton to open the attribute table
			//
			{
				final JButton openTable = new JButton();
				openTable.setAction(new AbstractAction(AtlasViewer
						.R("LayerToolMenu.table"),
						BasicMapLayerLegendPaneUI.ICON_TABLE) {

					@Override
					public void actionPerformed(final ActionEvent e) {
						AVDialogManager.dm_AttributeTable.getInstanceFor(
								styledLayer, AtlasChartJPanel.this,
								styledLayer, mapLegend);
					}
				});
				toolBar.addSeparator();
				toolBar.add(openTable);
			}

			/*
			 * Select/set data points button is activated by default
			 */
			getSelectableChartPanel().setWindowSelectionMode(
					WindowSelectionMode.SELECT_SET);
			setSelectionButton.setSelected(true);

		}
		return toolBar;
	}

	/**
	 * Create the simple GUI - buttons to the top, the chart into the center.
	 */
	private void initGUI() {
		setLayout(new BorderLayout());

		add(getToolBar(), BorderLayout.NORTH);
		add(getSelectableChartPanel(), BorderLayout.CENTER);

	}

	/**
	 * Reacts with an {@link SelectableChartPanel#refresh()} on every selection
	 * change.
	 */
	@Override
	public void selectionChanged(DatasetSelectionChangeEvent e) {
		if (!e.getSource().getValueIsAdjusting())
			getSelectableChartPanel().refresh();
	}

	/**
	 * Remove the listeners...
	 */
	public void dispose() {
		List<FeatureDatasetSelectionModel<?, ?, ?>> datasetSelectionModelFor = FeatureChartUtil
				.getFeatureDatasetSelectionModelFor(getSelectableChartPanel()
						.getChart());

		for (final FeatureDatasetSelectionModel selModel : datasetSelectionModelFor) {
			for (DatasetSelectionListener d : insertedListeners) {
				selModel.removeSelectionListener(d);
			}
		}
		insertedListeners.clear();
	}

	/**
	 * If {@link #mapPane} and {@link #mapLegend} are both not <code>null</code>
	 * , this method adds a {@link JButton}
	 * 
	 * @param atlasChartPanel
	 */
	public void addZoomToFeatureExtends(final SelectableXMapPane myMapPane,
			final MapLegend mapLegend,
			final StyledFeaturesInterface<?> styledFeatures,
			AtlasChartJPanel atlasChartPanel) {
		if (myMapPane != null && mapLegend != null) {

			/**
			 * Add a normal Button to zoom to the BBOX of the selected objects.
			 */
			final SmallButton zoomToSelectionButton = new SmallButton(
					new AbstractAction("",
							SelectableChartPanel.ICON_ZOOM_TO_SELECTED) {

						@Override
						public void actionPerformed(final ActionEvent e) {

							final StyledLayerSelectionModel<?> anySelectionModel = myMapPane != null ? mapLegend
									.getRememberSelection(styledFeatures
											.getId())
									: null;

							if ((anySelectionModel instanceof StyledFeatureLayerSelectionModel)) {
								final StyledFeatureLayerSelectionModel selectionModel = (StyledFeatureLayerSelectionModel) anySelectionModel;

								final Vector<String> selectionIDs = selectionModel
										.getSelection();

								try {

									/*
									 * Creating a set of FeatureIdImpl which can
									 * be used for a FidFilter
									 */
									final HashSet<Identifier> setOfIdentifiers = new HashSet<Identifier>();
									for (final String idString : selectionIDs) {
										setOfIdentifiers.add(new FeatureIdImpl(
												idString));
									}

									final FeatureCollection<SimpleFeatureType, SimpleFeature> selectedFeatures = styledFeatures
											.getFeatureSource()
											.getFeatures(
													FeatureUtil.FILTER_FACTORY2
															.id(setOfIdentifiers));
									myMapPane.zoomTo(selectedFeatures);
									myMapPane.refresh();

								} catch (final IOException e1) {
									throw new RuntimeException(
											"While zooming to the selected features.",
											e1);
								}

							}

						}

					});
			// zoomToSelectionButton.setBorder(BorderFactory
			// .createRaisedBevelBorder());
			zoomToSelectionButton
					.setToolTipText(GeotoolsGUIUtil
							.R("schmitzm.geotools.gui.SelectableFeatureTablePane.button.zoomToSelection.tt"));
			final JToolBar toolBar = atlasChartPanel.getToolBar();
			toolBar.add(zoomToSelectionButton, 6);


			{
				// Add listeners to the selection model, so we know when to
				// disable/enable the button

				// get the selectionmodel(s) of the chart
				List<FeatureDatasetSelectionModel<?, ?, ?>> datasetSelectionModelFor = FeatureChartUtil
						.getFeatureDatasetSelectionModelFor(atlasChartPanel
								.getChart());
				for (final FeatureDatasetSelectionModel selModel : datasetSelectionModelFor) {

					DatasetSelectionListener listener_ZoomToSelectionButtonEnbled = new DatasetSelectionListener() {

						@Override
						public void selectionChanged(
								DatasetSelectionChangeEvent e) {
							if (!e.getSource().getValueIsAdjusting()) {

								// Update the clearSelectionButton
								zoomToSelectionButton.setEnabled(selModel
										.getSelectedFeatures().size() > 0);
							}
						}
					};
					insertedListeners.add(listener_ZoomToSelectionButtonEnbled);
					selModel
							.addSelectionListener(listener_ZoomToSelectionButtonEnbled);

					zoomToSelectionButton.setEnabled(selModel
							.getSelectedFeatures().size() > 0);
				}
			}
		}
	}
}
