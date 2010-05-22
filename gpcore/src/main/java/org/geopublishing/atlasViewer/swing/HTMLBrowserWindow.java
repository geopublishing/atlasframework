/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasViewer.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasConfig;

import schmitzm.swing.SwingUtil;

/**
 * This dialog is used to display HTML info windows. Usually containing meta
 * information about a layer. TODO Suboptimal.. .HTMLBrowserWindow should become
 * a Facory/singleton pattern (getInstanceFor(URL))
 * 
 * @author Stefan A. Krueger
 * 
 */
public class HTMLBrowserWindow extends JDialog {

	static private final Logger LOGGER = Logger
			.getLogger(HTMLBrowserWindow.class);

	private final URL infoURL;

	private final AtlasConfig atlasConfig;

	/**
	 * 
	 * @param parent
	 *            A component of the parent Windows/Frame
	 * @param infoURL
	 *            The URL to present
	 * @param title
	 *            The title to set for the Window
	 * @param atlasConfig
	 *            if <code>null</code>, features like map://asdads links will
	 *            not work!
	 */
	public HTMLBrowserWindow(Component parent, URL infoURL, String title,
			AtlasConfig atlasConfig) {
		super(SwingUtil.getParentWindow(parent), title);
		this.infoURL = infoURL;
		this.atlasConfig = atlasConfig;
		init();
	}

	private void init() {
		JPanel cp = new JPanel(new BorderLayout());
		cp.add(new JScrollPane(new HTMLInfoJPane(infoURL, atlasConfig)),
				BorderLayout.CENTER);

		final String msg = AtlasViewerGUI.R("HtmlBrowserWindow.button.close");
		cp.add(new JButton(new AbstractAction(msg) {

			public void actionPerformed(ActionEvent e) {
				HTMLBrowserWindow.this.dispose();
			}
		}), BorderLayout.SOUTH);

		setContentPane(cp);
		setPreferredSize(new Dimension(600, 410));
		pack();
	}

}
