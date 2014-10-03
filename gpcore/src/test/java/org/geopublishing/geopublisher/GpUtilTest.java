package org.geopublishing.geopublisher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVector;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSourceShapefile;
import org.geopublishing.atlasViewer.exceptions.AtlasException;
import org.geopublishing.geopublisher.GpTestingUtil.TestAtlas;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.FileDataStoreFactorySpi;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.SchemaException;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;

import de.schmitzm.geotools.feature.FeatureUtil;
import de.schmitzm.geotools.io.GeoImportUtil;
import de.schmitzm.io.IOUtil;
import de.schmitzm.swing.ExceptionDialog;
import de.schmitzm.testing.TestingClass;

public class GpUtilTest extends TestingClass {

    @Test
    public void testSendGPBugReport_WithAddInfos() {
	GpUtil.initBugReporting();
	assertEquals(3, ExceptionDialog.getAdditionalAppInfo().size());
	for (Object o : ExceptionDialog.getAdditionalAppInfo()) {
	    System.out.println(o.toString());
	}
    }

    @Test
    public void testBuildXMLWithoutFiltering() {
	String resLoc = "/autoPublish/build.xml";
	URL templateBuildXml = GpUtil.class.getResource(resLoc);
	assertNotNull(templateBuildXml);

	String template = IOUtil.readURLasString(templateBuildXml);
	assertTrue(template.contains("-a ${basedir}"));
    }

    @Test
    @Ignore
    // broken since GPTestingUtil extends TestingUtil
    public void testInitGpLogging() {
	GpUtil.initGpLogging();

	assertEquals(2, countRootLoggers());

	GpUtil.initGpLogging();

	assertEquals(2, countRootLoggers());
    }

    public int countRootLoggers() {
	int countAppenders = 0;
	Enumeration allAppenders = Logger.getRootLogger().getAllAppenders();
	while (allAppenders.hasMoreElements()) {
	    countAppenders++;
	    Appender nextElement = (Appender) allAppenders.nextElement();
	    System.out.println(nextElement);
	    assertNotNull(nextElement);
	}
	return countAppenders;
    }

    @Test
    public void testAppendFeature() throws SchemaException, AtlasException, FactoryException,
	    TransformException, SAXException, IOException, ParserConfigurationException,
	    URISyntaxException {
	AtlasConfigEditable ace = GpTestingUtil.getAtlasConfigE(TestAtlas.small);
	Set<DpLayerVector> vl = ace.getDataPool().getVectorLayers();
	DpLayerVector vector = vl.iterator().next();

	// vector.getDataDirname() + vector.getFilename()
	SimpleFeatureCollection collection = FeatureCollections.newCollection();
	collection.addAll(vector.getFeatureCollection());
	assertEquals(collection.size(), 251);
	SimpleFeature featureNeu = FeatureUtil.createSampleFeature(vector.getSchema(), "");
	collection.add(featureNeu);
	System.out.println(collection.size());
	assertEquals(collection.size(), 252);

	DataStore dataStore = GeoImportUtil.readDataStoreFromShape(ace.getFileFor(vector).toURI()
		.toURL());
	File file = new File("/home/cib/my.shp");

	ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

	Map<String, Serializable> params = new HashMap<String, Serializable>();
	params.put("url", file.toURI().toURL());
	params.put("create spatial index", Boolean.TRUE);

	ShapefileDataStore newDataStore = (ShapefileDataStore) dataStore;

	newDataStore.createSchema(vector.getSchema());

	String typeName = newDataStore.getTypeNames()[0];
	SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
	SimpleFeatureType SHAPE_TYPE = featureSource.getSchema();
	System.out.println("SHAPE:" + SHAPE_TYPE);
	 if (featureSource instanceof FeatureStore) {
	     // we have write access to the feature data
	     FeatureStore featureStore = (FeatureStore) featureSource;

	     // add some new features
	     Transaction t = new DefaultTransaction("add");
	     featureStore.setTransaction(t);
	     try {
	         featureStore.addFeatures( collection );
	         t.commit();
	     } catch (Exception ex) {
	         ex.printStackTrace();
	         t.rollback();
	     } finally {
	         t.close();
	     }
	 }
    }
}
