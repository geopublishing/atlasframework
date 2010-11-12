package org.geopublishing.atlasStyler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;

import org.geotools.util.WeakHashSet;

public class RulesList extends ArrayList<AbstractRuleList> {

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
	public AbstractRuleList set(int index, AbstractRuleList element) {
		fireChangeListener();
		return super.set(index, element);
	}

	@Override
	public boolean add(AbstractRuleList e) {
		fireChangeListener();
		return super.add(e);
	}

	@Override
	public void add(int index, AbstractRuleList element) {
		fireChangeListener();
		super.add(index, element);
	}

	@Override
	public AbstractRuleList remove(int index) {
		fireChangeListener();
		return super.remove(index);
	}

	@Override
	public boolean remove(Object o) {
		fireChangeListener();
		return super.remove(o);
	}

	@Override
	public boolean addAll(Collection<? extends AbstractRuleList> c) {
		fireChangeListener();
		return super.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends AbstractRuleList> c) {
		fireChangeListener();
		return super.addAll(index, c);
	}

	@Override
	protected void removeRange(int fromIndex, int toIndex) {
		fireChangeListener();
		super.removeRange(fromIndex, toIndex);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		fireChangeListener();
		return super.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		fireChangeListener();
		return super.retainAll(c);
	}

}
