/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasViewer.dp;

import java.io.InputStream;
import java.net.URL;

import org.apache.log4j.Logger;

import rachel.ResourceLoader;

/**
 * Using rachel as a ressource manager, this is a {@link ResourceLoader} that
 * finds the Java Web Start stuff.
 * <p>
 * It runs as a singleton pattern
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 * 
 */
public class AtlasJWSCachedResourceLoader implements ResourceLoader {

	Logger LOGGER = Logger.getLogger(AtlasJWSCachedResourceLoader.class);

	private static AtlasJWSCachedResourceLoader instance;

	private AtlasJWSCachedResourceLoader() {
	}

	public static AtlasJWSCachedResourceLoader getInstance() {
		if (instance == null) {
			instance = new AtlasJWSCachedResourceLoader();
		}
		return instance;
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		InputStream resourceAsStream = 
			Thread.currentThread()
				.getContextClassLoader().
				getResourceAsStream(name);
		LOGGER.debug("getResourceAsStream for name = "+name+" returned "+resourceAsStream);

		return resourceAsStream;
	}

	@Override
	public URL getResourceAsUrl(String name) {
		URL resourceAsURL = Thread.currentThread()
				.getContextClassLoader().
				getResource(name);
		LOGGER.debug("getResourceAsURL for name = "+name+" returned "+resourceAsURL);
		
		LOGGER.debug(resourceAsURL);
		return resourceAsURL;
	}
}
