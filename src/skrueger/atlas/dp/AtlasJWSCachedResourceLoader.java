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
package skrueger.atlas.dp;

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
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
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

	public InputStream getResourceAsStream(String name) {
		InputStream resourceAsStream = 
			Thread.currentThread()
				.getContextClassLoader().
				getResourceAsStream(name);
		LOGGER.debug("getResourceAsStream for name = "+name+" returned "+resourceAsStream);

		return resourceAsStream;
	}

	public URL getResourceAsUrl(String name) {
		URL resourceAsURL = Thread.currentThread()
				.getContextClassLoader().
				getResource(name);
		LOGGER.debug("getResourceAsURL for name = "+name+" retruned "+resourceAsURL);
		
		LOGGER.debug(resourceAsURL);
		return resourceAsURL;
	}
}
