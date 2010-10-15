package org.geopublishing.atlasViewer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GpTestingUtil;
import org.geopublishing.geopublisher.GpTestingUtil.TestAtlas;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AtlasConfigTest {
	
	private AtlasConfigEditable ace;

	@Before
	public void setup() throws Exception
	{
		ace = GpTestingUtil.getAtlasConfigE(TestAtlas.small);
	}
	
	@After
	public void dispose() {
		if (ace != null) ace.dispose();
	}
	

	@Test
	public void testGetIconURL() {
		assertNotNull ("At least the fallback must be found.", ace.getIconURL() );
	}
	
	@Test
	public void getPpUpHTML() {
		assertNull ("Small atlas doesn't have a popup HTML", ace.getPopupHTMLURL());
	}

}
