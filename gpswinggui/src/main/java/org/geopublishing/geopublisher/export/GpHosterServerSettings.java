package org.geopublishing.geopublisher.export;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.geopublishing.geopublisher.export.gphoster.GpHosterClient;
import org.geopublishing.geopublisher.gui.settings.GpHosterServerList;
import org.geopublishing.geopublisher.gui.settings.GpHostingOptionsTab;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;

import de.schmitzm.geotools.io.AbstractServerSettings;
import de.schmitzm.swing.input.ManualInputOption;
import de.schmitzm.swing.input.ManualInputOption.PasswordViewable;
import de.schmitzm.swing.input.ManualInputOption.Text;
import de.schmitzm.swing.input.MultipleOptionPane;

public class GpHosterServerSettings extends AbstractServerSettings {

	public static final GpHosterServerSettings DEFAULT = new GpHosterServerSettings();
	static {
		DEFAULT.setTitle("geopublishing.org");
		DEFAULT.setRestUrl(GpHosterClient.DEFAULT_GPHOSTER_REST_URL);
		DEFAULT.setFtpHostname(GpHosterClient.DEFAULT_GPHOSTER_FTP_HOSTNAME);
		DEFAULT.setUsername(null);
		DEFAULT.setPassword(null);
	}

	Logger LOGGER = Logger.getLogger(GpHosterServerSettings.class);

	/**
	 * @return e.g."http://hoster.geopublishing.org:8088/gp-hoster-jsf/"
	 */
	public String getRestUrl() {
		return restUrl;
	}

	public void setRestUrl(String urlGpHosterRest) {
		this.restUrl = urlGpHosterRest;
	}

	public String getFtpHostname() {
		return ftpHostname;
	}

	public void setFtpHostname(String ftpHostname) {
		this.ftpHostname = ftpHostname;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		if (!checkString(username))
			throw new IllegalArgumentException(GeopublisherGUI.R(
					"GpHosterServerSettings.IllegalUsername", username,
					DELIMITER, GpHosterServerSettings.DELIMITER));
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		if (!checkString(password))
			throw new IllegalArgumentException(GeopublisherGUI.R(
					"GpHosterServerSettings.IllegalPassword", password,
					DELIMITER, GpHosterServerSettings.DELIMITER));
		this.password = password;
	}

	/**
	 * URL to where the REST interface of gp hoster is running
	 */
	String restUrl;

	/**
	 * Hostname (without any protocoll) of where the FTP is running.
	 */
	String ftpHostname;

	/**
	 * Username to use
	 */
	String username;

	/**
	 * Password to use
	 */
	String password;

	@Override
	public String toPropertiesString() {
		StringBuffer serialized = new StringBuffer(100);

		serialized.append(getAlias());
		serialized.append(DELIMITER);

		serialized.append(getRestUrl());
		serialized.append(DELIMITER);

		serialized.append(getFtpHostname());
		serialized.append(DELIMITER);

		serialized.append(getTitle());
		serialized.append(DELIMITER);

		serialized.append(getUsername());
		serialized.append(DELIMITER);

		serialized.append(getPassword());
		serialized.append(DELIMITER);

		return serialized.toString();
	}

	@Override
	public boolean parsePropertiesString(String propString) {
		if (propString == null || propString.isEmpty())
			return false;
		try {

			// + " " makes sense, trust me!
			String[] split = (propString + " ").split(Pattern.quote(DELIMITER));

			int i = 0;
			setAlias(split[i++]);
			setRestUrl(split[i++]);
			setFtpHostname(split[i++]);
			setTitle(split[i++]);
			setUsername(StringUtils.stripToNull(split[i++]));
			setPassword(split[i++]);

			return true;
		} catch (Exception e) {
			LOGGER.warn("couldn't parse " + propString, e);
			return false;
		}
	}

	@Override
	public boolean isAvailable() {
		// TODO Auto-generated method stub
		return false;
	}

	public static GpHosterServerSettings createOrEdit(
			GpHostingOptionsTab gpHostingOptionsTab,
			GpHosterServerSettings dbServer) {

		boolean newCreated = false;

		if (dbServer == null) {
			newCreated = true;
			dbServer = new GpHosterServerSettings();
		}

		Text titleInput = new ManualInputOption.Text(
				GeopublisherGUI.R("GpHosterServerSettings.Title"), true,
				dbServer.getTitle());

		Text hostInput = new ManualInputOption.Text(
				GeopublisherGUI.R("GpHosterServerSettings.RestURL"), true,
				dbServer.getRestUrl());

		Text ftphostInput = new ManualInputOption.Text(
				GeopublisherGUI.R("GpHosterServerSettings.FTPHostname"), true,
				dbServer.getFtpHostname());

		Text userInput = new ManualInputOption.Text(
				GeopublisherGUI.R("GpHosterServerSettings.Username"), false,
				dbServer.getUsername());

		PasswordViewable passwdInput = new ManualInputOption.PasswordViewable(
				GeopublisherGUI.R("GpHosterServerSettings.Password"), false,
				dbServer.getPassword());

		Object[] input = MultipleOptionPane.showMultipleInputDialog(
				gpHostingOptionsTab,
				GeopublisherGUI.R("GpHosterServerSettings.DBParameters"),
				titleInput, hostInput, ftphostInput, userInput, passwdInput);

		if (input == null) {
			if (newCreated)
				return null;
			else
				return dbServer;
		} else {
			dbServer.setTitle((String) input[0]);
			dbServer.setRestUrl((String) input[1]);
			dbServer.setFtpHostname((String) input[2]);
			dbServer.setUsername((String) input[3]);
			dbServer.setPassword(String.valueOf((char[]) input[4]));
		}

		return dbServer;

	}

	public static boolean checkString(String stringValue) {
		if (stringValue == null)
			return true;
		if (stringValue.contains(DELIMITER))
			return false;
		if (stringValue.contains(GpHosterServerList.DELIMITER))
			return false;
		return true;
	}
}
