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
import org.geopublishing.atlasStyler.AsSwingUtil;
import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geopublishing.atlasViewer.swing.Icons;
import org.geotools.styling.StyledLayer;

import de.schmitzm.geotools.styling.StyledLayerInterface;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.io.IOUtil;
import de.schmitzm.swing.ExceptionDialog;
import de.schmitzm.swing.FileExtensionFilter;

/**
 * Action to save the SLD of the {@link StyledLayer} to a {@link File}. Two versions of the SLD are saved. The normal
 * one, and an optimized one for production use. The later can not be properly reimported yet.
 */
public class AtlasStylerSaveAsLayerToSLDAction extends AbstractAction {
	private static final long serialVersionUID = 4726448851995462364L;

	static private final Logger LOGGER = Logger.getLogger(AtlasStylerSaveAsLayerToSLDAction.class);

	private final StyledLayerInterface<?> styledLayer;

	private final Component owner;

	public AtlasStylerSaveAsLayerToSLDAction(Component owner, StyledLayerInterface<?> styledLayer) {
		super(ASUtil.R("AtlasStylerGUI.saveAsToSLDFile"), Icons.ICON_EXPORT);
		this.owner = owner;
		this.styledLayer = styledLayer;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		boolean backup = false;

		File startWithFile = new File(System.getProperty("user.home"), styledLayer.getTitle().toString() + ".sld");

		if (styledLayer.getSldFile() != null)
			startWithFile = styledLayer.getSldFile();

		// JFileChooser dc = new JFileChooser(startWithFile);
		// dc.addChoosableFileFilter(ASUtil.FILTER_SLD);
		// dc.setDialogType(JFileChooser.SAVE_DIALOG);
		//
		// dc.setDialogTitle(ASUtil.R("StyledLayerSLD.ChooseFileLocationDialog.Title"));
		//
		// dc.setSelectedFile(startWithFile);
		//
		// if ((dc.showSaveDialog(owner) != JFileChooser.APPROVE_OPTION) || (dc.getSelectedFile() == null))
		// return;
		// File exportFile = dc.getSelectedFile();

		File exportFile = AsSwingUtil.chooseFileSave(owner, startWithFile,
				ASUtil.R("StyledLayerSLD.ChooseFileLocationDialog.Title"), new FileExtensionFilter(ASUtil.FILTER_SLD));

		if (exportFile == null)
			return;

		if (!(exportFile.getName().toLowerCase().endsWith(".sld") || exportFile.getName().toLowerCase()
				.endsWith(".xml"))) {
			exportFile = new File(exportFile.getParentFile(), exportFile.getName() + ".sld");
		}

		styledLayer.setSldFile(exportFile);

		if (styledLayer.getSldFile().exists()) {
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

			if (backup)
				AVSwingUtil.showMessageDialog(
						owner,
						AtlasStylerVector.R("AtlasStylerGUI.saveToSLDFileSuccessAndBackedUp",
								IOUtil.escapePath(styledLayer.getSldFile())));
			else
				AVSwingUtil.showMessageDialog(
						owner,
						AtlasStylerVector.R("AtlasStylerGUI.saveToSLDFileSuccess",
								IOUtil.escapePath(styledLayer.getSldFile())));

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
