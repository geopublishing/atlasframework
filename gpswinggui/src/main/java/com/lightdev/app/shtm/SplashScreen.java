/*
 * SimplyHTML, a word processor based on Java, HTML and CSS
 * Copyright (C) 2002 Ulrich Hilger
 * Copyright (C) 2006 Karsten Pawlik
 * Copyright (C) 2006 Dimitri Polivaev
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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;

/**
 * Class that displays a splash screen Is run in a separate thread so that the
 * applet continues to load in the background
 * 
 * @author Karsten Pawlik
 * 
 */
public class SplashScreen extends JWindow {
	private static SplashScreen instance = null;
	private static int counter;

	private SplashScreen() {
		try {
			JPanel panel = new JPanel(new BorderLayout());
			ImageIcon icon = new ImageIcon(SplashScreen.class.getResource(Util
					.getResourceString("splashImage")));
			panel.add(new JLabel(icon), BorderLayout.CENTER);
			panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			getContentPane().add(panel);
			getRootPane().setOpaque(true);
			pack();
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			setLocation((int) (d.getWidth() - getWidth()) / 2, (int) (d
					.getHeight() - getHeight()) / 2);
		} catch (Exception e) {
		}
	}

	/**
	 * Hides the splash screen.
	 */
	synchronized public static void hideInstance() {
		if (!Util.getPreference("show_splash_screen", "true").equalsIgnoreCase(
				"true"))
			return;
		if (counter > 0)
			counter--;
		if (counter == 0)
			instance.setVisible(false);
	}

	/**
	 * Shows the splash screen.
	 */
	synchronized public static void showInstance() {
		if (!Util.getPreference("show_splash_screen", "true").equalsIgnoreCase(
				"true"))
			return;

		if (instance == null) {
			instance = new SplashScreen();
			counter = 0;
		}
		if (counter == 0) {
			instance.setVisible(true);
			instance.getRootPane().paintImmediately(0, 0, instance.getWidth(),
					instance.getHeight());
		}
		counter++;
	}

}
