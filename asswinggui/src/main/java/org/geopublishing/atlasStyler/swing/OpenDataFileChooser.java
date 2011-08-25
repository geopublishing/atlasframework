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
package org.geopublishing.atlasStyler.swing;

import java.io.File;

import javax.swing.JFileChooser;

import org.geopublishing.atlasStyler.ASUtil;

/**
 * This extension of a {@link JFileChooser} is ment to be used to open one of the Supported Geodata filestypes
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 * 
 */
public class OpenDataFileChooser extends JFileChooser {

	public OpenDataFileChooser(File currentDirectory) {
		super(currentDirectory);

		// TODO Let them click on any part of ashapefile

		addChoosableFileFilter(ASUtil.FILTER_GML);

		addChoosableFileFilter(ASUtil.FILTER_RASTERSUPPORTED);

		// TODO Let them
		// click on
		// any
		// part of a
		// shapefile
		addChoosableFileFilter(ASUtil.FILTER_SHAPE);

		addChoosableFileFilter(ASUtil.FILTER_ALLSUPPORted);

		setDialogType(OPEN_DIALOG);
	}
}
