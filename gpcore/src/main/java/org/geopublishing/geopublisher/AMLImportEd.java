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
import java.io.IOException;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasCancelException;
import org.geopublishing.atlasViewer.AtlasStatusDialogInterface;
import org.geopublishing.atlasViewer.dp.AMLImport;
import org.geopublishing.atlasViewer.exceptions.AtlasException;
import org.geopublishing.atlasViewer.exceptions.AtlasImportException;
import org.geopublishing.atlasViewer.http.Webserver;

public class AMLImportEd extends AMLImport {
	final static private Logger LOGGER = Logger.getLogger(AMLImportEd.class);

	/**
	 * Creates a {@link AtlasConfigEditable} from AtlasML XML code <br>
	 * The sequence of the elements is strictly defined by the XSD <br>
	 * Also sets up the {@link Webserver} to serve from the filesystem:
	 * [atlasDir/ad/html]
	 * 
	 * <br>
	 * This should only be called by the AtlasCreator!
	 * 
	 * @param atlasDir
	 *            The atlas directory always has a folder ad and ad/atlas.xml
	 *            must exists!
	 * @param aceLoader
	 *            A {@link SwingWorker} who's publish method is called with some
	 *            info. May be null.
	 * @throws AtlasCancelException 
	 */
	@Override
	public AtlasConfigEditable parseAtlasConfig(
			AtlasStatusDialogInterface statusDialog, File atlasDir) throws AtlasException 
			{
		
		if (atlasDir.getName().endsWith(".gpa")) atlasDir  = atlasDir.getParentFile();
		
		LOGGER.info("Opening Atlas from Folder " + atlasDir );

		// Create virgin AtlasConfigEditable
		AtlasConfigEditable ace = new AtlasConfigEditable(atlasDir);
//		// Add the file resource loaders
//		ace.setAtlasDir(atlasDir);

		try {
			parseAtlasConfig(statusDialog,  ace, true);
		} catch (IOException e) {
			ace = null;
			throw new AtlasImportException(e); 
		}
		
		return ace;
	}


}
