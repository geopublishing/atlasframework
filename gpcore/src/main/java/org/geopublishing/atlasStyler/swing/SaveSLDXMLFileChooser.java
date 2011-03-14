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
import javax.swing.filechooser.FileNameExtensionFilter;

import org.geopublishing.atlasStyler.AtlasStylerVector;


/**
 * This extension to a {@link JFileChooser} is ment to be used to save SLD/SE
 * files.
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 * 
 */
public class SaveSLDXMLFileChooser extends JFileChooser {

	public SaveSLDXMLFileChooser(File currentDirectory) {
		super(currentDirectory);

		addChoosableFileFilter(new FileNameExtensionFilter("SLD", new String[] {
				"sld", "xml" }));
		setDialogTitle(AtlasStylerVector.R("AtlasStylerGUI.saveStyledLayerDescFileDialogTitle")); 
		setDialogType(JFileChooser.SAVE_DIALOG);
	}

}
