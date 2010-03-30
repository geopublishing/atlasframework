/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasViewer.http;

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
