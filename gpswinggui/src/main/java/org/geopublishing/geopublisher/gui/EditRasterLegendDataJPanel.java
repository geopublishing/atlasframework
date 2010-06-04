package org.geopublishing.geopublisher.gui;
/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
///*******************************************************************************
// * Copyright (c) 2009 Stefan A. Tzeggai.
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
// *     Stefan A. Tzeggai - initial API and implementation
// ******************************************************************************/
//package skrueger.creator.gui;
//
//import java.awt.BorderLayout;
//import java.awt.Component;
//import java.awt.GridLayout;
//import java.awt.event.ActionEvent;
//import java.util.List;
//
//import javax.swing.AbstractAction;
//import javax.swing.JButton;
//import javax.swing.JOptionPane;
//import javax.swing.JPanel;
//import javax.swing.JScrollPane;
//import javax.swing.JTable;
//import javax.swing.ListSelectionModel;
//import javax.swing.table.DefaultTableModel;
//
//import org.apache.log4j.Logger;
//
//import skrueger.RasterLegendData;
//import org.geopublishing.atlasViewer.swing.MapLegend;
//import org.geopublishing.atlasViewer.swing.internal.LayoutUtil;
//import skrueger.creator.AtlasConfigEditable;
//import skrueger.geotools.StyledLayerUtil;
//import skrueger.geotools.StyledRasterInterface;
//import skrueger.i8n.Translation;
//
///**
// * This Panel allows editing of {@link RasterLegendData} and provides a preview
// * of the legend.
// * 
// * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
// */
//public class EditRasterLegendDataJPanel extends JPanel {
//	static private final Logger LOGGER = Logger
//			.getLogger(EditRasterLegendDataJPanel.class);
//
//	private final RasterLegendData legendData;
//
//	private List<Double> sortedKeys;
//
//	private final AtlasConfigEditable ace;
//
//	private JTable table;
//
//	private JPanel preview = new JPanel(new BorderLayout());
//
//	private final StyledRasterInterface<?> styledObj;
//
//	public EditRasterLegendDataJPanel(StyledRasterInterface<?> layer,
//			AtlasConfigEditable ace) {
//		this.styledObj = layer;
//		this.ace = ace;
//		this.legendData = layer.getLegendMetaData();
//		sortedKeys = legendData.getSortedKeys();
//
//		initGUI();
//	}
//
//	private DefaultTableModel rasterLegendDataTableModel = null;
//
//	private void initGUI() {
//		setLayout(new GridLayout(1, 2));
//
//		add(left);
//
//		// ****************************************************************************
//		// The right side contains a preview
//		// ****************************************************************************
//		add(preview);
//		updatePreview();
//
//	}
//
//
//	protected void updatePreview() {
//		preview.removeAll();
//		preview.add(new JScrollPane(StyledLayerUtil.createLegendPanel(
//				styledObj, MapLegend.ICONWIDTH, MapLegend.ICONHEIGHT)),
//				BorderLayout.NORTH);
//		LayoutUtil.borderTitle(preview, "Legend preview"); // i8n // TODO
//		// rewrite
//		preview.validate();
//	}
//
//}
