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
package org.geopublishing.atlasViewer.swing.internal;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.DpRef;
import org.geopublishing.atlasViewer.dp.layer.DpLayerRaster;
import org.geopublishing.atlasViewer.dp.layer.DpLayerRasterPyramid;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVector;
import org.geopublishing.atlasViewer.dp.media.DpMediaPDF;
import org.geopublishing.atlasViewer.dp.media.DpMediaVideo;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.map.MapRef;
import org.geopublishing.atlasViewer.swing.AtlasViewerGUI;
import org.geopublishing.atlasViewer.swing.Icons;
import org.geotools.map.MapLayer;

import skrueger.i8n.I8NUtil;

public class AtlasMenuItem extends JMenuItem {
	final static private Logger LOGGER = Logger.getLogger(AtlasMenuItem.class);

	public static final String ACTIONCMD_DATAPOOL_PREFIX = "datapool";
	public static final String ACTIONCMD_MAPPOOL_PREFIX = "mappool";

	private AtlasViewerGUI atlasViewer;

	static final Color normalColor = new JMenuItem().getForeground();

	// TODO AtlasSettings
	public static final Font BIGFONT = new javax.swing.JLabel().getFont()
			.deriveFont(17f);

	/**
	 * Creates a {@link JMenuItem} from the given object. The actionCommand
	 * string is set to the {@link DpEntry}'s ID prefixed by Strings "mappool"
	 * or "datapool"
	 * 
	 * 
	 * @param atlasConfig
	 * @param child
	 *            Object present as {@link JMenuItem}
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Tzeggai</a>
	 */
	public AtlasMenuItem(Object child, AtlasViewerGUI atlasViewer) {

		this.atlasViewer = atlasViewer;
		addActionListener(atlasViewer);

		if (child instanceof MapRef) {
			MapRef mpr = (MapRef) child;
			Map map = mpr.getTarget();
			String string = map.getTitle().toString();
			setText(string);
			setIcon(Icons.ICON_MAP_BIG);

			if (!I8NUtil.isEmpty(map.getDesc()))
				setToolTipText(map.getDesc().toString());
			setActionCommand(ACTIONCMD_MAPPOOL_PREFIX + map.getId());
		}
		if (child instanceof DpRef) {
			DpRef dpr = (DpRef) child;
			DpEntry dpe = dpr.getTarget();
			String string = dpe.getTitle().toString();
			setText(string);
			if (!I8NUtil.isEmpty(dpe.getDesc()))
				setToolTipText(dpe.getDesc().toString());

			setActionCommand(ACTIONCMD_DATAPOOL_PREFIX + dpe.getId());

			// Setting a nice icon in the Menu
			if (dpe instanceof DpMediaPDF)
				setIcon(Icons.ICON_PDF_BIG);
			else if (dpe instanceof DpMediaVideo)
				setIcon(Icons.ICON_VIDEO_BIG);
			else if (dpe instanceof DpLayerRaster)
				setIcon(Icons.ICON_RASTER_BIG);
			else if (dpe instanceof DpLayerRasterPyramid)
				setIcon(Icons.ICON_RASTER_BIG);
			else if (dpe instanceof DpLayerVector) {
				DpLayerVector dplv = (DpLayerVector) dpe;
				setIcon(dplv.getType().getIconBig());
			}
		}

		setFont(BIGFONT);

	}

	/**
	 * Creates a normal {@link JMenuItem}, just using the big font
	 * 
	 * @param action
	 */
	public AtlasMenuItem(AbstractAction action, String toolTip) {
		super(action);
		setFont(BIGFONT);
		if (toolTip != null)
			setToolTipText(toolTip);
	}

	public AtlasMenuItem() {
		setFont(BIGFONT);
	}

	public AtlasMenuItem(AbstractAction abstractAction) {
		this(abstractAction, (String) null);
	}

	@Override
	/*
	 * The texts of a "add layer" menuitem are grey if the layer is
	 */
	protected void paintComponent(Graphics g) {

		// Check if this layer is already shown in the actual map
		if (getActionCommand().startsWith(ACTIONCMD_DATAPOOL_PREFIX)) {
			String dpId = getActionCommand().substring(
					ACTIONCMD_DATAPOOL_PREFIX.length());

			// If the layer is already part of the active map, disable it in the
			// menu.
			for (MapLayer layer : atlasViewer.getMapView().getGeoMapPane()
					.getMapContext().getLayers()) {
				if (layer.getTitle().equals(dpId)) {
					setForeground(Color.gray);
					super.paintComponent(g);
					return;
				}
			}
		}

		// Check if this map is already shown in the MapView
		else if (getActionCommand().startsWith(ACTIONCMD_MAPPOOL_PREFIX)) {
			String mapId = getActionCommand().substring(
					ACTIONCMD_MAPPOOL_PREFIX.length());

			// If this map
			if (atlasViewer.getMap().getId().equals(mapId)) {
				setForeground(Color.gray.lightGray);
				super.paintComponent(g);
				return;
			}
		}

		setForeground(normalColor);
		super.paintComponent(g);
	}

}
