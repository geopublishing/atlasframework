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
package org.geopublishing.geopublisher.dp;

import java.awt.Component;
import java.io.File;
import java.net.URL;

import javax.swing.SwingUtilities;

import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.AtlasStatusDialogInterface;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSourceWFS;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.DpEditableInterface;
import org.geopublishing.geopublisher.gui.internal.GPDialogManager;
import org.geotools.data.FeatureSource;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;


public class DpLayerVectorFeatureSourceWFSEd extends
		DpLayerVectorFeatureSourceWFS implements DpEditableInterface {

	public DpLayerVectorFeatureSourceWFSEd(AtlasConfig ace,
			FeatureSource<SimpleFeatureType, SimpleFeature> wfsFS,
			boolean guiInteraction, Component owner) {
		super(ace);

		dataStore = wfsFS.getDataStore();
		featureSource = wfsFS;

		if ((guiInteraction) && (!SwingUtilities.isEventDispatchThread()))
			throw new RuntimeException("Not on EDT!");

		if (guiInteraction) {
			GPDialogManager.dm_EditAttribute.getInstanceFor(this, owner, this);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see skrueger.creator.dp.DatapoolEditableInterface#getAce()
	 */
	public AtlasConfigEditable getAce() {
		return (AtlasConfigEditable) getAtlasConfig();
	}

	@Override
	public void copyFiles(URL sourceUrl, Component owner, File targetDir,
			AtlasStatusDialogInterface atlasStatusDialog) throws Exception {
		throw new RuntimeException("not implemented yet!");
	}

}
