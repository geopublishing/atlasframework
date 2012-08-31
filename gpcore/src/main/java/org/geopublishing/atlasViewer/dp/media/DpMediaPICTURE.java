/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasViewer.dp.media;

import java.awt.Component;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.dp.DpEntryType;
import org.geopublishing.atlasViewer.http.AtlasProtocol;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;

import de.schmitzm.jfree.chart.style.ChartStyle;

public class DpMediaPICTURE extends DpMedia<ChartStyle> {
    static private final Logger LOGGER = Logger.getLogger(DpMediaPDF.class);

    /**
     * All suffixes that will be accepted as PICTURE
     */
    public static final String[] POSSIBLESUFFIXES = { ".gif", ".jpg", ".jpeg", ".png" };

    public DpMediaPICTURE(AtlasConfig ac) {
	super(ac);
	setType(DpEntryType.PICTURE);
    }

    @Override
    public Object show(Component owner) {
	Exception error = AVSwingUtil.showImageAsHtmlPopup(owner, AVSwingUtil.getUrl(this, owner),
		ac);

	if (error != null) {
	    setBrokenException(error);
	}

	return error;
    }

    @Override
    public void exportWithGUI(Component owner) throws IOException {
	LOGGER.info("not implemented"); // TODO
    }

    @Override
    public String getInternalLink(String lang) {
	return "<a href=\"" + AtlasProtocol.PICTURE.toString().toLowerCase() + "://" + getId()
		+ "\">" + getTitle().get(lang) + "</a>";
    }

    @Override
    public String getInternalLink() {
	return "<a href=\"" + AtlasProtocol.PICTURE.toString().toLowerCase() + "://" + getId()
		+ "\">" + getTitle().toString() + "</a>";
    }
}
