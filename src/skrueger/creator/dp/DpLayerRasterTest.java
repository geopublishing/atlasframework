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
