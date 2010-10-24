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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AVProps;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.dp.DpRef;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;
import org.geopublishing.atlasViewer.dp.layer.DpLayerRaster;
import org.geopublishing.atlasViewer.dp.layer.DpLayerRasterPyramid;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;
import org.geopublishing.atlasViewer.dp.layer.LayerStyle;
import org.geopublishing.atlasViewer.map.Map;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListListener;
import org.geotools.styling.Style;

import schmitzm.geotools.gui.GeoMapPane;
import schmitzm.geotools.gui.SelectableXMapPane;
import schmitzm.geotools.gui.XMapPaneEvent;
import schmitzm.geotools.map.event.JMapPaneListener;
import schmitzm.geotools.map.event.ObjectSelectionEvent;
import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.swing.ExceptionDialog;
import skrueger.AttributeMetadataImpl;
import skrueger.RasterLegendData;
import skrueger.geotools.MapContextManagerInterface;
import skrueger.geotools.MapPaneToolBar;
import skrueger.geotools.MapPaneToolBar.MapPaneToolBarAction;
import skrueger.geotools.MapPaneToolSelectedListener;
import skrueger.geotools.MapView;
import skrueger.geotools.StyledLayerInterface;

import com.vividsolutions.jts.geom.Envelope;

/**
 * This {@link AtlasMapView} is used in {@link AtlasViewerGUI} to show a
 * {@link GeoMapPane} on the right, and a {@link JTabbedPane} on the left with
 * the legend and optionally other info.<br/>
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 */
public class AtlasMapView extends MapView implements MapContextManagerInterface {

	protected static final Logger LOGGER = Logger.getLogger(AtlasMapView.class);

	public static final int ACTION_SEARCH = 300;

	protected AtlasConfig atlasConfig;

	/**
	 * The map that is being displayed. This is the "real" object we are working
	 * on.
	 */
	protected Map map;

	/** The AtlasMapLegend that is filled when {@link #setMap(Map)} is called * */
	protected AtlasMapLegend layerManager;

	/** The tabbed pane on the left... * */
	protected JComponent leftSide;

	/**
	 * The {@link ClickInfoDialog} which displays information when clicking into
	 * the map. It's recycled... See {@link #disposeClickInfoDialog()}
	 */
	private ClickInfoDialog clickInfoDialog;

	/**
	 * Allows an external class to dispose the ClickInfoTool
	 */
	protected void disposeClickInfoDialog() {
		if (clickInfoDialog != null)
			clickInfoDialog.dispose();
		clickInfoDialog = null;
	}

	/**
	 * A component that links to the Window this {@link AtlasMapView} belongs to
	 * *
	 */
	private final Component parentGUI;

	/**
	 * 
	 */
	final MapPaneToolSelectedListener listenToToolSelectionToDisposeInfoClickDialog = new MapPaneToolSelectedListener() {

		@Override
		public void toolSelected(int toolId) {
			if (toolId != MapPaneToolBar.TOOL_INFO)
				disposeClickInfoDialog();
		}

	};

	/**
	 * You have to call initialize() after construction!
	 * 
	 * @param atlasConfig
	 *            The AtlasConfig where the {@link Map} is defined
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Tzeggai</a>
	 */
	public AtlasMapView(Component owner, AtlasConfig atlasConfig) {
		super(owner);
		this.parentGUI = owner;

		if (getToolBar() != null) {
			getToolBar().addButtonSelectedListener(
					listenToToolSelectionToDisposeInfoClickDialog);

			MapPaneToolBarAction defaultZoomAction = new MapPaneToolBarAction(
					MapPaneToolBar.ACTION_ZOOM_DEFAULT,
					getToolBar(),
					"",
					new ImageIcon(MapView.class
							.getResource("resource/icons/zoom_full_extend.png")),
					AtlasViewerGUI.R("MapPaneButtons.DefaultZoom.TT")) {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (map.getDefaultMapArea() != null )
						getMapPane().setMapArea(map.getDefaultMapArea());
					else if (map.getMaxExtend() != null ) 
						getMapPane().setMapArea(map.getMaxExtend());
					else 
						getMapPane().setMapArea(getMapPane().getMaxExtend());
				}

			};
			// Set Selection
			getToolBar().addAction(defaultZoomAction, false);

		}

