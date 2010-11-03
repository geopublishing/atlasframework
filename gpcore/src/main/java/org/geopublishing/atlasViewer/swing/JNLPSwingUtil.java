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

import java.awt.GraphicsEnvironment;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.jnlp.DownloadService;
import javax.jnlp.DownloadServiceListener;
import javax.jnlp.UnavailableServiceException;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.JNLPUtil;
import org.geopublishing.atlasViewer.swing.internal.AtlasStatusDialog;

/**
 * A utility class with static methods that deal with JNLP / JavaWebStart
 * related stuff.
 * 
 * @author Stefan Alfons Tzeggai
 * 
 */
public class JNLPSwingUtil extends JNLPUtil {
	final static private Logger LOGGER = Logger.getLogger(JNLPSwingUtil.class);

	//
	// /**
	// * Does not manage any GUI feedback! Please run it from an {@link
	// AtlasTask}
	// * . Will do nothing if the part is already cached.
	// *
	// * @param statusDialog
	// */
	// public static void loadPart(String part,
	// DownloadServiceListener statusDialog) {
	//
	// LOGGER.debug("loadPart(String[] parts, AtlasStatusDialogInterface statusDialog) EDT:"
	// + SwingUtilities.isEventDispatchThread());
	//
	// DownloadService ds;
	// try {
	// ds = getJNLPDownloadService();
	//
	// if (ds.isPartCached(part)) {
	// LOGGER.debug("part " + part + " is JWS cached, returning");
	// return;
	// }
	// LOGGER.info("part " + part + " is NOT cached.. start DL ");
	//
	// // load the resource into the JWS Cache
	// ds.loadPart(part, statusDialog);
	// } catch (Exception e) {
	// LOGGER.error(e);
	// }
	// }

	public static DownloadServiceListener getJNLPDialog()
			throws UnavailableServiceException {

		if (!AtlasViewerGUI.isRunning()
				|| !SwingUtilities.isEventDispatchThread()) {
			return new AtlasStatusDialog(null);
		}

		return new AtlasStatusDialog(AtlasViewerGUI.getInstance().getJFrame());
	}

	/**
	 * Blocks and downloades the URL on this Thread. Uses the
	 * {@link DownloadServiceListener} for any feedback. The
	 * {@link DownloadServiceListener} can be a
	 * {@link NoGuiDownloadServiceListener} or an {@link AtlasStatusDialog}.
	 * Throws not {@link Exception}s, but logs them.
	 */
	public static void loadPart(String[] parts,
			DownloadServiceListener serviceListener) {

		LOGGER.debug("loadPart(String[] parts, DownloadServiceListener statusDialog) EDT:"
				+ SwingUtilities.isEventDispatchThread());

		DownloadService ds;
		try {
			ds = getJNLPDownloadService();

			if (ds.isPartCached(parts)) {
				LOGGER.info(parts.length + " parts are JWS cached, returning");
				return;
			}

			LOGGER.info("starting ds.loadParts with statusdialog "
					+ serviceListener + " on EDT ("
					+ SwingUtilities.isEventDispatchThread() + ") ");
			ds.loadPart(parts, serviceListener);

		} catch (Exception e1) {
			LOGGER.error(e1);
		}
	}

	/**
	 * Creates a {@link AtlasStatusDialog} (unless headless) and runs the
	 * download in an {@link AtlasSwingWorker}. The {@link SwingWorker} is
	 * started from the correct {@link Thread}.
	 */
	public static void loadPartAndCreateDialogForIt(final String... parts) {
		try {

			if (GraphicsEnvironment.isHeadless()) {
				loadPart(parts, new NoGuiDownloadServiceListener());
				return;
			}

			boolean edt = SwingUtilities.isEventDispatchThread();

			LOGGER.debug("loadPartAndCreateDialogForIt(String[] parts) EDT:"
					+ edt);

			final AtlasStatusDialog sd = new AtlasStatusDialog(null,
					"Downloading data", "Downloading data"); // i8n i8n i8n TODO
																// TODO
			final AtlasSwingWorker<Void> asw = new AtlasSwingWorker<Void>(sd) {

				@Override
				protected Void doInBackground() throws Exception {
					loadPart(parts, sd);
					return null;
				}
			};

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
