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
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.List;
import java.util.WeakHashMap;

import javax.swing.AbstractAction;
import javax.swing.JButton;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.jfree.chart.JFreeChart;

import de.schmitzm.geotools.gui.SelectableXMapPane;
import de.schmitzm.geotools.gui.XMapPane;
import de.schmitzm.geotools.selection.ChartSelectionSynchronizer;
import de.schmitzm.geotools.selection.StyledFeatureLayerSelectionModel;
import de.schmitzm.geotools.selection.StyledLayerSelectionModel;
import de.schmitzm.geotools.selection.StyledLayerSelectionModelSynchronizer;
import de.schmitzm.geotools.styling.StyledFeaturesInterface;
import de.schmitzm.jfree.JFreeChartUtil;
import de.schmitzm.jfree.chart.selection.DatasetSelectionModel;
import de.schmitzm.jfree.chart.style.ChartStyle;
import de.schmitzm.jfree.feature.FeatureDatasetSelectionModel;
import de.schmitzm.jfree.feature.style.FeatureChartStyle;
import de.schmitzm.jfree.feature.style.FeatureChartUtil;
import de.schmitzm.swing.AtlasDialog;
import de.schmitzm.swing.ExceptionDialog;
import de.schmitzm.swing.JPanel;
import de.schmitzm.swing.SmallButton;
import de.schmitzm.swing.SwingUtil;
import de.schmitzm.swing.swingworker.AtlasStatusDialog;
import de.schmitzm.swing.swingworker.AtlasSwingWorker;

public class AtlasChartJDialog extends AtlasDialog {

	final static Logger LOGGER = Logger.getLogger(AtlasChartJDialog.class);

	protected final ChartStyle chartStyle;

	/**
	 * This {@link AtlasChartJPanel} actually displays the chart.
	 */
	protected volatile AtlasChartJPanel chartPanel;

	/**
	 * A reference to the {@link AtlasMapLegend} of the {@link XMapPane} that
	 * shows the geometries for the data-points.
	 **/
	private final AtlasMapLegend mapLegend;

	/**
	 * A reference to the {@link SelectableXMapPane} that shows the geometries
	 * for the data-points.
	 **/
	private final SelectableXMapPane mapPane;

	/**
	 * The {@link StyledFeaturesInterface} this {@link ChartStyle} is based on
	 **/
	private final StyledFeaturesInterface<?> styledLayer;

	/**
	 * Private references to the listeners that have been inserted by this
	 * class. In {@link #dispose()}, the reverence are nulled and any
	 * {@link WeakHashMap} forgets about the listeners.
	 */
	private final HashSet<ChartSelectionSynchronizer> listenersWeInserted = new HashSet<ChartSelectionSynchronizer>();

	public AtlasChartJDialog(final Component owner,
			final ChartStyle chartStyle, final AtlasMapLegend mapLegend,
			final StyledFeaturesInterface<?> styledLayer_) {

		super(owner);

		this.chartStyle = chartStyle;
		this.mapLegend = mapLegend;

		styledLayer = styledLayer_;

		// SK: Sadly has no effect :-( .. Only for JFrames?
		// setIconImage(BasicTaskPaneUI.ICON_TABLE.getImage());

		this.mapPane = mapLegend != null ? (mapLegend.getGeoMapPane() != null ? mapLegend
				.getGeoMapPane().getMapPane() : null)
				: null;

		// Filter filter = (mapLayer != null && mapLayer.getQuery() != null) ?
		// mapLayer.getQuery()
		// .getFilter() : Filter.INCLUDE;

		initGUI();

		SwingUtil.setRelativeFramePosition(this, owner, SwingUtil.BOUNDS_OUTER,
				SwingUtil.NORTHWEST);
	}

