/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the AtlasViewer application - A GIS viewer application targeting at end-users with no GIS-experience. Its main purpose is to present the atlases created with the Geopublisher application.
 * http://www.geopublishing.org
 * 
 * AtlasViewer is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.atlas.gui;

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

import schmitzm.swing.SwingUtil;
import skrueger.atlas.AtlasConfig;
import skrueger.atlas.AtlasViewer;
import skrueger.atlas.gui.map.HTMLInfoJPane;

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

		final String msg = AtlasViewer.R("HtmlBrowserWindow.button.close");
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
