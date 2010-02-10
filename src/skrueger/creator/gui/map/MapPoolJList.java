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
///** 
// Copyright 2008 Stefan Alfons Krüger 
// 
// atlas-framework - This file is part of the Atlas Framework
//
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA
//
// Diese Bibliothek ist freie Software; Sie dürfen sie unter den Bedingungen der GNU Lesser General Public License, wie von der Free Software Foundation veröffentlicht, weiterverteilen und/oder modifizieren; entweder gemäß Version 2.1 der Lizenz oder (nach Ihrer Option) jeder späteren Version.
// Diese Bibliothek wird in der Hoffnung weiterverbreitet, daß sie nützlich sein wird, jedoch OHNE IRGENDEINE GARANTIE, auch ohne die implizierte Garantie der MARKTREIFE oder der VERWENDBARKEIT FÜR EINEN BESTIMMTEN ZWECK. Mehr Details finden Sie in der GNU Lesser General Public License.
// Sie sollten eine Kopie der GNU Lesser General Public License zusammen mit dieser Bibliothek erhalten haben; falls nicht, schreiben Sie an die Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA.
// **/
//package skrueger.creator.gui.map;
//
//import java.awt.Color;
//import java.awt.Component;
//import java.awt.event.ActionEvent;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//
//import javax.swing.AbstractAction;
//import javax.swing.BorderFactory;
//import javax.swing.DefaultListModel;
//import javax.swing.JList;
//import javax.swing.JMenuItem;
//import javax.swing.JPopupMenu;
//import javax.swing.ListCellRenderer;
//import javax.swing.ListModel;
//import javax.swing.ListSelectionModel;
//import javax.swing.SwingUtilities;
//
//import skrueger.atlas.gui.internal.AtlasListCellRenderer;
//import skrueger.atlas.gui.internal.JPanelX;
//import skrueger.atlas.map.Map;
//import skrueger.atlas.map.MapPool;
//import skrueger.atlas.resource.icons.Icons;
//
//public class MapPoolJList extends JList {
//	private DefaultListModel listModel;
//
//	protected MapPool mapPool;
//
//	private JPopupMenu popupMenu;
//
//	/**
//	 * A JList that shows all the Maps, without ordering.
//	 * 
//	 * @param mapPool
//	 */
//	public MapPoolJList(final MapPool mapPool) {
//		super();
//
//		this.mapPool = mapPool;
//
//		this.listModel = new DefaultListModel();
//		setModel(listModel);
//
//		setCellRenderer(new ListCellRenderer() {
//
//			public Component getListCellRendererComponent(JList list,
//					Object value, int index, boolean isSelected,
//					boolean cellHasFocus) {
//
//				Map map = (Map) value;
//
//				JPanelX prototype = new JPanelX(Icons.ICON_MAP_BIG);
//
//				prototype.setNameLabelText(map.getTitle());
//				prototype.setDescLabelText(map.getDesc());
//
//				AtlasListCellRenderer.setColorsForSelectionState(prototype,
//						isSelected);
//
//				// Mark the default Map
//				if (mapPool.getStartMapID().equals(map.getId())) {
//					prototype.setBorder(BorderFactory
//							.createLineBorder(Color.GREEN));
//				} else {
//					prototype.setBorder(null);
//				}
//				return prototype;
//			}
//
//		});
//
//		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//
//		updateModelFromMapPool();
//
//		popupMenu = new JPopupMenu();
//		popupMenu.add(new JMenuItem(new AbstractAction("make default ") {
//
//			public void actionPerformed(ActionEvent e) {
//				Map elementAt = (Map) getModel().getElementAt(
//						getSelectedIndex());
//				mapPool.setStartMapID(elementAt.getId());
//				updateModelFromMapPool();
//			} // i8nAC
//
//		}));
//
//		// ****************************************************************************
//		// MouseListener to create a JPopupMenu
//		// ****************************************************************************
//		addMouseListener(new MouseAdapter() {
//			public void mouseClicked(MouseEvent me) {
//				// if right mouse button clicked (or me.isPopupTrigger())
//				if (SwingUtilities.isRightMouseButton(me)
//						&& !isSelectionEmpty()
//						&& locationToIndex(me.getPoint()) == getSelectedIndex()) {
//					popupMenu.show(MapPoolJList.this, me.getX(), me.getY());
//				}
//			}
//		});
//	}
//
//	/**
//	 * 
//	 */
//	public void updateModelFromMapPool() {
//		listModel.removeAllElements();
//		int anz = 0;
//		for (Map map : mapPool.values()) {
//			listModel.add(anz, map);
//			anz++;
//		}
//	}
//
//	/**
//	 * Return the listModel casted to {@link DefaultListModel}
//	 */
//	public DefaultListModel getDefaultListModel() {
//		ListModel model = super.getModel();
//		return (DefaultListModel) model;
//	}
//
//}
