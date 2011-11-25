package org.geopublishing.atlasViewer.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.GpCoreUtil;

public class PopupHTMLItemAction extends AbstractAction {
    
    private final AtlasViewerGUI atlasViewer;
    private final Component owner;

    public PopupHTMLItemAction(AtlasViewerGUI atlasViewer) {
        super(GpCoreUtil.R("AtlasViewer.FileMenu.openPopupHtml"));
        this.atlasViewer = atlasViewer;
        this.owner = atlasViewer.getJFrame();
    }

    public static Logger LOGGER = Logger.getLogger(PopupHTMLItemAction.class);
    
    @Override
    public void actionPerformed(ActionEvent arg0) {
        AtlasPopupDialog aboutWindow = new AtlasPopupDialog(owner,
                atlasViewer.getAtlasConfig());

    }

}
