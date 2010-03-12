/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package schmitzm.geotools.gui;

import java.awt.Color;
import java.awt.RenderingHints;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.shape.ShapefileRenderer;
import org.geotools.renderer.shape.TestUtilites;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.Id;
import org.opengis.filter.identity.FeatureId;

import schmitzm.geotools.FilterUtil;
import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.styling.StylingUtil;
import skrueger.atlas.gui.AtlasMapLayer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

public class JMapPaneTest extends TestCase {

	public void testDistWithin() {
		FilterFactory2 FF = FilterUtil.FILTER_FAC2;

		Point p1 = FeatureUtil.GEOMETRY_FACTORY.createPoint(new Coordinate(10.,
				10.));
		Point p2 = FeatureUtil.GEOMETRY_FACTORY.createPoint(new Coordinate(20.,
				20.));

		// FilterUtil.FILTER_FAC2.dwithin(FF.p1,p2,)
	}

	public PolygonSymbolizer createCoolPolygonSymbolizer() {
		final StyleBuilder STYLE_BUILDER = StylingUtil.STYLE_BUILDER;

		Stroke outline = STYLE_BUILDER.createStroke(Color.red, 3);
		Mark slashes = STYLE_BUILDER.createMark("shape://backslash");
		slashes.setStroke(STYLE_BUILDER.createStroke(Color.red, 3.));

		Graphic fillGraphic = STYLE_BUILDER.createGraphic(null, slashes, null);
		fillGraphic.setSize(CommonFactoryFinder.getFilterFactory2(null)
				.literal(9.));

		Fill fill = STYLE_BUILDER.createFill();
		fill.setGraphicFill(fillGraphic);
		PolygonSymbolizer polS0 = STYLE_BUILDER.createPolygonSymbolizer(
				outline, fill);

		return polS0;
	}

	public void testSelection() throws Exception {
		// DpLayerVectorFeatureSource cities = TestingUtil.getCities();

		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("url", DataUtilities.fileToURL(new File(
				"/home/stefan/afrikan_countries.shp")));
		DataStore dataStore = DataStoreFinder.getDataStore(params);

		final FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore
				.getFeatureSource(dataStore.getTypeNames()[0]);
		FeatureCollection<SimpleFeatureType, SimpleFeature> features = featureSource
				.getFeatures();

		Set<FeatureId> fidsOf5 = new HashSet<FeatureId>();
		ReferencedEnvelope boundsOf5 = new ReferencedEnvelope();

		// List the 50 first FIDs and put them into a Set
		Iterator<SimpleFeature> fIt = features.iterator();
		
		// Skip some features
		for (int i = 0; i++ < 10; fIt.next());

		for (int i = 0; i < 15; i++) {
			SimpleFeature sf = fIt.next();
			final FeatureId identifier = sf.getIdentifier();
			final String id = identifier.getID();
			System.out.println(i + "=" + identifier + "    " + id);

			boundsOf5.include(sf.getBounds());

			fidsOf5.add(identifier);
		}
		features.close(fIt);

		System.out.println("Bounds of all = " + features.getBounds());
		System.out.println("Bounds of   5 = " + boundsOf5);
		// assertEquals("ReferencedEnvelope[441890.0 : 457653.0, 1264324.0 : 1278331.0]",
		// boundsOf5.toString());

		Id filter5 = FilterUtil.FILTER_FAC2.id(fidsOf5);

		final StyleBuilder SB = StylingUtil.STYLE_BUILDER;

		Rule rule1 = SB.createRule(SB.createLineSymbolizer(Color.green));
		Rule rule2 = SB.createRule(SB.createPolygonSymbolizer(Color.red));
		rule2.setFilter(filter5);

		Style style = SB.createStyle();
		assertEquals(0, style.featureTypeStyles().size());

		style.featureTypeStyles().add(
				SB.createFeatureTypeStyle("Feature", new Rule[] { rule1, rule2  }));

		// Oe featuretypes, two rules
		 assertEquals(1, style.featureTypeStyles().size());
		 assertEquals(2, style.featureTypeStyles().get(0).rules().size());
		 // second rule has a FID filter
		 assertTrue(style.featureTypeStyles().get(0).rules().get(1).getFilter() instanceof Id);

		AtlasMapLayer layer = new AtlasMapLayer(featureSource, style);
		MapContext mapContext = new DefaultMapContext();

		mapContext.addLayer(layer);
		GTRenderer renderer = new ShapefileRenderer();
//		GTRenderer renderer = new StreamingRenderer();
		renderer.setContext(mapContext);
		RenderingHints renderingHints = new RenderingHints(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_DEFAULT);
		renderer.setJava2DHints(renderingHints);

		System.out.println(style);

		TestUtilites.showRender("full", renderer, 1000, features.getBounds());

		{
//			ReferencedEnvelope zoomOut = boundsOf5;
//			for (int i = 1; i < 4; i++) {
//				zoomOut = zoomOut(zoomOut);
//				TestUtilites.showRender("zomming in step " + i, renderer,
//						200 * i, zoomOut);
//			}

			ReferencedEnvelope zoomIn = boundsOf5;
			for (int i = 1; i < 10; i++) {
				zoomIn = zoomIn(zoomIn);
				TestUtilites.showRender("zomming in step " + i, renderer, 1100,
						zoomIn);
			}
		}

	}

