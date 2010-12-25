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

import org.apache.log4j.Logger;

import schmitzm.lang.LangUtil;

/**
 * This event communicated the change of some {@link AbstractRulesList}.
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 * 
 */
public class RuleChangedEvent {
	protected Logger LOGGER = LangUtil.createLogger(this);

	private final AbstractRulesList sourceRL;

	private final String reason;

	/**
	 * <code>null</code> or the new value where it makes sense (e.g. min Max
	 * Scale change)
	 */
	private Object newValue;

	/**
	 * <code>null</code> or the old value where it makes sense (e.g. min Max
	 * Scale change)
	 */
	private Object oldValue;

	public static final String RULE_CHANGE_EVENT_ENABLED_STRING = "Enabled or disabled all Rules in this RulesList";
	public static final String RULE_CHANGE_EVENT_FILTER_STRING = "Filter changed for this RulesList";
	public static final String RULE_CHANGE_EVENT_TITLE_STRING = "Title of rl has changed";
	public static final String RULE_CHANGE_EVENT_MINMAXSCALE_STRING = "min- or max-scale changed for this RulesList";
	public static final String RULE_PLACEMENT_CHANGED_STRING = "label placement changed";

	public RuleChangedEvent(String reason, final AbstractRulesList sourceRL) {
		this.reason = reason;
		this.sourceRL = sourceRL;
	}

	public AbstractRulesList getSourceRL() {
		return sourceRL;
	}

	@Override
	public String toString() {
		return "" + getReason() + " from RL: " + sourceRL.toString();
	}

	/**
	 * A constant STRING, @see RULE_CHANGE_EVENT_ENABLED_STRING or
	 * RULE_CHANGE_EVENT_FILTER_STRING
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * @return may return <code>null</code> or the old value where it makes
	 *         sense (e.g. min Max Scale change)
	 */
	public Object getOldValue() {
		return oldValue;
	}

	/**
	 * @return may return <code>null</code> or the new value where it makes
	 *         sense (e.g. min Max Scale change)
	 */
	public Object getNewValue() {
		return newValue;
	}

	public void setOldValue(Object oldValue) {
		this.oldValue = oldValue;
	}

	public void setNewValue(Object newValue) {
		this.newValue = newValue;
	}

}
