package org.geopublishing.atlasViewer.swing;

import java.net.URL;

import javax.jnlp.DownloadServiceListener;

import org.apache.log4j.Logger;

public class NoGuiDownloadServiceListener implements DownloadServiceListener {
	final static private Logger LOGGER = Logger.getLogger(NoGuiDownloadServiceListener.class);

	@Override
	public void downloadFailed(URL arg0, String arg1) {
		LOGGER.error("downloadFailed " + arg0 + " " + arg1);
	}

	@Override
	public void progress(URL arg0, String arg1, long arg2, long arg3, int arg4) {
		LOGGER.debug("progress " + arg0 + " " + arg1 + " " + arg2 + " " + arg3
				+ " " + arg4);
	}

	@Override
	public void upgradingArchive(URL arg0, String arg1, int arg2, int arg3) {
		LOGGER.debug("upgrading " + arg0 + " " + arg1 + " " + arg2 + " " + arg3);
	}

	@Override
	public void validating(URL arg0, String arg1, long arg2, long arg3, int arg4) {
		LOGGER.debug("validating " + arg0 + " " + arg1 + " " + arg2 + " "
				+ arg3 + " " + arg4);
	}

}
