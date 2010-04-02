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
package org.geopublishing.geopublisher.dp;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AVUtil;
import org.geopublishing.atlasViewer.AtlasStatusDialogInterface;
import org.geopublishing.atlasViewer.dp.media.DpMediaVideo;
import org.geopublishing.atlasViewer.exceptions.AtlasImportException;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.DpEditableInterface;
import org.geopublishing.geopublisher.GpUtil;


/**
 * A subclass of {@link DpMediaVideo} that is editable. It can be created from a
 * newly imported Video. It is bound to a {@link File} obejct, not only to a
 * {@link URL}
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
 * 
 */
public class DpMediaVideoEd extends DpMediaVideo implements DpEditableInterface {
	final static private Logger log = Logger.getLogger(DpMediaVideoEd.class);

	/**
	 * Constructor that automatically imports a given AVI file into the data
	 * directory.
	 * 
	 * @param ace
	 *            {@link AtlasConfigEditable}
	 * @param file
	 *            AVI file
	 * @throws IOException
	 */
	public DpMediaVideoEd(AtlasConfigEditable ace, File file)
			throws AtlasImportException {
		super(ace);
		try {
			copyAndImport(file);
		} catch (Exception e) {
			throw new AtlasImportException(e);
		}
	}

	/**
	 * Copy the video AVI file into the data directory
	 * 
	 * @param file
	 *            AVI {@link File}
	 * @throws IOException
	 */
	private void copyAndImport(File file) throws IOException {
		setFilename(file.getName());

		setId(GpUtil.getRandomID("video"));
		String dirname = getId() + "_"
				+ getFilename().substring(0, getFilename().lastIndexOf('.'));

		// setTitle(new Translation(getAc().getLanguages(), getFilename()));
		// setDesc(new Translation());

		// Create sub-directory to hold data, called the dataDir
		File dataDir = new File(getAce().getDataDir(), dirname);
		dataDir.mkdirs();
		if (!dataDir.exists())
			throw new IOException("Couldn't create "
					+ dataDir.getAbsolutePath());
		setDataDirname(dirname);

		// Copy file to data directory
		AVUtil.copyFile(log, file, dataDir);
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
	public void copyFiles(URL sourceUrl, Component owner,
			File targetDir,
			AtlasStatusDialogInterface atlasStatusDialog) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
