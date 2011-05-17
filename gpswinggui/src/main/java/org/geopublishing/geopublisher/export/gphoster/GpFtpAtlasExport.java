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

		log.info("Contacting " + getSelectedGpHosterServerSettings().getRestUrl());
		progress.setBusy("Contacting " + getSelectedGpHosterServerSettings().getRestUrl());// i8n

		checkAbort();
		AtlasFingerprint requestedFingerprint = null;
		
		if (gphc.atlasBasenameFree(ace.getBaseName()))
			log.info("GpFtf sync: this is a new atlas.");
		else {
			requestedFingerprint = gphc.atlasFingerprint(ace.getBaseName());
		}

		progress.setBusy("comparing atlases"); // i8n

		GpDiff gpDiff = gpSync.compare(requestedFingerprint);
		if (gpDiff.isSame()) {
			progress.finished("Atlases are the same. Nothing to upload"); // i8n
			return;
		}

		// long sizeMb = calcSize(gpDiff) / 1024 / 1024;

		progress.setBusy("Creating zip for upload"); // i8n
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


	/**
	 * @return The selected or default GpHosterServerSettings for exporting
	 */
	public static GpHosterServerSettings getSelectedGpHosterServerSettings() {
		try {

			String propertiesString = GPProps.get(Keys.gpHosterServerList);
			if (propertiesString == null) {
				propertiesString = GpHosterServerSettings.DEFAULT.toPropertiesString();
				GPProps.set(Keys.gpHosterServerList, propertiesString);
				GPProps.store();
			}
			Integer int1 = GPProps.getInt(Keys.lastGpHosterServerIdx, null);
			if (int1 == null) {
				GPProps.set(Keys.lastGpHosterServerIdx, 0);
				GPProps.store();
				int1 = 0;
			}
			return new GpHosterServerList(propertiesString).get(int1);
		} catch (Exception e) {
			log.error("Error in gphosterlist", e);
			GPProps.set(Keys.gpHosterServerList, GpHosterServerSettings.DEFAULT.toPropertiesString());
			GPProps.set(Keys.lastGpHosterServerIdx, 0);
			log.error("error getting the select gphosterservice", e);
			GPProps.store();
			return GpHosterServerSettings.DEFAULT;
		}
	}

}
