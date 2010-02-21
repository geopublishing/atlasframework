/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package skrueger.creator;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.geotools.map.MapLayer;

import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.jfree.feature.style.FeatureChartStyle;
import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.dp.layer.DpLayerVectorFeatureSource;
import skrueger.atlas.dp.layer.LayerStyle;
import skrueger.atlas.gui.map.AtlasMapLegend;
import skrueger.atlas.map.Map;
import skrueger.creator.gui.DesignAtlasChartJDialog;
import skrueger.creator.gui.DesignAtlasStylerDialog;
import skrueger.creator.gui.datapool.EditDpEntryGUI;
import skrueger.creator.gui.map.DesignAtlasMapLegend;
import skrueger.creator.gui.map.DesignMapViewJDialog;
import skrueger.creator.gui.map.EditMapJDialog;
import skrueger.creator.gui.map.ManageChartsForMapDialog;
import skrueger.geotools.StyledFeaturesInterface;
import skrueger.swing.AtlasDialog;
import skrueger.swing.CancellableDialogManager;
import skrueger.swing.DialogManager;

/**
 * This class manages all {@link DialogManager}s used in GeoPublisher. It hold
 * simple static public references to them.
 * 
 * TODO This would be a great place to
 */
@SuppressWarnings("unchecked")
public class GPDialogManager {

	/** DialogManager for EditAttributesDialogs **/
	final static public CancellableDialogManager<DpEntry<? extends ChartStyle>, EditDpEntryGUI> dm_EditDpEntry = new CancellableDialogManager<DpEntry<? extends ChartStyle>, EditDpEntryGUI>() {

		@Override
		public EditDpEntryGUI getInstanceFor(
				final DpEntry<? extends ChartStyle> key, final Component owner,
				final Object... constArgs) {

			return bringup(super.getInstanceFor(key,
					new DialogManager.FactoryInterface() {

						@Override
						public EditDpEntryGUI create() {
							return new EditDpEntryGUI(
									owner,
									(DpEntry<? extends ChartStyle>) constArgs[0]);
						}

					}));
		}
	};

	/** DialogManager for EditAttributesDialogs **/
	final static public CancellableDialogManager<Map, EditMapJDialog> dm_EditMapEntry = new CancellableDialogManager<Map, EditMapJDialog>() {

		@Override
		public EditMapJDialog getInstanceFor(final Map key,
				final Component owner, final Object... constArgs) {

			return bringup(super.getInstanceFor(key,
					new DialogManager.FactoryInterface() {

						@Override
						public EditMapJDialog create() {
							return new EditMapJDialog(owner, (Map) constArgs[0]);
						}

					}));
		}
	};

	/** DialogManager for EditAttributesDialogs **/
	final static public CancellableDialogManager<DpLayerVectorFeatureSource, EditAttributesJDialog> dm_EditAttribute = new CancellableDialogManager<DpLayerVectorFeatureSource, EditAttributesJDialog>() {

		@Override
		public EditAttributesJDialog getInstanceFor(
				final DpLayerVectorFeatureSource key, final Component owner,
				final Object... constArgs) {

			return bringup(super.getInstanceFor(key,
					new DialogManager.FactoryInterface() {

						@Override
						public EditAttributesJDialog create() {
							return new EditAttributesJDialog(owner,
									(DpLayerVectorFeatureSource) constArgs[0]);
						}

					}));
		}
	};

	/** A DialogManager for DesignMapViewJDialogs **/
	final static public CancellableDialogManager<Map, DesignMapViewJDialog> dm_MapComposer = new CancellableDialogManager<Map, DesignMapViewJDialog>() {

		@Override
		public DesignMapViewJDialog getInstanceFor(final Map key,
				final Component owner, final Object... constArgs) {

			return bringup(super.getInstanceFor(key,
					new DialogManager.FactoryInterface() {

						// @SuppressWarnings("deprecation")
						@Override
						public DesignMapViewJDialog create() {
							final Map map = (Map) constArgs[0];

							// JDialog waitDialog = AVUtil.getWaitDialog(owner,
							// AtlasViewer.R(
							// "AmlViewer.process.opening_map",
							// map.getTitle()));

							Cursor oldCursor = owner.getCursor();
							try {
								owner
										.setCursor(Cursor
												.getPredefinedCursor(Cursor.WAIT_CURSOR));

								final DesignMapViewJDialog designMapViewJDialog = new DesignMapViewJDialog(
										owner, map);

								// waitDialog.dispose();

								return designMapViewJDialog;

							} finally {
								owner.setCursor(oldCursor);
							}
						}

					}));
		}
	};

