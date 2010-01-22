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
