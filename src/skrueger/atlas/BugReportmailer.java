/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the AtlasViewer application - A GIS viewer application targeting at end-users with no GIS-experience. Its main purpose is to present the atlases created with the Geopublisher application.
 * http://www.geopublishing.org
 * 
 * AtlasViewer is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.atlas;

import java.awt.Component;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;

import org.apache.log4j.Logger;

import schmitzm.io.IOUtil;
import skrueger.creator.AtlasCreator;
import skrueger.creator.GPProps;

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
				skrueger.creator.GPProps.Keys.bugReportEmail,
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
								+ AtlasViewer
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
			AVUtil.showMessageDialog(owner, AtlasCreator.R(
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
