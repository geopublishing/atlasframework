/*
 * SimplyHTML, a word processor based on Java, HTML and CSS
 * Copyright (C) 2002 Ulrich Hilger
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.lightdev.app.shtm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

/**
 * A panel for displaying license information of application SimplyHTML.
 * 
 * @author Ulrich Hilger
 * @author Light Development
 * @author <a href="http://www.lightdev.com">http://www.lightdev.com</a>
 * @author <a href="mailto:info@lightdev.com">info@lightdev.com</a>
 * @author published under the terms and conditions of the GNU General Public
 *         License, for details see file gpl.txt in the distribution package of
 *         this software
 * 
 * 
 */

class LicensePane extends JPanel {

	/* line separator character (sequence) */
	private String lineSeparator = System.getProperty("line.separator");

	/**
	 * construct a <code>LicensePane</code>
	 */
	public LicensePane(Dimension d, String licensePath) {
		/* create a text area to show the license text in */
		JTextArea licText = new JTextArea(getLicenseText(getClass()
				.getResourceAsStream(licensePath)));
		licText.setEditable(false);
		licText.setFont(new Font("Courier", Font.PLAIN, 12));

		/* create a scroll pane as the license text is long */
		JScrollPane licPane = new JScrollPane();
		licPane = new JScrollPane(licText);
		licPane.setPreferredSize(d);
		licPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		licPane
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		/* add the scroll pane to this panel */
		setLayout(new BorderLayout());
		add(licPane, BorderLayout.CENTER);
		licText.setCaretPosition(0); // go to top of text (for JRE versions
		// earlier than 1.4)
	}

	/**
	 * read and return the license text
	 * 
	 * @return the license text
	 */
	private String getLicenseText(InputStream is) {
		StringBuffer license = new StringBuffer();
		try {
			// InputStream is = getClass().getResourceAsStream(getLicense());
			BufferedReader r = new BufferedReader(new InputStreamReader(is));
			String buf = r.readLine();
			while (buf != null) {
				license.append(buf);
				license.append(lineSeparator);
				buf = r.readLine();
			}
			r.close();
			is.close();
		} catch (Exception e) {
			Util
					.errMsg(
							this,
							"The license text could not be opened.\n\nPlease consult file 'readme.txt' for installation guidelines\n\nSimplyHTML and all of its parts are distributed under\nthe terms and conditions of the GNU General Public License (GPL).\nYou may want to obtain a free and complete distribution package at\nhttp://www.lightdev.com",
							e);
		}
		return license.toString();
	}
}
