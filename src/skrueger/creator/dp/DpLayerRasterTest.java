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
package skrueger.creator.dp;

import java.awt.Component;
import java.io.File;

import org.apache.log4j.Logger;

import schmitzm.geotools.io.GeoImportUtil;
import schmitzm.geotools.io.GeoImportUtil.ARCASCII_POSTFIXES;
import schmitzm.geotools.io.GeoImportUtil.GEOTIFF_POSTFIXES;
import schmitzm.geotools.io.GeoImportUtil.IMAGE_POSTFIXES;
import schmitzm.swing.ExceptionDialog;
import skrueger.atlas.dp.layer.DpLayerRaster;
import skrueger.atlas.exceptions.AtlasImportException;
import skrueger.creator.AtlasConfigEditable;
import skrueger.geotools.io.GeoImportUtilURL;

/**
 * A tester class that can determine, if a file is importable as a
 * {@link DpLayerRaster}
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
 */
public class DpLayerRasterTest implements DpEntryTesterInterface {
	Logger log = Logger.getLogger(DpLayerRasterTest.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see skrueger.creator.DatapoolEntryTester#test(java.io.File)
	 */
	public boolean test(Component owner, File file) {
		String filename = file.getName().toLowerCase();

		// ****************************************************************************
		// Check if the ending suggests a GeoTIFF
		// ****************************************************************************
		for (GEOTIFF_POSTFIXES ending : GeoImportUtil.GEOTIFF_POSTFIXES
				.values()) {
			if (filename.endsWith(ending.toString())) {
				try {
					GeoImportUtil.readGridFromGeoTiff(file);
					return true;
				} catch (Exception e) {
					ExceptionDialog.show(owner, e);
					return false;
				}
			}
		}

		// ****************************************************************************
		// Check if the ending suggests an Arc/Info ASCII Grid
		// ****************************************************************************
		for (ARCASCII_POSTFIXES ending : GeoImportUtil.ARCASCII_POSTFIXES
				.values()) {
			if (filename.endsWith(ending.toString())) {
				try {
					GeoImportUtil.readGridFromArcInfoASCII(
							file.toURI().toURL(), null);
					return true;
				} catch (Exception e) {
					ExceptionDialog.show(owner, e);
					return false;
				}
			}
		}

		// ****************************************************************************
		// Check if the ending suggests an ordenary image with .wld file
		// ****************************************************************************
		for (IMAGE_POSTFIXES ending : GeoImportUtil.IMAGE_POSTFIXES.values()) {
			if (filename.endsWith(ending.toString())) {
				try {
					GeoImportUtilURL.readGridFromImage(file.toURI().toURL());
					return true;
				} catch (Exception e) {
					ExceptionDialog.show(owner, e);
					return false;
				}
			}
		}

		log.debug(" " + filename + " rejected because of unknown postfix.");
		return false;
	}

	public DpLayerRasterEd create(AtlasConfigEditable ac, File f,
			Component owner)
			throws AtlasImportException {
		return new DpLayerRasterEd(owner, ac, f);
	}

}
