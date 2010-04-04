/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasStyler.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.SingleLineSymbolRuleList;
import org.geopublishing.atlasStyler.SinglePointSymbolRuleList;
import org.geopublishing.atlasStyler.SinglePolygonSymbolRuleList;
import org.geopublishing.atlasStyler.SingleRuleList;
import org.geopublishing.atlasViewer.swing.Icons;
import org.geotools.feature.GeometryAttributeType;
import org.opengis.feature.type.GeometryDescriptor;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.swing.ExceptionDialog;
import schmitzm.swing.SwingUtil;

public class JScrollPaneSymbolsOnline extends JScrollPaneSymbols {

	private Logger LOGGER = ASUtil.createLogger(this);

	@Override
	protected String getDesc() {
		return AtlasStyler.R("SymbolSelector.Tabs.OnlineSymbols");
	}

	@Override
	protected Icon getIcon() {
		return Icons.ICON_ONLINE;
	}

	private final GeometryDescriptor attType;

	private URL url;

	/**
	 * 
	 * @param attType
	 *            The {@link GeometryAttributeType} determines which folder will
	 *            be scanned for SLD fragments.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public JScrollPaneSymbolsOnline(GeometryDescriptor attType) {
		this.attType = attType;

		switch (FeatureUtil.getGeometryForm(attType)) {
		case POINT:
			url = AtlasStyler.getPointSymbolsURL();
			break;
		case LINE:
			url = AtlasStyler.getLineSymbolsURL();
			break;
		case POLYGON:
			url = AtlasStyler.getPolygonSymbolsURL();
			break;
		default:
			throw new IllegalStateException(
					"GeometryAttributeType not recognized!");
		}

		rescan(true);
	}

	/**
	 * Rescanns the online folder for symbols in background. Symbols that have
	 * been removed are not beeing removed.
	 * 
	 * @param reset
	 *            Shall the {@link JList} be cleared before rescan
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public void rescan(boolean reset) {

		if (reset)
			((DefaultListModel) getJListSymbols().getModel()).clear();

		SwingWorker<Object, Object> symbolLoader = getWorker();
		symbolLoader.execute();
	}

	/**
	 * @return A SwingWorker that adds the Online-Symbols in a background task.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	private SwingWorker<Object, Object> getWorker() {
		SwingWorker<Object, Object> swingWorker = new SwingWorker<Object, Object>() {

			@Override
			protected Object doInBackground() throws Exception {
				LOGGER.debug("Seaching for online Symbols");

				String a = url.toExternalForm();
				a += "/index";
				URL index = new URL(a);

				BufferedReader in = null;
				try {
					in = new BufferedReader(new InputStreamReader(index
							.openStream()));

					// Sorting them alphabetically by using a set
					SortedSet<String> symbolURLStrings = new TreeSet<String>();
					String oneLIne;
					while ((oneLIne = in.readLine()) != null) {
						String lastPartInURI = new File(oneLIne.substring(1))
								.toURI().getRawPath();
						// Thats what happens on Windows: lastPartInURI = "C:/"
						// + lastPartInURI;
						// Ich work arroundthat.. not very ellegant!
						// LOGGER.debug("lastpartinURI " + lastPartInURI);
						if (lastPartInURI.matches(".[A-Z]:.+")) {
							lastPartInURI = lastPartInURI.substring(3);
						}
						String string = url.toExternalForm() + lastPartInURI;
						// LOGGER.debug("string " + string);
						symbolURLStrings.add(new URL(string).toString());
					}

					List<URL> symbolURLs = new ArrayList<URL>();
					for (String urlStr : symbolURLStrings) {
						symbolURLs.add(new URL(urlStr));
					}

					/**
					 * Add every symbol as a SymbolButton
					 */
					for (final URL url : symbolURLs) {

						/*******************************************************
						 * Checking if a Style with the same name allready
						 * exists
						 */
						// Name without .sld
						String newNameWithOUtSLD = url.getFile().substring(0,
								url.getFile().length() - 4);

						final DefaultListModel model = (DefaultListModel) getJListSymbols()
								.getModel();
						Enumeration<?> name2 = model.elements();
						while (name2.hasMoreElements()) {
							String styleName = ((SingleRuleList) name2
									.nextElement()).getStyleName();
							if (styleName.equals(newNameWithOUtSLD)) {
								// A Symbol with the same StyleName already
								// exits
								continue;
							}
						}

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

						boolean b = symbolRuleList.loadURL(url);
						if (b) {

							// Cache

							SwingUtilities.invokeLater(new Runnable() {
								public void run() {

									model.addElement(symbolRuleList);

									JScrollPaneSymbolsOnline.this
											.setViewportView(JScrollPaneSymbolsOnline.this
													.getJListSymbols());
									JScrollPaneSymbolsOnline.this.doLayout();
									JScrollPaneSymbolsOnline.this.repaint();
								}
							});
						} else {
							// Load failed
							LOGGER.warn("Loading " + url + " failed");
						}
					}

				} catch (IOException e) {
					JLabel notOnlineLabel = new JLabel(AtlasStyler
							.R("JScrollPaneSymbolsOnline.notOnlineErrorLabel"));
					JScrollPaneSymbolsOnline.this
							.setViewportView(notOnlineLabel);
				} catch (Exception e) {
					ExceptionDialog
							.show(
									SwingUtil
											.getParentWindowComponent(JScrollPaneSymbolsOnline.this),
									e);
				} finally {
					if (in != null)
						in.close();
				}
				//
				// /**
				// * Sort the file list, so we have a sorted list of symbols
				// */
				// Collections.sort(Arrays.asList(symbolPaths));

				return null;
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
			JMenuItem rescan = new JMenuItem(AtlasStyler
					.R("SymbolSelector.Tabs.OnlineSymbols.Action.Rescan"));
			rescan.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					rescan(true);
				}

			});
			popupMenu.add(rescan);

		}
		return popupMenu;
	}

}
