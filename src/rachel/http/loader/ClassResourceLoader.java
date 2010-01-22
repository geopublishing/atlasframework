/*
 ** Rachel  - Resource Loading Toolkit for Web Start/JNLP
 ** Copyright (c) 2001, 2002 by Gerald Bauer
 **
 ** This program is free software.
 **
 ** You may redistribute it and/or modify it under the terms of the GNU
 ** General Public License as published by the Free Software Foundation.
 ** Version 2 of the license should be included with this distribution in
 ** the file LICENSE, as well as License.html. If the license is not
 ** included with this distribution, you may find a copy at the FSF web
 ** site at 'www.gnu.org' or 'www.fsf.org', or you may write to the
 ** Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139 USA.
 **
 ** THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND,
 ** NOT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR
 ** OF THIS SOFTWARE, ASSUMES _NO_ RESPONSIBILITY FOR ANY
 ** CONSEQUENCE RESULTING FROM THE USE, MODIFICATION, OR
 ** REDISTRIBUTION OF THIS SOFTWARE.
 **
 */

package rachel.http.loader;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import rachel.http.resource.InputStreamResource;
import rachel.http.resource.WebResource;

public class ClassResourceLoader implements WebResourceLoader {
	static Logger LOGGER = Logger.getLogger(ClassResourceLoader.class);

	private ClassLoader _loader;

	public ClassResourceLoader(Class<?> clazz) {
		_loader = clazz.getClassLoader();
	}

	public WebResource getResource(String name) {
		try {
			// strip leading slash

			name = name.substring(1);
			// LOGGER.debug( "name=" + name );

			InputStream in = _loader.getResourceAsStream(name);
			if (in == null)
				// couldn't locate resource in jar
				return null;

			InputStreamResource res = new InputStreamResource(name, in);
			return res;
		} catch (IOException ioex) {
			LOGGER.error("*** failed to retrieve resource " + name + ": "
					+ ioex.toString());
			return null;
		}
	}
}
