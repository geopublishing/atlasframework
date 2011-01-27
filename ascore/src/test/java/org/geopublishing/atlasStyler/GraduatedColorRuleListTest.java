/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasStyler;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.geotools.brewer.color.BrewerPalette;
import org.geotools.brewer.color.ColorBrewer;
import org.junit.Test;

import de.schmitzm.testing.TestingClass;
public class GraduatedColorRuleListTest extends TestingClass {

	@Test
	public void testGraduatedColorRuleList() throws IOException,
			TransformerException {

//		GraduatedColorPolygonRuleList polyRL = new GraduatedColorPolygonRuleList(
//				new StyledFS(TestDatasets.countryShp.getFeatureSource()));
//
//		polyRL.pushQuite();

		BrewerPalette[] palettes = ColorBrewer
				.instance(ColorBrewer.QUALITATIVE).getPalettes();
		for (BrewerPalette bp : palettes) {
//			polyRL.setBrewerPalette(bp);
			assertTrue(bp.getMaxColors() <= bp.getPaletteSuitability()
					.getMaxColors());
		}

//		polyRL.popQuite();

//		assertTrue(StylingUtil.validates(polyRL.getFTS()));
	}
}
