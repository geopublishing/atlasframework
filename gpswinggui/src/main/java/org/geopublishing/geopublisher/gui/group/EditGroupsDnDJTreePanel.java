/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.gui.group;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AVUtil;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.DpRef;
import org.geopublishing.atlasViewer.dp.Group;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.map.MapRef;
import org.geopublishing.geopublisher.gui.internal.GPDialogManager;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;

import schmitzm.jfree.chart.style.ChartStyle;

/**
 * This JPanel shows the {@link Group}s tree. New children can be added by
 * Drag'n'Drop and {@link Group}s can be created, edited and deleted.
 * 
 * @author Stefan Alfons Tzeggai
 */
public class EditGroupsDnDJTreePanel extends JPanel {

	static final private Logger LOGGER = Logger
			.getLogger(EditGroupsDnDJTreePanel.class);

	private final Group rootGroup;

	private JScrollPane groupsTreeScrollPane = null;

	// private JPanel buttonPanel;
	// private JButton editGroupJButton;
	// private JButton deleteGroupJButton;

	private DnDJTree dndTree;

	/**
	 * Creates a new {@link EditGroupsDnDJTreePanel} that allows to reorder the
	 * {@link JTree} by D'n'D and has buttons to add, delete and edit
	 * {@link Group}s
	 * 
	 * @param rootGroup
	 *            {@link Group} to be represented
	 */
	public EditGroupsDnDJTreePanel(final Group rootGroup) {
		this.rootGroup = rootGroup;

		setLayout(new BorderLayout());
		add(getGroupsTreeScrollPane(rootGroup), BorderLayout.CENTER);
		// add(getButtonPanel(), BorderLayout.SOUTH);

		/**
		 * Add a Listener to the MapPool to update the GroupTree whenever the
		 * MapPool changed.
		 */
		rootGroup.getAc().getMapPool().addChangeListener(
				new PropertyChangeListener() {

					@Override
					public void propertyChange(final PropertyChangeEvent evt) {
						updateJTree(null);
					}
				});

		/**
		 * Add a Listener to the DataPool to update the GroupTree whenever the
		 * DataPool changed. Note: ATM the listener is never removed :-/
		 */
		rootGroup.getAc().getDataPool().addChangeListener(
				new PropertyChangeListener() {

					@Override
					public void propertyChange(final PropertyChangeEvent evt) {
						updateJTree(null);
					}
				});

		/**
		 * Setting tool-tips so the users know how to handle this Panel
		 */
		setToolTipText(GeopublisherGUI.R("EditGroupsDnDJTreePanel.TT"));
		getJTree().setToolTipText(GeopublisherGUI.R("EditGroupsDnDJTreePanel.TT"));

	}

	/**
	 * @param rootGroup
	 *            If the {@link GroupsTreeScrollPane} has to be newly created,
	 *            only then a {@link Group} is needed
	 * @return New or lazy {@link GroupsTreeScrollPane}
	 */
	private JScrollPane getGroupsTreeScrollPane(final Group rootGroup) {
		if (groupsTreeScrollPane == null) {
			groupsTreeScrollPane = new JScrollPane(getJTree());
		}
		return groupsTreeScrollPane;
	}

