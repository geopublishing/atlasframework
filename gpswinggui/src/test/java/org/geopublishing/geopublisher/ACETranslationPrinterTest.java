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
package org.geopublishing.geopublisher;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;

import org.geopublishing.atlasViewer.exceptions.AtlasException;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geotools.data.DataUtilities;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;

import com.lightdev.app.shtm.DocNameMissingException;
import com.lightdev.app.shtm.DocumentPane;

import de.schmitzm.io.IOUtil;
import de.schmitzm.testing.TestingClass;
import de.schmitzm.testing.TestingUtil;
public class ACETranslationPrinterTest extends TestingClass {

	@Test
	public void testPrint() throws DocNameMissingException, IOException,
			AtlasException, FactoryException, TransformException, SAXException,
			ParserConfigurationException, InterruptedException, InvocationTargetException {
		AtlasConfigEditable ace = GpTestingUtil.getAtlasConfigE();
		assertNotNull(ace);

		/**HTMLInfoJWebBrowser
		 * Ask the user to select a save position
		 */

		final File exportFile = new File(IOUtil.getTempDir(), "translations.html");

		if (TestingUtil.hasGui()) {
			JFileChooser dc = new JFileChooser(exportFile);
			dc.setDialogType(JFileChooser.SAVE_DIALOG);
			dc.setDialogTitle(GpUtil
					.R("PrintTranslations.SaveHTMLDialog.Title"));
			dc.setSelectedFile(exportFile);
			// if ((dc.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
			// || (dc.getSelectedFile() == null))
			// return;
			File exportFile2 = dc.getSelectedFile();
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

		if (TestingUtil.hasGui()) {
			DocumentPane documentPane = new DocumentPane(
					DataUtilities.fileToURL(exportFile), 0);
			documentPane.saveDocument();
			documentPane = null;

			assertTrue(exportFile.exists());

			SwingUtilities.invokeAndWait(new Runnable() {
				
				@Override
				public void run() {
					AVSwingUtil.lauchHTMLviewer(null, exportFile.toURI());
				}
			});
		}
	}
}
