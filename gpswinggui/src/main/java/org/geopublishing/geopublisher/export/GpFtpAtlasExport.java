package org.geopublishing.geopublisher.export;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.gpsync.AtlasFingerprint;
import org.geopublishing.gpsync.GpDiff;
import org.geopublishing.gpsync.GpSync;
import org.jfree.util.Log;
import org.netbeans.spi.wizard.ResultProgressHandle;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPTransferType;

import de.schmitzm.swing.formatter.MbDecimalFormatter;

/**
 * Uploads a full or differential ZIP of an atlas to ftp://ftp.geopublishing.org
 */
public class GpFtpAtlasExport implements AtlasExporter {

	Logger LOGGER = Logger.getLogger(GpFtpAtlasExport.class);

	public static final String FTP_GEOPUBLISHING_ORG = "ftp.geopublishing.org";
	public static final String GEOPUBLISHING_ORG = "http://gisbert.wikisquare.de:8088/gp-hoster-jsf/";

	/**
	 * AtlasConfig zu export
	 */
	private final AtlasConfigEditable ace;

	public GpFtpAtlasExport(AtlasConfigEditable ace) {
		this.ace = ace;
	}

	@Override
	public void export(ResultProgressHandle progress) throws Exception {

		GpSync gpSync = new GpSync(ace.getAtlasDir(), ace.getBaseName());

		LOGGER.info("Contacting geopublishing.org");
		progress.setBusy("Contacting geopublishing.org");// i8n

		final AtlasFingerprint requestedFingerprint = requestFingerprint(progress);
		if (requestedFingerprint == null) {
			Log.info("GpFtf sync: this is a new atlas.");
		} else
			progress.setBusy("Comparing atlases");// i8n
		GpDiff gpDiff = gpSync.compare(requestedFingerprint);

		if (gpDiff.isSame()) {
			progress.finished("Atlases are the same. Nothing to upload"); // i8n
			return;
		}

		// long sizeMb = calcSize(gpDiff) / 1024 / 1024;

		progress.setBusy("Creating zip"); // i8n
		File zipFile = gpSync.createZip(gpDiff);

		long zipSizeMb = zipFile.length() / 1024 / 1024;
		progress.setBusy("Uploading "
				+ new MbDecimalFormatter().format(zipSizeMb) + " delete "
				+ gpDiff.getFilesToDelete().size() + "files"); // i8n

		FTPClient ftpClient = new FTPClient();
		try { // quit ftp connection
			ftpClient.setRemoteHost(FTP_GEOPUBLISHING_ORG);
			ftpClient.connect();
			ftpClient.login("geopublisher", "g9e8o7p6u5b4l3i2s1h0er");
			FileInputStream fis = new FileInputStream(zipFile);
			try {
				ftpClient.setType(FTPTransferType.BINARY);
				ftpClient.put(fis, gpSync.getAtlasname() + ".zip");
			} finally {
				fis.close();
				zipFile.delete();
			}
		} finally {
			ftpClient.quit();
		}
		progress.setBusy("Connection cosed. "); // i8n

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
	 * Read a (not too big) Inputtream directly into a String.
	 * 
	 * @param is
	 *            {@link InputStream} to read from
	 * 
	 * @deprecated use schmitzm
	 */
	@Deprecated
	public String convertStreamToString(InputStream is) throws IOException {
		/*
		 * To convert the InputStream to String we use the Reader.read(char[]
		 * buffer) method. We iterate until the Reader return -1 which means
		 * there's no more data to read. We use the StringWriter class to
		 * produce the string.
		 */
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}

	/**
	 * Initiates a URL request to the gp-hoster servlet and requests a
	 * fingerprint for this atlas.
	 * 
	 * @param progress
	 * 
	 * @MOVE GpHoster REST API module one day
	 */
	AtlasFingerprint requestFingerprint(ResultProgressHandle progress) {
		InputStream afp2Is;
		final String path = GEOPUBLISHING_ORG + ace.getBaseName()
				+ ".fingerprint";
		try {
			LOGGER.debug("Connecting " + path);
			String afp2Txt;
			afp2Is = new URL(path).openStream();
			try {
				LOGGER.debug("  connecting success!");
				afp2Txt = convertStreamToString(afp2Is);
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
		} catch (IOException e) {
			LOGGER.debug("Could not get atlas fingerprint from " + path, e);
			return null;
		}
	}
}
