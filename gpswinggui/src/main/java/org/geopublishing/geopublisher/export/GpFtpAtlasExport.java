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
import org.netbeans.spi.wizard.ResultProgressHandle;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPTransferType;

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
		GpDiff gpDiff = gpSync.compare(requestFingerprint());

		FTPClient ftpClient = new FTPClient();
		ftpClient.setRemoteHost(FTP_GEOPUBLISHING_ORG);
		ftpClient.connect();
		ftpClient.login("geopublisher", "g9e8o7p6u5b4l3i2s1h0er");
		try { // quit ftp connection

			// byte[] filesInDir = ftpClient.dir();
			// gpSync.getAtlasname() + ".txt";

			// boolean fileFound = ArrayUtils.contains(filesInDir, valueToFind)
			// for (String bla : filesInDir) { // new utility class?
			// if (bla.equals(gpSync.getAtlasname() + ".txt"))
			// fileFound = true;
			// }

			// GpDiff gpDiff = null;
			// if (fileFound) {
			// File incomingAfp = File.createTempFile(gpSync.getAtlasname(),
			// ".txt");
			// FileOutputStream AfpOut = new FileOutputStream(incomingAfp);
			// try {
			// ftpClient.setType(FTPTransferType.BINARY);
			// ftpClient.get(AfpOut, gpSync.getAtlasname() + ".txt");
			// AtlasFingerprint afpRemote = new AtlasFingerprint(
			// incomingAfp, true);
			// gpDiff = gpSync.compare(afpRemote);
			// } finally {
			// AfpOut.close();
			// incomingAfp.delete();
			// }
			// } // createZip with null as gpDiff
			// else {
			// gpDiff = gpSync.compare(null);
			// }
			File createZip = gpSync.createZip(gpDiff);
			FileInputStream fis = new FileInputStream(createZip);
			try {
				ftpClient.setType(FTPTransferType.BINARY);
				ftpClient.put(fis, gpSync.getAtlasname() + ".zip");
			} finally {
				fis.close();
				createZip.delete();
			}
		} finally {
			ftpClient.quit();
		}

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
	 * @MOVE GpHoster REST API module one day
	 */
	AtlasFingerprint requestFingerprint() {
		InputStream afp2Url;
		final String path = GEOPUBLISHING_ORG + "/" + ace.getBaseName()
				+ ".fingerprint";
		try {
			afp2Url = new URL(path).openStream();
			String afp2Txt = convertStreamToString(afp2Url);
			// if status 404 then return null;
			if (afp2Txt.contains("ERROR"))
				return null;

			final AtlasFingerprint afpRemote = new AtlasFingerprint(afp2Txt);
			return afpRemote;
		} catch (MalformedURLException e) {
			throw new RuntimeException("Could not get atlas fingerprint from "
					+ path, e);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Could not get atlas fingerprint from "
					+ path, e);
		} catch (IOException e) {
			LOGGER.debug("Could not get atlas fingerprint from " + path, e);
			return null;
		}
	}
}
