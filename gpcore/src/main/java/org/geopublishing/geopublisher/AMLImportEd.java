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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasCancelException;
import org.geopublishing.atlasViewer.dp.AMLImport;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;
import org.geopublishing.atlasViewer.exceptions.AtlasException;
import org.geopublishing.atlasViewer.exceptions.AtlasImportException;
import org.geopublishing.atlasViewer.http.Webserver;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.swing.AvUtil;
import org.jfree.util.Log;

import de.schmitzm.io.IOUtil;
import de.schmitzm.swing.swingworker.AtlasStatusDialogInterface;

public class AMLImportEd extends AMLImport {
	final static private Logger LOGGER = Logger.getLogger(AMLImportEd.class);

	/**
	 * Creates a {@link AtlasConfigEditable} from AtlasML XML code <br>
	 * The sequence of the elements is strictly defined by the XSD <br>
	 * Also sets up the {@link Webserver} to serve from the filesystem: [atlasDir/ad/html]
	 * 
	 * <br>
	 * This should only be called by the Geopublisher!
	 * 
	 * @param atlasDir
	 *            The atlas directory always has a folder ad and ad/atlas.xml must exists!
	 * @param aceLoader
	 *            A {@link SwingWorker} who's publish method is called with some info. May be null.
	 * @throws AtlasCancelException
	 */
	@Override
	public AtlasConfigEditable parseAtlasConfig(AtlasStatusDialogInterface statusDialog, File atlasDir)
			throws AtlasException {

		if (atlasDir.getName().endsWith(".gpa"))
			atlasDir = atlasDir.getParentFile();

		LOGGER.info("Opening Atlas from Folder " + atlasDir);
		//
		// // Added
		// AtlasViewerGUI.setupResLoMan(new String[] { atlasDir.getAbsolutePath() });

		// Create virgin AtlasConfigEditable
		AtlasConfigEditable ace = new AtlasConfigEditable(atlasDir);

		try {
			parseAtlasConfig(statusDialog, ace, true);

			if (upgradeFromPreGP17) {
				correctHtmlFiles(ace);
			}

		} catch (IOException e) {
			ace = null;
			throw new AtlasImportException(e);
		}

		return ace;
	}

	/**
	 * Checks all HTML files for color error created with GP versions <=1.6
	 */
	private void correctHtmlFiles(AtlasConfigEditable ace) {

		int countFixes = 0;

		countFixes += correctHtmlFiles(ace.getAboutHtMLFiles(null).toArray(new File[0]));
		countFixes += correctHtmlFiles(ace.getPopupHtMLFiles(null).toArray(new File[0]));

		// Iterate over all DPLayers with info files
		for (DpLayer dpe : ace.getDataPool().getLayers()) {
			for (String lang : ace.getLanguages()) {
				final URL infoURL = dpe.getInfoURL(lang);
				if (infoURL == null)
					continue;
				final File f = IOUtil.urlToFile(infoURL);
				if (f == null)
					continue;
				try {
					countFixes += AvUtil.fixBrokenBgColor(f) ? 1 : 0;
					;
				} catch (IOException e) {
					LOGGER.warn("Could not fix/upgrade html file " + f + " to correct color hex", e);
				}
			}
		}

		// Iterate over all maps with info files
		for (Map m : ace.getMapPool().values()) {
			for (String lang : ace.getLanguages()) {
				File htmlDirFor = ace.getHtmlDirFor(m);
				File[] listFiles = htmlDirFor.listFiles(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String name) {
						return name.toLowerCase().endsWith("html") || name.toLowerCase().endsWith("htm");
					}
				});
				countFixes += correctHtmlFiles(listFiles);
			}
		}

		Log.info("Upgrade " + countFixes + " old HTML files to version 1.7");

	}

	private int correctHtmlFiles(File... aboutHtMLFiles) {
		int countF = 0;
		for (File f : aboutHtMLFiles) {
			if (f != null)
				try {
					countF += AvUtil.fixBrokenBgColor(f) ? 1 : 0;
				} catch (IOException e) {
					LOGGER.warn("Could not fix/upgrade html file " + f + " to correct color hex", e);
				}
		}
		return countF;
	}

}
