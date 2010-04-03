/*
 * SimplyHTML, a word processor based on Java, HTML and CSS
 * Copyright (C) 2002, 2003 Ulrich Hilger
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
import java.util.Enumeration;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JEditorPane;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;

/**
 * An editor pane with syntax highlighting for HTML tags.
 * 
 * <p>
 * This is a basic approach to syntax highlighting using regular expressions. It
 * is used to make the plain HTML view of application SimplyHTML more legible by
 * separating tags and attributes from content.
 * </p>
 * 
 * <p>
 * The method doing the actual highlighting work is put into an implementation
 * of the Runnable interface so that it can be called without conflicts.
 * </p>
 * 
 * <p>
 * Can be refined in the way Patterns are set up, i.e. not hard wire pattern
 * setup and a GUI to let the use choose styles for patterns.
 * </p>
 * 
 * <p>
 * Recommended readings:<br>
 *'Regular Expressions and the JavaTM Programming Language' at<br>
 * <a href=
 * "http://developer.java.sun.com/developer/technicalArticles/releases/1.4regex/"
 * target ="_blank">http://developer.java.sun.com/developer/technicalArticles/
 * releases /1.4regex/</a><br>
 * and<br>
 * Presentation slides 'Rich Clients for Web Services' from JavaOne 2002 at<br>
 * <a href="http://servlet.java.sun.com/javaone/resources/content/sf2002/conf/sessions/pdfs/2274.pdf"
 * target
 * ="_blank">http://servlet.java.sun.com/javaone/resources/content/sf2002/conf
 * /sessions/pdfs/2274.pdf</a>
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
 */
public class SyntaxPane extends JEditorPane implements CaretListener {

	/**
	 * Creates a new <code>SyntaxPane</code>.
	 */
	public SyntaxPane() {
		super();
		setEditorKit(new StyledEditorKit());
		setupPatterns();
	}

	/**
	 * set up HTML patterns and attributes
	 */
	private void setupPatterns() {

		Pattern p;
		SimpleAttributeSet set;
		patterns = new Vector();

		// content text
		p = Pattern.compile("\\b\\w+");
		set = new SimpleAttributeSet();
		StyleConstants.setForeground(set, Color.BLACK);
		StyleConstants.setBold(set, false);
		patterns.addElement(new RegExStyle(p, set));

		// a tag
		p = Pattern.compile("<[/a-zA-Z0-9\\s]+");
		set = new SimpleAttributeSet();
		StyleConstants.setForeground(set, new Color(0, 0, 128));
		StyleConstants.setBold(set, true);
		patterns.addElement(new RegExStyle(p, set));

		// a tag end
		p = Pattern.compile(">");
		patterns.addElement(new RegExStyle(p, set));

		// an attribute
		p = Pattern.compile("\\s[/a-zA-Z0-9]+=");
		set = new SimpleAttributeSet();
		StyleConstants.setForeground(set, new Color(158, 119, 0));
		StyleConstants.setBold(set, true);
		patterns.addElement(new RegExStyle(p, set));

		// attribute values
		p = Pattern.compile("\"[\\x2D;:/.%#?=,\\w\\s]+\"");
		set = new SimpleAttributeSet();
		StyleConstants.setForeground(set, Color.BLUE);
		StyleConstants.setBold(set, false);
		patterns.addElement(new RegExStyle(p, set));
	}

	/**
	 * apply syntax highlighting to all HTML tags found in the given area of the
	 * given document
	 * 
	 * @param doc
	 *            the document to apply syntax highlighting to
	 * @param offset
	 *            the position inside the given document to start to apply
	 *            syntax highlighting to
	 * @param len
	 *            the number of characters to apply syntax highlighting to
	 */
	public void setMarks(StyledDocument sDoc, int offset, int len) {
		SwingUtilities.invokeLater(new StyleUpdater(this, sDoc, offset, len));
	}

	/**
	 * overridden from JEditorPane to suppress line wraps
	 * 
	 * @see setSize
	 */
	@Override
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	/**
	 * overridden from JEditorPane to suppress line wraps
	 * 
	 * @see getScrollableTracksViewportWidth
	 */
	@Override
	public void setSize(Dimension d) {
		if (d.width < getParent().getSize().width) {
			d.width = getParent().getSize().width;
		}
		super.setSize(d);
	}

	/**
	 * display a wait cursor for lengthy operations
	 */
	private void cursor() {
		final JRootPane rootPane = getRootPane();
		if (rootPane != null) {
			Component gp = rootPane.getGlassPane();
			if (!gp.isVisible()) {
				gp.setCursor(waitCursor);
				gp.setVisible(true);
			} else {
				gp.setVisible(false);
			}
		}
	}

