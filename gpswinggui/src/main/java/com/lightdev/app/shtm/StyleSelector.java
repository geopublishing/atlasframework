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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;

/**
 * Component to select styles
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

public class StyleSelector extends JComboBox implements AttributeComponent,
		ChangeListener {
	private SHTMLPanelImpl shtmlPanel;
	/** the CSS attribute key this AttributeComponent object represents */
	private HTML.Attribute key;

	/** indicates whether or not to ignore change events */
	private boolean ignoreChanges = false;

	private String standardStyleName = Util
			.getResourceString("standardStyleName");
	private String paragraphType;
	private boolean updateRunning;

	/**
	 * construct a <code>StyleSelector</code>
	 * 
	 * @param key
	 *            the attribute this component represents
	 */
	public StyleSelector(SHTMLPanelImpl shtmlPanel, HTML.Attribute key) {
		this.key = key;
		this.shtmlPanel = shtmlPanel;
		updateRunning = false;
	}

	/**
	 * set the value of this combo box
	 * 
	 * @param a
	 *            the set of attributes possibly having a font size attribute
	 *            this pick list could display
	 * 
	 * @return true, if the set of attributes had a matching attribute, false if
	 *         not
	 */
	public boolean setValue(AttributeSet a) {
		boolean success = false;
		Object attr = a.getAttribute(key);
		if (attr != null) {
			setSelectedItem(attr.toString());
			success = true;
		} else {
			setSelectedItem(standardStyleName);
		}
		return success;
	}

	/**
	 * get the value of this <code>AttributeComponent</code>
	 * 
	 * @return the value selected from this component
	 */
	public AttributeSet getValue() {
		SimpleAttributeSet set = new SimpleAttributeSet();
		set.addAttribute(key, getSelectedItem());
		return set;
	}

	public AttributeSet getValue(boolean includeUnchanged) {
		return getValue();
	}

	/* --------------- ChangeListener implementation start --------------- */

	/**
	 * this method listens and reacts to changes to either the JTabbedPane of
	 * FrmMain or a given StyleSheet this component was registered with. Once
	 * either one changes the list of styles of this componment is refreshed
	 * accordingly.
	 */
	public void stateChanged(ChangeEvent e) {
		paragraphType = null;
		update();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComboBox#fireActionEvent()
	 */
	@Override
	protected void fireActionEvent() {
		if (updateRunning) {
			return;
		}
		super.fireActionEvent();
	}

	public void update() {
		try {
			updateRunning = true;
			final DocumentPane currentDocumentPane = shtmlPanel
					.getCurrentDocumentPane();
			final int selectionStart = currentDocumentPane.getEditor()
					.getSelectionStart();
			final SHTMLDocument document = (SHTMLDocument) currentDocumentPane
					.getDocument();
			final String newParagraphType = document.getParagraphElement(
					selectionStart, true).getName();
			if (paragraphType == newParagraphType) {
				return;
			}
			paragraphType = newParagraphType;
			Vector styleNames = Util.getStyleNamesForTag((document)
					.getStyleSheet(), paragraphType);
			styleNames.insertElementAt(standardStyleName, 0);
			setModel(new DefaultComboBoxModel(styleNames));
		} catch (NullPointerException ex) {
			setModel(new DefaultComboBoxModel());
		} finally {
			updateRunning = false;
		}
	}

	/* --------------- ChangeListener implementation end ----------------- */
}
