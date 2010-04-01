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

import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.CSS;

/**
 * a panel to display and change line attributes
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
class EffectPanel extends JPanel implements AttributeComponent {

	/** a radio button for the underline attribute */
	JRadioButton uLine;

	/** a radio button for the strike through attribute */
	JRadioButton strike;

	/** a radio button if no line effect is set */
	JRadioButton noLine;

	private Object originalValue;

	private int setValCount = 0;

	String selection = Util.CSS_ATTRIBUTE_NONE;

	public EffectPanel() {
		super(new GridLayout(3, 1, 3, 3));

		/** initialize the line effects button group */
		noLine = new JRadioButton(Util.getResourceString("noLineLabel"));
		uLine = new JRadioButton(Util.getResourceString("uLineLabel"));
		strike = new JRadioButton(Util.getResourceString("strikeLabel"));
		ButtonGroup effectGroup = new ButtonGroup();
		effectGroup.add(noLine);
		effectGroup.add(uLine);
		effectGroup.add(strike);

		// JPanel linePanel = new JPanel(new GridLayout(3,1,3,3));
		setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), Util
				.getResourceString("effectLabel")));
		Font font = UIManager.getFont("TextField.font");
		uLine.setFont(font);
		strike.setFont(font);
		noLine.setFont(font);
		add(noLine);
		add(uLine);
		add(strike);
	}

	public AttributeSet getValue(boolean includeUnchanged) {
		if (includeUnchanged) {
			return getAttributes();
		} else {
			return getValue();
		}
	}

	private AttributeSet getAttributes() {
		SimpleAttributeSet set = new SimpleAttributeSet();
		selection = Util.CSS_ATTRIBUTE_NONE;
		if (uLine.isSelected()) {
			selection = Util.CSS_ATTRIBUTE_UNDERLINE;
			StyleConstants.setUnderline(set, true);
		} else if (strike.isSelected()) {
			selection = Util.CSS_ATTRIBUTE_LINE_THROUGH;
			StyleConstants.setStrikeThrough(set, true);
		}
		Util.styleSheet().addCSSAttribute(set, CSS.Attribute.TEXT_DECORATION,
				selection);
		return set;
	}

	public AttributeSet getValue() {
		final AttributeSet set = getAttributes();
		if (((originalValue == null) && (!selection
				.equalsIgnoreCase(Util.CSS_ATTRIBUTE_NONE)))
				|| ((originalValue != null) && (!originalValue.toString()
						.equalsIgnoreCase(selection)))) {
			return set;
		} else {
			return new SimpleAttributeSet();
		}
	}

	public boolean setValue(AttributeSet a) {
		boolean success = false;
		if (a.isDefined(CSS.Attribute.TEXT_DECORATION)) {
			String value = a.getAttribute(CSS.Attribute.TEXT_DECORATION)
					.toString();
			if (value.equalsIgnoreCase(Util.CSS_ATTRIBUTE_UNDERLINE)) {
				uLine.setSelected(true);
				if (++setValCount < 2) {
					originalValue = Util.CSS_ATTRIBUTE_UNDERLINE;
				}
				success = true;
			} else if (value.equalsIgnoreCase(Util.CSS_ATTRIBUTE_LINE_THROUGH)) {
				strike.setSelected(true);
				if (++setValCount < 2) {
					originalValue = Util.CSS_ATTRIBUTE_LINE_THROUGH;
				}
				success = true;
			} else {
				noLine.setSelected(true);
			}
		} else {
			noLine.setSelected(true);
		}
		return success;
	}
}