	/**
	 * StyleUpdater does the actual syntax highlighting work and can be used in
	 * SwingUtilities.invokeLater()
	 */
	private class StyleUpdater implements Runnable {

		/**
		 * construct a <code>StyleUpdater</code>
		 * 
		 * @param sp
		 *            the SyntaxPane this StyleUpdater works on
		 * @param doc
		 *            the document to apply syntax highlighting to
		 * @param offset
		 *            the position inside the given document to start to apply
		 *            syntax highlighting to
		 * @param len
		 *            the number of characters to apply syntax highlighting to
		 */
		public StyleUpdater(SyntaxPane sp, StyledDocument doc, int offset,
				int len) {
			this.sDoc = doc;
			this.offset = offset;
			this.len = len;
			this.sp = sp;
		}

		/**
		 * apply snytax highlighting
		 */
		public void run() {
			Matcher m;
			RegExStyle style;
			cursor();
			sp.removeCaretListener(sp);
			try {
				int length = sDoc.getLength();
				if (length > 0 && len > 0) {
					String text = sDoc.getText(offset, len);
					if (text != null && text.length() > 0) {
						Enumeration pe = patterns.elements();
						while (pe.hasMoreElements()) {
							style = (RegExStyle) pe.nextElement();
							m = style.getPattern().matcher(text);
							while (m.find()) {
								sDoc.setCharacterAttributes(offset + m.start(),
										m.end() - m.start(), style.getStyle(),
										true);
							}
						}
					}
				}
			} catch (Exception ex) {
				System.out.println("StyleUpdater ERROR: " + ex.getMessage());
			}
			sp.addCaretListener(sp);
			cursor();
		}

		/** the document to apply syntax highlighting to */
		private StyledDocument sDoc;

		/**
		 * the position inside the given document to start to apply syntax
		 * highlighting to
		 */
		private int offset;

		/** the number of characters to apply syntax highlighting to */
		private int len;

		/** the SyntaxPane this StyleUpdater works on */
		private SyntaxPane sp;
	}

	/**
	 * CaretListener implementation
	 * 
	 * <p>
	 * updates syntax highlighting for the current line when the caret moves
	 * </p>
	 */
	public void caretUpdate(CaretEvent e) {
		try {
			StyledDocument sDoc = (StyledDocument) getDocument();
			int cPos = e.getDot();
			int length = sDoc.getLength();
			String text = sDoc.getText(0, length);
			int lineStart = text.substring(0, cPos).lastIndexOf("\n") + 1;
			int lineEnd = text.indexOf("\n", cPos);
			if (lineEnd < 0) {
				lineEnd = length;
			}
			setMarks(sDoc, lineStart, lineEnd - lineStart);
		} catch (Exception ex) {
		}
	}

	/**
	 * overridden to keep caret changes during the initial text load from
	 * triggering syntax highlighting repetitively
	 */
	@Override
	public void setText(String t) {
		removeCaretListener(this);
		super.setText(t);
		StyledDocument sDoc = (StyledDocument) getDocument();
		setMarks(sDoc, 0, sDoc.getLength());
		setCaretPosition(0);
		addCaretListener(this);
	}

	/**
	 * convenience class associating a pattern with a set of attributes
	 */
	class RegExStyle {

		/**
		 * construct a <code>RegExStyle</code> instance
		 * 
		 * @param p
		 *            the <code>Pattern</code> to apply this style to
		 * @param a
		 *            the attributes making up this style
		 */
		public RegExStyle(Pattern p, AttributeSet a) {
			this.p = p;
			this.a = a;
		}

		/**
		 * get the <code>Pattern</code> this style is to be applied to
		 * 
		 * @return the Pattern
		 */
		public Pattern getPattern() {
			return p;
		}

		/**
		 * get the attributes making up this style
		 * 
		 * @return the set of attributes
		 */
		public AttributeSet getStyle() {
			return a;
		}

		/**
		 * set the Pattern to apply a given set of attributes to
		 * 
		 * @param p
		 *            the Pattern
		 */
		public void setPattern(Pattern p) {
			this.p = p;
		}

		/**
		 * set the set of attributes to apply to a given Pattern
		 * 
		 * @param a
		 *            the set of attributes to use
		 */
		public void setStyle(AttributeSet a) {
			this.a = a;
		}

		/** the Pattern to apply this style to */
		private Pattern p;

		/** the attributes making up this style */
		private AttributeSet a;
	}

	/** Patterns registered with this SnytaxPane */
	private Vector patterns;

	/** the cursor to use to indicate a lengthy operation is going on */
	private Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);

}
