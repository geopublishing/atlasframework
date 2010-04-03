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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.exceptions.AtlasImportException;
import org.geopublishing.geopublisher.AtlasConfigEditable;


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
