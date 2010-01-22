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
package skrueger.atlas.gui;

import java.io.File;
import java.net.URL;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import junit.framework.TestCase;
import schmitzm.io.IOUtil;
import skrueger.atlas.gui.map.AtlasMapView;
import skrueger.atlas.map.Map;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.TestingUtil;

public class AtlasMapLayerLegendTest extends TestCase {

	public void testOpenAttributeTable() throws Exception {

		URL atlasURL = ClassLoader.getSystemResource("data/testAtlas1");
		File atlasFile = IOUtil.urlToFile(atlasURL);
		final AtlasConfigEditable atlasConfigE = TestingUtil
				.getAtlasConfigE(atlasFile.getAbsolutePath());


		SwingUtilities.invokeAndWait(new Runnable() {

			@Override
			public void run() {
				AtlasMapView atlasMapView = new AtlasMapView(null, atlasConfigE);
				
				Map map = atlasConfigE.getMapPool().get(0);
				atlasMapView.setMap(map);
				atlasMapView.initialize();
				JDialog d = new JDialog();
				d.setContentPane(atlasMapView);
				d.pack();
				
				d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

				if (TestingUtil.INTERACTIVE){
					d.setModal(true);
					d.setVisible(true);
				}
				
				d.dispose();
				
			}
			
		});
		Thread.sleep(1000);
		
		

//		 GeoMapPane geoMapPane = atlasMapView.getGeoMapPane();
//		 new AtlasLayerPaneGroup(geoMapPane,
//		 geoMapPane.getMapContext().getLayer(0), null, atlasMapView., null,
//		 null);
	}
}
