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
package org.geopublishing.atlasViewer.dp.media;

import java.awt.Component;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.dp.DpEntryType;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;


public class DpMediaPDF extends DpMedia {
	static private final Logger LOGGER = Logger.getLogger(DpMediaPDF.class);

	public DpMediaPDF(AtlasConfig ac) {
		super(ac);
		setType(DpEntryType.PDF	);
	}
	
	/**
	 * Tries to open a PDF viewer of the host system.
	 */
	@Override
	public Object show(Component owner) {
		Exception error = AVSwingUtil.launchPDFViewer(owner, AVSwingUtil.getUrl(this, owner), getTitle().toString());
		
		if (error != null) {
			setBrokenException(error);
		}

		return error;
	}

	@Override
	public void exportWithGUI(Component owner) throws IOException {
		LOGGER.info("not implemented"); // TODO
	}
}
