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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleContext;
import javax.swing.text.html.HTML;
import javax.swing.text.html.StyleSheet;

/**
 * Dialog to set paragraph attributes and to manipulate styles in a given style
 * sheet.
 * 
 * <p>
 * In stage 9 this has an additional combo box to select different element types
 * in MODE_NAMED_STYLES.
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

class ParaStyleDialog extends DialogShell implements AttributeComponent,
		ActionListener, ListSelectionListener, ChangeListener {

	private String standardStyleName = Util
			.getResourceString("standardStyleName");

	/** mode to edit named styles with this dialog */
	private static int MODE_NAMED_STYLES = 1;

	/** mode to set a paragraph style with this dialog */
	private static int MODE_PARAGRAPH_STYLE = 2;

	/** button to save a named style */
	private JButton saveStyleBtn;

	/** button to save a named style under a different name */
	private JButton saveStyleAsBtn;

	/** button to delete a named style */
	private JButton deleteStyleBtn;

	/** the mode this dialog was created in */
	private int mode;

	/** the AttributeComponents in this dialog */
	private Vector components = new Vector();

	/** the FontPanel for the paragraph font settings */
	private FontPanel fp;

	/** list of styles available in style sheet */
	private JList styleList;

	/** style sheet to use in MODE_NAMED_STYLES */
	private StyleSheet styles;

	/** the document this dialog is operating on when in MODE_NAMED_STYLES */
	private Document doc;

	/** set of attributes for mapping discrepancies between HTML and Java */
	private AttributeSet mapSet;

	/**
	 * panel for setting paragraph styles (needed in the change listener of the
	 * list of named styles)
	 */
	private StylePanel sp;

	/**
	 * panel for setting margins (needed in the change listener of the list of
	 * named styles)
	 */
	private MarginPanel mp;

	/** table to map between HTML tags and 'content types' */
	static private NamedObject[] cTypes = null;

	/** selector for content type */
	private JComboBox cType;

	/**
	 * create a <code>ParaStyleDialog</code> to manipulate the format of a
	 * paragraph
	 * 
	 * @param parent
	 *            the parent frame of this dialog
	 * @param title
	 *            the text to be shown as title for this dialog
	 */
	public ParaStyleDialog(Frame parent, String title) {
		this(parent, title, null, MODE_PARAGRAPH_STYLE);
	}

	/**
	 * create a <code>ParaStyleDialog</code> to edit named styles of a given
	 * document
	 * 
	 * @param parent
	 *            the parent frame of this dialog
	 * @param title
	 *            the text to be shown as title for this dialog
	 * @param doc
	 *            the document having the style sheet to edit named styles from
	 */
	public ParaStyleDialog(Frame parent, String title, Document doc) {
		this(parent, title, doc, MODE_NAMED_STYLES);
	}

	/**
	 * construct a <code>ParaStyleDialog</code>
	 * 
	 * @param parent
	 *            the parent frame for this dialog
	 * @param title
	 *            the text to be shown as title for this dialog
	 * @param mode
	 *            the mode this dialog is to be created, one of
	 *            MODE_NAMED_STYLES or MODE_PARAGRAPH_STYLE
	 */
	private ParaStyleDialog(Frame parent, String title, Document doc, int mode) {
		super(parent, title);

		JPanel hPanel = null;

		this.mode = mode;
		this.doc = doc;

		// get content pane of DialogShell to add components to
		Container contentPane = super.getContentPane();

		// construct tabbed pane for the various groups of settings
		JTabbedPane tp = new JTabbedPane();
		tp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		// create style panel
		sp = new StylePanel(StylePanel.TYPE_PARAGRAPH);
		sp.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
				Util.getResourceString("cellGenTabLabel")));
		components.add(sp);

		// create margin panel
		mp = new MarginPanel();
		components.add(mp);
		mp.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
				Util.getResourceString("cellMarginTabLabel")));

		if (mode == MODE_NAMED_STYLES) {
			styles = ((SHTMLDocument) doc).getStyleSheet();

			// create a combo box for content type
			initContentTypes();
			cType = new JComboBox(cTypes);
			cType.addActionListener(this);

			// create a list of styles
			// Vector styleNames = Util.getStyleNamesForTag(styles,
			// getContentType());
			// styleNames.insertElementAt(standardStyleName, 0);
			styleList = new JList(/* new DefaultComboBoxModel(styleNames) */);
			updateStyleList();
			styles.addChangeListener(this);
			styleList.addListSelectionListener(this);

			// create a panel to control the styles
			JPanel btnPanel = new JPanel(new GridLayout(3, 1, 5, 5));
			saveStyleBtn = new JButton(Util
					.getResourceString("saveStyleButtonLabel"));
			saveStyleBtn.addActionListener(this);
			saveStyleBtn.setEnabled(false);
			saveStyleAsBtn = new JButton(Util
					.getResourceString("saveStyleAsButtonLabel"));
			saveStyleAsBtn.addActionListener(this);
			deleteStyleBtn = new JButton(Util
					.getResourceString("deleteStyleButtonLabel"));
			deleteStyleBtn.addActionListener(this);
			deleteStyleBtn.setEnabled(false);
			btnPanel.add(saveStyleBtn);
			btnPanel.add(saveStyleAsBtn);
			btnPanel.add(deleteStyleBtn);

			// use a helper panel for placement of buttons
			hPanel = new JPanel(new BorderLayout());
			hPanel.add(btnPanel, BorderLayout.NORTH);

			// create named styles panel
			JPanel nsPanel = new JPanel(new BorderLayout(5, 5));
			nsPanel.add(cType, BorderLayout.NORTH);
			nsPanel.add(new JScrollPane(styleList), BorderLayout.CENTER);
			nsPanel.add(hPanel, BorderLayout.EAST);
			nsPanel.setBorder(new TitledBorder(new EtchedBorder(
					EtchedBorder.LOWERED), Util
					.getResourceString("stylePanelLabel")));
			nsPanel.setVisible(mode == MODE_NAMED_STYLES);

			// use a helper panel for placement of style and named styles panels
			hPanel = new JPanel(new BorderLayout());
			hPanel.add(sp, BorderLayout.NORTH);
			hPanel.add(nsPanel, BorderLayout.CENTER);

			okButton.setText(Util.getResourceString("closeLabel"));
		} else {
			hPanel = new JPanel(new BorderLayout());
			hPanel.add(sp, BorderLayout.NORTH);
		}

		// create paragraph panel
		JPanel paraPanel = new JPanel(new BorderLayout());
		paraPanel.add(hPanel, BorderLayout.CENTER);
		paraPanel.add(mp, BorderLayout.EAST);

		// add paragraph panel to tabbed pane
		tp.add(Util.getResourceString("paraTabLabel"), paraPanel);

		// create font panel and add to tabbed pane
		fp = new FontPanel(true);

		// add tabbed pane to content pane of dialog
		contentPane.add(tp, BorderLayout.CENTER);

		cancelButton.setVisible(mode != MODE_NAMED_STYLES);
		tp.add(Util.getResourceString("fontTabLabel"), fp);

		// cause optimal placement of all elements
		pack();
	}

	/**
	 * update the list of available styles for the currently selected tag
	 */
	private void updateStyleList() {
		Vector styleNames = Util.getStyleNamesForTag(styles, getContentType());
		styleNames.insertElementAt(standardStyleName, 0);
		styleList.setModel(new DefaultComboBoxModel(styleNames));
	}

	/**
	 * initialize content types hashtable
	 */
	private void initContentTypes() {
		cTypes = new NamedObject[10];
		int i = 0;
		cTypes[i++] = new NamedObject(HTML.Tag.P.toString(), Util
				.getResourceString("cTagNamePara"));
		cTypes[i++] = new NamedObject(HTML.Tag.H1.toString(), Util
				.getResourceString("cTagNameHead1"));
		cTypes[i++] = new NamedObject(HTML.Tag.H2.toString(), Util
				.getResourceString("cTagNameHead2"));
		cTypes[i++] = new NamedObject(HTML.Tag.H3.toString(), Util
				.getResourceString("cTagNameHead3"));
		cTypes[i++] = new NamedObject(HTML.Tag.H4.toString(), Util
				.getResourceString("cTagNameHead4"));
		cTypes[i++] = new NamedObject(HTML.Tag.H5.toString(), Util
				.getResourceString("cTagNameHead5"));
		cTypes[i++] = new NamedObject(HTML.Tag.H6.toString(), Util
				.getResourceString("cTagNameHead6"));
		cTypes[i++] = new NamedObject(HTML.Tag.A.toString(), Util
				.getResourceString("cTagNameLink"));
		cTypes[i++] = new NamedObject(HTML.Tag.UL.toString(), Util
				.getResourceString("cTagNameUL"));
		cTypes[i++] = new NamedObject(HTML.Tag.OL.toString(), Util
				.getResourceString("cTagNameOL"));
	}

	/**
	 * get the currently selected tag
	 * 
	 * @return the tag name currently selected
	 */
	private String getContentType() {
		return ((NamedObject) cType.getSelectedItem()).getObject().toString();
	}

	/**
	 * get the value of this <code>AttributeComponent</code>
	 * 
	 * @return the value selected from this component
	 */
	public AttributeSet getValue() {
		SimpleAttributeSet attributes = new SimpleAttributeSet();
		Enumeration elements = components.elements();
		AttributeComponent ac;
		while (elements.hasMoreElements()) {
			ac = (AttributeComponent) elements.nextElement();
			attributes.addAttributes(ac.getValue());
		}
		attributes.addAttributes(fp.getAttributes());
		return attributes;
	}

	public AttributeSet getValue(boolean includeUnchanged) {
		if (includeUnchanged) {
			SimpleAttributeSet attributes = new SimpleAttributeSet();
			Enumeration elements = components.elements();
			AttributeComponent ac;
			while (elements.hasMoreElements()) {
				ac = (AttributeComponent) elements.nextElement();
				attributes.addAttributes(ac.getValue(includeUnchanged));
			}
			attributes.addAttributes(fp.getAttributes(includeUnchanged));
			return attributes;
		} else {
			return getValue();
		}
	}

	/**
	 * set the value of this <code>AttributeComponent</code>
	 * 
	 * @param a
	 *            the set of attributes possibly having an attribute this
	 *            component can display
	 * 
	 * @return true, if the set of attributes had a matching attribute, false if
	 *         not
	 */
	public boolean setValue(AttributeSet a) {
		boolean result = true;

		/*
		 * System.out.println("\r\n"); de.calcom.cclib.html.HTMLDiag hd = new
		 * de.calcom.cclib.html.HTMLDiag(); hd.listAttributes(a, 4);
		 */

		AttributeSet set = Util.resolveAttributes(a);
		Enumeration elements = components.elements();
		AttributeComponent ac;
		while (elements.hasMoreElements()) {
			ac = (AttributeComponent) elements.nextElement();
			if (!ac.setValue(set)) {
				result = false;
			}
		}
		fp.setAttributes(set);
		return result;
	}

	/**
	 * listen to changes of style list, switch state of save and delete buttons
	 * accordingly and set dialog to the selected style, if any
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource().equals(styleList)) {
			int selectedStyleNo = styleList.getSelectedIndex();
			boolean styleSelected = selectedStyleNo > -1;
			saveStyleBtn.setEnabled(styleSelected);
			deleteStyleBtn.setEnabled(styleSelected);
			if (styleSelected) {
				// set dialog contents to selected style
				sp.reset();
				fp.reset();
				mp.reset();
				String styleName;
				String className = styleList.getSelectedValue().toString();
				if (className.equalsIgnoreCase(standardStyleName)) {
					styleName = getContentType();
				} else {
					styleName = getContentType() + Util.CLASS_SEPARATOR
							+ className;
				}
				// Style style = styles.getStyle(styleName);
				AttributeSet style = styles.getStyle(styleName);
				if (style == null) {
					style = new SimpleAttributeSet();
				}
				MutableAttributeSet allStyles = (MutableAttributeSet) SHTMLPanelImpl
						.getMaxAttributes(((SHTMLDocument) doc)
								.getCharacterElement(doc.getEndPosition()
										.getOffset()), ((SHTMLDocument) doc)
								.getStyleSheet());
				allStyles.addAttributes(style);
				// mapSet = new
				// AttributeMapper(Util.resolveAttributes(style)).getMappedAttributes(AttributeMapper.toJava);
				// setValue(style);
				setValue(allStyles);
			}
		}
	}

	/**
	 * get the style name currently selected in the list of style names
	 * 
	 * @return the name of the style currently selected in the list of style
	 *         names or null if none is currently selected
	 */
	private String getSelectedStyleName() {
		String styleName = null;
		if (styleList.getSelectedIndex() > -1) {
			styleName = styleList.getSelectedValue().toString();
		}
		return styleName;
	}

	/**
	 * save the current settings on this <code>ParaStyleDialog</code> to its
	 * associated style sheet under the name currently selected in the list of
	 * named styles.
	 * 
	 * <p>
	 * This will overwrite the existing style with the current settings on this
	 * dialog.
	 * </p>
	 */
	private void doSaveStyle() {
		String styleName = getSelectedStyleName();
		if (styleName != null) {
			saveStyleAs(styleName);
		}
	}

	/**
	 * save the current settings on this <code>ParaStyleDialog</code> to its
	 * associated style sheet under a name defined by the user.
	 * 
	 * <p>
	 * This will ask for a name a style shall be saved under. If the name
	 * exists, the user is prompted whether or not it shall be overwritten. The
	 * sytle is saved according to the user's choices.
	 * </p>
	 */
	private void doSaveStyleAs() {
		String initialName = getSelectedStyleName();
		if (initialName == null) {
			initialName = Util.getResourceString("newStyleDefaultName");
		}
		String newStyleName = Util.nameInput(null, initialName, "\\w[\\w ]*",
				"styleNameInputTitle", "styleNameInputText").trim();
		if (newStyleName != null) {
			if (styleNameExists(newStyleName)
					|| newStyleName.equalsIgnoreCase(standardStyleName)) {
				if (Util.msg(JOptionPane.YES_NO_OPTION, "confirmSaveAs",
						"fileExistsQuery", newStyleName, " ")) {
					saveStyleAs(newStyleName);
				}
			} else {
				saveStyleAs(newStyleName);
			}
		}
	}

	/**
	 * delete the currently selected style name for the currently selected tag
	 */
	private void doDeleteStyle() {
		String styleName = getSelectedStyleName();
		if (styleName != null) {
			if (Util.msg(JOptionPane.YES_NO_OPTION, "confirmDelete",
					"deleteStyleQuery", styleName, "\r\n\r\n")) {
				styles.removeStyle(getContentType() + Util.CLASS_SEPARATOR
						+ styleName);
			}
		}
	}

	/**
	 * save a style under a given name
	 * 
	 * @param newStyleName
	 *            the name the style has to be saved under
	 */
	private void saveStyleAs(String newStyleName) {
		try {
			String className = getContentType();
			if (!newStyleName.equalsIgnoreCase(standardStyleName)) {
				className = className + Util.CLASS_SEPARATOR + newStyleName;
			}
			StringWriter sw = new StringWriter();
			CSSWriter cw = new CSSWriter(sw, null);
			SimpleAttributeSet a = new SimpleAttributeSet();
			if (mapSet != null) {
				a.addAttributes(mapSet);
			}

			/*
			 * AttributeSet test = getValue(true); de.calcom.cclib.html.HTMLDiag
			 * hd = new de.calcom.cclib.html.HTMLDiag(); hd.listAttributes(test,
			 * 4); System.out.println(" \r\n");
			 */

			a.addAttributes(new AttributeMapper(getValue(true))
					.getMappedAttributes(AttributeMapper.toCSS));

			// hd.listAttributes(a, 4);

			cw.writeRule(className, a);
			String ruleStr = sw.getBuffer().toString();
			styles.removeStyle(className);
			styles.addRule(ruleStr);
			if (doc != null) {
				SHTMLDocument sd = (SHTMLDocument) doc;
				if (!sd.hasStyleRef()) {
					sd.insertStyleRef();
				}
			}
		} catch (Exception ex) {
			Util.errMsg(this, ex.getMessage(), ex);
		}
	}

	/**
	 * get the style sheet this dialog uses
	 * 
	 * @return the used style sheet
	 */
	public StyleSheet getStyleSheet() {
		return styles;
	}

	/**
	 * check whether or not a named style already exists in the style sheet
	 * associated to this dialog
	 * 
	 * @param styleName
	 *            the name of the style to be looked for
	 * 
	 * @return true, if the given style name alread is used in the style sheet,
	 *         false if not
	 */
	private boolean styleNameExists(String styleName) {
		Vector styleNames = Util
				.getStyleNamesForTag(styles, getContentType() /*
															 * HTML.Tag.P.toString
															 * ()
															 */);
		return (styleNames.indexOf(styleName) > -1);
	}

	/**
	 * overridden to addd some custom cleanup upon closing of dialog
	 */
	@Override
	public void dispose() {
		if (mode == MODE_NAMED_STYLES) {
			styles.removeChangeListener(this);
		}
		super.dispose();
	}

	/**
	 * ChangeListener implementation to be used on a style sheet.
	 * 
	 * <p>
	 * This is used to update the list of named styles whenever a change was
	 * saved to the style sheet.
	 * </p>
	 */
	public void stateChanged(ChangeEvent e) {
		Object src = e.getSource();
		if (src instanceof StyleContext.NamedStyle) {
			Vector styleNames = Util.getStyleNamesForTag((AttributeSet) src,
					getContentType() /* HTML.Tag.P.toString() */);
			styleNames.insertElementAt(standardStyleName, 0);
			styleList.setModel(new DefaultComboBoxModel(styleNames));
		}
	}

	/**
	 * listen to actions and route them accordingly, i.e. react to buttons save,
	 * save as and delete style
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src.equals(saveStyleBtn)) {
			doSaveStyle();
		} else if (src.equals(saveStyleAsBtn)) {
			doSaveStyleAs();
		} else if (src.equals(deleteStyleBtn)) {
			doDeleteStyle();
		} else if (src.equals(cType)) {
			// update list of named styles
			updateStyleList();
		} else {
			super.actionPerformed(e);
		}
	}
}
