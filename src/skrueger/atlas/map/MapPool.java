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
package skrueger.atlas.map;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.geotools.util.WeakHashSet;

import schmitzm.jfree.chart.style.ChartStyle;
import skrueger.atlas.AtlasConfig;
import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.dp.DpRef;
import skrueger.atlas.dp.Group;

/**
 * This Class holds all {@link Map}s defined for this {@link AtlasConfig} by
 * their ID The Map are sorted/ordered by putting them in the {@link Group}s and
 * subgroups
 * 
 * @author Stefan Alfons Krüger
 * 
 */
public class MapPool extends TreeMap<String, Map> {
	final static private Logger LOGGER = Logger.getLogger(MapPool.class);

	/**
	 * This can be set to a mapID. The referenced {@link Map} will then be shown
	 * at startup *
	 */
	protected String startMapID = null;

	private WeakHashSet<PropertyChangeListener> listeners = new WeakHashSet<PropertyChangeListener>(
			PropertyChangeListener.class);

	public enum EventTypes {
		addMap, removeMap, changeMap, unknown
	};

	/**
	 * The referenced {@link Map} will then be shown at startup and it will be
	 * automatically downloaded
	 */
	public String getStartMapID() {
		return startMapID;
	}

	/**
	 * This can be set to a mapID. The referenced {@link Map} will then be shown
	 * at startup *
	 */
	public void setStartMapID(String mapID) {
		this.startMapID = mapID;
	}

	/**
	 * Add a {@link Map} by its getId() directly. Calling this informs all
	 * listeners.
	 * 
	 * @return
	 */
	public Map put(Map map) {
		return add(map);
	}

	/**
	 * Return a {@link Map} by its position in the {@link MapPool}
	 * @param idx
	 */
	public Map get(int idx) {
		return (Map) values().toArray()[idx];
	}
	

	/**
	 * You can ask for the String ID or for an Integer, which is then is
	 * interpreted as the position in the ordered list.
	 */
	@Override
	public Map get(Object key) {
		if (key instanceof Integer) {
			return get(((Integer)key).intValue());
		}
		return super.get(key);
	}

	/**
	 * Add a {@link Map} by its getId() directly
	 * 
	 * @return
	 */
	public Map add(Map map) {
		if ((map == null) || (map.getId() == null))
			throw new IllegalArgumentException(
					"map is null or doesn't have an id. can't add to mapPool!");
		return put(map.getId(), map);
	}

	/**
	 * Overridden from {@link Map} to inform our {@link PropertyChangeListener}
	 */
	@Override
	public Map put(String key, Map value) {
		Map result = super.put(key, value);
		fireChangeEvents(this, EventTypes.addMap, result);
		return result;
	}

	/**
	 * Overridden from {@link Map} to inform our {@link PropertyChangeListener}
	 */
	@Override
	public Map remove(Object key) {
		Map result = super.remove(key);
		if (result != null) {
			// Check whether we killed the start map
			if (result.getId().equals(getStartMapID())) {
				if (size() > 0) setStartMapID(get(0).getId());
			}
		}
		fireChangeEvents(this, EventTypes.removeMap, result);
		return result;
	}

	/**
	 * {@link PropertyChangeListener} can be registered to be informed when the
	 * {@link MapPool} changes.
	 * 
	 * @param propertyChangeListener
	 */
	public void addChangeListener(PropertyChangeListener propertyChangeListener) {
		listeners.add(propertyChangeListener);
	}

	/**
	 * {@link PropertyChangeListener} can be registered to be informed when the
	 * {@link MapPool} changes.
	 * 
	 * @param propertyChangeListener
	 */
	public void removeChangeListener(
			PropertyChangeListener propertyChangeListener) {
		listeners.remove(propertyChangeListener);
	}

	/**
	 * Informs all registered {@link PropertyChangeListener}s about a change in
	 * the {@link MapPool}.
	 */
	public void fireChangeEvents(Object source, EventTypes type, Map map) {
		PropertyChangeEvent pce = new PropertyChangeEvent(source, type
				.toString(), map, map);

		for (PropertyChangeListener pcl : listeners) {
			if (pcl != null)
				pcl.propertyChange(pce);
		}
	}

	public void dispose() {
		// TODO dispose all maps!?
		listeners.clear();
	}

	/**
	 * Returns a list of Maps that use the given DpEntry
	 */
	public Set<Map> getMapsUsing(DpEntry<? extends ChartStyle> dpl) {
		Set<Map> maps = new HashSet<Map>();

		for (Map m : values()) {
			for (DpRef dpRef : m.getLayers()) {
				if (dpRef.getTarget().equals(dpl)) {
					maps.add(m);
					continue;
				}
			}

			for (DpRef dpRef : m.getMedia()) {
				if (dpRef.getTarget().equals(dpl)) {
					maps.add(m);
					continue;
				}
			}
		}

		return maps;
	}
}
