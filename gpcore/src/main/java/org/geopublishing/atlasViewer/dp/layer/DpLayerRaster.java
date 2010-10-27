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
package org.geopublishing.atlasViewer.dp.layer;

import org.geopublishing.atlasViewer.AtlasConfig;
import org.geotools.coverage.grid.GridCoverage2D;

import schmitzm.jfree.chart.style.ChartStyle;
import skrueger.RasterLegendData;

/**
 * This class represents any {@link GridCoverage2D} that is read from one file.
 * 
 * @see DpLayerRasterPyramid
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 */
public abstract class DpLayerRaster<E, CHART_STYLE_IMPL extends ChartStyle>
		extends DpLayer<E, CHART_STYLE_IMPL> {

	public DpLayerRaster(AtlasConfig ac) {
		super(ac);
	}

	// TODO 
	abstract public RasterLegendData getLegendMetaData();

	// TODO 
	abstract public void setLegendMetaData(RasterLegendData parseRasterLegendData);

}
