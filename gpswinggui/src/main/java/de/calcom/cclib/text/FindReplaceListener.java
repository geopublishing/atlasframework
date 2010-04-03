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

import java.util.EventListener;

/**
 * Interface for implementing a listener for <code>FindReplaceEvents</code>.
 * 
 * <p>
 * For implementing this interface, methods getFirstDocument and getNextDocument
 * need to be overridden by code providing a new document for the a
 * FindReplaceDialog. Once a new document is on hand, methods resumeOperation or
 * terminateOperation of the FindReplaceDialog need to be called from out of
 * this interfaces methods to resume or terminate the find or replace operation,
 * which is waiting for the new document in the FindReplaceDialog.
 * </p>
 * 
 * <p>
 * Added i18n support for application SimplyHTML in version 1.5
 * </p>
 * 
 * @author Ulrich Hilger
 * @author CalCom
 * @author <a href="http://www.calcom.de">http://www.calcom.de</a>
 * @author <a href="mailto:info@calcom.de">info@calcom.de</a>
 * 
 * @version 1.5, April 27, 2003
 */

public interface FindReplaceListener extends EventListener {

	/**
	 * this events gets fired, when a FindReplaceDialog has reached the end of
	 * the current document and requires the next document of a group of
	 * documents.
	 * 
	 * @param e
	 *            the object having details for the event
	 */
	public void getNextDocument(FindReplaceEvent e);

	/**
	 * this events gets fired, when a FindReplaceDialog has initiated an
	 * operation for a group of documents which requires to start at the first
	 * document.
	 * 
	 * @param e
	 *            the object having details for the event
	 */
	public void getFirstDocument(FindReplaceEvent e);

	/**
	 * this event gets fired when a FindReplaceDialog has finalized its task.
	 * 
	 * @param e
	 *            the object having details for the event
	 */
	public void findReplaceTerminated(FindReplaceEvent e);

}
