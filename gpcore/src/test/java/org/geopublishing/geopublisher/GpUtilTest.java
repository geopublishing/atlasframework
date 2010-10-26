package org.geopublishing.geopublisher;


import static org.junit.Assert.*;

import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GpUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testInitGpLogging()
	{
		GpUtil.initGpLogging();
		
		assertEquals(2, countRootLoggers());
		
		GpUtil.initGpLogging();
		
		assertEquals(2, countRootLoggers());
	}

	private int countRootLoggers() {
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
