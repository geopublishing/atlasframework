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
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.geotools.data.DataUtilities;

import schmitzm.geotools.io.GeoImportUtil;
import schmitzm.io.IOUtil;
import skrueger.atlas.AVUtil;
import skrueger.atlas.AtlasConfig;
import skrueger.atlas.dp.layer.DpLayerRasterPyramid;
import skrueger.atlas.exceptions.AtlasImportException;
import skrueger.atlas.gui.internal.AtlasStatusDialog;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.GpUtil;

public class DpLayerRasterPyramidEd extends DpLayerRasterPyramid implements
		DpEditableInterface {

	public DpLayerRasterPyramidEd(AtlasConfig ac, File file, Component owner)
			throws AtlasImportException {
		super(ac);

		copyAndImport(file, owner);
	}

	/**
	 * Copy the directory with the image pyramid into the ad directory. Basic
	 * parameters like id, name are set..
	 * 
	 * @param file
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws AtlasImportException
	 */
	private void copyAndImport(File file, Component owner)
			throws AtlasImportException {
		// The file that has been selected for import will be the
		// "filename".. IF the user accepts any changes to clean the name!
		// Otherwise an AtlasImportException is thrown
		final String name = GpUtil.cleanFilenameWithUI(owner, file.getName());

		setFilename(name);

		setId(GpUtil.getRandomID("pyr"));

		// Set a directory
		String dirname = getId() + "_"
				+ getFilename().substring(0, getFilename().lastIndexOf('.'));

		setDataDirname(dirname);

		File dataDir = new File(getAce().getDataDir(), dirname);

		DpeImportUtil.copyFilesWithOrWithoutGUI(this, file, owner, dataDir);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see skrueger.creator.dp.DatapoolEditable#getAce()
	 */
	public AtlasConfigEditable getAce() {
		return (AtlasConfigEditable) getAc();
	}

	static private final Logger LOGGER = Logger
			.getLogger(DpLayerRasterPyramidEd.class);

	@Override
	public void copyFiles(URL sourceUrl, Component owner, File targetDir,
			AtlasStatusDialog atlasStatusDialog) throws Exception {

		// Copy TIFF file to data directory
		AVUtil.copyFile(null, DataUtilities.urlToFile(sourceUrl)
				.getParentFile(), targetDir, true);

		// Reading the CRS now!
		crs = GeoImportUtil.readProjectionFile(IOUtil.changeUrlExt(sourceUrl,
				"prj"));
	}

}
