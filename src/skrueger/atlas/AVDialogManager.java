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
package skrueger.atlas;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.geotools.map.MapLayer;
import org.geotools.styling.Style;
import org.opengis.sld.LayerStyle;

import schmitzm.jfree.chart.style.ChartStyle;
import skrueger.atlas.dp.layer.DpLayerVectorFeatureSource;
import skrueger.atlas.gui.AtlasChartJDialog;
import skrueger.atlas.gui.AtlasStylerDialog;
import skrueger.atlas.gui.AttributeTableJDialog;
import skrueger.atlas.gui.MapLayerLegend;
import skrueger.atlas.gui.MapLegend;
import skrueger.atlas.gui.map.AtlasMapLegend;
import skrueger.geotools.StyledFeaturesInterface;
import skrueger.geotools.StyledRasterInterface;
import skrueger.sld.AtlasStyler;
import skrueger.sld.StyleChangeListener;
import skrueger.sld.StyleChangedEvent;
import skrueger.sld.gui.AtlasStylerRasterDialog;
import skrueger.sld.gui.StylerDialog;
import skrueger.swing.AtlasDialog;
import skrueger.swing.DialogManager;

/**
 * This class manages all {@link DialogManager}s used in GeoPublisher. It hold
 * simple static public references to them.
 */
@SuppressWarnings("unchecked")
public class AVDialogManager {

	/**
	 * The key is a {@link StyledRasterInterface}. Parameters to get an instance
	 * are: KEY, OWNERGUI, AConfig, AtlasMapLegend (optional)
	 */
	final public static DialogManager<Object, AtlasStylerRasterDialog> dm_AtlasRasterStyler = new DialogManager<Object, AtlasStylerRasterDialog>() {

		@Override
		public AtlasStylerRasterDialog getInstanceFor(final Object key,
				final Component owner, final Object... constArgs) {
			return bringup(super.getInstanceFor(key,
					new DialogManager.FactoryInterface() {

						@Override
						public AtlasStylerRasterDialog create() {
							final StyledRasterInterface styledRaster = (StyledRasterInterface) key;
							final AtlasConfig ac = (AtlasConfig) constArgs[0];

							/***********************************************************************
							 * First create the AtlasStyler ....
							 */
							final AtlasStylerRasterDialog atlasRasterStyler = new AtlasStylerRasterDialog(
									owner, styledRaster, ac);

							if (constArgs.length == 2 && constArgs[1] != null) {
								final MapLayerLegend mapLayerLegend = (MapLayerLegend) constArgs[1];

								atlasRasterStyler
										.addListener(new StyleChangeListener() {

											@Override
											public void changed(
													StyleChangedEvent e) {
												styledRaster.setStyle(e
														.getStyle());
												mapLayerLegend.updateStyle(e
														.getStyle());

												// Because events from this
												// Dialog might also have
												// changed the Gap on-off state,
												// without changing the style,
												// we always recreate the legend
												// for this layer
												mapLayerLegend.recreateLegend();
											}
										});
							}

							return atlasRasterStyler;
						}

					}));
		}

	};

	final static public DialogManager<StyledFeaturesInterface<?>, AttributeTableJDialog> dm_AttributeTable = new DialogManager<StyledFeaturesInterface<?>, AttributeTableJDialog>() {

		@Override
		public AttributeTableJDialog getInstanceFor(
				final StyledFeaturesInterface<?> key, final Component owner,
				final Object... constArgs) {

			return bringup(super.getInstanceFor(key,
					new DialogManager.FactoryInterface() {

						private MapLegend mapLegend = null;

						@Override
						public AttributeTableJDialog create() {

							mapLegend = (MapLegend) constArgs[1];
							final StyledFeaturesInterface<?> styledObj = (StyledFeaturesInterface<?>) constArgs[0];

							return new AttributeTableJDialog(owner, styledObj,
									mapLegend);
						}

						@Override
						public void afterCreation(AtlasDialog newInstance) {
							if (mapLegend != null)
								mapLegend.showOrHideSelectionButtons();
							super.afterCreation(newInstance);
						}

						public void beforeDispose(AtlasDialog newInstance) {
							if (mapLegend != null)
								mapLegend.showOrHideSelectionButtons();
						};

					}));

		}

	};

