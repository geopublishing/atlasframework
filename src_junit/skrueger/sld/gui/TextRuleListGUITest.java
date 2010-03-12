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

import javax.swing.JDialog;

import junit.framework.TestCase;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import skrueger.creator.TestingUtil;
import skrueger.geotools.StyledFS;
import skrueger.geotools.StyledFeaturesInterface;
import skrueger.sld.AtlasStyler;
import skrueger.sld.AtlasStylerTest;
import skrueger.sld.TextRuleList;

public class TextRuleListGUITest extends TestCase {
	

	public void testOpenStreets() throws IOException {
		URL shpURL = AtlasStylerTest.class.getClassLoader().getResource(
				"data/shp_strassen/waterways.shp");
		assertNotNull(shpURL);
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("url", shpURL);
		DataStore dataStore = DataStoreFinder.getDataStore(params);
		FeatureSource<SimpleFeatureType, SimpleFeature> featureSource_polygon = dataStore
				.getFeatureSource(dataStore.getTypeNames()[0]);

		// SingleRuleList singleSymbolRuleList = new
		// SinglePointSymbolRuleList();
		StyledFeaturesInterface<?> styledPolygons = new StyledFS(
				featureSource_polygon);

		TextRuleList textRuleList = new TextRuleList(styledPolygons);
		AtlasStyler as = new AtlasStyler(styledPolygons);

		TextRuleListGUI editorGUI = new TextRuleListGUI(textRuleList, as);
		
		JDialog dialog = new JDialog();
		dialog.setContentPane(editorGUI);
		dialog.pack();

		if (TestingUtil.INTERACTIVE) {
			dialog.setModal(true);
		}
		
		dialog.setVisible(true);
	}

	public void testOpen() throws IOException {
		URL shpURL = AtlasStylerTest.class.getClassLoader().getResource(
				"data/shp countries/country.shp");
		assertNotNull(shpURL);
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("url", shpURL);
		DataStore dataStore = DataStoreFinder.getDataStore(params);
		FeatureSource<SimpleFeatureType, SimpleFeature> featureSource_polygon = dataStore
				.getFeatureSource(dataStore.getTypeNames()[0]);

		// SingleRuleList singleSymbolRuleList = new
		// SinglePointSymbolRuleList();
		StyledFeaturesInterface<?> styledPolygons = new StyledFS(
				featureSource_polygon);

		TextRuleList textRuleList = new TextRuleList(styledPolygons);
		AtlasStyler as = new AtlasStyler(styledPolygons);

		TextRuleListGUI editorGUI = new TextRuleListGUI(textRuleList, as);
		
		JDialog dialog = new JDialog();
		dialog.setContentPane(editorGUI);
		dialog.pack();

		if (TestingUtil.INTERACTIVE) {
			dialog.setModal(true);
		}
		
		dialog.setVisible(true);
	}

//	


}
