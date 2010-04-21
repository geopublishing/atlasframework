/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.xml.parsers.ParserConfigurationException;

import org.geopublishing.atlasViewer.exceptions.AtlasException;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geotools.data.DataUtilities;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;

import schmitzm.io.IOUtil;

import com.lightdev.app.shtm.DocNameMissingException;
import com.lightdev.app.shtm.DocumentPane;

public class ACETranslationPrinterTest  {

	@Test
	public void testPrint() throws DocNameMissingException, IOException, AtlasException, FactoryException, TransformException, SAXException, ParserConfigurationException {
		AtlasConfigEditable ace = GPTestingUtil.getAtlasConfigE();
		assertNotNull(ace);

		/**
		 * Ask the user to select a save position
		 */

		File exportFile = new File(IOUtil.getTempDir(),
			"translations.html");
		if (GPTestingUtil.INTERACTIVE) {
			JFileChooser dc = new JFileChooser(exportFile);
			dc.setDialogType(JFileChooser.SAVE_DIALOG);
			dc.setDialogTitle(GpUtil
					.R("PrintTranslations.SaveHTMLDialog.Title"));
			dc.setSelectedFile(exportFile);
			if ((dc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
					|| (dc.getSelectedFile() == null))
				return;
			exportFile = dc.getSelectedFile();
		}


		exportFile.delete();

		/**
		 * Create HTML output
		 */
		ACETranslationPrinter translationPrinter = new ACETranslationPrinter(
				ace);
		String allTrans = translationPrinter.printAllTranslations();

		/**
		 * Save it to file dirty
		 */
		BufferedWriter out = new BufferedWriter(new FileWriter(exportFile));
		out.write(allTrans);
		out.close();

		DocumentPane documentPane = new DocumentPane(
				DataUtilities.fileToURL(exportFile), 0);
		documentPane.saveDocument();
		documentPane = null;
		
		assertTrue(exportFile.exists());

		if (GPTestingUtil.INTERACTIVE)
			AVSwingUtil.lauchHTMLviewer(null, exportFile.toURI());
	}
}