		this.atlasConfig = atlasConfig;
		getGeoMapPane().getScalePane().getScaleLabel().setVisible(false);

		getGeoMapPane().getMapPane()
				.setAntiAliasing(
						atlasConfig.getProperties().get(AVProps.Keys.antialiasingMaps, "1").equals(
								"0") ? false : true);

		/**
		 * When the component is hidden, close any open info dialog.
		 */
		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentHidden(ComponentEvent e) {
				disposeClickInfoDialog();
			}

		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see skrueger.atlas.gui.MapView#initialize()
	 */
	@Override
	public void initialize() {

		super.initialize(); // important!

		// **********************************************************************
		// If we find a mapIcon we will set it to be always rendered into the
		// map
		// **********************************************************************
//		final URL mapIconURL = AtlasConfig.getResLoMan().getResourceAsUrl(
//				AtlasConfig.MAPICON_RESOURCE_NAME);
		final URL mapIconURL = atlasConfig.getResource(
				AtlasConfig.MAPICON_RESOURCE_NAME);
		
		if (mapIconURL != null) {
			try {
				BufferedImage mapImageIcon = ImageIO.read(mapIconURL);
				getGeoMapPane().getMapPane().setMapImage(mapImageIcon);
			} catch (IOException e) {
				LOGGER.error(e);
			}

		} else {
			LOGGER.info("No " + AtlasConfig.MAPICON_RESOURCE_NAME
					+ " found. Not displaying any icon on top of the map");
		}

		Border insideBorder = getGeoMapPane().getMapPane().getBorder();
		getGeoMapPane().getMapPane().setBorder(
				BorderFactory.createCompoundBorder(BorderFactory
						.createMatteBorder(0, 0, 0, 0, getGeoMapPane()
								.getBackground()), insideBorder));

		/**
		 * Now the GUI has been fully configured, and we dare to
		 * setValuesAdjusting to false
		 */
		layerManager.setValuesAdjusting(false);

		/**
		 * And right after that we dare to start painting the XMapPane
		 */
		getGeoMapPane().getMapPane().setPainting(true);

		/**
		 * A {@link JMapPaneListener} is registered with the {@link XMapPane}
		 * that listens for events of type {@link ObjectSelectionEvent}.
		 */
		final JMapPaneListener infoClickMapPaneListener = new JMapPaneListener() {

			@Override
			public void performMapPaneEvent(XMapPaneEvent evt) {

				/**
				 * This only reacts if the INFO tool has been selected in the
				 * toolbar, AND the event has not been thrown by the
				 * FilterDialog.
				 */
				if (getToolBar().getSelectedTool() == MapPaneToolBar.TOOL_INFO
						&& evt.getSourceObject() instanceof SelectableXMapPane
						&& evt instanceof ObjectSelectionEvent<?>) {

					final ObjectSelectionEvent<?> objectSelectionEvent = (ObjectSelectionEvent<?>) evt;

					getClickInfoDialog()
							.setSelectionEvent(objectSelectionEvent);

					getClickInfoDialog().setVisible(true);

					getClickInfoDialog().toFront();
				}
			}
		};
		getGeoMapPane().getMapPane().addMapPaneListener(
				infoClickMapPaneListener);
	}

	/**
	 * Lazily creates the {@link ClickInfoDialog} which is used to respond to
	 * every "into-the-map" click.
	 */
	public ClickInfoDialog getClickInfoDialog() {

		if (clickInfoDialog == null) {

			// **********************************************************************
			// Show selected features / information when clicked on a Info-Frame
			// **********************************************************************
			clickInfoDialog = new ClickInfoDialog(AtlasMapView.this, false,
					layerManager, atlasConfig);

			// SwingUtil.setRelativeFramePosition(clickInfoDialog, SwingUtil
			// .getParentFrame(AtlasMapView.this), 1., .08);
			//			
			clickInfoDialog.setLocationRelativeTo(getMapPane());

		}

		return clickInfoDialog;
	}

