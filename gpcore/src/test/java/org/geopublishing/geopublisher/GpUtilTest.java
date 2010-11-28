package org.geopublishing.geopublisher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import schmitzm.io.IOUtil;
import schmitzm.swing.ExceptionDialog;

public class GpUtilTest {

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
}
