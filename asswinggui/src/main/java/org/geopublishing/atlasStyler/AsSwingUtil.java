package org.geopublishing.atlasStyler;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.geopublishing.atlasStyler.ASProps.Keys;

import schmitzm.swing.ExceptionDialog;

public class AsSwingUtil extends ASUtil {

	/**
	 * Setting up the logger from a XML configuration file. We do that again in
	 * GPPros, as it outputs log messages first. Does not change the
	 * configuration if there are already appenders defined.
	 */
	public static void initAsLogging() throws FactoryConfigurationError {
		if (Logger.getRootLogger().getAllAppenders().hasMoreElements())
			return;
		DOMConfigurator.configure(ASProps.class
				.getResource("/geopublishing_log4j.xml"));

		Logger.getRootLogger().addAppender(
				Logger.getLogger("dummy").getAppender("asFileLogger"));

		// Apply the LOG level configured in the user-specific application
		// .properties file
		String logLevelStr = ASProps.get(Keys.logLevel);
		if (logLevelStr != null) {
			Logger.getRootLogger().setLevel(Level.toLevel(logLevelStr));
		}

		ExceptionDialog.setMailDestinationAddress("tzeggai@wikisquare.de");
	}
}
