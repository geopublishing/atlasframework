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
package skrueger.atlas.dp.media;

import java.awt.Component;

import org.apache.log4j.Logger;

import schmitzm.jfree.chart.style.ChartStyle;
import skrueger.atlas.AtlasConfig;
import skrueger.atlas.dp.DpEntry;

/**
 * This abstract class is the parent of all {@link DpEntry}s that contain no
 * layer but media
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see skrueger.atlas.datapool.DatapoolEntry#isLayer()
	 */
	@Override
	public final boolean isLayer() {
		return false;
	}

	/**
	 * Should open Dialog that shows the media... Can be singleton, must not be
	 */
	public abstract Object show(Component owner);

}
