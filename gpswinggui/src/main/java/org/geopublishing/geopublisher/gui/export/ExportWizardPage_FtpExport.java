package org.geopublishing.geopublisher.gui.export;

import java.awt.Component;
import java.awt.Cursor;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GPProps;
import org.geopublishing.geopublisher.export.GpFtpAtlasExport;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.netbeans.spi.wizard.WizardPage;

import com.enterprisedt.net.ftp.FTPClient;

import de.schmitzm.io.IOUtil;

public class ExportWizardPage_FtpExport extends WizardPage {
	final static protected Logger LOGGER = Logger
			.getLogger(ExportWizardPage_FtpExport.class);
	private JTextField UrlJTextField;
	private final String validationFtpFailedMsg_Offline = GeopublisherGUI
			.R("ExportWizard.Ftp.ValidationError_Offline");
	private final String validationFtpFailedMsg_GpFtpDown = GeopublisherGUI
			.R("ExportWizard.Ftp.ValidationError_GpFtpDown");
	private final String validationFtpFailedMsg_GpHosterDown = GeopublisherGUI
			.R("ExportWizard.Ftp.ValidationError_GpHosterDown");
	JLabel explanationJLabel = new JLabel(
			GeopublisherGUI.R("ExportWizard.Ftp.Explanation"));
	JCheckBox firstSyncJCheckBox;
	private final AtlasConfigEditable ace;

	public ExportWizardPage_FtpExport() {
		ace = GeopublisherGUI.getInstance().getAce();
		initGui();
	}

	private boolean checkFirstExport() {
		final String path = GpFtpAtlasExport.GEOPUBLISHING_ORG
				+ ace.getBaseName() + ".fingerprint";
		boolean isFirstExport = !IOUtil.urlExists(path);
		return isFirstExport;

	}

	public static String getDescription() {
		return GeopublisherGUI.R("ExportWizard.Ftp");
	}

	private void initGui() {
		setSize(ExportWizard.DEFAULT_WPANEL_SIZE);
		setPreferredSize(ExportWizard.DEFAULT_WPANEL_SIZE);
		setLayout(new MigLayout("wrap 1"));
		add(explanationJLabel);
		add(getFirstSyncJCheckBox());
		add(getUrlJTextField());
		getUrlJTextField().setText("http://geopublishing.org");
	}

	private JCheckBox getFirstSyncJCheckBox() {
		if (firstSyncJCheckBox == null) {
			firstSyncJCheckBox = new JCheckBox("Create new User");
			firstSyncJCheckBox.setName(ExportWizard.FTP_FIRST);
			firstSyncJCheckBox.setSelected(checkFirstExport());
		}
		return firstSyncJCheckBox;
	}

	private JTextField getUrlJTextField() {
		if (UrlJTextField == null) {
			UrlJTextField = new JTextField(GPProps.get(
					GPProps.Keys.LastExportFolder, ""));

			UrlJTextField.setName(ExportWizard.EXPORTFOLDER);
		}

		return UrlJTextField;
	}

	@Override
	protected String validateContents(final Component component,
			final Object event) {

		// this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		// SwingUtil.getParentWindow(component).setCursor(
		// Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		try {
			// Check online in general
			if (!IOUtil.urlExists("http://www.denic.de/"))
				return validationFtpFailedMsg_Offline;

			try {
				// Check FTP
				final FTPClient ftpClient = new FTPClient();
				ftpClient.setTimeout(5000);
				ftpClient.setRemoteHost(GpFtpAtlasExport.FTP_GEOPUBLISHING_ORG);
				ftpClient.connect();
				ftpClient.quit();
			} catch (Exception e) {
				if (!IOUtil
						.urlExists(GpFtpAtlasExport.FTP_GEOPUBLISHING_ORG_URL))
					return validationFtpFailedMsg_GpFtpDown;
			}

			// Check GpHoster Servlet
			if (!IOUtil.urlExists(GpFtpAtlasExport.GEOPUBLISHING_ORG))
				return validationFtpFailedMsg_GpHosterDown;
			return null;
		} finally {
			this.setCursor(Cursor.getDefaultCursor());
		}
	}
}