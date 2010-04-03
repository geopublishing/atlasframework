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
package org.geopublishing.geopublisher.gui.map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.dp.DpRef;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.resource.icons.Icons;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geopublishing.atlasViewer.swing.AtlasMapLayer;
import org.geopublishing.atlasViewer.swing.ClickInfoDialog;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GeopublisherGUI;
import org.geotools.data.memory.MemoryFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.identity.FeatureIdImpl;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.identity.Identifier;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import schmitzm.geotools.FilterUtil;
import schmitzm.geotools.JTSUtil;
import schmitzm.geotools.gui.GridPanelFormatter;
import schmitzm.geotools.gui.SelectableXMapPane;
import schmitzm.geotools.gui.XMapPane;
import schmitzm.geotools.gui.XMapPaneAction_Zoom;
import schmitzm.geotools.gui.XMapPaneTool;
import schmitzm.geotools.styling.StylingUtil;
import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.swing.ExceptionDialog;
import schmitzm.swing.JPanel;
import schmitzm.swing.SwingUtil;
import schmitzm.swing.event.MouseInputType;
import skrueger.creator.GPDialogManager;
import skrueger.creator.GPProps;
import skrueger.swing.CancelButton;
import skrueger.swing.CancellableDialogAdapter;
import skrueger.swing.OkButton;
import skrueger.swing.SmallButton;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

/**
 * Puts a {@link DesignMapView} into a {@link JDialog}. The static {@link Map}
 * openInstances ensures, that only one Dialog per Map is open.
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
 * 
 */
public class DesignMapViewJDialog extends CancellableDialogAdapter {
	final static private Logger LOGGER = Logger
			.getLogger(DesignMapViewJDialog.class);

	public static final String MAXMAPENTEND_FEATURE_ID = "MAXMAPENTEND_FEATURE_ID";
	public static final String DEFAULTMAPAREA_FEATURE_ID = "DEFAULTMAPAREA_FEATURE_ID";

	/**
	 * An {@link XMapPaneTool} that allows to select the maximal map extend
	 */
	class XMapPaneTool_SetMaxMapExtend extends XMapPaneTool {

		public XMapPaneTool_SetMaxMapExtend() {
			XMapPaneTool.ZOOM_IN.copyTo(this);

			setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)); // TODO
			// nicer
			// cursor

			// Clicking the right mouse button cancels the action.
			setMouseAction(MouseInputType.RClick, new XMapPaneAction_Zoom.In() {

				@Override
				public void performClick(XMapPane mapPane, MouseEvent ev,
						DirectPosition coord) {
					finish();
				}

			});

			// Clicking the right mouse button cancels the action.
			setMouseAction(MouseInputType.LClick, new XMapPaneAction_Zoom.In() {

				@Override
				public void performClick(XMapPane mapPane, MouseEvent ev,
						DirectPosition coord) {
					// Do nothing
				}

			});

