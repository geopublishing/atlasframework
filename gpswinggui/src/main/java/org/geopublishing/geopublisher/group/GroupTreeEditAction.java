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
package org.geopublishing.geopublisher.group;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.dp.Group;
import org.geopublishing.atlasViewer.swing.BasicMapLayerLegendPaneUI;
import org.geopublishing.geopublisher.AtlasCreator;
import org.geopublishing.geopublisher.gui.group.EditGroupJDialog;
import org.geopublishing.geopublisher.gui.group.EditGroupsDnDJTreePanel;


/**
 * Used on a {@link TreeNode} which is {@link Group} node, it opens a modal
 * {@link EditGroupJDialog} to edit/translate it.
 * 
 * @author SK
 * 
 */
public class GroupTreeEditAction extends AbstractAction {

	static final private Logger LOGGER = Logger
			.getLogger(GroupTreeEditAction.class);

	private final Component owner;

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
	public GroupTreeEditAction(EditGroupsDnDJTreePanel editGroupsPanel) {
		this(editGroupsPanel, null);

	}

	/**
	 * @param treeNode
	 *            When action is performed, the action will work on this node.
	 */
	public GroupTreeEditAction(EditGroupsDnDJTreePanel editGroupsPanel,
			TreeNode treeNode) {
		super(AtlasCreator.R("GroupTree.Action.Edit"),
				BasicMapLayerLegendPaneUI.ICON_TOOL);
		this.editGroupsPanel = editGroupsPanel;
		this.owner = editGroupsPanel;
		this.node = treeNode;

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

		if (node instanceof Group) {
			Group editGroup = (Group) node;
			EditGroupJDialog editGroupGUI = new EditGroupJDialog(owner,
					editGroup);
			editGroupGUI.setVisible(true);
			if (!editGroupGUI.isCancelled())
				owner.repaint();
		} else {
			LOGGER.warn("Can't edit selected item.");
		}

	}

}
