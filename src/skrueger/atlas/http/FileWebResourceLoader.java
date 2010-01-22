/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the AtlasViewer application - A GIS viewer application targeting at end-users with no GIS-experience. Its main purpose is to present the atlases created with the Geopublisher application.
 * http://www.geopublishing.org
 * 
 * AtlasViewer is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.atlas.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import rachel.http.loader.WebResourceLoader;
import rachel.http.resource.InputStreamResource;
import rachel.http.resource.WebResource;
import rachel.loader.FileResourceLoader;
import schmitzm.swing.ExceptionDialog;

/**
 * Allows to serve HTML directly from the filesystem
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
 * 
 */
public class FileWebResourceLoader extends FileResourceLoader implements
		WebResourceLoader {

	public FileWebResourceLoader(File root) {
		super(root);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rachel.http.loader.WebResourceLoader#getResource(java.lang.String)
	 */
	public final WebResource getResource(String name) {
		try {
			final InputStream resourceAsStream = getResourceAsStream(name);

			if (resourceAsStream == null)
				return null;
			return new InputStreamResource(name, resourceAsStream);
		} catch (IOException e) {
			ExceptionDialog.show(null, e);
			return null;
		}
	}
}
