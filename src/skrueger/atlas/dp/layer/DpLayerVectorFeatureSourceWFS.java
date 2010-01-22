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

import java.net.URL;

import org.geotools.data.FeatureSource;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import schmitzm.jfree.feature.style.FeatureChartStyle;
import skrueger.atlas.AtlasConfig;

public class DpLayerVectorFeatureSourceWFS extends DpLayerVectorFeatureSource {

	public DpLayerVectorFeatureSourceWFS(AtlasConfig ac) {
		super(ac);
		setId("wfs test layer");
	}
//
//	@Override
//	public void seeJAR(Component owner) throws AtlasFatalException, IOException {
//	}

	@Override
	public URL getInfoURL() {
		return null;
	}

	@Override
	public FeatureSource<SimpleFeatureType, SimpleFeature> getGeoObject() {
		throw new IllegalAccessError("Implementation for WFS pending!");
	}

	@Override
	public DpLayer<FeatureSource<SimpleFeatureType, SimpleFeature>, FeatureChartStyle> copy() {
		DpLayerVectorFeatureSourceWFS copy = new DpLayerVectorFeatureSourceWFS(ac);
		return copyTo(copy);
	}

}
