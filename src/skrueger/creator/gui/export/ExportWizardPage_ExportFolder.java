/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the Geopublisher application - An authoring tool to facilitate the publication and distribution of geoproducts in form of online and/or offline end-user GIS.
 * http://www.geopublishing.org
 * 
 * Geopublisher is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.creator.gui.export;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.netbeans.spi.wizard.WizardPage;

import skrueger.atlas.resource.icons.Icons;
import skrueger.creator.AtlasCreator;
import skrueger.creator.GPProps;
import skrueger.creator.GPProps.Keys;

public class ExportWizardPage_ExportFolder extends WizardPage {
	/*
	 * The short description label that appears on the left side of the wizard
	 */
	static String desc = AtlasCreator.R("ExportWizard.Folder");
	private static final String validationFolderFailedMsg_NotExits = AtlasCreator
			.R("ExportWizard.Folder.ValidationError.NotExists");
	private static final String validationFolderFailedMsg_NotWritable = AtlasCreator
			.R("ExportWizard.Folder.ValidationError.NotWritable");

	JLabel explanationJLabel = new JLabel(AtlasCreator
			.R("ExportWizard.Folder.Explanation"));

	private JButton folderChooserJButton;
	JTextField folderJTextField;
	final static private JLabel folderTextFieldJLabel = new JLabel(AtlasCreator
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
		return desc;
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

					dc.setDialogTitle(AtlasCreator.R("Export.Dialog.WhereTo"));
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
