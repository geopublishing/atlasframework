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
