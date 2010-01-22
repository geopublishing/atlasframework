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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.exceptions.AtlasImportException;
import skrueger.creator.AtlasConfigEditable;

/**
 * Interface to test is a file can be opened by an extension of the
 * {@link DpEntry} class
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
 * 
 */
public interface DpEntryTesterInterface {

	/**
	 * Tests if this file can be handled by a {@link DpEntry}-subtype. This
	 * function works on ugly filenames. Correcting ugly filenames is part of
	 * the import.
	 * 
	 * @param file
	 *            File to test
	 * @return true if a {@link DpEntry} can be created from this file
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public boolean test(Component owner, File file);

	/**
	 * Creates an "editable" object from a file. This function also corrects
	 * evil filenames.
	 * 
	 * @param ac
	 *            {@link AtlasConfigEditable} to create the {@link DpEntry} in
	 * @param file
	 *            For example: If the cities.shp is opened, other files like
	 *            cities.shx etc are expected automatically
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 * @throws URISyntaxException
	 * @throws Exception
	 */
	public DpEntry create(AtlasConfigEditable ace, File file, Component owner) throws AtlasImportException;
}
