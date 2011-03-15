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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.util.WeakHashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.swing.AtlasStylerSaveLayerToSLDAction;
import org.geopublishing.atlasStyler.swing.StylerDialog;
import org.geopublishing.atlasViewer.ExportableLayer;
import org.geopublishing.atlasViewer.GpCoreUtil;
import org.geopublishing.atlasViewer.swing.internal.DnDAtlasObject;
import org.geopublishing.atlasViewer.swing.internal.DnDAtlasObject.AtlasDragSources;
import org.geopublishing.atlasViewer.swing.plaf.BasicMapLayerLegendPaneUI;
import org.geopublishing.atlasViewer.swing.plaf.MetalMapLayerLegendPaneUI;
import org.geopublishing.atlasViewer.swing.plaf.WindowsClassicMapLayerLegendPaneUI;
import org.geopublishing.atlasViewer.swing.plaf.WindowsMapLayerLegendPaneUI;
import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.geotools.jdbc.JDBCFeatureSource;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.styling.Style;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.plaf.DefaultsList;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.jdesktop.swingx.plaf.TaskPaneAddon;
import org.jdesktop.swingx.plaf.windows.WindowsClassicLookAndFeelAddons;
import org.jdesktop.swingx.plaf.windows.WindowsLookAndFeelAddons;
import org.opengis.filter.Filter;

import de.schmitzm.geotools.MapContextManagerInterface;
import de.schmitzm.geotools.feature.FeatureOperationTreeFilter;
import de.schmitzm.geotools.feature.FeatureUtil;
import de.schmitzm.geotools.gui.AtlasFeatureLayerFilterDialog;
import de.schmitzm.geotools.gui.FeatureLayerFilterDialog;
import de.schmitzm.geotools.gui.GeoMapPane;
import de.schmitzm.geotools.styling.StyledFS;
import de.schmitzm.geotools.styling.StyledFeaturesInterface;
import de.schmitzm.geotools.styling.StyledLayerInterface;
import de.schmitzm.geotools.styling.StyledLayerUtil;
import de.schmitzm.geotools.styling.StyledRasterInterface;
import de.schmitzm.swing.ExceptionDialog;
import de.schmitzm.swing.SwingUtil;

/**
 * All information about a {@link MapLayer} is presented here. The class name is
 * derived from the superclass {@link JXTaskPane}.
 * 
 * This component is draggable by the mouse.
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 * 
 */
