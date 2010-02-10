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
package skrueger.atlas.dp.layer;

import java.awt.Component;
import java.io.File;
import java.net.URL;

import javax.swing.SwingUtilities;

import org.geotools.data.FeatureSource;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import skrueger.atlas.gui.internal.AtlasStatusDialog;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.GPDialogManager;
import skrueger.creator.dp.DpEditableInterface;

public class DpLayerVectorFeatureSourceWFSEd extends
		DpLayerVectorFeatureSourceWFS implements DpEditableInterface {

	public DpLayerVectorFeatureSourceWFSEd(AtlasConfigEditable ace,
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
		return (AtlasConfigEditable) getAc();
	}

	@Override
	public void copyFiles(URL sourceUrl, Component owner,
			File targetDir,
			AtlasStatusDialog atlasStatusDialog) throws Exception {
	}

}
