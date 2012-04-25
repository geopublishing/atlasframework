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
package org.geopublishing.atlasViewer.swing;

import java.awt.Graphics;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.image.ColorModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasViewer.ExportableLayer;
import org.geopublishing.atlasViewer.GpCoreUtil;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;
import org.geopublishing.atlasViewer.swing.internal.DnDAtlasObject;
import org.geopublishing.atlasViewer.swing.internal.DnDAtlasObject.AtlasDragSources;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;
import org.geotools.styling.ColorMap;
import org.geotools.styling.Style;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.opengis.feature.simple.SimpleFeatureType;

import de.schmitzm.geotools.MapContextManagerInterface;
import de.schmitzm.geotools.ZoomRestrictableGridInterface;
import de.schmitzm.geotools.data.amd.AttributeMetadataImpl;
import de.schmitzm.geotools.data.amd.AttributeMetadataMap;
import de.schmitzm.geotools.data.rld.RasterLegendData;
import de.schmitzm.geotools.gui.FeatureLayerFilterDialog;
import de.schmitzm.geotools.gui.GeoMapPane;
import de.schmitzm.geotools.gui.MapPaneToolBar;
import de.schmitzm.geotools.gui.MapPaneToolBar.MapPaneToolBarAction;
import de.schmitzm.geotools.gui.MapView;
import de.schmitzm.geotools.gui.SelectableXMapPane;
import de.schmitzm.geotools.gui.XMapPaneEvent;
import de.schmitzm.geotools.gui.XMapPaneTool;
import de.schmitzm.geotools.map.event.FeatureSelectedEvent;
import de.schmitzm.geotools.map.event.JMapPaneListener;
import de.schmitzm.geotools.map.event.ScaleChangedEvent;
import de.schmitzm.geotools.selection.FeatureMapLayerSelectionSynchronizer;
import de.schmitzm.geotools.selection.StyledFeatureLayerSelectionModel;
import de.schmitzm.geotools.selection.StyledLayerSelectionModel;
import de.schmitzm.geotools.selection.StyledLayerSelectionModelSynchronizer;
import de.schmitzm.geotools.styling.StyledFeatureSourceInterface;
import de.schmitzm.geotools.styling.StyledFeaturesInterface;
import de.schmitzm.geotools.styling.StyledGridCoverageInterface;
import de.schmitzm.geotools.styling.StyledGridCoverageReaderInterface;
import de.schmitzm.geotools.styling.StyledLayerInterface;
import de.schmitzm.geotools.styling.StyledLayerUtil;
import de.schmitzm.geotools.styling.StyledRasterInterface;
import de.schmitzm.swing.JPanel;
import de.schmitzm.swing.SwingUtil;

