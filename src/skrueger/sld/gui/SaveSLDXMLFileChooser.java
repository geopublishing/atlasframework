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
package skrueger.sld.gui;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import skrueger.sld.AtlasStyler;

/**
 * This extension to a {@link JFileChooser} is ment to be used to save SLD/SE
 * files.
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
 * 
 */
public class SaveSLDXMLFileChooser extends JFileChooser {

	public SaveSLDXMLFileChooser(File currentDirectory) {
		super(currentDirectory);

		addChoosableFileFilter(new FileNameExtensionFilter("SLD", new String[] {
				"sld", "xml" }));
		setDialogTitle(AtlasStyler.R("AtlasStylerGUI.saveStyledLayerDescFileDialogTitle")); 
		setDialogType(JFileChooser.SAVE_DIALOG);
	}

}
