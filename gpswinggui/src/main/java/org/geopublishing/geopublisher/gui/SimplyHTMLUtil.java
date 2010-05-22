/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;
import org.geopublishing.geopublisher.AtlasConfigEditable;

import schmitzm.swing.ExceptionDialog;
import schmitzm.swing.SwingUtil;

import com.lightdev.app.shtm.DocumentPane;
import com.lightdev.app.shtm.SHTMLEditorKitActions;
import com.lightdev.app.shtm.SHTMLPanelImpl;
import com.lightdev.app.shtm.SHTMLPanelMultipleDocImpl;

/**
 * TODO DOKU
 * 
 * @author stefan
 */
public class SimplyHTMLUtil {
	static final Logger LOGGER = Logger.getLogger(SimplyHTMLUtil.class);

	/**
	 * 
	 * @param owner
	 * @param ace
	 * @param htmlFiles
	 *            A {@link List} of {@link File}s that all will automatically be
	 *            created if they don't exist! No matter if the user saves his
	 *            changes.
	 * @param tabTitles
	 * @param windowTitle
	 */
	@SuppressWarnings("deprecation")
	public static void openHTMLEditors(Component owner,
			AtlasConfigEditable ace, List<File> htmlFiles,
			List<String> tabTitles, String windowTitle) {

		if (tabTitles.size() != htmlFiles.size())
			throw new IllegalArgumentException(
					"Number of HTML URLs and Titles must be equal.");

		/**
		 * We open the HTML editor to edit the About information.
		 */

		final SHTMLPanelMultipleDocImpl htmlEditorPanel = new SHTMLPanelMultipleDocImpl();
		final JDialog editorDialog = new JDialog(SwingUtil
				.getParentWindow(owner), windowTitle);
		editorDialog.setModal(true);

		editorDialog
				.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		editorDialog.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				new SHTMLEditorKitActions.SHTMLFileExitAction(htmlEditorPanel)
						.actionPerformed(new ActionEvent(editorDialog, 999,
								SHTMLPanelImpl.exitAction));
			}

		});

		htmlEditorPanel.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("closing")) {
					editorDialog.dispose();
				}
			}

		});

		editorDialog.setSize(new Dimension(620, 400));
		editorDialog.getContentPane().add(htmlEditorPanel);

		// String content = "<html> <body> <p> test </p> </body> </html>";
		// htmlEditorPanel.setCurrentDocumentContent(content

		htmlEditorPanel.getTabbedPaneForDocuments().removeAll();

		/**
		 * Add one Tab for every language that is supported
		 */
		for (int i = 0; i < ace.getLanguages().size(); i++) {
			try {
				File htmlFile = htmlFiles.get(i);
				if (!htmlFile.exists())
					htmlFile.createNewFile();

				// Sad but true, we have to use the depreciated way here
				URL htmlURL = htmlFile.toURL();

				// Create new Document
				DocumentPane oneLanguage = new DocumentPane(htmlURL, i);

				// Set Title for the Tabbed Document
				oneLanguage.setName(tabTitles.get(i));

				LOGGER.info("SimplyHTML for " + htmlURL + " (was file = "
						+ htmlFile.getCanonicalPath() + ")");

				htmlEditorPanel.getTabbedPaneForDocuments().add(oneLanguage);
			} catch (Exception ex) {
				ExceptionDialog.show(owner, ex);
			}
		}

		SwingUtil.centerFrameOnScreenRandom(editorDialog);

		editorDialog.setVisible(true);
	}
}