	private ReferencedEnvelope zoomIn(ReferencedEnvelope bounds) {

		ReferencedEnvelope b2 = new ReferencedEnvelope(bounds);
		double c = 1./6.;
		b2.expandBy(-b2.getSpan(0) * c, -b2.getSpan(1) *c );

		return b2;
	}
	//	
	// public void testSelection() throws Exception {
	// // DpLayerVectorFeatureSource cities = TestingUtil.getCities();
	//		
	// Map params = new HashMap();
	// params.put("url", DataUtilities.fileToURL(new File(
	// "/home/stefan/afrikan_countries.shp")));
	// DataStore dataStore = DataStoreFinder.getDataStore(params);
	//		
	// final FeatureSource<SimpleFeatureType, SimpleFeature> featureSource =
	// dataStore
	// .getFeatureSource(dataStore.getTypeNames()[0]);
	// FeatureCollection<SimpleFeatureType, SimpleFeature> features =
	// featureSource
	// .getFeatures();
	//		
	// Set<FeatureId> fidsOf5 = new HashSet<FeatureId>();
	// ReferencedEnvelope boundsOf5 = new ReferencedEnvelope();
	//		
	// // List the 50 first FIDs and put them into a Set
	// Iterator<SimpleFeature> fIt = features.iterator();
	// fIt.next();
	// fIt.next();
	// fIt.next();
	// fIt.next();
	// fIt.next();
	//		
	// for (int i = 0; i < 15; i++) {
	// SimpleFeature sf = fIt.next();
	// final FeatureId identifier = sf.getIdentifier();
	// final String id = identifier.getID();
	// System.out.println(i + "=" + identifier + "    " + id);
	//			
	// boundsOf5.include(sf.getBounds());
	//			
	// fidsOf5.add(identifier);
	// }
	// features.close(fIt);
	//		
	// System.out.println("Bounds of all = " + features.getBounds());
	// System.out.println("Bounds of   5 = " + boundsOf5);
	// //
	// assertEquals("ReferencedEnvelope[441890.0 : 457653.0, 1264324.0 : 1278331.0]",
	// // boundsOf5.toString());
	//		
	// Id filter5 = FilterUtil.FILTER_FAC2.id(fidsOf5);
	//		
	// final StyleBuilder SB = StylingUtil.STYLE_BUILDER;
	//		
	// Rule rule1 = SB.createRule(SB.createLineSymbolizer(Color.green));
	// // // Rule rule2 = SB.createRule(SB.createPolygonSymbolizer(Color.red));
	// // Rule rule2 = SB.createRule(createCoolPolygonSymbolizer());
	// // rule2.setFilter(filter5);
	//		
	// Style style = SB.createStyle();
	// assertEquals(0, style.featureTypeStyles().size());
	//		
	// style.featureTypeStyles().add(
	// SB.createFeatureTypeStyle("Feature", new Rule[] { rule1 }));
	// // style.featureTypeStyles().add(
	// // SB.createFeatureTypeStyle("Feature", new Rule[] { rule2 }));
	// final FeatureTypeStyle featureType =
	// StylingUtil.createSelectionStyle(features);
	// featureType.rules().get(0).setFilter(filter5);
	//		
	// style.featureTypeStyles().add(
	// featureType);
	//		
	// // Two featuretypes, one rule each
	// // assertEquals(2, style.featureTypeStyles().size());
	// // assertEquals(1, style.featureTypeStyles().get(0).rules().size());
	// //
	// assertNull(style.featureTypeStyles().get(0).rules().get(0).getFilter());
	// // assertTrue(style.featureTypeStyles().get(1).rules().get(0).getFilter()
	// // instanceof Id);
	//		
	// DefaultMapLayer layer = new DefaultMapLayer(featureSource, style);
	// MapContext mapContext = new DefaultMapContext();
	//		
	// mapContext.addLayer(layer);
	// ShapefileRenderer renderer = new ShapefileRenderer(mapContext);
	// RenderingHints renderingHints = new RenderingHints(
	// RenderingHints.KEY_ANTIALIASING,
	// RenderingHints.VALUE_ANTIALIAS_DEFAULT);
	// renderer.setJava2DHints(renderingHints);
	//		
	// System.out.println(style);
	//		
	// TestUtilites.showRender("full", renderer, 1000, features.getBounds());
	//		
	// {
	// ReferencedEnvelope zoomOut = boundsOf5;
	// for (int i = 1; i < 4; i++) {
	// zoomOut = zoomOut(zoomOut);
	// TestUtilites.showRender("zomming in step " + i, renderer,
	// 200 * i, zoomOut);
	// }
	//			
	// ReferencedEnvelope zoomIn = boundsOf5;
	// for (int i = 1; i < 10; i++) {
	// zoomIn = zoomIn(zoomIn);
	// TestUtilites.showRender("zomming in step " + i, renderer,
	// 1500, zoomIn);
	// }
	// }
	//		
	// }


//	private ReferencedEnvelope zoomOut(ReferencedEnvelope bounds) {
//
//		ReferencedEnvelope b2 = new ReferencedEnvelope(bounds);
//		double c = (new Random().nextInt(10) + 40) / 10.;
//		b2.expandBy(b2.getSpan(0) / c, b2.getSpan(1) / c);
//
//		return b2;
//	}
}
