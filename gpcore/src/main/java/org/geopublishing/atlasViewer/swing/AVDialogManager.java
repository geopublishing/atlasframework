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

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.StyleChangeListener;
import org.geopublishing.atlasStyler.StyleChangedEvent;
import org.geopublishing.atlasStyler.swing.StylerDialog;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;
import org.geopublishing.atlasViewer.dp.layer.DpLayerRaster_Reader;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;
import org.geopublishing.atlasViewer.dp.layer.LayerStyle;
import org.geotools.map.MapLayer;
import org.geotools.styling.Style;

import de.schmitzm.geotools.styling.StyledFeaturesInterface;
import de.schmitzm.jfree.chart.style.ChartStyle;
import de.schmitzm.swing.AtlasDialog;
import de.schmitzm.swing.DialogManager;
import de.schmitzm.swing.ExceptionDialog;

/**
 * This class manages all {@link DialogManager}s used in Geopublisher. It hold
 * simple static public references to them.
 */
@SuppressWarnings("unchecked")
public class AVDialogManager {

	final static public DialogManager<StyledFeaturesInterface<?>, AttributeTableJDialog> dm_AttributeTable = new DialogManager<StyledFeaturesInterface<?>, AttributeTableJDialog>() {

		@Override
		public AttributeTableJDialog getInstanceFor(
				final StyledFeaturesInterface<?> key, final Component owner,
				final Object... constArgs) {

			try {

				return bringup(super.getInstanceFor(key,
						new DialogManager.FactoryInterface() {

							private MapLegend mapLegend = null;

							@Override
							public AttributeTableJDialog create() {

								mapLegend = (MapLegend) constArgs[1];
								final StyledFeaturesInterface<?> styledObj = (StyledFeaturesInterface<?>) constArgs[0];

								return new AttributeTableJDialog(owner,
										styledObj, mapLegend);
							}

							@Override
							public void afterCreation(AtlasDialog newInstance) {
								if (mapLegend != null)
									mapLegend.showOrHideSelectionButtons();
								super.afterCreation(newInstance);
							}

							@Override
							public void beforeDispose(AtlasDialog newInstance) {
								if (mapLegend != null)
									mapLegend.showOrHideSelectionButtons();
							};

						}));

			} catch (Exception e) {
				ExceptionDialog.show(owner, e);
				return null;
			}
		}
	};

	public static final DialogManager<ChartStyle, AtlasChartJDialog> dm_Charts = new DialogManager<ChartStyle, AtlasChartJDialog>() {

		@Override
		public AtlasChartJDialog getInstanceFor(final ChartStyle key,
				final Component owner, final Object... constArgs) {

			try {

				final AtlasMapLegend mapLegend = (AtlasMapLegend) constArgs[1];

				final WindowAdapter listenerForMapLegendSelectionButtons = new WindowAdapter() {

					@Override
					public void windowClosed(WindowEvent e) {
						/**
						 * Maybe it's time to show the selection-related
						 * buttons?!
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

								return new AtlasChartJDialog(
										owner,
										(ChartStyle) constArgs[0],
										mapLegend,
										(StyledFeaturesInterface<?>) constArgs[2]);
							}

						}));
			} catch (Exception e) {
				ExceptionDialog.show(owner, e);
				return null;
			}
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
			try {
				return bringup(super.getInstanceFor(key,
						new DialogManager.FactoryInterface() {

							@Override
							public StylerDialog create() {
								final StyledFeaturesInterface<?> styledFeatures = (StyledFeaturesInterface<?>) constArgs[0];
								final MapLayer mapLayer = (MapLayer) constArgs[1];
								final MapLegend mapLegend = (MapLegend) constArgs[2];

								// // Create a copy of the Style.. otherwise
								// some
								// // updates where not detected
								// DuplicatingStyleVisitor dsv = new
								// DuplicatingStyleVisitor();
								// dsv.visit(mapLayer.getStyle());
								// Style importStyleCopy = (Style)
								// dsv.getCopy();

								/***********************************************************************
								 * First create the AtlasStyler ....
								 */
								final AtlasStylerVector atlasStyler = new AtlasStylerVector(
										styledFeatures, mapLayer.getStyle(),
										mapLayer, null, true);

								final MapLayerLegend mapLayerLegend = mapLegend
										.getLayerLegendForId(styledFeatures
												.getId());

								// This listener informs the MapLayerLegend,
								// resulting in a new legend and repained
								// JMapPane
								atlasStyler
										.addListener(new StyleChangeListener() {

											@Override
											public void changed(
													StyleChangedEvent e) {
												final Style style = atlasStyler
														.getStyle();
												styledFeatures.setStyle(style);
												mapLayerLegend
														.updateStyle(style);
											}

										});

								return new StylerDialog(owner, atlasStyler,
										mapLegend == null ? null : mapLegend
												.getGeoMapPane().getMapPane());
							}

						}));
			} catch (Exception e) {
				ExceptionDialog.show(owner, e);
				return null;
			}
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
		public AtlasStylerDialog getInstanceFor(final Object key,
				final Component owner, final Object... constArgs) {
			try {
				return bringup(super.getInstanceFor(key,
						new DialogManager.FactoryInterface() {

							@Override
							public AtlasStylerDialog create() {
								final DpLayer dpl = (DpLayer) constArgs[0];
								final AtlasMapLegend mapLegend = (AtlasMapLegend) constArgs[1];
								final MapLayer mapLayer = (MapLayer) constArgs[2];
								final LayerStyle layerStyle = (LayerStyle) constArgs[3];

								if (dpl instanceof DpLayerVectorFeatureSource)
									return new AtlasStylerDialog(owner,
											(DpLayerVectorFeatureSource) dpl,
											mapLegend, mapLayer, layerStyle);

								if (dpl instanceof DpLayerRaster_Reader)
									return new AtlasStylerDialog(owner,
											(DpLayerRaster_Reader) dpl,
											mapLegend, mapLayer, layerStyle);

								throw new IllegalArgumentException();

							}

						}));
			} catch (Exception e) {
				ExceptionDialog.show(owner, e);
				return null;
			}
		}

	};

}
