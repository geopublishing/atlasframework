/*
 * SimplyHTML, a word processor based on Java, HTML and CSS
 * Created on 23.11.2006
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

import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Default implementation of TextResources based on java.util.ResourceBundle
 * 
 * @author Dimitri Polivaev 14.01.2007
 */
public class DefaultTextResources implements TextResources {
	private Properties properties;
	private ResourceBundle resources;

	public DefaultTextResources(ResourceBundle languageResources) {
		this(languageResources, null);
	}

	public DefaultTextResources(ResourceBundle languageResources,
			Properties properties) {
		super();
		this.resources = languageResources;
		this.properties = properties;
	}

	public String getString(String pKey) {
		try {
			return resources.getString(pKey);
		} catch (MissingResourceException ex) {
			if (properties != null) {
				return properties.getProperty(pKey);
			}
			throw ex;
		}
	}
}
