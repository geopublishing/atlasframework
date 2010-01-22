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
package skrueger.atlas;

import java.util.LinkedList;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.log4j.Logger;
import org.geotools.map.MapContext;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;

/**
 * A ListModel that represents the order of a {@link MapContext}
 * 
 * @author Stefan Alfons Krüger
 */

// TODO add noch nicht möglich???
public final class MapContextListModel implements ListModel {
	final static Logger log = Logger.getLogger(MapContextListModel.class);

	List<ListDataListener> modelListeners = new LinkedList<ListDataListener>();

	private MapContext mapContext;

	public MapContextListModel(MapContext mapContext) {
		this.mapContext = mapContext;

		// TODO BESSER
		mapContext.addMapLayerListListener(new MapLayerListListener() {

			public void layerAdded(MapLayerListEvent event) {
				for (ListDataListener l : modelListeners) {
					l.contentsChanged(new ListDataEvent(this,
							ListDataEvent.CONTENTS_CHANGED, 0, getSize()));
				}

			}

			public void layerChanged(MapLayerListEvent event) {
				for (ListDataListener l : modelListeners) {
					l.contentsChanged(new ListDataEvent(this,
							ListDataEvent.CONTENTS_CHANGED, 0, getSize()));
				}

			}

			public void layerMoved(MapLayerListEvent event) {
				for (ListDataListener l : modelListeners) {
					l.contentsChanged(new ListDataEvent(this,
							ListDataEvent.CONTENTS_CHANGED, 0, getSize()));
				}

			}

			public void layerRemoved(MapLayerListEvent event) {
				for (ListDataListener l : modelListeners) {
					l.contentsChanged(new ListDataEvent(this,
							ListDataEvent.CONTENTS_CHANGED, 0, getSize()));
				}

			}

		});
	}

	/**
	 * The order has to be turned around!
	 */
	public final Object getElementAt(int index) {
		return mapContext.getLayer(mapContext.getLayerCount() - 1 - index);
	}

	public final int getSize() {
		return mapContext.getLayerCount();
	}

	public final void addListDataListener(ListDataListener l) {
		modelListeners.add(l);
	}

	public final void removeListDataListener(ListDataListener l) {
		modelListeners.remove(l);
	}

	public final void remove(int index) {
		mapContext.removeLayer(mapContext.getLayerCount() - 1 - index);
		for (ListDataListener l : modelListeners) {
			l.contentsChanged(new ListDataEvent(this,
					ListDataEvent.CONTENTS_CHANGED, 0, getSize()));
		}

	}

	public final void move(int from, int to) {
		for (ListDataListener l : modelListeners) {
			l.contentsChanged(new ListDataEvent(this,
					ListDataEvent.CONTENTS_CHANGED, 0, getSize()));
		}
		mapContext.moveLayer(mapContext.getLayerCount() - 1 - from, mapContext
				.getLayerCount()
				- 1 - to);
	}

	public MapContext getMapContext() {
		return mapContext;
	}
}
