/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai- initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasViewer.swing;

import java.awt.Event;
import java.awt.MenuBar;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.jnlp.UnavailableServiceException;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AVProps;
import org.geopublishing.atlasViewer.JNLPUtil;
import org.geopublishing.atlasViewer.dp.Group;
import org.geopublishing.atlasViewer.swing.internal.AtlasJMenu;
import org.geopublishing.atlasViewer.swing.internal.AtlasMenuItem;

import schmitzm.swing.SwingUtil;

/**
 * This extension of a {@link JMenuBar} holds all the logic of an atlas
 * {@link MenuBar}.
 * 
 * @author SK
 * 
 */
public class AtlasMenuBar extends JMenuBar {

	public static Logger LOGGER = Logger.getLogger(AtlasMenuBar.class);

	private final AtlasViewerGUI atlasViewer;

	private JCheckBoxMenuItem jCheckBoxMenuItemAntiAliasing;

	boolean hasFileMenu = false;
	boolean hasHelpMenu = false;

	// private JCheckBoxMenuItem rasterCheckBoxMenuItem;

	public AtlasMenuBar(AtlasViewerGUI atlasViewer) {
		this.atlasViewer = atlasViewer;

		Group firstGroup = atlasViewer.getAtlasConfig().getRootGroup();

		// Parse the Groups and represent them as MenuEntrys
		group2Menus(this, firstGroup);

		if (!hasFileMenu) {
			// Create a new default help menu with standard labels!

			AtlasJMenu fileMenu = new AtlasJMenu(
					AtlasViewerGUI.R("AtlasViewer.FileMenu"));

			addFileMenuItems(fileMenu);
			add(fileMenu, 0);
		}

		if (!hasHelpMenu) {
			AtlasJMenu helpMenu = new AtlasJMenu(
					AtlasViewerGUI.R("AtlasViewer.HelpMenu"));

			addHelpMenuItems(helpMenu);
			add(helpMenu);
		}

	}

	/**
	 * This method recurses though a groups-tree and adds the entries to the
	 * menu. If a Menu contains more AtlasMenuItems that fit on the screen, they
	 * will be put into a submenu automatically.
	 * 
	 * @param parent
	 *            The {@link JMenu}, {@link JMenuBar} or {@link AtlasJMenu} that
	 *            will contain the elements.
	 * @param group
	 *            The {@link Group} to present in this menu
	 */
	private void group2Menus(JComponent parent, Group group) {
		Enumeration<?> children = group.children();

		/**
		 * Calculates, that every JMenuItem
		 */
		final int maxItems = new Double(Toolkit.getDefaultToolkit()
				.getScreenSize().getHeight()).intValue() / 25;
		int countHere = 0;

		for (; children.hasMoreElements();) {
			Object child = children.nextElement();
			countHere++;
			if (countHere > maxItems) {
				JMenu weiterMenu = new JMenu(
						AtlasViewerGUI
								.R("AtlasViewer.MenuToLongForScreen.Next"));
				parent.add(weiterMenu);
				parent = weiterMenu;
				countHere = 0;
			}
			if (child instanceof Group) {
				Group subgroup = (Group) child;
				JMenu subMenu = new AtlasJMenu(subgroup);

				// First insert the user-defined entries
				group2Menus(subMenu, subgroup);

				/**
				 * Fill this JMenu with the special Help functions
				 */
				if (subgroup.isHelpMenu()) {
					subMenu.addSeparator();
					addHelpMenuItems(subMenu);
				}
				/**
				 * Fill this JMenu with the special File functions
				 */
				if (subgroup.isFileMenu()) {
					subMenu.addSeparator();
					addFileMenuItems(subMenu);
				}

				parent.add(subMenu);
			} else {
				parent.add(new AtlasMenuItem(child, atlasViewer));
			}
		}
	}

