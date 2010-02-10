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
package skrueger.atlas.exceptions;

public class AtlasUserinputException extends AtlasException {

	public AtlasUserinputException() {
		super();
	}

	public AtlasUserinputException(String message, Throwable cause) {
		super(message, cause);
	}

	public AtlasUserinputException(String message) {
		super(message);
	}

	public AtlasUserinputException(Throwable cause) {
		super(cause);
	}

}
