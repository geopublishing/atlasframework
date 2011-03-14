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
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geopublishing.atlasViewer.swing.Icons;

import de.schmitzm.geotools.styling.StyledFS;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.io.IOUtil;
import de.schmitzm.swing.ExceptionDialog;

public class AtlasStylerSaveLayerToSLDAction extends AbstractAction {
	static private final Logger LOGGER = Logger
			.getLogger(AtlasStylerSaveLayerToSLDAction.class);;

	private final StyledFS styledShp;

	private final Component owner;

	public AtlasStylerSaveLayerToSLDAction(Component owner, StyledFS styledShp) {
		super(AtlasStylerVector.R("AtlasStylerGUI.saveToSLDFile"),
				Icons.ICON_EXPORT);
		this.owner = owner;
		this.styledShp = styledShp;

		setEnabled(StylingUtil.isStyleDifferent(styledShp.getStyle(),
				styledShp.getSldFile()));
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		boolean backup = false;

		// Test whether a .sld file can be created. Ask the user to change
		// position of the .sld
		while (!IOUtil.canWriteOrCreate(styledShp.getSldFile())) {

			AVSwingUtil.showMessageDialog(
					owner,
					AtlasStylerVector.R("StyledLayerSLDNotWritable.Msg",
							IOUtil.escapePath(styledShp.getSldFile())));

			File startWithFile = new File(System.getProperty("user.home"),
					styledShp.getSldFile().getName());
			JFileChooser dc = new JFileChooser(startWithFile);
			dc.addChoosableFileFilter(new FileNameExtensionFilter("SLD",
					new String[] { "sld", "xml" }));
			dc.setDialogType(JFileChooser.SAVE_DIALOG);

			dc.setDialogTitle(AtlasStylerVector
					.R("StyledLayerSLDNotWritable.ChooseNewDialog.Title"));

			dc.setSelectedFile(startWithFile);

			if ((dc.showSaveDialog(owner) != JFileChooser.APPROVE_OPTION)
					|| (dc.getSelectedFile() == null))
				return;

			File exportFile = dc.getSelectedFile();

			if (!(exportFile.getName().toLowerCase().endsWith("sld") || exportFile
					.getName().toLowerCase().endsWith("xml"))) {
				exportFile = new File(exportFile.getParentFile(),
						exportFile.getName() + ".sld");
			}

			styledShp.setSldFile(exportFile);
		}

		if (styledShp.getSldFile().exists()) {
			try {
				FileUtils
						.copyFile(styledShp.getSldFile(), IOUtil.changeFileExt(
								styledShp.getSldFile(), "sld.bak"));
				backup = true;
			} catch (IOException e1) {
				LOGGER.warn("could not create a backup of the existing .sld",
						e1);
				return;
			}
		}

		try {
			StylingUtil.saveStyleToSld(styledShp.getStyle(),
					styledShp.getSldFile());

			if (backup)
				AVSwingUtil.showMessageDialog(owner, AtlasStylerVector.R(
						"AtlasStylerGUI.saveToSLDFileSuccessAndBackedUp",
						IOUtil.escapePath(styledShp.getSldFile())));
			else
				AVSwingUtil.showMessageDialog(owner, AtlasStylerVector.R(
						"AtlasStylerGUI.saveToSLDFileSuccess",
						IOUtil.escapePath(styledShp.getSldFile())));

			List<Exception> es = StylingUtil.validateSld(new FileInputStream(
					styledShp.getSldFile()));
			if (es.size() > 0) {
				ExceptionDialog.show(
						owner,
						new IllegalStateException(ASUtil.R(
								"AtlasStylerExport.WarningSLDNotValid",
								IOUtil.escapePath(styledShp.getSldFile())), es.get(0)));
			}

		} catch (Exception e1) {
			LOGGER.error("saveStyleToSLD", e1);
			ExceptionDialog.show(owner, e1);
			return;
		}
	}
}
