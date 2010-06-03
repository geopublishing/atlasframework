/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.geopublishing.atlasViewer.http;

import java.io.IOException;
import java.net.BindException;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AVUtil;
import org.geopublishing.atlasViewer.exceptions.AtlasFatalException;

import rachel.http.WebServer;
import rachel.http.loader.WebResourceManager;
import rachel.util.NetUtils;

/**
 * A Wrapper that starts the rachel WebServer to provide http access to html
 * pages which are stored in JARs.
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 */
public class Webserver {
	static private final Logger log = Logger.getLogger(Webserver.class);

	public static int PORT = 7272;

	public static int DEFAULTPORT = 7272;

	private static WebServer webserver = null;

	/** Under special conditions, this class doesn't start a new webserver. * */
	boolean startupWebserver = true;

	/**
	 * Webserver
	 * 
	 * @param interactive
	 *            If false, then the user will not be asked anything. If true
	 *            and the port is occupied, the user might be asked if he wanto
	 *            to start the server on another port.
	 * @throws AtlasFatalException
	 * @throws IOException
	 */
	public Webserver(boolean interactive) throws AtlasFatalException {
		interactive=true;

		/**
		 * Replace PORT selection with a random Number iteration
		 */

		if (webserver == null) {
			
			try {
			log.info("Starting internal webserver");
			try {
				webserver = new WebServer(PORT, WebResourceManager
						.getInstance());
			} catch (IOException e) {
				try {
					PORT = 8282;
					webserver = new WebServer(PORT, WebResourceManager
							.getInstance());
				} catch (BindException e1) {
					try {
						PORT = (AVUtil.RANDOM.nextInt(200)) + 9000;
						webserver = new WebServer(PORT, WebResourceManager
								.getInstance());
					} catch (BindException e2) {

						final String msg = "The internal webserver can't be started. All network ports are in use.\n"
								+ // i8n
								"You probably have too many instances of the atlas program open. Close\n"
								+ "all other atlas programs and retry.\n"
								+ "If you continue, this atlas-software might provide inconsistant HTML information.\n\n"
								+ "Do you wan't to continue?";
						log.error(msg);

						int result;
						if (interactive) {
							result = JOptionPane.showConfirmDialog(null, msg,
									"HTML support inconsistent!",
									JOptionPane.OK_OPTION);

							if (result != JOptionPane.YES_OPTION) {
								log
										.info(
												"Application stopped by user after BindException:",
												e2);
								System.exit(-1);
							}
						}
						startupWebserver = false;
						PORT = DEFAULTPORT;
					}
				}
			}

			if (startupWebserver) {
				webserver.start();
			} else {
				log
						.info("The constructor of Webserver has been started, but we decided not to start the webserver.");
			}
		} catch (Throwable e) {
			throw new AtlasFatalException("Webserver startup: ",e);
		}
		}
	}

	/**
	 * Shuts down the WebServer. TODO This is supposed to be not threadsafe!
	 */
	public static void dispose() {
//		if (webserver != null) {
//			log
//					.warn("Thread.stop() is supposed to be NOT threadsafe. Disposing the WebServer anyway.");
//			webserver.shutdown = true;
//			try {
//				Thread.sleep(500);
//			} catch (InterruptedException e) {
//				log.error("While waiting for the webserver to shutdown:",e);
//			}
//			if (webserver.isAlive()){
//				log.error("WebServer didn't stop. Doing webserver.stop() now");
//				webserver.stop();
//			}
//			webserver = null;
//		}
	}

	/**
	 * Returns the document base for the internal {@link WebServer}
	 * 
	 * Is very slow
	 * 
	 * @return somthing like http://localhost:4545
	 */
	public static String getDocumentBase() {
		return "http://" + NetUtils.getLocalHostName() + ":" + Webserver.PORT;
	}

	public static boolean isRunning() {
		if (webserver == null)
			return false;
		return webserver.isAlive();
	}

}
