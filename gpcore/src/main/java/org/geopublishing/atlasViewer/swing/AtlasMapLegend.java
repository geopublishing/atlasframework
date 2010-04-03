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
package org.geopublishing.atlasViewer.swing;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.ExportableLayer;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.DpRef;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVector;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;
import org.geopublishing.atlasViewer.dp.layer.LayerStyle;
import org.geopublishing.atlasViewer.map.AtlasLabelSearch;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.resource.icons.Icons;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.styling.Style;
import org.geotools.styling.TextSymbolizer;

import schmitzm.geotools.gui.GeoMapPane;
import schmitzm.geotools.styling.StylingUtil;
import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.jfree.feature.style.FeatureChartStyle;
import schmitzm.swing.ExceptionDialog;
import skrueger.geotools.AttributeMetadataMap;
import skrueger.geotools.MapPaneToolBar;
import skrueger.geotools.StyledFeaturesInterface;
import skrueger.geotools.StyledLayerInterface;
import skrueger.geotools.MapPaneToolBar.MapPaneToolBarAction;
import skrueger.geotools.labelsearch.LabelSearch;
import skrueger.geotools.labelsearch.SearchMapDialog;
import skrueger.geotools.selection.StyledLayerSelectionModel;
import skrueger.i8n.I8NUtil;

/**
 * This extends the {@link MapLegend}. Adding functionality to it, that only
 * makes sense when we are really working with an {@link AtlasConfig}. The
 * {@link AtlasStyler} SLD editor for example, uses {@link MapLegend} because it
 * doesn't know anything about any {@link AtlasConfig}.<br>
 * One special function for example are the additional/multiple {@link Style}s
 * for one {@link StyledLayerInterface}.
 * 
 * @author SK
 * 
 */
public class AtlasMapLegend extends MapLegend {
	static final private Logger LOGGER = ASUtil
			.createLogger(AtlasMapLegend.class);

	private final Map map;

	private final AtlasConfig ac;

	/** Are there charts available for this {@link MapContext}? **/
	private boolean isChartButtonVisible = false;

	/**
	 * Are there visible layers with {@link TextSymbolizer}s available for this
	 * {@link MapContext}?
	 **/
	private boolean isSearchButtonVisible = false;

	public AtlasMapLegend(GeoMapPane geoMapPane, Map map, AtlasConfig ac,
			MapPaneToolBar mapPaneToolBar) {

		super(geoMapPane, mapPaneToolBar);

		this.map = map;
		this.ac = ac;
	}
	
