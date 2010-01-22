package skrueger.atlas.gui;

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
