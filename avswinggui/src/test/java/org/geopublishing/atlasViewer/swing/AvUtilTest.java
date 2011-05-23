package org.geopublishing.atlasViewer.swing;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AvUtilTest {
	AvUtil avu = new AvUtil();

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFixBrokenBgColor(){
		String oldHtml = "<html> <head> </head> <body bgcolor=\"ffe07a\"> <p><img height=\"298\" src=\"images/n4_java_vulkan_semeru_rauch_lava.jpg\" align=\"top\" width=\"447\">" +
				"</p><p><font size=\"+1\">Eruption des Vulkans Semeru auf der indonesischen Insel Java, 2004 </font><br><a href=\"browser://http://www.creativecommons.org/\">cc " +
				"licence</a>&#160;M. Rietze </p></body></html>";

		avu.fixBrokenBgColor(oldHtml);
	}
}
