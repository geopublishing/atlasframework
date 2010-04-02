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
package org.geopublishing.geopublisher.gui.group;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
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
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import javax.swing.DropMode;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AVUtil;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.DpRef;
import org.geopublishing.atlasViewer.map.MapRef;
import org.geopublishing.atlasViewer.swing.dnd.RJLTransferable;
import org.geopublishing.atlasViewer.swing.internal.DnDAtlasObject;
import org.geopublishing.atlasViewer.swing.internal.DnDAtlasObject.AtlasDragSources;


/**
 * A {@link JTree} that is reorderable by D'n'D
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
 */
public class CopyOfDnDJTree extends JTree implements DragSourceListener,
		DropTargetListener, DragGestureListener {
	private static final Logger LOGGER = Logger.getLogger(CopyOfDnDJTree.class);

	// ****************************************************************************
	// D'n'D stuff
	// ****************************************************************************
	static DataFlavor localObjectFlavor;
	static {
		try {
			localObjectFlavor = new DataFlavor(
					DataFlavor.javaJVMLocalObjectMimeType);
		} catch (ClassNotFoundException cnfe) {
			LOGGER.error(cnfe);
		}
	}

	static DataFlavor[] supportedFlavors = { localObjectFlavor };

	DragSource dragSource;

	DropTarget dropTarget;

	TreeNode dropTargetNode, draggedNode;

	private Logger log = Logger.getLogger(CopyOfDnDJTree.class);

	/*
	 * 
	 */
	public CopyOfDnDJTree() {
		// setCellRenderer (new DnDTreeCellRenderer());

		// ****************************************************************************
		// D'n'D Stuff
		// ****************************************************************************
		dragSource = new DragSource();
		@SuppressWarnings("unused")
		DragGestureRecognizer dgr = dragSource
				.createDefaultDragGestureRecognizer(this,
						DnDConstants.ACTION_MOVE, this);
		dropTarget = new DropTarget(this, this);

		setDropMode(DropMode.INSERT_ROWS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.awt.dnd.DragGestureListener#dragGestureRecognized(java.awt.dnd.
	 * DragGestureEvent)
	 */
	public void dragGestureRecognized(DragGestureEvent dge) {
		// log.debug ("dragGestureRecognized");

		Point clickPoint = dge.getDragOrigin();
		TreePath path = getPathForLocation(clickPoint.x, clickPoint.y);
		if (path == null) {
			log.debug("not on a node");
			return;
		}
		draggedNode = (TreeNode) path.getLastPathComponent();
		Transferable trans = new RJLTransferable(draggedNode,
				AtlasDragSources.DNDTREE, TreeNode.class);
		dragSource.startDrag(dge, Cursor
				.getPredefinedCursor(Cursor.MOVE_CURSOR), trans, this);
	}

	// DragSourceListener events
	public void dragDropEnd(DragSourceDropEvent dsde) {
		// log.debug ("dragDropEnd()");
		dropTargetNode = null;
		draggedNode = null;
		repaint();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent
	 * )
	 */
	public void dragEnter(DropTargetDragEvent dtde) {
		dtde.acceptDrag(DnDConstants.ACTION_MOVE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
	 */
	public void dragExit(DropTargetEvent dte) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent
	 * )
	 */
	public void dragOver(DropTargetDragEvent dtde) {
		// figure out which cell it's over, no drag to self
		Point dragPoint = dtde.getLocation();
		TreePath path = getPathForLocation(dragPoint.x, dragPoint.y);
		if (path == null)
			dropTargetNode = null;
		else
			dropTargetNode = (TreeNode) path.getLastPathComponent();
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
	 */
	public void drop(DropTargetDropEvent dtde) {
		Point dropPoint = dtde.getLocation();

		TreePath path = getPathForLocation(dropPoint.x, dropPoint.y);
		// log.debug("drop path is " + path);

		// Is evaluated in the finally block
		boolean dropped = false;
		try {
			if (path == null)
				return;

			dtde.acceptDrop(DnDConstants.ACTION_MOVE);
			// log.debug("Drop accepted. Transfering...");

			Object droppedObject = dtde.getTransferable().getTransferData(
					localObjectFlavor);

			DefaultMutableTreeNode dropTargetNode = (DefaultMutableTreeNode) path
					.getLastPathComponent();

			MutableTreeNode droppedNode = null;
			if (droppedObject instanceof DnDAtlasObject) {
				// remove from old location
				DnDAtlasObject transObj = (DnDAtlasObject) droppedObject;

				if ((transObj.getSource() == AtlasDragSources.DNDTREE)) {
					droppedNode = (MutableTreeNode) transObj.getObject();

					// SK:
					if (droppedNode == dropTargetNode)
						return;

					if (droppedNode.getParent() != null)
						// Remove the source item, if it has a parent?! D'n'D
						// inside the GroupPane
						// if (dtde.getDropAction() == DnDConstants.ACTION_MOVE)
						// // Only remove if it is a move action
						((DefaultTreeModel) getModel())
								.removeNodeFromParent(droppedNode);
				} else if ((transObj.getSource() == AtlasDragSources.DATAPOOLLIST)) {
					// ****************************************************************************
					// This is a drop from the Datapool list
					// ****************************************************************************
					DpEntry dpe = (DpEntry) transObj.getObject();

					droppedNode = new DpRef(dpe);
				} else if ((transObj.getSource() == AtlasDragSources.MAPPOOLLIST)) {
					// ****************************************************************************
					// This is a drop from the Mappool list
					// ****************************************************************************
					droppedNode = (MapRef) transObj.getObject();
				}

			} else {
				droppedNode = new DefaultMutableTreeNode(droppedObject);
			}

			// insert into spec'd path. if dropped into a parent
			// make it last child of that parent
			// hochgeschoben,
			if (dropTargetNode.isLeaf()) {
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) dropTargetNode
						.getParent();

				// SK: When dropping onto itself, parent == null
				if (parent == null) {
					dtde.dropComplete(false);
					return;
				}

				int index = parent.getIndex(dropTargetNode);
				((DefaultTreeModel) getModel()).insertNodeInto(droppedNode,
						parent, index);

			} else {
				((DefaultTreeModel) getModel()).insertNodeInto(droppedNode,
						dropTargetNode, dropTargetNode.getChildCount());

				AVUtil.expandToNode(this, droppedNode);
			}

			dropped = true;
		} catch (Exception e) {
			LOGGER.error(e);
		} finally {
			dtde.dropComplete(dropped);
		}
	}

	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	// custom renderer
	class DnDTreeCellRenderer extends DefaultTreeCellRenderer {
		boolean isTargetNode;

		boolean isTargetNodeLeaf;

		boolean isLastItem;

		Insets normalInsets, lastItemInsets;

		int BOTTOM_PAD = 30;

		public DnDTreeCellRenderer() {
			super();
			normalInsets = super.getInsets();
			lastItemInsets = new Insets(normalInsets.top, normalInsets.left,
					normalInsets.bottom + BOTTOM_PAD, normalInsets.right);
		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean isSelected, boolean isExpanded, boolean isLeaf,
				int row, boolean hasFocus) {
			isTargetNode = (value == dropTargetNode);
			isTargetNodeLeaf = (isTargetNode && ((TreeNode) value).isLeaf());
			// isLastItem = (index == list.getModel().getSize()-1);

			// by SK auskommentiert weil keine funktion
			// boolean showSelected = isSelected &
			// (dropTargetNode == null);

			return super.getTreeCellRendererComponent(tree, value, isSelected,
					isExpanded, isLeaf, row, hasFocus);

		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (isTargetNode) {
				g.setColor(Color.black);
				if (isTargetNodeLeaf) {
					g.drawLine(0, 0, getSize().width, 0);
				} else {
					g.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
				}
			}
		}
	}
}
