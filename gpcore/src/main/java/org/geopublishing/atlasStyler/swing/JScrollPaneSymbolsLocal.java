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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.rulesLists.SingleRuleList;
import org.geopublishing.atlasViewer.swing.Icons;
import org.geotools.data.DataUtilities;

import de.schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import de.schmitzm.io.IOUtil;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.swingworker.AtlasStatusDialog;
import de.schmitzm.swing.swingworker.AtlasSwingWorker;

/**
 * A special {@link JScrollPane} that does threaded filling of the GUI with SLD
 * Symbol buttons.
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 * 
 */
public class JScrollPaneSymbolsLocal extends JScrollPaneSymbols {
	protected Logger LOGGER = LangUtil.createLogger(this);

	protected final File dir;

	/**
	 * 
	 * @param attType
	 *            The {@link GeometryAttributeType} determines which folder will
	 *            be scanned for SLD fragments.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public JScrollPaneSymbolsLocal(GeometryForm geoForm) {
		super(geoForm);
		dir = AtlasStylerVector.getSymbolsDir(geoForm);

		rescan(false);
	}

	@Override
	protected JPopupMenu getPopupMenu() {
		if (popupMenu == null) {
			popupMenu = new JPopupMenu();

			/*******************************************************************
			 * Rename a Symbol from disk
			 */
			JMenuItem rename = new JMenuItem(
					AtlasStylerVector
							.R("SymbolSelector.Tabs.LocalSymbols.Action.Rename"));
			rename.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					int index = getJListSymbols().locationToIndex(
							mouseCLickEvent.getPoint());
					SingleRuleList singleLocalRulesList = (SingleRuleList) ((DefaultListModel) getJListSymbols()
							.getModel()).get(index);
					String symbolFileName = singleLocalRulesList.getStyleName()
							+ ".sld";
					File symbolFile = new File(AtlasStylerVector
							.getSymbolsDir(singleLocalRulesList
									.getGeometryForm()), symbolFileName);

					String newName = ASUtil.askForString(
							JScrollPaneSymbolsLocal.this,
							singleLocalRulesList.getStyleName(),
							AtlasStylerVector
									.R("SymbolSelector.Tabs.LocalSymbols.Action.Rename.AskForNewName"));
					if ((newName == null)
							|| (newName.trim().equals(""))
							|| (newName.equals(singleLocalRulesList
									.getStyleName())))
						return;

					if (!newName.toLowerCase().endsWith(".sld")) {
						newName += ".sld";
					}

					File newSymbolFile = new File(AtlasStylerVector
							.getSymbolsDir(singleLocalRulesList
									.getGeometryForm()), newName);
					try {
						FileUtils.moveFile(symbolFile, newSymbolFile);
					} catch (IOException e1) {
						LOGGER.error("rename failed", e1);

						rescan(true);

						String message = AtlasStylerVector
								.R("SymbolSelector.Tabs.LocalSymbols.Action.Rename.Error",
										symbolFile.getAbsolutePath(),
										newSymbolFile.getAbsolutePath());
						LOGGER.warn(message);
						JOptionPane.showMessageDialog(
								JScrollPaneSymbolsLocal.this, message);
					}

