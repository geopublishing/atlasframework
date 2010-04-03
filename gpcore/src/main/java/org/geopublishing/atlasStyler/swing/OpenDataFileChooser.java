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
package org.geopublishing.atlasStyler.swing;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * This extension of a {@link JFileChooser} is ment to be used to open one of
 * the Supported Geodata filestypes
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
 * 
 */
public class OpenDataFileChooser extends JFileChooser {

	public OpenDataFileChooser(File currentDirectory) {
		super(currentDirectory);

		addChoosableFileFilter(new FileNameExtensionFilter("GML", "gml"));
		addChoosableFileFilter(new FileNameExtensionFilter("Shape", "shp", "SHP", "zip", "ZIP")); // TODO Let them click on any part of a shapefile
		setDialogTitle("Open a file"); // i8n i8nAS
		setDialogType(OPEN_DIALOG);
	}

}