	public static final DialogManager<ChartStyle, AtlasChartJDialog> dm_Charts = new DialogManager<ChartStyle, AtlasChartJDialog>() {

		@Override
		public AtlasChartJDialog getInstanceFor(final ChartStyle key,
				final Component owner, final Object... constArgs) {
			final MapLegend mapLegend = (MapLegend) constArgs[1];

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

						@Override
						public AtlasChartJDialog create() {

							return new AtlasChartJDialog(owner,
									(ChartStyle) constArgs[0], mapLegend,
									(StyledFeaturesInterface<?>) constArgs[2]);
						}

					}));

		}

	};

	/**
	 * The KEY for the AtlasStyler dialog manager is an Object. Valid types for
	 * KEY are {@link LayerStyle} and {@link StyledFeaturesInterface}
	 * 
	 * TODO Move to AS?!
	 * **/
	public static final DialogManager<Object, StylerDialog> dm_Styler = new DialogManager<Object, StylerDialog>() {

		@Override
		public StylerDialog getInstanceFor(Object key, final Component owner,
				final Object... constArgs) {
			return bringup(super.getInstanceFor(key,
					new DialogManager.FactoryInterface() {

						@Override
						public StylerDialog create() {
							final StyledFeaturesInterface<?> styledFeatures = (StyledFeaturesInterface<?>) constArgs[0];
							final MapLayer mapLayer = (MapLayer) constArgs[1];
							final MapLegend mapLegend = (MapLegend) constArgs[2];

//							// Create a copy of the Style.. otherwise some
//							// updates where not detected
//							DuplicatingStyleVisitor dsv = new DuplicatingStyleVisitor();
//							dsv.visit(mapLayer.getStyle());
//							Style importStyleCopy = (Style) dsv.getCopy();

							/***********************************************************************
							 * First create the AtlasStyler ....
							 */
							final AtlasStyler atlasStyler = new AtlasStyler(
									styledFeatures, mapLayer.getStyle(), mapLegend,
									mapLayer);

							final MapLayerLegend mapLayerLegend = mapLegend
									.getLayerLegendForId(styledFeatures.getId());

							// This listener informs the MapLayerLegend,
							// resulting in a new legend and repained JMapPane
							atlasStyler.addListener(new StyleChangeListener() {

								@Override
								public void changed(StyleChangedEvent e) {
									final Style style = atlasStyler.getStyle();
									styledFeatures.setStyle(style);
									mapLayerLegend.updateStyle(style);
								}

							});

							return new StylerDialog(owner, atlasStyler);
						}

					}));
		}

	};

	/**
	 * The KEY for the AtlasStyler dialog manager is an Object. Valid types for
	 * KEY are {@link LayerStyle} and {@link StyledFeaturesInterface}
	 * 
	 * TODO Move to AS?!
	 * **/
	public static final DialogManager<Object, AtlasStylerDialog> dm_AtlasStyler = new DialogManager<Object, AtlasStylerDialog>() {

		@Override
		public AtlasStylerDialog getInstanceFor(Object key,
				final Component owner, final Object... constArgs) {
			return bringup(super.getInstanceFor(key,
					new DialogManager.FactoryInterface() {

						@Override
						public AtlasStylerDialog create() {
							final DpLayerVectorFeatureSource dpl = (DpLayerVectorFeatureSource) constArgs[0];
							final AtlasMapLegend mapLegend = (AtlasMapLegend) constArgs[1];
							final MapLayer mapLayer = (MapLayer) constArgs[2];
							final skrueger.atlas.dp.layer.LayerStyle layerStyle = (skrueger.atlas.dp.layer.LayerStyle) constArgs[3];

							return new AtlasStylerDialog(owner, dpl, mapLegend,
									mapLayer, layerStyle);
						}

					}));
		}

	};

}
