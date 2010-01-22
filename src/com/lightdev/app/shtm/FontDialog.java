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
import java.awt.Container;
import java.awt.Frame;

import javax.swing.text.AttributeSet;

/**
 * Dialog to show and manipulate font attributes.
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

class FontDialog extends DialogShell {

	/** the font panel to use in this dialog */
	private FontPanel fontPanel;

	/**
	 * constructor
	 * 
	 * @param parent
	 *            the main frame having the TextResources
	 * @param title
	 *            the title for this dialog
	 * @param a
	 *            the set of attributes to show and manipulate
	 */
	public FontDialog(Frame parent, String title, AttributeSet a) {
		super(parent, title);

		// construct font panel
		fontPanel = new FontPanel(a, false);

		// add font panel to content pane of DialogShell
		Container contentPane = super.getContentPane();
		contentPane.add(fontPanel, BorderLayout.CENTER);

		// cause optimal placement of all elements
		pack();
	}

	/**
	 * get the set of attributes set in this dialog
	 * 
	 * @return the attributes set in this dialog
	 */
	public AttributeSet getAttributes() {
		return fontPanel.getAttributes();
	}
}
