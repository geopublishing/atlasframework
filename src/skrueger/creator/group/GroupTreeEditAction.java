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
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;

import skrueger.atlas.dp.Group;
import skrueger.atlas.gui.plaf.BasicMapLayerLegendPaneUI;
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
