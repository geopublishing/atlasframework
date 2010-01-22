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
package skrueger.creator.group;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;

import skrueger.atlas.AVUtil;
import skrueger.atlas.dp.Group;
import skrueger.atlas.gui.plaf.BasicMapLayerLegendPaneUI;
import skrueger.creator.AtlasCreator;
import skrueger.creator.gui.group.EditGroupsDnDJTreePanel;

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
		super(AtlasCreator.R("GroupTree.Action.Delete"),
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
		if (!AVUtil.askYesNo(owner, AtlasCreator.R(questionKey, deleteNode
				.toString())))
			return false;

		mutableParent.remove(deleteNode);

		editGroupsPanel.updateJTree(parentTreeNode);

		return true;
	}

}