	/**
	 * One task of this is method, to check whether the layer is marked as
	 * <code>hideInLegend</code> or <code>minimizeInLegend</code>. This
	 * information is only stored in the {@link DpRef} objects of the map.
	 * 
	 * @param gmp
	 *            The {@link GeoMapPane} that the legend is working on
	 * @param mapLayer
	 *            The maplayer presented by this {@link MapLayerLegend}
	 * @param exportable
	 *            <code>null</code> or instance of {@link ExportableLayer} if
	 *            the layer can be exported
	 * @param styledObj
	 *            the {@link StyledLayerInterface} object that is presented by
	 *            this {@link MapLayerLegend}
	 * @param mapLegend
	 *            The parent {@link MapLegend} or {@link DesignAtlasMapLegend}
	 * 
	 * @return Generally constructs a {@link MapLayerLegend} with the given
	 *         parameters. This method is overridden by
	 *         {@link DesignAtlasMapLegend} to create DesignLayerPaneGroup
	 *         objects.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	@Override
	protected MapLayerLegend createMapLayerLegend(MapLayer mapLayer,
			ExportableLayer exportable, StyledLayerInterface<?> styledObj,
			MapLegend mapLegend) {
		String id = styledObj.getId();

		DpLayer<?, ChartStyle> dpLayer = (DpLayer<?, ChartStyle>) ac
				.getDataPool().get(id);

		Boolean hidden = getMap().getHideInLegendMap().get(dpLayer.getId());
		if (hidden != null && hidden == true) {
			return null;
		}

		return new AtlasMapLayerLegend(mapLayer, exportable,
				(AtlasMapLegend) mapLegend, dpLayer, getMap());
	}

	@Override
	protected String getTitleFor(StyledLayerInterface<?> styledObj) {
		ArrayList<String> availableStyles = getMap().getAdditionalStyles().get(
				styledObj.getId());
		if (availableStyles.size() == 1) {
			DpLayerVectorFeatureSource dplv = (DpLayerVectorFeatureSource) styledObj;
			LayerStyle onlyAddStyle = dplv.getLayerStyleByID(availableStyles
					.get(0));
			return onlyAddStyle.getTitle().toString();
		}
		return super.getTitleFor(styledObj);
	}
//
//	/**
//	 * This method overrides the updateLegendIcon method in {@link MapLegend}.
//	 * This difference in this method is, that there can actually exist multiple
//	 * styles for one layer.
//	 * 
//	 * @param styledObj
//	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
//	 *         Kr&uuml;ger</a>
//	 */
//	@Override
//	public void updateLegendIcon(StyledLayerInterface<?> styledObj) {
//
//		final String id = styledObj.getId();
//
//		java.util.Map<String, ArrayList<String>> additionalStyles = getMap()
//				.getAdditionalStyles();
//		final ArrayList<String> availableStyles = additionalStyles.get(id);
//
//		if (availableStyles == null || availableStyles.size() == 0) {
//			super.updateLegendIcon(styledObj);
//			return;
//		}
//
//		/**
//		 * Once we are here, we can be sure that we do have at least one
//		 * additional style!<br/>
//		 * If there is only one additional (active) style for this layer (in
//		 * this map), we do not render a JTabbedPane. The Title of the only
//		 * additional style is not displayed. If there are more than 5
//		 * additional styles, we render a DropDown component.
//		 */
//
//		Component legendPanel = createAdditionalStylesPane(getMapLayerFor(id),
//				availableStyles, (DpLayer) styledObj);
//		// rememberLegend.put(styledObj.getId(), legendPanel);
//		rememberId2MapLayerLegend.remove(styledObj.getId());
//
//	}

	/**
	 * Returns a localized title for this layer. If the layer supports
	 * additional styles take care of this.
	 */
	@Override
	public String getTitleFor(MapLayer layer) {
		StyledLayerInterface<?> StyledLayerInterface = rememberId2StyledLayer
				.get(layer.getTitle());

		if (!(StyledLayerInterface instanceof DpLayerVector))
			return super.getTitleFor(layer);

		DpLayerVector dpl = (DpLayerVector) StyledLayerInterface;
		LayerStyle selectedStyle = getMap().getSelectedStyle(dpl.getId());

		if (selectedStyle == null) {
			return super.getTitleFor(layer);
		}

		if (I8NUtil.isEmpty(selectedStyle.getTitle()))
			super.getTitleFor(layer);

		final ArrayList<String> availableStyles = getMap()
				.getAdditionalStyles().get(dpl.getId());
		if (availableStyles == null || availableStyles.size() == 0
				|| availableStyles.size() > 1)
			return super.getTitleFor(layer);

		/**
		 * Return the add. Styles title ONLY if we only have one add Style avail
		 * in this map.
		 */
		return selectedStyle.getTitle().toString();
		// return super.getTitleFor(layer) + ":" + selectedStyle.getTitle();
	}