/**
 * This {@link JPanel} shows a list of expandable layer entries By implementing
 * the {@link MapContextManagerInterface}, this Object can manage the
 * {@link MapContext}
 * <hr>
 * <b>Changes by <a href="mailto:Martin.Schmitz@koeln.de">Martin Schmitz</a></b>
 * <br>
 * <ul>
 * <li>07.02.2008:<br>
 * Using {@link SimpleFeatureType#getSchema()} instead of
 * {@link SimpleFeatureType#getFeatureType()} to determine the "real" attribute
 * schema of a {@link FeatureSource}.</li>
 * <li>08.08.2007 (mark {@code ms-01}):<br>
 * Color for legend box no longer taken from {@link ColorModel}, because this
 * does not work properly. Instead the color is now taken directly from the
 * style's {@link ColorMap}.</li>
 * </ul>
 * 
 * <hr>
 * <b>Changes by SK</b><br>
 * <ul>
 * <li>08.04.2008:<br>
 * Inserting a StyledObj that is already in the MapContext will be rejected by
 * insterStyledObj(...) The StyledObj is identified by it's ID, which will be
 * the MapLayers Title
 * </ul>
 * * This class also manages the availability of the selection buttons.
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 */
public class MapLegend extends JXTaskPaneContainer implements
		MapContextManagerInterface, DropTargetListener {
	static private final Logger LOGGER = Logger.getLogger(MapLegend.class);

	private final GeoMapPane geoMapPane;

	/**
	 * Keeps track of the StyledLayerInterfaces that have been put into the
	 * MapContext. The key is the DPE ID which is also stored in the
	 * {@link MapLayer MapLayer's} title string.
	 */
	protected final HashMap<String, StyledLayerInterface<?>> rememberId2StyledLayer = new HashMap<String, StyledLayerInterface<?>>();

	/**
	 * Links a {@link MapLayer} title (which is a {@link DpLayer} ID) to a
	 * legend {@link JComponent}
	 */

	/**
	 * Links a {@link MapLayer} title (which is a {@link DpLayer} ID) to a
	 * {@link MapLayerLegend} *
	 */
	protected final HashMap<String, MapLayerLegend> rememberId2MapLayerLegend = new HashMap<String, MapLayerLegend>();

	public MapLayerLegend getLayerLegendForId(String id) {
		return rememberId2MapLayerLegend.get(id);
	}

	/**
	 * Holds a reference to the selection model of a {@link MapLayer}. Key is a
	 * MapLayer
	 */
	protected final HashMap<String, StyledLayerSelectionModel<?>> rememberSelectionModel = new HashMap<String, StyledLayerSelectionModel<?>>();

	/**
	 * Holds references to the {@link FeatureMapLayerSelectionSynchronizer}
	 * registered for each {@link MapLayer}. The key is the DPE ID.
	 */
	final protected HashMap<String, FeatureMapLayerSelectionSynchronizer> rememberMapLayerSyncronizers = new HashMap<String, FeatureMapLayerSelectionSynchronizer>();

	// ****************************************************************************
	// D'n'D stuff - it is needed.. so ignore the warning
	// ****************************************************************************
	@SuppressWarnings("unused")
	private DragSource dragSource;

	@SuppressWarnings("unused")
	private DropTarget dropTarget;

	static DataFlavor localObjectFlavor;
	static {
		try {
			localObjectFlavor = new DataFlavor(
					DataFlavor.javaJVMLocalObjectMimeType);
		} catch (ClassNotFoundException cnfe) {
			LOGGER.error(cnfe);
		}
	}

	static final DataFlavor[] supportedFlavors = { localObjectFlavor };

	/**
	 * A reference to the {@link MapPaneToolBar}.
	 */
	private final MapPaneToolBar mapPaneToolBar;

	private boolean isSelectionButtonsVisible = false;

	public static final int SEPERATOR2 = 299;
	public static final int SEPERATOR3 = 399;
	public static final int SEPERATOR4 = 499;
	public static final int SEPERATOR5 = 599;

	private boolean valuesAdjusting = false;

	/**
	 * This method is overwritten in AtlasLayerPanel.
	 * 
	 * @return <code>true</code> if the selection-related buttons shall appear.
	 */
	public boolean selectionButtonsShallAppear() {
		if (getMapPaneToolBar() == null)
			return false;
		return true;
	}

	/**
	 * Calling this function will hide or show the selection buttons. Function
	 * {@link #selectionButtonsShallAppear()} may be overwritten in subclasses.
	 */
	public void showOrHideSelectionButtons() {

		final boolean shallAppear = selectionButtonsShallAppear();

		if (shallAppear && !isSelectionButtonsVisible) {
			showSelectionButtons();
		} else if (!shallAppear && isSelectionButtonsVisible) {
			removeSelectionButtons();
		}
	}

	/**
	 * Remove selection-related buttons to the {@link MapPaneToolBar}
	 */
	public void removeSelectionButtons() {
		isSelectionButtonsVisible = false;
		if (getMapPaneToolBar() == null)
			return;

		getMapPaneToolBar().removeId(SEPERATOR2);
		getMapPaneToolBar().removeId(MapPaneToolBar.TOOL_SELECTION_SET);
		getMapPaneToolBar().removeId(MapPaneToolBar.TOOL_SELECTION_ADD);
		getMapPaneToolBar().removeId(MapPaneToolBar.TOOL_SELECTION_REMOVE);
		getMapPaneToolBar().removeId(MapPaneToolBar.TOOL_SELECTION_CLEAR);

		/**
		 * If one of these buttons has been activated, select the info tool by
		 * default
		 */
		if (getMapPaneToolBar().getSelectedTool() == MapPaneToolBar.TOOL_SELECTION_SET
				|| getMapPaneToolBar().getSelectedTool() == MapPaneToolBar.TOOL_SELECTION_ADD
				|| getMapPaneToolBar().getSelectedTool() == MapPaneToolBar.TOOL_SELECTION_REMOVE
				|| getMapPaneToolBar().getSelectedTool() == MapPaneToolBar.TOOL_SELECTION_CLEAR) {

			getMapPaneToolBar().setSelectedTool(MapPaneToolBar.TOOL_INFO);
		}

		/* Without initToolBar the changes will not be reflected */
		getMapPaneToolBar().initToolBar();
	}

	/**
	 * Add selection-related buttons to the {@link MapPaneToolBar}
	 */
	public void showSelectionButtons() {
		if (isSelectionButtonsVisible) {
			// Don't do it more than once
			return;
		}

		isSelectionButtonsVisible = true;

		if (getMapPaneToolBar() == null)
			return;

		getMapPaneToolBar().addSeparator(SEPERATOR2, new JToolBar.Separator());

		// Set Selection
		getMapPaneToolBar().addTool(
				new MapPaneToolBarAction(MapPaneToolBar.TOOL_SELECTION_SET,
						getMapPaneToolBar(), XMapPaneTool.SELECTION_SET) {
					@Override
					public void actionPerformed(ActionEvent e) {
						super.actionPerformed(e);
						getGeoMapPane().getMapPane().setTool(
								XMapPaneTool.SELECTION_SET);
					}

				}, false);

		// Add Selection
		getMapPaneToolBar().addTool(
				new MapPaneToolBarAction(MapPaneToolBar.TOOL_SELECTION_ADD,
						getMapPaneToolBar(), XMapPaneTool.SELECTION_ADD) {
					@Override
					public void actionPerformed(ActionEvent e) {
						super.actionPerformed(e);
						getGeoMapPane().getMapPane().setTool(
								XMapPaneTool.SELECTION_ADD);
					}
				}, false);

		// Remove Selection
		getMapPaneToolBar().addTool(
				new MapPaneToolBar.MapPaneToolBarAction(
						MapPaneToolBar.TOOL_SELECTION_REMOVE,
						getMapPaneToolBar(), XMapPaneTool.SELECTION_REMOVE) {
					@Override
					public void actionPerformed(ActionEvent e) {
						super.actionPerformed(e);
						getGeoMapPane().getMapPane().setTool(
								XMapPaneTool.SELECTION_REMOVE);
					}
				}, false);

		// ResetSelection
		getMapPaneToolBar()
				.addAction(
						new MapPaneToolBarAction(
								MapPaneToolBar.TOOL_SELECTION_CLEAR,
								getMapPaneToolBar(),
								"",
								new ImageIcon(
										MapView.class
												.getResource("resource/icons/selection_clear.png")),
								MapPaneToolBar
										.R("MapPaneButtons.Selection.ClearSelection.TT")) {
							@Override
							public void actionPerformed(ActionEvent e) {

								super.actionPerformed(e);

								// At the moment we clear all selection models..
								// an alternative would be to only clear the
								// selection models that are visible at the
								// moment.
								for (StyledLayerSelectionModel<?> sModel : rememberSelectionModel
										.values()) {
									sModel.clearSelection();
								}

							}

						}, true); // Look.. this true is important - otherwise
		// the MapPaneButtons are not recreated
	}

	/**
	 * This panel is backed by a {@link JXTaskPaneContainer} and shows the
	 * {@link MapLayerLegend}s for all {@link MapLayer}s in the
	 * {@link MapContext}
	 * 
	 * @param geoMapPane
	 *            The {@link GeoMapPane} that this legend is created for.
	 * @param mapPaneToolBar
	 *            A reference to the {@link MapPaneToolBar}
	 */
	public MapLegend(final GeoMapPane geoMapPane, MapPaneToolBar mapPaneToolBar) {

		this.geoMapPane = geoMapPane;
		this.mapPaneToolBar = mapPaneToolBar;

		// ****************************************************************************
		// This Listener will recreate the legend when the Zoom of the map
		// changes.
		// ****************************************************************************
		geoMapPane.getMapPane().addMapPaneListener(new JMapPaneListener() {

			// https://trac.wikisquare.de/gp/ticket/65 (Beim öffnen der Legende
			// einer Karte werden alle Styles aufgelistet, nicht die für die
			// zoomstufe relevanten)
			boolean first = true;

			@Override
			public void performMapPaneEvent(XMapPaneEvent e) {

				if (e instanceof ScaleChangedEvent) {
					ScaleChangedEvent sce = (ScaleChangedEvent) e;

					// Iterate over all layer-ids
					for (StyledLayerInterface<?> sl : rememberId2StyledLayer
							.values()) {

						if (first
								|| StyledLayerUtil.hasScalechangeAnyEffect(
										sl.getStyle(),
										sce.getOldScaleDenominator(),
										sce.getNewScaleDenominator())) {
							rememberId2MapLayerLegend.remove(sl.getId());
						}
					}

					first = false;
					recreateLayerList();
				}
			}

		});

		// ****************************************************************************
		// This Listener will visually mark the Layer where features were
		// selected. Typically the ClickInfoPanel will be shown in parallel.
		// ****************************************************************************
		geoMapPane.getMapPane().addMapPaneListener(new JMapPaneListener() {

			private MapLayerLegend lastSpecialGroup;

			@Override
			public void performMapPaneEvent(XMapPaneEvent e) {

				if (e instanceof FeatureSelectedEvent) {
					FeatureSelectedEvent fse = (FeatureSelectedEvent) e;
					MapLayerLegend layerLegend = rememberId2MapLayerLegend
							.get(fse.getSourceLayer().getTitle());

					if (layerLegend == null)
						return;

					if (lastSpecialGroup != null)
						lastSpecialGroup.setSpecial(false);
					lastSpecialGroup = layerLegend;

					layerLegend.setSpecial(true);
					layerLegend.getParent().repaint();
				}
			}

		});

		// ****************************************************************************
		// This Listener reacts to the FilterDialog
		// ****************************************************************************
		geoMapPane.getMapPane().addMapPaneListener(new JMapPaneListener() {

			@Override
			public void performMapPaneEvent(XMapPaneEvent e) {
				if (e instanceof FeatureSelectedEvent) {
					FeatureSelectedEvent fse = (FeatureSelectedEvent) e;
					if (fse.getSourceObject() instanceof FeatureLayerFilterDialog) {
						FeatureLayerFilterDialog fDialog = (FeatureLayerFilterDialog) fse
								.getSourceObject();
						org.opengis.filter.Filter filter = fDialog.getFilter();

						// There was BUG IN GT.. one had to compare with
						// toString otherwise it return true too often
						// but a BugFix is underway...
						if (fDialog.getFilter().equals(
								fDialog.getMapLayer().getQuery().getFilter())) {
							LOGGER.debug("Not reacting to this Filter change, because the filters are equal.");
							return;
						}

						// The changed filter has to be set into the
						// StyledFeaturesInterface, especially in AtlasStyler
						// this was missing
						StyledLayerInterface<?> styledLayerInterface = rememberId2StyledLayer
								.get(fDialog.getMapLayer().getTitle());
						if (styledLayerInterface instanceof StyledFeaturesInterface<?>) {
							StyledFeaturesInterface<?> sf = (StyledFeaturesInterface<?>) styledLayerInterface;
							sf.setFilter(filter);
						}

						String typeName = fDialog.getFilterPanel()
								.getFeatureType().getTypeName();

						// Will trigger XMapPane repaint
						fDialog.getMapLayer().setQuery(
								new DefaultQuery(typeName, filter));

						if (!isValuesAdjusting())
							recreateLayerList(fDialog.getMapLayer());
					}
				}

			}
		});

		dropTarget = new DropTarget(this, this);

		// ****************************************************************************
		// When MapLayers are added/changed in the managed map-context, we
		// re-create the list
		// ****************************************************************************

		if (getMapContext() != null)

			getMapContext().addMapLayerListListener(new MapLayerListListener() {

				@Override
				public void layerAdded(MapLayerListEvent event) {
					// LOGGER.debug("layerAdded MapLayerListListener");
					recreateLayerList();
				}

				@Override
				public void layerChanged(MapLayerListEvent event) {
					// i suppose we don't need to update when only the
					// layer's content is changed
					// LOGGER.debug("layerChanged");
					// recreateLayerList();
				}

				@Override
				public void layerMoved(MapLayerListEvent event) {
					// LOGGER.debug("layerMoved MapLayerListListener");

					// Recreate Legend with the new order
					recreateLayerList();
				}

				@Override
				public void layerRemoved(MapLayerListEvent event) {

					// LOGGER.debug("layerRemoved MapLayerListListener");

					MapLayer removedLayer = event.getLayer();

					// Removes the Selection Synchronizers. If removedLayer ==
					// null is understood as removing ALL layers.
					unregisterStyledLayer(removedLayer);

					// Recreate the Legend Panel
					if (removedLayer != null)
						recreateLayerList(removedLayer);
					else {
						rememberId2MapLayerLegend.clear();
						recreateLayerList();
					}
				}

				// @Override
				// public void layerPreDispose(MapLayerListEvent event) {
				// // TODO Auto-generated method stub
				//
				// }
			});

		setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
	}

	/**
	 * @return The {@link MapContext} managed by this
	 *         {@link MapContextManagerInterface}
	 */
	@Override
	public MapContext getMapContext() {
		return geoMapPane.getMapContext();
	}

	/**
	 * Insert an instance of {@link StyledLayerInterface} into the
	 * {@link MapContext}<br>
	 * A listener will call {@link #recreateLayerList()} to rebuild the gui.<br>
	 * If the Layer already exists, it will not be inserted an false is
	 * returned.
	 * 
	 * @param styledObj
	 *            The {@link StyledLayerInterface} to insert.
	 * @param idx
	 *            in mapContext order (bottom first), -1 means to insert the
	 *            layer topmost.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	@Override
	public boolean insertStyledLayer(StyledLayerInterface<?> styledObj, int idx) {
		if (styledObj == null) {
			LOGGER.warn("styledObj ist null? return false");
			return false;
		}
		// LOGGER.debug("insertStyledLayer " + styledObj.getTitle());

		/**
		 * Lets check, if there exists a Maplayer in the MapContext that has the
		 * same Title. If so, then we will NOT include this StyledObj again and
		 * return false
		 */
		MapLayer[] layers = getGeoMapPane().getMapContext().getLayers();
		for (MapLayer ml : layers) {
			if (ml.getTitle().equals(styledObj.getId())) {
				AVSwingUtil.showMessageDialog(this, GpCoreUtil.R(
						"MapLegend.InsertLayer.LayerAlreadyInContext",
						styledObj.getTitle()));
				return false;
			}
		}

		MapLayer mapLayer = null;
		StyledLayerSelectionModel<?> selectionModel = null;
		FeatureMapLayerSelectionSynchronizer mapSelectionSyncronizer = null;

		try {

			// ****************************************************************************
			// Determine what kind of styledLayer we have
			// ****************************************************************************
			if (styledObj instanceof StyledGridCoverageReaderInterface) {
				StyledGridCoverageReaderInterface styledGridReader = (StyledGridCoverageReaderInterface) styledObj;

				mapLayer = new AtlasMapLayer(styledGridReader.getGeoObject(),
						styledGridReader.getStyle());

			} else if (styledObj instanceof StyledGridCoverageInterface) {
				StyledGridCoverageInterface styledGrid = (StyledGridCoverageInterface) styledObj;

				mapLayer = new AtlasMapLayer(styledGrid.getGeoObject(),
						styledGrid.getStyle());

			} else if (styledObj instanceof StyledFeaturesInterface) {
				StyledFeaturesInterface<?> styledFS = (StyledFeaturesInterface<?>) styledObj;
				Style style = styledFS.getStyle();
				if (style == null) {
					// LOGGER.debug("Style was null, using default ");
					style = ASUtil.createDefaultStyle(styledFS);
				}
				mapLayer = new AtlasMapLayer(styledFS.getFeatureSource(), style);

				/**
				 * In case that we have a DpLayerVectorFeatureSource, we might
				 * have a FilterRule associated with the Layer
				 */
				if (styledFS instanceof DpLayerVectorFeatureSource) {
					DpLayerVectorFeatureSource dpLayerVectorFeatureSource = ((DpLayerVectorFeatureSource) styledFS);

					if (!dpLayerVectorFeatureSource.isBroken()) {

						DefaultQuery query = new DefaultQuery(
								dpLayerVectorFeatureSource.getTypeName()
										.getLocalPart(),
								dpLayerVectorFeatureSource.getFilter());

						mapLayer.setQuery(query);
					}
				}

				// SelectionModel management is provided for vector layer
				selectionModel = new StyledFeatureLayerSelectionModel(styledFS);

				mapSelectionSyncronizer = createMapSynchronizer(mapLayer,
						selectionModel, styledFS);

			}

			// Check for failure
			if ((mapLayer == null)) {
				LOGGER.error("A MapLayer instance could not be created for this StyledLayerInterface with Title "
						+ styledObj.getTitle());
				return false;
			}

			/**
			 * The ID is used in the ClickInfoPanl t and many other positions to
			 * find the AttributeColumnMetaData
			 */
			mapLayer.setTitle(styledObj.getId());

			rememberId2StyledLayer.put(styledObj.getId(), styledObj);

			// Adding the Layer to the mapContext, listeners will do the rest
			boolean b;
			if (idx < 0)
				b = getGeoMapPane().getMapContext().addLayer(mapLayer);
			else
				b = getGeoMapPane().getMapContext().addLayer(idx, mapLayer);
			if (!b) {
				// Return false if not successful
				return false;
			}

			recreateLayerList(styledObj.getId());

			/**
			 * We have a selection Model...
			 */
			if (selectionModel != null) {
				rememberSelectionModel.put(mapLayer.getTitle(), selectionModel);

				if (mapSelectionSyncronizer != null) {
					/**
					 * We have a Synchronizer that links the MapLayer with the
					 * SelectionModel. We register it here. When the layer is
					 * removed, we remove the Synchronizer again.
					 */
					getGeoMapPane().getMapPane().addMapPaneListener(
							mapSelectionSyncronizer);
					selectionModel
							.addSelectionListener((StyledLayerSelectionModelSynchronizer) mapSelectionSyncronizer);

					// Here we remember the syncronizer so that we can cleanly
					// unregister it when the layer is removed later.
					rememberMapLayerSyncronizers.put(mapLayer.getTitle(),
							mapSelectionSyncronizer);
				}
			}

			return true;

		} catch (Exception e) {
			LOGGER.error("Could not insert the layer", e);
			return false;
		}
	}

	/**
	 * Creates a {@link FeatureMapLayerSelectionSynchronizer} for a
	 * {@link StyledFeatureSourceInterface}
	 */
	protected FeatureMapLayerSelectionSynchronizer createMapSynchronizer(
			MapLayer mapLayer, StyledLayerSelectionModel<?> selectionModel,
			StyledFeaturesInterface<?> styledFS) {
		FeatureMapLayerSelectionSynchronizer mapSelectionSyncronizer = new FeatureMapLayerSelectionSynchronizer(
				(StyledFeatureLayerSelectionModel) selectionModel, styledFS,
				mapLayer, getGeoMapPane().getMapPane(), getMapPaneToolBar());
		return mapSelectionSyncronizer;
	}

	/**
	 * @return the MapLayer associated with the given id
	 */
	public MapLayer getMapLayerFor(String id) {
		for (MapLayer mapLayer : getGeoMapPane().getMapContext().getLayers()) {

			if (mapLayer.getTitle().equals(id)) {
				return mapLayer;
			}
		}

		return null;
	}

	/**
	 * {@link #recreateLayerList()} works on cached
	 * {@link #rememberId2MapLayerLegend} only. This method removes the cached
	 * legend for this layer before calling it.<br/>
	 * Calling this method invokes a call to JMapPane.refresh()
	 * 
	 * @param mapLayer
	 *            The {@link MapLayer} to recreate from scratch.
	 */
	public void recreateLayerList(MapLayer mapLayer) {
		recreateLayerList(mapLayer.getTitle());
	}

	/**
	 * {@link #recreateLayerList()} works on cached
	 * {@link #rememberId2MapLayerLegend} only. This method removes the cached
	 * legend for this layer before calling it.<br/>
	 * 
	 * @param id
	 *            The {@link MapLayer} ID to recreate from scratch.
	 * 
	 */
	public void recreateLayerList(String id) {
		rememberId2MapLayerLegend.remove(id);
		recreateLayerList();
	}

	/**
	 * Recreates the whole {@link MapLegend} according to the {@link MapLayer}s
	 * in the {@link MapContext}. Moving, inserting etc. is done in the
	 * {@link MapContext}. Here we update the GUI only. Changes to the
	 * Styles/Rules are not recognized unless recreateLegendPanel is called. <br/>
	 * If uses the {@link #rememberId2MapLayerLegend} cache.
	 */
	public void recreateLayerList() {

		if (isValuesAdjusting())
			return;

		removeAll();

		// Reverse, it because the GUI has the order top-first.
		final MapLayer[] layers = getGeoMapPane().getMapContext().getLayers();
		Collections.reverse(Arrays.asList(layers));

		double bestRes = Double.MAX_VALUE;

		for (MapLayer mapLayer : layers) {

			// Is a LayerPaneGroup Component already existing for this Layer?
			if (!rememberId2MapLayerLegend.containsKey(mapLayer.getTitle())) {

				/**
				 * If the Maplayer has been inserted via public boolean
				 * #{insertStyledLayer}, then the accoring StyledLayerInterface
				 * can be "remembered" .
				 */
				StyledLayerInterface<?> styledObj = rememberId2StyledLayer
						.get(mapLayer.getTitle());

				if (styledObj == null) {
					// Layers like "maximal map extend" are not layers that
					// should be part of the legend...
					continue;
				}

				// ****************************************************************************
				// Check if the styledObj is Exportable.. If so, pass it to the
				// LayerPaneGroup
				// ****************************************************************************
				ExportableLayer exportable = null;
				if (styledObj instanceof ExportableLayer) {
					exportable = ((ExportableLayer) styledObj);
				}

				/**
				 * Creating a MapLayerLegend by calling createLayerGroup.. this
				 * method might be overridden by DesignMapLegend to return
				 * DesignMapLayerLegends
				 */
				final MapLayerLegend mapLayerLegend = createMapLayerLegend(
						mapLayer, exportable, styledObj, this);

				if (mapLayerLegend == null) {
					// For "hidden in atlas legend" layers null is returned.
					// we continue with the next maplayer
					continue;
				}

				// ****************************************************************************
				// Remember the just created MapLayerLegend
				// ****************************************************************************
				rememberId2MapLayerLegend.put(mapLayer.getTitle(),
						mapLayerLegend);

				// ****************************************************************************
				// Additionally checking for the raster with the highest
				// resolution and setting
				// it as the maxZoomScale of the geotools JMapPane
				// ****************************************************************************
				if (styledObj instanceof ZoomRestrictableGridInterface) {
					final Double layerMaxRes = ((ZoomRestrictableGridInterface) styledObj)
							.getMaxResolution();

					if ((layerMaxRes != null)
							&& (layerMaxRes != Double.MAX_VALUE)
							&& (layerMaxRes != Double.MIN_VALUE)
							&& (layerMaxRes != Double.MIN_NORMAL)
							&& (layerMaxRes != 0) && (layerMaxRes < bestRes)) {
						bestRes = layerMaxRes;
					}
				}

			}

			// No we definitely find a MapLayerLegend in the cache...
			MapLayerLegend mapLayerLegend = rememberId2MapLayerLegend
					.get(mapLayer.getTitle());

			if ((bestRes != Double.MAX_VALUE) && (bestRes != Double.MIN_VALUE)) {
				LOGGER.debug("Setting maxZoomScale to " + bestRes / 512.);
				getGeoMapPane().getMapPane().setMaxZoomScale(bestRes / 512.);
			} else {
				getGeoMapPane().getMapPane().setMaxZoomScale(null);
			}

			mapLayerLegend.setTitle(getTitleFor(mapLayer));
			mapLayerLegend.setSpecial(false);
			add(mapLayerLegend);

		} // for (MapLayer mapLayer : layers )

		// Only when isAdjusting == false, is this method called. So we can
		// repaint all now...
		repaintMapAndTheGUI();
	}

	/**
	 * If set to <code>false</code>, this map and GUI are not updated
	 * automatically while changing the map. Is set to <code>true</code>, and it
	 * was <code>false</code> before, the {@link #repaintMapAndTheGUI()} is
	 * called.
	 * 
	 * @param valuesAdjusting
	 *            FalseFileFilter
	 */
	public void setValuesAdjusting(boolean valuesAdjusting) {
		if (valuesAdjusting != this.valuesAdjusting && valuesAdjusting == false) {
			this.valuesAdjusting = valuesAdjusting;
			recreateLayerList();
		} else {
			this.valuesAdjusting = valuesAdjusting;
		}

	}

	/**
	 * @return <code>true</code>, when the legend is beeing changed
	 *         programatically and should not be update the GUI yet.
	 */
	public boolean isValuesAdjusting() {
		return this.valuesAdjusting;
	}

	/**
	 * Triggers a repaint of the {@link SelectableXMapPane}. @see
	 * {@link #setValuesAdjusting(boolean)}.
	 */
	private void repaintMapAndTheGUI() {

		// LOGGER.debug("repaintMapAndTheGUI");

		if (!SwingUtilities.isEventDispatchThread()) {

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					revalidate();
					repaint();
					getGeoMapPane().refreshMap();
				}
			});
		} else {
			SwingUtil.checkOnEDT();
			revalidate();
			repaint();
			getGeoMapPane().refreshMap();
		}

	}

	protected String getTitleFor(StyledLayerInterface<?> styledObj) {
		return styledObj.getTitle().toString();
	}

	/**
	 * @param mapLayer
	 *            The maplayer presented by this {@link MapLayerLegend}
	 * @param exportable
	 *            <code>null</code> or instance of {@link ExportableLayer} if
	 *            the layer can be exported
	 * @param styledObj
	 *            the {@link StyledLayerInterface} object that is presented by
	 *            this {@link MapLayerLegend}
	 * @param layerPanel
	 *            The parent {@link MapLegend} or {@link DesignAtlasMapLegend}
	 * 
	 * @return <code>null</code> if no legend should be visible for this layer.
	 *         Generally constructs a {@link MapLayerLegend} with the given
	 *         parameters. This method may be overwritten by sub classes.
	 */
	protected MapLayerLegend createMapLayerLegend(MapLayer mapLayer,
			ExportableLayer exportable, StyledLayerInterface<?> styledObj,
			MapLegend layerPanel) {

		return new MapLayerLegend(mapLayer, exportable, styledObj, this);
	}

	/**
	 * Is never called :-(
	 */
	@Override
	public boolean removeStyledLayer(int mapContextIdx) {

		final MapLayer removedLayer = getGeoMapPane().getMapContext()
				.removeLayer(mapContextIdx);

		if (removedLayer == null)
			return false;

		unregisterStyledLayer(removedLayer);

		return true;
	}

	/**
	 * This {@link MapLegend} class manages all layers of a map and connects
	 * them with their selectionModels. When a layer is removed, we have to
	 * unregister all the Syncronizers. Also any open dialog is closed.
	 * 
	 * TODO FilterDialog have to be closed.
	 * 
	 * @param removedLayer
	 *            The {@link MapLayer} that has been removed.
	 */
	protected void unregisterStyledLayer(final MapLayer removedLayer) {

		if (removedLayer != null) {

			/**
			 * Closing dialogs associated with the layer
			 */
			{
				StyledLayerInterface<?> key = rememberId2StyledLayer
						.get(removedLayer.getTitle());
				AVDialogManager.dm_Styler.disposeInstanceFor(key);
			}

			/**
			 * Removing it from the selection system
			 */

			String id = removedLayer.getTitle();

			StyledLayerSelectionModel<?> removedSelectionModel = rememberSelectionModel
					.remove(id);
			if (removedSelectionModel != null) {
				FeatureMapLayerSelectionSynchronizer removedMapLayerSynchronizer = rememberMapLayerSyncronizers
						.remove(id);

				if (removedMapLayerSynchronizer != null) {
					getGeoMapPane().getMapPane().removeMapPaneListener(
							removedMapLayerSynchronizer);
					removedSelectionModel
							.removeSelectionListener((StyledLayerSelectionModelSynchronizer) removedMapLayerSynchronizer);
				}
			}

			final StyledLayerInterface<?> styledLayerInterface = rememberId2StyledLayer
					.get(id);

			if (styledLayerInterface instanceof StyledFeaturesInterface<?>) {
				final StyledFeaturesInterface<?> styledFeaturesInterface = (StyledFeaturesInterface<?>) styledLayerInterface;
				AVDialogManager.dm_AttributeTable
						.disposeInstanceFor(styledFeaturesInterface);

			}

			/**
			 * Removing it from the cache
			 */
			rememberId2MapLayerLegend.remove(id);
			rememberId2StyledLayer.remove(id);
		} else {
			// if removed Layer == null, it typically means that ALL layers have
			// been removed. see DefaultmapContext.clearLayerList
			// Remove all Synchronizers from all SelectionModels
			for (StyledLayerSelectionModel sm : rememberSelectionModel.values()) {
				for (FeatureMapLayerSelectionSynchronizer sync : rememberMapLayerSyncronizers
						.values()) {
					if (sm.removeSelectionListener(sync) == true) {
						// LOGGER.debug("removed a FeatureMapLayerSelectionSynchronizer listener from the selection model");
					}
					getGeoMapPane().getMapPane().removeMapPaneListener(sync);
				}
			}
			rememberSelectionModel.clear();
			rememberMapLayerSyncronizers.clear();
		}
	}

	/**
	 * Inserts a StyledLayerInterface Layer top-most by calling
	 * insertStyledLayer If the Layer already exists, it will not be inserted an
	 * false is returned
	 */
	@Override
	public boolean addStyledLayer(StyledLayerInterface<?> styledObj) {
		return insertStyledLayer(styledObj, -1);

	}

	/**
	 * Helpt the GarbageCollection...
	 */
	@Override
	public void dispose() {
		rememberId2MapLayerLegend.clear();

		rememberId2StyledLayer.clear();

		rememberMapLayerSyncronizers.clear();
		rememberSelectionModel.clear();
	}

	// ****************************************************************************
	//
	// D'n'Drop-Target stuff
	//
	// ****************************************************************************

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
	 */
	@Override
	public void dragExit(DropTargetEvent dte) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent
	 * )
	 */
	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent
	 * )
	 */
	@Override
	public void dragOver(DropTargetDragEvent dtde) {
	}

	/**
	 * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
	 *      TODO Move to its own DropTargetListener class?!
	 */
	@Override
	public void drop(DropTargetDropEvent dtde) {
		// LOGGER.debug("An object has been droppen onto the LayerPanel :"+dtde.getTransferable());
		Object dragged;
		try {
			dtde.getCurrentDataFlavors();
			dragged = dtde.getTransferable().getTransferData(localObjectFlavor);
		} catch (Exception e) {
			dtde.dropComplete(false);
			return;
		}
		if ((dragged instanceof DnDAtlasObject)) {
			DnDAtlasObject transObj = (DnDAtlasObject) dragged;

			if ((transObj.getSource() == AtlasDragSources.DATAPOOLLIST)) {
				// ****************************************************************************
				// This Drop is a new Layer in the Layerlist
				// ****************************************************************************
				LOGGER.debug(" inserting a new layer that has been dropped on the LayerPanel");

				if (transObj.getObject() instanceof StyledLayerInterface<?>) {
					StyledLayerInterface<?> styledObj = (StyledLayerInterface<?>) transObj
							.getObject();

					dtde.dropComplete(
					// layerManager.addStyledLayer(styledObj)
					addStyledLayer(styledObj));
					return;
				}
			}

			if ((transObj.getSource() == AtlasDragSources.DNDTREE)) {
				// ****************************************************************************
				// This Drop is a new Layer in the Layerlist
				// ****************************************************************************
				LOGGER.debug("inserting a new layer from DnDTree that has been dropped on the LayerPanel");

				if (transObj.getObject() instanceof StyledLayerInterface<?>) {
					StyledLayerInterface<?> styledObj = (StyledLayerInterface<?>) transObj
							.getObject();

					dtde.dropComplete(addStyledLayer(styledObj));
					return;
				}
			}
		}

		dtde.dropComplete(false);
		return;
	}

	/**
	 * @see java.awt.dnd.DropTargetListener#dropActionChanged(java.awt.dnd.
	 *      DropTargetDragEvent )
	 */
	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	// ****************************************************************************
	//
	// Provide access to MapLayerListeners of the MapContext
	//
	// ****************************************************************************
	/**
	 * Register interest in receiving a {@link LayerListEvent}. A
	 * <code>LayerListEvent</code> is sent if a layer is added or removed, but
	 * not if the data within a layer changes.
	 * 
	 * @param listener
	 *            The object to notify when Layers have changed.
	 */
	@Override
	public void addMapLayerListListener(MapLayerListListener listener) {
		getGeoMapPane().getMapContext().addMapLayerListListener(listener);
	}

	/**
	 * Remove interest in receiving {@link LayerListEvent}.
	 * 
	 * @param listener
	 *            The object to stop sending <code>LayerListEvent</code>s.
	 */
	@Override
	public void removeMapLayerListListener(MapLayerListListener listener) {
		getGeoMapPane().getMapContext().removeMapLayerListListener(listener);
	}

	@Override
	public List<StyledLayerInterface<?>> getStyledObjects() {
		final LinkedList<StyledLayerInterface<?>> styledObjects = new LinkedList<StyledLayerInterface<?>>();
		for (final MapLayer mapLayer : getGeoMapPane().getMapContext()
				.getLayers()) {
			final StyledLayerInterface<?> styledO = rememberId2StyledLayer
					.get(mapLayer.getTitle());
			styledObjects.add(styledO);
		}
		return styledObjects;
	}

	/**
	 * @see skrueger.geotools.MapContextManagerInterface#getVisibleAttribsFor(org
	 *      .geotools.map.MapLayer)
	 */
	@Override
	public List<AttributeMetadataImpl> getVisibleAttribsFor(MapLayer layer) {
		final StyledFeatureSourceInterface styledFC = (StyledFeatureSourceInterface) rememberId2StyledLayer
				.get(layer.getTitle());

		if (styledFC == null) {
			// Some MapLayers, like the one showing the maxExtendBBOX in
			// Geopublisher, are not DPLayers or StyledLayers.. So if it's null
			// here, thats ok and we can return an emtpy list.
			return new ArrayList<AttributeMetadataImpl>();
		}

		final AttributeMetadataMap attributeMetaDataMap = styledFC
				.getAttributeMetaDataMap();

		return attributeMetaDataMap.sortedValuesVisibleOnly();
	}

	/**
	 * Returns a localized title for this layer.
	 */
	@Override
	public String getTitleFor(MapLayer layer) {
		StyledLayerInterface<?> StyledLayerInterface = rememberId2StyledLayer
				.get(layer.getTitle());
		if (StyledLayerInterface == null)
			return null;
		return StyledLayerInterface.getTitle().toString();
	}

	/**
	 * Retunes a localized Description-String for this layer
	 */
	@Override
	public String getDescFor(MapLayer layer) {
		StyledLayerInterface<?> StyledLayerInterface = rememberId2StyledLayer
				.get(layer.getTitle());
		if (StyledLayerInterface == null)
			return null;
		return StyledLayerInterface.getDesc().toString();
	}

	@Override
	public RasterLegendData getLegendMetaData(MapLayer layer) {
		StyledLayerInterface<?> styledObj = rememberId2StyledLayer.get(layer
				.getTitle());
		if (styledObj == null)
			return null;
		if (styledObj instanceof StyledRasterInterface) {
			return ((StyledRasterInterface<?>) styledObj).getLegendMetaData();
		}
		return null;
	}

	@Override
	public StyledLayerInterface<?> getStyledObjectFor(MapLayer layer) {
		return rememberId2StyledLayer.get(layer.getTitle());
	}

	public StyledLayerSelectionModel<?> getRememberSelection(String id) {
		return rememberSelectionModel.get(id);
	}

	public MapPaneToolBar getMapPaneToolBar() {
		return mapPaneToolBar;
	}

	public GeoMapPane getGeoMapPane() {
		return geoMapPane;
	}

	@Override
	public void paint(Graphics g) {
		if (getGeoMapPane().getMapContext().getLayers().length == 0) {
			g.drawString(GpCoreUtil.R("MapLegend.IsEmptyMsg"), 2, 20);
		} else
			super.paint(g);
	}

}
