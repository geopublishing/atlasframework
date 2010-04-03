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
package org.geopublishing.geopublisher.gui.export;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasViewer.resource.icons.Icons;
import org.geopublishing.geopublisher.GeopublisherGUI;
import org.netbeans.spi.wizard.WizardPage;

import skrueger.creator.GPProps;
import skrueger.creator.GPProps.Keys;


public class ExportWizardPage_ExportFolder extends WizardPage {
	/*
	 * The short description label that appears on the left side of the wizard
	 */
	private final String validationFolderFailedMsg_NotExits = GeopublisherGUI
			.R("ExportWizard.Folder.ValidationError.NotExists");
	private final String validationFolderFailedMsg_NotWritable = GeopublisherGUI
			.R("ExportWizard.Folder.ValidationError.NotWritable");

	JLabel explanationJLabel = new JLabel(GeopublisherGUI
			.R("ExportWizard.Folder.Explanation"));

	private JButton folderChooserJButton;
	JTextField folderJTextField;
	final private JLabel folderTextFieldJLabel = new JLabel(GeopublisherGUI
			.R("ExportWizard.Folder.FolderTextBoxLabel"));

	public ExportWizardPage_ExportFolder() {
		setPreferredSize(ExportWizard.DEFAULT_WPANEL_SIZE);
		setSize(ExportWizard.DEFAULT_WPANEL_SIZE);

		setLayout(new MigLayout("wrap 1"));

		add(explanationJLabel);
		add(folderTextFieldJLabel, "gapy unrelated");
		add(getFolderJTextField(), "growx, split 2, left");
		add(getFolderChooserJButton(), "right");
	}

	public static String getDescription() {
		return GeopublisherGUI.R("ExportWizard.Folder");
	}

	private JTextField getFolderJTextField() {
		if (folderJTextField == null) {
			folderJTextField = new JTextField(GPProps.get(
					GPProps.Keys.LastExportFolder, ""));

			folderJTextField.setName(ExportWizard.EXPORTFOLDER);
		}

		return folderJTextField;
	}

	@Override
	protected String validateContents(final Component component,
			final Object event) {

		final String absPath = getFolderJTextField().getText();
		final File folder = new File(absPath);

		if (!folder.exists()) {
			return validationFolderFailedMsg_NotExits;
		}

		if (!folder.canWrite() || !folder.canRead()) {
			return validationFolderFailedMsg_NotWritable;
		}

		return null;
	}

	public JButton getFolderChooserJButton() {
		if (folderChooserJButton == null) {
			folderChooserJButton = new JButton(new AbstractAction("",
					Icons.ICON_SEARCH) {

				@Override
				public void actionPerformed(final ActionEvent e) {

					final String pathname = GPProps.get(Keys.LastExportFolder,
							"");
					final JFileChooser dc = new JFileChooser(new File(pathname));
					dc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					dc.setAcceptAllFileFilterUsed(false);

					dc.setDialogTitle(GeopublisherGUI.R("Export.Dialog.WhereTo"));
					dc.setMultiSelectionEnabled(false);

					if ((dc.showSaveDialog(ExportWizardPage_ExportFolder.this) != JFileChooser.APPROVE_OPTION)
							|| dc.getSelectedFile() == null) {
						return;
					}

					final File exportJWSandDISKdir = dc.getSelectedFile();

					if (exportJWSandDISKdir == null)
						return;

					getFolderJTextField().setText(
							exportJWSandDISKdir.getAbsolutePath());

					/*
					 * Radically store the path into the properties now - it
					 * really sucks to select that path all the time
					 */
					GPProps.set(Keys.LastExportFolder, exportJWSandDISKdir
							.getAbsolutePath());
					GPProps.store();

				}

			});

		}

		return folderChooserJButton;
	}

}
