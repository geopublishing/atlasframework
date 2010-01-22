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
package skrueger.atlas.gui.internal;

import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JMenu;

import skrueger.atlas.dp.Group;

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