	/**
	 * Called to fill the left side of the {@link MapView}<br>
	 * Is supposed to set {@link #layerManager} variable.
	 */
	@Override
	public JComponent getSidePane() {

		if (layerManager == null) {

			final AtlasMapLegend atlasMapLegend = new AtlasMapLegend(
					getGeoMapPane(), map, atlasConfig, getToolBar());
			layerManager = atlasMapLegend;

			final JScrollPane scrollPaneOfLayerPane = new JScrollPane(
					atlasMapLegend);
			scrollPaneOfLayerPane.setBorder(BorderFactory.createEmptyBorder());
			atlasMapLegend.setBorder(BorderFactory.createEmptyBorder());

			/**
			 * If HTML info exists for this map (and this language), then we
			 * create a JTabbedPane. Otherwise the legend is the top component.
			 */
			if (map.getInfoURL() != null) {

				// **************************************************************
				// If LayerPanel and Info are provided, use a tabbed Pane
				// **************************************************************
				JTabbedPane tabbedPane = new JTabbedPane();

				tabbedPane.setBorder(BorderFactory.createEmptyBorder());

				tabbedPane.addTab(AtlasViewerGUI
						.R("AtlasMapView.tabbedPane.LayersTab_label"),
						scrollPaneOfLayerPane);
				tabbedPane.setToolTipTextAt(0, AtlasViewerGUI
						.R("AtlasMapView.tabbedPane.LayersTab_tt"));
				HTMLInfoJPane infoPanel = new HTMLInfoJPane(map);
				tabbedPane.addTab(AtlasViewerGUI
						.R("AtlasMapView.tabbedPane.InfoTab_label"),
						new JScrollPane(infoPanel));
				tabbedPane.setToolTipTextAt(1, AtlasViewerGUI
						.R("AtlasMapView.tabbedPane.InfoTab_tt"));

				tabbedPane.setSelectedIndex(1);
				add(tabbedPane, BorderLayout.CENTER);

				leftSide = tabbedPane;
			} else {
				// **************************************************************
				// If only LayerPanel is provided, do not use a tabbed pane
				// **************************************************************

				leftSide = scrollPaneOfLayerPane;
			}

			add(leftSide, BorderLayout.CENTER);

		}

		return leftSide;
	}

	/**
	 * @return the active {@link Map} or <code>null</code>
	 */
	public Map getMap() {
		return map;
	}

	/**
	 * A Hack, because JSpitPane's ComponentListenrs's resize method is called
	 * twice
	 */
	Boolean firstTimeResize = true;

	/**
	 * Set map is an essential function! ;-) Sets the {@link AtlasMapView} to
	 * the given {@link Map}.<br/>
	 * 
	 * @return <code>false</code> is any problem occured.
	 */
	public void setMap(final Map newMap) {

		map = newMap;

		// Create a new AtlasSidePanel
		if (layerManager != null)
			layerManager.dispose();
		layerManager = null;

		getSidePane(); // sets the layerManager variable..

		/**
		 * Disable updates.. will be set to false in #initialize()
		 */
		layerManager.setValuesAdjusting(true);

		// Do not trigger paints of XMapPane until JSpitPane has resized
		getGeoMapPane().getMapPane().setPainting(false);

		/**
		 * If a preferred LeftRightRatio has been selected, apply it here. Else
		 * set it to -1, which is auto mode.
		 */
		Double leftRightRatio = map.getLeftRightRatio();
		if (leftRightRatio != null && leftRightRatio > 0) {
			// getSplitPane().setDividerLocation(leftRightRatio);

			/**
			 * The ratio has to be set here as an absolute value, because the
			 * JSpitPane is not visible ATM
			 */
			getSplitPane().setDividerLocation(
					calcAbsoluteWidthForDivider(map.getLeftRightRatio()));

		} else {
			/**
			 * The ratio has to be set here as an absolute value, because the
			 * JSpitPane is not visible ATM
			 */
			getSplitPane().setDividerLocation(
					calcAbsoluteWidthForDivider(1. - (1. / 1.618033)));
		}

		addTheLayersOfTheMapToTheMapLayerManager();

		// Optionally activate the SearchLabels button
		getToolBar().setButtonEnabled(ACTION_SEARCH, map != null, true);

		getGeoMapPane().getMapPane().setMaxExtend(map.getMaxExtend());

		/** Configuring the map margin **/
		getGeoMapPane().getScalePane().setUnits(map.getScaleUnits());
		getGeoMapPane().getScalePane().setVisible(map.isScaleVisible());
		
		getGeoMapPane().getVertGrid().setVisible(map.isGridPanelVisible());
		getGeoMapPane().getHorGrid().setVisible(map.isGridPanelVisible());
		map.getGridPanelFormatter().setCRS(map.getGridPanelCRS());
		getGeoMapPane().getHorGrid().setGridFormatter(
				map.getGridPanelFormatter());
		getGeoMapPane().getVertGrid().setGridFormatter(
				map.getGridPanelFormatter());
		
		getGeoMapPane().getScalePane().setUnits(map.getScaleUnits());
	}

