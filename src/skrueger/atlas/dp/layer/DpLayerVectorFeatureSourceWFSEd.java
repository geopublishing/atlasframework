/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the AtlasViewer application - A GIS viewer application targeting at end-users with no GIS-experience. Its main purpose is to present the atlases created with the Geopublisher application.
 * http://www.geopublishing.org
 * 
 * AtlasViewer is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
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

		dataAccess = wfsFS.getDataStore();
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
