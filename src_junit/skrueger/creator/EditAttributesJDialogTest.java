package skrueger.creator;

import junit.framework.TestCase;

import org.opengis.feature.type.Name;

import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.dp.layer.DpLayerRasterPyramid;
import skrueger.atlas.dp.layer.DpLayerVectorFeatureSource;

public class EditAttributesJDialogTest extends TestCase {

	DpLayerRasterPyramid pyr;
	private DpLayerVectorFeatureSource dplv;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		AtlasConfigEditable atlasConfigE = TestingUtil.getAtlasConfigE();
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
		
		dialog.setModal(true);
		while (dialog.isVisible()) {
			Thread.sleep(100);
		}
		
//		dplv.getAttributeMetaDataMap().get(aname).getTitle().put("de", "bbb");
//		dplv.getAttributeMetaDataMap().get(aname).setVisible(!backupVisibility);
//
//		assertEquals(backupSize, dplv.getAttributeMetaDataMap().size());
//
//		String expected = dplv.getAttributeMetaDataMap().get(aname).getTitle().get("de");
//		
//		assertEquals(
//				"Cancel doesn't work, because the Translation has not been reset",
//				expected,
//				"aaa");
//		assertEquals(
//				"Cancel doesn't work, because the visibility has not been reset",
//				dplv.getAttributeMetaDataMap().get(aname).isVisible(),
//				backupVisibility);
	}

//	public void testOkClose() {
//		if (!TestingUtil.INTERACTIVE)
//			return;
//		
//		Name aname = dplv.getSchema().getAttributeDescriptors().get(1).getName();
//
//		EditAttributesJDialog dialog = GPDialogManager.dm_EditAttribute
//				.getInstanceFor(dplv, null, dplv);
//		dplv.getAttributeMetaDataMap().get(aname).getTitle().put("de",
//				"sadasd{asdas");
//		assertFalse("okClose didn't work because it didn't see the {", dialog
//				.okClose());
//	}

}
