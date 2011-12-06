package org.geopublishing.atlasViewer.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.GpCoreUtil;

public class TermsOfUseHTMLItemAction extends AbstractAction {
    
    private final AtlasViewerGUI atlasViewer;
    private final Component owner;

    public TermsOfUseHTMLItemAction(AtlasViewerGUI atlasViewer) {
        super(GpCoreUtil.R("AtlasViewer.FileMenu.openTermsOfUseHtml"));
        this.atlasViewer = atlasViewer;
        this.owner = atlasViewer.getJFrame();
    }

    public static Logger LOGGER = Logger.getLogger(TermsOfUseHTMLItemAction.class);
    
    @Override
    public void actionPerformed(ActionEvent arg0) {
        AtlasTermsOfUseDialog aboutWindow = new AtlasTermsOfUseDialog(owner,
                atlasViewer.getAtlasConfig());

    }

}