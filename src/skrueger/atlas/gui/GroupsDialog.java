/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the AtlasViewer application - A GIS viewer application targeting at end-users with no GIS-experience. Its main purpose is to present the atlases created with the Geopublisher application.
 * http://www.geopublishing.org
 * 
 * AtlasViewer is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.atlas.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.HeadlessException;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;

import skrueger.atlas.AtlasConfig;

public class GroupsDialog extends JDialog {
	private Logger log = Logger.getLogger(GroupsDialog.class);

	private AtlasConfig ac;

	private JPanel myContentPane;

	private JScrollPane groupTree;

	private Frame ownerFrame;

	private JTree tree;

	public GroupsDialog(Frame owner, AtlasConfig ac) throws HeadlessException {
		super(owner);
		ownerFrame = owner;
		this.ac = ac;
		initialize();
		log.debug("GroupsDialog created.");
	}

	private void initialize() {
		getContentPane().add(getMyContentPane());
		setModal(true);
		pack();
	}

	private Component getMyContentPane() {
		if (myContentPane == null) {
			myContentPane = new JPanel();
			myContentPane.setLayout(new BorderLayout());
			myContentPane.add(getGroupTree());

		}
		return myContentPane;
	}

	private JScrollPane getGroupTree() {
		if (groupTree == null) {
			groupTree = new JScrollPane(getJTree());
		}
		return groupTree;
	}

	private JTree getJTree() {
		if (tree == null) {
			tree = new JTree();
			tree.setModel(new DefaultTreeModel(ac.getFirstGroup()));

			// Only allow single selections
			tree.getSelectionModel().setSelectionMode(
					TreeSelectionModel.SINGLE_TREE_SELECTION);
		}
		return tree;
	}

}
