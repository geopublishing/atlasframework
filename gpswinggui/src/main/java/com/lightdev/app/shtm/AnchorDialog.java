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
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.Highlighter;
import javax.swing.text.html.HTML;

/**
 * Dialog to create and edit link anchors.
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

class AnchorDialog extends DialogShell implements ActionListener,
		CaretListener, ListSelectionListener, DocumentListener {

	/** dialog components */
	private JList anchorList;
	private JButton addAnchor;
	private JButton delAnchor;
	private SHTMLEditorPane editor;
	private DocumentPane dp;

	/** the document this dialog was constructed with */
	private Document doc = null;

	/** the URL this document was loaded from (if loaded from this dialog) */
	private URL url = null;

	/** table for document anchors */
	private Hashtable anchorTable = new Hashtable();

	/** indicates whether or not changes to the document need to be saved */
	private boolean needsSaving = true;

	/** the help id for this dialog */
	private static final String helpTopicId = "item165";

	// private int renderMode;

	/**
	 * create an <code>AnchorDialog</code>
	 * 
	 * @param parent
	 *            the parent dialog of this dialog
	 * @param title
	 *            the dialog title
	 * @param doc
	 *            the document to edit anchors of
	 */
	public AnchorDialog(Dialog parent, String title, Document doc) {
		super(parent, title, helpTopicId);
		initDialog(doc, null/* , renderMode */);
	}

	/**
	 * create an <code>AnchorDialog</code>
	 * 
	 * @param parent
	 *            the parent frame of this dialog
	 * @param title
	 *            the dialog title
	 * @param doc
	 *            the document to edit anchors of
	 */
	public AnchorDialog(Frame parent, String title, Document doc) {
		super(parent, title, helpTopicId);
		initDialog(doc, null/* , renderMode */);
	}

	/**
	 * create an <code>AnchorDialog</code>
	 * 
	 * @param parent
	 *            the parent frame of this dialog
	 * @param title
	 *            the dialog title
	 * @param url
	 *            the document url
	 */
	public AnchorDialog(Dialog parent, String title, URL url) {
		super(parent, title, helpTopicId);
		initDialog(null, url/* , renderMode */);
	}

	/**
	 * create an <code>AnchorDialog</code>
	 * 
	 * @param parent
	 *            the parent frame of this dialog
	 * @param title
	 *            the dialog title
	 * @param url
	 *            the document url
	 */
	public AnchorDialog(Frame parent, String title, URL url) {
		super(parent, title, helpTopicId);
		initDialog(null, url/* , renderMode */);
	}

	/**
	 * initialize this <code>AnchorDialog</code>
	 * 
	 * <p>
	 * If a document is passed, anchors of this document are edited. If doc is
	 * null and a url is passed, this document is loaded (and saved).
	 * </p>
	 * 
	 * @param doc
	 *            the document to edit anchors of, or null
	 * @param url
	 *            the url to load a document from, or null
	 */
	private void initDialog(Document doc, URL url) {

		this.url = url;
		// this.renderMode = renderMode;

		// create anchor panel
		JPanel anchorPanel = new JPanel(new BorderLayout());
		anchorPanel.setBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED), Util
				.getResourceString("anchorPanelLabel")));
		// getAnchors(doc);
		anchorList = new JList(/* anchorTable.keySet().toArray() */);
		anchorPanel.add(new JScrollPane(anchorList), BorderLayout.CENTER);
		anchorList.addListSelectionListener(this);
		addAnchor = new JButton(Util.getResourceString("addImgBtnTitle"));
		addAnchor.addActionListener(this);
		delAnchor = new JButton(Util.getResourceString("delImgBtnTitle"));
		delAnchor.addActionListener(this);

		// use a help panel to add add/del buttons
		JPanel helpPanel = new JPanel();
		helpPanel.add(addAnchor);
		helpPanel.add(delAnchor);
		anchorPanel.add(helpPanel, BorderLayout.SOUTH);

		// init DocumentPane
		if (doc != null) {
			needsSaving = false;
			dp = new DocumentPane(/* renderMode */);
			doc.addDocumentListener(this);
			dp.setDocument(doc);
			this.doc = doc;
		} else {
			needsSaving = true;
			dp = new DocumentPane(url, 1/* , renderMode */);
			this.doc = dp.getDocument();
		}

		// init editor to our needs
		editor = dp.getEditor();
		editor.setEditable(false);
		editor.addCaretListener(this);

		// create document panel
		JPanel docPanel = new JPanel(new BorderLayout());
		docPanel
				.setBorder(new TitledBorder(new EtchedBorder(
						EtchedBorder.LOWERED), Util
						.getResourceString("docPanelLabel")));
		docPanel.add(dp, BorderLayout.CENTER);

		// use a help panel to properly align anchorPanel and docPanel
		helpPanel = new JPanel(new BorderLayout());
		helpPanel.add(anchorPanel, BorderLayout.WEST);
		helpPanel.add(docPanel, BorderLayout.CENTER);

		// get content pane of DialogShell to add components to
		Container contentPane = super.getContentPane();
		((JComponent) contentPane).setPreferredSize(new Dimension(600, 500));
		contentPane.add(helpPanel, BorderLayout.CENTER);

		// add help button

		// cause optimal placement of all elements
		pack();

		updateAnchorList();
		addAnchor.setEnabled(false);
		delAnchor.setEnabled(false);
	}

	/**
	 * overridden to addd some custom cleanup upon closing of dialog
	 */
	@Override
	public void dispose() {
		editor.removeCaretListener(this);
		doc.removeDocumentListener(this);
		super.dispose();
	}

	/**
	 * re-display the list of anchors of the document
	 */
	private void updateAnchorList() {
		getAnchors(doc);
		anchorList.setListData(anchorTable.keySet().toArray());
	}

	/**
	 * get the anchors of a given document
	 * 
	 * @param doc
	 *            the document to get anchors from
	 */
	private void getAnchors(Document doc) {
		String aTag = HTML.Tag.A.toString();
		Object nameAttr;
		Object link;
		anchorTable.clear();
		ElementIterator eli = new ElementIterator(doc);
		Element elem = eli.first();
		while (elem != null) {
			link = elem.getAttributes().getAttribute(HTML.Tag.A);
			if (link != null) {
				nameAttr = ((AttributeSet) link)
						.getAttribute(HTML.Attribute.NAME);
				if (nameAttr != null) {
					// model.addElement(nameAttr);
					anchorTable.put(nameAttr, elem);
				}
			}
			elem = eli.next();
		}
	}

	/**
	 * get an anchor name and add it at the current editor location
	 */
	private void doAddAnchor() {
		String anchorName = Util.nameInput(null, "", ".*", "addAnchorTitle",
				"addAnchorText");
		if (anchorName != null) {
			editor.insertAnchor(anchorName);
			saveChanges();
			updateAnchorList();
		}
	}

	/**
	 * save changes to the document
	 */
	private void saveChanges() {
		if (needsSaving) {
			if (url != null) {
				try {
					dp.saveDocument(/* renderMode */);
				} catch (Exception e) {
					Util.errMsg(this, e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * get the anchor currently selected in the list of anchors
	 * 
	 * @return the anchor name, or null if none is selected
	 */
	public String getAnchor() {
		String anchorName = null;
		if (anchorList.getSelectedIndex() > -1) {
			anchorName = anchorList.getSelectedValue().toString();
		}
		return anchorName;
	}

	/**
	 * remove an anchor from the document
	 */
	private void doDelAnchor() {
		String anchorName = anchorList.getSelectedValue().toString();
		dp.getEditor().removeAnchor(anchorName);
		saveChanges();
		updateAnchorList();
	}

	/**
	 * ActionListener implementatin for proper handling of buttons
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src.equals(addAnchor)) {
			doAddAnchor();
		} else if (src.equals(delAnchor)) {
			doDelAnchor();
		} else {
			super.actionPerformed(e);
		}
	}

	/**
	 * ListSelectionListener implementation to properly react to changes in the
	 * list of anchors
	 */
	public void valueChanged(ListSelectionEvent e) {
		Object src = e.getSource();
		if (src.equals(anchorList)) {
			// if(!ignoreAnchorListChanges) {
			Highlighter h = editor.getHighlighter();
			Highlighter.HighlightPainter p = new DefaultHighlighter.DefaultHighlightPainter(
					Color.yellow);
			Object o = anchorList.getSelectedValue();
			if (o != null) {
				Element elem = (Element) anchorTable.get(anchorList
						.getSelectedValue());
				int start = elem.getStartOffset();
				int end = elem.getEndOffset();
				try {
					if (end == start) {
						end = start + 3;
					}
					editor.select(start, end);
					h.removeAllHighlights();
					h.addHighlight(start, end, p);
				} catch (BadLocationException ble) {
					ble.printStackTrace();
				}
			}
			// }
		}
	}

	/**
	 * CaretListener implementation to adjust 'add anchor' button according to
	 * whether or not a selection is present in the document to possibly add an
	 * anchor to
	 */
	public void caretUpdate(CaretEvent e) {
		Object src = e.getSource();
		if (src.equals(editor)) {
			addAnchor.setEnabled(editor.getSelectionStart() != editor
					.getSelectionEnd());
			delAnchor.setEnabled(anchorList.getSelectedIndex() > -1);
		}
	}

	/* -------- DocumentListener implementation start ------------ */

	/**
	 * listens to inserts into the document to track whether or not the document
	 * needs to be saved.
	 */
	public void insertUpdate(DocumentEvent e) {
		updateAnchorList();
	}

	/**
	 * listens to removes into the document to track whether or not the document
	 * needs to be saved.
	 */
	public void removeUpdate(DocumentEvent e) {
		updateAnchorList();
	}

	/**
	 * listens to changes on the document to track whether or not the document
	 * needs to be saved.
	 */
	public void changedUpdate(DocumentEvent e) {
		updateAnchorList();
	}

	/* -------- DocumentListener implementation end ------------ */
}
