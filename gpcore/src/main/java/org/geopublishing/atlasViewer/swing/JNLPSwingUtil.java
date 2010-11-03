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

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.io.IOException;

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
		DownloadService ds;
		try {
			ds = getJNLPDownloadService();

			if (ds.isPartCached(part)) {
				LOGGER.info("part " + part + " is JWS cached");
			} else {
				LOGGER.info("part " + part + " is NOT cached.. start DL ");

				// load the resource into the JWS Cache
				ds.loadPart(part, statusDialog);
			}
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
		DownloadService ds;
		try {
			ds = getJNLPDownloadService();

			LOGGER.debug("starting loadPart on EDT ("
					+ SwingUtilities.isEventDispatchThread() + ") ");
			ds.loadPart(parts, statusDialog);

		} catch (UnavailableServiceException e1) {
			throw new IOException(e1);
		}
	}

	public static void loadPart(String part, Component owner)
			throws IOException {
		DownloadService ds;
		try {
			ds = getJNLPDownloadService();

			if (ds.isPartCached(part)) {
				LOGGER.info("part " + part + " is JWS cached");
			} else {

				LOGGER.info("part " + part
						+ " is NOT cached.. starting download... ");

				// load the resource into the JWS Cache
				ds.loadPart(part, new AtlasStatusDialog(owner));
			}
		} catch (UnavailableServiceException e1) {
			throw new IOException(e1);
		}
	}

	public static void loadPart(String id) throws IOException {
		// TODO && SwingUtilities.isEventDispatchThread() removen!
		if (!GraphicsEnvironment.isHeadless() && AtlasViewerGUI.isRunning()
				&& SwingUtilities.isEventDispatchThread()) {
			JNLPSwingUtil
					.loadPart(id, AtlasViewerGUI.getInstance().getJFrame());
		} else {
			loadPart(new String[] { id }, null);
		}
	}

}
