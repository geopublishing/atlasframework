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
package org.geopublishing.atlasViewer.swing;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.FeatureSource;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DefaultMapLayer;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.operation.TransformException;

import schmitzm.geotools.io.GeoImportUtil;

public class AtlasMapLayer extends DefaultMapLayer {

	public AtlasMapLayer(GridCoverage2D geoObject, Style style) throws FactoryRegistryException, TransformException, SchemaException {
		super(geoObject, style);
	}
	
	public AtlasMapLayer(FeatureCollection geoObject, Style style) {
		super(geoObject, style);
	}
	

	public AtlasMapLayer(
			FeatureSource<SimpleFeatureType, SimpleFeature> featureSource,
			Style style) {
		super(featureSource, style);
	}

	@Override
	public ReferencedEnvelope getBounds() {
		ReferencedEnvelope bounds = super.getBounds();
		if (bounds.getCoordinateReferenceSystem() == null) return new ReferencedEnvelope(bounds, GeoImportUtil.getDefaultCRS());
		else return bounds;
	}
}
