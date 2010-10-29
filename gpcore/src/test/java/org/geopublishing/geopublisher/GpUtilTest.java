package org.geopublishing.geopublisher;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import schmitzm.io.IOUtil;

public class GpUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testBuildXMLWithoutFiltering()
	{

		String resLoc = "/autoPublish/build.xml";
		URL templateBuildXml = GpUtil.class.getResource(resLoc);
		assertNotNull(templateBuildXml);
		
		String template = IOUtil.readURLasString(templateBuildXml);
		assertTrue(template.contains("-a ${basedir}"));
	}


	@Test
	public void testInitGpLogging()
	{
		GpUtil.initGpLogging();
		
		assertEquals(2, countRootLoggers());
		
		GpUtil.initGpLogging();
		
		assertEquals(2, countRootLoggers());
	}


	public int countRootLoggers() {
		int countAppenders = 0;
		Enumeration allAppenders = Logger.getRootLogger().getAllAppenders();
		while (allAppenders.hasMoreElements()) {
			countAppenders ++;
			Appender nextElement = (Appender) allAppenders.nextElement();
			System.out.println(nextElement);
			assertNotNull(nextElement);
		}
		return countAppenders;
	}
}
