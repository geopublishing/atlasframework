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

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.DpEntryType;
import org.opengis.feature.type.GeometryDescriptor;

import schmitzm.jfree.chart.style.ChartStyle;
import skrueger.geotools.StyledLayerInterface;

/**
 * This {@link DpEntry} represents a vector layer. Type <code>E</code>
 * represents the return type of {@link #getGeoObject()}
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 * @param <E>
 * 
 * TODO can be removed!
 */
abstract public class DpLayerVector<E, CHART_STYLE_IMPL extends ChartStyle>
		extends DpLayer<E, CHART_STYLE_IMPL> implements StyledLayerInterface<E> {
	Logger log = Logger.getLogger(DpLayerVector.class);


	abstract public GeometryDescriptor getDefaultGeometry() throws Exception;

	/**
	 * Constructs a new empty {@link DpLayerVector}
	 * 
	 * @param ac
	 */
	public DpLayerVector(final AtlasConfig ac) {
		super(ac);
		// a fallback.. when the shape is opened, the type is updated TODO save in atlas.xml
		setType(DpEntryType.VECTOR); 
	}

	/**
	 * Disposes
	 */
	@Override
	public void dispose() {
		uncache();
	}

	@Override
	public void uncache() {
		super.uncache();
	}


}
