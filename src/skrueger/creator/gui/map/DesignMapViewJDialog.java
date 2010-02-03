/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the Geopublisher application - An authoring tool to facilitate the publication and distribution of geoproducts in form of online and/or offline end-user GIS.
 * http://www.geopublishing.org
 * 
 * Geopublisher is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.creator.gui.map;

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
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
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
import org.geotools.data.memory.MemoryFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.BoundingBox;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import schmitzm.geotools.JTSUtil;
import schmitzm.geotools.gui.GridPanelFormatter;
import schmitzm.geotools.gui.SelectableXMapPane;
import schmitzm.geotools.styling.StylingUtil;
import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.swing.ExceptionDialog;
import schmitzm.swing.JPanel;
import schmitzm.swing.SwingUtil;
import schmitzm.swing.event.MouseInputType;
import skrueger.atlas.AVUtil;
import skrueger.atlas.dp.DpRef;
import skrueger.atlas.dp.layer.DpLayer;
import skrueger.atlas.gui.AtlasMapLayer;
import skrueger.atlas.gui.ClickInfoDialog;
import skrueger.atlas.map.Map;
import skrueger.atlas.resource.icons.Icons;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.AtlasCreator;
import skrueger.creator.GPDialogManager;
import skrueger.creator.GPProps;
import skrueger.geotools.XMapPane;
import skrueger.geotools.XMapPaneAction;
import skrueger.geotools.XMapPaneAction_Zoom;
import skrueger.geotools.XMapPaneTool;
import skrueger.swing.CancelButton;
import skrueger.swing.CancellableDialogAdapter;
import skrueger.swing.OkButton;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
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

	class XMapPaneTool_SetMapExtend extends XMapPaneTool {
		public XMapPaneTool_SetMapExtend() {

			setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)); // TODO
			// nicer
			// cursor

			setMouseAction(MouseInputType.LDrag, new XMapPaneAction_Zoom.In() {

				@Override
				public void performClick(XMapPane mapPane, MouseEvent ev,
						DirectPosition coord) {
				}

				@Override
				public void performDragged(XMapPane mapPane, MouseEvent ev,
						Point dragStartPos, Point dragLastPos,
						DirectPosition startCoord, DirectPosition endCoord) {

					// Re-enable the listeners state
					getDesignMapView().getMapPane().setTool(backupTool);

					// getDesignMapView().getToolBar().setEnabled(true);
					getDesignMapView().getToolBar().setAllToolsEnabled(true, false);
					
					final ReferencedEnvelope maxExtend = JTSUtil
							.createReferencedEnvelope(startCoord, endCoord);

					map.setMaxExtend(maxExtend);

					if (maxExtendPreviewCheckBox.isSelected()) {
						getDesignMapView().getGeoMapPane().getMapPane()
								.setMaxExtend(map.getMaxExtend());
					}

					if (maxExtendButton != null)
						maxExtendButton.setEnabled(false);
					if (maxExtendResetButton != null)
						maxExtendResetButton.setEnabled(true);

					updateMapMaxExtendInMapContext();
				}

				// @Override
				// public void performDragging(XMapPane mapPane, MouseEvent ev,
				// Point dragStartPos, Point dragLastPos,
				// DirectPosition startCoord, DirectPosition endCoord) {
				// super.p
				// }

			});
		}
	}

	protected final XMapPaneTool defineMaxMapExtendsTool = new XMapPaneTool_SetMapExtend();

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

	// A private MapLayer to show the selected max zoom region...
	private DefaultMapLayer maxExtendBBOXMapLayer = null;

	private JButton maxExtendButton;
	private JCheckBox maxExtendPreviewCheckBox;

	private JButton maxExtendResetButton;

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
		super(owner, AtlasCreator.R("DesignMapViewJDialog.title", map_
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
	 * If a maximum map extend is defined for the {@link Map}, it will be added
	 * to the {@link MapContext} as a red/white rectangle. If no map extend is
	 * defined, it will be removed from the {@link MapContext}.
	 */
	private void updateMapMaxExtendInMapContext() {

		if (map.getMaxExtend() == null) {

			// Wenn das layer schon existierte, dann entfernen wir es.
			if (maxExtendBBOXMapLayer != null) {
				getDesignMapView().getMapPane().getMapContext().removeLayer(
						maxExtendBBOXMapLayer);
				maxExtendBBOXMapLayer = null;
			}
			return;
		}

		final ReferencedEnvelope mapMaxExtend = new ReferencedEnvelope(map
				.getMaxExtend(), getDesignMapView().getMapPane()
				.getMapContext().getCoordinateReferenceSystem());

		/*
		 * Die max extend bbox soll in der karte rot erscheinen
		 */

		final SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName("internal");
		builder.setNamespaceURI("http://localhost/");
		builder.setCRS(mapMaxExtend.getCoordinateReferenceSystem());
		builder.add("Location", LineString.class);
		builder.add("Name", String.class);

		final SimpleFeatureType FLAG = builder.buildFeatureType();

		final GeometryFactory factory = JTSFactoryFinder
				.getGeometryFactory(null);

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

		final LineString lineString = factory
				.createLineString(new Coordinate[] { c1, c3, c2, c4, c1 });

		final SimpleFeature maxExtendFeature = SimpleFeatureBuilder.build(FLAG,
				new Object[] { lineString, "max extend" }, "flag.1");

		final MemoryFeatureCollection fc = new MemoryFeatureCollection(FLAG);
		fc.add(maxExtendFeature);

		final StyleBuilder sb = StylingUtil.STYLE_BUILDER;
		final LineSymbolizer dash1 = sb.createLineSymbolizer(sb.createStroke(
				Color.red.darker(), 3, new float[] { 1, 2 }));
		final LineSymbolizer dash2 = sb.createLineSymbolizer(sb.createStroke(
				Color.white, 3, new float[] { 2, 1 }));
		final Style style = sb.createStyle(dash2);
		style.featureTypeStyles().get(0).rules().get(0).symbolizers()
				.add(dash1);

		maxExtendBBOXMapLayer = new AtlasMapLayer(fc, style);
		maxExtendBBOXMapLayer.setTitle("max Extend");

		getDesignMapView().getMapPane().getMapContext().addLayer(
				maxExtendBBOXMapLayer);
		getDesignMapView().getMapPane().setMapLayerSelectable(
				maxExtendBBOXMapLayer, false);
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

		final JButton saveDividerRatioButton = new JButton(
				new AbstractAction(AtlasCreator
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
						if (!AVUtil
								.askYesNo(
										DesignMapViewJDialog.this,
										AtlasCreator
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
						if (!AVUtil
								.askYesNo(
										DesignMapViewJDialog.this,
										AtlasCreator
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

				});
		saveDividerRatioButton.setToolTipText(AtlasCreator
				.R("DesignMapViewJDialog.Button.SaveDivider.TT"));

		final JButton saveAutoDividerRatioButton = new JButton(
				new AbstractAction(AtlasCreator
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
						if (!AVUtil
								.askYesNo(
										DesignMapViewJDialog.this,
										AtlasCreator
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
						if (!AVUtil
								.askYesNo(
										DesignMapViewJDialog.this,
										AtlasCreator
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

				});
		saveAutoDividerRatioButton.setToolTipText(AtlasCreator
				.R("DesignMapViewJDialog.Button.AutoDivider.TT"));

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

			final JPanel maxExtendPanel = new JPanel(new MigLayout(),
					AtlasCreator
							.R("DesignMapViewJDialog.mapArea.maxExtend.border"));

			maxExtendPanel.add(new JLabel(AtlasCreator
					.R("DesignMapViewJDialog.mapArea.maxExtend.explanation")),
					"span 3, wrap");

			/*
			 * Adding the Button to select a max. zoom extend
			 */
			maxExtendButton = new JButton(new AbstractAction(AtlasCreator
					.R("DesignMapView.SetMaxBBoxTool"),
					Icons.ICON_MAPEXTEND_BBOX) {

				@Override
				public void actionPerformed(final ActionEvent e) {

					backupTool = getDesignMapView().getMapPane().getTool();

					getDesignMapView().getMapPane().setTool(
							defineMaxMapExtendsTool);
					
					getDesignMapView().getToolBar().setAllToolsEnabled(false, false);

					JOptionPane
							.showMessageDialog(
									DesignMapViewJDialog.this,
									AtlasCreator
											.R("DesignMapView.SetMaxBBoxTool.pleaseSelectBBOX.msg"),
									AtlasCreator
											.R("DesignMapView.SetMaxBBoxTool"),
									JOptionPane.INFORMATION_MESSAGE);

					// Disable itself until the listener got an event
					maxExtendButton.setEnabled(false);
				}
			});
			maxExtendButton.setToolTipText(AtlasCreator
					.R("DesignMapView.SetMaxBBoxTool.TT"));
			maxExtendPanel.add(maxExtendButton);
			maxExtendButton.setEnabled(map.getMaxExtend() == null);

			// Add the box to the map
			updateMapMaxExtendInMapContext();

			/*
			 * Adding the Button to reset the min. zoom extend
			 */
			maxExtendResetButton = new JButton(new AbstractAction(AtlasCreator
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
			maxExtendResetButton.setToolTipText(AtlasCreator
					.R("DesignMapView.SetMaxBBoxTool.RemoveButton.TT"));
			maxExtendPanel.add(maxExtendResetButton);
			maxExtendResetButton.setEnabled(map.getMaxExtend() != null);

			maxExtendPreviewCheckBox = new JCheckBox(
					new AbstractAction(
							AtlasCreator
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

			maxExtendPreviewCheckBox.setSelected(true);
			maxExtendPanel.add(maxExtendPreviewCheckBox, "wrap");

			mapAreaPanel.add(maxExtendPanel, "wrap");
		} // maxExtendPanel

		/**
		 * A button to store the start area
		 */
		final JButton saveRegionButton = new JButton(new AbstractAction(
				AtlasCreator.R("DesignMapViewJDialog.Button.SaveBBOX.Label")) {

			@Override
			public void actionPerformed(final ActionEvent e) {

				if (!AVUtil.askYesNo(DesignMapViewJDialog.this, AtlasCreator
						.R("DesignMapViewJDialog.SaveBBOX.Question")))
					return;

				final Envelope newArea = designMapView.getGeoMapPane()
						.getMapPane().getMapArea();

				map.setDefaultMapArea(newArea);

				/**
				 * Ask the user to store the value for all maps
				 */
				if (!AVUtil
						.askYesNo(
								DesignMapViewJDialog.this,
								AtlasCreator
										.R("DesignMapViewJDialog.SaveBBOX.Question.forAllMaps")))
					return;
				for (final Map map : ace.getMapPool().values()) {
					map.setDefaultMapArea(newArea);
				}
			}

		});
		mapAreaPanel.add(saveRegionButton, "wrap");

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

		gridCrsSelector.setToolTipText(AtlasCreator
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

		gridFormatterSelector.setToolTipText(AtlasCreator
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
				new AbstractAction(AtlasCreator
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
		gridVisibibleJCheckBox.setToolTipText(AtlasCreator
				.R("DesignMapViewJDialog.MapFrameVisible.TT"));

		final JCheckBox scaleVisibibleJCheckBox = new JCheckBox(
				new AbstractAction(AtlasCreator
						.R("DesignMapViewJDialog.MapScaleVisible")) {

					@Override
					public void actionPerformed(final ActionEvent e) {
						map.setScaleVisible(((JCheckBox) e.getSource())
								.isSelected());
						designMapView.getGeoMapPane().getScalePane()
								.setVisible(map.isScaleVisible());
					}

				});
		scaleVisibibleJCheckBox.setToolTipText(AtlasCreator
				.R("DesignMapViewJDialog.MapScaleVisible.TT"));

		// Setting the initial values
		gridCrsSelector.setSelectedItem(map.getGridPanelCRS());
		gridFormatterSelector.setSelectedItem(map.getGridPanelFormatter()
				.getClass());
		gridVisibibleJCheckBox.setSelected(map.isGridPanelVisible());
		scaleVisibibleJCheckBox.setSelected(map.isScaleVisible());

		// box.add(gridCrsSelector);
		// box.add(gridFormatterSelector);

		final JPanel gridPanel = new JPanel(new MigLayout("wrap 1"));
		gridPanel.setBorder(BorderFactory.createTitledBorder(AtlasCreator
				.R("DesignMapViewJDialog.MapMargin.Border")));
		// DesignMapViewJDialog.MapFrameCRS= Map grid CRS:
		// DesignMapViewJDialog.MapFrameFormat= Map grid format:
		gridPanel.add(new JLabel(AtlasCreator
				.R("DesignMapViewJDialog.MapFrameCRS")), "split 2, right");
		gridPanel.add(gridCrsSelector, "sgx");

		gridPanel.add(new JLabel(AtlasCreator
				.R("DesignMapViewJDialog.MapFrameFormat")), "split 2, right");
		gridPanel.add(gridFormatterSelector, "sgx");
		gridPanel.add(gridVisibibleJCheckBox, "split 2");
		gridPanel.add(scaleVisibibleJCheckBox);
		panel.add(gridPanel, "growx");

		return panel;
	}

	private JTabbedPane createTabs() {
		final JTabbedPane tabbedPane = new JTabbedPane();

		tabbedPane.insertTab(AtlasCreator.R("DesignMapViewJDialog.legend.tab"),
				null, createLegendePanel(), AtlasCreator
						.R("DesignMapViewJDialog.legend.tab.tt"), tabbedPane
						.getTabCount());
		tabbedPane.insertTab(AtlasCreator.R("DesignMapViewJDialog.margin.tab"),
				null, createMapMarginPanel(), AtlasCreator
						.R("DesignMapViewJDialog.margin.tab.tt"), tabbedPane
						.getTabCount());
		tabbedPane.insertTab(
				AtlasCreator.R("DesignMapViewJDialog.mapArea.tab"), null,
				createMapAreaPanel(), AtlasCreator
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
				AtlasCreator.getInstance().actionPerformed(
						new ActionEvent(DesignMapViewJDialog.this, (int) System
								.currentTimeMillis(),
								AtlasCreator.ActionCmds.testAV.toString()));
			}

		}, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

		return rootPane;
	}

}
