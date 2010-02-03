/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the AtlasViewer application - A GIS viewer application targeting at end-users with no GIS-experience. Its main purpose is to present the atlases created with the Geopublisher application.
 * http://www.geopublishing.org
 * 
 * AtlasViewer is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.atlas.gui.internal;

public class DnDAtlasObject {

	/**
	 * Enumeration of components that can be potentially valid sources for the
	 * drop event.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 * 
	 */
	public enum AtlasDragSources {
		UNDEFINED, DATAPOOLLIST, LAYERPANEGROUP, MAPPOOLLIST, LAYERLIST, DNDTREE, REORDERJLIST
	};

	private final Object object;

	private final AtlasDragSources source;

	private final Class<?> classOfObj;

	public Object getObject() {
		return object;
	}

	public AtlasDragSources getSource() {
		return source;
	}

	public DnDAtlasObject(Object o, AtlasDragSources source, Class<?> classOfObj) {
		this.object = o;
		this.source = source;
		this.classOfObj = classOfObj;
	}

	public Class<?> getClassOfObj() {
		return classOfObj;
	}

}