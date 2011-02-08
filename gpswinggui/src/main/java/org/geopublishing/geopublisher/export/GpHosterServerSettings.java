package org.geopublishing.geopublisher.export;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.geopublishing.geopublisher.export.gphoster.GpHosterClient;
import org.geopublishing.geopublisher.gui.settings.GpHostingOptionsTab;

import de.schmitzm.geotools.io.AbstractServerSettings;
import de.schmitzm.swing.input.ManualInputOption;
import de.schmitzm.swing.input.ManualInputOption.PasswordViewable;
import de.schmitzm.swing.input.ManualInputOption.Text;
import de.schmitzm.swing.input.MultipleOptionPane;

public class GpHosterServerSettings extends AbstractServerSettings {

	public static final GpHosterServerSettings DEFAULT = new GpHosterServerSettings();
	static {
		DEFAULT.setTitle("geopublishing.org");
		DEFAULT.setUrlGpHosterRest(GpHosterClient.DEFAULT_GPHOSTER_REST_URL);
		DEFAULT.setFtpHostname(GpHosterClient.DEFAULT_GPHOSTER_FTP_HOSTNAME);
		DEFAULT.setUsername(null);
		DEFAULT.setPassword(null);
	}

	Logger LOGGER = Logger.getLogger(GpHosterServerSettings.class);

	public String getUrlGpHosterRest() {
		return urlGpHosterRest;
	}

	public void setUrlGpHosterRest(String urlGpHosterRest) {
		this.urlGpHosterRest = urlGpHosterRest;
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
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * URL to where the REST interface of gp hoster is running
	 */
	String urlGpHosterRest;

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

		serialized.append(getUrlGpHosterRest());
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

			String[] split = propString.split(Pattern.quote(DELIMITER));

			int i = 0;
			setAlias(split[i++]);
			setUrlGpHosterRest(split[i++]);
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

		// i8n
		Text titleInput = new ManualInputOption.Text("Title", true,
				dbServer.getTitle());

		// i8n
		Text hostInput = new ManualInputOption.Text("REST URL", true,
				dbServer.getUrlGpHosterRest());

		// i8n
		Text ftphostInput = new ManualInputOption.Text("FTP Hostname", true,
				dbServer.getFtpHostname());

		// i8n
		Text userInput = new ManualInputOption.Text("Username", false,
				dbServer.getUsername());

		PasswordViewable passwdInput = new ManualInputOption.PasswordViewable(
				"Password", false, dbServer.getPassword());

		// i8n
		Object[] input = MultipleOptionPane.showMultipleInputDialog(
				gpHostingOptionsTab, "DB Connection paramters", titleInput,
				hostInput, ftphostInput, userInput, passwdInput);

		if (input == null) {
			if (newCreated)
				return null;
			else
				return dbServer;
		} else {
			dbServer.setTitle((String) input[0]);
			dbServer.setUrlGpHosterRest((String) input[1]);
			dbServer.setFtpHostname((String) input[2]);
			dbServer.setUsername((String) input[3]);
			dbServer.setPassword(String.valueOf((char[]) input[4]));
		}

		return dbServer;

	}

}
