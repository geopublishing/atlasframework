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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;

import skrueger.atlas.dp.Group;
import skrueger.atlas.resource.icons.Icons;
import skrueger.creator.AtlasCreator;
import skrueger.creator.gui.group.EditGroupJDialog;
import skrueger.creator.gui.group.EditGroupsDnDJTreePanel;

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
		super(AtlasCreator.R("GroupTree.Action.New"), Icons.ICON_ADD_SMALL);
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
