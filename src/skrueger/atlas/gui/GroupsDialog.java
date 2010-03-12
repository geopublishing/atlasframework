/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
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
