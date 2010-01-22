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
package skrueger.creator.dp.media;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.geotools.data.DataUtilities;

import skrueger.atlas.AVUtil;
import skrueger.atlas.dp.media.DpMediaPDF;
import skrueger.atlas.exceptions.AtlasImportException;
import skrueger.atlas.gui.internal.AtlasStatusDialog;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.GpUtil;
import skrueger.creator.dp.DpEditableInterface;
import skrueger.creator.dp.DpeImportUtil;

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
		return (AtlasConfigEditable) getAc();
	}

	@Override
	public void copyFiles(URL sourceUrl, Component owner, File targetDir,
			AtlasStatusDialog atlasStatusDialog) throws Exception {
		
		AVUtil.copyUrl( sourceUrl, targetDir, true);
		
	}

}
