package org.geopublishing.geopublisher;

import org.junit.Test;


public class GPPropsTest {

	@Test
	public void testInit() {
		// See if the resource gp_log4j.xml isfound
		
		GPProps.init(GPProps.PROPERTIES_FILENAME, GPProps.PROPERTIES_FOLDER);
	}
	
}
