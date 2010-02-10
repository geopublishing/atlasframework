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
package skrueger.sld.gui;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import junit.framework.TestCase;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.renderer.shape.TestUtilites;

import skrueger.sld.AtlasStylerTest;
import skrueger.sld.SinglePolygonSymbolRuleList;
import skrueger.sld.SingleRuleList;

public class SymbolEditorGUITest extends TestCase {

	public void testOpen() throws IOException {
		URL shpURL = AtlasStylerTest.class.getClassLoader().getResource(
				"data/shp countries/country.shp");
		assertNotNull(shpURL);
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("url", shpURL);
		DataStore dataStore = DataStoreFinder.getDataStore(params);
		FeatureSource featureSource_polygon = dataStore
				.getFeatureSource(dataStore.getTypeNames()[0]);

		// SingleRuleList singleSymbolRuleList = new
		// SinglePointSymbolRuleList();
		SingleRuleList singleSymbolRuleList = new SinglePolygonSymbolRuleList("");

		SymbolEditorGUI editorGUI = new SymbolEditorGUI((JFrame) null,
				singleSymbolRuleList);
		editorGUI.setModal(TestUtilites.INTERACTIVE);
		editorGUI.setVisible(true);
	}

}
