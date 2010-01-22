/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the AtlasStyler SLD/SE Editor application - A Java Swing-based GUI to create OGC Styled Layer Descriptor (SLD 1.0) / OGC Symbology Encoding 1.1 (SE) XML files.
 * http://www.geopublishing.org
 * 
 * AtlasStyler SLD/SE Editor is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.sld.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.geotools.data.FeatureSource;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;
import org.geotools.styling.SLDTransformer;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import schmitzm.swing.JPanel;
import skrueger.geotools.MapContextManagerInterface;
import skrueger.geotools.StyledLayerInterface;
import skrueger.sld.ASProps;
import skrueger.sld.ASUtil;
import skrueger.sld.AtlasStyler;
import skrueger.swing.AtlasDialog;

public class XMLCodeFrame extends AtlasDialog {
	protected Logger LOGGER = ASUtil.createLogger(this);
	private final MapContextManagerInterface mapContextManagerInterface;

	Map<String, JScrollPane> names2xml = new HashMap<String, JScrollPane>();
	private JTabbedPane tabbedPane;

	public XMLCodeFrame(Window parent,
			MapContextManagerInterface mapContextManagerInterface) {
		super(parent);

		this.mapContextManagerInterface = mapContextManagerInterface;

		this.mapContextManagerInterface
				.addMapLayerListListener(new MapLayerListListener() {

					@Override
					public void layerAdded(MapLayerListEvent arg0) {
						final FeatureSource<SimpleFeatureType, SimpleFeature> sFeatureSource = (FeatureSource<SimpleFeatureType, SimpleFeature>) arg0
								.getLayer().getFeatureSource();
						String layername = sFeatureSource.getSchema().getName()
								.getLocalPart();

						JTextPane textPane = new JTextPane();
						JScrollPane textScrollPane = new JScrollPane(textPane);

						getTabbedPane().add(layername, textScrollPane);

						LOGGER.debug("Adding Layname  = " + layername);

						names2xml.put(layername, textScrollPane);

						setStyleXMLforLayer(arg0.getLayer().getStyle(),
								textPane);
					}

					@Override
					public void layerChanged(MapLayerListEvent arg0) {
						String layername = arg0.getLayer().getFeatureSource()
								.getName().getLocalPart();
						
						JTextPane textPane = (JTextPane) names2xml.get(
								layername).getViewport().getView();
						
						Style style2show = arg0.getLayer().getStyle();

						setStyleXMLforLayer(style2show, textPane);

						JScrollPane com = names2xml.get(layername);

						getTabbedPane().setSelectedComponent(com);
						getTabbedPane().revalidate();
					}

					@Override
					public void layerMoved(MapLayerListEvent arg0) {
					}

					@Override
					public void layerRemoved(MapLayerListEvent arg0) {
						String layername = arg0.getLayer().getFeatureSource()
								.getName().getLocalPart();

						LOGGER.debug("Removing Tab " + layername
								+ " from JTabbedPane");

						names2xml.remove(layername);

						
						getTabbedPane().removeAll();
						tabbedPane = null;

						if (names2xml.size() > 0) {

							for (String title : names2xml.keySet()) {
								getTabbedPane()
								.add(title, names2xml.get(title));
							}
							
						}
						
						XMLCodeFrame.this.getContentPane().removeAll();
						XMLCodeFrame.this.getContentPane().add(getTabbedPane());
						XMLCodeFrame.this.invalidate();
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
		Charset charset = Charset.forName(ASProps.get(ASProps.Keys.charsetName,
				"UTF-8"));

		StringWriter w = null;
		final SLDTransformer aTransformer = new SLDTransformer();
		if (charset != null) {
			aTransformer.setEncoding(charset);
		}
		aTransformer.setIndentation(2);
		String xml;
		try {
			xml = aTransformer.transform(style2show);
			w = new StringWriter();
			w.write(xml);
			w.close();
			w.getBuffer();
			textPane.setText(w.getBuffer().toString());
		} catch (TransformerException e) {
			StringBuffer stackTrace = new StringBuffer();
			for (StackTraceElement ste : e.getStackTrace()) {
				stackTrace.append(ste.toString());
			}
			textPane.setText(stackTrace.toString());
		} catch (IOException e) {
			StringBuffer stackTrace = new StringBuffer();
			for (StackTraceElement ste : e.getStackTrace()) {
				stackTrace.append(ste.toString());
			}
			textPane.setText(stackTrace.toString());
		}

		LOGGER.info("Updated XML Pane");

	}

	protected JTabbedPane getTabbedPane() {
		if (tabbedPane == null) {
			tabbedPane = new JTabbedPane();
		}
		return tabbedPane;
	}

	public void initialize() {
		setTitle(AtlasStyler.R("XMLCodeFrame.title"));
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(screenSize.width / 3, screenSize.height);
		setLocation(screenSize.width / 3 * 2, 0);
		setVisible(false);
		setContentPane(new JPanel(new BorderLayout()));
		getContentPane().add(getTabbedPane(), BorderLayout.CENTER);
	}

}
