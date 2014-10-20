package org.geopublishing.atlasViewer.swing;

import java.awt.Component;

import org.geopublishing.atlasViewer.GpCoreUtil;

import de.schmitzm.geotools.styling.StyledFeaturesInterface;
import de.schmitzm.swing.AtlasDialog;
import de.schmitzm.swing.SwingUtil;

public class ShapefileEditorJDialog extends AtlasDialog {

    private Component owner;
    private StyledFeaturesInterface<?> styledObj;
    /**
     * If a table will contain more than that many cells, the user will be
     * warned
     **/
    public static final int WARN_CELLS = 15000;

    public ShapefileEditorJDialog(Component owner, final StyledFeaturesInterface<?> styledObj) {
	super(owner, GpCoreUtil.R("ShapefileEditor.dialog.title", styledObj.getTitle()));

	this.owner = owner;
	this.styledObj = styledObj;
	this.setSize(200, 200);
	this.setModal(true);
	this.setVisible(true);

	// make a check on howmany feateurs we have an print a warning if too
	// many
	int numCells = styledObj.getFeatureCollectionFiltered().size()
		* styledObj.getAttributeMetaDataMap().sortedValuesVisibleOnly().size();
	if (numCells > WARN_CELLS) {
	    if (SwingUtil.askYesNo(owner,
		    SwingUtil.R("AttributeTable.dialog.warnTooManyCells", numCells)) == false) {
		dispose();
		return;
	    }
	}
    }

}
