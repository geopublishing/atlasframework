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
package skrueger.atlas.dp.media;

import java.awt.Component;

import org.apache.log4j.Logger;

import skrueger.atlas.AVUtil;
import skrueger.atlas.AtlasConfig;
import skrueger.atlas.dp.DpEntryType;

public class DpMediaPDF extends DpMedia {
	static private final Logger LOGGER = Logger.getLogger(DpMediaPDF.class);

	public DpMediaPDF(AtlasConfig ac) {
		super(ac);
		setType(DpEntryType.PDF	);
	}
//	
//	@Override
//	public DpMediaPDF copy() {
//		return copyTo(new DpMediaPDF(ac));
//	}
//	
//	@Override
//	public DpMediaPDF copyTo(Object t) {
//		throw new RuntimeException("not implemented yet");
//	}

	/**
	 * Tries to open a PDF viewer of the host system.
	 */
	@Override
	public Object show(Component owner) {
		Exception error = AVUtil.launchPDFViewer(owner, getUrl(owner), getTitle().toString());
		
		if (error != null) {
			setBrokenException(error);
		}

		return error;
	}


	@Override
	public void exportWithGUI(Component owner) {
		// TODO exportWithGUI PDF
		throw new RuntimeException("Sorry, export of PDF with GUI is not yet implemented.");
	}

}
