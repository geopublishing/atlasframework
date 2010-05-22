/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.exceptions;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.exceptions.AtlasException;


public class AtlasExportException extends AtlasException {
	public AtlasExportException() {
		super("Exception while exporting the atlas."); // i8n
	}

	public AtlasExportException(String message, Throwable cause) {
		super(message, cause);
	}

	public AtlasExportException(String message) {
		super(message);
	}

	public AtlasExportException(Throwable cause) {
		super(cause);
	}

	final static private Logger LOGGER = Logger
			.getLogger(AtlasExportException.class);
}
