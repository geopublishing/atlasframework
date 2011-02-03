package org.geopublishing.geopublisher.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.LoggerResultProgressHandle;
import org.geopublishing.geopublisher.exceptions.AtlasExportException;
import org.geopublishing.gpsync.AtlasFingerprint;
import org.geopublishing.gpsync.GpDiff;
import org.geopublishing.gpsync.GpSync;
import org.jfree.util.Log;
import org.netbeans.spi.wizard.ResultProgressHandle;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPTransferType;

import de.schmitzm.io.IOUtil;
import de.schmitzm.swing.formatter.MbDecimalFormatter;

/**
 * Uploads a full or differential ZIP of an atlas to ftp://ftp.geopublishing.org
 */
public class GpFtpAtlasExport extends AbstractAtlasExporter {

	final static Logger LOGGER = Logger.getLogger(GpFtpAtlasExport.class);

	public static final String FTP_GEOPUBLISHING_ORG = "ftp.geopublishing.org";
	public static final String FTP_GEOPUBLISHING_ORG_URL = "ftp://"
			+ FTP_GEOPUBLISHING_ORG;
	public static final String GEOPUBLISHING_ORG = "http://gisbert.wikisquare.de:8088/gp-hoster-jsf/";

	public GpFtpAtlasExport(AtlasConfigEditable ace,
			ResultProgressHandle progress) {
		super(ace, progress);
	}

	public GpFtpAtlasExport(AtlasConfigEditable ace) {
		super(ace, new LoggerResultProgressHandle());
	}

	@Override
	public void export() throws Exception {

		GpSync gpSync = new GpSync(ace.getAtlasDir(), ace.getBaseName());

		LOGGER.info("Contacting geopublishing.org");
		progress.setBusy("Contacting geopublishing.org");// i8n

		final AtlasFingerprint requestedFingerprint = requestFingerprint(ace,
				progress);
		if (requestedFingerprint == null) {
			Log.info("GpFtf sync: this is a new atlas.");
		} else
			progress.setBusy("Comparing atlases");// i8n
		GpDiff gpDiff = gpSync.compare(requestedFingerprint);
		checkAbort();

		if (gpDiff.isSame()) {
			progress.finished("Atlases are the same. Nothing to upload"); // i8n
			return;
		}

		// long sizeMb = calcSize(gpDiff) / 1024 / 1024;

		progress.setBusy("Creating zip"); // i8n
		File zipFile = gpSync.createZip(gpDiff);
		checkAbort();

		long zipSizeMb = zipFile.length();
		progress.setBusy("Uploading " + gpDiff.getDiffFilePaths().size()
				+ " files (" + new MbDecimalFormatter().format(zipSizeMb)
				+ "), delete " + gpDiff.getFilesToDelete().size() + "files"); // i8n

		final FTPClient ftpClient = new FTPClient();
		try { // quit ftp connection
			ftpClient.setRemoteHost(FTP_GEOPUBLISHING_ORG);
			ftpClient.setTimeout(5000);
			ftpClient.connect();
			// TODO Generate programatically!?
			ftpClient.login("geopublisher", "g9e8o7p6u5b4l3i2s1h0er");
			FileInputStream fis = new FileInputStream(zipFile);
			try {
				ftpClient.setType(FTPTransferType.BINARY);

				// Start a Timer-Thread to look for cancel requests
				final Timer timer = new Timer();
				timer.schedule(new TimerTask() {

					@Override
					public void run() {
						if (cancel.get() == true) {
							ftpClient.cancelTransfer();
							timer.cancel();
						}
					}
				}, 300, 300);

				final String zipFilename = gpSync.getAtlasname() + ".zip";
				Log.info("FTP Upload " + zipFilename + " started");
				ftpClient.put(fis, zipFilename);
				if (cancel.get() == true) {
					Log.info("FTP Upload " + zipFilename
							+ " cancelled, deleting zip on ftp");
					ftpClient.delete(gpSync.getAtlasname() + ".zip");
					Log.debug("deleting " + zipFilename + " on ftp finished");
				} else
					Log.info("FTP Upload " + zipFilename + " finished");
				checkAbort();

			} finally {
				fis.close();
				zipFile.delete();
			}
		} finally {
			ftpClient.quit();
			progress.setBusy("FTP connection cosed"); // i8n
		}

	}

	/**
	 * Size of diff to upload uncompressed...
	 */
	private long calcSize(GpDiff gpDiff) {
		long size = 0l;
		for (String fp : gpDiff.getDiffFilePaths()) {
			size += new File(ace.getAtlasDir(), fp).length();
		}
		return size;
	}

	/**
	 * Initiates a URL request to the gp-hoster servlet and requests a
	 * fingerprint for this atlas. This method times out after 5 seconds and
	 * then throws a new runtimeEx(SocketTimeoutException). returns
	 * <code>null</code> if it is a new atlas.
	 * 
	 * @param progress
	 * 
	 *            TODO Should be moved to a "hoster api" package/class/module
	 * @throws SocketTimeoutException
	 * 
	 * @MOVE GpHoster REST API module one day
	 */
	public static AtlasFingerprint requestFingerprint(AtlasConfigEditable ace,
			ResultProgressHandle progress) {
		InputStream afp2Is;
		final String path = GEOPUBLISHING_ORG + ace.getBaseName()
				+ ".fingerprint";
		try {
			// LOGGER.debug("Connecting " + path);
			final URL url = new URL(path);

			URLConnection conn = url.openConnection();
			// setting these timeouts ensures the client does not deadlock
			// indefinitely
			// when the server has problems.
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);

			afp2Is = conn.getInputStream();
			String afp2Txt;
			try {
				// LOGGER.debug(" connecting success!");
				afp2Txt = IOUtil.convertStreamToString(afp2Is);
			} finally {
				afp2Is.close();
			}
			if (afp2Txt.contains("ERROR"))
				return null;
			final AtlasFingerprint afpRemote = new AtlasFingerprint(afp2Txt);

			return afpRemote;
		} catch (MalformedURLException e) {
			throw new RuntimeException("Could not get atlas fingerprint from "
					+ path, e);
		} catch (FileNotFoundException e) {
			// if status 404 then return null;
			// throw new
			return null;
		} catch (SocketTimeoutException e) {
			throw new AtlasExportException(
					"Timeout while connecting the gphoster servlet:", e);
		} catch (IOException e) {
			LOGGER.debug("Could not get atlas fingerprint from " + path, e);
			return null;
		}
	}
}
