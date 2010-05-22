/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasViewer.swing.internal;

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
