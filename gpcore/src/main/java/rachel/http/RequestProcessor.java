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

package rachel.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import rachel.http.loader.WebResourceLoader;
import rachel.http.resource.ErrorResource;
import rachel.http.resource.WebResource;

public class RequestProcessor implements Runnable {
	static Logger Status = Logger.getLogger(RequestProcessor.class);

	private static List<Socket> _pool = new LinkedList<Socket>();

	private WebResourceLoader _loader;

	public RequestProcessor(WebResourceLoader loader) {
		_loader = loader;
	}

	public static void processRequest(Socket request) {
		synchronized (_pool) {
			_pool.add(_pool.size(), request);
			_pool.notifyAll();
		}
	}

	@Override
	public void run() {
		while (true) {
			Socket connection;
			synchronized (_pool) {
				while (_pool.isEmpty()) {
					try {
						_pool.wait();
					} catch (InterruptedException ex) {
					}
				}
				connection = _pool.remove(0);
			}

			try {
				OutputStream raw = new BufferedOutputStream(connection
						.getOutputStream());
				Writer out = new OutputStreamWriter(raw);

				Reader in = new InputStreamReader(new BufferedInputStream(
						connection.getInputStream()), "ASCII");

				StringBuffer requestLineBuf = new StringBuffer();
				int c;
				while (true) {
					c = in.read();
					if (c == '\r' || c == '\n')
						break;
					requestLineBuf.append((char) c);
				}

				String requestLine = requestLineBuf.toString();

				StringTokenizer st = new StringTokenizer(requestLine);
				String method = st.nextToken();

				WebResource resource = null;

				if (method.equals(Http.Method.GET)) {
					String resourceName = st.nextToken();

					resource = _loader.getResource(resourceName);
				} else {
					// sorry, we only understand GET

					resource = new ErrorResource(Http.Error.NOT_IMPLEMENTED_501);
				}

				out.write(resource.getHeader());
				out.flush();

				// send the file; it may be an image or other binary data
				// so use the underlying output stream instead of the writer
				raw.write(resource.getData());
				raw.flush();
			} catch (IOException ioex) {
				Status.error(ioex.toString());
			} finally {
				try {
					connection.close();
				} catch (IOException ioex) {
					Status.error(ioex.toString());
				}
			}
		}
		// end while
	}
	// end run()

}
