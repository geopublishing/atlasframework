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
package org.geopublishing.atlasStyler.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geotools.data.FeatureSource;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.styling.Style;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

import de.schmitzm.geotools.MapContextManagerInterface;
import de.schmitzm.geotools.map.event.MapLayerListAdapter;
import de.schmitzm.geotools.styling.StyledLayerInterface;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.io.IOUtil;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.AtlasDialog;
import de.schmitzm.swing.JPanel;
import de.schmitzm.versionnumber.ReleaseUtil;

/**
 * This {@link AtlasDialog} shows XML/SLD for all layers in the map. It is
 * automatically updated by installing a listener to the
 * {@link MapContextManagerInterface} passed to the constructor.
 */
public class XMLCodeFrame extends AtlasDialog {
	private static final String PRODUCTION = ".production";
	protected Logger LOGGER = LangUtil.createLogger(this);
	private final MapContextManagerInterface mapContextManagerInterface;

	Map<String, JScrollPane> names2xml = new HashMap<String, JScrollPane>();
	private JTabbedPane tabbedPane;

	/**
	 * This {@link AtlasDialog} shows SLD for all layers in the map. It is
	 * automatically updated by installing a listener to the
	 * {@link MapContextManagerInterface} passed to the constructor.
	 */
	public XMLCodeFrame(Window parent,
			MapContextManagerInterface mapContextManagerInterface) {
		super(parent);

		this.mapContextManagerInterface = mapContextManagerInterface;

		this.mapContextManagerInterface
				.addMapLayerListListener(new MapLayerListAdapter() {

					@Override
					public void layerAdded(MapLayerListEvent evt) {

						String layername = getLayername(evt);

						String layernameProduction = layername + PRODUCTION;

						JTextPane textPane = new JTextPane();
						JTextPane textPaneProduction = new JTextPane();

						JScrollPane textScrollPane = new JScrollPane(textPane);
						JScrollPane textScrollPaneProduction = new JScrollPane(
								textPaneProduction);

						getTabbedPane().add(layername, textScrollPane);
						getTabbedPane().add(layernameProduction,
								textScrollPaneProduction);

						names2xml.put(layername, textScrollPane);
						names2xml.put(layernameProduction,
								textScrollPaneProduction);

						setStyleXMLforLayer(evt.getLayer().getStyle(), textPane);
						setStyleXMLforLayer(getOptimizedStyle(evt),textPaneProduction);
					}

					private Style getOptimizedStyle(MapLayerListEvent evt) {
						return StylingUtil.optimizeStyle(evt
								.getLayer().getStyle(), "AtlasStyler "
								+ ReleaseUtil.getVersionInfo(AtlasStyler.class)

								+ ", Export-Mode: PRODUCTION");
					}

					private String getLayername(MapLayerListEvent evt) {

						MapLayer layer = evt.getLayer();
						FeatureSource<? extends FeatureType, ? extends Feature> featureSource = layer
								.getFeatureSource();
						final FeatureSource<SimpleFeatureType, SimpleFeature> sFeatureSource = (FeatureSource<SimpleFeatureType, SimpleFeature>) featureSource;

						String layername = sFeatureSource.getSchema().getName()
								.getLocalPart();

						if (layername.equalsIgnoreCase("GridCoverage")) {
							layername = layer.getTitle();
							try {
								// Usually layername will now be path to a file.
								// If so, try to get the last part without he
								// ending.
								String nameExtraDot = IOUtil.changeFileExt(
										new File(layername), "").getName();
								layername = nameExtraDot.substring(0,
										nameExtraDot.length() - 1);
							} catch (Exception e) {
							}
						}

						return layername;

					}

					@Override
					public void layerChanged(MapLayerListEvent evt) {
						String layername = getLayername(evt);

						int oldSelected = getTabbedPane().getSelectedIndex();

						JTextPane textPane = (JTextPane) names2xml
								.get(layername).getViewport().getView();
						JTextPane textPaneProduction = (JTextPane) names2xml
								.get(layername + PRODUCTION).getViewport()
								.getView();

						setStyleXMLforLayer(evt.getLayer().getStyle(), textPane);
						setStyleXMLforLayer(getOptimizedStyle(evt), textPaneProduction);

						getTabbedPane().setSelectedIndex(oldSelected);

						getTabbedPane().revalidate();
					}

					@Override
					public void layerRemoved(MapLayerListEvent evt) {
						String layername = getLayername(evt);

						// LOGGER.debug("Removing Tab " + layername
						// + " from JTabbedPane");

						names2xml.remove(layername);
						names2xml.remove(layername + PRODUCTION);

						getTabbedPane().removeAll();
						tabbedPane = null;

						if (names2xml.size() > 0) {

							for (String title : names2xml.keySet()) {
								getTabbedPane()
										.add(title, names2xml.get(title));
							}

						}

						updateGuiTabs();

					}

					private void updateGuiTabs() {
						XMLCodeFrame.this.getContentPane().removeAll();
						XMLCodeFrame.this.getContentPane().add(getTabbedPane());
						XMLCodeFrame.this.invalidate();
						XMLCodeFrame.this.validate();
						XMLCodeFrame.this.repaint();
					}
				});

		// Alle bereits existierenden Layer einfügen:
		for (StyledLayerInterface s : mapContextManagerInterface
				.getStyledObjects()) {
			JTextPane textPane = new JTextPane();
			String layername = s.getTitle().toString();

			JScrollPane textScrollPane = new JScrollPane(textPane);
			names2xml.put(layername, textScrollPane);

			setStyleXMLforLayer(s.getStyle(), textPane);
		}

		initialize();

	}

	/**
	 * Converts a {@link Style} to String and shwos it inside a
	 * {@link JTextPane}. If an error occurs, the error is shown in the
	 * {@link JTextPane}.
	 */
	protected void setStyleXMLforLayer(Style style2show, JTextPane textPane) {
		try {
			String sldString = StylingUtil.sldToString(style2show);
			textPane.setText(sldString);
		} catch (TransformerException e) {
			StringBuffer stackTrace = new StringBuffer();
			for (StackTraceElement ste : e.getStackTrace()) {
				stackTrace.append(ste.toString());
			}
			textPane.setText(stackTrace.toString());
		}
	}

	protected JTabbedPane getTabbedPane() {
		if (tabbedPane == null) {
			tabbedPane = new JTabbedPane();
		}
		return tabbedPane;
	}

	public void initialize() {
		setTitle(ASUtil.R("XMLCodeFrame.title"));
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(screenSize.width / 3, screenSize.height);
		setLocation(screenSize.width / 3 * 2, 0);
		setVisible(false);
		setContentPane(new JPanel(new BorderLayout()));
		getContentPane().add(getTabbedPane(), BorderLayout.CENTER);
	}

}
