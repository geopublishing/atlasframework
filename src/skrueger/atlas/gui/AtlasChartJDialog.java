package skrueger.atlas.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.JButton;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;

import schmitzm.geotools.gui.SelectableXMapPane;
import schmitzm.jfree.JFreeChartUtil;
import schmitzm.jfree.chart.selection.DatasetSelectionModel;
import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.jfree.feature.FeatureDatasetSelectionModel;
import schmitzm.jfree.feature.style.FeatureChartStyle;
import schmitzm.jfree.feature.style.FeatureChartUtil;
import schmitzm.swing.ExceptionDialog;
import schmitzm.swing.JPanel;
import schmitzm.swing.SwingUtil;
import skrueger.atlas.AVDialogManager;
import skrueger.atlas.AtlasViewer;
import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.gui.internal.AtlasStatusDialog;
import skrueger.atlas.swing.AtlasSwingWorker;
import skrueger.creator.gui.DesignAtlasChartJDialog;
import skrueger.geotools.StyledFeaturesInterface;
import skrueger.geotools.selection.ChartSelectionSynchronizer;
import skrueger.geotools.selection.StyledFeatureLayerSelectionModel;
import skrueger.geotools.selection.StyledLayerSelectionModel;
import skrueger.geotools.selection.StyledLayerSelectionModelSynchronizer;
import skrueger.swing.AtlasDialog;
import skrueger.swing.OkButton;
import skrueger.swing.SmallButton;

public class AtlasChartJDialog extends AtlasDialog {
	final static Logger LOGGER = Logger.getLogger(AtlasChartJDialog.class);

	private final StyledFeaturesInterface<?> styledLayer;

	// /** A cache that manages maximum one instance of this class per layer **/
	// protected volatile static HashMap<String, AtlasChartJDialog> dialogCache
	// = new HashMap<String, AtlasChartJDialog>();

	protected final ChartStyle chartStyle;

	private final SelectableXMapPane mapPane;

	protected volatile AtlasChartJPanel chartPanel;

	private final MapLegend mapLegend;

	/**
	 * A private reference to the listeners. When nulled in dispose, the
	 * WeakHashMap forgets about them
	 */
	private Vector<ChartSelectionSynchronizer> insertedListeners = new Vector<ChartSelectionSynchronizer>();

	public AtlasChartJDialog(final Component owner,
			final ChartStyle chartStyle, final MapLegend mapLegend,
			final StyledFeaturesInterface<?> styledLayer_) {
		super(owner);

		this.chartStyle = chartStyle;
		this.mapLegend = mapLegend;

		styledLayer = styledLayer_;

		// SK: Sadly has no effect :-( .. Only for JFrames?
		// setIconImage(BasicTaskPaneUI.ICON_TABLE.getImage());

		this.mapPane = mapLegend != null ? (mapLegend.getGeoMapPane() != null ? mapLegend
				.getGeoMapPane().getMapPane()
				: null)
				: null;

		// Filter filter = (mapLayer != null && mapLayer.getQuery() != null) ?
		// mapLayer.getQuery()
		// .getFilter() : Filter.INCLUDE;

		initGUI();
		
		SwingUtil.setRelativeFramePosition(this, owner, SwingUtil.BOUNDS_OUTER, SwingUtil.NORTHWEST);
	}

	/**
	 * Lazily creates the {@link AtlasChartJPanel}
	 */
	protected AtlasChartJPanel getChartPanel(final MapLegend mapLegend,
			final StyledFeaturesInterface<?> styledLayer_) {

		if (chartPanel == null) {

			JFreeChart chart;

			if (getChartStyle() instanceof FeatureChartStyle) {
				final FeatureChartStyle fschart = (FeatureChartStyle) getChartStyle();

				AtlasStatusDialog statusDialog = new AtlasStatusDialog(
						AtlasChartJDialog.this, AtlasViewer.R("dialog.title.wait"), AtlasViewer.R("dialog.title.wait")); 
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
					.getRememberSelection(getStyledLayer().getId())
					: null;

			if ((anySelectionModel instanceof StyledFeatureLayerSelectionModel)) {
				final StyledFeatureLayerSelectionModel selectionModel = (StyledFeatureLayerSelectionModel) anySelectionModel;

				// get the selectionmodel(s) of the chart
				final List<FeatureDatasetSelectionModel<?, ?, ?>> datasetSelectionModelFor = FeatureChartUtil
						.getFeatureDatasetSelectionModelFor(chart);

				for (final FeatureDatasetSelectionModel<?, ?, ?> dsm : datasetSelectionModelFor) {

					// Create a ChartSelectionSynchronizer and connect
					// StyledFeatureLayerSelectionModel <->
					// FeatureDatasetSelectionModel

					final ChartSelectionSynchronizer synchronizer = new ChartSelectionSynchronizer(
							selectionModel, dsm);

					insertedListeners.add(synchronizer);

					selectionModel
							.addSelectionListener((StyledLayerSelectionModelSynchronizer) synchronizer);
					dsm.addSelectionListener(synchronizer);

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

		contentpane.add(getChartPanel(getMapLegend(), getStyledLayer()),"top, wrap");

		getChartPanel(mapLegend, styledLayer).addZoomToFeatureExtends(mapPane, mapLegend, getStyledLayer(),
				getChartPanel(mapLegend, getStyledLayer()));
		addZoomToFullChartExtends();
		
		
		contentpane.add( getButtonsPanel(),"growx");

		setContentPane(contentpane);
		pack();
	}

	private JPanel getButtonsPanel() {
		JPanel buttons = new JPanel(new MigLayout("fillx","[100%]"));

		{
			buttons.add(new OkButton(new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					close();
				}

			}), "right, tag ok");
		}

		return buttons;
	}

	/**
	 * Adds a Button to the ToolBar of the {@link AtlasChartJPanel}, that zoom
	 * the chart to full extends. has to be done in the
	 * {@link AtlasChartJDialog}, because {@link AtlasChartJPanel} doesn't know
	 * about the {@link ChartStyle}.
	 */
	private void addZoomToFullChartExtends() {
		final AtlasChartJPanel chartPanel = getChartPanel(mapLegend,
				getStyledLayer());

		/**
		 * Add an Action to SET the selection.
		 */
		JButton resetZoomTool = new SmallButton(new AbstractAction("",
				AtlasChartJPanel.ICON_FULL_EXTEND) {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (chartStyle.getPlotStyle() != null) {
					JFreeChartUtil.zoomPlotToFullExtend(chartPanel.getChart().getPlot(),
							null, chartStyle.getPlotStyle()
									.isCenterOriginSymetrically());
				} else {
					JFreeChartUtil.zoomPlotToFullExtend(chartPanel.getChart().getPlot(),
							null, false);
				}
			}

		}, AtlasViewer.R("AtlasChartJPanel.zoomFullExtent.tt"));
		chartPanel.getToolBar().add(resetZoomTool, 0);
	}


	public ChartStyle getChartStyle() {
		return chartStyle;
	}

	public MapLegend getMapLegend() {
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
		
		if (isDisposed) return;

		for (ChartSelectionSynchronizer d : insertedListeners) {
			d.setEnabled(false);
			d = null;
		}
		insertedListeners.clear();

		if (chartPanel != null)
			chartPanel.dispose();
		chartPanel = null;
		
		super.dispose();
	}
}
