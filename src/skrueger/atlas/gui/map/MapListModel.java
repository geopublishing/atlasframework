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
package skrueger.atlas.gui.map;

import java.util.LinkedList;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.log4j.Logger;
import org.geotools.map.MapLayer;

import schmitzm.geotools.gui.GeoMapPane;
import schmitzm.swing.ExceptionDialog;
import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.dp.DpRef;
import skrueger.atlas.dp.layer.DpLayerRaster;
import skrueger.atlas.dp.layer.DpLayerRasterPyramid;
import skrueger.atlas.dp.layer.DpLayerVectorFeatureSource;
import skrueger.atlas.gui.AtlasMapLayer;
import skrueger.atlas.map.Map;

public class MapListModel implements ListModel {

	final static private Logger log = Logger.getLogger(MapListModel.class);

	List<ListDataListener> modelListeners = new LinkedList<ListDataListener>();

	private final Map map;

	private final GeoMapPane geoMapPane;

	public MapListModel(Map map, GeoMapPane geoMapPane_) {
		if (map == null) {
			log.warn("initializing MapListModel with map==null");
		}
		this.map = map;
		if (geoMapPane_ == null) {
			log.warn("initializing MapListModel with geoMapPane==null");
		}
		this.geoMapPane = geoMapPane_;
	}

	/**
	 * Returns the DatapoolEntry, not the reference...
	 */
	public DpRef getElementAt(int index) {
		return map.getLayers().get(reverse(index));
	}

	/**
	 * Reverse from JList <-> mapContext
	 * 
	 * @param index
	 * @return
	 */
	private int reverse(int index) {
		// log.debug( "from "+index+" => "+ ( getSize()-1-index) );
		return getSize() - 1 - index;
	}

	public final int getSize() {
		if (map == null)
			return 0;
		return map.getLayers().size();
	}

	public final void addListDataListener(ListDataListener l) {
		modelListeners.add(l);
	}

	public final void removeListDataListener(ListDataListener l) {
		modelListeners.remove(l);
	}

	public final void remove(int index) {
		index = reverse(index);
		map.getLayers().remove(index);
		geoMapPane.getMapContext().removeLayer(index);
		for (ListDataListener l : modelListeners) {
			l.contentsChanged(new ListDataEvent(this,
					ListDataEvent.CONTENTS_CHANGED, 0, getSize()));
		}
	}

	/**
	 * Removes a {@link MapLayer} from the Map
	 * 
	 * @param index
	 *            Index of the {@link org.opengis.layer.Layer}
	 */
	public final void removeRev(int index) {
		remove(reverse(index));
	}

	/**
	 * Adds a datapooRef to the {@link Map}
	 * 
	 * @param whereIdx
	 *            If <0, then inserted at the end
	 * @param dpRef
	 *            {@link DpRef} to add
	 * 
	 * @return The {@link MapLayer} that was just added
	 * 
	 * @throws Exception
	 */
	// TODO das ist irgendwie doppelt mit MapView addLayer !!
	public final MapLayer add(int whereIdx, DpRef dpRef) {
		whereIdx = reverse(whereIdx);
		MapLayer mapLayer = null;
		DpEntry dpe = dpRef.getTarget();
		try {
			if (dpe instanceof DpLayerVectorFeatureSource) {
				DpLayerVectorFeatureSource dplv = (DpLayerVectorFeatureSource) dpe;
				mapLayer = new AtlasMapLayer(dplv.getGeoObject(), dplv
						.getStyle());
			}

			else if (dpe instanceof DpLayerRaster) {
				DpLayerRaster dplr = (DpLayerRaster) dpe;
				mapLayer = new AtlasMapLayer(dplr.getGeoObject(), dplr
						.getStyle());
			}

			else if (dpe instanceof DpLayerRasterPyramid) {
				DpLayerRasterPyramid dplp = (DpLayerRasterPyramid) dpe;
				mapLayer = new AtlasMapLayer(dplp.getGeoObject(), dplp
						.getStyle());
				//	
				// // This listener has the purpose of inform of PANning actions
				// final PyramidListener pyramidListener = new
				// PyramidListener(dplp);
				// geoMapPane.getMapPane().addMouseListener(pyramidListener);
				// geoMapPane.getMapPane().addMapPaneListener(pyramidListener);
			}
		} catch (Exception e) {
			ExceptionDialog.show(geoMapPane, e);
			mapLayer = null;
			return null;
		}

		/**
		 * Now adding the mapLayer to the MapContext at the right position
		 */

		if ((whereIdx < 0) || (whereIdx > map.getLayers().size())) { // TODO
			// vielleicht
			// +1 ?!
			// map.getLayers().add(dpRef);
			map.add(dpRef);
			geoMapPane.getMapContext().addLayer(mapLayer);
		} else {
			// map.getLayers().add(whereIdx+1, dpRef);
			map.add(dpRef, whereIdx + 1);
			geoMapPane.getMapContext().addLayer(whereIdx + 1, mapLayer);
		}

		/**
		 * Informing the listeners of the change TODO this can be optimized, so
		 * that only the corresponding part is informed
		 */
		for (ListDataListener l : modelListeners) {
			l.contentsChanged(new ListDataEvent(this,
					ListDataEvent.CONTENTS_CHANGED, 0, getSize()));
		}
		return mapLayer;
	}
}
