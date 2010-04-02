package org.geopublishing.geopublisher.gui.datapool;
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
///*******************************************************************************
// * Copyright (c) 2009 Stefan A. Krüger.
// * 
// * This file is part of the Geopublisher application - An authoring tool to facilitate the publication and distribution of geoproducts in form of online and/or offline end-user GIS.
// * http://www.geopublishing.org
// * 
// * Geopublisher is part of the Geopublishing Framework hosted at:
// * http://wald.intevation.org/projects/atlas-framework/
// * 
// * This program is free software; you can redistribute it and/or
// * modify it under the terms of the GNU General Public License
// * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// * 
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// * 
// * You should have received a copy of the GNU General Public License (license.txt)
// * along with this program; if not, write to the Free Software
// * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
// * or try this link: http://www.gnu.org/licenses/gpl.html
// * 
// * Contributors:
// *     Stefan A. Krüger - initial API and implementation
// ******************************************************************************/
//package skrueger.creator.gui.datapool;
//
//import java.awt.Component;
//import java.awt.event.ActionEvent;
//
//import javax.swing.AbstractAction;
//import javax.swing.JDialog;
//
//import org.geotools.styling.Style;
//
//import schmitzm.swing.SwingUtil;
//import skrueger.atlas.AVUtil;
//import skrueger.atlas.dp.DataPool;
//import skrueger.atlas.dp.DpEntry;
//import org.geopublishing.atlasViewer.swing.MapLayerLegend;
//import skrueger.creator.AtlasConfigEditable;
//import skrueger.creator.AtlasCreator;
//import skrueger.creator.gui.EditRasterLegendDataJPanel;
//import skrueger.geotools.StyledRasterInterface;
//
//public class DataPoolEditRasterLegendAction extends AbstractAction {
//
//	private DataPoolJTable dpTable;
//	private Component owner;
//	private StyledRasterInterface<?> dpr;
//	final private AtlasConfigEditable ace;
//
//	public DataPoolEditRasterLegendAction(DataPoolJTable dpTable,
//			Component owner) {
//		super(AtlasCreator.R("DataPoolWindow_Action_EditRasterLegend_label"));
//
//		this.dpTable = dpTable;
//		this.owner = owner;
//		this.ace = dpTable.getAce();
//	}
//
//	public DataPoolEditRasterLegendAction(StyledRasterInterface<?> dpr,AtlasConfigEditable ace,
//			MapLayerLegend owner) {
//		super(AtlasCreator.R("DataPoolWindow_Action_EditRasterLegend_label"));
//		this.dpr = dpr;
//		this.ace = ace;
//		this.owner = owner;
//	}
//
//	@Override
//	public void actionPerformed(ActionEvent e) {
//
//		if (dpTable != null) {
//			// Determine which DPEntry is selected
//			if (dpTable.getSelectedRow() == -1)
//				return;
//
//			DataPool dataPool = dpTable.getDataPool();
//			dpr = (StyledRasterInterface) dataPool.get(dpTable
//					.convertRowIndexToModel(dpTable.getSelectedRow()));
//		}
//
//		JDialog dialog = new JDialog(SwingUtil.getParentWindow(owner));
//
//		EditRasterLegendDataJPanel contentPane = null;
//		contentPane = new EditRasterLegendDataJPanel(dpr, ace);
//		dialog.setContentPane(contentPane);
//		dialog.setModal(true);
//		dialog.setTitle("editing legend for " + dpr.getTitle()); // i8n
//		dialog.pack();
//		dialog.setVisible(true);
//
//	}
//
//}
