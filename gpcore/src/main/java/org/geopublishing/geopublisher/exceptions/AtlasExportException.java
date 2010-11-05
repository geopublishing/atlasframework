/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.exceptions;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.exceptions.AtlasException;
import org.geopublishing.geopublisher.GpUtil;

public class AtlasExportException extends AtlasException {

	final static private Logger LOGGER = Logger
			.getLogger(AtlasExportException.class);

	public AtlasExportException() {
		super(GpUtil.R("AtlasExportException.message"));
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
}
