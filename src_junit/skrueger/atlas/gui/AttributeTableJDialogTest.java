package skrueger.atlas.gui;

import junit.framework.TestCase;

import org.geotools.map.MapLayer;

import skrueger.atlas.AVDialogManager;
import skrueger.atlas.dp.layer.DpLayerVectorFeatureSource;
import skrueger.atlas.gui.map.AtlasMapLegend;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.TestingUtil;

public class AttributeTableJDialogTest extends TestCase {

	private AtlasConfigEditable ace;

	protected void setUp() throws Exception {
		super.setUp();
		
		ace = TestingUtil.getAtlasConfigE();
	}
	
	public void testOpenTwice_WithoutJPane() {
		
		DpLayerVectorFeatureSource cities = TestingUtil.getCities(ace);
		
		AttributeTableJDialog first = AVDialogManager.dm_AttributeTable.getInstanceFor(cities, null, cities, null, null );
		
		assertTrue("The dialog is not created as visibile!", first.isVisible());

		AttributeTableJDialog second = AVDialogManager.dm_AttributeTable.getInstanceFor(cities, null, cities, null, null );
		
		assertEquals("The cache returned the same instacne of the dialog", first, second);
		
		AVDialogManager.dm_AttributeTable.disposeAll();
	}

	public void testOpenTwice_WithJPane() {
		
		DpLayerVectorFeatureSource cities = TestingUtil.getCities(ace);
		
		AtlasMapLegend aml = TestingUtil.getAtlasMapLegend(ace);
		
		assertTrue( aml.insertStyledLayer(cities, 0) );
		MapLayer layer = aml.getGeoMapPane().getMapContext().getLayer(0);
		AttributeTableJDialog first = AVDialogManager.dm_AttributeTable.getInstanceFor(cities, null, cities,  aml );
		
		assertTrue("The dialog is not created as visibile!", first.isVisible());
		
		AttributeTableJDialog second = AVDialogManager.dm_AttributeTable.getInstanceFor(cities, null, cities, null );
		
		assertEquals("The cache returned the same instacne of the dialog", first, second);
		
		
	}

}
