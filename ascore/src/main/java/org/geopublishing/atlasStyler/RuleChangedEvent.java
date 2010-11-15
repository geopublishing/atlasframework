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

/**
 * This event communicated the change of some {@link AbstractRulesList}.
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 * 
 */
public class RuleChangedEvent {
	protected Logger LOGGER = ASUtil.createLogger(this);

	private final AbstractRulesList sourceRL;

	private final String reason;

	public static final String RULE_CHANGE_EVENT_ENABLED_STRING = "Enabled or disabled all Rules in this RuleList";
	public static final String RULE_CHANGE_EVENT_FILTER_STRING = "Filter changed for this RuleList";

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

}
