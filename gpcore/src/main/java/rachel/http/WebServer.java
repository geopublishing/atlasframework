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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import rachel.http.loader.WebResourceLoader;

public class WebServer extends Thread {
	static Logger Status = Logger.getLogger(WebServer.class);
	private WebResourceLoader _loader;
	private int _numThreads = 10;

	private ServerSocket _server;
	public volatile transient static Boolean shutdown = false;

	public WebServer(int port, WebResourceLoader loader) throws IOException {
		_loader = loader;
		_server = new ServerSocket(port);

		setPriority(4);
		setDaemon(true);
	}

	@Override
	public void run() {
		ThreadGroup tg = new ThreadGroup("http");

		for (int i = 0; i < _numThreads; i++) {
			Thread t = new Thread(tg, new RequestProcessor(_loader), "http-"
					+ (i + 1));

			t.setPriority(4);
			t.setDaemon(true);
			t.start();
		}

		Status.debug("accepting connections on port " + _server.getLocalPort());

		while (!shutdown) {
//			synchronized (shutdown) {
				try {
					Socket request = _server.accept();
					RequestProcessor.processRequest(request);
				} catch (IOException ioex) {
					Status.error(ioex.toString());
				}
//			}
		}
	}

}
