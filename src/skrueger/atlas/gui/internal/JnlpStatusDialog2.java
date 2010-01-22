package skrueger.atlas.gui.internal;

import java.net.URL;

import javax.jnlp.DownloadServiceListener;

import org.apache.log4j.Logger;

/**
 * A very quite implementation of DownloadServiceListener. It doesn't show any
 * GUI, so it should only be started from a windows where there is already a
 * progress bar.
 */
public class JnlpStatusDialog2 implements DownloadServiceListener {

	final static private Logger LOGGER = Logger
			.getLogger(JnlpStatusDialog2.class);

	public JnlpStatusDialog2() {
	}

	@Override
	public void downloadFailed(URL arg0, String arg1) {
	}

	@Override
	public void progress(URL url, String urlString, long doneSoFar, long full,
			int percentage) {
		// updateStatus(url, urlString, doneSoFar, full, percentage);

	}

	private void updateStatus(URL url, String urlString, long doneSoFar,
			long full, int percentage) {
		// LOGGER.debug("progress   0="+url+" arg1="+urlString+" arg2="+doneSoFar+" arg3="+full+" arg4"+percentage);
	}

	@Override
	public void upgradingArchive(URL url, String urlString, int arg2, int arg3) {
	};

	@Override
	public void validating(URL url, String urlString, long doneSoFar,
			long full, int percentage) {
	};

}
