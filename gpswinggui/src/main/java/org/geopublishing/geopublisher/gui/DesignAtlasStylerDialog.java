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
package org.geopublishing.geopublisher.gui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;

import org.geopublishing.atlasViewer.dp.layer.DpLayer;
import org.geopublishing.atlasViewer.dp.layer.DpLayerRaster_Reader;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;
import org.geopublishing.atlasViewer.dp.layer.LayerStyle;
import org.geopublishing.atlasViewer.swing.AtlasStylerDialog;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.gui.map.DesignAtlasMapLegend;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.geotools.map.MapLayer;

import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.jfree.chart.style.ChartStyle;
import de.schmitzm.swing.ExceptionDialog;
import de.schmitzm.swing.SwingUtil;

public class DesignAtlasStylerDialog extends AtlasStylerDialog {

	private final DesignAtlasMapLegend mapLegend;

	public DesignAtlasStylerDialog(Component owner,
			DpLayerVectorFeatureSource dpLayer, DesignAtlasMapLegend mapLegend,
			MapLayer mapLayer, LayerStyle layerStyle) {
		super(owner, dpLayer, mapLegend, mapLayer, layerStyle);
		this.mapLegend = mapLegend;

	}
	
	public DesignAtlasStylerDialog(Component owner,
			DpLayerRaster_Reader dpLayer, DesignAtlasMapLegend mapLegend,
			MapLayer mapLayer, LayerStyle layerStyle) {
		super(owner, dpLayer, mapLegend, mapLayer, layerStyle);
		this.mapLegend = mapLegend;

	}


	@Override
	public boolean okClose() {

		/**
		 * Which add. Style is selected at the moment?
		 */
		if (layerStyle == null) {

			// That is saved at another place... probably a better place here,
			// beausee only now have we canged the Style
			// TODO

			return super.okClose();
		}

		if (!SwingUtil.askYesNo(DesignAtlasStylerDialog.this, GeopublisherGUI
				.R("DesignMapLayerLegend.SaveStyleForLayer",
						layerStyle.getTitle()))) {
			// // The user canceled his changes
			// TODO
			// we.getSource();
			// updateStyle();
			return false;
		}

		/***********************************************
		 * The user wants to update the user style. It has to be saved to a
		 * file.
		 */
		AtlasConfigEditable ace = mapLegend.getAce();

		DpLayer<? extends Object, ? extends ChartStyle> dpLayer = (DpLayer<? extends Object, ? extends ChartStyle>) getStyledLayer();
		
		File dataDir = new File(ace.getDataDir(), dpLayer.getDataDirname());

		final String filename = map.getSelectedStyleIDs().get(dpLayer.getId());
		try {
			StylingUtil.saveStyleToSld(getAtlasStyler().getStyle(), new File(
					dataDir, filename));
		} catch (IOException e) {
			LOGGER.error("Saving additional layer style", e);
			ExceptionDialog.show(DesignAtlasStylerDialog.this, e);
		}
		layerStyle.uncache();
		//
		return super.okClose();
	}

}
