package org.geopublishing.atlasViewer;

import static org.junit.Assert.*;

import java.net.URL;

import org.geopublishing.geopublisher.GpUtil;
import org.junit.Test;


public class AtlasConfigTest {

	@Test
	public void testFallbackIcons(){
		URL iconURL = GpUtil.class.getResource(
				AtlasConfig.JWSICON_RESOURCE_NAME_FALLBACK);
		assertNotNull(iconURL);
		
		assertNotNull( new AtlasConfig().getIconURL());

	}
}
