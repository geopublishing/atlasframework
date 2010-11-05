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

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.dp.DpEntry;

import schmitzm.jfree.chart.style.ChartStyle;

/**
 * This abstract class is the parent of all {@link DpEntry}s that contain no
 * layer but media
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 */
public abstract class DpMedia<CHART_STYLE_IMPL extends ChartStyle> extends
		DpEntry<CHART_STYLE_IMPL> {
	static Logger LOGGER = Logger.getLogger(DpMedia.class);

	public DpMedia(AtlasConfig ac) {
		super(ac);
	}

	@Override
	public void uncache() {
		super.uncache();
	}

	@Override
	public final boolean isLayer() {
		return false;
	}

	public abstract Object show(Component owner);

}
