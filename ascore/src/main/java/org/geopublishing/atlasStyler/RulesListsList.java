package org.geopublishing.atlasStyler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;

import org.geotools.util.WeakHashSet;

public class RulesListsList extends ArrayList<AbstractRulesList> {

	WeakHashSet<PropertyChangeListener> listeners = new WeakHashSet<PropertyChangeListener>(
			PropertyChangeListener.class);

	public void addListener(PropertyChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * Inform the listeners of a change in the list of rules
	 */
	void fireChangeListener() {
		PropertyChangeEvent pce = new PropertyChangeEvent(this, "change",
				false, true);

		for (PropertyChangeListener pcl : listeners) {
			if (pcl != null)
				pcl.propertyChange(pce);
		}
	}

	@Override
	public AbstractRulesList set(int index, AbstractRulesList element) {
		AbstractRulesList r = super.set(index, element);
		fireChangeListener();
		return r;
	}

	@Override
	public boolean add(AbstractRulesList e) {
		boolean r = super.add(e);
		fireChangeListener();
		return r;
	}

	@Override
	public void add(int index, AbstractRulesList element) {
		super.add(index, element);
		fireChangeListener();
	}

	@Override
	public AbstractRulesList remove(int index) {
		AbstractRulesList r = super.remove(index);
		fireChangeListener();
		return r;
	}

	@Override
	public boolean remove(Object o) {
		boolean r = super.remove(o);
		fireChangeListener();
		return r;
	}

	@Override
	public boolean addAll(Collection<? extends AbstractRulesList> c) {
		boolean addAll = super.addAll(c);
		fireChangeListener();
		return addAll;
	}

	@Override
	public boolean addAll(int index, Collection<? extends AbstractRulesList> c) {
		boolean addAll = super.addAll(index, c);
		fireChangeListener();
		return addAll;
	}

	@Override
	protected void removeRange(int fromIndex, int toIndex) {
		super.removeRange(fromIndex, toIndex);
		fireChangeListener();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean r = super.removeAll(c);
		fireChangeListener();
		return r;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean r = super.retainAll(c);
		fireChangeListener();
		return r;
	}

}
