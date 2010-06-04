/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.swing;

import java.awt.Component;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AVUtil;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geopublishing.geopublisher.GPProps;
import org.geopublishing.geopublisher.GpUtil;

import schmitzm.io.IOUtil;

/**
 * This class handles the preparation and sending of bug reports by mail. Its
 * abstract, and different subclasses exists for AV and GP
 * 
 * @author Stefan A. Krueger
 * 
 */
public abstract class BugReportmailer {

	/**
	 * When sending a bug report by mail, we can only send this number of bytes
	 * of the stacktrace in the email. Less than 5000 is more or less useless
	 */
	private static final int maxStrackTraceSize = 5000;

	private static final Logger LOGGER = Logger
			.getLogger(BugReportmailer.class);

	private String bugreportEmail;

	protected String logFileLocation;

	/**
	 * @param logfileName
	 *            e.g geopublisher.log or atlas.log
	 */
	public BugReportmailer(String logfileName) {

		this.bugreportEmail = GPProps.get(
				GPProps.Keys.bugReportEmail,
				"skpublic@wikisquare.de");

		LOGGER.debug("Trying to send a bugreport by mail...");

		this.logFileLocation = new File(IOUtil.getTempDir(), logfileName)
				.getAbsolutePath();

		logFileLocation = logFileLocation.replace("\\", "\\\\");
	}

	public void send(Component owner) {
		URI uriMailTo = null;

		try {

			if (Desktop.isDesktopSupported()
					&& Desktop.getDesktop().isSupported(Desktop.Action.MAIL)) {

				uriMailTo = new URI(
						"mailto",
						bugreportEmail
								+ "?SUBJECT="
								+ getSubject()
								+ "&BODY="
								+ getBody()
								+ "\n"
								+ System.getProperty("java.version")
								+ "\n"
								+ AVUtil
										.R("SendLogToAuthor.Email.Body.StacktraceHeader")
								+ getLogfileLines(), null);

				Desktop.getDesktop().mail(uriMailTo);
			}

		} catch (Exception eee) {
			LOGGER.warn("Second try to send the mail with a stacktrace of "
					+ maxStrackTraceSize + " bytes failed.", eee);
			if (Desktop.isDesktopSupported())
				try {

					/**
					 * Second try.. Send without the stacktrace inside the email
					 */
					uriMailTo = new URI("mailto", bugreportEmail + "?SUBJECT="
							+ getSubject() + "&BODY=" + getBody(), null);
					Desktop.getDesktop().mail(uriMailTo);
				} catch (Exception e2) {
					LOGGER
							.error("Second try to send the mail without a stacktrace failed also.");
				}

		} finally {
			AVSwingUtil.showMessageDialog(owner, GpUtil.R(
					"SendLogToAuthor.Msg", logFileLocation, bugreportEmail));
		}

	}

	protected String getLogfileLines() {
		try {
			FileReader fileReader = new FileReader(logFileLocation);

			BufferedReader input = new BufferedReader(fileReader);
			try {

				StringBuffer buffer = new StringBuffer();
				String line = null;
				while (input.ready() && (line = input.readLine()) != null) {
					buffer.insert(0, "\n" + line);
				}
				String logfileString = buffer.toString().trim();

				if (logfileString.length() > maxStrackTraceSize) {
					logfileString = logfileString.substring(0,
							maxStrackTraceSize);
				}

				return logfileString;

			} finally {
				input.close();
			}

		} catch (Exception ee) {
			return "no log found:" + ee.getLocalizedMessage();
		}

	}

	protected abstract String getBody();

	protected abstract String getSubject();

}
