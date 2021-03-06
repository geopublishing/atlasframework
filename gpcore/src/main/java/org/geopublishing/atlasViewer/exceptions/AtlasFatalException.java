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
package org.geopublishing.atlasViewer.exceptions;

public class AtlasFatalException extends AtlasException {

	public AtlasFatalException() {
		super();
	}

	public AtlasFatalException(String message, Throwable cause) {
		super(message, cause);
	}

	public AtlasFatalException(String message) {
		super(message);
	}

	public AtlasFatalException(Throwable cause) {
		super(cause);
	}

}
