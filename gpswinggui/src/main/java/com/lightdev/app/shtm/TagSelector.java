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

import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.text.html.HTML;

/**
 * Component to select a tag
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

public class TagSelector extends JComboBox {

	/** table with available tags to select */
	private Vector tags = new Vector();

	/** table with tag names corresponding to tags */
	private Vector tagNames = new Vector();

	/**
	 * construct a new TagSelector
	 */
	public TagSelector() {
		super();
		initTags();
		setModel(new DefaultComboBoxModel(tagNames));
	}

	/**
	 * get the name of the tag that is currently selected
	 * 
	 * @return the tag name
	 */
	public String getSelectedTag() {
		return (String) tags.elementAt(getSelectedIndex());
	}

	/**
	 * get the list of tags selectable through this component
	 * 
	 * @return a Vector of tags available from this component
	 */
	public Vector getTags() {
		return tags;
	}

	/**
	 * set the tag that is to be shown in this component
	 * 
	 * @param tag
	 *            the name of the tag to show
	 */
	public void setSelectedTag(String tag) {
		int index = tags.indexOf(tag);
		if (index > -1) {
			setSelectedIndex(tags.indexOf(tag));
		} else {
			setSelectedIndex(0);
		}
	}

	/**
	 * initialize content types hashtable
	 */
	private void initTags() {
		tags.addElement(HTML.Tag.P.toString());
		tags.addElement(HTML.Tag.H1.toString());
		tags.addElement(HTML.Tag.H2.toString());
		tags.addElement(HTML.Tag.H3.toString());
		tags.addElement(HTML.Tag.H4.toString());
		tags.addElement(HTML.Tag.H5.toString());
		tags.addElement(HTML.Tag.H6.toString());

		tagNames.addElement(Util.getResourceString("cTagNamePara"));
		tagNames.addElement(Util.getResourceString("cTagNameHead1"));
		tagNames.addElement(Util.getResourceString("cTagNameHead2"));
		tagNames.addElement(Util.getResourceString("cTagNameHead3"));
		tagNames.addElement(Util.getResourceString("cTagNameHead4"));
		tagNames.addElement(Util.getResourceString("cTagNameHead5"));
		tagNames.addElement(Util.getResourceString("cTagNameHead6"));
	}

}
