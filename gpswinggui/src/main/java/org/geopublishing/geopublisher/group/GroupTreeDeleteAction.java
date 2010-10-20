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
package org.geopublishing.geopublisher.group;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.dp.Group;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geopublishing.atlasViewer.swing.plaf.BasicMapLayerLegendPaneUI;
import org.geopublishing.geopublisher.gui.group.EditGroupsDnDJTreePanel;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;


/**
 * Used on a {@link Group} node, it verifies with the user, that she really
 * wants to delete it and does it.
 * 
 * @author SK
 * 
 */
public class GroupTreeDeleteAction extends AbstractAction {

	static final private Logger LOGGER = Logger
			.getLogger(GroupTreeDeleteAction.class);

	private final Component owner;

	/**
	 * If <code>null</code>, the selcted {@link TreeNode} is determined fomr the
	 * {@link JTree}
	 */
	private TreeNode node;

	private final EditGroupsDnDJTreePanel editGroupsPanel;

	/**
	 * @param dndJTree
	 *            When actionPerformed is called, the method will check which
	 *            Node is selected in this JTree
	 */
	public GroupTreeDeleteAction(EditGroupsDnDJTreePanel editGroupsPanel) {
		this(editGroupsPanel, null);
	}

	/**
	 * @param treeNode
	 *            When action is performed, the action will work on this node.
	 */
	public GroupTreeDeleteAction(EditGroupsDnDJTreePanel editGroupsPanel,
			TreeNode treeNode) {
		super(GeopublisherGUI.R("GroupTree.Action.Delete"),
				BasicMapLayerLegendPaneUI.ICON_REMOVE);
		this.owner = editGroupsPanel;
		this.node = treeNode;
		this.editGroupsPanel = editGroupsPanel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (node == null) {
			TreeSelectionModel selectionModel = editGroupsPanel.getJTree()
					.getSelectionModel();
			TreePath selectionPath = selectionModel.getSelectionPath();
			if (selectionPath == null) {
				LOGGER
						.warn("EditGroup action should never have been called, as nothing is selected.");
				return;
			}

			// Get the selected object
			node = (TreeNode) selectionPath.getLastPathComponent();
		}

		deleteGroupItem(node);

	}

	/**
	 * Delete this {@link DefaultMutableTreeNode} from the group. The user will
	 * be asked interactively and may cancel the action.
	 * 
	 * @param node
	 *            {@link TreeNode} to delete.
	 * 
	 * @return <code>true</code> only if the given node has really been deleted.
	 */
	public boolean deleteGroupItem(TreeNode node) {

		if (!(node instanceof DefaultMutableTreeNode)) {
			LOGGER
					.warn("Selected node is not a DefaultMutableTreeNode, can't delete.");
			return false;
		}

		DefaultMutableTreeNode deleteNode = (DefaultMutableTreeNode) node;
		if (deleteNode.equals(editGroupsPanel.getRootGroup())) {
			LOGGER.debug("Will not delete root node!"); // i8nlog
			return false;
		}

		TreeNode parentTreeNode = deleteNode.getParent();
		if (parentTreeNode == null) {
			LOGGER
					.warn("Can't delete item, because parent == null, but is not root!");
			return false;
		}

		if (!(parentTreeNode instanceof DefaultMutableTreeNode)) {
			LOGGER
					.warn("Can't delete item, because parent is not DefaultMutableTreeNode!");
			return false;
		}

		DefaultMutableTreeNode mutableParent = (DefaultMutableTreeNode) parentTreeNode;

		/**
		 * Ask the user if she really wants to delete the node
		 */
		String questionKey;
		if (deleteNode instanceof Group) {
			questionKey = "GroupTree.Action.DeleteGroup.Question";
		} else {
			questionKey = "GroupTree.Action.DeleteDpRef.Question";
		}
		if (!AVSwingUtil.askYesNo(owner, GeopublisherGUI.R(questionKey, deleteNode
				.toString())))
			return false;

		mutableParent.remove(deleteNode);

		editGroupsPanel.updateJTree(parentTreeNode);

		return true;
	}

}
