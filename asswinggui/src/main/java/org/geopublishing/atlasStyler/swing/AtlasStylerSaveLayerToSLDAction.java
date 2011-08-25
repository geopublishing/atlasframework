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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geopublishing.atlasViewer.swing.Icons;
import org.geotools.styling.StyledLayer;

import de.schmitzm.geotools.styling.StyledLayerInterface;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.io.IOUtil;
import de.schmitzm.swing.ExceptionDialog;

/**
 * Action to save the SLD of the {@link StyledLayer} to a {@link File}. Two versions of the SLD are saved. The normal
 * one, and an optimized one for production use. The later can not be properly reimported yet.
 */
public class AtlasStylerSaveLayerToSLDAction extends AbstractAction {
	private static final long serialVersionUID = 4726448851995462364L;

	static private final Logger LOGGER = Logger.getLogger(AtlasStylerSaveLayerToSLDAction.class);;

	private final StyledLayerInterface<?> styledLayer;

	private final Component owner;

	public AtlasStylerSaveLayerToSLDAction(Component owner, StyledLayerInterface<?> styledLayer) {
		super(ASUtil.R("AtlasStylerGUI.saveToSLDFile"), Icons.ICON_EXPORT);
		this.owner = owner;
		this.styledLayer = styledLayer;

		// This action in only enabled if the SLD changed AND a SLD file has been associated AND the file can be saved.
		setEnabled(IOUtil.canWriteOrCreate(styledLayer.getSldFile())
		// && StylingUtil.isStyleDifferent(styledLayer.getStyle(), styledLayer.getSldFile())
		);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		boolean backup = false;

		if (styledLayer.getSldFile().exists()) {

			// if (StylingUtil.isStyleDifferent(styledLayer.getStyle(), styledLayer.getSldFile())){
			// return;
			// }

			try {
				FileUtils.copyFile(styledLayer.getSldFile(), IOUtil.changeFileExt(styledLayer.getSldFile(), "sld.bak"));
				backup = true;
			} catch (IOException e1) {
				LOGGER.warn("could not create a backup of the existing .sld", e1);
				return;
			}
		}

		try {
			StylingUtil.saveStyleToSld(styledLayer.getStyle(), styledLayer.getSldFile(), false);
			StylingUtil.saveStyleToSld(styledLayer.getStyle(),
					ASUtil.changeToOptimizedFilename(styledLayer.getSldFile()), true);

			// TODO MJ: im dialog auch einen öffnen button über JDesktop
			if (backup)
				AVSwingUtil.showMessageDialog(
						owner,
						ASUtil.R("AtlasStylerGUI.saveToSLDFileSuccessAndBackedUp",
								IOUtil.escapePath(styledLayer.getSldFile())));
			else
				// TODO MJ: im dialog auch einen öffnen button über JDesktop
				AVSwingUtil.showMessageDialog(owner,
						ASUtil.R("AtlasStylerGUI.saveToSLDFileSuccess", IOUtil.escapePath(styledLayer.getSldFile())));

			List<Exception> es = StylingUtil.validateSld(new FileInputStream(styledLayer.getSldFile()));
			if (es.size() > 0) {
				ExceptionDialog.show(
						owner,
						new IllegalStateException(ASUtil.R("AtlasStylerExport.WarningSLDNotValid",
								IOUtil.escapePath(styledLayer.getSldFile())), es.get(0)));
			}

		} catch (Exception e1) {
			LOGGER.error("saveStyleToSLD", e1);
			ExceptionDialog.show(owner, e1);
			return;
		}
	}
}
