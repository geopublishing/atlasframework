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
package org.geopublishing.atlasStyler;

import org.geotools.styling.Style;

import de.schmitzm.geotools.data.rld.RasterLegendData;

public class RasterStyleChangedEvent extends StyleChangedEvent {

	private final RasterLegendData legendMetadata;

	public RasterStyleChangedEvent(Style style, RasterLegendData legendMetadata) {
		super(style);
		this.legendMetadata = legendMetadata;
	}

	public RasterLegendData getLegendMetadata() {
		return legendMetadata;
	}

}