	/**
	 * @return The {@link JTree} - actually a {@link DnDJTree} - that hold all
	 *         the menu and menu-items together.
	 */
	public final JTree getJTree() {
		if (dndTree == null) {

			if (rootGroup == null)
				throw new IllegalStateException("The rootGroup has to be set!");

			dndTree = new DnDJTree();
			dndTree.setModel(new DefaultTreeModel(rootGroup));

			// Only allow single selections
			dndTree.getSelectionModel().setSelectionMode(
					TreeSelectionModel.SINGLE_TREE_SELECTION);

			dndTree.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(final MouseEvent e) {

					/*
					 * In any case select the clicked node in the tree
					 */
					final Point clickPoint = e.getPoint();
					final TreePath path = dndTree.getPathForLocation(
							clickPoint.x, clickPoint.y);
					if (path == null) {
						// LOGGER.warn("not on a node");
						return;
					}
					final TreeNode clickedNode = (TreeNode) path
							.getLastPathComponent();
					dndTree.setSelectionPath(path);

					/*
					 * If a Map of Dpe is clicked, select it in the pools
					 */
					{
						if (clickedNode instanceof MapRef) {
							final Map targetMap = ((MapRef) clickedNode)
									.getTarget();

							// Select the Entry in the table
							GeopublisherGUI.getInstance().getJFrame()
									.getMappoolJTable().select(
											targetMap.getId());

						} else if (clickedNode instanceof DpRef<?>) {
							final DpEntry<? extends ChartStyle> targetDpe = ((DpRef<DpEntry<? extends ChartStyle>>) clickedNode)
									.getTarget();
							// Select the Entry in the table
							GeopublisherGUI.getInstance().getJFrame()
									.getDatapoolJTable().select(
											targetDpe.getId());
						}

					}

					/*
					 * Open a PopupMenu if the right mouse button was pressed.
					 * Start the default action if double-clicked.
					 */
					if (e.getButton() == MouseEvent.BUTTON3)
					// The right mouse opens the popup
					{

						new GroupJPopupMenu(EditGroupsDnDJTreePanel.this,
								clickedNode, rootGroup)
								.show(
										e.getSource() instanceof Component ? (Component) e
												.getSource()
												: EditGroupsDnDJTreePanel.this,
										clickPoint.x, clickPoint.y);
					} else if (e.getClickCount() == 2)
					// Double lick starts a default action
					{
						if (clickedNode instanceof MapRef) {
							final Map map = ((MapRef) clickedNode).getTarget();
							GPDialogManager.dm_MapComposer.getInstanceFor(map,
									EditGroupsDnDJTreePanel.this, map);
						} else if (clickedNode instanceof DpRef<?>) {
							final DpEntry<? extends ChartStyle> dpe = ((DpRef<DpEntry<? extends ChartStyle>>) clickedNode)
									.getTarget();
							GPDialogManager.dm_EditDpEntry.getInstanceFor(dpe,
									EditGroupsDnDJTreePanel.this, dpe);
						}
					}
				}

			});

