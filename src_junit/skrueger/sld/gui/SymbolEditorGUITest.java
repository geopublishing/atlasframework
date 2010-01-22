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
