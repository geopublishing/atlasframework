package org.geopublishing.atlasViewer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.URL;

import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GpTestingUtil;
import org.geopublishing.geopublisher.GpTestingUtil.TestAtlas;
import org.geopublishing.geopublisher.GpUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.schmitzm.testing.TestingClass;
public class AtlasConfigTest extends TestingClass {
	private AtlasConfigEditable ace;

	@Before
	public void setup() throws Exception {
		ace = GpTestingUtil.getAtlasConfigE(TestAtlas.small);
	}

	@After
	public void dispose() {
		if (ace != null)
			ace.dispose();
	}

	@Test
	public void testIconAndPopupURL() {
		assertNotNull("At least the fallback must be found.", ace.getIconURL());
		assertNull("Small atlas doesn't have a popup HTML",
				ace.getPopupHTMLURL());
	}

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
