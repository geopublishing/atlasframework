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
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.SingleLineSymbolRuleList;
import org.geopublishing.atlasStyler.SinglePointSymbolRuleList;
import org.geopublishing.atlasStyler.SinglePolygonSymbolRuleList;
import org.geopublishing.atlasStyler.SingleRuleList;
import org.geopublishing.atlasViewer.swing.Icons;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.swing.ProgressWindow;
import org.opengis.feature.type.GeometryDescriptor;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.io.IOUtil;

/**
 * A special {@link JScrollPane} that does threaded filling of the GUI with SLD
 * Symbol buttons.
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 * 
 */
public class JScrollPaneSymbolsLocal extends JScrollPaneSymbols {
	protected Logger LOGGER = ASUtil.createLogger(this);

	protected final File dir;

	private final GeometryDescriptor attType;

	/**
	 * 
	 * @param attType
	 *            The {@link GeometryAttributeType} determines which folder will
	 *            be scanned for SLD fragments.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Tzeggai</a>
	 */
	public JScrollPaneSymbolsLocal(GeometryDescriptor attType) {
		this.attType = attType;
		dir = AtlasStyler.getSymbolsDir(attType);

		rescan(true);
	}

	@Override
	protected JPopupMenu getPopupMenu() {
		if (popupMenu == null) {
			popupMenu = new JPopupMenu();

			/*******************************************************************
			 * Rename a Symbol from disk
			 */
			JMenuItem rename = new JMenuItem(AtlasStyler
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
					File symbolFile = new File(AtlasStyler
							.getSymbolsDir(singleLocalRulesList
									.getGeometryDescriptor()), symbolFileName);

					String newName = ASUtil
							.askForString(
									JScrollPaneSymbolsLocal.this,
									singleLocalRulesList.getStyleName(),
									AtlasStyler
											.R("SymbolSelector.Tabs.LocalSymbols.Action.Rename.AskForNewName"));
					if ((newName == null)
							|| (newName.trim().equals(""))
							|| (newName.equals(singleLocalRulesList
									.getStyleName())))
						return;

					if (!newName.toLowerCase().endsWith(".sld")) {
						newName += ".sld";
					}

					File newSymbolFile = new File(AtlasStyler
							.getSymbolsDir(singleLocalRulesList
									.getGeometryDescriptor()), newName);
					try {
						FileUtils.moveFile(symbolFile, newSymbolFile);
					} catch (IOException e1) {
						LOGGER.error(e1);

						rescan(true);

						String message = AtlasStyler
								.R(
										"SymbolSelector.Tabs.LocalSymbols.Action.Rename.Error",
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
			JMenuItem remove = new JMenuItem(AtlasStyler
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
					File symbolFile = new File(AtlasStyler
							.getSymbolsDir(singleLocalRulesList
									.getGeometryDescriptor()), symbolFileName);

					int res = JOptionPane
							.showConfirmDialog(
									JScrollPaneSymbolsLocal.this,
									AtlasStyler
											.R(
													"SymbolSelector.Tabs.LocalSymbols.Action.Delete.Ask",
													singleLocalRulesList
															.getStyleName(),
													symbolFile.getName()), "",
									JOptionPane.YES_NO_OPTION);

					if (res != JOptionPane.YES_OPTION)
						return;

					if (!symbolFile.delete()) {
						String message = AtlasStyler
								.R(
										"SymbolSelector.Tabs.LocalSymbols.Action.Delete.Error",
										symbolFile.getName());
						LOGGER.warn(message);
						JOptionPane.showMessageDialog(
								JScrollPaneSymbolsLocal.this, message);
						rescan(true);
					} else {
						// Delete the entry from the JListSymbols
						((DefaultListModel) getJListSymbols().getModel())
								.remove(index);
						rescan(true);
					}

				}

			});
			popupMenu.add(remove);

			popupMenu.add(new JPopupMenu.Separator());
			/*******************************************************************
			 * Rescan directory
			 */
			JMenuItem rescan = new JMenuItem(AtlasStyler
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
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Tzeggai</a>
	 */
	private SwingWorker<List<SingleRuleList<?>>, Object> getWorker(
			final ProgressWindow pw) {

		// SwingWorkers may never be reused. Create it fresh!
		SwingWorker<List<SingleRuleList<?>>, Object> swingWorker = new SwingWorker<List<SingleRuleList<?>>, Object>() {
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
					LOGGER
							.error(
									"While adding the newly found icons to the table model",
									e);
				} catch (ExecutionException e) {
					LOGGER
							.error(
									"While adding the newly found icons to the table model",
									e);
				}

				setViewportView(getJListSymbols());
				doLayout();
				repaint();
				setVisible(true);
				pw.complete();
				pw.dispose();
			}

			@Override
			protected List<SingleRuleList<?>> doInBackground() throws Exception {
				LOGGER.debug("Seaching for local symbols on SwingWoker");

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

				/**
				 * Sort the file list, so we have a sorted list of symbols
				 */
				Collections.sort(Arrays.asList(symbolPaths));

				/**
				 * Add every symbol as a SymbolButton
				 */
				for (final String s : symbolPaths) {

					/***********************************************************
					 * Checking if a Style with the same name allready exists
					 */
					// Name without .sld
					String newNameWithOUtSLD = s.substring(0, s.length() - 4);

					final SingleRuleList symbolRuleList;

					switch (FeatureUtil.getGeometryForm(attType)) {
					case POINT:
						symbolRuleList = new SinglePointSymbolRuleList("");
						break;
					case LINE:
						symbolRuleList = new SingleLineSymbolRuleList("");
						break;
					case POLYGON:
						symbolRuleList = new SinglePolygonSymbolRuleList("");
						break;
					default:
						throw new IllegalStateException("unrecognized type");
					}

					// LOGGER.debug("s="+s);
					final File file = new File(dir, s);
					// LOGGER.debug("fiel="+file.toString());
					final URI toURI = file.toURI();
					// LOGGER.debug("URI = "+toURI.toURL());
					final URL toURL = toURI.toURL();
					// LOGGER.debug("URL="+toURL);
					boolean b = symbolRuleList.loadURL(toURL);
					if (b) {
						String key = JScrollPaneSymbolsLocal.this.getClass()
								.getSimpleName()
								+ symbolRuleList.getStyleName()
								+ symbolRuleList.getStyleTitle()
								+ symbolRuleList.getStyleAbstract();
						if (weakImageCache.get(key) == null) {
							/**
							 * Render the image now
							 */
							// LOGGER.debug("Rendering an Image for the cache.
							// key = "+key );
							weakImageCache.put(key, symbolRuleList
									.getImage(SYMBOL_SIZE));
						}

						entriesForTheList.add(symbolRuleList);
					} else {
						// Load failed
						LOGGER.warn("Loading " + s + " failed");
					}
				}

				return entriesForTheList;
			}

		};
		return swingWorker;
	}

	@Override
	public String getDesc() {
		return AtlasStyler.R("SymbolSelector.Tabs.LocalSymbols");
	}

	@Override
	public Icon getIcon() {
		return Icons.ICON_LOCAL;
	}

	/**
	 * Rescanns the folder for symbols in background. Symbols that have been
	 * removed are not beeing removed.
	 * 
	 * @param reset
	 *            If <code>true</code> the {@link JList} of symbols will be
	 *            cleard first.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Tzeggai</a>
	 */
	public synchronized void rescan(boolean reset) {
		if (reset)
			((DefaultListModel) getJListSymbols().getModel()).clear();

		setVisible(false);

		ProgressWindow pw = new ProgressWindow(this);
		pw.started();
		pw
				.setTitle(AtlasStyler
						.R("LocalSymbolsSelector.process.loading_title"));
		pw.setDescription(AtlasStyler
				.R("LocalSymbolsSelector.process.loading_description"));
		getWorker(pw).execute();

		SwingWorker<List<SingleRuleList<?>>, Object> symbolLoader = getWorker(pw);

		symbolLoader.execute();
	}

	@Override
	protected String getToolTip() {
		File symbolsDir = AtlasStyler
				.getSymbolsDir(attType);
		// Be more windows friendly
		return AtlasStyler.R("SymbolSelector.Tabs.LocalSymbols.TT", IOUtil.escapePath(symbolsDir));
	}
}