public class MapLayerLegend extends JXTaskPane implements DragSourceListener,
		DragGestureListener, DropTargetListener {

	/** Caches one HTML info window per layer * */
	static protected WeakHashMap<URL, HTMLBrowserWindow> htmlInfoWindows = new WeakHashMap<URL, HTMLBrowserWindow>();

	static DataFlavor localObjectFlavor;

	static private final Logger LOGGER = Logger.getLogger(MapLayerLegend.class);;

	static DataFlavor[] supportedFlavors = { localObjectFlavor };

	private static final String SWINGX_MAP_LAYER_LEGEND_PANE_U_I = "swingx/MapLayerLegendPaneUI";

	static {

		LookAndFeelAddons.contribute(new TaskPaneAddon() {
			@Override
			protected void addBasicDefaults(LookAndFeelAddons addon,
					DefaultsList defaults) {
				super.addBasicDefaults(addon, defaults);
				defaults.add(SWINGX_MAP_LAYER_LEGEND_PANE_U_I,
						BasicMapLayerLegendPaneUI.class.getName());
			}

			@Override
			protected void addMetalDefaults(LookAndFeelAddons addon,
					DefaultsList defaults) {

				super.addMetalDefaults(addon, defaults);
				defaults.add(SWINGX_MAP_LAYER_LEGEND_PANE_U_I,
						MetalMapLayerLegendPaneUI.class.getName());
			}

			@Override
			protected void addWindowsDefaults(LookAndFeelAddons addon,
					DefaultsList defaults) {
				super.addWindowsDefaults(addon, defaults);
				if (addon instanceof WindowsLookAndFeelAddons)
					defaults.add(SWINGX_MAP_LAYER_LEGEND_PANE_U_I,
							WindowsMapLayerLegendPaneUI.class.getName());
				if (addon instanceof WindowsClassicLookAndFeelAddons) {
					defaults.add(SWINGX_MAP_LAYER_LEGEND_PANE_U_I,
							WindowsClassicMapLayerLegendPaneUI.class.getName());
				}
			}
		});
	}

	static {
		try {
			localObjectFlavor = new DataFlavor(
					DataFlavor.javaJVMLocalObjectMimeType);
		} catch (final ClassNotFoundException cnfe) {
			LOGGER.error(cnfe);
		}
	}

	// ****************************************************************************
	// D'n'D stuff
	// ****************************************************************************
	private final DragSource dragSource;

	private final DropTarget dropTarget;

	/** May this Layer be exported? */
	protected final ExportableLayer exportable;

	private final MapLayer mapLayer;

	/** The {@link MapLegend} containing this {@link MapLayerLegend} */
	protected final MapLegend mapLegend;

	protected final StyledLayerInterface<?> styledLayer;

	protected boolean transparentToggled = false;

	private URL cachedInfoUrl;

	private long lastTimeUrlCached;

	/**
	 * The {@link MapLayerLegend} represents one {@link MapLayer} in the legend.
	 * It can be Dragged'n'Dropped.
	 * 
	 * @param gmp
	 *            {@link GeoMapPane} that the legend is working on
	 * @param mapLayer
	 *            The {@link MapLayer} this is representings
	 * @param exportable
	 *            if != null, then the GUI will test if export is allowed. null
	 *            may be passed
	 * @param mapLegend
	 *            The {@link MapLegend} this MapLayerLegend is embedded into
	 */
	public MapLayerLegend(MapLayer mapLayer, ExportableLayer exportable,
			StyledLayerInterface<?> styledObj, MapLegend mapLegend) {

		if (mapLayer == null)
			throw new IllegalArgumentException("mapLayer may not be null!");
		if (mapLegend == null)
			throw new IllegalArgumentException("mapLegend may not be null!");

		this.mapLayer = mapLayer;
		this.exportable = exportable;
		this.styledLayer = styledObj;
		this.mapLegend = mapLegend;

		if (getLegendTooltip() != null)
			setToolTipText("<html>" + getLegendTooltip() + "</html>");
		else
			setToolTipText(null);

		// ****************************************************************************
		// D'n'D stuff
		// ****************************************************************************
		dragSource = new DragSource();

		// Fehlermeldung
		@SuppressWarnings("unused")
		final DragGestureRecognizer dgr = dragSource
				.createDefaultDragGestureRecognizer(this,
						DnDConstants.ACTION_MOVE, this);

		dropTarget = new DropTarget(this, this);

		// The next line starts the system dependent look of the MapLayerLegend
		// (thanks to swingx).
		updateUI();

		setScrollOnExpand(false);
		setAnimated(false);

		add(SldLegendUtil.createLegend(styledLayer));

		// Update the style in the MapLayer if needed and keep any selection FTS
		Style style2 = styledLayer.getStyle();
		StyledLayerUtil.updateMapLayerStyleIfChangedAndKeepSelection(mapLayer,
				style2);

	}

	/**
	 * The {@link MapLayerLegend} represents one {@link MapLayer} in the legend.
	 * It can be Dragged'n'Dropped. It can not be exported.
	 * 
	 * @param gmp
	 *            {@link GeoMapPane} that the legend is working on
	 * @param mapLayer
	 *            The {@link MapLayer} this is representings
	 * @param layerPanel
	 *            The {@link MapLegend} this LayerPaneGroup is embedded into
	 */
	public MapLayerLegend(MapLayer mapLayer, MapLegend layerPanel) {
		this(mapLayer, null, null, layerPanel);
	}

	/**
	 * The small INFO Icon has been clicked
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public void clickedInfoButton() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {

				HTMLBrowserWindow htmlWindow = htmlInfoWindows
						.get(getInfoURL());
				if (htmlWindow == null) {
					htmlWindow = getHTMLBrowserWindow();
					htmlInfoWindows.put(getInfoURL(), htmlWindow);
					htmlWindow.pack();
					htmlWindow.addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosing(WindowEvent e) {
							htmlInfoWindows.remove(getInfoURL());
							super.windowClosing(e);
						}
					});
				}
				SwingUtil.centerFrameOnScreenRandom(htmlWindow);
				htmlWindow.setVisible(true);
				htmlWindow.requestFocus();
			}
		});
	}

	@Override
	public void dragDropEnd(DragSourceDropEvent dsde) {
	}

	@Override
	public void dragEnter(DragSourceDragEvent dsde) {
	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		if (dtde.getSource() != dropTarget) {
			dtde.rejectDrag();
		} else {
			dtde.acceptDrag(DnDConstants.ACTION_MOVE);

			Object dragged;
			try {
				dragged = dtde.getTransferable().getTransferData(
						localObjectFlavor);
			} catch (final UnsupportedFlavorException e) {
				return;
			} catch (final IOException e) {
				return;
			}

			if (dragged instanceof DnDAtlasObject) {
				final DnDAtlasObject transObj = (DnDAtlasObject) dragged;

				if (transObj.getClassOfObj() == MapLayer.class) {
					final MapLayer insertHereMapLayer = (MapLayer) transObj
							.getObject();

					if (getMapLayer().equals(insertHereMapLayer)) {
						return;
					}

					if (!isSpecial())
						setSpecial(true);
				}
			}

		}
	}

	@Override
	public void dragExit(DragSourceEvent dse) {
	}

	@Override
	public void dragExit(DropTargetEvent dte) {
		setSpecial(false);
	}

	@Override
	public void dragGestureRecognized(final DragGestureEvent dge) {
		final Transferable trans = new RJLTransferable(getMapLayer(),
				AtlasDragSources.LAYERPANEGROUP, MapLayer.class);

		final Cursor moveCursor = Cursor
				.getPredefinedCursor(Cursor.MOVE_CURSOR);

		// ****************************************************************************
		// TODO Try to get a Image that will be dragged next to the cursor..
		// Can't see anything so far on linux :-/
		// ****************************************************************************
		if (getIcon() != null && getIcon() instanceof ImageIcon) {
			final Image image = ((ImageIcon) getIcon()).getImage();
			final Point point = new Point(5, 5);
			dge.startDrag(moveCursor, image, point, trans, MapLayerLegend.this);
		} else {
			dge.startDrag(moveCursor, trans, MapLayerLegend.this);
		}

	}

	@Override
	public void dragOver(DragSourceDragEvent dsde) {
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
	}

	@Override
	public void drop(DropTargetDropEvent dtde) {
		// LOGGER.debug("drop()!");
		setSpecial(false);

		MapContext mapContext = mapLegend.getGeoMapPane().getMapContext();

		Object dragged;
		try {
			dragged = dtde.getTransferable().getTransferData(localObjectFlavor);
		} catch (final UnsupportedFlavorException e) {
			dtde.dropComplete(false);
			return;
		} catch (final IOException e) {
			dtde.dropComplete(false);
			return;
		}

		if (dragged instanceof DnDAtlasObject) {
			final DnDAtlasObject transObj = (DnDAtlasObject) dragged;

			if (transObj.getSource() == AtlasDragSources.LAYERPANEGROUP
					&& transObj.getClassOfObj() == MapLayer.class) {
				// ****************************************************************************
				// This Drop is a reordering inside the Layerlist
				// ****************************************************************************
				final MapLayer insertHereMapLayer = (MapLayer) transObj
						.getObject();

				if (getMapLayer().equals(insertHereMapLayer)) {
					dtde.dropComplete(false);
					return;
				}

				final int dropIdx = mapContext.indexOf(getMapLayer());

				final int sourceIdx = mapContext.indexOf(insertHereMapLayer);
				// LOGGER.debug("moving from "+sourceIdx+ " to  "+dropIdx);
				mapContext.moveLayer(sourceIdx, dropIdx);

				dtde.dropComplete(true);

				return;
			}

			else

			if (transObj.getSource() == AtlasDragSources.DATAPOOLLIST) {
				// ****************************************************************************
				// This Drop is a nwe Layer in the Layerlist
				// ****************************************************************************
				// LOGGER.debug("inserting a new layer");

				if (transObj.getObject() instanceof StyledLayerInterface) {
					final StyledLayerInterface<?> styledObj = (StyledLayerInterface<?>) transObj
							.getObject();
					final MapContextManagerInterface layerManager = (MapContextManagerInterface) getParent();

					dtde.dropComplete(
					// layerManager.addStyledLayer(styledObj)
					layerManager.insertStyledLayer(styledObj,
							mapContext.indexOf(getMapLayer())));
					return;
				}

			}

		}

		dtde.dropComplete(false);
		return;
	}

	@Override
	public void dropActionChanged(DragSourceDragEvent dsde) {
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	/**
	 * Export this (if enabled) to the local disk
	 */
	public void export() {
		if (exportable == null)
			return;
		final Frame owner = SwingUtil.getParentFrame(this);
		try {
			exportable.exportWithGUI(owner);
		} catch (final IOException e) {
			final String msg = GpCoreUtil
					.R("LayerPaneGroup.JOptionPane.ShowMessageDialog.export_failed");
			JOptionPane.showMessageDialog(owner, msg); // i8ndone
		}
	}

	/**
	 * Overwritten in AtlasMapLayerLegend, where the atlasConfig parameter is
	 * passed properly
	 */
	protected HTMLBrowserWindow getHTMLBrowserWindow() {
		final String titleText = GpCoreUtil.R(
				"LayerPaneGroup.ClickedInfoButton.information_about",
				getTitle());

		return new HTMLBrowserWindow(
				SwingUtil.getParentWindow(MapLayerLegend.this), getInfoURL(),
				titleText, null);
	}

	// ****************************************************************************
	//
	// Methods for DragTarget support
	//
	// ****************************************************************************

	/**
	 * Returns <code>null</code> or the {@link URL} to an HTML page with info
	 * about this {@link StyledLayerInterface}.
	 */
	public URL getInfoURL() {
		if (cachedInfoUrl == null
				|| System.currentTimeMillis() - lastTimeUrlCached > 200) {
			lastTimeUrlCached = System.currentTimeMillis();
			if (styledLayer != null)
				cachedInfoUrl = styledLayer.getInfoURL();

		}
		return cachedInfoUrl;
	}

	/**
	 * Returns <code>null</code> or a tooltip for the legend. The tooltip does
	 * not start/end with a <code>html</code> tag.
	 */
	public String getLegendTooltip() {
		return GpCoreUtil.R("LayerBar.default.tooltip");

	}

	/**
	 * @return a {@link JPopupMenu} with valid MenuItems for this Layer.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public JPopupMenu getToolMenu() {

		final JPopupMenu toolPopup = new JPopupMenu();

		// ****************************************************************************
		// AtlasStyler related button to save the LayerStyler as .SLD
		// ****************************************************************************
		if (styledLayer instanceof StyledFS) {

			final StyledFS styledFS = (StyledFS) styledLayer;
			// We are in AtlasStyler. Offer to save the .SLD
			toolPopup.add(new JMenuItem(new AtlasStylerSaveLayerToSLDAction(
					this, styledFS)));
		}

		// ****************************************************************************
		// Create filter-related buttons if this layer is filterable
		// ****************************************************************************
		if (isFilterable()) {

			toolPopup.add(new JMenuItem(new AbstractAction(GpCoreUtil
					.R("LayerToolMenu.filter"), Icons.ICON_FILTER) {

				@Override
				public void actionPerformed(ActionEvent e) {
					openFilterDialog();
				}

			}));

			final JMenuItem removeFilterMenuItem = new JMenuItem();
			removeFilterMenuItem
					.setAction(new AbstractAction(GpCoreUtil
							.R("LayerToolMenu.remove_filter"),
							Icons.ICON_REMOVE_FILTER) {

						@Override
						public void actionPerformed(ActionEvent e) {
							removeFilter();
							removeFilterMenuItem.setEnabled(false);
							MapLayerLegend.this.repaint(); // Update the
															// eye-Icon
						}

					});

			removeFilterMenuItem.setEnabled(isFiltered());
			toolPopup.add(removeFilterMenuItem);
		}

		// long last = System.currentTimeMillis();

		// ****************************************************************************
		// Create AtlasStyler Button
		// ****************************************************************************
		if (isStyleEditable()
//				&& !(styledLayer instanceof StyledRasterInterface)
				) {

			toolPopup.add(new JMenuItem(new AbstractAction(GpCoreUtil
					.R("LayerToolMenu.style"), Icons.ICON_STYLE) {

				@Override
				public void actionPerformed(ActionEvent e) {

					openStylerDialog();
				}
			}));
		}

		// System.out
		// .println("after styler" + (System.currentTimeMillis() - last));
		// last = System.currentTimeMillis();

		// ****************************************************************************
		// Show Attribute Table
		// ****************************************************************************
		if (isTableViewable()) {

			final AbstractAction showTableAction = new AbstractAction(
					GpCoreUtil.R("LayerToolMenu.table"), Icons.ICON_TABLE) {

				@Override
				public void actionPerformed(ActionEvent e) {
					AVDialogManager.dm_AttributeTable
							.getInstanceFor(
									(StyledFeaturesInterface<?>) styledLayer,
									MapLayerLegend.this,
									(StyledFeaturesInterface<?>) styledLayer,
									mapLegend);

				}
			};
			showTableAction.putValue(Action.SHORT_DESCRIPTION,
					GpCoreUtil.R("LayerToolMenu.table.tt"));
			toolPopup.add(new JMenuItem(showTableAction));
		}

		/**
		 * Button to remove the layer from the MapContext
		 */
		toolPopup.add(new JMenuItem(new AbstractAction(GpCoreUtil
				.R("LayerToolMenu.remove"), Icons.ICON_REMOVE) {

			@Override
			public void actionPerformed(ActionEvent e) {
				final String msg1 = GpCoreUtil
						.R("LayerPaneGroup.GetParentWindow.This.realy_want_to_remove_layer");
				final String msg2 = GpCoreUtil
						.R("LayerPaneGroup.GetParentWindow.JOptionPane.YesNoOption.remove");
				// i8ndone
				if (JOptionPane.showConfirmDialog(
						SwingUtil.getParentWindow(MapLayerLegend.this), msg1,
						msg2, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							removeLayer();
						}
					});
				}
			}

		}));

		/**
		 * Button to export the layer to a local folder
		 */
		if (isExportable()) {
			toolPopup.add(new JMenuItem(new AbstractAction(GpCoreUtil
					.R("LayerToolMenu.export"), Icons.ICON_EXPORT) {

				@Override
				public void actionPerformed(ActionEvent e) {
					export();
				}

			}));
		}

		return toolPopup;
	}

	@Override
	public String getUIClassID() {
		return SWINGX_MAP_LAYER_LEGEND_PANE_U_I;
	}

	public Boolean isExportable() {
		if (exportable == null)
			return false;
		return exportable.isExportable();
	}

	/**
	 * @return <code>true</code> if the {@link MapLayer} is possible to be
	 *         filtered with {@link FeatureLayerFilterDialog}
	 */
	public boolean isFilterable() {
		return styledLayer instanceof StyledFeaturesInterface<?>
				&& ((StyledFeaturesInterface<?>) styledLayer)
						.getAttributeMetaDataMap().sortedValuesVisibleOnly()
						.size() > 0;
	}

	/**
	 * @return <code>true</code> if a {@link Filter}/Query different than
	 *         {@link Query}.ALL is used
	 */
	public boolean isFiltered() {
		final Query query = getMapLayer().getQuery();
		if (query == null)
			return false;
		if (query.equals(Query.ALL))
			return false;
		if (query.getFilter() == null)
			return false;
		if (query.getFilter().equals(Filter.INCLUDE))
			return false;
		if (query.getFilter() instanceof FeatureOperationTreeFilter) {
			final FeatureOperationTreeFilter fotf = (FeatureOperationTreeFilter) query
					.getFilter();
			final String rule = fotf.getRule();
			if (rule.equals("1"))
				return false;
		}

		return true;
	}

	/**
	 * Is the {@link MapLayer} associated with this {@link MapLayerLegend}
	 * visible?
	 */
	public boolean isLayerVisible() {
		return getMapLayer().isVisible();
	}

	/**
	 * @return Does the {@link JMenuItem} to open the AtlasStyler appear?
	 */
	public boolean isStyleEditable() {
		final boolean rasterStylable = styledLayer instanceof StyledRasterInterface
				&& StyledLayerUtil
						.isStyleable((StyledRasterInterface<?>) styledLayer);
		/**
		 * getMapLayer().getFeatureSource()
		 * .getClass().getSimpleName().contains("WFSFeatureStore"))
		 * 
		 * We are doing a string comparison here, because 1. we don't want a
		 * dependency to gt-wfs only for one instance of, and 2. because the
		 * FeatureUtil.getLayerSourceObject queries the WFS and hence takes
		 * time-
		 * 
		 * Stefan Tzeggai, 22.10.2010
		 */

		final boolean featureStylable = (getMapLayer().getFeatureSource()
				.getClass().getSimpleName().contains("WFS"))
				|| (FeatureUtil.getLayerSourceObject(getMapLayer()) instanceof FeatureCollection);
		return featureStylable || rasterStylable;
	}

	/**
	 * @return Will the Table Menu Item be visible?
	 */
	public boolean isTableViewable() {
		return isFilterable();
	}

	/**
	 * Returns if the layer is toggled as transparent
	 */
	public boolean isTransparent() {
		return transparentToggled;
	}

	/**
	 * Opens a {@link FeatureLayerFilterDialog} that allows to filter the
	 * {@link FeatureCollection}
	 */
	public void openFilterDialog() {
		AtlasFeatureLayerFilterDialog fDialog = new AtlasFeatureLayerFilterDialog(
				this, (StyledFeaturesInterface<?>) styledLayer, mapLegend
						.getGeoMapPane().getMapPane(), getMapLayer());

		// Opens the modal dialog
		fDialog.setVisible(true);
		fDialog.requestFocus();
	}

	/**
	 * Opens the AtlasStyler dialog responsible for editing this layer's active
	 * style.
	 * 
	 * @return An instance of AtlasStylerDialog
	 */
	public StylerDialog openStylerDialog() {

		if (!(styledLayer instanceof StyledLayerInterface))
			throw new IllegalArgumentException(
					"The AtlasStyler can only be opened for objects that implement StyledLayerInterface");

		// atlasStyler.importStyle(styledObj.getStyle());

		StylerDialog stylerDialog = AVDialogManager.dm_Styler.getInstanceFor(
				styledLayer, mapLegend, styledLayer, getMapLayer(), mapLegend);

		return stylerDialog;
	}

	/**
	 * Nuetzlich wenn die Componente gedruckt (z.B. wenn ein Screenshot gemacht
	 * wird) wird. Dann werden wird der Hintergrund auf WEISS gesetzt.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	@Override
	public void print(Graphics g) {
		final Color orig = getBackground();
		setBackground(Color.WHITE);
		// wrap in try/finally so that we always restore the state
		try {
			super.print(g);
		} finally {
			setBackground(orig);
		}
	}

	/**
	 * Removes the {@link Query} that filters this {@link FeatureCollection} by
	 * setting the {@link Query} to {@link Query}.ALL
	 */
	public void removeFilter() {
		getMapLayer().setQuery(Query.ALL);
	}

	/**
	 * Removes the Layer
	 * 
	 * @return
	 */
	public boolean removeLayer() {
		return mapLegend.getGeoMapPane().getMapContext()
				.removeLayer(getMapLayer());
	}

	/**
	 * Change the visibility of the associated {@link MapLayer} (on/off)
	 */
	public boolean toggleVisibility() {
		getMapLayer().setVisible(!getMapLayer().isVisible());
		return getMapLayer().isVisible();
	}

	/**
	 * Applies the given {@link Style} to the {@link MapLayer}, while keeping
	 * any selection FTS
	 * 
	 * @param style
	 *            New {@link Style} to apply to the {@link MapLayer}.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public void updateStyle(final Style style) {

		if (StyledLayerUtil.updateMapLayerStyleIfChangedAndKeepSelection(
				getMapLayer(), style)) {
			mapLegend.recreateLayerList(getMapLayer());
		}
	}

	/**
	 * Notification from the <code>UIManager</code> that the L&F has changed.
	 * Replaces the current UI object with the latest version from the
	 * <code>UIManager</code>.
	 * 
	 * @see javax.swing.JComponent#updateUI
	 */
	@Override
	public void updateUI() {

		if (mapLegend == null || mapLegend.getGeoMapPane() == null)
			return;

		setUI((BasicMapLayerLegendPaneUI) LookAndFeelAddons.getUI(this,
				MapLayerLegend.class));

	}

	/**
	 * Zoom the {@link GeoMapPane} to the extends of this {@link MapLayer}
	 */
	public void zoomTo() {
		try {

			mapLegend.getGeoMapPane().getMapPane()
					.zoomToLayer(getMapLayer(), true);

		} catch (java.lang.IllegalArgumentException e) {
			if (mapLayer.getFeatureSource() != null
					&& mapLayer.getFeatureSource() instanceof JDBCFeatureSource) {

				// i8n
				ExceptionDialog
						.show(MapLayerLegend.this,
								new IllegalStateException(
										"<html>The CRS could not be determined. Maybe the geometry column is not correctly described in 'geometry_columns'?</html>"));
			}
		}
	}

	/**
	 * Access to the Geotool's {@link MapLayer} that is presented in this
	 * legend.
	 */
	public MapLayer getMapLayer() {
		return mapLayer;
	}

	/**
	 * This {@link MapLayerLegend} is embedded into a {@link MapLegend}. This
	 * method tells the {@link MapLegend} to recreate the {@link MapLayerLegend}
	 * .
	 */
	public void recreateLegend() {
		mapLegend.recreateLayerList(mapLayer);
	}

}