					// Update the JListSymbols
					singleLocalRulesList.setStyleName(newName.substring(0,
							newName.length() - 4));
					getJListSymbols().repaint();
				}

			});
			popupMenu.add(rename);

			/*******************************************************************
			 * Delete a Symbol on disk
			 */
			JMenuItem remove = new JMenuItem(
					AtlasStylerVector
							.R("SymbolSelector.Tabs.LocalSymbols.Action.Delete"));
			remove.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					int index = getJListSymbols().locationToIndex(
							mouseCLickEvent.getPoint());
					SingleRuleList singleLocalRulesList = (SingleRuleList) ((DefaultListModel) getJListSymbols()
							.getModel()).get(index);
					String symbolFileName = singleLocalRulesList.getStyleName()
							+ ".sld";
					File symbolFile = new File(AtlasStylerVector
							.getSymbolsDir(singleLocalRulesList
									.getGeometryForm()), symbolFileName);

					int res = JOptionPane.showConfirmDialog(
							JScrollPaneSymbolsLocal.this,
							AtlasStylerVector
									.R("SymbolSelector.Tabs.LocalSymbols.Action.Delete.Ask",
											singleLocalRulesList.getStyleName(),
											symbolFile.getName()), "",
							JOptionPane.YES_NO_OPTION);

					if (res != JOptionPane.YES_OPTION)
						return;

					if (!symbolFile.delete()) {
						String message = AtlasStylerVector
								.R("SymbolSelector.Tabs.LocalSymbols.Action.Delete.Error",
										symbolFile.getName());
						LOGGER.warn(message);
						JOptionPane.showMessageDialog(
								JScrollPaneSymbolsLocal.this, message);
						rescan(true);
					} else {
						// Delete the entry from the JListSymbols
						((DefaultListModel) getJListSymbols().getModel())
								.remove(index);
						// rescan(true);
					}

				}

			});
			popupMenu.add(remove);

			popupMenu.add(new JPopupMenu.Separator());
			/*******************************************************************
			 * Rescan directory
			 */
			JMenuItem rescan = new JMenuItem(
					AtlasStylerVector
							.R("SymbolSelector.Tabs.LocalSymbols.Action.Rescan"));
			rescan.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					rescan(true);
				}

			});
			popupMenu.add(rescan);

		}
		return popupMenu;
	}

	/**
	 * @return A {@link SwingWorker} used in {@link #rescan(boolean)}
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	@Override
	protected AtlasSwingWorker<List<SingleRuleList<?>>> getWorker() {

		final AtlasStatusDialog sd = new AtlasStatusDialog(
				JScrollPaneSymbolsLocal.this,
				AtlasStylerVector.R("LocalSymbolsSelector.process.loading_title"),
				AtlasStylerVector
						.R("LocalSymbolsSelector.process.loading_description"));

		// SwingWorkers may never be reused. Create it fresh!
		AtlasSwingWorker<List<SingleRuleList<?>>> swingWorker = new AtlasSwingWorker<List<SingleRuleList<?>>>(
				sd) {
			final DefaultListModel model = (DefaultListModel) getJListSymbols()
					.getModel();

			@Override
			protected void done() {
				super.done();
				try {
					/**
					 * Create RulesLists and render the images
					 */
					List<SingleRuleList<?>> newlyFoundEntries = get();

					/**
					 * Finally add the RulesLists to the GUI
					 */

					for (SingleRuleList<?> newOne : newlyFoundEntries) {

						final Enumeration<?> existingObjects = model.elements();
						boolean alreadyIn = false;
						while (existingObjects.hasMoreElements()) {
							String styleName = ((SingleRuleList) existingObjects
									.nextElement()).getStyleName();

							// LOGGER.debug("comparing "+styleName + " vs "
							// + newOne.getStyleName());

							if (styleName.equals(newOne.getStyleName())) {
								// A Symbol with the same StyleName already
								// exits
								alreadyIn = true;
								break;
							}

						}
						if (alreadyIn)
							continue;

						model.addElement(newOne);
					}

				} catch (InterruptedException e) {
					LOGGER.error(
							"While adding the newly found icons to the table model",
							e);
				} catch (ExecutionException e) {
					LOGGER.error(
							"While adding the newly found icons to the table model",
							e);
				}

				setViewportView(getJListSymbols());
				doLayout();
				repaint();
				setVisible(true);
			}

			@Override
			protected List<SingleRuleList<?>> doInBackground() throws Exception {
				LOGGER.info("Seaching for local symbols on SwingWoker in "
						+ IOUtil.escapePath(dir));

				/**
				 * Create RulesLists and render the images
				 */
				List<SingleRuleList<?>> entriesForTheList = new ArrayList<SingleRuleList<?>>();

				String[] symbolPaths = dir.list(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String name) {
						if ((name.endsWith(".sld")) || (name.endsWith(".SLD")))
							return true;
						return false;
					}

				});

				// i8n
				if (symbolPaths == null)
					throw new RuntimeException(
							"Error reading "
									+ IOUtil.escapePath(dir)
									+ ". Please check permissions or broken link? Access to local symbols is broken.");

				final List<String> symbolPathsList = Arrays.asList(symbolPaths);
				Collections.sort(symbolPathsList);

				/**
				 * Add every symbol as a SymbolButton
				 */
				for (final String s : symbolPathsList) {

					// LOGGER.debug("s="+s);
					final File file = new File(dir, s);

					URL url = DataUtilities.fileToURL(file);

					String newNameWithOUtSLD = nameWithoutSld(url);
					sd.setDescription(newNameWithOUtSLD);

					cacheUrl(entriesForTheList, url);
				}

				return entriesForTheList;
			}

		};
		return swingWorker;
	}

	@Override
	public String getDesc() {
		return AtlasStylerVector.R("SymbolSelector.Tabs.LocalSymbols");
	}

	@Override
	public Icon getIcon() {
		return Icons.ICON_LOCAL;
	}

	@Override
	protected String getToolTip() {
		File symbolsDir = AtlasStylerVector.getSymbolsDir(geometryForm);
		// Be more windows friendly
		return AtlasStylerVector.R("SymbolSelector.Tabs.LocalSymbols.TT",
				IOUtil.escapePath(symbolsDir));
	}
}
