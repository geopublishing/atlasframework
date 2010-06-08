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

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.geopublishing.geopublisher.swing.GpSwingUtil;

import net.miginfocom.swing.MigLayout;

/**
 * The status bar of the {@link GpFrame}
 */
public class GpStatusBar extends JPanel {

	public GpStatusBar(GpFrame gpjFrame) {
		super(new MigLayout("nogrid, w 100%"));
		
		add(gpjFrame.getHeapBar(),"east");
		add(new JLabel(GpSwingUtil.R("GpStatusBar.HeapBar.label")),"east");
	}

}
