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
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geopublishing.atlasViewer.swing.plaf.BasicMapLayerLegendPaneUI;

import schmitzm.geotools.styling.StylingUtil;
import schmitzm.io.IOUtil;
import schmitzm.swing.ExceptionDialog;
import skrueger.geotools.StyledFS;

public class AtlasStylerSaveLayerToSLDAction extends AbstractAction {
	static private final Logger LOGGER = Logger
			.getLogger(AtlasStylerSaveLayerToSLDAction.class);;

	private final StyledFS styledShp;

	private final Component owner;

	public AtlasStylerSaveLayerToSLDAction(Component owner, StyledFS styledShp) {
		super(AtlasStyler.R("AtlasStylerGUI.saveToSLDFile"),
				BasicMapLayerLegendPaneUI.ICON_EXPORT);
		this.owner = owner;
		this.styledShp = styledShp;

		setEnabled(StylingUtil.isStyleDifferent(styledShp.getStyle(), styledShp
				.getSldFile()));
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		boolean backup = false;

		// Test whether a .sld file can be created. Ask the user to change
		// position of the .sld
		while (! IOUtil.canWriteOrCreate(styledShp.getSldFile()) ) {

			AVSwingUtil.showMessageDialog(owner, AtlasStyler.R(
					"StyledLayerSLDNotWritable.Msg", styledShp.getSldFile().getAbsolutePath()));

			File startWithFile = new File(System.getProperty("user.home"),
					styledShp.getSldFile().getName());
			JFileChooser dc = new JFileChooser(startWithFile);
			dc.addChoosableFileFilter(new FileNameExtensionFilter("SLD",
					new String[] { "sld", "xml" }));
			dc.setDialogType(JFileChooser.SAVE_DIALOG);
			
			dc.setDialogTitle(AtlasStyler
					.R("StyledLayerSLDNotWritable.ChooseNewDialog.Title"));
			
			dc.setSelectedFile(startWithFile);

			if ((dc.showSaveDialog(owner) != JFileChooser.APPROVE_OPTION)
					|| (dc.getSelectedFile() == null))
				return;

			File exportFile = dc.getSelectedFile();

			if (!(exportFile.getName().toLowerCase().endsWith("sld") || exportFile
					.getName().toLowerCase().endsWith("xml"))) {
				exportFile = new File(exportFile.getParentFile(), exportFile
						.getName()
						+ ".sld");
			}

			styledShp.setSldFile(exportFile);
		}

		if (styledShp.getSldFile().exists()) {
			try {
				FileUtils.copyFile(styledShp.getSldFile(), IOUtil
						.changeFileExt(styledShp.getSldFile(), "sld.bak"));
				backup = true;
			} catch (IOException e1) {
				LOGGER.warn("could not create a backup of the existing .sld",
						e1);
				return;
			}
		}

		try {
			StylingUtil.saveStyleToSLD(styledShp.getStyle(), styledShp
					.getSldFile());

			if (backup)
				AVSwingUtil.showMessageDialog(owner, AtlasStyler.R(
						"AtlasStylerGUI.saveToSLDFileSuccessAndBackedUp",
						styledShp.getSldFile().getAbsolutePath()));
			else
				AVSwingUtil.showMessageDialog(owner, AtlasStyler.R(
						"AtlasStylerGUI.saveToSLDFileSuccess", styledShp
								.getSldFile().getAbsolutePath()));

		} catch (Exception e1) {
			LOGGER.error("saveStyleToSLD", e1);
			ExceptionDialog.show(owner, e1);
			return;
		}
	}
}
