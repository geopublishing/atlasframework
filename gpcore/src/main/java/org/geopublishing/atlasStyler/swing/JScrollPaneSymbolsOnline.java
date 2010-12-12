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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.FreeMapSymbols;
import org.geopublishing.atlasStyler.SingleRuleList;
import org.geopublishing.atlasViewer.swing.Icons;
import org.geotools.data.DataUtilities;
import org.geotools.feature.GeometryAttributeType;
import org.opengis.feature.type.GeometryDescriptor;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import schmitzm.swing.ExceptionDialog;
import schmitzm.swing.SwingUtil;
import skrueger.swing.swingworker.AtlasStatusDialog;
import skrueger.swing.swingworker.AtlasSwingWorker;

public class JScrollPaneSymbolsOnline extends JScrollPaneSymbols {

	private final Logger LOGGER = ASUtil.createLogger(this);

	@Override
	protected String getDesc() {
		return AtlasStyler.R("SymbolSelector.Tabs.OnlineSymbols");
	}

	@Override
	protected Icon getIcon() {
		return Icons.ICON_ONLINE;
	}

	// private final GeometryDescriptor attType;

	private URL url;

	/**
	 * Construct a {@link JScrollPaneSymbolsOnline}, listing all .sld symbols
	 * from http://freemapsymbols.org. filtered for a special geoemtry type
	 * (point, line, polygon)
	 * 
	 * @param attType
	 *            The {@link GeometryAttributeType} determines which folder will
	 *            be scanned for SLD fragments.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public JScrollPaneSymbolsOnline(GeometryDescriptor attType) {
		this(FeatureUtil.getGeometryForm(attType));
	}

	/**
	 * Construct a {@link JScrollPaneSymbolsOnline}, listing all .sld symbols
	 * from http://freemapsymbols.org. filtered for a special geometry type
	 * (point, line, polygon)
	 * 
	 * @param attType
	 *            The {@link GeometryAttributeType} determines which folder will
	 *            be scanned for SLD fragments.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public JScrollPaneSymbolsOnline(GeometryForm geoForm) {
		super(geoForm);
		try {
			url = new URL(FreeMapSymbols.BASE_URL
					+ geoForm.toString().toLowerCase());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

		rescan(false);
	}

	// /**
	// * Rescans the online folder for symbols in background. Symbols that have
	// * been removed are not beeing removed.
	// *
	// * @param reset
	// * Shall the {@link JList} be cleared before rescan
	// *
	// * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	// Tzeggai</a>
	// */
	// public void rescan(boolean reset) {
	//
	// if (reset) {
	// getJListSymbols().setModel(new DefaultListModel());
	// imageCache.clear();
	// symbolPreviewComponentsCache.clear();
	// }
	//
	// AtlasSwingWorker<Void> symbolLoader = getWorker();
	// symbolLoader.executeModalNoEx();
	// }

	long lastTimeJScrollPaneUpdate = System.currentTimeMillis();

	/**
	 * @return A SwingWorker that adds the Online-Symbols in a background task.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	@Override
	protected AtlasSwingWorker<List<SingleRuleList<?>>> getWorker() {
		final AtlasStatusDialog sd = new AtlasStatusDialog(
				JScrollPaneSymbolsOnline.this, FreeMapSymbols.BASE_URL,
				"Updating Online Symbol list"); // i8n TODO
		AtlasSwingWorker<List<SingleRuleList<?>>> swingWorker = new AtlasSwingWorker<List<SingleRuleList<?>>>(
				sd) {

			@Override
			protected List<SingleRuleList<?>> doInBackground() {

				LOGGER.info("Seaching for online Symbols at " + url);
				List<SingleRuleList<?>> entriesForTheList = new ArrayList<SingleRuleList<?>>();
				try {

					URL index = DataUtilities.extendURL(url, "index");

					BufferedReader in = null;
					try {
						in = new BufferedReader(new InputStreamReader(
								index.openStream()));

						// Sorting them alphabetically by using a set
						SortedSet<String> symbolURLStrings = new TreeSet<String>();
						String oneLIne;
						while ((oneLIne = in.readLine()) != null) {
							String lastPartInURI = new File(
									oneLIne.substring(1)).toURI().getRawPath();
							// Thats what happens on Windows: lastPartInURI =
							// "C:/"
							// + lastPartInURI;
							// work arround.. not very ellegant!
							// LOGGER.debug("lastpartinURI " + lastPartInURI);
							if (lastPartInURI.matches(".[A-Z]:.+")) {
								lastPartInURI = lastPartInURI.substring(3);
							}
							String string = url.toExternalForm()
									+ lastPartInURI;
							// LOGGER.debug("string " + string);
							symbolURLStrings.add(new URL(string).toString());
						}

						List<URL> symbolURLs = new ArrayList<URL>();
						for (String urlStr : symbolURLStrings) {
							symbolURLs.add(new URL(urlStr));
						}

						/**
						 * Load every symbol from the URL into a SingleRuleList
						 */
						for (final URL url : symbolURLs) {
							String newNameWithOUtSLD = nameWithoutSld(url);
							sd.setDescription(newNameWithOUtSLD);
							cacheUrl(entriesForTheList, url);
						}

						updateJScrollPane();

					} catch (IOException e) {
						JLabel notOnlineLabel = new JLabel(
								AtlasStyler
										.R("JScrollPaneSymbolsOnline.notOnlineErrorLabel"));
						JScrollPaneSymbolsOnline.this
								.setViewportView(notOnlineLabel);
					} catch (Exception e) {
						ExceptionDialog
								.show(SwingUtil
										.getParentWindowComponent(JScrollPaneSymbolsOnline.this),
										e);
					} finally {
						if (in != null)
							try {
								in.close();
							} catch (IOException e) {
								LOGGER.error(e);
							}
					}

					return entriesForTheList;
				} catch (MalformedURLException e1) {
					ExceptionDialog.show(e1);
					return entriesForTheList;
				}

			}

		};
		return swingWorker;
	}

	@Override
	protected String getToolTip() {
		return AtlasStyler.R("SymbolSelector.Tabs.OnlineSymbols.TT");
	}

	@Override
	protected JPopupMenu getPopupMenu() {
		if (popupMenu == null) {
			popupMenu = new JPopupMenu();

			popupMenu.add(new JPopupMenu.Separator());
			/*******************************************************************
			 * Rescan directory
			 */
			JMenuItem rescan = new JMenuItem(
					AtlasStyler
							.R("SymbolSelector.Tabs.OnlineSymbols.Action.Rescan"));
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

}
