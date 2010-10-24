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

package rachel.url;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;

public class ClassUrlConnection extends URLConnection {
	static Logger LOGGER = Logger.getLogger(ClassUrlConnection.class);

	private Class<?> _clazz;

	public ClassUrlConnection(URL url) {
		super(url);
	}

	@Override
	public String getContentType() {
		String path = this.url.getPath();
		// LOGGER.debug( "path=" + path );

		String contentType = URLConnection.guessContentTypeFromName(path);
		// LOGGER.debug( "contentType=" + contentType );

		return contentType;
	}

	@Override
	public synchronized InputStream getInputStream() throws IOException {
		// LOGGER.debug( "getInputStream()" );

		if (!connected)
			connect();

		String path = this.url.getPath();
		// strip leading slash if present
		if (path.startsWith("/"))
			path = path.substring(1);

		// LOGGER.debug( "path=" + path );
		InputStream in = _clazz.getClassLoader().getResourceAsStream(path);

		// LOGGER.debug( "in=" + ( in != null ) );

		return in;
	}

	@Override
	public synchronized void connect() throws IOException {
		// LOGGER.debug( "connect()" );

		// get class from url e.g class://org.olympus.gr.Aphrodite/bio.html
		String className = this.url.getHost();

		// LOGGER.debug( "className=" + className );

		try {
			_clazz = Class.forName(className);
			this.connected = true;
		} catch (ClassNotFoundException cex) {
			throw new IOException("*** class not found: " + cex.toString());
		}
	}
}
