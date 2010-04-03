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
 * A dialog for showing and manipulating list format.
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

class ListDialog extends DialogShell {

	private ListPanel listPanel;

	public ListDialog(Frame parent, String title) {
		super(parent, title);

		// create a ListPanel and keep a reference for later use
		listPanel = new ListPanel();

		// add to content pane of DialogShell
		Container contentPane = super.getContentPane();
		contentPane.add(listPanel, BorderLayout.CENTER);

		// cause optimal placement of all elements
		pack();
	}

	/**
	 * set the attributes this ListDialog shall represent
	 * 
	 * @param a
	 *            the set of attributes to display list attributes from
	 */
	public void setListAttributes(AttributeSet a) {
		listPanel.setValue(a);
	}

	/**
	 * get the list attributes the ListDialog currently is set to.
	 * 
	 * @return the set of list attributes this ListDialog currently represents
	 */
	public AttributeSet getListAttributes() {
		return listPanel.getValue();
	}

	/**
	 * get the list tag currently selected in this <code>ListDialog</code>
	 * 
	 * @return the list tag currently selected or null, if no list is selected
	 */
	public String getListTag() {
		return listPanel.getListTag();
	}
}
