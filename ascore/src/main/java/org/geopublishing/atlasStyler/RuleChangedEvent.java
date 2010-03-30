/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasStyler;

import org.apache.log4j.Logger;

/**
 * This event communicated the change of some {@link AbstractRuleList}.
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
 * 
 */
public class RuleChangedEvent {
	protected Logger LOGGER = ASUtil.createLogger(this);

	private final AbstractRuleList sourceRL;

	private final String string;

	//	
	// public RuleChangedEvent( final AbstractRuleList sourceRL ) {
	// this.sourceRL = sourceRL;
	// string = "-";
	// }

	public RuleChangedEvent(String string, final AbstractRuleList sourceRL) {
		this.string = string;
		this.sourceRL = sourceRL;
	}

	public AbstractRuleList getSourceRL() {
		return sourceRL;
	}

	@Override
	public String toString() {
		return string+" from RL: "+sourceRL.toString();
	}

}
