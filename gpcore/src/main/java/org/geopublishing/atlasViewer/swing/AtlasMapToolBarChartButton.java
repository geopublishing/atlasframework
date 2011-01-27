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

import java.awt.event.ActionEvent;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPopupMenu;

import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;
import org.geopublishing.atlasViewer.map.Map;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;

import de.schmitzm.geotools.styling.StyledLayerInterface;
import de.schmitzm.jfree.chart.style.ChartStyle;
import de.schmitzm.swing.SmallButton;

/**
 * This {@link JButton} opens a {@link JPopupMenu} that lists all available
 * charts for this {@link MapContext}.
 */
public class AtlasMapToolBarChartButton extends SmallButton {

	private final MapContext mapContext;
	private final HashMap<String, StyledLayerInterface<?>> rememberId2StyledLayer;
	private final Map map;
	private final MapLegend mapLegend;

	public AtlasMapToolBarChartButton(
			final MapContext mapContext,
			final Map map,
			final HashMap<String, StyledLayerInterface<?>> rememberId2StyledLayer,
			final MapLegend mapLegend) {

		this.mapContext = mapContext;
		this.map = map;
		this.rememberId2StyledLayer = rememberId2StyledLayer;
		this.mapLegend = mapLegend;

//		setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

		setAction(new AbstractAction(AtlasViewerGUI
				.R("AtlasMapToolBarChartButton.Title"), Icons.ICON_CHART_MEDIUM) {
//			setAction(new AbstractAction(AtlasViewer
//					.R("AtlasMapToolBarChartButton.PopupMenu.Title"), Icons.ICON_CHART_BIG) {
			

			@Override
			public void actionPerformed(final ActionEvent e) {

				final JPopupMenu popup = new JPopupMenu();
//				popup.add(new JLabel(AtlasViewer
//						.R("AtlasMapToolBarChartButton.PopupMenu.Title")));

				for (final MapLayer mLayer : mapContext.getLayers()) {
					final StyledLayerInterface<?> styledLayerInterface = rememberId2StyledLayer
							.get(mLayer.getTitle());
					if (!(styledLayerInterface instanceof DpLayerVectorFeatureSource))
						continue;
					final DpLayerVectorFeatureSource dlvfs = (DpLayerVectorFeatureSource) styledLayerInterface;
					for (final ChartStyle chart : dlvfs.getCharts()) {

						if (map.containsDpe(mLayer.getTitle())) {
							/*
							 * The layer is a standard part of the map, so let's
							 * see if it should be visible
							 */
							if (map.getAvailableChartIDsFor(mLayer.getTitle())
									.contains(chart.getID())) {
								popup.add(new OpenChartAction(chart, dlvfs));
							}

						} else {
							popup.add(new OpenChartAction(chart, dlvfs));
						}

					}
				}

				popup.show(AtlasMapToolBarChartButton.this, 1,
						AtlasMapToolBarChartButton.this.getBounds().height);
			}

		});
		setToolTipText(AtlasViewerGUI.R("MapPaneButtons.ChartButton.TT"));

		setBorder(BorderFactory.createCompoundBorder(getBorder(),BorderFactory.createEmptyBorder(0, 0, 0, 3)));
	}

	/**
	 * A n {@link Action} that opens a {@link ChartStyle}
	 */
	class OpenChartAction extends AbstractAction {

		private final ChartStyle chart;
		private final DpLayerVectorFeatureSource dlvfs;

		public OpenChartAction(final ChartStyle chart,
				final DpLayerVectorFeatureSource dlvfs) {
			super(chart.getTitleStyle().getLabel());
			this.chart = chart;
			this.dlvfs = dlvfs;

			/* The button is disabled if the chart is already open */
			OpenChartAction.this.setEnabled(!AVDialogManager.dm_Charts.isVisibleFor(chart));

			OpenChartAction.this.putValue(Action.SHORT_DESCRIPTION, chart
					.getDescStyle().getLabel());
			OpenChartAction.this.putValue(Action.SMALL_ICON, chart.getType()
					.getIcon());
			// setIcon(chart.getType().getIcon());
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			dlvfs.openChart(AtlasMapToolBarChartButton.this, chart.getID(),
					mapLegend);
		}

	}
}
