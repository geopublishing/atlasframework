/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasViewer.exceptions;

import org.apache.log4j.Logger;

/**
 * Exceptions of type {@link AtlasRecoverableException} are not fatal to the
 * software. Stacktrace is not shown by default.
 * 
 * @author Stefan Alfons Tzeggai
 */
public class AtlasRecoverableException extends AtlasException {
	Logger log = Logger.getLogger(AtlasRecoverableException.class);

	public AtlasRecoverableException() {
	}

	public AtlasRecoverableException(String message, Throwable cause) {
		super(message, cause);
	}

	public AtlasRecoverableException(String message) {
		super(message);
	}

	public AtlasRecoverableException(Throwable cause) {
		super(cause);
	}
}
