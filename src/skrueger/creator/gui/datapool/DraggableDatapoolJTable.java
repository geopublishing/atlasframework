/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package skrueger.creator.gui.datapool;

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

import javax.swing.DropMode;
import javax.swing.JList;

import org.apache.log4j.Logger;

import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.dp.DpRef;
import skrueger.atlas.gui.dnd.RJLTransferable;
import skrueger.atlas.gui.internal.DnDAtlasObject.AtlasDragSources;
import skrueger.creator.AtlasConfigEditable;

/**
 * The {@link DraggableDatapoolJTable} can be used as a source by DnD
 * action-COPY The {@link DpEntry} dragged from the {@link JList} is used to
 * create a {@link DpRef} which is then wrapped by a {@link RJLTransferable}
 * that is "send" over by the DnD
 * 
 * @author Stefan Alfons Krüger
 */
public class DraggableDatapoolJTable extends DataPoolJTable implements
		DragGestureListener, DragSourceListener {

	Logger log = Logger.getLogger(DraggableDatapoolJTable.class);

	private final DragSource dragSource;

	/**
	 * Creates a {@link DraggableDatapoolJTable}.
	 * 
	 * @param owner
	 * @param dp
	 */
	public DraggableDatapoolJTable(AtlasConfigEditable ace) {
		super(ace);
		dragSource = new DragSource();
		@SuppressWarnings("unused")
		DragGestureRecognizer dgr = dragSource
				.createDefaultDragGestureRecognizer(this,
						DnDConstants.ACTION_COPY, this);

		setDropMode(DropMode.INSERT_ROWS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.awt.dnd.DragGestureListener#dragGestureRecognized(java.awt.dnd.
	 * DragGestureEvent)
	 */
	public void dragGestureRecognized(DragGestureEvent dge) {
		log.debug("drag gesture recognized");

		final int index = rowAtPoint(dge.getDragOrigin());
		if (index == -1)
			return;

		// What else could be in there ?! This is a Draggable-MapPool-JList
		DpEntry draggedDatapoolEntry = getDataPool().get(
				convertRowIndexToModel(index));

		DpRef datapoolRef = new DpRef(draggedDatapoolEntry);
		// Vorher nur die ref, jetzt den dpentry
		Transferable trans = new RJLTransferable(datapoolRef.getTarget(),
				AtlasDragSources.DATAPOOLLIST, DpEntry.class);

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
	public void dragDropEnd(DragSourceDropEvent dsde) {
		// draggedIndex = -1;
		// log.debug("dragDropEnd");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.dnd.DragSourceListener#dragEnter(java.awt.dnd.DragSourceDragEvent
	 * )
	 */
	public void dragEnter(DragSourceDragEvent dsde) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.dnd.DragSourceListener#dragExit(java.awt.dnd.DragSourceEvent)
	 */
	public void dragExit(DragSourceEvent dse) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.dnd.DragSourceListener#dragOver(java.awt.dnd.DragSourceDragEvent
	 * )
	 */
	public void dragOver(DragSourceDragEvent dsde) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.awt.dnd.DragSourceListener#dropActionChanged(java.awt.dnd.
	 * DragSourceDragEvent)
	 */
	public void dropActionChanged(DragSourceDragEvent dsde) {
	}

}
