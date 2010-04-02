/*
 * SimplyHTML, a word processor based on Java, HTML and CSS
 * Copyright (C) 2003 Ulrich Hilger
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package de.calcom.cclib.text;

import java.util.EventObject;

/**
 * This is simply overriding EventObject by storing a FindReplaceDialog into the
 * source field of the superclass.
 * 
 * <p>
 * Added i18n support for application SimplyHTML in version 1.5
 * </p>
 * 
 * @author Ulrich Hilger
 * @author CalCom
 * @author <a href="http://www.calcom.de">http://www.calcom.de</a>
 * @author <a href="mailto:info@calcom.de">info@calcom.de</a>
 * @version 1.5, April 27, 2003
 */

public class FindReplaceEvent extends EventObject {

	public FindReplaceEvent(FindReplaceDialog source) {
		super(source);
	}
}
