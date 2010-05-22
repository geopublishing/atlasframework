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

import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JMenu;

import org.geopublishing.atlasViewer.dp.Group;

public class AtlasJMenu extends JMenu {
	
	// TODO AtlasSettings
	private static final Font bigFont = new javax.swing.JLabel().getFont().deriveFont(16f);

	// static final Logger LOGGER = Logger.getLogger(AtlasJMenu.class);

	/**
	 * Creates a JMenu from a group Name = Name ToolTip = Desc Icon =
	 * 
	 * @param group
	 *            {@link Group} to use
	 */
	public AtlasJMenu(Group subgroup) {
		// LOGGER.debug("creating group with " + subgroup.getTitle());
		setText(subgroup.getTitle().toString());
		setToolTipText(subgroup.getDesc().toString());
		setBorder(BorderFactory.createEmptyBorder(8, 0, 2, 2));
		setFont( bigFont );
	}
	
	/**
	 * Creates a JMenu constructor for help and file only
	 */
	public AtlasJMenu(String title) {
		super(title);
		setBorder(BorderFactory.createEmptyBorder(8, 0, 2, 2));
		setFont( bigFont );
	}
}
