/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the Geopublisher application - An authoring tool to facilitate the publication and distribution of geoproducts in form of online and/or offline end-user GIS.
 * http://www.geopublishing.org
 * 
 * Geopublisher is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.creator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.geotools.data.DataUtilities;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;

import skrueger.atlas.AVUtil;
import skrueger.atlas.exceptions.AtlasException;

import com.lightdev.app.shtm.DocNameMissingException;
import com.lightdev.app.shtm.DocumentPane;

public class ACETranslationPrinterTest extends TestCase {

	@Test
	public void testPrint() throws DocNameMissingException, IOException, AtlasException, FactoryException, TransformException, SAXException, ParserConfigurationException {
		AtlasConfigEditable ace = TestingUtil.getAtlasConfigE();
		assertNotNull(ace);

		JFrame owner = null;

		/**
		 * Ask the user to select a save position
		 */

		File startWithDir = new File(System.getProperty("user.home"),
				"translations.html");
		JFileChooser dc = new JFileChooser(startWithDir);
		dc.setDialogType(JFileChooser.SAVE_DIALOG);
		dc.setDialogTitle(AtlasCreator
				.R("PrintTranslations.SaveHTMLDialog.Title"));
		dc.setSelectedFile(startWithDir);

		if (TestingUtil.INTERACTIVE) {
			if ((dc.showOpenDialog(owner) != JFileChooser.APPROVE_OPTION)
					|| (dc.getSelectedFile() == null))
				return;
		}

		File exportFile = dc.getSelectedFile();

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

		if (TestingUtil.INTERACTIVE)
			AVUtil.lauchHTMLviewer(null, exportFile.toURI());
	}
}
