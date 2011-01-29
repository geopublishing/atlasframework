package org.geopublishing.geopublisher.export;

import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GpTestingUtil;
import org.geopublishing.gpsync.AtlasFingerprint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.schmitzm.testing.TestingClass;

public class GpFtpAtlasExportTest extends TestingClass {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRequestFingerprint() {
		AtlasConfigEditable ace = GpTestingUtil.TestAtlas.small.getAce();
		ace.setBaseName("chartdemo");
		GpFtpAtlasExport gpFtpAtlasExport = new GpFtpAtlasExport(ace);
		AtlasFingerprint requestFingerprint = gpFtpAtlasExport
				.requestFingerprint();
		System.out.println(requestFingerprint);
	}

}
