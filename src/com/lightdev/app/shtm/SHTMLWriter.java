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

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLWriter;

/**
 * FixedHTMLWriter
 * 
 * 
 */

class SHTMLWriter extends HTMLWriter {
	private Element elem;
	private boolean replaceEntities;
	private boolean inTextArea;

	final private MutableAttributeSet convAttr = new SimpleAttributeSet();
	private boolean inPre;

	public SHTMLWriter(Writer w, HTMLDocument doc, int pos, int len) {
		super(w, doc, pos, len);
	}

	public SHTMLWriter(Writer w, HTMLDocument doc) {
		this(w, doc, 0, doc.getLength());
	}

	@Override
	protected ElementIterator getElementIterator() {
		if (elem == null)
			return super.getElementIterator();
		return new ElementIterator(elem);
	}

	@Override
	protected void output(char[] chars, int start, int length)
			throws IOException {
		if (replaceEntities) {
			if (chars[start] == ' ') {
				chars[start] = '\u00A0';
			}
			final int last = start + length - 1;
			for (int i = start + 1; i < last; i++) {
				if (chars[i] == ' '
						&& (chars[i - 1] == '\u00A0' || chars[i + 1] == ' ')) {
					chars[i] = '\u00A0';
				}
			}
			// if(chars[last] == ' '){
			// chars[last] = '\u00A0';
			// }
		}
		super.output(chars, start, length);
	}

	@Override
	protected void startTag(Element elem) throws IOException,
			BadLocationException {
		if (matchNameAttribute(elem.getAttributes(), HTML.Tag.PRE)) {
			inPre = true;
		}
		super.startTag(elem);
	}

	@Override
	protected void endTag(Element elem) throws IOException {
		if (matchNameAttribute(elem.getAttributes(), HTML.Tag.PRE)) {
			inPre = false;
		}
		super.endTag(elem);
	}

	@Override
	protected void text(Element elem) throws BadLocationException, IOException {
		replaceEntities = !inPre;
		super.text(elem);
		replaceEntities = false;
	}

	@Override
	protected void textAreaContent(AttributeSet attr)
			throws BadLocationException, IOException {
		inTextArea = true;
		super.textAreaContent(attr);
		inTextArea = false;
	}

	@Override
	public void write() throws IOException, BadLocationException {
		replaceEntities = false;
		super.write();
	}

	@Override
	protected void writeLineSeparator() throws IOException {
		boolean pre = replaceEntities;
		replaceEntities = false;
		super.writeLineSeparator();
		replaceEntities = pre;
	}

	@Override
	protected void indent() throws IOException {
		if (inTextArea) {
			return;
		}
		boolean pre = replaceEntities;
		replaceEntities = false;
		super.indent();
		replaceEntities = pre;
	}

	/**
	 * Iterates over the Element tree and controls the writing out of all the
	 * tags and its attributes.
	 * 
	 * @exception IOException
	 *                on any I/O error
	 * @exception BadLocationException
	 *                if pos represents an invalid location within the document.
	 * 
	 */
	synchronized void write(Element elem) throws IOException,
			BadLocationException {
		this.elem = elem;
		try {
			write();
		} catch (BadLocationException e) {
			elem = null;
			throw e;
		} catch (IOException e) {
			elem = null;
			throw e;
		}
	}

	/**
	 * invoke HTML creation for all children of a given element.
	 * 
	 * @param elem
	 *            the element which children are to be written as HTML
	 */
	public void writeChildElements(Element elem) throws IOException,
			BadLocationException {
		Element para;
		for (int i = 0; i < elem.getElementCount(); i++) {
			para = elem.getElement(i);
			write(para);
		}
	}

	@Override
	protected boolean inRange(Element next) {
		if (next.getStartOffset() >= ((SHTMLDocument) next.getDocument())
				.getLastDocumentPosition()) {
			return false;
		}
		int startOffset = getStartOffset();
		int endOffset = getEndOffset();
		if ((next.getStartOffset() >= startOffset
				&& (next.getStartOffset() < endOffset) || next.getEndOffset() - 1 == endOffset)
				|| (startOffset >= next.getStartOffset() && startOffset < next
						.getEndOffset())) {
			return true;
		}
		return false;
	}

