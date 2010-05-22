/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.gui.map;

import java.awt.Cursor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.map.MapRef;
import org.geopublishing.atlasViewer.swing.RJLTransferable;
import org.geopublishing.atlasViewer.swing.internal.DnDAtlasObject.AtlasDragSources;
import org.geopublishing.geopublisher.AtlasConfigEditable;

import skrueger.i8n.Translation;

/**
 * A {@link DraggableMapPoolJTable} is a {@link MapPoolJList} with the ability
 * to interact in Drag'n'Drop as a Source. The selected {@link Map}s will be
 * used to create a {@link MapRef} object which is "send" over DnD as a
 * {@link RJLTransferable} object.
 * 
 * @author Stefan Alfons Tzeggai
 * 
 */
public class DraggableMapPoolJTable extends MapPoolJTable implements
		DragGestureListener, DragSourceListener {

	static final private Logger log = Logger
			.getLogger(DraggableMapPoolJTable.class);

	private final DragSource dragSource;

	/**
	 * Creates a {@link DraggableMapPoolJTable}
	 * 
	 * @param mapPool
	 */
	public DraggableMapPoolJTable(AtlasConfigEditable ace) {
		super(ace);
		dragSource = new DragSource();
		@SuppressWarnings("unused")
		DragGestureRecognizer dgr = dragSource
				.createDefaultDragGestureRecognizer(this,
						DnDConstants.ACTION_COPY, this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.awt.dnd.DragGestureListener#dragGestureRecognized(java.awt.dnd.
	 * DragGestureEvent)
	 */
	@Override
	public void dragGestureRecognized(DragGestureEvent dge) {
		log.debug("drag gesture recognized");

		final int index = rowAtPoint(dge.getDragOrigin());
		if (index == -1)
			return;

		// What else could be in there ?! This is a Draggable-MapPool-JList
		final Map draggedMap = getMapPool().get(convertRowIndexToModel(index));

		final MapRef mapRef = new MapRef(draggedMap, getMapPool());
		Transferable trans = new RJLTransferable(mapRef,
				AtlasDragSources.MAPPOOLLIST, MapRef.class);

		dragSource.startDrag(dge, Cursor
				.getPredefinedCursor(Cursor.MOVE_CURSOR), trans, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.dnd.DragSourceListener#dragDropEnd(java.awt.dnd.DragSourceDropEvent
	 * )
	 */
	@Override
	public void dragDropEnd(DragSourceDropEvent dsde) {
		// log.debug("dragDropEnd");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.dnd.DragSourceListener#dragEnter(java.awt.dnd.DragSourceDragEvent
	 * )
	 */
	@Override
	public void dragEnter(DragSourceDragEvent dsde) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.dnd.DragSourceListener#dragExit(java.awt.dnd.DragSourceEvent)
	 */
	@Override
	public void dragExit(DragSourceEvent dse) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.dnd.DragSourceListener#dragOver(java.awt.dnd.DragSourceDragEvent
	 * )
	 */
	@Override
	public void dragOver(DragSourceDragEvent dsde) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.awt.dnd.DragSourceListener#dropActionChanged(java.awt.dnd.
	 * DragSourceDragEvent)
	 */
	@Override
	public void dropActionChanged(DragSourceDragEvent dsde) {
	}

	public void dispose() {
		Translation.removeLocaleChangeListener(localeChangeListener);
	}

}
