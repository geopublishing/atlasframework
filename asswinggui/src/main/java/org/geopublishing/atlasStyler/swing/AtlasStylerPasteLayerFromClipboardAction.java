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

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.rulesLists.AbstractRulesList.RulesListType;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geopublishing.atlasViewer.swing.Icons;
import org.geotools.styling.Style;

import de.schmitzm.geotools.styling.StyledFS;
import de.schmitzm.geotools.styling.StyledGridCoverageReader;
import de.schmitzm.geotools.styling.StyledLayerInterface;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.lang.LangUtil;

public class AtlasStylerPasteLayerFromClipboardAction extends AbstractAction {

	static private final Logger LOGGER = Logger
			.getLogger(AtlasStylerPasteLayerFromClipboardAction.class);;

	private final StyledLayerInterface<?> styledLayer;

	private final Component owner;

	public AtlasStylerPasteLayerFromClipboardAction(Component owner,
			StyledLayerInterface<?> styledLayer) {
		super(ASUtil.R("AtlasStylerGUI.pasteLayerFromClipboard"),
				Icons.ICON_ADD_SMALL);
		this.owner = owner;
		this.styledLayer = styledLayer;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String pastedXML = LangUtil.pasteFromClipboard();
		Style[] pastedSLD = StylingUtil.loadSLD(pastedXML);

		if (styledLayer instanceof StyledFS) {// vector
			if (RulesListType.isVectorStyle(pastedSLD[0])) {
				styledLayer.setStyle(pastedSLD[0]);
				owner.repaint();
			} else {
				AVSwingUtil.showMessageDialog(owner, ASUtil.R("AtlasStylerGUI.pasteLayerFromClipboard.warning"));

			}

		} else if (styledLayer instanceof StyledGridCoverageReader) {// raster
			if (RulesListType.isRasterStyle(pastedSLD[0])) {
				styledLayer.setStyle(pastedSLD[0]);
				owner.repaint();
			} else {
				AVSwingUtil.showMessageDialog(owner, ASUtil.R("AtlasStylerGUI.pasteLayerFromClipboard.warning"));
			}

		}

	}
}
