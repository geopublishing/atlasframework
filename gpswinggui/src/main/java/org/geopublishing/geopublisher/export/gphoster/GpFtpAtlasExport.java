package org.geopublishing.geopublisher.export.gphoster;

import java.io.File;
import java.io.FileInputStream;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GPProps;
import org.geopublishing.geopublisher.GPProps.Keys;
import org.geopublishing.geopublisher.LoggerResultProgressHandle;
import org.geopublishing.geopublisher.export.AbstractAtlasExporter;
import org.geopublishing.geopublisher.export.GpHosterServerSettings;
import org.geopublishing.geopublisher.gui.settings.GpHosterServerList;
import org.geopublishing.gpsync.AtlasFingerprint;
import org.geopublishing.gpsync.GpDiff;
import org.geopublishing.gpsync.GpSync;
import org.netbeans.spi.wizard.ResultProgressHandle;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPTransferType;

import de.schmitzm.swing.formatter.MbDecimalFormatter;

/**
 * Uploads a full or differential ZIP of an atlas to ftp://ftp.geopublishing.org
 */
public class GpFtpAtlasExport extends AbstractAtlasExporter {

	final static Logger log = Logger.getLogger(GpFtpAtlasExport.class);

	private final GpHosterClient gphc;

	public GpFtpAtlasExport(AtlasConfigEditable ace, GpHosterClient gphc, ResultProgressHandle progress) {
		super(ace, progress);
		this.gphc = gphc;
	}

	public GpFtpAtlasExport(AtlasConfigEditable ace, GpHosterClient gphc) {
		this(ace, gphc, new LoggerResultProgressHandle());
	}

	@Override
	public void export() throws Exception {

		GpSync gpSync = new GpSync(ace.getAtlasDir(), ace.getBaseName());

		log.info("Contacting geopublishing.org");
		progress.setBusy("Contacting geopublishing.org");// i8n

		checkAbort();
		AtlasFingerprint requestedFingerprint = null;
		if (!gphc.atlasBasenameFree(ace.getBaseName()))
			log.info("GpFtf sync: this is a new atlas.");
		else {
			requestedFingerprint = gphc.atlasFingerprint(ace.getBaseName());
		}

		GpDiff gpDiff = gpSync.compare(requestedFingerprint);
		if (gpDiff.isSame()) {
			progress.finished("Atlases are the same. Nothing to upload"); // i8n
			return;
		}

		// long sizeMb = calcSize(gpDiff) / 1024 / 1024;

		progress.setBusy("Creating zip"); // i8n
		File zipFile = gpSync.createZip(gpDiff);
		try {

			checkAbort();

			long zipSizeMb = zipFile.length();
			progress.setBusy("Uploading " + gpDiff.getDiffFilePaths().size() + " files ("
					+ new MbDecimalFormatter().format(zipSizeMb) + "), delete " + gpDiff.getFilesToDelete().size()
					+ "files"); // i8n

			final FTPClient ftpClient = new FTPClient();
			try { // quit ftp connection
				ftpClient.setRemoteHost(gphc.getFtpHostname());
				ftpClient.setTimeout(5000);
				ftpClient.connect();
				// TODO Generate programatically!
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

					final String zipFilename = zipFile.getName();
					// final String zipFilename = gpSync.getAtlasname() + ".zip";
					log.info("FTP Upload " + zipFilename + " started");
					ftpClient.put(fis, zipFilename);
					if (cancel.get() == true) {
						log.info("FTP Upload " + zipFilename + " cancelled, deleting zip on ftp");
						ftpClient.delete(gpSync.getAtlasname() + ".zip");
						log.debug("deleting " + zipFilename + " on ftp finished");
					} else {
						gphc.informAboutUploadedZipFile(ace.getBaseName(), zipFile);
						log.info("FTP Upload " + zipFilename + " finished");
					}
					checkAbort();

				} finally {
					fis.close();
				}
			} finally {
				ftpClient.quit();
			}
		} finally {
			zipFile.delete();
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

	//
	// /**
	// * Initiates a URL request to the gp-hoster servlet and requests a
	// * fingerprint for this atlas. This method times out after 5 seconds and
	// * then throws a new runtimeEx(SocketTimeoutException). returns
	// * <code>null</code> if it is a new atlas.
	// *
	// * @param progress
	// *
	// * TODO Should be moved to a "hoster api" package/class/module
	// * @throws SocketTimeoutException
	// *
	// * @MOVE GpHoster REST API module one day
	// */
	// public static AtlasFingerprint requestFingerprint(AtlasConfigEditable
	// ace,
	// ResultProgressHandle progress) {
	// InputStream afp2Is;
	// final String path = GEOPUBLISHING_ORG + ace.getBaseName()
	// + ".fingerprint";
	// try {
	// // LOGGER.debug("Connecting " + path);
	// final URL url = new URL(path);
	//
	// URLConnection conn = url.openConnection();
	// // setting these timeouts ensures the client does not deadlock
	// // indefinitely
	// // when the server has problems.
	// conn.setConnectTimeout(5000);
	// conn.setReadTimeout(5000);
	//
	// afp2Is = conn.getInputStream();
	// String afp2Txt;
	// try {
	// // LOGGER.debug(" connecting success!");
	// afp2Txt = IOUtil.convertStreamToString(afp2Is);
	// } finally {
	// afp2Is.close();
	// }
	// if (afp2Txt.contains("ERROR"))
	// return null;
	// final AtlasFingerprint afpRemote = new AtlasFingerprint(afp2Txt);
	//
	// return afpRemote;
	// } catch (MalformedURLException e) {
	// throw new RuntimeException("Could not get atlas fingerprint from "
	// + path, e);
	// } catch (FileNotFoundException e) {
	// // if status 404 then return null;
	// // throw new
	// return null;
	// } catch (SocketTimeoutException e) {
	// throw new AtlasExportException(
	// "Timeout while connecting the gphoster servlet:", e);
	// } catch (IOException e) {
	// LOGGER.debug("Could not get atlas fingerprint from " + path, e);
	// return null;
	// }
	// }

	/**
	 * @return The selected or default GpHosterServerSettings for exporting
	 */
	public static GpHosterServerSettings getSelectedGpHosterServerSettings() {
		try {

			GPProps.get(Keys.gpHosterServerList);
			final String propertiesString = GPProps.get(Keys.gpHosterServerList,
					GpHosterServerSettings.DEFAULT.toPropertiesString());
			final Integer int1 = GPProps.getInt(Keys.lastGpHosterServerIdx, 0);
			return new GpHosterServerList(propertiesString).get(int1);
		} catch (Exception e) {
			log.error("error getting the select gphosterservice", e);
			return GpHosterServerSettings.DEFAULT;
		}
	}

}
