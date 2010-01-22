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
package skrueger.atlas.gui.dnd;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.apache.log4j.Logger;

import skrueger.atlas.gui.internal.DnDAtlasObject;
import skrueger.atlas.gui.internal.DnDAtlasObject.AtlasDragSources;

/**
 * A {@link Transferable} that is designed to transfer a {@link DnDAtlasObject}
 * s.
 */
public class RJLTransferable implements Transferable {
	static private final Logger LOGGER = Logger
			.getLogger(RJLTransferable.class);

	static public DataFlavor localObjectFlavor;
	static {
		try {
			localObjectFlavor = new DataFlavor(
					DataFlavor.javaJVMLocalObjectMimeType);
		} catch (ClassNotFoundException cnfe) {
			LOGGER.error(cnfe);
		}
	}

	static public DataFlavor[] supportedFlavors = { localObjectFlavor };

	Object object;

	/**
	 * This is transferable by Drag'n'Drop
	 * 
	 * @param o
	 * @param source
	 *            Determine where this drag comes from. Needed to reaact
	 *            correctly when a {@link Component} is a multiple DropTarget
	 */
	public RJLTransferable(Object o, AtlasDragSources source,
			Class<?> classOfObj) {
		object = new DnDAtlasObject(o, source, classOfObj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer
	 * .DataFlavor)
	 */
	public Object getTransferData(DataFlavor df)
			throws UnsupportedFlavorException, IOException {
		if (isDataFlavorSupported(df))
			return object;
		else
			throw new UnsupportedFlavorException(df);
	}

	public boolean isDataFlavorSupported(DataFlavor df) {
		return (df.equals(localObjectFlavor));
	}

	public DataFlavor[] getTransferDataFlavors() {
		return supportedFlavors;
	}
}
