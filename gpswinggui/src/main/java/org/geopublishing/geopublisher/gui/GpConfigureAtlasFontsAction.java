/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.geopublishing.atlasViewer.swing.Icons;
import org.geopublishing.geopublisher.AtlasConfigEditable;

/**
 * This action allows to configure additional fonts that can be used in the atlas.
 *
 */
public class GpConfigureAtlasFontsAction extends AbstractAction {

	private final AtlasConfigEditable ace;
	private final Component owner;

	public GpConfigureAtlasFontsAction(String label, AtlasConfigEditable ace,
			Component owner) {
		super(label, Icons.ICON_FONTS_SMALL);
		this.ace = ace;
		this.owner = owner;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Opens a modal dialog to configure the fonts
		new ManageFontsDialog(owner, ace);
	}

}
