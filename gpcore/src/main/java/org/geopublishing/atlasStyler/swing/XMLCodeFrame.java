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
import java.util.HashMap;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geotools.data.FeatureSource;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import de.schmitzm.geotools.MapContextManagerInterface;
import de.schmitzm.geotools.map.event.MapLayerListAdapter;
import de.schmitzm.geotools.styling.StyledLayerInterface;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.AtlasDialog;
import de.schmitzm.swing.JPanel;

public class XMLCodeFrame extends AtlasDialog {
	protected Logger LOGGER = LangUtil.createLogger(this);
	private final MapContextManagerInterface mapContextManagerInterface;

	Map<String, JScrollPane> names2xml = new HashMap<String, JScrollPane>();
	private JTabbedPane tabbedPane;

	public XMLCodeFrame(Window parent,
			MapContextManagerInterface mapContextManagerInterface) {
		super(parent);

		this.mapContextManagerInterface = mapContextManagerInterface;

		this.mapContextManagerInterface
				.addMapLayerListListener(new MapLayerListAdapter() {

					@Override
					public void layerAdded(MapLayerListEvent arg0) {
						final FeatureSource<SimpleFeatureType, SimpleFeature> sFeatureSource = (FeatureSource<SimpleFeatureType, SimpleFeature>) arg0
								.getLayer().getFeatureSource();
						String layername = sFeatureSource.getSchema().getName()
								.getLocalPart();

						JTextPane textPane = new JTextPane();
						JScrollPane textScrollPane = new JScrollPane(textPane);

						getTabbedPane().add(layername, textScrollPane);

						// LOGGER.debug("Adding Layname  = " + layername);

						names2xml.put(layername, textScrollPane);

						setStyleXMLforLayer(arg0.getLayer().getStyle(),
								textPane);
					}

					@Override
					public void layerChanged(MapLayerListEvent arg0) {
						String layername = arg0.getLayer().getFeatureSource()
								.getName().getLocalPart();

						JTextPane textPane = (JTextPane) names2xml
								.get(layername).getViewport().getView();

						Style style2show = arg0.getLayer().getStyle();

						setStyleXMLforLayer(style2show, textPane);

						JScrollPane com = names2xml.get(layername);

						getTabbedPane().setSelectedComponent(com);
						getTabbedPane().revalidate();
					}

					@Override
					public void layerRemoved(MapLayerListEvent arg0) {
						String layername = arg0.getLayer().getFeatureSource()
								.getName().getLocalPart();

						// LOGGER.debug("Removing Tab " + layername
						// + " from JTabbedPane");

						names2xml.remove(layername);

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
