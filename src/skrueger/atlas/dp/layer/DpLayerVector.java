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
package skrueger.atlas.dp.layer;

import org.apache.log4j.Logger;
import org.opengis.feature.type.GeometryDescriptor;

import schmitzm.jfree.chart.style.ChartStyle;
import skrueger.atlas.AtlasConfig;
import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.dp.DpEntryType;
import skrueger.geotools.StyledLayerInterface;

/**
 * This {@link DpEntry} represents a vector layer. Type <code>E</code>
 * represents the return type of {@link #getGeoObject()}
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
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
	public void dispose() {
		uncache();
	}

	@Override
	public void uncache() {
		super.uncache();
	}


}
