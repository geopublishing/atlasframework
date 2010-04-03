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
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.GapContent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.text.html.HTML.Tag;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

/**
 * Extends <code>HTMLDocument</code> by a custom reader which supports the SPAN
 * tag.
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

public class SHTMLDocument extends HTMLDocument {

	public static final String SUFFIX = "&nbsp;";

	private static Set paragraphElements;

	private AttributeContext context;
	private CompoundEdit compoundEdit;
	private int compoundEditDepth;
	private boolean inSetParagraphAttributes = false;
	private boolean baseDirChecked = false;

	/**
	 * Constructs an SHTMLDocument.
	 */
	public SHTMLDocument() {
		this(new GapContent(BUFFER_SIZE_DEFAULT), new StyleSheet());
	}

	/**
	 * Constructs an SHTMLDocument with the default content storage
	 * implementation and the given style/attribute storage mechanism.
	 * 
	 * @param styles
	 *            the styles
	 */
	public SHTMLDocument(StyleSheet styles) {
		this(new GapContent(BUFFER_SIZE_DEFAULT), styles);
	}

	/**
	 * Constructs an SHTMLDocument with the given content storage implementation
	 * and the given style/attribute storage mechanism.
	 * 
	 * @param c
	 *            the container for the content
	 * @param styles
	 *            the styles
	 */
	public SHTMLDocument(Content c, StyleSheet styles) {
		super(c, styles);
		compoundEdit = null;
	}

	/**
	 * apply a set of attributes to a given document element
	 * 
	 * @param e
	 *            the element to apply attributes to
	 * @param a
	 *            the set of attributes to apply
	 */
	public void addAttributes(Element e, AttributeSet a) {
		if ((e != null) && (a != null)) {
			try {
				writeLock();
				// System.out.println("SHTMLDocument addAttributes e=" + e);
				// System.out.println("SHTMLDocument addAttributes a=" + a);
				int start = e.getStartOffset();
				DefaultDocumentEvent changes = new DefaultDocumentEvent(start,
						e.getEndOffset() - start,
						DocumentEvent.EventType.CHANGE);
				AttributeSet sCopy = a.copyAttributes();
				MutableAttributeSet attr = (MutableAttributeSet) e
						.getAttributes();
				changes.addEdit(new AttributeUndoableEdit(e, sCopy, false));
				attr.addAttributes(a);
				changes.end();
				fireChangedUpdate(changes);
				fireUndoableEditUpdate(new UndoableEditEvent(this, changes));
			} finally {
				writeUnlock();
			}
		}
	}

	/**
	 * Remove a consecutive group of child Elements
	 * 
	 * @param e
	 *            the parent element to remove child elements from
	 * @param index
	 *            the index of the first child element to remove
	 * @param count
	 *            the number of child elements to remove
	 */
	public void removeElements(Element e, int index, int count)
			throws BadLocationException {
		writeLock();
		int start = e.getElement(index).getStartOffset();
		int end = e.getElement(index + count - 1).getEndOffset();
		try {
			Element[] removed = new Element[count];
			Element[] added = new Element[0];
			for (int counter = 0; counter < count; counter++) {
				removed[counter] = e.getElement(counter + index);
			}
			DefaultDocumentEvent dde = new DefaultDocumentEvent(start, end
					- start, DocumentEvent.EventType.REMOVE);
			((AbstractDocument.BranchElement) e).replace(index, removed.length,
					added);
			dde.addEdit(new ElementEdit(e, index, removed, added));
			UndoableEdit u = getContent().remove(start, end - start);
			if (u != null) {
				dde.addEdit(u);
			}
			postRemoveUpdate(dde);
			dde.end();
			fireRemoveUpdate(dde);
			if (u != null) {
				fireUndoableEditUpdate(new UndoableEditEvent(this, dde));
			}
		} finally {
			writeUnlock();
		}
	}

	@Override
	public void insertString(int offs, String str, AttributeSet a)
			throws BadLocationException {
		super.insertString(offs, str.replaceAll("\t", "        "), a);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.text.html.HTMLDocument#setOuterHTML(javax.swing.text.Element,
	 * java.lang.String)
	 */
	@Override
	public void setOuterHTML(Element elem, String htmlText)
			throws BadLocationException, IOException {
		try {
			startCompoundEdit();
			super.setOuterHTML(elem, htmlText);
		} finally {
			endCompoundEdit();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.text.html.HTMLDocument#insertAfterEnd(javax.swing.text.Element
	 * , java.lang.String)
	 */
	@Override
	public void insertAfterEnd(Element elem, String htmlText)
			throws BadLocationException, IOException {
		try {
			startCompoundEdit();
			super.insertAfterEnd(elem, htmlText);
		} finally {
			endCompoundEdit();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.text.html.HTMLDocument#insertAfterStart(javax.swing.text.
	 * Element, java.lang.String)
	 */
	@Override
	public void insertAfterStart(Element elem, String htmlText)
			throws BadLocationException, IOException {
		try {
			startCompoundEdit();
			super.insertAfterStart(elem, htmlText);
		} finally {
			endCompoundEdit();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.text.html.HTMLDocument#insertBeforeEnd(javax.swing.text.Element
	 * , java.lang.String)
	 */
	@Override
	public void insertBeforeEnd(Element elem, String htmlText)
			throws BadLocationException, IOException {
		try {
			startCompoundEdit();
			super.insertBeforeEnd(elem, htmlText);
		} finally {
			endCompoundEdit();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.text.html.HTMLDocument#insertBeforeStart(javax.swing.text
	 * .Element, java.lang.String)
	 */
	@Override
	public void insertBeforeStart(Element elem, String htmlText)
			throws BadLocationException, IOException {
		try {
			startCompoundEdit();
			super.insertBeforeStart(elem, htmlText);
		} finally {
			endCompoundEdit();
		}
	}

	public void replaceHTML(Element firstElement, int number, String htmlText)
			throws BadLocationException, IOException {
		if (number > 1) {
			if (firstElement != null && firstElement.getParentElement() != null
					&& htmlText != null) {
				int start = firstElement.getStartOffset();
				Element parent = firstElement.getParentElement();
				int removeIndex = parent.getElementIndex(start);
				try {
					startCompoundEdit();
					removeElements(parent, removeIndex, number - 1);
					setOuterHTML(parent.getElement(removeIndex), htmlText);
				} finally {
					endCompoundEdit();
				}
			}
		} else if (number == 1) {
			setOuterHTML(firstElement, htmlText);
		}
	}

	public void startCompoundEdit() {
		compoundEditDepth++;
	}

	public void endCompoundEdit() {
		if (compoundEditDepth != 0) {
			compoundEditDepth--;
			if (compoundEditDepth == 0 && compoundEdit != null) {
				compoundEdit.end();
				super.fireUndoableEditUpdate(new UndoableEditEvent(this,
						compoundEdit));
				compoundEdit = null;
			}
		}
	}

	@Override
	protected void fireUndoableEditUpdate(UndoableEditEvent e) {
		if (compoundEditDepth == 0) {
			super.fireUndoableEditUpdate(e);
		} else {
			if (compoundEdit == null) {
				compoundEdit = new CompoundEdit();
				;
			}
			compoundEdit.addEdit(e.getEdit());
		}
	}

	/*
	 * ------------------ custom document title handling start
	 * --------------------
	 */

	/**
	 * set the title of this SHTMLDocument
	 * 
	 * @param title
	 *            the title this document shall have
	 */
	public void setDocumentTitle(String title) {
		try {
			String titleHTML = "<title></title>";
			Element defaultRoot = getDefaultRootElement();
			Element head = Util.findElementDown(HTML.Tag.HEAD.toString(),
					defaultRoot);
			if (head != null) {
				Element pImpl = Util.findElementDown(HTML.Tag.IMPLIED
						.toString(), head);
				if (pImpl != null) {
					Element tElem = Util.findElementDown(HTML.Tag.TITLE
							.toString(), pImpl);
					if (tElem == null) {
						insertBeforeEnd(pImpl, titleHTML);
					}
				}
			} else {
				Element body = Util.findElementDown(HTML.Tag.BODY.toString(),
						defaultRoot);
				insertBeforeStart(body, "<head>" + titleHTML + "</head>");
			}
			putProperty(Document.TitleProperty, title);
		} catch (Exception e) {
			Util
					.errMsg(
							null,
							"An exception occurred while trying to insert the title",
							e);
		}
	}

	/**
	 * get the title of this SHTMLDocument
	 * 
	 * @return the title of this document or null if none was set so far
	 */
	public String getDocumentTitle() {
		Object title = getProperty(Document.TitleProperty);
		if (title != null) {
			return title.toString();
		} else {
			return null;
		}
	}

	/*
	 * ------------------ custom document title handling end
	 * --------------------
	 */

	/*
	 * ------------------ custom style sheet reference handling start
	 * --------------------
	 */

	/**
	 * insert a style sheet reference into the head of this SHTMLDocument
	 */
	public void insertStyleRef() {
		try {
			String styleRef = "  <link rel=stylesheet type=\"text/css\" href=\""
					+ DocumentPane.DEFAULT_STYLE_SHEET_NAME + "\">";
			Element defaultRoot = getDefaultRootElement();
			Element head = Util.findElementDown(HTML.Tag.HEAD.toString(),
					defaultRoot);
			if (head != null) {
				Element pImpl = Util.findElementDown(HTML.Tag.IMPLIED
						.toString(), head);
				if (pImpl != null) {
					Element link = Util.findElementDown(HTML.Tag.LINK
							.toString(), pImpl);
					if (link != null) {
						setOuterHTML(link, styleRef);
					} else {
						insertBeforeEnd(pImpl, styleRef);
					}
				}
			} else {
				Element body = Util.findElementDown(HTML.Tag.BODY.toString(),
						defaultRoot);
				insertBeforeStart(body, "<head>" + styleRef + "</head>");
			}
		} catch (Exception e) {
			Util
					.errMsg(
							null,
							"An exception occurred while trying to insert the style sheet reference link",
							e);
		}
	}

	/**
	 * check whether or not this SHTMLDocument has an explicit style sheet
	 * reference
	 * 
	 * @return true, if a style sheet reference was found, false if not
	 */
	public boolean hasStyleRef() {
		return (getStyleRef() != null);
	}

	/**
	 * get the style sheet reference of the document in this
	 * <code>DocumentPane</code>.
	 * 
	 * @return the reference to this document's style sheet or null if none is
	 *         found
	 */
	public String getStyleRef() {
		String linkName = null;
		Element link = Util.findElementDown(HTML.Tag.LINK.toString(),
				getDefaultRootElement());
		if (link != null) {
			Object href = link.getAttributes()
					.getAttribute(HTML.Attribute.HREF);
			if (href != null) {
				linkName = href.toString();
			}
		}
		return linkName;
	}

	/*
	 * ------------------ custom style sheet reference handling end
	 * --------------------
	 */

	/* -------- custom reader implementation start ------ */

	/**
	 * Fetches the reader for the parser to use to load the document with HTML.
	 * This is implemented to return an instance of SHTMLDocument.SHTMLReader.
	 */
	@Override
	public HTMLEditorKit.ParserCallback getReader(int pos) {
		Object desc = getProperty(Document.StreamDescriptionProperty);
		if (desc instanceof URL) {
			setBase((URL) desc);
		}
		SHTMLReader reader = new SHTMLReader(pos, getLength() == 0);
		return reader;
	}

	/**
	 * get the list element a given element is inside (if any).
	 * 
	 * @param elem
	 *            the element to get the list element for
	 * 
	 * @return the list element the given element is inside, or null, if the
	 *         given element is not inside a list
	 */
	static Element getListElement(Element elem) {
		Element list = Util.findElementUp(HTML.Tag.UL.toString(), elem);
		if (list == null) {
			list = Util.findElementUp(HTML.Tag.OL.toString(), elem);
		}
		return list;
	}

	/**
	 * get the list element a given element is inside (if any).
	 * 
	 * @param elem
	 *            the element to get the list element for
	 * 
	 * @return the list element the given element is inside, or null, if the
	 *         given element is not inside a list
	 */
	static Element getTableCellElement(Element elem) {
		Element list = Util.findElementUp(HTML.Tag.TD.toString(), elem);
		return list;
	}

	/**
	 * get the list item element a given element is inside (if any).
	 * 
	 * @param elem
	 *            the element to get the list element for
	 * 
	 * @return the list element the given element is inside, or null, if the
	 *         given element is not inside a list
	 */
	static Element getListItemElement(Element elem) {
		Element list = Util.findElementUp(HTML.Tag.LI.toString(), elem);
		return list;
	}

	/**
	 * This reader extends HTMLDocument.HTMLReader by the capability to handle
	 * SPAN tags
	 */
	public class SHTMLReader extends HTMLDocument.HTMLReader {

		/** action needed to handle SPAN tags */
		SHTMLCharacterAction ca = new SHTMLCharacterAction();

		/** the attributes found in a STYLE attribute */
		AttributeSet styleAttributes;

		/** indicates whether we're inside a SPAN tag */
		boolean inSpan = false;
		boolean emptyDocument;

		private int paragraphLevel;
		private boolean inBody;
		private boolean paragraphCreated;

		private boolean isParagraphTag;

		/**
		 * Constructor
		 * 
		 */
		public SHTMLReader(int offset, boolean emptyDocument) {
			super(offset, 0, 0, null);
			this.emptyDocument = emptyDocument;
			inBody = false;
			paragraphLevel = 0;
			paragraphCreated = false;
		}

		/**
		 * handle a start tag received by the parser
		 * 
		 * if it is a SPAN tag, convert the contents of the STYLE attribute to
		 * an AttributeSet and add it to the contents of this tag.
		 * 
		 * Otherwise let HTMLDocument.HTMLReader do the work.
		 */
		@Override
		public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
			if (t == HTML.Tag.BODY) {
				inBody = true;
			} else if (inBody) {
				isParagraphTag = isParagraphTag(t);
				if (isParagraphTag) {
					if (paragraphCreated && paragraphLevel == 0) {
						insertParagraphEndTag(pos);
					}
					paragraphLevel++;
				} else if (!paragraphCreated && paragraphLevel == 0) {
					insertParagraphStartTag(pos);
				}
			}
			if (t == HTML.Tag.SPAN) {
				handleStartSpan(a);
			} else {
				super.handleStartTag(t, a, pos);
				if (t == HTML.Tag.FONT) {
					charAttr.removeAttribute(t);
				}
			}
		}

		private void insertParagraphStartTag(int pos) {
			super.handleStartTag(HTML.Tag.P, new SimpleAttributeSet(), pos);
			paragraphCreated = true;
		}

		private void insertParagraphEndTag(int pos) {
			super.handleEndTag(HTML.Tag.P, pos);
			paragraphCreated = false;
		}

		private boolean isParagraphTag(Tag t) {
			if (paragraphElements == null) {
				paragraphElements = new HashSet();
				Object[] elementList = new Object[] { HTML.Tag.BLOCKQUOTE,
						HTML.Tag.DIR, HTML.Tag.DIV, HTML.Tag.DL, HTML.Tag.DT,
						HTML.Tag.FRAMESET, HTML.Tag.H1, HTML.Tag.H2,
						HTML.Tag.H3, HTML.Tag.H4, HTML.Tag.H5, HTML.Tag.H6,
						HTML.Tag.HR, HTML.Tag.LI, HTML.Tag.MENU, HTML.Tag.OL,
						HTML.Tag.P, HTML.Tag.PRE, HTML.Tag.TABLE, HTML.Tag.TD,
						HTML.Tag.TH, HTML.Tag.TR, HTML.Tag.UL };
				for (int i = 0; i < elementList.length; i++)
					paragraphElements.add(elementList[i]);
			}
			return paragraphElements.contains(t);
		}

		private void handleStartSpan(MutableAttributeSet a) {
			if (a.isDefined(HTML.Attribute.STYLE)) {
				String decl = (String) a.getAttribute(HTML.Attribute.STYLE);
				a.removeAttribute(HTML.Attribute.STYLE);
				styleAttributes = getStyleSheet().getDeclaration(decl);
				a.addAttributes(styleAttributes);
			} else {
				styleAttributes = null;
			}
			TagAction action = ca;

			if (action != null) {
				/**
				 * remember which part we're in for handleSimpleTag
				 */
				inSpan = true;

				action.start(HTML.Tag.SPAN, a);
			}
		}

		/**
		 * SPAN tags are directed to handleSimpleTag by the parser. If a SPAN
		 * tag is detected in this method, it gets redirected to handleStartTag
		 * and handleEndTag respectively.
		 */
		@Override
		public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
			if (inBody && !paragraphCreated && paragraphLevel == 0) {
				insertParagraphStartTag(pos);
			}
			if (t == HTML.Tag.SPAN) {
				if (inSpan) {
					handleEndTag(t, pos);
				} else {
					handleStartTag(t, a, pos);
				}
			} else {
				super.handleSimpleTag(t, a, pos);
			}
		}

		/**
		 * If a SPAN tag is directed to this method, end its action, otherwise,
		 * let HTMLDocument.HTMLReader do the work
		 */
		@Override
		public void handleEndTag(HTML.Tag t, int pos) {
			if (t == HTML.Tag.BODY) {
				if (paragraphCreated) {
					insertParagraphEndTag(pos);
				}
				inBody = false;
				if (emptyDocument) {
					super.handleStartTag(HTML.Tag.P, getEndingAttributeSet(),
							pos);
					super.handleText(" ".toCharArray(), pos);
					super.handleEndTag(HTML.Tag.P, pos);
				}
				super.handleEndTag(t, pos);
			} else if (t == HTML.Tag.SPAN) {
				handleEndSpan();
			} else {
				super.handleEndTag(t, pos);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.text.html.HTMLDocument.HTMLReader#handleComment(char[],
		 * int)
		 */
		@Override
		public void handleComment(char[] data, int pos) {
			if (emptyDocument) {
				super.handleComment(data, pos);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.text.html.HTMLDocument.HTMLReader#handleText(char[],
		 * int)
		 */
		@Override
		public void handleText(char[] data, int pos) {
			for (int i = 0; i < data.length; i++) {
				if (data[i] == '\u00A0') {
					data[i] = ' ';
				}
			}
			if (inBody && !paragraphCreated && paragraphLevel == 0) {
				insertParagraphStartTag(pos);
			}
			super.handleText(data, pos);
		}

		private void handleEndSpan() {
			TagAction action = ca;
			if (action != null) {
				/**
				 * remember which part we're in for handleSimpleTag
				 */
				inSpan = false;

				action.end(HTML.Tag.SPAN);
			}
		}

		/**
		 * this action is used to read the style attribute from a SPAN tag and
		 * to map from HTML to Java attributes.
		 */
		class SHTMLCharacterAction extends
				HTMLDocument.HTMLReader.CharacterAction {
			@Override
			public void start(HTML.Tag t, MutableAttributeSet attr) {
				pushCharacterStyle();
				if (attr.isDefined(IMPLIED)) {
					attr.removeAttribute(IMPLIED);
				}
				charAttr.addAttribute(t, attr.copyAttributes());
				if (styleAttributes != null) {
					charAttr.addAttributes(styleAttributes);
				}
				if (charAttr.isDefined(HTML.Tag.SPAN)) {
					charAttr.removeAttribute(HTML.Tag.SPAN);
				}
				// System.out.println("mapping attributes");
				charAttr = (MutableAttributeSet) new AttributeMapper(charAttr)
						.getMappedAttributes(AttributeMapper.toJava);
			}

			@Override
			public void end(HTML.Tag t) {
				popCharacterStyle();
			}
		}

	}

	/* -------- custom reader implementation end -------- */
	@Override
	public Element getParagraphElement(int pos) {
		return getParagraphElement(pos, inSetParagraphAttributes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.text.DefaultStyledDocument#getParagraphElement(int)
	 */
	public Element getParagraphElement(int pos, boolean noImplied) {
		Element element = super.getParagraphElement(pos);
		if (noImplied) {
			while (element != null
					&& element.getName().equalsIgnoreCase("p-implied")) {
				element = element.getParentElement();
			}
		}
		return element;
	}

	public int getLastDocumentPosition() {
		final int length = getLength();
		final int suffixLength = 1;
		return length > suffixLength ? length - suffixLength : length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.text.html.HTMLDocument#setParagraphAttributes(int, int,
	 * javax.swing.text.AttributeSet, boolean)
	 */
	@Override
	public void setParagraphAttributes(int offset, int length, AttributeSet s,
			boolean replace) {
		startCompoundEdit();
		super.setParagraphAttributes(offset, length, s, replace);
		inSetParagraphAttributes = true;
		super.setParagraphAttributes(offset, length, s, replace);
		inSetParagraphAttributes = false;
		endCompoundEdit();
	}

	public void removeParagraphAttributes(int offset, int length) {
		startCompoundEdit();
		// clear all paragraph attributes in selection
		for (int i = offset; i < offset + length;) {
			final Element paragraphElement = super.getParagraphElement(i);
			removeParagraphAtributes(paragraphElement);
			final int endOffset = paragraphElement.getEndOffset();
			i = endOffset;
		}
		endCompoundEdit();
	}

	private void removeParagraphAtributes(final Element paragraphElement) {
		if (paragraphElement != null
				&& paragraphElement.getName().equalsIgnoreCase("p-implied")) {
			removeParagraphAtributes(paragraphElement.getParentElement());
			return;
		}
		StringWriter writer = new StringWriter();
		SHTMLWriter htmlStartWriter = new SHTMLWriter(writer, this);
		try {
			htmlStartWriter.writeStartTag(paragraphElement.getName(), null);
			htmlStartWriter.writeChildElements(paragraphElement);
			htmlStartWriter.writeEndTag(paragraphElement.getName());
			setOuterHTML(paragraphElement, writer.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	private SimpleAttributeSet getEndingAttributeSet() {
		final SimpleAttributeSet set = new SimpleAttributeSet();
		if (Util.preferenceIsTrue("gray_row_below_end")) {
			StyleConstants.setBackground(set, Color.GRAY);
		}
		return set;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.text.html.HTMLDocument#getBase()
	 */
	@Override
	public URL getBase() {
		URL url = super.getBase();
		if (false == baseDirChecked) {
			baseDirChecked = true;
			File docDir = new File(url.getFile());
			if (!docDir.exists()) {
				docDir.mkdirs();
			}
			try {
				// XXX
				url = docDir.toURI().toURL();
				super.setBase(url);
				return url;
			} catch (MalformedURLException e) {
			}
		}
		return url;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.text.html.HTMLDocument#setBase(java.net.URL)
	 */
	@Override
	public void setBase(URL u) {
		baseDirChecked = false;
		super.setBase(u);
	}

}
