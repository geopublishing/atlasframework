package org.geopublishing.geopublisher.export;

import static org.junit.Assert.assertNotNull;

import java.awt.Container;

import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GpTestingUtil;
import org.geopublishing.gpsync.AtlasFingerprint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.spi.wizard.ResultProgressHandle;

import de.schmitzm.lang.LangUtil;
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
				.requestFingerprint(null);
		System.out.println(requestFingerprint);
	}

	@Test
	public void testUpload() throws Exception {
		AtlasConfigEditable ace = GpTestingUtil.TestAtlas.small.getAce();
		ace.setBaseName("testUpload_" + System.currentTimeMillis());
		GpFtpAtlasExport gpFtpAtlasExport = new GpFtpAtlasExport(ace);
		AtlasFingerprint requestFingerprint = gpFtpAtlasExport
				.requestFingerprint(null);
		System.out.println(requestFingerprint);
		gpFtpAtlasExport.export(progress);

		// wait a minute for gphoster to locate the zip
		Thread.sleep(LangUtil.MIN_MILLIS * 2);

		requestFingerprint = gpFtpAtlasExport.requestFingerprint(null);
		assertNotNull("After the upload the atlas must have a fingerprint",
				requestFingerprint);

	}

	private final ResultProgressHandle progress = new ResultProgressHandle() {

		@Override
		public void setProgress(String description, int currentStep,
				int totalSteps) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setProgress(int currentStep, int totalSteps) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setBusy(String description) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean isRunning() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void finished(Object result) {
			// TODO Auto-generated method stub

		}

		@Override
		public void failed(String message, boolean canNavigateBack) {
			// TODO Auto-generated method stub

		}

		@Override
		public void addProgressComponents(Container panel) {
			// TODO Auto-generated method stub

		}
	};

}
