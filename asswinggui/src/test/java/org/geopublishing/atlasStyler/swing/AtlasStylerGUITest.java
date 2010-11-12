package org.geopublishing.atlasStyler.swing;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.geopublishing.atlasStyler.AtlasStyler;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import skrueger.geotools.StyledFS;

public class AtlasStylerGUITest  {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	@Ignore
	public void testWFS1() throws IOException {
		URL url = new URL(
				"http://svn.ugi.ru:1580/geoserver/ows?version=1.0.0&service=WFS&request=GetCapabilities");

		url = new URL(
				"http://localhost:8085/geoserver/ows?service=wfs&version=1.0.0&request=GetCapabilities");

		Map m = new HashMap();
		m.put(WFSDataStoreFactory.URL.key, url);
		m.put(WFSDataStoreFactory.TIMEOUT.key, new Integer(10000));
		m.put(WFSDataStoreFactory.MAXFEATURES.key, new Integer(100));

		DataStore wfs = (new WFSDataStoreFactory()).createDataStore(m);
		String[] typeNames = wfs.getTypeNames();
		for (String s : typeNames) {
			System.out.println(s);
		}
		FeatureSource wfsFS = wfs.getFeatureSource(typeNames[0]);

		wfsFS.getSchema();

		StyledFS wfsSFS = new StyledFS(wfsFS);

		AtlasStyler atlasStyler = new AtlasStyler(wfsSFS);

		atlasStyler.getRuleLists().add(atlasStyler.getRlf().createGraduatedColorRuleList(true));

		atlasStyler.getStyle();

	}

}
