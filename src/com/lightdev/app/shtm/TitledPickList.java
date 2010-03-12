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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.EventObject;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A pick list typically being used in font dialogs, consisting of a list title,
 * a text field for the currently selected value and the actual pick list
 * containing all possible values.
 * 
 * As three different lists are needed in our font panel for family, style and
 * size, its quite handy to have the code making up such a component in a
 * separate class only once.
 * 
 * As well in a separate class it is easier to implement the special
 * 'behaviour', i.e. when list is clicked, the value of the text field is set
 * accordingly and when a value is typed in the text field, the value in the
 * list changes accordingly.
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
class TitledPickList extends JPanel implements ListSelectionListener,
		CaretListener, FocusListener, KeyListener {
	/** the chosen list entry */
	private JTextField choice;

	boolean ignoreTextChanges = false;

	/** the list having all possible entries */
	private JList optionsList;

	/**
	 * constructor
	 * 
	 * @param options
	 *            the options to be selectable in this list
	 * @param titleText
	 *            the title for the pick list
	 */
	public TitledPickList(String[] options, String titleText) {
		super(new BorderLayout());

		choice = new JTextField();
		choice.addCaretListener(this);
		choice.addFocusListener(this);
		choice.addKeyListener(this);

		optionsList = new JList(options);
		optionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		optionsList.addListSelectionListener(this);

		JScrollPane scrollableList = new JScrollPane(optionsList);

		JPanel pickListPanel = new JPanel(new BorderLayout());
		pickListPanel.add(choice, BorderLayout.NORTH);
		pickListPanel.add(scrollableList, BorderLayout.CENTER);

		JLabel title = new JLabel();
		title.setText(titleText);

		add(title, BorderLayout.NORTH);
		add(pickListPanel, BorderLayout.CENTER);
	}

	/**
	 * if the caret was updated, i.e. the user typed into the text field, try to
	 * find a font family name starting with what was typed so far, set the list
	 * to that family
	 */
	public void caretUpdate(CaretEvent ce) {
		if (!ignoreTextChanges) {
			if (choice.hasFocus()) {
				ListModel model = optionsList.getModel();
				String key = choice.getText().toLowerCase();
				if (key != null) {
					int i = 0;
					int modelSize = model.getSize();
					String listEntry = (String) model.getElementAt(i);
					while (++i < modelSize
							&& !listEntry.toLowerCase().startsWith(key)) {
						listEntry = (String) model.getElementAt(i);
					}
					if (i < modelSize) {
						optionsList.setSelectedValue(listEntry, true);
					}
				}
			}
		}
	}

	/** for FocusListener implementation, but unused here */
	public void focusGained(FocusEvent e) {
	}

	/**
	 * put the currently selected value in the list into the text field when
	 * user leaves field
	 */
	public void focusLost(FocusEvent e) {
		updateTextFromList();
	}

	/**
	 * put the currently selected value in the list into the text field when
	 * user pressed enter in field
	 */
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			updateTextFromList();
		}
	}

	/** for KeyListener implementation, but unused here */
	public void keyReleased(KeyEvent e) {
	}

	/** for KeyListener implementation, but unused here */
	public void keyTyped(KeyEvent e) {
	}

	/** put selected value into text field */
	private void updateTextFromList() {
		Object value = optionsList.getSelectedValue();
		if (value != null) {
			choice.setText(value.toString());
		}
	}

	/**
	 * get the value selected from the pick list
	 * 
	 * @return the selected value or null if nothing is selected
	 */
	public Object getSelection() {
		return optionsList.getSelectedValue();
	}

	/**
	 * set the value selected in the pick list
	 * 
	 * @param value
	 *            the value to be selected in the list
	 */
	public void setSelection(Object value) {
		optionsList.setSelectedValue(value.toString(), true);
		updateTextFromList();
	}

	/**
	 * set the selected index in the pick list
	 * 
	 * @param index
	 *            the index of the value to be selected in the list
	 */
	public void setSelection(int index) {
		optionsList.setSelectedIndex(index);
		updateTextFromList();
	}

	/**
	 * get the index of the value selected in the pick list
	 * 
	 * @return the index of the selected value or -1 if none is selected
	 */
	public int getIndex() {
		return optionsList.getSelectedIndex();
	}

	/**
	 * if another value was picked from the list, set the textfield according to
	 * the choice and update the sample
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (optionsList.hasFocus()) {
			updateTextFromList();
		}
		fireValueChanged();
	}

	/* ------------- event handling start ------------ */

	/** the listeners for TitledPickkListEvents */
	private Vector listeners = new Vector(0);

	/**
	 * add an event listener.
	 * 
	 * @param listener
	 *            the event listener to add
	 */
	public void addTitledPickListListener(TitledPickListListener listener) {
		listeners.addElement(listener);
	}

	/**
	 * remove an event listener.
	 * 
	 * @param listener
	 *            the event listener to remove
	 */
	public void removeTitledPickListListener(TitledPickListListener listener) {
		listeners.removeElement(listener);
	}

	/** fire a value changed event to all registered listeners */
	void fireValueChanged() {
		Enumeration listenerList = listeners.elements();
		while (listenerList.hasMoreElements()) {
			((TitledPickListListener) listenerList.nextElement())
					.valueChanged(new TitledPickListEvent(this));
		}
	}

	/** the event object definition for ColorPanels */
	class TitledPickListEvent extends EventObject {
		public TitledPickListEvent(Object source) {
			super(source);
		}
	}

	/** the event listener definition for ColorPanels */
	interface TitledPickListListener extends EventListener {
		public void valueChanged(TitledPickListEvent e);
	}

	/* ------------- event handling end ------------ */

}
