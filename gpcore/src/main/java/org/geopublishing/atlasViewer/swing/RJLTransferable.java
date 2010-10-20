/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasViewer.swing;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.swing.internal.DnDAtlasObject;

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
	public RJLTransferable(Object o, org.geopublishing.atlasViewer.swing.internal.DnDAtlasObject.AtlasDragSources source,
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
