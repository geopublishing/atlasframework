package org.geopublishing.atlasStyler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;

import org.geotools.util.WeakHashSet;

public class RulesListsList extends ArrayList<AbstractRulesList> {

	private static final long serialVersionUID = 6528595862508704400L;
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

		fireChangeListener(pce);
	}

	private void fireChangeListener(PropertyChangeEvent pce) {
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

	/**
	 * If {@link #quite} == <code>true</code> no {@link RuleChangedEvent} will
	 * be fired.
	 */
	private boolean quite = false;
	Stack<Boolean> stackQuites = new Stack<Boolean>();

	/**
	 * If quite, the RuleList will not fire {@link RuleChangedEvent}s
	 */
	public boolean isQuite() {
		return quite;
	}

	/**
	 * If quite, the RuleList will not fire {@link RuleChangedEvent}s.
	 */
	private void setQuite(boolean b) {
		quite = b;
	}

	PropertyChangeEvent lastOpressedEvent = null;

	/**
	 * Remove a QUITE-State from the event firing state stack
	 */
	public void popQuite() {
		setQuite(stackQuites.pop());
		if (quite == false) {
			if (lastOpressedEvent != null)
				fireChangeListener(lastOpressedEvent);
		} else {
		}

	}

	public void popQuite(PropertyChangeEvent ruleChangedEvent) {
		setQuite(stackQuites.pop());
		if (quite == false)
			fireChangeListener(ruleChangedEvent);
		else {
			lastOpressedEvent = ruleChangedEvent;
		}
	}

	/**
	 * Add a QUITE-State to the event firing state stack
	 */
	public void pushQuite() {
		stackQuites.push(quite);
		setQuite(true);
	}

}
