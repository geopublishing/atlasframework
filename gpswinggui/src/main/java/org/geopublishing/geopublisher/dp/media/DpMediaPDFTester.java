/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.dp.media;

import java.awt.Component;
import java.io.File;

import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.DpEntryType;
import org.geopublishing.atlasViewer.dp.media.DpMediaPDF;
import org.geopublishing.atlasViewer.exceptions.AtlasImportException;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.dp.DpEntryTesterInterface;


public class DpMediaPDFTester implements DpEntryTesterInterface {
	static private final Logger LOGGER = Logger
			.getLogger(DpMediaPDFTester.class);
	
	public static final FileFilter FILEFILTER = new FileFilter() {

		@Override
		public String getDescription() {
			return DpEntryType.PDF.getDesc();
		}

		@Override
		public boolean accept(File f) {
			return f.isDirectory() || new DpMediaPDFTester().test(null, f);
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * skrueger.creator.gui.datapool.DpEntryTesterInterface#create(skrueger.
	 * creator.AtlasConfigEditable, java.io.File)
	 */
	@Override
	public DpEntry create(AtlasConfigEditable ace, File file, Component owner)
			throws AtlasImportException {
		DpMediaPDF datapoolMediaPDF;
		datapoolMediaPDF = new DpMediaPDFEd(ace, file, owner);
		return datapoolMediaPDF;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * skrueger.creator.gui.datapool.DpEntryTesterInterface#test(java.awt.Frame,
	 * java.io.File)
	 */
	@Override
	public boolean test(Component owner, File file) {
		if (file.getName().toLowerCase().endsWith(".pdf")) {
			return true;
		}
		return false;
	}
}
