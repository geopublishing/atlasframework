package org.geopublishing.geopublisher;

import org.junit.Test;

import de.schmitzm.testing.TestingClass;
public class GPPropsTest extends TestingClass {

	@Test
	public void testInit() {
		// See if the resource gp_log4j.xml isfound
		
		GPProps.init(GPProps.PROPERTIES_FILENAME, GPProps.PROPERTIES_FOLDER);
	}
	
}
