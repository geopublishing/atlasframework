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
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;

import schmitzm.swing.ExceptionDialog;

public class GPExportCSVAction extends AbstractAction {

	private final AtlasConfigEditable ace;
	private final Component owner;

	public GPExportCSVAction(String label, AtlasConfigEditable ace,
			Component owner) {
		super(label);
		this.ace = ace;
		this.owner = owner;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		File exportFile = askUserForFolder();

		if (exportFile == null)
			return;

		FileWriter fw;
		try {
			fw = new FileWriter(exportFile);

			fw.write("Name;Beschreibung;Dateiname\n");

			for (DpEntry dpe : ace.getDataPool().values()) {
				fw.write(dpe.getTitle() + ";" + dpe.getDesc() + ";"
						+ dpe.getFilename() + "\n");
			}

			fw.flush();
			fw.close();

		} catch (IOException e1) {
			ExceptionDialog.show(owner, e1);
		}
	}

	private File askUserForFolder() {
		File exportFile;
		/**
		 * Ask the user to select a save position 
		 * TODO Remember this position in the .properties
		 */

		File startWithDir = new File(System.getProperty("user.home"),
				"datapool.csv");
		JFileChooser dc = new JFileChooser(startWithDir);
		dc.setDialogType(JFileChooser.SAVE_DIALOG);
		dc.setDialogTitle(GeopublisherGUI.R("ExportCSV.SaveCSVDialog.Title"));
		dc.setSelectedFile(startWithDir);

		if ((dc.showSaveDialog(owner) != JFileChooser.APPROVE_OPTION)
				|| (dc.getSelectedFile() == null))
			return null;

		exportFile = dc.getSelectedFile();
		exportFile.delete();
		return exportFile;
	}

}
