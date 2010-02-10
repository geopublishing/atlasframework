/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package skrueger.atlas;

import junit.framework.TestCase;

import org.junit.Test;

public class TestAVUtil extends TestCase {

	@Test
	public void testCleanFilename() {
		assertEquals("Remove leding numbers failed: ",
				"n01_neu_hvo_basin_srtm_utm.shp", AVUtil
						.cleanFilename("01_neu_hvo_basin_srtm_utm.shp"));
		;
		assertEquals("Remove leding numbers failed: ",
				"n78_neu_hao_basin_srtm_utm.shp", AVUtil
						.cleanFilename("78_neu_häo_basin_srtm_utm.shp"));
		;
	}

}
