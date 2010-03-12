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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

/**
 * A dialog to display information about application SimplyHTML.
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

class AboutBox extends JDialog implements ActionListener {

	/** button to close the dialog */
	JButton closeButton = new JButton("Close");

	/** name of the license file */
	private String LICENSE = "resources/gpl.txt";

	/**
	 * construct an <code>AboutBox</code>.
	 * 
	 * @param parent
	 *            the parent frame of the about box
	 */
	public AboutBox(Frame parent) {
		super(parent);
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		closeButton.addActionListener(this);
		closeButton.setText(Util.getResourceString("closeBtnName"));
		constructFrame();
		setTitle(Util.getResourceString("aboutFrameTitle"));
		pack();
	}

	/**
	 * construct the dialog contents
	 */
	private void constructFrame() {
		/** initialize dialog components */
		Container contentPane = getContentPane();
		JPanel infoPane = new JPanel();
		JPanel imagePane = new JPanel();
		JPanel textPane = new JPanel();
		JPanel buttonPane = new JPanel();
		JPanel northPane = new JPanel();
		JPanel emptyPane = new JPanel();
		LicensePane licPane = new LicensePane(new Dimension(650, 200), LICENSE);
		JLabel imageLabel = new JLabel(new ImageIcon(this.getClass()
				.getResource(Util.getResourceString("splashImage"))));
		JLabel emptyLabel = new JLabel("");
		JLabel appTitleLabel = new JLabel(FrmMain.APP_NAME);
		JLabel appStageLabel = new JLabel(FrmMain.VERSION);
		JLabel appCopyrightLabel = new JLabel(
				"Copyright (c) 2002-2008 Ulrich Hilger, Dimitry Polivaev");
		JLabel appHomepageLabel = new JLabel("http://www.lightdev.com");

		/* set the dialog title */
		setTitle("About this application");
		/* highlight the application name with an appropriate font */
		appTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

		/* load the application image into a panel */
		imagePane.setLayout(new FlowLayout());
		imagePane.add(imageLabel);
		imagePane.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

		/**
		 * textPane is the panel where all the application infos are shown.
		 * Infos are shown in a one columns grid of labels, each on one row.
		 */
		textPane.setLayout(new GridLayout(6, 1, 5, 5));
		textPane.add(emptyLabel);
		textPane.add(appTitleLabel);
		textPane.add(appStageLabel);
		textPane.add(appCopyrightLabel);
		textPane.add(appHomepageLabel);

		/**
		 * infoPane shows the application image and the application info text in
		 * a one row, two column grid.
		 */
		infoPane.setLayout(new GridLayout(1, 2, 5, 5));
		infoPane.add(imagePane);
		infoPane.add(textPane);

		/**
		 * northPane is a helper pane to show application image and application
		 * info text left aligned in the upper left corner of the dialog.
		 */
		northPane.setLayout(new BorderLayout());
		northPane.add(infoPane, BorderLayout.WEST);
		northPane.add(emptyPane, BorderLayout.CENTER);

		/* panel for showing the close button at the dialog bottom */
		buttonPane.setLayout(new FlowLayout());
		buttonPane.add(closeButton);

		/**
		 * now put together all parts of above application info and combine them
		 * with license information
		 */
		contentPane.setLayout(new BorderLayout());
		contentPane.add(northPane, BorderLayout.NORTH);
		contentPane.add(licPane, BorderLayout.CENTER);
		contentPane.add(buttonPane, BorderLayout.SOUTH);
	}

	/**
	 * dispose the dialog properly in case of window close events
	 */
	@Override
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			cancel();
		}
		super.processWindowEvent(e);
	}

	/**
	 * dispose the dialog
	 */
	private void cancel() {
		dispose();
	}

	/**
	 * implements the ActionListener interface to be notified of clicks onto the
	 * ok button. Closes and disposes the dialog in this case.
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == closeButton) {
			cancel();
		}
	}

}
