/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher;

import junit.framework.TestCase;

import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.layer.DpLayerRasterPyramid;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;
import org.geopublishing.geopublisher.gui.internal.GPDialogManager;
import org.opengis.feature.type.Name;


public class EditAttributesJDialogTest extends TestCase {

	DpLayerRasterPyramid pyr;
	private DpLayerVectorFeatureSource dplv;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

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
					"no dplv with more at least 1 attrib found in atlas");
	}

	public void testCancel() throws InterruptedException {

		int backupSize = dplv.getAttributeMetaDataMap().size();
		
		Name aname = dplv.getSchema().getAttributeDescriptors().get(1).getName();
		
		boolean backupVisibility = dplv.getAttributeMetaDataMap().get(aname)
				.isVisible();

		dplv.getAttributeMetaDataMap().get(aname).getTitle().put("de", "aaa");

		EditAttributesJDialog dialog = GPDialogManager.dm_EditAttribute
				.getInstanceFor(dplv, null, dplv);
		
		dialog.setModal(GPTestingUtil.INTERACTIVE);
		dialog.setVisible(true);
		
		dialog.dispose();
	}

}
