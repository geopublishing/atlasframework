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
package org.geopublishing.atlasViewer.map;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.DpRef;
import org.geopublishing.atlasViewer.dp.Group;
import org.geotools.util.WeakHashSet;

import de.schmitzm.jfree.chart.style.ChartStyle;

/**
 * This Class holds all {@link Map}s defined for this {@link AtlasConfig} by
 * their ID The Map are sorted/ordered by putting them in the {@link Group}s and
 * subgroups
 * 
 * @author Stefan Alfons Tzeggai
 * 
 */
public class MapPool extends TreeMap<String, Map> {
	final static private Logger LOGGER = Logger.getLogger(MapPool.class);

	/**
	 * This can be set to a mapID. The referenced {@link Map} will then be shown
	 * at startup *
	 */
	protected String startMapID = null;

	private final WeakHashSet<PropertyChangeListener> listeners = new WeakHashSet<PropertyChangeListener>(
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
	 * 
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
			return get(((Integer) key).intValue());
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
				if (size() > 0)
					setStartMapID(get(0).getId());
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
	 * If {@link #quite} == <code>true</code> no {@link RuleChangedEvent} will
	 * be fired.
	 */
	private boolean quite = false;

	PropertyChangeEvent lastOpressedEvent = null;
	Stack<Boolean> stackQuites = new Stack<Boolean>();

	/**
	 * Add a QUITE-State to the event firing state stack
	 */
	public void pushQuite() {
		stackQuites.push(quite);
		setQuite(true);
	}

	/**
	 * Remove a QUITE-State from the event firing state stack
	 */
	public void popQuite() {
		setQuite(stackQuites.pop());
		if (quite == false) {
			if (lastOpressedEvent != null)
				fireChangeEvents(lastOpressedEvent);
		} else {
			LOGGER.debug("not firing event because there are "
					+ stackQuites.size() + " 'quites' still on the stack");
		}

	}

	public void popQuite(PropertyChangeEvent fireThis) {
		setQuite(stackQuites.pop());
		if (quite == false && fireThis != null)
			fireChangeEvents(fireThis);
		else {
			lastOpressedEvent = fireThis;
			LOGGER.debug("not firing event " + fireThis + " because there are "
					+ stackQuites.size() + " 'quites' still on the stack");
		}
	}

	/**
	 * If quite, the RuleList will not fire {@link RuleChangedEvent}s.
	 */
	private void setQuite(boolean b) {
		quite = b;
	}

	/**
	 * If quite, the RuleList will not fire {@link RuleChangedEvent}s
	 */
	public boolean isQuite() {
		return quite;
	}

	/**
	 * Informs all registered {@link PropertyChangeListener}s about a change in
	 * the {@link MapPool}.
	 */
	public void fireChangeEvents(Object source, EventTypes type, Map map) {

		PropertyChangeEvent pce = new PropertyChangeEvent(source,
				type.toString(), map, map);

		fireChangeEvents(pce);
	}

	private void fireChangeEvents(PropertyChangeEvent pce) {
		if (quite) {
			lastOpressedEvent = pce;
			return;
		} else {
			lastOpressedEvent = null;
		}

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
	 * Returns a list of {@link Map}s that use the given {@link DpEntry}
	 */
	public Set<Map> getMapsUsing(DpEntry<? extends ChartStyle> dpl) {
		Set<Map> maps = new HashSet<Map>();

		for (Map m : values()) {
			for (DpRef<?> dpRef : m.getLayers()) {
				if (dpRef.getTarget().equals(dpl)) {
					maps.add(m);
					continue;
				}
			}

			for (DpRef<?> dpRef : m.getMedia()) {
				if (dpRef.getTarget().equals(dpl)) {
					maps.add(m);
					continue;
				}
			}
		}

		return maps;
	}

	public Map getStartMap() {
		return get(getStartMapID());
	}
}
