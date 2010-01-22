//package skrueger.creator.internal;
//
//import java.awt.Component;
//import java.io.File;
//
//import skrueger.atlas.AVProps;
//import skrueger.atlas.gui.internal.AtlasStatusDialog;
//import skrueger.atlas.gui.internal.AtlasTask;
//import skrueger.creator.AMLExporter;
//import skrueger.creator.AtlasConfigEditable;
//
///**
// * The {@link SaveAtlasTask} extends {@link AtlasTask} and uses the
// * {@link AtlasStatusDialog} from there.
// * 
// */
//public class SaveAtlasTask extends AtlasTask<Boolean> {
//
//	private AtlasConfigEditable ace;
//
//	public SaveAtlasTask(Component parentGUI, String startText,
//			AtlasConfigEditable ace) {
//		super(parentGUI, startText);
//		this.ace = ace;
//	}
//
//	@Override
//	protected Boolean doInBackground() throws Exception {
//		AMLExporter amlExporter = new AMLExporter(ace);
//
//		if (amlExporter.saveAtlasConfigEditable(progressWindow)) {
//			AVProps.save(new File(ace.getAtlasDir(),
//					AVProps.PROPERTIESFILE_RESOURCE_NAME));
//
//			new File(ace.getAtlasDir(), AtlasConfigEditable.ATLAS_GPA_FILENAME)
//					.createNewFile();
//			
//			return true;
//		}
//		return false;
//	}
//
//	@Override
//	protected void done() {
//		progressWindow.complete();
//	}
//
//}