			setMouseAction(MouseInputType.LDrag, new XMapPaneAction_Zoom.In() {
				@Override
				public void performClick(XMapPane mapPane, MouseEvent ev,
						DirectPosition coord) {
					finish();
				}

				@Override
				public void performDragged(XMapPane mapPane, MouseEvent ev,
						Point dragStartPos, Point dragLastPos,
						DirectPosition startCoord, DirectPosition endCoord) {

					final ReferencedEnvelope maxExtend = JTSUtil
							.createReferencedEnvelope(startCoord, endCoord);

					// Apply to the Map
					map.setMaxExtend(maxExtend);

					// intersect with any maxMapExtend
					if (map.getDefaultMapArea() != null) {
						ReferencedEnvelope newDefArea = new ReferencedEnvelope(
								map.getMaxExtend().intersection(
										map.getDefaultMapArea()), startCoord
										.getCoordinateReferenceSystem());
						if (newDefArea.isEmpty())
							newDefArea = null;
						map.setDefaultMapArea(newDefArea);
					}

					// Apply to the XMapPane
					if (maxExtendPreviewCheckBox.isSelected()) {
						getDesignMapView().getGeoMapPane().getMapPane()
								.setMaxExtend(map.getMaxExtend());
					}

					finish();
				}

			});

		}

		private void finish() {
			// Re-enable the listeners state
			getDesignMapView().getMapPane().setTool(backupTool);

			// getDesignMapView().getToolBar().setEnabled(true);
			getDesignMapView().getToolBar().setAllToolsEnabled(true, false);

			if (maxExtendButton != null)
				maxExtendButton.setEnabled(map.getMaxExtend() == null);
			if (maxExtendResetButton != null)
				maxExtendResetButton.setEnabled(map.getMaxExtend() != null);

			updateMapMaxExtendInMapContext();
		}
	}

	/**
	 * An {@link XMapPaneTool} that allows to select the maximal map extend
	 */
	class XMapPaneTool_SetDefaultMapExtend extends XMapPaneTool {

		public XMapPaneTool_SetDefaultMapExtend() {
			XMapPaneTool.ZOOM_IN.copyTo(this);

			setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)); // TODO
			// nicer
			// cursor

			// Clicking the right mouse button cancels the action.
			setMouseAction(MouseInputType.RClick, new XMapPaneAction_Zoom.In() {

				@Override
				public void performClick(XMapPane mapPane, MouseEvent ev,
						DirectPosition coord) {
					finish();
				}

			});

			// Clicking the right mouse button cancels the action.
			setMouseAction(MouseInputType.LClick, new XMapPaneAction_Zoom.In() {

				@Override
				public void performClick(XMapPane mapPane, MouseEvent ev,
						DirectPosition coord) {
					// Do nothing
				}

			});

			setMouseAction(MouseInputType.LDrag, new XMapPaneAction_Zoom.In() {
				@Override
				public void performClick(XMapPane mapPane, MouseEvent ev,
						DirectPosition coord) {
					finish();
				}

				@Override
				public void performDragged(XMapPane mapPane, MouseEvent ev,
						Point dragStartPos, Point dragLastPos,
						DirectPosition startCoord, DirectPosition endCoord) {

					ReferencedEnvelope defaultArea = JTSUtil
							.createReferencedEnvelope(startCoord, endCoord);

					// intersect with any maxMapExtend
					if (map.getMaxExtend() != null)
						defaultArea = new ReferencedEnvelope(map.getMaxExtend()
								.intersection(defaultArea), startCoord
								.getCoordinateReferenceSystem());
					if (defaultArea.isEmpty())
						defaultArea = null;

					// Apply to the Map
					map.setDefaultMapArea(defaultArea);

					// Disabled
					// /**
					// * Ask the user to store the value for all maps
					// */
					// if (AVUtil
					// .askYesNo(
					// DesignMapViewJDialog.this,
					// AtlasCreator
					// .R("DesignMapViewJDialog.SaveBBOX.Question.forAllMaps")))
					// {
					// for (final Map map : ace.getMapPool().values()) {
					// map.setDefaultMapArea(defaultArea);
					// }
					// }

					finish();
				}

			});

		}

		private void finish() {
			// Re-enable the listeners state
			getDesignMapView().getMapPane().setTool(backupTool);

			// getDesignMapView().getToolBar().setEnabled(true);
			getDesignMapView().getToolBar().setAllToolsEnabled(true, false);

			if (setDefaultMapAreaButton != null)
				setDefaultMapAreaButton
						.setEnabled(map.getDefaultMapArea() == null);
			if (resetDefaultMapAreaButton != null)
				resetDefaultMapAreaButton
						.setEnabled(map.getDefaultMapArea() != null);

			updateMapMaxExtendInMapContext();
		}
	}

	protected final XMapPaneTool defineDefaultMapAreaTool = new XMapPaneTool_SetDefaultMapExtend();

	/**
	 * Updates and applies directly the use of anti-aliasing for all
	 * {@link SelectableXMapPane}s used by all open {@link DesignMapViewJDialog}
	 * s.
	 */
	public static void setAntiAliasing(final boolean useAntiAliase) {
		for (final DesignMapViewJDialog d : GPDialogManager.dm_MapComposer
				.getAllInstances()) {
			d.getDesignMapView().getMapPane().setAntiAliasing(useAntiAliase);
			d.getDesignMapView().getMapPane().repaint();
		}
	}

	/**
	 * It's always handy to have an instance of {@link AtlasConfigEditable}
	 * around
	 **/
	private final AtlasConfigEditable ace;

	private final HashMap<String, DpLayer<?, ? extends ChartStyle>> backupedLayers = new HashMap<String, DpLayer<?, ? extends ChartStyle>>();

	/** This is a backup of the original map object. **/
	private Map backupMap;

	protected XMapPaneTool backupTool;

	final private DesignMapView designMapView;

	/**
	 * This is the original map object. This is the version we are playing with.
	 **/
	private final Map map;

	// A private MapLayer to show special features like default map area
	private AtlasMapLayer specialMapLayer = null;

	private JButton maxExtendButton;
	private JCheckBox maxExtendPreviewCheckBox;

	private JButton maxExtendResetButton;

	private JButton setDefaultMapAreaButton;
	private JButton resetDefaultMapAreaButton;

	/**
	 * Creates a {@link DesignMapViewJDialog} and remembers its existence.
	 * getExisting() can return the existing instance for a {@link Map}
	 * 
	 * @param ace
	 * @param map_
	 * @throws HeadlessException
	 */
	public DesignMapViewJDialog(final Component owner, final Map map_)
			throws HeadlessException {
		super(owner, GeopublisherGUI.R("DesignMapViewJDialog.title", map_
				.getTitle()));

		this.ace = (AtlasConfigEditable) map_.getAc();

		this.map = map_;

		backup();

		designMapView = new DesignMapView(owner, ace);
		designMapView.setMap(map);
		designMapView.initialize();

		final JPanel cp = new JPanel(new BorderLayout());
		cp.add(designMapView, BorderLayout.CENTER);

		final JPanel controlPanel = new JPanel(new MigLayout());

		controlPanel.add(createTabs(), "top");
		controlPanel.add(getButtons(), "bottom, right");

		cp.add(controlPanel, BorderLayout.SOUTH);

		setContentPane(cp);

		final Dimension DIALOG_SIZE = new Dimension(GPProps.getInt(
				GPProps.Keys.mapComposerWidth, 800), GPProps.getInt(
				GPProps.Keys.mapComposerHeight, 600));
		setMinimumSize(new Dimension(780, 580));
		setPreferredSize(DIALOG_SIZE);
		pack();

		SwingUtil.centerFrameOnScreenRandom(this);
	}

	/**
	 * If a maximum map extend or a default maparea is defined for the
	 * {@link Map}, it will be added to the {@link MapContext} as a rectangle in
	 * a special layer. If no map extend is the special layer will be removed
	 * from the {@link MapContext}.
	 */
	private void updateMapMaxExtendInMapContext() {

		CoordinateReferenceSystem mapCrs = getDesignMapView().getMapPane()
				.getMapContext().getCoordinateReferenceSystem();

		final SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName("internal");
		builder.setNamespaceURI("http://localhost/");
		builder.setCRS(mapCrs);
		builder.add("Location", LineString.class);
		builder.add("Name", String.class);

		final SimpleFeatureType specialLinesFeatureType = builder
				.buildFeatureType();

		final GeometryFactory factory = JTSFactoryFinder
				.getGeometryFactory(null);

		final MemoryFeatureCollection fc = new MemoryFeatureCollection(
				specialLinesFeatureType);

		// Create a feature for the max map extend
		if (map.getMaxExtend() != null) {

			final ReferencedEnvelope mapMaxExtend = new ReferencedEnvelope(map
					.getMaxExtend(), mapCrs);

			final Coordinate c1 = new Coordinate(mapMaxExtend.getLowerCorner()
					.getCoordinate()[0], mapMaxExtend.getLowerCorner()
					.getCoordinate()[1]);
			final Coordinate c2 = new Coordinate(mapMaxExtend.getUpperCorner()
					.getCoordinate()[0], mapMaxExtend.getUpperCorner()
					.getCoordinate()[1]);

			final Coordinate c3 = new Coordinate(mapMaxExtend.getLowerCorner()
					.getCoordinate()[0], mapMaxExtend.getUpperCorner()
					.getCoordinate()[1]);
			final Coordinate c4 = new Coordinate(mapMaxExtend.getUpperCorner()
					.getCoordinate()[0], mapMaxExtend.getLowerCorner()
					.getCoordinate()[1]);
			final SimpleFeature maxExtendFeature = SimpleFeatureBuilder.build(
					specialLinesFeatureType, new Object[] {
							factory.createLineString(new Coordinate[] { c1, c3,
									c2, c4, c1 }), MAXMAPENTEND_FEATURE_ID },
					MAXMAPENTEND_FEATURE_ID);

			fc.add(maxExtendFeature);
		}

		// Create a feature for the default map area
		if (map.getDefaultMapArea() != null) {

			final ReferencedEnvelope defaultMapArea = new ReferencedEnvelope(
					map.getDefaultMapArea(), mapCrs);

			final Coordinate c1 = new Coordinate(defaultMapArea
					.getLowerCorner().getCoordinate()[0], defaultMapArea
					.getLowerCorner().getCoordinate()[1]);
			final Coordinate c2 = new Coordinate(defaultMapArea
					.getUpperCorner().getCoordinate()[0], defaultMapArea
					.getUpperCorner().getCoordinate()[1]);

			final Coordinate c3 = new Coordinate(defaultMapArea
					.getLowerCorner().getCoordinate()[0], defaultMapArea
					.getUpperCorner().getCoordinate()[1]);
			final Coordinate c4 = new Coordinate(defaultMapArea
					.getUpperCorner().getCoordinate()[0], defaultMapArea
					.getLowerCorner().getCoordinate()[1]);

			final SimpleFeature defaultMapAraFeature = SimpleFeatureBuilder
					.build(specialLinesFeatureType, new Object[] {
							factory.createLineString(new Coordinate[] { c1, c3,
									c2, c4, c1 }), DEFAULTMAPAREA_FEATURE_ID },
							DEFAULTMAPAREA_FEATURE_ID);

			fc.add(defaultMapAraFeature);
		}

		// A style
		final StyleBuilder sb = StylingUtil.STYLE_BUILDER;
		final Style style = sb.createStyle(null);
		style.featureTypeStyles().get(0).rules().clear();

		// final Style style = sb.createStyle();

		{
			// A FTStyle for maxMapExntend
			final LineSymbolizer dash2 = sb.createLineSymbolizer(sb
					.createStroke(Color.red, 4, new float[] { 1, 2 }));
			final LineSymbolizer dash1 = sb.createLineSymbolizer(sb
					.createStroke(Color.white, 4, new float[] { 2, 1 }));

			Rule rule = sb.createRule(dash1);
			rule.symbolizers().add(dash2);

			// A filter for the max map exented
			HashSet<Identifier> ids = new HashSet<Identifier>();
			ids.add(new FeatureIdImpl(MAXMAPENTEND_FEATURE_ID));
			rule.setFilter(FilterUtil.FILTER_FAC2.id(ids));

			style.featureTypeStyles().get(0).rules().add(rule);
		}

		{
			// A FTStyle for default mapArea
			final LineSymbolizer dash2 = sb.createLineSymbolizer(sb
					.createStroke(Color.green, 4, new float[] { 2, 3 }));
			final LineSymbolizer dash1 = sb.createLineSymbolizer(sb
					.createStroke(Color.white, 4, new float[] { 3, 2 }));

			Rule rule = sb.createRule(dash1);
			rule.symbolizers().add(dash2);

			// A filter for the max map exented
			HashSet<Identifier> ids = new HashSet<Identifier>();
			ids.add(new FeatureIdImpl(DEFAULTMAPAREA_FEATURE_ID));
			rule.setFilter(FilterUtil.FILTER_FAC2.id(ids));

			style.featureTypeStyles().get(0).rules().add(rule);
		}

		// Wenn das layer schon existierte, dann entfernen wir es.
		if (specialMapLayer != null) {
			getDesignMapView().getMapPane().getMapContext().removeLayer(
					specialMapLayer);
		}

		if (!fc.isEmpty()) {
			// Layer neu erzeugen
			specialMapLayer = new AtlasMapLayer(fc, style);
			System.err.println(style);
			specialMapLayer.setTitle(XMapPane.SPECIAL_LINES_LAYER_ID);

			getDesignMapView().getMapPane().getMapContext().addLayer(
					specialMapLayer);
			getDesignMapView().getMapPane().setMapLayerSelectable(
					specialMapLayer, false);
		}

	}

	private void backup() {
		backupMap = map.copy();

		// Make backups of all involved DpLayers
		for (final DpRef<DpLayer<?, ? extends ChartStyle>> dpr : map
				.getLayers()) {
			final DpLayer<?, ? extends ChartStyle> target = dpr.getTarget();
			backupedLayers.put(dpr.getTargetId(), target.copy());
		}
	}

	@Override
	public void cancel() {
		backupMap.copyTo(map);

		// Restore all involved layers..
		for (final DpRef<DpLayer<?, ? extends ChartStyle>> dpr : map
				.getLayers()) {
			final DpLayer target = dpr.getTarget();
			backupedLayers.get(dpr.getTargetId()).copyTo(target);
		}

	}

	/**
	 * This {@link JPanel} is used as a tab and allows to define the width of
	 * the legend
	 */
	private JPanel createLegendePanel() {
		final JPanel panel = new JPanel(new MigLayout());

		final JButton saveDividerRatioButton = new SmallButton(
				new AbstractAction(GeopublisherGUI
						.R("DesignMapViewJDialog.Button.SaveDivider.Label")) {

					@Override
					public void actionPerformed(final ActionEvent e) {

						/**
						 * Calc the ratio
						 */
						final int dividerLocation = designMapView
								.getSplitPane().getDividerLocation();
						Double currentRatio;
						if (dividerLocation != 0)
							currentRatio = dividerLocation
									/ (double) (designMapView.getSplitPane()
											.getWidth() - designMapView
											.getSplitPane().getDividerSize());
						else
							currentRatio = 0.;

						/**
						 * Ask the user to store the value for this map
						 */
						if (!AVSwingUtil
								.askYesNo(
										DesignMapViewJDialog.this,
										GeopublisherGUI
												.R(
														"DesignMapViewJDialog.SaveDivider.Question.forMap",
														NumberFormat
																.getPercentInstance()
																.format(
																		currentRatio))))
							return;
						map.setLeftRightRatio(currentRatio);

						/**
						 * Ask the user to store the value for all maps
						 */
						if (!AVSwingUtil
								.askYesNo(
										DesignMapViewJDialog.this,
										GeopublisherGUI
												.R(
														"DesignMapViewJDialog.SaveDivider.Question.forAllMaps",
														NumberFormat
																.getPercentInstance()
																.format(
																		currentRatio))))
							return;
						for (final Map map : ace.getMapPool().values()) {
							map.setLeftRightRatio(currentRatio);
						}
					}

				}, GeopublisherGUI.R("DesignMapViewJDialog.Button.SaveDivider.TT"));

		final JButton saveAutoDividerRatioButton = new SmallButton(
				new AbstractAction(GeopublisherGUI
						.R("DesignMapViewJDialog.Button.AutoDivider.Label")) {

					@Override
					public void actionPerformed(final ActionEvent e) {

						final Double currentRatio = 0.;

						// TODO has a strange random but.. ever second time
						// issued, it makes the left component much too small
						designMapView.getSplitPane().setDividerLocation(-1);
						/**
						 * Ask the user to store the value for this map
						 */
						if (!AVSwingUtil
								.askYesNo(
										DesignMapViewJDialog.this,
										GeopublisherGUI
												.R(
														"DesignMapViewJDialog.SaveDivider.Question.forMap",
														NumberFormat
																.getPercentInstance()
																.format(
																		currentRatio))))
							return;
						map.setLeftRightRatio(currentRatio);

						/**
						 * Ask the user to store the value for all maps
						 */
						if (!AVSwingUtil
								.askYesNo(
										DesignMapViewJDialog.this,
										GeopublisherGUI
												.R(
														"DesignMapViewJDialog.SaveDivider.Question.forAllMaps",
														NumberFormat
																.getPercentInstance()
																.format(
																		currentRatio))))
							return;
						for (final Map map : ace.getMapPool().values()) {
							map.setLeftRightRatio(currentRatio);
						}

						map.setLeftRightRatio(0.);
					}

				}, GeopublisherGUI.R("DesignMapViewJDialog.Button.AutoDivider.TT"));

		panel.add(saveAutoDividerRatioButton);
		panel.add(saveDividerRatioButton);

		return panel;
	}

	/**
	 * Creates a {@link JPanel} with map area settings
	 */
	private JPanel createMapAreaPanel() {

		final JPanel mapAreaPanel = new JPanel(new MigLayout("nogrid"));

		{

			// final JPanel maxExtendPanel = new JPanel(new MigLayout(),
			// AtlasCreator
			// .R("DesignMapViewJDialog.mapArea.maxExtend.border"));
			// final JPanel maxExtendPanel = new JPanel(new MigLayout());

			mapAreaPanel.add(new JLabel(GeopublisherGUI
					.R("DesignMapViewJDialog.mapArea.maxExtend.explanation")),
					"span 3, wrap");

			/*
			 * Adding the Button to select a max. zoom extend
			 */
			maxExtendButton = new SmallButton(new AbstractAction(GeopublisherGUI
					.R("DesignMapView.SetMaxBBoxTool"),
					Icons.ICON_MAPEXTEND_BBOX) {

				@Override
				public void actionPerformed(final ActionEvent e) {

					backupTool = getDesignMapView().getMapPane().getTool();

					getDesignMapView().getMapPane().setTool(
							new XMapPaneTool_SetMaxMapExtend());

					getDesignMapView().getToolBar().setAllToolsEnabled(false,
							false);

					JOptionPane
							.showMessageDialog(
									DesignMapViewJDialog.this,
									GeopublisherGUI
											.R("DesignMapView.SetMaxBBoxTool.pleaseSelectBBOX.msg"),
									GeopublisherGUI
											.R("DesignMapView.SetMaxBBoxTool"),
									JOptionPane.INFORMATION_MESSAGE);

					// Disable itself until the listener got an event
					maxExtendButton.setEnabled(false);
				}
			});
			maxExtendButton.setToolTipText(GeopublisherGUI
					.R("DesignMapView.SetMaxBBoxTool.TT"));
			mapAreaPanel.add(maxExtendButton);
			maxExtendButton.setEnabled(map.getMaxExtend() == null);

			// Add the box to the map
			updateMapMaxExtendInMapContext();

			/*
			 * Adding the Button to reset the min. zoom extend
			 */
			maxExtendResetButton = new SmallButton(
					new AbstractAction(GeopublisherGUI
							.R("DesignMapView.SetMaxBBoxTool.RemoveButton"),
							Icons.ICON_MAPEXTEND_BBOX_RESET) {

						@Override
						public void actionPerformed(final ActionEvent e) {

							// Reset the extend
							map.setMaxExtend(null);
							getDesignMapView().getMapPane().setMaxExtend(null);

							updateMapMaxExtendInMapContext();

							maxExtendResetButton.setEnabled(false);
							maxExtendButton.setEnabled(true);

						}

					});
			maxExtendResetButton.setToolTipText(GeopublisherGUI
					.R("DesignMapView.SetMaxBBoxTool.RemoveButton.TT"));
			mapAreaPanel.add(maxExtendResetButton);
			maxExtendResetButton.setEnabled(map.getMaxExtend() != null);

			maxExtendPreviewCheckBox = new JCheckBox(
					new AbstractAction(
							GeopublisherGUI
									.R("DesignMapViewJDialog.mapArea.maxExtend.hardPreviewoption")) {

						@Override
						public void actionPerformed(final ActionEvent e) {
							if (maxExtendPreviewCheckBox.isSelected()) {

								if (map.getMaxExtend() != null) {
									getDesignMapView().getGeoMapPane()
											.getMapPane().setMaxExtend(
													map.getMaxExtend());

									getDesignMapView().getGeoMapPane()
											.getMapPane().setMapArea(
													map.getMaxExtend());
									getDesignMapView().getGeoMapPane()
											.getMapPane().repaint();
								}

							} else {
								getDesignMapView().getGeoMapPane().getMapPane()
										.setMaxExtend(null);
							}
						}
					});

			// Unset the MaxMapExtend from the XMapPane, as this is the default
			// state of the checkbox
			maxExtendPreviewCheckBox.setSelected(false);
			getDesignMapView().getMapPane().setMaxExtend(null);

			mapAreaPanel.add(maxExtendPreviewCheckBox, "wrap");

		} // maxExtend stuff

		mapAreaPanel.add(new JLabel(GeopublisherGUI
				.R("DesignMapViewJDialog.mapArea.defaultArea.explanation")),
				"span 3, wrap");

		/**
		 * A button to store the start area
		 */
		setDefaultMapAreaButton = new SmallButton(new AbstractAction(
				GeopublisherGUI.R("DesignMapViewJDialog.Button.SaveBBOX.Label")) {

			@Override
			public void actionPerformed(final ActionEvent e) {

				backupTool = getDesignMapView().getMapPane().getTool();

				getDesignMapView().getMapPane().setTool(
						new XMapPaneTool_SetDefaultMapExtend());

				getDesignMapView().getToolBar()
						.setAllToolsEnabled(false, false);

				JOptionPane
						.showMessageDialog(
								DesignMapViewJDialog.this,
								GeopublisherGUI
										.R("DesignMapView.SetDefaultBBoxTool.pleaseSelectBBOX.msg"),
								GeopublisherGUI
										.R("DesignMapViewJDialog.Button.SaveBBOX.Label"),
								JOptionPane.INFORMATION_MESSAGE);

				// Disable itself until the listener got an event
				setDefaultMapAreaButton.setEnabled(false);

			}

		});
		setDefaultMapAreaButton.setEnabled(map.getDefaultMapArea() == null);
		mapAreaPanel.add(setDefaultMapAreaButton);

		/*
		 * Adding the Button to reset the min. zoom extend
		 */
		resetDefaultMapAreaButton = new SmallButton(new AbstractAction(
				GeopublisherGUI.R("DesignMapView.defaultMapArea.RemoveButton"),
				Icons.ICON_DEFAULTMAPAREA_BBOX_RESET) {

			@Override
			public void actionPerformed(final ActionEvent e) {

				// Reset the extend
				map.setDefaultMapArea(null);

				updateMapMaxExtendInMapContext();

				resetDefaultMapAreaButton.setEnabled(false);
				setDefaultMapAreaButton.setEnabled(true);

			}

		});
		resetDefaultMapAreaButton.setToolTipText(GeopublisherGUI
				.R("DesignMapView.SetMaxBBoxTool.RemoveButton.TT"));
		mapAreaPanel.add(resetDefaultMapAreaButton);

		resetDefaultMapAreaButton.setEnabled(map.getDefaultMapArea() != null);

		// A button to try out the default map area function
		SmallButton tryStartAreaButton = new SmallButton(new AbstractAction(
				GeopublisherGUI.R("DesignMapView.defaultMapArea.try.button")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				// if (!maxExtendPreviewCheckBox.isSelected()) {
				getDesignMapView().getMapPane()
						.setMaxExtend(map.getMaxExtend());
				// }
				getDesignMapView().getMapPane().setMapArea(
						map.getDefaultMapArea());
				if (!maxExtendPreviewCheckBox.isSelected()) {
					getDesignMapView().getMapPane().setMaxExtend(null);
				}
			}
		});
		mapAreaPanel.add(tryStartAreaButton);

		return mapAreaPanel;
	}

	private JPanel createMapMarginPanel() {
		final JPanel panel = new JPanel(new MigLayout("nogrid"));

		/**
		 * A JComboBox allowing to select a formatter of the GridCRS
		 */

		final JComboBox gridFormatterSelector = new JComboBox(
				GridPanelFormatter.FORMATTERS);

		gridFormatterSelector.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(final JList list,
					final Object value, final int index,
					final boolean isSelected, final boolean cellHasFocus) {

				final JLabel proto = (JLabel) super
						.getListCellRendererComponent(list, value, index,
								isSelected, cellHasFocus);

				try {
					proto.setText(((Class<GridPanelFormatter>) value)
							.newInstance().getTitle());
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}

				return proto;
			}
		});

		/*
		 * A JComboBox allowing to choose the CRS to use for the GRID, or
		 * disable the GRID
		 */

		final Vector<CoordinateReferenceSystem> crss = ace.getDataPool()
				.getCRSList();

		final JComboBox gridCrsSelector = new JComboBox(crss);
		gridCrsSelector.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(final JList list,
					final Object value, final int index,
					final boolean isSelected, final boolean cellHasFocus) {

				final JLabel proto = (JLabel) super
						.getListCellRendererComponent(list, value, index,
								isSelected, cellHasFocus);

				if (value instanceof CoordinateReferenceSystem) {

					final CoordinateReferenceSystem crs = (CoordinateReferenceSystem) value;

					// Name/Descriptor of the Map's CRS
					String mapCrsName;
					try {
						final ReferenceIdentifier name = crs.getName();
						if (name != null)
							mapCrsName = name.getCode();
						else
							mapCrsName = crs.toString();
					} catch (final Exception e) {
						mapCrsName = crs.toString();
					}

					proto.setText(mapCrsName);
				}

				return proto;
			}
		});

		gridCrsSelector.setToolTipText(GeopublisherGUI
				.R("DesignMapViewJDialog.MapFrameCRS.TT"));
		gridCrsSelector.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				final CoordinateReferenceSystem crs = (CoordinateReferenceSystem) gridCrsSelector
						.getSelectedItem();

				map.getGridPanelFormatter().setCRS(crs);

				map.setGridPanelCRS(crs);

				designMapView.getGeoMapPane().getHorGrid().setGridFormatter(
						map.getGridPanelFormatter());
				designMapView.getGeoMapPane().getVertGrid().setGridFormatter(
						map.getGridPanelFormatter());

				designMapView.getGeoMapPane().repaint();
			}

		});

		gridFormatterSelector.setToolTipText(GeopublisherGUI
				.R("DesignMapViewJDialog.MapFrameFormat.TT"));
		gridFormatterSelector.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {

				final CoordinateReferenceSystem crs = (CoordinateReferenceSystem) gridCrsSelector
						.getSelectedItem();
				final Class<? extends GridPanelFormatter> formatterClass = (Class<? extends GridPanelFormatter>) gridFormatterSelector
						.getSelectedItem();

				GridPanelFormatter formatter;
				try {
					formatter = formatterClass.newInstance();
				} catch (final Exception e1) {
					throw new RuntimeException(e1);
				}

				formatter.setCRS(crs);

				map.setGridPanelFormatter(formatter);

				designMapView.getGeoMapPane().getHorGrid().setGridFormatter(
						formatter);
				designMapView.getGeoMapPane().getVertGrid().setGridFormatter(
						formatter);

				designMapView.getGeoMapPane().repaint();
			}

		});

		final JCheckBox gridVisibibleJCheckBox = new JCheckBox(
				new AbstractAction(GeopublisherGUI
						.R("DesignMapViewJDialog.MapFrameVisible")) {

					@Override
					public void actionPerformed(final ActionEvent e) {
						map.setGridPanelVisible(((JCheckBox) e.getSource())
								.isSelected());
						designMapView.getGeoMapPane().getVertGrid().setVisible(
								map.isGridPanelVisible());
						designMapView.getGeoMapPane().getHorGrid().setVisible(
								map.isGridPanelVisible());
					}

				});
		gridVisibibleJCheckBox.setToolTipText(GeopublisherGUI
				.R("DesignMapViewJDialog.MapFrameVisible.TT"));

		final JCheckBox scaleVisibibleJCheckBox = new JCheckBox(
				new AbstractAction(GeopublisherGUI
						.R("DesignMapViewJDialog.MapScaleVisible")) {

					@Override
					public void actionPerformed(final ActionEvent e) {
						map.setScaleVisible(((JCheckBox) e.getSource())
								.isSelected());
						designMapView.getGeoMapPane().getScalePane()
								.setVisible(map.isScaleVisible());
					}

				});
		scaleVisibibleJCheckBox.setToolTipText(GeopublisherGUI
				.R("DesignMapViewJDialog.MapScaleVisible.TT"));

		// Setting the initial values
		gridCrsSelector.setSelectedItem(map.getGridPanelCRS());
		gridFormatterSelector.setSelectedItem(map.getGridPanelFormatter()
				.getClass());
		gridVisibibleJCheckBox.setSelected(map.isGridPanelVisible());
		scaleVisibibleJCheckBox.setSelected(map.isScaleVisible());

		// box.add(gridCrsSelector);
		// box.add(gridFormatterSelector);

		// final JPanel gridPanel = new JPanel(new MigLayout("wrap 1"));
		// gridPanel.setBorder(BorderFactory.createTitledBorder(AtlasCreator
		// .R("DesignMapViewJDialog.MapMargin.Border")));
		// DesignMapViewJDialog.MapFrameCRS= Map grid CRS:
		// DesignMapViewJDialog.MapFrameFormat= Map grid format:
		panel.add(
				new JLabel(GeopublisherGUI.R("DesignMapViewJDialog.MapFrameCRS")),
				"split 2, right");
		panel.add(gridCrsSelector);

		panel.add(new JLabel(GeopublisherGUI
				.R("DesignMapViewJDialog.MapFrameFormat")), "split 2, right");
		panel.add(gridFormatterSelector, "wrap");
		panel.add(gridVisibibleJCheckBox, "split 2");
		panel.add(scaleVisibibleJCheckBox);
		// panel.add(gridPanel, "growx");

		return panel;
	}

	private JTabbedPane createTabs() {
		final JTabbedPane tabbedPane = new JTabbedPane();

		tabbedPane.insertTab(GeopublisherGUI.R("DesignMapViewJDialog.legend.tab"),
				null, createLegendePanel(), GeopublisherGUI
						.R("DesignMapViewJDialog.legend.tab.tt"), tabbedPane
						.getTabCount());
		tabbedPane.insertTab(GeopublisherGUI.R("DesignMapViewJDialog.margin.tab"),
				null, createMapMarginPanel(), GeopublisherGUI
						.R("DesignMapViewJDialog.margin.tab.tt"), tabbedPane
						.getTabCount());
		tabbedPane.insertTab(
				GeopublisherGUI.R("DesignMapViewJDialog.mapArea.tab"), null,
				createMapAreaPanel(), GeopublisherGUI
						.R("DesignMapViewJDialog.mapArea.tab.tt"), tabbedPane
						.getTabCount());

		return tabbedPane;
	}

	/**
	 * Disposes this {@link DesignMapViewJDialog} and removes it from the list
	 * of open Instances. This is called when the {@link Window} closes. Any
	 * open {@link ClickInfoDialog} is also closed.
	 */
	@Override
	public void dispose() {

		// This disposing is actually quite expensive, so we do not do it more
		// than once.
		// the falg is set in #dispose of AtlasDialog
		if (isDisposed)
			return;

		setVisible(false); // Hide while doing the expensive disposing of the

		if (designMapView != null)
			designMapView.dispose();

		GPDialogManager.dm_DesignCharts.disposeInstanceForParent(this);
		GPDialogManager.dm_ManageCharts.disposeInstanceForParent(this);
		/*
		 * Stores the dimensions of the JDialog to the GP properties files
		 */
		GPProps.set(GPProps.Keys.mapComposerWidth, getSize().width);
		GPProps.set(GPProps.Keys.mapComposerHeight, getSize().height);

		super.dispose();
	}

	private JPanel getButtons() {
		final JPanel panel = new JPanel(new MigLayout());

		final JButton okButton = new OkButton(new AbstractAction() {

			public void actionPerformed(final ActionEvent e) {
				okClose();
			}

		});

		final JButton cancelButton = new CancelButton(new AbstractAction() {

			public void actionPerformed(final ActionEvent e) {
				cancelClose();
			}

		});

		/**
		 * Adding all the buttons
		 */
		panel.add(cancelButton, "gapx, right, tag cancel");
		panel.add(okButton, "right, tag ok");
		return panel;
	}

	public DesignMapView getDesignMapView() {
		return designMapView;
	}

	@Override
	public boolean okClose() {

		designMapView.updateMapLayersFromMapContext(map); // Sure we need this?
		// When changing the
		// order of layers
		// or adding layers,
		// this is not yet
		// directly
		// reflected in the
		// map object, but
		// in the mapContext
		// only. It would be
		// cooler, if this
		// would change the
		// map directly..
		// but thats a TODO

		// Saving SLDs is not needed here anymore.
		//
		final MapLayer[] layers = designMapView.getMapPane().getMapContext()
				.getLayers();
		/***********************************************************************
		 * Saving the Styles ....
		 */
		for (int i = 0; i < layers.length; i++) {
			try {

				final MapLayer ml = layers[i];

				// Remember: The dpe ID is stored in the MapLayers Title
				final String id = ml.getTitle();

				final DpLayer dpLayer = (DpLayer) ace.getDataPool().get(id);

				if (dpLayer == null) {
					// no problem.. Probably a special layer, like the one
					// showing the maxExtend..
					continue;
				}

				final Style newStyle = StylingUtil
						.removeSelectionFeatureTypeStyle(ml.getStyle());

				// if (StylingUtil.isStyleDifferent(newStyle,
				// dpLayer.getStyle())) {
				dpLayer.setStyle(newStyle);
				// }

				// not saving here any more.. It's done when the atlas is
				// saved...
				// File sldFile = DataUtilities.urlToFile(DataUtilities
				// .changeUrlExt(dpEntry.getUrl(), "sld"));
				//				
				// StylingUtil.saveStyleToSLD(mlStyle, sldFile);

			} catch (final Exception e) {
				LOGGER.error(e);
				ExceptionDialog.show(this, e);
			}

		}

		dispose();
		return true;
	}

	/**
	 * Add F5 for Preview to the MapComposer also. Since we are working on the
	 * real map object, this works now.
	 */
	@Override
	protected JRootPane createRootPane() {
		final KeyStroke stroke = KeyStroke
				.getKeyStroke(KeyEvent.VK_F5, 0, true);
		final JRootPane rootPane = super.createRootPane();
		rootPane.registerKeyboardAction(new ActionListener() {

			public void actionPerformed(final ActionEvent e) {
				GeopublisherGUI.getInstance().actionPerformed(
						new ActionEvent(DesignMapViewJDialog.this, (int) System
								.currentTimeMillis(),
								GeopublisherGUI.ActionCmds.previewAtlasLive
										.toString()));
			}

		}, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

		return rootPane;
	}

}
