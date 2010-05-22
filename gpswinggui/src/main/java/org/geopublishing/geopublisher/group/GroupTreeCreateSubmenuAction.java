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
package org.geopublishing.geopublisher.group;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.dp.Group;
import org.geopublishing.atlasViewer.swing.Icons;
import org.geopublishing.geopublisher.gui.group.EditGroupJDialog;
import org.geopublishing.geopublisher.gui.group.EditGroupsDnDJTreePanel;
import org.geopublishing.geopublisher.swing.GpSwingUtil;


/**
 * Used on a {@link TreeNode} which is {@link Group} node, it opens a modal
 * {@link EditGroupJDialog} to edit/translate it.
 * 
 * @author SK
 * 
 */
public class GroupTreeCreateSubmenuAction extends AbstractAction {

	static final private Logger LOGGER = Logger
			.getLogger(GroupTreeCreateSubmenuAction.class);

	/**
	 * Optionally this or {@link #jTree} has to be set
	 */
	private TreeNode node;

	private final EditGroupsDnDJTreePanel editGroupsPanel;

	/**
	 * @param dndJTree
	 *            When actionPerformed is called, the method will check which
	 *            Node is selected in this JTree
	 */
	public GroupTreeCreateSubmenuAction(EditGroupsDnDJTreePanel editGroupsPanel) {
		this(editGroupsPanel, null);

	}

	/**
	 * @param treeNode
	 *            When action is performed, the action will work on this node.
	 */
	public GroupTreeCreateSubmenuAction(
			EditGroupsDnDJTreePanel editGroupsPanel, TreeNode treeNode) {
		super(GpSwingUtil.R("GroupTree.Action.New"), Icons.ICON_ADD_SMALL);
		this.editGroupsPanel = editGroupsPanel;
		this.node = treeNode;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		Group newGroup = new Group(editGroupsPanel.getRootGroup().getAc());

		{
			/**
			 * Ask the user to describe the new submenu
			 */
			EditGroupJDialog translateGroupGUI = new EditGroupJDialog(
					editGroupsPanel, newGroup);
			translateGroupGUI.setVisible(true);
			if (translateGroupGUI.isCancelled())
				return;
		}

		if (node == null) {
			// Where to insert if node == null? First check if any TreeNode is
			// selected.
			TreeSelectionModel selectionModel = editGroupsPanel.getJTree()
					.getSelectionModel();
			TreePath selectionPath = selectionModel.getSelectionPath();
			if (selectionPath == null) {
				// If nothing is selected, we insert it on the root level
				node = editGroupsPanel.getRootGroup();
			} else {
				// Get the selected object
				node = (TreeNode) selectionPath.getLastPathComponent();
			}
		}

		if (!(node instanceof Group)) {
			node = node.getParent();
		}

		Group targetGroup = (Group) node;
		targetGroup.add(newGroup);

		// Maybe not the best way, but this makes the JTree update
		// the change
		editGroupsPanel.updateJTree(newGroup);
	}

}
