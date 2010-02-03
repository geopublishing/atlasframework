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
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.geotools.map.MapContext;

import skrueger.atlas.AtlasViewer;
import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.dp.DpRef;
import skrueger.atlas.gui.MapLegend;
import skrueger.atlas.gui.map.AtlasMapView;
import skrueger.atlas.map.Map;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.AtlasCreator;
import skrueger.creator.GPProps;
import skrueger.creator.GPProps.Keys;
import skrueger.geotools.MapView;
import skrueger.geotools.StyledLayerInterface;

/**
 * This is the {@link MapView} used in the {@link AtlasCreator} to preview the
 * {@link Map}s.<br>
 * It allows the HTML folder to be changed and can be edited.<br>
 */
public class DesignMapView extends AtlasMapView {
	final static private Logger LOGGER = Logger.getLogger(DesignMapView.class);

	protected AtlasConfigEditable ace;

	private DesignHTMLInfoJPane designInfoPanel;

	/**
	 * Opens a {@link DesignMapView} to edit/layout the {@link Map} Remembers
	 * its existence in openInstances
	 * 
	 * Call setMap() after construction, and then Call initialize()
	 * 
	 * @param owner
	 *            The parent frame
	 * @param ace_
	 */
	public DesignMapView(Component owner, AtlasConfigEditable ace_) {
		super(owner, ace_);
		ace = ace_;

		// Show Exceptions in the map window
		getGeoMapPane().getMapPane().setShowExceptions(true);

		// Using the AtlasCreator settings
		getGeoMapPane().getMapPane().setAntiAliasing(
				GPProps.getInt(GPProps.Keys.antialiasingMaps, 1) == 1);
	}

	/**
	 * This sidepanel is different . It uses a {@link DesignAtlasMapLegend}
	 * instead of a simple {@link MapLegend}
	 */
	@Override
	public JComponent getSidePane() {

		if (layerManager == null) {
			JTabbedPane tabbedPane;
			tabbedPane = new JTabbedPane();
			
			final DesignAtlasMapLegend designLayerPanel = new DesignAtlasMapLegend(
					getGeoMapPane(), map, ace, getToolBar());
			
			layerManager = designLayerPanel;
			JScrollPane scrollPane = new JScrollPane(designLayerPanel);
			tabbedPane.addTab(AtlasViewer
					.R("AtlasMapView.tabbedPane.LayersTab_label"),
					scrollPane);
			
			tabbedPane.setToolTipTextAt(0, AtlasViewer
					.R("AtlasMapView.tabbedPane.LayersTab_tt"));

			JScrollPane scrollpane2 = new JScrollPane(getDesignInfoPanel());
			tabbedPane.addTab(AtlasViewer
					.R("AtlasMapView.tabbedPane.InfoTab_label"),
					scrollpane2);
			
			tabbedPane.setToolTipTextAt(1, AtlasViewer
					.R("AtlasMapView.tabbedPane.InfoTab_tt"));

			add(tabbedPane, BorderLayout.CENTER);

			leftSide = tabbedPane;
			
		}
		return leftSide;
	}

	/**
	 * Updates the list of layers in this map according to the underlying
	 * {@link MapContext} of the preview map.
	 */
	public void updateMapLayersFromMapContext(Map map2) {
		if (layerManager == null) {
			return;
		}

		map2.clearLayerList();

		List<StyledLayerInterface<?>> styledObjects = layerManager
				.getStyledObjects();

		for (StyledLayerInterface<?> o : styledObjects) {
			if (o == null)
				continue;
			String id = o.getId();
			DpEntry<?> dpEntry = ace.getDataPool().get(id);
			if (dpEntry == null) {
				LOGGER
						.warn("strange.. deleted from datapool while the mapview was open???");
			} else {
				map2.add(new DpRef(dpEntry));
			}
		}

	}

	/**
	 * Lazily initializes the {@link DesignHTMLInfoJPane} for this map.
	 */
	public DesignHTMLInfoJPane getDesignInfoPanel() {
		if (designInfoPanel == null) {
			designInfoPanel = new DesignHTMLInfoJPane(ace, map);
		}
		return designInfoPanel;
	}

	/**
	 * The trick here is to calculate the position of the divider according to a
	 * ratio, BEFORE the {@link JSplitPane} is visible and has any width! In the
	 * Atlas it works to use the owner, which is instance of {@link JFrame}.
	 * 
	 * @return The number in pixels from the left
	 */
	@Override
	protected int calcAbsoluteWidthForDivider(Double ratio) {
		int width = getWidth();
		if (width <= 0) {
			width = GPProps.getInt(Keys.mapComposerWidth, 800);
		}
		return (int) (width * ratio);
	}
//
//	@Override
//	public void initialize() {
//		super.initialize(); // important!
//	}

}
