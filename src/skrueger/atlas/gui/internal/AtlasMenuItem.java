/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the AtlasViewer application - A GIS viewer application targeting at end-users with no GIS-experience. Its main purpose is to present the atlases created with the Geopublisher application.
 * http://www.geopublishing.org
 * 
 * AtlasViewer is part of the Geopublishing Framework hosted at:
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
package skrueger.atlas.gui.internal;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;
import org.geotools.map.MapLayer;

import skrueger.atlas.AtlasViewer;
import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.dp.DpRef;
import skrueger.atlas.dp.layer.DpLayerRaster;
import skrueger.atlas.dp.layer.DpLayerRasterPyramid;
import skrueger.atlas.dp.layer.DpLayerVector;
import skrueger.atlas.dp.media.DpMediaPDF;
import skrueger.atlas.dp.media.DpMediaVideo;
import skrueger.atlas.map.Map;
import skrueger.atlas.map.MapRef;
import skrueger.atlas.resource.icons.Icons;
import skrueger.i8n.I8NUtil;

public class AtlasMenuItem extends JMenuItem {
	final static private Logger LOGGER = Logger.getLogger(AtlasMenuItem.class);

	public static final String ACTIONCMD_DATAPOOL_PREFIX = "datapool";
	public static final String ACTIONCMD_MAPPOOL_PREFIX = "mappool";

	private AtlasViewer atlasViewer;

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
	 *         Kr&uuml;ger</a>
	 */
	public AtlasMenuItem(Object child, AtlasViewer atlasViewer) {

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