			dndTree.setCellRenderer(new GroupTreeCellRenderer());
		}
		return dndTree;
	}

	//
	// /**
	// * @return JPanel wit Edit, Create and Delete Button
	// */
	// private JPanel getButtonPanel() {
	// if (buttonPanel == null) {
	// buttonPanel = new JPanel();
	//
	// buttonPanel.add(getNewGroupJButton());
	// buttonPanel.add(getDeleteGroupJButton());
	// buttonPanel.add(getEditGroupJButton());
	// }
	//
	// return buttonPanel;
	// }
	//
	// /**
	// * @return {@link JButton} that deletes the selected {@link Group} or the
	// * Group element that is selected.
	// */
	// private JButton getDeleteGroupJButton() {
	// if (deleteGroupJButton == null) {
	//
	// deleteGroupJButton = new JButton();
	// deleteGroupJButton.setEnabled(false);
	//
	// // Adding a listener
	// JTree dndJTree = getJTree();
	// TreeSelectionModel selectionModel = dndJTree.getSelectionModel();
	// selectionModel
	// .addTreeSelectionListener(new TreeSelectionListener() {
	//
	// public void valueChanged(TreeSelectionEvent e) {
	// Object source = e.getSource();
	// if (source instanceof DefaultTreeSelectionModel) {
	// DefaultTreeSelectionModel s = (DefaultTreeSelectionModel) source;
	// if (s.getSelectionPath() == null) {
	// deleteGroupJButton.setEnabled(false);
	// } else {
	// deleteGroupJButton.setEnabled(true);
	// }
	// } else {
	// deleteGroupJButton.setEnabled(false);
	// }
	// }
	//
	// });
	//
	// deleteGroupJButton.setAction(new
	// GroupTreeDeleteAction(EditGroupsDnDJTreePanel.this));
	// }
	// return deleteGroupJButton;
	// }

	/**
	 * Recreate the tree
	 * 
	 * @param expandToThisNode
	 *            may be <code>null</code>
	 */
	public void updateJTree(final TreeNode expandToThisNode) {

		// Maybe not the best way, but this makes the JTree update
		// the change
		getJTree().setModel(new DefaultTreeModel(rootGroup));
		if (expandToThisNode != null)
			getJTree().expandPath(AVUtil.getPath(expandToThisNode));
		repaint();
	}

	//
	// /**
	// * @return A {@link JButton} that changes ask the user to change the name
	// +
	// * description of the selected {@link Group}
	// */
	// private JButton getEditGroupJButton() {
	//
	// if (editGroupJButton == null) {
	//
	// editGroupJButton = new JButton(new GroupTreeEditAction(
	// EditGroupsDnDJTreePanel.this) {
	//
	// });
	// editGroupJButton.setEnabled(false);
	//
	// // Adding a listener
	// JTree dndJTree = getJTree();
	// TreeSelectionModel selectionModel = dndJTree.getSelectionModel();
	// selectionModel
	// .addTreeSelectionListener(new TreeSelectionListener() {
	//
	// public void valueChanged(TreeSelectionEvent e) {
	// Object source = e.getSource();
	// if (source instanceof DefaultTreeSelectionModel) {
	// DefaultTreeSelectionModel s = (DefaultTreeSelectionModel) source;
	// TreePath selectionPath = s.getSelectionPath();
	// if (selectionPath == null) {
	// editGroupJButton.setEnabled(false);
	// } else {
	// Object node = selectionPath
	// .getLastPathComponent();
	// if (node instanceof Group) {
	// editGroupJButton.setEnabled(true);
	// } else
	// editGroupJButton.setEnabled(false);
	// }
	// } else {
	// editGroupJButton.setEnabled(false);
	// }
	// }
	//
	// });
	//
	// }
	// return editGroupJButton;
	// }
	//
	// /**
	// * @return A {@link JButton} that creates a new {@link Group} at either
	// the
	// * root entry or the selected entry.
	// */
	// private JButton getNewGroupJButton() {
	// final JButton newGroupJButton = new JButton();
	//
	// newGroupJButton.setAction(new AbstractAction(Geopublisher
	// .R("GroupTree.Action.New"), Icons.ICON_ADD_SMALL) {
	//
	// public void actionPerformed(final ActionEvent e) {
	// final Group newGroup = new Group(rootGroup.getAc());
	//
	// final EditGroupJDialog editGroupGUI = new EditGroupJDialog(
	// SwingUtil.getParentWindow(EditGroupsDnDJTreePanel.this),
	// newGroup);
	// editGroupGUI.setVisible(true);
	//
	// if (editGroupGUI.isCancelled())
	// return;
	//
	// final JTree dndJTree = getJTree();
	// final TreeSelectionModel selectionModel = dndJTree
	// .getSelectionModel();
	// // DefaultTreeModel treeModel = (DefaultTreeModel) dndJTree
	// // .getModel();
	//
	// if (selectionModel.isSelectionEmpty())
	// // If nothing is selected, create a new Group in the
	// // rootGroup
	// {
	// rootGroup.add(newGroup);
	// } else {
	// // Something is selected, create a new Group at the
	// // first selection
	// final TreePath selectionPath = selectionModel
	// .getSelectionPath();
	// final Object node = selectionPath.getLastPathComponent();
	// if (node instanceof Group) {
	// final Group targetGroup = (Group) node;
	// targetGroup.add(newGroup);
	// } else {
	// LOGGER
	// .warn("Can't insert a new group at selection. Adding to default rootGroup");
	// rootGroup.add(newGroup);
	// }
	// }
	//
	// // Maybe not the best way, but this makes the JTree update
	// // the change
	// dndJTree.setModel(new DefaultTreeModel(rootGroup));
	// dndJTree.expandPath(AVUtil.getPath(newGroup));
	// repaint();
	// }
	// });
	//
	// return newGroupJButton;
	// }

	public Group getRootGroup() {
		return rootGroup;
	}

}
