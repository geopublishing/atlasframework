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

package rachel.loader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;

import rachel.ResourceLoader;

public class JarResourceLoaderHelper implements ResourceLoader {
	static Logger log = Logger.getLogger(JarResourceLoaderHelper.class);

	private File _file;
	private JarFile _jarFile;

	public JarResourceLoaderHelper(File file, JarFile jarFile) {
		_file = file;
		_jarFile = jarFile;
	}

	public InputStream getResourceAsStream(String name) {
//		Status.debug("getResourceAsStream( name=" + name + " )");

		try {
			JarEntry entry = (JarEntry) _jarFile.getEntry(name);
			if (entry == null)
				return null;

			return _jarFile.getInputStream(entry);
		} catch (IOException ioex) {
			log.error("*** failed to retrieve resource '" + name
					+ "' inside jar: " + ioex.getMessage());
			return null;
		}
	}

	public URL getResourceAsUrl(String name) {
//		Status.debug("getResourceAsUrl( name=" + name + " )");

		try {
			// check if entry exists
			JarEntry entry = (JarEntry) _jarFile.getEntry(name);
			if (entry == null)
				return null;

			URL jarURL = new URL("jar:" + _file.toURI().toURL() + "!/" + name);

//			Status.debug("jarURL=" + jarURL.toExternalForm());

			return jarURL;
		} catch (IOException ioex) {
			log.error("*** failed to retrieve resource '" + name
					+ "' inside jar: " + ioex.getMessage());
			return null;
		}
	}
}