	/**
	 * The trick here is to calculate the position of the divider according to a
	 * ratio, BEFORE the JSpitPane is visible and has any width! In the Atlas it
	 * works to use the owner, which is instance of JFrame
	 * 
	 * @return The number in pixels from the left
	 */
	protected int calcAbsoluteWidthForDivider(Double ratio) {
		final int DEFAULT = 300;
		if (parentGUI == null)
			return DEFAULT;
		if (parentGUI.getWidth() == 0)
			return DEFAULT;
		int width = (int) (parentGUI.getWidth() * ratio);
		return width;
	}

	/**
	 * Also sets the default map area after all layers have been added
	 * 
	 * @param layerRefs
	 */
	private void addTheLayersOfTheMapToTheMapLayerManager() {

		// **********************************************************************
		// Adding all the Map's layers to the layerManager
		// **********************************************************************
		for (DpRef ref : map.getLayers()) {
			try {
				// LOGGER.debug("Adding dpRefId=" + ref.getTargetId()
				// + " to AtlasMapView...");

				// **************************************************************
				// Resolving the Reference to a real Datapoolentry
				// **************************************************************
				final DpLayer<?, ? extends ChartStyle> dpLayer = (DpLayer<?, ChartStyle>) ref
						.getTarget();

				// dpLayer.seeJAR(AtlasMapView.this); // getURL holt das doch
				// sowieso

				if (dpLayer instanceof DpLayerRaster) {
					layerManager.addStyledLayer(dpLayer);
				} else if (dpLayer instanceof DpLayerRasterPyramid) {
					// **********************************************************
					// Adding a Pyramidlayer to the map
					// **********************************************************
					final DpLayerRasterPyramid pyramid = (DpLayerRasterPyramid) dpLayer;
					layerManager.addStyledLayer(pyramid);

				} else if (dpLayer instanceof DpLayerVectorFeatureSource) {

					/**
					 * If an additional Style has been defined for the maplayer,
					 * we have to apply it
					 */
					if (map.getSelectedStyleID(dpLayer.getId()) != null) {
						LayerStyle layerStyleByID = dpLayer
								.getLayerStyleByID(map
										.getSelectedStyleID(dpLayer.getId()));
						Style style = layerStyleByID.getStyle();
						// dpLayer.setTitle(layerStyleByID.getTitle());
						// dpLayer.setDesc(layerStyleByID.getDesc());
						dpLayer.setStyle(style);
					}

					// **********************************************************
					// Adding a vectorlayer to the map. Ask the JMapPane NOT to
					// repaint it every time
					// **********************************************************
					layerManager
							.addStyledLayer(dpLayer);

				}

			} catch (final Exception e) {

				IllegalStateException illegalStateException = new IllegalStateException(
						"Layer " + ref.getTarget().getTitle() + " is missing! Will exit now.",
						e);
				LOGGER.error("Layer not found?", illegalStateException);
				ExceptionDialog.show(parentGUI, illegalStateException);
				System.exit(-99);
			}
		}

		// **********************************************************************
		// When opening the map, it is zoomed to the map's defaultMapArea (OR to
		// the first layer if no defaultMapArea is set)
		// **********************************************************************
		final Envelope defaultMapArea = map.getDefaultMapArea();
		// LOGGER.debug("Setting the MapArea to: " + defaultMapArea);
		if (defaultMapArea == null) {
			if (map.getLayers().size() > 0)
				getGeoMapPane().getMapPane().zoomToLayer(0);
		} else {
			getGeoMapPane().getMapPane().setMapArea(defaultMapArea);
		}

	}

