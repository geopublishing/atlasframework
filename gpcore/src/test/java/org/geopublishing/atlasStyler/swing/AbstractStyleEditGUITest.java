package org.geopublishing.atlasStyler.swing;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.schmitzm.regex.RegexCache;
import de.schmitzm.testing.TestingClass;

public class AbstractStyleEditGUITest extends TestingClass {

	@Test
	public void testUpdateExternalGraphicButton()
	 throws Exception {
		String regex = "\\$\\{.*?\\}";
		String url="http://chart?cht=bvg&chd=t:${POP_CNTRY}|${AREA}|${DEMSTAT}&chco=4D89F9&chs=100x100";
		assertTrue(RegexCache.getInstance().getPattern(regex).matcher(url).find());
	}

}