	/**
	 * Create an older style of HTML attributes. This will convert character
	 * level attributes that have a StyleConstants mapping over to an HTML
	 * tag/attribute. Other CSS attributes will be placed in an HTML style
	 * attribute.
	 */
	private static void convertToHTML(AttributeSet from, MutableAttributeSet to) {
		if (from == null) {
			return;
		}
		Enumeration keys = from.getAttributeNames();
		String value = "";
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			if (key instanceof CSS.Attribute) {
				// default is to store in a HTML style attribute
				if (value.length() > 0) {
					value = value + "; ";
				}
				value = value + key + ": " + from.getAttribute(key);
			} else {
				to.addAttribute(key, from.getAttribute(key));
			}
		}
		if (value.length() > 0) {
			to.addAttribute(HTML.Attribute.STYLE, value);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejavax.swing.text.html.HTMLWriter#writeAttributes(javax.swing.text.
	 * AttributeSet)
	 */
	@Override
	protected void writeAttributes(AttributeSet attr) throws IOException {
		// translate css attributes to html
		if (attr instanceof Element) {
			Element elem = (Element) attr;
			if (elem.isLeaf() || elem.getName().equalsIgnoreCase("p-implied")) {
				super.writeAttributes(attr);
				return;
			}
		}
		convAttr.removeAttributes(convAttr);
		convertToHTML(attr, convAttr);

		Enumeration names = convAttr.getAttributeNames();
		while (names.hasMoreElements()) {
			Object name = names.nextElement();
			if (name instanceof HTML.Tag || name instanceof StyleConstants
					|| name == HTML.Attribute.ENDTAG) {
				continue;
			}
			write(" " + name + "=\"" + convAttr.getAttribute(name) + "\"");
		}
	}

	/**
	 * write an element and all its children. If a given element is reached,
	 * writing stops with this element. If the end element is a leaf, it is
	 * written as the last element, otherwise it is not written.
	 * 
	 * @param e
	 *            the element to write including its children (if any)
	 * @param end
	 *            the last leaf element to write or the branch element to stop
	 *            writing at (whatever applies)
	 */
	private void writeElementsUntil(Element e, Element end) throws IOException,
			BadLocationException {
		if (e.isLeaf()) {
			write(e);
		} else {
			if (e != end) {
				startTag(e);
				int childCount = e.getElementCount();
				int index = 0;
				while (index < childCount) {
					writeElementsUntil(e.getElement(index), end); // drill down
					// in
					// recursion
					index++;
				}
				endTag(e);
			}
		}
	}

	/**
	 * write elements and their children starting at a given element until a
	 * given element is reached. The end element is written as the last element,
	 * if it is a leaf element.
	 * 
	 * @param start
	 *            the element to start writing with
	 * @param end
	 *            the last element to write
	 */
	void write(Element start, Element end) throws IOException,
			BadLocationException {
		Element parent = start.getParentElement();
		int count = parent.getElementCount();
		int i = 0;
		Element e = parent.getElement(i);
		while (i < count && e != start) {
			e = parent.getElement(i++);
		}
		while (i < count) {
			writeElementsUntil(e, end);
			e = parent.getElement(i++);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.text.html.HTMLWriter#startTag(javax.swing.text.Element)
	 */
	void writeStartTag(Element elem) throws IOException, BadLocationException {
		// TODO Auto-generated method stub
		super.startTag(elem);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.text.html.HTMLWriter#endTag(javax.swing.text.Element)
	 */
	void writeEndTag(Element elem) throws IOException {
		// TODO Auto-generated method stub
		super.endTag(elem);
	}

	void writeEndTag(String elementName) throws IOException {
		indent();
		write('<');
		write('/');
		write(elementName);
		write('>');
		writeLineSeparator();
	}

	void writeStartTag(String elementName, AttributeSet attributes)
			throws IOException {
		indent();
		write('<');
		write(elementName);
		if (attributes != null) {
			writeAttributes(attributes);
		}
		write('>');
		writeLineSeparator();
	}
}