	/**
	 * Lazily creates the {@link AtlasChartJPanel}
	 */
	protected AtlasChartJPanel getChartPanel(final AtlasMapLegend mapLegend,
			final StyledFeaturesInterface<?> styledLayer_) {

		if (chartPanel == null) {

			JFreeChart chart;

			if (getChartStyle() instanceof FeatureChartStyle) {
				final FeatureChartStyle fschart = (FeatureChartStyle) getChartStyle();

				// Check if normalization is enabled, and then configure the
				// visualization of unit strings accordingly.
				// If the first attribute is normalized, all are!
				// This check is also done in updateChart method.
				{
					boolean visible = !fschart.isAttributeNormalized(0);
					for (int axisIdx = 0; axisIdx < fschart.getAxisCount(); axisIdx++) {
						fschart.getAxisStyle(axisIdx).setUnitVisible(visible);
					}
				}

				AtlasStatusDialog statusDialog = new AtlasStatusDialog(
						AtlasChartJDialog.this,
						AtlasViewerGUI.R("dialog.title.wait"),
						AtlasViewerGUI.R("dialog.title.wait"));
				AtlasSwingWorker<JFreeChart> asw = new AtlasSwingWorker<JFreeChart>(
						statusDialog) {
					@Override
					protected JFreeChart doInBackground() throws Exception {
						return fschart.applyToFeatureCollection(styledLayer_
								.getFeatureCollection());
					}
				};
				try {
					chart = asw.executeModal();
				} catch (Exception e) {
					ExceptionDialog.show(e);
					chart = null;
				}
			} else {
				throw new RuntimeException(
						"chartStyles other than ? extends FeatureChartStyle not supported yet!");
			}

			chartPanel = new AtlasChartJPanel(chart, styledLayer_, mapLegend);

			// Connect to every chart selection model to refresh the
			// panel on selection changes
			for (final DatasetSelectionModel<?, ?, ?> selModel : FeatureChartUtil
					.getFeatureDatasetSelectionModelFor(chart)) {
				selModel.addSelectionListener(chartPanel);
			}

			final StyledLayerSelectionModel<?> anySelectionModel = mapLegend != null ? mapLegend
					.getRememberSelection(getStyledLayer().getId()) : null;

			if ((anySelectionModel instanceof StyledFeatureLayerSelectionModel)) {
				final StyledFeatureLayerSelectionModel selectionModel = (StyledFeatureLayerSelectionModel) anySelectionModel;

				// get the selectionmodel(s) of the chart
				final List<FeatureDatasetSelectionModel<?, ?, ?>> datasetSelectionModelFor = FeatureChartUtil
						.getFeatureDatasetSelectionModelFor(chart);

				for (final FeatureDatasetSelectionModel<?, ?, ?> dsm : datasetSelectionModelFor) {

					final ChartSelectionSynchronizer synchronizer = new ChartSelectionSynchronizer(
							selectionModel, dsm);

					// Add the synchronizer as a listener
					selectionModel
							.addSelectionListener((StyledLayerSelectionModelSynchronizer) synchronizer);
					dsm.addSelectionListener(synchronizer);

					// Keep a reference to the listener, as they may be stored
					// in a WeakHashMap. We remove all listeners in the
					// #dispose() method.
					listenersWeInserted.add(synchronizer);

					selectionModel.refreshSelection();
				}
			}

		}
		return chartPanel;
	}

	/**
	 * Create the GUI
	 */
	protected void initGUI() {
		setTitle(getChartStyle().getTitleStyle().getLabel());

		JPanel contentpane = new JPanel(new MigLayout());

		contentpane.add(getChartPanel(getMapLegend(), getStyledLayer()),
				"top, wrap");

		getChartPanel(mapLegend, styledLayer).addZoomToFeatureExtends(mapPane,
				mapLegend, getStyledLayer(),
				getChartPanel(mapLegend, getStyledLayer()));

		// Adds an ACTION button to zoom to the full extent of the chart
		chartPanel.getToolBar().add(
				createZoomToFullChartExtentButton(chartPanel, chartStyle), 0);

		contentpane.add(getButtonsPanel(), "growx");

		setContentPane(contentpane);
		pack();
	}

	private JPanel getButtonsPanel() {
		JPanel buttons = new JPanel(new MigLayout("fillx", "[100%]"));

		{
			buttons.add(getOkButton(), "right, tag ok");
		}

		return buttons;
	}

	/**
	 * Adds a Button to the ToolBar of the {@link AtlasChartJPanel}, that zoom
	 * the chart to full extends. has to be done in the
	 * {@link AtlasChartJDialog}, because {@link AtlasChartJPanel} doesn't know
	 * about the {@link ChartStyle}.
	 * 
	 * @return
	 */
	public static JButton createZoomToFullChartExtentButton(
			final AtlasChartJPanel chartPanel, final ChartStyle chartStyle) {
		/**
		 * Add an Action to SET the selection.
		 */
		JButton resetZoomTool = new SmallButton(new AbstractAction("",
				AtlasChartJPanel.ICON_FULL_EXTEND) {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (chartStyle.getPlotStyle() != null) {
					JFreeChartUtil.zoomPlotToFullExtend(chartPanel.getChart()
							.getPlot(), null, chartStyle.getPlotStyle()
							.isCenterOriginSymetrically());
				} else {
					JFreeChartUtil.zoomPlotToFullExtend(chartPanel.getChart()
							.getPlot(), null, false);
				}
			}

		}, AtlasViewerGUI.R("AtlasChartJPanel.zoomFullExtent.tt"));

		return resetZoomTool;
	}

	public ChartStyle getChartStyle() {
		return chartStyle;
	}

	public AtlasMapLegend getMapLegend() {
		return mapLegend;
	}

	public StyledFeaturesInterface<?> getStyledLayer() {
		return styledLayer;
	}

	/**
	 * This method is checking if the DialogManager holds instances for any of
	 * this layers charts.
	 */
	static public boolean isOpenForLayer(DpEntry<? extends ChartStyle> dpl) {

		if (dpl == null)
			return false;

		for (ChartStyle cs : dpl.getCharts()) {
			if (AVDialogManager.dm_Charts.isVisibleFor(cs))
				return true;
		}

		return false;
	}

	@Override
	public void dispose() {

		if (isDisposed)
			return;

		// Remove all references to synchronizers we added. This makes the
		// WeakHashMap will forget about them.
		for (ChartSelectionSynchronizer d : listenersWeInserted) {
			d.setEnabled(false);
			d = null;
		}
		listenersWeInserted.clear();

		if (chartPanel != null)
			chartPanel.dispose();
		chartPanel = null;

		super.dispose();
	}
}
