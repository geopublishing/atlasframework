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

import javax.swing.text.AttributeSet;

/**
 * Defines a set of methods common to components bound to AttributeSets.
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

public interface AttributeComponent {

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
	public boolean setValue(AttributeSet a);

	/**
	 * get the value of this <code>AttributeComponent</code>
	 * 
	 * @return the value selected from this component
	 */
	public AttributeSet getValue();

	public AttributeSet getValue(boolean includeUnchanged);

}
