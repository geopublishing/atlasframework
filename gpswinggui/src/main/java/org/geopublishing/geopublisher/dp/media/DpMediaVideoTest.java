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
package org.geopublishing.geopublisher.dp.media;

import java.awt.Component;
import java.io.File;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.media.DpMediaVideo;
import org.geopublishing.atlasViewer.exceptions.AtlasImportException;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.dp.DpEntryTesterInterface;
import org.geopublishing.geopublisher.dp.DpMediaVideoEd;


public class DpMediaVideoTest implements DpEntryTesterInterface {
	Logger log = Logger.getLogger(DpMediaVideoTest.class);

	@Override
	public final boolean test(Component owner, File f) {
		return false;
	}

	@Override
	public final DpEntry create(AtlasConfigEditable ace, File file,
			Component owner)
			throws AtlasImportException {
		final DpMediaVideo datapoolMediaVideo = new DpMediaVideoEd(ace, file);
		return datapoolMediaVideo;
	}
}