	/**
	 * Tries to dereference as much memory as possible
	 */
	@Override
	public void dispose() {

		super.dispose();

		disposeClickInfoDialog();

		getToolBar().removeButtonSelectedListener(
				listenToToolSelectionToDisposeInfoClickDialog);

		if (layerManager instanceof AtlasMapLegend) {
			// while removing the layers from the context, we don't wont the
			// legend to be recreated due to layerRevoed listeners...
			MapLegend atlasMapLegend = layerManager;
			atlasMapLegend.setValuesAdjusting(true);
		}

		if (layerManager != null)
			layerManager.dispose();

		AVDialogManager.dm_Styler.disposeAll(); // should go one up in the class
		// hirachy.. but MapView is in
		// schmitzm... mmm...the
		// dialogmanager should go there
		// to....

		AVDialogManager.dm_AtlasStyler.disposeAll();
		AVDialogManager.dm_AttributeTable.disposeAll();
		AVDialogManager.dm_Charts.disposeAll();

		map = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * skrueger.geotools.MapContextManagerInterface#addStyledLayer(skrueger.
	 * StyledLayerInterface)
	 */
	@Override
	public boolean addStyledLayer(StyledLayerInterface<?> styledLayerObject) {
		return layerManager.addStyledLayer(styledLayerObject);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * skrueger.geotools.MapContextManagerInterface#insertStyledLayer(skrueger
	 * .StyledLayerInterface, int)
	 */
	@Override
	public boolean insertStyledLayer(StyledLayerInterface<?> styledLayer,
			int mapContextIdx) {
		return layerManager.insertStyledLayer(styledLayer, mapContextIdx);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see skrueger.geotools.MapContextManagerInterface#removeStyledLayer(int)
	 */
	@Override
	public boolean removeStyledLayer(int mapContextIdx) {
		return layerManager.removeStyledLayer(mapContextIdx);
	}

	@Override
	public List<StyledLayerInterface<?>> getStyledObjects() {
		return layerManager.getStyledObjects();
	}

	@Override
	public List<AttributeMetadataImpl> getVisibleAttribsFor(MapLayer layer) {
		return layerManager.getVisibleAttribsFor(layer);
	}

	// **************************************************************************
	// Access to Listernes of the underlying MapContext
	// **************************************************************************

	@Override
	public void addMapLayerListListener(MapLayerListListener listener) {
		layerManager.addMapLayerListListener(listener);
	}

	@Override
	public void removeMapLayerListListener(MapLayerListListener listener) {
		layerManager.removeMapLayerListListener(listener);
	}

	@Override
	public String getDescFor(MapLayer layer) {
		return layerManager.getDescFor(layer);
	}

	@Override
	public String getTitleFor(MapLayer layer) {
		return layerManager.getTitleFor(layer);
	}

	@Override
	public RasterLegendData getLegendMetaData(MapLayer layer) {
		return layerManager.getLegendMetaData(layer);
	}

	@Override
	public StyledLayerInterface<?> getStyledObjectFor(MapLayer layer) {
		return layerManager.getStyledObjectFor(layer);
	}

	public AtlasConfig getAtlasConfig() {
		return atlasConfig;
	}

	/**
	 * @return The {@link AtlasMapLegend} {@link JComponent} that manages and
	 *         presents the layer list.
	 */
	public AtlasMapLegend getLegend() {
		return layerManager;
	}

	/**
	 * @return The {@link MapContext} managed by this
	 *         {@link MapContextManagerInterface}
	 */
	@Override
	public MapContext getMapContext() {
		return geoMapPane.getMapContext();
	}

}
