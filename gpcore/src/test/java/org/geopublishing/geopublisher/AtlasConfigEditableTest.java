package org.geopublishing.geopublisher;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class AtlasConfigEditableTest {

	@Test
	public void testCheckBasename() {
		assertFalse(AtlasConfigEditable.checkBasename("ä"));
		assertFalse(AtlasConfigEditable.checkBasename(" asd "));
		assertFalse(AtlasConfigEditable.checkBasename("my atlas"));
		assertFalse(AtlasConfigEditable.checkBasename("a?"));
	}

}
