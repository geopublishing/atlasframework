package org.geopublishing.geopublisher.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.gpsync.AtlasFingerPrint;
import org.geopublishing.gpsync.GpDiff;
import org.geopublishing.gpsync.GpSync;
import org.netbeans.spi.wizard.ResultProgressHandle;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPTransferType;

/**
 * TODO DOKU :-)
 */
public class GpFtpAtlasExport implements AtlasExporter {

	private static final String FTP_GEOPUBLISHING_ORG = "ftp.geopublishing.org";
	private final AtlasConfigEditable ace;

	public GpFtpAtlasExport(AtlasConfigEditable ace) {
		this.ace = ace;
	}

	@Override
	public void export(ResultProgressHandle progress) throws Exception {
		FTPClient ftpClient = new FTPClient();
		ftpClient.setRemoteHost(FTP_GEOPUBLISHING_ORG);
		ftpClient.connect();
		ftpClient.login("geopublisher", "g9e8o7p6u5b4l3i2s1h0er");
		GpSync gpSync = new GpSync(ace.getAtlasDir(), ace.getBaseName());

		String[] filesInDir = ftpClient.dir();
		// ArrayUtils.contains(array, valueToFind)
		boolean fileFound = false;
		for (String bla : filesInDir) { // new utility class?
			if (bla.equals(gpSync.getAtlasname() + ".txt"))
				fileFound = true;
		}

		GpDiff gpDiff = null;
		if (fileFound) {
			File incomingAfp = File.createTempFile(gpSync.getAtlasname(),
					".txt");
			FileOutputStream AfpOut = new FileOutputStream(incomingAfp);
			try {
				ftpClient.setType(FTPTransferType.BINARY);
				ftpClient.get(AfpOut, gpSync.getAtlasname() + ".txt");
				AtlasFingerPrint afpRemote = new AtlasFingerPrint(incomingAfp,
						true);
				gpDiff = gpSync.compare(afpRemote);
			} finally {
				AfpOut.close();
				incomingAfp.delete();
			}
		} // createZip with null as gpDiff
		else {
			gpDiff = gpSync.compare(null);
		}
		File createZip = gpSync.createZip(gpDiff);
		FileInputStream fis = new FileInputStream(createZip);
		try {
			ftpClient.setType(FTPTransferType.BINARY);
			ftpClient.put(fis, gpSync.getAtlasname() + ".zip");
		} finally {
			fis.close();
			createZip.delete();
			ftpClient.quit();
		}

	}

}
