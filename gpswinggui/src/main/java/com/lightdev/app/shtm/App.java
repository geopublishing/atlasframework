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

import java.util.prefs.Preferences;

import javax.swing.UIManager;

/**
 * Main class of application SimplyHTML.
 * 
 * <p>
 * This class contains method main and opens the application's main frame.
 * </p>
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

public class App {

	// Main method
	public static void main(String[] args) {
		try {
			Preferences prefs = Preferences.userNodeForPackage(Class
					.forName("com.lightdev.app.shtm.PrefsDialog"));
			UIManager.setLookAndFeel(prefs.get(
					PrefsDialog.PREFSID_LOOK_AND_FEEL, UIManager
							.getCrossPlatformLookAndFeelClassName()));
			/*
			 * The following line causes UIManager to correctly handle
			 * alignments of menu items when they do not have an icon.
			 * 
			 * At the Java Developer Connection, SKelvin writes: "If the UI
			 * class does not find an icon it can't calculate its width :-) It
			 * won't work if you just set the property to null (don't ask me
			 * why), but setting to any type other than icon works."
			 * 
			 * (see http://forum.java.sun.com/thread.jsp?forum=57&thread=126150)
			 */
			// UIManager.put("Menu.checkIcon", new ImageIcon("") );
			UIManager
					.put("Menu.checkIcon", UIManager.get("MenuItem.checkIcon"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		FrmMain.run(); // create an instance of the app's main window
	}
}
