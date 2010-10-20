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
package org.geopublishing.atlasViewer.dp.media;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.dp.DpEntryType;

import schmitzm.jfree.chart.style.ChartStyle;
import skrueger.i8n.Translation;

public class DpMediaVideo extends DpMedia<ChartStyle> implements ActionListener {
	static Logger LOGGER = Logger.getLogger(DpMediaVideo.class);

	/**
	 * This constructor is used when loading an {@link AtlasConfig} from
	 * atlas.xml file. The AVI is already copied and
	 * {@link #setFilename(String)} is called later.
	 * 
	 * @param ac
	 *            {@link AtlasConfig} to create the {@link DpMediaVideo} in
	 */
	public DpMediaVideo(AtlasConfig ac) {
		super(ac);
		setTitle(new Translation(getAtlasConfig().getLanguages(), getFilename()));
		setDesc(new Translation());
		setType(DpEntryType.VIDEO);
	}


	@Override
	/*
	 * Shows the Video
	 */
	public Object show(Component parent) {

		uncache();
		return null;
	}

	/**
	 * Called only by the OK/Close Button of the VideoPlayer...
	 */
	public void actionPerformed(ActionEvent e) {
		uncache();
	}

	/**
	 * Clears all memory-intensive cache objects
	 */
	@Override
	public void uncache() {
		super.uncache();
		LOGGER.debug("uncaching video " + getTitle());
	}


	@Override
	public void exportWithGUI(Component owner) throws IOException {
		LOGGER.info("not implemented"); //TODO 
	}

}
