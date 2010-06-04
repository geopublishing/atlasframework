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
package org.geopublishing.atlasStyler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.util.WeakHashSet;

/**
 * Any styling or other cartographic pattern that can be expressed as (SLD)
 * styling {@link Rule}s is presented in AtlasStyler as a
 * {@link AbstractRuleList}
 * 
 * @author stefan
 * 
 */
public abstract class AbstractRuleList {

	final protected Logger LOGGER = ASUtil.createLogger(this);

	/**
	 * These enum names should not be changed anymore. We use them with
	 * .tostring().equals(...)
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Tzeggai</a>
	 */
	public enum RulesListType {
		SINGLE_SYMBOL_POINT, SINGLE_SYMBOL_POLYGON, SINGLE_SYMBOL_LINE, QUANTITIES_COLORIZED_POINT, QUANTITIES_COLORIZED_LINE, QUANTITIES_COLORIZED_POLYGON, QUANTITIES_SIZED_POINT, QUANTITIES_SIZED_LINE, UNIQUE_VALUE_POINT, UNIQUE_VALUE_LINE, UNIQUE_VALUE_POLYGON, UNIQUE_VALUE_COMBINATIONS_POINT, UNIQUE_VALUE_COMBINATIONS_LINE, UNIQUE_VALUE_COMBINATIONS_POLYGONE, TEXT_LABEL
	}

	// This is a WeakHashSet, so references to the listeners have to exist in
	// the classes adding the listeners. They shall not be anonymous instances.
	final WeakHashSet<RuleChangeListener> listeners = new WeakHashSet<RuleChangeListener>(
			RuleChangeListener.class);

	/**
	 * If {@link #quite} == <code>true</code> no {@link RuleChangedEvent} will
	 * be fired.
	 */
	private boolean quite = false;

	RuleChangedEvent lastOpressedEvent = null;
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
				fireEvents(lastOpressedEvent);
			// Not anymore.. if lastOpressedEvent == null, there is no reason to
			// send an event now
			// else
			// fireEvents(new RuleChangedEvent("Not quite anymore", this));
		} else {
			LOGGER.debug("not firing event because there are "
					+ stackQuites.size() + " 'quites' still on the stack");
		}
		
	}

	public void popQuite(RuleChangedEvent ruleChangedEvent) {
		setQuite(stackQuites.pop());
		if (quite == false)
			fireEvents(ruleChangedEvent);
		else {
			lastOpressedEvent = ruleChangedEvent;
			LOGGER.debug("not firing event " + ruleChangedEvent
					+ " because there are " + stackQuites.size()
					+ " 'quites' still on the stack");
		}
	}

	/**
	 * @return Returns the SLD {@link Rule}s that it represents.
	 */
	public abstract List<Rule> getRules();

	/**
	 * @return Returns the SLD {@link FeatureTypeStyle}s that represents this
	 *         RuleList. This method implemented here doesn't set the
	 *         FeatureTypeName. This is overridden in {@link FeatureRuleList}
	 */
	public FeatureTypeStyle getFTS() {
		List<Rule> rules = getRules();
		FeatureTypeStyle ftstyle = ASUtil.SB.createFeatureTypeStyle("Feature",
				rules.toArray(new Rule[] {}));
		ftstyle.setName(getAtlasMetaInfoForFTSName());
		return ftstyle;
	}

	/**
	 * Adds a {@link RuleChangeListener} which listens to changes in the
	 * {@link Rule}. Very good to update previews.<br>
	 * <b>The listening class must keep a reference to the listener (e.g. make it a
	 * field variable) because the listeners are kept in a WeakHashSet.</b>
	 * 
	 * @param listener
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Tzeggai</a>
	 */
	public void addListener(RuleChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes a {@link RuleChangeListener} which listens to changes in the
	 * {@link Rule}.
	 * 
	 * @param listener
	 *            {@link RuleChangeListener} to remove
	 * @return <code>false</code> if {@link RuleChangeListener} not found
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Tzeggai</a>
	 */
	public boolean removeListener(RuleChangeListener listener) {
		return listeners.remove(listener);
	}

	/**
	 * Clears all {@link RuleChangeListener}s
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Tzeggai</a>
	 */
	public void clearListeners() {
		listeners.clear();
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
	 * Returns direct access to the {@link RuleChangeListener}s {@link HashSet}
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Tzeggai</a>
	 */
	public Set<RuleChangeListener> getListeners() {
		return listeners;
	}

	/**
	 * Tells all {@link RuleChangeListener} that the {@link Rule}s represented
	 * by this {@link AbstractRuleList} implementation have changed.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Tzeggai</a>
	 */
	public void fireEvents(RuleChangedEvent rce) {

		if (quite) {
			lastOpressedEvent = rce;
			return;
		} else {
			lastOpressedEvent = null;
		}

//		if (listeners.size() > 0)
//			LOGGER.debug("Fire RCE event from "
//					+ rce.getSourceRL().getClass().getSimpleName() + " = "
//					+ rce.toString());

		for (RuleChangeListener l : listeners) {
			try {
				l.changed(rce);
			} catch (Exception e) {
				LOGGER.error("While fireEvents: "+rce,e);
			}
		}
	}

	/**
	 * When importing a {@link Style}, the {@link AtlasStyler} recognizes its
	 * RuleLists by reading meta information from the {@link FeatureTypeStyle}s
	 * name. That information starts with a basic identifier for the RuleList
	 * type.
	 * 
	 * @return An identifier string for that RuleList type.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Tzeggai</a>
	 */
	public abstract RulesListType getTypeID();

	/**
	 * The AtlasStyler stores meta information in the name tag of
	 * {@link FeatureTypeStyle}s.
	 * 
	 * @return a {@link String} that contains all information for this
	 *         particular RuleList
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Tzeggai</a>
	 */
	public abstract String getAtlasMetaInfoForFTSName();
		
}
