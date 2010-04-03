/*
 * SimplyHTML, a word processor based on Java, HTML and CSS
 * Copyright (C) 2002 Ulrich Hilger
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

import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;

/**
 * A menu bar for handling of key events coming from its parent SHTMLPanelImpl
 * 
 * @author Dimitri Polivaev
 * @author published under the terms and conditions of the GNU General Public
 *         License, for details see file gpl.txt in the distribution package of
 *         this software
 * 
 * 
 */
public class SHTMLMenuBar extends JMenuBar {

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JMenuBar#processKeyBinding(javax.swing.KeyStroke,
	 * java.awt.event.KeyEvent, int, boolean)
	 */
	public boolean handleKeyBinding(KeyStroke ks, KeyEvent e, int condition,
			boolean pressed) {
		if (condition == JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT) {
			return super.processKeyBinding(ks, e,
					JComponent.WHEN_IN_FOCUSED_WINDOW, pressed);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JMenuBar#processKeyBinding(javax.swing.KeyStroke,
	 * java.awt.event.KeyEvent, int, boolean)
	 */
	@Override
	public boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition,
			boolean pressed) {
		return false;
	}
}
