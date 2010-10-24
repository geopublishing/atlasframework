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

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.event.CaretEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position.Bias;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;

/**
 * An editor pane for application SimplyHTML.
 * 
 * <p>
 * This is extending <code>JEditorPane</code> by cut and paste and drag and drop
 * for HTML text. <code>JEditorPane</code> inherits cut and paste from <code>
 * JTextComponent</code>
 * where handling for plain text is implemented only. <code>JEditorPane</code>
 * has no additional functionality to add cut and paste for the various content
 * types it supports (such as 'text/html').
 * </p>
 * 
 * <p>
 * In stage 4 support for caret movement inside tables and table manipulation
 * methods are added.
 * </p>
 * 
 * <p>
 * In stage 6 support for list manipulation was added.
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
 * 
 * @see com.lightdev.app.shtm.HTMLText
 * @see com.lightdev.app.shtm.HTMLTextSelection
 */

public class SHTMLEditorPane extends JEditorPane implements DropTargetListener,
		DragSourceListener, DragGestureListener {
	private static final boolean OLD_JAVA_VERSION = System.getProperty(
			"java.version").compareTo("1.5.0") < 0;
	private JPopupMenu popup;

	/**
	 * construct a new <code>SHTMLEditorPane</code>
	 */
	public SHTMLEditorPane() {
		super();
		setCaretColor(Color.black);
		setNavigationFilter(new MyNavigationFilter());
		/**
		 * set the cursor by adding a MouseListener that allows to display a
		 * text cursor when the mouse pointer enters the editor. For some reason
		 * (probably someone knows why and could let me know...) the method
		 * setCursor does not have the same effect.
		 */

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				Component gp = getRootPane().getGlassPane();
				gp.setCursor(textCursor);
				gp.setVisible(true);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				Component gp = getRootPane().getGlassPane();
				gp.setCursor(defaultCursor);
				gp.setVisible(false);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}

			private void maybeShowPopup(MouseEvent e) {
				if (popup != null && e.isPopupTrigger()) {
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}

		});

		/** implement customized caret movement */
		adjustKeyBindings();

		/** init drag and drop */
		initDnd();
	}

	/**
	 * adjust the key bindings of the key map existing for this editor pane to
	 * our needs (i.e. add actions to certain keys such as tab/shift tab for
	 * caret movement inside tables, etc.)
	 * 
	 * This method had to be redone for using InputMap / ActionMap instead of
	 * Keymap.
	 */
	private void adjustKeyBindings() {
		ActionMap myActionMap = new ActionMap();
		InputMap myInputMap = new InputMap();

		KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
		myActionMap.put(SHTMLPanelImpl.nextTableCellAction,
				new NextTableCellAction(SHTMLPanelImpl.nextTableCellAction));
		myInputMap.put(tab, SHTMLPanelImpl.nextTableCellAction);

		KeyStroke shiftTab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
				InputEvent.SHIFT_MASK);
		myActionMap.put(SHTMLPanelImpl.prevTableCellAction,
				new PrevTableCellAction(SHTMLPanelImpl.prevTableCellAction));
		myInputMap.put(shiftTab, SHTMLPanelImpl.prevTableCellAction);

		KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		myActionMap.put(newListItemAction, new NewListItemAction());
		myInputMap.put(enter, newListItemAction);

		KeyStroke lineBreak = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
				InputEvent.SHIFT_MASK);
		myActionMap.put(insertLineBreakAction, new InsertLineBreakAction());
		myInputMap.put(lineBreak, insertLineBreakAction);

		KeyStroke backspace = KeyStroke.getKeyStroke('\b', 0);
		myActionMap.put(deletePrevCharAction, new DeletePrevCharAction());
		myInputMap.put(backspace, deletePrevCharAction);

		KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
		myActionMap.put(deleteNextCharAction, new DeleteNextCharAction());
		myInputMap.put(delete, deleteNextCharAction);

		myActionMap.setParent(getActionMap());
		myInputMap.setParent(getInputMap());
		setActionMap(myActionMap);
		setInputMap(JComponent.WHEN_FOCUSED, myInputMap);

		/*
		 * implementation before 1.4.1 -------------------------------------
		 * 
		 * Keymap map = getKeymap(); KeyStroke tab =
		 * KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
		 * map.addActionForKeyStroke(tab, new
		 * NextTableCellAction(map.getAction(tab))); KeyStroke shiftTab =
		 * KeyStroke.getKeyStroke( KeyEvent.VK_TAB, InputEvent.SHIFT_MASK);
		 * map.addActionForKeyStroke(shiftTab, new
		 * PrevTableCellAction(map.getAction(shiftTab))); setKeymap(map);
		 */
	}

	private static gnu.regexp.RE pattern1 = null;
	private static gnu.regexp.RE pattern2 = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#processKeyBinding(javax.swing.KeyStroke,
	 * java.awt.event.KeyEvent, int, boolean)
	 */
	@Override
	protected boolean processKeyBinding(KeyStroke ks, KeyEvent e,
			int condition, boolean pressed) {
		final int maximumEndSelection = ((SHTMLDocument) getDocument())
				.getLastDocumentPosition();
		if (getSelectionStart() >= maximumEndSelection
				&& !(ks.getKeyCode() == KeyEvent.VK_LEFT
						|| ks.getKeyCode() == KeyEvent.VK_UP || ks.getKeyCode() == KeyEvent.VK_HOME)) {
			return true;
		}
		if (getSelectionEnd() >= maximumEndSelection) {
			setSelectionEnd(maximumEndSelection - 1);
		}
		return super.processKeyBinding(ks, e, condition, pressed);
	}

	/**
	 * Convenience method for setting the document text contains hack around JDK
	 * bug 4799813 see
	 * http://developer.java.sun.com/developer/bugParade/bugs/4799813.html
	 * regression in 1.4.x, to be fixed in 1.5 When setting the text to be
	 * "&amp; footext", it becomes "&amp;footext" (space disappears) same ocurrs
	 * for "&lt;/a&gt; &amp;amp;", it becomes "&lt;/a&gt;&amp;amp;" (space
	 * disappears) with the hack it now does not occur anymore.
	 * 
	 * @param sText
	 *            the html-text of the document
	 */
	@Override
	public void setText(String sText) {
		try {
			if (System.getProperty("java.version").substring(0, 3)
					.equals("1.4")) {
				if (pattern1 == null)
					pattern1 = new gnu.regexp.RE(
							"(&\\w+;|&#\\d+;)(\\s|&#160;|&nbsp;)(?=<|&\\w+;|&#\\d+;)");
				sText = pattern1.substituteAll(sText, "$1&#160;$3");
				if (pattern2 == null)
					pattern2 = new gnu.regexp.RE(
							"<(/[^>])>(\\s|&#160;|&nbsp;|\\n\\s+)(?!&#160;)(&\\w+;|&#\\d+;)");
				sText = pattern2.substituteAll(sText, "<$1>&#160;$3$4");
			}
		} catch (gnu.regexp.REException ree) {
			ree.printStackTrace();
		}
		SHTMLDocument doc = (SHTMLDocument) getDocument();
		doc.startCompoundEdit();
		if (sText == null || sText.equals("")) {
			sText = "<html><body><p></p></body></html>";
		}
		super.setText(sText);
		setCaretPosition(0);
		doc.endCompoundEdit();
		if (OLD_JAVA_VERSION) {
			SHTMLPanelImpl.getOwnerSHTMLPanel(this).purgeUndos();
		}
	}

	private class MyNavigationFilter extends NavigationFilter {

		/*
		 * (non-Javadoc)
		 * 
		 * @seejavax.swing.text.NavigationFilter#moveDot(javax.swing.text.
		 * NavigationFilter.FilterBypass, int, javax.swing.text.Position.Bias)
		 */
		@Override
		public void moveDot(FilterBypass fb, int dot, Bias bias) {
			dot = getValidPosition(dot);
			super.moveDot(fb, dot, bias);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @seejavax.swing.text.NavigationFilter#setDot(javax.swing.text.
		 * NavigationFilter.FilterBypass, int, javax.swing.text.Position.Bias)
		 */
		@Override
		public void setDot(FilterBypass fb, int dot, Bias bias) {
			dot = getValidPosition(dot);
			super.setDot(fb, dot, bias);
		}

	}

	private int getValidPosition(int position) {
		SHTMLDocument doc = (SHTMLDocument) getDocument();
		final int lastValidPosition = doc.getLastDocumentPosition() - 1;
		if (position > lastValidPosition) {
			position = lastValidPosition;
		}
		int startPos = 0;
		if (doc.getDefaultRootElement().getElementCount() > 1) {
			startPos = doc.getDefaultRootElement().getElement(1)
					.getStartOffset();
		}
		final int validPosition = Math.max(position, startPos);
		return validPosition;
	}

	class DeletePrevCharAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			final int selectionStart = getSelectionStart();
			final int selectionEnd = getSelectionEnd();
			SHTMLDocument doc = (SHTMLDocument) getDocument();
			if (selectionEnd >= doc.getLastDocumentPosition()) {
				return;
			}
			if (selectionStart == selectionEnd) {
				{
					Element elem = SHTMLDocument.getListElement(doc
							.getParagraphElement(selectionStart));
					if (elem != null && elem.getStartOffset() == selectionStart) {
						performToggleListAction(e, elem.getName());
						return;
					}
				}
				{
					Element elem = SHTMLDocument.getListItemElement(doc
							.getParagraphElement(selectionStart));
					if (elem != null && elem.getStartOffset() == selectionStart) {
						int nextPosition = elem.getStartOffset() - 1;
						mergeListItemElements(SHTMLDocument
								.getListItemElement(doc
										.getParagraphElement(nextPosition)),
								elem);
						setCaretPosition(nextPosition);
						return;
					}
				}
				if (selectionStart > 0) {
					int nextPosition = selectionStart - 1;
					Element elem = SHTMLDocument.getTableCellElement(doc
							.getParagraphElement(nextPosition));
					if (elem != null && elem.getEndOffset() == selectionStart) {
						KeyStroke left = KeyStroke.getKeyStroke(
								KeyEvent.VK_LEFT, 0);
						Object key = getInputMap().getParent().get(left);
						if (key != null) {
							getActionMap().getParent().get(key)
									.actionPerformed(e);
						}
						return;
					}
				}
			}
			KeyStroke backb = KeyStroke.getKeyStroke('\b', 0);
			Object key = getInputMap().getParent().get(backb);
			if (key != null) {
				getActionMap().getParent().get(key).actionPerformed(e);
			}
		}
	}

	class DeleteNextCharAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			final int selectionStart = getSelectionStart();
			if (selectionStart == getSelectionEnd()) {
				SHTMLDocument doc = (SHTMLDocument) getDocument();
				final int nextPosition = selectionStart + 1;
				if (selectionStart >= doc.getLastDocumentPosition() - 1) {
					return;
				}
				{
					Element elem = SHTMLDocument.getListElement(doc
							.getParagraphElement(nextPosition));
					if (elem != null && elem.getStartOffset() == nextPosition) {
						setCaretPosition(nextPosition);
						performToggleListAction(e, elem.getName());
						return;
					}
				}
				{
					Element elem = SHTMLDocument.getListItemElement(doc
							.getParagraphElement(nextPosition));
					if (elem != null && elem.getStartOffset() == nextPosition) {
						mergeListItemElements(
								SHTMLDocument.getListItemElement(doc
										.getParagraphElement(nextPosition - 1)),
								elem);
						setCaretPosition(nextPosition);
						return;
					}
				}
				{
					Element elem = SHTMLDocument.getTableCellElement(doc
							.getParagraphElement(selectionStart));
					if (elem != null && elem.getEndOffset() == nextPosition) {
						return;
					}
				}
				if (nextPosition < doc.getLength()) {
					Element elem = SHTMLDocument.getTableCellElement(doc
							.getParagraphElement(nextPosition));
					if (elem != null && elem.getStartOffset() == nextPosition) {
						return;
					}
				}
			}
			KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
			Object key = getInputMap().getParent().get(delete);
			if (key != null) {
				getActionMap().getParent().get(key).actionPerformed(e);
			}
		}
	}

	/* ------- list manipulation start ------------------- */

	/**
	 * apply a set of attributes to the list the caret is currently in (if any)
	 * 
	 * @param a
	 *            the set of attributes to apply
	 */
	public void applyListAttributes(AttributeSet a) {
		SHTMLDocument doc = (SHTMLDocument) getDocument();
		Element first = doc.getParagraphElement(getSelectionStart());
		Element list = SHTMLDocument.getListElement(first);
		if (list != null) {
			if (a.getAttributeCount() > 0) {
				doc.addAttributes(list, a);
				/**
				 * for some reason above code does not show the changed
				 * attributes of the table, although the element really has them
				 * (maybe somebody could let me know why...). Therefore we
				 * update the editor pane contents comparably rude (any other
				 * more elegant alternatives welcome!)
				 * 
				 * --> found out why: the swing package does not render short
				 * hand properties such as MARGIN or PADDING. When contained in
				 * a document inside an AttributeSet they already have to be
				 * split into MARGIN-TOP, MARGIN-LEFT, etc. adjusted
				 * AttributeComponents accordingly so we don't need refresh
				 * anymore
				 */
				// refresh();
			}
		}
	}

	/**
	 * <code>Action</code> to create a new list item. THIS ACTION ALSO CREATES A
	 * NEW PARAGRAPH. --Dan
	 */
	class NewListItemAction extends AbstractAction {
		/** construct a <code>NewListItemAction</code> */
		public NewListItemAction() {
		}

		/**
		 * create a new list item, when the caret is inside a list
		 * 
		 * <p>
		 * The new item is created after the item at the caret position
		 * </p>
		 */
		public void actionPerformed(ActionEvent ae) {
			try {
				SHTMLDocument doc = (SHTMLDocument) getDocument();
				int caretPosition = getCaretPosition();
				// if we are in a list, create a new item
				Element listItemElement = SHTMLDocument.getListItemElement(doc
						.getParagraphElement(caretPosition));
				if (listItemElement != null) {
					int so = listItemElement.getStartOffset();
					int eo = listItemElement.getEndOffset();
					if (so != eo) {
						StringWriter writer = new StringWriter();
						writer.write("\n<li>\n");
						if (caretPosition > so) {
							SHTMLWriter htmlStartWriter = new SHTMLWriter(
									writer, doc, so, caretPosition - so);
							htmlStartWriter.writeChildElements(listItemElement);
						}
						writer.write("\n</li>\n<li>\n");
						if (caretPosition < eo - 1) {
							SHTMLWriter htmlEndWriter = new SHTMLWriter(writer,
									doc, caretPosition, eo - caretPosition);
							htmlEndWriter.writeChildElements(listItemElement);
						}
						writer.write("\n</li>\n");
						String text = writer.toString();
						try {
							doc.startCompoundEdit();
							doc.setOuterHTML(listItemElement, text);
						} catch (Exception e) {
							Util.errMsg(null, e.getMessage(), e);
						} finally {
							doc.endCompoundEdit();
						}
						setCaretPosition(caretPosition + 1);
					}
				}
				// we are not in a list, call alternate action
				else {
					KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
							0);
					Object key = getInputMap().getParent().get(enter);
					if (key != null) {
						getActionMap().getParent().get(key).actionPerformed(ae);
					}

					/*
					 * removed for changes in J2SE 1.4.1 if(alternateAction !=
					 * null) { alternateAction.actionPerformed(ae); }
					 */
				}
			} catch (Exception e) {
				Util.errMsg(null, e.getMessage(), e);
			}
		}
	}

	/**
	 * toggle list formatting on or off for the currently selected text portion.
	 * 
	 * <p>
	 * Switches list display on for the given type, if the selection contains
	 * parts not formatted as list or parts formatted as list of another type.
	 * </p>
	 * 
	 * <p>
	 * Switches list formatting off, if the selection contains only parts
	 * formatted as list of the given type.
	 * </p>
	 * 
	 * @param listTag
	 *            the list tag type to toggle on or off (UL or OL)
	 * @param a
	 *            the attributes to use for the list to toggle to
	 * @param forceOff
	 *            indicator for toggle operation. If true, possibly exisiting
	 *            list formatting inside the selected parts always is switched
	 *            off. If false, the method decides, if list formatting for the
	 *            parts inside the selection needs to be switched on or off.
	 */
	public void toggleList(final String listTag, final AttributeSet a,
			final boolean forceOff) {
		class ListManager {
			private Element removeStart;
			private int removeCount;
			private Element parent;
			private SHTMLDocument doc;
			private Element first;
			private int start;
			private int end;
			StringWriter sw;
			private int oStart;
			private int oEnd;

			class SwitchListException extends Exception {

			}

			ListManager() {
				removeStart = null;
				removeCount = 0;
				doc = (SHTMLDocument) getDocument();
				oStart = getSelectionStart();
				oEnd = getSelectionEnd();
				if (oEnd > doc.getLastDocumentPosition() - 1) {
					return;
				}
				first = getParagraphElement(oStart);
				start = first.getStartOffset();
				end = getParagraphElement(oEnd).getEndOffset();
				parent = getListParent(first);
				sw = new StringWriter();
			}

			private Element getParagraphElement(int pos) {
				Element paragraphElement = doc.getParagraphElement(pos);
				if (paragraphElement.getName().equalsIgnoreCase("p-implied")) {
					paragraphElement = paragraphElement.getParentElement();
				}
				return paragraphElement;
			}

			private Element getListParent(Element elem) {
				Element listParent = elem.getParentElement();
				if (elem.getName().equalsIgnoreCase(HTML.Tag.LI.toString())) {
					listParent = listParent.getParentElement();
				}
				return listParent;
			}

			private boolean isValidParentElement(Element e) {
				final String name = e.getName();
				return name.equalsIgnoreCase(HTML.Tag.BODY.toString())
						|| name.equalsIgnoreCase(HTML.Tag.TD.toString())
						|| name.equalsIgnoreCase(HTML.Tag.LI.toString());
			}

			private boolean isValidElement(Element e) {
				final String name = e.getName();
				return name.equalsIgnoreCase(HTML.Tag.P.toString())
						|| name.equalsIgnoreCase(HTML.Tag.UL.toString())
						|| name.equalsIgnoreCase(HTML.Tag.OL.toString())
						|| name.equalsIgnoreCase(HTML.Tag.H1.toString())
						|| name.equalsIgnoreCase(HTML.Tag.H2.toString())
						|| name.equalsIgnoreCase(HTML.Tag.H3.toString())
						|| name.equalsIgnoreCase(HTML.Tag.H4.toString())
						|| name.equalsIgnoreCase(HTML.Tag.H5.toString())
						|| name.equalsIgnoreCase(HTML.Tag.H6.toString());
			}

			private boolean isListRootElement(Element e) {
				final String name = e.getName();
				return name.equalsIgnoreCase(HTML.Tag.UL.toString())
						|| name.equalsIgnoreCase(HTML.Tag.OL.toString());
			}

			/**
			 * switch OFF list formatting for a given block of elements.
			 * 
			 * <p>
			 * switches off all list formatting inside the block for the given
			 * tag.
			 * </p>
			 * 
			 * <p>
			 * Splits lists if the selection covers only part of a list.
			 * </p>
			 * 
			 * @throws BadLocationException
			 * @throws IOException
			 */
			private void listOff() throws IOException, BadLocationException {
				SHTMLWriter writer = new SHTMLWriter(sw, doc);
				int i;
				Element next = null;
				for (i = 0; i < parent.getElementCount(); i++) {
					next = parent.getElement(i);
					if (next.getEndOffset() > start) {
						break;
					}
				}
				removeStart = next;
				removeCount = 1;
				int j = 0;
				Element li = null;
				if (next.getStartOffset() < start) {
					writer.writeStartTag(next);
					i++;
					for (;; j++) {
						li = next.getElement(j);
						if (li.getStartOffset() == start) {
							break;
						}
						writer.write(li);
					}
					writer.writeEndTag(next);
					for (; j < next.getElementCount(); j++) {
						li = next.getElement(j);
						if (li.getStartOffset() >= end) {
							break;
						}
						writer.writeStartTag("p", null);
						writer.writeChildElements(li);
						writer.writeEndTag("p");
					}
				}
				if (next.getEndOffset() <= end) {
					for (; i < parent.getElementCount(); i++) {
						next = parent.getElement(i);
						if (next.getEndOffset() > end) {
							break;
						}
						if (next != removeStart && next.getStartOffset() < end) {
							removeCount++;
						}
						if (isListRootElement(next)) {
							for (j = 0; j < next.getElementCount(); j++) {
								writer.writeStartTag("p", null);
								writer.writeChildElements(next.getElement(j));
								writer.writeEndTag("p");
							}
						} else {
							writer.writeStartTag("p", null);
							writer.writeChildElements(next);
							writer.writeEndTag("p");
						}
					}
				}

				if (i < parent.getElementCount() && next.getStartOffset() < end) {
					if (next != removeStart) {
						removeCount++;
					}
					for (; j < next.getElementCount(); j++) {
						li = next.getElement(j);
						if (li.getStartOffset() >= end) {
							break;
						}
						writer.writeStartTag("p", null);
						writer.writeChildElements(li);
						writer.writeEndTag("p");
					}
					if (j < next.getElementCount()) {
						writer.writeStartTag(next);
						for (; j < next.getElementCount(); j++) {
							li = next.getElement(j);
							writer.write(li);
						}
						writer.writeEndTag(next);
					}
				}
			}

			/**
			 * switch ON list formatting for a given block of elements.
			 * 
			 * <p>
			 * Takes care of merging existing lists before, after and inside
			 * respective element block.
			 * </p>
			 * 
			 * @throws BadLocationException
			 * @throws IOException
			 * 
			 */
			private void listOn() throws IOException, BadLocationException {
				SHTMLWriter writer = new SHTMLWriter(sw, doc);
				if (start > 0) {
					Element before = getParagraphElement(start - 1);
					if (before.getName().equalsIgnoreCase(
							HTML.Tag.LI.toString())) {
						final Element listRoot = before.getParentElement();
						if (listRoot.getParentElement() == parent
								&& listRoot.getName().equalsIgnoreCase(listTag)) {
							start = listRoot.getStartOffset();
						}
					}
				}
				if (end < doc.getLength() - 1) {
					Element after = getParagraphElement(end);
					if (after.getName()
							.equalsIgnoreCase(HTML.Tag.LI.toString())) {
						final Element listRoot = after.getParentElement();
						if (listRoot.getParentElement() == parent
								&& listRoot.getName().equalsIgnoreCase(listTag)) {
							end = listRoot.getEndOffset();
						}
					}
				}
				int i;
				Element next = null;
				for (i = 0; i < parent.getElementCount(); i++) {
					next = parent.getElement(i);
					if (next.getEndOffset() > start) {
						break;
					}
				}
				removeStart = next;
				removeCount = 1;
				int j = 0;
				Element li = null;
				if (next.getStartOffset() < start) {
					i++;
					writer.writeStartTag(next);
					for (;; j++) {
						li = next.getElement(j);
						if (li.getStartOffset() == start) {
							break;
						}
						writer.write(li);
					}
					writer.writeEndTag(next);
					writer.writeStartTag(listTag, a);
					for (; j < next.getElementCount(); j++) {
						li = next.getElement(j);
						if (li.getStartOffset() >= end) {
							break;
						}
						writer.write(li);
					}
				} else {
					writer.writeStartTag(listTag, a);
				}

				if (next.getEndOffset() <= end) {
					for (; i < parent.getElementCount(); i++) {
						next = parent.getElement(i);
						if (next.getEndOffset() > end) {
							break;
						}
						if (removeStart != next && next.getStartOffset() < end) {
							removeCount++;
						}
						if (isListRootElement(next)) {
							writer.writeChildElements(next);
						} else {
							writer.writeStartTag("li", null);
							writer.writeChildElements(next);
							writer.writeEndTag("li");
						}
					}
				}
				if (i < parent.getElementCount() && next.getStartOffset() < end) {
					if (removeStart != next) {
						removeCount++;
					}
					for (; j < next.getElementCount(); j++) {
						li = next.getElement(j);
						if (li.getStartOffset() >= end) {
							break;
						}
						writer.write(li);
					}
					writer.writeEndTag(listTag);
					if (j < next.getElementCount()) {
						writer.writeStartTag(next);
						for (; j < next.getElementCount(); j++) {
							li = next.getElement(j);
							writer.write(li);
						}
						writer.writeEndTag(next);
					}
				} else {
					writer.writeEndTag(listTag);
				}

			}

			/**
			 * decide to switch on or off list formatting
			 * 
			 * @return true, if list formatting is to be switched on, false if
			 *         not
			 * @throws SwitchListException
			 */
			private boolean switchOn() throws SwitchListException {
				boolean listOn = false;
				int count = parent.getElementCount();
				for (int i = 0; i < count && !listOn; i++) {
					Element elem = parent.getElement(i);
					if (elem.getStartOffset() >= start
							&& elem.getEndOffset() <= end
							&& !isValidElement(elem)) {
						throw new SwitchListException();
					}
					int eStart = elem.getStartOffset();
					int eEnd = elem.getEndOffset();
					if (!elem.getName().equalsIgnoreCase(listTag)) {
						if (((eStart > start) && (eStart < end))
								|| ((eEnd > start) && (eEnd < end))
								|| ((start >= eStart) && (end <= eEnd))) {
							listOn = true;
						}
					}
				}
				return listOn;
			}

			void toggleList() {
				try {
					doc.startCompoundEdit();
					if (!isValidParentElement(parent)) {
						throw new SwitchListException();
					}
					if (oStart != oEnd) {
						Element last = getParagraphElement(end - 1);
						if (parent != getListParent(last)) {
							throw new SwitchListException();
						}
					}
					if (!switchOn() || forceOff) {
						listOff();
					} else {
						listOn();
					}
					StringBuffer newHTML = sw.getBuffer();
					if (newHTML.length() > 0) {
						// System.out.println("newHTML=\r\n\r\n" +
						// newHTML.toString());
						doc.replaceHTML(removeStart, removeCount, newHTML
								.toString());
						if (oStart == oEnd) {
							setCaretPosition(oStart);
						} else {
							select(oStart, oEnd);
						}
						requestFocus();
					}
				} catch (SwitchListException e) {
				} catch (Exception e) {
					Util.errMsg(null, e.getMessage(), e);
				} finally {
					doc.endCompoundEdit();
				}
			}
		}
		ListManager listMan = new ListManager();
		listMan.toggleList();
	}

	private void mergeListItemElements(Element first, Element second) {
		final SHTMLDocument doc = (SHTMLDocument) getDocument();
		final StringWriter sw = new StringWriter();
		final SHTMLWriter w = new SHTMLWriter(sw, doc);
		try {
			w.writeStartTag(first);
			w.writeChildElements(first);
			int l = sw.getBuffer().length();
			while (sw.getBuffer().charAt(--l) <= 13) {
				sw.getBuffer().deleteCharAt(l);
			}
			w.writeChildElements(second);
			w.writeEndTag(first);
			String htmlText = sw.toString();
			doc.replaceHTML(first, 2, htmlText);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/** range indicator for applying attributes to the current cell only */
	public static final int THIS_CELL = 0;

	/**
	 * range indicator for applying attributes to cells of the current column
	 * only
	 */
	public static final int THIS_COLUMN = 1;

	/** range indicator for applying attributes to cells of the current row only */
	public static final int THIS_ROW = 2;

	/** range indicator for applying attributes to all cells */
	public static final int ALL_CELLS = 3;

	/** default table width */
	public static final String DEFAULT_TABLE_WIDTH = "80%";

	/** default vertical alignment */
	public static final String DEFAULT_VERTICAL_ALIGN = "top";

	/**
	 * insert a table
	 * 
	 * @param colCount
	 *            the number of columns the new table shall have
	 */
	public void insertTable(int colCount) {
		final int selectionStart = getSelectionStart();
		int start = selectionStart;
		StringWriter sw = new StringWriter();
		SHTMLDocument doc = (SHTMLDocument) getDocument();
		SHTMLWriter w = new SHTMLWriter(sw, doc);
		// some needed constants
		String table = HTML.Tag.TABLE.toString();
		String tr = HTML.Tag.TR.toString();
		String td = HTML.Tag.TD.toString();
		String p = HTML.Tag.P.toString();
		// the attribute set to use for applying attributes to tags
		SimpleAttributeSet set = new SimpleAttributeSet();
		// build table attribute
		Util.styleSheet().addCSSAttribute(set, CSS.Attribute.WIDTH,
				DEFAULT_TABLE_WIDTH);
		Util.styleSheet().addCSSAttribute(set, CSS.Attribute.BORDER_STYLE,
				"solid");
		Util.styleSheet().addCSSAttribute(set, CSS.Attribute.BORDER_TOP_WIDTH,
				"0");
		Util.styleSheet().addCSSAttribute(set,
				CSS.Attribute.BORDER_RIGHT_WIDTH, "0");
		Util.styleSheet().addCSSAttribute(set,
				CSS.Attribute.BORDER_BOTTOM_WIDTH, "0");
		Util.styleSheet().addCSSAttribute(set, CSS.Attribute.BORDER_LEFT_WIDTH,
				"0");
		set.addAttribute(HTML.Attribute.BORDER, "0");
		try {
			w.writeStartTag(table, set);
			// start row tag
			w.writeStartTag(tr, null);
			// get width of each cell according to column count
			// build cell width attribute
			Util.styleSheet().addCSSAttribute(set, CSS.Attribute.WIDTH,
					Integer.toString(100 / colCount) + Util.pct);
			set.addAttribute(HTML.Attribute.VALIGN, DEFAULT_VERTICAL_ALIGN);
			Util.styleSheet().addCSSAttribute(set,
					CSS.Attribute.BORDER_TOP_WIDTH, "1");
			Util.styleSheet().addCSSAttribute(set,
					CSS.Attribute.BORDER_RIGHT_WIDTH, "1");
			Util.styleSheet().addCSSAttribute(set,
					CSS.Attribute.BORDER_BOTTOM_WIDTH, "1");
			Util.styleSheet().addCSSAttribute(set,
					CSS.Attribute.BORDER_LEFT_WIDTH, "1");
			SimpleAttributeSet pSet = new SimpleAttributeSet();
			Util.styleSheet().addCSSAttribute(pSet, CSS.Attribute.MARGIN_TOP,
					"1");
			Util.styleSheet().addCSSAttribute(pSet, CSS.Attribute.MARGIN_RIGHT,
					"1");
			Util.styleSheet().addCSSAttribute(pSet,
					CSS.Attribute.MARGIN_BOTTOM, "1");
			Util.styleSheet().addCSSAttribute(pSet, CSS.Attribute.MARGIN_LEFT,
					"1");
			set.removeAttribute(HTML.Attribute.BORDER);
			// add cells
			for (int i = 0; i < colCount; i++) {
				w.writeStartTag(td, set);
				w.writeStartTag(p, pSet);
				w.writeEndTag(p);
				w.writeEndTag(td);
			}
			// end row and table tags
			w.writeEndTag(tr);
			w.writeEndTag(table);
			// read table html into document
			Element para = doc.getParagraphElement(selectionStart);
			if (para == null) {
				throw new Exception("no text selected");
			}
			for (Element parent = para.getParentElement(); !parent.getName()
					.equalsIgnoreCase(HTML.Tag.BODY.toString())
					&& !parent.getName().equalsIgnoreCase(
							HTML.Tag.TD.toString()); para = parent, parent = parent
					.getParentElement())
				;

			if (para != null) {
				try {
					doc.startCompoundEdit();
					doc.insertBeforeStart(para, sw.getBuffer().toString());
				} catch (Exception e) {
					Util.errMsg(null, e.getMessage(), e);
				} finally {
					doc.endCompoundEdit();
				}
				// w.write(para);
				// doc.replaceHTML(para, 1, sw.getBuffer().toString());
			}
		} catch (Exception ex) {
			Util.errMsg(null, ex.getMessage(), ex);
		}
		select(start, start);
	}

	/**
	 * apply a new anchor to the currently selected text
	 * 
	 * <p>
	 * If nothing is selected, this method does nothing
	 * </p>
	 * 
	 * @param anchorName
	 *            the name of the new anchor
	 */
	public void insertAnchor(String anchorName) {
		if (getSelectionStart() != getSelectionEnd()) {
			SimpleAttributeSet aSet = new SimpleAttributeSet();
			aSet.addAttribute(HTML.Attribute.NAME, anchorName);
			SimpleAttributeSet set = new SimpleAttributeSet();
			set.addAttribute(HTML.Tag.A, aSet);
			applyAttributes(set, false);
		}
	}

	/**
	 * insert a line break (i.e. a break for which paragraph spacing is not
	 * applied)
	 */
	public void insertBreak() {
		int caretPos = getCaretPosition();
		SHTMLDocument doc = (SHTMLDocument) getDocument();
		try {
			((SHTMLEditorKit) getEditorKit()).insertHTML(doc, caretPos, "<BR>",
					0, 0, HTML.Tag.BR);
		} catch (Exception e) {
		}
		setCaretPosition(caretPos + 1);
	}

	/**
	 * set a text link at the current selection replacing the selection with a
	 * given text.
	 * 
	 * <p>
	 * If nothing is selected, but the caret is inside a link, this will replace
	 * the existing link. If nothing is selected and the caret is not inside a
	 * link, this method does nothing.
	 * </p>
	 * 
	 * @param linkText
	 *            the text that shall appear as link at the current selection
	 * @param href
	 *            the target this link shall refer to
	 * @param className
	 *            the style class to be used
	 */
	public void setLink(String linkText, String href, String className) {
		setLink(linkText, href, className, null, null);
	}

	/**
	 * set a link at the current selection replacing the selection with the
	 * given text or image.
	 * 
	 * @param linkText
	 *            the text to show as link (or null, if an image shall appear
	 *            instead)
	 * @param href
	 *            the link reference
	 * @param className
	 *            the style name to be used for the link
	 * @param linkImage
	 *            the file name of the image be used for the link (or null, if a
	 *            text link is to be set instead)
	 * @param size
	 *            the size of the image or null
	 */
	public void setLink(String linkText, String href, String className,
			String linkImage, Dimension size) {
		SHTMLDocument doc = (SHTMLDocument) getDocument();
		Element e = Util.findLinkElementUp(doc
				.getCharacterElement(getSelectionStart()));
		if (linkImage == null) {
			setTextLink(e, href, className, linkText, doc);
		} else {
			setImageLink(doc, e, href, className, linkImage, size);
		}
	}

	/**
	 * set an image link replacing the current selection
	 * 
	 * @param doc
	 *            the document to apply the link to
	 * @param e
	 *            the link element found at the selection, or null if none was
	 *            found
	 * @param href
	 *            the link reference
	 * @param className
	 *            the style name to be used for the link
	 * @param linkImage
	 *            the file name of the image be used for the link
	 * @param size
	 *            the size of the image
	 */
	private void setImageLink(SHTMLDocument doc, Element e, String href,
			String className, String linkImage, Dimension size) {
		String a = HTML.Tag.A.toString();
		SimpleAttributeSet set = new SimpleAttributeSet();
		set.addAttribute(HTML.Attribute.HREF, href);
		set.addAttribute(HTML.Attribute.CLASS, className);
		StringWriter sw = new StringWriter();
		SHTMLWriter w = new SHTMLWriter(sw, doc);
		try {
			w.writeStartTag(a, set);
			set = new SimpleAttributeSet();
			set.addAttribute(HTML.Attribute.SRC, Util.getRelativePath(new File(
					doc.getBase().getFile()), new File(linkImage)));
			set.addAttribute(HTML.Attribute.BORDER, "0");
			if (size != null) {
				set.addAttribute(HTML.Attribute.WIDTH, Integer
						.toString(new Double(size.getWidth()).intValue()));
				set.addAttribute(HTML.Attribute.HEIGHT, Integer
						.toString(new Double(size.getHeight()).intValue()));
			}
			w.writeStartTag(HTML.Tag.IMG.toString(), set);
			w.writeEndTag(a);
			if (e != null) {
				System.out
						.println("SHTMLEditorPane.setImageLink setOuterHTML html='"
								+ sw.getBuffer() + "'");
				doc.setOuterHTML(e, sw.getBuffer().toString());
			} else {
				int start = getSelectionStart();
				if (start < getSelectionEnd()) {
					replaceSelection("");
					System.out
							.println("SHTMLEditorPane.setImageLink insertAfterEnd html='"
									+ sw.getBuffer() + "'");
					doc.insertAfterEnd(doc.getCharacterElement(start), sw
							.getBuffer().toString());
				}
			}
		} catch (Exception ex) {
			Util.errMsg(this, ex.getMessage(), ex);
		}
	}

	/**
	 * set a text link replacing the current selection
	 * 
	 * @param e
	 *            the link element found at the selection, or null if none was
	 *            found
	 * @param href
	 *            the link reference
	 * @param className
	 *            the style name to be used for the link
	 * @param linkText
	 *            the text to show as link
	 * @param doc
	 *            the document to apply the link to
	 */
	private void setTextLink(Element e, String href, String className,
			String linkText, SHTMLDocument doc) {
		SimpleAttributeSet aSet = new SimpleAttributeSet();
		aSet.addAttribute(HTML.Attribute.HREF, href);
		String sStyleName = Util.getResourceString("standardStyleName");
		if (className != null && !className.equalsIgnoreCase(sStyleName)) {
			aSet.addAttribute(HTML.Attribute.CLASS, className);
		}
		SimpleAttributeSet set = new SimpleAttributeSet();
		if (e != null) {
			// replace existing link
			set.addAttributes(e.getAttributes());
			set.addAttribute(HTML.Tag.A, aSet);
			int start = e.getStartOffset();
			try {
				doc.replace(start, e.getEndOffset() - start, linkText, set);
			} catch (BadLocationException ex) {
				Util.errMsg(this, ex.getMessage(), ex);
			}
		} else {
			// create new link for text selection
			int start = getSelectionStart();
			if (start < getSelectionEnd()) {
				set.addAttribute(HTML.Tag.A, aSet);
				replaceSelection(linkText);
				doc
						.setCharacterAttributes(start, linkText.length(), set,
								false);
			}
		}
	}

	/**
	 * remove an anchor with a given name
	 * 
	 * @param anchorName
	 *            the name of the anchor to remove
	 */
	public void removeAnchor(String anchorName) {
		// System.out.println("SHTMLEditorPane removeAnchor");
		AttributeSet attrs;
		Object nameAttr;
		Object link;
		ElementIterator eli = new ElementIterator(getDocument());
		Element elem = eli.first();
		while (elem != null) {
			attrs = elem.getAttributes();
			link = attrs.getAttribute(HTML.Tag.A);
			if (link != null /*
							 * &&
							 * link.toString().equalsIgnoreCase(HTML.Tag.A.toString
							 * ())
							 */) {
				// System.out.println("found anchor attribute");
				nameAttr = ((AttributeSet) link)
						.getAttribute(HTML.Attribute.NAME);
				if (nameAttr != null
						&& nameAttr.toString().equalsIgnoreCase(anchorName)) {
					// remove anchor here
					// System.out.println("removing anchor name=" + nameAttr);
					SimpleAttributeSet newSet = new SimpleAttributeSet(attrs);
					newSet.removeAttribute(HTML.Tag.A);
					SHTMLDocument doc = (SHTMLDocument) getDocument();
					int start = elem.getStartOffset();
					doc.setCharacterAttributes(elem.getStartOffset(), elem
							.getEndOffset()
							- start, newSet, true);
				}
			}
			elem = eli.next();
		}
	}

	/**
	 * insert a table column before the current column (if any)
	 */
	public void insertTableColumn() {
		Element cell = getCurTableCell();
		if (cell != null) {
			createTableColumn(cell, Util.getElementIndex(cell)/*
															 * getColNumber(cell)
															 */, true);
		}
	}

	/**
	 * append a table column after the last column (if any)
	 */
	public void appendTableColumn() {
		Element cell = getCurTableCell();
		if (cell != null) {
			Element lastCell = getLastTableCell(cell);
			createTableColumn(lastCell, Util.getElementIndex(cell)/*
																 * getColNumber(lastCell
																 * )
																 */, false);
		}
	}

	/**
	 * create a table column before or after a given column
	 * 
	 * the width of the first cell in the column (if there is a width attribute)
	 * is split into half so that the new column and the column inserted before
	 * are sharing the space originally taken by the column inserted before.
	 * 
	 * @param cell
	 *            the cell to copy from
	 * @param cIndex
	 *            the number of the column 'cell' is in
	 * @param before
	 *            true indicates insert before, false append after
	 */
	private void createTableColumn(Element cell, int cIndex, boolean before) {

		// get the new width setting for this column and the new column
		SHTMLDocument doc = (SHTMLDocument) getDocument();
		Element table = cell.getParentElement().getParentElement();
		Element srcCell = table.getElement(0).getElement(cIndex);
		SimpleAttributeSet set = new SimpleAttributeSet();
		Object attr = set.getAttribute(CSS.Attribute.WIDTH);
		if (attr != null) {
			// LengthValue lv = new LengthValue(attr);
			// String unit = lv.getUnit();
			// int width = (int) lv.getAttrValue(attr.toString(), unit);
			int width = (int) Util.getAbsoluteAttrVal(attr); // Util.getAttrValue(attr);
			// System.out.println("SHTMLEditorPane.createTableColumn width=" +
			// width);
			String unit = Util.getLastAttrUnit();
			// System.out.println("SHTMLEditorPane.createTableColumn unit=" +
			// unit);
			String widthString = Integer.toString(width / 2) + unit;
			// System.out.println("SHTMLEditorPane.createTableColumn widthString="
			// + widthString);
			Util.styleSheet().addCSSAttribute(set, CSS.Attribute.WIDTH,
					widthString);
		}

		// adjust width and insert new column
		// SimpleAttributeSet a = new SimpleAttributeSet(set.copyAttributes());
		// int cellIndex = getCellIndex(srcCell);
		// boolean insertFirst = (before && (cellIndex == 0));
		for (int rIndex = 0; rIndex < table.getElementCount(); rIndex++) {
			srcCell = table.getElement(rIndex).getElement(cIndex);
			/*
			 * if(rIndex > 0) { adjustBorder(a, a, a,
			 * CombinedAttribute.ATTR_TOP); } adjustBorder(a, a, a,
			 * CombinedAttribute.ATTR_LEFT);
			 */
			doc.addAttributes(srcCell, set);
			try {
				if (before) {
					doc
							.insertBeforeStart(srcCell,
									createTableCellHTML(srcCell));
				} else {
					doc.insertAfterEnd(srcCell, createTableCellHTML(srcCell));
				}
			} catch (IOException ioe) {
				Util.errMsg(null, ioe.getMessage(), ioe);
			} catch (BadLocationException ble) {
				Util.errMsg(null, ble.getMessage(), ble);
			}
		}
		// adjustColumnBorders(table.getElement(0).getElement(cIndex));
		// adjustColumnBorders(table.getElement(0).getElement(++cIndex));
	}

	/**
	 * append a row to a table assuming the caret currently is inside a table
	 */
	public void appendTableRow() {
		Element cell = getCurTableCell();
		if (cell != null) {
			Element table = cell.getParentElement().getParentElement();
			Element lastRow = table.getElement(table.getElementCount() - 1);
			createTableRow(lastRow, Util.getRowIndex(lastRow.getElement(0)),
					false);
		}
	}

	/**
	 * insert a row to a table assuming the caret currently is inside a table
	 */
	public void insertTableRow() {
		Element cell = getCurTableCell();
		if (cell != null) {
			createTableRow(cell.getParentElement(), Util.getRowIndex(cell),
					true);
		}
	}

	/**
	 * create a new table row by either inserting it before a given row or
	 * appending it after a given row
	 * 
	 * this method is shared by appendRow and insertRow
	 * 
	 * @param srcRow
	 *            the row element to copy from
	 * @param before
	 *            true indicates insert before, false append after
	 */
	private void createTableRow(Element srcRow, int rowIndex, boolean before) {
		try {
			if (before) {
				((SHTMLDocument) getDocument()).insertBeforeStart(srcRow,
						getTableRowHTML(srcRow));
				if (rowIndex == 0) {
					rowIndex++;
				}
			} else {
				((SHTMLDocument) getDocument()).insertAfterEnd(srcRow,
						getTableRowHTML(srcRow));
				rowIndex++;
			}
		} catch (IOException ioe) {
			Util.errMsg(null, ioe.getMessage(), ioe);
		} catch (BadLocationException ble) {
			Util.errMsg(null, ble.getMessage(), ble);
		}
	}

	/**
	 * build an HTML string copying from an existing table row.
	 * 
	 * For each table column found in srcRow a start and end tag TD is created
	 * with the same attributes as in the column found in srcRow. The attributes
	 * of srcRow are applied to the newly created row HTML string as well.
	 * 
	 * @param srcRow
	 *            the table row Element to copy from
	 * @param insert
	 *            indicates if a row is inserted before another row
	 * 
	 * @return an HTML string representing the new table row (without cell
	 *         contents)
	 */
	private String getTableRowHTML(Element srcRow) {
		StringWriter sw = new StringWriter();
		SHTMLWriter w = new SHTMLWriter(sw, (SHTMLDocument) getDocument());
		String tr = HTML.Tag.TR.toString();
		try {
			w.writeStartTag(tr, srcRow.getAttributes());
			for (int i = 0; i < srcRow.getElementCount(); i++) {
				final Element cell = srcRow.getElement(i);
				createTableCellHTML(w, cell);
			}
			w.writeEndTag(tr);
		} catch (IOException ex) {
			Util.errMsg(null, ex.getMessage(), ex);
		}
		return sw.getBuffer().toString();
	}

	private void createTableCellHTML(SHTMLWriter w, final Element cell)
			throws IOException {
		{
			w.writeStartTag(cell.getName(), cell.getAttributes());
			final Element paragraph = cell.getElement(0);
			final String parName = paragraph.getName();
			if (!parName.equalsIgnoreCase(HTML.Tag.IMPLIED.toString())) {
				w.writeStartTag(parName, paragraph.getAttributes());
				w.writeEndTag(parName);
			}
			w.writeEndTag(cell.getName());
		}
	}

	/**
	 * build an HTML string copying from an existing table cell
	 * 
	 * @param srcCell
	 *            the cell to get the HTML for
	 * @param a
	 *            set of attributes to copy if we are inserting first table
	 *            column
	 * @param insertFirst
	 *            indicates if we are inserting first table column
	 * @param rNum
	 *            number of row a cell is to be inserted to (can be any value if
	 *            insertFirst is false)
	 * 
	 * @return the HTML string for the given cell (without cell contents)
	 */
	private String createTableCellHTML(Element srcCell) {
		StringWriter sw = new StringWriter();
		SHTMLWriter w = new SHTMLWriter(sw, (SHTMLDocument) getDocument());
		try {
			createTableCellHTML(w, srcCell);
		} catch (IOException e) {
			Util.errMsg(null, e.getMessage(), e);
		}
		// System.out.println("getTableCellHTML buffer='" +
		// sw.getBuffer().toString() + "'");
		return sw.getBuffer().toString();
	}

	/**
	 * delete the row of the table the caret is currently in (if any)
	 */
	public void deleteTableRow() {
		Element cell = getCurTableCell();
		if (cell != null) {
			removeElement(cell.getParentElement());
		}
	}

	/**
	 * delete the column of the table the caret is currently in (if any)
	 * 
	 * <p>
	 * width of adjacent column is adjusted, if there is more than one column in
	 * the table. Width adjustment only works, if width attributes of both the
	 * column to remove and its adjacent column have the same unit (pt or %).
	 * </p>
	 * 
	 * <p>
	 * If there is only one cell or if the caret is not in a table, this method
	 * does nothing
	 * </p>
	 * 
	 * <p>
	 * Smart border handling automatically sets the left border of a cell to
	 * zero, if the cell on the left of that cell has a right border and both
	 * cells have no margin. In that case removing the first column will cause
	 * all cells of the new first column to have no left border.
	 * </p>
	 */
	public void deleteTableCol() {
		Element cell = getCurTableCell();
		if (cell != null) {

			Element row = cell.getParentElement();
			int lastColIndex = row.getElementCount() - 1;

			if (lastColIndex > 0) {
				int cIndex = Util.getElementIndex(cell); // getColNumber(cell);
				int offset = -1; // adjacent cell is left of current cell
				if (cIndex == 0) { // if current cell is in first column...
					offset *= -1; // ...adjacent cell is right of current cell
				}

				Object attrC = cell.getAttributes().getAttribute(
						CSS.Attribute.WIDTH);
				Object attrA = row.getElement(cIndex + offset).getAttributes()
						.getAttribute(CSS.Attribute.WIDTH);
				SimpleAttributeSet set = null;

				if (attrC != null && attrA != null) {
					// LengthValue lvC = new LengthValue(attrC);
					// LengthValue lvA = new LengthValue(attrA);
					int widthC = (int) Util.getAbsoluteAttrVal(attrC); // Util.getAttrValue(attrC);
					String cUnit = Util.getLastAttrUnit();
					// String cUnit = lvC.getUnit();
					int widthA = (int) Util.getAbsoluteAttrVal(attrA); // Util.getAttrValue(attrA);
					String aUnit = Util.getLastAttrUnit();
					if (aUnit.equalsIgnoreCase(cUnit)) {
						int width = 0;
						width += widthC;
						width += widthA;
						if (width > 0) {
							String widthString = Integer.toString(width)
									+ cUnit;
							set = new SimpleAttributeSet(row.getElement(
									cIndex + offset).getAttributes());
							Util.styleSheet().addCSSAttribute(set,
									CSS.Attribute.WIDTH, widthString);
						}
					}
				}

				Element table = row.getParentElement();
				SHTMLDocument doc = (SHTMLDocument) getDocument();

				if (cIndex < lastColIndex) {
					offset = 0;
				}

				for (int rIndex = table.getElementCount() - 1; rIndex >= 0; rIndex--) {
					row = table.getElement(rIndex);
					try {
						doc.removeElements(row, cIndex, 1);
						/*
						 * the following line does not work for the last column
						 * in a table so we use above code instead
						 * 
						 * removeElement(row.getElement(cIndex));
						 */
					} catch (BadLocationException ble) {
						Util.errMsg(null, ble.getMessage(), ble);
					}
					if (set != null) {
						doc.addAttributes(row.getElement(cIndex + offset), set);
					}
					// adjustColumnBorders(table.getElement(0).getElement(cIndex
					// + offset));
				}
			}
		}
	}

	/**
	 * remove an element from the document of this editor (shared by
	 * deleteTableRow and deleteTableCol)
	 * 
	 * @param e
	 *            the element to remove
	 */
	private void removeElement(Element e) {
		int start = e.getStartOffset();
		try {
			((SHTMLDocument) getDocument()).remove(start, e.getEndOffset()
					- start);
		} catch (BadLocationException ble) {
			Util.errMsg(null, ble.getMessage(), ble);
		}

	}

	/**
	 * apply a set of attributes to the table the caret is currently in (if any)
	 * 
	 * @param a
	 *            the set of attributes to apply
	 */
	public void applyTableAttributes(AttributeSet a) {
		Element cell = getCurTableCell();
		if (cell != null) {
			Element table = cell.getParentElement().getParentElement();
			if (a.getAttributeCount() > 0) {
				// System.out.println("applyTableAttributes count=" +
				// a.getAttributeCount() + " a=" + a);
				((SHTMLDocument) getDocument()).addAttributes(table, a);
				/**
				 * for some reason above code does not show the changed
				 * attributes of the table, although the element really has them
				 * (maybe somebody could let me know why...). Therefore we
				 * update the editor pane contents comparably rude (any other
				 * more elegant alternatives welcome!)
				 * 
				 * --> found out why: the swing package does not render short
				 * hand properties such as MARGIN or PADDING. When contained in
				 * a document inside an AttributeSet they already have to be
				 * split into MARGIN-TOP, MARGIN-LEFT, etc. adjusted
				 * AttributeComponents accordingly so we don't need refresh
				 * anymore
				 */
				// refresh();
			}
		}
	}

	/**
	 * refresh the whole contents of this editor pane with brute force
	 */
	private void refresh() {
		int pos = getCaretPosition();
		String data = getText();
		setText("");
		setText(data);
		setCaretPosition(pos);
	}

	/**
	 * apply a set of attributes to a given range of cells of the table the
	 * caret is currently in (if any)
	 * 
	 * @param a
	 *            the set of attributes to apply
	 * @param range
	 *            the range of cells to apply attributes to
	 * 
	 * @see adjustColWidths
	 */
	public void applyCellAttributes(AttributeSet a, int range) {
		// System.out.println("SHTMLEditorPane applyCellAttributes a=" + a);
		Element cell = getCurTableCell();
		int cIndex = 0;
		int rIndex = 0;
		SHTMLDocument doc = (SHTMLDocument) getDocument();
		if (cell != null) {
			Element row = cell.getParentElement();
			Element table = row.getParentElement();
			Element aCell;
			switch (range) {
			case THIS_CELL:
				doc.addAttributes(cell, a);
				break;
			case THIS_ROW:
				for (cIndex = 0; cIndex < row.getElementCount(); cIndex++) {
					aCell = row.getElement(cIndex);
					doc.addAttributes(aCell, a);
				}
				break;
			case THIS_COLUMN:
				cIndex = Util.getElementIndex(cell); // getColNumber(cell);
				for (rIndex = 0; rIndex < table.getElementCount(); rIndex++) {
					aCell = table.getElement(rIndex).getElement(cIndex);
					doc.addAttributes(aCell, a);
				}
				break;
			case ALL_CELLS:
				while (rIndex < table.getElementCount()) {
					row = table.getElement(rIndex);
					cIndex = 0;
					while (cIndex < row.getElementCount()) {
						aCell = row.getElement(cIndex);
						// System.out.println("applyCellAttributes ALL_CELLS adjusted a="
						// + adjustCellBorders(aCell, a));
						doc.addAttributes(aCell, a);
						cIndex++;
					}
					rIndex++;
				}
				break;
			}
		}
	}

//	/**
//	 * get the number of the table column a given cell is in
//	 * 
//	 * @param cell
//	 *            the cell to get the column number for
//	 * @return the column number of the given cell
//	 */
//	private int getColNumber(Element cell) {
//		int i = 0;
//		Element thisRow = cell.getParentElement();
//		int last = thisRow.getElementCount() - 1;
//		Element aCell = thisRow.getElement(i);
//		if (aCell != cell) {
//			while ((i < last) && (aCell != cell)) {
//				aCell = thisRow.getElement(++i);
//			}
//		}
//		return i;
//	}

	/* ------- table manipulation end -------------------- */

	/* ------- table cell navigation start --------------- */

	/**
	 * <code>Action</code> to move the caret from the current table cell to the
	 * next table cell.
	 */
	class NextTableCellAction extends AbstractAction {

		/** action to use when not inside a table */
		/*
		 * removed for changes in J2SE 1.4.1 private Action alternateAction;
		 */

		/** construct a <code>NextTableCellAction</code> */
		public NextTableCellAction(String actionName) {
			super(actionName);
		}

		/**
		 * construct a <code>NextTableCellAction</code>
		 * 
		 * @param altAction
		 *            the action to use when the caret is not inside a table
		 */
		/*
		 * removed for changes in J2SE 1.4.1 public NextTableCellAction(Action
		 * altAction) { alternateAction = altAction; }
		 */

		/**
		 * move to the previous cell or invoke an alternate action if the caret
		 * is not inside a table
		 * 
		 * this will append a new table row when the caret is inside the last
		 * table cell
		 */
		public void actionPerformed(ActionEvent ae) {
			Element cell = getCurTableCell();
			if (cell != null) {
				goNextCell(cell);
			} else {
				KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
				Object key = getInputMap().getParent().get(tab);
				if (key != null) {
					getActionMap().getParent().get(key).actionPerformed(ae);
				}

				/*
				 * removed for changes in J2SE 1.4.1 if(alternateAction != null)
				 * { alternateAction.actionPerformed(ae); }
				 */
			}
		}

	}

	/**
	 * <code>Action</code> to move the caret from the current table cell to the
	 * previous table cell.
	 */
	class PrevTableCellAction extends AbstractAction {

		/** action to use when not inside a table */
		/*
		 * removed for changes in J2SE 1.4.1 private Action alternateAction;
		 */

		/** construct a <code>PrevTableCellAction</code> */
		public PrevTableCellAction(String actionName) {
			super(actionName);
		}

		/**
		 * construct a <code>PrevTableCellAction</code>
		 * 
		 * @param altAction
		 *            the action to use when the caret is not inside a table
		 */
		/*
		 * removed for changes in J2SE 1.4.1 public PrevTableCellAction(Action
		 * altAction) { alternateAction = altAction; }
		 */

		/**
		 * move to the previous cell or invoke an alternate action if the caret
		 * is not inside a table
		 */
		public void actionPerformed(ActionEvent ae) {
			Element cell = getCurTableCell();
			if (cell != null) {
				goPrevCell(cell);
			} else {
				KeyStroke shiftTab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
						InputEvent.SHIFT_MASK);
				Object key = getInputMap().getParent().get(shiftTab);
				if (key != null) {
					getActionMap().getParent().get(key).actionPerformed(ae);
				}

				/*
				 * removed for changes in J2SE 1.4.1 if(alternateAction != null)
				 * { alternateAction.actionPerformed(ae); }
				 */
			}
		}
	}

	/**
	 * <code>Action</code> to create a new list item.
	 */
	class InsertLineBreakAction extends AbstractAction {

		/** construct a <code>NewListItemAction</code> */
		public InsertLineBreakAction() {
		}

		/**
		 * create a new list item, when the caret is inside a list
		 * 
		 * <p>
		 * The new item is created after the item at the caret position
		 * </p>
		 */
		public void actionPerformed(ActionEvent ae) {
			try {
				SHTMLDocument doc = (SHTMLDocument) getDocument();
				int caretPosition = getCaretPosition();
				Element paragraphElement = doc
						.getParagraphElement(caretPosition);
				if (paragraphElement != null) {
					int so = paragraphElement.getStartOffset();
					int eo = paragraphElement.getEndOffset();
					if (so != eo) {
						StringWriter writer = new StringWriter();
						if (caretPosition > so) {
							SHTMLWriter htmlStartWriter = new SHTMLWriter(
									writer, doc, so, caretPosition - so);
							htmlStartWriter
									.writeChildElements(paragraphElement);
						}
						// work around: <br> is written twice by java
						if (!doc.getCharacterElement(caretPosition).getName()
								.equalsIgnoreCase(HTML.Tag.BR.toString())) {
							writer.write("<br>");
						}
						if (caretPosition < eo - 1) {
							SHTMLWriter htmlEndWriter = new SHTMLWriter(writer,
									doc, caretPosition, eo - caretPosition);
							htmlEndWriter.writeChildElements(paragraphElement);
						}
						String text = writer.toString();
						try {
							doc.startCompoundEdit();
							doc.setInnerHTML(paragraphElement, text);
						} catch (Exception e) {
							Util.errMsg(null, e.getMessage(), e);
						} finally {
							doc.endCompoundEdit();
						}
						setCaretPosition(caretPosition + 1);
					}
				}
			} catch (Exception e) {
				Util.errMsg(null, e.getMessage(), e);
			}
		}
	}

	public void goNextCell(Element cell) {
		if (cell == getLastTableCell(cell)) {
			appendTableRow();
			cell = getCurTableCell();
		}
		int pos = getNextCell(cell).getStartOffset();
		select(pos, pos);
	}

	public void goPrevCell(Element cell) {
		int newPos;
		if (cell != getFirstTableCell(cell)) {
			cell = getPrevCell(cell);
			newPos = cell.getStartOffset();
			select(newPos, newPos);
		}
	}

	/**
	 * get the table cell following a given table cell
	 * 
	 * @param cell
	 *            the cell whose following cell shall be found
	 * @return the Element having the cell following the given cell or null if
	 *         the given cell is the last cell in the table
	 */
	private Element getNextCell(Element cell) {
		Element nextCell = null;
		Element thisRow = cell.getParentElement();
		Element nextRow = null;
		Element table = thisRow.getParentElement();
		int i = thisRow.getElementCount() - 1;
		Element aCell = thisRow.getElement(i);
		if (aCell != cell) {
			while ((i > 0) && (aCell != cell)) {
				nextCell = aCell;
				aCell = thisRow.getElement(--i);
			}
		} else {
			i = table.getElementCount() - 1;
			Element aRow = table.getElement(i);
			while ((i > 0) && (aRow != thisRow)) {
				nextRow = aRow;
				aRow = table.getElement(--i);
			}
			nextCell = nextRow.getElement(0);
		}
		return nextCell;
	}

	/**
	 * get the table cell preceding a given table cell
	 * 
	 * @param cell
	 *            the cell whose preceding cell shall be found
	 * @return the Element having the cell preceding the given cell or null if
	 *         the given cell is the first cell in the table
	 */
	private Element getPrevCell(Element cell) {
		Element thisRow = cell.getParentElement();
		Element table = thisRow.getParentElement();
		Element prevCell = null;
		int i = 0;
		Element aCell = thisRow.getElement(i);
		if (aCell != cell) {
			while (aCell != cell) {
				prevCell = aCell;
				aCell = thisRow.getElement(i++);
			}
		} else {
			Element prevRow = null;
			Element aRow = table.getElement(i);
			while (aRow != thisRow) {
				prevRow = aRow;
				aRow = table.getElement(i++);
			}
			prevCell = prevRow.getElement(prevRow.getElementCount() - 1);
		}
		return prevCell;
	}

	/**
	 * get the last cell of the table a given table cell belongs to
	 * 
	 * @param cell
	 *            a cell of the table to get the last cell of
	 * @return the Element having the last table cell
	 */
	private Element getLastTableCell(Element cell) {
		Element table = cell.getParentElement().getParentElement();
		Element lastRow = table.getElement(table.getElementCount() - 1);
		Element lastCell = lastRow.getElement(lastRow.getElementCount() - 1);
		return lastCell;
	}

	/**
	 * get the first cell of the table a given table cell belongs to
	 * 
	 * @param cell
	 *            a cell of the table to get the first cell of
	 * @return the Element having the first table cell
	 */
	private Element getFirstTableCell(Element cell) {
		Element table = cell.getParentElement().getParentElement();
		Element firstCell = table.getElement(0).getElement(0);
		return firstCell;
	}

	/**
	 * get the table cell at the current caret position
	 * 
	 * @return the Element having the current table cell or null if the caret is
	 *         not inside a table cell
	 */
	public Element getCurTableCell() {
		final Element element = ((SHTMLDocument) getDocument())
				.getCharacterElement(getSelectionStart());
		final String td = HTML.Tag.TD.toString();
		final String th = HTML.Tag.TH.toString();
		Element elem = element;
		String elementName = elem.getName();
		while ((elem != null)
				&& !((elementName = elem.getName()).equalsIgnoreCase(td) || elementName
						.equalsIgnoreCase(th))) {
			elem = elem.getParentElement();
		}
		return elem;
	}

	/* ---------- table cell navigation end -------------- */

	/**
	 * Replaces the currently selected content with new content represented by
	 * the given <code>HTMLText</code>. If there is no selection this amounts to
	 * an insert of the given text. If there is no replacement text this amounts
	 * to a removal of the current selection.
	 * 
	 * @overrides replaceSelection in <code>JEditorPane</code> for usage of our
	 *            own HTMLText object
	 * 
	 * @param content
	 *            the content to replace the selection with
	 */
	public void replaceSelection(HTMLText content) {
		SHTMLDocument doc = (SHTMLDocument) getDocument();
		Caret caret = getCaret();
		if (doc != null) {
			try {
				int p0 = Math.min(caret.getDot(), caret.getMark());
				int p1 = Math.max(caret.getDot(), caret.getMark());
				if (p0 != p1) {
					doc.remove(p0, p1 - p0);
				}
				if (content != null) {
					content.pasteHTML(doc, p0);
				}
			} catch (Exception e) {
				getToolkit().beep();
			}
		}
	}

	/*
	 * ------ start of drag and drop implementation -------------------------
	 * (see also constructor of this class)
	 */

	/** enables this component to be a Drop Target */
	DropTarget dropTarget = null;

	/** enables this component to be a Drag Source */
	DragSource dragSource = null;

	/** the last selection start */
	private int lastSelStart = 0;

	/** the last selection end */
	private int lastSelEnd = 0;

	/** the location of the last event in the text component */
	private int dndEventLocation = 0;

	/**
	 * <p>
	 * This flag is set by this objects dragGestureRecognizer to indicate that a
	 * drag operation has been started from this object. It is cleared once
	 * dragDropEnd is captured by this object.
	 * </p>
	 * 
	 * <p>
	 * If a drop occurs in this object and this object started the drag
	 * operation, then the element to be dropped comes from this object and thus
	 * has to be removed somewhere else in this object.
	 * </p>
	 * 
	 * <p>
	 * To the contrary if a drop occurs in this object and the drag operation
	 * was not started in this object, then the element to be dropped does not
	 * come from this object and has not to be removed here.
	 * </p>
	 */
	private boolean dragStartedHere = false;

	/**
	 * Initialize the drag and drop implementation for this component.
	 * 
	 * <p>
	 * DropTarget, DragSource and DragGestureRecognizer are instantiated and a
	 * MouseListener is established to track the selection in drag operations
	 * </p>
	 * 
	 * <p>
	 * this ideally is called in the constructor of a class which would like to
	 * implement drag and drop
	 * </p>
	 */
	public void initDnd() {
		dropTarget = new DropTarget(this, this);
		dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer(this,
				DnDConstants.ACTION_MOVE, this);
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				this_mouseReleased(e);
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				this_mouseClicked(e);
			}
		});
	}

	/** a drag gesture has been initiated */
	public void dragGestureRecognized(DragGestureEvent event) {
		int selStart = getSelectionStart();
		try {
			if ((lastSelEnd > lastSelStart) && (selStart >= lastSelStart)
					&& (selStart < lastSelEnd)) {
				dragStartedHere = true;
				select(lastSelStart, lastSelEnd);
				HTMLText text = new HTMLText();
				int start = getSelectionStart();
				text.copyHTML(this, start, getSelectionEnd() - start);
				HTMLTextSelection trans = new HTMLTextSelection(text);
				dragSource.startDrag(event, DragSource.DefaultMoveDrop, trans,
						this);
			}
		} catch (Exception e) {
			// getToolkit().beep();
		}
	}

	/**
	 * this message goes to DragSourceListener, informing it that the dragging
	 * has ended
	 */
	public void dragDropEnd(DragSourceDropEvent event) {
		dragStartedHere = false;
	}

	/** is invoked when a drag operation is going on */
	public void dragOver(DropTargetDragEvent event) {
		dndEventLocation = viewToModel(event.getLocation());
		try {
			setCaretPosition(dndEventLocation);
		} catch (Exception e) {
			// getToolkit().beep();
		}
	}

	/**
	 * a drop has occurred. If the dragged element has a suitable
	 * <code>DataFlavor</code>, do the drop.
	 * 
	 * @param event
	 *            - the event specifiying the drop operation
	 * @see java.awt.datatransfer.DataFlavor
	 * @see de.calcom.cclib.text.StyledText
	 * @see de.calcom.cclib.text.StyledTextSelection
	 */
	public void drop(DropTargetDropEvent event) {
		dndEventLocation = viewToModel(event.getLocation());
		if ((dndEventLocation >= lastSelStart)
				&& (dndEventLocation <= lastSelEnd)) {
			event.rejectDrop();
			select(lastSelStart, lastSelEnd);
		} else {
			final SHTMLDocument doc = (SHTMLDocument) getDocument();
			doc.startCompoundEdit();
			try {
				Transferable transferable = event.getTransferable();
				if (transferable.isDataFlavorSupported(df)) {
					HTMLText s = (HTMLText) transferable.getTransferData(df);
					doDrop(event, s);
				} else if (transferable
						.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					String s = (String) transferable
							.getTransferData(DataFlavor.stringFlavor);
					doDrop(event, s);
				} else {
					event.rejectDrop();
				}
			} catch (Exception exception) {
				exception.printStackTrace();
				event.rejectDrop();
			} finally {
				doc.endCompoundEdit();
			}
		}
	}

	/**
	 * do the drop operation consisting of adding the dragged element and
	 * necessarily removing the dragged element from the original position
	 */
	private void doDrop(DropTargetDropEvent event, Object s) {
		int removeOffset = 0;
		int moveOffset = 0;
		int newSelStart;
		int newSelEnd;
		event.acceptDrop(DnDConstants.ACTION_MOVE);
		setCaretPosition(dndEventLocation);
		if (s instanceof HTMLText) {
			replaceSelection((HTMLText) s);
		} else if (s instanceof String) {
			replaceSelection((String) s);
		}
		if (dndEventLocation < lastSelStart) {
			removeOffset = s.toString().length();
		} else {
			moveOffset = s.toString().length();
		}
		newSelEnd = dndEventLocation + (lastSelEnd - lastSelStart) - moveOffset;
		newSelStart = dndEventLocation - moveOffset;
		if (dragStartedHere) {
			lastSelStart += removeOffset;
			lastSelEnd += removeOffset;
			select(lastSelStart, lastSelEnd);
			replaceSelection("");
		}
		lastSelEnd = newSelEnd;
		lastSelStart = newSelStart;
		select(lastSelStart, lastSelEnd);
		event.getDropTargetContext().dropComplete(true);
	}

	/** remember current selection when mouse button is released */
	void this_mouseReleased(MouseEvent e) {
		lastSelStart = getSelectionStart();
		lastSelEnd = getSelectionEnd();
	}

	/** remember current selection when mouse button is double clicked */
	void this_mouseClicked(MouseEvent e) {
		if (e.getClickCount() > 1) {
			lastSelStart = getSelectionStart();
			lastSelEnd = getSelectionEnd();
		}
	}

	/** is invoked if the user modifies the current drop gesture */
	public void dropActionChanged(DropTargetDragEvent event) {
	}

	/** is invoked when the user changes the dropAction */
	public void dropActionChanged(DragSourceDragEvent event) {
	}

	/** is invoked when you are dragging over the DropSite */
	public void dragEnter(DropTargetDragEvent event) {
	}

	/** is invoked when you are exit the DropSite without dropping */
	public void dragExit(DropTargetEvent event) {
	}

	/**
	 * this message goes to DragSourceListener, informing it that the dragging
	 * has entered the DropSite
	 */
	public void dragEnter(DragSourceDragEvent event) {
	}

	/**
	 * this message goes to DragSourceListener, informing it that the dragging
	 * has exited the DropSite
	 */
	public void dragExit(DragSourceEvent event) {
	}

	/**
	 * this message goes to DragSourceListener, informing it that the dragging
	 * is currently ocurring over the DropSite
	 */
	public void dragOver(DragSourceDragEvent event) {
	}

	// ------ end of drag and drop implementation ----------------------------

	/* ------ start of cut, copy and paste implementation ------------------- */

	@Override
	public TransferHandler getTransferHandler() {
		final TransferHandler defaultTransferHandler = super
				.getTransferHandler();
		if (defaultTransferHandler == null)
			return null;
		class LocalTransferHandler extends TransferHandler {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * javax.swing.TransferHandler#canImport(javax.swing.JComponent,
			 * java.awt.datatransfer.DataFlavor[])
			 */
			@Override
			public boolean canImport(JComponent comp,
					DataFlavor[] transferFlavors) {
				return defaultTransferHandler.canImport(comp, transferFlavors);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * javax.swing.TransferHandler#exportAsDrag(javax.swing.JComponent,
			 * java.awt.event.InputEvent, int)
			 */
			@Override
			public void exportAsDrag(JComponent comp, InputEvent e, int action) {
				defaultTransferHandler.exportAsDrag(comp, e, action);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * javax.swing.TransferHandler#exportToClipboard(javax.swing.JComponent
			 * , java.awt.datatransfer.Clipboard, int)
			 */
			@Override
			public void exportToClipboard(JComponent comp, Clipboard clip,
					int action) {
				SHTMLDocument doc = (SHTMLDocument) getDocument();
				if (doc.getParagraphElement(getSelectionStart()) != doc
						.getParagraphElement(getSelectionEnd())) {
					defaultTransferHandler
							.exportToClipboard(comp, clip, action);
					return;
				}
				try {
					HTMLText st = new HTMLText();
					int start = getSelectionStart();
					st.copyHTML(SHTMLEditorPane.this, start, getSelectionEnd()
							- start);
					final Transferable additionalContents = new HTMLTextSelection(
							st);
					Clipboard temp = new Clipboard("");
					defaultTransferHandler
							.exportToClipboard(comp, temp, action);
					final Transferable defaultContents = temp.getContents(this);
					clip.setContents(new Transferable() {
						public DataFlavor[] getTransferDataFlavors() {
							DataFlavor[] defaultFlavors = defaultContents
									.getTransferDataFlavors();
							DataFlavor[] additionalFlavors = additionalContents
									.getTransferDataFlavors();
							DataFlavor[] resultFlavor = new DataFlavor[defaultFlavors.length
									+ additionalFlavors.length];
							System.arraycopy(defaultFlavors, 0, resultFlavor,
									0, defaultFlavors.length);
							System.arraycopy(additionalFlavors, 0,
									resultFlavor, defaultFlavors.length,
									additionalFlavors.length);
							return resultFlavor;
						}

						public boolean isDataFlavorSupported(DataFlavor flavor) {
							return additionalContents
									.isDataFlavorSupported(flavor)
									|| defaultContents
											.isDataFlavorSupported(flavor);
						}

						public Object getTransferData(DataFlavor flavor)
								throws UnsupportedFlavorException, IOException {
							if (additionalContents
									.isDataFlavorSupported(flavor))
								return additionalContents
										.getTransferData(flavor);
							return defaultContents.getTransferData(flavor);
						}
					}, null);
				} catch (Exception e) {
					getToolkit().beep();
				}
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * javax.swing.TransferHandler#getSourceActions(javax.swing.JComponent
			 * )
			 */
			@Override
			public int getSourceActions(JComponent c) {
				return defaultTransferHandler.getSourceActions(c);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * javax.swing.TransferHandler#getVisualRepresentation(java.awt.
			 * datatransfer.Transferable)
			 */
			@Override
			public Icon getVisualRepresentation(Transferable t) {
				return defaultTransferHandler.getVisualRepresentation(t);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * javax.swing.TransferHandler#importData(javax.swing.JComponent,
			 * java.awt.datatransfer.Transferable)
			 */
			@Override
			public boolean importData(JComponent comp, final Transferable t) {
				SHTMLDocument doc = (SHTMLDocument) getDocument();
				doc.startCompoundEdit();
				boolean result = false;
				try {
					if (t.isDataFlavorSupported(df)) {
						HTMLText st = (HTMLText) t.getTransferData(df);
						replaceSelection(st);
						result = true;
					} else {
						result = importExternalData(comp, t);
					}
				} catch (Exception e) {
					getToolkit().beep();
				}
				doc.endCompoundEdit();
				return result;
			}

			private boolean importExternalData(JComponent comp,
					final Transferable t) throws ClassNotFoundException,
					UnsupportedFlavorException, IOException {
				// workaround for java decoding bug
				// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6740877
				final DataFlavor htmlFlavor = new DataFlavor(
						"text/html; class=java.lang.String");
				if (t.isDataFlavorSupported(htmlFlavor)) {
					final String s = (String) t.getTransferData(htmlFlavor);
					if (s.charAt(0) == 65533) {
						return defaultImportData(comp, new Transferable() {

							public Object getTransferData(DataFlavor flavor)
									throws UnsupportedFlavorException,
									IOException {
								if (isValid(flavor))
									return t.getTransferData(flavor);
								throw new UnsupportedFlavorException(flavor);
							}

							public DataFlavor[] getTransferDataFlavors() {
								final DataFlavor[] transferDataFlavors = t
										.getTransferDataFlavors();
								int counter = 0;
								for (int i = 0; i < transferDataFlavors.length; i++) {
									if (isValid(transferDataFlavors[i])) {
										counter++;
									}
								}
								final DataFlavor[] validDataFlavors = new DataFlavor[counter];
								int j = 0;
								for (int i = 0; i < transferDataFlavors.length; i++) {
									final DataFlavor flavor = transferDataFlavors[i];
									if (isValid(flavor)) {
										validDataFlavors[j++] = flavor;
									}
								}
								return validDataFlavors;
							}

							public boolean isDataFlavorSupported(
									DataFlavor flavor) {
								return isValid(flavor)
										&& t.isDataFlavorSupported(flavor);
							}

							private boolean isValid(DataFlavor flavor) {
								return !flavor.isMimeTypeEqual("text/html");
							}

						});
					}
				}
				return defaultImportData(comp, t);
			}

			private boolean defaultImportData(JComponent comp, Transferable t) {
				return defaultTransferHandler.importData(comp, t);
			}
		}
		return new LocalTransferHandler();
	}

	/* ------ end of cut, copy and paste implementation --------------- */

	/* ------ start of font/paragraph manipulation --------------- */

	public void removeCharacterAttributes() {
		int p0 = getSelectionStart();
		int p1 = getSelectionEnd();
		if (p0 != p1) {
			SHTMLDocument doc = (SHTMLDocument) getDocument();
			doc.startCompoundEdit();
			// clear all character attributes in selection
			SimpleAttributeSet sasText = null;
			for (int i = p0; i < p1;) {
				final Element characterElement = doc.getCharacterElement(i);
				sasText = new SimpleAttributeSet(characterElement
						.getAttributes().copyAttributes());
				final int endOffset = characterElement.getEndOffset();
				Enumeration attribEntries1 = sasText.getAttributeNames();
				while (attribEntries1.hasMoreElements()) {
					Object entryKey = attribEntries1.nextElement();
					if (!entryKey.toString().equals(
							HTML.Attribute.NAME.toString())) {
						sasText.removeAttribute(entryKey);
					}
				}
				final int last = p1 < endOffset ? p1 : endOffset;
				try {
					doc.setCharacterAttributes(i, last - i, sasText, true);
				} catch (Exception e) {
				}
				i = last;
			}
			doc.endCompoundEdit();
		}
	}

	public void removeParagraphAttributes() {
		int p0 = getSelectionStart();
		int p1 = getSelectionEnd();
		SHTMLDocument doc = (SHTMLDocument) getDocument();
		doc.removeParagraphAttributes(p0, p1 - p0 + 1);
		select(p0, p1);
	}

	public void applyAttributes(AttributeSet a, boolean para) {
		applyAttributes(a, para, false);
	}

	/**
	 * set the attributes for a given part of this editor. If a range of text is
	 * selected, the attributes are applied to the selection. If nothing is
	 * selected, the input attributes of the given editor are set thus applying
	 * the given attributes to future inputs.
	 * 
	 * @param a
	 *            the set of attributes to apply
	 * @param para
	 *            true, if the attributes shall be applied to the whole
	 *            paragraph, false, if only the selected range of characters
	 *            shall have them
	 * @param replace
	 *            true, if existing attribtes are to be replaced, false if not
	 */
	public void applyAttributes(AttributeSet a, boolean para, boolean replace) {
		SHTMLDocument doc = (SHTMLDocument) getDocument();
		requestFocus();
		int start = getSelectionStart();
		int end = getSelectionEnd();
		if (para) {
			doc.setParagraphAttributes(start, end - start, a, replace);
		} else {
			if (end != start) {
				doc.setCharacterAttributes(start, end - start, a, replace);
			} else {
				MutableAttributeSet inputAttributes = ((SHTMLEditorKit) getEditorKit())
						.getInputAttributes();
				inputAttributes.addAttributes(a);
			}
		}
	}

	/**
	 * switch elements in the current selection to a given tag
	 * 
	 * @param tag
	 *            the tag name to switche elements to
	 * @param allowedTags
	 *            tags that may be switched
	 */
	public void applyTag(String tag, Vector allowedTags) {
		int start = getSelectionStart();
		int end = getSelectionEnd();
		// System.out.println("SHTMLEditorPane applyTag start=" + start +
		// ", end=" + end);
		StringWriter sw = new StringWriter();
		SHTMLDocument doc = (SHTMLDocument) getDocument();
		try {
			doc.startCompoundEdit();
			SHTMLWriter w = new SHTMLWriter(sw, doc);
			Element elem = doc.getParagraphElement(start);
			// System.out.println("SHTMLEditorPane applyTag elemName=" +
			// elem.getName());
			start = elem.getStartOffset();
			if (end < elem.getEndOffset()) {
				end = elem.getEndOffset();
			}
			// System.out.println("SHTMLEditorPane applyTag start=" + start +
			// ", end=" + end);
			elem = elem.getParentElement();
			int replaceStart = -1;
			int replaceEnd = -1;
			int index = -1;
			int removeCount = 0;
			int eCount = elem.getElementCount();
			// System.out.println("SHTMLEditorPane applyTag parent elem=" +
			// elem.getName() + ", eCount=" + eCount);
			for (int i = 0; i < eCount; i++) {
				Element child = elem.getElement(i);
				// System.out.println("SHTMLEditorPane applyTag child elem=" +
				// child.getName() + ", eCount=" + eCount);
				int eStart = child.getStartOffset();
				int eEnd = child.getEndOffset();
				// System.out.println("SHTMLEditorPane applyTag eStart=" +
				// eStart + ", eEnd=" + eEnd);
				if ((eStart >= start && eStart < end)
						|| (eEnd > start && eEnd <= end)) {
					++removeCount;
					if (allowedTags.contains(child.getName())) {
						// System.out.println("SHTMLEditorPane applyTag element is in selection");
						w.writeStartTag(tag.toString(), child.getAttributes());
						w.writeChildElements(child);
						w.writeEndTag(tag.toString());
						if (index < 0) {
							index = i;
						}
						if (replaceStart < 0 || replaceStart > eStart) {
							replaceStart = eStart;
						}
						if (replaceEnd < 0 || replaceEnd < eEnd) {
							replaceEnd = eEnd;
						}
					} else {
						w.write(child);
					}
				}
			}
			// System.out.println("SHTMLEditorPane applyTag remove index=" +
			// index + ", removeCount=" + removeCount);
			if (index > -1) {
				doc.insertAfterEnd(elem.getElement(index), sw.getBuffer()
						.toString());
				doc.removeElements(elem, index, removeCount);
			}
			// SHTMLEditorKit kit = (SHTMLEditorKit) getEditorKit();
			// System.out.println("SHTMLEditorPane applyTag new HTML=\r\n" +
			// sw.getBuffer().toString() );
			// kit.read(new StringReader(sw.getBuffer().toString()), doc,
			// getCaretPosition());
		} catch (Exception e) {
			Util.errMsg(this, e.getMessage(), e);
		} finally {
			doc.endCompoundEdit();
		}
	}

	/* ------ end of font/paragraph manipulation --------------- */

	/* ---------- class fields start -------------- */

	static Action toggleBulletListAction = null;
	static Action toggleNumberListAction = null;

	private void performToggleListAction(ActionEvent e, String elemName) {
		if (elemName.equalsIgnoreCase(HTML.Tag.UL.toString())) {
			if (toggleBulletListAction == null) {
				Component c = (Component) e.getSource();
				SHTMLPanelImpl panel = SHTMLPanelImpl.getOwnerSHTMLPanel(c);
				toggleBulletListAction = panel.dynRes
						.getAction(SHTMLPanelImpl.toggleBulletsAction);
			}
			toggleBulletListAction.actionPerformed(e);
		} else if (elemName.equalsIgnoreCase(HTML.Tag.OL.toString())) {
			if (toggleNumberListAction == null) {
				Component c = (Component) e.getSource();
				SHTMLPanelImpl panel = SHTMLPanelImpl.getOwnerSHTMLPanel(c);
				toggleNumberListAction = panel.dynRes
						.getAction(SHTMLPanelImpl.toggleNumbersAction);
			}
			toggleNumberListAction.actionPerformed(e);
		}
	}

	public static final String newListItemAction = "newListItem";
	public static final String insertLineBreakAction = "insertLineBreak";

	public static final String deletePrevCharAction = "deletePrevChar";
	public static final String deleteNextCharAction = "deleteNextChar";

	/** a data flavor for transferables processed by this component */
	private DataFlavor df = new DataFlavor(
			com.lightdev.app.shtm.HTMLText.class, "HTMLText");

	/* Cursors for mouseovers in the editor */
	private Cursor textCursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
	private Cursor defaultCursor = Cursor
			.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

	void updateInputAttributes() {
		((SHTMLEditorKit) getEditorKit()).updateInputAttributes(this);
		fireCaretUpdate(new CaretEvent(this) {

			@Override
			public int getDot() {
				return getSelectionStart();
			}

			@Override
			public int getMark() {
				return getSelectionEnd();
			}

		});
	}

	public JPopupMenu getPopup() {
		return popup;
	}

	public void setPopup(JPopupMenu popup) {
		this.popup = popup;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#getTransferHandler()
	 */

	/* ---------- class fields end -------------- */

}
