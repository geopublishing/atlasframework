/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasStyler.swing;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.ExportableLayer;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.swing.MapLayerLegend;
import org.geopublishing.atlasViewer.swing.MapLegend;
import org.geotools.map.MapLayer;

import de.schmitzm.geotools.styling.StyledLayerInterface;
import de.schmitzm.lang.LangUtil;

/**
 * An extension to an ordinary {@link MapLayerLegend}. The difference is, that {@link MapLayerLegend} works on
 * {@link StyledLayerInterface} objects, and {@link AtlasStylerMapLayerLegend} works on {@link DpLayer DpLayers} and has
 * a reference to a {@link Map}. {@link DpLayer}. The class also extends the {@link JPopupMenu} with Atlas specific
 * {@link JMenuItem}s.
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 */
public class AtlasStylerMapLayerLegend extends MapLayerLegend {

	private static final long serialVersionUID = -8055199049564037014L;

	public AtlasStylerMapLayerLegend(MapLayer mapLayer, ExportableLayer exportable, StyledLayerInterface<?> styledObj,
			MapLegend mapLegend) {
		super(mapLayer, exportable, styledObj, mapLegend);
	}

	final private Logger LOGGER = LangUtil.createLogger(this);

	@Override
	public JPopupMenu getToolMenu() {
		JPopupMenu toolPopup = super.getToolMenu();

		// We are in AtlasStyler. Offer to save the .SLD
		toolPopup.insert(new JMenuItem(new AtlasStylerSaveLayerToSLDAction(this, styledLayer)),0);

		// We are in AtlasStyler. Offer to save the .SLD as...
		toolPopup.insert(new JMenuItem(new AtlasStylerSaveAsLayerToSLDAction(this, styledLayer)),1);

		// We are in AtlasStyler. Offer to copy the SLD to clipboard
		toolPopup.insert(new JMenuItem(new AtlasStylerCopyLayerToClipboardAction(this, styledLayer)),2);
		
		// We are in AtlasStyler. Offer to paste the SLD from clipboard
		JMenuItem pasteFromClipBoard = new JMenuItem(new AtlasStylerPasteLayerFromClipboardAction(this, styledLayer));
		// TODO MJ better method to check if we actually have a valid style in clipboard
		if(!LangUtil.pasteFromClipboard().startsWith("<?xml")){
			pasteFromClipBoard.setEnabled(false);
		}
		toolPopup.insert(pasteFromClipBoard,3);

		return toolPopup;
	}
}
