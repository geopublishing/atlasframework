package org.geopublishing.geopublisher.export.gphoster;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GpHosterClientTest {

	private GpHosterClient gphc;

	@Before
	public void setUp() throws Exception {
		// gphc = new GpHosterClient("http://localhost:8080/gp-hoster-jsf/");
		gphc = new GpHosterClient("http://localhost:8080/gp-hoster-jsf/");
		// gphc = new GpHosterClient();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testUserExists() throws IOException {
		if (gphc.checkService() != SERVICE_STATUS.OK)
			return;
		assertFalse(gphc.userExists("asd"));
		assertTrue(gphc.userExists("w2"));
	}

	@Test
	public void testUserCreate() throws IOException {
		if (gphc.checkService() != SERVICE_STATUS.OK)
			return;
		final String testUsername = "testUser" + System.currentTimeMillis();
		assertFalse(gphc.userExists(testUsername));
		assertTrue(gphc.userCreate(testUsername, "tzeggai@wikisquare.de"));
		assertTrue(gphc.userExists(testUsername));
		assertFalse("On the second try the username is not free anymore",
				gphc.userCreate(testUsername, "tzeggai@wikisquare.de"));
		// assertTrue(gphc.userDelete(testUsername, "???"));
		// assertFalse(gphc.userExists(testUsername));
	}

	@Test
	public void testAtlasBasenameFree() throws IOException {
		assertTrue(gphc.atlasBasenameFree("33234534"));
	}
}
