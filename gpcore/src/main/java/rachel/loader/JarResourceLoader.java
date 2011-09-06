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
import java.net.URLConnection;
import java.util.jar.JarFile;

import rachel.ResourceLoader;
import rachel.util.FileUtils;

public class JarResourceLoader implements ResourceLoader {
	private ResourceLoader _delegate;

	public JarResourceLoader(URL url) throws IOException {
		URLConnection con = url.openConnection();

		InputStream in = con.getInputStream();

		File file = File.createTempFile("rachel-", null);
		FileUtils.saveStreamToFile(in, file);
		JarFile jarFile = new JarFile(file, false);
		// don't verify jar

		init(file, jarFile);
		
		file.deleteOnExit();
	}

	public JarResourceLoader(File file) throws IOException {
		JarFile jarFile = new JarFile(file, false);
		// don't verify jar
		init(file, jarFile);
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		return _delegate.getResourceAsStream(name);
	}

	@Override
	public URL getResourceAsUrl(String name) {
		return _delegate.getResourceAsUrl(name);
	}

	private void init(File file, JarFile jarFile) {
		_delegate = new JarResourceLoaderHelper(file, jarFile);
	}
}
