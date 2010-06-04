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
package org.geopublishing.atlasViewer.dp;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Stack;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.map.MapPool;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.util.WeakHashSet;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import schmitzm.geotools.io.GeoImportUtil;
import schmitzm.jfree.chart.style.ChartStyle;

/**
 * Stores {@link DpEntry}s by their IDs. Offers to add
 * {@link PropertyChangeListener}s that listen to changes in the
 * {@link DataPool}.
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 * 
 */
public class DataPool extends TreeMap<String, DpEntry<? extends ChartStyle>> {

	static final Logger LOGGER = Logger.getLogger(DataPool.class);

	private WeakHashSet<PropertyChangeListener> listeners = new WeakHashSet<PropertyChangeListener>(
			PropertyChangeListener.class);

	public enum EventTypes {
		addDpe, removeDpe, changeDpe, unknown
	};

	/**
	 * Convenience method to add a {@link DpEntry}
	 * 
	 * @param entry
	 */
	public void add(DpEntry<? extends ChartStyle> entry) {
		if (entry == null) {
			LOGGER.warn("A null has been added to the datapool!");
			return;
		}
		put(entry.getId(), entry);
		fireChangeEvents(EventTypes.addDpe);
	}

	/**
	 * You can ask for the String ID or for an Integer, which is then is
	 * interpreted as the position in the ordered list.
	 */
	@Override
	public DpEntry<? extends ChartStyle> get(Object key) {
		if (key instanceof Integer) {
			String[] orderedKeys = keySet().toArray(new String[size()]);
			return get(orderedKeys[(Integer) key]);

		}
		return super.get(key);
	}

	/**
	 * Overridden from {@link Map} to inform our {@link PropertyChangeListener}
	 */
	@Override
	public DpEntry<? extends ChartStyle> put(String key,
			DpEntry<? extends ChartStyle> value) {
		DpEntry<? extends ChartStyle> result = super.put(key, value);
		if (result == null)
			fireChangeEvents(EventTypes.addDpe);
		return result;
	}

	/**
	 * Overridden from {@link Map} to inform our {@link PropertyChangeListener}
	 */
	@Override
	public DpEntry<? extends ChartStyle> remove(Object key) {
		DpEntry<? extends ChartStyle> result = super.remove(key);

		if (result != null)
			fireChangeEvents(EventTypes.removeDpe);
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
	 * {@link PropertyChangeListener} can be unregistered.
	 * 
	 * @param propertyChangeListener
	 */
	public void removeChangeListener(
			PropertyChangeListener propertyChangeListener) {
		if (listeners.contains(propertyChangeListener)) {
			listeners.remove(propertyChangeListener);
		} else {
			LOGGER
					.warn("Removing a PropertyChangeListener that is not registered.");
		}
	}

	/**
	 * If {@link #quite} == <code>true</code> no {@link RuleChangedEvent} will
	 * be fired.
	 */
	private boolean quite = false;

	EventTypes lastOpressedEvent = null;
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
			// Not anymore.. if lastOpressedEvent == null, there is no reason to
			// send an event now
			// else
			// fireEvents(new RuleChangedEvent("Not quite anymore", this));
		} else {
			LOGGER.debug("not firing event because there are "
					+ stackQuites.size() + " 'quites' still on the stack");
		}

	}

	public void popQuite(EventTypes fireThis) {
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
	 * the {@link MapPool}. The events are fires on the EDT.
	 */
	public void fireChangeEvents(final EventTypes type) {

		if (quite) {
			lastOpressedEvent = type;
			return;
		} else {
			lastOpressedEvent = null;
		}

		PropertyChangeEvent pce = new PropertyChangeEvent(this,
				type.toString(), false, true);
		for (PropertyChangeListener pcl : listeners) {
			if (pcl != null)
				pcl.propertyChange(pce);
		}

	}

	public void dispose() {
		listeners.clear();
	}

	/**
	 * @return a list of all CRS used in the DataPool
	 */
	public Vector<CoordinateReferenceSystem> getCRSList() {

		Vector<CoordinateReferenceSystem> crss = new Vector<CoordinateReferenceSystem>();

		crss.add(DefaultGeographicCRS.WGS84);

		if (!crss.contains(GeoImportUtil.getDefaultCRS()))
			crss.add(GeoImportUtil.getDefaultCRS());

		for (DpEntry dpe : values()) {
			if (dpe instanceof DpLayer) {
				DpLayer dpl = (DpLayer) dpe;
				if (!crss.contains(dpl.getCrs()))
					crss.add(dpl.getCrs());
			}
		}

		return crss;
	}

}
