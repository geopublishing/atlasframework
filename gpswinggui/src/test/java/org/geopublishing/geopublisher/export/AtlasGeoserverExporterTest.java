package org.geopublishing.geopublisher.export;

import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GpTestingUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.netbeans.spi.wizard.ResultProgressHandle;

import skrueger.geotools.io.GsServerSettings;

public class AtlasGeoserverExporterTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	@Ignore
	public void testExport() throws Exception {
		AtlasConfigEditable ace = GpTestingUtil.TestAtlas.small.getAce();
		GsServerSettings settings = new GsServerSettings();
		settings.setUrl("http://localhost:8085/geoserver");
		settings.setUsername("admin");
		settings.setPassword("geoserver");

		AtlasGeoserverExporter ae = new AtlasGeoserverExporter(ace, settings);
		
		ResultProgressHandle progress = null;
		ae.export(progress);
	}

}
