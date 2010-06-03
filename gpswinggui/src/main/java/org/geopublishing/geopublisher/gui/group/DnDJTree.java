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
import org.geopublishing.atlasViewer.swing.RJLTransferable;
import org.geopublishing.atlasViewer.swing.internal.DnDAtlasObject;
import org.geopublishing.atlasViewer.swing.internal.DnDAtlasObject.AtlasDragSources;


/**
 * A {@link JTree} that is reorderable by D'n'D
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 */
public class DnDJTree extends JTree implements DragSourceListener,
		DropTargetListener, DragGestureListener {
	// custom renderer
	class DnDTreeCellRenderer extends DefaultTreeCellRenderer {
		int BOTTOM_PAD = 30;

		boolean isLastItem;

		boolean isTargetNode;

		boolean isTargetNodeLeaf;

		Insets normalInsets, lastItemInsets;

		public DnDTreeCellRenderer() {
			super();
			normalInsets = super.getInsets();
			lastItemInsets = new Insets(normalInsets.top, normalInsets.left,
					normalInsets.bottom + BOTTOM_PAD, normalInsets.right);
		}

		@Override
		public Component getTreeCellRendererComponent(final JTree tree,
				final Object value, final boolean isSelected,
				final boolean isExpanded, final boolean isLeaf, final int row,
				final boolean hasFocus) {
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
		public void paintComponent(final Graphics g) {
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

	// ****************************************************************************
	// D'n'D stuff
	// ****************************************************************************
	static DataFlavor localObjectFlavor;
	private static final Logger LOGGER = Logger.getLogger(CopyOfDnDJTree.class);

	static DataFlavor[] supportedFlavors = { localObjectFlavor };

	static {
		try {
			localObjectFlavor = new DataFlavor(
					DataFlavor.javaJVMLocalObjectMimeType);
		} catch (final ClassNotFoundException cnfe) {
			LOGGER.error(cnfe);
		}
	}

	DragSource dragSource;

	DropTarget dropTarget;

	TreeNode dropTargetNode, draggedNode;

	private final Logger log = Logger.getLogger(CopyOfDnDJTree.class);

	public DnDJTree() {
		// ****************************************************************************
		// D'n'D Stuff
		// ****************************************************************************
		dragSource = new DragSource();
		@SuppressWarnings("unused")
		final DragGestureRecognizer dgr = dragSource
				.createDefaultDragGestureRecognizer(this,
						DnDConstants.ACTION_MOVE, this);
		dropTarget = new DropTarget(this, this);

		setDropMode(DropMode.ON);
	}

	// DragSourceListener events
	public void dragDropEnd(final DragSourceDropEvent dsde) {
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
	public void dragEnter(final DragSourceDragEvent dsde) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent
	 * )
	 */
	@Override
	public void dragEnter(final DropTargetDragEvent dtde) {
//		// figure out which cell it's over, no drag to self
//		final Point dragPoint = dtde.getLocation();
//		final TreePath path = getPathForLocation(dragPoint.x, dragPoint.y);
//		if (path == null)
//			dtde.acceptDrag(DnDConstants.ACTION_NONE);
//		else {
//			dropTargetNode = (TreeNode) path.getLastPathComponent();
//
//			Object droppedObject;
//			try {
//				droppedObject = dtde.getTransferable().getTransferData(
//						localObjectFlavor);
//				final DefaultMutableTreeNode dropTargetNode = (DefaultMutableTreeNode) path
//						.getLastPathComponent();
//				if (droppedObject instanceof DnDAtlasObject) {
//					// remove from old location
//					final DnDAtlasObject transObj = (DnDAtlasObject) droppedObject;
//
//					if ((transObj.getSource() == AtlasDragSources.DNDTREE)) {
//						MutableTreeNode droppedNode = (MutableTreeNode) transObj
//								.getObject();
//
//						// this is also checked in checkIsParent
//						// if (droppedNode == dropTargetNode)
//						// return;
//
//						if (!checkIsParent(dropTargetNode, droppedNode)) {
							dtde.acceptDrag(DnDConstants.ACTION_MOVE); // OK!
//							System.out.println("wanted");
//							return;
//						}
//					}
//				}
//				
//			} catch (UnsupportedFlavorException e) {
//				dtde.acceptDrag(DnDConstants.ACTION_NONE);
//			} catch (IOException e) {
//				dtde.acceptDrag(DnDConstants.ACTION_NONE);
//			}
//
//		}
//		dtde.acceptDrag(DnDConstants.ACTION_NONE);
//		System.out.println("not wanted");
//		
		repaint();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.dnd.DragSourceListener#dragExit(java.awt.dnd.DragSourceEvent)
	 */
	@Override
	public void dragExit(final DragSourceEvent dse) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
	 */
	@Override
	public void dragExit(final DropTargetEvent dte) {
	}

	@Override
	public void dragGestureRecognized(final DragGestureEvent dge) {
		// log.debug ("dragGestureRecognized");

		final Point clickPoint = dge.getDragOrigin();
		final TreePath path = getPathForLocation(clickPoint.x, clickPoint.y);
		if (path == null) {
			log.debug("not on a node");
			return;
		}
		draggedNode = (TreeNode) path.getLastPathComponent();
		final Transferable trans = new RJLTransferable(draggedNode,
				AtlasDragSources.DNDTREE, TreeNode.class);
		dragSource.startDrag(dge, Cursor
				.getPredefinedCursor(Cursor.MOVE_CURSOR), trans, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.dnd.DragSourceListener#dragOver(java.awt.dnd.DragSourceDragEvent
	 * )
	 */
	@Override
	public void dragOver(final DragSourceDragEvent dsde) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent
	 * )
	 */
	@Override
	public void dragOver(final DropTargetDragEvent dtde) {
		// figure out which cell it's over, no drag to self
		final Point dragPoint = dtde.getLocation();
		final TreePath path = getPathForLocation(dragPoint.x, dragPoint.y);
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
	@Override
	public void drop(final DropTargetDropEvent dtde) {
		final Point dropPoint = dtde.getLocation();

		final TreePath path = getPathForLocation(dropPoint.x, dropPoint.y);
		// log.debug("drop path is " + path);

		// Is evaluated in the finally block
		boolean dropped = false;
		try {
			if (path == null)
				return;

			dtde.acceptDrop(DnDConstants.ACTION_MOVE);
			// log.debug("Drop accepted. Transfering...");

			final Object droppedObject = dtde.getTransferable()
					.getTransferData(localObjectFlavor);

			final DefaultMutableTreeNode dropTargetNode = (DefaultMutableTreeNode) path
					.getLastPathComponent();

			MutableTreeNode droppedNode = null;
			if (droppedObject instanceof DnDAtlasObject) {
				// remove from old location
				final DnDAtlasObject transObj = (DnDAtlasObject) droppedObject;

				if ((transObj.getSource() == AtlasDragSources.DNDTREE)) {
					droppedNode = (MutableTreeNode) transObj.getObject();

					// this is also checked in checkIsParent
					// if (droppedNode == dropTargetNode)
					// return;

					if (droppedNode.getParent() != null) {
						// Before we remove the droppedNode, we have to verify,
						// that the dropTargetNode is not a child of that.
						if (checkIsParent(dropTargetNode, droppedNode)) {
							return;
						}

						((DefaultTreeModel) getModel())
								.removeNodeFromParent(droppedNode);
					}

				} else if ((transObj.getSource() == AtlasDragSources.DATAPOOLLIST)) {
					// ****************************************************************************
					// This is a drop from the Datapool list
					// ****************************************************************************
					final DpEntry dpe = (DpEntry) transObj.getObject();

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

			// insert into spec'd path.
			if (dropTargetNode.isLeaf()) {
				// if dropped onto a leaf, add it as the last sibling
				final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) dropTargetNode
						.getParent();

				// SK: When dropping onto itself, parent == null
				if (parent == null) {
					// dtde.dropComplete(false);
					return;
				}

				final int index = parent.getIndex(dropTargetNode);
				((DefaultTreeModel) getModel()).insertNodeInto(droppedNode,
						parent, index);

			} else {
				((DefaultTreeModel) getModel()).insertNodeInto(droppedNode,
						dropTargetNode, dropTargetNode.getChildCount());

				AVUtil.expandToNode(this, droppedNode);
			}

			dropped = true;
		} catch (final Exception e) {
			LOGGER.error(e);
		} finally {
			dtde.dropComplete(dropped);
		}
	}

	/**
	 * Checks if the first {@link TreeNode} is a child of (or equal to) the
	 * second {@link TreeNode}.
	 */
	private boolean checkIsParent(TreeNode child, TreeNode parent) {
		if (child == null || parent == null)
			return false;

		if (child == parent || child.equals(parent)) {
			return true;
		}

		return checkIsParent(child.getParent(), parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.awt.dnd.DragSourceListener#dropActionChanged(java.awt.dnd.
	 * DragSourceDragEvent)
	 */
	@Override
	public void dropActionChanged(final DragSourceDragEvent dsde) {
	}

	@Override
	public void dropActionChanged(final DropTargetDragEvent dtde) {
	}
}
