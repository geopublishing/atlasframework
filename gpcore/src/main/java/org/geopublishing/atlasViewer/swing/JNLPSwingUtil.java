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
// TODO move to schmitzm
public class JNLPSwingUtil extends JNLPUtil {
	final static private Logger LOGGER = Logger.getLogger(JNLPSwingUtil.class);

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

		boolean oldCancelState = false;
		if (serviceListener instanceof AtlasStatusDialog) {
			oldCancelState = ((AtlasStatusDialog) serviceListener)
					.isCancelAllowed();
		}

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
		} finally {
			if (serviceListener instanceof AtlasStatusDialog) {
				((AtlasStatusDialog) serviceListener)
						.setCancelAllowed(oldCancelState);
			}
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

				org.geotools.resources.SwingUtilities
						.invokeAndWait(new Runnable() {

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
