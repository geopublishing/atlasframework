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
package org.geopublishing.atlasViewer.swing.internal;

import java.awt.Component;
import java.net.URL;

import javax.jnlp.DownloadServiceListener;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.swing.AtlasViewerGUI;

import de.schmitzm.swing.StatusDialog;

/**
 * An extension of {@link StatusDialog} that also implements a
 * {@link DownloadServiceListener} {@link InterfaceAdapter#}. The dialog is
 * visible by default and disposes when >=99% of the process are reached.
 * 
 * @author stefan
 * 
 */
public class JnlpStatusDialog extends StatusDialog implements
		DownloadServiceListener {
	final static private Logger LOGGER = Logger.getLogger(JnlpStatusDialog.class);

	public JnlpStatusDialog(Component parent, String title, String message) {
		super(parent, title, message);
		
		getProgressBar().setIndeterminate(false);
		getProgressBar().setMinimum(0);
		getProgressBar().setMaximum(100);

		setVisible(true);
	}

	public JnlpStatusDialog(Component owner) {
		this(owner, AtlasViewerGUI.R("JNLPStatus.Downloading"), AtlasViewerGUI.R("JNLPStatus.Downloading"));
	}

	@Override
	public void downloadFailed(URL arg0, String arg1) {
		canceled = true;
		dispose();
	}

	@Override
	public void progress(URL url, String urlString, long doneSoFar, long full,
			int percentage) {
		
		updateStatus(url, urlString, doneSoFar, full, percentage);
		
	}

	private void updateStatus(URL url, String urlString, long doneSoFar, long full,
			int percentage){

		LOGGER.debug("progress   0="+url+" arg1="+urlString+" arg2="+doneSoFar+" arg3="+full+" arg4"+percentage);
		
		if (doneSoFar >= full)
			dispose();
		else
			getProgressBar().setValue( (int) (full*100 / doneSoFar) );

	}

	@Override
	public void upgradingArchive(URL url, String urlString, int arg2, int arg3) {
//		updateStatus(url, urlString, doneSoFar, full, percentage);
	};

	@Override
	public void validating(URL url, String urlString, long doneSoFar, long full,
			int percentage) {
//		updateStatus(url, urlString, doneSoFar, full, percentage);
	}

}
