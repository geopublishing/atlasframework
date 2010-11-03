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
package org.geopublishing.atlasViewer.swing;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.jnlp.DownloadService;
import javax.jnlp.DownloadServiceListener;
import javax.jnlp.UnavailableServiceException;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasStatusDialogInterface;
import org.geopublishing.atlasViewer.JNLPUtil;
import org.geopublishing.atlasViewer.swing.internal.AtlasStatusDialog;
import org.geopublishing.atlasViewer.swing.internal.AtlasTask;

/**
 * A utility class with static methods that deal with JNLP / JavaWebStart
 * related stuff.
 * 
 * @author Stefan Alfons Tzeggai
 * 
 */
public class JNLPSwingUtil extends JNLPUtil {
	final static private Logger LOGGER = Logger.getLogger(JNLPSwingUtil.class);

	/**
	 * Does not manage any GUI feedback! Please run it from an {@link AtlasTask}
	 * . Will do nothing if the part is already cached.
	 * 
	 * @param statusDialog
	 */
	public static void loadPart(String part,
			AtlasStatusDialogInterface statusDialog) throws IOException {

		LOGGER.debug("loadPart(String[] parts, AtlasStatusDialogInterface statusDialog) EDT:"
				+ SwingUtilities.isEventDispatchThread());

		DownloadService ds;
		try {
			ds = getJNLPDownloadService();

			if (ds.isPartCached(part)) {
				LOGGER.debug("part " + part + " is JWS cached, returning");
				return;
			}
			LOGGER.info("part " + part + " is NOT cached.. start DL ");

			// load the resource into the JWS Cache
			ds.loadPart(part, statusDialog);
		} catch (UnavailableServiceException e1) {
			throw new IOException(e1);
		}
	}

	public static DownloadServiceListener getJNLPDialog()
			throws UnavailableServiceException {

		if (!AtlasViewerGUI.isRunning()
				|| !SwingUtilities.isEventDispatchThread()) {
			return new AtlasStatusDialog(null);
		}

		return new AtlasStatusDialog(AtlasViewerGUI.getInstance().getJFrame());
	}

	public static void loadPart(String[] parts,
			AtlasStatusDialogInterface statusDialog) throws IOException {

		LOGGER.debug("loadPart(String[] parts, AtlasStatusDialogInterface statusDialog) EDT:"
				+ SwingUtilities.isEventDispatchThread());

		DownloadService ds;
		try {
			ds = getJNLPDownloadService();

			if (ds.isPartCached(parts)) {
				LOGGER.info(parts.length + " parts are JWS cached, returning");
				return;
			}

			LOGGER.info("starting ds.loadParts with statusdialog "
					+ statusDialog + " on EDT ("
					+ SwingUtilities.isEventDispatchThread() + ") ");
			ds.loadPart(parts, statusDialog);

		} catch (UnavailableServiceException e1) {
			LOGGER.error("", e1);
			throw new IOException("", e1);
		}
	}

	// public static void loadPart(String part, Component owner)
	// throws IOException {
	//
	// LOGGER.info("loadPart(String part, Component owner) EDT "
	// + SwingUtilities.isEventDispatchThread() + " id =" + part
	// + " owner " + owner);
	//
	// DownloadService ds;
	// try {
	// ds = getJNLPDownloadService();
	//
	// if (ds.isPartCached(part)) {
	// LOGGER.info("part " + part + " is JWS cached");
	// } else {
	//
	// LOGGER.info("part " + part
	// + " is NOT cached.. starting download... ");
	//
	// // load the resource into the JWS Cache
	// ds.loadPart(part, new AtlasStatusDialog(owner));
	// }
	// } catch (UnavailableServiceException e1) {
	// throw new IOException(e1);
	// }
	// }

	// public static void loadPart(String part) throws IOException {
	//
	// LOGGER.debug("loadPart(String part " + part + ") EDT:"
	// + SwingUtilities.isEventDispatchThread());
	//
	// // TODO && SwingUtilities.isEventDispatchThread() removen!
	// loadPart(new String[] { part } );
	// }

	public static void loadPartAndCreateDialogForIt(final String... parts) {
		boolean edt = SwingUtilities.isEventDispatchThread();

		LOGGER.debug("loadPartAndCreateDialogForIt(String[] parts) EDT:" + edt);

		final AtlasStatusDialog sd = new AtlasStatusDialog(null,
				"Downloading data", "Downloading data"); // i8n i8n i8n TODO TODO 
		final AtlasSwingWorker<Void> asw = new AtlasSwingWorker<Void>(sd) {

			@Override
			protected Void doInBackground() throws Exception {
				loadPart(parts, sd);
				return null;
			}
		};

		try {

			if (edt) {
				LOGGER.debug("  starting a AtlasSwingWorker (null) to download on edt");
				asw.executeModal();
			}

			else {
				LOGGER.debug("  starting a AtlasSwingWorker (null) to download not on edt, via Invoke AndWait");

				SwingUtilities.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						try {
							asw.executeModal();
						} catch (CancellationException e) {
							LOGGER.error(e);
						} catch (InterruptedException e) {
							LOGGER.error(e);
						} catch (ExecutionException e) {
							LOGGER.error(e);
						}
					}
				});

			}

		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

}
