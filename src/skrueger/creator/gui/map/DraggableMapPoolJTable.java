/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the Geopublisher application - An authoring tool to facilitate the publication and distribution of geoproducts in form of online and/or offline end-user GIS.
 * http://www.geopublishing.org
 * 
 * Geopublisher is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.creator.gui.map;

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

import skrueger.atlas.gui.dnd.RJLTransferable;
import skrueger.atlas.gui.internal.DnDAtlasObject.AtlasDragSources;
import skrueger.atlas.map.Map;
import skrueger.atlas.map.MapRef;
import skrueger.creator.AtlasConfigEditable;
import skrueger.i8n.Translation;

/**
 * A {@link DraggableMapPoolJTable} is a {@link MapPoolJList} with the ability
 * to interact in Drag'n'Drop as a Source. The selected {@link Map}s will be
 * used to create a {@link MapRef} object which is "send" over DnD as a
 * {@link RJLTransferable} object.
 * 
 * @author Stefan Alfons Krüger
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
