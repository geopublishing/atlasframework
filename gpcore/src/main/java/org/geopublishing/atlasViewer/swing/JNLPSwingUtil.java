/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasViewer.swing;

import java.io.IOException;

import javax.jnlp.DownloadService;
import javax.jnlp.DownloadServiceListener;
import javax.jnlp.UnavailableServiceException;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasStatusDialogInterface;
import org.geopublishing.atlasViewer.JNLPUtil;
import org.geopublishing.atlasViewer.jnlp.JnlpStatusDialog2;
import org.geopublishing.atlasViewer.swing.internal.AtlasTask;


/**
 * A utility class with static methods that deal with JNLP / JavaWebStart
 * related stuff.
 * 
 * @author Stefan Alfons Krueger
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
	public static void loadPart(String part, AtlasStatusDialogInterface statusDialog)
			throws IOException {
		DownloadService ds;
		try {
			ds = getJNLPDownloadService();

			if (ds.isPartCached(part)) {
				LOGGER.info("part " + part + " is JWS cached");
			} else {
				LOGGER.info("part " + part + " is NOT cached.. start DL ");

				// load the resource into the JWS Cache
				ds.loadPart(part, getJNLPDialog()); // TODO use statusDialog
			}
		} catch (UnavailableServiceException e1) {
			throw new IOException(e1);
		}
	}

	public static DownloadServiceListener getJNLPDialog()
			throws UnavailableServiceException {
		// return getJNLPDownloadService().getDefaultProgressWindow();
		return new JnlpStatusDialog2();
	}

	public static void loadPart(String[] parts, AtlasStatusDialogInterface statusDialog)
			throws IOException {
		DownloadService ds;
		try {
			ds = getJNLPDownloadService();

			// load the resource into the JWS Cache
			ds.loadPart(parts, getJNLPDialog()); // use statusDialog

		} catch (UnavailableServiceException e1) {
			throw new IOException(e1);
		}
	}

}
