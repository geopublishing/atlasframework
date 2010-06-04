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
package org.geopublishing.geopublisher.dp;

import java.awt.Component;
import java.io.File;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.exceptions.AtlasImportException;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.gce.imagepyramid.ImagePyramidFormat;


/**
 * Tests if a given directory can be imported as an image pyramid to create a
 * {@link GridCoverage2D}
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 * 
 */
public class DpLayerRasterPyramidTest implements DpEntryTesterInterface {
	static private final Logger LOGGER = Logger
			.getLogger(DpLayerRasterPyramidTest.class);

	final static AbstractGridFormat format = new ImagePyramidFormat();

	public DpEntry create(AtlasConfigEditable ace, File file, Component owner) throws AtlasImportException {
		return new DpLayerRasterPyramidEd(ace, file, owner);
	}

	public boolean test(Component owner, File file) {

		return format.accepts(file);
	}
}