	/**
	 * This method initializes jMenu
	 * 
	 * @return javax.swing.JMenu
	 */
	private void addFileMenuItems(JMenu fileMenu) {

		hasFileMenu = true;

		/**
		 * Removed until it has a proper function
		 */
		JMenuItem screenshotMenuItem = new AtlasMenuItem(
				new AbstractAction(
						AtlasViewerGUI
								.R("AtlasViewer.FileMenu.JMenuItem.save_smart_screenshots"),
						Icons.ICON_SCREENSHOT_SMALL) {

					@Override
					public void actionPerformed(ActionEvent e) {

						if ((atlasViewer.getMap() == null)
								|| (atlasViewer.getMapView() == null)) {
							JOptionPane.showMessageDialog(
									atlasViewer.getJFrame(),
									"Please open a map first!", "message", // i8n
									JOptionPane.OK_OPTION);
							return;
						}
						AtlasScreenScreenshotsDialog atlasScreenScreenshotsDialog = new AtlasScreenScreenshotsDialog(
								atlasViewer.getMapView());
						atlasScreenScreenshotsDialog.setVisible(true);
					}

				},
				AtlasViewerGUI
						.R("AtlasViewer.FileMenu.JMenuItem.ToolTip.save_smart_screenshots"));
		screenshotMenuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_PRINTSCREEN, 0, true));

		fileMenu.add(screenshotMenuItem);

		/**
		 * If we are in a JWS context, we allow to download the whole atlas at
		 * once
		 */
		try {
			fileMenu.add(getJWSDownloadAllMenuItem());
		} catch (UnavailableServiceException e1) {
		}

		/**
		 * The MenuItem Language to change the language of the application
		 */
		fileMenu.add(atlasViewer.getLanguageSubMenu());

		/**
		 * The MenuItem EXIT to end the application
		 */
		fileMenu.add(getExitMenuItem());

	}

	/**
	 * @return a button that allows to download and cache all JARs that are
	 *         referenced in the whole atlas at once. It is invisible if we are
	 *         not in a JavaWebStart environment.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 * @throws UnavailableServiceException
	 */
	public JMenuItem getJWSDownloadAllMenuItem()
			throws UnavailableServiceException {

		DownloadAllJNLPAction downloadAllJNLPAction = new DownloadAllJNLPAction(
				atlasViewer);

		JMenuItem jwsDownloadAllMenuItem = new AtlasMenuItem(
				downloadAllJNLPAction);

		ArrayList<String> haveToDownload = JNLPUtil
				.countPartsToDownload(atlasViewer.getAtlasConfig()
						.getDataPool());

		// This menu-item is disabled if all is already cached
		jwsDownloadAllMenuItem.setEnabled(haveToDownload.size() > 0);

		return jwsDownloadAllMenuItem;
	}

	/**
	 * This method initializes the jMenu that will show Help
	 * 
	 * @return javax.swing.JMenu
	 */
	private void addHelpMenuItems(JMenu helpMenu) {
		hasHelpMenu = true;

		/**
		 * Memory test menu item is removed because it should not be available
		 * in a release helpMenu.add(getMemoryTestMenuItem());
		 */

		// ******************************************************************
		// Menuitem to enable/ disable aa
		// ******************************************************************
		helpMenu.add(getJCheckBoxMenuItemAntiAliasing());

		// ******************************************************************
		// Menuitem to show About info - will only be created if there
		// exists a matching HTML file
		// ******************************************************************
		if (atlasViewer.getAtlasConfig().getAboutHTMLURL() != null) {
			JMenuItem aboutMenuItem = new AtlasMenuItem();
			aboutMenuItem.setText(AtlasViewerGUI.R(
					"AtlasViewer.HelpMenu.About", atlasViewer.getAtlasConfig()
							.getTitle().toString()));
			aboutMenuItem.setToolTipText(AtlasViewerGUI.R(
					"AtlasViewer.HelpMenu.About.tooltip", atlasViewer
							.getAtlasConfig().getTitle().toString()));

			aboutMenuItem.setActionCommand("about");
			aboutMenuItem.addActionListener(atlasViewer);
			helpMenu.add(aboutMenuItem);
		}

		// Add the logging menu, which allows to open the logfile, TODO
		// OptionsMenu
		helpMenu.add(SwingUtil.createChangeLog4JLevelJMenu());

		// {
		// final AVProps p = atlasViewer.getAtlasConfig().getProperties();
		// rasterCheckBoxMenuItem = new JCheckBoxMenuItem(new AbstractAction(
		// "Use new reader for raster") {
		//
		// @Override
		// public void actionPerformed(ActionEvent e) {
		// boolean oldValue = p.getBoolean(Keys.rasterReader, true);
		//
		// boolean newValue = !oldValue;
		// GPProps.set(
		// org.geopublishing.geopublisher.GPProps.Keys.rasterReader,
		// newValue);
		// rasterCheckBoxMenuItem.setSelected(newValue);
		// }
		// });
		// rasterCheckBoxMenuItem.setSelected(p.getBoolean(Keys.rasterReader,
		// true));
		// helpMenu.add(rasterCheckBoxMenuItem);
		// }

	}

	/**
	 * /** This method initializes jMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getExitMenuItem() {
		JMenuItem exitMenuItem = new AtlasMenuItem();
		exitMenuItem.setText(AtlasViewerGUI
				.R("AtlasViewer.FileMenu.ExitMenuItem.exit_application"));
		exitMenuItem.setIcon(Icons.ICON_EXIT_SMALL);
		exitMenuItem.addActionListener(atlasViewer);
		exitMenuItem.setActionCommand("exit");
		exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
				Event.CTRL_MASK, true));
		return exitMenuItem;
	}

	public void setJCheckBoxMenuItemAntiAliasing(
			JCheckBoxMenuItem jCheckBoxMenuItemAntiAliasing) {
		this.jCheckBoxMenuItemAntiAliasing = jCheckBoxMenuItemAntiAliasing;
	}

	public JCheckBoxMenuItem getJCheckBoxMenuItemAntiAliasing() {
		if (jCheckBoxMenuItemAntiAliasing == null) {
			jCheckBoxMenuItemAntiAliasing = new JCheckBoxMenuItem();
			jCheckBoxMenuItemAntiAliasing.setSelected(atlasViewer
					.getAtlasConfig().getProperties()
					.get(AVProps.Keys.antialiasingMaps, "1").equals("1"));
			jCheckBoxMenuItemAntiAliasing.setText(AtlasViewerGUI
					.R("AtlasViewer.AAMenuItem.SetText.toggle_antialiasing"));
			jCheckBoxMenuItemAntiAliasing.setActionCommand("antiAliasing");
			jCheckBoxMenuItemAntiAliasing.addActionListener(atlasViewer);
		}

		return jCheckBoxMenuItemAntiAliasing;
	}

}