	/**
	 * If there are charts available for the map, display a button in the
	 * {@link MapPaneToolBar}.<br/>
	 * Note: Layers that are a static part of the visible map, have their
	 * visibility set in the {@link Map}. If more layers have been added
	 * manually (from the menu), their statistics are automatically handled as
	 * visible.
	 */
	public boolean chartButtonShallAppear() {

		for (MapLayer mLayer : getMapContext().getLayers()) {
			StyledLayerInterface<?> styledLayerInterface = rememberId2StyledLayer
					.get(mLayer.getTitle());
			if (styledLayerInterface instanceof DpLayerVectorFeatureSource) {
				DpLayerVectorFeatureSource dplv = (DpLayerVectorFeatureSource) styledLayerInterface;
				for (FeatureChartStyle chartStyle : dplv.getCharts()) {

					if (getMap().containsDpe(dplv.getId())) {
						/*
						 * Is that layer part of the map? Then check whether
						 * this chartStyle is visible in this map.
						 */
						if (getMap().getAvailableChartIDsFor(dplv.getId())
								.contains(chartStyle.getID()))
							return true;
					} else {
						/*
						 * Layer is not part of the map. It has been added
						 * manually. It's charts shall automatically be visible.
						 */
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * If there are layers with textsymbolizers available in the map, display a
	 * search-in-labels button {@link MapPaneToolBar}.<br/>
	 */
	public boolean searchButtonShallAppear() {

		for (MapLayer mLayer : getMapContext().getLayers()) {
			if (!mLayer.isVisible())
				continue;
			Style mStyle = mLayer.getStyle();
			List<TextSymbolizer> textSymbolizers = StylingUtil
					.getVisibleTextSymbolizers(mStyle);

			// TODO Evaluate against filter.. at least check for simple 1==2
			// rule!
			if (textSymbolizers.size() > 0)
				return true;
		}
		return false;
	}

	/**
	 * Calling this function will hide or show the Chart button.
	 */
	public void showOrHideChartButton() {

		final boolean shallAppear = chartButtonShallAppear();

		if (shallAppear && !isChartButtonVisible) {
			showChartButton();
		} else if (!shallAppear && isChartButtonVisible) {
			removeChartButton();
		}
	}

	/**
	 * Calling this function will hide or show the Search button.
	 */
	public void showOrHideSearchButton() {

		final boolean shallAppear = searchButtonShallAppear();

		if (shallAppear && !isSearchButtonVisible) {
			showSearchButton();
		} else if (!shallAppear && isSearchButtonVisible) {
			removeSearchButton();
		}
	}

	/**
	 * Remove selection-related buttons to the {@link MapPaneToolBar}
	 */
	public void removeChartButton() {
		isChartButtonVisible = false;
		if (getMapPaneToolBar() == null)
			return;

		getMapPaneToolBar().removeId(SEPERATOR3);
		getMapPaneToolBar().removeId(MapPaneToolBar.ACTION_CHARTS);

		/* Without initToolBar the changes will not be reflected */
		getMapPaneToolBar().initToolBar();
	}

	/**
	 * Add a chart button to the {@link MapPaneToolBar} that allows to access
	 * all charts available in the {@link MapContext}.
	 */
	public void showChartButton() {
		if (isChartButtonVisible) {
			// Don't do it more than once
			return;
		}

		isChartButtonVisible = true;

		if (getMapPaneToolBar() == null)
			return;

		getMapPaneToolBar().addSeparator(SEPERATOR3, new JToolBar.Separator());

		final JButton chartButton = new AtlasMapToolBarChartButton(
				getMapContext(), getMap(), 
				rememberId2StyledLayer, this);
		getMapPaneToolBar().addJComponent(chartButton,
				MapPaneToolBar.ACTION_CHARTS, true);
		// Look.. this true is important - otherwise
		// the MapPaneButtons are not recreated

	}

	public void showSearchButton() {
		if (isSearchButtonVisible) {
			// Don't do it more than once
			return;
		}

		isSearchButtonVisible = true;

		if (getMapPaneToolBar() == null)
			return;

		getMapPaneToolBar().addSeparator(SEPERATOR4, new JToolBar.Separator());

		/*
		 * Adds the search button to the MapPaneToolBar
		 */
		MapPaneToolBarAction searchAction = new MapPaneToolBarAction(
				AtlasMapView.ACTION_SEARCH, getMapPaneToolBar(), AtlasViewer
						.R("AtlasMapView.SearchButton.Label"),
				Icons.ICON_SEARCH) {

			private SearchMapDialog searchMapDialog;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (searchMapDialog == null) {
					// It's not only the title which makes trouble if map is
					// NULL
					// so do nothing at all (ISDSS)
					if (getMap() == null) {
						ExceptionDialog
								.show(
										AtlasMapLegend.this,
										null,
										AtlasViewer
												.R("AtlasMapView.errmess.Title"),
										AtlasViewer
												.R("AtlasMapView.errmess.FunctionNotAvailable"));
						return;
					}
					searchMapDialog = new SearchMapDialog(new AtlasLabelSearch(
							getGeoMapPane().getMapPane(), ac.getDataPool()),
							getGeoMapPane().getMapPane(), LabelSearch.R(
									"SearchMapDialog.title", getMap()
											.getTitle()));
					searchMapDialog.setModal(false);
				}
				if (searchMapDialog != null) {
					searchMapDialog.setVisible(true);
				}
			}
		};
		searchAction.putValue(Action.SHORT_DESCRIPTION, AtlasViewer
				.R("AtlasMapView.SearchButton.tt"));
		getMapPaneToolBar().addAction(searchAction, true);
		// Look.. this true is important - otherwise
		// the MapPaneButtons are not recreated

		getMapPaneToolBar().setButtonEnabled(AtlasMapView.ACTION_SEARCH,
				getMap() != null, true);

		//		

	}

	public void removeSearchButton() {
		isSearchButtonVisible = false;
		if (getMapPaneToolBar() == null)
			return;

		getMapPaneToolBar().removeId(SEPERATOR4);
		getMapPaneToolBar().removeId(AtlasMapView.ACTION_SEARCH);

		/* Without initToolBar the changes will not be reflected */
		getMapPaneToolBar().initToolBar();
	}

	/**
	 * Let's only show the selection-related buttons if at least one
	 * Chart-dialog or AttributeTable-Dialog is visible.
	 * 
	 * @return <code>true</code> if the selection-related buttons shall appear.
	 */
	@Override
	public boolean selectionButtonsShallAppear() {

		Boolean shallBeVisible = false;

		/**
		 * Iterating over all layers.. should't be too many...
		 */
		for (MapLayer l : getGeoMapPane().getMapContext().getLayers()) {
			String id = l.getTitle();

			// LOGGER.debug("Check if we have to remove the selection for layer "+id);

			final StyledLayerInterface<?> styledLayerInterface = rememberId2StyledLayer
					.get(id);
			final DpLayer<?, ? extends ChartStyle> dpl = (DpLayer<?, ? extends ChartStyle>) styledLayerInterface;

			if (selectionButtonShallAppearFor(dpl)) {
				shallBeVisible = true;

				/**
				 * This will be done to often, but let's anyway activate the
				 * Synchronizers here
				 */
				if (rememberMapLayerSyncronizers.containsKey(id)) {
					rememberMapLayerSyncronizers.get(id).setEnabled(true);
				}

			} else {
				// For layers, that don't have a appropriate dialog open, the
				// FeatureMapLayerSelectionSynchronizer are disabled and (if
				// anything is selected) the selections are cleared.
				if (rememberSelectionModel.containsKey(id)) {
					// A selectionModel exists!

					final StyledLayerSelectionModel<?> styledLayerSelectionModel = rememberSelectionModel
							.get(id);
					if (styledLayerSelectionModel.getSelection() != null
							&& styledLayerSelectionModel.getSelection().size() > 0) {
						// The selectionModel is not empty.. clear it!
						// LOGGER.info("CmapSelectionSyncronizerlear selection for "+id);
						styledLayerSelectionModel.clearSelection();
					}

					// If we have a selectionModel, we should also have a
					// synchronizer, but we better double check..
					if (rememberMapLayerSyncronizers.containsKey(id)) {
						// A selectionSynchronizer is installed => disable it!
						// This approach will disable the synchronizers multiple
						// times.. but who cares!

						rememberMapLayerSyncronizers.get(id).setEnabled(false);
					}

				}

			}
		}

		return shallBeVisible;

	}

	protected boolean selectionButtonShallAppearFor(
			final DpLayer<?, ? extends ChartStyle> dpl) {
		return AtlasChartJDialog.isOpenForLayer(dpl)
				|| dpl instanceof StyledFeaturesInterface<?>
				&& AVDialogManager.dm_AttributeTable
						.isVisibleFor((StyledFeaturesInterface<?>) dpl);
	}

	@Override
	public void recreateLayerList() {
		if (!isValuesAdjusting()) {
			super.recreateLayerList();
			showOrHideChartButton();
			showOrHideSearchButton();
		}
	}

	@Override
	public boolean insertStyledLayer(StyledLayerInterface<?> styledObj, int idx) {
		boolean result = super.insertStyledLayer(styledObj, idx);
		if (result) {
			String id = styledObj.getId();

			MapLayer mapLayer = getMapLayerFor(id);
			mapLayer.setVisible(!getMap().getHiddenFor(styledObj.getId()));

			if (styledObj instanceof StyledFeaturesInterface<?>) {
				/**
				 * Efficiency: Ignore layer on selection if there are no visible
				 * attributes!
				 */
				AttributeMetadataMap amd = ((StyledFeaturesInterface<?>) styledObj)
						.getAttributeMetaDataMap();
				final int counteVisibleAtts = amd.sortedValuesVisibleOnly().size();
				
				if (!getMap().isSelectableFor(styledObj.getId())
						|| counteVisibleAtts <= 0) {
					getGeoMapPane().getMapPane().setMapLayerSelectable(
							mapLayer, false);
				}
			}

			if (styledObj instanceof DpLayerVectorFeatureSource)
			// Add all it's Charts to the Map by default:
			{
				DpLayerVectorFeatureSource dplvfs = (DpLayerVectorFeatureSource) styledObj;
				for (FeatureChartStyle fcs : dplvfs.getCharts())
					map.getAvailableChartIDsFor(dplvfs.getId())
							.add(fcs.getID());

				showOrHideChartButton();
				
			}
		}
		return result;
	}

	public Map getMap() {
		return map;
	}

	@Override
	protected void unregisterStyledLayer(final MapLayer removedLayer) {
		super.unregisterStyledLayer(removedLayer);

		if (removedLayer != null) {
			final StyledLayerInterface<?> styledLayerInterface = rememberId2StyledLayer
					.get(removedLayer.getTitle());

			/**
			 * Closing dialogs associated with the layer
			 */
			{
				StyledLayerInterface<?> key = rememberId2StyledLayer
						.get(removedLayer.getTitle());
				AVDialogManager.dm_Styler.disposeInstanceFor(key);

				if (styledLayerInterface instanceof StyledFeaturesInterface<?>) {
					AVDialogManager.dm_AttributeTable
							.disposeInstanceFor((StyledFeaturesInterface<?>) styledLayerInterface);
				}

				if (styledLayerInterface instanceof DpLayerVectorFeatureSource) {
					DpLayerVectorFeatureSource dplvfs = (DpLayerVectorFeatureSource) styledLayerInterface;
					for (LayerStyle ls : dplvfs.getLayerStyles()) {
						AVDialogManager.dm_Styler.disposeInstanceFor(ls);
					}
				}
			}

			/**
			 * Close any open chart dialogs for this layer
			 */
			if (styledLayerInterface instanceof DpEntry) {
				DpEntry<? extends ChartStyle> dpe = (DpEntry<? extends ChartStyle>) styledLayerInterface;
				for (ChartStyle cs : dpe.getCharts()) {
					AVDialogManager.dm_Charts.disposeInstanceFor(cs);
				}
			}

		}

	}

}
