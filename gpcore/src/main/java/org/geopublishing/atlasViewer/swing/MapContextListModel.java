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
package org.geopublishing.atlasViewer.swing;

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
 * @author Stefan Alfons Tzeggai
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

			@Override
			public void layerAdded(MapLayerListEvent event) {
				for (ListDataListener l : modelListeners) {
					l.contentsChanged(new ListDataEvent(this,
							ListDataEvent.CONTENTS_CHANGED, 0, getSize()));
				}

			}

			@Override
			public void layerChanged(MapLayerListEvent event) {
				for (ListDataListener l : modelListeners) {
					l.contentsChanged(new ListDataEvent(this,
							ListDataEvent.CONTENTS_CHANGED, 0, getSize()));
				}

			}

			@Override
			public void layerMoved(MapLayerListEvent event) {
				for (ListDataListener l : modelListeners) {
					l.contentsChanged(new ListDataEvent(this,
							ListDataEvent.CONTENTS_CHANGED, 0, getSize()));
				}

			}

			@Override
			public void layerRemoved(MapLayerListEvent event) {
				for (ListDataListener l : modelListeners) {
					l.contentsChanged(new ListDataEvent(this,
							ListDataEvent.CONTENTS_CHANGED, 0, getSize()));
				}

			}
//
			@Override
			public void layerPreDispose(MapLayerListEvent event) {
				// GT 2.7.4
				
			}

		});
	}

	/**
	 * The order has to be turned around!
	 */
	@Override
	public final Object getElementAt(int index) {
		return mapContext.getLayer(mapContext.getLayerCount() - 1 - index);
	}

	@Override
	public final int getSize() {
		return mapContext.getLayerCount();
	}

	@Override
	public final void addListDataListener(ListDataListener l) {
		modelListeners.add(l);
	}

	@Override
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
