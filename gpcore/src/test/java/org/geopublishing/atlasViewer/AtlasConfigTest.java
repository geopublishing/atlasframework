package org.geopublishing.atlasViewer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URL;

import org.geopublishing.geopublisher.GpUtil;
import org.junit.Test;

public class AtlasConfigTest {

	@Test
	public void testFallbackIcons() {
		URL iconURL = GpUtil.class
				.getResource(AtlasConfig.JWSICON_RESOURCE_NAME_FALLBACK);
		assertNotNull(iconURL);

		assertNotNull(new AtlasConfig().getIconURL());
	}

	@Test
	public void testBasename() {
		AtlasConfig ac = new AtlasConfig();
		assertEquals("myatlas", ac.getBaseName());

		ac.setJnlpBaseUrl("http://www.bahn.de/cool/");
		assertEquals("cool", ac.getBaseName());

		ac.setJnlpBaseUrl("http://www.bahn.de/cool");
		assertEquals("cool", ac.getBaseName());

	}

}
