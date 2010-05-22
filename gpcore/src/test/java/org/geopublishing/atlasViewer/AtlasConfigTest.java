package org.geopublishing.atlasViewer;

import static org.junit.Assert.assertNotNull;

import org.geopublishing.geopublisher.GpUtil;
import org.junit.Test;

public class AtlasConfigTest {

	@Test
	public void testResources() {
		assertNotNull(GpUtil.class
				.getResource(AtlasConfig.LICENSEHTML_RESOURCE_NAME));
		
		assertNotNull(GpUtil.class
				.getResource(AtlasConfig.SPLASHSCREEN_RESOURCE_NAME_FALLBACK));
		
		assertNotNull(GpUtil.class
				.getResource(AtlasConfig.JSMOOTH_SKEL_AD_RESOURCE1));

		assertNotNull(GpUtil.class
				.getResource(AtlasConfig.JSMOOTH_SKEL_AD_RESOURCE2));

		assertNotNull(GpUtil.class
				.getResource(AtlasConfig.JSMOOTH_SKEL_AD_RESOURCE3));

		assertNotNull(GpUtil.class
				.getResource(AtlasConfig.JWSICON_RESOURCE_NAME_FALLBACK));
		
	}
}
