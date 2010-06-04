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
package org.geopublishing.geopublisher.dp.media;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AVUtil;
import org.geopublishing.atlasViewer.AtlasStatusDialogInterface;
import org.geopublishing.atlasViewer.dp.media.DpMediaPDF;
import org.geopublishing.atlasViewer.exceptions.AtlasImportException;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.DpEditableInterface;
import org.geopublishing.geopublisher.GpUtil;
import org.geopublishing.geopublisher.dp.DpeImportUtil;
import org.geotools.data.DataUtilities;


/**
 * A subclass of {@link DpMediaPDF} that is bound to a {@link File} object, not
 * a {@link URL}
 */
public class DpMediaPDFEd extends DpMediaPDF implements DpEditableInterface {
	static private final Logger LOGGER = Logger.getLogger(DpMediaPDFEd.class);

	/**
	 * Constructor that automatically imports a given PDF file into the data
	 * directory.
	 * 
	 * @param ace
	 *            {@link AtlasConfigEditable}
	 * @param file
	 *            PDF {@link File}
	 * @param owner
	 * @param guiInteraction
	 * @throws IOException
	 */
	public DpMediaPDFEd(AtlasConfigEditable ace, File file, Component owner) throws AtlasImportException {
		super(ace);
		String cleanFilename = AVUtil.cleanFilename(file.getName());
		
		setFilename(cleanFilename);
		
		setId(GpUtil.getRandomID("pdf"));
		
		String dirname = getId() + "_"
		+ cleanFilename.substring(0, getFilename().lastIndexOf('.'));
		
		setId(dirname);
		
		// Create sub-directory to hold data, called the dataDir
		final File targetDir = new File(getAce().getDataDir(), dirname);
		
		setDataDirname(dirname);
		
		DpeImportUtil.copyFilesWithOrWithoutGUI(this, DataUtilities.fileToURL(file), owner, targetDir);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see skrueger.creator.dp.DatapoolEditableInterface#getAce()
	 */
	public AtlasConfigEditable getAce() {
		return (AtlasConfigEditable) getAtlasConfig();
	}

	@Override
	public void copyFiles(URL sourceUrl, Component owner, File targetDir,
			AtlasStatusDialogInterface atlasStatusDialog) throws Exception {
		
		AVUtil.copyUrl( sourceUrl, targetDir, true);
		
	}

}
