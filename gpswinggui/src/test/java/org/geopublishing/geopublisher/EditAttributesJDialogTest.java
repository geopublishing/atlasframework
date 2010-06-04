/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher;

import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.layer.DpLayerRasterPyramid;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;
import org.geopublishing.geopublisher.gui.internal.GPDialogManager;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.feature.type.Name;

public class EditAttributesJDialogTest {

	static DpLayerRasterPyramid pyr;
	private static DpLayerVectorFeatureSource dplv;

	@BeforeClass
	public static void setUp() throws Exception {

		AtlasConfigEditable atlasConfigE = GPTestingUtil.getAtlasConfigE();
		for (DpEntry dpe : atlasConfigE.getDataPool().values()) {
			if (dpe instanceof DpLayerRasterPyramid) {
				pyr = (DpLayerRasterPyramid) dpe;
			}
			if (dpe instanceof DpLayerVectorFeatureSource
					&& ((DpLayerVectorFeatureSource) dpe)
							.getAttributeMetaDataMap().size() >= 1) {
				dplv = (DpLayerVectorFeatureSource) dpe;
			}
		}

		if (dplv == null)
			throw new RuntimeException(
					"no dplv with more than at least 1 attrib found in atlas");
	}

	@Test
	public void testConstruct() throws InterruptedException {

		Name aname = dplv.getSchema().getAttributeDescriptors().get(1)
				.getName();

		dplv.getAttributeMetaDataMap().get(aname).getTitle().put("de", "aaa");

		if (GPTestingUtil.INTERACTIVE) {
			EditAttributesJDialog dialog = GPDialogManager.dm_EditAttribute
					.getInstanceFor(dplv, null, dplv);
			dialog.setModal(true);
			dialog.setVisible(true);
			dialog.dispose();
		}
	}

}
