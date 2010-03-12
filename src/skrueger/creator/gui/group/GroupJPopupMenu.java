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
package skrueger.creator.gui.group;

import java.awt.event.ActionEvent;
import java.util.Enumeration;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreeNode;

import schmitzm.jfree.chart.style.ChartStyle;
import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.dp.DpRef;
import skrueger.atlas.dp.Group;
import skrueger.atlas.map.Map;
import skrueger.atlas.map.MapRef;
import skrueger.creator.AtlasCreator;
import skrueger.creator.group.GroupTreeCreateSubmenuAction;
import skrueger.creator.group.GroupTreeDeleteAction;
import skrueger.creator.group.GroupTreeEditAction;
import skrueger.creator.gui.datapool.DataPoolEditAction;
import skrueger.creator.gui.map.MapPoolPrefsAction;

/**
 * This class provides the right-mouse button context-menu available in the
 * {@link EditGroupsDnDJTreePanel}. 
 */
public class GroupJPopupMenu extends JPopupMenu {

	public GroupJPopupMenu(EditGroupsDnDJTreePanel editGroupsPanel,
			TreeNode clickedNode, final Group firstGroup) {

		add(new JMenuItem(new GroupTreeCreateSubmenuAction(editGroupsPanel,
				clickedNode)));

		/**
		 * Delete is possible on all TreeNodes but the root one.
		 */
		if (clickedNode instanceof TreeNode && clickedNode != firstGroup) {

			add(new JMenuItem(new GroupTreeDeleteAction(editGroupsPanel,
					clickedNode)));
		}

		/**
		 * These functions are only possible on TreeNodes that are Groups
		 */
		if (clickedNode instanceof Group && clickedNode != firstGroup) {
			final Group g = (Group) clickedNode;

			add(new JMenuItem(new GroupTreeEditAction(editGroupsPanel,
					clickedNode)));

			/**
			 * A checkbox that allows to define this group as the special FILE
			 * menu
			 */
			JCheckBoxMenuItem isFileMenuMenuItem = new JCheckBoxMenuItem(
					new AbstractAction(AtlasCreator
							.R("MenuTree.PopupmenuItem.ThisIsTheFileMenu")) {

						@Override
						public void actionPerformed(ActionEvent e) {

							if (!g.isFileMenu()) {
								// If we set this group to be the File menu, we
								// have to remove the flag from all the other
								// group menus first
								removeFileMenuFlag(firstGroup);
							}

							g.setFileMenu(!g.isFileMenu());
						}

						/**
						 * Recurse through the group-tree and remove any
						 * "File-Menu" flags
						 */
						private void removeFileMenuFlag(Group group) {
							Enumeration<?> children = group.children();

							for (; children.hasMoreElements();) {
								Object child = children.nextElement();

								if (child instanceof Group) {
									Group subgroup = (Group) child;
									subgroup.setFileMenu(false);
									removeFileMenuFlag(subgroup);
								}
							}
						}

					});
			isFileMenuMenuItem.setSelected(g.isFileMenu());
			add(isFileMenuMenuItem);

			/**
			 * A checkbox that allows to define this group as the official HELP
			 * menu
			 */
			JCheckBoxMenuItem isHelpMenuMenuItem = new JCheckBoxMenuItem(
					new AbstractAction(AtlasCreator
							.R("MenuTree.PopupmenuItem.ThisIsTheHelpMenu")) {

						@Override
						public void actionPerformed(ActionEvent e) {

							if (!g.isHelpMenu()) {
								// If we set this group to be the help menu, we
								// have to remove the flag from all the other
								// group menus first
								removeHelpMenuFlag(firstGroup);
							}

							g.setHelpMenu(!g.isHelpMenu());
						}

						/**
						 * Recurse through the group-tree and remove any
						 * "Help-Menu" flags
						 */
						private void removeHelpMenuFlag(Group group) {
							Enumeration<?> children = group.children();

							for (; children.hasMoreElements();) {
								Object child = children.nextElement();

								if (child instanceof Group) {
									Group subgroup = (Group) child;
									subgroup.setHelpMenu(false);
									removeHelpMenuFlag(subgroup);
								}
							}
						}

					});
			isHelpMenuMenuItem.setSelected(g.isHelpMenu());
			add(isHelpMenuMenuItem);
		}

		if (clickedNode instanceof MapRef) {
			MapRef mapRef = (MapRef) clickedNode;

			final Map targetMap = mapRef.getTarget();

			add(new MapPoolPrefsAction(editGroupsPanel, targetMap, mapRef
					.getMapPool()));
		} else if (clickedNode instanceof DpRef) {
			DpRef<DpEntry<? extends ChartStyle>> dpRef = (DpRef<DpEntry<? extends ChartStyle>>) clickedNode;
			final DpEntry<? extends ChartStyle> targetDpe = dpRef.getTarget();

			add(new DataPoolEditAction(editGroupsPanel, targetDpe));
		}

	}

}