	public static final CancellableDialogManager<FeatureChartStyle, DesignAtlasChartJDialog> dm_DesignCharts = new CancellableDialogManager<FeatureChartStyle, DesignAtlasChartJDialog>() {

		@Override
		public DesignAtlasChartJDialog getInstanceFor(FeatureChartStyle key,
				final Component owner, final Object... constArgs) {

			final AtlasMapLegend mapLegend = (AtlasMapLegend) constArgs[1];

			final WindowAdapter listenerForMapLegendSelectionButtons = new WindowAdapter() {

				@Override
				public void windowClosed(WindowEvent e) {
					/**
					 * Maybe it's time to show the selection-related buttons?!
					 */
					mapLegend.showOrHideSelectionButtons();
				}
			};

			return bringup(super.getInstanceFor(key,
					new DialogManager.FactoryInterface() {

						@Override
						public DesignAtlasChartJDialog create() {

							return new DesignAtlasChartJDialog(owner,
									(FeatureChartStyle) constArgs[0],
									mapLegend,
									(StyledFeaturesInterface<?>) constArgs[2],
									(AtlasConfigEditable) constArgs[3]);

						}

						@Override
						public void afterCreation(AtlasDialog newInstance) {
							if (mapLegend != null) {

								mapLegend.showOrHideSelectionButtons();

								newInstance
										.addWindowListener(listenerForMapLegendSelectionButtons);
							}

						};

						@Override
						public void beforeDispose(AtlasDialog newInstance) {
							if (mapLegend != null) {

								newInstance
										.removeWindowListener(listenerForMapLegendSelectionButtons);
							}

						};

					}));
		}

	};

	public static final DialogManager<Object, DesignAtlasStylerDialog> dm_DesignAtlasStyler = new DialogManager<Object, DesignAtlasStylerDialog>() {

		@Override
		public DesignAtlasStylerDialog getInstanceFor(Object key,
				final Component owner, final Object... constArgs) {
			return bringup(super.getInstanceFor(key,
					new DialogManager.FactoryInterface() {

						@Override
						public DesignAtlasStylerDialog create() {
							final DpLayerVectorFeatureSource dpl = (DpLayerVectorFeatureSource) constArgs[0];
							final DesignAtlasMapLegend mapLegend = (DesignAtlasMapLegend) constArgs[1];
							final MapLayer mapLayer = (MapLayer) constArgs[2];
							final LayerStyle layerStyle = (LayerStyle) constArgs[3];

							return new DesignAtlasStylerDialog(owner, dpl,
									mapLegend, mapLayer, layerStyle);
						}

					}));
		}
	};

	public static final DialogManager<DpEntry<? extends ChartStyle>, ManageChartsForMapDialog> dm_ManageCharts = new DialogManager<DpEntry<? extends ChartStyle>, ManageChartsForMapDialog>() {

		@Override
		public ManageChartsForMapDialog getInstanceFor(
				DpEntry<? extends ChartStyle> key, final Component owner,
				final Object... constArgs) {
			return bringup(super.getInstanceFor(key,
					new DialogManager.FactoryInterface() {

						@Override
						public ManageChartsForMapDialog create() {
							final DpLayerVectorFeatureSource dpl = (DpLayerVectorFeatureSource) constArgs[0];
							final DesignAtlasMapLegend mapLegend = (DesignAtlasMapLegend) constArgs[1];

							return new ManageChartsForMapDialog(owner, dpl,
									mapLegend);
						}

					}));
		}
	};

}
