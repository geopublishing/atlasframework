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
package skrueger.creator.gui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.geotools.map.MapLayer;

import schmitzm.geotools.styling.StylingUtil;
import schmitzm.swing.ExceptionDialog;
import skrueger.atlas.AVUtil;
import skrueger.atlas.dp.layer.DpLayerVectorFeatureSource;
import skrueger.atlas.dp.layer.LayerStyle;
import skrueger.atlas.gui.AtlasStylerDialog;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.AtlasCreator;
import skrueger.creator.gui.map.DesignAtlasMapLegend;

public class DesignAtlasStylerDialog extends AtlasStylerDialog {

	private final DesignAtlasMapLegend mapLegend;

	public DesignAtlasStylerDialog(Component owner,
			DpLayerVectorFeatureSource dpLayer, DesignAtlasMapLegend mapLegend,
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
			// TODO

			return super.okClose();
		}

		if (!AVUtil.askYesNo(DesignAtlasStylerDialog.this, AtlasCreator
				.R("DesignMapLayerLegend.SaveStyleForLayer", layerStyle
						.getTitle()))) {
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

		File dataDir = new File(ace.getDataDir(), dpLayer.getDataDirname());

		final String filename = map.getSelectedStyleIDs().get(dpLayer.getId());
		try {
			StylingUtil.saveStyleToSLD(getAtlasStyler().getStyle(), new File(
					dataDir, filename));
		} catch (TransformerException e) {
			LOGGER.error("Saving additional layer style", e);
			ExceptionDialog.show(DesignAtlasStylerDialog.this, e);
		} catch (IOException e) {
			LOGGER.error("Saving additional layer style", e);
			ExceptionDialog.show(DesignAtlasStylerDialog.this, e);
		}
		layerStyle.uncache();
		//		
		return super.okClose();
	}

}
