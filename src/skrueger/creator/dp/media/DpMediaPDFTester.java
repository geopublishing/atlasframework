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
package skrueger.creator.dp.media;

import java.awt.Component;
import java.io.File;

import org.apache.log4j.Logger;

import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.dp.media.DpMediaPDF;
import skrueger.atlas.exceptions.AtlasImportException;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.dp.DpEntryTesterInterface;

public class DpMediaPDFTester implements DpEntryTesterInterface {
	static private final Logger LOGGER = Logger
			.getLogger(DpMediaPDFTester.class);

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
