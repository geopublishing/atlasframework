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
import java.net.URL;

import org.apache.log4j.Logger;

import skrueger.atlas.AVUtil;
import skrueger.atlas.dp.media.DpMediaVideo;
import skrueger.atlas.exceptions.AtlasImportException;
import skrueger.atlas.gui.internal.AtlasStatusDialog;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.GpUtil;

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
		return (AtlasConfigEditable) getAc();
	}

	@Override
	public void copyFiles(URL sourceUrl, Component owner,
			File targetDir,
			AtlasStatusDialog atlasStatusDialog) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
