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

import java.awt.GridLayout;

import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.html.CSS;

/**
 * Panel to set text margin attributes.
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

class MarginPanel extends AttributePanel {

	private BoundariesPanel margin;
	private BoundariesPanel padding;

	public MarginPanel() {
		super();

		// margin/padding panel
		setLayout(new GridLayout(2, 1, 3, 3));

		// construct margin panel
		margin = new BoundariesPanel(CSS.Attribute.MARGIN);

		// set border and title and add margin panel
		margin.setBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED), Util
						.getResourceString("marginLabel")));
		this.add(margin);

		// construct padding panel
		padding = new BoundariesPanel(CSS.Attribute.PADDING);

		// set border and title adn add padding panel
		padding.setBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED), Util.getResourceString("paddingLabel")));
		this.add(padding);
	}

	public void reset() {
		margin.reset();
		padding.reset();
	}
}
